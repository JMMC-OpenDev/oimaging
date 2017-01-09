/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model;

import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.jmcs.util.ToStringable;
import fr.jmmc.oiexplorer.core.model.oi.TargetUID;
import fr.jmmc.oitools.model.Granule;
import fr.jmmc.oitools.model.InstrumentMode;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIWavelength;
import fr.jmmc.oitools.model.Target;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage data collection and provide utility methods.
 */
public final class OIFitsCollection implements ToStringable {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(OIFitsCollection.class);
    /** instrument mode manager */
    private final static InstrumentModeManager imm = InstrumentModeManager.getInstance();
    /** Target manager */
    private final static TargetManager tm = TargetManager.getInstance();
    /* members */
    /** OIFits file collection keyed by absolute file path */
    private final Map<String, OIFitsFile> oiFitsCollection = new HashMap<String, OIFitsFile>();
    /** fake OIFitsFile structure per TargetUID */
    private final Map<TargetUID, OIFitsFile> oiFitsPerTarget = new HashMap<TargetUID, OIFitsFile>();
    /** fake OIFitsFile structure (to gather OIData) keyed by global Granule */
    private final Map<Granule, OIFitsFile> oiFitsPerGranule = new HashMap<Granule, OIFitsFile>();

    /**
     * Protected constructor
     */
    protected OIFitsCollection() {
        super();
    }

    /**
     * Clear the OIFits file collection
     */
    public void clear() {
        // clear all loaded OIFitsFile (in memory):
        oiFitsCollection.clear();

        clearCache();
    }

    /**
     * Clear the cached meta-data
     */
    public void clearCache() {
        // clear OIFits structure per TargetUID:
        oiFitsPerTarget.clear();
        // clear granules:
        oiFitsPerGranule.clear();
        // clear insMode mappings:
        imm.clear();
        // clear Target mappings:
        tm.clear();
    }

    public boolean isEmpty() {
        return oiFitsCollection.isEmpty();
    }

    public List<OIFitsFile> getOIFitsFiles() {
        return new ArrayList<OIFitsFile>(oiFitsCollection.values());
    }

    /**
     * Add the given OIFits file to this collection
     * @param oifitsFile OIFits file
     * @return previous OIFits file or null if not present
     */
    OIFitsFile addOIFitsFile(final OIFitsFile oifitsFile) {
        if (oifitsFile != null) {
            final String key = getFilePath(oifitsFile);

            final OIFitsFile previous = getOIFitsFile(key);

            if (previous != null) {
                logger.warn("TODO: handle overwriting OIFitsFile : {}", key);
                removeOIFitsFile(previous);
            }

            oiFitsCollection.put(key, oifitsFile);

            // analyze the given file:
            oifitsFile.analyze();

            logger.debug("addOIFitsFile: {}", oifitsFile);

            return previous;
        }
        return null;
    }

    public OIFitsFile getOIFitsFile(final String absoluteFilePath) {
        if (absoluteFilePath != null) {
            return oiFitsCollection.get(absoluteFilePath);
        }
        return null;
    }

    OIFitsFile removeOIFitsFile(final OIFitsFile oifitsFile) {
        if (oifitsFile != null) {
            final String key = getFilePath(oifitsFile);
            final OIFitsFile previous = oiFitsCollection.remove(key);

            return previous;
        }
        return null;
    }

    private String getFilePath(final OIFitsFile oifitsFile) {
        if (oifitsFile.getAbsoluteFilePath() == null) {
            // TODO: remove asap
            throw new IllegalStateException("Undefined OIFitsFile.absoluteFilePath !");
        }
        return oifitsFile.getAbsoluteFilePath();
    }

    /**
     * toString() implementation wrapper to get complete information
     * Note: prefer using @see #toString(java.lang.StringBuilder) instead
     * @return string representation
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(256);
        toString(sb, OIBase.TO_STRING_VERBOSITY);
        return sb.toString();
    }

    /**
     * toString() implementation using string builder
     * 
     * @param sb string builder to append to
     * @param full true to get complete information; false to get main information (shorter)
     */
    @Override
    public void toString(final StringBuilder sb, final boolean full) {
        ObjectUtils.getObjectInfo(sb, this);

        sb.append("{files=").append(this.oiFitsCollection.keySet());

        if (full) {
            if (this.oiFitsPerTarget != null) {
                sb.append(", oiFitsPerTarget=");
                ObjectUtils.toString(sb, full, this.oiFitsPerTarget);
            }
        }
        sb.append('}');
    }

