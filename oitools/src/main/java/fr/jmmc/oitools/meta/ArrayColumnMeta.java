/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.meta;

/**
 * This specific ColumnMeta indicates this column is always giving an array (2D)
 * @author bourgesl
 */
public class ArrayColumnMeta extends ColumnMeta {

    /**
     * ArrayColumnMeta class constructor with the given cardinality
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     * @param repeat column cardinality
     * @param optional
     */
    public ArrayColumnMeta(final String name, final String desc, final Types dataType, final int repeat, final boolean optional) {
        super(name, desc, dataType, repeat, optional);
    }

    /**
     * ArrayColumnMeta class constructor with the given cardinality and unit
     *
     * @param name column name
     * @param desc column descriptive comment
     * @param dataType column data type
     * @param repeat column cardinality
     * @param unit column unit
     * @param errName optional column name storing error values (may be null)
     * @param acceptedValues
     * @param dataRange optional data range (may be null)
     * @param optional
     * @param is3D
     */
    public ArrayColumnMeta(final String name, final String desc, final Types dataType, final int repeat, final boolean optional, final boolean is3D,
            final String[] acceptedValues, final Units unit, final String errName, final DataRange dataRange) {
        super(name, desc, dataType, repeat, optional, is3D, acceptedValues, unit, errName, dataRange);
    }

}
