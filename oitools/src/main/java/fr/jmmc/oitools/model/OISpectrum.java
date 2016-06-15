/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.meta.ArrayColumnMeta;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.DataRange;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.meta.Units;
import fr.jmmc.oitools.meta.WaveColumnMeta;

/**
 * Class for OI_Spectrum / OI_Flux table.
 */
public final class OISpectrum extends OIData {

    /** 
     * Public OISpectrum class constructor
     * @param oifitsFile main OifitsFile
     */
    public OISpectrum(final OIFitsFile oifitsFile) {
        // do not use common columns:
        super(oifitsFile, false);

        // OI_SPECTRUM.FLUXDATA column definition
        ColumnMeta colMeta = new WaveColumnMeta(OIFitsConstants.COLUMN_FLUXDATA, "flux per telescope", Types.TYPE_DBL, Units.NO_UNIT, OIFitsConstants.COLUMN_FLUXERR, DataRange.RANGE_POSITIVE, this);
        // OI_FLUX.FLUX column definition (optional)
        colMeta.setAlias(OIFitsConstants.COLUMN_FLUX);
        addColumnMeta(colMeta);

        // FLUXERR  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_FLUXERR, "error in flux", Types.TYPE_DBL, DataRange.RANGE_POSITIVE, this));

        // STA_INDEX  column definition
        addColumnMeta(new ArrayColumnMeta(OIFitsConstants.COLUMN_STA_INDEX, "station number contributing to the data", Types.TYPE_INT, 1) {
            @Override
            public short[] getIntAcceptedValues() {
                return getAcceptedStaIndexes();
            }
        });

        // FLAG  column definition
        addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_FLAG, "flag", Types.TYPE_LOGICAL, this));
    }

    /**
     * Public OISpectrum class constructor to create a new table
     * @param oifitsFile main OifitsFile
     * @param insName value of INSNAME keyword
     * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
     */
    public OISpectrum(final OIFitsFile oifitsFile, final String insName, final int nbRows) {
        this(oifitsFile);

        setInsName(insName);

        this.initializeTable(nbRows);
    }


    /* --- Columns --- */
    /**
     * Return the FLUXDATA column.
     * @return the FLUXDATA column.
     */
    public double[][] getFluxData() {
        double[][] values = this.getColumnDoubles(OIFitsConstants.COLUMN_FLUXDATA);
        if (values == null) {
            // GRAVITY OI_FLUX:
            values = this.getColumnDoubles(OIFitsConstants.COLUMN_FLUX);
        }
        return values;
    }

    /**
     * Return the FLUXERR column.
     * @return the FLUXERR column.
     */
    public double[][] getFluxErr() {
        return this.getColumnDoubles(OIFitsConstants.COLUMN_FLUXERR);
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
        return null; // undefined
    }

    /**
     * Return the radius column i.e. projected base line (m).
     *
     * @return the computed radius r[x] (x for coordIndex)
     */
    public double[] getRadius() {
        return null; // undefined
    }

    /**
     * Return the position angle column i.e. position angle of the projected base line (deg).
     *
     * @return the computed position angle r[x] (x for coordIndex)
     */
    public double[] getPosAngle() {
        return null; // undefined
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
        return super.getDerivedColumnAsDoubles(name);
    }

    /* --- Other methods --- */
    /** 
     * Do syntactical analysis.
     * @param checker checker component
     */
    @Override
    public void checkSyntax(final OIFitsChecker checker) {
        // TODO remove next commented line when V2.0 will be supported
        //super.checkSyntax(checker);

        // TODO: specific rules
    }
}
/*___oOo___*/
