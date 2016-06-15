/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

/**
 * A value-type (immutable) representing a single target in any OI_Target table
 * @author bourgesl
 */
public class Target {

    public final static Target UNDEFINED = new Target("UNDEFINED", Double.NaN, Double.NaN,
            Float.NaN, Double.NaN, Double.NaN,
            Double.NaN, "", "", Double.NaN, Double.NaN, Double.NaN, Double.NaN,
            Float.NaN, Float.NaN, "");

    private final String target;
    private final double raEp0;
    private final double decEp0;
    private final float equinox;
    private final double raErr;
    private final double decErr;
    private final double sysvel;
    private final String velTyp;
    private final String velDef;
    private final double pmRa;
    private final double pmDec;
    private final double pmRaErr;
    private final double pmDecErr;
    private final float parallax;
    private final float paraErr;
    private final String specTyp;
    // cached precomputed hashcode:
    private int hashcode = 0;

    public Target(final String target,
                  final double raEp0,
                  final double decEp0,
                  final float equinox,
                  final double raErr,
                  final double decErr,
                  final double sysvel,
                  final String velTyp,
                  final String velDef,
                  final double pmRa,
                  final double pmDec,
                  final double pmRaErr,
                  final double pmDecErr,
                  final float parallax,
                  final float paraErr,
                  final String specTyp) {
        this.target = target;
        this.raEp0 = raEp0;
        this.decEp0 = decEp0;
        this.equinox = equinox;
        this.raErr = raErr;
        this.decErr = decErr;
        this.sysvel = sysvel;
        this.velTyp = velTyp;
        this.velDef = velDef;
        this.pmRa = pmRa;
        this.pmDec = pmDec;
        this.pmRaErr = pmRaErr;
        this.pmDecErr = pmDecErr;
        this.parallax = parallax;
        this.paraErr = paraErr;
        this.specTyp = specTyp;
    }

    public Target(final Target t) {
        this(t.getTarget(), t.getRaEp0(), t.getDecEp0(),
                t.getEquinox(), t.getRaErr(), t.getDecErr(),
                t.getSysVel(), t.getVelTyp(), t.getVelDef(),
                t.getPmRa(), t.getPmDec(),
                t.getPmRaErr(), t.getPmDecErr(),
                t.getParallax(), t.getParaErr(),
                t.getSpecTyp());
    }

    /**
     * Get TARGET value.
     * 
     * @return TARGET
     */
    public final String getTarget() {
        return target;
    }

    /**
     * Get RAEP0 value.
     * @return RAEP0.
     */
    public final double getRaEp0() {
        return raEp0;
    }

    /**
     * Get DECEP0 value.
     * @return DECEP0.
     */
    public final double getDecEp0() {
        return decEp0;
    }

    /**
     * Get EQUINOX value.
     * @return EQUINOX.
     */
    public final float getEquinox() {
        return equinox;
    }

    /**
     * Get RA_ERR value.
     * @return RA_ERR.
     */
    public final double getRaErr() {
        return raErr;
    }

    /**
     * Get DEC_ERR value.
     * @return DEC_ERR.
     */
    public final double getDecErr() {
        return decErr;
    }

    /**
     * Get SYSVEL value.
     * @return SYSVEL.
     */
    public final double getSysVel() {
        return sysvel;
    }

    /**
     * Get VELTYP value.
     * @return VELTYP
     */
    public final String getVelTyp() {
        return velTyp;
    }

    /**
     * Get VELDEF value.
     * @return VELDEF
     */
    public final String getVelDef() {
        return velDef;
    }

    /**
     * Get PMRA value.
     * @return PMRA.
     */
    public final double getPmRa() {
        return pmRa;
    }

    /**
     * Get PMDEC value.
     * @return PMDEC.
     */
    public final double getPmDec() {
        return pmDec;
    }

    /**
     * Get PMRA_ERR value.
     * @return PMRA_ERR.
     */
    public final double getPmRaErr() {
        return pmRaErr;
    }

    /**
     * Get PMDEC_ERR value.
     * @return PMDEC_ERR.
     */
    public final double getPmDecErr() {
        return pmDecErr;
    }

    /**
     * Get PARALLAX value.
     * @return PARALLAX.
     */
    public final float getParallax() {
        return parallax;
    }

    /**
     * Get PARA_ERR value.
     * @return PARA_ERR.
     */
    public final float getParaErr() {
        return paraErr;
    }

    /**
     * Get SPECTYP value.
     * @return SPECTYP
     */
    public final String getSpecTyp() {
        return specTyp;
    }

