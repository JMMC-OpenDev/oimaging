/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.OIFitsConstants;
import static fr.jmmc.oitools.meta.CellMeta.NO_STR_VALUES;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.DataRange;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.meta.Units;
import fr.jmmc.oitools.meta.WaveColumnMeta;
import fr.jmmc.oitools.util.MathUtils;

/**
 * Class for OI_VIS table.
 */
public final class OIVis extends OIData {

    /**
     * Public OIVis class constructor
     * @param oifitsFile main OifitsFile
     */
    public OIVis(final OIFitsFile oifitsFile) {
        super(oifitsFile);

        if (oifitsFile.isOIFits2()) {
            // AMPTYP  keyword definition
            addKeywordMeta(new KeywordMeta(OIFitsConstants.KEYWORD_AMPTYP,
                    "'absolute, 'differential', correlated flux'", Types.TYPE_CHAR, true,
                    new String[]{OIFitsConstants.KEYWORD_AMPTYP_ABSOLUTE, OIFitsConstants.KEYWORD_AMPTYP_CORR,
                        OIFitsConstants.KEYWORD_AMPTYP_DIFF}));
            // PHITYP  keyword definition
            addKeywordMeta(new KeywordMeta(OIFitsConstants.KEYWORD_PHITYP,
                    "'absolute, 'differential'", Types.TYPE_CHAR, true,
                    new String[]{OIFitsConstants.KEYWORD_PHITYP_ABSOLUTE, OIFitsConstants.KEYWORD_PHITYP_DIFF}));
            // AMPORDER  keyword definition
            addKeywordMeta(new KeywordMeta(OIFitsConstants.KEYWORD_AMPORDER,
                    "Polynomial fit order for differential chromatic amplitudes", Types.TYPE_INT, true, NO_STR_VALUES));
            // PHIORDER  keyword definition
            addKeywordMeta(new KeywordMeta(OIFitsConstants.KEYWORD_PHIORDER,
                    "Polynomial fit order for differential chromatic phases", Types.TYPE_INT, true, NO_STR_VALUES));
        }

        if (DataModel.hasOiVisComplexSupport()) {
            // Optional Complex visibilities (ASPRO or AMBER - not OIFits) :
            // VISDATA column definition
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISDATA, "raw complex visibilities",
                    Types.TYPE_COMPLEX, OIFitsConstants.COLUMN_VISERR, true, this));

