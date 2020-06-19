/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model.util;

import fr.jmmc.jmal.ALX;
import static fr.jmmc.jmal.ALX.DEG_IN_HOUR;

/**
 * Converts MJD to String "date[ time]"
 * @author bourgesl
 */
public final class MJDConverter {

    /** Modified Juliean day reference */
    public final static double MJD_REF = 2400000.5d;
    public static final double DAY_IN_YEAR = 1d / 365.25d;
    /** MJD starting at 1-JAN-2000 00:00 UT */
    public final static double MJD2000 = 51544.5d;

    public static StringBuilder mjdToString(final int mjd, final StringBuilder sb) {
        return jdToString(mjd + MJD_REF, sb, false);
    }

    // From JSkyCalc#GenericCalDat.calFromJD()
    public static StringBuilder jdToString(final double jd, final StringBuilder sb, final boolean time) {
        /* sets the calendar date using the current values of jd -- can
         be either a local or UT date */

 /* Adapted from J. Meeus,  Astronomical Formulae for Calculators,
         published by Willman-Bell Inc.
         Avoids a copyrighted routine from Numerical Recipes.
         Tested and works properly from the beginning of the
         calendar era (1583) to beyond 3000 AD. */
        double tmp;
        long alpha;
        long Z, A, B, C, D, E;
        double F;
        boolean rounded_ok = false;

        int day = 0, month = 0, year = 0;
        double timeofday = 0f;

        double jdin = jd;

        while (!rounded_ok) {
            tmp = jdin + 0.5d;
            Z = (long) tmp;

            F = tmp - Z;
            if (Z < 2299161d) {
                A = Z;
            } else {
                alpha = (long) ((Z - 1867216.25d) / 36524.25d);
                A = Z + 1l + alpha - (long) (alpha / 4d);
            }

            B = A + 1524l;
            C = (long) ((B - 122.1d) * DAY_IN_YEAR);
            D = (long) (365.25d * C);
            E = (long) ((B - D) / 30.6001d);

            day = (int) (B - D - (long) (30.6001d * E));
            if (E < 13.5d) {
                month = (int) (E - 1l);
            } else {
                month = (int) (E - 13l);
            }
            if (month > 2.5d) {
                year = (int) (C - 4716l);
            } else {
                year = (int) (C - 4715l);
            }

            // LBO: reduce memory footprint
            timeofday = 24d * F;

            if (timeofday >= 24f) {
                jdin += 1.0e-7d; // near to the resolution of the double
            } else {
                rounded_ok = true;
            }
        }

        // date as "yyyy/mm/dd"
        sb.append(year).append('/');
        if (month < 10) {
            sb.append('0');
        }
        sb.append(month).append('/');
        if (day < 10) {
            sb.append('0');
        }
        sb.append(day);

        if (time) {
            sb.append(' ');
            ALX.toHMS(sb, timeofday * DEG_IN_HOUR);
        }
        return sb;
    }

    private MJDConverter() {
        super();
    }

    public static void main(String[] args) {
        final StringBuilder sb = new StringBuilder();

        sb.append("MJD0: ");
        mjdToString(0, sb).append('\n');

        sb.append("MJD2000: ");
        mjdToString((int) MJD2000, sb).append('\n');

        System.out.println(sb.toString());
    }
}
