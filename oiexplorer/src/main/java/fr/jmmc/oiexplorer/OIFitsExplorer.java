/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer;

import fr.jmmc.jmcs.App;
import fr.jmmc.jmcs.Bootstrapper;
import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.jmcs.data.app.ApplicationDescription;
import fr.jmmc.jmcs.gui.component.ComponentResizeAdapter;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.gui.task.TaskSwingWorkerExecutor;
import fr.jmmc.jmcs.gui.util.ResourceImage;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.network.interop.SampCapability;
import fr.jmmc.jmcs.network.interop.SampMessageHandler;
import fr.jmmc.jmcs.util.CommandLineUtils;
import fr.jmmc.jmcs.util.StringUtils;
import fr.jmmc.jmcs.util.concurrent.ParallelJobExecutor;
import fr.jmmc.oiexplorer.core.export.DocumentOptions;
import fr.jmmc.oiexplorer.core.export.ImageOptions;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.gui.MainPanel;
import fr.jmmc.oiexplorer.gui.action.LoadOIDataCollectionAction;
import fr.jmmc.oiexplorer.gui.action.LoadOIFitsAction;
import fr.jmmc.oiexplorer.gui.action.LoadOIFitsFromCollectionAction;
import fr.jmmc.oiexplorer.gui.action.NewAction;
import fr.jmmc.oiexplorer.gui.action.OIFitsExplorerExportAction;
import fr.jmmc.oiexplorer.gui.action.OIFitsExplorerExportAllAction;
import fr.jmmc.oiexplorer.gui.action.SaveOIDataCollectionAction;
import fr.jmmc.oitools.model.OIFitsChecker;
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
 * This class represents the OIFitsExplorer application
 *
 * @author mella, bourgesl
 */
public final class OIFitsExplorer extends App {

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(OIFitsExplorer.class.getName());

    /** pdf addExportListener argument */
    public final static String ARG_PDF = "pdf";
    /** png addExportListener argument */
    public final static String ARG_PNG = "png";
    /** jpg addExportListener argument */
    public final static String ARG_JPG = "jpg";
    /** choosing mode (multi/single page) argument */
    public final static String ARG_MODE = "mode";
    /** choosing dimensions argument */
    public final static String ARG_DIMS = "dims";

    /* Minimal size of main component */
    private static final Dimension INITIAL_DIMENSION = new java.awt.Dimension(1200, 700);

    /* members */
    /** main Panel */
    private MainPanel mainPanel;

    /**
     * Main entry point : use swing setup and then launch the application
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        // Start application with the command line arguments
        Bootstrapper.launchApp(new OIFitsExplorer(args));
    }

    /**
     * Return the OIFitsExplorer singleton
     * @return OIFitsExplorer singleton
     */
    public static OIFitsExplorer getInstance() {
        return (OIFitsExplorer) App.getInstance();
    }

    /**
     * Public constructor with command line arguments
     *
     * @param args command line arguments
     */
    public OIFitsExplorer(final String[] args) {
        super(args);
    }

