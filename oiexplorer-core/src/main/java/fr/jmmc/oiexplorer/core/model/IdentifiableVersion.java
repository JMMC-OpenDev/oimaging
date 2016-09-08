/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model;

import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.jmcs.util.ToStringable;
import fr.jmmc.oiexplorer.core.model.oi.Identifiable;

/**
 *
 * @author bourgesl
 */
public final class IdentifiableVersion implements ToStringable {

    /* members */
    /** identifiable identifier */
    private final String id;
    /** version to track effective changes */
    private final int version;

    /**
     * Public Constructor
     * @param id identifiable identifier
     * @param version version
     */
    public IdentifiableVersion(final String id, final int version) {
        this.id = id;
        this.version = version;
    }

    /**
     * Return the identifiable identifier
     * @return identifiable identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Return the version
     * @return version
     */
    public int getVersion() {
        return version;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        // identity check:
        if (this == obj) {
            return true;
        }
        if (IdentifiableVersion.class == obj.getClass()) {
            final IdentifiableVersion other = (IdentifiableVersion) obj;

            if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId())) {
                return false;
            }
            if (this.version != other.getVersion()) {
                return false;
            }

        } else if (Identifiable.class.isAssignableFrom(obj.getClass())) {
            final Identifiable other = (Identifiable) obj;

            if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId())) {
                return false;
            }
            if (this.version != other.getVersion()) {
                return false;
            }

        } else {
            return false;
        }
        return true;
    }

    /**
     * toString() implementation wrapper to get complete information
     * Note: prefer using @see #toString(java.lang.StringBuilder) instead
     * @return string representation
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(32);
        toString(sb, true);
        return sb.toString();
    }

    /**
     * toString() implementation using string builder
     * 
     * Note: to be override in child classes to append their fields
     * 
     * @param sb string builder to append to
     * @param full true to get complete information; false to get main information (shorter)
     */
    @Override
    public void toString(final StringBuilder sb, final boolean full) {
        ObjectUtils.getObjectInfo(sb, this);
        sb.append("{id=").append(this.id);
        sb.append(", version=").append(this.version);
        sb.append('}');
    }
}
