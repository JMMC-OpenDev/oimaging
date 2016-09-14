/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.ir.gui.action;

import fr.jmmc.ir.model.IRModel;
import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.gui.task.Task;
import fr.jmmc.jmcs.gui.task.TaskSwingWorker;
import fr.jmmc.jmcs.gui.task.TaskSwingWorkerExecutor;
import fr.jmmc.jmcs.util.ImageUtils;
import fr.jmmc.oitools.image.FitsImageHDU;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OIFitsWriter;
import fr.nom.tam.fits.FitsException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;
import javax.swing.Action;
import javax.swing.ButtonModel;
import javax.swing.text.Document;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunAction extends RegisteredAction {

    /** Main logger */
    static final Logger logger = LoggerFactory.getLogger(RunAction.class.getName());
    /** Class name. This name is used to register to the ActionRegistrar */
    public final static String className = RunAction.class.getName();
    /** Action name. This name is used to register to the ActionRegistrar */
    public static final String actionName = "run";

    ButtonModel iTMaxButtonModel = null;
    Document iTMaxDocument = null;
    ButtonModel skipPlotDocument = null;

    //boolean running = false;
    private Task task;

    private IRModel irModel = null;

    public RunAction() {
        super(className, actionName);
        task = new Task(actionName + "_" + this.hashCode());
    }

    public void setConstraints(ButtonModel iTMaxButtonModel, Document iTMaxDocument, ButtonModel skipPlotDocument) {
        this.iTMaxButtonModel = iTMaxButtonModel;
        this.iTMaxDocument = iTMaxDocument;
        this.skipPlotDocument = skipPlotDocument;
    }

    public void setIrModel(IRModel m) {
        irModel = m;
    }

    private void setRunningState(boolean running) {
        irModel.setRunning(running);
        putValue(Action.NAME, (running) ? "Cancel" : "Run");
        putValue(Action.LARGE_ICON_KEY, running ? ImageUtils.loadResourceIcon("fr/jmmc/jmcs/resource/image/spinner.gif") : null);
    }

    public void actionPerformed(ActionEvent e) {

        if (irModel.isRunning()) {
            // cancel job
            TaskSwingWorkerExecutor.cancelTask(getTask());
            setRunningState(false);
        } else {

            StringBuffer args = new StringBuffer();
            File tmpFile = null;
            try {
                tmpFile = irModel.prepareTempFile();
            } catch (FitsException ex) {
                logger.error("Can't prepare temporary file before running process", ex);
                setRunningState(false);
            } catch (IOException ex) {
                logger.error("Can't prepare temporary file before running process", ex);
                setRunningState(false);
            }

            StatusBar.show("Spawn " + irModel.getSelectedSoftware() + " process");
            new RunFitActionWorker(getTask(), tmpFile, args.toString(), irModel, this).executeTask();
            return;

        }
    }

    public Task getTask() {
        return task;
    }

    static class RunFitActionWorker extends TaskSwingWorker<File> {

        private final java.io.File inputFile;
        private final String methodArg;
        private final IRModel parent;
        private final RunAction parentAction;

        private static int i = 0;

        public RunFitActionWorker(Task task, java.io.File inputFile, String methodArg, IRModel sm, RunAction runAction) {
            super(task);
            this.inputFile = inputFile;
            this.methodArg = methodArg;
            this.parent = sm; // only for callback
            this.parentAction = runAction;
        }

        @Override
        public File computeInBackground() {

            // change model state to lock it and extract its snapshot
            parentAction.setRunningState(true);

            if (true) {
                logger.info("skip remote call for testing purpose. Just loop for pause");
                try {
                    int max = 10;
                    for (int i = 1; i <= max; i++) {
                        Thread.sleep(500);
                        StatusBar.show("Fake process - " + i + "/" + max);
                    }
                } catch (InterruptedException ex) {
                    logger.info("interruped during loop", ex);
                }
                // TODO perform something more elaborated here
                try {
                    OIFitsFile outputOIFitsFile = OIFitsLoader.loadOIFits(inputFile.getAbsolutePath());

                    // TODO change hdu names for images
                    for (FitsImageHDU imageHdu : outputOIFitsFile.getImageOiData().getFitsImageHDUs()) {
                        imageHdu.setHduName(imageHdu.getHduName() + i);
                        i++;
                    }

                    OIFitsWriter.writeOIFits(outputOIFitsFile.getAbsoluteFilePath(), outputOIFitsFile);

                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                } catch (FitsException ex) {
                    throw new RuntimeException(ex);
                }

                return inputFile;
            }

            return null;
            // TODO here real code
//            try {
//                return LITpro.execMethod(actionName, inputFile, methodArg);
//            } catch (IOException ex) {
//                // should only come from http io execption
//                throw new RuntimeException(ex);
//            }
        }

        @Override
        public void refreshUI(File f) {
            // action finished, we can change state and update model just after.
            parentAction.setRunningState(false);

            this.parent.updateWithNewModel(f);
        }

        @Override
        public void handleException(ExecutionException ee) {
            // notify that process is finished
            this.parent.setRunning(false);

            // filter some exceptions to avoid feedback report
            if (filter(ee)) {
                MessagePane.showErrorMessage("Please check your network setup", ee);
            } else {
                super.handleException(ee);
            }

            StatusBar.show("Error occured during process");
        }

        public boolean filter(Exception e) {
            Throwable c = e.getCause();
            return c != null
                    && (c instanceof UnknownHostException
                    || c instanceof ConnectTimeoutException);
        }
    }
}
