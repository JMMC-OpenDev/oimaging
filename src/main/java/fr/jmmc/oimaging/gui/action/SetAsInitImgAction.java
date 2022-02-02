/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ***************************************************************************** */
package fr.jmmc.oimaging.gui.action;

import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.oimaging.OImaging;
import fr.jmmc.oimaging.gui.ViewerPanel;
import fr.jmmc.oimaging.model.IRModelManager;
import fr.jmmc.oitools.image.FitsImageHDU;
import java.awt.event.ActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Set as init image.
 */
public class SetAsInitImgAction extends RegisteredAction {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SetAsInitImgAction.class);

    /**
     * Class name. This name is used to register to the ActionRegistrar
     */
    public final static String CLASS_NAME = SetAsInitImgAction.class.getName();
    /**
     * Action name. This name is used to register to the ActionRegistrar
     */
    public final static String ACTION_NAME = "setAsInitImg";

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     */
    public SetAsInitImgAction() {
        super(CLASS_NAME, ACTION_NAME);
    }

    /**
     * Handle the action event
     *
     * @param evt action event
     */
    @Override
    public void actionPerformed(final ActionEvent evt) {
        LOGGER.debug("actionPerformed");

        ViewerPanel viewerPanel = OImaging.getInstance().getMainPanel().getViewerPanelActive();
        if (viewerPanel != null) {
            FitsImageHDU fihdu = viewerPanel.getDisplayedFitsImageHDU();
            if (fihdu != null) {
                IRModelManager.getInstance().setAsInitImg(fihdu);
            } else {
                LOGGER.error("Cannot procede SetAsInitImgAction because selected image is null");
            }
        } else {
            LOGGER.error("Cannot procede SetAsInitImgAction because active viewer panel is null");
        }
    }
}
