/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.model;

import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.jmcs.data.preference.SessionSettingsPreferences;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.service.RecentFilesManager;
import fr.jmmc.jmcs.util.FileUtils;
import fr.jmmc.jmcs.util.jaxb.JAXBFactory;
import fr.jmmc.jmcs.util.jaxb.JAXBUtils;
import fr.jmmc.jmcs.util.jaxb.XmlBindException;
import fr.jmmc.oiexplorer.core.model.event.EventNotifier;
import fr.jmmc.oitools.image.FitsImageFile;
import fr.jmmc.oitools.image.FitsImageLoader;
import fr.jmmc.oitools.meta.OIFitsStandard;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.nom.tam.fits.FitsException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumMap;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: remove StatusBar / MessagePane (UI)
/**
 * Handle the model for image reconstruction orchestration.
 * @author mella, bourgesl
 */
public final class IRModelManager {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(IRModelManager.class);
    /** package name for JAXB generated code */
    //private final static String IRMODEL_JAXB_PATH = IRModel.class.getPackage().getName();
    /** Singleton pattern */
    private final static IRModelManager instance = new IRModelManager();
    /* members */
    /** internal JAXB Factory */
    private final JAXBFactory jf;
    /** flag to enable/disable firing events during startup (before calling start) */
    private boolean enableEvents = false;

    /** associated file to the model */
    private File irModelFile = null;
    /** IR Model (session)*/
    private IRModel irModel = null;
    /* event dispatchers */
    /** IRModelEventType event notifier map */
    private final EnumMap<IRModelEventType, EventNotifier<IRModelEvent, IRModelEventType, Object>> irModelManagerEventNotifierMap;

    /**
     * Return the Manager singleton
     * @return singleton instance
     */
    public static IRModelManager getInstance() {
        return instance;
    }

    /**
     * Prevent instanciation of singleton.
     * Manager instance should be obtained using getInstance().
     */
    private IRModelManager() {
        super();

//        this.jf = JAXBFactory.getInstance(IRMODEL_JAXB_PATH);
        this.jf = null; // TODO

        logger.debug("IRModelManager: JAXBFactory: {}", this.jf);

        this.irModelManagerEventNotifierMap = new EnumMap<IRModelEventType, EventNotifier<IRModelEvent, IRModelEventType, Object>>(IRModelEventType.class);

        int priority = 0;
        EventNotifier<IRModelEvent, IRModelEventType, Object> eventNotifier;

        for (IRModelEventType eventType : IRModelEventType.values()) {
            eventNotifier = new EventNotifier<IRModelEvent, IRModelEventType, Object>(eventType.name(), priority, false);
            this.irModelManagerEventNotifierMap.put(eventType, eventNotifier);
            priority += 10;
        }

        // reset anyway:
        reset();
    }

    /* --- data of model handling ------------------------------------- */
    /**
     * Load the IR Model at given URL
     * @param file model file to load
     * @param checker optional OIFits checker instance (may be null)
     * @param listener progress listener
     * @throws IOException if an I/O exception occurred
     * @throws IllegalStateException if an unexpected exception occurred
     * @throws XmlBindException if a JAXBException was caught while creating an unmarshaller
     */
    public void loadIRModel(final File file, final OIFitsChecker checker,
                            final LoadIRModelListener listener) throws IOException, IllegalStateException, XmlBindException {
        loadIRModel(file, checker, listener, false);
    }

    /**
     * Load the IR Model at given URL or onl the include OIFits file references.
     * @param file OIFits explorer collection file file to load
     * @param checker optional OIFits checker instance (may be null)
     * @param listener progress listener
     * @param appendOIFitsFilesOnly load only OIFits and skip plot+subset if true, else reset and load whole collection content
     * @throws IOException if an I/O exception occurred
     * @throws IllegalStateException if an unexpected exception occurred
     * @throws XmlBindException if a JAXBException was caught while creating an unmarshaller
     */
    public void loadIRModel(final File file, final OIFitsChecker checker,
                            final LoadIRModelListener listener, final boolean appendOIFitsFilesOnly) throws IOException, IllegalStateException, XmlBindException {

        final IRModel loadedModel = (IRModel) JAXBUtils.loadObject(file.toURI().toURL(), this.jf);

        // TODO
        // OIDataCollectionFileProcessor.onLoad(loadedUserCollection);
    }

