/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.gui.action;

import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.oimaging.OImaging;
import java.awt.event.ActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteSelectionAction extends RegisteredAction {

    /** Class name. This name is used to register to the ActionRegistrar */
    public final static String className = DeleteSelectionAction.class.getName();
    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String actionName = "deleteSelection";

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(className);
    private static final long serialVersionUID = 1L;

    public DeleteSelectionAction() {
        super(className, actionName);
        // action should be enabled in the future
        setEnabled(false);
    }

    public void actionPerformed(ActionEvent e) {
        logger.debug("actionPerformed");
        OImaging.getInstance().getMainPanel().deleteSelectedRows();
    }
}
