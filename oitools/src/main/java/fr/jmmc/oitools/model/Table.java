/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.fits.FitsConstants;
import fr.jmmc.oitools.fits.FitsHeaderCard;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.nom.tam.util.ArrayFuncs;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Generic table description.
 */
public class Table extends ModelBase {

    /* constants */

    /* static descriptors */
    /** NAXIS2 keyword descriptor */
    private final static KeywordMeta KEYWORD_NAXIS2 = new KeywordMeta(FitsConstants.KEYWORD_NAXIS2, "number of table rows", Types.TYPE_INT);
    /** EXTNAME keyword descriptor */
    private final static KeywordMeta KEYWORD_EXTNAME = new KeywordMeta(FitsConstants.KEYWORD_EXT_NAME, "extension name", Types.TYPE_CHAR);
    /** EXTVER keyword descriptor */
    private final static KeywordMeta KEYWORD_EXTVER = new KeywordMeta(FitsConstants.KEYWORD_EXT_VER, "extension version", Types.TYPE_INT, 0);
    
    /* members */
   
    /** Fits extension number (-1 means undefined) */
    private int extNb = -1;
    /* descriptors */
    /** Map storing keyword definitions ordered according to OIFits specification */
    private final Map<String, KeywordMeta> keywordsDesc = new LinkedHashMap<String, KeywordMeta>();
    /** Map storing column definitions ordered according to OIFits specification */
    private final Map<String, ColumnMeta> columnsDesc = new LinkedHashMap<String, ColumnMeta>();
    /** Map storing derived column definitions */
    private Map<String, ColumnMeta> columnsDerivedDesc = null;
    /* data */
    /** Map storing keyword values */
    private final Map<String, Object> keywordsValue = new HashMap<String, Object>();
    /** Map storing column values */
    private final Map<String, Object> columnsValue = new HashMap<String, Object>();
    /* cached computed data */
    /** Map storing computed values derived from this data table or related tables */
    private Map<String, Object> columnsDerivedValue = null;
    /** Map storing min/max computed values derived from this column */
    private Map<String, Object> columnsRangeValue = null;
    /** optional list of extra FITS header cards */
    private ArrayList<FitsHeaderCard> headerCards = null;

    /**
     * Protected OITable class constructor
     */
    protected Table() {
        super();
        
        // since every class constructor of OI table calls super
        // constructor, next keywords will be common to every subclass :
        // NAXIS2    keyword definition
        addKeywordMeta(KEYWORD_NAXIS2);

        // EXTNAME   keyword definition
        addKeywordMeta(KEYWORD_EXTNAME);

        // EXTVER    keyword definition
        addKeywordMeta(KEYWORD_EXTVER);
    }

    /**
     * Initialize the table with minimal keywords and empty columns
     * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
     * @throws IllegalArgumentException if the number of rows is less than 1
     */
    protected final void initializeTable(final int nbRows) throws IllegalArgumentException {
        if (nbRows < 1) {
            throw new IllegalArgumentException("Invalid number of rows : the table must have at least 1 row !");
        }

        this.setNbRows(nbRows);

        this.initializeColumnArrays();
    }

    /**
     * Initialize column arrays according to their format and the current number of rows (NAXIS2)
     */
    private void initializeColumnArrays() {
        final int nbRows = getNbRows();
        // TODO: check nbRows > 0 ?

        for (ColumnMeta column : getColumnDescCollection()) {
            setColumnValue(column.getName(), createColumnArray(column, nbRows));
        }
    }

    /**
     * Create a new column arrays according to their format and the given number of rows (NAXIS2)
     * @param column meta column descriptor
     * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
     * @return new column arrays
     */
    public Object createColumnArray(final ColumnMeta column, final int nbRows) {
        final String name = column.getName();
        final int repeat = column.getRepeat(); // repeat = row size
        final Types type = column.getDataType(); // data type

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("COLUMN [" + name + "] [" + repeat + column.getType() + "]");
        }

        // extract dimensions :
        int ndims = 1; // number of dimensions
        if (column.isArray() && type != Types.TYPE_CHAR) {
            ndims++;
        }
        if (type == Types.TYPE_COMPLEX) {
            ndims++;
        }

        final int[] dims = new int[ndims];
        ndims = 0;
        dims[ndims++] = nbRows;

        if (column.isArray() && type != Types.TYPE_CHAR) {
            dims[ndims++] = repeat;
        }
        if (type == Types.TYPE_COMPLEX) {
            dims[ndims] = 2;
        }

