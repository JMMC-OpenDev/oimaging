/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui;

import fr.jmmc.jmcs.gui.component.BasicTableSorter;
import fr.jmmc.jmcs.gui.util.AutofitTableColumns;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oiexplorer.core.gui.model.ColumnsTableModel;
import static fr.jmmc.oiexplorer.core.gui.model.ColumnsTableModel.COLUMN_COL_INDEX;
import static fr.jmmc.oiexplorer.core.gui.model.ColumnsTableModel.COLUMN_ROW_INDEX;
import fr.jmmc.oiexplorer.core.gui.model.KeywordsTableModel;
import fr.jmmc.oitools.fits.FitsHDU;
import fr.jmmc.oitools.fits.FitsTable;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bourgesl
 */
public final class FitsTableViewerPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(FitsTableViewerPanel.class.getName());

    private static final TableCellRenderer RDR_NUM_INSTANCE = new TableCellNumberRenderer();

    /* members */
    private final KeywordsTableModel keywordsModel;
    private final BasicTableSorter keywordsTableSorter;
    private final ColumnsTableModel columnsModel;
    private final BasicTableSorter columnsTableSorter;

    /** Creates new form FitsTableViewer */
    public FitsTableViewerPanel() {
        this.keywordsModel = new KeywordsTableModel();
        this.columnsModel = new ColumnsTableModel();

        initComponents();

        // Configure table sorting
        keywordsTableSorter = new BasicTableSorter(keywordsModel, jTableKeywords.getTableHeader());
        jTableKeywords.setModel(keywordsTableSorter);

        columnsTableSorter = new BasicTableSorter(columnsModel, jTableColumns.getTableHeader());
        jTableColumns.setModel(columnsTableSorter);

        // Fix row height:
        SwingUtils.adjustRowHeight(jTableKeywords);
        SwingUtils.adjustRowHeight(jTableColumns);

        jTableKeywords.setDefaultRenderer(Boolean.class, RDR_NUM_INSTANCE);
        jTableKeywords.setDefaultRenderer(Double.class, RDR_NUM_INSTANCE);
        jTableColumns.setDefaultRenderer(Float.class, RDR_NUM_INSTANCE);
        jTableColumns.setDefaultRenderer(Double.class, RDR_NUM_INSTANCE);
    }

    public void setViewerOptions(boolean includeDerivedColumns, boolean expandRows) {
        columnsModel.setIncludeDerivedColumns(includeDerivedColumns);
        columnsModel.setExpandRows(expandRows);
    }

    // Display Table
    public void setHdu(final FitsHDU hdu) {
        keywordsModel.setFitsHdu(hdu);

        final FitsTable table = (hdu instanceof FitsTable) ? (FitsTable) hdu : null;
        columnsModel.setFitsHdu(table);

        if (jTableKeywords.getRowCount() != 0) {
            AutofitTableColumns.autoResizeTable(jTableKeywords);
        }
        if (jTableColumns.getRowCount() != 0) {
            AutofitTableColumns.autoResizeTable(jTableColumns);
        }

        if (table != null) {
            jScrollPaneColumns.setVisible(true);
            jSplitPaneVert.setDividerLocation(0.25);
        } else {
            jScrollPaneColumns.setVisible(false);
            jSplitPaneVert.setDividerLocation(1.0);
        }
    }

    public void setSelection(final int row, final int col) {
        if (logger.isDebugEnabled()) {
            logger.debug("setSelection (row, col) = ({}, {})", row, col);
        }
        final int nRows = jTableColumns.getRowCount();

        if (nRows != 0) {
            final int rowColIdx = columnsTableSorter.findColumn(COLUMN_ROW_INDEX);
            final int colColIdx = (col != -1) ? columnsTableSorter.findColumn(COLUMN_COL_INDEX) : -1;

            if (logger.isDebugEnabled()) {
                logger.debug("rowColIdx: {}", rowColIdx);
                logger.debug("colColIdx: {}", colColIdx);
            }

            int rowIdx = -1;

            // Iterate on rows:
            for (int i = 0; i < nRows; i++) {
                final Integer rowValue = (Integer) columnsTableSorter.getValueAt(i, rowColIdx);

                // check row first:
                if ((rowValue != null) && (rowValue == row)) {
                    if (colColIdx != -1) {
                        final Integer colValue = (Integer) columnsTableSorter.getValueAt(i, colColIdx);

                        // check col:
                        if ((colValue != null) && (colValue != col)) {
                            // skip row:
                            continue;
                        }
                    }
                    // match row (and optionally col):
                    rowIdx = i;
                    // exit loop (first match)
                    break;
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("rowIdx: {}", rowIdx);
            }

            if (rowIdx != -1) {
                jTableColumns.getSelectionModel().setSelectionInterval(rowIdx, rowIdx);

                // Move view to show found row
                jTableColumns.scrollRectToVisible(jTableColumns.getCellRect(rowIdx, 0, true));
            } else {
                jTableColumns.getSelectionModel().clearSelection();
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPaneVert = new javax.swing.JSplitPane();
        jScrollPaneKeywords = new javax.swing.JScrollPane();
        jTableKeywords = new javax.swing.JTable();
        jScrollPaneColumns = new javax.swing.JScrollPane();
        jTableColumns = new javax.swing.JTable();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jSplitPaneVert.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPaneVert.setName("jSplitPaneVert"); // NOI18N

        jScrollPaneKeywords.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPaneKeywords.setAutoscrolls(true);
        jScrollPaneKeywords.setName("jScrollPaneKeywords"); // NOI18N
        jScrollPaneKeywords.setPreferredSize(new java.awt.Dimension(300, 300));

        jTableKeywords.setModel(keywordsModel);
        jTableKeywords.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTableKeywords.setMinimumSize(new java.awt.Dimension(50, 50));
        jTableKeywords.setName("jTableKeywords"); // NOI18N
        jTableKeywords.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPaneKeywords.setViewportView(jTableKeywords);

        jSplitPaneVert.setLeftComponent(jScrollPaneKeywords);

        jScrollPaneColumns.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        jScrollPaneColumns.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPaneColumns.setName("jScrollPaneColumns"); // NOI18N
        jScrollPaneColumns.setPreferredSize(new java.awt.Dimension(300, 300));

        jTableColumns.setModel(columnsModel);
        jTableColumns.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTableColumns.setMinimumSize(new java.awt.Dimension(50, 50));
        jTableColumns.setName("jTableColumns"); // NOI18N
        jTableColumns.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPaneColumns.setViewportView(jTableColumns);

        jSplitPaneVert.setRightComponent(jScrollPaneColumns);

        add(jSplitPaneVert, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPaneColumns;
    private javax.swing.JScrollPane jScrollPaneKeywords;
    private javax.swing.JSplitPane jSplitPaneVert;
    private javax.swing.JTable jTableColumns;
    private javax.swing.JTable jTableKeywords;
    // End of variables declaration//GEN-END:variables

    /**
     * Used to format numbers in cells.
     *
     * @warning: No trace log implemented as this is very often called (performance).
     */
    private final static class TableCellNumberRenderer extends DefaultTableCellRenderer {

        /** default serial UID for Serializable interface */
        private static final long serialVersionUID = 1;

        /**
         * Constructor
         */
        private TableCellNumberRenderer() {
            super();
        }

        /**
         * Sets the <code>String</code> object for the cell being rendered to
         * <code>value</code>.
         *
         * @param value  the string value for this cell; if value is
         *          <code>null</code> it sets the text value to an empty string
         * @see JLabel#setText
         *
         */
        @Override
        public void setValue(final Object value) {
            String text = "";
            if (value != null) {
                if (value instanceof Double) {
                    text = NumberUtils.format(((Double) value).doubleValue());
                } else if (value instanceof Boolean) {
                    text = ((Boolean) value).booleanValue() ? "T" : "F";
                } else {
                    text = value.toString();
                }
            }
            setText(text);
        }
    }

}
