/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging;

import fr.jmmc.jmcs.util.FileUtils;
import fr.jmmc.oimaging.model.IRModel;
import fr.jmmc.oimaging.model.IRModelManager;
import fr.jmmc.oimaging.services.Service;
import fr.jmmc.oimaging.services.ServiceList;
import fr.jmmc.oimaging.services.ServiceResult;
import fr.nom.tam.fits.FitsException;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author antoine
 */
public class DevMode {
    
    private static final Logger logger = LoggerFactory.getLogger(DevMode.class.getName());
    
    public static void craftAllServiceResults () {
        craftServiceResult(
                "PI_GRU_forImage.fits8736571368747455694.export-0.fits",
                "PI_GRU_forImage.fits8736571368747455694.export-0.fits.output.fits",
                "PI_GRU_forImage.fits8736571368747455694.export-0.fits.log.txt",
                ServiceList.SERVICE_WISARD);

        craftServiceResult(
                "PI_GRU_forImage.fits1339114126933496664.export-1.fits",
                "PI_GRU_forImage.fits1339114126933496664.export-1.fits.output.fits",
                "PI_GRU_forImage.fits1339114126933496664.export-1.fits.log.txt",
                ServiceList.SERVICE_WISARD);

        craftServiceResult(
                "PI_GRU_forImage.fits3804739948616985083.export-2.fits",
                "PI_GRU_forImage.fits3804739948616985083.export-2.fits.output.fits",
                "PI_GRU_forImage.fits3804739948616985083.export-2.fits.log.txt",
                ServiceList.SERVICE_WISARD);
    }
    
    private static void craftServiceResult (String sInputFile, String sOifitsResultFile, String sExecutionLogResultFile, String serviceName) {
        IRModel irModel = IRModelManager.getInstance().getIRModel();
                    
        final String prefix = SystemUtils.getUserHome() + "/.jmmc-devmode/";
        
        File inputFile = FileUtils.getFile(prefix + sInputFile);
        File oifitsResultFile = FileUtils.getFile(prefix + sOifitsResultFile);
        File executionLogResultFile = FileUtils.getFile(prefix + sExecutionLogResultFile);

        if (inputFile == null || oifitsResultFile == null || executionLogResultFile == null) {
            logger.info("Could not craft Service Result, because of null File");
            return;
        }
        try {
            Service service = ServiceList.getAvailableService(serviceName);
            ServiceResult serviceResult = new ServiceResult(
                    inputFile, oifitsResultFile, executionLogResultFile,
                    0, "No comments", new Date(), new Date(), service
            );
            serviceResult.setValid(true);
            irModel.addServiceResult(serviceResult);
            logger.info("Added one ServiceResult");
        }
        catch (IOException | FitsException e) {
            logger.info("Could not craft ServiceResult, error: " + e);
        }
    }
}

