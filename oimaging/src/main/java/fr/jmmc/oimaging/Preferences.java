/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging;

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

            // enable future notifications:
            _singleton.setNotify(true);
        }

        return _singleton;
    }

    @Override
    protected String getPreferenceFilename() {
        return "fr.jmmc.oimaging.properties";
    }

    @Override
    protected int getPreferencesVersionNumber() {
        return 1;
    }

}
