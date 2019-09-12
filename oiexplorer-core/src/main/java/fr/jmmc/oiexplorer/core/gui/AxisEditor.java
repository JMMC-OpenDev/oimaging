/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui;

import fr.jmmc.jmal.AbsorptionLineRange;
import fr.jmmc.jmcs.gui.component.GenericListModel;
import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.oiexplorer.core.function.ConverterFactory;
import fr.jmmc.oiexplorer.core.model.plot.Axis;
import fr.jmmc.oiexplorer.core.model.plot.AxisRangeMode;
import fr.jmmc.oiexplorer.core.model.plot.Range;
import fr.jmmc.oitools.OIFitsConstants;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.text.NumberFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Axis editor widget.
 * 
 * @author mella
 */
public class AxisEditor extends javax.swing.JPanel {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1L;
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(AxisEditor.class.getName());
    /** undefined value for range list */
    private static final String RANGE_NONE = "[None]";

    /* members */
    /** PlotDefinitionEditor to notify in case of modification */
    private final PlotDefinitionEditor parentToNotify;
    /** Edited axis reference */
    private Axis axisToEdit;
    /** List of available axis names */
    private final GenericListModel<String> nameComboBoxModel;
    /** List of possible ranges for the axis */
    private final GenericListModel<String> rangeComboBoxModel;
    /** Flag notification of associated plotDefinitionEditor */
    private boolean notify = true;

