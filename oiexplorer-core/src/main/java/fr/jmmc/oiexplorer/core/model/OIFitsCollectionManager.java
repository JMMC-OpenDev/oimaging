/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model;

import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.jmcs.data.preference.SessionSettingsPreferences;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.gui.task.HttpTaskSwingWorker;
import fr.jmmc.jmcs.gui.task.TaskSwingWorkerExecutor;
import fr.jmmc.jmcs.service.RecentFilesManager;
import fr.jmmc.jmcs.util.FileUtils;
import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.jmcs.util.StringUtils;
import fr.jmmc.jmcs.util.jaxb.JAXBFactory;
import fr.jmmc.jmcs.util.jaxb.JAXBUtils;
import fr.jmmc.jmcs.util.jaxb.XmlBindException;
import fr.jmmc.oiexplorer.core.gui.OIExplorerTaskRegistry;
import fr.jmmc.oiexplorer.core.model.event.EventNotifier;
import fr.jmmc.oiexplorer.core.model.oi.Identifiable;
import fr.jmmc.oiexplorer.core.model.oi.OIDataFile;
import fr.jmmc.oiexplorer.core.model.oi.OiDataCollection;
import fr.jmmc.oiexplorer.core.model.oi.Plot;
import fr.jmmc.oiexplorer.core.model.oi.SubsetDefinition;
import fr.jmmc.oiexplorer.core.model.oi.TableUID;
import fr.jmmc.oiexplorer.core.model.plot.PlotDefinition;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.nom.tam.fits.FitsException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: remove StatusBar / MessagePane (UI)
/**
 * Handle the oifits files collection.
 * @author mella, bourgesl
 */
public final class OIFitsCollectionManager implements OIFitsCollectionManagerEventListener {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(OIFitsCollectionManager.class);
    /** package name for JAXB generated code */
    private final static String OIFITS_EXPLORER_MODEL_JAXB_PATH = OiDataCollection.class.getPackage().getName();
    /** Current key for SubsetDefinition */
    public final static String CURRENT_SUBSET_DEFINITION = "SUBSET_0";
    /** Current key for PlotDefinition */
    public final static String CURRENT_PLOT_DEFINITION = "PLOT_DEF_0";
    /** Current key for View */
    public final static String CURRENT_VIEW = "VIEW_0";
    /** Singleton pattern */
    private final static OIFitsCollectionManager instance = new OIFitsCollectionManager();
    /** Plot Definition factory singleton */
    private final static PlotDefinitionFactory plotDefFactory = PlotDefinitionFactory.getInstance();
    /* members */
    /** internal JAXB Factory */
    private final JAXBFactory jf;
    /** flag to enable/disable firing events during startup (before calling start) */
    private boolean enableEvents = false;
    /** OIFits explorer collection structure */
    private OiDataCollection userCollection = null;
    /** associated file to the OIFits explorer collection */
    private File oiFitsCollectionFile = null;
    /** OIFits collection */
    private OIFitsCollection oiFitsCollection = null;
    /* event dispatchers */
    /** OIFitsCollectionManagerEventType event notifier map */
    private final EnumMap<OIFitsCollectionManagerEventType, EventNotifier<OIFitsCollectionManagerEvent, OIFitsCollectionManagerEventType, Object>> oiFitsCollectionManagerEventNotifierMap;

    /**
     * Return the Manager singleton
     * @return singleton instance
     */
    public static OIFitsCollectionManager getInstance() {
        return instance;
    }

    /**
     * Prevent instanciation of singleton.
     * Manager instance should be obtained using getInstance().
     */
    private OIFitsCollectionManager() {
        super();

        this.jf = JAXBFactory.getInstance(OIFITS_EXPLORER_MODEL_JAXB_PATH);

        logger.debug("OIFitsCollectionManager: JAXBFactory: {}", this.jf);

        this.oiFitsCollectionManagerEventNotifierMap = new EnumMap<OIFitsCollectionManagerEventType, EventNotifier<OIFitsCollectionManagerEvent, OIFitsCollectionManagerEventType, Object>>(OIFitsCollectionManagerEventType.class);

        int priority = 0;
        EventNotifier<OIFitsCollectionManagerEvent, OIFitsCollectionManagerEventType, Object> eventNotifier;

        for (OIFitsCollectionManagerEventType eventType : OIFitsCollectionManagerEventType.values()) {
            // false argument means allow self notification:
            final boolean skipSourceListener = (eventType != OIFitsCollectionManagerEventType.COLLECTION_CHANGED);

            eventNotifier = new EventNotifier<OIFitsCollectionManagerEvent, OIFitsCollectionManagerEventType, Object>(eventType.name(), priority, skipSourceListener);

            this.oiFitsCollectionManagerEventNotifierMap.put(eventType, eventNotifier);
            priority += 10;
        }

        // listen for COLLECTION_CHANGED event to analyze collection and fire initial events:
        getOiFitsCollectionChangedEventNotifier().register(this);

        // reset anyway:
        reset();
    }

    /**
     * Free any resource or reference to this instance :
     * throw an IllegalStateException as it is invalid
     */
    @Override
    public void dispose() {
        if (logger.isDebugEnabled()) {
            logger.debug("dispose: {}", ObjectUtils.getObjectInfo(this));
        }

        throw new IllegalStateException("Using OIFitsCollectionManager.dispose() is invalid !");
    }

    /* --- OIFits file collection handling ------------------------------------- */
    /**
     * Load the OIFits collection at given URL
     * @param file OIFits explorer collection file file to load
     * @param checker optional OIFits checker instance (may be null)
     * @param listener progress listener
     * @throws IOException if an I/O exception occurred
     * @throws IllegalStateException if an unexpected exception occurred
     * @throws XmlBindException if a JAXBException was caught while creating an unmarshaller
     */
    public void loadOIFitsCollection(final File file, final OIFitsChecker checker,
            final LoadOIFitsListener listener) throws IOException, IllegalStateException, XmlBindException {

        final OiDataCollection loadedUserCollection = (OiDataCollection) JAXBUtils.loadObject(file.toURI().toURL(), this.jf);

        OIDataCollectionFileProcessor.onLoad(loadedUserCollection);

        loadOIDataCollection(file, loadedUserCollection, checker, listener);
    }

