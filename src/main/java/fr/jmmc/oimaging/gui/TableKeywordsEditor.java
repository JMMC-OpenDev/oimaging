/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.gui;

import fr.jmmc.oitools.fits.FitsUtils;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.fits.FitsTable;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * This class displays and edit table params using swing widgets.
 * To be improved with all keywords types JCheckBox for boolean, combobox if meta give accepted values...
 * @author mellag
 */
// TODO move it in oitools module ?
public class TableKeywordsEditor extends javax.swing.JPanel implements ActionListener, PropertyChangeListener {

    FitsTable model = null;
    AlgorithmSettingsPanel notifiedParent = null;

    final static Insets insets = new java.awt.Insets(2, 2, 2, 2);

    /** Creates new form TableEditor */
    public TableKeywordsEditor() {
        initComponents();
    }

    public void setModel(final FitsTable model, AlgorithmSettingsPanel notifiedParent) {
        setModel(model, model == null ? null : model.getKeywordsDesc() == null ? null : model.getKeywordsDesc().keySet(), notifiedParent);

    }

    void setModel(FitsTable model, Set<String> keywords, AlgorithmSettingsPanel notifiedParent) {

        this.model = model;
        this.notifiedParent = notifiedParent;

        removeAll();

        if (model == null) {
            return;
        }

        int gridy = 0;
        setLayout(new java.awt.GridBagLayout());
        java.awt.GridBagConstraints gridBagConstraints;

        for (String key : keywords) {
            if (!FitsUtils.isStandardKeyword(key)) {

                final KeywordMeta desc = model.getKeywordsDesc().get(key);
                final JLabel jLabel = new JLabel(key);
                jLabel.setToolTipText(desc.getDescription());

                JTextField jTextField;
                JFormattedTextField jFormattedTextField;
                Object value = model.getKeywordValue(key);
                boolean supportedKeyword = true;
                switch (model.getKeywordsDesc().get(key).getDataType()) {
                    case TYPE_CHAR:
                        jTextField = new JTextField(value == null ? "" : value.toString());

                        break;
                    case TYPE_DBL:
                    case TYPE_REAL:
                        jFormattedTextField = new JFormattedTextField(value);
                        jFormattedTextField.setFormatterFactory(AlgorithmSettingsPanel.getDecimalFormatterFactory());
                        jTextField = jFormattedTextField;
                        break;
                    case TYPE_SHORT:
                        jFormattedTextField = new JFormattedTextField(value);
                        jFormattedTextField.setFormatterFactory(AlgorithmSettingsPanel.getIntegerFormatterFactory());
                        jTextField = jFormattedTextField;
                        break;
                    default:
                        jTextField = new JTextField(model.getKeywordsDesc().get(key).getDataType() + " UNSUPPORTED");
                        supportedKeyword = false;
                }

                jTextField.setToolTipText(desc.getDescription());
                if (supportedKeyword) {
                    // store name to retieve back on edit
                    jTextField.setName(key);
                    jTextField.addActionListener(this);
                    jTextField.addPropertyChangeListener(this);
                } else {

                    jTextField.setEditable(false);
                }

                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = gridy;
                gridBagConstraints.weightx = 0.1;
                gridBagConstraints.weighty = 0.1;
                gridBagConstraints.insets = insets;
                gridBagConstraints.anchor = GridBagConstraints.LINE_END;
                add(jLabel, gridBagConstraints);

                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 2;
                gridBagConstraints.gridy = gridy;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.gridwidth = 5;
                gridBagConstraints.weightx = 0.8;
                gridBagConstraints.weighty = 0.1;
                gridBagConstraints.insets = insets;
                gridBagConstraints.anchor = GridBagConstraints.CENTER;

                add(jTextField, gridBagConstraints);

                gridy++;
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
        java.awt.GridBagConstraints gridBagConstraints;

        setBorder(javax.swing.BorderFactory.createTitledBorder("Specific params"));
        setLayout(new java.awt.GridBagLayout());
    }// </editor-fold>//GEN-END:initComponents

    @Override
    public void actionPerformed(ActionEvent evt) {
        final JTextField textField = (JTextField) evt.getSource();
        update(textField.getName(), textField.getText());
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Ignore most property change except value change
        // TODO check that this filtering follow standard conventions
        if (evt.getPropertyName().equals("value")) {
            final JTextField textField = (JTextField) evt.getSource();
            if (textField != null && textField.getName() != null && evt.getNewValue() != null) {
                update(textField.getName(), evt.getNewValue().toString());
            }
        }
    }

    private void update(String name, String value) {
        // Store content as a string even for every types
        if (name != null && value != null) {
            model.updateKeyword(name, value);
            notifiedParent.updateModel(true);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
