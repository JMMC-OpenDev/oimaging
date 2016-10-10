/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services;

import fr.jmmc.jmcs.gui.component.GenericListModel;
import java.util.ArrayList;
import javax.swing.ComboBoxModel;

/**
 *
 * @author mellag
 */
public class ServiceList {

    /** Singleton instance */
    private static ServiceList _instance = null;

    GenericListModel<String> availableServices = null;

    private ServiceList() {
        availableServices = new GenericListModel<String>(new ArrayList<String>(25), true);

        availableServices.add("BSMEM");
        availableServices.add("Dummy");
    }

    public static ComboBoxModel getAvailableServices() {
        if (_instance == null) {
            _instance = new ServiceList();
        }
        return _instance.availableServices;
    }

}
