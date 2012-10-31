/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.chart;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ObjectUtilities;
import org.jfree.util.PaintUtilities;

/**
 * This class extends XYErrorRenderer for performance
 * @author bourgesl
 */
public final class FastXYErrorRenderer extends FastXYLineAndShapeRenderer /* XYLineAndShapeRenderer */ {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1L;

    /* member */
    /** A flag that controls whether or not the x-error bars are drawn. */
    private boolean drawXError;
    /** A flag that controls whether or not the y-error bars are drawn. */
    private boolean drawYError;
    /** The length of the cap at the end of the error bars. */
    private double capLength;
    /**
     * The paint used to draw the error bars (if <code>null</code> we use the
     * series paint).
     */
    private transient Paint errorPaint;
    /**
     * The stroke used to draw the error bars (if <code>null</code> we use the
     * series outline stroke).
     *
     * @since 1.0.13
     */
    private transient Stroke errorStroke;
    /** flag to draw cap */
    private transient boolean useCap;

    /**
     * Creates a new <code>XYErrorRenderer</code> instance.
     */
    public FastXYErrorRenderer() {
        super(false, true);
        this.drawXError = true;
        this.drawYError = true;
        this.errorPaint = null;
        this.errorStroke = null;
        setCapLength(4d);
    }

    /**
     * Returns the flag that controls whether or not the renderer draws error
     * bars for the x-values.
     *
     * @return A boolean.
     *
     * @see #setDrawXError(boolean)
     */
    public boolean getDrawXError() {
        return this.drawXError;
    }

    /**
     * Sets the flag that controls whether or not the renderer draws error
     * bars for the x-values and, if the flag changes, sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param draw  the flag value.
     *
     * @see #getDrawXError()
     */
    public void setDrawXError(final boolean draw) {
        if (this.drawXError != draw) {
            this.drawXError = draw;
            fireChangeEvent();
        }
    }

    /**
     * Returns the flag that controls whether or not the renderer draws error
     * bars for the y-values.
     *
     * @return A boolean.
     *
     * @see #setDrawYError(boolean)
     */
    public boolean getDrawYError() {
        return this.drawYError;
    }

    /**
     * Sets the flag that controls whether or not the renderer draws error
     * bars for the y-values and, if the flag changes, sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param draw  the flag value.
     *
     * @see #getDrawYError()
     */
    public void setDrawYError(final boolean draw) {
        if (this.drawYError != draw) {
            this.drawYError = draw;
            fireChangeEvent();
        }
    }

    /**
     * Returns the length (in Java2D units) of the cap at the end of the error
     * bars.
     *
     * @return The cap length.
     *
     * @see #setCapLength(double)
     */
    public double getCapLength() {
        return this.capLength;
    }

    /**
     * Sets the length of the cap at the end of the error bars, and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param length  the length (in Java2D units).
     *
     * @see #getCapLength()
     */
    public void setCapLength(final double length) {
        this.capLength = length;
        this.useCap = (length > 0d);
        fireChangeEvent();
    }

    /**
     * Returns the paint used to draw the error bars.  If this is
     * <code>null</code> (the default), the item paint is used instead.
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setErrorPaint(Paint)
     */
    public Paint getErrorPaint() {
        return this.errorPaint;
    }

