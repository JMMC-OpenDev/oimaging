/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging;

import fr.jmmc.jmcs.App;
import fr.jmmc.jmcs.Bootstrapper;
import fr.jmmc.jmcs.data.app.ApplicationDescription;
import fr.jmmc.jmcs.gui.PreferencesView;
import fr.jmmc.jmcs.gui.component.ComponentResizeAdapter;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.gui.task.TaskSwingWorkerExecutor;
import fr.jmmc.jmcs.gui.util.ResourceImage;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.network.http.Http;
import fr.jmmc.jmcs.network.interop.SampCapability;
import fr.jmmc.jmcs.network.interop.SampMessageHandler;
import fr.jmmc.jmcs.util.CommandLineUtils;
import fr.jmmc.jmcs.util.FileUtils;
import fr.jmmc.jmcs.util.ResourceUtils;
import fr.jmmc.jmcs.util.StringUtils;
import fr.jmmc.jmcs.util.concurrent.ParallelJobExecutor;
import fr.jmmc.oiexplorer.core.model.PlotDefinitionFactory;
import fr.jmmc.oimaging.gui.MainPanel;
import fr.jmmc.oimaging.gui.PreferencePanel;
import fr.jmmc.oimaging.gui.ViewerPanel.ProcessImageOperation;
import fr.jmmc.oimaging.gui.action.ContinueAction;
import fr.jmmc.oimaging.gui.action.CreateImageAction;
import fr.jmmc.oimaging.gui.action.DeleteSelectionAction;
import fr.jmmc.oimaging.gui.action.ExportFitsImageAction;
import fr.jmmc.oimaging.gui.action.ExportOIFitsAction;
import fr.jmmc.oimaging.gui.action.LoadFitsImageAction;
import fr.jmmc.oimaging.gui.action.LoadOIFitsAction;
import fr.jmmc.oimaging.gui.action.NewAction;
import fr.jmmc.oimaging.gui.action.ProcessImageAction;
import fr.jmmc.oimaging.gui.action.OIFitsBrowserAction;
import fr.jmmc.oimaging.gui.action.RunAction;
import fr.jmmc.oimaging.gui.action.TableEditorAction;
import fr.jmmc.oimaging.interop.SendFitsAction;
import fr.jmmc.oimaging.interop.SendOIFitsAction;
import fr.jmmc.oimaging.model.IRModelManager;
import fr.jmmc.oitools.image.FitsImageWriter;
import fr.jmmc.oitools.model.DataModel;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import org.astrogrid.samp.Message;
import org.astrogrid.samp.client.SampException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the OImaging application
 *
 * @author mella, bourgesl
 */
public final class OImaging extends App {

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(OImaging.class.getName());

    public final static boolean DEV_MODE = "true".equalsIgnoreCase(System.getProperty("oimaging.devMode", "false"));

    /* members */
    /** main Panel */
    private MainPanel mainPanel;

    /**
     * Main entry point : use swing setup and then launch the application
     * @param args command line arguments
     */
    public static void main(final String[] args) {

        System.setProperty("org.restlet.engine.loggerFacadeClass", "org.restlet.ext.slf4j.Slf4jLoggerFacade");

        // Start application with the command line arguments
        Bootstrapper.launchApp(new OImaging(args));
    }

    /**
     * Return the OImaging singleton
     * @return OImaging singleton
     */
    public static OImaging getInstance() {
        return (OImaging) App.getInstance();
    }

    /**
     * Public constructor with command line arguments
     *
     * @param args command line arguments
     */
    public OImaging(final String[] args) {
        super(args);
    }

    /**
     * Initialize services before the GUI
     *
     * @throws IllegalStateException if the configuration files are not found or IO failure
     * @throws IllegalArgumentException if the load configuration failed
     */
    @Override
    protected void initServices() throws IllegalStateException, IllegalArgumentException {
        // Initialize tasks and the task executor :
        TaskSwingWorkerExecutor.start(2); // 2 threads (1 compute reconstruction and 1 image viewer)

        // Initialize the parallel job executor:
        ParallelJobExecutor.getInstance();

        // Enable OI columns for OIFits datamodel
        DataModel.setOiModelColumnsSupport(true);

        // Early initialisation of the default Plot preset:
        PlotDefinitionFactory.getInstance().setPlotDefault("OIMG_DATA_MODEL/SPATIAL_FREQ");

        // disable update of fitsImage identifiers when writing oiFitsFile
        FitsImageWriter.setUpdateFitsImageIdentifierOnWrite(false);
    }

    /**
     * Initialize application objects
     *
     * @throws RuntimeException if the OifitsExplorerGui initialization failed
     */
    @Override
    protected void setupGui() throws RuntimeException {
        logger.debug("OifitsExplorerGui.setupGui() handler : enter");

        prepareFrame();
        createPreferencesView();

        logger.debug("OifitsExplorerGui.setupGui() handler : exit");
    }

