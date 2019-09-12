/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.chart;

import java.awt.geom.Rectangle2D;
import org.jfree.chart.plot.XYPlot;

/**
 * This class defines an mouse selection event listener
 * @author bourgesl
 */
public interface ChartMouseSelectionListener {

    /**
     * Handle rectangular selection event
     *
     * @param plot the plot or subplot where the selection happened or null if invalid.
     * @param selection the selected region.
     */
    public void mouseSelected(final XYPlot plot, final Rectangle2D selection);
}
