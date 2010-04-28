/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: OIT3.java,v 1.1 2010-04-28 14:47:38 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.9  2009/03/09 10:27:24  mella
 * Add spacialFreq and spacialCoord getter
 *
 * Revision 1.8  2008/10/28 08:21:43  mella
 * Add javadoc
 *
 * Revision 1.7  2008/04/08 14:22:16  mella
 * Include Evelyne comments
 *
 * Revision 1.6  2008/03/28 09:02:38  mella
 * Add AcceptedStaIndex for further checks
 *
 * Revision 1.5  2008/03/20 14:25:06  mella
 * First semantic step
 *
 * Revision 1.4  2008/03/18 13:23:04  mella
 * suppress common descs and inherit from oiData
 *
 * Revision 1.3  2008/03/13 07:25:48  mella
 * General commit after first keywords and columns definitions
 *
 * Revision 1.2  2008/03/11 14:48:52  mella
 * commit when evening is comming
 *
 * Revision 1.1  2008/02/28 08:10:40  mella
 * First revision
 *
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
public class OIT3 extends OIData {

  /* constants */

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

  /* members */

  /** 
   * OIT3 class constructor.
   *
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
   * Return the T3AMP column.
   *
   * @return the T3AMP column.
   */
  public double[][] getT3Amp() {
    return this.getColumnDoubles(OIFitsConstants.COLUMN_T3AMP);
  }

  /**
   * Return the T3AMPERR column.
   *
   * @return the T3AMPERR column.
   */
  public double[][] getT3AmpErr() {
    return this.getColumnDoubles(OIFitsConstants.COLUMN_T3AMPERR);
  }

  /**
   * Return the T3PHI column.
   *
   * @return the T3PHI column.
   */
  public double[][] getT3Phi() {
    return this.getColumnDoubles(OIFitsConstants.COLUMN_T3PHI);
  }

  /**
   * Return the T3PHIERR column.
   *
   * @return the T3PHIERR column.
   */
  public double[][] getT3PhiErr() {
    return this.getColumnDoubles(OIFitsConstants.COLUMN_T3PHIERR);
  }

  /**
   * Return the U1COORD column.
   *
   * @return the U1COORD column.
   */
  public double[] getU1Coord() {
    return this.getColumnDouble(OIFitsConstants.COLUMN_U1COORD);
  }

  /**
   * Return the V1COORD column.
   *
   * @return the V1COORD column.
   */
  public double[] getV1Coord() {
    return this.getColumnDouble(OIFitsConstants.COLUMN_V1COORD);
  }

  /**
   * Return the U2COORD column.
   *
   * @return the U2COORD column.
   */
  public double[] getU2Coord() {
    return this.getColumnDouble(OIFitsConstants.COLUMN_U2COORD);
  }

  /**
   * Return the V2COORD column.
   *
   * @return the V2COORD column.
   */
  public double[] getV2Coord() {
    return this.getColumnDouble(OIFitsConstants.COLUMN_V2COORD);
  }

  /**
   * Return the spacial frequencies column. The computation is based
   * on the maximum distance of u1,v1 u2,v2 and u1-u2,v1-v2 vectors.
   *
   * @return the computed spacial frequencies.
   */
  public double[][] getSpacial() {
    double[][] r = new double[getNbRows()][getNWave()];
    float[] effWaves = getOiWavelength().getEffWave();
    double[] u1coord = getU1Coord();
    double[] v1coord = getV1Coord();
    double[] u2coord = getU2Coord();
    double[] v2coord = getV2Coord();

    for (int i = 0; i < u1coord.length; i++) {
      for (int j = 0; j < effWaves.length; j++) {
        double effWave = effWaves[j];

        // mimic OIlib/yorick/oidata.i cridx3
        double u3 = -(u1coord[i] + u2coord[i]);
        double v3 = -(v1coord[i] + v2coord[i]);
        double dist1 = Math.sqrt((u1coord[i] * u1coord[i])
                + (v1coord[i] * v1coord[i]));
        double dist2 = Math.sqrt((u1coord[i] * u1coord[i])
                + (v1coord[i] * v1coord[i]));
        double dist3 = Math.sqrt((u3 * u3) + (v3 * v3));
        double dist = Math.max(Math.max(dist1, dist2), dist3);
        r[i][j] = dist / effWave;
      }
    }

    return r;
  }

  /**
   * Return the spacial u1coord.
   * u1coord/effWave
   *
   * @return the computed spacial coords r[x][y] (x,y for coordIndex,effWaveIndex) .
   */
  public double[][] getSpacialU1Coord() {
    double[][] r = new double[getNbRows()][getNWave()];
    float[] effWaves = getOiWavelength().getEffWave();
    double[] ucoord = getU1Coord();

    for (int i = 0; i < ucoord.length; i++) {
      for (int j = 0; j < effWaves.length; j++) {
        double effWave = effWaves[j];
        r[i][j] = ucoord[i] / effWave;
      }
    }
    return r;
  }

  /**
   * Return the spacial u2coord.
   * u2coord/effWave
   *
   * @return the computed spacial coords r[x][y] (x,y for coordIndex,effWaveIndex) .
   */
  public double[][] getSpacialU2Coord() {
    double[][] r = new double[getNbRows()][getNWave()];
    float[] effWaves = getOiWavelength().getEffWave();
    double[] ucoord = getU2Coord();

    for (int i = 0; i < ucoord.length; i++) {
      for (int j = 0; j < effWaves.length; j++) {
        double effWave = effWaves[j];
        r[i][j] = ucoord[i] / effWave;
      }
    }
    return r;
  }

  /**
   * Return the spacial v1coord.
   * v1coord/effWave
   *
   * @return the computed spacial coords r[x][y] (x,y for coordIndex,effWaveIndex) .
   */
  public double[][] getSpacialV1Coord() {
    double[][] r = new double[getNbRows()][getNWave()];
    float[] effWaves = getOiWavelength().getEffWave();
    double[] vcoord = getV1Coord();

    for (int i = 0; i < vcoord.length; i++) {
      for (int j = 0; j < effWaves.length; j++) {
        double effWave = effWaves[j];
        r[i][j] = vcoord[i] / effWave;
      }
    }
    return r;
  }

  /**
   * Return the spacial v2coord.
   * v2coord/effWave
   *
   * @return the computed spacial coords r[x][y] (x,y for coordIndex,effWaveIndex) .
   */
  public double[][] getSpacialV2Coord() {
    double[][] r = new double[getNbRows()][getNWave()];
    float[] effWaves = getOiWavelength().getEffWave();
    double[] vcoord = getV2Coord();

    for (int i = 0; i < vcoord.length; i++) {
      for (int j = 0; j < effWaves.length; j++) {
        double effWave = effWaves[j];
        r[i][j] = vcoord[i] / effWave;
      }
    }
    return r;
  }
}
/*___oOo___*/
