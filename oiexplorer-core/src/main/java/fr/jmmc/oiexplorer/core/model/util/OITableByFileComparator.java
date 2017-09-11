/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model.util;

import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OITable;
import java.util.Comparator;

/**
 * OITable comparator based on their file
 * @author bourgesl
 */
public final class OITableByFileComparator implements Comparator<OITable> {

    /** singleton instance */
    public static final OITableByFileComparator INSTANCE = new OITableByFileComparator();

    private OITableByFileComparator() {
        // private constructor
    }

    @Override
    public int compare(final OITable t1, final OITable t2) {
        return String.CASE_INSENSITIVE_ORDER.compare(getFileName(t1), getFileName(t2));
    }

    public static String getFileName(final OITable t) {
        if (t != null) {
            final OIFitsFile oiFitsFile = t.getOIFitsFile();
            if (oiFitsFile != null) {
                return oiFitsFile.getName();
            }
        }
        return "[Undefined]";
    }
}
