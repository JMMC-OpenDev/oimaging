/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model.util;

/**
 * This enumeration associates a float value (YYYY.MM) to a version label
 * @author mella
 */
public enum OIExplorerModelVersion {

    /** initial revision */
    InitialRevision(2012.4f),
    /** OIFitsExplorer 0.1 (id / name) */
    April2013(2013.4f);

    /**
     * Return the current revision of the OIExplorer DM
     * @return Current revision of the OIExplorer DM
     */
    public static OIExplorerModelVersion getCurrentVersion() {
        return April2013;
    }

    /* members */
    /** version as a float value */
    private final float version;

    /**
     * Protected constructor
     * @param version as a float value
     */
    OIExplorerModelVersion(final float version) {
        this.version = version;
    }

    /**
     * Return the version as a float value
     * @return version as a float value
     */
    public float getVersion() {
        return this.version;
    }

    /**
     * Return the OIExplorerModelVersion corresponding to the given version.
     * It returns the initial revision if there is no matching revision
     * @param version version as a float value
     * @return OIExplorerModelVersion
     */
    public static OIExplorerModelVersion valueOf(final float version) {
        for (OIExplorerModelVersion rev : OIExplorerModelVersion.values()) {
            if (rev.getVersion() == version) {
                return rev;
            }
        }
        return InitialRevision;
    }
}
