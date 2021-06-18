/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.chart;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
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
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.util.PublicCloneable;
import org.jfree.chart.util.SerialUtils;
import org.jfree.chart.util.ShapeUtils;

/**
 * A renderer that connects data points with paths (until NaN found).
 * This renderer is designed for use with the {@link XYPlot}
 * class.
 */
public class FastXYPathAreaRenderer extends AbstractXYItemRenderer
        implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -7435246895986425885L;
    /** flag to test shape intersection or only data point vs data area */
    private static final boolean useShapeIntersection = false;
    /** flag to show entity area (i.e. stroked line area) */
    private static final boolean debugEntityArea = false;

    /** The shape that is used to represent a line in the legend. */
    private transient Shape legendLine;
    /**
     * A flag that controls whether the fill paint is used for filling
     * shapes.
     */
    private boolean useFillPaint;
    /** The paint list. */
    private final FastPaintList paintList;
    /** item paints list */
    private final FastItemPaintsList itemPaintsList;

    /**
     * Creates a new renderer
     */
    public FastXYPathAreaRenderer() {
        super();
        this.legendLine = new Line2D.Double(-7.0, 0.0, 7.0, 0.0);

        this.useFillPaint = false;     // use item paint for fills by default

        // paint and shape lists:
        paintList = new FastPaintList();
        itemPaintsList = new FastItemPaintsList();
    }

    /**
     * Increases the capacity of the paint and item shapes lists, if
     * necessary, to ensure that it can hold at least the number of elements
     * specified by the minimum capacity argument.
     *
     * @param   minCapacity   the desired minimum capacity
     */
    public void ensureCapacity(final int minCapacity) {
        paintList.ensureCapacity(minCapacity);
        itemPaintsList.ensureCapacity(minCapacity);
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
        return 1;
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
     * Records the state for the renderer.  This is used to preserve state
     * information between calls to the drawItem() method for a single chart
     * drawing.
     */
    protected static class State extends XYItemRendererState {

        public final static int POLYGON_CAPACITY = 4;

        /** The path for the current series. */
        Path2D.Double seriesPath = null;
        /**
         * A flag that indicates if the last (x, y) point was 'good'
         * (non-null).
         */
        boolean lastPointGood;

        /* temporary array to traverse path */
        final double[] coords = new double[6];

        /* temporary arrays for entity polygon */
        int[] xp = new int[POLYGON_CAPACITY];
        int[] yp = new int[POLYGON_CAPACITY];

        /**
         * Creates a new state instance.
         *
         * @param info  the plot rendering info.
         */
        protected State(PlotRenderingInfo info) {
            super(info);
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
            resetState();
            super.startSeriesPass(dataset, series, firstItem, lastItem, pass, passCount);
        }
        /** cached Translate Graphics2D AffineTransform */
        transient AffineTransform g2AT;
        /** cached xAxis location */
        transient RectangleEdge xAxisLocation;
        /** cached yAxis location */
        transient RectangleEdge yAxisLocation;

        void resetState() {
            if (this.seriesPath != null) {
                this.seriesPath.reset();
            }
            this.lastPointGood = false;
        }
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

        final FastXYPathAreaRenderer.State state = new FastXYPathAreaRenderer.State(info);
        state.seriesPath = new Path2D.Double();

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
    public void drawItem(final Graphics2D g2,
                         final XYItemRendererState state,
                         final Rectangle2D dataArea,
                         final PlotRenderingInfo info,
                         final XYPlot plot,
                         final ValueAxis domainAxis,
                         final ValueAxis rangeAxis,
                         final XYDataset dataset,
                         final int series,
                         final int item,
                         final CrosshairState crosshairState,
                         final int pass) {

        // setup for collecting optional entity info...
        EntityCollection entities = null;
        if (info != null) {
            entities = info.getOwner().getEntityCollection();
        }

        // Fills the path formed by series data:
        // get the data point...
        final double x1 = dataset.getXValue(series, item);
        final double y1 = dataset.getYValue(series, item);

        final double transX1 = domainAxis.valueToJava2D(x1, dataArea, plot.getDomainAxisEdge());
        final double transY1 = rangeAxis.valueToJava2D(y1, dataArea, plot.getRangeAxisEdge());

        final FastXYPathAreaRenderer.State s = (FastXYPathAreaRenderer.State) state;

        // update path to reflect latest point
        boolean draw = false;
        
        if (!Double.isNaN(transX1) && !Double.isNaN(transY1)) {
            double x = transX1;
            double y = transY1;
            if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
                x = transY1;
                y = transX1;
            }
            if (s.lastPointGood) {
                s.seriesPath.lineTo(x, y);
            } else {
                s.lastPointGood = true;
                s.seriesPath.moveTo(x, y);
            }
        } else {
            // do close path:
            if (s.lastPointGood) {
                draw = true;
            }
        }
        // if draw the path ...
        if (draw /* || (s.lastPointGood && (item == s.getLastItemIndex())) */) {

            // Note: Shape.intersects() may be slow for GeneralPath (Path2D) and bounding box may be helpful
            
            // Shape intersect may be slow:
            final boolean visible;
            if (useShapeIntersection) {
                visible = s.seriesPath.intersects(dataArea.getX(), dataArea.getY(), dataArea.getWidth(), dataArea.getHeight());
            } else {
                visible = dataArea.intersects(s.seriesPath.getBounds2D()); // TODO: compute the bounding box on the fly ?
            }

            if (visible) {
                // draw path
                fillPath(g2, series, item, s.seriesPath);

                // add an entity for the line, but only if it falls within the data area...
                if (entities != null) {
                    final Shape entityArea = createEntityShape(s.seriesPath, s);

                    if (debugEntityArea) {
                        g2.setColor(Color.PINK);
                        g2.draw(entityArea);
                    }

                    // note: item corresponds to last point:
                    addEntity(entities, entityArea, dataset, series, item, Double.NaN, Double.NaN);
                }
            }

            // reset:
            s.resetState();
        }
    }

    /**
     * Fill the pass shape.
     *
     * @param g2  the graphics device.
     * @param series  the series index.
     * @param item  the item index.
     * @param shape  the shape.
     */
    private void fillPath(final Graphics2D g2, final int series, final int item, final Shape shape) {
        g2.setStroke(getItemStroke(series, item));
        g2.setPaint(getItemPaint(series, item));
        g2.fill(shape);
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
                Paint fillPaint = (this.useFillPaint
                        ? lookupSeriesFillPaint(series)
                        : lookupSeriesPaint(series));
                Paint outlinePaint = lookupSeriesPaint(series);
                Stroke outlineStroke = lookupSeriesOutlineStroke(series);
                boolean lineVisible = true;
                Stroke lineStroke = lookupSeriesStroke(series);
                Paint linePaint = lookupSeriesPaint(series);
                result = new LegendItem(label, description, toolTipText,
                        urlText, false, null, false,
                        fillPaint, false, outlinePaint,
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
        FastXYPathAreaRenderer clone = (FastXYPathAreaRenderer) super.clone();
        if (this.legendLine != null) {
            clone.legendLine = ShapeUtils.clone(this.legendLine);
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
        if (!(obj instanceof FastXYPathAreaRenderer)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        FastXYPathAreaRenderer that = (FastXYPathAreaRenderer) obj;
        if (!ShapeUtils.equal(this.legendLine, that.legendLine)) {
            return false;
        }
        if (this.useFillPaint != that.useFillPaint) {
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
        this.legendLine = SerialUtils.readShape(stream);
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
        SerialUtils.writeShape(this.legendLine, stream);
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

    /**
     * Returns the paint used to fill an item drawn by the renderer.
     *
     * @param series  the series index (zero-based).
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setSeriesPaint(int, Paint)
     */
    @Override
    public final Paint getSeriesPaint(int series) {
        return this.paintList.getPaint(series);
    }

    /**
     * Sets the paint used for a series and sends a {@link RendererChangeEvent}
     * to all registered listeners.
     *
     * @param series  the series index (zero-based).
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @see #getSeriesPaint(int)
     */
    @Override
    public final void setSeriesPaint(int series, Paint paint) {
        setSeriesPaint(series, paint, true);
    }

    /**
     * Sets the paint used for a series and, if requested, sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param series  the series index.
     * @param paint  the paint (<code>null</code> permitted).
     * @param notify  notify listeners?
     *
     * @see #getSeriesPaint(int)
     */
    @Override
    public final void setSeriesPaint(int series, Paint paint, boolean notify) {
        this.paintList.setPaint(series, paint);
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Clears the series paint settings for this renderer and, if requested,
     * sends a {@link RendererChangeEvent} to all registered listeners.
     *
     * @param notify  notify listeners?
     *
     * @since 1.0.11
     */
    @Override
    public final void clearSeriesPaints(final boolean notify) {
        this.paintList.clear();
        if (notify) {
            fireChangeEvent();
        }
    }

    /**
     * Returns a paint used for this data item.
     *
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     *
     * @return The shape (never <code>null</code>).
     */
    @Override
    public final Paint getItemPaint(final int series, final int item) {
        // use paint per [serie, item]
        final Paint[] itemShapes = this.itemPaintsList.getItemPaints(series);
        if (itemShapes != null) {
            if (item < itemShapes.length) {
                final Paint paint = itemShapes[item];
                if (paint != null) {
                    return paint;
                }
            }
        }
        // fallback: use paint for the complete serie:
        return lookupSeriesPaint(series);
    }

    /**
     * Clears the item paints settings for this renderer
     */
    public final void clearItemPaints() {
        this.itemPaintsList.clear();
    }

    /**
     * Define the item paints settings for this renderer
     * @param series  the series index (zero-based).
     * @param itemPaints  the item paints as array (<code>null</code> permitted).
     */
    public final void setItemPaints(int series, Paint[] itemPaints) {
        this.itemPaintsList.setItemPaints(series, itemPaints);
    }

    /**
     * Create the polygon shape arround the given line with a 4px margin
     * @param path shape
     * @param s renderer state
     * @return entity shape
     */
    private static Shape createEntityShape(final Path2D.Double path, final State s) {
        final double[] coords = s.coords;
        int[] xp = s.xp;
        int[] yp = s.yp;

        int segType, x, y, i = 0;

        for (final PathIterator pi = path.getPathIterator(null); !pi.isDone(); pi.next()) {
            segType = pi.currentSegment(coords);

            if (segType != PathIterator.SEG_LINETO
                    && segType != PathIterator.SEG_MOVETO) {
                continue;
            }
            x = (int) Math.round(coords[0]);
            y = (int) Math.round(coords[1]);

            // ensure capacity
            if (i >= xp.length) {
                // resize cached arrays:
                final int newLen = xp.length * 2;
                s.xp = xp = Arrays.copyOf(xp, newLen);
                s.yp = yp = Arrays.copyOf(yp, newLen);
            }
            xp[i] = x;
            yp[i] = y;
            i++;
        }
        return new Polygon(xp, yp, i);
    }
}
