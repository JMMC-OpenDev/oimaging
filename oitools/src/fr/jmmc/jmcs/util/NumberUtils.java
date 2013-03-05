/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.jmcs.util;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;

/**
 * This class is copied from Jmcs (same package) in order to let OITools compile properly 
 * but at runtime only one NumberUtils class will be loaded (by class loader)
 * 
 * Note: Jmcs Changes must be reported here to avoid compile issues !
 * 
 * 
 * This class handles double number comparisons with absolute error and number helper methods
 * http://www.cygnus-software.com/papers/comparingfloats/comparingfloats.htm
 *
 * @author bourgesl
 */
public final class NumberUtils {

    /**
     * Smallest positive number used in double comparisons (rounding).
     */
    public final static double EPSILON = 1e-6d;
    /** default formatter */
    private final static NumberFormat fmtDef = NumberFormat.getInstance();
    /** scientific formatter */
    private final static NumberFormat fmtScience = new DecimalFormat("0.0##E0");
    /** formatter string buffer argument */
    private final static StringBuffer fmtBuffer = new StringBuffer(32);
    /** ignore formatter position argument */
    private final static FieldPosition ignorePosition = new FieldPosition(0);

    /**
     * Private constructor
     */
    private NumberUtils() {
        super();
    }

    /**
     * Adjust the given double value to keep only 3 decimal digits
     * @param value value to adjust
     * @return double value with only 3 decimal digits
     */
    public static double trimTo3Digits(final double value) {
        return ((long) (1e3d * value)) / 1e3d;
    }

    /**
     * Adjust the given double value to keep only 5 decimal digits
     * @param value value to adjust
     * @return double value with only 5 decimal digits
     */
    public static double trimTo5Digits(final double value) {
        return ((long) (1e5d * value)) / 1e5d;
    }

    /**
     * Format the given double value using custom formaters:
     * - '0'     if abs(val) < 1e-9
     * - 0.000   if 1e-3 < abs(val) < 1e6
     * - 0.0##E0 else
     * 
     * Note: this method is not thread safe (synchronization must be performed by callers)
     * 
     * @param val double value
     * @return formatted value
     */
    public static String format(final double val) {
        final double abs = Math.abs(val);

        if (abs < 1e-9d) {
            // means zero:
            return "0";
        }

        if (abs < 1e-3d || abs > 1e6d) {
            return format(fmtScience, val);
        }
        return format(fmtDef, val);
    }

    /**
     * Format the given double value using given formater
     * 
     * Note: this method is not thread safe (synchronization must be performed by callers)
     * 
     * @param fmt formatter to use
     * @param val double value
     * @return formatted value
     */
    public static String format(final NumberFormat fmt, final double val) {
        // reset shared buffer:
        fmtBuffer.setLength(0);

        return format(fmt, fmtBuffer, val).toString();
    }

    /**
     * Format the given double value using given formater and append into the given string buffer
     * 
     * Note: this method is thread safe
     * 
     * @param fmt formatter to use
     * @param sb string buffer to append to
     * @param val double value
     * @return formatted value
     */
    public static StringBuffer format(final NumberFormat fmt, final StringBuffer sb, final double val) {
        return fmt.format(val, sb, ignorePosition);
    }

    /**
     * Returns true if two doubles are considered equal.  
     * Test if the absolute difference between two doubles has a difference less than EPSILON.
     *
     * @param a double to compare.
     * @param b double to compare.
     * @return true true if two doubles are considered equal.
     */
    public static boolean equals(final double a, final double b) {
        return equals(a, b, EPSILON);
    }

    /**
     * Returns true if two doubles are considered equal. 
     * 
     * Test if the absolute difference between the two doubles has a difference less then a given
     * double (epsilon).
     *
     * @param a double to compare.
     * @param b double to compare
     * @param epsilon double which is compared to the absolute difference.
     * @return true if a is considered equal to b.
     */
    public static boolean equals(final double a, final double b, final double epsilon) {
        return (a == b) ? true : (Math.abs(a - b) < epsilon);
    }

