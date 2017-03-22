/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.DataRange;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.meta.Units;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Class for OI_ARRAY table.
 */
public final class OIArray extends OITable {

    /* constants */

    /* static descriptors */
    /** ARRNAME keyword descriptor */
    private final static KeywordMeta KEYWORD_ARRNAME = new KeywordMeta(OIFitsConstants.KEYWORD_ARRNAME, "array name for cross-referencing", Types.TYPE_CHAR);
    /** FRAME   keyword descriptor */
    private final static KeywordMeta KEYWORD_FRAME = new KeywordMeta(OIFitsConstants.KEYWORD_FRAME, "coordinate frame", Types.TYPE_CHAR,
            new String[]{OIFitsConstants.KEYWORD_FRAME_GEOCENTRIC});
    /** ARRAYX  keyword descriptor */
    private final static KeywordMeta KEYWORD_ARRAY_X = new KeywordMeta(OIFitsConstants.KEYWORD_ARRAY_X, "[m] array center X-coordinate", Types.TYPE_DBL, Units.UNIT_METER);
    /** ARRAYY  keyword descriptor */
    private final static KeywordMeta KEYWORD_ARRAY_Y = new KeywordMeta(OIFitsConstants.KEYWORD_ARRAY_Y, "[m] array center Y-coordinate", Types.TYPE_DBL, Units.UNIT_METER);
    /** ARRAYZ  keyword descriptor */
    private final static KeywordMeta KEYWORD_ARRAY_Z = new KeywordMeta(OIFitsConstants.KEYWORD_ARRAY_Z, "[m] array center Z-coordinate", Types.TYPE_DBL, Units.UNIT_METER);
    /** TEL_NAME column descriptor */
    private final static ColumnMeta COLUMN_TEL_NAME = new ColumnMeta(OIFitsConstants.COLUMN_TEL_NAME, "telescope name", Types.TYPE_CHAR, 16);
    /** STA_NAME column descriptor */
    private final static ColumnMeta COLUMN_STA_NAME = new ColumnMeta(OIFitsConstants.COLUMN_STA_NAME, "station name", Types.TYPE_CHAR, 16);
    /** STA_INDEX column descriptor */
    private final static ColumnMeta COLUMN_STA_INDEX = new ColumnMeta(OIFitsConstants.COLUMN_STA_INDEX, "station index", Types.TYPE_INT);
    /** DIAMETER column descriptor */
    private final static ColumnMeta COLUMN_DIAMETER = new ColumnMeta(OIFitsConstants.COLUMN_DIAMETER, "element diameter", Types.TYPE_REAL, Units.UNIT_METER, DataRange.RANGE_POSITIVE_STRICT);
    /** STAXYZ column descriptor */
    private final static ColumnMeta COLUMN_STA_XYZ = new ColumnMeta(OIFitsConstants.COLUMN_STA_XYZ, "station coordinates relative to array center", Types.TYPE_DBL, 3, Units.UNIT_METER);

    /* members */
    /* cached analyzed data */
    /** mapping of staIndex values to row index */
    private final Map<Short, Integer> staIndexToRowIndex = new HashMap<Short, Integer>();
    /** cached StaNames corresponding to given OIData StaIndex arrays */
    private final Map<short[], String> staIndexesToStaNames = new IdentityHashMap<short[], String>();

    /**
     * Public OIArray class constructor
     * @param oifitsFile main OifitsFile
     */
    public OIArray(final OIFitsFile oifitsFile) {
        super(oifitsFile);

        // ARRNAME  keyword definition
        addKeywordMeta(KEYWORD_ARRNAME);

        // FRAME  keyword definition
        addKeywordMeta(KEYWORD_FRAME);

        // ARRAYX  keyword definition
        addKeywordMeta(KEYWORD_ARRAY_X);

        // ARRAYY  keyword definition
        addKeywordMeta(KEYWORD_ARRAY_Y);

        // ARRAYZ  keyword definition
        addKeywordMeta(KEYWORD_ARRAY_Z);

        // TEL_NAME  column definition
        addColumnMeta(COLUMN_TEL_NAME);

        // STA_NAME  column definition
        addColumnMeta(COLUMN_STA_NAME);

        // STA_INDEX  column definition
        addColumnMeta(COLUMN_STA_INDEX);

        // DIAMETER  column definition
        addColumnMeta(COLUMN_DIAMETER);

        // STAXYZ  column definition
        addColumnMeta(COLUMN_STA_XYZ);
    }

    /**
     * Public OIArray class constructor to create a new table
     * @param oifitsFile main OifitsFile
     * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
     */
    public OIArray(final OIFitsFile oifitsFile, final int nbRows) {
        this(oifitsFile);

        this.initializeTable(nbRows);
    }

    /* --- Keywords --- */
    /**
     * Get the value of ARRNAME keyword
     * @return the value of ARRNAME keyword
     */
    public String getArrName() {
        return getKeyword(OIFitsConstants.KEYWORD_ARRNAME);
    }