    private void postLoadIRModel(final File file, final IRModel irModel, final OIFitsChecker checker) {

        // after loadIRModel as it calls reset():
        setIRModelFile(file);

        // add given file to Open recent menu
        RecentFilesManager.addFile(file);

        throw new IllegalStateException("TODO");
    }

    /**
     * Load the IR Model at given URL
     * @param file model file to load
     * @throws IOException if an I/O exception occurred
     * @throws IllegalStateException if an unexpected exception occurred
     */
    public void saveIRModel(final File file) throws IOException, IllegalStateException {
        final long startTime = System.nanoTime();

        final IRModel saved = getIRModel();

//        OIDataCollectionFileProcessor.onSave(savedUserCollection);
        // TODO: may also save OIFits file copies into zip archive (xml + OIFits files) ??
        JAXBUtils.saveObject(file, saved, this.jf);

        setIRModelFile(file);

        logger.info("saveIRModel: duration = {} ms.", 1e-6d * (System.nanoTime() - startTime));

        // add given file to Open recent menu
        RecentFilesManager.addFile(file);
    }

    public void loadOIFitsFile(File file) throws IOException {
        loadOIFitsFile(file.getAbsolutePath(), null);
    }

    /**
     * Load the given OI Fits File with the given checker component
     * and add it to the IR Model
     * @param fileLocation absolute File Path or remote URL
     * @param checker checker component
     * @throws IOException if a fits file can not be loaded
     */
    public void loadOIFitsFile(final String fileLocation, final OIFitsChecker checker) throws IOException {
        loadOIFitsFile(loadOIFits(fileLocation, checker));
    }

    /**
     * (Download) and load the given OI Fits File with the given checker component
     * @param fileLocation absolute File Path or remote URL
     * @param checker checker component
     * @return loaded OIFits File
     * @throws IOException if a fits file can not be loaded
     */
    private static OIFitsFile loadOIFits(final String fileLocation, final OIFitsChecker checker) throws IOException {
        //@todo test if file has already been loaded before going further ??

        final OIFitsFile oifitsFile;
        try {
            // retrieve oifits if remote or use local one
            if (FileUtils.isRemote(fileLocation)) {
                // TODO let the user customize the application file storage preference:
                final String parentPath = SessionSettingsPreferences.getApplicationFileStorage();

                final File localCopy = FileUtils.retrieveRemoteFile(fileLocation, parentPath, MimeType.OIFITS);

                if (localCopy != null) {
                    // TODO: remove StatusBar !
                    StatusBar.show("loading file: " + fileLocation + " ( local copy: " + localCopy.getAbsolutePath() + " )");

                    oifitsFile = OIFitsLoader.loadOIFits(OIFitsStandard.VERSION_1, checker, localCopy.getAbsolutePath());
                    oifitsFile.setSourceURI(new URI(fileLocation));
                } else {
                    // download failed:
                    oifitsFile = null;
                }
            } else {
                // TODO: remove StatusBar !
                StatusBar.show("loading file: " + fileLocation);

                oifitsFile = OIFitsLoader.loadOIFits(OIFitsStandard.VERSION_1, checker, fileLocation);
            }
        } catch (AuthenticationException ae) {
            throw new IOException("Could not load the file : " + fileLocation, ae);
        } catch (IOException ioe) {
            throw new IOException("Could not load the file : " + fileLocation, ioe);
        } catch (FitsException fe) {
            throw new IOException("Could not load the file : " + fileLocation, fe);
        } catch (URISyntaxException use) {
            throw new IOException("Could not load the file : " + fileLocation, use);
        }

        if (oifitsFile == null) {
            throw new IOException("Could not load the file : " + fileLocation);
        }

        return oifitsFile;
    }

