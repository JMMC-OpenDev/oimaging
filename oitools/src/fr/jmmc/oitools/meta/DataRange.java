/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.meta;

/**
 * This range class represents default range for any column data
 * @author bourgesl
 */
public final class DataRange {

    /** data range representing strictly positive numbers [0; +Inf] ie invalid values are replaced by NaN (see OIFits checker) */
    public final static DataRange RANGE_POSITIVE_STRICT = new DataRange(0d, Double.NaN);
    /** data range representing positive numbers [0; +Inf] but non strict */
    public final static DataRange RANGE_POSITIVE = new DataRange(0d, Double.NaN);

    /* members */
    /** min value (may be NaN to indicate no min value) */
    private final double min;
    /** max value (may be NaN to indicate no max value) */
    private final double max;

    /**
     * Public constructor
     * @param min min value
     * @param max max value
     */
    public DataRange(final double min, final double max) {
        this.min = min;
        this.max = max;
    }

    /**
     * Return the min value
     * @return min value
     */
    public double getMin() {
        return min;
    }

    /**
     * Return the max value
     * @return max value
     */
    public double getMax() {
        return max;
    }
}
