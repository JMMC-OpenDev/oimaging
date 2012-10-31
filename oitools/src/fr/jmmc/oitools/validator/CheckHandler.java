/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * This class stores the checking results.
 */
public final class CheckHandler extends Handler {

    /* constants */
    /** Formatter associated to checkHandler */
    private final static Formatter formatter = new CheckFormatter();

    /* members */
    /** Vector storing all records */
    private final List<LogRecord> records;

    /** 
     * CheckHandler class constructor.
     */
    public CheckHandler() {
        this.records = new ArrayList<LogRecord>(50);
        setFormatter(formatter);
        setLevel(Level.INFO);
    }

    /** 
     * Close the CheckHandler and free all associated resources
     */
    @Override
    public void close() {
    }

    /** 
     * Flush any buffered output
     */
    @Override
    public void flush() {
    }

    /**
     * Publish a LogRecord.
     *
     * @param record LogRecord to be published.
     */
    @Override
    public void publish(final LogRecord record) {
        if (isLoggable(record)) {
            records.add(record);
        }
    }

    /** 
     * Get number of warning errors
     * @return number of warning errors
     */
    public int getNbWarnings() {
        int warnings = 0;

        LogRecord record;
        for (int i = 0, len = records.size(); i < len; i++) {
            record = records.get(i);

            if (record.getLevel() == Level.WARNING) {
                warnings++;
            }
        }

        return warnings;
    }

    /**
     * Get number of severe errors
     * @return number of severe errors 
     */
    public int getNbSeveres() {
        int severes = 0;

        LogRecord record;
        for (int i = 0, len = records.size(); i < len; i++) {
            record = records.get(i);

            if (record.getLevel() == Level.SEVERE) {
                severes++;
            }
        }

        return severes;
    }

    /**
     * Return a simple text report that log past check.
     *
     * @return the string with report content.
     */
    public String getReport() {
        final int len = records.size();
        final StringBuilder sb = new StringBuilder(len * 50);

        LogRecord record;
        for (int i = 0; i < len; i++) {
            record = records.get(i);

            sb.append(formatter.format(record)).append("\n");
        }
        sb.append("\n").append(getStatus());

        return sb.toString();
    }

    /**
     * Return a simple string that show numbers of warnings and severe errors.
     *
     * @return a string with number of warnings and severe errors.
     */
    public String getStatus() {
        int warnings = 0;
        int severes = 0;

        LogRecord record;
        for (int i = 0, len = records.size(); i < len; i++) {
            record = records.get(i);

            if (record.getLevel() == Level.WARNING) {
                warnings++;
            } else if (record.getLevel() == Level.SEVERE) {
                severes++;
            }
        }

        return warnings + " warnings, " + severes + " severe errors";
    }

    /**
     * Clear previously collected report informations
     */
    public void clearReport() {
        records.clear();
    }
}
/*___oOo___*/
