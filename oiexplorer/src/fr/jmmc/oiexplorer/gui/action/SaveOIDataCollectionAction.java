/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.gui.action;

import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.jmcs.gui.component.FileChooser;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load one (or more) files from xml file.
 * @author mella
 */
public final class SaveOIDataCollectionAction extends RegisteredAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class name. This name is used to register to the ActionRegistrar */
    public final static String className = SaveOIDataCollectionAction.class.getName();
    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String actionName = "saveOIDataCollection";
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(className);
    /** OIFitsExplorer MimeType */
    private final static MimeType mimeType = MimeType.OIFITS_EXPLORER_COLLECTION;

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     */
    public SaveOIDataCollectionAction() {
        super(className, actionName);
    }

    /**
     * Handle the action event
     * @param evt action event
     */
    @Override
    public void actionPerformed(final ActionEvent evt) {
        logger.debug("actionPerformed");

        final OIFitsCollectionManager ocm = OIFitsCollectionManager.getInstance();

        final File oiFitsCollectionFile = ocm.getOiFitsCollectionFile();

        final String defaultFileName;

        if (oiFitsCollectionFile != null) {
            defaultFileName = oiFitsCollectionFile.getName();
        } else {
            defaultFileName = null;
        }

        File file;

        file = FileChooser.showSaveFileChooser("Save the current OIFits Explorer Collection", null, mimeType, defaultFileName);

        // If a file was defined (No cancel in the dialog)
        if (file != null) {
            final String fileLocation = file.getAbsolutePath();

            Exception e = null;
            try {
                ocm.saveOIFitsCollection(file);

            } catch (IOException ex) {
                e = ex;
            } catch (IllegalStateException ex) {
                e = ex;
            } finally {
                if (e != null) {
                    MessagePane.showErrorMessage("Could not save the file: " + fileLocation, e);
                    StatusBar.show("Could not save the file: " + fileLocation);
                }
            }
        }
    }
}
