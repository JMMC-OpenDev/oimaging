/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui;

import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEvent;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventListener;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventType;
import fr.jmmc.oiexplorer.core.model.plot.Axis;
import fr.jmmc.oiexplorer.core.model.plot.ColorMapping;
import fr.jmmc.oiexplorer.core.model.plot.PlotDefinition;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OITable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Panel allow to select columns of data to be plotted.
 * After being created and inserted in a GUI, it becomes plotDefinition editor of a dedicated plotDefinition using setPlotDefId().
 * It can also be editor for the plotDefinition of a particular Plot using setPlotId(). In the Plot case, 
 * the subset is also watched to find available columns to plot.
 * 
 * @author mella
 */
public final class PlotDefinitionEditor extends javax.swing.JPanel implements OIFitsCollectionManagerEventListener {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Logger */
    private final static Logger logger = LoggerFactory.getLogger(PlotDefinitionEditor.class);

    /* members */
    /** OIFitsCollectionManager singleton */
    private final OIFitsCollectionManager ocm = OIFitsCollectionManager.getInstance();
    /** OPTIONAL plot identifier */
    private String plotId = null;
    /** plot definition identifier */
    private String plotDefId = null;
    /* Swing components */
    /** Store all choices available to plot on x axis given the plot's subset if any */
    private final List<String> xAxisChoices = new LinkedList<String>();
    /** Store all choices available to plot on y axes given the plot's subset if any */
    private final List<String> yAxisChoices = new LinkedList<String>();
    /** List of y axes with their editors (identity hashcode) */
    private final HashMap<Axis, AxisEditor> yAxes = new LinkedHashMap<Axis, AxisEditor>();
    /** Flag to declare that component has to notify an event from user gesture */
    private boolean notify;
    /** xAxisEditor */
    private AxisEditor xAxisEditor;

    /** Creates new form PlotDefinitionEditor */
    public PlotDefinitionEditor() {
        // TODO maybe move it in setPlotId, setPlotId to register to te proper eventnotifiers instead of all
        ocm.getPlotDefinitionChangedEventNotifier().register(this);
        ocm.getPlotChangedEventNotifier().register(this);

        initComponents();
        postInit();
    }

    /**
     * Free any resource or reference to this instance :
     * remove this instance from OIFitsCollectionManager event notifiers
     */
    @Override
    public void dispose() {
        if (logger.isDebugEnabled()) {
            logger.debug("dispose: {}", ObjectUtils.getObjectInfo(this));
        }

        ocm.unbind(this);
    }

    /**
     * This method is useful to set the models and specific features of initialized swing components :
     */
    private void postInit() {
        // start with compact form
        extendedPanel.setVisible(false);

        // TODO check if it has to be done by the netbeans GUI builder ?
        xAxisEditor = new AxisEditor(this);
        xAxisPanel.add(xAxisEditor);
    }

