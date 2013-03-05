/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.DataRange;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.meta.Units;
import fr.jmmc.oitools.meta.WaveColumnMeta;

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

        // Optional Complex visibilities (ASPRO or AMBER - not OIFits) :
        // VISDATA column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISDATA, "raw complex visibilities", Types.TYPE_COMPLEX, OIFitsConstants.COLUMN_VISERR, true, this));

        // VISERR  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISERR, "error in raw complex visibilities", Types.TYPE_COMPLEX, true, this));

        // VISAMP  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISAMP, "visibility amplitude", Types.TYPE_DBL, OIFitsConstants.COLUMN_VISAMPERR, RANGE_VIS, this));

        // VISAMPERR  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISAMPERR, "error in visibility amplitude", Types.TYPE_DBL, DataRange.RANGE_POSITIVE, this));

        // VISPHI  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISPHI, "visibility phase", Types.TYPE_DBL, Units.UNIT_DEGREE, OIFitsConstants.COLUMN_VISPHIERR, RANGE_ANGLE, this));

        // VISPHIERR  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISPHIERR, "error in visibility phase", Types.TYPE_DBL, Units.UNIT_DEGREE, DataRange.RANGE_POSITIVE, this));

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
}
/*___oOo___*/
