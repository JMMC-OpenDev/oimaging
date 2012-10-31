/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.image;

/**
 * This interface contains useful constants of the FITS Image format
 * @author bourgesl
 */
public interface FitsImageConstants {

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
