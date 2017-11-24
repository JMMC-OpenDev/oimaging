/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.fits;

/**
 * This interface contains useful constants of the FITS format
 * @author bourgesl
 */
public interface FitsConstants {

    /* Fits standard */
    /** SIMPLE keyword = Fits standard compliant */
    public final static String KEYWORD_SIMPLE = "SIMPLE";
    /** EXTEND keyword = File may contain extensions */
    public final static String KEYWORD_EXTEND = "EXTEND";
    /** XTENSION keyword = Fits extension type (BINTABLE ...) */
    public final static String KEYWORD_XTENSION = "XTENSION";
    /** EXTNAME keyword = Extension name */
    public final static String KEYWORD_EXT_NAME = "EXTNAME";
    /** EXTVER keyword = Extension version */
    public final static String KEYWORD_EXT_VER = "EXTVER";
    /** BITPIX keyword = bits per data value */
    public final static String KEYWORD_BITPIX = "BITPIX";
    /** NAXIS keyword = number of data axes */
    public final static String KEYWORD_NAXIS = "NAXIS";
    /** NAXIS1 keyword = number of columns in the image */
    public final static String KEYWORD_NAXIS1 = "NAXIS1";
    /** NAXIS2 keyword = number of rows in the image */
    public final static String KEYWORD_NAXIS2 = "NAXIS2";
    /** NAXIS3 keyword = number of planes in the cube */
    public final static String KEYWORD_NAXIS3 = "NAXIS3";
    /** GCOUNT keyword = Group count */
    public final static String KEYWORD_GCOUNT = "GCOUNT";
    /** PCOUNT keyword = Random parameter count */
    public final static String KEYWORD_PCOUNT = "PCOUNT";
    /** BZERO keyword = zero point in scaling equation */
    public final static String KEYWORD_BZERO = "BZERO";
    /** BSCALE keyword = linear factor in scaling equation */
    public final static String KEYWORD_BSCALE = "BSCALE";
    /** DATAMIN keyword = minimum data value */
    public final static String KEYWORD_DATAMIN = "DATAMIN";
    /** DATAMAX keyword = maximum data value */
    public final static String KEYWORD_DATAMAX = "DATAMAX";

    /* Fits Main Header */
    /** ORIGIN keyword = Insitution responsible for file creation */
    public final static String KEYWORD_ORIGIN = "ORIGIN";
    /** DATE keyword = Date the HDU was written */
    public final static String KEYWORD_DATE = "DATE";
    /** DATE_OBS keyword = Strat date of observation */
    public final static String KEYWORD_DATE_OBS = "DATE-OBS";
    /** CONTENT keyword = Must contain only the string "OIFITS2" */
    public final static String KEYWORD_CONTENT = "CONTENT";
    /** value OIFITS2 for KEYWORD CONTENT  */
    public final static String KEYWORD_CONTENT_OIFITS2 = "OIFITS2";
    /** AUTHOR keyword = As defined in Fits norm */
    public final static String KEYWORD_AUTHOR = "AUTHOR";
    /** DATASUM keyword = HDU Datasum */
    public final static String KEYWORD_DATASUM = "DATASUM";
    /** CHECKSUM keyword = HDU CHECKSUM */
    public final static String KEYWORD_CHECKSUM = "CHECKSUM";
    /** TELESCOP keyword = A generic identification of the ARRAY */
    public final static String KEYWORD_TELESCOP = "TELESCOP";
    /** INSTRUME keyword = A generic identifition of this instrument */
    public final static String KEYWORD_INSTRUME = "INSTRUME";
    /** OBSERVER keyword = Who acquired the data */
    public final static String KEYWORD_OBSERVER = "OBSERVER";
    /** INSMODE keyword = Instrument mode */
    public final static String KEYWORD_INSMODE = "INSMODE";
    /** OBJECT keyword = Object identifier */
    public final static String KEYWORD_OBJECT = "OBJECT";
    /** REFERENC keyword = Bibliographic reference */
    public final static String KEYWORD_REFERENC = "REFERENC";

    /* Ascii or Binary Table Fits standard */
    /** TFIELDS keyword = number of columns */
    public final static String KEYWORD_TFIELDS = "TFIELDS";
    /** TFORM keyword = Column data type */
    public final static String KEYWORD_TFORM = "TFORM";
    /** TTYPE keyword = Column label (optional) */
    public final static String KEYWORD_TTYPE = "TTYPE";
    /** TDIM keyword = size of the multidimensional array (optional) */
    public final static String KEYWORD_TDIM = "TDIM";
    /** TUNIT keyword = Column unit (optional) */
    public final static String KEYWORD_TUNIT = "TUNIT";
    /** HISTORY keyword = history information */
    public final static String KEYWORD_HISTORY = "HISTORY";
    /** COMMENT keyword = useless information */
    public final static String KEYWORD_COMMENT = "COMMENT";

    /** Speed of light (2.99792458e8) */
    public final static double C_LIGHT = 2.99792458e8d;
}
