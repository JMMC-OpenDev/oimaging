/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services;

import fr.jmmc.jmcs.gui.component.GenericListModel;
import fr.jmmc.oimaging.services.software.SoftwareInputParam;
import java.io.File;
import java.util.ArrayList;
import javax.swing.ComboBoxModel;

/**
 *
 * @author mellag
 */
public final class ServiceList {

    public static final String SERVICE_BSMEM = "BSMEM";
    public static final String SERVICE_MIRA = "MIRA";
    public static final String SERVICE_WISARD = "WISARD";

    public static final String CMD_BSMEM = "bsmem-ci";
    public static final String CMD_MIRA = "mira-ci";
    public static final String CMD_WISARD = "wisard-ci";

    /** Singleton instance */
    private static ServiceList _instance = null;

    /** Default service */
    final Service preferedService;
    /** service list */
    final GenericListModel<Service> availableServices;

    private ServiceList() {
        final OImagingExecutionMode remoteExecutionMode = RemoteExecutionMode.INSTANCE;
        final OImagingExecutionMode localExecutionMode = LocalExecutionMode.INSTANCE;

        availableServices = new GenericListModel<Service>(new ArrayList<Service>(8), true);

        final SoftwareInputParam swParamBsmem = SoftwareInputParam.newInstance(SERVICE_BSMEM);
        final SoftwareInputParam swParamMira = SoftwareInputParam.newInstance(SERVICE_MIRA);
        final SoftwareInputParam swParamWisard = SoftwareInputParam.newInstance(SERVICE_WISARD);

        availableServices.add(new Service(SERVICE_BSMEM + " (local)", CMD_BSMEM, localExecutionMode, "", "", swParamBsmem));
        availableServices.add(new Service(SERVICE_MIRA + " (local)", CMD_MIRA, localExecutionMode, "", "", swParamMira));
        availableServices.add(new Service(SERVICE_WISARD + " (local)", CMD_WISARD, localExecutionMode, "", "", swParamWisard));

        availableServices.add(new Service(SERVICE_BSMEM, CMD_BSMEM, remoteExecutionMode, "", "", swParamBsmem));
        availableServices.add(new Service(SERVICE_MIRA, CMD_MIRA, remoteExecutionMode, "", "", swParamMira));
        // TODO remove code configuration and link this it to a preference
        preferedService = new Service(SERVICE_WISARD, CMD_WISARD, remoteExecutionMode, "", "", swParamWisard);

        availableServices.add(preferedService);
    }

    private static ServiceList getInstance() {
        if (_instance == null) {
            _instance = new ServiceList();
        }
        return _instance;
    }

    public static ComboBoxModel getAvailableServices() {
        return getInstance().availableServices;
    }

    public static Service getAvailableService(final String name) {
        final ComboBoxModel model = getInstance().availableServices;
        for (int i = 0, len =  model.getSize(); i < len; i++) {
            final Service service = (Service)model.getElementAt(i);
            if (service.getName().equalsIgnoreCase(name)) {
                return service;
            }
        }
        return null;
    }

    public static Service getPreferedService() {
        return getInstance().preferedService;
    }
}
