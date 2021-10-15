/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging;

import fr.jmmc.jmal.image.ColorModels;
import fr.jmmc.jmal.image.ColorScale;
import fr.jmmc.jmal.image.ImageUtils.ImageInterpolation;
import fr.jmmc.jmcs.data.preference.PreferencesException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TBD class
 * @author mellag
 */
public class Preferences extends fr.jmmc.oiexplorer.core.Preferences {

    /** Singleton instance */
    private static Preferences _singleton = null;
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(Preferences.class.getName());
    /** prefix for all results columns */
    public static final String RESULTS_COLUMNS_ALL = "results.columns.all";
    /** prefix for visible results columns */
    public static final String RESULTS_COLUMNS_VISIBLE = "results.columns.visible";
    /** Default results columns order list, as of default */
    private static final String COLUMNS_DEFAULT = ""; // TODO: define interesting sets of columns

    /**
     * Private constructor that must be empty.
     *
     * @param notify flag to enable/disable observer notifications
     */
    private Preferences(final boolean notify) {
        super(notify);
    }

    /**
     * Return the singleton instance of Preferences.
     *
     * @return the singleton preference instance
     */
    public synchronized static Preferences getInstance() {
        // Build new reference if singleton does not already exist
        // or return previous reference
        if (_singleton == null) {
            logger.debug("Preferences.getInstance()");

            // disable notifications:
            _singleton = new Preferences(false);

            // register color palette observer:
            _singleton.addPreferenceObserver();

            // enable future notifications:
            _singleton.setNotify(true);
        }

        return _singleton;
    }

    /**
     * Define the default properties used to reset default preferences.
     *
     * @throws PreferencesException if any preference value has a unsupported class type
     */
    @Override
    protected void setDefaultPreferences() throws PreferencesException {
        super.setDefaultPreferences();

        logger.debug("Preferences.setDefaultPreferences()");

        // Use GD's prefered value for a better image viewing that isophot...
        setDefaultPreference(MODEL_IMAGE_LUT, ColorModels.COLOR_MODEL_HEAT);
        setDefaultPreference(MODEL_IMAGE_SCALE, ColorScale.LOGARITHMIC.toString());
        // Disable interpolation:
        setDefaultPreference(MODEL_IMAGE_INTERPOLATION, ImageInterpolation.None.toString());

        setDefaultPreference(RESULTS_COLUMNS_ALL, COLUMNS_DEFAULT);
        setDefaultPreference(RESULTS_COLUMNS_VISIBLE, COLUMNS_DEFAULT);
    }

    @Override
    protected String getPreferenceFilename() {
        return "fr.jmmc.oimaging.properties";
    }

    @Override
    protected int getPreferencesVersionNumber() {
        return 1;
    }

    public List<String> getResultsAllColumns() {
        return getPreferenceAsStringList(RESULTS_COLUMNS_ALL);
    }

    public void setResultsAllColumns(final List<String> allColumns) {
        logger.debug("setResultsAllColumns: [{}]", allColumns);
        setPreferenceAndSaveToFile(RESULTS_COLUMNS_ALL, allColumns);
    }

    public List<String> getResultsVisibleColumns() {
        return getPreferenceAsStringList(RESULTS_COLUMNS_VISIBLE);
    }

    public void setResultsVisibleColumns(final List<String> visibleColumns) {
        logger.debug("setResultsVisibleColumns: [{}]", visibleColumns);
        setPreferenceAndSaveToFile(RESULTS_COLUMNS_VISIBLE, visibleColumns);
    }
}
