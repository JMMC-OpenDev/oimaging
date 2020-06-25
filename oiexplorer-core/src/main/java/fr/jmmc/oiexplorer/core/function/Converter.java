/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.function;

/**
 * This function defines a simple value conversion
 * @author bourgesl
 */
public interface Converter {

    /**
     * Compute an output value given one input value
     * @param value input value
     * @return output value
     */
    public double evaluate(final double value);

    /**
     * Return the optional unit label
     * @return unit label or null if undefined
     */
    public String getUnit();
}
