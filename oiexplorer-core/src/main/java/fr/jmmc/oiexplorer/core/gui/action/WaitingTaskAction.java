/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.action;

import fr.jmmc.jmcs.gui.action.RegisteredAction;
import fr.jmmc.jmcs.gui.task.TaskSwingWorkerExecutor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This specific action executes only when there is no running task and waits for task to complete before executing.
 *
 * Note : when an action is waiting for completion, other action calls are discarded.
 *
 * @author bourgesl
 */
public abstract class WaitingTaskAction extends RegisteredAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(WaitingTaskAction.class.getName());
    /** delay in milliseconds between each poll to check if there is still a task running */
    private final static int POLLING_DELAY = 100;
    /** shared flag to know if there is already a  pending action */
    private static boolean pending = false;

    /**
     * Constructor, that automatically register the action in RegisteredAction.
     * Action name, icon, accelerator and description is first initialized using
     * fieldName to build a MCSAction.
     * @param classPath the path of the class containing the field pointing to
     * the action, in the form returned by 'getClass().getName();'.
     * @param fieldName the name of the field pointing to the action.
     */
    public WaitingTaskAction(final String classPath, final String fieldName) {
        super(classPath, fieldName);
    }

    /**
     * Handle the action event
     * @param ae action event
     */
    @Override
    public final void actionPerformed(final ActionEvent ae) {
        logger.debug("actionPerformed");

        // If there is already a pending action ?
        if (pending) {
            // discard this action event :

            if (logger.isDebugEnabled()) {
                logger.debug("discard action (another action is pending): {}", this.getClass().getName());
            }
            return;
        }

        // check if there is any running task :
        if (TaskSwingWorkerExecutor.isTaskRunning()) {
            // indicate to other actions that this action is pending for execution :
            pending = true;

            // delay the delegate action until there is no running task :
            new DelayedActionPerformer(this).start();

        } else {
            logger.debug("execute action : {}", this);

            actionPerformed();
        }
    }

    /**
     * This inner class uses a timer internally to check if there is still running tasks.
     */
    private final static class DelayedActionPerformer implements ActionListener {

        /** defered action */
        private final WaitingTaskAction adapter;
        /** Swing timer */
        private Timer timer;

        /**
         * Constructor that creates a timer to delay the action execution
         * @param adapter WaitingTaskAction
         */
        protected DelayedActionPerformer(final WaitingTaskAction adapter) {
            this.adapter = adapter;

            this.timer = new Timer(POLLING_DELAY, this);

            this.timer.setRepeats(true);
            this.timer.setCoalesce(false);
        }

        /**
         * Starts the <code>Timer</code>
         */
        void start() {
            this.timer.start();
        }

        /**
         * Handle the timer calls until there is no running task
         * @param ae action event
         */
        @Override
        public void actionPerformed(final ActionEvent ae) {
            final boolean taskRunning = TaskSwingWorkerExecutor.isTaskRunning();

            if (logger.isDebugEnabled()) {
                logger.debug("running task : {}", taskRunning);
            }

            if (!taskRunning) {
                // indicate to other actions that this action is no more pending :
                pending = false;

                // stop this timer :
                this.timer.stop();

                logger.debug("execute action : {}", this.adapter);

                // execute the action :
                this.adapter.actionPerformed();
            }
        }
    }

    /**
     * Handle the action event
     */
    public abstract void actionPerformed();
}
