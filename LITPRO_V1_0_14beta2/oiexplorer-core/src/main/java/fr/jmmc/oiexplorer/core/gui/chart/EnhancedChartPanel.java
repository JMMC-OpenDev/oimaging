/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.chart;

import fr.jmmc.jmcs.util.IntrospectionUtils;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This enhanced ChartPanel customizes the mouse listener support
 * @author bourgesl
 */
public class EnhancedChartPanel extends ChartPanel {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(EnhancedChartPanel.class.getName());
    /** flag to debug paint operations */
    public static final boolean DEBUG_PAINT = false;
    /** ChartPanel.mouseWheelHandler Field */
    private static final Field mwhField = getMouseWheelHandlerField();

    /* members */
    /** flag to redirect zoom action */
    private boolean redirectZoomEvent = false;
    /** ChartMouseSelectionListener which handles rectangular mouse selection event */
    private ChartMouseSelectionListener mouseRectangularSelectionEventListener = null;
    /** flag to redirect mouse events */
    private boolean redirectMouseEvents = false;
    /** special EnhancedChartMouseListener which handles redirected mouse events */
    private EnhancedChartMouseListener redirectedMouseEventListener = null;

    /**
     * Constructs an enhanced JFreeChart panel.
     *
     * @param chart  the chart.
     * @param width  the preferred width of the panel.
     * @param height  the preferred height of the panel.
     * @param minimumDrawWidth  the minimum drawing width.
     * @param minimumDrawHeight  the minimum drawing height.
     * @param maximumDrawWidth  the maximum drawing width.
     * @param maximumDrawHeight  the maximum drawing height.
     * @param useBuffer  a flag that indicates whether to use the off-screen
     *                   buffer to improve performance (at the expense of
     *                   memory).
     * @param properties  a flag indicating whether or not the chart property
     *                    editor should be available via the popup menu.
     * @param copy  a flag indicating whether or not a copy option should be
     *              available via the popup menu.
     * @param save  a flag indicating whether or not save options should be
     *              available via the popup menu.
     * @param print  a flag indicating whether or not the print option
     *               should be available via the popup menu.
     * @param zoom  a flag indicating whether or not zoom options should be
     *              added to the popup menu.
     * @param tooltips  a flag indicating whether or not tooltips should be
     *                  enabled for the chart.
     *
     * @since 1.0.13
     */
    public EnhancedChartPanel(final JFreeChart chart, final int width, final int height,
                              final int minimumDrawWidth, final int minimumDrawHeight, final int maximumDrawWidth,
                              final int maximumDrawHeight, final boolean useBuffer, final boolean properties,
                              final boolean copy, boolean save, final boolean print, final boolean zoom,
                              final boolean tooltips) {
        super(chart, width, height, minimumDrawWidth, minimumDrawHeight, maximumDrawWidth, maximumDrawHeight,
                useBuffer, properties, copy, save, print, zoom, tooltips);
    }

    /**
     * Paints the component by drawing the chart to fill the entire component,
     * but allowing for the insets (which will be non-zero if a border has been
     * set for this component).  To increase performance (at the expense of
     * memory), an off-screen buffer image can be used.
     *
     * @param g  the graphics device for drawing on.
     */
    @Override
    public final void paintComponent(final Graphics g) {
        final long startTime = System.nanoTime();

        super.paintComponent(g);

        if (logger.isDebugEnabled()) {
            logger.debug("Paint chart time = {} ms.", 1e-6d * (System.nanoTime() - startTime));
        }
        if (DEBUG_PAINT) {
            logger.warn("Paint[{}] chart time = {} ms.", getRefreshBuffer(), 1e-6d * (System.nanoTime() - startTime));
        }
    }

    /**
     * Enables or disables mouse wheel support for the panel.
     * Note that this method does nothing when running JFreeChart on JRE 1.3.1,
     * because that older version of the Java runtime does not support
     * mouse wheel events.
     * 
     * LBO: FIX leaking ChartPanel.mouseWheelHandler value
     *
     * @param flag  a boolean.
     *
     * @since 1.0.13
     */
    @Override
    public void setMouseWheelEnabled(boolean flag) {
        super.setMouseWheelEnabled(flag);

        if (!flag) {
            /* FIX leaking ChartPanel.mouseWheelHandler value */
            if (mwhField != null) {
                try {
                    mwhField.set(this, null);
                } catch (IllegalAccessException iae) {
                    logger.error("unable to set ChartPanel.mouseWheelHandler to null", iae);
                } catch (IllegalArgumentException iae) {
                    logger.error("unable to set ChartPanel.mouseWheelHandler to null", iae);
                }
            }
        }
    }

