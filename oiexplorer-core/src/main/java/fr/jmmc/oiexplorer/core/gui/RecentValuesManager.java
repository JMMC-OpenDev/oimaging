/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui;

import fr.jmmc.jmcs.data.preference.SessionSettingsPreferences;
import fr.jmmc.jmcs.util.StringUtils;
import fr.jmmc.jmcs.util.collection.FixedSizeLinkedHashMap;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RecentValuesManager singleton class.
 * 
 * @author Laurent BOURGES.
 */
public final class RecentValuesManager {

    /** Logger */
    private static final Logger _logger = LoggerFactory.getLogger(RecentValuesManager.class.getName());
    /** Maximum number of recent values by field type */
    private static final int MAXIMUM_HISTORY_ENTRIES = 10;
    /** Singleton instance */
    private static volatile RecentValuesManager _instance = null;
    /* Members */
    /** Flag to enable or disable this feature */
    boolean _enabled = true;
    /** Hook to the "Open Recent" sub-menu */
    final JPopupMenu _popup;
    /** recent values repository */
    private final Map<String, Map<String, String>> _repository = new HashMap<String, Map<String, String>>(64);

    /**
     * Return the singleton instance
     * @return singleton instance
     */
    static synchronized RecentValuesManager getInstance() {
        // DO NOT MODIFY !!!
        if (_instance == null) {
            _instance = new RecentValuesManager();
        }

        return _instance;
        // DO NOT MODIFY !!!
    }

    /**
     * Hidden constructor
     */
    protected RecentValuesManager() {
        _popup = new JPopupMenu();
    }

    /**
     * Enables or disables this feature
     * @param enabled false to disable
     */
    public static void setEnabled(final boolean enabled) {
        getInstance()._enabled = enabled;
    }

    /**
     * Return flag to enable or disable this feature
     * @return true if enabled; false otherwise
     */
    public boolean isEnabled() {
        return _enabled;
    }

    /**
     * Return the Popup menu
     * @return Popup menu
     */
    public static JPopupMenu getMenu() {
        final RecentValuesManager rvm = getInstance();
        if (rvm.isEnabled()) {
            return rvm._popup;
        }
        return null;
    }

    // TODO:
    public void register(final String key) {
        populateRepositoryFromPreferences(key);
    }

    /**
     * Add the given recent value.
     * @param key given key
     * @param value value as String
     */
    public static void addValue(final String key, final String value) {
        if (value == null) {
            return;
        }

        final RecentValuesManager rvm = getInstance();
        if (!rvm.isEnabled() || !rvm.storeValue(key, value)) {
            return;
        }
        rvm.refreshMenu(key);
        rvm.flushRepositoryToPreferences(key);
    }

    /**
     * Store the given value in the recent repository.
     * @param key given key
     * @param value value to be added in the repository
     * @return true if operation succeeded else false.
     */
    private boolean storeValue(final String key, final String value) {
        // Check parameter validity
        if (StringUtils.isEmpty(value)) {
            return false;
        }

        // Store value (at first position if already referenced)
        synchronized (_repository) {
            Map<String, String> repositoryValues = _repository.get(key);
            if (repositoryValues == null) {
                repositoryValues = new FixedSizeLinkedHashMap<String, String>(MAXIMUM_HISTORY_ENTRIES);
                _repository.put(key, repositoryValues);
            }
            // 
            repositoryValues.remove(value);
            repositoryValues.put(value, value);
        }
        return true;
    }

    /**
     * Grab recent values from shared preference.
     * @param key given key
     */
    private void populateRepositoryFromPreferences(final String key) {
        final List<String> values = SessionSettingsPreferences.getRecentValues(key);
        if (values == null) {
            return;
        }
        for (String value : values) {
            storeValue(key, value);
        }
        refreshMenu(key);
    }

    /**
     * Flush values associated to the given key to shared preference.
     * @param key given key
     */
    private void flushRepositoryToPreferences(final String key) {
        // Create list of paths
        final List<String> values;
        synchronized (_repository) {
            values = new ArrayList<String>(_repository.get(key).keySet());
        }
        // Put this to prefs
        SessionSettingsPreferences.setRecentValues(key, values);
    }

    /**
     * Refresh content of the associated pop menu.
     * @param key given key
     */
    private void refreshMenu(final String key) {
        // Clean, then re-fill sub-menu
        _popup.removeAll();
        _popup.setEnabled(false);

        // For each registered values
        final ListIterator<Map.Entry<String, String>> iter;
        synchronized (_repository) {
            final Map<String, String> repositoryValues = _repository.get(key);
            if (repositoryValues == null) {
                return;
            } else {
                iter = new ArrayList<Map.Entry<String, String>>(repositoryValues.entrySet()).listIterator(repositoryValues.size());
            }
        }

        int count = 0;

        while (iter.hasPrevious()) {
            final Map.Entry<String, String> entry = iter.previous();
            final String currentName = entry.getValue();
            final String currentPath = entry.getKey();

            // Create an action to open it
            //final String currentName = _repository.get(currentPath);
            final AbstractAction currentAction = new AbstractAction(currentName) {
                /** default serial UID for Serializable interface */
                private static final long serialVersionUID = 1;

                @Override
                public void actionPerformed(ActionEvent ae) {
                    // TODO: ??
                }
            };

            final JMenuItem menuItem = new JMenuItem(currentAction);
            menuItem.setToolTipText(currentPath);
            _popup.add(menuItem);
            count++;
        }
        if (count != 0) {
            _popup.setEnabled(true);
        }
    }
}
