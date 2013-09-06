/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui;

import fr.jmmc.jmcs.gui.component.GenericListModel;
import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEvent;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventListener;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventType;
import fr.jmmc.oiexplorer.core.model.PlotDefinitionFactory;
import fr.jmmc.oiexplorer.core.model.plot.Axis;
import fr.jmmc.oiexplorer.core.model.plot.ColorMapping;
import fr.jmmc.oiexplorer.core.model.plot.PlotDefinition;
import fr.jmmc.oiexplorer.core.model.util.ColorMappingListCellRenderer;
import fr.jmmc.oitools.model.OIFitsFile;
import java.util.ArrayList;
import java.util.Collection;
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
    /** Define the max number of plots */
    private final static int MAX_Y_AXES = 2;

    /** Creates new form PlotDefinitionEditor */
    public PlotDefinitionEditor() {
        // TODO maybe move it in setPlotId, setPlotId to register to event notifiers instead of both:
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
        detailledToggleButtonActionPerformed(null);

        colorMappingComboBox.setRenderer(ColorMappingListCellRenderer.getListCellRenderer());

        // Fill colorMapping combobox
        for (ColorMapping cm : ColorMapping.values()) {
            if (cm != ColorMapping.OBSERVATION_DATE) { // not implemented
                colorMappingComboBox.addItem(cm);
            }
        }

        // TODO check if it has to be done by the netbeans GUI builder ?
        xAxisEditor = new AxisEditor(this);
        xAxisPanel.add(xAxisEditor);
    }

    private void resetForm() {
        logger.debug("resetForm : plotDefId = {}", plotDefId);

        // TODO: is it necessary to use notify flag here ?
        try {
            // Leave programatic changes on widgets ignored to prevent model changes 
            notify = false;

            // Clear all content
            xAxisChoices.clear();
            yAxisChoices.clear();

            // clear y AxisEditors
            yAxes.clear();
            yAxesPanel.removeAll();
        } finally {
            notify = true;
        }
    }

    /**
     * Fill axes combo boxes with all distinct columns present in the available
     * tables.
     * @param plotDef plot definition to use
     * @param oiFitsSubset OIFits structure coming from plot's subset definition
     */
    private void refreshForm(final PlotDefinition plotDef, final OIFitsFile oiFitsSubset) {
        logger.debug("refreshForm : plotDefId = {} - plotDef {}", plotDefId, plotDef);

        if (plotDef == null) {
            resetForm();
        } else {
            try {
                // Leave programatic changes on widgets ignored to prevent model changes 
                notify = false;

                // Add column present in associated subset if any
                // TODO generate one synthetic OiFitsSubset to give all available choices
                if (oiFitsSubset != null) {
                    // Get whole available columns
                    final Set<String> columns = getDistinctColumns(oiFitsSubset);

                    // Clear all content
                    xAxisChoices.clear();
                    xAxisChoices.addAll(columns);

                    yAxisChoices.clear();
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

                // clear y AxisEditors
                yAxes.clear();
                yAxesPanel.removeAll();

                for (Axis yAxis : plotDef.getYAxes()) {
                    addYEditor((Axis) yAxis.clone());
                }

                // Init colorMapping
                colorMappingComboBox.setSelectedItem((plotDef.getColorMapping() != null) ? plotDef.getColorMapping() : ColorMapping.WAVELENGTH_RANGE);

                // Init flaggedDataCheckBox
                flaggedDataCheckBox.setSelected(plotDef.isSkipFlaggedData());

                // Init drawLinesCheckBox
                drawLinesCheckBox.setSelected(plotDef.isDrawLine());

                checkYAxisActionButtons();

                refreshPlotDefinitionNames(plotDef);
            } finally {
                notify = true;
            }
        }
    }

    private void checkYAxisActionButtons() {
        // disable buttons to limit number of yAxes 
        addYAxisButton.setEnabled(yAxes.size() < MAX_Y_AXES);
        delYAxisButton.setEnabled(yAxes.size() > 1);
    }

    private void refreshPlotDefinitionNames(final PlotDefinition plotDef) {
        logger.debug("refreshPlotDefinitionNames: {}", plotId);

        // use identifiers to keep unique values:
        final Set<String> plotDefNames = new LinkedHashSet<String>();

        final StringBuilder sb = new StringBuilder(64);

        if (plotDef != null) {
            // Y axes:
            for (Axis axis : plotDef.getYAxes()) {
                // skip invalid axis names:
                if (axis.getName() != null) {
                    if (sb.length() != 0) {
                        sb.append(", ");
                    }
                    sb.append(axis.getName());
                }
            }
            // X axis:
            sb.append(" vs ").append(plotDef.getXAxis().getName());

            // add first entry corresponding to the edited plot definition:
            plotDefNames.add(sb.toString());
        }

        for (PlotDefinition plotDefPreset : PlotDefinitionFactory.getInstance().getDefaults()) {
            sb.setLength(0);
            sb.append("preset: ").append(plotDefPreset.getName());
            plotDefNames.add(sb.toString());
        }

        plotDefinitionComboBox.setModel(new GenericListModel<String>(new ArrayList<String>(plotDefNames), true));
        plotDefinitionComboBox.setSelectedIndex(0);
    }

    /**
     * Return the set of distinct columns available in the table of given OIFitsFile.
     * @param oiFitsFile oifitsFile to search data into
     * @return a Set of Strings with every distinct column names
     */
    private Set<String> getDistinctColumns(final OIFitsFile oiFitsFile) {
        final Set<String> columns = new LinkedHashSet<String>(32);

        // Add every column of every tables for given target into ordered sets
        if (oiFitsFile.hasOiVis2()) {
            oiFitsFile.getOiVis2()[0].getNumericalColumnsNames(columns);
        }
        if (oiFitsFile.hasOiVis()) {
            oiFitsFile.getOiVis()[0].getNumericalColumnsNames(columns);
        }
        if (oiFitsFile.hasOiT3()) {
            oiFitsFile.getOiT3()[0].getNumericalColumnsNames(columns);
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

        plotDefLabel = new javax.swing.JLabel();
        plotDefinitionComboBox = new javax.swing.JComboBox();
        colorMappingLabel = new javax.swing.JLabel();
        colorMappingComboBox = new javax.swing.JComboBox();
        plotDefinitionName = new javax.swing.JLabel();
        flaggedDataCheckBox = new javax.swing.JCheckBox();
        detailledToggleButton = new javax.swing.JToggleButton();
        drawLinesCheckBox = new javax.swing.JCheckBox();
        extendedPanel = new javax.swing.JPanel();
        yLabel = new javax.swing.JLabel();
        xLabel = new javax.swing.JLabel();
        addYAxisButton = new javax.swing.JButton();
        delYAxisButton = new javax.swing.JButton();
        xAxisPanel = new javax.swing.JPanel();
        yAxesPanel = new javax.swing.JPanel();
        refreshButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        plotDefLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        plotDefLabel.setText("Show");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        add(plotDefLabel, gridBagConstraints);

        plotDefinitionComboBox.setPrototypeDisplayValue("01234567890123456789");
        plotDefinitionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plotDefinitionComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.2;
        add(plotDefinitionComboBox, gridBagConstraints);

        colorMappingLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        colorMappingLabel.setText("Color by");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        add(colorMappingLabel, gridBagConstraints);

        colorMappingComboBox.setPrototypeDisplayValue("0123456789");
        colorMappingComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorMappingComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.2;
        add(colorMappingComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        add(plotDefinitionName, gridBagConstraints);

        flaggedDataCheckBox.setText("Skip flagged data");
        flaggedDataCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                flaggedDataCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weightx = 0.1;
        add(flaggedDataCheckBox, gridBagConstraints);

        detailledToggleButton.setText("...");
        detailledToggleButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        detailledToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                detailledToggleButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        add(detailledToggleButton, gridBagConstraints);

        drawLinesCheckBox.setText("Draw lines");
        drawLinesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                drawLinesCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weightx = 0.1;
        add(drawLinesCheckBox, gridBagConstraints);

        extendedPanel.setLayout(new java.awt.GridBagLayout());

        yLabel.setText("y Axes");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        extendedPanel.add(yLabel, gridBagConstraints);

        xLabel.setText("x Axis");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        extendedPanel.add(xLabel, gridBagConstraints);

        addYAxisButton.setText("+");
        addYAxisButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        addYAxisButton.setPreferredSize(new java.awt.Dimension(21, 21));
        addYAxisButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addYAxisButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        extendedPanel.add(addYAxisButton, gridBagConstraints);

        delYAxisButton.setText("-");
        delYAxisButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        delYAxisButton.setPreferredSize(new java.awt.Dimension(21, 21));
        delYAxisButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delYAxisButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        extendedPanel.add(delYAxisButton, gridBagConstraints);

        xAxisPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        extendedPanel.add(xAxisPanel, gridBagConstraints);

        yAxesPanel.setLayout(new javax.swing.BoxLayout(yAxesPanel, javax.swing.BoxLayout.Y_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        extendedPanel.add(yAxesPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 0, 0);
        add(extendedPanel, gridBagConstraints);

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/fr/jmmc/jmcs/resource/image/refresh.png"))); // NOI18N
        refreshButton.setToolTipText("refresh zoom / remove plot selection");
        refreshButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        add(refreshButton, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void addYAxisButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addYAxisButtonActionPerformed
        Axis axis = new Axis();

        // Add to PlotDefinition
        addYEditor(axis);
        updateModel(true);

        checkYAxisActionButtons();
    }//GEN-LAST:event_addYAxisButtonActionPerformed

    private void delYAxisButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delYAxisButtonActionPerformed
        final int size = yAxes.size();
        if (size > 1) {
            // TODO replace by removal of the last yCombobox which one has lost the focus
            Axis[] yAxisArray = yAxes.keySet().toArray(new Axis[size]);
            Axis yAxis = yAxisArray[size - 1];
            delYEditor(yAxis);

            // Delete from PlotDefinition
            updateModel(true);
        }
        checkYAxisActionButtons();
    }//GEN-LAST:event_delYAxisButtonActionPerformed

    private void colorMappingComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorMappingComboBoxActionPerformed
        updateModel();
    }//GEN-LAST:event_colorMappingComboBoxActionPerformed

    private void detailledToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_detailledToggleButtonActionPerformed
        extendedPanel.setVisible(detailledToggleButton.isSelected());
        drawLinesCheckBox.setVisible(detailledToggleButton.isSelected());
        revalidate();
    }//GEN-LAST:event_detailledToggleButtonActionPerformed

    private void flaggedDataCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_flaggedDataCheckBoxActionPerformed
        updateModel();
    }//GEN-LAST:event_flaggedDataCheckBoxActionPerformed

    private void drawLinesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_drawLinesCheckBoxActionPerformed
        updateModel();
    }//GEN-LAST:event_drawLinesCheckBoxActionPerformed

    private void plotDefinitionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plotDefinitionComboBoxActionPerformed
        // this method should apply preset on current plotDef

        // TODO find better solution to allow the selection of a preset with keyboard shortcut
        // until now the choice is limited to the first item
        final int idx = plotDefinitionComboBox.getSelectedIndex();
        if (idx == 0) {
            return;
        }

        String presetPlotDefId = null;

        Collection<PlotDefinition> presets = PlotDefinitionFactory.getInstance().getDefaults();

        int i = 1; // first element is not a preset
        for (PlotDefinition plotDefinition : presets) {
            if (i == plotDefinitionComboBox.getSelectedIndex()) {
                presetPlotDefId = plotDefinition.getId();
                break;
            }
            i++;
        }

        if (presetPlotDefId == null) {
            logger.debug("[{}] plotDefinitionComboBoxActionPerformed() event ignored : no current selection", plotId);
            return;
        }

        final PlotDefinition plotDefCopy = getPlotDefinition();

        final ColorMapping colorMapping = plotDefCopy.getColorMapping();

        // TODO: decide: should only copy axis infos or all ?
        // copy values from preset:
        plotDefCopy.copyValues(PlotDefinitionFactory.getInstance().getDefault(presetPlotDefId));

        // TODO: clear name and description fields ?

        // keep color mapping:
        plotDefCopy.setColorMapping(colorMapping);

        refreshForm(plotDefCopy, null);
        updateModel();
    }//GEN-LAST:event_plotDefinitionComboBoxActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        final PlotDefinition plotDefCopy = getPlotDefinition();

        plotDefCopy.incVersion();

        ocm.updatePlotDefinition(this, plotDefCopy);
    }//GEN-LAST:event_refreshButtonActionPerformed

    /**
     * Return colorMapping Value stored by associated combobox.
     * @return the colorMapping Value stored by associated combobox.
     */
    private ColorMapping getColorMapping() {
        return (ColorMapping) colorMappingComboBox.getSelectedItem();
    }

    /** 
     * Create a new widget to edit given Axis.
     * @param yAxis axis to be edited by new yAxisEditor
     */
    private void addYEditor(final Axis yAxis) {

        // Link new Editor and Axis
        final AxisEditor yAxisEditor = new AxisEditor(this);
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
     * Update current plotDefinition 
     * and request a plotDefinitionUpdate to the OIFitsCollectionManager.    
     */
    void updateModel() {
        updateModel(false);
    }

    /** 
     * Update current plotDefinition 
     * and request a plotDefinitionUpdate to the OIFitsCollectionManager.
     * 
     * @param forceRefreshPlotDefNames true to refresh plotDefinition names
     */
    void updateModel(final boolean forceRefreshPlotDefNames) {
        if (notify) {
            // get copy:
            final PlotDefinition plotDefCopy = getPlotDefinition();

            if (plotDefCopy != null) {
                // handle xAxis
                plotDefCopy.setXAxis(xAxisEditor.getAxis());
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

            if (forceRefreshPlotDefNames) {
                refreshPlotDefinitionNames(plotDefCopy);
            }

        } else {
            logger.debug("updateModel: disabled");
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
    private javax.swing.JLabel plotDefLabel;
    private javax.swing.JComboBox plotDefinitionComboBox;
    private javax.swing.JLabel plotDefinitionName;
    private javax.swing.JButton refreshButton;
    private javax.swing.JPanel xAxisPanel;
    private javax.swing.JLabel xLabel;
    private javax.swing.JPanel yAxesPanel;
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
            resetForm();
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
            resetForm();

        } else if (!ObjectUtils.areEquals(prevPlotDefId, plotDefId)) {
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
                _setPlotDefId(event.getPlotDefinition().getId());

                refreshForm(event.getPlotDefinition(), null);
                break;
            case PLOT_CHANGED:
                final PlotDefinition plotDef = event.getPlot().getPlotDefinition();

                // define id of associated plotDefinition
                _setPlotDefId(plotDef.getId());

                refreshForm(plotDef, event.getPlot().getSubsetDefinition().getOIFitsSubset());
                break;
            default:
                logger.debug("onProcess {} - done", event);
        }
    }
}