    private static Field getMouseWheelHandlerField() {
        final Field field = IntrospectionUtils.getField(ChartPanel.class, "mouseWheelHandler");
        if (field != null) {
            try {
                field.setAccessible(true);
            } catch (SecurityException se) {
                logger.error("unable to call Field.setAccessible() for ChartPanel.mouseWheelHandler", se);
            }
        }
        return field;
    }


    /* MouseListener implementation */
    /**
     * Receives notification of mouse clicks on the panel (pressed and released).
     * These are translated and passed on to any registered {@link ChartMouseListener}s.
     *
     * This method overrides default ChartPanel implementation to avoid useless redraws (crosshair related)
     * and return fast if no registered ChartMouseListener !
     *
     * @param event  Information about the mouse event.
     */
    @Override
    public final void mouseClicked(final MouseEvent event) {
        final JFreeChart chart = this.getChart();

        if (chart != null) {
            final Object[] listeners = this.getListeners(ChartMouseListener.class);

            // check if the chart is using crosshairs:
            final Plot plot = chart.getPlot();

            final boolean useAnyCrossHair = (plot instanceof XYPlot) ? ((XYPlot) plot).isDomainCrosshairVisible() || ((XYPlot) plot).isRangeCrosshairVisible() : false;

            if (useAnyCrossHair || listeners.length != 0) {
                final Insets insets = getInsets();
                final int x = (int) ((event.getX() - insets.left) / this.getScaleX());
                final int y = (int) ((event.getY() - insets.top) / this.getScaleY());

                if (useAnyCrossHair) {
                    // anchor is used by crosshair in draw() method to match closest data:
                    this.setAnchor(new Point2D.Double(x, y));
                    chart.setNotify(true);  // force a redraw
                }

                // discard useless events:
                if (!shouldFireEvent(listeners, EnhancedChartMouseListener.EVENT_CLICKED)) {
                    return;
                }

                ChartEntity entity = null;
                if (this.getChartRenderingInfo() != null) {
                    EntityCollection entities = this.getChartRenderingInfo().getEntityCollection();
                    if (entities != null) {
                        entity = entities.getEntity(x, y);
                    }
                }

                final ChartMouseEvent chartEvent = new ChartMouseEvent(chart, event, entity);
                for (int i = listeners.length - 1; i >= 0; i -= 1) {
                    ((ChartMouseListener) listeners[i]).chartMouseClicked(chartEvent);
                }
            }
        }
    }