        // base class :
        final Class<?> clazz = Types.getBaseClass(type);

        Object value = ArrayFuncs.newInstance(clazz, dims); // array value

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("column array = " + ArrayFuncs.arrayDescription(value));
        }

        // Ensure arrays are initialized with undefined values:
        fillUndefinedArrays(value, dims, 0);

        return value;
    }

    /**
     * Fill the given multi-d array with undefined values (NaN, -32768, false or "").
     * @param output The multi-dimensional array to be filled.
     * @param dimens array of output dimensions.
     * @param index the index in dimens (current level)
     */
    protected static void fillUndefinedArrays(final Object output, final int[] dimens, final int index) {
        int xindex = index + 1;

        if (xindex == dimens.length) {
            if (output instanceof double[]) {
                /* double data type */
                Arrays.fill((double[]) output, UNDEFINED_DBL);
            } else if (output instanceof float[]) {
                /* real data type */
                /* complex data type */
                Arrays.fill((float[]) output, UNDEFINED_FLOAT);
            } else if (output instanceof short[]) {
                /* integer data type */
                Arrays.fill((short[]) output, UNDEFINED_SHORT);
            } else if (output instanceof boolean[]) {
                /* logical data type */
                Arrays.fill((boolean[]) output, false);
            } else if (output instanceof String[]) {
                /* character/date data type */
                Arrays.fill((String[]) output, UNDEFINED_STRING);
            } else {
                logger.info("fillEmptyArrays: Unsupported array type: " + output.getClass());
            }
            return;
        }

        int len = dimens[index];

        Object[] oo = (Object[]) output;
        for (int i = 0; i < len; i += 1) {
            fillUndefinedArrays(oo[i], dimens, xindex);
        }
    }

    /**
     * Indicate to clear any cached value (derived column ...)
     */
    public void setChanged() {
        clearColumnsDerivedValue();
        clearColumnsRangeValue();
    }

    /**
     * Implements the Visitor pattern
     * @param visitor visitor implementation
     */
    @Override
    public final void accept(final ModelVisitor visitor) {
        // TODO fix implementation of other children Tables
        if (this instanceof OITable) {
            visitor.visit((OITable) this);
        }
    }

   

    /*
     * --- Keyword descriptors -------------------------------------------------
     */
    /**
     * Return the Map storing keyword definitions
     * @return Map storing keyword definitions
     */
    protected final Map<String, KeywordMeta> getKeywordsDesc() {
        return this.keywordsDesc;
    }

    /**
     * Return the ordered collection of keyword definitions
     * @return ordered collection of keyword definitions
     */
    protected final Collection<KeywordMeta> getKeywordDescCollection() {
        return getKeywordsDesc().values();
    }

    /**
     * Add the given keyword descriptor
     * @param meta keyword descriptor
     */
    protected final void addKeywordMeta(final KeywordMeta meta) {
        getKeywordsDesc().put(meta.getName(), meta);
    }

    /*
     * --- Keyword values ------------------------------------------------------
     */
    /**
     * Return the Map storing keyword values
     * @return Map storing keyword values
     */
    protected final Map<String, Object> getKeywordsValue() {
        return this.keywordsValue;
    }

    /**
     * Return the keyword value given its name
     * The returned value can be null if the keyword is optional or has never been defined
     * @param name keyword name
     * @return any object value or null if undefined
     */
    protected final Object getKeywordValue(final String name) {
        return getKeywordsValue().get(name);
    }

    /**
     * Return the keyword value given its name as a String
     * @param name keyword name
     * @return String value
     */
    protected final String getKeyword(final String name) {
        return (String) getKeywordValue(name);
    }

    /**
     * Return the keyword value given its name as an integer (primitive type)
     * @param name keyword name
     * @return int value or 0 if undefined
     */
    protected final int getKeywordInt(final String name) {
        return getKeywordInt(name, 0);
    }

    /**
     * Return the keyword value given its name as an integer (primitive type)
     * @param name keyword name
     * @param def default value
     * @return int value or def if undefined
     */
    protected final int getKeywordInt(final String name, final int def) {
        final Number value = (Number) getKeywordValue(name);
        if (value == null) {
            return def;
        }
        return value.intValue();
    }

    /**
     * Return the keyword value given its name as a double (primitive type)
     * @param name keyword name
     * @return double value or 0d if undefined
     */
    protected final double getKeywordDouble(final String name) {
        return getKeywordDouble(name, 0d);
    }

    /**
     * Return the keyword value given its name as a double (primitive type)
     * @param name keyword name
     * @param def default value
     * @return double value or 0d if undefined
     */
    protected final double getKeywordDouble(final String name, final double def) {
        final Number value = (Number) getKeywordValue(name);
        if (value == null) {
            return def;
        }
        return value.doubleValue();
    }

    /**
     * Return the keyword value given its name as a boolean (primitive type)
     * @param name keyword name
     * @return double value or false if undefined
     */
    protected final boolean getKeywordLogical(final String name) {
        return getKeywordLogical(name, false);
    }

    /**
     * Return the keyword value given its name as a boolean (primitive type)
     * @param name keyword name
     * @param def default value
     * @return double value or 0d if undefined
     */
    protected final boolean getKeywordLogical(final String name, final boolean def) {
        final Boolean value = (Boolean) getKeywordValue(name);
        if (value == null) {
            return def;
        }
        return value.booleanValue();
    }

    /**
     * Define the keyword value given its name and value
     * @param name keyword name
     * @param value any object value
     */
    protected final void setKeywordValue(final String name, final Object value) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("KEYWORD [" + name + "] = '" + value + "' [" + ((value != null) ? value.getClass().getSimpleName() : "") + "]");
        }
        getKeywordsValue().put(name, value);
    }

    /**
     * Define the keyword value given its name and value as a String
     * @param name keyword name
     * @param value a String value
     */
    protected final void setKeyword(final String name, final String value) {
        setKeywordValue(name, value);
    }

    /**
     * Define the keyword value given its name and value as a String
     * @param name keyword name
     * @param value a String value
     */
    protected final void setKeywordInt(final String name, final int value) {
        setKeywordValue(name, NumberUtils.valueOf(value));
    }

    /**
     * Define the keyword value given its name and value as a String
     * @param name keyword name
     * @param value a String value
     */
    protected final void setKeywordDouble(final String name, final double value) {
        setKeywordValue(name, Double.valueOf(value));
    }

    /**
     * Define the keyword value given its name and value as a String
     * @param name keyword name
     * @param value a String value
     */
    protected final void setKeywordLogical(final String name, final boolean value) {
        setKeywordValue(name, Boolean.valueOf(value));
    }

    /*
     * --- Column descriptors --------------------------------------------------
     */
    /**
     * Return the Map storing column definitions
     * @return Map storing column definitions
     */
    protected final Map<String, ColumnMeta> getColumnsDesc() {
        return this.columnsDesc;
    }

    /**
     * Return the column definition given its name
     * @param name column name
     * @return column definition or null if undefined
     */
    protected final ColumnMeta getColumnDesc(final String name) {
        return getColumnsDesc().get(name);
    }

    /**
     * Return the ordered collection of column definitions
     * @return ordered collection of column definitions
     */
    protected final Collection<ColumnMeta> getColumnDescCollection() {
        return getColumnsDesc().values();
    }

    /**
     * Return the number of columns
     * @return the number of columns
     */
    public final int getNbColumns() {
        return getColumnsDesc().size();
    }

    /**
     * Add the given column descriptor
     * @param meta column descriptor
     */
    protected final void addColumnMeta(final ColumnMeta meta) {
        getColumnsDesc().put(meta.getName(), meta);
    }

    /**
     * Append column names (standard then derived) representing numerical values (double arrays)
     * @param columnNames set to be filled with numerical column names
     */
    public final void getNumericalColumnsNames(final Set<String> columnNames) {
        ColumnMeta meta;
        // Standard columns:
        for (Map.Entry<String, ColumnMeta> entry : getColumnsDesc().entrySet()) {
            meta = entry.getValue();
            switch (meta.getDataType()) {
                case TYPE_DBL:
                    columnNames.add(entry.getKey());
                    break;
                default:
            }
        }
        // Derived columns:
        for (Map.Entry<String, ColumnMeta> entry : getColumnsDerivedDesc().entrySet()) {
            meta = entry.getValue();
            switch (meta.getDataType()) {
                case TYPE_DBL:
                    columnNames.add(entry.getKey());
                    break;
                default:
            }
        }
    }

    /**
     * Return the column descriptors (standard then derived) representing numerical values (double arrays)
     * @return list of numerical column descriptors
     */
    public final List<ColumnMeta> getNumericalColumnsDescs() {
        final ArrayList<ColumnMeta> columnDescList = new ArrayList<ColumnMeta>();
        ColumnMeta meta;
        // Standard columns:
        for (Map.Entry<String, ColumnMeta> entry : getColumnsDesc().entrySet()) {
            meta = entry.getValue();
            switch (meta.getDataType()) {
                case TYPE_DBL:
                    columnDescList.add(entry.getValue());
                    break;
                default:
            }
        }
        // Derived columns:
        for (Map.Entry<String, ColumnMeta> entry : getColumnsDerivedDesc().entrySet()) {
            meta = entry.getValue();
            switch (meta.getDataType()) {
                case TYPE_DBL:
                    columnDescList.add(entry.getValue());
                    break;
                default:
            }
        }
        return columnDescList;
    }
    /*
     * --- Column values -------------------------------------------------------
     */

    /**
     * Return the Map storing column values
     * @return Map storing column values
     */
    protected final Map<String, Object> getColumnsValue() {
        return this.columnsValue;
    }

    /**
     * Return true if the table contains the column given its column descriptor
     * @param meta column descriptor
     * @return true if the table contains the column
     */
    protected final boolean hasColumn(final ColumnMeta meta) {
        if (meta.isOptional()) {
            return getColumnValue(meta.getName()) != null;
        }
        return true;
    }

    /**
     * Return the column value given its name
     * The returned value can be null if the column has never been defined
     * @param name column name
     * @return any array value or null if undefined
     */
    protected final Object getColumnValue(final String name) {
        return getColumnsValue().get(name);
    }

    /**
     * Return the column value given its name as a String array
     * The returned value can be null if the column has never been defined
     * @param name column name
     * @return String array or null if undefined
     */
    protected final String[] getColumnString(final String name) {
        return (String[]) getColumnValue(name);
    }

    /**
     * Return the column value given its name as an integer array (short primitive type)
     * The returned value can be null if the column has never been defined
     * @param name column name
     * @return integer array or null if undefined
     */
    protected final short[] getColumnShort(final String name) {
        return (short[]) getColumnValue(name);
    }

    /**
     * Return the column value given its name as a 2D integer array (short primitive type)
     * The returned value can be null if the column has never been defined
     * @param name column name
     * @return 2D integer array or null if undefined
     */
    protected final short[][] getColumnShorts(final String name) {
        return (short[][]) getColumnValue(name);
    }

    /**
     * Return the column value given its name as a float array
     * The returned value can be null if the column has never been defined
     * @param name column name
     * @return float array or null if undefined
     */
    protected final float[] getColumnFloat(final String name) {
        return (float[]) getColumnValue(name);
    }

    /**
     * Return the column value given its name as a double array
     * The returned value can be null if the column has never been defined
     * @param name column name
     * @return double array or null if undefined
     */
    protected final double[] getColumnDouble(final String name) {
        return (double[]) getColumnValue(name);
    }

    /**
     * Return the column value given its name as a 2D double array
     * The returned value can be null if the column has never been defined
     * @param name column name
     * @return 2D double array or null if undefined
     */
    protected final double[][] getColumnDoubles(final String name) {
        return (double[][]) getColumnValue(name);
    }

    /**
     * Return the column value given its name as a 2D complex array
     * The returned value can be null if the column has never been defined
     * @param name column name
     * @return 2D complex array or null if undefined
     */
    protected final float[][][] getColumnComplexes(final String name) {
        return (float[][][]) getColumnValue(name);
    }

    /**
     * Return the column value given its name as a 2D boolean array
     * The returned value can be null if the column has never been defined
     * @param name column name
     * @return 2D boolean array or null if undefined
     */
    protected final boolean[][] getColumnBooleans(final String name) {
        return (boolean[][]) getColumnValue(name);
    }

    /**
     * Define the column value given its name and an array value (String[] or a primitive array)
     * @param name column name
     * @param value any array value
     */
    protected final void setColumnValue(final String name, final Object value) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("COLUMN [" + name + "] = " + ((value != null) ? ArrayFuncs.arrayDescription(value) : ""));
        }
        getColumnsValue().put(name, value);
    }

    /*
     * --- Derived Column descriptors ------------------------------------------
     */
    /**
     * Return the Map storing derived column definitions
     * @return Map storing derived column definitions
     */
    protected final Map<String, ColumnMeta> getColumnsDerivedDesc() {
        // lazy
        if (this.columnsDerivedDesc == null) {
            this.columnsDerivedDesc = new LinkedHashMap<String, ColumnMeta>();
        }
        return this.columnsDerivedDesc;
    }

    /**
     * Return the derived column definition given its name
     * @param name column name
     * @return derived column definition or null if undefined
     */
    protected final ColumnMeta getColumnDerivedDesc(final String name) {
        if (this.columnsDerivedDesc != null) {
            return getColumnsDerivedDesc().get(name);
        }
        return null;
    }

    /**
     * Return the collection of derived column definitions
     * @return collection of derived column definitions
     */
    protected final Collection<ColumnMeta> getColumnDerivedDescCollection() {
        return getColumnsDerivedDesc().values();
    }

    /**
     * Return the number of derived columns
     * @return the number of derived columns
     */
    public final int getNbDerivedColumns() {
        return getColumnsDerivedDesc().size();
    }

    /**
     * Add the given derived column descriptor
     * @param meta derived meta column descriptor
     */
    protected final void addDerivedColumnMeta(final ColumnMeta meta) {
        getColumnsDerivedDesc().put(meta.getName(), meta);
    }

    /**
     * Remove the derived column definition given its name
     * @param name column name
     */
    protected final void removeDerivedColumnMeta(final String name) {
        getColumnsDerivedDesc().remove(name);
    }

    /*
     * --- Derived Column values -----------------------------------------------
     */
    /**
     * Clear the Map storing column derived value
     */
    protected final void clearColumnsDerivedValue() {
        if (this.columnsDerivedValue != null) {
            this.columnsDerivedValue.clear();
        }
    }

    /**
     * Return the Map storing column derived value
     * @return Map storing column derived value
     */
    protected final Map<String, Object> getColumnsDerivedValue() {
        // lazy
        if (this.columnsDerivedValue == null) {
            this.columnsDerivedValue = new HashMap<String, Object>();
        }

        return this.columnsDerivedValue;
    }

    /**
     * Return the column derived value given its name
     * The returned value can be null if the column derived value has never been defined
     * @param name column name
     * @return any value or null if undefined
     */
    protected final Object getColumnDerivedValue(final String name) {
        return getColumnsDerivedValue().get(name);
    }

    /**
     * Return the column derived value given its name as a String array
     * The returned value can be null if the column derived value has never been defined
     * @param name column name
     * @return String array or null if undefined
     */
    protected final String[] getColumnDerivedString(final String name) {
        return (String[]) getColumnDerivedValue(name);
    }

    /**
     * Return the column derived value given its name as an integer array (short primitive type)
     * The returned value can be null if the column derived value has never been defined
     * @param name column name
     * @return integer array or null if undefined
     */
    protected final short[] getColumnDerivedShort(final String name) {
        return (short[]) getColumnDerivedValue(name);
    }

    /**
     * Return the column derived value given its name as a 2D integer array (short primitive type)
     * The returned value can be null if the column derived value has never been defined
     * @param name column name
     * @return 2D integer array or null if undefined
     */
    protected final short[][] getColumnDerivedShorts(final String name) {
        return (short[][]) getColumnDerivedValue(name);
    }

    /**
     * Return the column derived value given its name as a float array
     * The returned value can be null if the column derived value has never been defined
     * @param name column name
     * @return float array or null if undefined
     */
    protected final float[] getColumnDerivedFloat(final String name) {
        return (float[]) getColumnDerivedValue(name);
    }

    /**
     * Return the column derived value given its name as a double array
     * The returned value can be null if the column derived value has never been defined
     * @param name column name
     * @return double array or null if undefined
     */
    protected final double[] getColumnDerivedDouble(final String name) {
        return (double[]) getColumnDerivedValue(name);
    }

    /**
     * Return the column derived value given its name as a 2D double array
     * The returned value can be null if the column derived value has never been defined
     * @param name column name
     * @return 2D double array or null if undefined
     */
    protected final double[][] getColumnDerivedDoubles(final String name) {
        return (double[][]) getColumnDerivedValue(name);
    }

    /**
     * Define the column derived value given its name and any value
     * @param name column name
     * @param value any value
     */
    protected final void setColumnDerivedValue(final String name, final Object value) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("DERIVED COLUMN VALUE [" + name + "] = " + ((value != null) ? ArrayFuncs.arrayDescription(value) : ""));
        }
        getColumnsDerivedValue().put(name, value);
    }

    /**
     * Remove the column derived value given its name
     * @param name column name
     */
    protected final void removeColumnDerivedValue(final String name) {
        getColumnsDerivedValue().remove(name);
    }

    /*
     * --- Column Range values -------------------------------------------------
     */
    /**
     * Clear the Map storing min/max column values
     */
    protected final void clearColumnsRangeValue() {
        if (this.columnsRangeValue != null) {
            this.columnsRangeValue.clear();
        }
    }

    /**
     * Return the Map storing min/max column values
     * @return Map storing min/max of column values
     */
    protected final Map<String, Object> getColumnsRangeValue() {
        // lazy
        if (this.columnsRangeValue == null) {
            this.columnsRangeValue = new HashMap<String, Object>();
        }
        return this.columnsRangeValue;
    }

    /**
     * Return the minimum and maximum column value given its name
     * The returned value can be null if the column has never been defined
     * @param name column name
     * @return [min;max] values or null if undefined
     */
    protected Object getMinMaxColumnValue(final String name) {
        /* retrieve range in columnsRangeValue map of associated column */
        Object range = getColumnsRangeValue().get(name);

        /* compute min and max values if not previously set */
        if (range == null) {
            final ColumnMeta column = getColumnsDesc().get(name);

            switch (column.getDataType()) {
                case TYPE_CHAR:
                    // Not Applicable
                    break;

                case TYPE_INT:
                    int iMin = Integer.MAX_VALUE;
                    int iMax = Integer.MIN_VALUE;

                    if (column.isArray()) {
                        final short[][] iValues = getColumnShorts(column.getName());
                        short[] iRowValues;
                        for (int i = 0, len = iValues.length; i < len; i++) {
                            iRowValues = iValues[i];
                            for (int j = 0, jlen = iRowValues.length; j < jlen; j++) {
                                if (iRowValues[j] < iMin) {
                                    iMin = iRowValues[j];
                                }
                                if (iRowValues[j] > iMax) {
                                    iMax = iRowValues[j];
                                }
                            }
                        }
                    } else {
                        final short[] iValues = getColumnShort(column.getName());
                        for (int i = 0, len = iValues.length; i < len; i++) {
                            if (iValues[i] < iMin) {
                                iMin = iValues[i];
                            }
                            if (iValues[i] > iMax) {
                                iMax = iValues[i];
                            }
                        }
                    }
                    range = new int[]{iMin, iMax};
                    break;

                case TYPE_DBL:
                    double dMin = Double.POSITIVE_INFINITY;
                    double dMax = Double.NEGATIVE_INFINITY;

                    if (column.isArray()) {
                        final double[][] dValues = getColumnDoubles(column.getName());
                        double[] dRowValues;
                        for (int i = 0, len = dValues.length; i < len; i++) {
                            dRowValues = dValues[i];
                            for (int j = 0, jlen = dRowValues.length; j < jlen; j++) {
                                if (dRowValues[j] < dMin) {
                                    dMin = dRowValues[j];
                                }
                                if (dRowValues[j] > dMax) {
                                    dMax = dRowValues[j];
                                }
                            }
                        }
                    } else {
                        final double[] dValues = getColumnDouble(column.getName());
                        for (int i = 0, len = dValues.length; i < len; i++) {
                            if (dValues[i] < dMin) {
                                dMin = dValues[i];
                            }
                            if (dValues[i] > dMax) {
                                dMax = dValues[i];
                            }
                        }
                    }
                    range = new double[]{dMin, dMax};
                    break;
                case TYPE_REAL:
                    float fMin = Float.POSITIVE_INFINITY;
                    float fMax = Float.NEGATIVE_INFINITY;

                    if (column.isArray()) {
                        // Impossible case in OIFits
                        break;
                    }
                    final float[] fValues = getColumnFloat(column.getName());
                    for (int i = 0, len = fValues.length; i < len; i++) {
                        if (fValues[i] < fMin) {
                            fMin = fValues[i];
                        }
                        if (fValues[i] > fMax) {
                            fMax = fValues[i];
                        }
                    }
                    range = new float[]{fMin, fMax};
                    break;

                case TYPE_COMPLEX:
                    // Not Applicable
                    break;

                case TYPE_LOGICAL:
                    // Not Applicable
                    break;

                default:
                // do nothing
            }

            /* store in associated column range value */
            getColumnsRangeValue().put(name, range);
        }

        return range;
    }

    /* --- ext number --- */
    /**
     * Get the extension number
     * @return the extension number
     */
    public final int getExtNb() {
        return extNb;
    }

    /**
     * Define the extension number
     * @param extNb extension number
     */
    protected final void setExtNb(final int extNb) {
        this.extNb = extNb;
    }

    /*
     * --- OIFits standard Keywords --------------------------------------------
     */
    /**
     * Get the EXTNAME keyword value
     * @return value of EXTNAME keyword
     */
    public final String getExtName() {
        return getKeyword(FitsConstants.KEYWORD_EXT_NAME);
    }

    /**
     * Define the EXTNAME keyword value
     * @param extName value of EXTNAME keyword
     */
    protected final void setExtName(final String extName) {
        setKeyword(FitsConstants.KEYWORD_EXT_NAME, extName);
    }

    /**
     * Get the EXTVER keyword value
     * @return value of EXTVER keyword
     */
    public final int getExtVer() {
        return getKeywordInt(FitsConstants.KEYWORD_EXT_VER);
    }

    /**
     * Define the EXTVER keyword value
     * @param extVer value of EXTVER keyword
     */
    protected final void setExtVer(final int extVer) {
        setKeywordInt(FitsConstants.KEYWORD_EXT_VER, extVer);
    }

    /**
     * Get the OI_REVN keyword value
     * @return value of OI_REVN keyword
     */
    public final int getOiRevn() {
        return getKeywordInt(OIFitsConstants.KEYWORD_OI_REVN);
    }

    /**
     * Define the OI_REVN keyword value
     * @param oiRevn value of OI_REVN keyword
     */
    protected final void setOiRevn(final int oiRevn) {
        setKeywordInt(OIFitsConstants.KEYWORD_OI_REVN, oiRevn);
    }

    /**
     * Return the number of rows i.e. the Fits NAXIS2 keyword value
     * @return the number of rows i.e. the Fits NAXIS2 keyword value
     */
    public final int getNbRows() {
        return getKeywordInt(FitsConstants.KEYWORD_NAXIS2);
    }

    /**
     * Define the number of rows i.e. the Fits NAXIS2 keyword value
     * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
     */
    protected final void setNbRows(final int nbRows) {
        setKeywordInt(FitsConstants.KEYWORD_NAXIS2, nbRows);
    }

    /* --- Extra keywords --- */
    /**
     * Return true if the optional list of extra FITS header cards is not empty
     * @return true if the optional list of extra FITS header cards is not empty
     */
    public boolean hasHeaderCards() {
        return (this.headerCards != null && !this.headerCards.isEmpty());
    }

    /**
     * Return the list of header cards
     * @return list of header cards
     */
    public List<FitsHeaderCard> getHeaderCards() {
        return getHeaderCards(10);
    }

    /**
     * Return the list of header cards
     * @param nCards number of cards to define the initial capacity of the list
     * @return list of header cards
     */
    public List<FitsHeaderCard> getHeaderCards(final int nCards) {
        if (this.headerCards == null) {
            this.headerCards = new ArrayList<FitsHeaderCard>(nCards);
        }
        return this.headerCards;
    }

    /**
     * Return a string representation of the list of header cards
     * @param separator separator to use after each header card
     * @return string representation of the list of header cards
     */
    public String getHeaderCardsAsString(final String separator) {
        if (this.headerCards == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(1024);
        for (FitsHeaderCard h : this.headerCards) {
            h.toString(sb);
            sb.append(separator);
        }
        return sb.toString();
    }

    /**
     * Trim the list of header cards
     */
    public void trimHeaderCards() {
        if (this.headerCards != null) {
            if (this.headerCards.size() > 0) {
                this.headerCards.trimToSize();
            } else {
                this.headerCards = null;
            }
        }
    }

    /* --- Other methods --- */
    /**
     * Returns the table id [EXT_NAME#EXT_NB] as string
     * @param extName extension name
     * @param extNb extension number
     * @return table id [EXT_NAME # EXT_NB] as string
     */
    public static String getTableId(final String extName, final int extNb) {
        return extName + '#' + extNb;
    }

    /**
     * Returns the table id[EXT_NAME # EXT_NB] as string
     * @return table id[EXT_NAME # EXT_NB] as string
     */
    public String idToString() {
        return getTableId(getExtName(), getExtNb());
    }

    /**
     * Returns a string representation of this table
     * @return a string representation of this table
     */
    @Override
    public String toString() {
        return idToString();
    }

    /*
     * --- Checker -------------------------------------------------------------
     */
    /**
     * Do syntactical analysis of the table
     * @param checker checker component
     */
    public void checkSyntax(final OIFitsChecker checker) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "checkSyntax : {0}", this.toString());
        }
        logger.log(Level.INFO, "Analysing table [{0}]:", idToString());

        // First analyse keywords
        checkKeywords(checker);
        // Second analyse columns
        checkColumns(checker);
    }

    /**
     * Check syntax of table's keywords.
     * It consists in checking all mandatory keywords are present, with right
     * name, right format and right values (if they do belong to a given set of
     * accepted values).
     *
     * @param checker checker component
     */
    public void checkKeywords(final OIFitsChecker checker) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("checkKeywords : " + this.toString());
        }
        String keywordName;
        Object value;

        /* Get mandatory keywords names */
        for (KeywordMeta keyword : getKeywordDescCollection()) {
            keywordName = keyword.getName();

            // get keyword value :
            value = getKeywordValue(keywordName);

            if (value == null) {
                if (keyword.isMandatory()) {
                    /* No keyword with keywordName name */
                    checker.severe("Missing keyword '" + keywordName + "'");
                }
            } else {
                /* Check the keyword validity */
                keyword.check(value, checker);
            }
        }
    }

    /**
     * Check syntax of table's columns.
     * It consists in checking all mandatory columns are present, with right
     * name, right format and right associated unit.
     *
     * @param checker checker component
     */
    public void checkColumns(final OIFitsChecker checker) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("checkColumns : " + this.toString());
        }
        String columnName;
        Object value;

        /* Get mandatory columns names */
        for (ColumnMeta column : getColumnDescCollection()) {
            columnName = column.getName();
            value = getColumnValue(columnName);

            if (value == null) {
                if (!column.isOptional()) {
                    /* No column with columnName name */
                    checker.severe("Missing column '" + columnName + "'");
                }
            } else {
                /* Check the column validity */
                column.check(value, getNbRows(), checker);
            }
        }
    }

    /*
     * --- public descriptor access ---------------------------------------------------------
     */
    /**
     * Return the column definition given its name (standard or derived)
     * @param name column name
     * @return column definition or null if undefined
     */
    public final ColumnMeta getColumnMeta(final String name) {
        ColumnMeta meta = getColumnDesc(name);
        if (meta != null) {
            return meta;
        }
        meta = getColumnDerivedDesc(name);
        if (meta != null) {
            return meta;
        }
        return null;
    }

    /*
     * --- public data access ---------------------------------------------------------
     */
    /**
     * Return the column data as double array (1D) for the given column name (standard or derived).
     * No conversion are performed here: only column storing double values are returned !
     * @param name any column name
     * @return column data as double array (1D) or null if undefined or wrong type
     */
    public final double[] getColumnAsDouble(final String name) {
        ColumnMeta meta = getColumnDesc(name);
        if (meta != null
                && meta.getDataType() == Types.TYPE_DBL
                && !meta.isArray()) {
            return getColumnDouble(name);
        }
        meta = getColumnDerivedDesc(name);
        if (meta != null
                && meta.getDataType() == Types.TYPE_DBL
                && !meta.isArray()) {
            return getDerivedColumnAsDouble(name);
        }
        return null;
    }

    /**
     * Return the derived column data as double array (1D) for the given column name
     * To be overriden in child classes for lazy computed columns
     * @param name any column name
     * @return column data as double array (1D) or null if undefined or wrong type
     */
    protected double[] getDerivedColumnAsDouble(final String name) {
        return null;
    }

    /**
     * Return the column data as double arrays (2D) for the given column name (standard or derived).
     * No conversion are performed here: only column storing double values are returned !
     * @param name any column name
     * @return column data as double arrays (2D) or null if undefined or wrong type
     */
    public final double[][] getColumnAsDoubles(final String name) {
        ColumnMeta meta = getColumnDesc(name);
        if (meta != null
                && meta.getDataType() == Types.TYPE_DBL
                && meta.isArray()) {
            return getColumnDoubles(name);
        }
        meta = getColumnDerivedDesc(name);
        if (meta != null
                && meta.getDataType() == Types.TYPE_DBL
                && meta.isArray()) {
            return getDerivedColumnAsDoubles(name);
        }
        return null;
    }

    /**
     * Return the derived column data as double arrays (2D) for the given column name
     * To be overriden in child classes for lazy computed columns
     * @param name any column name
     * @return column data as double arrays (2D) or null if undefined or wrong type
     */
    protected double[][] getDerivedColumnAsDoubles(final String name) {
        return null;
    }
}
/*___oOo___*/
