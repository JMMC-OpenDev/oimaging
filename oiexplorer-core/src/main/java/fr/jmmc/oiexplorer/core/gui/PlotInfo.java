/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui;

import fr.jmmc.oitools.model.OIData;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jfree.data.Range;

/**
 * Plot information used during data processing
 */
public final class PlotInfo {

    /** flag indicating that this table has data to plot */
    boolean hasPlotData = false;
    /** flag indicating that the dataset has several wavelengths */
    boolean useWaveLengths = false;
    /** total number of data points */
    int nDataPoints = 0;
    /** total number of rendered points */
    int nDisplayedPoints = 0;
    /** flag indicating that the dataset contains flagged data */
    boolean hasDataFlag = false;
    /** y axis index in plot definition */
    int yAxisIndex = -1;
    /** list of OIData tables used */
    final List<OIData> oidataList;
    /** all distinct station indexes from OIData tables (not filtered) */
    List<String> distinctStaIndexNames;
    /** used distinct station indexes in the plot (filtered) */
    final Set<String> usedStaIndexNames;
    /** all distinct station configuration from OIData tables (not filtered) */
    List<String> distinctStaConfNames;
    /** used distinct station configuration in the plot (filtered) */
    final Set<String> usedStaConfNames;
    /** largest wave length range (not filtered) */
    Range waveLengthRange = null;
    /** x axis information */
    final AxisInfo xAxisInfo;
    /** y axis information */
    final AxisInfo yAxisInfo;

    PlotInfo() {
        oidataList = new ArrayList<OIData>();
        usedStaIndexNames = new LinkedHashSet<String>();
        usedStaConfNames = new LinkedHashSet<String>();
        xAxisInfo = new AxisInfo();
        yAxisInfo = new AxisInfo();
    }

}
