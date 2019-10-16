/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model;

import fr.jmmc.oiexplorer.core.model.oi.OIDataFile;
import fr.jmmc.oiexplorer.core.model.oi.OiDataCollection;
import fr.jmmc.oiexplorer.core.model.util.OIExplorerModelVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles OiExplorer model conversion between model revisions
 * @author bourgesl, mella
 */
public final class OIDataCollectionFileProcessor {

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(OIDataCollectionFileProcessor.class.getName());

    /**
     * Forbidden constructor
     */
    private OIDataCollectionFileProcessor() {
        super();
    }

    /**
     * Perform the onLoad event : check the schema version and convert if needed
     * @param collection oifits collection to process
     */
    public static void onLoad(final OiDataCollection collection) {
        logger.debug("onLoad: {}", collection);

        // Check version :
        final float schemaVersion = collection.getSchemaVersion();

        logger.debug("SchemaVersion = {}", schemaVersion);

        final OIExplorerModelVersion revision = OIExplorerModelVersion.valueOf(schemaVersion);

        logger.debug("revision = {}", revision);

        // convert ?
        if (revision != OIExplorerModelVersion.getCurrentVersion()) {
            // model conversion is needed :
            convertModel(collection, revision);
        }

        // TODO check and update references
        //collection.checkReferences();
    }

    /**
     * Perform the onSave event : set the schema version
     * @param collection collection to process
     */
    public static void onSave(final OiDataCollection collection) {
        logger.debug("onSave: {}", collection);

        collection.setSchemaVersion(OIExplorerModelVersion.getCurrentVersion().getVersion());

        // TODO check and update references :
        //collection.checkReferences();
    }

    /**
     * Convert the collection to the latest model
     * @param collection collection to convert
     * @param revision collection revision
     */
    private static void convertModel(final OiDataCollection collection, final OIExplorerModelVersion revision) {

        if (revision.getVersion() < OIExplorerModelVersion.April2013.getVersion()) {
            logger.info("convert collection model from {} to {}", revision, OIExplorerModelVersion.getCurrentVersion());

            // update model to April 2013 version:
            // force to generate identifiers (was name previously) for input data 
            // but remove all other material because jaxb can't resolve references
            // data will be loaded but old plot will not be restored
            for (OIDataFile identifiable : collection.getFiles()) {
                if (identifiable.getId() == null) {
                    identifiable.setId(identifiable.getName());
                    logger.debug("add ID for '{}'", identifiable);
                }
            }
            collection.getPlotDefinitions().clear();
            collection.getSubsetDefinitions().clear();
            collection.getPlots().clear();

            logger.debug("convertModel done : {}", revision);
        }
    }
}
