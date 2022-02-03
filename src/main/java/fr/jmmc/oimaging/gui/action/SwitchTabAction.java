/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.gui.action;

import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.oimaging.OImaging;
import java.awt.event.ActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Switch tab.
 */
public class SwitchTabAction extends RegisteredAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;

    /** Class logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(SwitchTabAction.class);

    /** Class name. This name is used to register to the ActionRegistrar */
    public final static String CLASS_NAME = SwitchTabAction.class.getName();
    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String ACTION_NAME = "switchTab";

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     */
    public SwitchTabAction() {
        super(CLASS_NAME, ACTION_NAME);
    }

    /**
     * Handle the action event
     * @param evt action event
     */
    @Override
    public void actionPerformed(final ActionEvent evt) {
        LOGGER.debug("actionPerformed");
        OImaging.getInstance().getMainPanel().switchTab();
    }
}
