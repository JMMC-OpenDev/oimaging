/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ***************************************************************************** */
package fr.jmmc.oimaging.gui.action;

import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.oimaging.model.IRModelManager;
import java.awt.event.ActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Change parameters.
 */
public class LoadResultAsInputAction extends RegisteredAction {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadResultAsInputAction.class);

    /**
     * Class name. This name is used to register to the ActionRegistrar
     */
    public final static String CLASS_NAME = LoadResultAsInputAction.class.getName();
    /**
     * Action name. This name is used to register to the ActionRegistrar
     */
    public final static String ACTION_NAME = "loadResultAsInput";

    /**
     * Constant indicating we want to use the last img as init img
     */
    public final static String USE_LAST_IMG_AS_INIT = "USE_LAST_IMG_AS_INIT";

    /**
     * Constant indicating we want to use the init img as init img
     */
    public final static String USE_INIT_IMG_AS_INIT = "USE_INIT_IMG_AS_INIT";

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     */
    public LoadResultAsInputAction() {
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

        final boolean useLastImgAsInit;
        switch (evt.getActionCommand()) {
            case USE_INIT_IMG_AS_INIT:
                useLastImgAsInit = false;
                break;
            case USE_LAST_IMG_AS_INIT:
                useLastImgAsInit = true;
                break;
            default:
                useLastImgAsInit = true;
                break;
        }

        IRModelManager.getInstance().loadResultAsInput(useLastImgAsInit);
    }
}
