/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services;

import fr.jmmc.jmcs.util.FileUtils;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.nom.tam.fits.FitsException;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Result container that gather multiple elements.
 * @author mellag
 */
public final class ServiceResult {

    private static final Logger logger = LoggerFactory.getLogger(ServiceResult.class.getName());

    private final File inputFile;
    private final File oifitsResultFile;
    private final File executionLogResultFile;
    // TODO int errorCode;
    private boolean cancelled = false;
    private boolean valid = false;
    private String errorMessage = null;
    /** number received when added to the list of results */
    private int index;
    /** must be kept in sync with STRTDATE keyword in the OIFitsFile. */
    private Date startTime;
    /** must be kept in sync with ENDDATE keyword in the OIFitsFile. */
    private Date endTime;

    /** computed property : endTime - startTime.
     * To avoid computing it every UI refresh.
     * Must be recomputed if startTime/endTime changes.
     * Unit is second.
     */
    private double jobDuration;

    private Service service;

    // Post process cached data
    private OIFitsFile oiFitsFile = null;
    private String executionLog = null;

    /** the extension for fits file */
    public static final String FITS_FILE_EXT = ".fits";
    /** the extenion for the result OIFits file */
    public static final String RESULT_FILE_EXT = ".output.fits";
    /** the extension for the log execution file */
    public static final String LOG_FILE_EXT = ".log.txt";

    private static final int UNDEFINED_INDEX = -1;

    /**
     * Helper constructor that created result files using given inputfile name.
     * inputFile must be a temporary filename.
     * The oifitsResultFile result get inpuFilename with .output.fits suffix.
     * The log file get inputFilename with .log.txt suffix.
     */
    public ServiceResult(File inputFile) {
        this(inputFile,
                FileUtils.getTempFile(inputFile.getName() + RESULT_FILE_EXT),
                FileUtils.getTempFile(inputFile.getName() + LOG_FILE_EXT));
    }

    /**
     * Constructor where Result and Log files are given by caller.
     * thus they are not necessarily temp files.
     */
    public ServiceResult(File inputFile, File oifitsResultFile, File executionLogResultFile) {
        this.inputFile = inputFile;
        this.oifitsResultFile = oifitsResultFile;
        this.executionLogResultFile = executionLogResultFile;
        this.startTime = new Date();
        this.jobDuration = 0;
        this.index = UNDEFINED_INDEX; // undefined value that will be set by IRModel.addServiceResult()
        logger.debug("new ServiceResult({}, {}, {})", inputFile, oifitsResultFile, executionLogResultFile);
    }

    public File getInputFile() {
        return inputFile;
    }

    public File getOifitsResultFile() {
        return oifitsResultFile;
    }

    public void loadOIFitsFile() throws IOException, FitsException {
        if ((oiFitsFile == null) && (oifitsResultFile != null) && oifitsResultFile.exists()) {
            try {
                oiFitsFile = OIFitsLoader.loadOIFits(oifitsResultFile.getAbsolutePath());
            } catch (IOException | FitsException e) {
                setValid(false);
                throw e;
            }
        }
    }

    public OIFitsFile getOifitsFile() {
        return oiFitsFile;
    }

    public File getExecutionLogResultFile() {
        return executionLogResultFile;
    }

    public void loadExecutionLogFile() throws IOException {
        if ((executionLog == null) && (executionLogResultFile != null) && executionLogResultFile.exists()) {
            executionLog = FileUtils.readFile(executionLogResultFile.getAbsoluteFile());
        }
    }

    public String getExecutionLog() {
        return executionLog;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Date getStartTime() {
        return this.startTime;
    }

    public Date getEndTime() {
        return this.endTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
        computeJobDuration();
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
        computeJobDuration();
    }

    public double getJobDuration() {
        return jobDuration;
    }

    private void computeJobDuration() {
        if (startTime == null || endTime == null) {
            jobDuration = 0;
        } else {
            jobDuration = (endTime.getTime() - startTime.getTime()) / 1000D;
        }
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(int index) {
        this.index = index;
    }

}
