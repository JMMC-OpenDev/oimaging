/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.util;

/**
 * This class gathers main constant values
 * @author bourgesl
 */
public interface Constants {

    /** chart : enables the zoom in / out */
    public final static boolean ENABLE_ZOOM = true;

    /* ASTRO constants */
    /** EPOCH J2000 */
    public final static float EPOCH_J2000 = 2000.f;
    /** micrometers to meter */
    public final static double MICRO_METER = 1e-6;
    /** no value for combo boxes */
    public static final String NONE = "None";

    /* time references */
    /** LST time reference */
    public static final String TIME_LST = "L.S.T.";
    /** UTC time reference */
    public static final String TIME_UTC = "U.T.C.";
    /** HA time reference */
    public static final String TIME_HA = "H.A.";
    /** default value for undefined magnitude = -99 */
    public final static double UNDEFINED_MAGNITUDE = -99d;

    /* plots */
    /** JMMC legal notice on plots */
    public static final String JMMC_ANNOTATION = "Made by OIFitsExplorer/JMMC ";
    /** label to display when multiple configurations are in use (file names, chart titles ...) */
    public static final String MULTI_CONF = "MULTI CONFIGURATION";
    /** regular expression used to match characters different than alpha/numeric/+/- */
    public static final String REGEXP_INVALID_TEXT_CHARS = "[^a-zA-Z_\\+\\-0-9]";
}