            // VISERR  column definition
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISERR, "error in raw complex visibilities",
                    Types.TYPE_COMPLEX, true, false, this));
        }

        // VISAMP  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISAMP, "visibility amplitude",
                Types.TYPE_DBL, OIFitsConstants.COLUMN_VISAMPERR, RANGE_VIS, this));

        // VISAMPERR  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISAMPERR, "error in visibility amplitude",
                Types.TYPE_DBL, DataRange.RANGE_POSITIVE, this));

        // VISPHI  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISPHI, "visibility phase", Types.TYPE_DBL,
                Units.UNIT_DEGREE, OIFitsConstants.COLUMN_VISPHIERR, RANGE_ANGLE, this));

        // VISPHIERR  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISPHIERR, "error in visibility phase",
                Types.TYPE_DBL, Units.UNIT_DEGREE, DataRange.RANGE_POSITIVE, this));

        // if IMAGE_OI support is enabled
        if (DataModel.hasOiModelColumnsSupport()) {

            // VIS MODEL columns definition (optional)
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_NS_MODEL_VISAMP, "model of the visibility amplitude",
                    Types.TYPE_DBL, true, false, NO_STR_VALUES, Units.NO_UNIT, null, RANGE_VIS, this));
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_NS_MODEL_VISPHI, "model of the visibility phase",
                    Types.TYPE_DBL, true, false, NO_STR_VALUES, Units.UNIT_DEGREE, null, RANGE_ANGLE, this));

        }

        // UCOORD  column definition
        addColumnMeta(COLUMN_UCOORD);

        // VCOORD  column definition
        addColumnMeta(COLUMN_VCOORD);

        // STA_INDEX  column definition
        addColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_STA_INDEX, "station numbers contributing to the data",
                Types.TYPE_SHORT, 2) {
            @Override
            public short[] getIntAcceptedValues() {
                return getAcceptedStaIndexes();
            }
        });

        // FLAG  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_FLAG, "flag", Types.TYPE_LOGICAL, this));

        if (oifitsFile.isOIFits2()) {
            // Derived CORRINDX_VISAMP column definition
            addColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_CORRINDX_VISAMP, "Index into correlation matrix for 1st VISAMP element",
                    Types.TYPE_INT, 1, true, false, Units.NO_UNIT));
            // Derived CORRINDX_VISPHI column definition
            addColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_CORRINDX_VISPHI, "Index into correlation matrix for 1st VISPHI element",
                    Types.TYPE_INT, 1, true, false, Units.NO_UNIT));
            // Derived VISREFMAP column definition
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISREFMAP, "Matrix of indexes for establishing the reference channels",
                    Types.TYPE_LOGICAL, true, true, NO_STR_VALUES, Units.NO_UNIT, null, null, this));
            // Derived RVIS column definition
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_RVIS, "Complex coherent flux (Real) in units of TUNITn",
                    Types.TYPE_DBL, true, false, NO_STR_VALUES, Units.NO_UNIT, null, null, this));
            // Derived RVISERR column definition
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_RVISERR, "Error RVIS",
                    Types.TYPE_DBL, true, false, NO_STR_VALUES, Units.NO_UNIT, null, null, this));
            // Derived CORRINDX_RVIS column definition
            addColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_CORRINDX_RVIS, "Index into correlation matrix for 1st RVIS element",
                    Types.TYPE_INT, 1, true, false, Units.NO_UNIT));
            // Derived IVIS column definition
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_IVIS, "Complex coherent flux (Imaginary) in units of TUNITn",
                    Types.TYPE_DBL, true, false, NO_STR_VALUES, Units.NO_UNIT, null, null, this));
            // Derived IVISERR column definition
            addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_IVISERR, "Error IVIS",
                    Types.TYPE_DBL, true, false, NO_STR_VALUES, Units.NO_UNIT, null, null, this));
            // Derived CORRINDX_IVIS column definition
            addColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_CORRINDX_IVIS, "Index into correlation matrix for 1st IVIS element",
                    Types.TYPE_INT, 1, true, false, Units.NO_UNIT));
        }

        // Derived SPATIAL_U_FREQ column definition
        addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_UCOORD_SPATIAL, "spatial U frequency", Types.TYPE_DBL, this));

        // Derived SPATIAL_V_FREQ column definition
        addDerivedColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VCOORD_SPATIAL, "spatial V frequency", Types.TYPE_DBL, this));
    }

    /**
     * Public OIVis class constructor to create a new table
     * @param oifitsFile main OifitsFile
     * @param insName value of INSNAME keyword
     * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
     */
    public OIVis(final OIFitsFile oifitsFile, final String insName, final int nbRows) {
        this(oifitsFile);

        setInsName(insName);

        this.initializeTable(nbRows);
    }

    /* --- keywords --- */
    /**
     * Get the value of AMPTYPE keyword
     * @return the value of AMPTYPE keyword
     */
    public String getAmpTyp() {
        return getKeyword(OIFitsConstants.KEYWORD_AMPTYP);
    }

    /**
     * Define the value of AMPTYPE keyword
     * @param amptype value of AMPTYPE keyword
     */
    public void setAmpTyp(String amptype) {
        setKeyword(OIFitsConstants.KEYWORD_AMPTYP, amptype);
    }

    /**
     * Get the value of PHITYP keyword
     * @return the value of PHITYP keyword
     */
    public String getPhiType() {
        return getKeyword(OIFitsConstants.KEYWORD_PHITYP);
    }

    /**
     * Define the value of PHITYP keyword
     * @param phitype value of PHITYP keyword
     */
    public void setPhiTyp(String phitype) {
        setKeyword(OIFitsConstants.KEYWORD_PHITYP, phitype);
    }

    /**
     * Get the value of AMPORDER keyword
     * @return the value of AMPORDER keyword
     */
    public int getAmpOrder() {
        return getKeywordInt(OIFitsConstants.KEYWORD_AMPORDER);
    }

    /**
     * Define the value of AMPORDER, keyword
     * @param amporder value of AMPORDER, keyword
     */
    public void setAmpOrder(int amporder) {
        setKeywordInt(OIFitsConstants.KEYWORD_AMPORDER, amporder);
    }

    /**
     * Get the value of PHIORDER keyword
     * @return the value of PHIORDER keyword
     */
    public int getPhiOrder() {
        return getKeywordInt(OIFitsConstants.KEYWORD_PHIORDER);
    }

    /**
     * Define the value of PHIORDER, keyword
     * @param phiorder value of PHIORDER, keyword
     */
    public void setPhiOrder(int phiorder) {
        setKeywordInt(OIFitsConstants.KEYWORD_PHIORDER, phiorder);
    }

    /* --- Columns --- */
    /**
     * Return the optional VISDATA column.
     * @return the VISDATA column or null if missing.
     */
    public float[][][] getVisData() {
        return this.getColumnComplexes(OIFitsConstants.COLUMN_VISDATA);
    }

    /**
     * Return the optional VISERR column.
     * @return the VISERR column or null if missing.
     */
    public float[][][] getVisErr() {
        return this.getColumnComplexes(OIFitsConstants.COLUMN_VISERR);
    }

    /**
     * Return the VISAMP column.
     * @return the VISAMP column.
     */
    public double[][] getVisAmp() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_VISAMP);
    }

    /**
     * Return the VISAMPERR column.
     * @return the VISAMPERR column.
     */
    public double[][] getVisAmpErr() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_VISAMPERR);
    }

    /**
     * Return the VISPHI column.
     * @return the VISPHI column.
     */
    public double[][] getVisPhi() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_VISPHI);
    }

    /**
     * Return the VISPHIERR column.
     * @return the VISPHIERR column.
     */
    public double[][] getVisPhiErr() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_VISPHIERR);
    }

    /**
     * Return the UCOORD column.
     * @return the UCOORD column.
     */
    public double[] getUCoord() {
        return this.getColumnDouble(OIFitsConstants.COLUMN_UCOORD);
    }

    /**
     * Return the VCOORD column.
     * @return the VCOORD column.
     */
    public double[] getVCoord() {
        return this.getColumnDouble(OIFitsConstants.COLUMN_VCOORD);
    }

    /**
     * Return the CORRINDX_VISAMP column.
     * @return the CORRINDX_VISAMP column.
     */
    public int[] getCorrIndxVisAmp() {
        return this.getColumnInt(OIFitsConstants.COLUMN_CORRINDX_VISAMP);
    }

    /**
     * Return the CORRINDX_VISPHI column.
     * @return the CORRINDX_VISPHI column.
     */
    public int[] getCorrIndxVisPhi() {
        return this.getColumnInt(OIFitsConstants.COLUMN_CORRINDX_VISPHI);
    }

    /**
     * Return the VISREFMAP column.
     * @return the VISREFMAP column.
     */
    public boolean[][][] getVisRefMap() {
        return this.getColumnBoolean3D(OIFitsConstants.COLUMN_VISREFMAP);
    }

    /**
     * Return the RVIS column.
     * @return the RVIS column.
     */
    public double[][] getRVis() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_RVIS);
    }

    /**
     * Return the RVISERR column.
     * @return the RVISERR column.
     */
    public double[][] getRVisErr() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_RVISERR);
    }

    /**
     * Return the CORRINDX_RVIS column.
     * @return the CORRINDX_RVIS column.
     */
    public int[] getCorrIndxRVis() {
        return this.getColumnInt(OIFitsConstants.COLUMN_CORRINDX_RVIS);
    }

    /**
     * Return the IVIS column.
     * @return the IVIS column.
     */
    public double[][] getIVis() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_IVIS);
    }

    /**
     * Return the IVISERR column.
     * @return the IVISERR column.
     */
    public double[][] getIVisErr() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_IVISERR);
    }

    /**
     * Return the CORRINDX_IVIS column.
     * @return the CORRINDX_IVIS column.
     */
    public int[] getCorrIndxIVis() {
        return this.getColumnInt(OIFitsConstants.COLUMN_CORRINDX_IVIS);
    }


    /*
     * --- public data access ---------------------------------------------------------
     */
    /**
     * Return the derived column data as double arrays (2D) for the given column name
     * To be overriden in child classes for lazy computed columns
     * @param name any column name
     * @return column data as double arrays (2D) or null if undefined or wrong type
     */
    @Override
    protected double[][] getDerivedColumnAsDoubles(final String name) {
        if (OIFitsConstants.COLUMN_UCOORD_SPATIAL.equals(name)) {
            return getSpatialUCoord();
        }
        if (OIFitsConstants.COLUMN_VCOORD_SPATIAL.equals(name)) {
            return getSpatialVCoord();
        }
        return super.getDerivedColumnAsDoubles(name);
    }

    /* --- Alternate data representation methods --- */
    /**
     * Return the spatial frequencies column.  The computation is based
     * on ucoord and vcoord.
     * sqrt(ucoord^2+vcoord^2)/effWave
     *
     * @return the computed spatial frequencies r[x][y] (x,y for coordIndex,effWaveIndex)
     */
    @Override
    public double[][] getSpatialFreq() {
        // lazy:
        double[][] spatialFreq = this.getColumnDerivedDoubles(OIFitsConstants.COLUMN_SPATIAL_FREQ);

        if (spatialFreq == null) {
            final int nRows = getNbRows();
            final int nWaves = getNWave();
            spatialFreq = new double[nRows][nWaves];

            if (nWaves != 0) {
                final double[] effWaves = getOiWavelength().getEffWaveAsDouble();
                final double[] ucoord = getUCoord();
                final double[] vcoord = getVCoord();

                double[] row;
                double r;
                for (int i = 0, j; i < nRows; i++) {
                    row = spatialFreq[i];
                    r = MathUtils.carthesianNorm(ucoord[i], vcoord[i]);

                    for (j = 0; j < nWaves; j++) {
                        row[j] = r / effWaves[j];
                    }
                }
            }
            this.setColumnDerivedValue(OIFitsConstants.COLUMN_SPATIAL_FREQ, spatialFreq);
        }

        return spatialFreq;
    }

    /**
     * Return the spatial ucoord.
     * ucoord/effWave
     *
     * @return the computed spatial coords r[x][y] (x,y for coordIndex,effWaveIndex) .
     */
    public double[][] getSpatialUCoord() {
        return getSpatialCoord(OIFitsConstants.COLUMN_UCOORD_SPATIAL, OIFitsConstants.COLUMN_UCOORD);
    }

    /**
     * Return the spatial vcoord.
     * vcoord/effWave
     *
     * @return the computed spatial coords r[x][y] (x,y for coordIndex,effWaveIndex) .
     */
    public double[][] getSpatialVCoord() {
        return getSpatialCoord(OIFitsConstants.COLUMN_VCOORD_SPATIAL, OIFitsConstants.COLUMN_VCOORD);
    }

    /**
     * Return the radius column i.e. projected base line (m).
     *
     * @return the computed radius r[x] (x for coordIndex)
     */
    public double[] getRadius() {
        // lazy:
        double[] radius = this.getColumnDerivedDouble(OIFitsConstants.COLUMN_RADIUS);

        if (radius == null) {
            final int nRows = getNbRows();
            radius = new double[nRows];

            final double[] ucoord = getUCoord();
            final double[] vcoord = getVCoord();

            for (int i = 0; i < nRows; i++) {
                radius[i] = MathUtils.carthesianNorm(ucoord[i], vcoord[i]);
            }

            this.setColumnDerivedValue(OIFitsConstants.COLUMN_RADIUS, radius);
        }

        return radius;
    }

    /**
     * Return the position angle column i.e. position angle of the projected base line (deg).
     *
     * @return the computed position angle r[x] (x for coordIndex)
     */
    public double[] getPosAngle() {
        // lazy:
        double[] angle = this.getColumnDerivedDouble(OIFitsConstants.COLUMN_POS_ANGLE);

        if (angle == null) {
            final int nRows = getNbRows();
            angle = new double[nRows];

            final double[] ucoord = getUCoord();
            final double[] vcoord = getVCoord();

            for (int i = 0, j; i < nRows; i++) {
                angle[i] = Math.toDegrees(Math.atan2(ucoord[i], vcoord[i]));
            }

            this.setColumnDerivedValue(OIFitsConstants.COLUMN_POS_ANGLE, angle);
        }

        return angle;
    }

    /* --- Other methods --- */
    /**
     * Do syntactical analysis.
     * @param checker checker component
     */
    @Override
    public void checkSyntax(final OIFitsChecker checker) {
        super.checkSyntax(checker);

        final int nRows = getNbRows();
        // TODO: use VIS dimensions (Nwave = 0 if missing table)
        final int nWaves = getNWave();

        final boolean[][] flags = getFlag();
        final double[][] visAmpErr = getVisAmpErr();
        final double[][] visPhiErr = getVisPhiErr();

        boolean[] rowFlag;
        double[] rowVisampErr, rowVisPhiErr;

        for (int i = 0, j; i < nRows; i++) {
            rowFlag = flags[i];
            rowVisampErr = visAmpErr[i];
            rowVisPhiErr = visPhiErr[i];

            for (j = 0; j < nWaves; j++) {
                if (!rowFlag[j]) {
                    // Not flagged:
                    if (!isErrorValid(rowVisampErr[j])) {
                        checker.severe("Invalid value at index " + j + " for column '" + OIFitsConstants.COLUMN_VISAMPERR
                                + "' line " + i + ", found '" + rowVisampErr[j] + "' should be >= 0 or NaN or flagged out");
                    }
                    if (!isErrorValid(rowVisPhiErr[j])) {
                        checker.severe("Invalid value at index " + j + " for column '" + OIFitsConstants.COLUMN_VISPHIERR
                                + "' line " + i + ", found '" + rowVisPhiErr[j] + "' should be >= 0 or NaN or flagged out");
                    }
                }
            }
        }

        // check STA_INDEX Unique
        checkStaIndexes(checker, getStaIndex());

        // OIFITS2: check OI_CORR indexes
        final OICorr oiCorr = getOiCorr();
        final int[] corrindx_visAmp = getCorrIndxVisAmp();
        final int[] corrindx_visPhi = getCorrIndxVisPhi();
        final int[] corrindx_visRvis = getCorrIndxRVis();
        final int[] corrindx_visIvis = getCorrIndxIVis();

        if (corrindx_visAmp != null || corrindx_visPhi != null || corrindx_visRvis != null
                || corrindx_visIvis != null) {
            if (oiCorr == null) {
                //TODO : fix message
                checker.severe("Missing OI_CORR table but the column CORRINDX_VISAMP,"
                        + " CORRINDX_VISPHI, CORRINDX_RVIS or CORRINDX_IVIS is defined.");
            } else {
                // column is defined
                if (corrindx_visAmp != null) {
                    checkCorrIndex(checker, oiCorr, this, OIFitsConstants.COLUMN_CORRINDX_VISAMP, corrindx_visAmp);
                }
                if (corrindx_visPhi != null) {
                    checkCorrIndex(checker, oiCorr, this, OIFitsConstants.COLUMN_CORRINDX_VISPHI, corrindx_visPhi);
                }
                if (corrindx_visRvis != null) {
                    checkCorrIndex(checker, oiCorr, this, OIFitsConstants.COLUMN_CORRINDX_RVIS, corrindx_visRvis);
                }
                if (corrindx_visIvis != null) {
                    checkCorrIndex(checker, oiCorr, this, OIFitsConstants.COLUMN_CORRINDX_IVIS, corrindx_visIvis);
                }
            }
        }
    }
}
/*___oOo___*/
