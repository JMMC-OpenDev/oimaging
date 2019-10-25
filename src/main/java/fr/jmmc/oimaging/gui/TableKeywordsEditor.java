/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.gui;

import fr.jmmc.jmcs.gui.component.GenericListModel;
import fr.jmmc.jmcs.util.SpecialChars;
import fr.jmmc.jmcs.util.StringUtils;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.image.FitsUnit;
import fr.jmmc.oitools.meta.Units;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class displays and edit table params using swing widgets.
 */
public final class TableKeywordsEditor extends javax.swing.JPanel implements ActionListener, PropertyChangeListener {

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(TableKeywordsEditor.class.getName());

    private static final long serialVersionUID = 1L;

    private final static Insets INSETS = new Insets(2, 6, 2, 6);

    // members
    private SoftwareSettingsPanel notifiedParent = null;
    private FitsTable fitsTable = null;
    /** editor conversions */
    private final HashMap<String, FitsUnit> unitKeywords = new HashMap<String, FitsUnit>();
    private final HashMap<String, FitsUnit> unitFields = new HashMap<String, FitsUnit>();

    /** Creates new form TableEditor */
    public TableKeywordsEditor() {
        initComponents();
    }

    public SoftwareSettingsPanel getNotifiedParent() {
        return notifiedParent;
    }

    public void setNotifiedParent(final SoftwareSettingsPanel notifiedParent) {
        this.notifiedParent = notifiedParent;
    }

    void setModel(final FitsTable fitsTable) {
        setModel(fitsTable, null);
    }

