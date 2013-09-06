/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.image;

/**
 * This enumeration provides minimal unit conversion to standard units (rad, m)
 * @author bourgesl
 */
public enum FitsUnit {

    /** undefined Units */
    NO_UNIT(""),
    /** angle expressed in radians */
    ANGLE_RAD("rad|radian|radians"),
    /** angle expressed in degrees */
    ANGLE_DEG("deg|degree|degrees", ANGLE_RAD, (Math.PI / 180d)),
    /** angle expressed in arcminutes */
    ANGLE_ARCMIN("arcmin|arcmins", ANGLE_RAD, (Math.PI / 180d) / 60d),
    /** angle expressed in arcseconds */
    ANGLE_ARCSEC("arcsec|arcsecs", ANGLE_RAD, (Math.PI / 180d) / 3600d),
    /** wavelength expressed in meters */
    WAVELENGTH_METER("m|meter|meters"),
    /** wavelength expressed in micro meters */
    WAVELENGTH_MICRO_METER("micron|microns|micrometer|micrometers", WAVELENGTH_METER, 1e-6d),
    /** wavelength expressed in nano meters */
    WAVELENGTH_NANO_METER("nm|nanometer|nanometers", WAVELENGTH_METER, 1e-9d);

    /**
     * Unit parsing
     * @param unit unit to parse
     * @return matching FitsUnit according to its tokens
     * @throws IllegalArgumentException if unsupported unit
     */
    public static FitsUnit parseUnit(final String unit) throws IllegalArgumentException {
        if (unit == null || unit.length() == 0) {
            return NO_UNIT;
        }
        for (FitsUnit u : FitsUnit.values()) {
            if (u.checkTokens(unit)) {
                return u;
            }
        }
        throw new IllegalArgumentException("Unsupported unit: [" + unit + "] !");
    }
    /* members */
    /** string representation separated by '|' */
    private final String representation;
    /** representation array */
    private final String[] tokens;
    /** unit reference */
    private final FitsUnit reference;
    /** conversion factor to the unit reference */
    private final double factor;

    /**
     * Custom constructor
     * @param allowedValues string containing several representation separated by '|'
     */
    private FitsUnit(final String allowedValues) {
        this(allowedValues, null, Double.NaN);
    }

    /**
     * Custom constructor
     * @param allowedValues string containing several representation separated by '|'
     * @param reference unit reference
     * @param factor conversion factor to the unit reference
     */
    private FitsUnit(final String allowedValues, final FitsUnit reference, final double factor) {
        this.representation = allowedValues;
        this.tokens = this.representation.split("\\|");
        this.reference = reference;
        this.factor = factor;
    }

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
     * Return the unit reference
     * @return unit reference
     */
    public final FitsUnit getReference() {
        return this.reference;
    }

    /**
     * Convert the given value expressed in this unit to the given unit
     * @param value value to convert expressed in this unit
     * @param unit unit to convert into
     * @return converted value or given value if no conversion
     * @throws IllegalArgumentException if unit conversion is not allowed
     */
    public double convert(final double value, final FitsUnit unit) throws IllegalArgumentException {
        if (this.reference != null) {
            if (this.reference == unit) {
                // conversion is possible:
                return this.factor * value;
            }
            throw new IllegalArgumentException("Unit conversion not allowed from [" + getStandardRepresentation()
                    + "] to [" + unit.getStandardRepresentation() + "] !");
        }
        // no conversion needed:
        return value;
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
}
