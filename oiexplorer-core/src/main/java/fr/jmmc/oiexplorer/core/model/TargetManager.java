/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model;

import fr.jmmc.jmal.ALX;
import fr.jmmc.jmal.CoordUtils;
import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oitools.model.Target;

/**
 *
 * @author bourgesl
 */
public class TargetManager extends AbstractMapper<Target> {

    /** distance in degrees to consider same targets = 1 arcsecs */
    public final static double SAME_TARGET_DISTANCE = 1d * ALX.ARCSEC_IN_DEGREES;

    /** Singleton pattern */
    private final static TargetManager instance = new TargetManager();

    /**
     * Return the Manager singleton
     * @return singleton instance
     */
    public static TargetManager getInstance() {
        return instance;
    }

    @Override
    protected boolean match(final Target src, final Target other) {
        // only check ra/dec:
        final double distance = CoordUtils.computeDistanceInDegrees(src.getRaEp0(), src.getDecEp0(),
                other.getRaEp0(), other.getDecEp0());

        final boolean match = (distance <= SAME_TARGET_DISTANCE);

        if (match) {
            logger.info("match [{} vs {}] = {} arcsec",
                    src.getTarget(), other.getTarget(),
                    NumberUtils.trimTo3Digits(distance * ALX.DEG_IN_ARCSEC)
            );
        }

        return match;
    }

    @Override
    protected Target createGlobal(final Target local) {
        return new Target(local);
    }

    @Override
    protected String getName(final Target src) {
        return src.getTarget();
    }
}
