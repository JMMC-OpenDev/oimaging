/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui;

import fr.jmmc.jmal.AbsorptionLineRange;
import fr.jmmc.jmcs.gui.component.Disposable;
import fr.jmmc.jmcs.gui.component.GenericListModel;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.service.RecentValuesManager;
import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.oiexplorer.core.function.ConverterFactory;
import fr.jmmc.oiexplorer.core.model.plot.Axis;
import fr.jmmc.oiexplorer.core.model.plot.AxisRangeMode;
import fr.jmmc.oiexplorer.core.model.plot.Range;
import fr.jmmc.oitools.OIFitsConstants;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JPopupMenu;
import javax.swing.text.NumberFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Axis editor widget.
 * 
 * @author mella
 */
public class AxisEditor extends javax.swing.JPanel implements Disposable {

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
    /** Flag indicating a user input */
    private boolean user_input = true;
    // recent values keys for min/max:
    private String keyMin = null;
    private String keyMax = null;
    // listener references (alive until this editor is dead or axis changes):
    private ActionListener popupListenerMin = null;
    private ActionListener popupListenerMax = null;
    // popup menus in action (alive until this editor is dead or axis changes):
    private JPopupMenu popupMenuMin = null;
    private JPopupMenu popupMenuMax = null;

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
        
