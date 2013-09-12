/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.validator.CheckHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains several static methods to validate the OI Fits structure (keywords, columns)
 * @author bourgesl
 */
public final class OIFitsChecker {

    /* members */
    /** Specific logger dedicated to validation (DO NOT MAKE IT STATIC) */
    private final Logger checkLogger;
    /** Handler associated to check logger */
    private final CheckHandler checkHandler;

    /**
     * Public constructor
     */
    public OIFitsChecker() {
        super();

        /* Build logger that will collect checking treatment results */

        // This logger is anonymous to avoid any concurrency issue ...
        checkLogger = Logger.getAnonymousLogger();
        checkLogger.setUseParentHandlers(false);

        /* Build and associate new specific handler to handle output */
        checkHandler = new CheckHandler();
        checkLogger.addHandler(checkHandler);
    }

    /**
     * Return true if the FINE level is enabled
     * @return true if the FINE level is enabled
     */
    public boolean isFineEnabled() {
        return checkLogger.isLoggable(Level.FINE);
    }

    /**
     * Add an information message
     * @param message information message
     */
    protected void fine(final String message) {
        checkLogger.fine(message);
    }

    /**
     * Add an information message
     * @param message information message
     */
    public void info(final String message) {
        checkLogger.info(message);
    }

    /**
     * Add a warning message
     * @param message warning message
     */
    public void warning(final String message) {
        checkLogger.warning(message);
    }

    /**
     * Add a severe message
     * @param message severe message
     */
    public void severe(final String message) {
        checkLogger.severe(message);
    }

    /*
     * Getter - Setter -----------------------------------------------------------
     */
    /**
     * Return the checkHandler that get informations about checking process.
     *
     * @return the checkHandler
     */
    public CheckHandler getCheckHandler() {
        return checkHandler;
    }

    /**
     * Get check handler status
     *
     * @return a string containing the status
     */
    public String getCheckStatus() {
        return checkHandler.getStatus();
    }

    /**
     * Get check report message
     *
     * @return a string containing the analysis report
     */
    public String getCheckReport() {
        return checkHandler.getReport();
    }

    /**
     * Clear the report message informations
     */
    public void clearCheckReport() {
        checkHandler.clearReport();
    }
}
