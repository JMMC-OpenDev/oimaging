/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging;

import fr.jmmc.jmcs.util.FileUtils;
import fr.jmmc.jmcs.util.StringUtils;
import fr.jmmc.oimaging.model.IRModel;
import fr.jmmc.oimaging.model.IRModelManager;
import fr.jmmc.oimaging.services.Service;
import fr.jmmc.oimaging.services.ServiceList;
import fr.jmmc.oimaging.services.ServiceResult;
import static fr.jmmc.oimaging.services.ServiceResult.FITS_FILE_EXT;
import static fr.jmmc.oimaging.services.ServiceResult.LOG_FILE_EXT;
import static fr.jmmc.oimaging.services.ServiceResult.RESULT_FILE_EXT;
import fr.jmmc.oitools.image.ImageOiOutputParam;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.nom.tam.fits.FitsDate;
import fr.nom.tam.fits.FitsException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Activate tools for easier developing.
 * example add ServiceResults without needing to launch run.
 */
public class DevMode {

    private static final Logger logger = LoggerFactory.getLogger(DevMode.class.getName());

    private final static File FILE_DIR = new File(SystemUtils.getUserHome() + "/.jmmc-devmode/");

    /** search and craft service results. */
    public static void searchAndCraftAllServiceResults() {
        List<ServiceResult> listSR = searchForServiceResults();

        logger.info("Found {} ServiceResults to load.", listSR.size());

        listSR.forEach(DevMode::craftServiceResult);
    }

    /**
     * Search in directory (currently ~/.jmmc-devmode/) the ServiceResult files.
     * Files with extension .fits are all read
     * if "file1.output.fits" exists, "file1.log.txt" is searched as execution log file
     * this execution log file is optional. A temporary file will be used in replacement.
     * @return list of ServiceResult
     */
    private static List<ServiceResult> searchForServiceResults() {
        List<ServiceResult> listSR = new ArrayList<>();

        if (FILE_DIR.isDirectory() && FILE_DIR.canRead()) {
            // get all files
            File[] files = FILE_DIR.listFiles();

            // extract the .fits files and the .log.txt files
            Map<String, File> fitsFiles = new HashMap<>(files.length);
            Map<String, File> logFiles = new HashMap<>(files.length);
            for (File file : files) {
                if (file.getName().endsWith(FITS_FILE_EXT)) {
                    fitsFiles.put(file.getName(), file);
                } else if (file.getName().endsWith(LOG_FILE_EXT)) {
                    logFiles.put(file.getName(), file);
                }
            }

            // foreach fits file, check readability and look if an associated log file exists
            fitsFiles.forEach((fitsName, fitsFile) -> {
                if (fitsFile.exists() && fitsFile.canRead()) {

                    // we use the result file as input file too
                    File inputFile = fitsFile;

                    // we remove either RESULT_FILE_EXT or FITS_FILE_EXT from fitsName
                    String baseFitsName
                           = fitsName.endsWith(RESULT_FILE_EXT)
                            ? fitsName.substring(0, fitsName.length() - RESULT_FILE_EXT.length())
                            : fitsName.substring(0, fitsName.length() - FITS_FILE_EXT.length());

                    // we look for an associated log file with the log file extension
                    String logName = baseFitsName + LOG_FILE_EXT;
                    // if log file not found, we use one temporary one. TODO: should we check that this tmp file does not exist yet ?
                    File logFile = logFiles.containsKey(logName) ? logFiles.get(logName) : FileUtils.getTempFile(logName);

                    // we create the ServiceResult and add it to the result list
                    ServiceResult sr = new ServiceResult(inputFile, fitsFile, logFile);
                    listSR.add(sr);
                } else {
                    logger.info("Could not find or read file: {}", fitsFile.getAbsolutePath());
                }
            });
        } else {
            logger.info("Could not find or read FILEDIR: {}", FILE_DIR.getAbsolutePath());
        }

        return listSR;
    }

    /**
     * Craft a ServiceResult that is got not from a run but from files, and add the ServiceResult
     * @param serviceResult required
     */
    private static void craftServiceResult(ServiceResult serviceResult) {
        serviceResult.setValid(true);

        // explicitly loading oiFitsFile so we can inspect it, and extract Service for example
        // irModel.addServiceResult also call this function, but this function should be idempotent so it is ok.
        try {
            serviceResult.loadOIFitsFile();
        } catch (IOException | FitsException e) {
            logger.error("Can't load result oiFits file '{}':", serviceResult.getOifitsResultFile(), e);
        }
        // note: serviceResult may be invalid

        // try to guess and set service
        final Service service = ServiceList.getServiceFromOIFitsFile(serviceResult.getOifitsFile());
        if (service == null) {
            serviceResult.setService(ServiceList.getPreferedService());
        } else {
            serviceResult.setService(service);
        }

        IRModelManager.getInstance().getIRModel().addServiceResult(serviceResult);

        // must be called AFTER IRModel.addServiceResult().
        // that function will update keywords used here
        OIFitsFile oiFitsFile = serviceResult.getOifitsFile();
        if (oiFitsFile != null) {
            ImageOiOutputParam outputParams = oiFitsFile.getImageOiData().getOutputParam();

            final String strStartDate = outputParams.getKeyword(IRModel.KEYWORD_START_DATE.getName());
            serviceResult.setStartTime(parseKeywordDate(strStartDate));

            final String strEndDate = outputParams.getKeyword(IRModel.KEYWORD_END_DATE.getName());
            serviceResult.setEndTime(parseKeywordDate(strEndDate));
        }

        logger.info("Added one ServiceResult for '{}'", serviceResult.getOifitsResultFile());
    }

    /** Parse the date contained in a keyword (and supposedly stored in FitsDate format).
     *
     * @param keywordDate optional. content can be unparsable.
     * @return Date if parsing successful. null if parsing failed or keywordDate null.
     */
    private static Date parseKeywordDate(String keywordDate) {
        if (!StringUtils.isEmpty(keywordDate)) {
            try {
                return new FitsDate(keywordDate).toDate();
            } catch (FitsException e) {
                logger.info("Could not parse the date {}.", keywordDate);
            }
        }
        return null;
    }

    private DevMode() {
        // forbidden
    }
}
