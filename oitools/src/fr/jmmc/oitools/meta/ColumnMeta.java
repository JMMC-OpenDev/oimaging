/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.meta;

import fr.jmmc.oitools.model.OIFitsChecker;
import fr.nom.tam.util.ArrayFuncs;
import java.util.logging.Level;

/**
 * This class describes a FITS column
 * @author bourgesl
 */
public class ColumnMeta extends CellMeta {

    /* members */
    /** optional column name storing error values (may be null) */
    private final String errName;
    /** optional data range (may be null) */
    private final DataRange dataRange;

    /**
     * ColumnMeta class constructor with cardinality of 1 and without unit
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     */
    public ColumnMeta(final String name, final String desc, final Types dataType) {
        this(name, desc, dataType, 1, NO_INT_VALUES, NO_STR_VALUES, Units.NO_UNIT, null, null);
    }

    /**
     * ColumnMeta class constructor with the given cardinality
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     * @param repeat column cardinality
     */
    public ColumnMeta(final String name, final String desc, final Types dataType, final int repeat) {
        this(name, desc, dataType, repeat, NO_INT_VALUES, NO_STR_VALUES, Units.NO_UNIT, null, null);
    }

    /**
     * ColumnMeta class constructor with the given cardinality and unit
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     * @param unit column unit
     */
    public ColumnMeta(final String name, final String desc, final Types dataType, final Units unit) {
        this(name, desc, dataType, 1, NO_INT_VALUES, NO_STR_VALUES, unit, null, null);
    }

    /**
     * ColumnMeta class constructor with the given cardinality and unit
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     * @param unit column unit
     * @param dataRange optional data range (may be null)
     */
    public ColumnMeta(final String name, final String desc, final Types dataType, final Units unit, final DataRange dataRange) {
        this(name, desc, dataType, 1, NO_INT_VALUES, NO_STR_VALUES, unit, null, dataRange);
    }

    /**
     * ColumnMeta class constructor with the given cardinality and unit
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     * @param repeat column cardinality
     * @param unit column unit
     */
    public ColumnMeta(final String name, final String desc, final Types dataType, final int repeat, final Units unit) {
        this(name, desc, dataType, repeat, NO_INT_VALUES, NO_STR_VALUES, unit, null, null);
    }

    /**
     * ColumnMeta class constructor with the given cardinality and unit
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     * @param repeat column cardinality
     * @param unit column unit
     * @param errName optional column name storing error values (may be null)
     */
    public ColumnMeta(final String name, final String desc, final Types dataType, final int repeat,
            final Units unit, final String errName) {
        this(name, desc, dataType, repeat, NO_INT_VALUES, NO_STR_VALUES, unit, errName, null);
    }

    /**
     * ColumnMeta class constructor with the given cardinality and unit
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     * @param repeat column cardinality
     * @param unit column unit
     * @param errName optional column name storing error values (may be null)
     * @param dataRange optional data range (may be null)
     */
    public ColumnMeta(final String name, final String desc, final Types dataType, final int repeat,
            final Units unit, final String errName, final DataRange dataRange) {
        this(name, desc, dataType, repeat, NO_INT_VALUES, NO_STR_VALUES, unit, errName, dataRange);
    }

    /**
     * Private ColumnMeta class constructor with the given cardinality and string possible values
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     * @param repeat column cardinality
     * @param stringAcceptedValues string possible values for column/keyword
     */
    public ColumnMeta(final String name, final String desc, final Types dataType,
            final int repeat, final String[] stringAcceptedValues) {
        this(name, desc, dataType, repeat, NO_INT_VALUES, stringAcceptedValues, Units.NO_UNIT, null, null);
    }

    /**
     * Private ColumnMeta class constructor with the given cardinality and string possible values
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     * @param repeat column cardinality
     * @param intAcceptedValues integer possible values for column/keyword
     * @param stringAcceptedValues string possible values for column/keyword
     * @param unit keyword/column unit
     * @param errName optional column name storing error values (may be null)
     * @param dataRange optional data range (may be null)
     */
    private ColumnMeta(final String name, final String desc, final Types dataType,
            final int repeat, final short[] intAcceptedValues, final String[] stringAcceptedValues,
            final Units unit, final String errName, final DataRange dataRange) {
        super(MetaType.COLUMN, name, desc, dataType, repeat, intAcceptedValues, stringAcceptedValues, unit);
        this.errName = errName;
        this.dataRange = dataRange;
    }

    /**
     * Return true if the value is multiple (array)
     * @return true if the value is multiple
     */
    public final boolean isArray() {
        return getRepeat() > 1 || this instanceof WaveColumnMeta;
    }

    /**
     * Return true if the column is optional
     * Can be overriden
     * @return true if the column is optional
     */
    public boolean isOptional() {
        return false;
    }

    /* ---  Error relationships --- */
    /**
     * Return the optional column name storing error values (may be null)
     * @return optional column name storing error values (may be null)
     */
    public final String getErrorColumnName() {
        return errName;
    }

    /* ---  Data range --- */
    /**
     * Return the optional data range (may be null)
     * @return optional data range (may be null)
     */
    public final DataRange getDataRange() {
        return dataRange;
    }

