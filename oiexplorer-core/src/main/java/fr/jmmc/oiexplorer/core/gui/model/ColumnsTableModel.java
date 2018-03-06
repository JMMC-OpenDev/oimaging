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

    /* members */
    /** FITS table reference */
    private FitsTable table = null;
    /** flag to include derived columns */
    private boolean includeDerivedColumns = true;
    /** flag to expand or not arrays (2D only) */
    private boolean expandArrays = true;
    /** column mapping */
    private final ArrayList<ColumnMapping> mappings = new ArrayList<ColumnMapping>();
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

    private void init() {
        mappings.clear();
        if (table == null) {
            return;
        }

        for (ColumnMeta meta : table.getColumnDescCollection()) {
            addColumns(meta, false);
        }
        if (includeDerivedColumns) {
            for (ColumnMeta meta : table.getColumnDerivedDescCollection()) {
                addColumns(meta, true);
            }
        }
    }

    private void addColumns(final ColumnMeta meta, final boolean isDerived) {
        final String name = meta.getName();
        if (meta.is3D()) {
            // use String representation:
            mappings.add(new ColumnMapping(meta, isDerived, name, String.class));
        } else {
            final Class<?> type = getType(meta.getDataType());
            if (meta.isArray()) {
                if (expandArrays && meta.getDataType() != Types.TYPE_COMPLEX
                        && !OIFitsConstants.COLUMN_STA_CONF.equals(name)) {
                    final int repeat = meta.getRepeat();
                    for (int i = 0; i < repeat; i++) {
                        mappings.add(new ColumnMapping(meta, isDerived, name + "_" + i, type, i));
                    }
                } else {
                    // use String representation:
                    mappings.add(new ColumnMapping(meta, isDerived, name, String.class));
                }
            } else {
                mappings.add(new ColumnMapping(meta, isDerived, name, type));
            }
        }
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
        return mappings.size();
    }

    @Override
    public String getColumnName(final int column) {
        return mappings.get(column).name;
    }

    @Override
    public Class<?> getColumnClass(final int column) {
        return mappings.get(column).type;
    }

    @Override
    public int getRowCount() {
        return (table != null) ? table.getNbRows() : 0;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        if (table == null) {
            return null;
        }
        final ColumnMapping mapping = mappings.get(columnIndex);
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

    private Object getColumnValue(final ColumnMapping mapping, final int rowIndex) {
        final ColumnMeta column = mapping.meta;
        final boolean isDerived = mapping.isDerived;

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
                        if (mapping.hasIndex()) {
                            return Short.valueOf(rowValues[mapping.index]);
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
                    final int[][] iValues = /*(isDerived) ? table.getColumnDerivedInts(column.getName()) : */ table.getColumnInts(column.getName());
                    if (iValues != null) {
                        final int[] rowValues = iValues[rowIndex];
                        if (mapping.hasIndex()) {
                            return NumberUtils.valueOf(rowValues[mapping.index]);
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
                final int[] iValues = table.getColumnInt(column.getName());
                if (iValues != null) {
                    return NumberUtils.valueOf(iValues[rowIndex]);
                }
                break;

            case TYPE_DBL:
                if (column.isArray()) {
                    final double[][] dValues = table.getColumnAsDoubles(column.getName());
                    if (dValues != null) {
                        final double[] rowValues = dValues[rowIndex];
                        if (mapping.hasIndex()) {
                            return Double.valueOf(rowValues[mapping.index]);
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
                        if (mapping.hasIndex()) {
                            if (USE_STRING_FOR_BOOLEAN) {
                                return (rowValues[mapping.index] ? 'T' : 'F');
                            }
                            return Boolean.valueOf(rowValues[mapping.index]);
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

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
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
}
