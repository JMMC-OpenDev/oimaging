/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui;

import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.oitools.meta.ColumnMeta;
import org.jfree.data.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bourgesl
 */
public final class AxisInfo {
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(AxisInfo.class.getName());

    /** colum meta data */
    ColumnMeta columnMeta = null;
    /** converter unit */
    String unit = null;
    /** is log axis */
    boolean useLog = false;
    /** data range */
    Range dataRange = null;
    /** data + error range  */
    Range dataErrRange = null;
    /** flag indicating that the dataset has data with error on this axis */
    boolean hasDataError = false;
    /** view bounds (with margin) */
    Range viewBounds = null;
    /** view range */
    Range viewRange = null;

    AxisInfo() {
    }

    AxisInfo(final AxisInfo src) {
        this.columnMeta = src.columnMeta;
        this.unit = src.unit;
        this.useLog = src.useLog;
        this.dataRange = src.dataRange;
        this.dataErrRange = src.dataErrRange;
        this.hasDataError = src.hasDataError;
        this.viewBounds = null;
        this.viewRange = null;
    }

    void combineRanges(final AxisInfo src) {
        this.dataRange = Range.combine(dataRange, src.dataRange);
        this.dataErrRange = Range.combine(dataErrRange, src.dataErrRange);
    }

    public ColumnMeta getColumnMeta() {
        return columnMeta;
    }

    public String getUnit() {
        return unit;
    }

    public boolean isUseLog() {
        return useLog;
    }

    public Range getDataRange() {
        return dataRange;
    }

    public Range getDataErrRange() {
        return dataErrRange;
    }

    public boolean isHasDataError() {
        return hasDataError;
    }

    public Range getViewBounds() {
        return viewBounds;
    }

    public Range getViewRange() {
        return viewRange;
    }

    public boolean isCompatible(final AxisInfo other) {
        return columnMeta.getName().equals(other.columnMeta.getName())
                && useLog == other.useLog
                && ObjectUtils.areEquals(unit, other.unit);
    }

}
