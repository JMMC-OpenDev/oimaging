/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.meta;

import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.model.ModelBase;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.Rule;
import fr.nom.tam.util.ArrayFuncs;
import java.util.logging.Level;

/**
 * This class describes a FITS column
 * @author bourgesl
 */
public class ColumnMeta extends CellMeta {

    /* members */
    /** Column flag is3D (VisRefMap) */
    final boolean is3D;
    /** Cardinality of column :
     * For a Column, there are two cases :
     * - String (A) : maximum number of characters
     * - Other : dimension of the value (1 = single value, more it is an array)
     */
    private final int repeat;
    /** optional column name storing error values (may be null) */
    private final String errName;
    /** optional data range (may be null) */
    private final DataRange dataRange;
    /** optional column alias ie alternate name (may be null) */
    private String alias = null;

    /**
     * ColumnMeta class constructor with cardinality of 1 and without unit
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     */
    public ColumnMeta(final String name, final String desc, final Types dataType) {
        this(name, desc, dataType, 1, false, false, NO_INT_VALUES, NO_STR_VALUES, Units.NO_UNIT, null, null);
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
        this(name, desc, dataType, repeat, false, false, NO_INT_VALUES, NO_STR_VALUES, Units.NO_UNIT, null, null);
    }

    /**
     * ColumnMeta class constructor with is optional
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     * @param repeat column cardinality
     * @param optional
     */
    public ColumnMeta(final String name, final String desc, final Types dataType, final int repeat, final boolean optional) {
        this(name, desc, dataType, repeat, optional, false, NO_INT_VALUES, NO_STR_VALUES, Units.NO_UNIT, null, null);
    }

    /**
     * ColumnMeta class constructor with isoptinal and 3D
     * @param name
     * @param desc
     * @param dataType
     * @param repeat
     * @param optional
     * @param is3D
     * @param unit
     */
    public ColumnMeta(String name, String desc, Types dataType, int repeat, boolean optional, boolean is3D, final Units unit) {
        this(name, desc, dataType, repeat, optional, is3D, NO_INT_VALUES, NO_STR_VALUES, unit, null, null);
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
        this(name, desc, dataType, 1, false, false, NO_INT_VALUES, NO_STR_VALUES, unit, null, null);
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
        this(name, desc, dataType, 1, false, false, NO_INT_VALUES, NO_STR_VALUES, unit, null, dataRange);
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
        this(name, desc, dataType, repeat, false, false, NO_INT_VALUES, NO_STR_VALUES, unit, null, null);
    }

    /**
     * ColumnMeta class constructor with the given cardinality and unit
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     * @param repeat column cardinality
     * @param optional
     * @param is3D
     * @param acceptedValues
     * @param unit column unit
     * @param errName optional column name storing error values (may be null)
     * @param dataRange optional data range (may be null)
     */
    public ColumnMeta(final String name, final String desc, final Types dataType, final int repeat, final boolean optional, final boolean is3D,
            final String[] acceptedValues, final Units unit, final String errName, final DataRange dataRange) {
        this(name, desc, dataType, repeat, optional, is3D, NO_INT_VALUES, acceptedValues, unit, errName, dataRange);
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
        this(name, desc, dataType, repeat, false, false, NO_INT_VALUES, stringAcceptedValues, Units.NO_UNIT, null, null);
    }

    /**
     * Private ColumnMeta class constructor with the given cardinality and string possible values
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     * @param repeat column cardinality
     * @param is3D column is3D
     * @param repeat column cardinality
     * @param intAcceptedValues integer possible values for column/keyword
     * @param stringAcceptedValues string possible values for column/keyword
     * @param unit keyword/column unit
     * @param errName optional column name storing error values (may be null)
     * @param dataRange optional data range (may be null)
     */
    private ColumnMeta(final String name, final String desc, final Types dataType,
            final int repeat, final boolean optional, final boolean is3D, final short[] intAcceptedValues, final String[] stringAcceptedValues,
            final Units unit, final String errName, final DataRange dataRange) {
        super(MetaType.COLUMN, name, desc, dataType, optional, intAcceptedValues, stringAcceptedValues, unit);

        this.is3D = is3D;
        this.repeat = repeat;
        this.errName = errName;
        this.dataRange = dataRange;
    }

    /**
     * Return true if the value is multiple (array)
     * @return true if the value is multiple
     */
    public final boolean isArray() {
        return getDataType() != Types.TYPE_CHAR
                && (getRepeat() > 1 || this instanceof ArrayColumnMeta);
    }

    /**
     * Return true if the value is multiple (3D array)
     * @return true if the value is multiple
     */
    public final boolean is3D() {
        return is3D;
    }

