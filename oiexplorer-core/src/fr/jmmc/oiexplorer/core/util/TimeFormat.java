/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.util;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Custom date formatter to display hours only and HA instead of hours for the baseline limits
 *
 * @author bourgesl
 */
public final class TimeFormat extends DateFormat {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** separator (:) */
    private final static char SEPARATOR = ':';

    /* members */
    /** format also minutes */
    private final boolean useMinutes;
    /** hour angle format */
    private final boolean useHA;

    /**
     * Create a new HADateFormat
     *
     * @param useHA true to indicate to format time as hour angles
     * @param useMinutes true to indicate to format also minutes
     */
    public TimeFormat(final boolean useHA, final boolean useMinutes) {
        super();
        this.useHA = useHA;
        this.useMinutes = useMinutes;

        // using default locale (UK) :
        this.calendar = new GregorianCalendar();
    }

    /**
     * Formats a Date into a date/time string.
     *
     * It only returns the hour field minus 12 so HA are in [-12;12]
     *
     * @param date a Date to be formatted into a date/time string.
     * @param toAppendTo the string buffer for the returning date/time string.
     * @param fieldPosition unused
     * @return the string buffer passed in as toAppendTo, with formatted text appended.
     */
    @Override
    public StringBuffer format(final Date date, final StringBuffer toAppendTo,
            final FieldPosition fieldPosition) {

        final Calendar cal = getCalendar();
        cal.setTime(date);

        int h = cal.get(Calendar.HOUR_OF_DAY);
        final int m = cal.get(Calendar.MINUTE);

        if (useHA) {
            h -= 12;
        } else {
            if (h < 10) {
                toAppendTo.append('0');
            }
        }
        toAppendTo.append(h);

        if (useMinutes) {
            toAppendTo.append(SEPARATOR);
            if (m < 10) {
                toAppendTo.append('0');
            }
            toAppendTo.append(m);
        }

        return toAppendTo;
    }

    /**
     * Parse a date/time string according to the given parse position.
     *
     * NOTE: Not implemented
     *
     * @param source  The date/time string to be parsed
     * @param pos   On input, the position at which to start parsing; on
     *              output, the position at which parsing terminated, or the
     *              start position if the parse failed.
     * @return      A Date, or null if the input could not be parsed
     *
     * @throws UnsupportedOperationException as it is not implemented
     */
    @Override
    public Date parse(final String source, final ParsePosition pos)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not implemented !");
    }
}
