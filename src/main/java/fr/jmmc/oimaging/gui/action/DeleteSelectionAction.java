/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.gui.action;

import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.oimaging.model.IRModel;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeleteSelectionAction extends RegisteredAction implements ListSelectionListener {

    /** Class name. This name is used to register to the ActionRegistrar */
    public final static String className = DeleteSelectionAction.class.getName();
    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String actionName = "deleteSelection";

    /** Class logger */
    static Logger logger = LoggerFactory.getLogger(className);

    protected IRModel irModel = null;
    protected List serviceResultsList = null;

    public DeleteSelectionAction() {
        super(className, actionName);
        // action should be enabled in the future
        setEnabled(false);
    }

    public void actionPerformed(ActionEvent e) {
        if (irModel != null && serviceResultsList != null) {
            irModel.removeServiceResults(serviceResultsList);
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void watchResultsSelection(IRModel irModel, JList jListResultSet) {
        if (jListResultSet.getSelectedIndices().length == 0) {
            this.setEnabled(false);
            this.irModel = null;
            this.serviceResultsList = null;
        } else {
            this.setEnabled(true);
            this.irModel = irModel;
            this.serviceResultsList = jListResultSet.getSelectedValuesList();
        }

    }
}
