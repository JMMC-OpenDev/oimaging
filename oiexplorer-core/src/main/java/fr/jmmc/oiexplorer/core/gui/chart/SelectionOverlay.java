/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.chart;

import fr.jmmc.jmcs.util.ImageUtils;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Transparency;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import javax.swing.ImageIcon;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.panel.AbstractOverlay;
import org.jfree.chart.panel.Overlay;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.jfree.ui.RectangleEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This overlay handles rectangular, polygon and free hand data selection using the mouse
 * @author bourgesl
 */
public final class SelectionOverlay extends AbstractOverlay implements Overlay, EnhancedChartMouseListener, ChartMouseSelectionListener /*  , PublicCloneable, Cloneable, Serializable */ {

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(SelectionOverlay.class.getName());
    /** Common resource directory containing icon files */
    private final static String IMAGE_RESOURCE_COMMON_PATH = "fr/jmmc/oiexplorer/core/resource/image/";
    private final static double margin = 4d;
    private final static ImageIcon imgZoom;
    private final static ImageIcon imgSelRect;
    private final static ImageIcon imgSelPolygon;
    /** default draw stroke */
    public static final Stroke SELECT_STROKE = new BasicStroke(2.0f);

    static {
        imgZoom = ImageUtils.loadResourceIcon(IMAGE_RESOURCE_COMMON_PATH + "Zoom24.gif");
        imgSelRect = ImageUtils.loadResourceIcon(IMAGE_RESOURCE_COMMON_PATH + "region.gif");
        imgSelPolygon = ImageUtils.loadResourceIcon(IMAGE_RESOURCE_COMMON_PATH + "polygon.gif");
    }

    private enum ToolMode {

        ZOOM, SELECT_RECTANGLE, SELECT_POLYGON
    }

    /* members */
    private final EnhancedChartPanel chartPanel;
    /** ChartMouseSelectionListener which handles rectangular mouse selection event */
    private ChartMouseSelectionListener mouseRectangularSelectionEventListener = null;
    private ToolMode mode = ToolMode.ZOOM;
    /* graphic components */
    private final Rectangle2D rectZoom;
    private final Rectangle2D rectSelRect;
    private final Rectangle2D rectSelPolygon;
    /** A flag that controls whether or not the off-screen buffer is used. */
    private boolean useBuffer = false;
    /** A flag that indicates that the buffer should be refreshed. */
    private boolean refreshBuffer;
    /** A buffer for the rendered chart. */
    private transient Image chartBuffer;
    /** The height of the chart buffer. */
    private int chartBufferHeight;
    /** The width of the chart buffer. */
    private int chartBufferWidth;
    /** the selected rectangle region in a plot or subplot */
    private SelectedSubPlotArea selectedArea;
    /* selected data points */
    private List<Point2D> points = null;
    /* cache of x axis range to detect axis changes */
    private Range xAxisRange = null;
    /* cache of y axis range to detect axis changes */
    private Range yAxisRange = null;

    public SelectionOverlay(final ChartPanel chartPanel, final ChartMouseSelectionListener mouseRectangularSelectionEventListener) {
        super();

        // TODO: check cast
        this.chartPanel = (EnhancedChartPanel) chartPanel;
        this.mouseRectangularSelectionEventListener = mouseRectangularSelectionEventListener;

        this.rectZoom = new Rectangle2D.Double(0d, 0d, imgZoom.getIconWidth() + margin, imgZoom.getIconHeight() + margin);
        this.rectSelRect = new Rectangle2D.Double(this.rectZoom.getMaxX() + 1d, 0d, imgSelRect.getIconWidth() + margin, imgSelRect.getIconHeight() + margin);
        this.rectSelPolygon = new Rectangle2D.Double(this.rectSelRect.getMaxX() + 1d, 0d, imgSelPolygon.getIconWidth() + +margin, imgSelPolygon.getIconHeight() + +margin);

        // finish setup:
        this.chartPanel.addChartMouseListener(this);
    }

    /**
     * Reset the selection state
     */
    public void reset() {
        this.selectedArea = null;
        this.points = null;
        this.refreshBuffer = true;
        this.xAxisRange = null;
        this.yAxisRange = null;
    }

