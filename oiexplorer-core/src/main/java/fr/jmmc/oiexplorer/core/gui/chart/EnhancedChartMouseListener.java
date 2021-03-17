/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.chart;

import org.jfree.chart.ChartMouseListener;

/**
 * This class extends ChartMouseListener to provide new features: mouse events (enabled / disabled)
 * @author bourgesl
 */
public interface EnhancedChartMouseListener extends ChartMouseListener {

    /** mouse clicked event */
    public int EVENT_CLICKED = 1;
    /** mouse moved event */
    public int EVENT_MOVED = 2;

    /**
     * Return true if this listener implements / uses this mouse event type
     * @param eventType mouse event type
     * @return true if this listener implements / uses this mouse event type
     */
    public boolean support(final int eventType);
}
