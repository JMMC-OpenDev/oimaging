/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.gui.action;

import fr.jmmc.jmcs.data.preference.SessionSettingsPreferences;
import fr.jmmc.jmcs.gui.action.ActionRegistrar;
import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.jmcs.gui.component.FileChooser;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oitools.model.OIFitsChecker;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load one (or more) files from xml file.
 * @author mella
 */
public final class LoadOIDataCollectionAction extends RegisteredAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class name. This name is used to register to the ActionRegistrar */
    public final static String className = LoadOIDataCollectionAction.class.getName();
    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String actionName = "loadOIDataCollection";
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(className);
    /** OIFitsExplorer MimeType */
    private final static MimeType mimeType = MimeType.OIFITS_EXPLORER_COLLECTION;

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     */
    public LoadOIDataCollectionAction() {
        super(className, actionName);
        flagAsOpenAction();
    }

    /**
     * Handle the action event
     * @param evt action event
     */
    @Override
    public void actionPerformed(final ActionEvent evt) {
        logger.debug("actionPerformed");

        final OIFitsCollectionManager ocm = OIFitsCollectionManager.getInstance();

        File file;

        // If the action was automatically triggered from App launch
        if (evt.getSource() == ActionRegistrar.getInstance()) {
            file = new File(evt.getActionCommand());

            if (!file.exists() || !file.isFile()) {
                MessagePane.showErrorMessage("Could not load the file : " + file.getAbsolutePath());
                file = null;
            }

            if (file != null) {
                // update current directory for oidata:
                SessionSettingsPreferences.setCurrentDirectoryForMimeType(mimeType, file.getParent());
            }

        } else {

            final File oiFitsCollectionFile = ocm.getOiFitsCollectionFile();

            final String defaultFileName;

            if (oiFitsCollectionFile != null) {
                defaultFileName = oiFitsCollectionFile.getName();
            } else {
                defaultFileName = null;
            }

            file = FileChooser.showOpenFileChooser("Load an OIFits Explorer Collection", null, mimeType, defaultFileName);
        }

        // If a file was defined (No cancel in the dialog)
        if (file != null) {

            final OIFitsChecker checker = new OIFitsChecker();

            final String fileLocation = file.getAbsolutePath();

            StatusBar.show("loading OIFits Explorer Collection: " + fileLocation);

            Exception e = null;
            try {
                ocm.loadOIFitsCollection(file, checker);

            } catch (IllegalStateException ise) {
                e = ise;
            } catch (IOException ioe) {
                e = ioe;
            } finally {
                if (e != null) {
                    StatusBar.show("Could not load OIFits Explorer Collection: " + fileLocation);
                    MessagePane.showErrorMessage("Could not load OIFits Explorer Collection: " + fileLocation, e);
                }

                // log validation messages anyway:
                final String checkReport = checker.getCheckReport();
                logger.info("validation results:\n{}", checkReport);

                // TODO: use a preference to show or hide the validation report:
                if (false) {
                    MessagePane.showMessage(checkReport);
                }
            }
        }
    }
}
