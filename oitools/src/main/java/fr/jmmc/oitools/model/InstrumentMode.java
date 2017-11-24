/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

/**
 * A value-type (immutable) representing a single backend (INSNAME) = 1 OI_Wavelength table
 * @author bourgesl
 */
public final class InstrumentMode {

    public final static InstrumentMode UNDEFINED = new InstrumentMode("UNDEFINED", 0, Float.NaN, Float.NaN, Float.NaN, Float.NaN);

    // should add EFF_WAVE/EFF_BAND arrays ?
    private final String insName;
    private final int nbChannels;
    private final float lambdaMin;
    private final float lambdaMax;
    private final float resPower;
    private final float bandMin;
    // cached precomputed hashcode:
    private int hashcode = 0;

    public InstrumentMode(final String insName,
                          final int nbChannels,
                          final float lambdaMin,
                          final float lambdaMax,
                          final float resPower,
                          final float bandMin) {
        this.insName = insName;
        this.nbChannels = nbChannels;
        this.lambdaMin = lambdaMin;
        this.lambdaMax = lambdaMax;
        this.resPower = resPower;
        this.bandMin = bandMin;
    }

    public InstrumentMode(final InstrumentMode im) {
        this(im.getInsName(), im.getNbChannels(),
                im.getLambdaMin(), im.getLambdaMax(), im.getResPower(), im.getBandMin());
    }

    public String getInsName() {
        return insName;
    }

    public int getNbChannels() {
        return nbChannels;
    }

    public float getLambdaMin() {
        return lambdaMin;
    }

    public float getLambdaMax() {
        return lambdaMax;
    }

    public float getResPower() {
        return resPower;
    }

    public float getBandMin() {
        return bandMin;
    }

    @Override
    public int hashCode() {
        if (hashcode != 0) {
            // use precomputed hash code (or equals 0 but low probability):
            return hashcode;
        }
        int hash = 7;
        hash = 59 * hash + (this.insName != null ? this.insName.hashCode() : 0);
        hash = 59 * hash + this.nbChannels;
        hash = 59 * hash + Float.floatToIntBits(this.lambdaMin);
        hash = 59 * hash + Float.floatToIntBits(this.lambdaMax);
        hash = 59 * hash + Float.floatToIntBits(this.resPower);
        hash = 59 * hash + Float.floatToIntBits(this.bandMin);
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
        final InstrumentMode other = (InstrumentMode) obj;
        if (this.nbChannels != other.getNbChannels()) {
            return false;
        }
        if (Float.floatToIntBits(this.lambdaMin) != Float.floatToIntBits(other.getLambdaMin())) {
            return false;
        }
        if (Float.floatToIntBits(this.lambdaMax) != Float.floatToIntBits(other.getLambdaMax())) {
            return false;
        }
        if (Float.floatToIntBits(this.resPower) != Float.floatToIntBits(other.getResPower())) {
            return false;
        }
        if (Float.floatToIntBits(this.bandMin) != Float.floatToIntBits(other.getBandMin())) {
            return false;
        }
        if ((this.insName == null) ? (other.getInsName() != null) : !this.insName.equals(other.getInsName())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "InstrumentMode{" + "insName=" + insName + ", nbChannels=" + nbChannels + ", lambdaMin=" + lambdaMin + ", lambdaMax=" + lambdaMax
                + ", resPower=" + resPower + ", bandMin=" + bandMin + '}';
    }

}
