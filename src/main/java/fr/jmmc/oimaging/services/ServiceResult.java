/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services;

import fr.jmmc.jmcs.gui.FeedbackReport;
import fr.jmmc.jmcs.util.FileUtils;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.nom.tam.fits.FitsException;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Result container that gather multiple elements.
 * @author mellag
 */
public class ServiceResult {

    private final File oifitsResultFile;
    private final File executionLogResultFile;

    // Post process cached data
    private OIFitsFile oiFitsFile = null;
    private String executionLog = null;

    private boolean valid = false;
    private String errorMessage = null;
    private Date startTime;
    private Date endTime;

    // TODO int errorCode;
    public ServiceResult(final File oifitsResultFile, final File executionLogResultFile) {
        this.oifitsResultFile = oifitsResultFile;
        this.executionLogResultFile = executionLogResultFile;
        init();
    }

    /**
     * Helper constructor that created result files using given inputfile name.
     * inputFile must be a tempory filename.
     * The oifitsResultFile result get inpuFilename with .output.fits suffix.
     * The log file get inputFilename with .log.txt suffix.
     */
    public ServiceResult(File inputFile) {
        String inputFilename = inputFile.getName();
        this.oifitsResultFile = FileUtils.getTempFile(inputFilename + ".output.fits");
        this.executionLogResultFile = FileUtils.getTempFile(inputFilename + ".log.txt");
        init();
    }

    private void init() {
        setStartTime(new Date());
    }

    public File getOifitsResultFile() {
        return oifitsResultFile;
    }

    public OIFitsFile getOifitsFile() throws IOException, FitsException {
        if (oiFitsFile == null) {
            oiFitsFile = OIFitsLoader.loadOIFits(oifitsResultFile.getAbsolutePath());
        }
        return oiFitsFile;
    }

    public File getExecutionLogResultFile() {
        return executionLogResultFile;
    }

    public String getExecutionLog() {
        if (executionLog == null) {
            try {
                executionLog = FileUtils.readFile(executionLogResultFile.getAbsoluteFile());
            } catch (IOException ex) {
                FeedbackReport.openDialog(ex);
                executionLog = "Can't recover log report.";
            }
        }
        return executionLog;
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
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

}
