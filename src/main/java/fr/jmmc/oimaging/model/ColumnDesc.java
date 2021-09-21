/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ***************************************************************************** */
package fr.jmmc.oimaging.model;

import java.util.Objects;

/** describes a column in a table. */
public class ColumnDesc {

    /** undefined source value = -1 */
    public static final int SOURCE_UNDEFINED = -1;

    /** name is used for equality */
    private final String name;

    /** class of the elements in the columns */
    private Class<?> dataClass;

    /** information about the source of the column.
     * For example, from a ServiceResult, there is Input and Output sources 
     * it is an `int` to make it more generic 
     */
    private final int source;

    /** Prettier name for GUI display */
    private final String label;

    /** Informative longer text for GUI display (tooltips, etc) */
    private final String description;

    public ColumnDesc(String name, Class<?> dataClass) {
        this(name, dataClass, SOURCE_UNDEFINED, null, null);
    }

    public ColumnDesc(String name, Class<?> dataClass, int source) {
        this(name, dataClass, source, null, null);
    }

    public ColumnDesc(String name, Class<?> dataClass, int source, String label) {
        this(name, dataClass, source, label, null);
    }

    public ColumnDesc(String name, Class<?> dataClass, int source, String label, String description) {
        this.name = name;
        this.dataClass = dataClass;
        this.source = source;
        this.label = label;
        this.description = description;
    }

    public int getSource() {
        return source;
    }

    public String getName() {
        return name;
    }

    public Class<?> getDataClass() {
        return dataClass;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    /** 
     * Equality is only on name 
     * @param otherObject another instance
     */
    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }
        if (otherObject == null) {
            return false;
        }
        if (getClass() != otherObject.getClass()) {
            return false;
        }
        ColumnDesc other = (ColumnDesc) otherObject;
        return getName().equals(other.getName());
    }

    /** hashCode that only use getName() to hash. consistent with equals. */
    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }

    @Override
    public String toString() {
        return getName();
    }
}
