/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.function;

/**
 * This converter performs linear conversion (y = a.x + b)
 * @author bourgesl
 */
public final class LinearConverter implements Converter {

    /* members */
    /** scaling factor (a) */
    private final double scalingFactor;
    /** constant part (b) */
    private final double constant;
    /** optional unit label (may be null) */
    private final String unit;

    /**
     * Public constructor
     * @param scalingFactor scaling factor (a)
     * @param constant constant part (b)
     * @param unit optional unit label (may be null)
     */
    public LinearConverter(final double scalingFactor, final double constant, final String unit) {
        this.scalingFactor = scalingFactor;
        this.constant = constant;
        this.unit = unit;
    }

    /**
     * Compute an output value given one input value using:
     * y = a.x + b
     * @param value input value (x)
     * @return output value (y)
     */
    @Override
    public double evaluate(final double value) {
        return scalingFactor * value + constant;
    }

    /**
     * Return the optional unit label
     * @return unit label or null if undefined
     */
    @Override
    public String getUnit() {
        return unit;
    }
}
