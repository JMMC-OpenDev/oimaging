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

    File oifits;
    File executionLog;

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

}
