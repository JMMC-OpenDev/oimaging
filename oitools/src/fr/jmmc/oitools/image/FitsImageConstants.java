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
    /** CRPIX1 keyword = Position of the reference pixel along the columns */
    public final static String KEYWORD_CRPIX1 = "CRPIX1";
    /** CRPIX2 keyword = Position of the reference pixel along the rows */
    public final static String KEYWORD_CRPIX2 = "CRPIX2";
    /** CRPIX3 keyword = Position of the reference pixel along the wavelengths */
    public final static String KEYWORD_CRPIX3 = "CRPIX3";
    /** CRVAL1 keyword = Coordinate value at the reference pixel column (Units) */
    public final static String KEYWORD_CRVAL1 = "CRVAL1";
    /** CRVAL2 keyword = Coordinate value at the reference pixel row (Units) */
    public final static String KEYWORD_CRVAL2 = "CRVAL2";
    /** CRVAL3 keyword = Wavelength value at the reference pixel wavelength (Units) */
    public final static String KEYWORD_CRVAL3 = "CRVAL3";
    /** CDELT1 keyword = Coordinate increment per pixel along the columns (Units) */
    public final static String KEYWORD_CDELT1 = "CDELT1";
    /** CDELT2 keyword = Coordinate increment per pixel along the rows (Units) */
    public final static String KEYWORD_CDELT2 = "CDELT2";
    /** CDELT3 keyword = Wavelength increment per pixel along the wavelengths (Units) */
    public final static String KEYWORD_CDELT3 = "CDELT3";
    /** CUNIT1 keyword = Physical units of the axis 1 = columns (Units) */
    public final static String KEYWORD_CUNIT1 = "CUNIT1";
    /** CUNIT2 keyword = Physical units of the axis 2 = rows (Units) */
    public final static String KEYWORD_CUNIT2 = "CUNIT2";
    /** CUNIT3 keyword = Physical units of the axis 3 = wavelengths (Units) */
    public final static String KEYWORD_CUNIT3 = "CUNIT3";

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