    /* ---  checker --- */
    /**
     * Check if the input column is valid.
     *
     * @param value column data to check
     * @param nbRows number of rows in the column
     * @param checker checker component
     */
    public final void check(final Object value, final int nbRows, final OIFitsChecker checker) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("check : " + getName() + " = " + ArrayFuncs.arrayDescription(value));
        }

        // Check type and cardinality
        final Class<?> baseClass = ArrayFuncs.getBaseClass(value);
        char columnType = Types.getDataType(baseClass).getRepresentation();

        final int[] dims = ArrayFuncs.getDimensions(value);
        final int ndims = dims.length;

        // check rows
        final int columnRows = dims[0];
        if (columnRows != nbRows) {
            checker.severe("Invalid length for column '" + this.getName() + "', found " + columnRows
                    + "row(s) should be " + nbRows + " row(s)");
        }

        int columnRepeat;
        if (ndims == 1) {
            columnRepeat = 1;

            if (columnType == Types.TYPE_CHAR.getRepresentation()) {
                final String[] strings = (String[]) value;

                // For Strings, repeat corresponds to the number of characters :
                int max = 0;
                String val;
                for (int i = 0, len = strings.length; i < len; i++) {
                    val = strings[i];
                    if (val == null) {
                        // fix null value by empty value:
                        strings[i] = ""; 
                    } else {
                        if (val.length() > max) {
                            max = val.length();
                        }
                    }
                }
                columnRepeat = max;
            }

        } else {
            columnRepeat = dims[1];

            if (ndims > 2) {
                // special case for Complex type :
                if (ndims == 3) {
                    if (dims[2] == 2 && baseClass == float.class) {
                        columnType = Types.TYPE_COMPLEX.getRepresentation();
                    } else {
                        logger.severe("unsupported array dimensions : " + ArrayFuncs.arrayDescription(value));
                    }
                } else {
                    logger.severe("unsupported array dimensions : " + ArrayFuncs.arrayDescription(value));
                }
            }
        }

        // Note : ColumnMeta.getRepeat() is lazily computed for cross-reference columns
        final char descType = this.getType();
        final int descRepeat = this.getRepeat();

        if (descRepeat > 0) {
            boolean severe = false;

            if (columnType != descType) {
                severe = true;
            } else {
                if (columnType == Types.TYPE_CHAR.getRepresentation()) {
                    // For String values, report only errors when the maximum length is exceeded.
                    if (columnRepeat > descRepeat) {
                        severe = true;
                    }
                } else {
                    if (columnRepeat != descRepeat) {
                        severe = true;
                    }
                }
            }

            if (severe) {
                checker.severe("Invalid format for column '" + this.getName() + "', found '" + columnRepeat + columnType
                        + "' should be '" + descRepeat + descType + "'");
            }
        } else {
            checker.warning("Can't check repeat for column '" + this.getName() + "'");

            if (columnType != descType) {
                checker.severe("Invalid format for column '" + this.getName() + "', found '" + columnType
                        + "' should be '" + descType + "'");
            }
        }

        // skip check units as the raw object has not this information.

        // Check accepted value
        checkAcceptedValues(value, columnRows, columnRepeat, checker);
    }

    /**
     * If any are mentioned, check column values are fair.
     *
     * @param value column data to check
     * @param columnRows number of rows in the given column
     * @param columnRepeat number of values per row in the given column
     * @param checker checker component
     */
    private void checkAcceptedValues(final Object value, final int columnRows, final int columnRepeat, final OIFitsChecker checker) {
        boolean error;

        final short[] intAcceptedValues = getIntAcceptedValues();
        final String[] stringAcceptedValues = getStringAcceptedValues();

        if (intAcceptedValues.length != 0) {
            // OIData : STA_INDEX or TARGET_ID

            final short[] single = new short[1];
            short[] values;

            for (int rowNb = 0; rowNb < columnRows; rowNb++) {
                if (columnRepeat > 1) {
                    values = ((short[][]) value)[rowNb];
                } else {
                    single[0] = ((short[]) value)[rowNb];
                    values = single;
                }

                for (int r = 0, rlen = values.length; r < rlen; r++) {
                    error = true;

                    for (int i = 0, len = intAcceptedValues.length; i < len; i++) {
                        if (values[r] == intAcceptedValues[i]) {
                            error = false;
                        }
                    }

                    if (error) {
                        if (values.length > 1) {
                            checker.severe("Invalid value at index " + r + " for column '" + this.getName() + "' line " + rowNb + ", found '" + values[r] + "' should be '" + getIntAcceptedValuesAsString() + "'");
                        } else {
                            checker.severe("Invalid value for column '" + this.getName() + "' line " + rowNb + ", found '" + values[r] + "' should be '" + getIntAcceptedValuesAsString() + "'");
                        }
                    }
                }
            }
        } else if (stringAcceptedValues.length != 0) {
            // OITarget : VELTYP, VELDEF

            final String[] sValues = (String[]) value;

            String val;
            for (int rowNb = 0; rowNb < columnRows; rowNb++) {
                error = true;

                val = sValues[rowNb];

                if (val == null) {
                    val = "";
                } else {
                    for (int i = 0, len = stringAcceptedValues.length; i < len; i++) {
                        if (val.equals(stringAcceptedValues[i])) {
                            error = false;
                            break;
                        }
                    }
                }

                if (error) {
                    checker.severe("Invalid value for column '" + this.getName() + "' line " + rowNb + ", found '" + val + "' should be '" + getStringAcceptedValuesAsString() + "'");
                }
            }
        }
    }
}
