/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer;

import fr.jmmc.jmcs.App;
import fr.jmmc.jmcs.Bootstrapper;
import fr.jmmc.jmcs.data.app.ApplicationDescription;
import fr.jmmc.jmcs.gui.component.ComponentResizeAdapter;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.gui.task.TaskSwingWorkerExecutor;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.network.interop.SampCapability;
import fr.jmmc.jmcs.network.interop.SampMessageHandler;
import fr.jmmc.jmcs.resource.image.ResourceImage;
import fr.jmmc.jmcs.util.concurrent.ParallelJobExecutor;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.gui.MainPanel;
import fr.jmmc.oiexplorer.gui.action.LoadOIDataCollectionAction;
import fr.jmmc.oiexplorer.gui.action.LoadOIFitsAction;
import fr.jmmc.oiexplorer.gui.action.NewAction;
import fr.jmmc.oiexplorer.gui.action.OIFitsExplorerExportPDFAction;
import fr.jmmc.oiexplorer.gui.action.SaveOIDataCollectionAction;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import javax.swing.JFrame;
import org.astrogrid.samp.Message;
import org.astrogrid.samp.client.SampException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the OIFitsExplorer application
 * @author mella, bourgesl
 */
public final class OIFitsExplorer extends App {

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(OIFitsExplorer.class.getName());

    /* members */
    /** main Panel */
    private MainPanel mainPanel;
    /* Minimal size of main component */
    private static final Dimension INITIAL_DIMENSION = new java.awt.Dimension(1200, 700);  
   

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
     * @param args command line arguments
     */
    public OIFitsExplorer(final String[] args) {
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
        logger.debug("OifitsExplorerGui.init() handler : enter");
        prepareFrame();
        logger.debug("OifitsExplorerGui.init() handler : exit");
    }

    /**
     * Execute application body = make the application frame visible
     */
    @Override
    protected void execute() {
        logger.debug("OifitsExplorerGui.execute() handler called.");

        SwingUtils.invokeLaterEDT(new Runnable() {
            /**
             * Show the application frame using EDT
             */
            @Override
            public void run() {
                logger.debug("OifitsExplorerGui.ready : handler called.");

                // reset OIFitsManager to fire an OIFits collection changed event to all registered listeners:
                OIFitsCollectionManager.getInstance().start();

                getFrame().setVisible(true);
            }
        });
    }

    /**
     * Hook to handle operations before closing application.
     *
     * @return should return true if the application can exit, false otherwise
     * to cancel exit.
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
     * @param frame
     */
    private void prepareFrame() {
        logger.debug("prepareFrame : enter");

        final JFrame frame = new JFrame();

        // initialize the actions :
        registerActions();

        frame.setTitle(ApplicationDescription.getInstance().getProgramName());

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

        // init the main panel :
        final Container container = frame.getContentPane();
        createContent(container);

        // Handle status bar
        container.add(new StatusBar(), BorderLayout.SOUTH);

        StatusBar.show("application started.");
        App.setFrame(frame);
                              
        App.getFrame().setPreferredSize(INITIAL_DIMENSION);
        App.getFrame().pack();

        logger.debug("prepareFrame : exit");
    }

    /**
     * Create the main content i.e. the setting panel
     */
    private void createContent(final Container container) {
        // adds the main panel in scrollPane
        this.mainPanel = new MainPanel();
        this.mainPanel.setName("mainPanel"); // Fest

        container.add(this.mainPanel, BorderLayout.CENTER);
    }

    /**
     * Create the main actions present in the menu bar
     */
    private void registerActions() {
        // File menu :
        new NewAction();
        new LoadOIFitsAction();
        new LoadOIDataCollectionAction();
        new SaveOIDataCollectionAction();
        // export PDF :
        new OIFitsExplorerExportPDFAction();

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
                // bring this application to front and load data
                SwingUtils.invokeLaterEDT(new Runnable() {
                    public void run() {
                        App.showFrameToFront();
                        try {
                            final String url = (String) message.getParam("url");
                            OIFitsCollectionManager.getInstance().loadOIFitsFile(url, null);
                        } catch (IOException ex) {
                            MessagePane.showErrorMessage("Could not load file from samp message : " + message, ex);
                        }
                    }
                });
            }
        };
    }

    /**
     * Return the main panel
     * @return main panel
     */
    public MainPanel getMainPanel() {
        return mainPanel;
    }
}
