/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model;

import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oitools.model.InstrumentMode;

/**
 *
 * @author bourgesl
 */
public final class InstrumentModeManager extends AbstractMapper<InstrumentMode> {

    /** smallest precision on wavelength */
    public final static float LAMBDA_PREC = 1e-10f;

    /** Singleton pattern */
    private final static InstrumentModeManager INSTANCE = new InstrumentModeManager();

    /**
     * Return the Manager singleton
     * @return singleton instance
     */
    public static InstrumentModeManager getInstance() {
        return INSTANCE;
    }

    /**
     * Clear the mappings
     */
    @Override
    public void clear() {
        super.clear();
        // insert mapping for Undefined:
        register(InstrumentMode.UNDEFINED);
    }

    @Override
    protected boolean match(final InstrumentMode src, final InstrumentMode other) {
        if (src == other) {
            return true;
        }
        // Compare all values:
        if (NumberUtils.compare(src.getNbChannels(), other.getNbChannels()) != 0) {
            return false;
        }

        // precision = 1/2 channel width ie min(eff_band)/2
        float prec = 0.5f * Math.min(src.getBandMin(), other.getBandMin());

        if (Float.isNaN(prec) || prec < LAMBDA_PREC) {
            prec = LAMBDA_PREC;
        }

        // precision = 1e-10 ie 3 digits in nm:
        if (!NumberUtils.equals(src.getLambdaMin(), other.getLambdaMin(), prec)) {
            return false;
        }
        if (!NumberUtils.equals(src.getLambdaMax(), other.getLambdaMax(), prec)) {
            return false;
        }
        return true;
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
