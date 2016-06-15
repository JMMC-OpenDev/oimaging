/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model.util;

import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oitools.model.Granule;
import fr.jmmc.oitools.model.Granule.GranuleField;
import fr.jmmc.oitools.model.InstrumentMode;
import fr.jmmc.oitools.model.Target;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Granule comparator based on target name
 * @author bourgesl
 */
public final class GranuleComparator implements Comparator<Granule> {

    /** singleton instance */
    public static final GranuleComparator DEFAULT = new GranuleComparator(
            Arrays.asList(
                    GranuleField.TARGET,
                    GranuleField.INS_MODE,
                    GranuleField.NIGHT
            )
    );

    public static final Comparator<Target> cmpTarget = new Comparator<Target>() {
        @Override
        public int compare(final Target t1, final Target t2) {
            // Just name
            return String.CASE_INSENSITIVE_ORDER.compare(t1.getTarget(), t2.getTarget());
        }
    };

    public static final Comparator<InstrumentMode> cmpInsMode = new Comparator<InstrumentMode>() {
        @Override
        public int compare(final InstrumentMode i1, final InstrumentMode i2) {
            // name
            int cmp = String.CASE_INSENSITIVE_ORDER.compare(i1.getInsName(), i2.getInsName());
            if (cmp != 0) {
                return cmp;
            }
            // nb channels
            cmp = NumberUtils.compare(i1.getNbChannels(), i2.getNbChannels());
            if (cmp != 0) {
                return cmp;
            }
            cmp = Float.compare(i1.getResPower(), i2.getResPower());

            return cmp;
        }
    };

    @SuppressWarnings("unchecked")
    public static final Comparator<Comparable> cmpComparable = new Comparator<Comparable>() {
        @Override
        public int compare(Comparable c1, Comparable c2) {
            return c1.compareTo(c2);
        }
    };

    // members:
    private final List<GranuleField> sortDirectives;

    public GranuleComparator(List<GranuleField> sortDirectives) {
        this.sortDirectives = sortDirectives;
    }

    public List<GranuleField> getSortDirectives() {
        return sortDirectives;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int compare(final Granule g1, final Granule g2) {

        // @see fr.jmmc.sclgui.calibrator.TableSorter
        int comparison;
        Object o1, o2;

        for (int i = 0, len = sortDirectives.size(); i < len; i++) {
            final GranuleField field = sortDirectives.get(i);

            o1 = g1.getField(field);
            o2 = g2.getField(field);

            // Define null less than everything, except null.
            if ((o1 == null) && (o2 == null)) {
                comparison = 0;
            } else if (o1 == null) {
                comparison = -1;
            } else if (o2 == null) {
                comparison = 1;
            } else {
                comparison = getComparator(field).compare(o1, o2);
            }
            if (comparison != 0) {
                return comparison;
            }
        }

        return 0;
    }

    public Comparator getComparator(GranuleField field) {
        switch (field) {
            case TARGET:
                return cmpTarget;
            case INS_MODE:
                return cmpInsMode;
            case NIGHT:
                return cmpComparable;
            default:
                return null;
        }
    }

}
