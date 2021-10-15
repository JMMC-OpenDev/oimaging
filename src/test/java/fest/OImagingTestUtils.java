/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fest;

import static org.fest.swing.timing.Pause.*;
import org.fest.swing.timing.Timeout;

import fr.jmmc.jmcs.gui.task.TaskSwingWorkerExecutor;
import org.fest.swing.edt.GuiActionRunner;
import org.fest.swing.edt.GuiQuery;
import org.fest.swing.timing.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains utility methods
 */
public final class OImagingTestUtils {

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(OImagingTestUtils.class.getName());
    /** 20s timeout */
    private static final Timeout LONG_TIMEOUT = Timeout.timeout(20000l);

    /**
     * Forbidden constructor
     */
    private OImagingTestUtils() {
        super();
    }

    /**
     * Waits until the computation tasks are done (+ timeout)
     */
    public static void checkRunningTasks() {
        logger.debug("checkRunningTasks : enter");

        pause(200);

        pause(new Condition("TaskRunning") {

            /**
             * Checks if the condition has been satisfied.
             * @return <code>true</code> if the condition has been satisfied, otherwise <code>false</code>.
             */
            @Override
            public boolean test() {

                return GuiActionRunner.execute(new GuiQuery<Boolean>() {

                    @Override
                    protected Boolean executeInEDT() {
                        final boolean done = !TaskSwingWorkerExecutor.isTaskRunning();

                        if (logger.isDebugEnabled()) {
                            logger.debug("checkRunningTasks : test = {}", done);
                        }
                        return done;
                    }
                });

            }
        }, LONG_TIMEOUT);

        logger.debug("checkRunningTasks : exit");
    }
}
