/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.gui;

import fr.jmmc.jmcs.gui.component.GenericListModel;
import fr.jmmc.jmcs.util.StringUtils;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.fits.FitsTable;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * This class displays and edit table params using swing widgets.
 * To be improved with all keywords types JCheckBox for boolean, combobox if meta give accepted values...
 * @author mellag
 */
public final class TableKeywordsEditor extends javax.swing.JPanel implements ActionListener, PropertyChangeListener {

    private static final long serialVersionUID = 1L;

    private final static Insets insets = new Insets(2, 6, 2, 6);

    // members
    private SoftwareSettingsPanel notifiedParent = null;
    private FitsTable fitsTable = null;

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

        if (fitsTable == null) {
            return;
        }

        int gridy = 0;
        setLayout(new GridBagLayout());

        GridBagConstraints gridBagConstraints;

        for (String name : keywordNames) {
            final KeywordMeta meta = fitsTable.getKeywordsDesc(name);
            final Object value = fitsTable.getKeywordValue(name);

            final JLabel jLabel = new JLabel(name);
            jLabel.setToolTipText(meta.getDescription());

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
                    component = createFormattedTextField(SoftwareSettingsPanel.getDecimalFormatterFactory(), value);
                    break;
                case TYPE_INT:
                    component = createFormattedTextField(SoftwareSettingsPanel.getIntegerFormatterFactory(), value);
                    break;
                case TYPE_LOGICAL:
                    JCheckBox checkbox = new JCheckBox();
                    checkbox.setSelected(Boolean.TRUE.equals(value));
                    component = checkbox;
                    break;
                default:
                    component = new JTextField(meta.getDataType() + " UNSUPPORTED");
                    supportedKeyword = false;
            }
            component.setToolTipText(meta.getDescription());

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
            gridBagConstraints.insets = insets;
            gridBagConstraints.anchor = GridBagConstraints.LINE_END;
            add(jLabel, gridBagConstraints);

            gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = gridy;
            gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridwidth = 5;
            gridBagConstraints.weightx = 0.8;
            gridBagConstraints.weighty = 0.1;
            gridBagConstraints.insets = insets;
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
            fitsTable.updateKeyword(name, value);
            notifiedParent.updateModel(true);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
