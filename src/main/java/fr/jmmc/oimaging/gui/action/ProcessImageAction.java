/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.gui.action;

import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.oimaging.OImaging;
import fr.jmmc.oimaging.gui.ViewerPanel.ProcessImageOperation;
import java.awt.event.ActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Modify the current FITS image
 */
public final class ProcessImageAction extends RegisteredAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class name. This name is used to register to the ActionRegistrar */
    public final static String className = ProcessImageAction.class.getName();
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(className);

    /** image processing operation */
    private final ProcessImageOperation op;

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     * @param op operation to perform
     */
    public ProcessImageAction(final ProcessImageOperation op) {
        super(className, op.name());
        this.op = op;
        logger.info("ProcessImageAction[{}] : op = {}", op.name(), op);
    }

    /**
     * Handle the action event
     * @param evt action event
     */
    @Override
    public void actionPerformed(final ActionEvent evt) {
        logger.debug("actionPerformed");
        OImaging.getInstance().getMainPanel().getViewerPanelActive().processFitsImage(op);
    }

}
