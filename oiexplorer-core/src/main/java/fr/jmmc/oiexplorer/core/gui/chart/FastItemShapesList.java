/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.chart;

import java.awt.Shape;
import org.jfree.util.ShapeUtilities;

/**
 * A table of {@link Shape[]} objects.
 *
 * @author bourgesl
 */
public final class FastItemShapesList extends FastAbstractObjectList {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;

    /**
     * Creates a new list.
     */
    public FastItemShapesList() {
        super();
    }

    /**
     * Returns a {@link Shape[]} object from the list.
     *
     * @param index the index (zero-based).
     *
     * @return The object.
     */
    public Shape[] getItemShapes(final int index) {
        return (Shape[]) get(index);
    }

    /**
     * Sets the {@link Shape[]} for an item in the list.  The list is expanded
     * if necessary.
     *
     * @param index  the index (zero-based).
     * @param itemShapes  the {@link Shape[]}.
     */
    public void setItemShapes(final int index, final Shape[] itemShapes) {
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
        if (!(obj instanceof FastItemShapesList)) {
            return false;
        }
        FastItemShapesList that = (FastItemShapesList) obj;
        int listSize = size();
        for (int i = 0; i < listSize; i++) {
            if (!equals((Shape[]) get(i), (Shape[]) that.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean equals(Shape[] a, Shape[] a2) {
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
            Shape o1 = a[i];
            Shape o2 = a2[i];
            if (!(o1 == null ? o2 == null : ShapeUtilities.equal(o1, o2))) {
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
    /**
     * Provides serialization support.
     *
     * @param stream  the output stream.
     *
     * @throws IOException  if there is an I/O error.
     */
    /*    
     private void writeObject(final ObjectOutputStream stream) throws IOException {

     stream.defaultWriteObject();
     final int count = size();
     stream.writeInt(count);
     for (int i = 0; i < count; i++) {
     final Shape shape = getItemShapes(i);
     if (shape != null) {
     stream.writeInt(i);
     SerialUtilities.writeShape(shape, stream);
     }
     else {
     stream.writeInt(-1);
     }
     }
     }
     */
    /**
     * Provides serialization support.
     *
     * @param stream  the input stream.
     *
     * @throws IOException  if there is an I/O error.
     * @throws ClassNotFoundException  if there is a classpath problem.
     */
    /*    
     private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {

     stream.defaultReadObject();
     final int count = stream.readInt();
     for (int i = 0; i < count; i++) {
     final int index = stream.readInt();
     if (index != -1) {
     setItemShapes(index, SerialUtilities.readShape(stream));
     }
     }
     }
     */
}