    /**
     * Add addExportListener PDF/PNG/JPG custom command line argument(s)
     */
    @Override
    protected void defineCustomCommandLineArgumentsAndHelp() {
        addCustomCommandLineArgument(ARG_PDF, true, "export plots to the given file (PDF format)",
                App.ExecMode.TTY);
        addCustomCommandLineArgument(ARG_PNG, true, "export plots to the given file (PNG format)",
                App.ExecMode.TTY);
        addCustomCommandLineArgument(ARG_JPG, true, "export plots to the given file (JPG format)",
                App.ExecMode.TTY);
        addCustomCommandLineArgument(ARG_MODE, true, " export mode [multi|single] page");
        addCustomCommandLineArgument(ARG_DIMS, true, " export image dimensions [width,height]",
                App.ExecMode.TTY);
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
        TaskSwingWorkerExecutor.start();

        // Initialize the parallel job executor:
        ParallelJobExecutor.getInstance();
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
        logger.debug("OifitsExplorerGui.setupGui() handler : exit");
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
                logger.debug("OifitsExplorerGui.execute() handler called.");

                // reset OIFitsManager to fire an OIFits collection changed event to all registered listeners:
                OIFitsCollectionManager.getInstance().start();

                // headless mode:
                final JFrame appFrame = App.getExistingFrame();
                if (appFrame != null) {
                    appFrame.setVisible(true);
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

            if (logger.isInfoEnabled()) {
                logger.info("screen size = {} x {}", screenSize.getWidth(), screenSize.getHeight());
            }

            // hack for screens smaller than 1152x864 screens :
            final int appWidth = 950;
            final int appHeightMin = 700;
            final int appHeightPref = (screenSize.getHeight() >= 864) ? 800 : appHeightMin;

            final Dimension dim = new Dimension(appWidth, appHeightMin);
            frame.setMinimumSize(dim);
            frame.setPreferredSize(new Dimension(appWidth, appHeightPref));
            frame.addComponentListener(new ComponentResizeAdapter(dim));
            frame.setPreferredSize(INITIAL_DIMENSION);

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
        new LoadOIFitsFromCollectionAction();

        new LoadOIDataCollectionAction();
        new SaveOIDataCollectionAction();
        // addExportListener actions:
        new OIFitsExplorerExportAction(MimeType.PDF);
        new OIFitsExplorerExportAction(MimeType.PNG);
        new OIFitsExplorerExportAction(MimeType.JPG);
        // addExportListener all actions:
        new OIFitsExplorerExportAllAction(MimeType.PDF);
        new OIFitsExplorerExportAllAction(MimeType.PNG);
        new OIFitsExplorerExportAllAction(MimeType.JPG);

        // Edit menu :
        // Interop menu :
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

                            final OIFitsChecker checker = new OIFitsChecker();

                            try {
                                final long startTime = System.nanoTime();

                                OIFitsCollectionManager.getInstance().loadOIFitsFile(url, checker);

                                logger.info("LoadSampOIFitsAction: duration = {} ms.", 1e-6d * (System.nanoTime() - startTime));

                            } catch (IOException ioe) {
                                MessagePane.showErrorMessage(ioe.getMessage(), ioe.getCause());
                                StatusBar.show(ioe.getMessage());
                            } finally {
                                // display validation messages anyway:
                                final String checkReport = checker.getCheckReport();
                                logger.info("validation results:\n{}", checkReport);

                                MessagePane.showMessage(checkReport);
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

        final String pdfFile = argValues.get(ARG_PDF);
        final String pngFile = argValues.get(ARG_PNG);
        final String jpgFile = argValues.get(ARG_JPG);
        final String mode = argValues.get(ARG_MODE);
        final String dims = argValues.get(ARG_DIMS);

        try {
            boolean doLoad = false;

            if (pdfFile != null) {
                doLoad = initializeExport(pdfFile, MimeType.PDF, mode, dims);
            }
            if (pngFile != null) {
                doLoad = initializeExport(pngFile, MimeType.PNG, mode, dims);
            }
            if (jpgFile != null) {
                doLoad = initializeExport(jpgFile, MimeType.JPG, mode, dims);
            }
            if (doLoad) {
                ExportUtils.loadDataAndWaitUntilExportDone();
            }

        } catch (IOException ioe) {
            logger.error("IO error:", ioe);
            // ignore
        }
        logger.debug("processShellCommandLine: done.");
    }

    private static boolean initializeExport(final String filePath, final MimeType mimeType,
            final String mode, final String dims) throws IOException {

        if (filePath != null) {
            final DocumentOptions options = DocumentOptions.createInstance(mimeType).setMode(mode);

            if (options instanceof ImageOptions) {
                // specific to images:
                ((ImageOptions) options).setDimensions(dims);
            }

            final File file = new File(filePath).getAbsoluteFile();

            if (!file.getParentFile().canWrite()) {
                throw new IllegalArgumentException("Can not write into: " + file.getParentFile());
            }

            ExportUtils.addExportListener(file, options);
            return true;
        }
        return false;
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