        // Adjust fonts:
        final Font fixedFont = new Font(Font.MONOSPACED, Font.PLAIN, SwingUtils.adjustUISize(12));
        this.nameComboBox.setFont(fixedFont);
    }

    /** 
     * Creates new form AxisEditor.
     * This empty constructor leave here for Netbeans GUI builder
     */
    public AxisEditor() {
        this(null);
    }

    /**
     * Free any ressource or reference to this instance :
     */
    @Override
    public void dispose() {
        if (logger.isDebugEnabled()) {
            logger.debug("AxisEditor[{}]: dispose", axisToEdit.getName());
        }
        reset();
    }

    public void reset() {
        setAxis(null, null);
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
            // reset state
            keyMin = keyMax = null;
            // dispose popup menus:
            jFieldMin.setComponentPopupMenu(null);
            jFieldMax.setComponentPopupMenu(null);
            popupListenerMin = popupListenerMax = null;
            popupMenuMin = popupMenuMax = null;
            return;
        }
        try {
            notify = user_input = false;
            final String axisName = axis.getName();

            nameComboBoxModel.add(axisChoices);
            nameComboBox.setSelectedItem(axisName);

            includeZeroCheckBox.setSelected(axis.isIncludeZero());
            logScaleCheckBox.setSelected(axis.isLogScale());

            // Add popup menus to min/max fields:
            keyMin = axisName + ".min";
            keyMax = axisName + ".max";

            // create new listeners to release previous listeners / popup menus:
            popupListenerMin = new FieldSetter(jFieldMin);
            popupListenerMax = new FieldSetter(jFieldMax);

            popupMenuMin = RecentValuesManager.getMenu(keyMin, popupListenerMin);
            popupMenuMax = RecentValuesManager.getMenu(keyMax, popupListenerMax);

            // enable or disable popup menus:
            updateRangeEditor(axis.getRange(), axis.getRangeModeOrDefault(), true);
            updateRangeList();

        } finally {
            notify = user_input = true;
        }
    }

    public boolean setAxisRange(final double min, final double max) {
        logger.debug("setAxisRange: [{} - {}]", min, max);

        boolean changed = false;
        try {
            notify = user_input = false;

            changed |= setFieldValue(jFieldMin, min);
            changed |= setFieldValue(jFieldMax, max);
        } finally {
            notify = user_input = true;
        }
        return changed;
    }

    private boolean setFieldValue(final JFormattedTextField field, final double value) {
        final Object newValue = Double.valueOf(value);
        final Object prev = field.getValue();
        if (ObjectUtils.areEquals(prev, newValue)) {
            return false;
        }
        field.setValue(newValue);
        return true;
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
            try {
                user_input = false;

                jFieldMin.setValue(isFinite(min) ? Double.valueOf(min) : null);
                jFieldMax.setValue(isFinite(max) ? Double.valueOf(max) : null);
                jRadioModeFixed.doClick();
            } finally {
                user_input = true;
            }
        }
    }

    private boolean isWavelengthAxis() {
        return (OIFitsConstants.COLUMN_EFF_WAVE.equalsIgnoreCase((String) nameComboBox.getSelectedItem()));
    }

    public void updateRangeMode(final AxisRangeMode mode) {
        axisToEdit.setRangeMode(mode);

        if (mode == AxisRangeMode.RANGE) {
            // hack to initialize range from plot values:
            final Range r = getFieldRange();
            axisToEdit.setRange(r);
        }
        updateRangeEditor(axisToEdit.getRange(), axisToEdit.getRangeMode());
    }

    private void updateRangeEditor(final Range range, final AxisRangeMode mode) {
        updateRangeEditor(range, mode, false);
    }

    private void updateRangeEditor(final Range range, final AxisRangeMode mode, final boolean setRange) {
        if (setRange) {
            if (range == null) {
                jFieldMin.setValue(null);
                jFieldMax.setValue(null);
            } else {
                jFieldMin.setValue(isFinite(range.getMin()) ? Double.valueOf(range.getMin()) : null);
                jFieldMax.setValue(isFinite(range.getMax()) ? Double.valueOf(range.getMax()) : null);
            }
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

        // enable or disable popup menus:
        enablePopupMenu(jFieldMin, popupMenuMin);
        enablePopupMenu(jFieldMax, popupMenuMax);
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

        Range range = null;

        if ((minFinite != maxFinite)
                || (minFinite && maxFinite && (min < max))) {

            range = new Range();
            range.setMin(min);
            range.setMax(max);
        }

        // Do not store values set programmatically:
        if (user_input && jRadioModeFixed.isSelected()) {
            // Update recent values:
            if (keyMin != null) {
                RecentValuesManager.addValue(keyMin, (minFinite) ? Double.toString(min) : null);
            }
            if (keyMax != null) {
                RecentValuesManager.addValue(keyMax, (maxFinite) ? Double.toString(max) : null);
            }
        }
        return range;
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
        if (axisToEdit == null) {
            // disposed:
            return;
        }
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
            updateRangeMode(AxisRangeMode.AUTO);
        } else if (evt.getSource() == jRadioModeDefault) {
            updateRangeMode(AxisRangeMode.DEFAULT);
        } else if (evt.getSource() == jRadioModeFixed) {
            updateRangeMode(AxisRangeMode.RANGE);
        } else if (evt.getSource() == jFieldMin) {
            // only update edited axis when the mode is RANGE ie Enabled:
            if (jFieldMin.isEnabled()) {
                final Range r = getFieldRange();
                axisToEdit.setRange(r);
                if (r == null) {
                    jFieldMin.requestFocus();
                }
            }
        } else if (evt.getSource() == jFieldMax) {
            // only update edited axis when the mode is RANGE ie Enabled:
            if (jFieldMax.isEnabled()) {
                final Range r = getFieldRange();
                axisToEdit.setRange(r);
                if (r == null) {
                    jFieldMax.requestFocus();
                }
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

    private static final class FieldSetter implements ActionListener {

        private final JFormattedTextField textField;

        FieldSetter(final JFormattedTextField textField) {
            this.textField = textField;
        }

        @Override
        public void actionPerformed(final ActionEvent ae) {
            final String value = ae.getActionCommand();
            textField.setValue((value != null) ? Double.valueOf(value) : null);
        }
    }

    private static void enablePopupMenu(final JComponent component, final JPopupMenu popupMenu) {
        JPopupMenu enabledPopupMenu = null;
        if (popupMenu != null) {
            if (component.isEnabled() && popupMenu.getComponentCount() != 0) {
                enabledPopupMenu = popupMenu;
            }
        }
        component.setComponentPopupMenu(enabledPopupMenu);
    }
}
