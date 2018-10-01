/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.gui.action;

import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.oimaging.model.IRModelManager;
import java.awt.event.ActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reset from scratch.
 * @author mella
 */
public final class NewAction extends RegisteredAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class name. This name is used to register to the ActionRegistrar */
    public final static String className = NewAction.class.getName();
    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String actionName = "newOIFits";
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(className);
    /** OIFits MimeType */
    private final static MimeType mimeType = MimeType.OIFITS;

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

        // Reset Manager and call loadOIFits Action.
        IRModelManager.getInstance().reset();
    }

}