    /* --- data analysis --- */
    /**
     * Analyze the complete OIFits collection to provide OIFits structure per unique target (name)
     */
    public void analyzeCollection() {
        clearCache();
        
        // Always define mapping for Undefined values:
        imm.register(InstrumentMode.UNDEFINED);
        tm.register(Target.UNDEFINED);

        // analyze instrument modes & targets:
        for (OIFitsFile oiFitsFile : oiFitsCollection.values()) {
            for (OIWavelength oiTable : oiFitsFile.getOiWavelengths()) {
                imm.register(oiTable.getInstrumentMode());
            }

            if (oiFitsFile.hasOiTarget()) {
                for (Target target : oiFitsFile.getOiTarget().getTargetObjToTargetId().keySet()) {
                    tm.register(target);
                }
            }
        }

        imm.dump();
        tm.dump();

        // This can be replaced by an OIFits merger / filter that creates a new consistent OIFitsFile structure
        // from criteria (target / insname ...)
        for (OIFitsFile oiFitsFile : oiFitsCollection.values()) {

            // Note: per OIFits, multiple target ids may have the same target name !
            // take care !
            for (Map.Entry<String, List<OIData>> entry : oiFitsFile.getOiDataPerTarget().entrySet()) {

                final TargetUID target = new TargetUID(entry.getKey());

                // TODO: Cross Match on target RA/DEC because names ...
                OIFitsFile oiFitsTarget = oiFitsPerTarget.get(target);
                if (oiFitsTarget == null) {
                    oiFitsTarget = new OIFitsFile();

                    oiFitsPerTarget.put(target, oiFitsTarget);
                }

                for (OIData data : entry.getValue()) {
                    oiFitsTarget.addOiTable(data);
                }
            }

            // Granules:
            // reused Granule:
            Granule gg = new Granule();

            for (Map.Entry<Granule, Set<OIData>> entry : oiFitsFile.getOiDataPerGranule().entrySet()) {

                // Relations between global Granule and OIFits Granules ?
                final Granule g = entry.getKey();

                // create global granule with matching global target & instrument mode:
                final Target globalTarget = tm.getGlobal(g.getTarget());
                final InstrumentMode globalInsMode = imm.getGlobal(g.getInsMode());

                gg.set(globalTarget, globalInsMode, g.getNight());

                // TODO: keep mapping between global granule and OIFits Granules ?
                OIFitsFile oiFitsGranule = oiFitsPerGranule.get(gg);
                if (oiFitsGranule == null) {
                    oiFitsGranule = new OIFitsFile();

                    oiFitsPerGranule.put(gg, oiFitsGranule);
                    gg = new Granule();
                }

                for (OIData data : entry.getValue()) {
                    oiFitsGranule.addOiTable(data);
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("analyzeCollection:");

            for (Map.Entry<TargetUID, OIFitsFile> entry : oiFitsPerTarget.entrySet()) {
                logger.debug("{} : {}", entry.getKey(), entry.getValue().getOiDataList());
            }
            logger.debug("Granules: {}", oiFitsPerGranule.keySet());
        }
    }

    /** 
     * Return the OIFitsFile structure per target found in loaded files.
     * @return OIFitsFile structure per target
     */
    public Map<TargetUID, OIFitsFile> getOiFitsPerTarget() {
        return oiFitsPerTarget;
    }

    /** 
     * Return the OIFitsFile structure per Granule found in loaded files.
     * @return OIFitsFile structure per Granule
     */
    public Map<Granule, OIFitsFile> getOiFitsPerGranule() {
        return oiFitsPerGranule;
    }

    /**
     * Return the OIFitsFile structure corresponding to the given target (name) or null if missing
     * @param target targetUID
     * @return list of OIData tables corresponding to the given target (name) or null if missing
     */
    public OIFitsFile getOiFits(final TargetUID target) {

        // TODO: create an OIFits dynamically from filters (target...)
        return getOiFitsPerTarget().get(target);
    }
}
