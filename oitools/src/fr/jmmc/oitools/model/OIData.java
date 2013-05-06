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
import fr.jmmc.oitools.meta.WaveColumnMeta;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * OIData table is the base class for OI_VIS, OI_VIS2 and OI_T3 tables.
 */
public abstract class OIData extends OITable {

    /* constants */
    /** data range representing visibilities [0; 1] */
    public final static DataRange RANGE_VIS = new DataRange(0d, 1d);
    /** data range representing angle range [-180; 180] */
    public final static DataRange RANGE_ANGLE = new DataRange(-180d, 180d);
    /* static descriptors */
    /** DATE-OBS keyword descriptor */
    private final static KeywordMeta KEYWORD_DATE_OBS = new KeywordMeta(OIFitsConstants.KEYWORD_DATE_OBS,
            "UTC start date of observations", Types.TYPE_CHAR);
    /** TIME column descriptor */
    private final static ColumnMeta COLUMN_TIME = new ColumnMeta(OIFitsConstants.COLUMN_TIME,
            "UTC time of observation", Types.TYPE_DBL, Units.UNIT_SECOND, DataRange.RANGE_POSITIVE);
    /** MJD column descriptor */
    private final static ColumnMeta COLUMN_MJD = new ColumnMeta(OIFitsConstants.COLUMN_MJD,
            "modified Julian Day", Types.TYPE_DBL, Units.UNIT_MJD);
    /** INT_TIME column descriptor */
    private final static ColumnMeta COLUMN_INT_TIME = new ColumnMeta(OIFitsConstants.COLUMN_INT_TIME,
            "integration time", Types.TYPE_DBL, Units.UNIT_SECOND, DataRange.RANGE_POSITIVE_STRICT);
    /** UCOORD column descriptor */
    protected final static ColumnMeta COLUMN_UCOORD = new ColumnMeta(OIFitsConstants.COLUMN_UCOORD,
            "U coordinate of the data", Types.TYPE_DBL, Units.UNIT_METER);
    /** VCOORD column descriptor */
    protected final static ColumnMeta COLUMN_VCOORD = new ColumnMeta(OIFitsConstants.COLUMN_VCOORD,
            "V coordinate of the data", Types.TYPE_DBL, Units.UNIT_METER);
    /** members */
    /** cached reference on OI_ARRAY table associated to this OIData table */
    private OIArray oiArrayRef = null;
    /** cached reference on OI_WAVELENGTH table associated to this OIData table */
    private OIWavelength oiWavelengthRef = null;
    /* cached analyzed data */
    /** number of data flagged out (-1 means undefined) */
    private int nFlagged = -1;
    /** distinct targetId values present in this table (identity hashcode) */
    private final Set<Short> distinctTargetId = new LinkedHashSet<Short>();
    /** distinct StaIndex values present in this table (identity hashcode) */
    private final Set<short[]> distinctStaIndex = new LinkedHashSet<short[]>();
    /** cached StaNames corresponding to given OIData StaIndex arrays */
    private final Map<short[], String> staIndexesToString = new HashMap<short[], String>();
    /** distinct StaConf values present in this table (station configuration) (sorted) */
    private final Set<short[]> distinctStaConf = new LinkedHashSet<short[]>();

