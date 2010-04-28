/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: OITarget.java,v 1.1 2010-04-28 14:47:38 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
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
import java.util.logging.Logger;

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

  /* members */
  /**
   * OITarget class constructor.
   *
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

  /**
   * Get target identifiers defined in this table.
   *
   * @return an integer array containing all station indexes.
   */
  public final short[] getAcceptedTargetIds() {
    final short[] data = getColumnShort(OIFitsConstants.COLUMN_TARGET_ID);

    if (data == null) {
      return EMPTY_SHORT_ARRAY;
    }

    return data;
  }

  /**
   * Get target name defined in this table according given target id.
   *
   * @param targetId target id
   * @return the target name associated to the given targetId.
   */
  public final String getTargetName(final int targetId) {
    return getTargetNames()[targetId];
  }

  /**
   * Get target names.
   *
   * @return the array of target names.
   */
  public final String[] getTargetNames() {
    return getColumnString(OIFitsConstants.COLUMN_TARGET);
  }

  /**
   * Get RAEP0 defined in this table according given target id.
   *
   * @param targetId target id
   * @return the RAEP0 associated to the given targetId.
   */
  public final double getRaEp0(final int targetId) {
    return getRaEp0s()[targetId];
  }

  /**
   * Get RAEP0s.
   *
   * @return the array of RAEP0.
   */
  public final double[] getRaEp0s() {
    return getColumnDouble(OIFitsConstants.COLUMN_RAEP0);
  }

  /**
   * Get DECEP0 defined in this table according given target id.
   *
   * @param targetId target id
   * @return the DECEP0 associated to the given targetId.
   */
  public final double getDecEp0(final int targetId) {
    return getDecEp0s()[targetId];
  }

  /**
   * Get DECEP0s.
   *
   * @return the array of DECEP0.
   */
  public final double[] getDecEp0s() {
    return getColumnDouble(OIFitsConstants.COLUMN_DECEP0);
  }

  /**
   * Get EQUINOX defined in this table according given target id.
   *
   * @param targetId target id
   * @return the EQUINOX associated to the given targetId.
   */
  public final float getEquinox(final int targetId) {
    return getEquinoxs()[targetId];
  }

  /**
   * Get EQUINOXs.
   *
   * @return the array of EQUINOX.
   */
  public final float[] getEquinoxs() {
    return getColumnFloat(OIFitsConstants.COLUMN_EQUINOX);
  }

  /**
   * Get RA_ERR defined in this table according given target id.
   *
   * @param targetId target id
   * @return the RA_ERR associated to the given targetId.
   */
  public final double getRaErr(final int targetId) {
    return getRaErrs()[targetId];
  }

  /**
   * Get RA_ERRs.
   *
   * @return the array of RA_ERR.
   */
  public final double[] getRaErrs() {
    return getColumnDouble(OIFitsConstants.COLUMN_RA_ERR);
  }

  /**
   * Get DEC_ERR defined in this table according given target id.
   *
   * @param targetId target id
   * @return the DEC_ERR associated to the given targetId.
   */
  public final double getDecErr(final int targetId) {
    return getDecErrs()[targetId];
  }

  /**
   * Get DEC_ERRs.
   *
   * @return the array of DEC_ERR.
   */
  public final double[] getDecErrs() {
    return getColumnDouble(OIFitsConstants.COLUMN_DEC_ERR);
  }

  /**
   * Get SYSVEL defined in this table according given target id.
   *
   * @param targetId target id
   * @return the SYSVEL associated to the given targetId.
   */
  public final double getSysVel(final int targetId) {
    return getSysVels()[targetId];
  }

  /**
   * Get SYSVELs.
   *
   * @return the array of SYSVEL.
   */
  public final double[] getSysVels() {
    return getColumnDouble(OIFitsConstants.COLUMN_SYSVEL);
  }

  /**
   * Get VELTYP defined in this table according given target id.
   *
   * @param targetId target id
   * @return the VELTYP associated to the given targetId.
   */
  public final String getVelTyp(final int targetId) {
    return getVelTyps()[targetId];
  }

  /**
   * Get VELTYPs.
   *
   * @return the array of VELTYP.
   */
  public final String[] getVelTyps() {
    return getColumnString(OIFitsConstants.COLUMN_VELTYP);
  }

  /**
   * Get VELDEF defined in this table according given target id.
   *
   * @param targetId target id
   * @return the VELDEF associated to the given targetId.
   */
  public final String getVelDef(final int targetId) {
    return getVelDefs()[targetId];
  }

  /**
   * Get VELDEFs.
   *
   * @return the array of VELDEF.
   */
  public final String[] getVelDefs() {
    return getColumnString(OIFitsConstants.COLUMN_VELDEF);
  }

  /**
   * Get PMRA defined in this table according given target id.
   *
   * @param targetId target id
   * @return the PMRA associated to the given targetId.
   */
  public final double getPmRa(final int targetId) {
    return getPmRas()[targetId];
  }

  /**
   * Get PMRAs.
   *
   * @return the array of PMRA.
   */
  public final double[] getPmRas() {
    return getColumnDouble(OIFitsConstants.COLUMN_PMRA);
  }

  /**
   * Get PMDEC defined in this table according given target id.
   *
   * @param targetId target id
   * @return the PMDEC associated to the given targetId.
   */
  public final double getPmDec(final int targetId) {
    return getPmDecs()[targetId];
  }

  /**
   * Get PMDECs.
   *
   * @return the array of PMDEC.
   */
  public final double[] getPmDecs() {
    return getColumnDouble(OIFitsConstants.COLUMN_PMDEC);
  }

  /**
   * Get PMRA_ERR defined in this table according given target id.
   *
   * @param targetId target id
   * @return the PMRA_ERR associated to the given targetId.
   */
  public final double getPmRaErr(final int targetId) {
    return getPmRaErrs()[targetId];
  }

  /**
   * Get PMRA_ERRs.
   *
   * @return the array of PMRA_ERR.
   */
  public final double[] getPmRaErrs() {
    return getColumnDouble(OIFitsConstants.COLUMN_PMRA_ERR);
  }

  /**
   * Get PMDEC_ERR defined in this table according given target id.
   *
   * @param targetId target id
   * @return the PMDEC_ERR associated to the given targetId.
   */
  public final double getPmDecErr(final int targetId) {
    return getPmDecErrs()[targetId];
  }

  /**
   * Get PMDEC_ERRs.
   *
   * @return the array of PMDEC_ERR.
   */
  public final double[] getPmDecErrs() {
    return getColumnDouble(OIFitsConstants.COLUMN_PMDEC_ERR);
  }

  /**
   * Get PARALLAX defined in this table according given target id.
   *
   * @param targetId target id
   * @return the PARALLAX associated to the given targetId.
   */
  public final float getParallax(final int targetId) {
    return getParallaxs()[targetId];
  }

  /**
   * Get PARALLAXs.
   *
   * @return the array of PARALLAX.
   */
  public final float[] getParallaxs() {
    return getColumnFloat(OIFitsConstants.COLUMN_PARALLAX);
  }

  /**
   * Get PARA_ERR defined in this table according given target id.
   *
   * @param targetId target id
   * @return the PARA_ERR associated to the given targetId.
   */
  public final float getParaErr(final int targetId) {
    return getParaErrs()[targetId];
  }

  /**
   * Get PARA_ERRs.
   *
   * @return the array of PARA_ERR.
   */
  public final float[] getParaErrs() {
    return getColumnFloat(OIFitsConstants.COLUMN_PARA_ERR);
  }

  /**
   * Get SPECTYP defined in this table according given target id.
   *
   * @param targetId target id
   * @return the SPECTYP associated to the given targetId.
   */
  public final String getSpecTyp(final int targetId) {
    return getSpecTyps()[targetId];
  }

  /**
   * Get SPECTYPs.
   *
   * @return the array of SPECTYP.
   */
  public final String[] getSpecTyps() {
    return getColumnString(OIFitsConstants.COLUMN_SPECTYP);
  }

  /** 
   * Do syntactical analysis
   */
  @Override
  public void checkSyntax(final Logger checkLogger) {
    super.checkSyntax(checkLogger);

    final int len = getNbTargets();
    final short[] targetIds = getAcceptedTargetIds();
    final String[] targetNames = getTargetNames();

    for (int i = 0; i < len; i++) {
      for (int j = i + 1; j < len; j++) {
        if (targetIds[i] == targetIds[j]) {
          checkLogger.severe("TARGET_ID duplicated on lines " + i + "|" + j);
        }

        if (targetNames[i].equals(targetNames[j])) {
          checkLogger.severe("TARGET duplicated on lines " + i + "|" + j);
        }
      }
    }

    oifitsFile.checkCrossRefering(this, checkLogger);
  }

  /**
   * Fill the given buffer with the xml serialisation of the table.
   * @param sb string buffer
   * @param detailled if true the result will contain the table content
   */
  @Override
  public void getXmlDesc(final StringBuilder sb, final boolean detailled) {

    sb.append("<targets>");

    final short[] targetIds = getAcceptedTargetIds();

    for (int i = 0, len = getNbTargets(); i < len; i++) {
      sb.append("<target>");
      sb.append("<name>").append(getTargetName(i)).append("</name>");
      sb.append("<id>").append(targetIds[i]).append("</id>");
      sb.append("<raEp0>").append(getRaEp0(i)).append("</raEp0>");
      sb.append("<decEp0>").append(getDecEp0(i)).append("</decEp0>");
      sb.append("<equinox>").append(getEquinox(i)).append("</equinox>");
      sb.append("<raErr>").append(getRaErr(i)).append("</raErr>");
      sb.append("<decErr>").append(getDecErr(i)).append("</decErr>");
      sb.append("<sysVel>").append(getSysVel(i)).append("</sysVel>");
      sb.append("<velTyp>").append(getVelTyp(i)).append("</velTyp>");
      sb.append("<velDef>").append(getVelDef(i)).append("</velDef>");
      sb.append("<pmRa>").append(getPmRa(i)).append("</pmRa>");
      sb.append("<pmDec>").append(getPmDec(i)).append("</pmDec>");
      sb.append("<pmRaErr>").append(getPmRaErr(i)).append("</pmRaErr>");
      sb.append("<pmDecErr>").append(getPmDecErr(i)).append("</pmDecErr>");
      sb.append("<parallax>").append(getParallax(i)).append("</parallax>");
      sb.append("<paraErr>").append(getParaErr(i)).append("</paraErr>");
      sb.append("<specTyp>").append(getSpecTyp(i)).append("</specTyp>");
      sb.append("</target>");
    }

    sb.append("</targets>\n\n");

    super.getXmlDesc(sb, detailled);
  }

  /**
   * Return the stringified description.
   *
   * @return the description
   */
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(128);

    final short[] targetIds = getAcceptedTargetIds();

    for (int i = 0, len = getNbTargets(); i < len; i++) {
      sb.append("| ").append(getTargetName(i)).append("(").append(targetIds[i]).append(") ");
    }

    return super.toString() + " [ TARGETS[" + sb.toString().substring(1) + "]]";
  }
}
/*___oOo___*/
