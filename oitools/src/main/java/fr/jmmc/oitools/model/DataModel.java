/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.meta.CellMeta;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.DataRange;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.OIFitsStandard;
import fr.jmmc.oitools.meta.Units;
import java.util.List;

/**
 *
 * @author bourgesl, mellag
 */
public class DataModel {

    /** 
     * flag to globally support or not additional columns for Image Reconstruction Imaging (disabled by default)
     * @see https://github.com/emmt/OI-Imaging-JRA
     */
    private static boolean oiModelColumnsSupport = false;
    /** flag to globally support OI_VIS Complex visibility columns (disabled by default) */
    private static boolean oiVisComplexSupport = false;

    /**
     *   Get support of additional columns for Image Reconstruction Imaging.
     *  @returns true if columns are present in the datamodel else false.
     */
    public static boolean hasOiModelColumnsSupport() {
        return oiModelColumnsSupport;
    }

    /**
     * Set support of additional columns for Image Reconstruction Imaging.
     *
     * @param oiModelColumnsSupport true will include optional columns in the datamodel, else will ignore them.
     */
    public static void setOiModelColumnsSupport(boolean oiModelColumnsSupport) {
        DataModel.oiModelColumnsSupport = oiModelColumnsSupport;
    }

    public static boolean hasOiVisComplexSupport() {
        return oiVisComplexSupport;
    }

    public static void setOiVisComplexSupport(boolean oiVisComplexSupport) {
        DataModel.oiVisComplexSupport = oiVisComplexSupport;
    }

    public static void dumpDataModel() {
        final int nRows = 1;
        final int nWLen = 13;
        System.out.println("WaveLength dimension: " + nWLen);

        // fake data model:
        final OIFitsFile oiFitsFile = new OIFitsFile(OIFitsStandard.VERSION_1);

        // OITarget:
        final OITarget oiTarget = new OITarget(oiFitsFile, nRows);
        oiFitsFile.addOiTable(oiTarget);

        // OIArray:
        final String arrName = "ARRNAME_VALUE";
        final OIArray oiArray = new OIArray(oiFitsFile, 1);
        oiArray.setArrName(arrName);
        oiFitsFile.addOiTable(oiArray);

        // OIWavelength: 13 rows
        final String insName = "INSNAME_VALUE";
        final OIWavelength oiWaveLength = new OIWavelength(oiFitsFile, nWLen);
        oiWaveLength.setInsName(insName);
        oiFitsFile.addOiTable(oiWaveLength);

        // Data:
        // OIVis:
        final OIVis oiVis = new OIVis(oiFitsFile, insName, nRows);
        oiVis.setArrName(arrName);
        oiFitsFile.addOiTable(oiVis);

        final OIVis2 oiVis2 = new OIVis2(oiFitsFile, insName, nRows);
        oiVis.setArrName(arrName);
        oiFitsFile.addOiTable(oiVis2);

        final OIT3 oit3 = new OIT3(oiFitsFile, insName, nRows);
        oiVis.setArrName(arrName);
        oiFitsFile.addOiTable(oit3);

        final StringBuilder sb = new StringBuilder(32 * 1024);
        sb.append("<datamodel>\n");
        dumpTables(oiFitsFile.getOITableList(), sb);
        sb.append("</datamodel>\n");

        System.out.println("DataModel: \n" + sb);
    }

    public static void dumpTables(final List<OITable> tables, final StringBuilder sb) {

        for (OITable table : tables) {
            sb.append("<table name=\"").append(table.getExtName()).append("\">\n");

            for (KeywordMeta keyword : table.getKeywordDescCollection()) {
                dumpKeyword(keyword, sb);
            }

            for (ColumnMeta column : table.getColumnDescCollection()) {
                dumpColumn(column, sb);
            }

            sb.append("</table>\n");
        }
    }

    public static void dumpMeta(final CellMeta meta, final StringBuilder sb) {
        sb.append("<name>").append(meta.getName()).append("</name>\n");
        sb.append("<datatype>").append(meta.getDataType()).append("</datatype>\n");
        sb.append("<description>").append(meta.getDescription()).append("</description>\n");
        sb.append("<repeat>").append(meta.getRepeat()).append("</repeat>\n");
        if (meta.getUnits() != Units.NO_UNIT) {
            sb.append("<unit>").append(meta.getUnits()).append("</unit>\n");
        }

        final short[] intAcceptedValues = meta.getIntAcceptedValues();

        if (intAcceptedValues.length != 0) {
            sb.append("<values>\n");

            for (int i = 0, len = intAcceptedValues.length; i < len; i++) {
                sb.append("<value>").append(intAcceptedValues[i]).append("</value>\n");
            }

            sb.append("</values>\n");
        }

        final String[] stringAcceptedValues = meta.getStringAcceptedValues();

        if (stringAcceptedValues.length != 0) {

            for (int i = 0; i < stringAcceptedValues.length; i++) {
                sb.append("<value>").append(stringAcceptedValues[i]).append("</value>\n");
            }
        }
    }

    public static void dumpKeyword(final KeywordMeta keyword, final StringBuilder sb) {
        sb.append("<keyword>\n");
        dumpMeta(keyword, sb);
        // specific members :
        sb.append("<mandatory>").append(!keyword.isOptional()).append("</mandatory>\n");
        sb.append("</keyword>\n");
    }

    public static void dumpColumn(final ColumnMeta column, final StringBuilder sb) {
        sb.append("<column>\n");
        dumpMeta(column, sb);
        // specific members :
        // note: optional is overriden by WaveColumnMeta:
        sb.append("<mandatory>").append(!column.isOptional()).append("</mandatory>\n");
        sb.append("<array>").append(column.isArray()).append("</array>\n");
        if (column.getErrorColumnName() != null) {
            sb.append("<errorColumn>").append(column.getErrorColumnName()).append("</errorColumn>\n");
        }
        if (column.getDataRange() != null) {
            sb.append("<dataRange>\n");
            final DataRange range = column.getDataRange();
            final double min = range.getMin();
            if (!Double.isNaN(min)) {
                sb.append("<min>").append(min).append("</min>\n");
            }
            final double max = range.getMax();
            if (!Double.isNaN(max)) {
                sb.append("<max>").append(max).append("</max>\n");
            }
            sb.append("</dataRange>\n");
        }
        sb.append("</column>\n");
    }

    public static void main(String[] args) {
        dumpDataModel();
    }
}
