/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui;

import fr.jmmc.jmcs.gui.task.Task;
import fr.jmmc.jmcs.gui.task.TaskRegistry;

/**
 * This class describes the OIExplorer tasks associated with SwingWorker(s).
 *
 * @author bourgesl
 */
public class OIExplorerTaskRegistry extends TaskRegistry {

    /** task registry singleton */
    private final static OIExplorerTaskRegistry _instance;

    /* OIExplorer tasks */
    /** load OIFits files */
    public final static Task TASK_LOAD_OIFITS;

    /**
     * Static initializer to define tasks and their child tasks
     */
    static {
        // create the task registry singleton :
        _instance = new OIExplorerTaskRegistry();

        // create tasks :
        TASK_LOAD_OIFITS = new Task("LoadOIFits");

        // register tasks :
        _instance.addTask(TASK_LOAD_OIFITS);
    }

    /**
     * Singleton pattern for the registry itself
     * @return registry instance
     */
    public static TaskRegistry getInstance() {
        return _instance;
    }

    /**
     * Protected constructor
     */
    protected OIExplorerTaskRegistry() {
        super();
    }
}
