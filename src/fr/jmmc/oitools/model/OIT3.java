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
 * Class for OI_T3 table.
 */
public final class OIT3 extends OIData {

  /* static descriptors */
  /** U1COORD column descriptor */
  private final static ColumnMeta COLUMN_U1COORD = new ColumnMeta(OIFitsConstants.COLUMN_U1COORD,
          "U coordinate of baseline AB of the triangle", Types.TYPE_DBL, Units.UNIT_METER);
  /** V1COORD column descriptor */
  private final static ColumnMeta COLUMN_V1COORD = new ColumnMeta(OIFitsConstants.COLUMN_V1COORD,
          "V coordinate of baseline AB of the triangle", Types.TYPE_DBL, Units.UNIT_METER);
  /** U2COORD column descriptor */
  private final static ColumnMeta COLUMN_U2COORD = new ColumnMeta(OIFitsConstants.COLUMN_U2COORD,
          "U coordinate of baseline BC of the triangle", Types.TYPE_DBL, Units.UNIT_METER);
  /** V2COORD column descriptor */
  private final static ColumnMeta COLUMN_V2COORD = new ColumnMeta(OIFitsConstants.COLUMN_V2COORD,
          "V coordinate of baseline BC of the triangle", Types.TYPE_DBL, Units.UNIT_METER);

  /** 
   * Public OIT3 class constructor
   * @param oifitsFile main OifitsFile
   */
  public OIT3(final OIFitsFile oifitsFile) {
    super(oifitsFile);

    // T3AMP  column definition
    addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_T3AMP, "triple product amplitude", Types.TYPE_DBL, this));

    // T3AMPERR  column definition
    addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_T3AMPERR, "error in triple product amplitude", Types.TYPE_DBL, this));

    // T3PHI  column definition
    addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_T3PHI, "triple product phase", Types.TYPE_DBL, Units.UNIT_DEGREE, this));

    // T3PHIERR  column definition
    addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_T3PHIERR, "error in triple product phase", Types.TYPE_DBL, Units.UNIT_DEGREE, this));

    // U1COORD  column definition
    addColumnMeta(COLUMN_U1COORD);

    // V1COORD  column definition
    addColumnMeta(COLUMN_V1COORD);

    // U2COORD  column definition
    addColumnMeta(COLUMN_U2COORD);

    // V2COORD  column definition
    addColumnMeta(COLUMN_V2COORD);

    // STA_INDEX  column definition
    addColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_STA_INDEX, "station numbers contributing to the data", Types.TYPE_INT, 3) {

      @Override
      public short[] getIntAcceptedValues() {
        return getAcceptedStaIndexes();
      }
    });

    // FLAG  column definition
    addColumnMeta(new WaveColumnMeta(OIFitsConstants.COLUMN_FLAG, "flag", Types.TYPE_LOGICAL, this));
  }

  /**
   * Public OIT3 class constructor to create a new table
   * @param oifitsFile main OifitsFile
   * @param insName value of INSNAME keyword
   * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
   */
  public OIT3(final OIFitsFile oifitsFile, final String insName, final int nbRows) {
    this(oifitsFile);

    setInsName(insName);

    this.initializeTable(nbRows);
  }

  /* --- Columns --- */
  /**
   * Return the T3AMP column.
   * @return the T3AMP column.
   */
  public double[][] getT3Amp() {
    return this.getColumnDoubles(OIFitsConstants.COLUMN_T3AMP);
  }

  /**
   * Return the T3AMPERR column.
   * @return the T3AMPERR column.
   */
  public double[][] getT3AmpErr() {
    return this.getColumnDoubles(OIFitsConstants.COLUMN_T3AMPERR);
  }

  /**
   * Return the T3PHI column.
   * @return the T3PHI column.
   */
  public double[][] getT3Phi() {
    return this.getColumnDoubles(OIFitsConstants.COLUMN_T3PHI);
  }

  /**
   * Return the T3PHIERR column.
   * @return the T3PHIERR column.
   */
  public double[][] getT3PhiErr() {
    return this.getColumnDoubles(OIFitsConstants.COLUMN_T3PHIERR);
  }

  /**
   * Return the U1COORD column.
   * @return the U1COORD column.
   */
  public double[] getU1Coord() {
    return this.getColumnDouble(OIFitsConstants.COLUMN_U1COORD);
  }

  /**
   * Return the V1COORD column.
   * @return the V1COORD column.
   */
  public double[] getV1Coord() {
    return this.getColumnDouble(OIFitsConstants.COLUMN_V1COORD);
  }

  /**
   * Return the U2COORD column.
   * @return the U2COORD column.
   */
  public double[] getU2Coord() {
    return this.getColumnDouble(OIFitsConstants.COLUMN_U2COORD);
  }

  /**
   * Return the V2COORD column.
   * @return the V2COORD column.
   */
  public double[] getV2Coord() {
    return this.getColumnDouble(OIFitsConstants.COLUMN_V2COORD);
  }

  /* --- Alternate data representation methods --- */
  /**
   * Return the spatial frequencies column. The computation is based
   * on the maximum distance of u1,v1 (AB), u2,v2 (BC) and -(u1+u2), - (v1+v2) (CA) vectors.
   *
   * @return the computed spatial frequencies.
   */
  public double[][] getSpatial() {
    final int nRows  = getNbRows();
    final int nWaves = getNWave();
    
    final double[][] r = new double[nRows][nWaves];
    final float[] effWaves = getOiWavelength().getEffWave();
    final double[] u1coord = getU1Coord();
    final double[] v1coord = getV1Coord();
    final double[] u2coord = getU2Coord();
    final double[] v2coord = getV2Coord();

    for (int i = 0, j = 0; i < nRows; i++) {
      for (j = 0; j < nWaves; j++) {

        // mimic OIlib/yorick/oidata.i cridx3
        final double dist1 = Math.sqrt((u1coord[i] * u1coord[i]) + (v1coord[i] * v1coord[i]));
        final double dist2 = Math.sqrt((u2coord[i] * u2coord[i]) + (v2coord[i] * v2coord[i]));

        final double u3 = u1coord[i] + u2coord[i];
        final double v3 = v1coord[i] + v2coord[i];
        final double dist3 = Math.sqrt((u3 * u3) + (v3 * v3));

        final double dist = Math.max(Math.max(dist1, dist2), dist3);
        r[i][j] = dist / effWaves[j];
      }
    }

    return r;
  }

  /**
   * Return the spatial u1coord.
   * u1coord/effWave
   *
   * @return the computed spatial coords r[x][y] (x,y for coordIndex,effWaveIndex) .
   */
  public double[][] getSpatialU1Coord() {
    return getSpatialCoord(getU1Coord());
  }

  /**
   * Return the spatial u2coord.
   * u2coord/effWave
   *
   * @return the computed spatial coords r[x][y] (x,y for coordIndex,effWaveIndex) .
   */
  public double[][] getSpatialU2Coord() {
    return getSpatialCoord(getU2Coord());
  }

  /**
   * Return the spatial v1coord.
   * v1coord/effWave
   *
   * @return the computed spatial coords r[x][y] (x,y for coordIndex,effWaveIndex) .
   */
  public double[][] getSpatialV1Coord() {
    return getSpatialCoord(getV1Coord());
  }

  /**
   * Return the spatial v2coord.
   * v2coord/effWave
   *
   * @return the computed spatial coords r[x][y] (x,y for coordIndex,effWaveIndex) .
   */
  public double[][] getSpatialV2Coord() {
    return getSpatialCoord(getV2Coord());
  }
}
/*___oOo___*/
