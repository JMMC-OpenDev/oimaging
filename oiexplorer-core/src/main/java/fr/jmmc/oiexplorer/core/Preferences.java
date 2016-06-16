/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core;

import fr.jmmc.jmal.image.ColorModels;
import fr.jmmc.jmal.image.ColorScale;
import fr.jmmc.jmcs.data.preference.PreferencesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles common preferences for the Fits image viewer colorscale, LUT).
 */
public abstract class Preferences extends fr.jmmc.jmcs.data.preference.Preferences {

    /** default image LUT */
    public final static String DEFAULT_IMAGE_LUT = ColorModels.COLOR_MODEL_ASPRO_ISOPHOT;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(Preferences.class.getName());
    /* Preferences */
 /* TODO: adjust preference key values depending on the application ? */
    /** Preference : LUT table to use for the object model image in the UV Coverage plot */
    public final static String MODEL_IMAGE_LUT = "model.image.lut";
    /** Preference : Color scaling method to use for the object model image in the UV Coverage plot */
    public final static String MODEL_IMAGE_SCALE = "model.image.scale";

    /**
     * Creates a new Preferences object.
     *
     * This will set default preferences values (by invoking user overridden
     * setDefaultPreferences()), then try to load the preference file, if any.
     *
     * @param notify flag to enable/disable observer notifications
     */
    protected Preferences(final boolean notify) {
        super(notify);
    }    
    
    /**
     * Define the default properties used to reset default preferences.
     *
     * @throws PreferencesException if any preference value has a unsupported class type
     */
    @Override
    protected void setDefaultPreferences() throws PreferencesException {
        logger.debug("Preferences.setDefaultPreferences()");

        // Default color scale and LUT:
        setDefaultPreference(MODEL_IMAGE_LUT, DEFAULT_IMAGE_LUT);
        setDefaultPreference(MODEL_IMAGE_SCALE, ColorScale.LINEAR.toString());
    }

    /**
     * Return the Color scaling method Preference : use preferences or LINEAR if it is undefined
     * @return Color scaling method (LINEAR/LOGARITHMIC)
     */
    public final ColorScale getImageColorScale() {
        final String value = getPreference(MODEL_IMAGE_SCALE);

        try {
            return ColorScale.valueOf(value);
        } catch (IllegalArgumentException iae) {
            logger.debug("ignored invalid value: {}", value);
        }
        return ColorScale.LINEAR;
    }
}
