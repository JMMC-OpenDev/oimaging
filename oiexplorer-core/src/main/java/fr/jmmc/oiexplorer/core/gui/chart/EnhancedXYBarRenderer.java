/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2017, by Object Refinery Limited and Contributors.
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
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * ------------------
 * XYBarRenderer.java
 * ------------------
 * (C) Copyright 2001-2017, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   Richard Atkinson;
 *                   Christian W. Zuckschwerdt;
 *                   Bill Kelemen;
 *                   Marc van Glabbeek (bug 1775452);
 *                   Richard West, Advanced Micro Devices, Inc.;
 *
 * Changes
 * -------
 * 13-Dec-2001 : Version 1, makes VerticalXYBarPlot class redundant (DG);
 * 23-Jan-2002 : Added DrawInfo parameter to drawItem() method (DG);
 * 09-Apr-2002 : Removed the translated zero from the drawItem method. Override
 *               the initialise() method to calculate it (DG);
 * 24-May-2002 : Incorporated tooltips into chart entities (DG);
 * 25-Jun-2002 : Removed redundant import (DG);
 * 05-Aug-2002 : Small modification to drawItem method to support URLs for HTML
 *               image maps (RA);
 * 25-Mar-2003 : Implemented Serializable (DG);
 * 01-May-2003 : Modified drawItem() method signature (DG);
 * 30-Jul-2003 : Modified entity constructor (CZ);
 * 20-Aug-2003 : Implemented Cloneable and PublicCloneable (DG);
 * 24-Aug-2003 : Added null checks in drawItem (BK);
 * 16-Sep-2003 : Changed ChartRenderingInfo --> PlotRenderingInfo (DG);
 * 07-Oct-2003 : Added renderer state (DG);
 * 05-Dec-2003 : Changed call to obtain outline paint (DG);
 * 10-Feb-2004 : Added state class, updated drawItem() method to make
 *               cut-and-paste overriding easier, and replaced property change
 *               with RendererChangeEvent (DG);
 * 25-Feb-2004 : Replaced CrosshairInfo with CrosshairState (DG);
 * 26-Apr-2004 : Added gradient paint transformer (DG);
 * 19-May-2004 : Fixed bug (879709) with bar zero value for secondary axis (DG);
 * 15-Jul-2004 : Switched getX() with getXValue() and getY() with
 *               getYValue() (DG);
 * 01-Sep-2004 : Added a flag to control whether or not the bar outlines are
 *               drawn (DG);
 * 03-Sep-2004 : Added option to use y-interval from dataset to determine the
 *               length of the bars (DG);
 * 08-Sep-2004 : Added equals() method and updated clone() method (DG);
 * 26-Jan-2005 : Added override for getLegendItem() method (DG);
 * 20-Apr-2005 : Use generators for label tooltips and URLs (DG);
 * 19-May-2005 : Added minimal item label implementation - needs improving (DG);
 * 14-Oct-2005 : Fixed rendering problem with inverted axes (DG);
 * ------------- JFREECHART 1.0.x ---------------------------------------------
 * 21-Jun-2006 : Improved item label handling - see bug 1501768 (DG);
 * 24-Aug-2006 : Added crosshair support (DG);
 * 13-Dec-2006 : Updated getLegendItems() to return gradient paint
 *               transformer (DG);
 * 02-Feb-2007 : Changed setUseYInterval() to only notify when the flag
 *               changes (DG);
 * 06-Feb-2007 : Fixed bug 1086307, crosshairs with multiple axes (DG);
 * 09-Feb-2007 : Updated getLegendItem() to observe drawBarOutline flag (DG);
 * 05-Mar-2007 : Applied patch 1671126 by Sergei Ivanov, to fix rendering with
 *               LogarithmicAxis (DG);
 * 20-Apr-2007 : Updated getLegendItem() for renderer change (DG);
 * 17-May-2007 : Set datasetIndex and seriesIndex in getLegendItem() (DG);
 * 18-May-2007 : Set dataset and seriesKey for LegendItem (DG);
 * 15-Jun-2007 : Changed default for drawBarOutline to false (DG);
 * 26-Sep-2007 : Fixed bug 1775452, problem with bar margins for inverted
 *               axes, thanks to Marc van Glabbeek (DG);
 * 12-Nov-2007 : Fixed NPE in drawItemLabel() method, thanks to Richard West
 *               (see patch 1827829) (DG);
 * 17-Jun-2008 : Apply legend font and paint attributes (DG);
 * 19-Jun-2008 : Added findRangeBounds() method override to fix bug in default
 *               axis range (DG);
 * 24-Jun-2008 : Added new barPainter mechanism (DG);
 * 03-Feb-2009 : Added defaultShadowsVisible flag (DG);
 * 05-Feb-2009 : Added barAlignmentFactor (DG);
 * 10-May-2012 : Fix findDomainBounds() and findRangeBounds() to account for
 *               non-visible series (DG);
 * 03-Jul-2013 : Use ParamChecks (DG);
 * 24-Aug-2014 : Add begin/endElementGroup() (DG);
 * 18-Feb-2017 : Updates for crosshairs (bug #36) (DG);
 *
 */
package fr.jmmc.oiexplorer.core.gui.chart;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;

/**
 * A renderer that draws bars on an {@link XYPlot} (requires an
 * {@link IntervalXYDataset}).  The example shown here is generated by the
 * {@code XYBarChartDemo1.java} program included in the JFreeChart
 * demo collection:
 * <br><br>
 * <img src="../../../../../images/XYBarRendererSample.png"
 * alt="XYBarRendererSample.png">
 */
public final class EnhancedXYBarRenderer extends XYBarRenderer {

    private static final long serialVersionUID = 1L;

    /* members */
    private boolean notify = true;

    /** The plot orientation. (optional) */
    private PlotOrientation orientation = null;

    /** The domain axis. (optional) */
    private ValueAxis domainAxis = null;

    /** The range axis. (optional) */
    private ValueAxis rangeAxis = null;

    /**
     * Public constructor
     */
    public EnhancedXYBarRenderer() {
        super(0.0);
    }

    /**
     * Sends a {@link RendererChangeEvent} to all registered listeners.
     *
     * @since 1.0.5
     */
    @Override
    protected void fireChangeEvent() {
        if (notify) {
            // the commented out code would be better, but only if
            // RendererChangeEvent is immutable, which it isn't.  See if there is
            // a way to fix this...
            //if (this.event == null) {
            //    this.event = new RendererChangeEvent(this);
            //}
            //notifyListeners(this.event);
            notifyListeners(new RendererChangeEvent(this));
        }
    }

    /**
     * Returns a flag that controls whether or not change events are sent to
     * registered listeners.
     *
     * @return A boolean.
     *
     * @see #setNotify(boolean)
     */
    public boolean isNotify() {
        return this.notify;
    }

    /**
     * Sets a flag that controls whether or not listeners receive
     * {@link RendererChangeEvent} notifications.
     *
     * @param notify  a boolean.
     *
     * @see #isNotify()
     */
    public void setNotify(final boolean notify) {
        this.notify = notify;
        // if the flag is being set to true, there may be queued up changes...
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns the orientation of the plot.
     *
     * @return The orientation (never {@code null}).
     *
     * @see #setOrientation(PlotOrientation)
     */
    public PlotOrientation getOrientation() {
        return this.orientation;
    }

    /**
     * Sets the orientation for the plot and sends a {@link PlotChangeEvent} to
     * all registered listeners.
     *
     * @param orientation  the orientation ({@code null} not allowed).
     *
     * @see #getOrientation()
     */
    public void setOrientation(final PlotOrientation orientation) {
        if (orientation != this.orientation) {
            this.orientation = orientation;
            fireChangeEvent();
        }
    }

    public ValueAxis getDomainAxis() {
        return domainAxis;
    }

    public void setDomainAxis(final ValueAxis domainAxis) {
        if (domainAxis != this.domainAxis) {
            this.domainAxis = domainAxis;
            fireChangeEvent();
        }
    }

    public ValueAxis getRangeAxis() {
        return rangeAxis;
    }

    public void setRangeAxis(final ValueAxis rangeAxis) {
        if (rangeAxis != this.rangeAxis) {
            this.rangeAxis = rangeAxis;
            fireChangeEvent();
        }
    }

    // internals ---
    private ValueAxis getDomainAxis(final ValueAxis axis) {
        if (getDomainAxis() != null) {
            return getDomainAxis();
        }
        return axis;
    }

    private ValueAxis getRangeAxis(final ValueAxis axis) {
        if (getRangeAxis() != null) {
            return getRangeAxis();
        }
        return axis;
    }

    private PlotOrientation getPlotOrientation(final XYPlot plot) {
        final PlotOrientation o = getOrientation();
        if (o != null) {
            return o;
        }
        return plot.getOrientation();
    }

    private RectangleEdge getDomainAxisEdge(final XYPlot plot) {
        return Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), getPlotOrientation(plot));
    }

    private RectangleEdge getRangeAxisEdge(final XYPlot plot) {
        return Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), getPlotOrientation(plot));
    }

    /**
     * Draws the visual representation of a single data item.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area within which the plot is being drawn.
     * @param info  collects information about the drawing.
     * @param plot  the plot (can be used to obtain standard color
     *              information etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param crosshairState  crosshair information for the plot
     *                        ({@code null} permitted).
     * @param pass  the pass index.
     */
    @Override
    public void drawItem(Graphics2D g2, XYItemRendererState state,
                         Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
                         final ValueAxis datasetDomainAxis, final ValueAxis datasetRangeAxis, XYDataset dataset,
                         int series, int item, CrosshairState crosshairState, int pass) {

        if (!getItemVisible(series, item)) {
            return;
        }
        // override axes:
        final ValueAxis domainAxis = getDomainAxis(datasetDomainAxis);
        final ValueAxis rangeAxis = getRangeAxis(datasetRangeAxis);

        final IntervalXYDataset intervalDataset = (IntervalXYDataset) dataset;

        final double value0;
        final double value1;
        if (this.getUseYInterval()) {
            value0 = intervalDataset.getStartYValue(series, item);
            value1 = intervalDataset.getEndYValue(series, item);
        } else {
            value0 = this.getBase();
            value1 = intervalDataset.getYValue(series, item);
        }
        if (Double.isNaN(value0) || Double.isNaN(value1)) {
            return;
        }
        if (value0 <= value1) {
            if (!rangeAxis.getRange().intersects(value0, value1)) {
                return;
            }
        } else {
            if (!rangeAxis.getRange().intersects(value1, value0)) {
                return;
            }
        }

        final RectangleEdge rangeAxisEdge = getRangeAxisEdge(plot);
        double translatedValue0 = rangeAxis.valueToJava2D(value0, dataArea, rangeAxisEdge);
        double translatedValue1 = rangeAxis.valueToJava2D(value1, dataArea, rangeAxisEdge);
        double bottom = Math.min(translatedValue0, translatedValue1);
        double top = Math.max(translatedValue0, translatedValue1);

        double startX = intervalDataset.getStartXValue(series, item);
        if (Double.isNaN(startX)) {
            return;
        }
        double endX = intervalDataset.getEndXValue(series, item);
        if (Double.isNaN(endX)) {
            return;
        }
        if (startX <= endX) {
            if (!domainAxis.getRange().intersects(startX, endX)) {
                return;
            }
        } else {
            if (!domainAxis.getRange().intersects(endX, startX)) {
                return;
            }
        }

        // is there an alignment adjustment to be made?
        if (this.getBarAlignmentFactor() >= 0.0 && this.getBarAlignmentFactor() <= 1.0) {
            double x = intervalDataset.getXValue(series, item);
            double interval = endX - startX;
            startX = x - interval * this.getBarAlignmentFactor();
            endX = startX + interval;
        }

        final RectangleEdge domainAxisEdge = getDomainAxisEdge(plot);
        double translatedStartX = domainAxis.valueToJava2D(startX, dataArea, domainAxisEdge);
        double translatedEndX = domainAxis.valueToJava2D(endX, dataArea, domainAxisEdge);

        double translatedWidth = Math.max(1, Math.abs(translatedEndX - translatedStartX));

        double left = Math.min(translatedStartX, translatedEndX);
        if (getMargin() > 0.0) {
            double cut = translatedWidth * getMargin();
            translatedWidth = translatedWidth - cut;
            left = left + cut / 2;
        }

        Rectangle2D bar = null;
        PlotOrientation orientation = getPlotOrientation(plot); // plot.getOrientation();
        if (orientation.isHorizontal()) {
            // clip left and right bounds to data area
            bottom = Math.max(bottom, dataArea.getMinX());
            top = Math.min(top, dataArea.getMaxX());
            bar = new Rectangle2D.Double(bottom, left, top - bottom, translatedWidth);
        } else if (orientation.isVertical()) {
            // clip top and bottom bounds to data area
            bottom = Math.max(bottom, dataArea.getMinY());
            top = Math.min(top, dataArea.getMaxY());
            bar = new Rectangle2D.Double(left, bottom, translatedWidth, top - bottom);
        }

        boolean positive = (value1 > 0.0);
        boolean inverted = rangeAxis.isInverted();
        RectangleEdge barBase;
        if (orientation.isHorizontal()) {
            if (positive && inverted || !positive && !inverted) {
                barBase = RectangleEdge.RIGHT;
            } else {
                barBase = RectangleEdge.LEFT;
            }
        } else {
            if (positive && !inverted || !positive && inverted) {
                barBase = RectangleEdge.BOTTOM;
            } else {
                barBase = RectangleEdge.TOP;
            }
        }

        if (state.getElementHinting()) {
            beginElementGroup(g2, dataset.getSeriesKey(series), item);
        }
        if (getShadowsVisible()) {
            this.getBarPainter().paintBarShadow(g2, this, series, item, bar, barBase, !this.getUseYInterval());
        }
        this.getBarPainter().paintBar(g2, this, series, item, bar, barBase);
        if (state.getElementHinting()) {
            endElementGroup(g2);
        }

        if (isItemLabelVisible(series, item)) {
            XYItemLabelGenerator generator = getItemLabelGenerator(series, item);
            drawItemLabel(g2, dataset, series, item, plot, generator, bar, value1 < 0.0);
        }

        // update the crosshair point
        double x1 = (startX + endX) / 2.0;
        double y1 = dataset.getYValue(series, item);
        double transX1 = domainAxis.valueToJava2D(x1, dataArea, domainAxisEdge);
        double transY1 = rangeAxis.valueToJava2D(y1, dataArea, rangeAxisEdge);
        int datasetIndex = plot.indexOf(dataset);
        updateCrosshairValues(crosshairState, x1, y1, datasetIndex, transX1, transY1, orientation); // plot.getOrientation()

        EntityCollection entities = state.getEntityCollection();
        if (entities != null) {
            addEntity(entities, bar, dataset, series, item, 0.0, 0.0);
        }
    }
}
