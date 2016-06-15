/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model.util;

import fr.jmmc.oiexplorer.core.model.oi.TargetUID;
import java.util.Comparator;

/**
 * TargetUID comparator based on target name
 * @author bourgesl
 */
public final class TargetUIDComparator implements Comparator<TargetUID> {

    /** singleton instance */
    public static final TargetUIDComparator INSTANCE = new TargetUIDComparator();

    private TargetUIDComparator() {
        // private constructor
    }

    @Override
    public int compare(final TargetUID o1, final TargetUID o2) {
        final String target1 = (o1 != null) ? o1.getTarget() : "";
        final String target2 = (o2 != null) ? o2.getTarget() : "";
        return String.CASE_INSENSITIVE_ORDER.compare(target1, target2);
    }

}
