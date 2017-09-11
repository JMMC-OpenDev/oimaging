/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.meta;

import fr.jmmc.oitools.model.OIFitsChecker;
import java.util.logging.Level;

/**
 * This class describes a FITS keyword.
 * 
 * Notes :
 * - OIFits uses only 'A', 'I', 'D' types for keywords => Other types are not supported for keywords.
 * - Keyword units are only useful for XML representation of an OIFits file (not defined in FITS)
 *
 * @author bourgesl
 */
public class KeywordMeta extends CellMeta {

    /**
     * KeywordMeta class constructor
     *
     * @param name keyword name
     * @param desc keyword descriptive comment
     * @param dataType keyword data type
     */
    public KeywordMeta(final String name, final String desc, final Types dataType) {
        super(MetaType.KEYWORD, name, desc, dataType, false, false, 1, NO_INT_VALUES, NO_STR_VALUES, Units.NO_UNIT);
    }

    /**
     * KeywordMeta class constructor
     *
     * @param name keyword name
     * @param desc keyword descriptive comment
     * @param dataType keyword data type
     * @param unit keyword unit
     */
    public KeywordMeta(final String name, final String desc, final Types dataType, final Units unit) {
        super(MetaType.KEYWORD, name, desc, dataType, false, false, 1, NO_INT_VALUES, NO_STR_VALUES, unit);
    }

    /**
     * KeywordMeta class constructor
     *
     * @param name keyword name
     * @param desc keyword descriptive comment
     * @param dataType keyword data type
     * @param optional
     * @param acceptedValues
     */
    public KeywordMeta(final String name, final String desc, final Types dataType, final boolean optional, final String[] acceptedValues) {
        super(MetaType.KEYWORD, name, desc, dataType, optional, false, 1, NO_INT_VALUES, acceptedValues, Units.NO_UNIT);
    }

    /**
     * KeywordMeta class constructor
     *
     * @param name keyword name
     * @param desc keyword descriptive comment
     * @param dataType keyword data type
     * @param unit keyword unit
     * @param optional
     * @param is3D
     */
    public KeywordMeta(final String name, final String desc, final Types dataType, final Units unit, final boolean optional, boolean is3D) {
        super(MetaType.KEYWORD, name, desc, dataType, optional, is3D, 1, NO_INT_VALUES, NO_STR_VALUES, unit);
    }

    /**
     * KeywordMeta class constructor with integer possible values
     *
     * @param name keyword name
     * @param desc keyword descriptive comment
     * @param dataType keyword data type
     * @param acceptedValues integer possible values
     */
    public KeywordMeta(final String name, final String desc, final Types dataType, final short[] acceptedValues) {
        super(MetaType.KEYWORD, name, desc, dataType, false, false, 1, acceptedValues, NO_STR_VALUES, Units.NO_UNIT);
    }

    /**
     * KeywordMeta class constructor with string possible values
     *
     * @param name keyword name
     * @param desc keyword descriptive comment
     * @param dataType keyword data type
     * @param acceptedValues string possible values
     */
    public KeywordMeta(final String name, final String desc, final Types dataType, final String[] acceptedValues) {
        super(MetaType.KEYWORD, name, desc, dataType, false, false, 1, NO_INT_VALUES, acceptedValues, Units.NO_UNIT);
    }

    /**
     * Get the data type corresponding to the given value
     * Does not support an array value
     *
     * @param value keyword value
     *
     * @return data type if it is known, null otherwise.
     */
    protected final Types getDataType(final Object value) {
        if (value instanceof String) {
            return Types.TYPE_CHAR;
        }
        if (value instanceof Double) {
            return Types.TYPE_DBL;
        }
        if (value instanceof Short) {
            return Types.TYPE_SHORT;
        }
        if (value instanceof Integer) {
            return Types.TYPE_INT;
        }
        if (value instanceof Float) {
            return Types.TYPE_REAL;
        }
        if (value instanceof Boolean) {
            return Types.TYPE_LOGICAL;
        }

        logger.log(Level.SEVERE, "{0} keyword type for : ''{1}'' is not supported.", new Object[]{getName(), value});

        return null;
    }

    /**
     * Check if the given keyword value is valid.
     *
     * @param value keyword value to check
     * @param checker checker component
     */
    public final void check(final Object value, final OIFitsChecker checker) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "check : {0} = {1}", new Object[]{getName(), value});
        }

        // Check type
        final Types kDataType = Types.getDataType(value.getClass());

        if (kDataType != this.getDataType()) {
            checker.severe("Invalid format for keyword '" + this.getName() + "', found '" + kDataType.getRepresentation() + "' should be '" + this.getType() + "'");
        } else {
            // Check accepted value
            checkAcceptedValues(value, checker);
        }
    }

    /**
     * If any are mentioned, check keyword values are fair.
     *
     * @param value keyword value to check
     * @param checker checker component
     */
    private void checkAcceptedValues(final Object value, final OIFitsChecker checker) {
        final short[] intAcceptedValues = getIntAcceptedValues();
        final String[] stringAcceptedValues = getStringAcceptedValues();

        if (intAcceptedValues.length != 0) {
            final short val = ((Number) value).shortValue();

            for (int i = 0, len = intAcceptedValues.length; i < len; i++) {
                if (val == intAcceptedValues[i]) {
                    return;
                }
            }

            checker.severe("Invalid value for keyword '" + this.getName() + "', found '" + val + "' should be '" + getIntAcceptedValuesAsString() + "'");

        } else if (stringAcceptedValues.length != 0) {
            final String val = (String) value;

            for (int i = 0, len = stringAcceptedValues.length; i < len; i++) {
                if (val.equals(stringAcceptedValues[i])) {
                    return;
                }
            }

            checker.severe("Invalid value for keyword '" + this.getName() + "', found '" + val + "' should be '" + getStringAcceptedValuesAsString() + "'");
        }
    }
}
