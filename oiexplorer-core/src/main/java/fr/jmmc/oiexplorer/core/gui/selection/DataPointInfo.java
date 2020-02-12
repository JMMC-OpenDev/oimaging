/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.selection;

import fr.jmmc.oiexplorer.core.gui.AxisInfo;

/**
 *
 * @author bourgesl
 */
public final class DataPointInfo extends XYPlotPoint {

    private final DataPointer ptr;

    public DataPointInfo(final AxisInfo xAxisInfo, final AxisInfo yAxisInfo,
                         final double x, final double y,
                         final DataPointer ptr) {
        super(xAxisInfo, yAxisInfo, x, y);
        this.ptr = ptr;
    }

    public DataPointer getDataPointer() {
        return ptr;
    }

    @Override
    public String toString() {
        return super.toString() + "{ptr = " + ptr + '}';
    }
}