    /**
     * Sets the paint used to draw the error bars and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @see #getErrorPaint()
     */
    public void setErrorPaint(final Paint paint) {
        this.errorPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the stroke used to draw the error bars.  If this is
     * <code>null</code> (the default), the item outline stroke is used
     * instead.
     *
     * @return The stroke (possibly <code>null</code>).
     *
     * @see #setErrorStroke(Stroke)
     *
     * @since 1.0.13
     */
    public Stroke getErrorStroke() {
        return this.errorStroke;
    }

    /**
     * Sets the stroke used to draw the error bars and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param stroke   the stroke (<code>null</code> permitted).
     *
     * @see #getErrorStroke()
     *
     * @since 1.0.13
     */
    public void setErrorStroke(final Stroke stroke) {
        this.errorStroke = stroke;
        fireChangeEvent();
    }

    /**
     * Returns the range required by this renderer to display all the domain
     * values in the specified dataset.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     *
     * @return The range, or <code>null</code> if the dataset is
     *     <code>null</code>.
     */
    @Override
    public Range findDomainBounds(final XYDataset dataset) {
        if (dataset != null) {
            return DatasetUtilities.findDomainBounds(dataset, true);
        } else {
            return null;
        }
    }

    /**
     * Returns the range required by this renderer to display all the range
     * values in the specified dataset.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     *
     * @return The range, or <code>null</code> if the dataset is
     *     <code>null</code>.
     */
    @Override
    public Range findRangeBounds(final XYDataset dataset) {
        if (dataset != null) {
            return DatasetUtilities.findRangeBounds(dataset, true);
        } else {
            return null;
        }
    }

    /**
     * Draws the visual representation for one data item.
     *
     * @param g2  the graphics output target.
     * @param renderState  the renderer state.
     * @param dataArea  the data area.
     * @param info  the plot rendering info.
     * @param plot  the plot.
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset.
     * @param series  the series index.
     * @param item  the item index.
     * @param crosshairState  the crosshair state.
     * @param pass  the pass index.
     */
    @Override
    public void drawItem(final Graphics2D g2, final XYItemRendererState renderState,
            final Rectangle2D dataArea, final PlotRenderingInfo info, final XYPlot plot,
            final ValueAxis domainAxis, final ValueAxis rangeAxis, final XYDataset dataset,
            final int series, final int item, final CrosshairState crosshairState, final int pass) {

        if (isLinePass(pass) && (drawYError || drawXError) && dataset instanceof IntervalXYDataset /* && getItemVisible(series, item) */) {
            final IntervalXYDataset ixyd = (IntervalXYDataset) dataset;

            final FastXYLineAndShapeRenderer.State state = (FastXYLineAndShapeRenderer.State) renderState;

            final PlotOrientation orientation = plot.getOrientation();

            final RectangleEdge xAxisLocation = state.xAxisLocation;
            final RectangleEdge yAxisLocation = state.yAxisLocation;

            final Paint paint = this.errorPaint;
            final Stroke stroke = this.errorStroke;
            final double adj = (this.useCap) ? 0.5d * this.getCapLength() : 0d;

            if (paint != null) {
                g2.setPaint(paint);
            } else {
                g2.setPaint(getItemPaint(series, item));
            }
            if (stroke != null) {
                g2.setStroke(stroke);
            } else {
                g2.setStroke(getItemStroke(series, item));
            }

            if (drawXError) {
                // draw the error bar for the x-interval
                final double x0 = ixyd.getStartXValue(series, item);
                final double x1 = ixyd.getEndXValue(series, item);
                final double y = ixyd.getYValue(series, item);

                if (!Double.isNaN(x0) && !Double.isNaN(x1) && !Double.isNaN(y)) {
                    final double xx0 = domainAxis.valueToJava2D(x0, dataArea, xAxisLocation);
                    final double xx1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
                    final double yy = rangeAxis.valueToJava2D(y, dataArea, yAxisLocation);

                    if (orientation == PlotOrientation.VERTICAL) {
                        state.workingLine.setLine(xx0, yy, xx1, yy);
                        g2.draw(state.workingLine);

                        if (this.useCap) {
                            state.workingLine.setLine(xx0, yy - adj, xx0, yy + adj);
                            g2.draw(state.workingLine);
                            state.workingLine.setLine(xx1, yy - adj, xx1, yy + adj);
                            g2.draw(state.workingLine);
                        }
                    } else {  // PlotOrientation.HORIZONTAL
                        state.workingLine.setLine(yy, xx0, yy, xx1);
                        g2.draw(state.workingLine);

                        if (this.useCap) {
                            state.workingLine.setLine(yy - adj, xx0, yy + adj, xx0);
                            g2.draw(state.workingLine);
                            state.workingLine.setLine(yy - adj, xx1, yy + adj, xx1);
                            g2.draw(state.workingLine);
                        }
                    }
                }
            }
            if (drawYError) {
                // draw the error bar for the y-interval
                final double y0 = ixyd.getStartYValue(series, item);
                final double y1 = ixyd.getEndYValue(series, item);
                final double x = ixyd.getXValue(series, item);

                if (!Double.isNaN(y0) && !Double.isNaN(y1) && !Double.isNaN(x)) {
                    final double yy0 = rangeAxis.valueToJava2D(y0, dataArea, yAxisLocation);
                    final double yy1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);
                    final double xx = domainAxis.valueToJava2D(x, dataArea, xAxisLocation);

                    if (orientation == PlotOrientation.VERTICAL) {
                        state.workingLine.setLine(xx, yy0, xx, yy1);
                        g2.draw(state.workingLine);

                        if (this.useCap) {
                            state.workingLine.setLine(xx - adj, yy0, xx + adj, yy0);
                            g2.draw(state.workingLine);
                            state.workingLine.setLine(xx - adj, yy1, xx + adj, yy1);
                            g2.draw(state.workingLine);
                        }
                    } else {  // PlotOrientation.HORIZONTAL
                        state.workingLine.setLine(yy0, xx, yy1, xx);
                        g2.draw(state.workingLine);

                        if (this.useCap) {
                            state.workingLine.setLine(yy0, xx - adj, yy0, xx + adj);
                            g2.draw(state.workingLine);
                            state.workingLine.setLine(yy1, xx - adj, yy1, xx + adj);
                            g2.draw(state.workingLine);
                        }
                    }
                }
            }
        }
        super.drawItem(g2, renderState, dataArea, info, plot, domainAxis, rangeAxis, dataset, series, item, crosshairState, pass);
    }

    /**
     * Returns a boolean that indicates whether or not the specified series
     * should be drawn.
     *
     * @param series  the series index.
     *
     * @return A boolean.
     */
    @Override
    public boolean isSeriesVisible(final int series) {
        return true;
    }

    /**
     * Tests this instance for equality with an arbitrary object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FastXYErrorRenderer)) {
            return false;
        }
        FastXYErrorRenderer that = (FastXYErrorRenderer) obj;
        if (this.drawXError != that.drawXError) {
            return false;
        }
        if (this.drawYError != that.drawYError) {
            return false;
        }
        if (this.capLength != that.capLength) {
            return false;
        }
        if (!PaintUtilities.equal(this.errorPaint, that.errorPaint)) {
            return false;
        }
        if (!ObjectUtilities.equal(this.errorStroke, that.errorStroke)) {
            return false;
        }
        return super.equals(obj);
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the input stream.
     *
     * @throws IOException  if there is an I/O error.
     * @throws ClassNotFoundException  if there is a classpath problem.
     */
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.errorPaint = SerialUtilities.readPaint(stream);
        this.errorStroke = SerialUtilities.readStroke(stream);
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the output stream.
     *
     * @throws IOException  if there is an I/O error.
     */
    private void writeObject(final ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint(this.errorPaint, stream);
        SerialUtilities.writeStroke(this.errorStroke, stream);
    }
}