    /**
     * Create the Preferences view
     * @return Preferences view
     */
    public static PreferencesView createPreferencesView() {
        // Retrieve application preferences and attach them to their view
        // (This instance must be instanciated after dependencies)
        final LinkedHashMap<String, JPanel> panels = new LinkedHashMap<String, JPanel>(2);
        panels.put("General settings", new PreferencePanel());

        final PreferencesView preferencesView = new PreferencesView(getFrame(), Preferences.getInstance(), panels);
        preferencesView.init();

        return preferencesView;
    }

    /**
     * Execute application body = make the application frame visible
     */
    @Override
    protected void execute() {

        SwingUtils.invokeLaterEDT(new Runnable() {
            /**
             * Show the application frame using EDT
             */
            @Override
            public void run() {
                logger.debug("OImaging.execute() handler called.");

                // reset IRModelManager to fire an IRMODEL changed event to all registered listeners:
                IRModelManager.getInstance().start();

                // headless mode:
                final JFrame appFrame = App.getExistingFrame();
                if (appFrame != null) {
                    appFrame.setVisible(true);
                }

                // if devMode, load some ServiceResults from home folder .jmmc-devmode
                if (DEV_MODE) {
                    logger.info("OImaging dev mode: enabled !");
                    DevMode.searchAndCraftAllServiceResults();
                }
            }
        });
    }

    /**
     * Hook to handle operations before closing application.
     *
     * @return should return true if the application can exit, false otherwise to cancel exit.
     */
    @Override
    public boolean canBeTerminatedNow() {
        logger.debug("OifitsExplorerGui.finish() handler called.");

        // Can't exit if a job is running
        if (IRModelManager.getInstance().getIRModel().isRunning()) {
            MessagePane.showMessage("A job is running... Please wait for its completion or cancel it before quitting.");
            return false;
        }

        // Ask the user if he wants to save modifications
        //@TODO replace by code when save will be available.
        MessagePane.ConfirmSaveChanges result = MessagePane.ConfirmSaveChanges.Ignore;
        //MessagePane.ConfirmSaveChanges result = MessagePane.showConfirmSaveChangesBeforeClosing();

        // Handle user choice
        switch (result) {
            // If the user clicked the "Save" button, save and exit
            case Save:
                /*
                 if (this.saveAction != null) {
                 return this.saveAction.save();
                 }
                 */
                break;

            // If the user clicked the "Don't Save" button, exit
            case Ignore:
                break;

            // If the user clicked the "Cancel" button or pressed 'esc' key, don't exit
            case Cancel:
            default: // Any other case
                return false;
        }

        return true;
    }

    /**
     * Hook to handle operations when exiting application.
     *
     * @see App#exit(int)
     */
    @Override
    public void cleanup() {
        // dispose GUI:
        if (this.mainPanel != null) {
            this.mainPanel.dispose();
        }
    }

    /**
     * Prepare the frame widgets and define its minimum size
     */
    private void prepareFrame() {
        logger.debug("prepareFrame : enter");

        // initialize the actions :
        registerActions();
        final Container container;

        if (Bootstrapper.isHeadless()) {
            container = null;
        } else {
            final JFrame frame = new JFrame(ApplicationDescription.getInstance().getProgramName());

            // handle frame icon
            final Image jmmcFavImage = ResourceImage.JMMC_FAVICON.icon().getImage();
            frame.setIconImage(jmmcFavImage);

            // get screen size to adjust minimum window size :
            final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            logger.info("screen size = {} x {}", screenSize.getWidth(), screenSize.getHeight());
            // hack for screens smaller than 1024x768 screens:
            final Dimension dim = new Dimension(950, 700);
            frame.setMinimumSize(dim);
            frame.addComponentListener(new ComponentResizeAdapter(dim));
            frame.setPreferredSize(dim);

            App.setFrame(frame);

            container = frame.getContentPane();
        }
        // init the main panel:
        createContent(container);

        StatusBar.show("application started.");

        logger.debug("prepareFrame : exit");
    }

    /**
     * Create the main content i.e. the main panel
     *
     * @param container frame's content pane
     */
    private void createContent(final Container container) {
        this.mainPanel = new MainPanel();
        this.mainPanel.setName("mainPanel"); // Fest

        if (container != null) {
            // adds the main panel
            container.add(this.mainPanel, BorderLayout.CENTER);

            // Handle status bar
            container.add(StatusBar.getInstance(), BorderLayout.SOUTH);
        }
    }

    /**
     * Create the main actions present in the menu bar
     */
    private void registerActions() {
        // File menu :
        new NewAction();
        new LoadOIFitsAction();
        new LoadFitsImageAction();

        new ExportOIFitsAction();
        new ExportFitsImageAction();

        // Edit menu :
        new DeleteSelectionAction();
        new OIFitsBrowserAction();

        // Processing menu :
        new RunAction();
        new CreateImageAction();
        
        for (ProcessImageOperation op : ProcessImageOperation.values()) {
            new ProcessImageAction(op);
        }

        // Interop menu :
        // Send OIFits (SAMP) :
        new SendOIFitsAction();
        // Send Fits (SAMP) :
        new SendFitsAction();

        // accessible indirectly from the menu by Preferences Panel:
        new TableEditorAction();

        new ContinueAction();
    }

