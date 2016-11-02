/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services;

import fr.jmmc.jmcs.util.FileUtils;
import java.io.File;

/**
 * Result container that gather multiple elements.
 * @author mellag
 */
public class ServiceResult {

    private final File oifits;
    private final File executionLog;

    private boolean valid = false;
    private String errorMessage = null;

    // TODO int errorCode;
    public ServiceResult(final File oifits, final File executionLog) {
        this.oifits = oifits;
        this.executionLog = executionLog;
    }

    /**
     * Helper constructor that created result files using given inputfile name.
     * inputFile must be a tempory filename.
     * The oifits result get inpuFilename with .output.fits suffix.
     * The log file get inputFilename with .log.txt suffix.
     */
    public ServiceResult(File inputFile) {
        String inputFilename = inputFile.getName();
        this.oifits = FileUtils.getTempFile(inputFilename + ".output.fits");
        this.executionLog = FileUtils.getTempFile(inputFilename + ".log.txt");
    }

    public File getOifits() {
        return oifits;
    }

    public File getExecutionLog() {
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

}
