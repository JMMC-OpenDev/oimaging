/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging;

import fr.jmmc.oimaging.model.IRModel;
import fr.jmmc.oimaging.model.IRModelManager;
import fr.jmmc.oimaging.services.ServiceList;
import fr.jmmc.oimaging.services.ServiceResult;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Activate tools for easier developing. 
 * example add ServiceResults without needing to launch run.
 */
public class DevMode {
    
    private static final Logger logger = LoggerFactory.getLogger(DevMode.class.getName());
    
    final static File FILEDIR = new File(SystemUtils.getUserHome() + "/.jmmc-devmode/");
    
    /** search and craft service results. */
    public static void craftAllServiceResults () {
        List<ServiceResult> listSR = searchForFiles();
        
        logger.info("Found " + listSR.size() + " ServiceResults to load.");
        
        listSR.forEach(DevMode::craftServiceResult);
    }
        
        /** search in directory (currently ~/.jmmc-devmode/) the ServiceResult files.
     * The files must be in this way :
     * file1.fits.log.txt
     * file1.fits.output.fits
     * file2.fits.log.txt
     * file2.fits.output.fits
     * etc...
     * @return list of ServiceResult
     */
    private static List<ServiceResult> searchForFiles () {
        List<ServiceResult> listSR = new ArrayList<> ();
        try {
            if (FILEDIR.isDirectory()) {
                File[] dirFiles = FILEDIR.listFiles();
                Comparator<File> cmp = Comparator.comparing(File::getName, Comparator.naturalOrder());
                Arrays.sort(dirFiles, cmp);
                
                for (int i = 1 ; i < dirFiles.length ; i += 2) {
                    
                    String oiFitsResultFileName = dirFiles[i-1].getAbsolutePath().toString();
                    String executionLogResultFileName = dirFiles[i].getAbsolutePath().toString();
                    
                    String baseName1 = oiFitsResultFileName.substring(0, oiFitsResultFileName.length() - ".log.txt".length());
                    String baseName2 = executionLogResultFileName.substring(0, executionLogResultFileName.length() - ".output.fits".length());
                    
                    if (baseName1.equals(baseName2)) {
                        
                        // the input file is fake but will not be opened so it is ok
                        ServiceResult sr = new ServiceResult(
                                new File(baseName1), new File(executionLogResultFileName), new File(oiFitsResultFileName));
                        listSR.add(sr);
                    }
                    else {
                        logger.info("ServiceResult files are not well named: "
                                + oiFitsResultFileName + " " + executionLogResultFileName);
                    }
                }
            }
        } catch (SecurityException e) {
            logger.info("Cannot access files for crafting ServiceResults.");
        }
        return listSR;
    }
    
    /** Craft a ServiceResult that is got not from a run but from files, and add the ServiceResult 
     * @param sExecutionLogResultFile  must not be null. can be invalid file or protected in reading
     * @param sOifitsResultFile  must not be null. can be invalid file or protected in reading
     */
    private static void craftServiceResult (ServiceResult serviceResult) {
        
        IRModel irModel = IRModelManager.getInstance().getIRModel();

        // we set the prefered service. TODO : when we will have a Service keyword or similar we can use it here
        serviceResult.setService(ServiceList.getPreferedService());

        // we know it is valid, we would not load an invalid output fits file.
        serviceResult.setValid(true);

        irModel.addServiceResult(serviceResult);
        logger.info("Added one ServiceResult");
    }
}

