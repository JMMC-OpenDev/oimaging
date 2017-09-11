/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.meta;

/**
 * This enumeration describes allowed data types in the OIFits standard :
 *   - 'A' : character
 *   - 'I' : integer (16-bit signed integers)
 *   - 'J' : integer (32 bits)
 *   - 'E' : real    (32 bits)
 *   - 'D' : double  (64 bits)
 *   - 'L' : logical (true/false)
 * We added the complex type (VISDATA) :
 *   - 'C' : complex (2 float values ie 2x32 bits)
 * @author bourgesl
 */
public enum Types {

    /** character/date data type */
    TYPE_CHAR('A'),
    /** integer data type (16-bit signed integers) */
    TYPE_SHORT('I'),
    /** integer data type (32-bit signed integers) */
    TYPE_INT('J'),
    /** real data type */
    TYPE_REAL('E'),
    /** double data type */
    TYPE_DBL('D'),
    /** logical data type */
    TYPE_LOGICAL('L'),
    /** complex data type */
    TYPE_COMPLEX('C');

    /**
     * Custom constructor
     * @param representation fits char representation
     */
    private Types(final char representation) {
        this.representation = representation;
    }
    /** fits char representation */
    private final char representation;

    /**
     * Return the Fits Type character
     * @return Fits Type character
     */
    public char getRepresentation() {
        return representation;
    }

    /**
     * Return the java class representation of the given data type
     * @param type data type
     * @return java class representation
     */
    public static Class<?> getBaseClass(final Types type) {
        switch (type) {
            case TYPE_CHAR:
                return String.class;
            case TYPE_DBL:
                return double.class;
            case TYPE_SHORT:
                return short.class;
            case TYPE_INT:
                return int.class;
            case TYPE_REAL:
            case TYPE_COMPLEX:
                return float.class;
            case TYPE_LOGICAL:
                return boolean.class;
            default:
                return null;
        }
    }

    /**
     * Get the data type corresponding to the given value class
     * Does not support an array value and 
     * does not guess Complex type (return TYPE_REAL)
     *
     * @param clazz value class
     *
     * @return data type if it is known, null otherwise.
     */
    public static Types getDataType(final Class<?> clazz) {

        if (clazz == String.class) {
            return Types.TYPE_CHAR;
        }
        if (clazz == Double.class) {
            return Types.TYPE_DBL;
        }
        if (clazz == double.class) {
            return Types.TYPE_DBL;
        }
        if (clazz == Integer.class) {
            return Types.TYPE_INT;
        }
        if (clazz == int.class) {
            return Types.TYPE_INT;
        }
        if (clazz == Short.class) {
            return Types.TYPE_SHORT;
        }
        if (clazz == short.class) {
            return Types.TYPE_SHORT;
        }
        if (clazz == Float.class) {
            return Types.TYPE_REAL;
        }
        if (clazz == float.class) {
            return Types.TYPE_REAL;
        }
        if (clazz == Boolean.class) {
            return Types.TYPE_LOGICAL;
        }
        if (clazz == boolean.class) {
            return Types.TYPE_LOGICAL;
        }

        return null;
    }
}
