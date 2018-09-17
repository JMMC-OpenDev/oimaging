/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services;

import fr.jmmc.jmcs.gui.component.GenericListModel;
import java.io.File;
import java.util.ArrayList;
import javax.swing.ComboBoxModel;

/**
 *
 * @author mellag
 */
public final class ServiceList {

    public static final String SERVICE_BSMEM = "BSMEM";
    public static final String SERVICE_WISARD = "WISARD";

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

        availableServices.add(new Service(SERVICE_BSMEM, "bsmem-ci", localExecutionMode, "", ""));
        availableServices.add(new Service(SERVICE_WISARD, "wisard-ci", localExecutionMode, "", ""));

        availableServices.add(new Service(SERVICE_BSMEM + " (remote)", "bsmem-ci", remoteExecutionMode, "", ""));
        // TODO remove code configuration and link this it to a preference
        preferedService = new Service(SERVICE_WISARD + " (remote)", "wisard-ci", remoteExecutionMode, "", "");
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

    public static Service getPreferedService() {
        return getInstance().preferedService;
    }

    public static ServiceResult reconstructsImage(Service service, File inputFile) {
        return service.getExecMode().reconstructsImage(service.getProgram(), inputFile);
    }
}
