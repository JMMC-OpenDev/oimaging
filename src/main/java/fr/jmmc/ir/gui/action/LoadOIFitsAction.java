/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.ir.gui.action;

import fr.jmmc.ir.model.IRModelManager;
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
public final class LoadOIFitsAction extends RegisteredAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class name. This name is used to register to the ActionRegistrar */
    public final static String className = LoadOIFitsAction.class.getName();
    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String actionName = "loadOIFits";
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(className);
    /** OIFits MimeType */
    private final static MimeType mimeType = MimeType.OIFITS;

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     */
    public LoadOIFitsAction() {
        super(className, actionName);
    }

    /**
     * Handle the action event
     * @param evt action event
     */
    @Override
    public void actionPerformed(final ActionEvent evt) {
        logger.debug("actionPerformed");

        File file;

        // If the action was automatically triggered from App launch
        if (evt.getSource() == ActionRegistrar.getInstance()) {
            file = new File(evt.getActionCommand());

            if (!file.exists() || !file.isFile()) {
                MessagePane.showErrorMessage("Could not load the file : " + file.getAbsolutePath());
                file = null;
            } else {
                // update current directory for oidata:
                SessionSettingsPreferences.setCurrentDirectoryForMimeType(mimeType, file.getParent());
            }

        } else {
            file = FileChooser.showOpenFileChooser("Load oifits file", null, mimeType);
        }

        // If a file was defined (No cancel in the dialog)
        if (file != null) {

            try {
                IRModelManager.getInstance().loadOIFitsFile(file);
            } catch (IOException ex) {
                StatusBar.show("Could not load OIFits : " + file.getAbsolutePath());
                MessagePane.showErrorMessage("Could not load OIFits : " + file.getAbsolutePath(), ex);
            } catch (IllegalArgumentException iae) {
                // IllegalArgumentException matches unit conversion or mandatory CDELT keyword test
                StatusBar.show("Could not load OIFits : " + file.getAbsolutePath());
                MessagePane.showErrorMessage("Could not load Fits Image: " + file.getName() + "\n  " + iae.getMessage(), "Could not load file");
            }
        }
    }

}
