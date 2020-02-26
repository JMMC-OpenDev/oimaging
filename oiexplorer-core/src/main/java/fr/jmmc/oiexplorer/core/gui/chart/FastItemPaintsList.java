/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.chart;

import java.awt.Paint;
import org.jfree.chart.util.PaintUtils;

/**
 * A table of {@link Paint[]} objects.
 *
 * @author bourgesl
 */
public final class FastItemPaintsList extends FastAbstractObjectList {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;

    /**
     * Creates a new list.
     */
    public FastItemPaintsList() {
        super();
    }

    /**
     * Returns a {@link Paint[]} object from the list.
     *
     * @param index the index (zero-based).
     *
     * @return The object.
     */
    public Paint[] getItemPaints(final int index) {
        return (Paint[]) get(index);
    }

    /**
     * Sets the {@link Paint[]} for an item in the list.  The list is expanded
     * if necessary.
     *
     * @param index  the index (zero-based).
     * @param itemShapes  the {@link Paint[]}.
     */
    public void setItemPaints(final int index, final Paint[] itemShapes) {
        set(index, itemShapes);
    }

    /**
     * Returns an independent copy of the list.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if an item in the list does not
     *         support cloning.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Tests the list for equality with another object (typically also a list).
     *
     * @param obj  the other object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FastItemPaintsList)) {
            return false;
        }
        FastItemPaintsList that = (FastItemPaintsList) obj;
        int listSize = size();
        for (int i = 0; i < listSize; i++) {
            if (!equals((Paint[]) get(i), (Paint[]) that.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean equals(Paint[] a, Paint[] a2) {
        if (a == a2) {
            return true;
        }
        if (a == null || a2 == null) {
            return false;
        }

        int length = a.length;
        if (a2.length != length) {
            return false;
        }

        for (int i = 0; i < length; i++) {
            Paint o1 = a[i];
            Paint o2 = a2[i];
            if (!(o1 == null ? o2 == null : PaintUtils.equal(o1, o2))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return the hashcode
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