    private void postLoadOIFitsCollection(final File file, final OiDataCollection oiDataCollection, final OIFitsChecker checker) {

        // TODO: check missing files !
        // TODO what about user plot definitions ...
        // add them but should be check for consistency related to loaded files (errors can occur while loading):
        // then add SubsetDefinition:
        for (SubsetDefinition subsetDefinition : oiDataCollection.getSubsetDefinitions()) {
            // fix OIDataFile reference:
            for (TableUID tableUID : subsetDefinition.getTables()) {
                tableUID.setFile(getOIDataFile(tableUID.getFile().getId()));
                // if missing, remove ?
            }
            addSubsetDefinitionRef(subsetDefinition);
        }

        // then add PlotDefinition:
        for (PlotDefinition plotDefinition : oiDataCollection.getPlotDefinitions()) {
            addPlotDefinitionRef(plotDefinition);
        }

        // TODO: check subset and plot definition references in Plot ?
        // then add Plot:
        for (Plot plot : oiDataCollection.getPlots()) {
            this.addPlotRef(plot);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("subsetDefinitions {}", getSubsetDefinitionList());
        }

        // after loadOIDataCollection as it calls reset():
        setOiFitsCollectionFile(file);

        // add given file to Open recent menu
        RecentFilesManager.addFile(file);
    }

    /**
     * Load the OIFits collection at given URL
     * @param file OIFits explorer collection file file to load
     * @throws IOException if an I/O exception occurred
     * @throws IllegalStateException if an unexpected exception occurred
     */
    public void saveOIFitsCollection(final File file) throws IOException, IllegalStateException {
        final long startTime = System.nanoTime();

        final OiDataCollection savedUserCollection = getUserCollection();

        OIDataCollectionFileProcessor.onSave(savedUserCollection);

        // TODO: may also save OIFits file copies into zip archive (xml + OIFits files) ??
        JAXBUtils.saveObject(file, savedUserCollection, this.jf);

        setOiFitsCollectionFile(file);

        logger.info("saveOIFitsCollection: duration = {} ms.", 1e-6d * (System.nanoTime() - startTime));

        // add given file to Open recent menu
        RecentFilesManager.addFile(file);
    }

    /**
     * Load OIFits files from the loaded OIDataCollection file using an async LoadOIFits task
     * @param file loaded OIFits explorer collection file
     * @param oiDataCollection OiDataCollection to look for
     * @param checker to report validation information
     * @param listener progress listener
     */
    private void loadOIDataCollection(final File file, final OiDataCollection oiDataCollection, final OIFitsChecker checker,
            final LoadOIFitsListener listener) {

        final List<OIDataFile> oidataFiles = oiDataCollection.getFiles();
        final List<String> fileLocations = new ArrayList<String>(oidataFiles.size());
        for (OIDataFile oidataFile : oidataFiles) {
            fileLocations.add(oidataFile.getFile());
        }

        new LoadOIFitsFilesSwingWorker(fileLocations, checker, listener) {
            /**
             * Refresh GUI invoked by the Swing Event Dispatcher Thread (Swing EDT)
             * Called by @see #done()
             * @param oifitsFiles computed data
             */
            @Override
            public void refreshUI(final List<OIFitsFile> oifitsFiles) {
                // first reset:
                reset();

                // add OIFits files to collection = fire OIFitsCollectionChanged:
                super.refreshUI(oifitsFiles);

                postLoadOIFitsCollection(file, oiDataCollection, checker);

                listener.done(false);
            }

            @Override
            public void refreshNoData(final boolean cancelled) {
                listener.done(cancelled);
            }

        }.executeTask();
    }

    /**
     * Load the given OI Fits Files with the given checker component using an async LoadOIFits task
     * and add it to the OIFits collection
     * @param files files to load
     * @param checker checker component
     * @param listener progress listener
     */
    public void loadOIFitsFiles(final File[] files, final OIFitsChecker checker, final LoadOIFitsListener listener) {
        if (files != null) {
            final List<String> fileLocations = new ArrayList<String>(files.length);
            for (File file : files) {
                fileLocations.add(file.getAbsolutePath());
            }

            new LoadOIFitsFilesSwingWorker(fileLocations, checker, listener) {
                /**
                 * Refresh GUI invoked by the Swing Event Dispatcher Thread (Swing EDT)
                 * Called by @see #done()
                 * @param oifitsFiles computed data
                 */
                @Override
                public void refreshUI(final List<OIFitsFile> oifitsFiles) {
                    // add OIFits files to collection = fire OIFitsCollectionChanged:
                    super.refreshUI(oifitsFiles);

                    listener.done(false);
                }

                @Override
                public void refreshNoData(final boolean cancelled) {
                    listener.done(cancelled);
                }

            }.executeTask();
        }
    }

    /**
     * Cancel any running LoadOIFits task
     */
    public static void cancelTaskLoadOIFits() {
        // cancel any running task:
        TaskSwingWorkerExecutor.cancelTask(OIExplorerTaskRegistry.TASK_LOAD_OIFITS);
    }

    /**
     * TaskSwingWorker child class to download and load OIFits files in background
     * @author bourgesl
     */
    class LoadOIFitsFilesSwingWorker extends HttpTaskSwingWorker<List<OIFitsFile>> {

        private final List<String> fileLocations;
        private final OIFitsChecker checker;

        LoadOIFitsFilesSwingWorker(final List<String> fileLocations, final OIFitsChecker checker,
                final LoadOIFitsListener listener) {
            super(OIExplorerTaskRegistry.TASK_LOAD_OIFITS);
            this.fileLocations = fileLocations;
            this.checker = checker;
            this.addPropertyChangeListener(listener);
        }

        @Override
        public List<OIFitsFile> computeInBackground() {
            final int size = fileLocations.size();

            final List<OIFitsFile> oiFitsFiles = new ArrayList<OIFitsFile>(size);

            final long startTime = System.nanoTime();

            int n = 0;
            for (int i = 0; i < size; i++) {
                // fast interrupt :
                if (Thread.currentThread().isInterrupted()) {
                    // Update status bar:
                    StatusBar.show("Loading file(s) cancelled.");
                    return null;
                }

                final String fileLocation = fileLocations.get(i);
                try {
                    oiFitsFiles.add(loadOIFits(fileLocation, checker));
                    n++;
                } catch (IOException ioe) {
                    logger.info("Error reading file: {}", fileLocation, ioe.getCause());
                    // Append to checker report:
                    checker.severe("Could not load the file : " + fileLocation);
                    // Update status bar:
                    StatusBar.show("Could not load the file : " + fileLocation);
                }
                // publish progress:
                setProgress(Math.round((100f * i) / size));
            }

            logger.info("loadOIFitsFiles: duration = {} ms.", 1e-6d * (System.nanoTime() - startTime));

            // Update status bar:
            StatusBar.show(n + " loaded file(s).");

            return oiFitsFiles;
        }

        /**
         * Refresh GUI invoked by the Swing Event Dispatcher Thread (Swing EDT)
         * Called by @see #done()
         * @param oifitsFiles computed data
         */
        @Override
        public void refreshUI(final List<OIFitsFile> oifitsFiles) {
            for (OIFitsFile oifitsFile : oifitsFiles) {
                // fire OIFitsCollectionChanged:
                addOIFitsFile(oifitsFile);
            }
        }
    }

