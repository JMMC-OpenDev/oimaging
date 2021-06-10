/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core;

import fr.jmmc.jmal.image.ColorModels;
import fr.jmmc.jmal.image.ColorScale;
import fr.jmmc.jmal.image.ImageUtils;
import fr.jmmc.jmal.image.ImageUtils.ImageInterpolation;
import fr.jmmc.jmcs.data.preference.PreferencesException;
import fr.jmmc.oiexplorer.core.gui.chart.ColorPalette;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oitools.model.Target;
import java.util.Observable;
import java.util.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles common preferences for the Fits image viewer colorscale, LUT).
 */
public abstract class Preferences extends fr.jmmc.jmcs.data.preference.Preferences {

    /** default image LUT */
    public final static String DEFAULT_IMAGE_LUT = ColorModels.COLOR_MODEL_ASPRO_ISOPHOT;
    /** default color palette */
    public final static String DEFAULT_COLOR_PALETTE = ColorPalette.DEFAULT_COLOR_PALETTE_NAME;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(Preferences.class.getName());
    /* Preferences */
    /** Preference : LUT table to use for the any image */
    public final static String MODEL_IMAGE_LUT = "model.image.lut";
    /** Preference : Color scaling method to use for the any image */
    public final static String MODEL_IMAGE_SCALE = "model.image.scale";
    /** Preference : image interpolation */
    public final static String MODEL_IMAGE_INTERPOLATION = "model.image.interpolation";
    /** Preference : Color palette to use in the charts / plots */
    public final static String CHART_PALETTE = "chart.palette";
    /** Preference : target matcher distance (as) */
    public final static String TARGET_MATCHER_SEPARATION = "target.matcher.sep";

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

    protected void addPreferenceObserver() {
        final Observer observer = new PreferenceObserver();
        addObserver(observer);
        // notify:
        observer.update(this, null);
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
        setDefaultPreference(MODEL_IMAGE_INTERPOLATION, ImageInterpolation.Bicubic.toString());
        // Color palette:
        setDefaultPreference(CHART_PALETTE, DEFAULT_COLOR_PALETTE);

        setDefaultPreference(TARGET_MATCHER_SEPARATION, Double.valueOf(1.0));
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

    /**
     * Return the Image interpolation Preference : use preferences or Bicubic if it is undefined
     * @return Image interpolation
     */
    public final ImageInterpolation getImageInterpolation() {
        final String value = getPreference(MODEL_IMAGE_INTERPOLATION);
        try {
            return ImageInterpolation.valueOf(value);
        } catch (IllegalArgumentException iae) {
            logger.debug("ignored invalid value: {}", value);
        }
        return ImageInterpolation.Bicubic;
    }

    private final class PreferenceObserver implements Observer {

        @Override
        public void update(Observable o, Object arg) {
            logger.debug("PreferenceObserver notified");

            ColorPalette.setColorPalettes(getPreference(CHART_PALETTE));

            ImageUtils.setImageInterpolation(getImageInterpolation());

            if (Target.MATCHER_LIKE.setSeparationInArcsec(getPreferenceAsDouble(Preferences.TARGET_MATCHER_SEPARATION))) {
                OIFitsCollectionManager.getInstance().fireOIFitsCollectionChanged();
            }
        }

    }
}
