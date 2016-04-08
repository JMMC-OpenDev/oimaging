/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.DataRange;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.meta.Units;
import fr.jmmc.oitools.meta.WaveColumnMeta;
import fr.jmmc.oitools.util.MathUtils;
import java.util.Arrays;
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
    /** MJD starting at 1-JAN-2000 00:00 UT */
    public final double MJD2000 = 51544.5d;
    /** Inverse of century */
    public static final double INV_CENTURY = 1d / 36525d; // 1/ (100 * YEAR)
    /** data range representing visibilities [0; 1] with margins = [-0.1; 1.1] */
    public final static DataRange RANGE_VIS = new DataRange(-0.1, 1.1);
    /** data range representing angle range [-180; 180] with margins = [-200; 200] */
    public final static DataRange RANGE_ANGLE = new DataRange(-200.0, 200.0);
    /* static descriptors */
    /** DATE-OBS keyword descriptor */
    private final static KeywordMeta KEYWORD_DATE_OBS = new KeywordMeta(OIFitsConstants.KEYWORD_DATE_OBS, "UTC start date of observations", Types.TYPE_CHAR);
    /** TIME column descriptor */
    private final static ColumnMeta COLUMN_TIME = new ColumnMeta(OIFitsConstants.COLUMN_TIME, "UTC time of observation", Types.TYPE_DBL, Units.UNIT_SECOND, DataRange.RANGE_POSITIVE);
    /** MJD column descriptor */
    private final static ColumnMeta COLUMN_MJD = new ColumnMeta(OIFitsConstants.COLUMN_MJD, "modified Julian Day", Types.TYPE_DBL, Units.UNIT_MJD);
    /** INT_TIME column descriptor */
    private final static ColumnMeta COLUMN_INT_TIME = new ColumnMeta(OIFitsConstants.COLUMN_INT_TIME, "integration time", Types.TYPE_DBL, Units.UNIT_SECOND, DataRange.RANGE_POSITIVE_STRICT);
    /** UCOORD column descriptor */
    protected final static ColumnMeta COLUMN_UCOORD = new ColumnMeta(OIFitsConstants.COLUMN_UCOORD, "U coordinate of the data", Types.TYPE_DBL, Units.UNIT_METER);
    /** VCOORD column descriptor */
    protected final static ColumnMeta COLUMN_VCOORD = new ColumnMeta(OIFitsConstants.COLUMN_VCOORD, "V coordinate of the data", Types.TYPE_DBL, Units.UNIT_METER);
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
        this(oifitsFile, true);
    }

    /**
     * Protected OIData class constructor
     * @param oifitsFile main OifitsFile
     * @param useCommonCols flag indicating to add common columns (OI_VIS, OI_VIS2, OI_T3)
     */
    protected OIData(final OIFitsFile oifitsFile, final boolean useCommonCols) {
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

        if (useCommonCols) {
            // TIME  column definition
            addColumnMeta(COLUMN_TIME);
        }

        // MJD  column definition
        addColumnMeta(COLUMN_MJD);

        // INT_TIME  column definition
        addColumnMeta(COLUMN_INT_TIME);

        // Derived STA_CONF column definition
        addDerivedColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_STA_CONF, "station configuration", Types.TYPE_INT, 2)); // fake repeat to mimic 2D array

        // Derived EFF_WAVE (double) column definition
        addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_EFF_WAVE, "effective wavelength of channel", Types.TYPE_DBL, Units.UNIT_METER, this));

        if (useCommonCols) {
            // Derived HOUR_ANGLE column definition
            addDerivedColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_HOUR_ANGLE, "hour angle", Types.TYPE_DBL, Units.UNIT_HOUR));

            // Derived RADIUS column definition
            addDerivedColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_RADIUS, "radius i.e. projected base line", Types.TYPE_DBL, Units.UNIT_METER, DataRange.RANGE_POSITIVE));

            // Derived POS_ANGLE column definition
            addDerivedColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_POS_ANGLE, "position angle of the projected base line", Types.TYPE_DBL, Units.UNIT_DEGREE, RANGE_ANGLE));

            // Derived SPATIAL_FREQ column definition
            addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_SPATIAL_FREQ, "spatial frequencies", Types.TYPE_DBL, DataRange.RANGE_POSITIVE, this));
        }
        
        // Derived NIGHT_ID column definition
        addDerivedColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_NIGHT_ID, "night identifier", Types.TYPE_DBL));
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

    /* --- Derived Column from expression --- */
    /**
     * Verify the validity of the expression
     * @param name name of the new column
     * @param expression expression to verify
     * @throws RuntimeException
     */
    public void checkExpression(final String name, final String expression) throws RuntimeException {

        // Test if name conflicts with standard columns (VIS2DATA)
        if (getColumnDesc(name) != null) {
            throw new IllegalArgumentException("Column name [" + name + "] already existing (OIFITS standard) !");
        }

        // Try expression now: may throw RuntimeException
        ExpressionEvaluator.getInstance().eval(this, name, expression, true);
    }

    /**
     * Make the creation or modification of a column given its name and expression
     * @param name name of the column
     * @param expression expression of the column
     */
    public void updateExpressionColumn(final String name, final String expression) {
        // remove column (descriptor and values) if existing:
        removeExpressionColumn(name);

        addDerivedColumnMeta(new WaveColumnMeta(name, "expression: " + expression,
                Types.TYPE_DBL, this, expression));

        // Force computation now (not lazy):
        getExprColumnDoubles(name, expression);
    }

    /**
     * Remove the column given its name.
     * @param name name of the column
     */
    public void removeExpressionColumn(final String name) {
        final ColumnMeta column = getColumnDerivedDesc(name);

        // check if the column has an expression
        if (column instanceof WaveColumnMeta) {
            final WaveColumnMeta colMeta = (WaveColumnMeta) column;

            if (colMeta.getExpression() != null) {
                // remove descriptor:
                removeDerivedColumnMeta(name);

                // remove values:
                removeColumnDerivedValue(name);
            }
        }
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
     * @return the computed spatial frequencies f[x][y] (x,y for coordIndex, effWaveIndex)
     */
    public abstract double[][] getSpatialFreq();

    /**
     * Return the spatial coordinates given the coordinates array = coordinates / effWave
     * @param name derived column name to get/store spatial coordinates
     * @param coordName coord column name
     * @return the computed spatial coordinates f[x][y] (x,y for coordIndex, effWaveIndex) .
     */
    protected double[][] getSpatialCoord(final String name, final String coordName) {
        // lazy:
        double[][] spatialCoord = this.getColumnDerivedDoubles(name);

        if (spatialCoord == null) {
            final int nRows = getNbRows();
            final int nWaves = getNWave();
            spatialCoord = new double[nRows][nWaves];

            final double[] coord = getColumnAsDouble(coordName);
            final double[] effWaves = getOiWavelength().getEffWaveAsDouble();

            double[] row;
            double c;
            for (int i = 0, j; i < nRows; i++) {
                row = spatialCoord[i];
                c = coord[i];
                for (j = 0; j < nWaves; j++) {
                    row[j] = c / effWaves[j];
                }
            }

            this.setColumnDerivedValue(name, spatialCoord);
        }

        return spatialCoord;
    }

    /**
     * Return the array with the given expression
     * @param name derived column name
     * @param expression expression entered by the user
     * @return the computed expression f[x][y]
     */
    protected double[][] getExprColumnDoubles(final String name, final String expression) {
        // lazy: get previously computed results:
        double[][] exprResults = this.getColumnDerivedDoubles(name);

        // check if expression changed ?
        if (exprResults == null) {
            // not computed; do it now (LAZY):
            exprResults = ExpressionEvaluator.getInstance().eval(this, name, expression, false);

            // store computed results for next time:
            this.setColumnDerivedValue(name, exprResults);
        }
        return exprResults;
    }

    /**
     * Return the radius column i.e. projected base line (m).
     *
     * @return the computed radius r[x] (x for coordIndex)
     */
    public abstract double[] getRadius();

    /**
     * Return the position angle column i.e. position angle of the projected base line (deg).
     *
     * @return the computed position angle r[x] (x for coordIndex)
     */
    public abstract double[] getPosAngle();

    /**
     * Return the hour angle column.
     *
     * @return the computed hour angle
     */
    public double[] getHourAngle() {
        // lazy:
        double[] hourAngle = this.getColumnDerivedDouble(OIFitsConstants.COLUMN_HOUR_ANGLE);

        if (hourAngle == null) {
            final boolean isLogDebug = logger.isLoggable(Level.FINE);

            final int nRows = getNbRows();
            hourAngle = new double[nRows];

            // Initialize hour angle to NaN:
            Arrays.fill(hourAngle, UNDEFINED_DBL);

            // Get array and target tables:
            final OIArray oiArray = getOiArray();
            final OITarget oiTarget = getOiTarget();

            if (oiArray != null && oiTarget != null) {
                final double[] arrayXYZ = oiArray.getArrayXYZ();

                // ensure coordinates != 0 (not undefined; expected correctly set)
                // TODO: add this validity check into OIFits validator:
                if (MathUtils.carthesianNorm(arrayXYZ[0], arrayXYZ[1], arrayXYZ[2]) > 1e-6d) {

                    final double[] lonLatDist = MathUtils.cartesianToSpherical(oiArray.getArrayXYZ());
                    final double arrayLongitude = Math.toDegrees(lonLatDist[0]);

                    if (isLogDebug) {
                        logger.fine("arrayLongitude = " + arrayLongitude + " (deg)");
                    }

                    // Get Target RA/DE columns:
                    final double[] ra = oiTarget.getRaEp0();

                    // Get Target Id column:
                    final short[] targetId = getTargetId();

                    // Get MJD column:
                    final double[] mjd = getMjd();

                    Integer row;
                    double j2000, gmst, T, EPS, OMEGA, L, L1, dL, dE, dT, gast, last;
                    double targetRA, ha;

                    for (int i = 0; i < nRows; i++) {
                        // From Aspro1 formula:
                        // Modified using Matlab JD2GAST:
                        // http://www.mathworks.com/matlabcentral/fileexchange/28232-convert-julian-date-to-greenwich-apparent-sidereal-time/content/JD2GAST.m

                        // let j2000 OI_DATA%OI_VIS2%COL%MJD-51544.5
                        j2000 = mjd[i] - MJD2000; // days from J2000

                        // let julcen j2000/36525.0 (fraction of epoch/century time elapsed since J2000)
                        T = j2000 * INV_CENTURY;

                        // let gmst mod(280.46061837+360.98564736629*j2000,360.0)
                        // gmst = (280.46061837 + 360.98564736629 * j2000) % 360d; // Greenwich Mean Sidereal Time (deg)
                        gmst = ((280.46061837 + 360.98564736629 * j2000) + 0.000387933 * T * T - T * T * T / 38710000.0) % 360.0;

                        // Obliquity of the Ecliptic
                        // let eps 23.43929111-46.815/60/60*julcen
                        EPS = 23.439291 - 0.0130111 * T - 1.64E-07 * T * T + 5.04E-07 * T * T * T; // matlab (deg)

                        // let Om mod(125.04452-1934.136261*julcen,360.0)*PI|180
                        OMEGA = Math.toRadians((125.04452 - 1934.136261 * T) % 360d); // ascending node of sun

                        // let L mod(280.4665+36000.7698*julcen,360.0)*PI|180
                        L = Math.toRadians((280.4665 + 36000.7698 * T) % 360d); // MeanLongOfSun

                        // let L1 mod(218.3165+481267.8813*JULCEN,360.0)*PI|180
                        L1 = Math.toRadians((218.3165 + 481267.8813 * T) % 360d); // MeanLongOfMoon

                        // Explanations:
                        // http://www.cv.nrao.edu/~rfisher/Ephemerides/earth_rot.html#nut
                        // change in the ecliptic longitude of a star due to the nutation (good to about 0.5arcsec)
                        // let dp -17.2*sin(Om)-1.32*sin(2*L)-0.23*sin(2*l1)+0.21*sin(2*Om)
                        dL = -17.2 * Math.sin(OMEGA) - 1.32 * Math.sin(2d * L) - 0.23 * Math.sin(2d * L1) + 0.21 * Math.sin(2d * OMEGA); // arcsec

                        // shift in angle between the ecliptic and equator (good to about 0.1 arcsec)
                        // let de 9.2*cos(Om)+0.57*cos(2*L)+0.1*cos(2*l1)-0.09*cos(2*Om)
                        dE = 9.2 * Math.cos(OMEGA) + 0.57 * Math.cos(2d * L) + 0.1 * Math.cos(2d * L1) - 0.09 * Math.cos(2d * OMEGA); // arcsec

                        // Convert arcsec to degrees:
                        dL /= 3600d;
                        dE /= 3600d;

                        // difference between Mean and Apparent Sidereal Times
                        // let dT dp*cos((de+eps)*PI/180)/3600 seems wrong as de is expressed in arcsec not in degrees !
                        dT = dL * Math.cos(Math.toRadians(dE + EPS)); // deg

                        // Greenwich Apparent sidereal time
                        gast = gmst + dT;

                        // Local Apparent Sidereal Time
                        last = gast + arrayLongitude;

                        if (isLogDebug) {
                            logger.fine("gmst = " + gmst + " (deg)");
                            logger.fine("dT   = " + dT + " (deg)");
                            logger.fine("gast = " + gast + " (deg)");
                            logger.fine("last = " + last + " (deg)");
                        }

                        // Get RA (target):
                        row = oiTarget.getRowIndex(Short.valueOf(targetId[i])); // requires previously OIFits Analyzer call()

                        if (row != null) {
                            targetRA = ra[row.intValue()]; // deg

                            /*
                             * Note: target's coordinates are not precessed up to mjd (as Aspro 2 does)
                             */
                            if (isLogDebug) {
                                logger.fine("ra = " + targetRA + " (deg)");
                            }

                            // let oi%ha (last-oi_data%oi_target%COL%RAEP0)|15.0D0
                            ha = (last - targetRA) / 15d;

                            if (isLogDebug) {
                                logger.fine("ha = " + ha + " (deg)");
                            }

                            // ensure HA in within [-12;12]
                            while (ha < -12d) {
                                ha += 24d;
                            }
                            while (ha > 12d) {
                                ha -= 24d;
                            }

                            if (isLogDebug) {
                                logger.fine("ha (fixed) = " + ha + " (deg)");
                            }

                            hourAngle[i] = ha;
                        }
                    }
                }
            }

            this.setColumnDerivedValue(OIFitsConstants.COLUMN_HOUR_ANGLE, hourAngle);
        }

        return hourAngle;
    }

    /**
     * Return the night identifier column.
     *
     * @return the computed night identifier
     */
    public double[] getNightId() {
        // lazy:
        double[] nightId = this.getColumnDerivedDouble(OIFitsConstants.COLUMN_NIGHT_ID);

        if (nightId == null) {
            final int nRows = getNbRows();
            nightId = new double[nRows];

            double[] mjds = getMjd();
            for (int i = 0; i < nRows; i++) {
                // TODO: use array center coordinates, adjust night
                // TODO: if no MJD, use DATE-OBS + TIME[i] instead
                nightId[i] = (double) Math.round(mjds[i]);
            }

            this.setColumnDerivedValue(OIFitsConstants.COLUMN_NIGHT_ID, nightId);
        }

        return nightId;
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
            } else if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Missing OI_WAVELENGTH table identified by INSNAME='" + insName + "'");
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

        // TODO also add test on DATE-OBS + time
        double[] minMaxMjd = (double[]) getMinMaxColumnValue(COLUMN_MJD.getName());
        double minMjd = minMaxMjd[0];
        double maxMjd = minMaxMjd[1];
        double MJD1950_01_01 = 33282;
        double MJD2150_01_01 = 106332;
        if (maxMjd < MJD1950_01_01 || minMjd > MJD2150_01_01) {
            checker.warning("some MJD values are out of range , min/max [" + minMjd + "-" + maxMjd + "] should probably be into [" + MJD1950_01_01 + " - " + MJD2150_01_01 + "]");
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
        if (OIFitsConstants.COLUMN_RADIUS.equals(name)) {
            return getRadius();
        }
        if (OIFitsConstants.COLUMN_POS_ANGLE.equals(name)) {
            return getPosAngle();
        }
        if (OIFitsConstants.COLUMN_HOUR_ANGLE.equals(name)) {
            return getHourAngle();
        }
        if (OIFitsConstants.COLUMN_NIGHT_ID.equals(name)) {
            return getNightId();
        }
        return null;
    }

    /**
     * Return the derived column data as double arrays (2D) for the given column name
     * To be overriden in child classes for lazy computed columns
     * @param name any column name
     * @return column data as double arrays (2D) or null if undefined or wrong type
     */
    @Override
    protected double[][] getDerivedColumnAsDoubles(final String name) {
        if (OIFitsConstants.COLUMN_EFF_WAVE.equals(name)) {
            return getEffWaveAsDoubles();
        }
        if (OIFitsConstants.COLUMN_SPATIAL_FREQ.equals(name)) {
            return getSpatialFreq();
        }
        // handle user expressions
        for (ColumnMeta column : getColumnDerivedDescCollection()) {
            if (column.getName().equals(name)) {

                if (column instanceof WaveColumnMeta) {
                    WaveColumnMeta colMeta = (WaveColumnMeta) column;

                    if (colMeta.getExpression() != null) {
                        return getExprColumnDoubles(name, colMeta.getExpression());
                    }
                }
            }
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
            return UNDEFINED_STRING;
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
        return NumberUtils.isFinitePositive(err) || Double.isNaN(err);
    }
}
/*___oOo___*/