    /** 
     * Creates the new AxisEditor form.
     * Use setAxis() to change model to edit.
     * @param parent PlotDefinitionEditor to be notified of changes.
     */
    public AxisEditor(final PlotDefinitionEditor parent) {
        initComponents();
        parentToNotify = parent;
        nameComboBoxModel = new GenericListModel<String>(new ArrayList<String>(25), true);
        nameComboBox.setModel(nameComboBoxModel);

        rangeComboBoxModel = new GenericListModel<String>(new ArrayList<String>(10), true);
        rangeListComboBox.setModel(rangeComboBoxModel);

        // hidden until request and valid code to get a correct behaviour
        final JComponent[] components = new JComponent[]{
            includeZeroCheckBox, jRadioModeAuto, jRadioModeDefault, jRadioModeFixed, jFieldMin, jFieldMax
        };
        for (JComponent c : components) {
            c.setVisible(c.isEnabled());
        }

        jFieldMin.addPropertyChangeListener("value", new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                if (!ObjectUtils.areEquals(evt.getOldValue(), evt.getNewValue())) {
                    actionPerformed(new ActionEvent(jFieldMin, 0, ""));
                }
            }
        });

        jFieldMax.addPropertyChangeListener("value", new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                if (!ObjectUtils.areEquals(evt.getOldValue(), evt.getNewValue())) {
                    actionPerformed(new ActionEvent(jFieldMax, 0, ""));
                }
            }
        });
    }

    /** 
     * Creates new form AxisEditor.
     * This empty constructor leave here for Netbeans GUI builder
     */
    public AxisEditor() {
        this(null);
    }

    /** 
     * Initialize widgets according to given axis 
     * 
     * @param axis used to initialize widget states
     * @param axisChoices column names to display
     */
    public void setAxis(final Axis axis, final List<String> axisChoices) {
        axisToEdit = axis;
        nameComboBoxModel.clear();
        if (axis == null) {
            // TODO push in a reset state
            return;
        }
        try {
            notify = false;
            nameComboBoxModel.add(axisChoices);
            nameComboBox.setSelectedItem(axis.getName());

            includeZeroCheckBox.setSelected(axis.isIncludeZero());
            logScaleCheckBox.setSelected(axis.isLogScale());

            updateRangeEditor(axis.getRange(), axis.getRangeModeOrDefault());

            updateRangeList();
        } finally {
            notify = true;
        }
    }

    private void updateRangeList() {
        final boolean isWavelengthAxis = isWavelengthAxis();

        // TODO: use a factory to get predefined ranges per column name ?
        if (isWavelengthAxis) {
            rangeComboBoxModel.clear();
            rangeComboBoxModel.add(RANGE_NONE); // empty
            for (AbsorptionLineRange r : AbsorptionLineRange.values()) {
                rangeComboBoxModel.add(r.getName());
            }
        }
        rangeListComboBox.setVisible(isWavelengthAxis);
    }

    private void handleRangeListSelection() {
        final String selected = (String) rangeListComboBox.getSelectedItem();

        if (isWavelengthAxis()) {
            double min = Double.NaN;
            double max = Double.NaN;

            if (!RANGE_NONE.equalsIgnoreCase(selected)) {
                for (AbsorptionLineRange r : AbsorptionLineRange.values()) {
                    if (r.getName().equals(selected)) {
                        logger.debug("AbsorptionLineRange: {}", r);
                        min = r.getMin();
                        max = r.getMax();
                        break;
                    }
                }
            }
            jRadioModeFixed.doClick();
            jFieldMin.setValue(isFinite(min) ? min : null);
            jFieldMax.setValue(isFinite(max) ? max : null);
        }
    }

    private boolean isWavelengthAxis() {
        return (OIFitsConstants.COLUMN_EFF_WAVE.equalsIgnoreCase((String) nameComboBox.getSelectedItem()));
    }

    private void updateRangeEditor(final Range range, final AxisRangeMode mode) {
        if (range == null) {
            jFieldMin.setValue(null);
            jFieldMax.setValue(null);
        } else {
            jFieldMin.setValue(isFinite(range.getMin()) ? range.getMin() : null);
            jFieldMax.setValue(isFinite(range.getMax()) ? range.getMax() : null);
        }
        switch (mode) {
            case AUTO:
                jRadioModeAuto.setSelected(true);
                break;
            default:
            case DEFAULT:
                jRadioModeDefault.setSelected(true);
                break;
            case RANGE:
                jRadioModeFixed.setSelected(true);
                break;
        }
        final boolean enable = (mode == AxisRangeMode.RANGE);
        jFieldMin.setEnabled(enable);
        jFieldMax.setEnabled(enable);
    }

    private Range getFieldRange() {
        double min = Double.NaN;
        double max = Double.NaN;

        Object value = this.jFieldMin.getValue();

        if (value instanceof Double) {
            min = ((Double) value).doubleValue();
        }

        value = this.jFieldMax.getValue();
        if (value instanceof Double) {
            max = ((Double) value).doubleValue();
        }

        final boolean minFinite = isFinite(min);
        final boolean maxFinite = isFinite(max);

        if ((minFinite != maxFinite)
                || (minFinite && maxFinite && (min < max))) {

            final Range range = new Range();
            range.setMin(min);
            range.setMax(max);

            return range;
        }
        return null;
    }

    /** 
     * Return the edited Axis.
     * @return the edited Axis.
     */
    public Axis getAxis() {
        return axisToEdit;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroupRangeModes = new javax.swing.ButtonGroup();
        nameComboBox = new javax.swing.JComboBox();
        logScaleCheckBox = new javax.swing.JCheckBox();
        includeZeroCheckBox = new javax.swing.JCheckBox();
        jPanelBounds = new javax.swing.JPanel();
        jRadioModeAuto = new javax.swing.JRadioButton();
        jRadioModeDefault = new javax.swing.JRadioButton();
        jRadioModeFixed = new javax.swing.JRadioButton();
        jFieldMin = new JFormattedTextField(getNumberFieldFormatter());
        jFieldMax = new JFormattedTextField(getNumberFieldFormatter());
        rangeListComboBox = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        nameComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AxisEditor.this.actionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        add(nameComboBox, gridBagConstraints);

        logScaleCheckBox.setText("log");
        logScaleCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AxisEditor.this.actionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        add(logScaleCheckBox, gridBagConstraints);

        includeZeroCheckBox.setText("inc. 0");
        includeZeroCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AxisEditor.this.actionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        add(includeZeroCheckBox, gridBagConstraints);

        jPanelBounds.setLayout(new java.awt.GridBagLayout());

        buttonGroupRangeModes.add(jRadioModeAuto);
        jRadioModeAuto.setText("auto");
        jRadioModeAuto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AxisEditor.this.actionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanelBounds.add(jRadioModeAuto, gridBagConstraints);

        buttonGroupRangeModes.add(jRadioModeDefault);
        jRadioModeDefault.setSelected(true);
        jRadioModeDefault.setText("default");
        jRadioModeDefault.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AxisEditor.this.actionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        jPanelBounds.add(jRadioModeDefault, gridBagConstraints);

        buttonGroupRangeModes.add(jRadioModeFixed);
        jRadioModeFixed.setText("fixed");
        jRadioModeFixed.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AxisEditor.this.actionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanelBounds.add(jRadioModeFixed, gridBagConstraints);

        jFieldMin.setColumns(10);
        jFieldMin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AxisEditor.this.actionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 2);
        jPanelBounds.add(jFieldMin, gridBagConstraints);

        jFieldMax.setColumns(10);
        jFieldMax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AxisEditor.this.actionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanelBounds.add(jFieldMax, gridBagConstraints);

        rangeListComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rangeListComboBoxActionPerformed(evt);
            }
        });
        jPanelBounds.add(rangeListComboBox, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 2, 0, 2);
        add(jPanelBounds, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void actionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionPerformed
        boolean forceRefreshPlotDefNames = false;

        if (evt.getSource() == includeZeroCheckBox) {
            axisToEdit.setIncludeZero(includeZeroCheckBox.isSelected());
        } else if (evt.getSource() == logScaleCheckBox) {
            axisToEdit.setLogScale(logScaleCheckBox.isSelected());
        } else if (evt.getSource() == nameComboBox) {
            final String columnName = (String) nameComboBox.getSelectedItem();
            axisToEdit.setName(columnName);

            // only modify axis if the user changes the axis, not by swing events due to model changes:
            if (notify) {
                // reset converter and log scale:
                axisToEdit.setConverter(ConverterFactory.getInstance().getDefaultByColumn(columnName));
                axisToEdit.setLogScale(logScaleCheckBox.isSelected());
                // force refresh plot definition names:
                forceRefreshPlotDefNames = true;

                updateRangeList();
            }
        } else if (evt.getSource() == jRadioModeAuto) {
            axisToEdit.setRangeMode(AxisRangeMode.AUTO);
            updateRangeEditor(axisToEdit.getRange(), axisToEdit.getRangeMode());
        } else if (evt.getSource() == jRadioModeDefault) {
            axisToEdit.setRangeMode(AxisRangeMode.DEFAULT);
            updateRangeEditor(axisToEdit.getRange(), axisToEdit.getRangeMode());
        } else if (evt.getSource() == jRadioModeFixed) {
            axisToEdit.setRangeMode(AxisRangeMode.RANGE);
            updateRangeEditor(axisToEdit.getRange(), axisToEdit.getRangeMode());
        } else if (evt.getSource() == jFieldMin) {
            final Range r = getFieldRange();
            axisToEdit.setRange(r);
            if (r == null) {
                jFieldMin.requestFocus();
            }
        } else if (evt.getSource() == jFieldMax) {
            final Range r = getFieldRange();
            axisToEdit.setRange(r);
            if (r == null) {
                jFieldMax.requestFocus();
            }
        } else if (evt.getSource() == rangeListComboBox) {
            handleRangeListSelection();
        } else {
            throw new IllegalStateException("TODO: handle event from " + evt.getSource());
        }

        if (notify) {
            parentToNotify.updateModel(forceRefreshPlotDefNames);
        }
    }//GEN-LAST:event_actionPerformed

    private void rangeListComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rangeListComboBoxActionPerformed
        AxisEditor.this.actionPerformed(evt);
    }//GEN-LAST:event_rangeListComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupRangeModes;
    private javax.swing.JCheckBox includeZeroCheckBox;
    private javax.swing.JFormattedTextField jFieldMax;
    private javax.swing.JFormattedTextField jFieldMin;
    private javax.swing.JPanel jPanelBounds;
    private javax.swing.JRadioButton jRadioModeAuto;
    private javax.swing.JRadioButton jRadioModeDefault;
    private javax.swing.JRadioButton jRadioModeFixed;
    private javax.swing.JCheckBox logScaleCheckBox;
    private javax.swing.JComboBox nameComboBox;
    private javax.swing.JComboBox rangeListComboBox;
    // End of variables declaration//GEN-END:variables

    /**
     * Return the custom double formatter that accepts null values
     * @return number formatter
     */
    private static NumberFormatter getNumberFieldFormatter() {
        final NumberFormatter nf = new NumberFormatter(new DecimalFormat("####.####")) {
            /** default serial UID for Serializable interface */
            private static final long serialVersionUID = 1;

            /**
             * Hack to allow empty string
             */
            @Override
            public Object stringToValue(final String text) throws ParseException {
                if (text == null || text.length() == 0) {
                    return null;
                }
                return super.stringToValue(text);
            }
        };
        nf.setValueClass(Double.class);
        nf.setCommitsOnValidEdit(false);
        return nf;
    }

    private static boolean isFinite(final double value) {
        return !(Double.isNaN(value) && !Double.isInfinite(value));
    }
}
