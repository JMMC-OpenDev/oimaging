/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ***************************************************************************** */
package fr.jmmc.oimaging.gui;

import fr.jmmc.jmcs.App;
import fr.jmmc.jmcs.gui.component.BasicTableSorter;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oimaging.model.ColumnDesc;
import fr.jmmc.oimaging.model.ResultSetTableModel;
import fr.jmmc.oimaging.model.RatingCell;
import fr.jmmc.oimaging.services.ServiceResult;
import java.awt.Dimension;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author martin
 */
public class TablePanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(TablePanel.class);
    
    /**
     * ResultSet table model
     */
    private final ResultSetTableModel resultSetTableModel;
    
    private final BasicTableSorter resultSetTableSorter;
    
    /** object with rendering and editing responsabilities for column RATING. */
    private final RatingCell ratingCell;
    
    private final SuccessCell successCell;
    
    private final TableCellRenderer standardRenderer = new TableCellNumberRenderer();

    /**
     * Creates new form TablePanel
     */
    public TablePanel() {

        // Build ResultsTable
        resultSetTableModel = new ResultSetTableModel();
        
        ratingCell = new RatingCell();
        successCell = new SuccessCell();

        initComponents();

        // must come after initComponents()
        resultSetTableSorter = new BasicTableSorter(resultSetTableModel, jResultSetTable.getTableHeader());

        jResultSetTable.setModel(resultSetTableSorter);
        SwingUtils.adjustRowHeight(jResultSetTable);
        
        // set default renderers
        jResultSetTable.setDefaultRenderer(Boolean.class, standardRenderer);
        jResultSetTable.setDefaultRenderer(Double.class, standardRenderer);
        jResultSetTable.setDefaultRenderer(Float.class, standardRenderer);
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
        jScrollPane1 = new javax.swing.JScrollPane();
        jResultSetTable = new javax.swing.JTable();
        jPanelTableOptions = new javax.swing.JPanel();
        jButtonShowTableEditor = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jResultSetTable.setModel(resultSetTableModel);
        jScrollPane1.setViewportView(jResultSetTable);

        jSplitPane1.setRightComponent(jScrollPane1);

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
        // Set the dialog box
        JDialog dialog = new JDialog(App.getFrame(), "Edit table headers", true);
        TableEditorPanel tableEditorPanel = new TableEditorPanel(dialog, new ArrayList<>(getTableModel().getListColumnDesc()), getTableModel().getListColumnDesc()); // TODO : fix displayed column
        dialog.setContentPane(tableEditorPanel);
        dialog.setMinimumSize(new Dimension(600, 500));
        dialog.setLocationRelativeTo(null); // centering the dialog on the screen
        dialog.setResizable(true);
        dialog.setVisible(true);
        
        // when dialog returns we set the chosen columns by user
        if (tableEditorPanel.getProcessOK()) setUserListColumnDesc(tableEditorPanel.getColumnsToDisplay());
    }                                                      

    /** find the columns to apply pretty renderers. 
     * should  be called each time the columns change
     * it is called in setResults() and setUserUnionColumnDesc().
     */
    public void reTargetRenderers () {
        // We must re-ask for rendering since the TableColumn object is different
        try {
            final TableColumn columnRating = jResultSetTable.getColumn(ResultSetTableModel.HardCodedColumn.RATING.toString());
            columnRating.setCellRenderer(ratingCell);
            columnRating.setCellEditor(ratingCell);
        } 
        catch (IllegalArgumentException e) {
            logger.debug("Missing RATING column, cannot set Renderer and Editor.");
        }
        
        try {
            final TableColumn columnSuccess = jResultSetTable.getColumn(ResultSetTableModel.HardCodedColumn.SUCCESS.toString());
            columnSuccess.setCellRenderer(successCell);
        } 
        catch (IllegalArgumentException e) {
            logger.debug("Missing SUCCESS column, cannot set Renderer.");
        }
    }
    
    public void setResults(List<ServiceResult> results) {
        getTableModel().setResults(results);
        reTargetRenderers();
    }
    
    /** modify the user selected columns in BasicTableSorter
     * Used when Table Editor dialog returns and we must apply the user choices.
     * @param userListColumnDesc the new list of columns selected by user. Must be a (possibly reordered) sublist of ResultSetTableModel.getListColumnDesc()
     */
    public void setUserListColumnDesc(List<ColumnDesc> userListColumnDesc) {
        // TODO set UserListColumnDesc in BasicTableSorter
        reTargetRenderers();
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
    private javax.swing.JScrollPane jScrollPane1;
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
                } else {
                    text = value.toString();
                }
            }
            setText(text);
        }
    }
    
}