    /**
     * Fill axes combo boxes with all distinct columns present in the available
     * tables.
     * @param plotDef plot definition to use
     * @param oiFitsSubset OIFits structure coming from plot's subset definition
     */
    private void refreshForm(final PlotDefinition plotDef, final OIFitsFile oiFitsSubset) {
        logger.debug("refreshForm : plotDefId = {} - plotDef {}", plotDefId, plotDef);

        try {
            // Leave programatic changes on widgets ignored to prevent model changes 
            notify = false;

            // Clear all content
            // TODO: fix that code: invalid as modifying the internal list is forbidden !
            xAxisChoices.clear();
            yAxisChoices.clear();

            // clear y AxisEditors
            // TODO find somehing cleaner
            yAxes.clear();
            yAxesPanel.removeAll();

            // At present time on plotDef is required to work : if it is null then return and leave in reset state
            if (plotDef == null) {
                return;
            }

            // Add column present in associated subset if any
            // TODO generate one synthetic OiFitsSubset to give all available choices
            if (oiFitsSubset != null) {
                // Get whole available columns
                final Set<String> columns = getDistinctColumns(oiFitsSubset);

                xAxisChoices.addAll(columns);
                yAxisChoices.addAll(columns);
            }
            // Add choices present in the associated plotDef
            final String currentX = plotDef.getXAxis().getName();
            if (!xAxisChoices.contains(currentX)) {
                xAxisChoices.add(currentX);
            }

            for (Axis y : plotDef.getYAxes()) {
                final String currentY = y.getName();
                if (!yAxisChoices.contains(currentY)) {
                    yAxisChoices.add(currentY);
                }
            }

            logger.debug("refreshForm : xAxisChoices {}, yAxisChoices {}", xAxisChoices, yAxisChoices);

            xAxisEditor.setAxis((Axis) plotDef.getXAxis().clone(), xAxisChoices);

            // fill with associated plotdefinition            
            if (logger.isDebugEnabled()) {
                logger.debug("refreshForm : yaxes to add : {}", plotDef.getYAxes());
            }
            for (Axis yAxis : plotDef.getYAxes()) {
                addYEditor((Axis) yAxis.clone());
            }

            // Fill colorMapping combobox
            colorMappingComboBox.removeAllItems();
            for (ColorMapping cm : ColorMapping.values()) {
                if (cm != ColorMapping.OBSERVATION_DATE) { // not implemented
                    colorMappingComboBox.addItem(cm);
                }
            }
            colorMappingComboBox.setSelectedItem((plotDef.getColorMapping() != null) ? plotDef.getColorMapping() : ColorMapping.WAVELENGTH_RANGE);

            // Init flaggedDataCheckBox
            flaggedDataCheckBox.setSelected(plotDef.isSkipFlaggedData());
            // Init drawLinesCheckBox
            drawLinesCheckBox.setSelected(plotDef.isDrawLine());

        } finally {
            notify = true;
        }
    }

    /**
     * Return the set of distinct columns available in the table of given OIFitsFile.
     * @param oiFitsFile oifitsFile to search data into
     * @return a Set of Strings with every distinct column names
     */
    private Set<String> getDistinctColumns(final OIFitsFile oiFitsFile) {
        final Set<String> columns = new LinkedHashSet<String>();


        // Add every column of every tables for given target into combomodel sets
        // TODO optimization could be operated walking only on the first element
        for (OITable oiTable : oiFitsFile.getOiVis2()) {
            oiTable.getNumericalColumnsNames(columns);
        }
        for (OITable oiTable : oiFitsFile.getOiVis()) {
            oiTable.getNumericalColumnsNames(columns);
        }
        for (OITable oiTable : oiFitsFile.getOiT3()) {
            oiTable.getNumericalColumnsNames(columns);
        }
        return columns;
    }

