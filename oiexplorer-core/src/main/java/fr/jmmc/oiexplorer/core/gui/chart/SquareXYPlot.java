/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.chart;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This custom xy plot ensures the drawing area to have a square shape and finds the axes bounds.
 *
 * Note : this class must support the inherited cloneable interface.
 *
 * @author bourgesl
 */
public final class SquareXYPlot extends XYPlot {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(SquareXYPlot.class.getName());
    /** flag to enable image anti aliasing and bi-cubic interpolation */
    public static final boolean USE_INTERPOLATION = true;

    /**
     * Creates a new plot with the specified dataset, axes and renderer.  Any
     * of the arguments can be <code>null</code>, but in that case you should
     * take care to specify the value before using the plot (otherwise a
     * <code>NullPointerException</code> may be thrown).
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     * @param domainAxis  the domain axis (<code>null</code> permitted).
     * @param rangeAxis  the range axis (<code>null</code> permitted).
     * @param renderer  the renderer (<code>null</code> permitted).
     */
    public SquareXYPlot(final XYDataset dataset,
            final ValueAxis domainAxis,
            final ValueAxis rangeAxis,
            final XYItemRenderer renderer) {
        super(dataset, domainAxis, rangeAxis, renderer);
    }

    /**
     * Draws the plot within the specified area on a graphics device.
     *
     * @param g2d  the graphics device.
     * @param area  the plot area (in Java2D space).
     * @param anchor  an anchor point in Java2D space (<code>null</code>
     *                permitted).
     * @param parentState  the state from the parent plot, if there is one
     *                     (<code>null</code> permitted).
     * @param info  collects chart drawing information (<code>null</code>
     *              permitted).
     */
    @Override
    public void draw(final Graphics2D g2d, final Rectangle2D area, final Point2D anchor,
            final PlotState parentState, final PlotRenderingInfo info) {

        double hSpace = 0d;
        double vSpace = 0d;

        // get plot insets :
        final RectangleInsets insets = getInsets();

        hSpace += insets.getLeft() + insets.getRight();
        vSpace += insets.getTop() + insets.getBottom();

        // compute Axis Space :
        final AxisSpace space = calculateAxisSpace(g2d, area);

        hSpace += space.getLeft() + space.getRight();
        vSpace += space.getTop() + space.getBottom();

        // range (Y) / domain (X) ratio:
        final double ratio = getAspectRatio();

        // compute the square data area size :
        final double size = Math.min(area.getWidth() - hSpace, area.getHeight() - vSpace);

        // adjusted dimensions to get a square data area :
        final double adjustedWidth;
        final double adjustedHeight;

        if (ratio > 1d) {
            // taller (Y > X):
            adjustedWidth = size / ratio + hSpace;
            adjustedHeight = size + vSpace;
        } else {
            // larger (X > Y):
            adjustedWidth = size + hSpace;
            adjustedHeight = size / ratio + vSpace;
        }

        // margins to center the plot into the rectangle area :
        final double marginWidth = (area.getWidth() - adjustedWidth) / 2d;
        final double marginHeight = (area.getHeight() - adjustedHeight) / 2d;

        final Rectangle2D adjustedArea = new Rectangle2D.Double();

        // note :
        // - rounding is required to have the background image fitted (int coordinates) in the plot area (double rectangle) :
        // - there can be some rounding issue that adjust lightly the square shape :
        adjustedArea.setRect(Math.round(area.getX() + marginWidth), Math.round(area.getY() + marginHeight), Math.round(adjustedWidth), Math.round(adjustedHeight));

        // Force rendering hints :
        // set quality flags:
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        if (USE_INTERPOLATION) {
            // Use bicubic interpolation (slower) for quality:
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        } else {
            // Use no interpolation (faster) for raw display:
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        }
        super.draw(g2d, adjustedArea, anchor, parentState, info);
    }

