/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer;

import fr.jmmc.jmcs.Bootstrapper;
import fr.jmmc.jmcs.util.concurrent.ThreadExecutors;
import fr.jmmc.oiexplorer.core.export.DocumentExportable;
import fr.jmmc.oiexplorer.core.export.DocumentOptions;
import fr.jmmc.oiexplorer.core.gui.action.ExportDocumentAction;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEvent;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventListener;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventType;
import fr.jmmc.oiexplorer.core.model.oi.SubsetDefinition;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is dedicated to addExportListener plots as documents or PNG images ...
 * @author grosje
 */
public final class ExportUtils {

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(ExportUtils.class.getName());
    /** keep listener alive as a static listener list */
    private final static Vector<OIFitsCollectionManagerEventListener> aliveListeners = new Vector<OIFitsCollectionManagerEventListener>();

    /**
     * Private Constructor
     */
    private ExportUtils() {
        // no-op
    }

    /**
     * Save the plots as a document in the given file
     * @param file file to create
     * @param options parameters of the document page
     * @throws IOException if the file exists but is a directory
     *                   rather than a regular file, does not exist but cannot
     *                   be created, or cannot be opened for any other reason
     * @throws IllegalStateException if a document exception occurred
     */
    public static void addExportListener(final File file, final DocumentOptions options)
            throws IOException, IllegalStateException {

        final OIFitsCollectionManager ocm = OIFitsCollectionManager.getInstance();

        final OIFitsCollectionManagerEventListener readyEventListener
                                                   = new ExportWhenReadyListener(file, options);

        // Register READY event listener:
        ocm.getReadyEventNotifier().register(readyEventListener);

        // Keep code alive:
        aliveListeners.add(readyEventListener);
    }

    public static void loadDataAndWaitUntilExportDone() {

        // Load file asynchronously:
        OIFitsExplorer.getInstance().openCommandLineFile();

        // Note: it will call back any registered READY event listeners to export documents ...
        final long lapse = 60000l;
        logger.info("Waiting for {} ms to load files asynchronously and perform exports...", lapse);
        ThreadExecutors.sleep(lapse);
    }

    private static class ExportWhenReadyListener implements OIFitsCollectionManagerEventListener {

        /** OIFitsCollectionManager singleton reference */
        private final static OIFitsCollectionManager ocm = OIFitsCollectionManager.getInstance();
        private final File file;
        private final DocumentOptions options;

        ExportWhenReadyListener(final File file, final DocumentOptions options) {
            this.file = file;
            this.options = options;
        }

        @Override
        public void dispose() {
            ocm.unbind(this);
        }

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
        public void onProcess(OIFitsCollectionManagerEvent event) {
            logger.debug("readyEventListener.onProcess {}", event);
            switch (event.getType()) {
                case READY:
                    doExport();
                    break;
                default:
            }
        }

        private void doExport() {
            boolean noData = false;
            // check that OIFitsCollection (no data) and subsets (bad filter criteria) are not empty      
            if (ocm.getOIFitsCollection().isEmpty()) {
                noData = true;
                logger.error("No loaded data");
            } else {
                noData = true;
                for (String subsetId : ocm.getSubsetDefinitionIds()) {
                    final SubsetDefinition subsetDefinition = ocm.getSubsetDefinitionRef(subsetId);
                    if (subsetDefinition != null) {
                        if (subsetDefinition.getOIFitsSubset() != null) {
                            noData = false;
                        } else {
                            logger.warn("Subset[{}] has no data with filter {}", subsetId, subsetDefinition.getFilter());
                        }
                    }
                }
                if (noData) {
                    logger.error("All Subsets have no data");
                }
            }
            if (noData) {
                Bootstrapper.stopApp(1);
            }

            final DocumentExportable exportable = OIFitsExplorer.getInstance().getMainPanel();

            try {
                ExportDocumentAction.export(exportable, this.file, options);
            } catch (Throwable th) {
                // unexpected errors:
                logger.error("doExport failure:", th);
            } finally {
                // remove this listener from all alive listeners:
                aliveListeners.remove(this);

                // if no more listeners: stop !
                if (aliveListeners.isEmpty()) {
                    Bootstrapper.stopApp(0);
                }
            }
        }
    }

}
