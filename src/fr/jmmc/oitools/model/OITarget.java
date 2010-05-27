/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: OITarget.java,v 1.3 2010-05-27 16:13:29 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2010/04/29 15:47:02  bourgesl
 * use OIFitsChecker instead of CheckLogger / Handler to make OIFits validation
 *
 * Revision 1.1  2010/04/28 14:47:38  bourgesl
 * refactored OIValidator classes to represent the OIFits data model
 *
 * Revision 1.13  2009/09/08 16:10:42  mella
 * add same block for all oitable after optionnal specific data
 *
 * Revision 1.12  2009/01/20 06:24:29  mella
 * Add getter for most element of the table
 *
 * Revision 1.11  2009/01/19 20:23:07  mella
 * remove duplicated column definitions
 *
 * Revision 1.10  2008/10/28 08:34:34  mella
 * Add javadoc
 *
 * Revision 1.9  2008/04/08 14:22:16  mella
 * Include Evelyne comments
 *
 * Revision 1.8  2008/04/01 11:16:25  mella
 * Add getter method to access number of oitargets
 *
 * Revision 1.7  2008/03/28 09:04:33  mella
 * better array handling during construction
 *
 * Revision 1.6  2008/03/20 14:25:06  mella
 * First semantic step
 *
 * Revision 1.5  2008/03/19 09:05:10  mella
 * fix right choice for VELTYPE and VELDEF cols
 *
 * Revision 1.4  2008/03/19 09:02:13  mella
 * fix right choice for FRAME keyword
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

/**
 * Class for OI_TARGET table.
 */
public class OITarget extends OITable {

  /* constants */

