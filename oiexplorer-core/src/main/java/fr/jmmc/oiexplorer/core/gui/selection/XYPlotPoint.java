/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.selection;

import fr.jmmc.oiexplorer.core.gui.AxisInfo;

/**
 *
 * @author bourgesl
 */
public class XYPlotPoint {

    public static final XYPlotPoint UNDEFINED = new XYPlotPoint(null, null, Double.NaN, Double.NaN);

    /* members */
    /** x axis information */
    private final AxisInfo xAxisInfo;
    /** y axis information */
    private final AxisInfo yAxisInfo;
    /** x coordinate */
    private final double x;
    /** y coordinate */
    private final double y;

    public XYPlotPoint(final AxisInfo xAxisInfo, final AxisInfo yAxisInfo,
                     final double x, final double y) {
        this.xAxisInfo = xAxisInfo;
        this.yAxisInfo = yAxisInfo;
        this.x = x;
        this.y = y;
    }

    public final double getX() {
        return x;
    }

    public final double getY() {
        return y;
    }

    public final AxisInfo getxAxisInfo() {
        return xAxisInfo;
    }

    public final AxisInfo getyAxisInfo() {
        return yAxisInfo;
    }

    @Override
    public String toString() {
        return "XYPlotPoint{" + "x=" + x + ", y=" + y + '}';
    }

}