    void setModel(final FitsTable fitsTable, final Set<String> keywordNames) {
        this.fitsTable = fitsTable;

        removeAll();

        // reset converters:
        this.unitKeywords.clear();
        this.unitFields.clear();

        if (fitsTable == null) {
            return;
        }

        int gridy = 0;
        setLayout(new GridBagLayout());

        GridBagConstraints gridBagConstraints;

        for (final String name : keywordNames) {
            final KeywordMeta meta = fitsTable.getKeywordsDesc(name);
            final Object value = fitsTable.getKeywordValue(name);

            final JComponent component;
            boolean supportedKeyword = true;

            switch (meta.getDataType()) {
                case TYPE_CHAR:
                    if (meta.getStringAcceptedValues() == null) {
                        component = new JTextField((value == null) ? "" : value.toString());
                    } else {
                        JComboBox comboBox = new JComboBox(new GenericListModel(Arrays.asList(meta.getStringAcceptedValues()), true));
                        if (value != null) {
                            comboBox.setSelectedItem(value);
                        }
                        component = comboBox;
                    }
                    break;
                case TYPE_DBL:
                    component = createFormattedTextField(SoftwareSettingsPanel.getDecimalFormatterFactory(),
                            convertValueToField(name, meta.getUnits(), value));
                    break;
                case TYPE_INT:
                    component = createFormattedTextField(SoftwareSettingsPanel.getIntegerFormatterFactory(), value);
                    break;
                case TYPE_LOGICAL:
                    final JCheckBox checkbox = new JCheckBox();
                    checkbox.setSelected(Boolean.TRUE.equals(value));
                    component = checkbox;
                    break;
                default:
                    component = new JTextField(meta.getDataType() + " UNSUPPORTED");
                    supportedKeyword = false;
            }

            // show description in tooltips:
            String description = meta.getDescription();

            FitsUnit unit = this.unitFields.get(name);
            if (unit != null) {
                description = "<html>" + description + "<br/><b>Editor unit is '" + unit.getRepresentation() + "'</b></html>";
            } else {
                unit = this.unitKeywords.get(name);
            }

            // define label and optionally the unit:
            String label = name;
            if (unit != null) {
                label += " [" + ((unit == FitsUnit.WAVELENGTH_MICRO_METER)
                        ? SpecialChars.UNIT_MICRO_METER : unit.getStandardRepresentation()) + ']';
            }
            final JLabel jLabel = new JLabel(label);
            jLabel.setToolTipText(description);
            component.setToolTipText(description);

            if (supportedKeyword) {
                // store name to retieve back on edit
                component.setName(name);
                component.addPropertyChangeListener("value", this);

                if (component instanceof JTextField) {
                    ((JTextField) component).addActionListener(this);
                } else if (component instanceof JComboBox) {
                    ((JComboBox) component).addActionListener(this);
                } else if (component instanceof JCheckBox) {
                    ((JCheckBox) component).addActionListener(this);
                }
            } else {
                ((JTextField) component).setEditable(false);
            }

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = gridy;
            gridBagConstraints.weightx = 0.1;
            gridBagConstraints.weighty = 0.1;
            gridBagConstraints.insets = INSETS;
            gridBagConstraints.anchor = GridBagConstraints.LINE_END;
            add(jLabel, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = gridy;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridwidth = 5;
            gridBagConstraints.weightx = 0.8;
            gridBagConstraints.weighty = 0.1;
            gridBagConstraints.insets = INSETS;
            gridBagConstraints.anchor = GridBagConstraints.CENTER;

            add(component, gridBagConstraints);

            gridy++;
        }
    }

    private static JFormattedTextField createFormattedTextField(final JFormattedTextField.AbstractFormatterFactory formatterFactory, final Object value) {
        final JFormattedTextField jFormattedTextField = new JFormattedTextField(value);
        jFormattedTextField.setFormatterFactory(formatterFactory);
        return jFormattedTextField;
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

        setBorder(javax.swing.BorderFactory.createTitledBorder("Specific params"));
        setLayout(new java.awt.GridBagLayout());
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void actionPerformed(final ActionEvent ae) {
        final JComponent component = (JComponent) ae.getSource();
        if (component != null) {
            final String name = component.getName();
            String value = null;
            if (component instanceof JTextField) {
                value = ((JTextField) component).getText();
            } else if (component instanceof JCheckBox) {
                value = Boolean.toString(((JCheckBox) component).isSelected());
            } else if (component instanceof JComboBox) {
                value = (String) ((JComboBox) component).getSelectedItem();
            }
            update(name, value);
        }
    }

    @Override
    public void propertyChange(final PropertyChangeEvent pce) {
        final JComponent component = (JComponent) pce.getSource();
        if (component != null) {
            final String name = component.getName();
            if (pce.getNewValue() != null) {
                update(name, pce.getNewValue().toString());
            }
        }
    }

    private void update(final String name, final String value) {
        // Store content as a string even for every types
        if (name != null && value != null) {
            // handle unit conversion:
            final FitsUnit unitField = this.unitFields.get(name);

            if (unitField != null) {
                fitsTable.setKeywordValue(name,
                        convertFieldToValue(name, unitField, value));
            } else {
                fitsTable.updateKeyword(name, value);
            }
            notifiedParent.updateModel(true);
        }
    }

    private Object convertValueToField(final String name, final Units unit, final Object value) {
        Object output = value;

        if (unit != Units.NO_UNIT) {
            try {
                // parse unit:
                final FitsUnit unitKeyword = FitsUnit.parseUnit(unit.getStandardRepresentation());

                if (unitKeyword != FitsUnit.NO_UNIT) {
                    this.unitKeywords.put(name, unitKeyword);

                    final FitsUnit unitField;
                    switch (unitKeyword) {
                        case WAVELENGTH_METER:
                            // implictely suppose wavelength argument:
                            unitField = FitsUnit.WAVELENGTH_MICRO_METER;
                            break;
                        case ANGLE_DEG:
                            unitField = FitsUnit.ANGLE_MILLI_ARCSEC;
                            break;
                        default:
                            // no conversion
                            unitField = null;
                    }

                    if (unitField != null) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Keyword[{}] unitKeyword: {}, unitField: {}", name, unitKeyword, unitField);
                        }
                        this.unitFields.put(name, unitField);

                        // perform conversion:
                        if (value instanceof Double) {
                            final double val = (Double) value;
                            final double converted = unitKeyword.convert(val, unitField);

                            if (logger.isDebugEnabled()) {
                                logger.debug("Keyword[{}] val: {}, converted: {}", name, val, converted);
                            }
                            output = Double.valueOf(converted);
                        }
                    }
                }
            } catch (IllegalArgumentException iae) {
                logger.info("convertValueToField: failure:", iae);
            }
        }
        return output;
    }

    private Double convertFieldToValue(final String name, final FitsUnit unitField, final String value) {
        Double output = null;

        final FitsUnit unitKeyword = this.unitKeywords.get(name);

        if (unitKeyword != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Keyword[{}] unitKeyword: {}, unitField: {}", name, unitKeyword, unitField);
            }

            if (!StringUtils.isEmpty(value)) {
                // perform conversion:
                final double val = Double.valueOf(value);
                final double converted = unitField.convert(val, unitKeyword);

                if (logger.isDebugEnabled()) {
                    logger.debug("Keyword[{}] val: {}, converted: {}", name, val, converted);
                }
                output = Double.valueOf(converted);
            }
        }
        return output;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