    @Override
    public void paintOverlay(final Graphics2D g2, final ChartPanel chartPanel) {
        final long startTime = System.nanoTime();

        final boolean doPaint;

        // are we using the chart buffer or is there selected points ?
        if (this.useBuffer || isPoints()) {
            final Dimension size = this.chartPanel.getSize();
            final Insets insets = this.chartPanel.getInsets();

            final Rectangle2D available = new Rectangle2D.Double(insets.left, insets.top,
                    size.getWidth() - insets.left - insets.right,
                    size.getHeight() - insets.top - insets.bottom);

            // do we need to resize the buffer?
            if ((this.chartBuffer == null)
                    || (this.chartBufferWidth != available.getWidth())
                    || (this.chartBufferHeight != available.getHeight())) {

                this.chartBufferWidth = (int) available.getWidth();
                this.chartBufferHeight = (int) available.getHeight();

                final GraphicsConfiguration gc = g2.getDeviceConfiguration();
                if (this.chartBuffer != null) {
                    this.chartBuffer.flush();
                }
                this.chartBuffer = gc.createCompatibleImage(this.chartBufferWidth, this.chartBufferHeight, Transparency.TRANSLUCENT);
                this.refreshBuffer = true;
            }

            if (!this.refreshBuffer) {
                // check if axis ranges changed (zoom):
                this.refreshBuffer = true;

                if (false) {
                    // TODO: use selected area
                    final JFreeChart chart = chartPanel.getChart();
                    final XYPlot plot = chart.getXYPlot();

                    final ValueAxis xAxis = plot.getDomainAxis();
                    final ValueAxis yAxis = plot.getRangeAxis();

                    if ((xAxisRange != null && !xAxisRange.equals(xAxis.getRange())) || (yAxisRange != null && !yAxisRange.equals(yAxis.getRange()))) {
                        logger.warn("Refresh: axis range changed");
                        this.refreshBuffer = true;
                    }
                }
            }

            doPaint = this.refreshBuffer;

            // do we need to redraw the buffer?
            if (this.refreshBuffer) {

                this.refreshBuffer = false; // clear the flag

                final Graphics2D bufferG2 = (Graphics2D) this.chartBuffer.getGraphics();

                // Make all filled pixels transparent
                bufferG2.setBackground(new Color(0, 0, 0, 0));
                bufferG2.clearRect(0, 0, this.chartBufferWidth, this.chartBufferHeight);

                // draw on bufferG2
                draw(bufferG2, chartPanel);
            }

            // zap the buffer onto the panel...
            g2.drawImage(this.chartBuffer, insets.left, insets.top, null);
        } else {
            doPaint = true;
            // draw on bufferG2
            draw(g2, chartPanel);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Paint chart time = {} ms.", 1e-6d * (System.nanoTime() - startTime));
        }
        if (EnhancedChartPanel.DEBUG_PAINT) {
            logger.info("Paint[{}] chart time = {} ms.", doPaint, 1e-6d * (System.nanoTime() - startTime));
        }
    }