    /**
     * Protected OIData class constructor
     * @param oifitsFile main OifitsFile
     */
    protected OIData(final OIFitsFile oifitsFile) {
        super(oifitsFile);

        // since every class constructor of OI_VIS, OI_VIS2, OI_T3 calls super
        // constructor, next keywords will be common to every subclass :

        // DATE-OBS  keyword definition
        addKeywordMeta(KEYWORD_DATE_OBS);

        // ARRNAME  Optional keyword definition
        addKeywordMeta(new KeywordMeta(OIFitsConstants.KEYWORD_ARRNAME, "name of corresponding array", Types.TYPE_CHAR, 0) {
            @Override
            public String[] getStringAcceptedValues() {
                return getOIFitsFile().getAcceptedArrNames();
            }
        });

        // INSNAME  keyword definition
        addKeywordMeta(new KeywordMeta(OIFitsConstants.KEYWORD_INSNAME, "name of corresponding detector", Types.TYPE_CHAR) {
            @Override
            public String[] getStringAcceptedValues() {
                return getOIFitsFile().getAcceptedInsNames();
            }
        });

        // TARGET_ID  column definition
        addColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_TARGET_ID,
                "target number as index into OI_TARGET table", Types.TYPE_INT) {
            @Override
            public short[] getIntAcceptedValues() {
                return getOIFitsFile().getAcceptedTargetIds();
            }
        });

        // TIME  column definition
        addColumnMeta(COLUMN_TIME);

        // MJD  column definition
        addColumnMeta(COLUMN_MJD);

        // INT_TIME  column definition
        addColumnMeta(COLUMN_INT_TIME);

        // Derived EFF_WAVE_DBL column definition
        addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_EFF_WAVE, "effective wavelength of channel", Types.TYPE_DBL, Units.UNIT_METER, this));

        // Derived STA_CONF column definition
        addDerivedColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_STA_CONF, "station configuration", Types.TYPE_INT, 2)); // fake repeat to mimic 2D array

        // Derived SPATIAL_FREQ column definition
        addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_SPATIAL_FREQ, "spatial frequencies", Types.TYPE_DBL, DataRange.RANGE_POSITIVE, this));
    }

    /**
     * Return the number of measurements in this table.
     * @return the number of measurements.
     */
    public final int getNbMeasurements() {
        return getNbRows();
    }

    /* 
     * --- Keywords ------------------------------------------------------------
     */
    /**
     * Get the DATE-OBS keyword value.
     * @return the value of DATE-OBS keyword
     */
    public final String getDateObs() {
        return getKeyword(OIFitsConstants.KEYWORD_DATE_OBS);
    }

    /**
     * Define the DATE-OBS keyword value
     * @param dateObs value of DATE-OBS keyword
     */
    public final void setDateObs(final String dateObs) {
        setKeyword(OIFitsConstants.KEYWORD_DATE_OBS, dateObs);
    }

    /**
     * Return the Optional ARRNAME keyword value.
     * @return the value of ARRNAME keyword if present, NULL otherwise.
     */
    public final String getArrName() {
        return getKeyword(OIFitsConstants.KEYWORD_ARRNAME);
    }

    /**
     * Define the Optional ARRNAME keyword value
     * @param arrName value of ARRNAME keyword
     */
    public final void setArrName(final String arrName) {
        setKeyword(OIFitsConstants.KEYWORD_ARRNAME, arrName);
        // reset cached reference :
        this.oiArrayRef = null;
    }

    /**
     * Get the INSNAME keyword value.
     * @return the value of INSNAME keyword
     */
    public final String getInsName() {
        return getKeyword(OIFitsConstants.KEYWORD_INSNAME);
    }

    /**
     * Define the INSNAME keyword value
     * @param insName value of INSNAME keyword
     */
    protected final void setInsName(final String insName) {
        setKeyword(OIFitsConstants.KEYWORD_INSNAME, insName);
        // reset cached reference :
        this.oiWavelengthRef = null;
    }

    /* 
     * --- Columns -------------------------------------------------------------
     */
    /**
     * Return the TARGET_ID column.
     * @return the TARGET_ID column.
     */
    public short[] getTargetId() {
        return this.getColumnShort(OIFitsConstants.COLUMN_TARGET_ID);
    }

    /**
     * Return the TIME column.
     * @return the TIME column.
     */
    public double[] getTime() {
        return this.getColumnDouble(OIFitsConstants.COLUMN_TIME);
    }

    /**
     * Return the MJD column.
     * @return the MJD column.
     */
    public double[] getMjd() {
        return this.getColumnDouble(OIFitsConstants.COLUMN_MJD);
    }

    /**
     * Return the INT_TIME column.
     * @return the INT_TIME column.
     */
    public double[] getIntTime() {
        return this.getColumnDouble(OIFitsConstants.COLUMN_INT_TIME);
    }

    /**
     * Return the STA_INDEX column.
     * @return the STA_INDEX column.
     */
    public final short[][] getStaIndex() {
        return this.getColumnShorts(OIFitsConstants.COLUMN_STA_INDEX);
    }

    /**
     * Return the FLAG column.
     * @return the FLAG column.
     */
    public final boolean[][] getFlag() {
        return this.getColumnBooleans(OIFitsConstants.COLUMN_FLAG);
    }

    /* --- Alternate data representation methods --- */
    /**
     * Return the effective wavelength of channel as double arrays (2D)
     * @return the wavelength of channel array as double arrays (2D)
     */
    public double[][] getEffWaveAsDoubles() {
        // lazy:
        double[][] effWaveDbls = this.getColumnDerivedDoubles(OIFitsConstants.COLUMN_EFF_WAVE);

        if (effWaveDbls == null) {
            final int nRows = getNbRows();
            final int nWaves = getNWave();
            effWaveDbls = new double[nRows][nWaves];

            final double[] effWaves = getOiWavelength().getEffWaveAsDouble();

            for (int i = 0; i < nRows; i++) {
                effWaveDbls[i] = effWaves;
            }

            this.setColumnDerivedValue(OIFitsConstants.COLUMN_EFF_WAVE, effWaveDbls);
        }

        return effWaveDbls;
    }

    /**
     * Return the station configuration as short arrays (2D)
     * @see Analyzer#processStaConf(fr.jmmc.oitools.model.OIData) which fills that column
     * @return the station configuration as short arrays (2D)
     */
    public short[][] getStaConf() {
        // lazy:
        short[][] staConfs = this.getColumnDerivedShorts(OIFitsConstants.COLUMN_STA_CONF);

        if (staConfs == null) {
            staConfs = new short[getNbRows()][];

            // not filled here: see Analyzer

            this.setColumnDerivedValue(OIFitsConstants.COLUMN_STA_CONF, staConfs);
        }

        return staConfs;
    }

    /**
     * Return the spatial frequencies column.
     *
     * @return the computed spatial frequencies r[x][y] (x,y for coordIndex,effWaveIndex)
     */
    public abstract double[][] getSpatialFreq();

    /**
     * Return the spatial coordinates given the coordinates array = coordinates / effWave
     * @param coord coordinates array
     * @return the computed spatial coordinates r[x][y] (x,y for coordIndex,effWaveIndex) .
     */
    protected double[][] getSpatialCoord(final double[] coord) {
        final int nRows = getNbRows();
        final int nWaves = getNWave();

        final double[][] r = new double[nRows][nWaves];
        final double[] effWaves = getOiWavelength().getEffWaveAsDouble();

        double[] row;
        double c;
        for (int i = 0, j; i < nRows; i++) {
            row = r[i];
            c = coord[i];
            for (j = 0; j < nWaves; j++) {
                row[j] = c / effWaves[j];
            }
        }
        return r;
    }

    /* --- Utility methods for cross-referencing --- */
    /**
     * Return the associated OITarget table.
     * @return the associated OITarget
     */
    public final OITarget getOiTarget() {
        return getOIFitsFile().getOiTarget();
    }

    /**
     * Return the associated optional OIArray table.
     * @return the associated OIArray or null if the keyword ARRNAME is undefined
     */
    public final OIArray getOiArray() {
        /** cached resolved reference */
        if (this.oiArrayRef != null) {
            return this.oiArrayRef;
        }

        final String arrName = getArrName();
        if (arrName != null) {
            final OIArray oiArray = getOIFitsFile().getOiArray(getArrName());

            if (oiArray != null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Resolved OI_Array reference [" + oiArray.getExtNb() + "] to " + super.toString());
                }
                this.oiArrayRef = oiArray;
            }
            return oiArray;
        }

        return null;
    }

    /**
     * Mediator method to resolve cross references. Returns the accepted (ie
     * valid) station indexes for the associated OIArray table.
     *
     * @return the array containing the indexes.
     */
    public final short[] getAcceptedStaIndexes() {
        return getOIFitsFile().getAcceptedStaIndexes(getOiArray());
    }

    /**
     * Return the associated OIWavelength table.
     * @return the associated OIWavelength
     */
    public final OIWavelength getOiWavelength() {
        /** cached resolved reference */
        if (this.oiWavelengthRef != null) {
            return this.oiWavelengthRef;
        }

        final String insName = getInsName();
        if (insName != null) {
            final OIWavelength oiWavelength = getOIFitsFile().getOiWavelength(insName);

            if (oiWavelength != null) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Resolved OI_WAVELENGTH reference [" + oiWavelength.getExtNb() + " | NWAVE=" + oiWavelength.getNWave() + " ] to " + super.toString());
                }
                this.oiWavelengthRef = oiWavelength;
            } else {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.warning("Missing OI_WAVELENGTH identified by '" + insName + "'");
                }
            }
            return oiWavelength;
        }

        return null;
    }

    /**
     * Return the number of distinct spectral channels of the associated OI_WAVELENGTH.
     * @return the number of distinct spectral channels of the associated OI_WAVELENGTH.
     */
    public final int getNWave() {
        final OIWavelength oiWavelength = getOiWavelength();
        if (oiWavelength != null) {
            return oiWavelength.getNWave();
        }
        return 0;
    }

    /* --- Other methods --- */
    /**
     * Returns a string representation of this table
     * @return a string representation of this table
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(32);
        sb.append(super.toString());
        sb.append(" [ INSNAME=").append(getInsName());
        sb.append(" NB_MEASUREMENTS=").append(getNbMeasurements());

        if (nFlagged > 0) {
            sb.append(" (").append(nFlagged).append(" data flagged out - ");
            sb.append(getNbRows() * getNWave() - nFlagged).append(" data ok )");
        }
        sb.append(" ]");
        return sb.toString();
    }

    /**
     * Add arrname and oiarray test in addition to OITable.checkKeywords()
     * @param checker checker component
     */
    @Override
    public final void checkKeywords(final OIFitsChecker checker) {
        super.checkKeywords(checker);

        if (getArrName() != null && getOiArray() == null) {
            /* No keyword with keywordName name */
            checker.severe("Missing OI_ARRAY table that describes the '" + getArrName() + "' array");
        }
    }

    /* 
     * --- public data access --------------------------------------------------------- 
     */
    /**
     * Return the derived column data as double array (1D) for the given column name
     * To be override in child classes for lazy computed columns
     * @param name any column name 
     * @return column data as double array (1D) or null if undefined or wrong type
     */
    @Override
    protected double[] getDerivedColumnAsDouble(final String name) {
        throw new IllegalStateException("Code not yet implemented");
        // TODO: HA ...
        //return null;
    }

    /**
     * Return the derived column data as double arrays (2D) for the given column name
     * To be overriden in child classes for lazy computed columns
     * @param name any column name 
     * @return column data as double arrays (2D) or null if undefined or wrong type
     */
    @Override
    protected double[][] getDerivedColumnAsDoubles(final String name) {
        if (OIFitsConstants.COLUMN_SPATIAL_FREQ.equals(name)) {
            return getSpatialFreq();
        }
        if (OIFitsConstants.COLUMN_EFF_WAVE.equals(name)) {
            return getEffWaveAsDoubles();
        }

        return null;
    }


    /* --- data analysis --- */
    /**
     * Indicate to clear any cached value (derived column ...)
     */
    @Override
    public void setChanged() {
        super.setChanged();
        distinctTargetId.clear();
        distinctStaIndex.clear();
        staIndexesToString.clear();
        distinctStaConf.clear();
        nFlagged = -1;
    }

    /**
     * Return the wavelenth range (min - max)
     * @return float[]{min, max}
     */
    public float[] getEffWaveRange() {
        final OIWavelength oiWavelength = getOiWavelength();
        if (oiWavelength != null) {
            return oiWavelength.getEffWaveRange();
        }
        return null;
    }

    public Set<Short> getDistinctTargetId() {
        return distinctTargetId;
    }

    public boolean hasSingleTarget() {
        return getDistinctTargetId().size() == 1;
    }

    /**
     * Return the targetId corresponding to the given target (name) or null if missing
     * @param target target (name)
     * @return targetId corresponding to the given target (name) or null if missing
     */
    public Short getTargetId(final String target) {
        final OITarget oiTarget = getOiTarget();
        if (oiTarget != null) {
            return oiTarget.getTargetId(target);
        }
        return null;
    }

    public Set<short[]> getDistinctStaIndex() {
        return distinctStaIndex;
    }

    public int getDistinctStaIndexCount() {
        return distinctStaIndex.size();
    }

    public short[][] getDistinctStaIndexes() {
        final short[][] distinctStaIndexes = new short[distinctStaIndex.size()][];

        int i = 0;
        for (short[] staIndexes : distinctStaIndex) {
            distinctStaIndexes[i++] = staIndexes;
        }

        return distinctStaIndexes;
    }

    public Set<short[]> getDistinctStaConf() {
        return distinctStaConf;
    }

    public int getDistinctStaConfCount() {
        return distinctStaConf.size();
    }

    /**
     * TODO: move this 
     * @param staIndexes
     * @return 
     */
    public String getStaNames(final short[] staIndexes) {
        if (staIndexes == null) {
            return "";
        }
        final OIArray oiArray = getOiArray();

        if (oiArray == null) {
            // warning: identity hashcode so use carefully using distinct array instances:
            String label = staIndexesToString.get(staIndexes);

            if (label == null) {
                final StringBuilder sb = new StringBuilder(32);

                for (short staIndex : staIndexes) {
                    sb.append(staIndex).append(' ');
                }
                sb.setLength(sb.length() - 1);

                label = sb.toString();

                staIndexesToString.put(staIndexes, label);
            }
            return label;
        }
        return oiArray.getStaNames(staIndexes);
    }

    public int getNFlagged() {
        return nFlagged;
    }

    protected void setNFlagged(final int nFlagged) {
        this.nFlagged = nFlagged;
    }
    
    /**
     * Return true if the given error value is valid ie. NaN or is positive or equals to 0
     * @param err error value
     * @return true if the given error value is valid 
     */
    public static boolean isErrorValid(final double err) {
        return (Double.isNaN(err) || err >= 0d);
    }
}
/*___oOo___*/
