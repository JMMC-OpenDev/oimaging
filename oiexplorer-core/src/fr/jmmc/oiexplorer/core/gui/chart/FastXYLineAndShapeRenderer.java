/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.chart;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jfree.chart.LegendItem;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.xy.XYDataset;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PublicCloneable;
import org.jfree.util.ShapeUtilities;

/**
 *
 * @author bourgesl
 */
/**
 * A renderer that connects data points with lines and/or draws shapes at each
 * data point.  This renderer is designed for use with the {@link XYPlot}
 * class.  The example shown here is generated by
 * the <code>FastXYLineAndShapeRendererDemo2.java</code> program included in the
 * JFreeChart demo collection:
 * <br><br>
 * <img src="../../../../../images/FastXYLineAndShapeRendererSample.png"
 * alt="FastXYLineAndShapeRendererSample.png" />
 *
 */
public class FastXYLineAndShapeRenderer extends AbstractXYItemRenderer
        implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -7435246895986425885L;
    /**
     * A flag that controls whether or not lines are visible for ALL series.
     */
    private boolean linesVisible;
    /** The shape that is used to represent a line in the legend. */
    private transient Shape legendLine;
    /**
     * A flag that controls whether or not shapes are visible for ALL series.
     */
    private boolean shapesVisible;
    /**
     * A flag that controls whether or not shapes are filled for ALL series.
     */
    private boolean shapesFilled;
    /** A flag that controls whether outlines are drawn for shapes. */
    private boolean drawOutlines;
    /**
     * A flag that controls whether the fill paint is used for filling
     * shapes.
     */
    private boolean useFillPaint;
    /**
     * A flag that controls whether the outline paint is used for drawing shape
     * outlines.
     */
    private boolean useOutlinePaint;
    /**
     * A flag that controls whether or not each series is drawn as a single
     * path.
     */
    private boolean drawSeriesLineAsPath;

    /**
     * Creates a new renderer with both lines and shapes visible.
     */
    public FastXYLineAndShapeRenderer() {
        this(true, true);
    }

    /**
     * Creates a new renderer.
     *
     * @param lines  lines visible?
     * @param shapes  shapes visible?
     */
    public FastXYLineAndShapeRenderer(boolean lines, boolean shapes) {
        this.linesVisible = false;
        this.legendLine = new Line2D.Double(-7.0, 0.0, 7.0, 0.0);

        this.shapesVisible = false;

        this.shapesFilled = false;
        this.useFillPaint = false;     // use item paint for fills by default

        this.drawOutlines = true;
        this.useOutlinePaint = false;  // use item paint for outlines by
        // default, not outline paint

        this.drawSeriesLineAsPath = false;
    }

    /**
     * Returns a flag that controls whether or not each series is drawn as a
     * single path.
     *
     * @return A boolean.
     *
     * @see #setDrawSeriesLineAsPath(boolean)
     */
    public boolean getDrawSeriesLineAsPath() {
        return this.drawSeriesLineAsPath;
    }

    /**
     * Sets the flag that controls whether or not each series is drawn as a
     * single path and sends a {@link RendererChangeEvent} to all registered
     * listeners.
     *
     * @param flag  the flag.
     *
     * @see #getDrawSeriesLineAsPath()
     */
    public void setDrawSeriesLineAsPath(boolean flag) {
        if (this.drawSeriesLineAsPath != flag) {
            this.drawSeriesLineAsPath = flag;
            fireChangeEvent();
        }
    }

    /**
     * Returns the number of passes through the data that the renderer requires
     * in order to draw the chart.  Most charts will require a single pass, but
     * some require two passes.
     *
     * @return The pass count.
     */
    @Override
    public int getPassCount() {
        return 2;
    }

    // LINES VISIBLE
    /**
     * Returns the flag used to control whether or not the shape for an item is
     * visible.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return A boolean.
     */
    public final boolean getItemLineVisible(final int series, final int item) {
        return this.linesVisible;
    }

    /**
     * Returns a flag that controls whether or not lines are drawn for ALL
     * series.  If this flag is <code>null</code>, then the "per series"
     * settings will apply.
     *
     * @return A flag (possibly <code>null</code>).
     *
     * @see #setLinesVisible(Boolean)
     */
    public final boolean isLinesVisible() {
        return this.linesVisible;
    }

    /**
     * Sets a flag that controls whether or not lines are drawn between the
     * items in ALL series, and sends a {@link RendererChangeEvent} to all
     * registered listeners.
     *
     * @param visible  the flag.
     *
     * @see #getLinesVisible()
     */
    public final void setLinesVisible(final boolean visible) {
        this.linesVisible = visible;
    }

    /**
     * Returns the shape used to represent a line in the legend.
     *
     * @return The legend line (never <code>null</code>).
     *
     * @see #setLegendLine(Shape)
     */
    public Shape getLegendLine() {
        return this.legendLine;
    }

    /**
     * Sets the shape used as a line in each legend item and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param line  the line (<code>null</code> not permitted).
     *
     * @see #getLegendLine()
     */
    public void setLegendLine(Shape line) {
        if (line == null) {
            throw new IllegalArgumentException("Null 'line' argument.");
        }
        this.legendLine = line;
        fireChangeEvent();
    }

    // SHAPES VISIBLE
    /**
     * Returns the flag used to control whether or not the shape for an item is
     * visible.
     * <p>
     * The default implementation passes control to the
     * <code>getSeriesShapesVisible</code> method. You can override this method
     * if you require different behavior.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return A boolean.
     */
    public final boolean getItemShapeVisible(final int series, final int item) {
        return this.shapesVisible;
    }

    /**
     * Returns the flag that controls whether the shapes are visible for the
     * items in ALL series.
     *
     * @return The flag (possibly <code>null</code>).
     *
     * @see #setShapesVisible(Boolean)
     */
    public boolean getShapesVisible() {
        return this.shapesVisible;
    }

    /**
     * Sets the shapes to visible for ALL series and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param visible  the flag (<code>null</code> permitted).
     *
     * @see #getShapesVisible()
     */
    public void setShapesVisible(boolean visible) {
        this.shapesVisible = visible;
        fireChangeEvent();
    }

    // SHAPES FILLED
    /**
     * Returns the flag used to control whether or not the shape for an item
     * is filled.
     * <p>
     * The default implementation passes control to the
     * <code>getSeriesShapesFilled</code> method. You can override this method
     * if you require different behaviour.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return A boolean.
     */
    public final boolean getItemShapeFilled(final int series, final int item) {
        return this.shapesFilled;
    }

    /**
     * Sets the 'shapes filled' for ALL series and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param filled  the flag (<code>null</code> permitted).
     */
    public void setShapesFilled(boolean filled) {
        this.shapesFilled = filled;
        fireChangeEvent();
    }

    /**
     * Returns <code>true</code> if outlines should be drawn for shapes, and
     * <code>false</code> otherwise.
     *
     * @return A boolean.
     *
     * @see #setDrawOutlines(boolean)
     */
    public boolean getDrawOutlines() {
        return this.drawOutlines;
    }

    /**
     * Sets the flag that controls whether outlines are drawn for
     * shapes, and sends a {@link RendererChangeEvent} to all registered
     * listeners.
     * <P>
     * In some cases, shapes look better if they do NOT have an outline, but
     * this flag allows you to set your own preference.
     *
     * @param flag  the flag.
     *
     * @see #getDrawOutlines()
     */
    public void setDrawOutlines(boolean flag) {
        this.drawOutlines = flag;
        fireChangeEvent();
    }

    /**
     * Returns <code>true</code> if the renderer should use the fill paint
     * setting to fill shapes, and <code>false</code> if it should just
     * use the regular paint.
     * <p>
     * Refer to <code>FastXYLineAndShapeRendererDemo2.java</code> to see the
     * effect of this flag.
     *
     * @return A boolean.
     *
     * @see #setUseFillPaint(boolean)
     * @see #getUseOutlinePaint()
     */
    public boolean getUseFillPaint() {
        return this.useFillPaint;
    }

    /**
     * Sets the flag that controls whether the fill paint is used to fill
     * shapes, and sends a {@link RendererChangeEvent} to all
     * registered listeners.
     *
     * @param flag  the flag.
     *
     * @see #getUseFillPaint()
     */
    public void setUseFillPaint(boolean flag) {
        this.useFillPaint = flag;
        fireChangeEvent();
    }

    /**
     * Returns <code>true</code> if the renderer should use the outline paint
     * setting to draw shape outlines, and <code>false</code> if it should just
     * use the regular paint.
     *
     * @return A boolean.
     *
     * @see #setUseOutlinePaint(boolean)
     * @see #getUseFillPaint()
     */
    public boolean getUseOutlinePaint() {
        return this.useOutlinePaint;
    }

    /**
     * Sets the flag that controls whether the outline paint is used to draw
     * shape outlines, and sends a {@link RendererChangeEvent} to all
     * registered listeners.
     * <p>
     * Refer to <code>FastXYLineAndShapeRendererDemo2.java</code> to see the
     * effect of this flag.
     *
     * @param flag  the flag.
     *
     * @see #getUseOutlinePaint()
     */
    public void setUseOutlinePaint(boolean flag) {
        this.useOutlinePaint = flag;
        fireChangeEvent();
    }

    /**
     * Records the state for the renderer.  This is used to preserve state
     * information between calls to the drawItem() method for a single chart
     * drawing.
     */
    public static class State extends XYItemRendererState {

        /** The path for the current series. */
        GeneralPath seriesPath;
        /**
         * A flag that indicates if the last (x, y) point was 'good'
         * (non-null).
         */
        private boolean lastPointGood;

        /**
         * Creates a new state instance.
         *
         * @param info  the plot rendering info.
         */
        public State(PlotRenderingInfo info) {
            super(info);
        }

        /**
         * Returns a flag that indicates if the last point drawn (in the
         * current series) was 'good' (non-null).
         *
         * @return A boolean.
         */
        public boolean isLastPointGood() {
            return this.lastPointGood;
        }

        /**
         * Sets a flag that indicates if the last point drawn (in the current
         * series) was 'good' (non-null).
         *
         * @param good  the flag.
         */
        public void setLastPointGood(boolean good) {
            this.lastPointGood = good;
        }

        /**
         * This method is called by the {@link XYPlot} at the start of each
         * series pass.  We reset the state for the current series.
         *
         * @param dataset  the dataset.
         * @param series  the series index.
         * @param firstItem  the first item index for this pass.
         * @param lastItem  the last item index for this pass.
         * @param pass  the current pass index.
         * @param passCount  the number of passes.
         */
        @Override
        public void startSeriesPass(XYDataset dataset, int series,
                int firstItem, int lastItem, int pass, int passCount) {
            this.seriesPath.reset();
            this.lastPointGood = false;
            super.startSeriesPass(dataset, series, firstItem, lastItem, pass, passCount);
        }
        /** cached Translate Graphics2D AffineTransform */
        transient AffineTransform g2AT;
        /** cached xAxis location */
        transient RectangleEdge xAxisLocation;
        /** cached yAxis location */
        transient RectangleEdge yAxisLocation;
    }

    /**
     * Initialises the renderer.
     * <P>
     * This method will be called before the first item is rendered, giving the
     * renderer an opportunity to initialise any state information it wants to
     * maintain.  The renderer can do nothing if it chooses.
     *
     * @param g2  the graphics device.
     * @param dataArea  the area inside the axes.
     * @param plot  the plot.
     * @param data  the data.
     * @param info  an optional info collection object to return data back to
     *              the caller.
     *
     * @return The renderer state.
     */
    @Override
    public XYItemRendererState initialise(Graphics2D g2,
            Rectangle2D dataArea,
            XYPlot plot,
            XYDataset data,
            PlotRenderingInfo info) {

        final FastXYLineAndShapeRenderer.State state = new FastXYLineAndShapeRenderer.State(info);
        state.seriesPath = new GeneralPath();

        // not very efficient with the FastIntervalXYDataset:
        state.setProcessVisibleItemsOnly(false);

        state.g2AT = g2.getTransform();
        state.xAxisLocation = plot.getDomainAxisEdge();
        state.yAxisLocation = plot.getRangeAxisEdge();

        return state;
    }

    /**
     * Draws the visual representation of a single data item.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.Draws
     * @param dataArea  the area within which the data is being drawn.
     * @param info  collects information about the drawing.
     * @param plot  the plot (can be used to obtain standard color
     *              information etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param crosshairState  crosshair information for the plot
     *                        (<code>null</code> permitted).
     * @param pass  the pass index.
     */
    @Override
    public void drawItem(Graphics2D g2,
            XYItemRendererState state,
            Rectangle2D dataArea,
            PlotRenderingInfo info,
            XYPlot plot,
            ValueAxis domainAxis,
            ValueAxis rangeAxis,
            XYDataset dataset,
            int series,
            int item,
            CrosshairState crosshairState,
            int pass) {

        // do nothing if item is not visible
        if (!getItemVisible(series, item)) {
            return;
        }

        // first pass draws the background (lines, for instance)
        if (isLinePass(pass)) {
            if (getItemLineVisible(series, item)) {
                if (this.drawSeriesLineAsPath) {
                    drawPrimaryLineAsPath(state, g2, plot, dataset, pass,
                            series, item, domainAxis, rangeAxis, dataArea);
                } else {
                    drawPrimaryLine((FastXYLineAndShapeRenderer.State) state, g2, plot, dataset, pass, series,
                            item, domainAxis, rangeAxis, dataArea);
                }
            }
        } // second pass adds shapes where the items are ..
        else if (isItemPass(pass)) {

            // setup for collecting optional entity info...
            EntityCollection entities = null;
            if (info != null) {
                entities = info.getOwner().getEntityCollection();
            }

            drawSecondaryPass((FastXYLineAndShapeRenderer.State) state, g2, plot, dataset, pass, series, item,
                    domainAxis, dataArea, rangeAxis, crosshairState, entities);
        }
    }

    /**
     * Returns <code>true</code> if the specified pass is the one for drawing
     * lines.
     *
     * @param pass  the pass.
     *
     * @return A boolean.
     */
    protected final boolean isLinePass(final int pass) {
        return pass == 0;
    }

    /**
     * Returns <code>true</code> if the specified pass is the one for drawing
     * items.
     *
     * @param pass  the pass.
     *
     * @return A boolean.
     */
    protected final boolean isItemPass(final int pass) {
        return pass == 1;
    }

    /**
     * Draws the item (first pass). This method draws the lines
     * connecting the items.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area within which the data is being drawn.
     * @param plot  the plot (can be used to obtain standard color
     *              information etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param pass  the pass.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     */
    protected void drawPrimaryLine(FastXYLineAndShapeRenderer.State state,
            Graphics2D g2,
            XYPlot plot,
            XYDataset dataset,
            int pass,
            int series,
            int item,
            ValueAxis domainAxis,
            ValueAxis rangeAxis,
            Rectangle2D dataArea) {
        if (item == 0) {
            return;
        }

        // get the data point...
        final double x1 = dataset.getXValue(series, item);
        final double y1 = dataset.getYValue(series, item);
        if (Double.isNaN(y1) || Double.isNaN(x1)) {
            return;
        }

        final double x0 = dataset.getXValue(series, item - 1);
        final double y0 = dataset.getYValue(series, item - 1);
        if (Double.isNaN(y0) || Double.isNaN(x0)) {
            return;
        }

        final RectangleEdge xAxisLocation = state.xAxisLocation;
        final RectangleEdge yAxisLocation = state.yAxisLocation;

        final double transX0 = domainAxis.valueToJava2D(x0, dataArea, xAxisLocation);
        final double transY0 = rangeAxis.valueToJava2D(y0, dataArea, yAxisLocation);

        final double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
        final double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

        // only draw if we have good values
        if (Double.isNaN(transX0) || Double.isNaN(transY0)
                || Double.isNaN(transX1) || Double.isNaN(transY1)) {
            return;
        }

        PlotOrientation orientation = plot.getOrientation();
        if (orientation == PlotOrientation.HORIZONTAL) {
            state.workingLine.setLine(transY0, transX0, transY1, transX1);
        } else if (orientation == PlotOrientation.VERTICAL) {
            state.workingLine.setLine(transX0, transY0, transX1, transY1);
        }

        if (state.workingLine.intersects(dataArea)) {
            drawFirstPassShape(g2, pass, series, item, state.workingLine);
        }
    }

    /**
     * Draws the first pass shape.
     *
     * @param g2  the graphics device.
     * @param pass  the pass.
     * @param series  the series index.
     * @param item  the item index.
     * @param shape  the shape.
     */
    protected void drawFirstPassShape(Graphics2D g2, int pass, int series, int item, Shape shape) {
        g2.setStroke(getItemStroke(series, item));
        g2.setPaint(getItemPaint(series, item));
        g2.draw(shape);
    }

    /**
     * Draws the item (first pass). This method draws the lines
     * connecting the items. Instead of drawing separate lines,
     * a GeneralPath is constructed and drawn at the end of
     * the series painting.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param plot  the plot (can be used to obtain standard color information
     *              etc).
     * @param dataset  the dataset.
     * @param pass  the pass.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataArea  the area within which the data is being drawn.
     */
    protected void drawPrimaryLineAsPath(XYItemRendererState state,
            Graphics2D g2, XYPlot plot,
            XYDataset dataset,
            int pass,
            int series,
            int item,
            ValueAxis domainAxis,
            ValueAxis rangeAxis,
            Rectangle2D dataArea) {


        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

        // get the data point...
        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);
        double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
        double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

        FastXYLineAndShapeRenderer.State s = (FastXYLineAndShapeRenderer.State) state;

        // update path to reflect latest point
        if (!Double.isNaN(transX1) && !Double.isNaN(transY1)) {
            float x = (float) transX1;
            float y = (float) transY1;
            PlotOrientation orientation = plot.getOrientation();
            if (orientation == PlotOrientation.HORIZONTAL) {
                x = (float) transY1;
                y = (float) transX1;
            }
            if (s.isLastPointGood()) {
                s.seriesPath.lineTo(x, y);
            } else {
                s.seriesPath.moveTo(x, y);
            }
            s.setLastPointGood(true);
        } else {
            s.setLastPointGood(false);
        }
        // if this is the last item, draw the path ...
        if (item == s.getLastItemIndex()) {
            // draw path
            drawFirstPassShape(g2, pass, series, item, s.seriesPath);
        }
    }

    /**
     * Draws the item shapes and adds chart entities (second pass). This method
     * draws the shapes which mark the item positions. If <code>entities</code>
     * is not <code>null</code> it will be populated with entity information
     * for points that fall within the data area.
     *
     * @param state  the renderer state.
     * @param g2  the graphics device.
     * @param plot  the plot (can be used to obtain standard color
     *              information etc).
     * @param domainAxis  the domain axis.
     * @param dataArea  the area within which the data is being drawn.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param pass  the pass.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param crosshairState  the crosshair state.
     * @param entities the entity collection.
     */
    protected void drawSecondaryPass(FastXYLineAndShapeRenderer.State state,
            Graphics2D g2, XYPlot plot,
            XYDataset dataset,
            int pass, int series, int item,
            ValueAxis domainAxis,
            Rectangle2D dataArea,
            ValueAxis rangeAxis,
            CrosshairState crosshairState,
            EntityCollection entities) {

        // Note: entities are disabled for performance !
        Shape entityArea = null;

        // get the data point...
        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);
        if (Double.isNaN(y1) || Double.isNaN(x1)) {
            return;
        }

        final PlotOrientation orientation = plot.getOrientation();
        final RectangleEdge xAxisLocation = state.xAxisLocation;
        final RectangleEdge yAxisLocation = state.yAxisLocation;

        final double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
        final double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

        if (getItemShapeVisible(series, item)) {
            final Shape shape = getItemShape(series, item);

            if (shape != null) {

                final boolean visible;
                if (orientation == PlotOrientation.HORIZONTAL) {
                    visible = shape.intersects(dataArea.getX() - transY1, dataArea.getY() - transX1, dataArea.getWidth(), dataArea.getHeight());
                } else {
                    visible = shape.intersects(dataArea.getX() - transX1, dataArea.getY() - transY1, dataArea.getWidth(), dataArea.getHeight());
                }
                if (visible) {
                    // Perform transformation
                    if (orientation == PlotOrientation.HORIZONTAL) {
                        g2.translate(transY1, transX1);
                    } else if (orientation == PlotOrientation.VERTICAL) {
                        g2.translate(transX1, transY1);
                    }

                    entityArea = shape;

                    if (getItemShapeFilled(series, item)) {
                        if (this.useFillPaint) {
                            g2.setPaint(getItemFillPaint(series, item));
                        } else {
                            g2.setPaint(getItemPaint(series, item));
                        }
                        g2.fill(shape);
                    }
                    if (this.drawOutlines) {
                        if (getUseOutlinePaint()) {
                            g2.setPaint(getItemOutlinePaint(series, item));
                        } else {
                            g2.setPaint(getItemPaint(series, item));
                        }
                        g2.setStroke(getItemOutlineStroke(series, item));
                        g2.draw(shape);
                    }

                    // Restore original transform (LBO)
                    g2.setTransform(state.g2AT);
                }
            }
        }

        double xx = transX1;
        double yy = transY1;
        if (orientation == PlotOrientation.HORIZONTAL) {
            xx = transY1;
            yy = transX1;
        }

        // draw the item label if there is one...
        if (isItemLabelVisible(series, item)) {
            drawItemLabel(g2, orientation, dataset, series, item, xx, yy, (y1 < 0.0));
        }

        // LBO: disable updateCrosshairValues

        // add an entity for the item, but only if it falls within the data
        // area...
        if (entities != null && isPointInRect(dataArea, xx, yy)) {
            addEntity(entities, entityArea, dataset, series, item, xx, yy);
        }
    }

    /**
     * Returns a legend item for the specified series.
     *
     * @param datasetIndex  the dataset index (zero-based).
     * @param series  the series index (zero-based).
     *
     * @return A legend item for the series.
     */
    @Override
    public LegendItem getLegendItem(int datasetIndex, int series) {

        XYPlot plot = getPlot();
        if (plot == null) {
            return null;
        }

        LegendItem result = null;
        XYDataset dataset = plot.getDataset(datasetIndex);
        if (dataset != null) {
            if (getItemVisible(series, 0)) {
                String label = getLegendItemLabelGenerator().generateLabel(
                        dataset, series);
                String description = label;
                String toolTipText = null;
                if (getLegendItemToolTipGenerator() != null) {
                    toolTipText = getLegendItemToolTipGenerator().generateLabel(
                            dataset, series);
                }
                String urlText = null;
                if (getLegendItemURLGenerator() != null) {
                    urlText = getLegendItemURLGenerator().generateLabel(
                            dataset, series);
                }
                boolean shapeIsVisible = getItemShapeVisible(series, 0);
                Shape shape = lookupLegendShape(series);
                boolean shapeIsFilled = getItemShapeFilled(series, 0);
                Paint fillPaint = (this.useFillPaint
                        ? lookupSeriesFillPaint(series)
                        : lookupSeriesPaint(series));
                boolean shapeOutlineVisible = this.drawOutlines;
                Paint outlinePaint = (this.useOutlinePaint
                        ? lookupSeriesOutlinePaint(series)
                        : lookupSeriesPaint(series));
                Stroke outlineStroke = lookupSeriesOutlineStroke(series);
                boolean lineVisible = getItemLineVisible(series, 0);
                Stroke lineStroke = lookupSeriesStroke(series);
                Paint linePaint = lookupSeriesPaint(series);
                result = new LegendItem(label, description, toolTipText,
                        urlText, shapeIsVisible, shape, shapeIsFilled,
                        fillPaint, shapeOutlineVisible, outlinePaint,
                        outlineStroke, lineVisible, this.legendLine,
                        lineStroke, linePaint);
                result.setLabelFont(lookupLegendTextFont(series));
                Paint labelPaint = lookupLegendTextPaint(series);
                if (labelPaint != null) {
                    result.setLabelPaint(labelPaint);
                }
                result.setSeriesKey(dataset.getSeriesKey(series));
                result.setSeriesIndex(series);
                result.setDataset(dataset);
                result.setDatasetIndex(datasetIndex);
            }
        }

        return result;

    }

    /**
     * Returns a clone of the renderer.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if the clone cannot be created.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        FastXYLineAndShapeRenderer clone = (FastXYLineAndShapeRenderer) super.clone();
        if (this.legendLine != null) {
            clone.legendLine = ShapeUtilities.clone(this.legendLine);
        }
        return clone;
    }

    /**
     * Tests this renderer for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return <code>true</code> or <code>false</code>.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FastXYLineAndShapeRenderer)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        FastXYLineAndShapeRenderer that = (FastXYLineAndShapeRenderer) obj;
        if (!ObjectUtilities.equal(this.linesVisible, that.linesVisible)) {
            return false;
        }
        if (!ShapeUtilities.equal(this.legendLine, that.legendLine)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.shapesVisible, that.shapesVisible)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.shapesFilled, that.shapesFilled)) {
            return false;
        }
        if (this.drawOutlines != that.drawOutlines) {
            return false;
        }
        if (this.useOutlinePaint != that.useOutlinePaint) {
            return false;
        }
        if (this.useFillPaint != that.useFillPaint) {
            return false;
        }
        if (this.drawSeriesLineAsPath != that.drawSeriesLineAsPath) {
            return false;
        }
        return true;
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the input stream.
     *
     * @throws IOException  if there is an I/O error.
     * @throws ClassNotFoundException  if there is a classpath problem.
     */
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.legendLine = SerialUtilities.readShape(stream);
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the output stream.
     *
     * @throws IOException  if there is an I/O error.
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writeShape(this.legendLine, stream);
    }

    // ITEM VISIBLE
    /**
     * Returns a boolean that indicates whether or not the specified item
     * should be drawn (this is typically used to hide an entire series).
     *
     * @param series  the series index.
     * @param item  the item index.
     *
     * @return A boolean.
     */
    @Override
    public final boolean getItemVisible(final int series, final int item) {
        return true;
    }

    // ITEM LABEL VISIBILITY...
    /**
     * Returns <code>true</code> if an item label is visible, and
     * <code>false</code> otherwise.
     *
     * @param row  the row index (zero-based).
     * @param column  the column index (zero-based).
     *
     * @return A boolean.
     */
    @Override
    public final boolean isItemLabelVisible(final int row, final int column) {
        return false;
    }
    // SHAPE

    /**
     * Returns a shape used to represent a data item.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The shape (never <code>null</code>).
     */
    @Override
    public final Shape getItemShape(final int series, final int item) {
        // TODO: use shape per [serie, item]
        return getBaseShape();
    }
}
