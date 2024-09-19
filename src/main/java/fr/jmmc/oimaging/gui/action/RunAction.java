/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.gui.action;

import fr.jmmc.jmcs.gui.action.ActionRegistrar;
import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.gui.task.Task;
import fr.jmmc.jmcs.gui.task.TaskSwingWorker;
import fr.jmmc.jmcs.gui.task.TaskSwingWorkerExecutor;
import fr.jmmc.jmcs.util.ImageUtils;
import fr.jmmc.jmcs.util.concurrent.ThreadExecutors;
import fr.jmmc.oimaging.model.IRModel;
import fr.jmmc.oimaging.model.IRModelManager;
import fr.jmmc.oimaging.services.Service;
import fr.jmmc.oimaging.services.ServiceResult;
import fr.nom.tam.fits.FitsException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunAction extends RegisteredAction {

    private static final long serialVersionUID = 1L;
    /** Main logger */
    static final Logger logger = LoggerFactory.getLogger(RunAction.class.getName());
    /** Class name. This name is used to register to the ActionRegistrar */
    public final static String className = RunAction.class.getName();
    /** Action name. This name is used to register to the ActionRegistrar */
    public static final String actionName = "run";

    private static final boolean TEST_WORK_LOAD = false;
    private static final int WORK_LOAD_JOBS = 500;

    /** Task Run IR */
    public static final Task TASK_RUN_IR = new Task("RUN_IR");
    /** Spinner icon gif to decorate "cancel" label. */
    private static final ImageIcon spinnerIcon = ImageUtils.loadResourceIcon("fr/jmmc/jmcs/resource/image/spinner.gif");

    static {
        if (TEST_WORK_LOAD) {
            logger.warn("WARNING: TEST_WORK_LOAD=true (dev) - DO NOT USE IN PRODUCTION !");
        }
    }

    public RunAction() {
        super(className, actionName);
    }

    private void setRunningState(final IRModel irModel, boolean running) {
        irModel.setRunning(running);
        putValue(Action.NAME, (running) ? "Cancel" : "Run");
        putValue(Action.LARGE_ICON_KEY, running ? spinnerIcon : null);

        // update associated RunMoreIterationsAction label and icon
        Action runMoreIterationsAction = ActionRegistrar.getInstance().get(
                RunMoreIterationsAction.CLASS_NAME, RunMoreIterationsAction.ACTION_NAME);
        if (runMoreIterationsAction != null) {
            runMoreIterationsAction.putValue(RunMoreIterationsAction.NAME,
                    running ? RunMoreIterationsAction.LABEL_CANCEL : RunMoreIterationsAction.LABEL_IDLE);
            runMoreIterationsAction.putValue(Action.LARGE_ICON_KEY, running ? spinnerIcon : null);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO: make a snapshot of the required information from model
        final IRModel irModel = IRModelManager.getInstance().getIRModel();
        if (irModel.isRunning()) {
            // cancel job
            TaskSwingWorkerExecutor.cancelTask(TASK_RUN_IR);
            StatusBar.show("Process cancelled.");
        } else {
            try {
                StatusBar.show("Spawn " + irModel.getSelectedService() + " process");
                // change model state to lock it and extract its snapshot
                setRunningState(irModel, true);
                new RunFitActionWorker(irModel.getCliOptions(), irModel.prepareTempFile(), irModel, this).executeTask();
            } catch (FitsException fe) {
                setRunningState(irModel, false);
                logger.error("Can't prepare temporary file before running process", fe);
                StatusBar.show("Can't spawn new process: " + fe.getMessage());
            } catch (IOException ioe) {
                logger.error("Can't prepare temporary file before running process", ioe);
                StatusBar.show("Can't spawn new process: " + ioe.getMessage());
                setRunningState(irModel, false);
            }
        }
    }

    static final class RunFitActionWorker extends TaskSwingWorker<ServiceResult> {

        private final String cliOptions;
        private final File inputFile;
        private final IRModel irModel;
        private final RunAction parentAction;

        RunFitActionWorker(String cliOptions, File inputFile, IRModel irModel, RunAction runAction) {
            super(TASK_RUN_IR);
            this.cliOptions = cliOptions;
            this.inputFile = inputFile;
            this.irModel = irModel; // only for callback
            this.parentAction = runAction;
        }

        @Override
        public ServiceResult computeInBackground() {
            final Service service = irModel.getSelectedService();
            ServiceResult result = null;
            try {
                if (TEST_WORK_LOAD) {
                    final List<Callable<ServiceResult>> jobs = new ArrayList<>(WORK_LOAD_JOBS);

                    for (int i = 0; i < WORK_LOAD_JOBS; i++) {
                        jobs.add(new Callable<ServiceResult>() {
                            @Override
                            public ServiceResult call() throws Exception {
                                ServiceResult r = service.getExecMode().reconstructsImage(service.getProgram(), cliOptions, inputFile);
                                logger.warn("Result: {}", r);
                                return r;
                            }
                        });
                    }

                    final List<Future<ServiceResult>> futures = new ArrayList<>(WORK_LOAD_JOBS);
                    logger.warn("Spawning jobs: {}", WORK_LOAD_JOBS);

                    for (int i = 0; i < WORK_LOAD_JOBS; i++) {
                        futures.add(ThreadExecutors.getRunnerExecutor().submit(jobs.get(i)));
                    }

                    logger.warn("Waiting for completion...");

                    for (int i = 0; i < WORK_LOAD_JOBS; i++) {
                        try {
                            result = futures.get(i).get(); // wait
                        } catch (Exception e) {
                            logger.warn("futures: exception: ", e);
                            break;
                        }
                    }

                    logger.warn("All jobs completed.");

                    // result should be defined now !
                    if (result == null) {
                        throw new IllegalStateException("result is null");
                    }
                } else {
                    result = service.getExecMode().reconstructsImage(service.getProgram(), cliOptions, inputFile);
                }
                result.setService(service);

                if (result.getErrorMessage() == null) {
                    // Result is valid only if the OIFITS file was downloaded successfully:
                    boolean valid = result.getOifitsResultFile().exists();
                    result.setValid(valid);

                    if (!valid) {
                        result.setErrorMessage("No OIFits ouput (probably a server error occured) !");
                    }
                }
                return result;
            } catch (IllegalStateException ise) {
                logger.warn("computeInBackground: exception: ", ise);
                throw ise;
            } finally {
                if (result != null) {
                    result.setEndTime(new Date());
                }
            }
        }

        @Override
        public void refreshUI(ServiceResult serviceResult) {
            // action finished, we can change state and update model just after.
            parentAction.setRunningState(irModel, false);

            if (!serviceResult.isCancelled()) {
                this.irModel.addServiceResult(serviceResult);
            } else {
                StatusBar.show("Error occured during process : " + serviceResult.getErrorMessage());
            }
        }

        /**
         * Refresh GUI when no data (null returned or cancelled) invoked by the Swing Event Dispatcher Thread (Swing EDT)
         * @param cancelled true if task cancelled; false if null returned by computeInBackground()
         */
        @Override
        public void refreshNoData(final boolean cancelled) {
            // action finished, we can change state.
            parentAction.setRunningState(irModel, false);
        }

        @Override
        public void handleException(ExecutionException ee) {
            // action finished, we can change state.
            parentAction.setRunningState(irModel, false);

            // filter some exceptions to avoid feedback report
            if (filterNetworkException(ee)) {
                MessagePane.showErrorMessage("Please check your network setup", "Please check your network setup", ee);
            } else {
                super.handleException(ee);
            }

            StatusBar.show("Error occured during process");
        }

        public boolean filterNetworkException(Exception e) {
            Throwable c = getRootCause(e);
            return c != null
                    && (c instanceof UnknownHostException
                    || c instanceof ConnectTimeoutException);
        }

        private static Throwable getRootCause(final Throwable th) {
            Throwable parent = th;
            while (parent.getCause() != null) {
                parent = parent.getCause();
            }
            return parent;
        }
    }
}
