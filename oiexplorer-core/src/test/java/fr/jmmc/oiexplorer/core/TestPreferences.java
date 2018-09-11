/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core;

import fr.jmmc.jmal.image.ColorScale;
import fr.jmmc.jmcs.data.preference.PreferencesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles preferences for OIFitsExplorer TESTS.
 */
public final class TestPreferences extends fr.jmmc.oiexplorer.core.Preferences {

    /** Singleton instance */
    private static Preferences _singleton = null;
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(Preferences.class.getName());

    /**
     * Private constructor that must be empty.
     *
     * @param notify flag to enable/disable observer notifications
     */
    private TestPreferences(final boolean notify) {
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

            _singleton = new TestPreferences(true);
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
    }

    /**
     * Return the preference filename.
     *
     * @return preference filename.
     */
    @Override
    protected String getPreferenceFilename() {
        return "fr.jmmc.aspro.properties";
    }

    /**
     *  Return preference version number.
     *
     * @return preference version number.
     */
    @Override
    protected int getPreferencesVersionNumber() {
        return 1;
    }

}
