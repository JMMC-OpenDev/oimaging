/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.model;

import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.Types;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.table.AbstractTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bourgesl
 */
public final class ColumnsTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(ColumnsTableModel.class.getName());

    private static final boolean USE_STRING_FOR_BOOLEAN = true;

    /** ROW_INDEX derived column as int[] */
    private final static String COLUMN_ROW_INDEX = "ROW_INDEX";

    /** Derived ROW_INDEX column definition */
    private final static ColumnMeta COLUMN_META_ROW_INDEX = new ColumnMeta(COLUMN_ROW_INDEX, "row index", Types.TYPE_INT, 1);

    /* members */
    /** FITS table reference */
    private FitsTable table = null;
    /** flag to include derived columns */
    private boolean includeDerivedColumns = true;
    /** flag to expand or not arrays (2D only) as columns */
    private boolean expandArrays = false;
    /** flag to expand or not arrays (2D only) as columns */
    private boolean expandRows = true;
    /** column mapping */
    private final ArrayList<ColumnMapping> columnMap = new ArrayList<ColumnMapping>();
    /** column dimensions */
    private final int[] columnDims = new int[2]; // 2d max
    /** number of (virtual) rows */
    private int nbRows = 0;
    /* temporary buffer */
    private final StringBuilder sb = new StringBuilder();

    public ColumnsTableModel() {
        super();
    }

    public void setFitsHdu(final FitsTable table) {
        this.table = table;
        init();
        fireTableStructureChanged();
    }

    public boolean isIncludeDerivedColumns() {
        return includeDerivedColumns;
    }

    public void setIncludeDerivedColumns(boolean includeDerivedColumns) {
        this.includeDerivedColumns = includeDerivedColumns;
    }

    public boolean isExpandArrays() {
        return expandArrays;
    }

    public void setExpandArrays(boolean expandArrays) {
        this.expandArrays = expandArrays;
    }

    public boolean isExpandRows() {
        return expandRows;
    }

    public void setExpandRows(boolean expandRows) {
        this.expandRows = expandRows;
    }

    private void init() {
        columnMap.clear();
        nbRows = 0;
        columnDims[0] = 0;
        columnDims[1] = 0;
        if (table == null) {
            return;
        }

        // columnDims[0] = nbRows
        nbRows = table.getNbRows();
        columnDims[0] = nbRows;

        // Prepare row index column:
        table.addDerivedColumnMeta(COLUMN_META_ROW_INDEX);
        getRowIndex(table); // compute row index
        addColumns(COLUMN_META_ROW_INDEX, true); // first

        for (ColumnMeta meta : table.getColumnDescCollection()) {
            addColumns(meta, false);
        }
        if (includeDerivedColumns) {
            for (ColumnMeta meta : table.getColumnDerivedDescCollection()) {
                if (meta != COLUMN_META_ROW_INDEX) {
                    addColumns(meta, true);
                }
            }
        }
        if (expandRows) {
            // Fix nbRows:
            if (columnDims[1] != 0) {
                nbRows *= columnDims[1];
            }
        }
        logger.debug("column mapping: {}", columnMap);
    }

    private void addColumns(final ColumnMeta meta, final boolean isDerived) {
        ColumnMapping col = null;

        final String name = meta.getName();
        if (meta.is3D()) {
            // TODO: fix with expandRows
            // use String representation:
            col = new ColumnMapping(meta, isDerived, name, String.class);
        } else {
            final Class<?> type = getType(meta.getDataType());
            if (meta.isArray()) {
                // TODO: handle complex properly !
                if (meta.getDataType() != Types.TYPE_COMPLEX
                        && meta.getDataType() != Types.TYPE_SHORT) {

                    final int repeat = meta.getRepeat();

                    if (expandArrays) {
                        for (int i = 0; i < repeat; i++) {
                            col = new ColumnMapping(meta, isDerived, name + "_" + i, type, i);
                        }
                    } else if (expandRows) {
                        // 2d
                        if (columnDims[1] == 0) {
                            columnDims[1] = repeat;
                        } else {
                            // check consistency ?
                            if (columnDims[1] != repeat) {
                                logger.warn("Bad dimensions for 2D column: {} expected: {}", repeat, columnDims[1]);
                                columnDims[1] = Math.min(columnDims[1], repeat);
                            }
                        }
                        col = new ColumnMapping(meta, isDerived, name, type, 1); // index == 1 means 2D
                    }
                }
            } else {
                col = new ColumnMapping(meta, isDerived, name, type);
            }
        }
        if (col == null) {
            col = new ColumnMapping(meta, isDerived, name, String.class);
        }
        columnMap.add(col);
    }

    private Class<?> getType(final Types type) {
        switch (type) {
            case TYPE_CHAR:
                return String.class;
            case TYPE_SHORT:
                return Short.class;
            case TYPE_INT:
                return Integer.class;
            case TYPE_DBL:
                return Double.class;
            case TYPE_COMPLEX:
            case TYPE_REAL:
                return Float.class;
            case TYPE_LOGICAL:
                if (USE_STRING_FOR_BOOLEAN) {
                    return String.class;
                }
                return Boolean.class;
        }
        return null;
    }

    @Override
    public int getColumnCount() {
        return columnMap.size();
    }

    @Override
    public String getColumnName(final int columnIndex) {
        return columnMap.get(columnIndex).name;
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex) {
        return columnMap.get(columnIndex).type;
    }

    @Override
    public int getRowCount() {
        return nbRows;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        if (table == null) {
            return null;
        }
        final ColumnMapping mapping = columnMap.get(columnIndex);
        if (mapping != null) {
            try {
                return getColumnValue(mapping, rowIndex);
            } catch (RuntimeException re) {
                logger.info("Exception on table[{}] for mapping[{}]", table, mapping);
                logger.info("At ({}, {})", rowIndex, columnIndex);
                logger.error("Exception: ", re);
            }
        }
        return null;
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex) {
        return false;
    }

    private Object getColumnValue(final ColumnMapping mapping, final int row) {
        final ColumnMeta column = mapping.meta;
        final boolean isDerived = mapping.isDerived;

        // compute real row:
        int rowIndex = row;
        int columnIndex = -1;

        // compute real column:
        if (expandRows) {
            if (columnDims[1] != 0) {
                rowIndex = row / columnDims[1];
                if (mapping.hasIndex()) {
                    columnIndex = row % columnDims[1];
                }
            }
        } else if (mapping.hasIndex()) {
            columnIndex = mapping.index;
        }

        switch (column.getDataType()) {
            case TYPE_CHAR:
                final String[] chValues = (isDerived) ? table.getColumnDerivedString(column.getName()) : table.getColumnString(column.getName());
                if (chValues != null) {
                    return chValues[rowIndex];
                }
                break;

            case TYPE_SHORT:
                if (column.isArray()) {
                    final short[][] sValues = (isDerived) ? table.getColumnDerivedShorts(column.getName()) : table.getColumnShorts(column.getName());
                    if (sValues != null) {
                        final short[] rowValues = sValues[rowIndex];
                        if (columnIndex >= 0) {
                            return Short.valueOf(rowValues[columnIndex]);
                        } else {
                            // append values :
                            sb.setLength(0);
                            for (int i = 0, len = rowValues.length; i < len; i++) {
                                if (i > 0) {
                                    sb.append(' ');
                                }
                                sb.append(rowValues[i]);
                            }
                            return sb.toString();
                        }
                    }
                    break;
                }
                final short[] sValues = (isDerived) ? table.getColumnDerivedShort(column.getName()) : table.getColumnShort(column.getName());
                if (sValues != null) {
                    return Short.valueOf(sValues[rowIndex]);
                }
                break;

            case TYPE_INT:
                if (column.isArray()) {
                    final int[][] iValues = (isDerived) ? table.getColumnDerivedInts(column.getName()) : table.getColumnInts(column.getName());
                    if (iValues != null) {
                        final int[] rowValues = iValues[rowIndex];
                        if (columnIndex >= 0) {
                            return NumberUtils.valueOf(rowValues[columnIndex]);
                        } else {
                            // append values :
                            sb.setLength(0);
                            for (int i = 0, len = rowValues.length; i < len; i++) {
                                if (i > 0) {
                                    sb.append(' ');
                                }
                                sb.append(rowValues[i]);
                            }
                            return sb.toString();
                        }
                    }
                    break;
                }
                final int[] iValues = (isDerived) ? table.getColumnDerivedInt(column.getName()) : table.getColumnInt(column.getName());
                if (iValues != null) {
                    return NumberUtils.valueOf(iValues[rowIndex]);
                }
                break;

            case TYPE_DBL:
                if (column.isArray()) {
                    final double[][] dValues = table.getColumnAsDoubles(column.getName());
                    if (dValues != null) {
                        final double[] rowValues = dValues[rowIndex];
                        if (columnIndex >= 0) {
                            return Double.valueOf(rowValues[columnIndex]);
                        } else {
                            // append values :
                            sb.setLength(0);
                            for (int i = 0, len = rowValues.length; i < len; i++) {
                                if (i > 0) {
                                    sb.append(' ');
                                }
                                sb.append(NumberUtils.format(rowValues[i]));
                            }
                            return sb.toString();
                        }
                    }
                    break;
                }
                final double[] dValues = table.getColumnAsDouble(column.getName());
                if (dValues != null) {
                    return Double.valueOf(dValues[rowIndex]);
                }
                break;

            case TYPE_REAL:
                if (column.isArray()) {
                    // Impossible case in OIFits
                    break;
                }
                final float[] fValues = table.getColumnFloat(column.getName());
                if (fValues != null) {
                    return Float.valueOf(fValues[rowIndex]);
                }
                break;

            case TYPE_COMPLEX:
                // Special case for complex visibilities :
                if (column.isArray()) {
                    final float[][][] cValues = table.getColumnComplexes(column.getName());
                    if (cValues != null) {
                        final float[][] rowValues = cValues[rowIndex];
                        // append values :
                        sb.setLength(0);
                        for (int i = 0, len = rowValues.length; i < len; i++) {
                            if (i > 0) {
                                sb.append(' ');
                            }
                            // real,img pattern for complex values :
                            sb.append(NumberUtils.format(rowValues[i][0])).append(',').append(NumberUtils.format(rowValues[i][1]));
                        }
                        return sb.toString();
                    }
                    break;
                }
                // Impossible case in OIFits
                break;

            case TYPE_LOGICAL:
                if (column.is3D()) {
                    final boolean[][][] bValues = table.getColumnBoolean3D(column.getName());
                    if (bValues != null) {
                        final boolean[][] rowValues = bValues[rowIndex];
                        // append values :
                        sb.setLength(0);
                        for (int i = 0, lenI = rowValues.length; i < lenI; i++) {
                            if (i > 0) {
                                sb.append(' ');
                            }
                            final boolean[] cellValues = rowValues[i];
                            for (int j = 0, lenJ = cellValues.length; j < lenJ; j++) {
                                if (j > 0) {
                                    sb.append(',');
                                }
                                sb.append(cellValues[j] ? 'T' : 'F');
                            }
                        }
                        return sb.toString();
                    }
                    break;
                }
                if (column.isArray()) {
                    final boolean[][] bValues = table.getColumnBooleans(column.getName());
                    if (bValues != null) {
                        final boolean[] rowValues = bValues[rowIndex];
                        if (columnIndex >= 0) {
                            if (USE_STRING_FOR_BOOLEAN) {
                                return (rowValues[columnIndex] ? 'T' : 'F');
                            }
                            return Boolean.valueOf(rowValues[columnIndex]);
                        } else {
                            // append values :
                            sb.setLength(0);
                            for (int i = 0, len = rowValues.length; i < len; i++) {
                                if (i > 0) {
                                    sb.append(' ');
                                }
                                sb.append(rowValues[i] ? 'T' : 'F');
                            }
                            return sb.toString();
                        }
                    }
                    break;
                }
                // Impossible case in OIFits
                break;

            default:
            // Bad type
        }
        return null;
    }

    final static class ColumnMapping {

        /** column meta */
        final ColumnMeta meta;
        /** is derived column ? */
        final boolean isDerived;
        /** column name */
        final String name;
        /** column types */
        final Class<?> type;
        /** index in 2D column */
        final int index;

        ColumnMapping(final ColumnMeta meta, final boolean isDerived, final String name, final Class<?> type) {
            this(meta, isDerived, name, type, -1);
        }

        ColumnMapping(final ColumnMeta meta, final boolean isDerived, final String name, final Class<?> type, final int index) {
            this.meta = meta;
            this.isDerived = isDerived;
            this.name = name;
            this.type = type;
            this.index = index;
        }

        boolean hasIndex() {
            return index >= 0;
        }

        @Override
        public String toString() {
            return "ColumnMapping{" + "meta=" + meta + ", isDerived=" + isDerived + ", name=" + name + ", type=" + type + ", index=" + index + '}';
        }

    }

    /**
     * Return the row index column of the given table
     *
     * @param table FitsTable to process
     * @return the computed row index
     */
    public static int[] getRowIndex(final FitsTable table) {
        // lazy:
        int[] rowIndex = table.getColumnDerivedInt(COLUMN_ROW_INDEX);

        if (rowIndex == null) {
            final int nRows = table.getNbRows();
            rowIndex = new int[nRows];

            for (int i = 0; i < nRows; i++) {
                rowIndex[i] = i;
            }

            table.setColumnDerivedValue(COLUMN_ROW_INDEX, rowIndex);
        }

        return rowIndex;
    }
}