    /**
     * Receives notification of a change to the plot's dataset.
     * The axis ranges are updated if necessary.
     *
     * HACK : find the bounds for both range and domain axes and adjust the axes bounds (reset zoom)
     *
     * @param event  information about the event (not used here).
     */
    @Override
    public void datasetChanged(final DatasetChangeEvent event) {
        restoreAxesBounds();
    }

    /**
     * Define the bounds for both range and domain axes using the given maximum value.
     *
     * Note : this method must be called before calling plot.setDataset(...) or restoreAxesBounds()
     *
     * @param max maximum value
     */
    public void defineBounds(final double max) {
        defineAxisBounds(0, max);
    }

    /**
     * Define the bounds for both range and domain axes using the given maximum value.
     *
     * Note : this method must be called before calling plot.setDataset(...) or restoreAxesBounds()
     *
     * @param axisIndex index of the range and domain axes to use
     * @param max maximum value
     */
    public void defineAxisBounds(final int axisIndex, final double max) {
        // same range on both axes :
        final Range bounds = new Range(-max, max);
        defineAxisBounds(axisIndex, bounds, bounds);
    }

    /**
     * Define the bounds for both range and domain axes using the given ranges.
     *
     * Note : this method must be called before calling plot.setDataset(...) or restoreAxesBounds()
     *
     * @param domainRange range applied to the domain axis
     * @param rangeRange range applied to the range axis
     */
    public void defineBounds(final Range domainRange, final Range rangeRange) {
        defineAxisBounds(0, domainRange, rangeRange);
    }

    /**
     * Define the bounds for both range and domain axes for the given axis index using the given ranges.
     *
     * Note : this method must be called before calling plot.setDataset(...) or restoreAxesBounds()
     *
     * @param axisIndex index of the range and domain axes to use
     * @param domainRange range applied to the domain axis
     * @param rangeRange range applied to the range axis
     */
    public void defineAxisBounds(final int axisIndex, final Range domainRange, final Range rangeRange) {
        final BoundedNumberAxis domainAxis = getBoundedAxis(getDomainAxis(axisIndex));
        if (domainAxis != null) {
            domainAxis.setBounds(domainRange);
        }
        final BoundedNumberAxis rangeAxis = getBoundedAxis(getRangeAxis(axisIndex));
        if (rangeAxis != null) {
            rangeAxis.setBounds(rangeRange);
        }
    }

    /**
     * Use the axis bounds to redefine the ranges for both axes (reset zoom)
     */
    public void restoreAxesBounds() {
        for (int i = 0, len = getDomainAxisCount(); i < len; i++) {
            final BoundedNumberAxis domainAxis = getBoundedAxis(getDomainAxis(i));
            if (domainAxis != null && domainAxis.getBounds() != null) {
                // do not disable auto range :
                domainAxis.setRange(domainAxis.getBounds(), false, false);
            }
        }
        for (int i = 0, len = getRangeAxisCount(); i < len; i++) {
            final BoundedNumberAxis rangeAxis = getBoundedAxis(getRangeAxis(i));
            if (rangeAxis != null && rangeAxis.getBounds() != null) {
                // do not disable auto range :
                rangeAxis.setRange(rangeAxis.getBounds(), false, false);
            }
        }
    }

    /**
     * Return the BoundedNumberAxis given an axis
     * @param axis axis to cast
     * @return BoundedNumberAxis or null if the given axis is not a BoundedNumberAxis
     */
    public static BoundedNumberAxis getBoundedAxis(final ValueAxis axis) {
        if (axis instanceof BoundedNumberAxis) {
            return (BoundedNumberAxis) axis;
        }
        return null;
    }

    /**
     * Return the aspect ratio of the plot ie Range axis Length / Domain axis length
     * @return aspect ratio of the plot ie Range axis Length / Domain axis length
     */
    public double getAspectRatio() {
        final double ratio = getRangeAxis(0).getRange().getLength() / getDomainAxis(0).getRange().getLength();

        if (logger.isDebugEnabled()) {
            logger.debug("aspect ratio: {}", ratio);
        }

        return ratio;
    }
}
