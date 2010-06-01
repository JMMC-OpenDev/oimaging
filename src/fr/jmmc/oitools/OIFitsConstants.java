/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: OIFitsConstants.java,v 1.2 2010-06-01 15:55:10 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2010/04/28 14:45:00  bourgesl
 * String constants for OI Fits standard rev 1.0
 *
 */
package fr.jmmc.oitools;

/**
 * This interface contains useful constants of the FITS format
 * @author bourgesl
 */
public interface OIFitsConstants {

  /** UNKNOWN value */
  public final static String UNKNOWN_VALUE = "UNKNOWN";

  /* Fits standard */
  /** NAXIS2 keyword = number of rows in a binary table */
  public final static String KEYWORD_NAXIS2 = "NAXIS2";
  /** EXTNAME keyword = Extension name */
  public final static String KEYWORD_EXT_NAME = "EXTNAME";
  /** EXTVER keyword = Extension version */
  public final static String KEYWORD_EXT_VER = "EXTVER";

  /* OIFits standard */
  /** Table OI_ARRAY */
  public final static String TABLE_OI_ARRAY = "OI_ARRAY";
  /** Table OI_TARGET */
  public final static String TABLE_OI_TARGET = "OI_TARGET";
  /** Table OI_WAVELENGTH */
  public final static String TABLE_OI_WAVELENGTH = "OI_WAVELENGTH";
  /** Table OI_VIS */
  public final static String TABLE_OI_VIS = "OI_VIS";
  /** Table OI_VIS2 */
  public final static String TABLE_OI_VIS2 = "OI_VIS2";
  /** Table OI_T3 */
  public final static String TABLE_OI_T3 = "OI_T3";

  /* shared keywords or columns */
  /** OI_REVN keyword */
  public final static String KEYWORD_OI_REVN = "OI_REVN";
  /** OI_REVN = 1 keyword value */
  public final static int KEYWORD_OI_REVN_1 = 1;
  /** ARRNAME keyword */
  public final static String KEYWORD_ARRNAME = "ARRNAME";
  /** INSNAME keyword */
  public final static String KEYWORD_INSNAME = "INSNAME";
  /** TARGET_ID column */
  public final static String COLUMN_TARGET_ID = "TARGET_ID";
  /** STA_INDEX column */
  public final static String COLUMN_STA_INDEX = "STA_INDEX";

  /* OI_ARRAY table */
  /** FRAME   keyword */
  public final static String KEYWORD_FRAME = "FRAME";
  /** FRAME GEOCENTRIC keyword value */
  public final static String KEYWORD_FRAME_GEOCENTRIC = "GEOCENTRIC";
  /** ARRAYX  keyword */
  public final static String KEYWORD_ARRAY_X = "ARRAYX";
  /** ARRAYY  keyword */
  public final static String KEYWORD_ARRAY_Y = "ARRAYY";
  /** ARRAYZ  keyword */
  public final static String KEYWORD_ARRAY_Z = "ARRAYZ";
  /** TEL_NAME column */
  public final static String COLUMN_TEL_NAME = "TEL_NAME";
  /** STA_NAME column */
  public final static String COLUMN_STA_NAME = "STA_NAME";
  /** DIAMETER column */
  public final static String COLUMN_DIAMETER = "DIAMETER";
  /** STAXYZ column */
  public final static String COLUMN_STA_XYZ = "STAXYZ";

  /* OI_WAVELENGTH table */
  /** EFF_WAVE column */
  public final static String COLUMN_EFF_WAVE = "EFF_WAVE";
  /** EFF_BAND column */
  public final static String COLUMN_EFF_BAND = "EFF_BAND";

