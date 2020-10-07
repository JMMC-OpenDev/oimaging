/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui;

/**
 *
 * @author bourgesl
 */
public final class PlotInfosData {

    private final String plotId;
    private final PlotInfo[] plotInfos;

    public PlotInfosData(final String plotId, final PlotInfo[] plotInfos) {
        this.plotId = plotId;
        this.plotInfos = plotInfos;
    }

    public String getPlotId() {
        return plotId;
    }

    public PlotInfo[] getPlotInfos() {
        return plotInfos;
    }

    @Override
    public String toString() {
        return "PlotInfosData{plotId: " + plotId + '}';
    }
}