    /**
     * Define the ARRNAME keyword value
     * @param arrName value of ARRNAME keyword
     */
    public void setArrName(final String arrName) {
        setKeyword(OIFitsConstants.KEYWORD_ARRNAME, arrName);
    }

    /**
     * Get the value of FRAME keyword
     * @return the value of FRAME keyword
     */
    public String getFrame() {
        return getKeyword(OIFitsConstants.KEYWORD_FRAME);
    }

    /**
     * Define the FRAME keyword value
     * @param frame value of FRAME keyword
     */
    public void setFrame(final String frame) {
        setKeyword(OIFitsConstants.KEYWORD_FRAME, frame);
    }

    /**
     * Get the value of ARRAYX, ARRAYY, ARRAYZ keywords
     * @return the value of ARRAYX, ARRAYY, ARRAYZ keywords
     */
    public double[] getArrayXYZ() {
        return new double[]{
            getKeywordDouble(OIFitsConstants.KEYWORD_ARRAY_X),
            getKeywordDouble(OIFitsConstants.KEYWORD_ARRAY_Y),
            getKeywordDouble(OIFitsConstants.KEYWORD_ARRAY_Z)};
    }

    /**
     * Define the value of ARRAYX, ARRAYY, ARRAYZ keywords
     * @param values value of ARRAYX, ARRAYY, ARRAYZ keywords
     */
    public void setArrayXYZ(final double[] values) {
        if (values != null && values.length == 3) {
            setKeywordDouble(OIFitsConstants.KEYWORD_ARRAY_X, values[0]);
            setKeywordDouble(OIFitsConstants.KEYWORD_ARRAY_Y, values[1]);
            setKeywordDouble(OIFitsConstants.KEYWORD_ARRAY_Z, values[2]);
        }
    }

    /* --- Columns --- */
    /**
     * Get TEL_NAME column.
     * 
     * Note: this column must not store any NULL value
     * 
     * @return the array of TEL_NAME
     */
    public final String[] getTelName() {
        return getColumnString(OIFitsConstants.COLUMN_TEL_NAME);
    }

    /**
     * Get STA_NAME column.
     * 
     * Note: this column must not store any NULL value
     * 
     * @return the array of STA_NAME or null if undefined
     */
    public final String[] getStaName() {
        return getColumnString(OIFitsConstants.COLUMN_STA_NAME);
    }

    /**
     * Return the STA_INDEX column.
     * @return the STA_INDEX column i.e. an array containing station indexes.
     */
    public short[] getStaIndex() {
        final short[] data = getColumnShort(OIFitsConstants.COLUMN_STA_INDEX);
        if (data == null) {
            return EMPTY_SHORT_ARRAY;
        }
        return data;
    }

    /**
     * Get DIAMETER column.
     * @return the array of DIAMETER.
     */
    public final float[] getDiameter() {
        return getColumnFloat(OIFitsConstants.COLUMN_DIAMETER);
    }

    /**
     * Get STAXYZ column.
     * @return the array of STAXYZ.
     */
    public double[][] getStaXYZ() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_STA_XYZ);
    }

    /* --- Other methods --- */
    /**
     * Returns a string representation of this table
     * @return a string representation of this table
     */
    @Override
    public String toString() {
        return super.toString() + " [ ARRNAME=" + getArrName() + " | " + getNbRows() + " telescopes ]";
    }

    /**
     * Do syntactical analysis.
     * @param checker checker component
     */
    @Override
    public void checkSyntax(final OIFitsChecker checker) {
        super.checkSyntax(checker);

        if (getArrName() != null && getArrName().length() == 0) {
            checker.severe("ARRNAME identifier has blank value");
        }

        getOIFitsFile().checkCrossReference(this, checker);
    }

    /* --- data analysis --- */
    /**
     * Indicate to clear any cached value (derived column ...)
     */
    @Override
    public void setChanged() {
        super.setChanged();
        staIndexToRowIndex.clear();
        staIndexesToStaNames.clear();
    }

    protected Map<Short, Integer> getStaIndexToRowIndex() {
        return staIndexToRowIndex;
    }

    /**
     * Return the row index corresponding to the given staIndex or null if missing
     * @param staIndex staIndex (may be null)
     * @return row index corresponding to the given staIndex or null if missing
     */
    public Integer getRowIndex(final Short staIndex) {
        return getStaIndexToRowIndex().get(staIndex);
    }

    public String getStaNames(final short[] staIndexes) {
        if (staIndexes == null) {
            return UNDEFINED_STRING;
        }
        // warning: identity hashcode so use carefully using distinct array instances:
        String label = staIndexesToStaNames.get(staIndexes);

        if (label == null) {
            final StringBuilder sb = new StringBuilder(32);

            final String[] staNames = getStaName();

            Integer i;
            for (short staIndex : staIndexes) {
                i = getRowIndex(Short.valueOf(staIndex));

                if (i == null) {
                    sb.append(staIndex);
                } else {
                    sb.append(staNames[i]);
                }
                sb.append('-');
            }
            sb.setLength(sb.length() - 1);

            label = sb.toString();

            staIndexesToStaNames.put(staIndexes, label);
        }
        return label;
    }
}
/*___oOo___*/