    public void draw(final Graphics2D g2, final ChartPanel chartPanel) {

        g2.addRenderingHints(chartPanel.getChart().getRenderingHints());

        final Shape savedClip = g2.getClip();
        final Paint savedPaint = g2.getPaint();
        final Stroke savedStroke = g2.getStroke();

        g2.setPaint((this.mode == ToolMode.ZOOM) ? Color.RED : Color.GRAY);
        g2.draw(this.rectZoom);
        g2.drawImage(imgZoom.getImage(), (int) (this.rectZoom.getX() + 0.5d * margin), (int) (this.rectZoom.getY() + 0.5d * margin), null);

        g2.setPaint((this.mode == ToolMode.SELECT_RECTANGLE) ? Color.RED : Color.GRAY);
        g2.draw(this.rectSelRect);
        g2.drawImage(imgSelRect.getImage(), (int) (this.rectSelRect.getX() + 0.5d * margin), (int) (this.rectSelRect.getY() + 0.5d * margin), null);
        /*
        g2.setPaint((this.mode == ToolMode.SELECT_POLYGON) ? Color.RED : Color.GRAY);
        g2.draw(this.rectSelPolygon);
        g2.drawImage(imgSelPolygon.getImage(), (int) (this.rectSelPolygon.getX() + 0.5d * margin), (int) (this.rectSelPolygon.getY() + 0.5d * margin), null);
         */
        // orange 50% transparent
        g2.setPaint(new Color(255, 200, 0, 128));

        g2.setStroke(SELECT_STROKE);

        Range xRange = null;
        Range yRange = null;

        // paint selection rectangle:
        if (this.selectedArea != null) {
            final XYPlot selectedPlot = this.selectedArea.plot;

            // Get current data area for the plot:
            Rectangle2D dataArea = null;

            final PlotRenderingInfo plotInfo = chartPanel.getChartRenderingInfo().getPlotInfo();
            if (plotInfo.getSubplotCount() > 0) {
                // subplot mode
                final List subPlots = getSubPlots(chartPanel.getChart().getXYPlot());
                if (subPlots != null) {
                    final int subPlotIndex = subPlots.indexOf(selectedPlot);
                    if (subPlotIndex != -1) {
                        final PlotRenderingInfo subPlotInfo = plotInfo.getSubplotInfo(subPlotIndex);
                        dataArea = subPlotInfo.getDataArea();
                    }
                }
            } else {
                dataArea = plotInfo.getDataArea();
            }

            if (dataArea != null) {
                g2.clip(dataArea);

                final ValueAxis xAxis = selectedPlot.getDomainAxis();
                final ValueAxis yAxis = selectedPlot.getRangeAxis();

                final RectangleEdge xAxisEdge = selectedPlot.getDomainAxisEdge();
                final RectangleEdge yAxisEdge = selectedPlot.getRangeAxisEdge();

                final Rectangle2D rectSelArea = this.selectedArea.rectSelArea;

                final double x1 = xAxis.valueToJava2D(rectSelArea.getX(), dataArea, xAxisEdge);
                final double y1 = yAxis.valueToJava2D(rectSelArea.getY(), dataArea, yAxisEdge);

                final double x2 = xAxis.valueToJava2D(rectSelArea.getMaxX(), dataArea, xAxisEdge);
                final double y2 = yAxis.valueToJava2D(rectSelArea.getMaxY(), dataArea, yAxisEdge);

                final Rectangle2D.Double selArea = new Rectangle2D.Double(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x2 - x1), Math.abs(y2 - y1));

                // selected area overlaps the view area:
                if (dataArea.intersects(selArea)) {
                    g2.draw(selArea);

                    // paint selected points:
                    if (isPoints()) {
                        xRange = xAxis.getRange();
                        yRange = yAxis.getRange();

                        final double half = 3d;
                        final double width = half + half;

                        double x, y;
                        for (Point2D point : this.points) {
                            x = xAxis.valueToJava2D(point.getX(), dataArea, xAxisEdge);
                            y = yAxis.valueToJava2D(point.getY(), dataArea, yAxisEdge);

                            g2.draw(new Rectangle2D.Double(x - half, y - half, width, width));
                        }
                    }
                }
            }
        }

        this.xAxisRange = xRange;
        this.yAxisRange = yRange;