    /**
     * Return true if there is at least one ChartMouseListener supporting the given event type
     * @param listeners ChartMouseListener array
     * @param eventType mouse event type
     * @return true if there is at least one ChartMouseListener supporting the given event type
     */
    private boolean shouldFireEvent(final Object[] listeners, final int eventType) {
        if (listeners.length == 0) {
            return false;
        }
        for (int i = listeners.length - 1; i >= 0; i -= 1) {
            if (listeners[i] instanceof EnhancedChartMouseListener) {
                if (((EnhancedChartMouseListener) listeners[i]).support(eventType)) {
                    return true;
                }
            } else if (listeners[i] instanceof ChartMouseListener) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles a 'mouse pressed' event:
     * This event handles Pan/Zoom features and also is the pop-up trigger on Unix/Linux.
     * For Windows, the pop-up trigger is the 'mouse released' event.
     *
     * @param event  The mouse event.
     */
    @Override
    public final void mousePressed(final MouseEvent event) {
        super.mousePressed(event);
    }

    /**
     * Handles a 'mouse released' event:
     * This event handles Pan/Zoom features.
     * On Windows, we need to check if this is a pop-up trigger,
     * but only if we haven't already been tracking a zoom rectangle.
     *
     * @param event  information about the event.
     */
    @Override
    public final void mouseReleased(final MouseEvent event) {
        super.mouseReleased(event);
    }

    /**
     * Handles a 'mouse entered' event.
     * This method disables default ChartPanel implementation:
     * @see ChartPanel#mouseEntered(java.awt.event.MouseEvent)
     *
     * @param event  the mouse event.
     */
    @Override
    public final void mouseEntered(final MouseEvent event) {
    }

    /**
     * Handles a 'mouse exited' event.
     * This method disables default ChartPanel implementation:
     * @see ChartPanel#mouseExited(java.awt.event.MouseEvent)
     *
     * @param event  the mouse event.
     */
    @Override
    public final void mouseExited(final MouseEvent event) {
    }

    /* MouseMotionListener implementation */
    /**
     * Handles a 'mouse dragged' event:
     * This event handles Pan/Zoom features.
     *
     * @param event  the mouse event.
     */
    @Override
    public final void mouseDragged(final MouseEvent event) {
        super.mouseDragged(event);
    }

    /**
     * Handles a 'mouse moved' event:
     * These are translated and passed on to any registered {@link ChartMouseListener}s.
     *
     * This method overrides default ChartPanel implementation to disable horizontal and vertical traces
     * and return fast if no registered ChartMouseListener !
     *
     *
     * @param event  the mouse event.
     */
    @Override
    public final void mouseMoved(final MouseEvent event) {
        // check first if there is at least one ChartMouseListener:
        final Object[] listeners = this.getListeners(ChartMouseListener.class);

        // discard useless events:
        if (!shouldFireEvent(listeners, EnhancedChartMouseListener.EVENT_MOVED)) {
            return;
        }

        final JFreeChart chart = this.getChart();
        if (chart != null) {
            ChartEntity entity = null;
            if (this.getChartRenderingInfo() != null) {
                EntityCollection entities = this.getChartRenderingInfo().getEntityCollection();
                if (entities != null) {
                    // lazy:
                    final Insets insets = getInsets();
                    final int x = (int) ((event.getX() - insets.left) / this.getScaleX());
                    final int y = (int) ((event.getY() - insets.top) / this.getScaleY());

                    entity = entities.getEntity(x, y);
                }
            }

            final ChartMouseEvent chartEvent = new ChartMouseEvent(chart, event, entity);
            for (int i = listeners.length - 1; i >= 0; i -= 1) {
                ((ChartMouseListener) listeners[i]).chartMouseMoved(chartEvent);
            }
        }
    }

    /* zoom event handling */
    /**
     * Zooms in on a selected region.
     *
     * @param selection  the selected region.
     */
    @Override
    public void zoom(final Rectangle2D selection) {
        if (this.redirectZoomEvent) {
            logger.debug("zoom: redirect zoom event: {}", selection);
            this.mouseRectangularSelectionEventListener.mouseSelected(selection);
        } else {
            super.zoom(selection);
        }
    }

    /**
     * Restores the auto-range calculation on both axes.
     * ie reset zoom by mouse
     */
    @Override
    public void restoreAutoBounds() {
        if (this.redirectZoomEvent) {
            logger.debug("restoreAutoBounds: discarded zoom event");
        } else {
            super.restoreAutoBounds();
        }
    }

    public final void redirectZoomEventTo(final ChartMouseSelectionListener mouseRectangularSelectionEventListener) {
        if (!this.redirectZoomEvent && mouseRectangularSelectionEventListener != null) {
            this.redirectZoomEvent = true;
            this.mouseRectangularSelectionEventListener = mouseRectangularSelectionEventListener;
        }
    }

    public final void restoreZoomEvent() {
        if (this.redirectZoomEvent) {
            this.redirectZoomEvent = false;
            this.mouseRectangularSelectionEventListener = null;
        }
    }

    public final void redirectMouseEventsTo(final EnhancedChartMouseListener redirectedMouseEventListener) {
        if (!this.redirectMouseEvents && redirectedMouseEventListener != null) {
            this.redirectMouseEvents = true;
            this.redirectedMouseEventListener = redirectedMouseEventListener;
        }
    }

    public final void restoreMouseEvents() {
        if (this.redirectMouseEvents) {
            this.redirectMouseEvents = false;
            this.redirectedMouseEventListener = null;
        }
    }
}
