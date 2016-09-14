/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.gui.action;

import fr.jmmc.oimaging.model.IRModelManager;
import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.jmcs.data.preference.SessionSettingsPreferences;
import fr.jmmc.jmcs.gui.action.ActionRegistrar;
import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.jmcs.gui.component.FileChooser;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.StatusBar;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load one (and only one) file.
 * @author mella
 */
public final class LoadFitsImageAction extends RegisteredAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class name. This name is used to register to the ActionRegistrar */
    public final static String className = LoadFitsImageAction.class.getName();
    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String actionName = "loadFitsImage";
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(className);
    /** OIFits MimeType */
    private final static MimeType mimeType = MimeType.FITS_IMAGE;

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     */
    public LoadFitsImageAction() {
        super(className, actionName);
    }

    /**
     * Handle the action event
     * @param evt action event
     */
    @Override
    public void actionPerformed(final ActionEvent evt) {
        logger.debug("actionPerformed");

        File[] files;

        // If the action was automatically triggered from App launch
        if (evt != null && evt.getSource() == ActionRegistrar.getInstance()) {
            File file = new File(evt.getActionCommand());

            if (!file.exists() || !file.isFile()) {
                MessagePane.showErrorMessage("Could not load the file : " + file.getAbsolutePath());
                files = null;
            } else {
                // update current directory for oidata:
                SessionSettingsPreferences.setCurrentDirectoryForMimeType(mimeType, file.getParent());
                files = new File[]{file};
            }

        } else {
            files = FileChooser.showOpenFilesChooser("Load fits image files", null, mimeType);
        }

        // If a file was defined (No cancel in the dialog)
        if (files != null) {
            for (File file : files) {
                try {
                    IRModelManager.getInstance().loadFitsImageFile(file);
                } catch (IOException ex) {
                    StatusBar.show("Could not load Fits Image: " + file.getAbsolutePath());
                    MessagePane.showErrorMessage("Could not load Fits Image: " + file.getAbsolutePath(), ex);
                } catch (IllegalArgumentException iae) {
                    // IllegalArgumentException matches unit conversion or mandatory CDELT keyword test
                    StatusBar.show("Could not load Fits Image: " + file.getAbsolutePath());
                    MessagePane.showErrorMessage("Could not load Fits Image: " + file.getName() + "\n  " + iae.getMessage(), "Could not load file");
                }
            }
        }
    }

}
