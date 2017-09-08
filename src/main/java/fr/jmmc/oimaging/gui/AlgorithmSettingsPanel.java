/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.gui;

import fr.jmmc.jmcs.gui.action.ActionRegistrar;
import fr.jmmc.oimaging.gui.action.LoadFitsImageAction;
import fr.jmmc.oimaging.model.IRModel;
import fr.jmmc.oimaging.services.Service;
import fr.jmmc.oimaging.services.ServiceList;
import fr.jmmc.oitools.image.FitsImageHDU;
import fr.jmmc.oitools.image.ImageOiInputParam;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JFormattedTextField;

/**
 *
 * @author mellag
 */
public class AlgorithmSettingsPanel extends javax.swing.JPanel {

    /* members */
    /** associated mainPanel */
    private MainPanel mainPanel;

    /** temporary flag set to force notification of model changed .
     * Mostly used by TableKeywordEditor.
     */
    private boolean forceChange;

    /** Creates new form AlgorithmSettinsPanel */
    public AlgorithmSettingsPanel() {
        initComponents();
        postInit();
    }

    /**
     * This method is useful to set the models and specific features of initialized swing components :
     */
    private void postInit() {
        registerActions();
        jComboBoxImage.setRenderer(new OiCellRenderer());
    }

    /**
     * Create the main actions
     */
    private void registerActions() {
        // Map actions to widgets
        jButtonLoadFitsImage.setAction(ActionRegistrar.getInstance().get(LoadFitsImageAction.className, LoadFitsImageAction.actionName));
        jButtonLoadFitsImage.setHideActionText(true);
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

        jPanelAlgorithmSettings = new javax.swing.JPanel();
        jLabelInitImg = new javax.swing.JLabel();
        jLabelMaxIter = new javax.swing.JLabel();
        jLabelRglName = new javax.swing.JLabel();
        jLabelRglWgt = new javax.swing.JLabel();
        jLabelRglAlph = new javax.swing.JLabel();
        jLabelRglBeta = new javax.swing.JLabel();
        jLabelRglPrio = new javax.swing.JLabel();
        jComboBoxSoftware = new javax.swing.JComboBox();
        jComboBoxImage = new javax.swing.JComboBox();
        jSpinnerMaxIter = new javax.swing.JSpinner();
        jComboBoxRglName = new javax.swing.JComboBox();
        jFormattedTextFieldRglWgt = new javax.swing.JFormattedTextField();
        jFormattedTextFieldRglAlph = new javax.swing.JFormattedTextField();
        jFormattedTextFieldRglBeta = new javax.swing.JFormattedTextField();
        jComboBoxRglPrio = new javax.swing.JComboBox();
        jButtonRemoveFitsImage = new javax.swing.JButton();
        jButtonLoadFitsImage = new javax.swing.JButton();
        jCheckBoxAutoWgt = new javax.swing.JCheckBox();
        jLabelFluxErr = new javax.swing.JLabel();
        jFormattedTextFieldFluxErr = new javax.swing.JFormattedTextField();
        tableEditor1 = new fr.jmmc.oimaging.gui.TableKeywordsEditor();

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jPanelAlgorithmSettings.setBorder(javax.swing.BorderFactory.createTitledBorder("Algorithm settings"));
        jPanelAlgorithmSettings.setLayout(new java.awt.GridBagLayout());

        jLabelInitImg.setText("INIT_IMG");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelAlgorithmSettings.add(jLabelInitImg, gridBagConstraints);

        jLabelMaxIter.setText("MAXITER");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelAlgorithmSettings.add(jLabelMaxIter, gridBagConstraints);

        jLabelRglName.setText("RGL_NAME");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelAlgorithmSettings.add(jLabelRglName, gridBagConstraints);

        jLabelRglWgt.setText("RGL_WGT");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelAlgorithmSettings.add(jLabelRglWgt, gridBagConstraints);

        jLabelRglAlph.setText("RGL_ALPH");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelAlgorithmSettings.add(jLabelRglAlph, gridBagConstraints);

        jLabelRglBeta.setText("RGL_BETA");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelAlgorithmSettings.add(jLabelRglBeta, gridBagConstraints);

        jLabelRglPrio.setText("RGL_PRIO");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weighty = 0.1;
        jPanelAlgorithmSettings.add(jLabelRglPrio, gridBagConstraints);

        jComboBoxSoftware.setModel(ServiceList.getAvailableServices());
        jComboBoxSoftware.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxSoftwareActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelAlgorithmSettings.add(jComboBoxSoftware, gridBagConstraints);

        jComboBoxImage.setMinimumSize(new java.awt.Dimension(140, 28));
        jComboBoxImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxImageActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelAlgorithmSettings.add(jComboBoxImage, gridBagConstraints);

        jSpinnerMaxIter.setModel(new javax.swing.SpinnerNumberModel(0, -1, null, 5));
        jSpinnerMaxIter.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerMaxIterStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelAlgorithmSettings.add(jSpinnerMaxIter, gridBagConstraints);

        jComboBoxRglName.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "mem_prior" }));
        jComboBoxRglName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxRglNameActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelAlgorithmSettings.add(jComboBoxRglName, gridBagConstraints);

        jFormattedTextFieldRglWgt.setFormatterFactory(getDecimalFormatterFactory());
        jFormattedTextFieldRglWgt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextFieldRglWgtjFormattedTextFieldActionPerformed(evt);
            }
        });
        jFormattedTextFieldRglWgt.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jFormattedTextFieldRglWgtjFormattedTextFieldPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelAlgorithmSettings.add(jFormattedTextFieldRglWgt, gridBagConstraints);

        jFormattedTextFieldRglAlph.setFormatterFactory(getDecimalFormatterFactory());
        jFormattedTextFieldRglAlph.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextFieldRglAlphjFormattedTextFieldActionPerformed(evt);
            }
        });
        jFormattedTextFieldRglAlph.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jFormattedTextFieldRglAlphjFormattedTextFieldPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelAlgorithmSettings.add(jFormattedTextFieldRglAlph, gridBagConstraints);

        jFormattedTextFieldRglBeta.setFormatterFactory(getDecimalFormatterFactory());
        jFormattedTextFieldRglBeta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextFieldRglBetajFormattedTextFieldActionPerformed(evt);
            }
        });
        jFormattedTextFieldRglBeta.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jFormattedTextFieldRglBetajFormattedTextFieldPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelAlgorithmSettings.add(jFormattedTextFieldRglBeta, gridBagConstraints);

        jComboBoxRglPrio.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelAlgorithmSettings.add(jComboBoxRglPrio, gridBagConstraints);

        jButtonRemoveFitsImage.setText("-");
        jButtonRemoveFitsImage.setEnabled(false);
        jButtonRemoveFitsImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveFitsImageActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        jPanelAlgorithmSettings.add(jButtonRemoveFitsImage, gridBagConstraints);

        jButtonLoadFitsImage.setText("+");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        jPanelAlgorithmSettings.add(jButtonLoadFitsImage, gridBagConstraints);

        jCheckBoxAutoWgt.setText("AUTO");
        jCheckBoxAutoWgt.setToolTipText("Automatic regularization weight");
        jCheckBoxAutoWgt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxAutoWgtActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelAlgorithmSettings.add(jCheckBoxAutoWgt, gridBagConstraints);

        jLabelFluxErr.setText("FLUXERR");
        jLabelFluxErr.setToolTipText("Error on zero-baseline squared visibility point (used to enforce flux   normalisation)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelAlgorithmSettings.add(jLabelFluxErr, gridBagConstraints);

        jFormattedTextFieldFluxErr.setFormatterFactory(getDecimalFormatterFactory());
        jFormattedTextFieldFluxErr.setToolTipText("Error on zero-baseline squared visibility point (used to enforce flux   normalisation)");
        jFormattedTextFieldFluxErr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextFieldFluxErrjFormattedTextFieldActionPerformed(evt);
            }
        });
        jFormattedTextFieldFluxErr.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jFormattedTextFieldFluxErrjFormattedTextFieldPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelAlgorithmSettings.add(jFormattedTextFieldFluxErr, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        jPanelAlgorithmSettings.add(tableEditor1, gridBagConstraints);

        add(jPanelAlgorithmSettings);
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxSoftwareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxSoftwareActionPerformed
        updateModel();
    }//GEN-LAST:event_jComboBoxSoftwareActionPerformed

    private void jComboBoxImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxImageActionPerformed
        updateModel();

    }//GEN-LAST:event_jComboBoxImageActionPerformed

    private void jSpinnerMaxIterStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerMaxIterStateChanged
        updateModel();
    }//GEN-LAST:event_jSpinnerMaxIterStateChanged

    private void jComboBoxRglNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxRglNameActionPerformed
        updateModel();
    }//GEN-LAST:event_jComboBoxRglNameActionPerformed

    private void jFormattedTextFieldRglWgtjFormattedTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextFieldRglWgtjFormattedTextFieldActionPerformed
        updateModel();
    }//GEN-LAST:event_jFormattedTextFieldRglWgtjFormattedTextFieldActionPerformed

    private void jFormattedTextFieldRglWgtjFormattedTextFieldPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jFormattedTextFieldRglWgtjFormattedTextFieldPropertyChange
        updateModel();
    }//GEN-LAST:event_jFormattedTextFieldRglWgtjFormattedTextFieldPropertyChange

    private void jFormattedTextFieldRglAlphjFormattedTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextFieldRglAlphjFormattedTextFieldActionPerformed
        updateModel();
    }//GEN-LAST:event_jFormattedTextFieldRglAlphjFormattedTextFieldActionPerformed

    private void jFormattedTextFieldRglAlphjFormattedTextFieldPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jFormattedTextFieldRglAlphjFormattedTextFieldPropertyChange
        updateModel();
    }//GEN-LAST:event_jFormattedTextFieldRglAlphjFormattedTextFieldPropertyChange

    private void jFormattedTextFieldRglBetajFormattedTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextFieldRglBetajFormattedTextFieldActionPerformed
        updateModel();
    }//GEN-LAST:event_jFormattedTextFieldRglBetajFormattedTextFieldActionPerformed

    private void jFormattedTextFieldRglBetajFormattedTextFieldPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jFormattedTextFieldRglBetajFormattedTextFieldPropertyChange
        updateModel();
    }//GEN-LAST:event_jFormattedTextFieldRglBetajFormattedTextFieldPropertyChange

    private void jButtonRemoveFitsImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveFitsImageActionPerformed
        // TODO
    }//GEN-LAST:event_jButtonRemoveFitsImageActionPerformed

    private void jCheckBoxAutoWgtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxAutoWgtActionPerformed
        updateModel();
    }//GEN-LAST:event_jCheckBoxAutoWgtActionPerformed

    private void jFormattedTextFieldFluxErrjFormattedTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextFieldFluxErrjFormattedTextFieldActionPerformed
        updateModel();
    }//GEN-LAST:event_jFormattedTextFieldFluxErrjFormattedTextFieldActionPerformed

    private void jFormattedTextFieldFluxErrjFormattedTextFieldPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jFormattedTextFieldFluxErrjFormattedTextFieldPropertyChange
        updateModel();
    }//GEN-LAST:event_jFormattedTextFieldFluxErrjFormattedTextFieldPropertyChange

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonLoadFitsImage;
    private javax.swing.JButton jButtonRemoveFitsImage;
    private javax.swing.JCheckBox jCheckBoxAutoWgt;
    private javax.swing.JComboBox jComboBoxImage;
    private javax.swing.JComboBox jComboBoxRglName;
    private javax.swing.JComboBox jComboBoxRglPrio;
    private javax.swing.JComboBox jComboBoxSoftware;
    private javax.swing.JFormattedTextField jFormattedTextFieldFluxErr;
    private javax.swing.JFormattedTextField jFormattedTextFieldRglAlph;
    private javax.swing.JFormattedTextField jFormattedTextFieldRglBeta;
    private javax.swing.JFormattedTextField jFormattedTextFieldRglWgt;
    private javax.swing.JLabel jLabelFluxErr;
    private javax.swing.JLabel jLabelInitImg;
    private javax.swing.JLabel jLabelMaxIter;
    private javax.swing.JLabel jLabelRglAlph;
    private javax.swing.JLabel jLabelRglBeta;
    private javax.swing.JLabel jLabelRglName;
    private javax.swing.JLabel jLabelRglPrio;
    private javax.swing.JLabel jLabelRglWgt;
    private javax.swing.JPanel jPanelAlgorithmSettings;
    private javax.swing.JSpinner jSpinnerMaxIter;
    private fr.jmmc.oimaging.gui.TableKeywordsEditor tableEditor1;
    // End of variables declaration//GEN-END:variables

    public static JFormattedTextField.AbstractFormatterFactory getDecimalFormatterFactory() {
        return new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.#####")));
    }

    public static JFormattedTextField.AbstractFormatterFactory getIntegerFormatterFactory() {
        return new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(NumberFormat.getIntegerInstance()));
    }

    void syncUI(MainPanel panel, IRModel irModel, List<String> failures) {
        mainPanel = panel;

        ImageOiInputParam inputParam = irModel.getImageOiData().getInputParam();

        if (inputParam.getSubTable() != null) {
            tableEditor1.setModel(inputParam, inputParam.getSubTable().getKeywordsDesc().keySet(), this);
        } else {
            tableEditor1.setModel(null, this);
        }
        // image combo
        jComboBoxImage.removeAllItems();
        for (FitsImageHDU fitsImageHDU : irModel.getFitsImageHDUs()) {
            jComboBoxImage.addItem(fitsImageHDU);
        }

        // max iter
        jSpinnerMaxIter.setValue(inputParam.getMaxiter());

        // regulation
        // update content of jCombobox
        updateJComboBoxRglName(irModel);

        String rglName = inputParam.getRglName();
        if (rglName != null) {
            jComboBoxRglName.setSelectedItem(rglName);
        }
        jFormattedTextFieldRglWgt.setValue(inputParam.getRglWgt());
        jFormattedTextFieldRglAlph.setValue(inputParam.getRglAlph());
        jFormattedTextFieldRglBeta.setValue(inputParam.getRglBeta());

        String rglPrio = inputParam.getRglPrio();
        if (rglPrio != null) {
            jComboBoxRglPrio.setSelectedItem(rglPrio);
        }

        boolean wFlag = inputParam.useAutoWgt();
        jCheckBoxAutoWgt.setSelected(wFlag);
        jFormattedTextFieldRglWgt.setEnabled(!wFlag);
        jLabelRglWgt.setEnabled(!wFlag);

        double fluxerr = inputParam.getFluxErr();
        jFormattedTextFieldFluxErr.setValue(inputParam.getFluxErr());

        if (fluxerr < 1e-5) {
            failures.add("FluxErr must be greater than 1e-5");
        } else if (fluxerr > 1) {
            failures.add("FluxErr must be smaller than 1");
        }

        Service mService = irModel.getSelectedService();
        Service wService = (Service) jComboBoxSoftware.getSelectedItem();
        if (mService != wService) {
            jComboBoxSoftware.setSelectedItem(mService);
        }

        FitsImageHDU selectedFitsImageHDU = irModel.getSelectedInputImageHDU();
        if (selectedFitsImageHDU != null) {
            jComboBoxImage.getModel().setSelectedItem(selectedFitsImageHDU);
        } else {
            failures.add(irModel.getSelectedInputFitsImageError());
        }

    }

    protected void updateModel() {
        updateModel(false);
    }

    protected void updateModel(final boolean forceChange) {

        this.forceChange = forceChange;

        if (mainPanel != null) {
            mainPanel.updateModel();
        }
    }

    boolean updateModel(IRModel irModel) {
        ImageOiInputParam params = irModel.getImageOiData().getInputParam();

        // Update if model_values != swing_values and detect change if one or more values change
        boolean changed = false;
        double mDouble, wDouble;
        String mString, wString;
        boolean mFlag, wFlag;
        int mInt, wInt;

        // Selected software
        final Service guiService = (Service) jComboBoxSoftware.getSelectedItem();
        final Service modelSoftware = irModel.getSelectedService();
        if (guiService != null && !guiService.equals(modelSoftware)) {
            irModel.setSelectedSoftware(guiService);
            updateJComboBoxRglName(irModel);
            changed = true;
        }

        // Init Image
        final FitsImageHDU mFitsImageHDU = irModel.getSelectedInputImageHDU();
        final FitsImageHDU sFitsImageHDU = (FitsImageHDU) this.jComboBoxImage.getSelectedItem();
        if (sFitsImageHDU != null && !(sFitsImageHDU == mFitsImageHDU)) {
            irModel.setSelectedInputImageHDU(sFitsImageHDU);
            changed = true;
        }

        // max iter
        try {
            // guarantee last user value
            jSpinnerMaxIter.commitEdit();
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(MainPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        mInt = params.getMaxiter();
        wInt = (Integer) jSpinnerMaxIter.getValue();
        if (mInt != wInt) {
            params.setMaxiter(wInt);
            changed = true;
        }

        // regularization
        mString = params.getRglName();
        if (jComboBoxRglName.getSelectedItem() != null) {
            wString = (String) jComboBoxRglName.getSelectedItem();
            if (!wString.equals(mString)) {
                params.setRglName(wString);
                irModel.initSpecificParams(); // update call required to apply on fly specific param handling
                changed = true;
            }
        }

        mDouble = params.getRglWgt();
        if (jFormattedTextFieldRglWgt.getValue() != null) {
            wDouble = ((Number) jFormattedTextFieldRglWgt.getValue()).doubleValue();
            if (mDouble != wDouble) {
                params.setRglWgt(wDouble);
                changed = true;
            }
        }
        mFlag = params.useAutoWgt();
        wFlag = jCheckBoxAutoWgt.isSelected();
        if (mFlag != wFlag) {
            params.useAutoWgt(wFlag);
            changed = true;
        }
        jFormattedTextFieldRglWgt.setEnabled(!wFlag);
        jLabelRglWgt.setEnabled(!wFlag);

        mDouble = params.getRglAlph();
        if (jFormattedTextFieldRglAlph.getValue() != null) {
            wDouble = ((Number) jFormattedTextFieldRglAlph.getValue()).doubleValue();
            if (mDouble != wDouble) {
                params.setRglAlph(wDouble);
                changed = true;
            }
        }

        mDouble = params.getRglBeta();
        if (jFormattedTextFieldRglBeta.getValue() != null) {
            wDouble = ((Number) jFormattedTextFieldRglBeta.getValue()).doubleValue();
            if (mDouble != wDouble) {
                params.setRglBeta(wDouble);
                changed = true;
            }
        }

        mString = params.getRglPrio();
        if (jComboBoxRglPrio.getSelectedItem() != null) {
            wString = (String) jComboBoxRglPrio.getSelectedItem();
            if (!wString.equals(mString)) {
                params.setRglPrio(wString);
                changed = true;
            }
        }

        mDouble = params.getFluxErr();
        if (jFormattedTextFieldFluxErr.getValue() != null) {
            wDouble = ((Number) jFormattedTextFieldFluxErr.getValue()).doubleValue();
            if (mDouble != wDouble) {
                params.setFluxErr(wDouble);
                changed = true;
            }
        }

        changed = changed || forceChange;
        forceChange = false;
        return changed;
    }

    private void updateJComboBoxRglName(IRModel irModel) {
        jComboBoxRglName.removeAllItems();
        for (String v : irModel.getSelectedService().getSupported_RGL_NAME()) {
            jComboBoxRglName.addItem(v);
        }
    }

}
