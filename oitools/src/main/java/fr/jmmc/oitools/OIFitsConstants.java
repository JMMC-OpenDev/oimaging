/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools;

/**
 * This interface contains useful constants of the FITS format
 * @author bourgesl
 */
public interface OIFitsConstants {

    /** UNKNOWN value */
    public final static String UNKNOWN_VALUE = "UNKNOWN";

    /* OIFits standard 1.0 */
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

    /* OIFits standard 2.0 */
    /** Table OI_SPECTRUM */
    public final static String TABLE_OI_SPECTRUM = "OI_SPECTRUM";
    // GRAVITY OI_FLUX:
    public final static String TABLE_OI_FLUX = "OI_FLUX";

    /* shared keywords or columns */
    /** OI_REVN keyword */
    public final static String KEYWORD_OI_REVN = "OI_REVN";
    /** OI_REVN = 1 keyword value */
    public final static int KEYWORD_OI_REVN_1 = 1;
    /** OI_REVN = 2 keyword value */
    public final static int KEYWORD_OI_REVN_2 = 2;
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

    /* OI_SPECTRUM table */
    /** FLUXDATA column */
    public final static String COLUMN_FLUXDATA = "FLUXDATA";
    /** FLUXERR column */
    public final static String COLUMN_FLUXERR = "FLUXERR";
    /* OI_FLUX table */
    /** FLUX column */
    public final static String COLUMN_FLUX = "FLUX";

    /* derived columns */
    /** STA_CONF derived OIData column as short[] */
    public final static String COLUMN_STA_CONF = "STA_CONF";
    /** HOUR_ANGLE derived OIData column as double[] */
    public final static String COLUMN_HOUR_ANGLE = "HOUR_ANGLE";
    /** RADIUS derived OIData column as double[] */
    public final static String COLUMN_RADIUS = "RADIUS";
    /** POS_ANGLE derived OIData column as double[] */
    public final static String COLUMN_POS_ANGLE = "POS_ANGLE";
    /** SPATIAL_FREQ derived OIData column as double[][] */
    public final static String COLUMN_SPATIAL_FREQ = "SPATIAL_FREQ";
    /** NIGHT_ID derived OiData column as int[] */
    public final static String COLUMN_NIGHT_ID = "NIGHT_ID";
    /** UCOORD_SPATIAL derived OIData column as double[][] */
    public final static String COLUMN_UCOORD_SPATIAL = "UCOORD_SPATIAL";
    /** VCOORD_SPATIAL derived OIData column as double[][] */
    public final static String COLUMN_VCOORD_SPATIAL = "VCOORD_SPATIAL";
    /** U1COORD_SPATIAL derived OIData column as double[][] */
    public final static String COLUMN_U1COORD_SPATIAL = "U1COORD_SPATIAL";
    /** V1COORD_SPATIAL derived OIData column as double[][] */
    public final static String COLUMN_V1COORD_SPATIAL = "V1COORD_SPATIAL";
    /** U2COORD_SPATIAL derived OIData column as double[][] */
    public final static String COLUMN_U2COORD_SPATIAL = "U2COORD_SPATIAL";
    /** V2COORD_SPATIAL derived OIData column as double[][] */
    public final static String COLUMN_V2COORD_SPATIAL = "V2COORD_SPATIAL";
    /** Resolution value */
    public final static String VALUE_RESOLUTION = "RESOLUTION";
}
