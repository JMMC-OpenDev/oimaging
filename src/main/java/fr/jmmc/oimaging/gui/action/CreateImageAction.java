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
 * Create a new FitsImageHDU with one FitsImage representing a gaussian.
 */
public class CreateImageAction extends RegisteredAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(CreateImageAction.class);

    /** Class name. This name is used to register to the ActionRegistrar */
    public final static String CLASS_NAME = CreateImageAction.class.getName();
    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String ACTION_NAME = "createImage";

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     */
    public CreateImageAction() {
        super(CLASS_NAME, ACTION_NAME);
    }

    /**
     * Handle the action event
     * @param evt action event
     */
    @Override
    public void actionPerformed(final ActionEvent evt) {
        logger.debug("actionPerformed");
        OImaging.getInstance().getMainPanel().getViewerPanelInput().createFitsImage();
    }
}
