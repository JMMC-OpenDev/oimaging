/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.meta;

/**
 * This enumeration describes allowed units in the OIFits standard :
 *      - noUnit             no Unit associated
 *      - unitInMeters       must be 'm' or 'meters'
 *      - unitInDegrees      must be 'deg' or 'degrees'
 *      - unitInSeconds      must be 's', 'sec' or 'seconds'
 *      - unitInMJD          must be 'day'
 *      - unitInYears        must be 'yr', 'year' or 'years'
 *      - unitInMetersPerSecond
 *                           must be 'm/s', 'm / s', 'meters per second',
 *                           'meters/second', 'meters / second'
 *      - unitInDegreesPerYear
 *                           must be 'deg/yr', 'deg/year', 'deg / year' or
 *                           'deg / yr'
 * @author bourgesl
 */
public enum Units {

    /** undefined Units */
    NO_UNIT(""),
    /** Units are expressed in meters */
    UNIT_METER("m|meter|meters"),
    /** Units are expressed in degrees */
    UNIT_DEGREE("deg|degree|degrees"),
    /** Units are expressed in seconds */
    UNIT_SECOND("s|sec|second|seconds"),
    /** Units are expressed in julian day */
    UNIT_MJD("day|days"),
    /** Units are expressed in years */
    UNIT_YEAR("yr|year|years"),
    /** Units are expressed in meters per second */
    UNIT_METER_PER_SECOND("m/s|m / s|meter per second|meters per second|meter/second|meters/second|meter / second|meters / second"),
    /** Units are expressed in degrees per year */
    UNIT_DEGREE_PER_YEAR("deg/yr|deg / yr|degree/yr|degree / yr|degrees/yr|degrees / yr|deg/year|deg / year|degree/year|degree / year|degrees/year|degrees / year"),
    /** Units are expressed in hours */
    UNIT_HOUR("h|hour|hours");

    /**
     * Custom constructor
     * @param allowedValues string containing several representation separated by '|'
     */
    private Units(final String allowedValues) {
        this.representation = allowedValues;
        this.tokens = this.representation.split("\\|");
    }
    /** string representation separated by '|' */
    private final String representation;
    /** representation array */
    private final String[] tokens;

    /**
     * Return the unit representations
     * @return string containing several representation separated by '|'
     */
    public final String getRepresentation() {
        return representation;
    }

    /**
     * Return the standard unit representation i.e. the first token
     * @return standard unit representation or ""
     */
    public final String getStandardRepresentation() {
        if (tokens != null && tokens.length > 0) {
            return tokens[0];
        }
        return "";
    }

    /**
     * Check if the given unit is present in the tokens
     * @param unit unit to check
     * @return true if the given unit is present in the tokens
     */
    private boolean checkTokens(final String unit) {
        for (String token : tokens) {
            if (token.equalsIgnoreCase(unit)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Unit analysis.
     *
     * @param unit unit to check
     * @return matching Units according to its tokens or null if no unit matches
     */
    public static Units parseUnit(final String unit) {
        if (unit == null || unit.length() == 0) {
            return NO_UNIT;
        } else if (UNIT_METER.checkTokens(unit)) {
            return UNIT_METER;
        } else if (UNIT_DEGREE.checkTokens(unit)) {
            return UNIT_DEGREE;
        } else if (UNIT_SECOND.checkTokens(unit)) {
            return UNIT_SECOND;
        } else if (UNIT_MJD.checkTokens(unit)) {
            return UNIT_MJD;
        } else if (UNIT_YEAR.checkTokens(unit)) {
            return UNIT_YEAR;
        } else if (UNIT_METER_PER_SECOND.checkTokens(unit)) {
            return UNIT_METER_PER_SECOND;
        } else if (UNIT_DEGREE_PER_YEAR.checkTokens(unit)) {
            return UNIT_DEGREE_PER_YEAR;
        } else if (UNIT_HOUR.checkTokens(unit)) {
            return UNIT_HOUR;
        }

        return null;
    }
}
