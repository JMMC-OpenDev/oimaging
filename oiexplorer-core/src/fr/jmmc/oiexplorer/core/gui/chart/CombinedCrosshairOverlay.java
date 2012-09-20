/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.chart;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.panel.AbstractOverlay;
import org.jfree.chart.panel.Overlay;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.text.TextUtilities;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;
import org.jfree.util.PublicCloneable;

/**
 * An overlay for a {@link ChartPanel} that draws cross-hairs on a plot.
 *
 * It supports combined subplots also.
 *
 * @since 1.0.13
 */
public class CombinedCrosshairOverlay extends AbstractOverlay implements Overlay,
        PropertyChangeListener, PublicCloneable, Cloneable, Serializable {

    /** main plot index (0) */
    public final static Integer MAIN_PLOT = Integer.valueOf(0);
    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1L;

    /* TODO: use CrosshairPlotState class to store both x and y List<Crosshair> per plot */
    /** Storage for the cross-hairs along the x-axis. */
    private final Map<Integer, List<Crosshair>> xCrosshairs;
    /** Storage for the cross-hairs along the y-axis. */
    private final Map<Integer, List<Crosshair>> yCrosshairs;

    /**
     * Default constructor.
     */
    public CombinedCrosshairOverlay() {
        super();
        this.xCrosshairs = new HashMap<Integer, List<Crosshair>>();
        this.yCrosshairs = new HashMap<Integer, List<Crosshair>>();
    }

    private static void add(final Map<Integer, List<Crosshair>> crosshairMap, final Integer plotIndex, final Crosshair crosshair) {
        List<Crosshair> crosshairs = crosshairMap.get(plotIndex);
        if (crosshairs == null) {
            crosshairs = new ArrayList<Crosshair>();
            crosshairMap.put(plotIndex, crosshairs);
        }
        crosshairs.add(crosshair);
    }

    private static boolean remove(final Map<Integer, List<Crosshair>> crosshairMap, final Integer plotIndex, final Crosshair crosshair) {
        final List<Crosshair> crosshairs = crosshairMap.get(plotIndex);
        if (crosshairs != null) {
            return crosshairs.remove(crosshair);
        }
        return false;
    }

    private void addCrosshair(final Map<Integer, List<Crosshair>> crosshairMap, final Integer plotIndex, final Crosshair crosshair) {
        if (crosshair == null) {
            throw new IllegalArgumentException("Null 'crosshair' argument.");
        }
        add(crosshairMap, plotIndex, crosshair);
        crosshair.addPropertyChangeListener(this);
    }

    private void removeCrosshair(final Map<Integer, List<Crosshair>> crosshairMap, final Integer plotIndex, final Crosshair crosshair) {
        if (crosshair == null) {
            throw new IllegalArgumentException("Null 'crosshair' argument.");
        }
        if (remove(crosshairMap, plotIndex, crosshair)) {
            crosshair.removePropertyChangeListener(this);
            fireOverlayChanged();
        }
    }

    private static List<Crosshair> getCrosshairs(final Map<Integer, List<Crosshair>> crosshairMap, final Integer plotIndex) {
        final List<Crosshair> crosshairs = crosshairMap.get(plotIndex);
        if (crosshairs != null) {
            return Collections.unmodifiableList(crosshairs);
        }
        return Collections.emptyList();
    }

    public void clearCrosshairs(final Map<Integer, List<Crosshair>> crosshairMap, final Integer plotIndex) {
        if (crosshairMap.isEmpty()) {
            return;  // nothing to do
        }
        final List<Crosshair> crosshairs = getCrosshairs(crosshairMap, plotIndex);
        if (crosshairs.isEmpty()) {
            return;  // nothing to do
        }
        for (int i = 0; i < crosshairs.size(); i++) {
            final Crosshair c = crosshairs.get(i);
            remove(crosshairMap, plotIndex, c);
            c.removePropertyChangeListener(this);
        }
        fireOverlayChanged();
    }

    /**
     * Adds a cross-hair against the domain axis.
     *
     * @param crosshair  the cross-hair.
     */
    public void addDomainCrosshair(final Crosshair crosshair) {
        addCrosshair(this.xCrosshairs, MAIN_PLOT, crosshair);
    }

    public void addDomainCrosshair(final Integer plotIndex, final Crosshair crosshair) {
        addCrosshair(this.xCrosshairs, plotIndex, crosshair);
    }

    public void removeDomainCrosshair(final Crosshair crosshair) {
        removeCrosshair(this.xCrosshairs, MAIN_PLOT, crosshair);
    }

    public void removeDomainCrosshair(final Integer plotIndex, final Crosshair crosshair) {
        removeCrosshair(this.xCrosshairs, plotIndex, crosshair);
    }

    public void clearDomainCrosshairs() {
        clearCrosshairs(this.xCrosshairs, MAIN_PLOT);
    }

    public void clearDomainCrosshairs(final Integer plotIndex) {
        clearCrosshairs(this.xCrosshairs, plotIndex);
    }

    public List<Crosshair> getDomainCrosshairs() {
        return getCrosshairs(this.xCrosshairs, MAIN_PLOT);
    }

    public List<Crosshair> getDomainCrosshairs(final Integer plotIndex) {
        return getCrosshairs(this.xCrosshairs, plotIndex);
    }

    /**
     * Adds a cross-hair against the range axis.
     *
     * @param crosshair  the cross-hair.
     */
    public void addRangeCrosshair(final Crosshair crosshair) {
        addCrosshair(this.yCrosshairs, MAIN_PLOT, crosshair);
    }

    public void addRangeCrosshair(final Integer plotIndex, final Crosshair crosshair) {
        addCrosshair(this.yCrosshairs, plotIndex, crosshair);
    }

    public void removeRangeCrosshair(final Crosshair crosshair) {
        removeCrosshair(this.yCrosshairs, MAIN_PLOT, crosshair);
    }

    public void removeRangeCrosshair(final Integer plotIndex, final Crosshair crosshair) {
        removeCrosshair(this.yCrosshairs, plotIndex, crosshair);
    }

    public void clearRangeCrosshairs() {
        clearCrosshairs(this.yCrosshairs, MAIN_PLOT);
    }

    public void clearRangeCrosshairs(final Integer plotIndex) {
        clearCrosshairs(this.yCrosshairs, plotIndex);
    }

    public List<Crosshair> getRangeCrosshairs() {
        return getCrosshairs(this.yCrosshairs, MAIN_PLOT);
    }

    public List<Crosshair> getRangeCrosshairs(final Integer plotIndex) {
        return getCrosshairs(this.yCrosshairs, plotIndex);
    }

    /**
     * Receives a property change event (typically a change in one of the
     * crosshairs).
     *
     * @param e  the event.
     */
    @Override
    public void propertyChange(final PropertyChangeEvent e) {
        fireOverlayChanged();
    }

    /**
     * Paints the cross-hairs in the layer.
     *
     * @param g2  the graphics target.
     * @param chartPanel  the chart panel.
     */
    @Override
    public void paintOverlay(final Graphics2D g2, final ChartPanel chartPanel) {
        final Shape savedClip = g2.getClip();

        final JFreeChart chart = chartPanel.getChart();

        // use chart's antialiasing rendering hints:
        g2.addRenderingHints(chart.getRenderingHints());

        final XYPlot plot = (XYPlot) chart.getPlot();

        final PlotRenderingInfo plotInfo = chartPanel.getChartRenderingInfo().getPlotInfo();
        if (plotInfo.getSubplotCount() > 0) {
            // subplot mode
            final List subPlots;
            if (plot instanceof CombinedDomainXYPlot) {
                subPlots = ((CombinedDomainXYPlot) plot).getSubplots();
            } else if (plot instanceof CombinedRangeXYPlot) {
                subPlots = ((CombinedRangeXYPlot) plot).getSubplots();
            } else {
                System.out.println("Unsupported combined plot: " + plot);
                subPlots = null;
            }

            if (subPlots != null) {
                for (int i = 0, len = subPlots.size(); i < len; i++) {
                    final XYPlot subPlot = (XYPlot) subPlots.get(i);

                    final Rectangle2D dataArea = chartPanel.scale(plotInfo.getSubplotInfo(i).getDataArea());

                    paintPlotCrosshair(g2, dataArea, subPlot, Integer.valueOf(i + 1));

                    // restore clip:
                    g2.setClip(savedClip);
                }
            }

        } else {
            // standard mode
            final Rectangle2D dataArea = chartPanel.getScreenDataArea();
            g2.clip(dataArea);

            paintPlotCrosshair(g2, dataArea, plot, MAIN_PLOT);
        }

        g2.setClip(savedClip);
    }

    public void paintPlotCrosshair(final Graphics2D g2, final Rectangle2D dataArea, final XYPlot plot, final Integer plotIndex) {
        g2.clip(dataArea);

        final ValueAxis xAxis = plot.getDomainAxis();
        final RectangleEdge xAxisEdge = plot.getDomainAxisEdge();

        double x, xx;
        for (Crosshair ch : this.getDomainCrosshairs(plotIndex)) {
            if (ch.isVisible()) {
                x = ch.getValue();
                if (!Double.isNaN(x)) {
                    xx = xAxis.valueToJava2D(x, dataArea, xAxisEdge);
                    if (plot.getOrientation() == PlotOrientation.VERTICAL) {
                        drawVerticalCrosshair(g2, dataArea, xx, ch);
                    } else {
                        drawHorizontalCrosshair(g2, dataArea, xx, ch);
                    }
                }
            }
        }

        final ValueAxis yAxis = plot.getRangeAxis();
        final RectangleEdge yAxisEdge = plot.getRangeAxisEdge();

        double y, yy;
        for (Crosshair ch : this.getRangeCrosshairs(plotIndex)) {
            if (ch.isVisible()) {
                y = ch.getValue();
                if (!Double.isNaN(y)) {
                    yy = yAxis.valueToJava2D(y, dataArea, yAxisEdge);
                    if (plot.getOrientation() == PlotOrientation.VERTICAL) {
                        drawHorizontalCrosshair(g2, dataArea, yy, ch);
                    } else {
                        drawVerticalCrosshair(g2, dataArea, yy, ch);
                    }
                }
            }
        }
    }

    /**
     * Draws a cross-hair horizontally across the plot.
     *
     * @param g2  the graphics target.
     * @param dataArea  the data area.
     * @param y  the y-value in Java2D space.
     * @param crosshair  the cross-hair.
     */
    protected void drawHorizontalCrosshair(final Graphics2D g2, final Rectangle2D dataArea, final double y, final Crosshair crosshair) {

        if (y >= dataArea.getMinY() && y <= dataArea.getMaxY()) {
            final Line2D line = new Line2D.Double(dataArea.getMinX(), y, dataArea.getMaxX(), y);
            final Paint savedPaint = g2.getPaint();
            final Stroke savedStroke = g2.getStroke();
            g2.setPaint(crosshair.getPaint());
            g2.setStroke(crosshair.getStroke());
            g2.draw(line);

            if (crosshair.isLabelVisible()) {
                final String label = crosshair.getLabelGenerator().generateLabel(crosshair);
                RectangleAnchor anchor = crosshair.getLabelAnchor();
                Point2D pt = calculateLabelPoint(line, anchor, 5, 5);
                float xx = (float) pt.getX();
                float yy = (float) pt.getY();
                TextAnchor alignPt = textAlignPtForLabelAnchorH(anchor);
                Shape hotspot = TextUtilities.calculateRotatedStringBounds(label, g2, xx, yy, alignPt, 0.0, TextAnchor.CENTER);

                if (!dataArea.contains(hotspot.getBounds2D())) {
                    anchor = flipAnchorV(anchor);
                    pt = calculateLabelPoint(line, anchor, 5, 5);
                    xx = (float) pt.getX();
                    yy = (float) pt.getY();
                    alignPt = textAlignPtForLabelAnchorH(anchor);
                    hotspot = TextUtilities.calculateRotatedStringBounds(label, g2, xx, yy, alignPt, 0.0, TextAnchor.CENTER);
                }

                g2.setPaint(crosshair.getLabelBackgroundPaint());
                g2.fill(hotspot);
                g2.setPaint(crosshair.getLabelOutlinePaint());
                g2.draw(hotspot);
                TextUtilities.drawAlignedString(label, g2, xx, yy, alignPt);
            }
            g2.setPaint(savedPaint);
            g2.setStroke(savedStroke);
        }
    }

    /**
     * Draws a cross-hair vertically on the plot.
     *
     * @param g2  the graphics target.
     * @param dataArea  the data area.
     * @param x  the x-value in Java2D space.
     * @param crosshair  the cross-hair.
     */
    protected void drawVerticalCrosshair(final Graphics2D g2, final Rectangle2D dataArea, final double x, final Crosshair crosshair) {

        if (x >= dataArea.getMinX() && x <= dataArea.getMaxX()) {
            final Line2D line = new Line2D.Double(x, dataArea.getMinY(), x, dataArea.getMaxY());
            final Paint savedPaint = g2.getPaint();
            final Stroke savedStroke = g2.getStroke();
            g2.setPaint(crosshair.getPaint());
            g2.setStroke(crosshair.getStroke());
            g2.draw(line);

            if (crosshair.isLabelVisible()) {
                final String label = crosshair.getLabelGenerator().generateLabel(crosshair);
                RectangleAnchor anchor = crosshair.getLabelAnchor();
                Point2D pt = calculateLabelPoint(line, anchor, 5, 5);
                float xx = (float) pt.getX();
                float yy = (float) pt.getY();
                TextAnchor alignPt = textAlignPtForLabelAnchorV(anchor);
                Shape hotspot = TextUtilities.calculateRotatedStringBounds(label, g2, xx, yy, alignPt, 0.0, TextAnchor.CENTER);

                if (!dataArea.contains(hotspot.getBounds2D())) {
                    anchor = flipAnchorH(anchor);
                    pt = calculateLabelPoint(line, anchor, 5, 5);
                    xx = (float) pt.getX();
                    yy = (float) pt.getY();
                    alignPt = textAlignPtForLabelAnchorV(anchor);
                    hotspot = TextUtilities.calculateRotatedStringBounds(label, g2, xx, yy, alignPt, 0.0, TextAnchor.CENTER);
                }
                g2.setPaint(crosshair.getLabelBackgroundPaint());
                g2.fill(hotspot);
                g2.setPaint(crosshair.getLabelOutlinePaint());
                g2.draw(hotspot);
                TextUtilities.drawAlignedString(label, g2, xx, yy, alignPt);
            }
            g2.setPaint(savedPaint);
            g2.setStroke(savedStroke);
        }
    }

    /**
     * Calculates the anchor point for a label.
     *
     * @param line  the line for the cross-hair.
     * @param anchor  the anchor point.
     * @param deltaX  the x-offset.
     * @param deltaY  the y-offset.
     *
     * @return The anchor point.
     */
    private static Point2D calculateLabelPoint(final Line2D line, final RectangleAnchor anchor,
            final double deltaX, final double deltaY) {
        double x, y;
        final boolean left = (anchor == RectangleAnchor.BOTTOM_LEFT
                || anchor == RectangleAnchor.LEFT
                || anchor == RectangleAnchor.TOP_LEFT);
        final boolean right = (anchor == RectangleAnchor.BOTTOM_RIGHT
                || anchor == RectangleAnchor.RIGHT
                || anchor == RectangleAnchor.TOP_RIGHT);
        final boolean top = (anchor == RectangleAnchor.TOP_LEFT
                || anchor == RectangleAnchor.TOP
                || anchor == RectangleAnchor.TOP_RIGHT);
        final boolean bottom = (anchor == RectangleAnchor.BOTTOM_LEFT
                || anchor == RectangleAnchor.BOTTOM
                || anchor == RectangleAnchor.BOTTOM_RIGHT);
        // we expect the line to be vertical or horizontal
        if (line.getX1() == line.getX2()) {  // vertical
            x = line.getX1();
            y = (line.getY1() + line.getY2()) / 2.0;
            if (left) {
                x -= deltaX;
            }
            if (right) {
                x += deltaX;
            }
            if (top) {
                y = Math.min(line.getY1(), line.getY2()) + deltaY;
            }
            if (bottom) {
                y = Math.max(line.getY1(), line.getY2()) - deltaY;
            }
        } else {  // horizontal
            x = (line.getX1() + line.getX2()) / 2.0;
            y = line.getY1();
            if (left) {
                x = Math.min(line.getX1(), line.getX2()) + deltaX;
            }
            if (right) {
                x = Math.max(line.getX1(), line.getX2()) - deltaX;
            }
            if (top) {
                y -= deltaY;
            }
            if (bottom) {
                y += deltaY;
            }
        }
        return new Point2D.Double(x, y);
    }

    /**
     * Returns the text anchor that is used to align a label to its anchor
     * point.
     *
     * @param anchor the anchor.
     *
     * @return The text alignment point.
     */
    private static TextAnchor textAlignPtForLabelAnchorV(RectangleAnchor anchor) {
        TextAnchor result = TextAnchor.CENTER;
        if (anchor.equals(RectangleAnchor.TOP_LEFT)) {
            result = TextAnchor.TOP_RIGHT;
        } else if (anchor.equals(RectangleAnchor.TOP)) {
            result = TextAnchor.TOP_CENTER;
        } else if (anchor.equals(RectangleAnchor.TOP_RIGHT)) {
            result = TextAnchor.TOP_LEFT;
        } else if (anchor.equals(RectangleAnchor.LEFT)) {
            result = TextAnchor.HALF_ASCENT_RIGHT;
        } else if (anchor.equals(RectangleAnchor.RIGHT)) {
            result = TextAnchor.HALF_ASCENT_LEFT;
        } else if (anchor.equals(RectangleAnchor.BOTTOM_LEFT)) {
            result = TextAnchor.BOTTOM_RIGHT;
        } else if (anchor.equals(RectangleAnchor.BOTTOM)) {
            result = TextAnchor.BOTTOM_CENTER;
        } else if (anchor.equals(RectangleAnchor.BOTTOM_RIGHT)) {
            result = TextAnchor.BOTTOM_LEFT;
        }
        return result;
    }

    /**
     * Returns the text anchor that is used to align a label to its anchor
     * point.
     *
     * @param anchor the anchor.
     *
     * @return The text alignment point.
     */
    private static TextAnchor textAlignPtForLabelAnchorH(RectangleAnchor anchor) {
        TextAnchor result = TextAnchor.CENTER;
        if (anchor.equals(RectangleAnchor.TOP_LEFT)) {
            result = TextAnchor.BOTTOM_LEFT;
        } else if (anchor.equals(RectangleAnchor.TOP)) {
            result = TextAnchor.BOTTOM_CENTER;
        } else if (anchor.equals(RectangleAnchor.TOP_RIGHT)) {
            result = TextAnchor.BOTTOM_RIGHT;
        } else if (anchor.equals(RectangleAnchor.LEFT)) {
            result = TextAnchor.HALF_ASCENT_LEFT;
        } else if (anchor.equals(RectangleAnchor.RIGHT)) {
            result = TextAnchor.HALF_ASCENT_RIGHT;
        } else if (anchor.equals(RectangleAnchor.BOTTOM_LEFT)) {
            result = TextAnchor.TOP_LEFT;
        } else if (anchor.equals(RectangleAnchor.BOTTOM)) {
            result = TextAnchor.TOP_CENTER;
        } else if (anchor.equals(RectangleAnchor.BOTTOM_RIGHT)) {
            result = TextAnchor.TOP_RIGHT;
        }
        return result;
    }

    private static RectangleAnchor flipAnchorH(final RectangleAnchor anchor) {
        RectangleAnchor result = anchor;
        if (anchor.equals(RectangleAnchor.TOP_LEFT)) {
            result = RectangleAnchor.TOP_RIGHT;
        } else if (anchor.equals(RectangleAnchor.TOP_RIGHT)) {
            result = RectangleAnchor.TOP_LEFT;
        } else if (anchor.equals(RectangleAnchor.LEFT)) {
            result = RectangleAnchor.RIGHT;
        } else if (anchor.equals(RectangleAnchor.RIGHT)) {
            result = RectangleAnchor.LEFT;
        } else if (anchor.equals(RectangleAnchor.BOTTOM_LEFT)) {
            result = RectangleAnchor.BOTTOM_RIGHT;
        } else if (anchor.equals(RectangleAnchor.BOTTOM_RIGHT)) {
            result = RectangleAnchor.BOTTOM_LEFT;
        }
        return result;
    }

    private static RectangleAnchor flipAnchorV(final RectangleAnchor anchor) {
        RectangleAnchor result = anchor;
        if (anchor.equals(RectangleAnchor.TOP_LEFT)) {
            result = RectangleAnchor.BOTTOM_LEFT;
        } else if (anchor.equals(RectangleAnchor.TOP_RIGHT)) {
            result = RectangleAnchor.BOTTOM_RIGHT;
        } else if (anchor.equals(RectangleAnchor.TOP)) {
            result = RectangleAnchor.BOTTOM;
        } else if (anchor.equals(RectangleAnchor.BOTTOM)) {
            result = RectangleAnchor.TOP;
        } else if (anchor.equals(RectangleAnchor.BOTTOM_LEFT)) {
            result = RectangleAnchor.TOP_LEFT;
        } else if (anchor.equals(RectangleAnchor.BOTTOM_RIGHT)) {
            result = RectangleAnchor.TOP_RIGHT;
        }
        return result;
    }

    /**
     * Tests this overlay for equality with an arbitrary object.
     *
     * @param obj the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CombinedCrosshairOverlay)) {
            return false;
        }
        final CombinedCrosshairOverlay that = (CombinedCrosshairOverlay) obj;
        if (!this.xCrosshairs.equals(that.xCrosshairs)) {
            return false;
        }
        if (!this.yCrosshairs.equals(that.yCrosshairs)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a clone of this instance.
     *
     * @return A clone of this instance.
     *
     * @throws java.lang.CloneNotSupportedException if there is some problem
     *     with the cloning.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        final CombinedCrosshairOverlay clone = (CombinedCrosshairOverlay) super.clone();
        // TODO: clone maps
/*
         clone.xCrosshairs = (List) ObjectUtilities.deepClone(this.xCrosshairs);
         clone.yCrosshairs = (List) ObjectUtilities.deepClone(this.yCrosshairs);
         */
        return clone;
    }

    private final static class CrosshairPlotState implements PublicCloneable, Cloneable, Serializable {

        /** default serial UID for Serializable interface */
        private static final long serialVersionUID = 1L;
        /** Storage for the cross-hairs along the x-axis. */
        private final List<Crosshair> xCrosshairs;
        /** Storage for the cross-hairs along the y-axis. */
        private final List<Crosshair> yCrosshairs;

        private CrosshairPlotState() {
            xCrosshairs = new ArrayList<Crosshair>();
            yCrosshairs = new ArrayList<Crosshair>();
        }

        /**
         * Tests this overlay for equality with an arbitrary object.
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
            if (!(obj instanceof CombinedCrosshairOverlay)) {
                return false;
            }
            final CombinedCrosshairOverlay that = (CombinedCrosshairOverlay) obj;
            if (!this.xCrosshairs.equals(that.xCrosshairs)) {
                return false;
            }
            if (!this.yCrosshairs.equals(that.yCrosshairs)) {
                return false;
            }
            return true;
        }

        /**
         * Returns a clone of this instance.
         *
         * @return A clone of this instance.
         *
         * @throws java.lang.CloneNotSupportedException if there is some problem
         *     with the cloning.
         */
        @Override
        public Object clone() throws CloneNotSupportedException {
            final CrosshairPlotState clone = (CrosshairPlotState) super.clone();
            // TODO: clone
/*
             clone.xCrosshairs = (List) ObjectUtilities.deepClone(this.xCrosshairs);
             clone.yCrosshairs = (List) ObjectUtilities.deepClone(this.yCrosshairs);
             */
            return clone;
        }
    }
}
