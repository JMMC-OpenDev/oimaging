/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging;

import fr.jmmc.jmal.image.ColorModels;
import fr.jmmc.jmal.image.ColorScale;
import fr.jmmc.jmal.image.ImageUtils.ImageInterpolation;
import fr.jmmc.jmcs.data.preference.PreferencesException;
import java.util.ArrayList;
import java.util.Arrays;
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

    /* following names are raw string values, not constants (may be deprecated or unknown values) */
    public static final List<String> COLUMNS_DEFAULT_ALL = generateAllColumns();

    /** Visible results columns order list, as of default */
    public static final List<String> COLUMNS_DEFAULT_VISIBLE = Arrays.asList(
            "INDEX", "TARGET", "SOFTWARE", "INIT_IMG", "RGL_NAME", "RGL_WGT",
            "NITER", "CHISQ", "SUCCESS", "RATING", "USERNOTE", "FOV", "FILE"
    );

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
        setDefaultPreference(MODEL_IMAGE_SCALE, ColorScale.LINEAR.toString());
        // Disable interpolation:
        setDefaultPreference(MODEL_IMAGE_INTERPOLATION, ImageInterpolation.None.toString());

        // Use 10as by default
        setDefaultPreference(TARGET_MATCHER_SEPARATION, Double.valueOf(10.0));

        setDefaultPreference(RESULTS_COLUMNS_ALL, COLUMNS_DEFAULT_ALL);
        setDefaultPreference(RESULTS_COLUMNS_VISIBLE, COLUMNS_DEFAULT_VISIBLE);
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

    private static List<String> generateAllColumns() {
        // All results columns order list, as of default (collected by FEST OImagingDocJUnitTest @ 2021.12.21)
        final List<String> COLUMNS_ALL = Arrays.asList(
                "FILE", "INDEX", "JOB_DURATION", "SUCCESS", "CHISQ", "DELTA",
                "FLUX", "FLUXERR", "FOV", "JOBEND", "JOBSTART", "LAST_IMG",
                "MAXITER", "NITER", "NP_MIN", "RATING", "RGL_NAME", "RGL_WGT",
                "SOFTWARE", "TARGET", "USERNOTE", "VERSION", "WAVE_MAX", "WAVE_MIN",
                "AUTO_WGT", "INIT_IMG", "RGL_ALPH", "RGL_BETA", "RGL_PRIO", "SCALE",
                "THRESHOL", "USE_T3", "USE_VIS", "USE_VIS2", "ENTROPY", "FPRIOR",
                "INITFLUX", "NDATA", "NEVAL", "PLUGIN", "PXL_MIN", "RECENTER", "REPEAT", "RGL_GAMM", "RGL_TAU",
                "SMEAR_FC", "SMEAR_FN", "T3AMPA",
                "T3AMPB", "T3PHIA", "T3PHIB", "UV_MAX", "V2A", "V2B", "XFORM"
        );

        /* sparco specific (dynamic keywords) */
        final List<String> COLUMNS_SPARCO = Arrays.asList(
                "SDEX", "SDEY", "SFLU", "SIDX", "SMOD", "SPEC", "SWAVE"
        );

        final List<String> allColumns = new ArrayList<>(COLUMNS_ALL);

        for (int i = 0; i <= 5; i++) {
            for (String prefix : COLUMNS_SPARCO) {
                allColumns.add(prefix + i);
            }
        }
        logger.debug("generateAllColumns: {}", allColumns);
        return allColumns;
    }
}