    /**
     * Create SAMP Message handlers
     */
    @Override
    protected void declareInteroperability() {

        // Add handler to load one new oifits
        new SampMessageHandler(SampCapability.LOAD_FITS_TABLE) {
            @Override
            protected void processMessage(final String senderId, final Message message) throws SampException {
                final String url = (String) message.getParam("url");

                if (!StringUtils.isEmpty(url)) {
                    // bring this application to front and load data
                    SwingUtils.invokeLaterEDT(new Runnable() {
                        @Override
                        public void run() {
                            App.showFrameToFront();

                            Exception e = null; // Store exception if something bad occurs                            
                            try {

                                if (FileUtils.isRemote(url)) {
                                    final URI uri = new URI(url);
                                    File tmpFile = FileUtils.getTempFile(ResourceUtils.filenameFromResourcePath(url));
                                    if (Http.download(uri, tmpFile, false)) {
                                        IRModelManager.getInstance().loadOIFitsFile(tmpFile);
                                    } else {
                                        e = new IOException();
                                    }

                                } else {
                                    IRModelManager.getInstance().loadOIFitsFile(new File(new URI(url)));
                                }
                            } catch (IllegalArgumentException ex) {
                                e = ex;
                            } catch (URISyntaxException ex) {
                                e = ex;
                            } catch (IOException ex) {
                                e = ex;
                            }

                            if (e != null) {
                                MessagePane.showErrorMessage("Could not load oifits file from samp message : " + message, e);
                            }
                        }
                    });
                }
            }
        };

        // Add handler to load one new oifits
        new SampMessageHandler(SampCapability.LOAD_FITS_IMAGE) {
            @Override
            protected void processMessage(final String senderId, final Message message) throws SampException {
                final String url = (String) message.getParam("url");

                if (!StringUtils.isEmpty(url)) {
                    // bring this application to front and load data
                    SwingUtils.invokeLaterEDT(new Runnable() {
                        @Override
                        public void run() {
                            App.showFrameToFront();

                            Exception e = null; // Store exception if something bad occurs
                            try {

                                if (FileUtils.isRemote(url)) {
                                    final URI uri = new URI(url);
                                    File tmpFile = FileUtils.getTempFile(ResourceUtils.filenameFromResourcePath(url));
                                    if (Http.download(uri, tmpFile, false)) {
                                        IRModelManager.getInstance().loadFitsImageFile(tmpFile);
                                    } else {
                                        e = new IOException();
                                    }

                                } else {
                                    IRModelManager.getInstance().loadFitsImageFile(new File(new URI(url)));
                                }

                            } catch (IllegalArgumentException ex) {
                                e = ex;
                            } catch (URISyntaxException ex) {
                                e = ex;
                            } catch (IOException ex) {
                                e = ex;
                            }

                            if (e != null) {
                                MessagePane.showErrorMessage("Could not load fits image file from samp message : " + message, e);
                            }
                        }
                    });
                }
            }
        };
    }

    /**
     * check the arguments given by the user in TTY mode
     * and begin the exportation in pdf ,png, jpg or all if possible
     * Note: executed by the thread [main]: must block until asynchronous task finishes !
     * @throws IllegalArgumentException if one (or several) argument is missing or invalid
     */
    @Override
    protected void processShellCommandLine() throws IllegalArgumentException {
        final Map<String, String> argValues = getCommandLineArguments();
        logger.debug("processShellCommandLine: {}", argValues);

        // note: open file is NOT done in background ...
        final String fileArgument = argValues.get(CommandLineUtils.CLI_OPEN_KEY);

        // required open file check:
        if (fileArgument == null) {
            throw new IllegalArgumentException("Missing file argument !");
        }
        final File fileOpen = new File(fileArgument);

        // same checks than LoadOIDataCollectionAction:
        if (!fileOpen.exists() || !fileOpen.isFile()) {
            throw new IllegalArgumentException("Could not load the file: " + fileOpen.getAbsolutePath());
        }

        logger.debug("processShellCommandLine: done.");
    }

    /**
     * Return the main panel
     *
     * @return main panel
     */
    public MainPanel getMainPanel() {
        return mainPanel;
    }

    /**
     * Create a generic progress panel (typically shown in overlay)
     *
     * @param message message displayed as tooltip
     * @param progressBar progress bar to use
     * @param cancelListener optional cancel action listener
     * @return new panel
     */
    public static JPanel createProgressPanel(final String message, final JProgressBar progressBar, final ActionListener cancelListener) {
        final JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        progressPanel.setBorder(BorderFactory.createEtchedBorder());
        progressPanel.setToolTipText(message);

        final Dimension dim = new Dimension(80, 18);
        progressBar.setMinimumSize(dim);
        progressBar.setPreferredSize(dim);
        progressBar.setMaximumSize(dim);

        progressBar.setStringPainted(true);
        progressPanel.add(progressBar);

        if (cancelListener != null) {
            final JButton cancelBtn = new JButton("cancel");
            cancelBtn.setMargin(new Insets(0, 2, 0, 2));
            cancelBtn.addActionListener(cancelListener);
            progressPanel.add(cancelBtn);
        }

        return progressPanel;
    }

}
