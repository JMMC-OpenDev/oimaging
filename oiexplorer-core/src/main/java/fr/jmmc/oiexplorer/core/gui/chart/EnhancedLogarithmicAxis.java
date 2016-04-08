/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * --------------------
 * LogarithmicAxis.java
 * --------------------
 * (C) Copyright 2000-2008, by Object Refinery Limited and Contributors.
 *
 * Original Author:  Michael Duffy / Eric Thomas;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *                   David M. O'Donnell;
 *                   Scott Sams;
 *                   Sergei Ivanov;
 *
 * Changes
 * -------
 * 14-Mar-2002 : Version 1 contributed by Michael Duffy (DG);
 * 19-Apr-2002 : drawVerticalString() is now drawRotatedString() in
 *               RefineryUtilities (DG);
 * 23-Apr-2002 : Added a range property (DG);
 * 15-May-2002 : Modified to be able to deal with negative and zero values (via
 *               new 'adjustedLog10()' method);  occurrences of "Math.log(10)"
 *               changed to "LOG10_VALUE"; changed 'intValue()' to
 *               'longValue()' in 'refreshTicks()' to fix label-text value
 *               out-of-range problem; removed 'draw()' method; added
 *               'autoRangeMinimumSize' check; added 'log10TickLabelsFlag'
 *               parameter flag and implementation (ET);
 * 25-Jun-2002 : Removed redundant import (DG);
 * 25-Jul-2002 : Changed order of parameters in ValueAxis constructor (DG);
 * 16-Jul-2002 : Implemented support for plotting positive values arbitrarily
 *               close to zero (added 'allowNegativesFlag' flag) (ET).
 * 05-Sep-2002 : Updated constructor reflecting changes in the Axis class (DG);
 * 02-Oct-2002 : Fixed errors reported by Checkstyle (DG);
 * 08-Nov-2002 : Moved to new package com.jrefinery.chart.axis (DG);
 * 22-Nov-2002 : Bug fixes from David M. O'Donnell (DG);
 * 14-Jan-2003 : Changed autoRangeMinimumSize from Number --> double (DG);
 * 20-Jan-2003 : Removed unnecessary constructors (DG);
 * 26-Mar-2003 : Implemented Serializable (DG);
 * 08-May-2003 : Fixed plotting of datasets with lower==upper bounds when
 *               'minAutoRange' is very small; added 'strictValuesFlag'
 *               and default functionality of throwing a runtime exception
 *               if 'allowNegativesFlag' is false and any values are less
 *               than or equal to zero; added 'expTickLabelsFlag' and
 *               changed to use "1e#"-style tick labels by default
 *               ("10^n"-style tick labels still supported via 'set'
 *               method); improved generation of tick labels when range of
 *               values is small; changed to use 'NumberFormat.getInstance()'
 *               to create 'numberFormatterObj' (ET);
 * 14-May-2003 : Merged HorizontalLogarithmicAxis and
 *               VerticalLogarithmicAxis (DG);
 * 29-Oct-2003 : Added workaround for font alignment in PDF output (DG);
 * 07-Nov-2003 : Modified to use new NumberTick class (DG);
 * 08-Apr-2004 : Use numberFormatOverride if set - see patch 930139 (DG);
 * 11-Jan-2005 : Removed deprecated code in preparation for 1.0.0 release (DG);
 * 21-Apr-2005 : Added support for upper and lower margins; added
 *               get/setAutoRangeNextLogFlag() methods and changed
 *               default to 'autoRangeNextLogFlag'==false (ET);
 * 22-Apr-2005 : Removed refreshTicks() and fixed names and parameters for
 *               refreshHorizontalTicks() & refreshVerticalTicks();
 *               changed javadoc on setExpTickLabelsFlag() to specify
 *               proper default (ET);
 * 22-Apr-2005 : Renamed refreshHorizontalTicks --> refreshTicksHorizontal
 *               (and likewise the vertical version) for consistency with
 *               other axis classes (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 02-Feb-2007 : Removed author tags all over JFreeChart sources (DG);
 * 02-Mar-2007 : Applied patch 1671069 to fix zooming (DG);
 * 22-Mar-2007 : Use new defaultAutoRange attribute (DG);
 *
 */
package fr.jmmc.oiexplorer.core.gui.chart;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnit;
import org.jfree.chart.event.AxisChangeEvent;

import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

/**
 * Enhanced logarithmic scale to support dynamic ticks and switch to normal ticks if less than 1 decade.
 */
public class EnhancedLogarithmicAxis extends NumberAxis {

    /** For serialization. */
    private static final long serialVersionUID = 2502918599004103054L;

    /** Smallest arbitrarily-close-to-zero value allowed. */
    public static final double SMALL_LOG_VALUE = 1e-100;

    /* members */
    /** Flag set true to allow negative values in data. */
    private boolean allowNegativesFlag = false;

    /**
     * Flag set true make axis throw exception if any values are
     * <= 0 and 'allowNegativesFlag' is false.
     */
    private boolean strictValuesFlag = true;

    /** True to make 'autoAdjustRange()' select "10^n" values. */
    private boolean autoRangeNextLogFlag = false;

    /** Helper flag for log axis processing. */
    private boolean smallLogFlag = false;

    /** default tick unit to define the number formatter */
    private final TickUnit defaultTickUnit;

    /**
     * Creates a new axis.
     *
     * @param label  the axis label.
     */
    public EnhancedLogarithmicAxis(String label) {
        this(label, new NumberTickUnit(1d, new DecimalFormat("0E0")));
    }

    /**
     * Creates a new axis.
     *
     * @param label  the axis label.
     * @param defaultTickUnit default tick unit to define the number formatter
     */
    public EnhancedLogarithmicAxis(final String label, final TickUnit defaultTickUnit) {
        super(label);
        this.defaultTickUnit = defaultTickUnit;
    }

    /**
     * Sets the 'allowNegativesFlag' flag; true to allow negative values
     * in data, false to be able to plot positive values arbitrarily close to
     * zero.
     *
     * @param flgVal  the new value of the flag.
     */
    public void setAllowNegativesFlag(boolean flgVal) {
        this.allowNegativesFlag = flgVal;
    }

    /**
     * Returns the 'allowNegativesFlag' flag; true to allow negative values
     * in data, false to be able to plot positive values arbitrarily close
     * to zero.
     *
     * @return The flag.
     */
    public boolean getAllowNegativesFlag() {
        return this.allowNegativesFlag;
    }

    /**
     * Sets the 'strictValuesFlag' flag; if true and 'allowNegativesFlag'
     * is false then this axis will throw a runtime exception if any of its
     * values are less than or equal to zero; if false then the axis will
     * adjust for values less than or equal to zero as needed.
     *
     * @param flgVal true for strict enforcement.
     */
    public void setStrictValuesFlag(boolean flgVal) {
        this.strictValuesFlag = flgVal;
    }

    /**
     * Returns the 'strictValuesFlag' flag; if true and 'allowNegativesFlag'
     * is false then this axis will throw a runtime exception if any of its
     * values are less than or equal to zero; if false then the axis will
     * adjust for values less than or equal to zero as needed.
     *
     * @return <code>true</code> if strict enforcement is enabled.
     */
    public boolean getStrictValuesFlag() {
        return this.strictValuesFlag;
    }

    /**
     * Sets the 'autoRangeNextLogFlag' flag.  This determines whether or
     * not the 'autoAdjustRange()' method will select the next "10^n"
     * values when determining the upper and lower bounds.  The default
     * value is false.
     *
     * @param flag <code>true</code> to make the 'autoAdjustRange()'
     * method select the next "10^n" values, <code>false</code> to not.
     */
    public void setAutoRangeNextLogFlag(boolean flag) {
        this.autoRangeNextLogFlag = flag;
    }

    /**
     * Returns the 'autoRangeNextLogFlag' flag.
     *
     * @return <code>true</code> if the 'autoAdjustRange()' method will
     * select the next "10^n" values, <code>false</code> if not.
     */
    public boolean getAutoRangeNextLogFlag() {
        return this.autoRangeNextLogFlag;
    }

    /**
     * Overridden version that calls original and then sets up flag for
     * log axis processing.
     *
     * @param range  the new range.
     */
    @Override
    public void setRange(Range range) {
        super.setRange(range);      // call parent method
        setupSmallLogFlag();        // setup flag based on bounds values
    }

    /**
     * Sets up flag for log axis processing.  Set true if negative values
     * not allowed and the lower bound is between 0 and 10.
     */
    protected void setupSmallLogFlag() {
        // set flag true if negative values not allowed and the
        // lower bound is between 0 and 10:
        double lowerVal = getRange().getLowerBound();
        this.smallLogFlag = (!this.allowNegativesFlag && lowerVal < 10.0 && lowerVal > 0.0);
    }

    /**
     * Sets up the number formatter object to '0E0'
     * @return number formatter
     */
    protected NumberFormat setupNumberFmtObj() {
        return new DecimalFormat("0E0");
    }

    /**
     * Returns the log10 value, depending on if values between 0 and
     * 1 are being plotted.  If negative values are not allowed and
     * the lower bound is between 0 and 10 then a normal log is
     * returned; otherwise the returned value is adjusted if the
     * given value is less than 10.
     *
     * @param val the value.
     *
     * @return log<sub>10</sub>(val).
     *
     * @see #switchedPow10(double)
     */
    protected double switchedLog10(double val) {
        return this.smallLogFlag ? Math.log10(val) : adjustedLog10(val);
    }

    /**
     * Returns a power of 10, depending on if values between 0 and
     * 1 are being plotted.  If negative values are not allowed and
     * the lower bound is between 0 and 10 then a normal power is
     * returned; otherwise the returned value is adjusted if the
     * given value is less than 1.
     *
     * @param val the value.
     *
     * @return 10<sup>val</sup>.
     *
     * @since 1.0.5
     * @see #switchedLog10(double)
     */
    public double switchedPow10(double val) {
        return this.smallLogFlag ? Math.pow(10.0, val) : adjustedPow10(val);
    }

    /**
     * Returns an adjusted log10 value for graphing purposes.  The first
     * adjustment is that negative values are changed to positive during
     * the calculations, and then the answer is negated at the end.  The
     * second is that, for values less than 10, an increasingly large
     * (0 to 1) scaling factor is added such that at 0 the value is
     * adjusted to 1, resulting in a returned result of 0.
     *
     * @param val  value for which log10 should be calculated.
     *
     * @return An adjusted log<sub>10</sub>(val).
     *
     * @see #adjustedPow10(double)
     */
    public double adjustedLog10(double val) {
        boolean negFlag = (val < 0.0);
        if (negFlag) {
            val = -val;          // if negative then set flag and make positive
        }
        if (val < 10.0) {                // if < 10 then
            val += (10.0 - val) / 10.0;  //increase so 0 translates to 0
        }
        //return value; negate if original value was negative:
        double res = Math.log10(val);
        return negFlag ? (-res) : res;
    }

    /**
     * Returns an adjusted power of 10 value for graphing purposes.  The first
     * adjustment is that negative values are changed to positive during
     * the calculations, and then the answer is negated at the end.  The
     * second is that, for values less than 1, a progressive logarithmic
     * offset is subtracted such that at 0 the returned result is also 0.
     *
     * @param val  value for which power of 10 should be calculated.
     *
     * @return An adjusted 10<sup>val</sup>.
     *
     * @since 1.0.5
     * @see #adjustedLog10(double)
     */
    public double adjustedPow10(double val) {
        boolean negFlag = (val < 0.0);
        if (negFlag) {
            val = -val; // if negative then set flag and make positive
        }
        double res;
        if (val < 1.0) {
            res = (Math.pow(10, val + 1.0) - 10.0) / 9.0; //invert adjustLog10
        } else {
            res = Math.pow(10, val);
        }
        return negFlag ? (-res) : res;
    }

    /**
     * Returns the largest (closest to positive infinity) double value that is
     * not greater than the argument, is equal to a mathematical integer and
     * satisfying the condition that log base 10 of the value is an integer
     * (i.e., the value returned will be a power of 10: 1, 10, 100, 1000, etc.).
     *
     * @param lower a double value below which a floor will be calcualted.
     *
     * @return 10<sup>N</sup> with N .. { 1 ... }
     */
    protected double computeLogFloor(double lower) {
        double logFloor;
        if (this.allowNegativesFlag) {
            //negative values are allowed
            if (lower > 10.0) {   //parameter value is > 10
                logFloor = Math.log10(lower);
                logFloor = Math.floor(logFloor);
                logFloor = Math.pow(10, logFloor);
            } else if (lower < -10.0) {   //parameter value is < -10
                //calculate log using positive value:
                logFloor = Math.log10(-lower);
                //calculate floor using negative value:
                logFloor = Math.floor(-logFloor);
                //calculate power using positive value; then negate
                logFloor = -Math.pow(10, -logFloor);
            } else {
                //parameter value is -10 > val < 10
                logFloor = Math.floor(lower);   //use as-is
            }
        } else {
            //negative values not allowed
            if (lower > 0.0) {   //parameter value is > 0
                logFloor = Math.log10(lower);
                logFloor = Math.floor(logFloor);
                logFloor = Math.pow(10, logFloor);
            } else {
                //parameter value is <= 0
                logFloor = Math.floor(lower);   //use as-is
            }
        }
        return logFloor;
    }

    /**
     * Returns the smallest (closest to negative infinity) double value that is
     * not less than the argument, is equal to a mathematical integer and
     * satisfying the condition that log base 10 of the value is an integer
     * (i.e., the value returned will be a power of 10: 1, 10, 100, 1000, etc.).
     *
     * @param upper a double value above which a ceiling will be calcualted.
     *
     * @return 10<sup>N</sup> with N .. { 1 ... }
     */
    protected double computeLogCeil(double upper) {
        double logCeil;
        if (this.allowNegativesFlag) {
            //negative values are allowed
            if (upper > 10.0) {
                //parameter value is > 10
                logCeil = Math.log10(upper);
                logCeil = Math.ceil(logCeil);
                logCeil = Math.pow(10, logCeil);
            } else if (upper < -10.0) {
                //parameter value is < -10
                //calculate log using positive value:
                logCeil = Math.log10(-upper);
                //calculate ceil using negative value:
                logCeil = Math.ceil(-logCeil);
                //calculate power using positive value; then negate
                logCeil = -Math.pow(10, -logCeil);
            } else {
                //parameter value is -10 > val < 10
                logCeil = Math.ceil(upper);     //use as-is
            }
        } else {
            //negative values not allowed
            if (upper > 0.0) {
                //parameter value is > 0
                logCeil = Math.log10(upper);
                logCeil = Math.ceil(logCeil);
                logCeil = Math.pow(10, logCeil);
            } else {
                //parameter value is <= 0
                logCeil = Math.ceil(upper);     //use as-is
            }
        }
        return logCeil;
    }

    /**
     * Rescales the axis to ensure that all data is visible.
     */
    @Override
    public void autoAdjustRange() {
        Plot plot = getPlot();
        if (plot == null) {
            return;  // no plot, no data.
        }

        if (plot instanceof ValueAxisPlot) {
            ValueAxisPlot vap = (ValueAxisPlot) plot;

            double lower;
            Range r = vap.getDataRange(this);
            if (r == null) {
                //no real data present
                r = getDefaultAutoRange();
                lower = r.getLowerBound();    //get lower bound value
            } else {
                //actual data is present
                lower = r.getLowerBound();    //get lower bound value
                if (this.strictValuesFlag
                        && !this.allowNegativesFlag && lower <= 0.0) {
                    //strict flag set, allow-negatives not set and values <= 0
                    throw new RuntimeException("Values less than or equal to "
                            + "zero not allowed with logarithmic axis");
                }
            }

            //apply lower margin by decreasing lower bound:
            final double lowerMargin;
            if (lower > 0.0 && (lowerMargin = getLowerMargin()) > 0.0) {
                //lower bound and margin OK; get log10 of lower bound
                final double logLower = Math.log10(lower);
                double logAbs;      //get absolute value of log10 value
                if ((logAbs = Math.abs(logLower)) < 1.0) {
                    logAbs = 1.0;     //if less than 1.0 then make it 1.0
                }              //subtract out margin and get exponential value:
                lower = Math.pow(10, (logLower - (logAbs * lowerMargin)));
            }

            //if flag then change to log version of lowest value
            // to make range begin at a 10^n value:
            if (this.autoRangeNextLogFlag) {
                lower = computeLogFloor(lower);
            }

            if (!this.allowNegativesFlag && lower >= 0.0
                    && lower < SMALL_LOG_VALUE) {
                //negatives not allowed and lower range bound is zero
                lower = r.getLowerBound();    //use data range bound instead
            }

            double upper = r.getUpperBound();

            //apply upper margin by increasing upper bound:
            final double upperMargin;
            if (upper > 0.0 && (upperMargin = getUpperMargin()) > 0.0) {
                //upper bound and margin OK; get log10 of upper bound
                final double logUpper = Math.log10(upper);
                double logAbs;      //get absolute value of log10 value
                if ((logAbs = Math.abs(logUpper)) < 1.0) {
                    logAbs = 1.0;     //if less than 1.0 then make it 1.0
                }              //add in margin and get exponential value:
                upper = Math.pow(10, (logUpper + (logAbs * upperMargin)));
            }

            if (!this.allowNegativesFlag && upper < 1.0 && upper > 0.0
                    && lower > 0.0) {
                //negatives not allowed and upper bound between 0 & 1
                //round up to nearest significant digit for bound:
                //get negative exponent:
                double expVal = Math.log10(upper);
                expVal = Math.ceil(-expVal + 0.001); //get positive exponent
                expVal = Math.pow(10, expVal);      //create multiplier value
                //multiply, round up, and divide for bound value:
                upper = (expVal > 0.0) ? Math.ceil(upper * expVal) / expVal
                        : Math.ceil(upper);
            } else {
                //negatives allowed or upper bound not between 0 & 1
                //if flag then change to log version of highest value to
                // make range begin at a 10^n value; else use nearest int
                upper = (this.autoRangeNextLogFlag) ? computeLogCeil(upper)
                        : Math.ceil(upper);
            }
            // ensure the autorange is at least <minRange> in size...
            double minRange = getAutoRangeMinimumSize();
            if (upper - lower < minRange) {
                upper = (upper + lower + minRange) / 2;
                lower = (upper + lower - minRange) / 2;
                //if autorange still below minimum then adjust by 1%
                // (can be needed when minRange is very small):
                if (upper - lower < minRange) {
                    double absUpper = Math.abs(upper);
                    //need to account for case where upper==0.0
                    double adjVal = (absUpper > SMALL_LOG_VALUE) ? absUpper
                            / 100.0 : 0.01;
                    upper = (upper + lower + adjVal) / 2;
                    lower = (upper + lower - adjVal) / 2;
                }
            }

            setRange(new Range(lower, upper), false, false);
            setupSmallLogFlag();       //setup flag based on bounds values
        }
    }

    /**
     * Converts a data value to a coordinate in Java2D space, assuming that
     * the axis runs along one edge of the specified plotArea.
     * Note that it is possible for the coordinate to fall outside the
     * plotArea.
     *
     * @param value  the data value.
     * @param plotArea  the area for plotting the data.
     * @param edge  the axis location.
     *
     * @return The Java2D coordinate.
     */
    @Override
    public double valueToJava2D(double value, Rectangle2D plotArea,
                                RectangleEdge edge) {

        Range range = getRange();
        double axisMin = switchedLog10(range.getLowerBound());
        double axisMax = switchedLog10(range.getUpperBound());

        double min = 0.0;
        double max = 0.0;
        if (RectangleEdge.isTopOrBottom(edge)) {
            min = plotArea.getMinX();
            max = plotArea.getMaxX();
        } else if (RectangleEdge.isLeftOrRight(edge)) {
            min = plotArea.getMaxY();
            max = plotArea.getMinY();
        }

        value = switchedLog10(value);

        if (isInverted()) {
            return max - (((value - axisMin) / (axisMax - axisMin))
                    * (max - min));
        } else {
            return min + (((value - axisMin) / (axisMax - axisMin))
                    * (max - min));
        }

    }

    /**
     * Converts a coordinate in Java2D space to the corresponding data
     * value, assuming that the axis runs along one edge of the specified
     * plotArea.
     *
     * @param java2DValue  the coordinate in Java2D space.
     * @param plotArea  the area in which the data is plotted.
     * @param edge  the axis location.
     *
     * @return The data value.
     */
    @Override
    public double java2DToValue(double java2DValue, Rectangle2D plotArea,
                                RectangleEdge edge) {

        Range range = getRange();
        double axisMin = switchedLog10(range.getLowerBound());
        double axisMax = switchedLog10(range.getUpperBound());

        double plotMin = 0.0;
        double plotMax = 0.0;
        if (RectangleEdge.isTopOrBottom(edge)) {
            plotMin = plotArea.getX();
            plotMax = plotArea.getMaxX();
        } else if (RectangleEdge.isLeftOrRight(edge)) {
            plotMin = plotArea.getMaxY();
            plotMax = plotArea.getMinY();
        }

        if (isInverted()) {
            return switchedPow10(axisMax - ((java2DValue - plotMin)
                    / (plotMax - plotMin)) * (axisMax - axisMin));
        } else {
            return switchedPow10(axisMin + ((java2DValue - plotMin)
                    / (plotMax - plotMin)) * (axisMax - axisMin));
        }
    }

    /**
     * Increases or decreases the axis range by the specified percentage about
     * the specified anchor value and sends an {@link AxisChangeEvent} to all
     * registered listeners.
     * <P>
     * To double the length of the axis range, use 200% (2.0).
     * To halve the length of the axis range, use 50% (0.5).
     *
     * @param percent  the resize factor.
     * @param anchorValue  the new central value after the resize.
     *
     * @see #resizeRange(double)
     */
    @Override
    public final void resizeRange(final double percent, final double anchorValue) {
        resizeRange2(percent, anchorValue);
    }

    /**
     * Increases or decreases the axis range by the specified percentage about
     * the specified anchor value and sends an {@link AxisChangeEvent} to all
     * registered listeners.
     * <P>
     * To double the length of the axis range, use 200% (2.0).
     * To halve the length of the axis range, use 50% (0.5).
     *
     * @param percent  the resize factor.
     * @param anchorValue  the new central value after the resize.
     *
     * @see #resizeRange(double)
     *
     * @since 1.0.13
     */
    @Override
    public final void resizeRange2(final double percent, final double anchorValue) {
        if (percent > 0.0) {
            // Convert anchor value in log range:
            // see zoomRange()
            final Range range = getRange();
            final double lowerVal = range.getLowerBound();
            final double upperVal = range.getUpperBound();

            double anchorLog = switchedLog10(anchorValue);
            double leftLog = anchorLog - switchedLog10(lowerVal);
            double rightLog = switchedLog10(upperVal) - anchorLog;

            Range adjusted = new Range(
                    switchedPow10(anchorLog - leftLog * percent),
                    switchedPow10(anchorLog + rightLog * percent));
            setRange(adjusted);
        } else {
            setAutoRange(true);
        }
    }

    /**
     * Zooms in on the current range.
     *
     * @param lowerPercent  the new lower bound.
     * @param upperPercent  the new upper bound.
     */
    @Override
    public final void zoomRange(final double lowerPercent, final double upperPercent) {
        // Convert anchor value in log range:
        // see zoomRange()
        final Range range = getRange();
        final double lowerVal = range.getLowerBound();
        final double upperVal = range.getUpperBound();

        double startLog = switchedLog10(lowerVal);
        double lengthLog = switchedLog10(upperVal) - startLog;

        Range adjusted;
        if (isInverted()) {
            adjusted = new Range(
                    switchedPow10(startLog + (lengthLog * (1 - upperPercent))),
                    switchedPow10(startLog + (lengthLog * (1 - lowerPercent))));
        } else {
            adjusted = new Range(
                    switchedPow10(startLog + (lengthLog * lowerPercent)),
                    switchedPow10(startLog + (lengthLog * upperPercent)));
        }

        setRange(adjusted);
    }

    /**
     * Converts a length in data coordinates into the corresponding length in
     * Java2D coordinates.
     *
     * @param length  the length.
     * @param area  the plot area.
     * @param edge  the edge along which the axis lies.
     *
     * @return The length in Java2D coordinates.
     */
    @Override
    public final double lengthToJava2D(final double length,
                                       final Rectangle2D area,
                                       final RectangleEdge edge) {

        final Range range = getRange();
        final double lowerVal = range.getLowerBound();
        final double upperVal = range.getUpperBound();

        // LBO: as the log axis is non linear, estimate the length of 1% (range)
        final double pct1 = 0.01d * (upperVal - lowerVal);

        // use upperVal side (smaller length)
        final double max = valueToJava2D(upperVal, area, edge);
        final double len_pct10_small = valueToJava2D(upperVal - pct1, area, edge);

        // return proportional length:
        return Math.abs(len_pct10_small - max) * (length / pct1);
    }

    /**
     * Calculates the positions of the tick labels for the axis, storing the
     * results in the tick label list (ready for drawing).
     *
     * @param g2  the graphics device.
     * @param dataArea  the area in which the plot should be drawn.
     * @param edge  the location of the axis.
     *
     * @return A list of ticks.
     */
    @Override
    protected List refreshTicksHorizontal(Graphics2D g2,
                                          Rectangle2D dataArea,
                                          RectangleEdge edge) {

        final Range range = getRange();

        //get lower bound value:
        double lowerBoundVal = range.getLowerBound();
        //if small log values and lower bound value too small
        // then set to a small value (don't allow <= 0):
        if (this.smallLogFlag && lowerBoundVal < SMALL_LOG_VALUE) {
            lowerBoundVal = SMALL_LOG_VALUE;
        }

        //get upper bound value
        double upperBoundVal = range.getUpperBound();

        final double lowerLog = switchedLog10(lowerBoundVal);
        final double upperLog = switchedLog10(upperBoundVal);
        final double deltaLog = upperLog - lowerLog;

        // Use standard ticks if log range is small ie less than 1:
        if (deltaLog <= 1d) {
            return super.refreshTicksHorizontal(g2, dataArea, edge);
        }

        //get log10 version of lower bound and round to integer:
        final int iBegCount = (int) Math.floor(lowerLog);
        //get log10 version of upper bound and round to integer:
        final int iEndCount = (int) Math.ceil(upperLog);

        double pow;
        double tickVal;
        String tickLabel;

        final boolean[] dispLabel = new boolean[10];
        // anyway show major ticks:
        dispLabel[1] = true;

        // If less than 3 decades: adjust minor tick label:
        if (deltaLog < 3d) {
            final Font tickLabelFont = getTickLabelFont();
            g2.setFont(tickLabelFont);

            // note: 2 half width = width
            // because formatter gives 'constant-width' values ('0E0)
            final double tickLabelWidth = estimateMaximumTickLabelWidth(g2, defaultTickUnit);

            pow = Math.pow(10d, iBegCount);

            // initial major tick:
            tickVal = pow;
            double prev = valueToJava2D(tickVal, dataArea, edge);

            double tickPos, ticksSpace;
            pow *= 0.1d;

            // Go from 10 to 2 ie smaller tick space to larger:
            for (int j = 9; j >= 2; j--) {
                tickVal -= pow;

                tickPos = valueToJava2D(tickVal, dataArea, edge);
                // space between current tick position and previous tick label:
                ticksSpace = Math.abs(tickPos - prev);

                if (ticksSpace >= tickLabelWidth) {
                    // enough space to display 2 half tick labels:
                    dispLabel[j] = true;
                    prev = tickPos;
                }
            }
        }

        // fix lower bound:
        lowerBoundVal -= SMALL_LOG_VALUE;

        List ticks = new java.util.ArrayList();

        for (int i = iBegCount; i <= iEndCount; i++) {
            //for each tick with a label to be displayed
            int jEndCount = 9;
            if (i == iEndCount) {
                jEndCount = 1;
            }

            pow = Math.pow(10, i);

            for (int j = 0; j < jEndCount; j++) {
                //for each tick to be displayed
                tickVal = pow * (1 + j);

                if (tickVal > upperBoundVal) {
                    //if past highest data value then exit method
                    return ticks;
                }

                if (tickVal >= lowerBoundVal) {
                    //tick value not below lowest data value
                    tickLabel = "";

                    if (dispLabel[1 + j]) {
                        tickLabel = makeTickLabel(tickVal);
                    }

                    final TextAnchor anchor;
                    final TextAnchor rotationAnchor;
                    double angle = 0.0;
                    if (isVerticalTickLabels()) {
                        anchor = TextAnchor.CENTER_RIGHT;
                        rotationAnchor = TextAnchor.CENTER_RIGHT;
                        if (edge == RectangleEdge.TOP) {
                            angle = Math.PI / 2.0;
                        } else {
                            angle = -Math.PI / 2.0;
                        }
                    } else {
                        if (edge == RectangleEdge.TOP) {
                            anchor = TextAnchor.BOTTOM_CENTER;
                            rotationAnchor = TextAnchor.BOTTOM_CENTER;
                        } else {
                            anchor = TextAnchor.TOP_CENTER;
                            rotationAnchor = TextAnchor.TOP_CENTER;
                        }
                    }
                    //create tick object and add to list:
                    ticks.add(new NumberTick(new Double(tickVal), tickLabel, anchor, rotationAnchor, angle));
                }
            }
        }
        return ticks;

    }

    /**
     * Calculates the positions of the tick labels for the axis, storing the
     * results in the tick label list (ready for drawing).
     *
     * @param g2  the graphics device.
     * @param dataArea  the area in which the plot should be drawn.
     * @param edge  the location of the axis.
     *
     * @return A list of ticks.
     */
    @Override
    protected final List refreshTicksVertical(final Graphics2D g2,
                                              final Rectangle2D dataArea,
                                              final RectangleEdge edge) {

        final Range range = getRange();

        //get lower bound value:
        double lowerBoundVal = range.getLowerBound();
        //if small log values and lower bound value too small
        // then set to a small value (don't allow <= 0):
        if (this.smallLogFlag && lowerBoundVal < SMALL_LOG_VALUE) {
            lowerBoundVal = SMALL_LOG_VALUE;
        }

        //get upper bound value
        double upperBoundVal = range.getUpperBound();

        final double lowerLog = switchedLog10(lowerBoundVal);
        final double upperLog = switchedLog10(upperBoundVal);
        final double deltaLog = upperLog - lowerLog;

        // Use standard ticks if log range is small ie less than 1:
        if (deltaLog <= 1d) {
            return super.refreshTicksVertical(g2, dataArea, edge);
        }

        //get log10 version of lower bound and round to integer:
        final int iBegCount = (int) Math.floor(lowerLog);
        //get log10 version of upper bound and round to integer:
        final int iEndCount = (int) Math.ceil(upperLog);

        double pow;
        double tickVal;
        String tickLabel;

        final boolean[] dispLabel = new boolean[10];
        // anyway show major ticks:
        dispLabel[1] = true;

        // If less than 3 decades: adjust minor tick label:
        if (deltaLog < 3d) {
            final Font tickLabelFont = getTickLabelFont();
            g2.setFont(tickLabelFont);

            // note: 2 half height = height
            final double tickLabelHeight = estimateMaximumTickLabelHeight(g2);

            pow = Math.pow(10d, iBegCount);

            // initial major tick:
            tickVal = pow;
            double prev = valueToJava2D(tickVal, dataArea, edge);

            double tickPos, ticksSpace;
            pow *= 0.1d;

            // Go from 10 to 2 ie smaller tick space to larger:
            for (int j = 9; j >= 2; j--) {
                tickVal -= pow;

                tickPos = valueToJava2D(tickVal, dataArea, edge);
                // space between current tick position and previous tick label:
                ticksSpace = Math.abs(tickPos - prev);

                if (ticksSpace >= tickLabelHeight) {
                    // enough space to display 2 half tick labels:
                    dispLabel[j] = true;
                    prev = tickPos;
                }
            }
        }

        // fix lower bound:
        lowerBoundVal -= SMALL_LOG_VALUE;

        List ticks = new java.util.ArrayList();

        for (int i = iBegCount; i <= iEndCount; i++) {
            //for each tick with a label to be displayed
            int jEndCount = 9;
            if (i == iEndCount) {
                jEndCount = 1;
            }

            pow = Math.pow(10, i);

            for (int j = 0; j < jEndCount; j++) {
                //for each tick to be displayed
                tickVal = pow * (1 + j);

                if (tickVal > upperBoundVal) {
                    //if past highest data value then exit method
                    return ticks;
                }

                if (tickVal >= lowerBoundVal) {
                    //tick value not below lowest data value
                    tickLabel = "";

                    if (dispLabel[1 + j]) {
                        tickLabel = makeTickLabel(tickVal);
                    }

                    final TextAnchor anchor;
                    final TextAnchor rotationAnchor;
                    double angle = 0.0;
                    if (isVerticalTickLabels()) {
                        if (edge == RectangleEdge.LEFT) {
                            anchor = TextAnchor.BOTTOM_CENTER;
                            rotationAnchor = TextAnchor.BOTTOM_CENTER;
                            angle = -Math.PI / 2.0;
                        } else {
                            anchor = TextAnchor.BOTTOM_CENTER;
                            rotationAnchor = TextAnchor.BOTTOM_CENTER;
                            angle = Math.PI / 2.0;
                        }
                    } else {
                        if (edge == RectangleEdge.LEFT) {
                            anchor = TextAnchor.CENTER_RIGHT;
                            rotationAnchor = TextAnchor.CENTER_RIGHT;
                        } else {
                            anchor = TextAnchor.CENTER_LEFT;
                            rotationAnchor = TextAnchor.CENTER_LEFT;
                        }
                    }
                    //create tick object and add to list:
                    ticks.add(new NumberTick(new Double(tickVal), tickLabel, anchor, rotationAnchor, angle));
                }
            }
        }

        return ticks;
    }

    /**
     * Converts the given value to a tick label string.
     * @param val the value to convert.
     *
     * @return The tick label string.
     */
    protected final String makeTickLabel(double val) {
        return defaultTickUnit.valueToString(val);
    }

}
