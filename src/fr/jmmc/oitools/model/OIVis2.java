/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.meta.ColumnMeta;
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
    addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VIS2DATA, "squared visibility", Types.TYPE_DBL, this));

    // VIS2ERR  column definition
    addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_VIS2ERR, "error in squared visibility", Types.TYPE_DBL, this));

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