    /**
     * Load the given OI Fits File with the given checker component
     * and add it to the OIFits collection
     * @param fileLocation absolute File Path or remote URL
     * @param checker checker component
     * @throws IOException if a fits file can not be loaded
     */
    public void loadOIFitsFile(final String fileLocation, final OIFitsChecker checker) throws IOException {
        cancelTaskLoadOIFits();
        addOIFitsFile(loadOIFits(fileLocation, checker));
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

                    oifitsFile = OIFitsLoader.loadOIFits(checker, localCopy.getAbsolutePath());
                    oifitsFile.setSourceURI(new URI(fileLocation));
                } else {
                    // download failed:
                    oifitsFile = null;
                }
            } else {
                // TODO: remove StatusBar !
                StatusBar.show("loading file: " + fileLocation);

                oifitsFile = OIFitsLoader.loadOIFits(checker, fileLocation);
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
     * Return the current OIFits explorer collection file
     * @return the current OIFits explorer collection file or null if undefined
     */
    public File getOiFitsCollectionFile() {
        return this.oiFitsCollectionFile;
    }

    /**
     * Private : define the current OIFits explorer collection file
     * @param file new OIFits explorer collection file to use
     */
    private void setOiFitsCollectionFile(final File file) {
        this.oiFitsCollectionFile = file;
    }

    // TODO: save / merge ... (elsewhere)
    /**
     * Reset the OIFits file collection and start firing events
     */
    public void start() {
        enableEvents = true;
        reset();
    }

    /**
     * Reset the OIFits file collection
     */
    public void reset() {
        cancelTaskLoadOIFits();

        userCollection = new OiDataCollection();
        oiFitsCollection = new OIFitsCollection();

        setOiFitsCollectionFile(null);

        fireOIFitsCollectionChanged();
    }

    /**
     * Remove all OIDataFiles
     */
    public void removeAllOIFitsFiles() {
        this.oiFitsCollection.clear();

        getOIDataFileList().clear();

        fireOIFitsCollectionChanged();
    }

    /**
     * Add an OIDataFile given its corresponding OIFits structure
     * @param oiFitsFile OIFits structure
     * @return true if an OIDataFile was added
     */
    public boolean addOIFitsFile(final OIFitsFile oiFitsFile) {
        if (oiFitsFile != null) {

            // check if already present in collection:
            if (oiFitsCollection.addOIFitsFile(oiFitsFile) == null) {

                // Add new OIDataFile in collection
                final OIDataFile dataFile = new OIDataFile();

                final String id = StringUtils.replaceNonAlphaNumericCharsByUnderscore(oiFitsFile.getName());

                // TODO: make it unique !!
                dataFile.setId(id);

                dataFile.setName(oiFitsFile.getName());

                dataFile.setFile(oiFitsFile.getAbsoluteFilePath());
                // checksum !

                // store oiFitsFile reference:
                dataFile.setOIFitsFile(oiFitsFile);

                addOIDataFileRef(dataFile);

                fireOIFitsCollectionChanged();

                return true;
            }
        }
        return false;
    }

    /**
     * Remove the OIDataFile given its corresponding OIFits structure (filePath matching)
     * @param oiFitsFile OIFits structure
     * @return removed OIDataFile or null if not found
     */
    public OIFitsFile removeOIFitsFile(final OIFitsFile oiFitsFile) {
        final OIFitsFile previous = this.oiFitsCollection.removeOIFitsFile(oiFitsFile);

        if (previous != null) {
            // Remove OiDataFile from user collection
            final String filePath = oiFitsFile.getAbsoluteFilePath();

            for (final Iterator<OIDataFile> it = getOIDataFileList().iterator(); it.hasNext();) {
                final OIDataFile dataFile = it.next();
                if (filePath.equals(dataFile.getFile())) {
                    it.remove();
                }
            }

            fireOIFitsCollectionChanged();
        }

        return previous;
    }

    /**
     * Protected: Return the OIFits explorer collection structure
     * @return OIFits explorer collection structure
     */
    OiDataCollection getUserCollection() {
        return userCollection;
    }

    /**
     * Protected: return the OIFits collection
     * // TODO try to make method private back and replace by event handling for datatreepanel update ( see MainPanel.updateDataTree() )
     * @return OIFits collection
     */
    public OIFitsCollection getOIFitsCollection() {
        return oiFitsCollection;
    }

    /* --- file handling ------------------------------------- */
    /**
     * Return the OIDataFile list (reference)
     * @return OIDataFile list (reference)
     */
    List<OIDataFile> getOIDataFileList() {
        return this.userCollection.getFiles();
    }

    /**
     * Return an OIDataFile given its identifier
     * @param id OIDataFile identifier
     * @return OIDataFile or null if not found
     */
    public OIDataFile getOIDataFile(final String id) {
        return Identifiable.getIdentifiable(id, getOIDataFileList());
    }

    /**
     * Return an OIDataFile given its related OIFitsFile
     * @param oiFitsFile OIFitsFile to find
     * @return OIDataFile or null if not found
     */
    public OIDataFile getOIDataFile(final OIFitsFile oiFitsFile) {
        for (OIDataFile dataFile : getOIDataFileList()) {
            if (oiFitsFile == dataFile.getOIFitsFile()) {
                return dataFile;
            }
        }
        return null;
    }

    /**
     * Add the given OIDataFile
     * @param dataFile OIDataFile to add
     * @return true if the given OIDataFile was added
     */
    private boolean addOIDataFileRef(final OIDataFile dataFile) {
        if (logger.isDebugEnabled()) {
            logger.debug("addOIDataFileRef: {}", dataFile);
        }
        return Identifiable.addIdentifiable(dataFile, getOIDataFileList());
    }

    /**
     * Remove the OIDataFile given its identifier
     * @param id OIDataFile identifier
     * @return removed OIDataFile instance or null if the identifier was not found
     */
    private OIDataFile removeOIDataFile(final String id) {
        return Identifiable.removeIdentifiable(id, getOIDataFileList());
    }

    /* --- subset definition handling ------------------------------------- */
    /**
     * Return the subset definition list (reference)
     * @return subset definition list (reference)
     */
    List<SubsetDefinition> getSubsetDefinitionList() {
        return this.userCollection.getSubsetDefinitions();
    }

    /**
     * Return the current subset definition (copy)
     * @return subset definition (copy)
     */
    public SubsetDefinition getCurrentSubsetDefinition() {
        final SubsetDefinition subsetDefinition = Identifiable.clone(getCurrentSubsetDefinitionRef());

        if (logger.isDebugEnabled()) {
            logger.debug("getCurrentSubsetDefinition {}", subsetDefinition);
        }
        return subsetDefinition;
    }

    /**
     * Return the current subset definition (reference)
     * @return subset definition (reference)
     */
    public SubsetDefinition getCurrentSubsetDefinitionRef() {
        SubsetDefinition subsetDefinition = getSubsetDefinitionRef(CURRENT_SUBSET_DEFINITION);
        if (subsetDefinition == null) {
            subsetDefinition = new SubsetDefinition();
            subsetDefinition.setId(CURRENT_SUBSET_DEFINITION);

            addSubsetDefinitionRef(subsetDefinition);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("getCurrentSubsetDefinitionRef {}", subsetDefinition);
        }
        return subsetDefinition;
    }

    /**
     * Add the given SubsetDefinition
     * @param subsetDefinition SubsetDefinition to add
     * @return true if the given SubsetDefinition was added
     */
    public boolean addSubsetDefinition(final SubsetDefinition subsetDefinition) {
        if (logger.isDebugEnabled()) {
            logger.debug("addSubsetDefinition: {}", subsetDefinition);
        }

        if (addSubsetDefinitionRef(subsetDefinition)) {
            // update subset reference and fire events (SubsetDefinitionChanged, PlotChanged):
            updateSubsetDefinitionRef(this, subsetDefinition);
            return true;
        }
        return false;
    }

    /**
     * Add the given SubsetDefinition
     * @param subsetDefinition SubsetDefinition to add
     * @return true if the given SubsetDefinition was added
     */
    private boolean addSubsetDefinitionRef(final SubsetDefinition subsetDefinition) {
        if (logger.isDebugEnabled()) {
            logger.debug("addSubsetDefinitionRef: {}", subsetDefinition);
        }
        if (Identifiable.addIdentifiable(subsetDefinition, getSubsetDefinitionList())) {
            fireSubsetDefinitionListChanged();
            return true;
        }
        return false;
    }

    /**
     * Remove the SubsetDefinition given its identifier
     * @param id SubsetDefinition identifier
     * @return removed SubsetDefinition instance or null if the identifier was not found
     */
    private SubsetDefinition removeSubsetDefinition(final String id) {
        return Identifiable.removeIdentifiable(id, getSubsetDefinitionList());
    }

    /**
     * Return a subset definition (copy) by its identifier
     * @param id subset definition id
     * @return subset definition (copy) or null if not found
     */
    public SubsetDefinition getSubsetDefinition(final String id) {
        final SubsetDefinition subsetDefinition = Identifiable.clone(getSubsetDefinitionRef(id));

        if (logger.isDebugEnabled()) {
            logger.debug("getSubsetDefinition {}", subsetDefinition);
        }
        return subsetDefinition;
    }

    /**
     * Return a subset definition (reference) by its identifier
     * @param id subsetDefinition identifier
     * @return subset definition (reference) or null if not found
     */
    public SubsetDefinition getSubsetDefinitionRef(final String id) {
        return Identifiable.getIdentifiable(id, getSubsetDefinitionList());
    }

    /**
     * Return true if this subset definition exists in this data collection given its identifier
     * @param id subset definition identifier
     * @return true if this subset definition exists in this data collection given its identifier
     */
    public boolean hasSubsetDefinition(final String id) {
        return getSubsetDefinitionRef(id) != null;
    }

    /**
     * Update the subset definition corresponding to the same name
     * @param source event source
     * @param subsetDefinition subset definition with updated values
     */
    public void updateSubsetDefinition(final Object source, final SubsetDefinition subsetDefinition) {
        final SubsetDefinition subset = getSubsetDefinitionRef(subsetDefinition.getId());

        if (subset == null) {
            throw new IllegalStateException("subset not found : " + subsetDefinition);
        }

        boolean changed = false;

        if (subset != subsetDefinition) {
            changed = !ObjectUtils.areEquals(subset, subsetDefinition);
        } else {
            throw new IllegalStateException("equal subset references : " + subset);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("updateSubsetDefinition: {}", subsetDefinition);
            logger.debug("updateSubsetDefinition: changed: {}", changed);
        }

        if (changed) {
            subset.copy(subsetDefinition); // full copy

            // update subset reference and fire events (SubsetDefinitionChanged, PlotChanged):
            updateSubsetDefinitionRef(source, subset);
        }
    }

    /**
     * Update the given subset definition (reference) and fire events
     * @param source event source
     * @param subsetDefinition subset definition (reference)
     */
    private void updateSubsetDefinitionRef(final Object source, final SubsetDefinition subsetDefinition) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateSubsetDefinitionRef: subsetDefinition: {}", subsetDefinition);
        }

        // Get OIFitsFile structure for this target:
        final OIFitsFile oiFitsSubset;

        if (this.oiFitsCollection.isEmpty()) {
            oiFitsSubset = null;
        } else {
            final OIFitsFile dataForTarget = this.oiFitsCollection.getOiFits(subsetDefinition.getTarget());

            if (dataForTarget == null) {
                oiFitsSubset = null;
            } else {
                // apply table selection:
                if (subsetDefinition.getTables().isEmpty()) {
                    oiFitsSubset = dataForTarget;
                } else {
                    oiFitsSubset = new OIFitsFile();

                    for (TableUID table : subsetDefinition.getTables()) {
                        final OIDataFile oiDataFile = table.getFile();
                        final OIFitsFile oiFitsFile = oiDataFile.getOIFitsFile();

                        if (oiFitsFile != null) {
                            final Integer extNb = table.getExtNb();

                            // add all tables:
                            for (OIData oiData : dataForTarget.getOiDataList()) {
                                // file path comparison:
                                if (oiData.getOIFitsFile().equals(oiFitsFile)) {

                                    if (extNb == null || oiData.getExtNb() == extNb.intValue()) {
                                        oiFitsSubset.addOiTable(oiData);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("updateSubsetDefinitionRef: oiFitsSubset: {}", oiFitsSubset);
        }

        subsetDefinition.setOIFitsSubset(oiFitsSubset);
        subsetDefinition.incVersion();

        fireSubsetDefinitionChanged(source, subsetDefinition.getId());

        // find dependencies:
        for (Plot plot : getPlotList()) {
            if (plot.getSubsetDefinition() != null && plot.getSubsetDefinition().getId().equals(subsetDefinition.getId())) {
                // match
                plot.setSubsetDefinition(subsetDefinition);

                // update plot version and fire events (PlotChanged):
                updatePlotRef(source, plot);
            }
        }
    }

    /* --- plot definition handling --------- ---------------------------- */
    /**
     * Return the plot definition list (reference)
     * @return plot definition list (reference)
     */
    List<PlotDefinition> getPlotDefinitionList() {
        return this.userCollection.getPlotDefinitions();
    }

    /**
     * Return the current plot definition (copy)
     * @return plot definition (copy)
     */
    public PlotDefinition getCurrentPlotDefinition() {
        final PlotDefinition plotDefinition = Identifiable.clone(getCurrentPlotDefinitionRef());

        if (logger.isDebugEnabled()) {
            logger.debug("getCurrentPlotDefinition {}", plotDefinition);
        }
        return plotDefinition;
    }

    /**
     * Return the current plot definition (reference)
     * @return plot definition (reference)
     */
    public PlotDefinition getCurrentPlotDefinitionRef() {
        PlotDefinition plotDefinition = getPlotDefinitionRef(CURRENT_PLOT_DEFINITION);
        if (plotDefinition == null) {
            plotDefinition = new PlotDefinition();
            plotDefinition.setId(CURRENT_PLOT_DEFINITION);

            // copy values:
            plotDefinition.copyValues(plotDefFactory.getDefault(PlotDefinitionFactory.PLOT_DEFAULT));

            addPlotDefinitionRef(plotDefinition);
        }
        return plotDefinition;
    }

    /**
     * Add the given PlotDefinition
     * @param plotDefinition PlotDefinition to add
     * @return true if the given PlotDefinition was added
     */
    public boolean addPlotDefinition(final PlotDefinition plotDefinition) {
        if (logger.isDebugEnabled()) {
            logger.debug("addPlotDefinition: {}", plotDefinition);
        }

        if (addPlotDefinitionRef(plotDefinition)) {
            // update plot definition version and fire events (PlotDefinitionChanged, PlotChanged):
            updatePlotDefinitionRef(this, plotDefinition);
            return true;
        }
        return false;
    }

    /**
     * Add the given PlotDefinition
     * @param plotDefinition PlotDefinition to add
     * @return true if the given PlotDefinition was added
     */
    private boolean addPlotDefinitionRef(final PlotDefinition plotDefinition) {
        if (logger.isDebugEnabled()) {
            logger.debug("addPlotDefinitionRef: {}", plotDefinition);
        }
        if (Identifiable.addIdentifiable(plotDefinition, getPlotDefinitionList())) {
            firePlotDefinitionListChanged();
            return true;
        }
        return false;
    }

    /**
     * Remove the PlotDefinition given its identifier
     * @param id PlotDefinition identifier
     * @return removed PlotDefinition instance or null if the identifier was not found
     */
    private PlotDefinition removePlotDefinition(final String id) {
        return Identifiable.removeIdentifiable(id, getPlotDefinitionList());
    }

    /**
     * Return a plot definition (copy) by its identifier
     * @param id plot identifier
     * @return plot definition (copy) or null if not found
     */
    public PlotDefinition getPlotDefinition(final String id) {
        final PlotDefinition plotDefinition = Identifiable.clone(getPlotDefinitionRef(id));

        if (logger.isDebugEnabled()) {
            logger.debug("getPlotDefinition {}", plotDefinition);
        }
        return plotDefinition;
    }

    /**
     * Return a plot definition (reference) by its identifier
     * @param id plot definition identifier
     * @return plot definition (reference) or null if not found
     */
    public PlotDefinition getPlotDefinitionRef(final String id) {
        return Identifiable.getIdentifiable(id, getPlotDefinitionList());
    }

    /**
     * Return true if this plot definition exists in this data collection given its identifier
     * @param id plot definition identifier
     * @return true if this plot definition exists in this data collection given its identifier
     */
    public boolean hasPlotDefinition(final String id) {
        return getPlotDefinitionRef(id) != null;
    }

    /**
     * Update the plot definition corresponding to the same name
     * @param source event source
     * @param plotDefinition plot definition with updated values
     */
    public void updatePlotDefinition(final Object source, final PlotDefinition plotDefinition) {
        final PlotDefinition plotDef = getPlotDefinitionRef(plotDefinition.getId());

        if (plotDef == null) {
            throw new IllegalStateException("plot definition not found : " + plotDefinition);
        }

        boolean changed = false;

        if (plotDef != plotDefinition) {
            changed = !ObjectUtils.areEquals(plotDef, plotDefinition);
        } else {
            throw new IllegalStateException("equal plot definition references : " + plotDef);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("updatePlotDefinition: {}", plotDefinition);
            logger.debug("updatePlotDefinition: changed: {}", changed);
        }

        if (changed) {
            plotDef.copy(plotDefinition); // full copy

            // update plot definition version and fire events (PlotDefinitionChanged, PlotChanged):
            updatePlotDefinitionRef(source, plotDefinition);
        }
    }

    /**
     * Update the given plot definition (reference) and fire events
     * @param source event source
     * @param plotDefinition plot definition (reference)
     */
    private void updatePlotDefinitionRef(final Object source, final PlotDefinition plotDefinition) {
        if (logger.isDebugEnabled()) {
            logger.debug("updatePlotDefinitionRef: plotDefinition: {}", plotDefinition);
        }

        plotDefinition.incVersion();
        firePlotDefinitionChanged(source, plotDefinition.getId());

        // find dependencies:
        for (Plot plot : getPlotList()) {
            if (plot.getPlotDefinition() != null && plot.getPlotDefinition().getId().equals(plotDefinition.getId())) {
                // match
                plot.setPlotDefinition(plotDefinition);

                // update plot version and fire events (PlotChanged):
                updatePlotRef(source, plot);
            }
        }
    }

    /* --- plot handling --------- ---------------------------- */
    /**
     * Return the plot list (reference)
     * @return plot list (reference)
     */
    List<Plot> getPlotList() {
        return this.userCollection.getPlots();
    }

    /**
     * Return the current plot (copy)
     * @return plot (copy)
     */
    public Plot getCurrentPlot() {
        final Plot plot = Identifiable.clone(getCurrentPlotRef());

        if (logger.isDebugEnabled()) {
            logger.debug("getCurrentPlot {}", plot);
        }
        return plot;
    }

    /**
     * Return the current plot (reference)
     * @return plot (reference)
     */
    public Plot getCurrentPlotRef() {
        Plot plot = getPlotRef(CURRENT_VIEW);
        if (plot == null) {
            plot = new Plot();
            plot.setId(CURRENT_VIEW);

            // define current pointers:
            plot.setSubsetDefinition(getCurrentSubsetDefinitionRef());
            plot.setPlotDefinition(getCurrentPlotDefinitionRef());

            addPlotRef(plot);
        }
        return plot;
    }

    /**
     * Add the given Plot
     * @param plot Plot to add
     * @return true if the given Plot was added
     */
    public boolean addPlot(final Plot plot) {
        if (logger.isDebugEnabled()) {
            logger.debug("addPlot: {}", plot);
        }

        if (addPlotRef(plot)) {
            // update plot version and fire events (PlotChanged):
            updatePlotRef(this, plot);
            return true;
        }
        return false;
    }

    /**
     * Add the given Plot
     * @param plot Plot to add
     * @return true if the given Plot was added
     */
    private boolean addPlotRef(final Plot plot) {
        if (logger.isDebugEnabled()) {
            logger.debug("addPlotRef: {}", plot);
        }
        if (Identifiable.addIdentifiable(plot, getPlotList())) {
            firePlotListChanged();
            return true;
        }
        return false;
    }

    /**
     * Remove the Plot given its identifier
     * @param id Plot identifier
     * @return true if the given Plot was removed
     */
    public boolean removePlot(final String id) {
        if (logger.isDebugEnabled()) {
            logger.debug("removePlot: {}", id);
        }
        Plot p = Identifiable.removeIdentifiable(id, getPlotList());
        if (p != null) {
            // try to cleanup associated elements
            // TODO check if some element are shared when available
            // See also ObservationSetting.checkReferences()
            removePlotDefinition(p.getPlotDefinition().getId());
            removeSubsetDefinition(p.getSubsetDefinition().getId());
            firePlotListChanged();
            return true;
        }
        return false;
    }

    /**
     * Return a plot (copy) by its identifier
     * @param id plot identifier
     * @return plot (copy) or null if not found
     */
    public Plot getPlot(final String id) {
        final Plot plot = Identifiable.clone(getPlotRef(id));

        if (logger.isDebugEnabled()) {
            logger.debug("getPlot {}", plot);
        }
        return plot;
    }

    /**
     * Return a plot (reference) by its identifier
     * @param id plot identifier
     * @return plot (reference) or null if not found
     */
    public Plot getPlotRef(final String id) {
        return Identifiable.getIdentifiable(id, getPlotList());
    }

    /**
     * Return true if this plot exists in this data collection given its identifier
     * @param id plot identifier
     * @return true if this plot exists in this data collection given its identifier
     */
    public boolean hasPlot(final String id) {
        return getPlotRef(id) != null;
    }

    /**
     * Update the plot corresponding to the same name
     * @param source event source
     * @param plot plot with updated values
     */
    public void updatePlot(final Object source, final Plot plot) {
        final Plot plotRef = getPlotRef(plot.getId());

        if (plotRef == null) {
            throw new IllegalStateException("plot not found : " + plot);
        }

        boolean changed = false;

        if (plotRef != plot) {
            changed = !ObjectUtils.areEquals(plotRef, plot);
        } else {
            throw new IllegalStateException("equal plot references : " + plotRef);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("updatePlot: {}", plot);
            logger.debug("updatePlot: changed: {}", changed);
        }

        if (changed) {
            plotRef.copy(plot); // full copy

            // update plot version and fire events (PlotChanged):
            updatePlotRef(source, plot);
        }
    }

    /**
     * Update the given plot (reference) and fire events
     * @param source event source
     * @param plot plot (reference)
     */
    private void updatePlotRef(final Object source, final Plot plot) {
        if (logger.isDebugEnabled()) {
            logger.debug("updatePlotRef: plot: {}", plot);
        }

        plot.incVersion();
        firePlotChanged(source, plot.getId());
    }

    // --- EVENTS ----------------------------------------------------------------
    /**
     * Unbind the given listener to ANY event
     * @param listener listener to unbind
     */
    public void unbind(final OIFitsCollectionManagerEventListener listener) {
        for (final EventNotifier<OIFitsCollectionManagerEvent, OIFitsCollectionManagerEventType, Object> eventNotifier : this.oiFitsCollectionManagerEventNotifierMap.values()) {
            eventNotifier.unregister(listener);
        }
    }

    /**
     * Bind the given listener to COLLECTION_CHANGED event and fire such event to initialize the listener properly
     * @param listener listener to bind
     */
    public void bindCollectionChangedEvent(final OIFitsCollectionManagerEventListener listener) {
        getOiFitsCollectionChangedEventNotifier().register(listener);

        // Note: no fire COLLECTION_CHANGED event because first call to reset() fires it (at the right time i.e. not too early):
        // force fire COLLECTION_CHANGED event to initialize the listener ASAP:
        fireOIFitsCollectionChanged(null, listener);
    }

    /**
     * Return the COLLECTION_CHANGED event notifier
     * @return COLLECTION_CHANGED event notifier
     */
    private EventNotifier<OIFitsCollectionManagerEvent, OIFitsCollectionManagerEventType, Object> getOiFitsCollectionChangedEventNotifier() {
        return this.oiFitsCollectionManagerEventNotifierMap.get(OIFitsCollectionManagerEventType.COLLECTION_CHANGED);
    }

    /**
     * Bind the given listener to SUBSET_LIST_CHANGED event and fire such event to initialize the listener properly
     * @param listener listener to bind
     */
    public void bindSubsetDefinitionListChangedEvent(final OIFitsCollectionManagerEventListener listener) {
        getSubsetDefinitionListChangedEventNotifier().register(listener);

        // force fire SUBSET_LIST_CHANGED event to initialize the listener ASAP:
        fireSubsetDefinitionListChanged(null, listener);
    }

    /**
     * Return the SUBSET_LIST_CHANGED event notifier
     * @return SUBSET_LIST_CHANGED event notifier
     */
    private EventNotifier<OIFitsCollectionManagerEvent, OIFitsCollectionManagerEventType, Object> getSubsetDefinitionListChangedEventNotifier() {
        return this.oiFitsCollectionManagerEventNotifierMap.get(OIFitsCollectionManagerEventType.SUBSET_LIST_CHANGED);
    }

    /**
     * Bind the given listener to PLOT_DEFINITION_LIST_CHANGED event and fire such event to initialize the listener properly
     * @param listener listener to bind
     */
    public void bindPlotDefinitionListChangedEvent(final OIFitsCollectionManagerEventListener listener) {
        getPlotDefinitionListChangedEventNotifier().register(listener);

        // force fire PLOT_DEFINITION_LIST_CHANGED event to initialize the listener ASAP:
        firePlotDefinitionListChanged(null, listener);
    }

    /**
     * Return the PLOT_DEFINITION_LIST_CHANGED event notifier
     * @return PLOT_DEFINITION_LIST_CHANGED event notifier
     */
    private EventNotifier<OIFitsCollectionManagerEvent, OIFitsCollectionManagerEventType, Object> getPlotDefinitionListChangedEventNotifier() {
        return this.oiFitsCollectionManagerEventNotifierMap.get(OIFitsCollectionManagerEventType.PLOT_DEFINITION_LIST_CHANGED);
    }

    /**
     * Bind the given listener to PLOT_LIST_CHANGED event and fire such event to initialize the listener properly
     * @param listener listener to bind
     */
    public void bindPlotListChangedEvent(final OIFitsCollectionManagerEventListener listener) {
        getPlotListChangedEventNotifier().register(listener);

        // force fire PLOT_LIST_CHANGED event to initialize the listener with current OIFitsCollection ASAP:
        firePlotListChanged(null, listener);
    }

    /**
     * Return the PLOT_LIST_CHANGED event notifier
     * @return PLOT_LIST_CHANGED event notifier
     */
    private EventNotifier<OIFitsCollectionManagerEvent, OIFitsCollectionManagerEventType, Object> getPlotListChangedEventNotifier() {
        return this.oiFitsCollectionManagerEventNotifierMap.get(OIFitsCollectionManagerEventType.PLOT_LIST_CHANGED);
    }

    /* TODO: use bind instead of eventNotifier directly */
    /**
     * Return the SUBSET_CHANGED event notifier
     * @return SUBSET_CHANGED event notifier
     */
    public EventNotifier<OIFitsCollectionManagerEvent, OIFitsCollectionManagerEventType, Object> getSubsetDefinitionChangedEventNotifier() {
        return this.oiFitsCollectionManagerEventNotifierMap.get(OIFitsCollectionManagerEventType.SUBSET_CHANGED);
    }

    /**
     * Return the PLOT_DEFINITION_CHANGED event notifier
     * @return PLOT_DEFINITION_CHANGED event notifier
     */
    public EventNotifier<OIFitsCollectionManagerEvent, OIFitsCollectionManagerEventType, Object> getPlotDefinitionChangedEventNotifier() {
        return this.oiFitsCollectionManagerEventNotifierMap.get(OIFitsCollectionManagerEventType.PLOT_DEFINITION_CHANGED);
    }

    /**
     * Return the PLOT_CHANGED event notifier
     * @return PLOT_CHANGED event notifier
     */
    public EventNotifier<OIFitsCollectionManagerEvent, OIFitsCollectionManagerEventType, Object> getPlotChangedEventNotifier() {
        return this.oiFitsCollectionManagerEventNotifierMap.get(OIFitsCollectionManagerEventType.PLOT_CHANGED);
    }

    /**
     * Return the ACTIVE_PLOT_CHANGED event notifier
     * @return ACTIVE_PLOT_CHANGED event notifier
     */
    public EventNotifier<OIFitsCollectionManagerEvent, OIFitsCollectionManagerEventType, Object> getActivePlotChangedEventNotifier() {
        return this.oiFitsCollectionManagerEventNotifierMap.get(OIFitsCollectionManagerEventType.ACTIVE_PLOT_CHANGED);
    }

    /**
     * Return the READY event notifier
     * @return READY event notifier
     */
    public EventNotifier<OIFitsCollectionManagerEvent, OIFitsCollectionManagerEventType, Object> getReadyEventNotifier() {
        return this.oiFitsCollectionManagerEventNotifierMap.get(OIFitsCollectionManagerEventType.READY);
    }

    /**
     * This fires an COLLECTION_CHANGED event to given registered listener ASYNCHRONOUSLY !
     *
     * Note: this is ONLY useful to initialize new registered listeners properly !
     *
     * @param source event source
     * @param destination destination listener (null means all)
     */
    public void fireOIFitsCollectionChanged(final Object source, final OIFitsCollectionManagerEventListener destination) {
        if (enableEvents) {
            if (logger.isDebugEnabled()) {
                logger.debug("fireOIFitsCollectionChanged TO {}", (destination != null) ? destination : "ALL");
            }
            getOiFitsCollectionChangedEventNotifier().queueEvent((source != null) ? source : this,
                    new OIFitsCollectionManagerEvent(OIFitsCollectionManagerEventType.COLLECTION_CHANGED, null), destination);
        }
    }

    /**
     * This fires an COLLECTION_CHANGED event to given registered listeners ASYNCHRONOUSLY !
     */
    private void fireOIFitsCollectionChanged() {
        fireOIFitsCollectionChanged(this, null);
    }

    /**
     * This fires a SUBSET_LIST_CHANGED event to given registered listener ASYNCHRONOUSLY !
     *
     * Note: this is ONLY useful to initialize new registered listeners properly !
     *
     * @param source event source
     * @param destination destination listener (null means all)
     */
    public void fireSubsetDefinitionListChanged(final Object source, final OIFitsCollectionManagerEventListener destination) {
        if (enableEvents) {
            if (logger.isDebugEnabled()) {
                logger.debug("fireSubsetDefinitionListChanged");
            }
            getSubsetDefinitionListChangedEventNotifier().queueEvent((source != null) ? source : this,
                    new OIFitsCollectionManagerEvent(OIFitsCollectionManagerEventType.SUBSET_LIST_CHANGED, null), destination);
        }
    }

    /**
     * This fires an SUBSET_LIST_CHANGED event to given registered listeners ASYNCHRONOUSLY !
     */
    private void fireSubsetDefinitionListChanged() {
        fireSubsetDefinitionListChanged(this, null);
    }

    /**
     * This fires a PLOT_DEFINITION_LIST_CHANGED event to given registered listener ASYNCHRONOUSLY !
     *
     * Note: this is ONLY useful to initialize new registered listeners properly !
     *
     * @param source event source
     * @param destination destination listener (null means all)
     */
    public void firePlotDefinitionListChanged(final Object source, final OIFitsCollectionManagerEventListener destination) {
        if (enableEvents) {
            if (logger.isDebugEnabled()) {
                logger.debug("firePlotDefinitionListChanged");
            }
            getPlotDefinitionListChangedEventNotifier().queueEvent((source != null) ? source : this,
                    new OIFitsCollectionManagerEvent(OIFitsCollectionManagerEventType.PLOT_DEFINITION_LIST_CHANGED, null), destination);
        }
    }

    /**
     * This fires a PLOT_DEFINITION_LIST_CHANGED event to all registered listeners ASYNCHRONOUSLY !
     */
    private void firePlotDefinitionListChanged() {
        firePlotDefinitionListChanged(this, null);
    }

    /**
     * This fires a PLOT_LIST_CHANGED event to given registered listener ASYNCHRONOUSLY !
     *
     * Note: this is ONLY useful to initialize new registered listeners properly !
     *
     * @param source event source
     * @param destination destination listener (null means all)
     */
    public void firePlotListChanged(final Object source, final OIFitsCollectionManagerEventListener destination) {
        if (enableEvents) {
            if (logger.isDebugEnabled()) {
                logger.debug("firePlotListChanged");
            }
            getPlotListChangedEventNotifier().queueEvent((source != null) ? source : this,
                    new OIFitsCollectionManagerEvent(OIFitsCollectionManagerEventType.PLOT_LIST_CHANGED, null), destination);
        }
    }

    /**
     * This fires a PLOT_LIST_CHANGED event to all registered listeners ASYNCHRONOUSLY !
     */
    private void firePlotListChanged() {
        firePlotListChanged(this, null);
    }

    /**
     * This fires a SUBSET_CHANGED event to given registered listener ASYNCHRONOUSLY !
     * @param source event source
     * @param subsetId subset definition identifier
     * @param destination destination listener (null means all)
     */
    public void fireSubsetDefinitionChanged(final Object source, final String subsetId, final OIFitsCollectionManagerEventListener destination) {
        if (enableEvents) {
            if (logger.isDebugEnabled()) {
                logger.debug("fireSubsetDefinitionChanged [{}] TO {}", subsetId, (destination != null) ? destination : "ALL");
            }
            getSubsetDefinitionChangedEventNotifier().queueEvent((source != null) ? source : this,
                    new OIFitsCollectionManagerEvent(OIFitsCollectionManagerEventType.SUBSET_CHANGED, subsetId), destination);
        }
    }

    /**
     * This fires a SUBSET_CHANGED event to all registered listeners ASYNCHRONOUSLY !
     * @param source event source
     * @param subsetId subset definition identifier
     */
    private void fireSubsetDefinitionChanged(final Object source, final String subsetId) {
        fireSubsetDefinitionChanged(source, subsetId, null);
    }

    /**
     * This fires a PLOT_DEFINITION_CHANGED event to given registered listener ASYNCHRONOUSLY !
     * @param source event source
     * @param plotDefId plot definition identifier
     * @param destination destination listener (null means all)
     */
    public void firePlotDefinitionChanged(final Object source, final String plotDefId, final OIFitsCollectionManagerEventListener destination) {
        if (enableEvents) {
            if (logger.isDebugEnabled()) {
                logger.debug("firePlotDefinitionChanged [{}] TO {}", plotDefId, (destination != null) ? destination : "ALL");
            }
            getPlotDefinitionChangedEventNotifier().queueEvent((source != null) ? source : this,
                    new OIFitsCollectionManagerEvent(OIFitsCollectionManagerEventType.PLOT_DEFINITION_CHANGED, plotDefId), destination);
        }
    }

    /**
     * This fires a PLOT_DEFINITION_CHANGED event to all registered listeners ASYNCHRONOUSLY !
     * @param source event source
     * @param plotDefId plot definition identifier
     */
    private void firePlotDefinitionChanged(final Object source, final String plotDefId) {
        firePlotDefinitionChanged(source, plotDefId, null);
    }

    /**
     * This fires a PLOT_CHANGED event to given registered listener ASYNCHRONOUSLY !
     * @param source event source
     * @param plotId plot identifier
     * @param destination destination listener (null means all)
     */
    public void firePlotChanged(final Object source, final String plotId, final OIFitsCollectionManagerEventListener destination) {
        if (enableEvents) {
            if (logger.isDebugEnabled()) {
                logger.debug("firePlotChanged [{}] TO {}", plotId, (destination != null) ? destination : "ALL");
            }
            getPlotChangedEventNotifier().queueEvent((source != null) ? source : this,
                    new OIFitsCollectionManagerEvent(OIFitsCollectionManagerEventType.PLOT_CHANGED, plotId), destination);
        }
    }

    /**
     * This fires a PLOT_CHANGED event to all registered listeners ASYNCHRONOUSLY !
     * @param source event source
     * @param plotId plot identifier
     */
    private void firePlotChanged(final Object source, final String plotId) {
        firePlotChanged(source, plotId, null);
    }

    /**
     * This fires a ACTIVE_PLOT_CHANGED event to given registered listener ASYNCHRONOUSLY !
     * @param source event source
     * @param plotId plot identifier
     * @param destination destination listener (null means all)
     */
    public void fireActivePlotChanged(final Object source, final String plotId, final OIFitsCollectionManagerEventListener destination) {
        if (enableEvents) {
            if (logger.isDebugEnabled()) {
                logger.debug("fireActivePlotChanged [{}] TO {}", plotId, (destination != null) ? destination : "ALL");
            }
            getActivePlotChangedEventNotifier().queueEvent((source != null) ? source : this,
                    new OIFitsCollectionManagerEvent(OIFitsCollectionManagerEventType.ACTIVE_PLOT_CHANGED, plotId), destination);
        }
    }

    /**
     * This fires a READY event to given registered listener ASYNCHRONOUSLY !
     * @param source event source
     * @param destination destination listener (null means all)
     */
    public void fireReady(final Object source, final OIFitsCollectionManagerEventListener destination) {
        if (enableEvents) {
            if (logger.isDebugEnabled()) {
                logger.debug("fireReadyChanged TO {}", (destination != null) ? destination : "ALL");
            }
            getReadyEventNotifier().queueEvent((source != null) ? source : this,
                    new OIFitsCollectionManagerEvent(OIFitsCollectionManagerEventType.READY, null), destination);
        }
    }

    /*
     * OIFitsCollectionManagerEventListener implementation
     */
    /**
     * Return the optional subject id i.e. related object id that this listener accepts
     * @param type event type
     * @return subject id (null means accept any event) or DISCARDED_SUBJECT_ID to discard event
     */
    @Override
    public String getSubjectId(final OIFitsCollectionManagerEventType type) {
        // accept all
        return null;
    }

    /**
     * Handle the given OIFits collection event
     * @param event OIFits collection event
     */
    @Override
    public void onProcess(final OIFitsCollectionManagerEvent event) {
        logger.debug("onProcess {}", event);

        switch (event.getType()) {
            case COLLECTION_CHANGED:
                // update collection analysis:
                oiFitsCollection.analyzeCollection();

                // TODO: see if the "GUI" manager decide to create objects itself ?
                // TODO: remove ASAP:
                // initialize current objects: subsetDefinition, plotDefinition, plot if NOT PRESENT:
                getCurrentPlotRef();

                // CASCADE EVENTS:
                // SubsetDefinition:
                for (SubsetDefinition subsetDefinition : getSubsetDefinitionList()) {
                    // force fireSubsetChanged, update plot reference and firePlotChanged:
                    updateSubsetDefinitionRef(this, subsetDefinition);
                }

                // PlotDefinition:
                for (PlotDefinition plotDefinition : getPlotDefinitionList()) {
                    // force PlotDefinitionChanged, update plot reference and firePlotChanged:
                    updatePlotDefinitionRef(this, plotDefinition);
                }

                // Note: no explicit firePlotChanged event fired as done in updateSubsetDefinitionRef and updatePlotDefinitionRef
                break;
            default:
        }
        logger.debug("onProcess {} - done", event);
    }
}