  /* static descriptors */
  /** TARGET_ID column descriptor */
  private final static ColumnMeta COLUMN_TARGET_ID = new ColumnMeta(OIFitsConstants.COLUMN_TARGET_ID,
          "index number", Types.TYPE_INT);
  /** TARGET column descriptor */
  private final static ColumnMeta COLUMN_TARGET = new ColumnMeta(OIFitsConstants.COLUMN_TARGET,
          "target name", Types.TYPE_CHAR, 16);
  /** RAEP0 column descriptor */
  private final static ColumnMeta COLUMN_RAEP0 = new ColumnMeta(OIFitsConstants.COLUMN_RAEP0,
          "RA at mean equinox", Types.TYPE_DBL, Units.UNIT_DEGREE);
  /** DECEP0 column descriptor */
  private final static ColumnMeta COLUMN_DECEP0 = new ColumnMeta(OIFitsConstants.COLUMN_DECEP0,
          "DEC at mean equinox", Types.TYPE_DBL, Units.UNIT_DEGREE);
  /** EQUINOX column descriptor */
  private final static ColumnMeta COLUMN_EQUINOX = new ColumnMeta(OIFitsConstants.COLUMN_EQUINOX,
          "equinox", Types.TYPE_REAL, Units.UNIT_YEAR);
  /** RA_ERR column descriptor */
  private final static ColumnMeta COLUMN_RA_ERR = new ColumnMeta(OIFitsConstants.COLUMN_RA_ERR,
          "error in RA at mean equinox", Types.TYPE_DBL, Units.UNIT_DEGREE);
  /** DEC_ERR column descriptor */
  private final static ColumnMeta COLUMN_DEC_ERR = new ColumnMeta(OIFitsConstants.COLUMN_DEC_ERR,
          "error in DEC at mean equinox", Types.TYPE_DBL, Units.UNIT_DEGREE);
  /** SYSVEL column descriptor */
  private final static ColumnMeta COLUMN_SYSVEL = new ColumnMeta(OIFitsConstants.COLUMN_SYSVEL,
          "systemic radial velocity", Types.TYPE_DBL, Units.UNIT_METER_PER_SECOND);
  /** VELTYP column descriptor */
  private final static ColumnMeta COLUMN_VELTYP = new ColumnMeta(OIFitsConstants.COLUMN_VELTYP,
          "reference for radial velocity", Types.TYPE_CHAR, 8,
          new String[]{OIFitsConstants.COLUMN_VELTYP_LSR, OIFitsConstants.COLUMN_VELTYP_HELIOCEN,
                       OIFitsConstants.COLUMN_VELTYP_BARYCENT, OIFitsConstants.COLUMN_VELTYP_GEOCENTR,
                       OIFitsConstants.COLUMN_VELTYP_TOPOCENT, OIFitsConstants.UNKNOWN_VALUE});
  // Note : UNKNOWN is not in OIFits standard
  /** VELDEF column descriptor */
  private final static ColumnMeta COLUMN_VELDEF = new ColumnMeta(OIFitsConstants.COLUMN_VELDEF,
          "definition of radial velocity", Types.TYPE_CHAR, 8,
          new String[]{OIFitsConstants.COLUMN_VELDEF_RADIO, OIFitsConstants.COLUMN_VELDEF_OPTICAL});
  /** PMRA column descriptor */
  private final static ColumnMeta COLUMN_PMRA = new ColumnMeta(OIFitsConstants.COLUMN_PMRA,
          "proper motion in RA", Types.TYPE_DBL, Units.UNIT_DEGREE_PER_YEAR);
  /** PMDEC column descriptor */
  private final static ColumnMeta COLUMN_PMDEC = new ColumnMeta(OIFitsConstants.COLUMN_PMDEC,
          "proper motion in DEC", Types.TYPE_DBL, Units.UNIT_DEGREE_PER_YEAR);
  /** PMRA_ERR column descriptor */
  private final static ColumnMeta COLUMN_PMRA_ERR = new ColumnMeta(OIFitsConstants.COLUMN_PMRA_ERR,
          "error of proper motion in RA", Types.TYPE_DBL, Units.UNIT_DEGREE_PER_YEAR);
  /** PMDEC_ERR column descriptor */
  private final static ColumnMeta COLUMN_PMDEC_ERR = new ColumnMeta(OIFitsConstants.COLUMN_PMDEC_ERR,
          "error of proper motion in DEC", Types.TYPE_DBL, Units.UNIT_DEGREE_PER_YEAR);
  /** PARALLAX column descriptor */
  private final static ColumnMeta COLUMN_PARALLAX = new ColumnMeta(OIFitsConstants.COLUMN_PARALLAX,
          "parallax", Types.TYPE_REAL, Units.UNIT_DEGREE);
  /** PARA_ERR column descriptor */
  private final static ColumnMeta COLUMN_PARA_ERR = new ColumnMeta(OIFitsConstants.COLUMN_PARA_ERR,
          "error in parallax", Types.TYPE_REAL, Units.UNIT_DEGREE);
  /** SPECTYP column descriptor */
  private final static ColumnMeta COLUMN_SPECTYP = new ColumnMeta(OIFitsConstants.COLUMN_SPECTYP,
          "spectral type", Types.TYPE_CHAR, 16);

  /**
   * Public OITarget class constructor.
   * @param oifitsFile main OifitsFile
   */
  public OITarget(final OIFitsFile oifitsFile) {
    super(oifitsFile);

    // TARGET_ID  column definition
    addColumnMeta(COLUMN_TARGET_ID);

    // TARGET  column definition
    addColumnMeta(COLUMN_TARGET);

    // RAEP0  column definition
    addColumnMeta(COLUMN_RAEP0);

    // DECEP0  column definition
    addColumnMeta(COLUMN_DECEP0);

    // EQUINOX  column definition
    addColumnMeta(COLUMN_EQUINOX);

    // RA_ERR  column definition
    addColumnMeta(COLUMN_RA_ERR);

    // DEC_ERR  column definition
    addColumnMeta(COLUMN_DEC_ERR);

    // SYSVEL  column definition
    addColumnMeta(COLUMN_SYSVEL);

    // VELTYP  column definition
    addColumnMeta(COLUMN_VELTYP);

    // VELDEF  column definition
    addColumnMeta(COLUMN_VELDEF);

    // PMRA  column definition
    addColumnMeta(COLUMN_PMRA);

    // PMDEC  column definition
    addColumnMeta(COLUMN_PMDEC);

    // PMRA_ERR  column definition
    addColumnMeta(COLUMN_PMRA_ERR);

    // PMDEC_ERR  column definition
    addColumnMeta(COLUMN_PMDEC_ERR);

    // PARALLAX  column definition
    addColumnMeta(COLUMN_PARALLAX);

    // PARA_ERR  column definition
    addColumnMeta(COLUMN_PARA_ERR);

    // SPECTYP  column definition
    addColumnMeta(COLUMN_SPECTYP);
  }

