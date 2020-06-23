/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.selection;

import fr.jmmc.oitools.model.OIData;

/**
 *
 * @author bourgesl
 */
public class OIDataPointer {

    /* member */
    /** data table */
    protected final OIData oiData;

    public OIDataPointer(final OIData oiData) {
        this.oiData = oiData;
    }

    public final OIData getOiData() {
        return oiData;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.oiData != null ? this.oiData.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof OIDataPointer)) {
            return false;
        }
        final OIDataPointer other = (OIDataPointer) obj;
        return !(this.oiData != other.getOiData() && (this.oiData == null || !this.oiData.equals(other.getOiData())));
    }

    @Override
    public String toString() {
        return "OIDataPointer{oidata: " + oiData
                + " ArrName: " + getArrName()
                + " InsName: " + getInsName()
                + " FileName: " + getOIFitsFileName() + '}';
    }

    public final String getArrName() {
        return oiData.getArrName();
    }

    public final String getInsName() {
        return oiData.getInsName();
    }

    public final String getOIFitsFileName() {
        return oiData.getOIFitsFile().getFileName();
    }
}
