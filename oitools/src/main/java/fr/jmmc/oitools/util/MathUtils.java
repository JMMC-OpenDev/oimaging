/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.util;

/**
 * Provides several norm functions (2D and 3D)
 * 
 * @author bourgesl
 */
public final class MathUtils {

    private MathUtils() {
        // no-op
    }

    /**
     * Return the carthesian norm i.e. square root of square sum
     * @param x x value
     * @param y y value
     * @return SQRT(x^2 + y^2)
     */
    public static double carthesianNorm(final double x, final double y) {
        return Math.sqrt(x * x + y * y);
    }

    /**
     * Return the carthesian norm i.e. square root of square sum
     * @param x x value
     * @param y y value
     * @param z z value
     * @return SQRT(x^2 + y^2 + z^2)
     */
    public static double carthesianNorm(final double x, final double y, final double z) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Convert cartesian coordinates to spherical coordinates (radians,radians,meters)
     * @param position XYZ Geocentric coordinates (m)
     * @return longitude, latitude in radians and distance (m)
     */
    public static double[] cartesianToSpherical(final double[] position) {
        final double x = position[0];
        final double y = position[1];
        final double z = position[2];

        final double rxy2 = x * x + y * y;
        final double rxy = Math.sqrt(rxy2);

        double a;
        double b;
        if (rxy2 != 0d) {
            a = Math.atan2(y, x);
            b = Math.atan2(z, rxy);
        } else {
            a = 0d;
            b = (z == 0d) ? 0d : Math.atan2(z, rxy);
        }
        final double r = Math.sqrt(rxy2 + z * z);

        return new double[]{a, b, r};
    }
}