        // restore graphics:
        g2.setStroke(savedStroke);
        g2.setPaint(savedPaint);
        g2.setClip(savedClip);
    }

    /**
     * Sends a default {@link ChartChangeEvent} to all registered listeners.
     * <P>
     * This method is for convenience only.
     */
    @Override
    public void fireOverlayChanged() {
        this.refreshBuffer = true;
        super.fireOverlayChanged();
    }


    /* EnhancedChartMouseListener implementation */
    /**
     * Return true if this listener implements / uses this mouse event type
     * @param eventType mouse event type
     * @return true if this listener implements / uses this mouse event type
     */
    @Override
    public boolean support(final int eventType) {
        return (eventType == EnhancedChartMouseListener.EVENT_CLICKED);
    }

    /**
     * Handle click on plot
     * @param chartMouseEvent chart mouse event
     */
    @Override
    public void chartMouseClicked(final ChartMouseEvent chartMouseEvent) {
        final int i = chartMouseEvent.getTrigger().getX();
        final int j = chartMouseEvent.getTrigger().getY();

        logger.debug("chartMouseClicked: mouse coordinates ({}, {})", i, j);

        final ToolMode newMode;
        if (this.rectZoom.contains(i, j)) {
            newMode = ToolMode.ZOOM;
        } else if (this.rectSelRect.contains(i, j)) {
            newMode = ToolMode.SELECT_RECTANGLE;
            /*            
        } else if (this.rectSelPolygon.contains(i, j)) {
            newMode = ToolMode.SELECT_POLYGON;
             */
        } else {
            newMode = null;
        }
        if (newMode != null && this.mode != newMode) {
            logger.info("chartMouseClicked: mode changed: {}", newMode);

            // restore state:
            switch (this.mode) {
                case SELECT_RECTANGLE:
                    this.chartPanel.restoreZoomEvent();
                    break;
                /*                    
                case SELECT_POLYGON:
                    this.chartPanel.restoreZoomEvent();
                    break;
                 */
                default:
            }

            this.mode = newMode;

            // define state:
            switch (this.mode) {
                case SELECT_RECTANGLE:
                    this.chartPanel.redirectZoomEventTo(this);
                    break;
                default:
            }

            // mode changed, force repaint:
            this.fireOverlayChanged();
        }
    }

    /**
     * Not implemented
     * @param chartMouseEvent useless
     */
    @Override
    public void chartMouseMoved(final ChartMouseEvent chartMouseEvent) {
        if (false) {
            chartMouseClicked(chartMouseEvent);
        }
    }

    /**
     * Handle rectangular selection event
     * Not implemented
     *
     * @param nullPlot null given.
     * @param selection the selected region.
     */
    @Override
    public void mouseSelected(final XYPlot nullPlot, final Rectangle2D selection) {
        logger.debug("mouseSelected: rectangle {}", selection);

        final Point2D pointOrigin = this.chartPanel.translateScreenToJava2D(
                new Point((int) selection.getMinX(), (int) selection.getMinY()));

        XYPlot plot = this.chartPanel.getChart().getXYPlot();

        Rectangle2D dataArea = null;

        final PlotRenderingInfo plotInfo = chartPanel.getChartRenderingInfo().getPlotInfo();
        if (plotInfo.getSubplotCount() > 0) {
            // subplot mode
            final List subPlots = getSubPlots(plot);
            if (subPlots != null) {
                // mimics getSubplotIndex(point):
                for (int i = 0, len = subPlots.size(); i < len; i++) {
                    final XYPlot subPlot = (XYPlot) subPlots.get(i);

                    final Rectangle2D plotDataArea = plotInfo.getSubplotInfo(i).getDataArea();

                    if (plotDataArea.contains(pointOrigin)) {
                        dataArea = plotDataArea;
                        plot = subPlot;
                        break;
                    }
                }
            }
        } else {
            dataArea = plotInfo.getDataArea();
        }

        if (dataArea != null) {
            final ValueAxis xAxis = plot.getDomainAxis();
            final ValueAxis yAxis = plot.getRangeAxis();

            final RectangleEdge xAxisEdge = plot.getDomainAxisEdge();
            final RectangleEdge yAxisEdge = plot.getRangeAxisEdge();

            final double xOrigin = xAxis.java2DToValue(pointOrigin.getX(), dataArea, xAxisEdge);
            final double yOrigin = yAxis.java2DToValue(pointOrigin.getY(), dataArea, yAxisEdge);

            final Point2D pointEnd = this.chartPanel.translateScreenToJava2D(
                    new Point((int) (selection.getMaxX() + 0.5), (int) (selection.getMaxY() + 0.5)));

            final double xEnd = xAxis.java2DToValue(pointEnd.getX(), dataArea, xAxisEdge);
            final double yEnd = yAxis.java2DToValue(pointEnd.getY(), dataArea, yAxisEdge);

            // fix orientation:
            final double x1 = Math.min(xOrigin, xEnd);
            final double x2 = Math.max(xOrigin, xEnd);

            final double y1 = Math.min(yOrigin, yEnd);
            final double y2 = Math.max(yOrigin, yEnd);

            final Rectangle2D dataSelection = new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);

            logger.info("Selected data rectangle ({}, {}) to ({}, {})",
                    dataSelection.getX(), dataSelection.getY(), dataSelection.getMaxX(), dataSelection.getMaxY());

            this.selectedArea = new SelectedSubPlotArea(plot, dataSelection);

            this.mouseRectangularSelectionEventListener.mouseSelected(plot, dataSelection);
        }
        // force repaint to hide zoom rectangle:
        this.fireOverlayChanged();
    }

    private static List getSubPlots(final XYPlot plot) {
        final List subPlots;
        if (plot instanceof CombinedDomainXYPlot) {
            subPlots = ((CombinedDomainXYPlot) plot).getSubplots();
        } else if (plot instanceof CombinedRangeXYPlot) {
            subPlots = ((CombinedRangeXYPlot) plot).getSubplots();
        } else {
            System.out.println("Unsupported combined plot: " + plot);
            subPlots = null;
        }
        return subPlots;
    }

    public boolean isPoints() {
        return this.points != null && !this.points.isEmpty();
    }

    public void setPoints(final List<Point2D> points) {
        this.points = points;
    }

    private static final class SelectedSubPlotArea {

        XYPlot plot;
        // rectangle in data-space
        Rectangle2D rectSelArea;

        SelectedSubPlotArea(final XYPlot plot, Rectangle2D rectSelArea) {
            this.plot = plot;
            this.rectSelArea = rectSelArea;
        }
    }
}
