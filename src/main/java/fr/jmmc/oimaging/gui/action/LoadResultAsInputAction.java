/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ***************************************************************************** */
package fr.jmmc.oimaging.gui.action;

import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.oimaging.OImaging;
import fr.jmmc.oimaging.gui.MainPanel;
import fr.jmmc.oimaging.model.IRModelManager;
import fr.jmmc.oimaging.services.ServiceResult;
import java.awt.event.ActionEvent;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Change parameters.
 */
public class LoadResultAsInputAction extends RegisteredAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;

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
        MainPanel mainPanel = OImaging.getInstance().getMainPanel();
        List<ServiceResult> selectedResultList = mainPanel.getResultSetTablePanel().getSelectedRows();

        if (selectedResultList.size() == 1) {
            ServiceResult selectedResult = selectedResultList.get(0);

            IRModelManager.getInstance().loadResultAsInput(selectedResult, useLastImgAsInit);
        } else {
            LOGGER.error("Cannot procede LoadResultAsInputAction when the number of selected results != 1");
        }
    }
}
