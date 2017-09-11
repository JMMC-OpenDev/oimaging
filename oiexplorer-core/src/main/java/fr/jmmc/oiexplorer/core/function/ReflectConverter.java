/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.function;

/**
 * This converter performs reflection (y = -x)
 * @author bourgesl
 */
public final class ReflectConverter implements Converter {

    /**
     * Compute an output value given one input value using:
     * y = -x
     * @param value input value (x)
     * @return output value (y)
     */
    @Override
    public double evaluate(final double value) {
        return -value;
    }

    /**
     * Return the optional unit label
     * @return null
     */
    @Override
    public String getUnit() {
        return null;
    }
}
