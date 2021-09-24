/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ***************************************************************************** */
package fr.jmmc.oimaging.gui;

import fr.jmmc.jmcs.gui.component.BasicTableColumnMovedListener;
import fr.jmmc.jmcs.gui.component.BasicTableSorter;
import fr.jmmc.jmcs.gui.util.AutofitTableColumns;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oimaging.Preferences;
import fr.jmmc.oimaging.model.ResultSetTableModel;
import fr.jmmc.oimaging.model.RatingCell;
import fr.jmmc.oimaging.services.ServiceResult;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author martin
 */
public class TablePanel extends javax.swing.JPanel implements BasicTableColumnMovedListener {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(TablePanel.class);

    /** Table key to remember dialog dimensions */
    private final static String TABLE_EDITOR_DIMENSION_KEY = "___OIMAGING_TABLE_EDITOR_DIMENSION";

    /* members */
    /** ResultSet table model */
    private final ResultSetTableModel resultSetTableModel;
    /** Sorter table model */
    private final BasicTableSorter resultSetTableSorter;
    /** object handling rendering and editing for column RATING. */
    private final RatingCell ratingCell;
    /** object handling rendering for column Success. */
    private final SuccessCell successCell;
    /** custom number / date cell renderer */
    private final TableCellRenderer customCellRenderer = new TableCellNumberRenderer();
    /** preference singleton */
    private final Preferences myPreferences = Preferences.getInstance();

