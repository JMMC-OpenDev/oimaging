/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging;

import fr.jmmc.oimaging.model.IRModel;
import fr.jmmc.oimaging.model.IRModelManager;
import fr.jmmc.oimaging.services.ServiceResult;
import fr.nom.tam.fits.FitsException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
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
        List<File> srFiles = searchForFiles();
        logger.info("DevMode: found " + srFiles.size() / 3 + " ServiceResults to load.");
        
        for (int i = 2 ; i < srFiles.size() ; i += 3) {
            craftServiceResult(srFiles.get(i-2), srFiles.get(i-1), srFiles.get(i));
        }
    }
    
    /** search in planned directory (~/.jmmc-devmode/) the ServiceResult files.
     * The files must be in this way :
     * ~/.jmmc-devmode/file1.fits
     * ~/.jmmc-devmode/file1.fits.log.txt
     * ~/.jmmc-devmode/file1.fits.output.fits
     * ~/.jmmc-devmode/file2.fits
     * ~/.jmmc-devmode/file2.fits.log.txt
     * ~/.jmmc-devmode/file2.fits.output.fits
     * etc...
     * @return list of files that are not null, but they may be invalid or read protected
     */
    private static List<File> searchForFiles () {
        List<File> srFiles = new ArrayList<> ();
        try {
            if (FILEDIR.isDirectory()) {
                File[] dirFiles = FILEDIR.listFiles();
                Comparator<File> cmp = Comparator.comparing(File::getName, Comparator.naturalOrder());
                Arrays.sort(dirFiles, cmp);
                for (int i = 2 ; i < dirFiles.length ; i += 3) {
                    if (
                        dirFiles[i-1].getName().equals(dirFiles[i-2].getName() + ".log.txt")
                        &&  dirFiles[i].getName().equals(dirFiles[i-2].getName() + ".output.fits")
                    ){
                        srFiles.add(dirFiles[i-2]);
                        srFiles.add(dirFiles[i-1]);
                        srFiles.add(dirFiles[i]);
                    }
                    else {
                        logger.info("DevMode : ServiceResult files are not well named: "
                                + dirFiles[i-2].getName() + " " + dirFiles[i-1].getName()
                                + " " + dirFiles[i]);
                    }
                }
            }
        } catch (SecurityException e) {
            logger.info("DevMode: cannot access files for crafting ServiceResults.");
        }
        return srFiles;
    }
        
    /** Craft a ServiceResult that is got not from a run but from files, and add the ServiceResult 
     * @param sInputFile must not be null. can be invalid file or protected in reading
     * @param sExecutionLogResultFile  must not be null. can be invalid file or protected in reading
     * @param sOifitsResultFile  must not be null. can be invalid file or protected in reading
     */
    private static void craftServiceResult (File inputFile, File executionLogResultFile, File oifitsResultFile) {
        IRModel irModel = IRModelManager.getInstance().getIRModel();
        
        try {
            ServiceResult serviceResult = new ServiceResult(
                    inputFile, oifitsResultFile, executionLogResultFile,
                    0, "No comments", new Date(), new Date());
            serviceResult.setValid(true);
            irModel.addServiceResult(serviceResult);
            logger.info("Added one ServiceResult");
        }
        catch (IOException | FitsException e) {
            logger.info("Could not craft ServiceResult, error: " + e);
        }
    }
}

