/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

/**
 * This is the base class for OIFitsFile and OITable classes
 * @author bourgesl
 */
public abstract class ModelBase {
    /* constants */

    /** undefined short value = -32768 */
    public final static short UNDEFINED_SHORT = Short.MIN_VALUE;
    /** undefined float value = NaN */
    public final static float UNDEFINED_FLOAT = Float.NaN;
    /** undefined float value = NaN */
    public final static double UNDEFINED_DBL = Double.NaN;
    /** undefined String value = "" */
    public final static String UNDEFINED_STRING = "";
    /** Logger associated to model classes */
    protected final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger("fr.jmmc.oitools.model");
    /** empty int array */
    protected final static short[] EMPTY_SHORT_ARRAY = new short[0];
    /** empty float array */
    protected final static float[] EMPTY_FLOAT_ARRAY = new float[0];
    /** empty double array */
    protected final static double[] EMPTY_DBL_ARRAY = new double[0];
    /** empty String array */
    protected final static String[] EMPTY_STRING = new String[0];

    /**
     * Public constructor
     */
    public ModelBase() {
        super();
    }

    /**
     * Implements the Visitor pattern 
     * @param visitor visitor implementation
     */
    public abstract void accept(final ModelVisitor visitor);

    /**
     * Utility method for <code>equals()</code> methods.
     *
     * @param o1 one object
     * @param o2 another object
     *
     * @return <code>true</code> if they're both <code>null</code> or both equal
     */
    public static boolean areEquals(final Object o1, final Object o2) {
        if ((o1 != o2) && ((o1 == null) || !o1.equals(o2))) {
            return false;
        }

        return true;
    }

    /**
     * Return true if the given value is the undefined short value (blanking)
     * @param val value to check
     * @return true if the given value is the undefined short value (blanking)
     */
    public static boolean isUndefined(final short val) {
        return (val == UNDEFINED_SHORT);
    }
}