    /**
     * Load OIDate from given OIFitsFile
     * @param oiFitsFile OIFits structure
     * @return true if an OIDataFile was added
     */
    public boolean loadOIFitsFile(final OIFitsFile oiFitsFile) {
        if (oiFitsFile != null) {
            irModel.loadOifitsFile(oiFitsFile);
            fireIRModelUpdated(this, null);
            return true;

        }
        return false;
    }

    public void loadFitsImageFile(File file) throws IOException {
        loadFitsImageFile(file.getAbsolutePath());
    }

    /**
     * Load the given OI Fits File with the given checker component
     * and add it to the IR Model
     * @param fileLocation absolute File Path or remote URL
     * @throws IOException if a fits file can not be loaded
     */
    public void loadFitsImageFile(final String fileLocation) throws IOException {
        addFitsImageFile(loadFitsImage(fileLocation));
    }

    /**
     * (Download) and load the given OI Fits File with the given checker component
     * @param fileLocation absolute File Path or remote URL
     * @return loaded FitsImage File
     * @throws IOException if a fits file can not be loaded
     */
    private static FitsImageFile loadFitsImage(final String fileLocation) throws IOException {
        //@todo test if file has already been loaded before going further ??

        final FitsImageFile fitsImageFile;
        try {
            // retrieve oifits if remote or use local one
            if (FileUtils.isRemote(fileLocation)) {
                // TODO let the user customize the application file storage preference:
                final String parentPath = SessionSettingsPreferences.getApplicationFileStorage();

                final File localCopy = FileUtils.retrieveRemoteFile(fileLocation, parentPath, MimeType.OIFITS);

                if (localCopy != null) {
                    // TODO: remove StatusBar !
                    StatusBar.show("loading file: " + fileLocation + " ( local copy: " + localCopy.getAbsolutePath() + " )");

                    fitsImageFile = FitsImageLoader.load(localCopy.getAbsolutePath(), true, true);
                } else {
                    // download failed:
                    fitsImageFile = null;
                }
            } else {
                // TODO: remove StatusBar !
                StatusBar.show("loading file: " + fileLocation);

                fitsImageFile = FitsImageLoader.load(fileLocation, true, true);
            }
        } catch (AuthenticationException ae) {
            throw new IOException("Could not load the file : " + fileLocation, ae);
        } catch (IOException ioe) {
            throw new IOException("Could not load the file : " + fileLocation, ioe);
        } catch (URISyntaxException use) {
            throw new IOException("Could not load the file : " + fileLocation, use);
        } catch (FitsException fe) {
            throw new IOException("Could not load the file : " + fileLocation, fe);
        } catch (IllegalArgumentException iae) {
            throw iae;
        }

        if (fitsImageFile == null) {
            throw new IOException("Could not load the file : " + fileLocation);
        }

        return fitsImageFile;
    }

    /**
     * Add an FitsImageFile given its corresponding FitsImage structure
     * @param fitsImageFile FitsImage file
     * @return true if an FitsImageFile was added
     */
    public boolean addFitsImageFile(final FitsImageFile fitsImageFile) {
        if (fitsImageFile != null) {
            irModel.addFitsImageFile(fitsImageFile);
            fireIRModelChanged();
            return true;
        }
        return false;
    }

    /**
     * Return the current model file
     * @return the current model file or null if undefined
     */
    public File getIRModelFile() {
        return this.irModelFile;
    }

    /**
     * Private : define the current model file
     * @param file new model file to use
     */
    private void setIRModelFile(final File file) {
        this.irModelFile = file;
    }

    /**
     * Reset the reference model and start firing events
     */
    public void start() {
        enableEvents = true;
        reset();
    }

    /**
     * Reset the reference model.
     */
    public void reset() {
        irModel = new IRModel();
        setIRModelFile(null);

        fireIRModelChanged();
    }

    /**
     * Remove the OIDataFile given its corresponding OIFits structure (filePath matching)
     * @param oiFitsFile OIFits structure
     * @return removed OIDataFile or null if not found
     */
    public OIFitsFile removeOIFitsFile(final OIFitsFile oiFitsFile) {
        final OIFitsFile previous = null;

        return previous;
    }

