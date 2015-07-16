/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.chart.dataset;

/**
 *
 * @author bourgesl
 */
public final class OITableSerieKey implements java.io.Serializable, Comparable<OITableSerieKey> {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;

    /* member */
    /* table index */
    private final int tableIndex;
    /* baseline index */
    private final int baseline;
    /* waveLength index */
    private final int waveLength;

    public OITableSerieKey(final int tableIndex, final int baseline, final int waveLength) {
        this.tableIndex = tableIndex;
        this.baseline = baseline;
        this.waveLength = waveLength;
    }

    @Override
    public int compareTo(final OITableSerieKey o) {
        int res = Integer.compare(tableIndex, o.getTableIndex());
        if (res == 0) {
            res = Integer.compare(baseline, o.getBaseline());
            if (res == 0) {
                res = Integer.compare(waveLength, o.getWaveLength());
            }
        }
        return res;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + this.tableIndex;
        hash = 31 * hash + this.baseline;
        hash = 67 * hash + this.waveLength;
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OITableSerieKey other = (OITableSerieKey) obj;
        if (this.tableIndex != other.getTableIndex()) {
            return false;
        }
        if (this.baseline != other.getBaseline()) {
            return false;
        }
        if (this.waveLength != other.getWaveLength()) {
            return false;
        }
        return true;
    }

    public int getTableIndex() {
        return tableIndex;
    }

    public int getBaseline() {
        return baseline;
    }

    public int getWaveLength() {
        return waveLength;
    }

    @Override
    public String toString() {
        return "#" + tableIndex + " B" + baseline + " W" + waveLength;
    }
}
