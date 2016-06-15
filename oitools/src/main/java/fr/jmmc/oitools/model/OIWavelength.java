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

/**
 * Class for OI_WAVELENGTH table.
 */
public final class OIWavelength extends OITable {

    /* constants */
    /* static descriptors */
    /** INSNAME keyword descriptor */
    private final static KeywordMeta KEYWORD_INSNAME = new KeywordMeta(OIFitsConstants.KEYWORD_INSNAME,
            "name of detector for cross-referencing", Types.TYPE_CHAR);
    /** EFF_WAVE column descriptor */
    private final static ColumnMeta COLUMN_EFF_WAVE = new ColumnMeta(OIFitsConstants.COLUMN_EFF_WAVE,
            "effective wavelength of channel", Types.TYPE_REAL, Units.UNIT_METER, DataRange.RANGE_POSITIVE_STRICT);
    /** EFF_BAND column descriptor */
    private final static ColumnMeta COLUMN_EFF_BAND = new ColumnMeta(OIFitsConstants.COLUMN_EFF_BAND,
            "effective bandpass of channel", Types.TYPE_REAL, Units.UNIT_METER, DataRange.RANGE_POSITIVE_STRICT);
    /* members */
    /* cached analyzed data */
    private InstrumentMode insMode = null;
    
    /** 
     * Public OIWavelength class constructor
     * @param oifitsFile main OifitsFile
     */
    public OIWavelength(final OIFitsFile oifitsFile) {
        super(oifitsFile);

        // INSNAME  keyword definition
        addKeywordMeta(KEYWORD_INSNAME);

        // EFF_WAVE  column definition
        addColumnMeta(COLUMN_EFF_WAVE);

        // EFF_BAND  column definition
        addColumnMeta(COLUMN_EFF_BAND);
    }

    /**
     * Public OIWavelength class constructor to create a new table
     * @param oifitsFile main OifitsFile
     * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
     */
    public OIWavelength(final OIFitsFile oifitsFile, final int nbRows) {
        this(oifitsFile);

        this.initializeTable(nbRows);
    }

    /**
     * Get number of wavelengths
     * @return the number of wavelengths.
     */
    public int getNWave() {
        return getNbRows();
    }

    /* --- Keywords --- */
    /**
     * Get the INSNAME keyword value.
     * @return the value of INSNAME keyword
     */
    public String getInsName() {
        return getKeyword(OIFitsConstants.KEYWORD_INSNAME);
    }

    /**
     * Define the INSNAME keyword value
     * @param insName value of INSNAME keyword
     */
    public void setInsName(final String insName) {
        setKeyword(OIFitsConstants.KEYWORD_INSNAME, insName);
    }

    /* --- Columns --- */
    /**
     * Return the effective wavelength of channel
     * @return the wavelength of channel array
     */
    public float[] getEffWave() {
        return this.getColumnFloat(OIFitsConstants.COLUMN_EFF_WAVE);
    }

    /**
     * Return the effective bandpass of channel
     * @return the bandpass of channel array
     */
    public float[] getEffBand() {
        return this.getColumnFloat(OIFitsConstants.COLUMN_EFF_BAND);
    }

    /* --- Derived Column --- */
    /**
     * Return the resolution (mean) from all couples lambda / delta_lambda ie (wavelength / bandpass)
     * @return resolution
     */
    public Float getResolution() {
        /* retrieve value in columnsRangeValue map of associated column */
        Float value = (Float) getColumnsRangeValue().get(OIFitsConstants.VALUE_RESOLUTION);

        /* compute resolution value if not previously set */
        if (value == null) {
            final int nWaves = getNWave();

            final float[] effWaves = getEffWave();
            final float[] effBands = getEffBand();

            int n = 0;
            double total = 0d;

            for (int i = 0; i < nWaves; i++) {
                double res_ch = effWaves[i] / effBands[i];
                if (NumberUtils.isFinite(res_ch)) {
                    total += res_ch;
                    n++;
                }
            }

            value = (n != 0) ? Float.valueOf((float) (total / n)) : Float.NaN;

            /* store value in associated column range value */
            getColumnsRangeValue().put(OIFitsConstants.VALUE_RESOLUTION, value);
        }

        return value;
    }

    /**
     * Return the effective wavelength of channel as double array
     * @return the wavelength of channel array as double array
     */
    public double[] getEffWaveAsDouble() {
        // lazy:
        double[] effWaveDbls = this.getColumnDerivedDouble(OIFitsConstants.COLUMN_EFF_WAVE);

        if (effWaveDbls == null) {
            final int nWaves = getNWave();
            effWaveDbls = new double[nWaves];

            final float[] effWaves = getEffWave();

            for (int j = 0; j < nWaves; j++) {
                effWaveDbls[j] = effWaves[j];
            }

            this.setColumnDerivedValue(OIFitsConstants.COLUMN_EFF_WAVE, effWaveDbls);
        }

        return effWaveDbls;
    }

    /**
     * Return the wavelength range
     * @return the wavelength range
     */
    public float[] getEffWaveRange() {
        return (float[]) getMinMaxColumnValue(OIFitsConstants.COLUMN_EFF_WAVE);
    }

    /**
     * Return the minimum wavelength
     * @return the minimum wavelength
     */
    public float getEffWaveMin() {
        return getEffWaveRange()[0];
    }

    /**
     * Return the maximum wavelength
     * @return the maximum wavelength
     */
    public float getEffWaveMax() {
        return getEffWaveRange()[1];
    }

    /* --- Other methods --- */
    /**
     * Returns a string representation of this table
     * @return a string representation of this table
     */
    @Override
    public String toString() {
        return super.toString() + " [ INSNAME=" + getInsName() + " | NWAVE=" + getNWave() + " ]";
    }

    /** 
     * Do syntactical analysis.
     * @param checker checker component
     */
    @Override
    public void checkSyntax(final OIFitsChecker checker) {
        super.checkSyntax(checker);

        if (getInsName() != null && getInsName().length() == 0) {
            /* Problem: INSNAME keyword has value "", that should not be
             * possible. */
            checker.severe("INSNAME identifier has blank value");
        }

        // TODO: check value range: positive and 0.3 < value < 20 µm !!
        getOIFitsFile().checkCrossReference(this, checker);
    }


    /* --- data analysis --- */
    /**
     * Indicate to clear any cached value (derived column ...)
     */
    @Override
    public void setChanged() {
        super.setChanged();
        setInstrumentMode(null);
    }

    public InstrumentMode getInstrumentMode() {
        return insMode;
    }

    protected void setInstrumentMode(final InstrumentMode insMode) {
        this.insMode = insMode;
    }
}
/*___oOo___*/
