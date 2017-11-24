/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

/**
 * A value-type (immutable) representing a single night in any OI_Data table
 * @author bourgesl
 */
public final class NightId {

    public final static NightId UNDEFINED = new NightId();

    /* nightId = rounded MJD */
    private int nightId;

    public NightId() {
        this(Integer.MAX_VALUE);
    }

    public NightId(final int nightId) {
        set(nightId);
    }

    public void set(final int nightId) {
        this.nightId = nightId;
    }

    public int getNightId() {
        return nightId;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.nightId;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NightId other = (NightId) obj;
        if (this.nightId != other.getNightId()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NightId{" + nightId + '}';
    }

}
