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
}
