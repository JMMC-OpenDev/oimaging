/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.services;

import fr.jmmc.jmcs.util.FileUtils;
import fr.jmmc.jmcs.util.StringUtils;
import fr.jmmc.jmcs.util.runner.LocalLauncher;
import fr.jmmc.jmcs.util.runner.RootContext;
import fr.jmmc.jmcs.util.runner.RunState;
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
    private static final Logger _logger = LoggerFactory.getLogger(LocalExecutionMode.class.getName());
    /** application identifier for LocalExecutionMode */
    public final static String APP_NAME = "OImaging";
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
     * @param inputFilename input filename
     * @param outputFilename result filename
     * @param logFilename execution log filename
     * @return the job context identifier
     * @throws IllegalStateException if the job can not be submitted to the job queue
     */
    public static int exec(final String software, final String inputFilename, final String outputFilename, final String logFilename) throws IllegalStateException {

        if (StringUtils.isEmpty(software)) {
            throw new IllegalArgumentException("empty application name !");
        }
        if (StringUtils.isEmpty(inputFilename)) {
            throw new IllegalArgumentException("empty input filename !");
        }
        if (StringUtils.isEmpty(outputFilename)) {
            throw new IllegalArgumentException("empty output filename !");
        }
        if (StringUtils.isEmpty(logFilename)) {
            throw new IllegalArgumentException("empty log filename !");
        }

        _logger.info("exec: {} {} {}", software, inputFilename, outputFilename);

        // create the execution context without log file:
        final RootContext jobContext = LocalLauncher.prepareMainJob(APP_NAME, USER_NAME, FileUtils.getTempDirPath(), logFilename);

        final String[] cmd = new String[]{software, inputFilename, outputFilename};
        LocalLauncher.prepareChildJob(jobContext, TASK_NAME, cmd);

        // Puts the job in the job queue (can throw IllegalStateException if job not queued)
        LocalLauncher.startJob(jobContext);

        final Long jobId = jobContext.getId();

        // Wait for process completion
        try {
            // Wait for task to be done :
            jobContext.getFuture().get();
        } catch (InterruptedException ie) {
            _logger.warn("exec: interrupted", ie);

            // TODO: handle resource cleanup
            LocalLauncher.killJob(jobId);

        } catch (ExecutionException ee) {
            _logger.info("exec: execution error", ee);
        }

        // TODO: retrieve command execution status code
        return jobContext.getState() == RunState.STATE_FINISHED_OK ? 0 : 1;
    }

    @Override
    public ServiceResult reconstructsImage(final String software, final File inputFile) {
        ServiceResult result = new ServiceResult(inputFile);
        LocalExecutionMode.exec(software, inputFile.getAbsolutePath(),
                result.getOifitsResultFile().getAbsolutePath(),
                result.getExecutionLogResultFile().getAbsolutePath());
        return result;
    }

}