    /**
     * Creates new form TablePanel
     */
    public TablePanel() {

        // Build ResultsTable
        resultSetTableModel = new ResultSetTableModel();

        ratingCell = new RatingCell();
        successCell = new SuccessCell();

        initComponents();

        // Configure table sorting
        // must come after initComponents()
        resultSetTableSorter = new BasicTableSorter(resultSetTableModel, jResultSetTable.getTableHeader());

        // Process the listeners last to first, so register before jtable, not after:
        resultSetTableSorter.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(final TableModelEvent e) {
                // If the table structure has changed, reapply the custom renderer/editor on columns + auto-fit
                if ((e.getSource() != resultSetTableSorter)
                        || (e.getFirstRow() == TableModelEvent.HEADER_ROW)) {

                    updateTableRenderers();
                }
            }
        });
        resultSetTableSorter.setTableHeaderChangeListener(this);

        jResultSetTable.setModel(resultSetTableSorter);

        // Fix row height:
        SwingUtils.adjustRowHeight(jResultSetTable);

        // set default renderers
        jResultSetTable.setDefaultRenderer(Boolean.class, customCellRenderer);
        jResultSetTable.setDefaultRenderer(Double.class, customCellRenderer);
        jResultSetTable.setDefaultRenderer(Date.class, customCellRenderer);

        // load user preference for columns:
        resultSetTableSorter.setVisibleColumnNames(myPreferences.getResultsVisibleColumns());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPaneTable = new javax.swing.JScrollPane();
        jResultSetTable = new javax.swing.JTable();
        jPanelTableOptions = new javax.swing.JPanel();
        jButtonShowTableEditor = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jResultSetTable.setModel(resultSetTableModel);
        jResultSetTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jScrollPaneTable.setViewportView(jResultSetTable);

        jSplitPane1.setRightComponent(jScrollPaneTable);

        jPanelTableOptions.setLayout(new javax.swing.BoxLayout(jPanelTableOptions, javax.swing.BoxLayout.PAGE_AXIS));

        jButtonShowTableEditor.setText("Table editor");
        jButtonShowTableEditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonShowTableEditorActionPerformed(evt);
            }
        });
        jPanelTableOptions.add(jButtonShowTableEditor);

        jSplitPane1.setLeftComponent(jPanelTableOptions);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Display the table keywords editor and set the new headers
     */
    private void jButtonShowTableEditorActionPerformed(java.awt.event.ActionEvent evt) {

        // show the table editor dialog to select displayed columns:
	final List<String> availableColumns = resultSetTableSorter.getVisibleColumnNames();
        final List<String> hiddenColumns = getTableModel().getColumnNames();
        hiddenColumns.removeAll(availableColumns);

	final List<String> newAvailableColumns = TableEditorPanel.showEditor(
                hiddenColumns,
                availableColumns,
                TABLE_EDITOR_DIMENSION_KEY
        );

        if (newAvailableColumns != null) {
            // Update visible columns if needed:
            if (!availableColumns.equals(newAvailableColumns)) {
                setVisibleColumnNames(newAvailableColumns);
            }
        }
    }

    /**
     * find the columns to apply pretty renderers.
     * should  be called each time the columns change
     */
    public void updateTableRenderers() {
        // update renderer since the TableColumn object is different
        try {
            final TableColumn columnRating = jResultSetTable.getColumn(ResultSetTableModel.HardCodedColumn.RATING.toString());
            columnRating.setCellRenderer(ratingCell);
            columnRating.setCellEditor(ratingCell);
        } catch (IllegalArgumentException iae) {
            logger.debug("Missing RATING column, cannot set Renderer and Editor.", iae);
        }

        try {
            final TableColumn columnSuccess = jResultSetTable.getColumn(ResultSetTableModel.HardCodedColumn.SUCCESS.toString());
            columnSuccess.setCellRenderer(successCell);
        } catch (IllegalArgumentException iae) {
            logger.debug("Missing SUCCESS column, cannot set Renderer.", iae);
        }

        AutofitTableColumns.autoResizeTable(jResultSetTable, true, true); // include header width
    }

    public void setResults(List<ServiceResult> results) {
        final List<String> prevAllColumns = myPreferences.getResultsAllColumns();
        getTableModel().setResults(results, prevAllColumns);

        // Update all columns if needed:
        final List<String> newAllColumns = getTableModel().getColumnNames();
        if (!prevAllColumns.equals(newAllColumns)) {
            updateAllColumnsPreferences(newAllColumns);
            // show new columns:
            prevAllColumns.forEach(newAllColumns::remove);
            logger.debug("setResults: new columns : {}", prevAllColumns);
            final List<String> newVisibleColumns = resultSetTableSorter.getVisibleColumnNames();
            newAllColumns.forEach(newVisibleColumns::add);
            setVisibleColumnNames(newVisibleColumns);
        }
    }

    @Override
    public void tableColumnMoved(BasicTableSorter source) {
        // save preference after resultSetTableSorter updated:
        updateVisibleColumnsPreferences();
    }

    /**
     * modify the user selected columns in BasicTableSorter
     * Used when Table Editor dialog returns and we must apply the user choices.
     * @param visibleColumnNames the new list of columns selected by user.
     */
    private void setVisibleColumnNames(final List<String> visibleColumnNames) {
        logger.debug("setVisibleColumnNames: {}", visibleColumnNames);

        resultSetTableSorter.setVisibleColumnNames(visibleColumnNames);
        // save preference after resultSetTableSorter updated:
        updateVisibleColumnsPreferences();
    }

    private void updateVisibleColumnsPreferences() {
        final List<String> visibleColumnNames = resultSetTableSorter.getVisibleColumnNames();
        logger.debug("updateVisibleColumnsPreferences: {}", visibleColumnNames);
        myPreferences.setResultsVisibleColumns(visibleColumnNames);
    }

    private void updateAllColumnsPreferences(final List<String> allColumns) {
        logger.debug("updateAllColumnsPreferences: {}", allColumns);
        myPreferences.setResultsAllColumns(allColumns);
    }

    public ListSelectionModel getSelectionModel() {
        return getTable().getSelectionModel();
    }

    public int getSelectedRowsCount() {
        return getTable().getSelectedRowCount();
    }

    public List<ServiceResult> getSelectedRows() {
        List<ServiceResult> results = new ArrayList<>();

        for (int index : getTable().getSelectedRows()) {
            results.add(resultSetTableModel.getServiceResult(resultSetTableSorter.modelIndex(index)));
        }
        return results;
    }

    public ServiceResult getSelectedRow() {
        final List<ServiceResult> results = getSelectedRows();
        return (results.isEmpty()) ? null : results.get(0);
    }

    public void setSelectedRow(final int rowIndex) {
        final int index = resultSetTableSorter.viewIndex(rowIndex);
        getTable().setRowSelectionInterval(index, index);
    }

    private JTable getTable() {
        return this.jResultSetTable;
    }

    private ResultSetTableModel getTableModel() {
        return this.resultSetTableModel;
    }

    public void addControlComponent(JComponent component) {
        jPanelTableOptions.add(component);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonShowTableEditor;
    private javax.swing.JPanel jPanelTableOptions;
    private javax.swing.JTable jResultSetTable;
    private javax.swing.JScrollPane jScrollPaneTable;
    private javax.swing.JSplitPane jSplitPane1;
    // End of variables declaration//GEN-END:variables

    /**
     * Used to format numbers in cells.
     *
     * @warning: No trace log implemented as this is very often called (performance).
     */
    private final static class TableCellNumberRenderer extends DefaultTableCellRenderer {

        /** default serial UID for Serializable interface */
        private static final long serialVersionUID = 1;

        private final SimpleDateFormat tf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

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
                    text = NumberUtils.format(((Double) value));
                } else if (value instanceof Boolean) {
                    text = ((Boolean) value) ? "T" : "F";
                } else if (value instanceof Date) {
                    text = tf.format((Date) value);
                } else {
                    text = value.toString();
                }
            }
            setText(text);
        }
    }

}
