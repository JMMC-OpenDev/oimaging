/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services;

import java.io.File;

/**
 * Interface of OImaging services
 * @author mellag
 */
public interface OImagingExecutionMode {

    /**
     * Main basic interface.
     * @param software algorithm to run
     * @param inputFile oifits file that must be used by the service.
     * @return the result file produced by the algorithm (oifits) as first file, program output as second file (text) if any
     */
    public ServiceResult reconstructsImage(final String software, final File inputFile);

}
