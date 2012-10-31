/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.function;

/**
 * This converter performs scaling conversion (y = a.x)
 * @author bourgesl
 */
public final class ScalingConverter implements Converter {

    /* members */
    /** scaling factor (a) */
    private final double scalingFactor;
    /** optional unit label (may be null) */
    private final String unit;

    /**
     * Public constructor
     * @param scalingFactor scaling factor (a)
     * @param unit optional unit label (may be null)
     */
    public ScalingConverter(final double scalingFactor, final String unit) {
        this.scalingFactor = scalingFactor;
        this.unit = unit;
    }

    /**
     * Compute an output value given one input value using:
     * y = a.x
     * @param value input value (x)
     * @return output value (y)
     */
    @Override
    public double evaluate(final double value) {
        return scalingFactor * value;
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
