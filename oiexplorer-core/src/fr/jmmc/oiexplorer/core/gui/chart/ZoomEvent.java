/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.chart;

/**
 *
 * @author bourgesl
 */
public final class ZoomEvent {

    /** domain lower bound */
    private final double domainLowerBound;
    /** domain Upper bound */
    private final double domainUpperBound;
    /** range lower bound */
    private final double rangeLowerBound;
    /** range Upper bound */
    private final double rangeUpperBound;

    public ZoomEvent(
            final double domainLowerBound,
            final double domainUpperBound,
            final double rangeLowerBound,
            final double rangeUpperBound) {
        this.domainLowerBound = domainLowerBound;
        this.domainUpperBound = domainUpperBound;
        this.rangeLowerBound = rangeLowerBound;
        this.rangeUpperBound = rangeUpperBound;
    }

    public double getDomainLowerBound() {
        return domainLowerBound;
    }

    public double getDomainUpperBound() {
        return domainUpperBound;
    }

    public double getRangeLowerBound() {
        return rangeLowerBound;
    }

    public double getRangeUpperBound() {
        return rangeUpperBound;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ZoomEvent other = (ZoomEvent) obj;
        if (this.domainLowerBound != other.domainLowerBound) {
            return false;
        }
        if (this.domainUpperBound != other.domainUpperBound) {
            return false;
        }
        if (this.rangeLowerBound != other.rangeLowerBound) {
            return false;
        }
        if (this.rangeUpperBound != other.rangeUpperBound) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }

    /**
     * Return string representation
     * @return string representation
     */
    @Override
    public String toString() {
        return "ZoomEvent X[" + domainLowerBound + ", " + domainUpperBound + "], Y[" + rangeLowerBound + ", " + rangeUpperBound + "]";
    }
}