  /** 
   * Get number of target identified in this table.
   * @return number of target identified in this table.
   */
  public final int getNbTargets() {
    return getNbRows();
  }

  /* --- Columns --- */
  /**
   * Get TARGET_ID column
   * @return the target identifiers
   */
  public final short[] getTargetId() {
    final short[] data = getColumnShort(OIFitsConstants.COLUMN_TARGET_ID);

    if (data == null) {
      return EMPTY_SHORT_ARRAY;
    }

    return data;
  }

  /**
   * Get TARGET column.
   * @return the array of target names.
   */
  public final String[] getTarget() {
    return getColumnString(OIFitsConstants.COLUMN_TARGET);
  }

  /**
   * Get RAEP0 column.
   * @return the array of RAEP0.
   */
  public final double[] getRaEp0() {
    return getColumnDouble(OIFitsConstants.COLUMN_RAEP0);
  }

  /**
   * Get DECEP0 column.
   * @return the array of DECEP0.
   */
  public final double[] getDecEp0() {
    return getColumnDouble(OIFitsConstants.COLUMN_DECEP0);
  }

  /**
   * Get EQUINOX column.
   * @return the array of EQUINOX.
   */
  public final float[] getEquinox() {
    return getColumnFloat(OIFitsConstants.COLUMN_EQUINOX);
  }

  /**
   * Get RA_ERR column.
   * @return the array of RA_ERR.
   */
  public final double[] getRaErr() {
    return getColumnDouble(OIFitsConstants.COLUMN_RA_ERR);
  }

  /**
   * Get DEC_ERR column.
   * @return the array of DEC_ERR.
   */
  public final double[] getDecErr() {
    return getColumnDouble(OIFitsConstants.COLUMN_DEC_ERR);
  }

  /**
   * Get SYSVEL column.
   * @return the array of SYSVEL.
   */
  public final double[] getSysVel() {
    return getColumnDouble(OIFitsConstants.COLUMN_SYSVEL);
  }

  /**
   * Get VELTYP column.
   * @return the array of VELTYP.
   */
  public final String[] getVelTyp() {
    return getColumnString(OIFitsConstants.COLUMN_VELTYP);
  }

  /**
   * Get VELDEF column.
   * @return the array of VELDEF.
   */
  public final String[] getVelDef() {
    return getColumnString(OIFitsConstants.COLUMN_VELDEF);
  }

  /**
   * Get PMRA column.
   * @return the array of PMRA.
   */
  public final double[] getPmRa() {
    return getColumnDouble(OIFitsConstants.COLUMN_PMRA);
  }

  /**
   * Get PMDEC column.
   * @return the array of PMDEC.
   */
  public final double[] getPmDec() {
    return getColumnDouble(OIFitsConstants.COLUMN_PMDEC);
  }

  /**
   * Get PMRA_ERR column.
   * @return the array of PMRA_ERR.
   */
  public final double[] getPmRaErr() {
    return getColumnDouble(OIFitsConstants.COLUMN_PMRA_ERR);
  }

  /**
   * Get PMDEC_ERR column.
   * @return the array of PMDEC_ERR.
   */
  public final double[] getPmDecErr() {
    return getColumnDouble(OIFitsConstants.COLUMN_PMDEC_ERR);
  }