    /**
     * Protected: return the IR Model
     * // TODO try to make method private back and replace by event handling
     * @return IR Model
     */
    public IRModel getIRModel() {
        return irModel;
    }

    // --- EVENTS ----------------------------------------------------------------
    /**
     * Unbind the given listener to ANY event
     * @param listener listener to unbind
     */
    public void unbind(final IRModelEventListener listener) {
        for (final EventNotifier<IRModelEvent, IRModelEventType, Object> eventNotifier : this.irModelManagerEventNotifierMap.values()) {
            eventNotifier.unregister(listener);
        }
    }

    /**
     * Bind the given listener to IRMODEL_CHANGED event and fire such event to initialize the listener properly
     * @param listener listener to bind
     */
    public void bindIRModelChangedEvent(final IRModelEventListener listener) {
        getIRModelChangedEventNotifier().register(listener);

        // Note: no fire IRMODEL_CHANGED event because first call to reset() fires it (at the right time i.e. not too early):
        // force fire IRMODEL_CHANGED event to initialize the listener ASAP:
        fireIRModelChanged(null, listener);
    }

    /**
     * Return the IRMODEL_CHANGED event notifier
     * @return IRMODEL_CHANGED event notifier
     */
    private EventNotifier<IRModelEvent, IRModelEventType, Object> getIRModelChangedEventNotifier() {
        return this.irModelManagerEventNotifierMap.get(IRModelEventType.IRMODEL_CHANGED);
    }

    /**
     * Return the READY event notifier
     * @return READY event notifier
     */
    public EventNotifier<IRModelEvent, IRModelEventType, Object> getReadyEventNotifier() {
        return this.irModelManagerEventNotifierMap.get(IRModelEventType.READY);
    }

    /**
     * This fires an IRMODEL_CHANGED event to given registered listener ASYNCHRONOUSLY !
     *
     * Note: this is ONLY useful to initialize new registered listeners properly !
     *
     * @param source event source
     * @param destination destination listener (null means all)
     */
    public void fireIRModelChanged(final Object source, final IRModelEventListener destination) {
        if (enableEvents) {
            if (logger.isDebugEnabled()) {
                logger.debug("fireIRModelChanged TO {}", (destination != null) ? destination : "ALL");
            }
            getIRModelChangedEventNotifier().queueEvent((source != null) ? source : this,
                    new IRModelEvent(IRModelEventType.IRMODEL_CHANGED, null, getIRModel()), destination);
        }
    }

    /**
     * This fires an IRMODEL_UPDATED event to given registered listener ASYNCHRONOUSLY !
     *     *
     * @param source event source
     * @param destination destination listener (null means all)
     */
    public void fireIRModelUpdated(final Object source, final IRModelEventListener destination) {
        if (enableEvents) {
            if (logger.isDebugEnabled()) {
                logger.debug("fireIRModelUpdated TO {}", (destination != null) ? destination : "ALL");
            }
            getIRModelChangedEventNotifier().queueEvent((source != null) ? source : this,
                    new IRModelEvent(IRModelEventType.IRMODEL_UPDATED, null, getIRModel()), destination);
        }
    }

    /**
     * This fires an IRMODEL_CHANGED event to given registered listeners ASYNCHRONOUSLY !
     */
    private void fireIRModelChanged() {
        fireIRModelChanged(this, null);
    }

    /**
     * This fires a READY event to given registered listener ASYNCHRONOUSLY !
     * @param source event source
     * @param destination destination listener (null means all)
     */
    public void fireReady(final Object source, final IRModelEventListener destination) {
        if (enableEvents) {
            if (logger.isDebugEnabled()) {
                logger.debug("fireReady TO {}", (destination != null) ? destination : "ALL");
            }
            getReadyEventNotifier().queueEvent((source != null) ? source : this,
                    new IRModelEvent(IRModelEventType.READY, null, getIRModel()), destination);
        }
    }
}
