/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services;

import fr.jmmc.jmcs.gui.component.GenericListModel;
import fr.jmmc.oimaging.model.IRModel;
import fr.jmmc.oimaging.services.software.SoftwareInputParam;
import fr.jmmc.oitools.fits.FitsHeaderCard;
import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.model.OIFitsFile;
import java.util.ArrayList;
import javax.swing.ComboBoxModel;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author mellag
 */
public final class ServiceList {

    private static final boolean ENABLE_LOCAL_MODE = true;

    public static final String SERVICE_BSMEM = "BSMEM";
    public static final String SERVICE_MIRA = "MIRA";
    public static final String SERVICE_SPARCO = "SPARCO";
    public static final String SERVICE_WISARD = "WISARD";

    public static final String CMD_BSMEM = "bsmem-ci";
    public static final String CMD_MIRA = "mira-ci";
    public static final String CMD_SPARCO = "sparco-ci";
    public static final String CMD_WISARD = "wisard-ci";

    /** Singleton instance */
    private static ServiceList _instance = null;

    /** Default service */
    final Service preferedService;
    /** service list */
    final GenericListModel<Service> availableServices;

    private ServiceList() {
        final OImagingExecutionMode remoteExecutionMode = RemoteExecutionMode.INSTANCE;

        availableServices = new GenericListModel<Service>(new ArrayList<Service>(8), true);

        final SoftwareInputParam swParamBsmem = SoftwareInputParam.newInstance(SERVICE_BSMEM);
        final SoftwareInputParam swParamMira = SoftwareInputParam.newInstance(SERVICE_MIRA);
        final SoftwareInputParam swParamSparco = SoftwareInputParam.newInstance(SERVICE_SPARCO);
        final SoftwareInputParam swParamWisard = SoftwareInputParam.newInstance(SERVICE_WISARD);

        availableServices.add(new Service(SERVICE_BSMEM, CMD_BSMEM, remoteExecutionMode, "", "", swParamBsmem));
        availableServices.add(new Service(SERVICE_MIRA, CMD_MIRA, remoteExecutionMode, "", "", swParamMira));
        availableServices.add(new Service(SERVICE_SPARCO, CMD_SPARCO, remoteExecutionMode, "", "", swParamSparco));
        // TODO define prefered service in a preference:
        preferedService = new Service(SERVICE_WISARD, CMD_WISARD, remoteExecutionMode, "", "", swParamWisard);
        availableServices.add(preferedService);

        if (ENABLE_LOCAL_MODE) {
            final OImagingExecutionMode localExecutionMode = LocalExecutionMode.INSTANCE;
            availableServices.add(new Service(SERVICE_BSMEM + " (local)", CMD_BSMEM, localExecutionMode, "", "", swParamBsmem));
            availableServices.add(new Service(SERVICE_MIRA + " (local)", CMD_MIRA, localExecutionMode, "", "", swParamMira));
            availableServices.add(new Service(SERVICE_SPARCO + " (local)", CMD_SPARCO, localExecutionMode, "", "", swParamSparco));
            availableServices.add(new Service(SERVICE_WISARD + " (local)", CMD_WISARD, localExecutionMode, "", "", swParamWisard));
        }
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
        if (!StringUtils.isEmpty(name)) {
            final ComboBoxModel model = getInstance().availableServices;
            for (int i = 0, len = model.getSize(); i < len; i++) {
                final Service service = (Service) model.getElementAt(i);
                if (service.getName().equalsIgnoreCase(name)) {
                    return service;
                }
            }
        }
        return null;
    }

    public static Service getPreferedService() {
        return getInstance().preferedService;
    }

    public static Service getServiceFromOIFitsFile(final OIFitsFile oiFitsFile) {
        // try to guess and get service
        String serviceName = getServiceNameFromOiFitsFile(oiFitsFile);
        if (serviceName != null) {
            return ServiceList.getAvailableService(serviceName);
        }
        return null;
    }

    /**
     * Find the Service program from information in an OIFitsFile
     * @param oiFitsFile required
     * @return the program or null if could not find information
     */
    private static String getServiceNameFromOiFitsFile(final OIFitsFile oiFitsFile) {
        if (oiFitsFile != null) {
            final FitsTable outputFitsTable = oiFitsFile.getImageOiData().getOutputParam();

            // Attempt 1: looking for a SOFTWARE output param
            // TODO: there will be a ResultSetTableModel.getKeywordValue method in a future merge, maybe use it here
            if (outputFitsTable.hasKeywordMeta(IRModel.KEYWORD_SOFTWARE.getName())) {
                Object algoValue = outputFitsTable.getKeywordValue(IRModel.KEYWORD_SOFTWARE.getName());
                if (algoValue instanceof String) {
                    return (String) algoValue;
                }
            }

            // Attempt 2: looking for known specific keywords
            // guessing WISARD program from SOFTWARE=WISARD output header card
            if (outputFitsTable.hasHeaderCards()) {
                FitsHeaderCard card = outputFitsTable.findFirstHeaderCard("SOFTWARE");
                if (card != null) {
                    Object softwareValue = card.parseValue();
                    if (softwareValue instanceof String) {
                        String softwareStr = (String) softwareValue;
                        if (softwareStr.equals(SERVICE_WISARD)) {
                            return SERVICE_WISARD;
                        }
                    }
                }
            }

            final FitsTable inputFitsTable = oiFitsFile.getImageOiData().getInputParam();

            // guessing BSMEM program from presence of INITFLUX input header card
            if (inputFitsTable.hasHeaderCards()) {
                FitsHeaderCard card = inputFitsTable.findFirstHeaderCard("INITFLUX");
                if (card != null) {
                    return SERVICE_BSMEM;
                }
            }

            // guessing SPARCO program from presence of SPEC0 input header card
            if (inputFitsTable.hasHeaderCards()) {
                FitsHeaderCard card = inputFitsTable.findFirstHeaderCard("SPEC0");
                if (card != null) {
                    return SERVICE_SPARCO;
                }
            }

            // guessing MIRA program from presence of SMEAR_FN input header card and missing SPEC0
            if (inputFitsTable.hasHeaderCards()) {
                FitsHeaderCard card = inputFitsTable.findFirstHeaderCard("SMEAR_FN");
                if (card != null) {
                    card = inputFitsTable.findFirstHeaderCard("SPEC0");
                    if (card == null) {
                        return SERVICE_MIRA;
                    }
                }
            }
        }
        return null;
    }
}
