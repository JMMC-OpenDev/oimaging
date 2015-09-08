/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

/**
 * Validation message holder
 * @author bourgesl
 */
public final class CheckMessage {

    /** message level */
    public enum Level {

        /** information */
        Information("INFO"),
        /** warning */
        Warning("WARNING"),
        /** error */
        Error("SEVERE"),
        /** disabled */
        Disabled("OFF");

        /* members */
        /** level's label in logs */
        private final String label;

        Level(final String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }
    /* members */
    /** message */
    private final String message;
    /** message level */
    private final Level level;

    /**
     * Protected Constructor
     * @param level message level
     * @param message message
     */
    public CheckMessage(final Level level, final String message) {
        this.level = level;
        this.message = message;
    }

    /**
     * Return the message
     * @return message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Return the message level 
     * @return message level 
     */
    public Level getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return "CheckMessage{level=" + level + ", message='" + message + '}';
    }
}