    /**
     * Return the set of distinct columns from the tables of given 
     * OIFitsFile and compatible with given column.
     * @param oiFitsFile oifitsFile to search data into
     * @return a Set of Strings with every distinct column names
     */
    private Set<String> getDistinctColumns(final OIFitsFile oiFitsFile, String columnName) {
        final Set<String> columns = new LinkedHashSet<String>();

        // TODO see previous getDistinctColumns() for perf note
        // Add every column of every tables for given target into combomodel sets
        for (OITable oiTable : oiFitsFile.getOiVis2()) {
            ColumnMeta meta = oiTable.getColumnMeta(columnName);
            if (meta != null) {
                oiTable.getNumericalColumnsNames(columns);
            } else {
                logger.debug("Can't use data from '{}' table with column '{}'", oiTable, columnName);
            }
        }
        for (OITable oiTable : oiFitsFile.getOiVis()) {
            ColumnMeta meta = oiTable.getColumnMeta(columnName);
            if (meta != null) {
                oiTable.getNumericalColumnsNames(columns);
            } else {
                logger.debug("Can't use data from '{}' table with column '{}'", oiTable, columnName);
            }
        }
        for (OITable oiTable : oiFitsFile.getOiT3()) {
            ColumnMeta meta = oiTable.getColumnMeta(columnName);
            if (meta != null) {
                oiTable.getNumericalColumnsNames(columns);
            } else {
                logger.debug("Can't use data from '{}' table with column '{}'", oiTable, columnName);
            }
        }
        return columns;
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        colorMappingLabel = new javax.swing.JLabel();
        colorMappingComboBox = new javax.swing.JComboBox();
        plotDefinitionName = new javax.swing.JLabel();
        extendedPanel = new javax.swing.JPanel();
        yLabel = new javax.swing.JLabel();
        xLabel = new javax.swing.JLabel();
        addYAxisButton = new javax.swing.JButton();
        delYAxisButton = new javax.swing.JButton();
        yAxesScrollPane = new javax.swing.JScrollPane();
        yAxesPanel = new javax.swing.JPanel();
        xAxisPanel = new javax.swing.JPanel();
        detailledToggleButton = new javax.swing.JToggleButton();
        drawLinesCheckBox = new javax.swing.JCheckBox();
        flaggedDataCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        colorMappingLabel.setText("Colored by");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(colorMappingLabel, gridBagConstraints);

        colorMappingComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorMappingComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        add(colorMappingComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        add(plotDefinitionName, gridBagConstraints);

        extendedPanel.setLayout(new java.awt.GridBagLayout());

        yLabel.setText("yAxis");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        extendedPanel.add(yLabel, gridBagConstraints);

        xLabel.setText("xAxis");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        extendedPanel.add(xLabel, gridBagConstraints);

        addYAxisButton.setText("+");
        addYAxisButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addYAxisButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        extendedPanel.add(addYAxisButton, gridBagConstraints);

        delYAxisButton.setText("-");
        delYAxisButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delYAxisButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        extendedPanel.add(delYAxisButton, gridBagConstraints);

        yAxesScrollPane.setBorder(null);

        yAxesPanel.setBorder(null);
        yAxesPanel.setLayout(new javax.swing.BoxLayout(yAxesPanel, javax.swing.BoxLayout.Y_AXIS));
        yAxesScrollPane.setViewportView(yAxesPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        extendedPanel.add(yAxesScrollPane, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        extendedPanel.add(xAxisPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        add(extendedPanel, gridBagConstraints);

        detailledToggleButton.setText("...");
        detailledToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                detailledToggleButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        add(detailledToggleButton, gridBagConstraints);

        drawLinesCheckBox.setText("draw lines");
        drawLinesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                drawLinesCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        add(drawLinesCheckBox, gridBagConstraints);

        flaggedDataCheckBox.setText("skip flagged data");
        flaggedDataCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flaggedDataCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        add(flaggedDataCheckBox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void addYAxisButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addYAxisButtonActionPerformed
        Axis axis = new Axis();
        getPlotDefinition().getYAxes().add(axis);
        addYEditor(axis);
        updateModel();
    }//GEN-LAST:event_addYAxisButtonActionPerformed

    private void delYAxisButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delYAxisButtonActionPerformed
        if (yAxes.size() > 1) {
            // TODO replace by removal of the last yCombobox which one has lost the foxus
            Axis[] yAxisArray = yAxes.keySet().toArray(new Axis[]{});
            Axis yAxis = yAxisArray[yAxes.size() - 1];
            delYEditor(yAxis);

            // Delete from PlotDefinition
            getPlotDefinition().getYAxes().remove(yAxis);
            updateModel();
        }
    }//GEN-LAST:event_delYAxisButtonActionPerformed

    private void colorMappingComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorMappingComboBoxActionPerformed
        updateModel();
    }//GEN-LAST:event_colorMappingComboBoxActionPerformed

    private void detailledToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_detailledToggleButtonActionPerformed
        extendedPanel.setVisible(detailledToggleButton.isSelected());
        revalidate();
    }//GEN-LAST:event_detailledToggleButtonActionPerformed

    private void flaggedDataCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_flaggedDataCheckBoxActionPerformed
        updateModel();
    }//GEN-LAST:event_flaggedDataCheckBoxActionPerformed

    private void drawLinesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drawLinesCheckBoxActionPerformed
        updateModel();
    }//GEN-LAST:event_drawLinesCheckBoxActionPerformed