    @Override
    public int hashCode() {
        if (hashcode != 0) {
            // use precomputed hash code (or equals 0 but low probability):
            return hashcode;
        }
        int hash = 7;
        hash = 11 * hash + (this.target != null ? this.target.hashCode() : 0);
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.raEp0) ^ (Double.doubleToLongBits(this.raEp0) >>> 32));
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.decEp0) ^ (Double.doubleToLongBits(this.decEp0) >>> 32));
        hash = 11 * hash + Float.floatToIntBits(this.equinox);
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.raErr) ^ (Double.doubleToLongBits(this.raErr) >>> 32));
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.decErr) ^ (Double.doubleToLongBits(this.decErr) >>> 32));
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.sysvel) ^ (Double.doubleToLongBits(this.sysvel) >>> 32));
        hash = 11 * hash + (this.velTyp != null ? this.velTyp.hashCode() : 0);
        hash = 11 * hash + (this.velDef != null ? this.velDef.hashCode() : 0);
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.pmRa) ^ (Double.doubleToLongBits(this.pmRa) >>> 32));
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.pmDec) ^ (Double.doubleToLongBits(this.pmDec) >>> 32));
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.pmRaErr) ^ (Double.doubleToLongBits(this.pmRaErr) >>> 32));
        hash = 11 * hash + (int) (Double.doubleToLongBits(this.pmDecErr) ^ (Double.doubleToLongBits(this.pmDecErr) >>> 32));
        hash = 11 * hash + Float.floatToIntBits(this.parallax);
        hash = 11 * hash + Float.floatToIntBits(this.paraErr);
        hash = 11 * hash + (this.specTyp != null ? this.specTyp.hashCode() : 0);
        // cache hash code:
        hashcode = hash;
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
        final Target other = (Target) obj;
        if (Double.doubleToLongBits(this.raEp0) != Double.doubleToLongBits(other.raEp0)) {
            return false;
        }
        if (Double.doubleToLongBits(this.decEp0) != Double.doubleToLongBits(other.decEp0)) {
            return false;
        }
        if (Float.floatToIntBits(this.equinox) != Float.floatToIntBits(other.equinox)) {
            return false;
        }
        if (Double.doubleToLongBits(this.raErr) != Double.doubleToLongBits(other.raErr)) {
            return false;
        }
        if (Double.doubleToLongBits(this.decErr) != Double.doubleToLongBits(other.decErr)) {
            return false;
        }
        if (Double.doubleToLongBits(this.sysvel) != Double.doubleToLongBits(other.sysvel)) {
            return false;
        }
        if (Double.doubleToLongBits(this.pmRa) != Double.doubleToLongBits(other.pmRa)) {
            return false;
        }
        if (Double.doubleToLongBits(this.pmDec) != Double.doubleToLongBits(other.pmDec)) {
            return false;
        }
        if (Double.doubleToLongBits(this.pmRaErr) != Double.doubleToLongBits(other.pmRaErr)) {
            return false;
        }
        if (Double.doubleToLongBits(this.pmDecErr) != Double.doubleToLongBits(other.pmDecErr)) {
            return false;
        }
        if (Float.floatToIntBits(this.parallax) != Float.floatToIntBits(other.parallax)) {
            return false;
        }
        if (Float.floatToIntBits(this.paraErr) != Float.floatToIntBits(other.paraErr)) {
            return false;
        }
        if ((this.target == null) ? (other.target != null) : !this.target.equals(other.target)) {
            return false;
        }
        if ((this.velTyp == null) ? (other.velTyp != null) : !this.velTyp.equals(other.velTyp)) {
            return false;
        }
        if ((this.velDef == null) ? (other.velDef != null) : !this.velDef.equals(other.velDef)) {
            return false;
        }
        if ((this.specTyp == null) ? (other.specTyp != null) : !this.specTyp.equals(other.specTyp)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Target{" + "target=" + target + ", raEp0=" + raEp0 + ", decEp0=" + decEp0 + ", equinox=" + equinox + ", raErr=" + raErr + ", decErr=" + decErr + ", sysvel=" + sysvel + ", velTyp=" + velTyp + ", velDef=" + velDef + ", pmRa=" + pmRa + ", pmDec=" + pmDec + ", pmRaErr=" + pmRaErr + ", pmDecErr=" + pmDecErr + ", parallax=" + parallax + ", paraErr=" + paraErr + ", specTyp=" + specTyp + '}';
    }

}
