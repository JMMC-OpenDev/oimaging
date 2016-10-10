/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services;

import fr.jmmc.jmcs.util.FileUtils;
import fr.jmmc.jmcs.util.StringUtils;
import fr.jmmc.jmcs.util.runner.EmptyJobListener;
import fr.jmmc.jmcs.util.runner.JobListener;
import fr.jmmc.jmcs.util.runner.LocalLauncher;
import fr.jmmc.jmcs.util.runner.RootContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support local service runner.
 * @author Guillaume MELLA.
 */
public final class LocalService {

    /** Class logger */
    private static final Logger _logger = LoggerFactory.getLogger(LocalService.class.getName());
    /** application identifier for LocalService */
    public final static String APP_NAME = "OImaging";
    /** user for LocalService */
    public final static String USER_NAME = "JMMC";
    /** task identifier for LocalService */
    public final static String TASK_NAME = "LocalRunner";

    /** Forbidden constructor */
    private LocalService() {
    }

    /**
     * Launch the application in background.
     *
     * @param app_name
     * @param input_filename
     * @param output_filename
     * @return the job context identifier
     * @throws IllegalStateException if the job can not be submitted to the job queue
     */
    public static Long launch(final String app_name, final String input_filename, final String output_filename) throws IllegalStateException {
        return launch(app_name, input_filename, output_filename, new EmptyJobListener());
    }

    /**
     * Launch the given application in background.
     *
     * @param app_name
     * @param input_filename
     * @param output_filename
     * @param jobListener job event listener (not null)
     * @return the job context identifier
     * @throws IllegalStateException if the job can not be submitted to the job queue
     */
    public static Long launch(final String app_name, final String input_filename, final String output_filename, final JobListener jobListener) throws IllegalStateException {

        if (StringUtils.isEmpty(app_name)) {
            throw new IllegalArgumentException("empty application name !");
        }
        if (StringUtils.isEmpty(input_filename)) {
            throw new IllegalArgumentException("empty input filename !");
        }
        if (StringUtils.isEmpty(output_filename)) {
            throw new IllegalArgumentException("empty output filename !");
        }
        if (jobListener == null) {
            throw new IllegalArgumentException("undefined job listener !");
        }

        _logger.info("launch: {} {} {}", app_name, input_filename, output_filename);

        // create the execution context without log file:
        final RootContext jobContext = LocalLauncher.prepareMainJob(APP_NAME, USER_NAME, FileUtils.getTempDirPath(), null);

        final String[] cmd = new String[]{app_name, input_filename, output_filename};
        LocalLauncher.prepareChildJob(jobContext, TASK_NAME, cmd);

        // Puts the job in the job queue (can throw IllegalStateException if job not queued)
        LocalLauncher.startJob(jobContext, jobListener);

        return jobContext.getId();
    }

}
