/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services;

import fr.jmmc.jmcs.util.FileUtils;
import fr.jmmc.jmcs.util.StringUtils;
import fr.jmmc.jmcs.util.runner.LocalLauncher;
import fr.jmmc.jmcs.util.runner.RootContext;
import java.io.File;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Support local service runner.
 * @author Guillaume MELLA.
 */
public final class LocalExecutionMode implements OImagingExecutionMode {

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(LocalExecutionMode.class.getName());
    /** application identifier for LocalExecutionMode */
    public final static String APP_NAME = "OImaging-gui";
    /** user for LocalExecutionMode */
    public final static String USER_NAME = "JMMC";
    /** task identifier for LocalExecutionMode */
    public final static String TASK_NAME = "LocalRunner";

    /** singleton */
    public static final LocalExecutionMode INSTANCE = new LocalExecutionMode();

    private LocalExecutionMode() {
        super();
    }

    /**
     * Execute the application and wait end of execution.
     *
     * @param software software to run
     * @param cliOptions software options on command line or null
     * @param inputFilename input filename
     * @param result the service result pointing result file to write data into.
     * @throws IllegalStateException if the job can not be submitted to the job queue
     */
    public static void exec(final String software, final String cliOptions, final String inputFilename, final ServiceResult result) throws IllegalStateException {

        if (StringUtils.isEmpty(software)) {
            throw new IllegalArgumentException("empty application name !");
        }
        if (StringUtils.isEmpty(inputFilename)) {
            throw new IllegalArgumentException("empty input filename !");
        }
        if (StringUtils.isEmpty(result.getOifitsResultFile().getAbsolutePath())) {
            throw new IllegalArgumentException("empty output filename !");
        }
        if (StringUtils.isEmpty(result.getExecutionLogResultFile().getAbsolutePath())) {
            throw new IllegalArgumentException("empty log filename !");
        }

        final String outputFilename = result.getOifitsResultFile().getAbsolutePath();
        final String logFilename = result.getExecutionLogResultFile().getAbsolutePath();

        logger.info("exec: software={} cliOptions={} inputFilenane={} outputFilenane={} logFilename={}", software, cliOptions, inputFilename, outputFilename, logFilename);

        // create the execution context without log file:
        final RootContext jobContext = LocalLauncher.prepareMainJob(APP_NAME, USER_NAME, FileUtils.getTempDirPath(), logFilename);

        final String[] cmd;
        if (cliOptions == null) {
            cmd = new String[]{software, inputFilename, outputFilename};
        } else {
            cmd = new String[]{software, cliOptions, inputFilename, outputFilename};
        }

        LocalLauncher.prepareChildJob(jobContext, TASK_NAME, cmd);

        // Puts the job in the job queue (can throw IllegalStateException if job not queued)
        LocalLauncher.startJob(jobContext);

        final Long jobId = jobContext.getId();

        // Wait for process completion
        try {
            // TODO: use timeout to kill (too long or hanging) jobs anyway ?
            // Wait for task to be done :
            jobContext.getFuture().get();
        } catch (InterruptedException ie) {
            logger.warn("exec: interrupted", ie);

            LocalLauncher.cancelOrKillJob(jobId);

            try {
                // Wait for process to die:
                jobContext.getFuture().get();
            } catch (InterruptedException ie2) {
                logger.debug("Job[{}] waitFor: interrupted 2 {}", jobId);
            } catch (ExecutionException ee) {
                logger.debug("Job[{}] waitFor: execution error", jobId, ee);
            }
            logger.debug("Job[{}] waitFor: interrupted, done", jobId);

        } catch (ExecutionException ee) {
            logger.info("exec: execution error", ee);
        }

        // retrieve command execution status code
        switch (jobContext.getState()) {
            case STATE_CANCELED:
            case STATE_INTERRUPTED:
            case STATE_KILLED:
                result.setCancelled(true);
                break;
            default:
        }
    }

    @Override
    public ServiceResult reconstructsImage(final String software, final String cliOptions, final File inputFile) {
        final ServiceResult result = new ServiceResult(inputFile);
        LocalExecutionMode.exec(software, cliOptions, inputFile.getAbsolutePath(), result);
        return result;
    }

}
