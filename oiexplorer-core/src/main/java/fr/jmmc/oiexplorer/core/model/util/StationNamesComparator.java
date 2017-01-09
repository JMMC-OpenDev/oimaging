/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model.util;

import fr.jmmc.jmcs.util.NumberUtils;
import java.util.Comparator;

/**
 * Custom comparator for baselines and configurations
 * @author bourgesl
 */
public final class StationNamesComparator implements Comparator<String> {

    /** singleton instance */
    public static final StationNamesComparator INSTANCE = new StationNamesComparator();

    private StationNamesComparator() {
        // private constructor
    }

    @Override
    public int compare(final String s1, final String s2) {
        int cmp = NumberUtils.compare(s1.length(), s2.length());
        if (cmp == 0) {
            cmp = s1.compareTo(s2);
        }
        return cmp;
    }
}
