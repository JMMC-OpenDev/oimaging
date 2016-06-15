/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.image;

/**
 * This interface contains useful constants of the IMAGE-OI 
 * @author mella
 */
public interface ImageOiConstants {

    /* IMAGE-OI standard (WIP) */
    
    /** IMAGE-OI_INPUT_PARAM : extension name for IMAGE-OI parameters  */
    public final static String EXTNAME_IMAGE_OI_INPUT_PARAM = "IMAGE-OI INPUT PARAM";

    // Data Selection keywords
    /** TARGET keyword */
    public final static String KEYWORD_TARGET = "TARGET";
    /** WAVE_MIN keyword */
    public final static String KEYWORD_WAVE_MIN = "WAVE_MIN";
    /** WAVE_MAX keyword */
    public final static String KEYWORD_WAVE_MAX = "WAVE_MAX";
    /** USE_VIS keyword */
    public final static String KEYWORD_USE_VIS = "USE_VIS";
    /** USE_VIS2 keyword */
    public final static String KEYWORD_USE_VIS2 = "USE_VIS2";
    /** USE_T3 keyword */
    public final static String KEYWORD_USE_T3 = "USE_T3";

    // Algorithm setting keywords
    /** INIT_IMG keyword */
    public final static String KEYWORD_INIT_IMG = "INIT_IMG";
    /** MAXITER keyword */
    public final static String KEYWORD_MAXITER = "MAXITER";
    /** RGL_NAME keyword */
    public final static String KEYWORD_RGL_NAME = "RGL_NAME";
    /** RGL_WGT keyword */
    public final static String KEYWORD_RGL_WGT = "RGL_WGT";
    /** RGL_ALPH keyword */
    public final static String KEYWORD_RGL_ALPH = "RGL_ALPH";
    /** RGL_BETA keyword */
    public final static String KEYWORD_RGL_BETA = "RGL_BETA";
    /** RGL_PRIO keyword */
    public final static String KEYWORD_RGL_PRIO = "RGL_PRIO";

    // Image parameters keywords
    /** HDUNAME : keyword */
    public final static String KEYWORD_HDUNAME = "HDUNAME";
    /** HDUNAME : keyword */
    public final static String KEYWORD_DESCRIPTION_HDUNAME = "Unique name for the image within the FITS file";
   
}
