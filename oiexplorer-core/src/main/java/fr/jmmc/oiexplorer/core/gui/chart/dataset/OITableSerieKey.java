/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.chart.dataset;

import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oiexplorer.core.gui.selection.DataPointer;

/**
 *
 * @author bourgesl
 */
public final class OITableSerieKey implements java.io.Serializable, Comparable<OITableSerieKey> {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;

    /* member */
 /* generated table index (ensure key uniqueness) */
    private final int tableIndex;
    /* origin of the data (OIData + col) */
    private final DataPointer ptr;
    /* StaIndex index */
    private final int staIdxIndex;
    /* keys used for global series attributes */
    private final String staIndexName;
    private final String staConfName;

    public OITableSerieKey(final int tableIndex, final DataPointer ptr, final int staIdxIndex, final int waveLengthIndex,
                           final String staIndexName, final String staConfName) {
        this.tableIndex = tableIndex;
        this.ptr = ptr;
        this.staIdxIndex = staIdxIndex;

        this.staIndexName = staIndexName;
        this.staConfName = staConfName;
    }

    @Override
    public int compareTo(final OITableSerieKey key) {
        int res = NumberUtils.compare(tableIndex, key.getTableIndex());
        if (res == 0) {
            res = NumberUtils.compare(staIdxIndex, key.getStaIdxIndex());
            if (res == 0) {
                res = NumberUtils.compare(getWaveLengthIndex(), key.getWaveLengthIndex());
            }
        }
        return res;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 13 * hash + this.tableIndex;
        hash = 31 * hash + this.staIdxIndex;
        hash = 67 * hash + this.getWaveLengthIndex();
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
        if (this.staIdxIndex != other.getStaIdxIndex()) {
            return false;
        }
        if (this.getWaveLengthIndex() != other.getWaveLengthIndex()) {
            return false;
        }
        return true;
    }

    public int getTableIndex() {
        return tableIndex;
    }

    public DataPointer getDataPointer() {
        return ptr;
    }

    public int getStaIdxIndex() {
        return staIdxIndex;
    }

    public int getWaveLengthIndex() {
        return ptr.getCol();
    }

    @Override
    public String toString() {
        return "#" + tableIndex + " B" + staIdxIndex + " W" + getWaveLengthIndex()
                + " pointer: " + ptr
                + " staIndexName: " + getStaIndexName()
                + " staConfName: " + getStaConfName();
    }

    public String getStaIndexName() {
        return staIndexName;
    }

    public String getStaConfName() {
        return staConfName;
    }

}