    /**
     * Return the repeat value i.e. cardinality
     * Can be overriden to represent cross - references
     * @return repeat value i.e. cardinality
     */
    public int getRepeat() {
        return this.repeat;
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

    /* --- optional Alias --- */
    /**
     * Return the optional column alias
     * @return optional column alias
     */
    public final String getAlias() {
        return alias;
    }

    /**
     * Define the optional column alias
     * @param alias optional column alias
     */
    public final void setAlias(final String alias) {
        this.alias = alias;
    }

    /* ---  checker --- */
    /**
     * Check if the input column is valid.
     *
     * @param value column data to check
     * @param nbRows number of rows in the column
     * @param checker checker component
     * @param table
     * @param colName
     */
    public final void check(final Object value, final int nbRows, final OIFitsChecker checker, final FitsTable table, final String colName) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "check : {0} = {1}", new Object[]{getName(), ArrayFuncs.arrayDescription(value)});
        }

        // Check type and cardinality
        final Class<?> baseClass = ArrayFuncs.getBaseClass(value);
        char columnType = Types.getDataType(baseClass).getRepresentation();

        final int[] dims = ArrayFuncs.getDimensions(value);
        final int ndims = dims.length;

        // check rows
        final int columnRows = dims[0];
        if ((columnRows != nbRows) || OIFitsChecker.isInspectRules()) {
            // rule [GENERIC_COL_NBROWS] check if the column length matches the expected number of rows
            checker.ruleFailed(Rule.GENERIC_COL_NBROWS, table, colName);
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
                    } else if (val.length() > max) {
                        max = val.length();
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
                    } else if (dims[2] == dims[1] && baseClass == boolean.class) {
                        // square matrix of boolean (visRefMap)
                    } else {
                        logger.log(Level.SEVERE, "unsupported array dimensions : {0}", ArrayFuncs.arrayDescription(value));
                    }
                } else {
                    logger.log(Level.SEVERE, "unsupported array dimensions : {0}", ArrayFuncs.arrayDescription(value));
                }
            }
        }

        // Note : ColumnMeta.getRepeat() is lazily computed for cross-reference columns
        // see WaveColumnMeta.getRepeat()
        final char descType = this.getType();
        final int descRepeat = this.getRepeat();

        // rule [GENERIC_COL_FORMAT] check if the column format matches the expected format (data type & dimensions)
        checker.ruleFailed(Rule.GENERIC_COL_FORMAT, table, colName);

        if ((descRepeat == 0) || OIFitsChecker.isInspectRules()) {
            // May happen if bad reference (wavelength table):
            // rule [GENERIC_COL_DIM] check if the dimension of column values >= 1
            checker.ruleFailed(Rule.GENERIC_COL_DIM, table, colName);

            checker.warning("Can't check repeat for column '" + this.getName() + "'");

            if (columnType != descType) {
                checker.severe("Invalid format for column '" + this.getName() + "', found '" + columnType
                        + "' should be '" + descType + "'");
            }
        } else {
            boolean severe = false;

            if (columnType != descType) {
                severe = true;
            } else if (columnType == Types.TYPE_CHAR.getRepresentation()) {
                // For String values, report only errors when the maximum length is exceeded.
                if (columnRepeat > descRepeat) {
                    severe = true;
                }
            } else if (columnRepeat != descRepeat) {
                severe = true;
            }

            if (severe) {
                checker.severe("Invalid format for column '" + this.getName() + "', found '" + columnRepeat + columnType
                        + "' should be '" + descRepeat + descType + "'");
            }
        }

        // skip check units as the raw object has not this information.
        // Check values:
        checkValues(value, columnRows, checker, table, colName);
    }

    /**
     * If any are mentioned, check column values are fair.
     *
     * @param value column data to check (Not null)
     * @param columnRows number of rows in the given column
     * @param checker checker component
     */
    private void checkValues(final Object value, final int columnRows, final OIFitsChecker checker, final FitsTable table, final String colName) {
        boolean error;

        final short[] intAcceptedValues = getIntAcceptedValues();
        final String[] stringAcceptedValues = getStringAcceptedValues();

        if (intAcceptedValues.length != 0) {
            // OIData : STA_INDEX or TARGET_ID
            final boolean isArray = isArray();

            final short[] single = new short[1];
            short[] values;

            for (int rowNb = 0; rowNb < columnRows; rowNb++) {
                if (isArray) {
                    values = ((short[][]) value)[rowNb];
                } else {
                    single[0] = ((short[]) value)[rowNb];
                    values = single;
                }

                for (int r = 0, rlen = values.length; r < rlen; r++) {
                    if (!ModelBase.isUndefined(values[r]) || OIFitsChecker.isInspectRules()) {
                        error = true;

                        for (int i = 0, len = intAcceptedValues.length; i < len; i++) {
                            if (values[r] == intAcceptedValues[i]) {
                                error = false;
                                break;
                            }
                        }

                        if (error || OIFitsChecker.isInspectRules()) {
                            // rule [GENERIC_COL_VAL_ACCEPTED_INT] check if column values match the 'accepted' values (integer)
                            checker.ruleFailed(Rule.GENERIC_COL_VAL_ACCEPTED_INT, table, colName);

                            if (values.length > 1) {
                                checker.severe("Invalid value at index " + r + " for column '" + this.getName() + "' line " + rowNb + ", found '" + values[r] + "' should be '" + getIntAcceptedValuesAsString() + "'");
                            } else {
                                checker.severe("Invalid value for column '" + this.getName() + "' line " + rowNb + ", found '" + values[r] + "' should be '" + getIntAcceptedValuesAsString() + "'");
                            }
                        }
                    }
                }
            }
        } else if (stringAcceptedValues.length != 0) {
            // OITarget : VELTYP, VELDEF
            // OIInspol: INSNAME ...

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

                if (error || OIFitsChecker.isInspectRules()) {
                    // rule [GENERIC_COL_VAL_ACCEPTED_STR] check if column values match the 'accepted' values (string)
                    checker.ruleFailed(Rule.GENERIC_COL_VAL_ACCEPTED_STR, table, colName);
                    checker.severe("Invalid value for column '" + this.getName() + "' line " + rowNb + ", found '" + val + "' should be '" + getStringAcceptedValuesAsString() + "'");
                }
            }
        } else if (getDataRange() == DataRange.RANGE_POSITIVE_STRICT) {
            final boolean isArray = isArray();

            if (getDataType() == Types.TYPE_REAL) {
                final float[] single = new float[1];
                float[] values;

                for (int rowNb = 0; rowNb < columnRows; rowNb++) {
                    if (isArray) {
                        values = ((float[][]) value)[rowNb];
                    } else {
                        single[0] = ((float[]) value)[rowNb];
                        values = single;
                    }

                    for (int r = 0, rlen = values.length; r < rlen; r++) {
                        error = true;

                        // ignore NaN:
                        if (NumberUtils.isFinitePositive(values[r]) || Float.isNaN(values[r])) {
                            error = false;
                        }

                        if (error || OIFitsChecker.isInspectRules()) {
                            // rule [GENERIC_COL_VAL_POSITIVE] check if column values are finite and positive
                            checker.ruleFailed(Rule.GENERIC_COL_VAL_POSITIVE, table, colName);

                            if (values.length > 1) {
                                checker.severe("Invalid value at index " + r + " for column '" + this.getName() + "' line " + rowNb + ", found '" + values[r] + "' should be >= 0");
                            } else {
                                checker.severe("Invalid value for column '" + this.getName() + "' line " + rowNb + ", found '" + values[r] + "'  should be >= 0");
                            }
                        }
                    }
                }
            } else if (getDataType() == Types.TYPE_DBL) {
                final double[] single = new double[1];
                double[] values;

                for (int rowNb = 0; rowNb < columnRows; rowNb++) {
                    if (isArray) {
                        values = ((double[][]) value)[rowNb];
                    } else {
                        single[0] = ((double[]) value)[rowNb];
                        values = single;
                    }

                    for (int r = 0, rlen = values.length; r < rlen; r++) {
                        error = true;

                        // ignore NaN:
                        if (NumberUtils.isFinitePositive(values[r]) || Double.isNaN(values[r])) {
                            error = false;
                        }

                        if (error || OIFitsChecker.isInspectRules()) {
                            // rule [GENERIC_COL_VAL_POSITIVE] check if column values are finite and positive
                            checker.ruleFailed(Rule.GENERIC_COL_VAL_POSITIVE, table, colName);

                            if (values.length > 1) {
                                checker.severe("Invalid value at index " + r + " for column '" + this.getName() + "' line " + rowNb + ", found '" + values[r] + "' should be >= 0");
                            } else {
                                checker.severe("Invalid value for column '" + this.getName() + "' line " + rowNb + ", found '" + values[r] + "'  should be >= 0");
                            }
                        }
                    }
                }
            } else {
                logger.log(Level.SEVERE, "Incompatible data type {0} with positive values for column ''{1}'' ...", new Object[]{getDataType(), this.getName()});
            }
        }
    }
}