  /**
   * Get PARALLAX column.
   * @return the array of PARALLAX.
   */
  public final float[] getParallax() {
    return getColumnFloat(OIFitsConstants.COLUMN_PARALLAX);
  }

  /**
   * Get PARA_ERR column.
   * @return the array of PARA_ERR.
   */
  public final float[] getParaErr() {
    return getColumnFloat(OIFitsConstants.COLUMN_PARA_ERR);
  }

  /**
   * Get SPECTYP column.
   * @return the array of SPECTYP.
   */
  public final String[] getSpecTyp() {
    return getColumnString(OIFitsConstants.COLUMN_SPECTYP);
  }

  /* --- Other methods --- */
  /**
   * Do syntactical analysis.
   * @param checker checker component
   */
  @Override
  public void checkSyntax(final OIFitsChecker checker) {
    super.checkSyntax(checker);

    final int len = getNbTargets();
    final short[] targetIds = getTargetId();
    final String[] targetNames = getTarget();

    for (int i = 0; i < len; i++) {
      for (int j = i + 1; j < len; j++) {
        if (targetIds[i] == targetIds[j]) {
          checker.severe("TARGET_ID duplicated on lines " + i + "|" + j);
        }

        if (targetNames[i].equals(targetNames[j])) {
          checker.severe("TARGET duplicated on lines " + i + "|" + j);
        }
      }
    }

    getOIFitsFile().checkCrossRefering(this, checker);
  }

  /**
   * Fill the given buffer with the xml serialisation of the table.
   * @param sb string buffer
   * @param detailled if true the result will contain the table content
   */
  @Override
  public void getXmlDesc(final StringBuilder sb, final boolean detailled) {

    sb.append("<targets>");

    for (int i = 0, len = getNbTargets(); i < len; i++) {
      sb.append("<target>");
      sb.append("<name>").append(getTarget()[i]).append("</name>");
      sb.append("<id>").append(getTargetId()[i]).append("</id>");
      sb.append("<raEp0>").append(getRaEp0()[i]).append("</raEp0>");
      sb.append("<decEp0>").append(getDecEp0()[i]).append("</decEp0>");
      sb.append("<equinox>").append(getEquinox()[i]).append("</equinox>");
      sb.append("<raErr>").append(getRaErr()[i]).append("</raErr>");
      sb.append("<decErr>").append(getDecErr()[i]).append("</decErr>");
      sb.append("<sysVel>").append(getSysVel()[i]).append("</sysVel>");
      sb.append("<velTyp>").append(getVelTyp()[i]).append("</velTyp>");
      sb.append("<velDef>").append(getVelDef()[i]).append("</velDef>");
      sb.append("<pmRa>").append(getPmRa()[i]).append("</pmRa>");
      sb.append("<pmDec>").append(getPmDec()[i]).append("</pmDec>");
      sb.append("<pmRaErr>").append(getPmRaErr()[i]).append("</pmRaErr>");
      sb.append("<pmDecErr>").append(getPmDecErr()[i]).append("</pmDecErr>");
      sb.append("<parallax>").append(getParallax()[i]).append("</parallax>");
      sb.append("<paraErr>").append(getParaErr()[i]).append("</paraErr>");
      sb.append("<specTyp>").append(getSpecTyp()[i]).append("</specTyp>");
      sb.append("</target>");
    }

    sb.append("</targets>\n\n");

    super.getXmlDesc(sb, detailled);
  }

  /**
   * Returns a string representation of this table
   * @return a string representation of this table
   */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(128);

    final short[] targetIds = getTargetId();

    for (int i = 0, len = getNbTargets(); i < len; i++) {
      sb.append("| ").append(getTarget()[i]).append("(").append(targetIds[i]).append(") ");
    }

    return super.toString() + " [ TARGETS[" + sb.toString().substring(1) + "]]";
  }
}
/*___oOo___*/