    /**
     * Return colorMapping Value stored by associated combobox.
     * @return the colorMapping Value stored by associated combobox.
     */
    private ColorMapping getColorMapping() {
        return (ColorMapping) colorMappingComboBox.getSelectedItem();
    }

    private void yAxisComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
        updateModel();
    }

    /** Create a new widget to edit given Axis.
     * @param axis axis to be edited by new yAxisEditor
     */
    private void addYEditor(final Axis yAxis) {

        // Link new Editor and Axis
        AxisEditor yAxisEditor = new AxisEditor(this);
        yAxisEditor.setAxis(yAxis, yAxisChoices);

        // Add in editor list
        yAxesPanel.add(yAxisEditor);

        // Add in Map
        yAxes.put(yAxis, yAxisEditor);

        revalidate();
    }

    /** Synchronize management for the addition of a given combo and update GUI. 
     * @param yAxis yAxis of editor to remove
     */
    private void delYEditor(final Axis yAxis) {
        // Link new Editor and Axis
        AxisEditor yAxisEditor = yAxes.get(yAxis);

        // Remove from editor list
        yAxesPanel.remove(yAxisEditor);

        // Delete from Map
        yAxes.remove(yAxis);

        revalidate();

    }

    /** 
     * Request a plotDefinitionUpdate to the OIFitsCollectionManager     
     */
    public void updateModel() {
        logger.debug("updateModel notify = {}", notify);
        if (notify) {
            // get copy:
            final PlotDefinition plotDefCopy = getPlotDefinition();

            if (plotDefCopy != null) {
                // handle xAxis
                plotDefCopy.setXAxis((Axis) xAxisEditor.getAxis());
                // handle yAxes
                final List<Axis> yAxesCopy = plotDefCopy.getYAxes();
                yAxesCopy.clear();
                // We may also compute the yAxes Collection calling getAxis on the editor list
                // This may reduce references nightmare 
                yAxesCopy.addAll(yAxes.keySet());
            }

            plotDefCopy.setColorMapping(getColorMapping());

            plotDefCopy.setDrawLine(drawLinesCheckBox.isSelected());
            plotDefCopy.setSkipFlaggedData(flaggedDataCheckBox.isSelected());

            ocm.updatePlotDefinition(this, plotDefCopy);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addYAxisButton;
    private javax.swing.JComboBox colorMappingComboBox;
    private javax.swing.JLabel colorMappingLabel;
    private javax.swing.JButton delYAxisButton;
    private javax.swing.JToggleButton detailledToggleButton;
    private javax.swing.JCheckBox drawLinesCheckBox;
    private javax.swing.JPanel extendedPanel;
    private javax.swing.JCheckBox flaggedDataCheckBox;
    private javax.swing.JLabel plotDefinitionName;
    private javax.swing.JPanel xAxisPanel;
    private javax.swing.JLabel xLabel;
    private javax.swing.JPanel yAxesPanel;
    private javax.swing.JScrollPane yAxesScrollPane;
    private javax.swing.JLabel yLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Define the plot identifier and reset plot
     * @param plotId plot identifier or null to reset state
     */
    public void setPlotId(final String plotId) {
        logger.debug("setPlotId {}", plotId);

        final String prevPlotId = this.plotId;

        _setPlotId(plotId);

        if (plotId != null && !ObjectUtils.areEquals(prevPlotId, plotId)) {
            logger.debug("firePlotChanged {}", plotId);

            // bind(plotId) ?
            // fire PlotChanged event to initialize correctly the widget:
            ocm.firePlotChanged(null, plotId, this); // null forces different source
        }
    }

    /**
     * Define the plot identifier and reset plot
     * @param plotId plot identifier or null to reset state
     */
    private void _setPlotId(final String plotId) {
        logger.debug("_setPlotId {}", plotId);

        this.plotId = plotId;

        // reset case:
        if (plotId == null) {
            // reset plotDefId:
            if (this.plotDefId != null) {
                _setPlotDefId(null);
            }

            // TODO: how to fire reset event ie DELETE(id)
            refreshForm(null, null);
        }
    }

    /**
     * Return a new copy of the PlotDefinition given its identifier (to update it)
     * @return copy of the PlotDefinition or null if not found
     */
    private PlotDefinition getPlotDefinition() {
        if (plotDefId != null) {
            return ocm.getPlotDefinition(plotDefId);
        }
        return null;
    }

    /**
     * Define the plot definition identifier and reset plot definition
     * @param plotDefId plot definition identifier
     */
    public void setPlotDefId(final String plotDefId) {
        logger.debug("setPlotDefId {}", plotDefId);

        final String prevPlotDefId = this.plotDefId;

        _setPlotDefId(plotDefId);

        // reset plotId:
        if (this.plotId != null) {
            _setPlotId(null);
        }

        // reset case:
        if (plotDefId == null) {
            // reset plotId:
            if (this.plotId != null) {
                _setPlotId(null);
            }

            // TODO: how to fire reset event ie DELETE(id)
            refreshForm(null, null);
        }

        if (plotDefId != null && !ObjectUtils.areEquals(prevPlotDefId, plotDefId)) {
            logger.debug("firePlotDefinitionChanged {}", plotDefId);

            // bind(plotDefId) ?
            // fire PlotDefinitionChanged event to initialize correctly the widget:
            ocm.firePlotDefinitionChanged(null, plotDefId, this); // null forces different source
        }
    }

    /**
     * Define the plot definition identifier and reset plot definition
     * @param plotDefId plot definition identifier
     */
    private void _setPlotDefId(final String plotDefId) {
        logger.debug("_setPlotDefId {}", plotDefId);

        this.plotDefId = plotDefId;

        // do not change plotId
    }

    /*
     * OIFitsCollectionManagerEventListener implementation 
     */
    /**
     * Return the optional subject id i.e. related object id that this listener accepts
     * @param type event type
     * @return subject id (null means accept any event) or DISCARDED_SUBJECT_ID to discard event
     */
    @Override
    public String getSubjectId(final OIFitsCollectionManagerEventType type) {
        switch (type) {
            case PLOT_DEFINITION_CHANGED:
                if (this.plotDefId != null) {
                    return this.plotDefId;
                }
                break;
            case PLOT_CHANGED:
                if (this.plotId != null) {
                    return this.plotId;
                }
                break;
            default:
        }
        return DISCARDED_SUBJECT_ID;
    }

    /**
     * Handle the given OIFits collection event
     * @param event OIFits collection event
     */
    @Override
    public void onProcess(final OIFitsCollectionManagerEvent event) {
        logger.debug("onProcess {}", event);

        switch (event.getType()) {
            case PLOT_DEFINITION_CHANGED:
                // define id of associated plotDefinition
                _setPlotDefId(event.getPlotDefinition().getName());

                refreshForm(event.getPlotDefinition(), null);
                break;
            case PLOT_CHANGED:
                final PlotDefinition plotDef = event.getPlot().getPlotDefinition();

                // define id of associated plotDefinition
                _setPlotDefId(plotDef.getName());

                refreshForm(plotDef, event.getPlot().getSubsetDefinition().getOIFitsSubset());
                break;
            default:
                logger.debug("onProcess {} - done", event);
        }
    }
}
