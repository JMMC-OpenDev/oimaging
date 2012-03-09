/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.image;

/**
 * This interface contains useful constants of the FITS Image format
 * @author bourgesl
 */
public interface FitsImageConstants {

    /* Fits standard */
    /** SIMPLE keyword = Fits standard compliant */
    public final static String KEYWORD_SIMPLE = "SIMPLE";
    /** BITPIX keyword = bits per data value */
    public final static String KEYWORD_BITPIX = "BITPIX";
    /** EXTEND keyword = File may contain extensions */
    public final static String KEYWORD_EXTEND = "EXTEND";
    /** EXTVER keyword = Extension version */
    public final static String KEYWORD_EXT_VER = "EXTVER";
    /** NAXIS keyword = number of data axes */
    public final static String KEYWORD_NAXIS = "NAXIS";
    /** NAXIS1 keyword = number of columns in the image */
    public final static String KEYWORD_NAXIS1 = "NAXIS1";
    /** NAXIS2 keyword = number of rows in the image */
    public final static String KEYWORD_NAXIS2 = "NAXIS2";
    /** BZERO keyword = zero point in scaling equation */
    public final static String KEYWORD_BZERO = "BZERO";
    /** BSCALE keyword = linear factor in scaling equation */
    public final static String KEYWORD_BSCALE = "BSCALE";
    /** DATAMIN keyword = minimum data value */
    public final static String KEYWORD_DATAMIN = "DATAMIN";
    /** DATAMAX keyword = maximum data value */
    public final static String KEYWORD_DATAMAX = "DATAMAX";

    /* Image Fits standard */
    /** CRPIX1 keyword = position of the reference pixel along the columns */
    public final static String KEYWORD_CRPIX1 = "CRPIX1";
    /** CRPIX2 keyword = position of the reference pixel along the rows */
    public final static String KEYWORD_CRPIX2 = "CRPIX2";
    /** CRVAL1 keyword = coordinate value at the reference pixel column */
    public final static String KEYWORD_CRVAL1 = "CRVAL1";
    /** CRVAL2 keyword = coordinate value at the reference pixel row */
    public final static String KEYWORD_CRVAL2 = "CRVAL2";
    /** CDELT1 keyword = Coordinate increment per pixel along the columns */
    public final static String KEYWORD_CDELT1 = "CDELT1";
    /** CDELT2 keyword = Coordinate increment per pixel along the rows */
    public final static String KEYWORD_CDELT2 = "CDELT2";

    /* default values for missing keywords */
    /** default value (1.0) for CRPIXn keywords */
    public final static double DEFAULT_CRPIX = 1d;
    /** default value (0.0) for CRVALn keywords */
    public final static double DEFAULT_CRVAL = 0d;
    /** default value (1.0 rad) for CDELTn keywords */
    public final static double DEFAULT_CDELT = 1d;
    /** default value (Double.NaN) for DATAMIN keywords */
    public final static double DEFAULT_DATA_MIN = Double.NaN;
    /** default value (Double.NaN) for DATAMAX keywords */
    public final static double DEFAULT_DATA_MAX = Double.NaN;
}
