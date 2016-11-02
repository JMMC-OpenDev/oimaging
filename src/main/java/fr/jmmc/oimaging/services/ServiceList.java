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
public class ServiceList {

    /** Singleton instance */
    private static ServiceList _instance = null;

    GenericListModel<Service> availableServices = null;

    private ServiceList() {
        OImagingExecutionMode dummyExecutionMode = new DummyExecutionMode();
        OImagingExecutionMode remoteExecutionMode = new RemoteExecutionMode();
        OImagingExecutionMode localExecutionMode = new LocalExecutionMode();

        availableServices = new GenericListModel<Service>(new ArrayList<Service>(25), true);
        availableServices.add(new Service("DummyOI", "dummy", dummyExecutionMode, "This is a dumy service", "by G.MELLA"));
        availableServices.add(new Service("BSMEM", "bsmem-ci", localExecutionMode, "", ""));
        availableServices.add(new Service("BSMEM (remote)", "bsmem-ci", remoteExecutionMode, "", ""));

    }

    public static ComboBoxModel getAvailableServices() {
        if (_instance == null) {
            _instance = new ServiceList();
        }
        return _instance.availableServices;
    }

    public static ServiceResult reconstructsImage(Service service, File inputFile) {
        return service.getExecMode().reconstructsImage(service.getProgram(), inputFile);
    }
}
