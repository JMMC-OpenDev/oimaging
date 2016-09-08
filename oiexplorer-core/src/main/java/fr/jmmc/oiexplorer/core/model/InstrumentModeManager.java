/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model;

import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oitools.model.InstrumentMode;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author bourgesl
 */
public final class InstrumentModeManager extends AbstractMapper<InstrumentMode> {

    /** Singleton pattern */
    private final static InstrumentModeManager instance = new InstrumentModeManager();
    /** cache locals for Undefined InstrumentMode */
    private final static List<InstrumentMode> UNDEFINED_LOCALS = Arrays.asList(new InstrumentMode[]{InstrumentMode.UNDEFINED});

    /**
     * Return the Manager singleton
     * @return singleton instance
     */
    public static InstrumentModeManager getInstance() {
        return instance;
    }

    /**
     * Clear the mappings
     */
    public void clear() {
        super.clear();
        // insert mapping for Undefined:
        globalPerLocal.put(InstrumentMode.UNDEFINED, InstrumentMode.UNDEFINED);
        localsPerGlobal.put(InstrumentMode.UNDEFINED, UNDEFINED_LOCALS);
    }

    @Override
    protected boolean match(final InstrumentMode src, final InstrumentMode other) {
        int cmp = NumberUtils.compare(src.getNbChannels(), other.getNbChannels());
        if (cmp != 0) {
            return false;
        }
        // precision ?
        cmp = Float.compare(src.getLambdaMin(), other.getLambdaMin());
        if (cmp != 0) {
            return false;
        }
        cmp = Float.compare(src.getLambdaMax(), other.getLambdaMax());
        if (cmp != 0) {
            return false;
        }
        cmp = Float.compare(src.getResPower(), other.getResPower());

        return (cmp == 0);
    }

    @Override
    protected InstrumentMode createGlobal(final InstrumentMode local) {
        return new InstrumentMode(local);
    }

    @Override
    protected String getName(final InstrumentMode src) {
        return src.getInsName();
    }
}
