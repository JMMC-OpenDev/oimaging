/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.chart;

import org.jfree.chart.event.AxisChangeEvent;

/**
 *
 * @author bourgesl
 */
public class AutoBoundedNumberAxis extends BoundedNumberAxis {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;

    /**
     * Constructs a number axis, using default values where necessary.
     *
     * Changes the default tick label insets
     *
     * @param label  the axis label (<code>null</code> permitted).
     */
    public AutoBoundedNumberAxis(final String label) {
        super(label);
    }

    /**
     * Returns a clone of the object.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if some component of the axis does
     *         not support cloning.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return (AutoBoundedNumberAxis) super.clone();
    }

    /**
     * Sets the auto range attribute.  If the <code>notify</code> flag is set,
     * an {@link AxisChangeEvent} is sent to registered listeners.
     *
     * HACK : log a message and setRange(bounds) to reset the zoom
     *
     * @param auto  the flag.
     * @param notify  notify listeners?
     *
     * @see #isAutoRange()
     */
    @Override
    protected void setAutoRange(final boolean auto, final boolean notify) {
        if (auto) {
            // autoRange must stay disabled.

            // This is called by JFreeChart to reset the zoom:
      /*
             at fr.jmmc.aspro.gui.chart.BoundedDateAxis.setAutoRange(BoundedDateAxis.java:93)
             at org.jfree.chart.axis.ValueAxis.setAutoRange(ValueAxis.java:975)
             at org.jfree.chart.axis.ValueAxis.resizeRange(ValueAxis.java:1563)
             at org.jfree.chart.axis.ValueAxis.resizeRange(ValueAxis.java:1539)
             at org.jfree.chart.plot.XYPlot.zoomRangeAxes(XYPlot.java:5158)
             at org.jfree.chart.plot.XYPlot.zoomRangeAxes(XYPlot.java:5123)
             at org.jfree.chart.ChartPanel.restoreAutoRangeBounds(ChartPanel.java:2430)
             at org.jfree.chart.ChartPanel.restoreAutoBounds(ChartPanel.java:2391)
             at org.jfree.chart.ChartPanel.mouseReleased(ChartPanel.java:2044)
             */

            if (this.getBounds() != null) {
                // Use the axis bounds to redefine the ranges (reset zoom)
                setRange(this.getBounds(), false, notify);

                return;
            }
        }

        super.setAutoRange(auto, notify);
    }
}