    /**
     * Returns true if the first double is considered greater than the second
     * double.  
     * 
     * Test if the difference of first minus second is greater than EPSILON.
     *
     * @param a first double
     * @param b second double
     * @return true if the first double is considered greater than the second
     *              double
     */
    public static boolean greaterThan(final double a, final double b) {
        return greaterThan(a, b, EPSILON);
    }

    /**
     * Returns true if the first double is considered greater than the second
     * double.
     *
     * Test if the difference of first minus second is greater then
     * a given double (epsilon).
     *
     * @param a first double
     * @param b second double
     * @param epsilon double which is compared to the absolute difference.
     * @return true if the first double is considered greater than the second
     *              double
     */
    public static boolean greaterThan(final double a, final double b, final double epsilon) {
        return a + epsilon - b > 0d;
    }

    /**
     * Returns true if the first double is considered less than the second
     * double.
     *
     * Test if the difference of second minus first is greater than EPSILON.
     *
     * @param a first double
     * @param b second double
     * @return true if the first double is considered less than the second
     *              double
     */
    public static boolean lessThan(final double a, final double b) {
        return greaterThan(b, a, EPSILON);
    }

    /**
     * Returns true if the first double is considered less than the second
     * double.  Test if the difference of second minus first is greater then
     * a given double (epsilon).  Determining the given epsilon is highly
     * dependant on the precision of the doubles that are being compared.
     *
     * @param a first double
     * @param b second double
     * @param epsilon double which is compared to the absolute difference.
     * @return true if the first double is considered less than the second
     *              double
     */
    public static boolean lessThan(final double a, final double b, final double epsilon) {
        return a - epsilon - b < 0d;
    }

    /**
     * Returns an {@code Integer} instance representing the specified
     * {@code int} value.  If a new {@code Integer} instance is not
     * required, this method should generally be used in preference to
     * the constructor {@link #Integer(int)}, as this method is likely
     * to yield significantly better space and time performance by
     * caching frequently requested values.
     *
     * This method will always cache values in the range -128 to 128 * 1024,
     * inclusive, and may cache other values outside of this range.
     *
     * @param  i an {@code int} value.
     * @return an {@code Integer} instance representing {@code i}.
     */
    public static Integer valueOf(final int i) {
        return IntegerCache.get(i);
    }

    /**
     * Returns an {@code Integer} object holding the
     * value of the specified {@code String}. The argument is
     * interpreted as representing a signed decimal integer, exactly
     * as if the argument were given to the {@link
     * #parseInt(java.lang.String)} method. The result is an
     * {@code Integer} object that represents the integer value
     * specified by the string.
     *
     * <p>In other words, this method returns an {@code Integer}
     * object equal to the value of:
     *
     * <blockquote>
     *  {@code new Integer(Integer.parseInt(s))}
     * </blockquote>
     *
     * @param      s   the string to be parsed.
     * @return     an {@code Integer} object holding the value
     *             represented by the string argument.
     * @exception  NumberFormatException  if the string cannot be parsed
     *             as an integer.
     */
    public static Integer valueOf(final String s) throws NumberFormatException {
        return IntegerCache.get(Integer.parseInt(s, 10));
    }

    /**
     * Integer Cache to support the object identity semantics of autoboxing for values between
     * -128 and HIGH value (inclusive).
     */
    private static final class IntegerCache {

        /** lower value */
        static final int low = -1024;
        /** higher value */
        static final int high = 128 * 1024;
        /** Integer cache */
        static final Integer cache[];

        static {
            // high value may be configured by system property (NumberUtils.IntegerCache.high)

            cache = new Integer[(high - low) + 1];
            int j = low;
            for (int k = 0, len = cache.length; k < len; k++) {
                cache[k] = new Integer(j++);
            }
        }

        /**
         * Return cached Integer instance or new one
         * @param i integer value
         * @return cached Integer instance or new one 
         */
        static Integer get(final int i) {
            if (i >= low && i <= high) {
                return cache[i + (-low)];
            }
            return new Integer(i);
        }

        /**
         * Forbidden constructor
         */
        private IntegerCache() {
        }
    }
}
