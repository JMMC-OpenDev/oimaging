/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model.util;

import fr.jmmc.oitools.model.OIFitsFile;
import java.util.Comparator;

/**
 * OIFitsFile comparator based on file name
 * @author bourgesl
 */
public final class OIFitsFileComparator implements Comparator<OIFitsFile> {

    /** singleton instance */
    public static final OIFitsFileComparator INSTANCE = new OIFitsFileComparator();

    private OIFitsFileComparator() {
        // private constructor
    }

    @Override
    public int compare(final OIFitsFile o1, final OIFitsFile o2) {
        final String filename1 = (o1 != null) ? o1.getName() : "";
        final String filename2 = (o2 != null) ? o2.getName() : "";
        return String.CASE_INSENSITIVE_ORDER.compare(filename1, filename2);
    }

}
