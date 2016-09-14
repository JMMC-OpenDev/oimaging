/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.gui.action;

import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.MessagePane.ConfirmSaveChanges;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import java.awt.event.ActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * New action
 * @author bourgesl
 */
public final class NewAction extends RegisteredAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class name. This name is used to register to the ActionRegistrar */
    public final static String className = NewAction.class.getName();
    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String actionName = "newCollection";
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(className);

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     */
    public NewAction() {
        super(className, actionName);
    }

    /**
     * Handle the action event
     * @param evt action event
     */
    @Override
    public void actionPerformed(final ActionEvent evt) {
        logger.debug("actionPerformed");

        if (false) {
            // Ask the user if he wants to save modifications
            final ConfirmSaveChanges result = MessagePane.showConfirmSaveChanges("creating a new OIFits collection");

            // Handle user choice
            switch (result) {
                // If the user clicked the "Save" button, save and go on
                case Save:
                    /*
                     final AbstractAction action = (SaveObservationAction) ActionRegistrar.getInstance().get(SaveObservationAction.className, SaveObservationAction.actionName);

                     action.actionPerformed(null);
                     */
                    break;

                // If the user clicked the "Don't Save" button, go on
                case Ignore:
                    break;

                // If the user clicked the "Cancel" button or pressed 'esc' key, return
                case Cancel:
                default: // Any other case
                    return;
            }
        }
        // If the user clicked the "Don't Save" button, go on
        OIFitsCollectionManager.getInstance().reset();

        StatusBar.show("new OIFits collection created.");
    }
}
