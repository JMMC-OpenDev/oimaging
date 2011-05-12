/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.meta.ColumnMeta;
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
    addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISDATA, "raw complex visibilities", Types.TYPE_COMPLEX, true, this));

    // VISERR  column definition
    addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISERR, "error in raw complex visibilities", Types.TYPE_COMPLEX, true, this));

    // VISAMP  column definition
    addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISAMP, "visibility amplitude", Types.TYPE_DBL, this));

    // VISAMPERR  column definition
    addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISAMPERR, "error in visibility amplitude", Types.TYPE_DBL, this));

    // VISPHI  column definition
    addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISPHI, "visibility phase", Types.TYPE_DBL, Units.UNIT_DEGREE, this));

    // VISPHIERR  column definition
    addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VISPHIERR, "error in visibility phase", Types.TYPE_DBL, Units.UNIT_DEGREE, this));

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
   * Return the spacial frequencies column.  The computation is based
   * on ucoord and vcoord.
   * sqrt(ucoord^2+vcoord^2)/effWave
   *
   * @return the computed spacial frequencies r[x][y] (x,y for coordIndex,effWaveIndex)
   */
  public double[][] getSpacialFreq() {
    final double[][] r = new double[getNbRows()][getNWave()];
    final float[] effWaves = getOiWavelength().getEffWave();
    final double[] ucoord = getUCoord();
    final double[] vcoord = getVCoord();

    for (int i = 0, sizeU = ucoord.length; i < sizeU; i++) {
      for (int j = 0, sizeW = effWaves.length; j < sizeW; j++) {
        r[i][j] = (Math.sqrt((ucoord[i] * ucoord[i]) + (vcoord[i] * vcoord[i]))) / effWaves[j];
      }
    }

    return r;
  }

  /**
   * Return the spacial ucoord.
   * ucoord/effWave
   *
   * @return the computed spacial coords r[x][y] (x,y for coordIndex,effWaveIndex) .
   */
  public double[][] getSpacialUCoord() {
    return getSpacialCoord(getUCoord());
  }

  /**
   * Return the spacial vcoord.
   * vcoord/effWave
   *
   * @return the computed spacial coords r[x][y] (x,y for coordIndex,effWaveIndex) .
   */
  public double[][] getSpacialVCoord() {
    return getSpacialCoord(getVCoord());
  }
}
/*___oOo___*/
