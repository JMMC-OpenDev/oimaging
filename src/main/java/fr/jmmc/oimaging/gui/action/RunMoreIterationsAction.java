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
 * Load selected Result as input and trigger RunAction.
 */
public class RunMoreIterationsAction extends RegisteredAction {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RunMoreIterationsAction.class);

    /**
     * Class name. This name is used to register to the ActionRegistrar
     */
    public final static String CLASS_NAME = RunMoreIterationsAction.class.getName();
    /**
     * Action name. This name is used to register to the ActionRegistrar
     */
    public final static String ACTION_NAME = "runMoreIterations";

    public final static String LABEL_IDLE = "Run more iterations";
    public final static String LABEL_CANCEL = "Cancel";

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     */
    public RunMoreIterationsAction() {
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
        IRModelManager.getInstance().runMoreIterations();
    }

}
