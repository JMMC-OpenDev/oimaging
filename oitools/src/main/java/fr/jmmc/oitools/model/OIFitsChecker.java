/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.model.CheckMessage.Level;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains several static methods to validate the OI Fits structure (keywords, columns)
 *
 * TODO: add error codes to messages ...
 *
 * @author bourgesl
 */
public final class OIFitsChecker {

    /* members */
    /** List storing all validation message */
    private final ArrayList<CheckMessage> records;

    /** Don't record message while true */
    private boolean recordMessages = true;

    /** OIFITS2: temporary state to check correlation indexes keyed by CORRNAME */
    private final Map<String, OIFitsCorrChecker> corrCheckers = new HashMap<String, OIFitsCorrChecker>();

    /**
     * Public constructor
     */
    public OIFitsChecker() {
        this.records = new ArrayList<CheckMessage>(32);
    }

    /**
     * Clear the map
     */
    void cleanup() {
        corrCheckers.clear();
    }

    /**
     * Add an information message
     * @param message information message
     */
    public void info(final String message) {
        addMessage(Level.Information, message);
    }

    /**
     * Add a warning message
     * @param message warning message
     */
    public void warning(final String message) {
        addMessage(Level.Warning, message);
    }

    /**
     * Add a severe message
     * @param message severe message
     */
    public void severe(final String message) {
        addMessage(Level.Error, message);
    }

    private void addMessage(final Level level, final String message) {
        if (this.recordMessages) {
            this.records.add(new CheckMessage(level, message));
        }
    }

    /** 
     * Get number of warning errors
     * @return number of warning errors
     */
    public int getNbWarnings() {
        int warnings = 0;

        CheckMessage record;
        for (int i = 0, len = records.size(); i < len; i++) {
            record = records.get(i);

            if (record.getLevel() == Level.Warning) {
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

        CheckMessage record;
        for (int i = 0, len = records.size(); i < len; i++) {
            record = records.get(i);

            if (record.getLevel() == Level.Error) {
                severes++;
            }
        }

        return severes;
    }

    /**
     * Return a simple string that show numbers of warnings and severe errors.
     *
     * @return a string with number of warnings and severe errors.
     */
    public String getCheckStatus() {
        return getNbWarnings() + " warnings, " + getNbSeveres() + " severe errors";
    }

    /**
     * Get the checker's report
     *
     * @return a string containing the analysis report
     */
    public String getCheckReport() {
        final int len = records.size();
        final StringBuilder sb = new StringBuilder(len * 50);

        CheckMessage record;
        for (int i = 0; i < len; i++) {
            record = records.get(i);

            sb.append(record.getLevel().getLabel()).append('\t')
                    .append(record.getMessage()).append("\n");
        }
        sb.append("\n").append(getCheckStatus());

        return sb.toString();
    }

    /**
     * Append the checker's report as Xml elements
     * @param buffer append into
     */
    public void appendReportAsXml(final StringBuilder buffer) {
        buffer.append("<validation warn=\"").append(getNbWarnings()).append("\" error=\"")
                .append(getNbSeveres()).append("\">\n");

        CheckMessage record;
        for (int i = 0, len = records.size(); i < len; i++) {
            record = records.get(i);

            buffer.append("<message level=\"").append(record.getLevel().getLabel()).append("\">");
            buffer.append(record.getMessage()).append("</message>\n");
        }
        buffer.append("</validation>\n");
    }

    /**
     * Clear the validation messages
     */
    public void clearCheckReport() {
        records.clear();
    }

    /**
     * Change record mode.
     * @param flag true stop message recording, else add every message in memory
     */
    public void ignoreMessages(boolean flag) {
        this.recordMessages = !flag;
    }

    /**
     * Give OIFitsCorrChecker map
    @param corrname
    @return corrChecker
     */
    public OIFitsCorrChecker getCorrChecker(final String corrname) {
        OIFitsCorrChecker corrChecker = corrCheckers.get(corrname);
        if (corrChecker == null) {
            corrChecker = new OIFitsCorrChecker();
            corrCheckers.put(corrname, corrChecker);
        }
        return corrChecker;
    }
}