  /* OI_TARGET table */
  /** TARGET column */
  public final static String COLUMN_TARGET = "TARGET";
  /** RAEP0 column */
  public final static String COLUMN_RAEP0 = "RAEP0";
  /** DECEP0 column */
  public final static String COLUMN_DECEP0 = "DECEP0";
  /** EQUINOX column */
  public final static String COLUMN_EQUINOX = "EQUINOX";
  /** RA_ERR column */
  public final static String COLUMN_RA_ERR = "RA_ERR";
  /** DEC_ERR column */
  public final static String COLUMN_DEC_ERR = "DEC_ERR";
  /** SYSVEL column */
  public final static String COLUMN_SYSVEL = "SYSVEL";
  /** VELTYP column */
  public final static String COLUMN_VELTYP = "VELTYP";
  /** VELTYP LSR column value */
  public final static String COLUMN_VELTYP_LSR = "LSR";
  /** VELTYP HELIOCEN column value */
  public final static String COLUMN_VELTYP_HELIOCEN = "HELIOCEN";
  /** VELTYP BARYCENT column value */
  public final static String COLUMN_VELTYP_BARYCENT = "BARYCENT";
  /** VELTYP GEOCENTR column value */
  public final static String COLUMN_VELTYP_GEOCENTR = "GEOCENTR";
  /** VELTYP TOPOCENT column value */
  public final static String COLUMN_VELTYP_TOPOCENT = "TOPOCENT";
  /** VELDEF column */
  public final static String COLUMN_VELDEF = "VELDEF";
  /** VELDEF RADIO column value */
  public final static String COLUMN_VELDEF_RADIO = "RADIO";
  /** VELDEF OPTICAL column value */
  public final static String COLUMN_VELDEF_OPTICAL = "OPTICAL";
  /** PMRA column */
  public final static String COLUMN_PMRA = "PMRA";
  /** PMDEC column */
  public final static String COLUMN_PMDEC = "PMDEC";
  /** PMRA_ERR column */
  public final static String COLUMN_PMRA_ERR = "PMRA_ERR";
  /** PMDEC_ERR column */
  public final static String COLUMN_PMDEC_ERR = "PMDEC_ERR";
  /** PARALLAX column */
  public final static String COLUMN_PARALLAX = "PARALLAX";
  /** PARA_ERR column */
  public final static String COLUMN_PARA_ERR = "PARA_ERR";
  /** SPECTYP column */
  public final static String COLUMN_SPECTYP = "SPECTYP";

  /* OI DATA tables */
  /** DATE-OBS keyword */
  public final static String KEYWORD_DATE_OBS = "DATE-OBS";
  /** TIME column */
  public final static String COLUMN_TIME = "TIME";
  /** MJD column */
  public final static String COLUMN_MJD = "MJD";
  /** INT_TIME column */
  public final static String COLUMN_INT_TIME = "INT_TIME";
  /** UCOORD column */
  public final static String COLUMN_UCOORD = "UCOORD";
  /** VCOORD column */
  public final static String COLUMN_VCOORD = "VCOORD";
  /** FLAG column */
  public final static String COLUMN_FLAG = "FLAG";

  /* OI_VIS table */
  /** VISAMP column */
  public final static String COLUMN_VISAMP = "VISAMP";
  /** VISAMPERR column */
  public final static String COLUMN_VISAMPERR = "VISAMPERR";
  /** VISPHI column */
  public final static String COLUMN_VISPHI = "VISPHI";
  /** VISPHIERR column */
  public final static String COLUMN_VISPHIERR = "VISPHIERR";

  /* Aspro Extension with complex visibilities (like AMBER OIFits) */
  /** VISDATA column */
  public final static String COLUMN_VISDATA = "VISDATA";
  /** VISERR column */
  public final static String COLUMN_VISERR = "VISERR";

  /* OI_VIS2 table */
  /** VIS2DATA column */
  public final static String COLUMN_VIS2DATA = "VIS2DATA";
  /** VIS2ERR column */
  public final static String COLUMN_VIS2ERR = "VIS2ERR";

  /* OI_T3 table */
  /** T3AMP column */
  public final static String COLUMN_T3AMP = "T3AMP";
  /** T3AMPERR column */
  public final static String COLUMN_T3AMPERR = "T3AMPERR";
  /** T3PHI column */
  public final static String COLUMN_T3PHI = "T3PHI";
  /** T3PHIERR column */
  public final static String COLUMN_T3PHIERR = "T3PHIERR";
  /** U1COORD column */
  public final static String COLUMN_U1COORD = "U1COORD";
  /** V1COORD column */
  public final static String COLUMN_V1COORD = "V1COORD";
  /** U2COORD column */
  public final static String COLUMN_U2COORD = "U2COORD";
  /** V2COORD column */
  public final static String COLUMN_V2COORD = "V2COORD";
}
