/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.fits.FitsHDU;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Fix row/channel to be 1-based (not 0-based)
 * @author kempsc
 */
public final class OIFitsCorrChecker {

    private final Map<Integer, Origin> indexOrigins = new HashMap<Integer, Origin>();

    public boolean contains(final Integer index) {
        return indexOrigins.containsKey(index);
    }

    // Puts in the map at the given index, the new object Origin with OIData information
    public void put(final Integer index, String extName, int extNb, String column, int row, int channel) {
        indexOrigins.put(index, new Origin(extName, extNb, column, row, channel));
    }

    // Origin display call asString in Origin object
    public String getOriginAsString(final Integer index) {
        return indexOrigins.get(index).asString();
    }

    // Classic toString call Origin toString
    @Override
    public String toString() {
        return "CorrChecker{" + "usedIndexes=" + indexOrigins + '}';
    }

    //Origin object for store OIData information for an index
    private final static class Origin {

        String extName;
        int extNb;
        String column;
        int row;
        int channel;

        Origin(String extName, int extNb, String column, int row, int channel) {
            this.extName = extName;
            this.extNb = extNb;
            this.column = column;
            this.row = row;
            this.channel = channel;
        }

        @Override
        public String toString() {
            return "Origin{" + "extName=" + extName + ", extNb=" + extNb + ", column=" + column + ", row=" + row + ", channel=" + channel + '}';
        }

        private String asString() {
            return FitsHDU.getHDUId(extName, extNb)
                    + " in " + column + " column at [row=" + row + ", wlen=" + channel + ']';
        }

    }
}
