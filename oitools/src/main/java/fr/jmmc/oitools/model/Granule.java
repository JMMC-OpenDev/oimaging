/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

/**
 * A value-type representing a granule = INSNAME (backend) + TARGET + NIGHT
 *
 * @author bourgesl
 */
public class Granule {

    public enum GranuleField {
        TARGET, INS_MODE, NIGHT;
    }

    private Target target;
    private InstrumentMode insMode;
    private Integer night;

    public Granule() {
        this(null, null, null);
    }

    public Granule(final Target target, final InstrumentMode insMode, final Integer night) {
        set(target, insMode, night);
    }

    public void set(final Target target, final InstrumentMode insMode, final Integer night) {
        this.target = target;
        this.insMode = insMode;
        this.night = night;
    }

    public Target getTarget() {
        return target;
    }

    public InstrumentMode getInsMode() {
        return insMode;
    }

    public Integer getNight() {
        return night;
    }

    public Object getField(GranuleField field) {
        switch (field) {
            case TARGET:
                return getTarget();
            case INS_MODE:
                return getInsMode();
            case NIGHT:
                return getNight();
            default:
                return null;
        }
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.target != null ? this.target.hashCode() : 0);
        hash = 67 * hash + (this.insMode != null ? this.insMode.hashCode() : 0);
        hash = 67 * hash + (this.night != null ? this.night.hashCode() : 0);
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
        final Granule other = (Granule) obj;
        if (this.target != other.target && (this.target == null || !this.target.equals(other.target))) {
            return false;
        }
        if (this.insMode != other.insMode && (this.insMode == null || !this.insMode.equals(other.insMode))) {
            return false;
        }
        if (this.night != other.night && (this.night == null || !this.night.equals(other.night))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Granule{" + "target=" + target + ", insMode=" + insMode + ", night=" + night + '}';
    }

}
