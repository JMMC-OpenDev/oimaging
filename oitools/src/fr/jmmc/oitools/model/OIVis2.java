/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.DataRange;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.meta.WaveColumnMeta;

/**
 * Class for OI_VIS2 table.
 */
public final class OIVis2 extends OIData {

    /** 
     * Public OIVis2 class constructor
     * @param oifitsFile main OifitsFile
     */
    public OIVis2(final OIFitsFile oifitsFile) {
        super(oifitsFile);

        // VIS2DATA  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VIS2DATA, "squared visibility", Types.TYPE_DBL, OIFitsConstants.COLUMN_VIS2ERR, RANGE_VIS, this));

        // VIS2ERR  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VIS2ERR, "error in squared visibility", Types.TYPE_DBL, DataRange.RANGE_POSITIVE, this));

        // UCOORD  column definition
        addColumnMeta(COLUMN_UCOORD);

        // VCOORD  column definition
        addColumnMeta(COLUMN_VCOORD);

        // STA_INDEX  column definition
        addColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_STA_INDEX, "station numbers contributing to the data", Types.TYPE_INT, 2) {
            @Override
            public short[] getIntAcceptedValues() {
                return getAcceptedStaIndexes();
            }
        });

        // FLAG  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_FLAG, "flag", Types.TYPE_LOGICAL, this));
    }

    /**
     * Public OIVis2 class constructor to create a new table
     * @param oifitsFile main OifitsFile
     * @param insName value of INSNAME keyword
     * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
     */
    public OIVis2(final OIFitsFile oifitsFile, final String insName, final int nbRows) {
        this(oifitsFile);

        setInsName(insName);

        this.initializeTable(nbRows);
    }

    /* --- Columns --- */
    /**
     * Return the VIS2DATA column.
     * @return the VIS2DATA column.
     */
    public double[][] getVis2Data() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_VIS2DATA);
    }

    /**
     * Return the VIS2ERR column.
     * @return the VIS2ERR column.
     */
    public double[][] getVis2Err() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_VIS2ERR);
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

            final double[] effWaves = getOiWavelength().getEffWaveAsDouble();
            final double[] ucoord = getUCoord();
            final double[] vcoord = getVCoord();

            double[] row;
            double c;
            for (int i = 0, j; i < nRows; i++) {
                row = spatialFreq[i];
                c = Math.sqrt((ucoord[i] * ucoord[i]) + (vcoord[i] * vcoord[i]));

                for (j = 0; j < nWaves; j++) {
                    row[j] = c / effWaves[j];
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
        return getSpatialCoord(getUCoord());
    }

    /**
     * Return the spatial vcoord.
     * vcoord/effWave
     *
     * @return the computed spatial coords r[x][y] (x,y for coordIndex,effWaveIndex) .
     */
    public double[][] getSpatialVCoord() {
        return getSpatialCoord(getVCoord());
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
        final int nWaves = getNWave();

        final boolean[][] flags = getFlag();
        final double[][] vis2Err = getVis2Err();

        boolean[] rowFlag;
        double[] rowVis2Err;

        for (int i = 0, j; i < nRows; i++) {
            rowFlag = flags[i];
            rowVis2Err = vis2Err[i];

            for (j = 0; j < nWaves; j++) {
                if (!rowFlag[j]) {
                    // Not flagged:
                    if (!isErrorValid(rowVis2Err[j])) {
                        checker.severe("Invalid value at index " + j + " for column '" + OIFitsConstants.COLUMN_VIS2ERR
                                + "' line " + i + ", found '" + rowVis2Err[j] + "' should be >= 0 or NaN or flagged out");
                    }
                }
            }
        }
    }
}
/*___oOo___*/
