/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ***************************************************************************** */
package fr.jmmc.oimaging.gui;

import fr.jmmc.jmcs.gui.action.ActionRegistrar;
import fr.jmmc.oimaging.gui.action.LoadFitsImageAction;
import fr.jmmc.oimaging.model.IRModel;
import static fr.jmmc.oimaging.model.IRModel.NULL_IMAGE_HDU;
import fr.jmmc.oimaging.services.Service;
import fr.jmmc.oimaging.services.ServiceList;
import fr.jmmc.oitools.image.FitsImageHDU;
import fr.jmmc.oitools.image.ImageOiConstants;
import static fr.jmmc.oitools.image.ImageOiConstants.KEYWORD_INIT_IMG;
import static fr.jmmc.oitools.image.ImageOiConstants.KEYWORD_RGL_PRIO;
import fr.jmmc.oitools.image.ImageOiInputParam;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import javax.swing.JFormattedTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mellag
 */
public class SoftwareSettingsPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 1L;

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(SoftwareSettingsPanel.class);


    /* members */
    /**
     * associated mainPanel
     */
    private MainPanel mainPanel;

    /**
     * Flag set to true while the GUI is being updated by model else false.
     */
    private boolean syncingUI = false;

    /**
     * Which of the input images has been changed last : INIT_IMG or RGL_PRIO.
     * Knowing this, viewerPanel can display the good one.
     * Values: null, "initImage", "rglPrio".
     * We need this field here because only here can we react to the event properly,
     * IRModel does not have enough information to do it itself.
     */
    private String lastImageChanged = null;

    /**
     * Creates new form AlgorithmSettinsPanel
     */
    public SoftwareSettingsPanel() {
        initComponents();
        postInit();
    }

    /**
     * This method is useful to set the models and specific features of
     * initialized swing components :
     */
    private void postInit() {
        registerActions();
        jComboBoxImage.setRenderer(new OiCellRenderer());
        jComboBoxRglPrio.setRenderer(new OiCellRenderer());
        jTableKeywordsEditor.setNotifiedParent(this);

        this.jTextAreaOptions.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent e) {
                updateModel();
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                updateModel();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                //Plain text components do not fire these events
            }
        });
    }

    /**
     * Create the main actions
     */
    private void registerActions() {
        // Map actions to widgets
        jButtonLoadFitsImage.setAction(ActionRegistrar.getInstance().get(LoadFitsImageAction.className, LoadFitsImageAction.actionName));
        jButtonLoadFitsImage.setHideActionText(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanelForm = new javax.swing.JPanel();
        jComboBoxSoftware = new javax.swing.JComboBox();
        jLabelInitImg = new javax.swing.JLabel();
        jComboBoxImage = new javax.swing.JComboBox<>();
        jLabelMaxIter = new javax.swing.JLabel();
        jSpinnerMaxIter = new javax.swing.JSpinner();
        jLabelRglName = new javax.swing.JLabel();
        jComboBoxRglName = new javax.swing.JComboBox();
        jLabelAutoWgt = new javax.swing.JLabel();
        jLabelRglWgt = new javax.swing.JLabel();
        jFormattedTextFieldRglWgt = new javax.swing.JFormattedTextField();
        jLabelFlux = new javax.swing.JLabel();
        jFormattedTextFieldFlux = new javax.swing.JFormattedTextField();
        jLabelFluxErr = new javax.swing.JLabel();
        jFormattedTextFieldFluxErr = new javax.swing.JFormattedTextField();
        jLabelRglPrio = new javax.swing.JLabel();
        jComboBoxRglPrio = new javax.swing.JComboBox<>();
        jButtonLoadFitsImage = new javax.swing.JButton();
        jButtonRemoveFitsImage = new javax.swing.JButton();
        jCheckBoxAutoWgt = new javax.swing.JCheckBox();
        jTableKeywordsEditor = new fr.jmmc.oimaging.gui.TableKeywordsEditor();
        jPanelOptions = new javax.swing.JPanel();
        jTextAreaOptions = new javax.swing.JTextArea();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Algorithm settings"));
        setLayout(new java.awt.GridBagLayout());

        jPanelForm.setLayout(new java.awt.GridBagLayout());

        jComboBoxSoftware.setModel(ServiceList.getAvailableServices());
        jComboBoxSoftware.setPrototypeDisplayValue("XXXX");
        jComboBoxSoftware.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxSoftwareActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelForm.add(jComboBoxSoftware, gridBagConstraints);

        jLabelInitImg.setText("INIT_IMG");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelForm.add(jLabelInitImg, gridBagConstraints);

        jComboBoxImage.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_INIT_IMG));
        jComboBoxImage.setPrototypeDisplayValue(NULL_IMAGE_HDU);
        jComboBoxImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxImageActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelForm.add(jComboBoxImage, gridBagConstraints);

        jLabelMaxIter.setText("MAXITER");
        jLabelMaxIter.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_MAXITER));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelForm.add(jLabelMaxIter, gridBagConstraints);

        jSpinnerMaxIter.setModel(new javax.swing.SpinnerNumberModel(50, 0, 1000, 5));
        jSpinnerMaxIter.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_MAXITER));
        jSpinnerMaxIter.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerMaxIterStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelForm.add(jSpinnerMaxIter, gridBagConstraints);

        jLabelRglName.setText("RGL_NAME");
        jLabelRglName.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_RGL_NAME));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelForm.add(jLabelRglName, gridBagConstraints);

        jComboBoxRglName.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "mem_prior" }));
        jComboBoxRglName.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_RGL_NAME));
        jComboBoxRglName.setPrototypeDisplayValue("XXXX");
        jComboBoxRglName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxRglNameActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelForm.add(jComboBoxRglName, gridBagConstraints);

        jLabelAutoWgt.setText("AUTO_WGT");
        jLabelAutoWgt.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_AUTO_WGT));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelForm.add(jLabelAutoWgt, gridBagConstraints);

        jLabelRglWgt.setText("RGL_WGT");
        jLabelRglWgt.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_RGL_WGT));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelForm.add(jLabelRglWgt, gridBagConstraints);

        jFormattedTextFieldRglWgt.setColumns(6);
        jFormattedTextFieldRglWgt.setFormatterFactory(getDecimalFormatterFactory());
        jFormattedTextFieldRglWgt.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_RGL_WGT));
        jFormattedTextFieldRglWgt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextFieldRglWgtActionPerformed(evt);
            }
        });
        jFormattedTextFieldRglWgt.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jFormattedTextFieldRglWgtPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelForm.add(jFormattedTextFieldRglWgt, gridBagConstraints);

        jLabelFlux.setText("FLUX");
        jLabelFlux.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_FLUX));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelForm.add(jLabelFlux, gridBagConstraints);

        jFormattedTextFieldFlux.setColumns(6);
        jFormattedTextFieldFlux.setFormatterFactory(getDecimalFormatterFactory());
        jFormattedTextFieldFlux.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_FLUX));
        jFormattedTextFieldFlux.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextFieldFluxActionPerformed(evt);
            }
        });
        jFormattedTextFieldFlux.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jFormattedTextFieldFluxPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelForm.add(jFormattedTextFieldFlux, gridBagConstraints);

        jLabelFluxErr.setText("FLUXERR");
        jLabelFluxErr.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_FLUXERR));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelForm.add(jLabelFluxErr, gridBagConstraints);

        jFormattedTextFieldFluxErr.setColumns(6);
        jFormattedTextFieldFluxErr.setFormatterFactory(getDecimalFormatterFactory());
        jFormattedTextFieldFluxErr.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_FLUXERR));
        jFormattedTextFieldFluxErr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextFieldFluxErrActionPerformed(evt);
            }
        });
        jFormattedTextFieldFluxErr.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jFormattedTextFieldFluxErrPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelForm.add(jFormattedTextFieldFluxErr, gridBagConstraints);

        jLabelRglPrio.setText("RGL_PRIO");
        jLabelRglPrio.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_RGL_PRIO));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelForm.add(jLabelRglPrio, gridBagConstraints);

        jComboBoxRglPrio.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_RGL_PRIO));
        jComboBoxRglPrio.setEnabled(false);
        jComboBoxRglPrio.setPrototypeDisplayValue(NULL_IMAGE_HDU);
        jComboBoxRglPrio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxRglPrioActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelForm.add(jComboBoxRglPrio, gridBagConstraints);

        jButtonLoadFitsImage.setText("+");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelForm.add(jButtonLoadFitsImage, gridBagConstraints);

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
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelForm.add(jButtonRemoveFitsImage, gridBagConstraints);

        jCheckBoxAutoWgt.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_AUTO_WGT));
        jCheckBoxAutoWgt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxAutoWgtActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelForm.add(jCheckBoxAutoWgt, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jPanelForm, gridBagConstraints);

        jTableKeywordsEditor.setBorder(javax.swing.BorderFactory.createTitledBorder("Specific parameters"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jTableKeywordsEditor, gridBagConstraints);

        jPanelOptions.setBorder(javax.swing.BorderFactory.createTitledBorder("Manual options"));
        jPanelOptions.setMinimumSize(new java.awt.Dimension(100, 100));
        jPanelOptions.setPreferredSize(new java.awt.Dimension(100, 100));
        jPanelOptions.setLayout(new java.awt.GridBagLayout());

        jTextAreaOptions.setColumns(10);
        jTextAreaOptions.setLineWrap(true);
        jTextAreaOptions.setRows(3);
        jTextAreaOptions.setTabSize(2);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        jPanelOptions.add(jTextAreaOptions, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.1;
        add(jPanelOptions, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxSoftwareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxSoftwareActionPerformed
        updateModel();
    }//GEN-LAST:event_jComboBoxSoftwareActionPerformed

    private void jComboBoxImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxImageActionPerformed
        this.lastImageChanged = "initImg";
        updateModel();
    }//GEN-LAST:event_jComboBoxImageActionPerformed

    private void jSpinnerMaxIterStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerMaxIterStateChanged
        updateModel();
    }//GEN-LAST:event_jSpinnerMaxIterStateChanged

    private void jComboBoxRglNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxRglNameActionPerformed
        updateModel();
    }//GEN-LAST:event_jComboBoxRglNameActionPerformed

    private void jFormattedTextFieldRglWgtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextFieldRglWgtActionPerformed
        updateModel();
    }//GEN-LAST:event_jFormattedTextFieldRglWgtActionPerformed

    private void jFormattedTextFieldRglWgtPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jFormattedTextFieldRglWgtPropertyChange
        updateModel();
    }//GEN-LAST:event_jFormattedTextFieldRglWgtPropertyChange

    private void jFormattedTextFieldFluxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextFieldFluxActionPerformed
        updateModel();
    }//GEN-LAST:event_jFormattedTextFieldFluxActionPerformed

    private void jFormattedTextFieldFluxPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jFormattedTextFieldFluxPropertyChange
        updateModel();
    }//GEN-LAST:event_jFormattedTextFieldFluxPropertyChange

    private void jFormattedTextFieldFluxErrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextFieldFluxErrActionPerformed
        updateModel();
    }//GEN-LAST:event_jFormattedTextFieldFluxErrActionPerformed

    private void jFormattedTextFieldFluxErrPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jFormattedTextFieldFluxErrPropertyChange
        updateModel();
    }//GEN-LAST:event_jFormattedTextFieldFluxErrPropertyChange

    private void jButtonRemoveFitsImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveFitsImageActionPerformed
        // TODO
    }//GEN-LAST:event_jButtonRemoveFitsImageActionPerformed

    private void jCheckBoxAutoWgtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxAutoWgtActionPerformed
        updateModel();
    }//GEN-LAST:event_jCheckBoxAutoWgtActionPerformed

    private void jComboBoxRglPrioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxRglPrioActionPerformed
        this.lastImageChanged = "rglPrio";
        updateModel();
    }//GEN-LAST:event_jComboBoxRglPrioActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonLoadFitsImage;
    private javax.swing.JButton jButtonRemoveFitsImage;
    private javax.swing.JCheckBox jCheckBoxAutoWgt;
    private javax.swing.JComboBox<FitsImageHDU> jComboBoxImage;
    private javax.swing.JComboBox jComboBoxRglName;
    private javax.swing.JComboBox<FitsImageHDU> jComboBoxRglPrio;
    private javax.swing.JComboBox jComboBoxSoftware;
    private javax.swing.JFormattedTextField jFormattedTextFieldFlux;
    private javax.swing.JFormattedTextField jFormattedTextFieldFluxErr;
    private javax.swing.JFormattedTextField jFormattedTextFieldRglWgt;
    private javax.swing.JLabel jLabelAutoWgt;
    private javax.swing.JLabel jLabelFlux;
    private javax.swing.JLabel jLabelFluxErr;
    private javax.swing.JLabel jLabelInitImg;
    private javax.swing.JLabel jLabelMaxIter;
    private javax.swing.JLabel jLabelRglName;
    private javax.swing.JLabel jLabelRglPrio;
    private javax.swing.JLabel jLabelRglWgt;
    private javax.swing.JPanel jPanelForm;
    private javax.swing.JPanel jPanelOptions;
    private javax.swing.JSpinner jSpinnerMaxIter;
    private fr.jmmc.oimaging.gui.TableKeywordsEditor jTableKeywordsEditor;
    private javax.swing.JTextArea jTextAreaOptions;
    // End of variables declaration//GEN-END:variables

    public static JFormattedTextField.AbstractFormatterFactory getDecimalFormatterFactory() {
        return new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("0.0####E00")) {
            private static final long serialVersionUID = 1L;

            private final NumberFormat fmtDef = new DecimalFormat("0.0####");

            @Override
            public String valueToString(final Object value) throws ParseException {
                if (value == null) {
                    return "";
                }
                if (value instanceof Double) {
                    // check value range:
                    final double abs = Math.abs((Double) value);

                    if ((abs > 1e-3d) && (abs < 1e3d)) {
                        return fmtDef.format(value);
                    }
                }
                final String formatted = super.valueToString(value);
                if (formatted.endsWith("E00")) {
                    return formatted.substring(0, formatted.length() - 3);
                }
                return formatted;
            }
        });
    }

    public static JFormattedTextField.AbstractFormatterFactory getIntegerFormatterFactory() {
        return new DefaultFormatterFactory(new NumberFormatter(NumberFormat.getIntegerInstance()));
    }

    void syncUI(final MainPanel panel, final IRModel irModel, final List<String> failures) {
        mainPanel = panel;
        syncingUI = true;
        try {
            final ImageOiInputParam inputParam = irModel.getImageOiData().getInputParam();
            final Service service = irModel.getSelectedService();

            boolean show;

            if (inputParam.getSpecificKeywords().isEmpty()) {
                jTableKeywordsEditor.setModel(null);
                jTableKeywordsEditor.setVisible(false);
            } else {
                jTableKeywordsEditor.setModel(inputParam, inputParam.getSpecificKeywords());
                jTableKeywordsEditor.setVisible(true);
            }

            // INIT IMG
            jComboBoxImage.removeAllItems();

            if (service.supportsMissingKeyword(KEYWORD_INIT_IMG)) {
                jComboBoxImage.addItem(NULL_IMAGE_HDU);
            }

            irModel.getFitsImageHDUs().forEach(jComboBoxImage::addItem);

            final FitsImageHDU initImage = irModel.getSelectedInputImageHDU();

            if (service.supportsMissingKeyword(KEYWORD_INIT_IMG)) {
                if (initImage == null) {
                    jComboBoxImage.getModel().setSelectedItem(NULL_IMAGE_HDU);
                } else {
                    jComboBoxImage.getModel().setSelectedItem(initImage);
                }
            } else {
                if (initImage == null || initImage == NULL_IMAGE_HDU) {
                    failures.add(irModel.getSelectedInputFitsImageError());
                    logger.error("Cannot select null item because the keyword INIT_IMG is mandatory.");
                } else {
                    jComboBoxImage.getModel().setSelectedItem(initImage);
                }
            }

            // RGL PRIO
            show = service.supportsStandardKeyword(KEYWORD_RGL_PRIO);
            jLabelRglPrio.setVisible(show);
            jComboBoxRglPrio.setVisible(show);
            jComboBoxRglPrio.setEnabled(show);

            if (show) {

                jComboBoxRglPrio.removeAllItems();

                if (service.supportsMissingKeyword(KEYWORD_RGL_PRIO)) {
                    jComboBoxRglPrio.addItem(NULL_IMAGE_HDU);
                }

                irModel.getFitsImageHDUs().forEach(jComboBoxRglPrio::addItem);

                FitsImageHDU rglPrioImage = irModel.getSelectedRglPrioImage();

                if (service.supportsMissingKeyword(KEYWORD_RGL_PRIO)) {
                    if (rglPrioImage == null) {
                        jComboBoxRglPrio.getModel().setSelectedItem(NULL_IMAGE_HDU);
                    } else {
                        jComboBoxRglPrio.getModel().setSelectedItem(rglPrioImage);
                    }
                } else {
                    if (rglPrioImage == null || rglPrioImage == NULL_IMAGE_HDU) {
                        failures.add("No image for RGL_PRIO");
                        logger.error("Cannot select null item because the keyword RGL_PRIO is mandatory.");
                    } else {
                        jComboBoxRglPrio.getModel().setSelectedItem(rglPrioImage);
                    }
                }
            }

            // Max iter:
            jSpinnerMaxIter.setValue(inputParam.getMaxiter());
            show = service.supportsStandardKeyword(ImageOiConstants.KEYWORD_MAXITER);

            jLabelMaxIter.setVisible(show);

            jSpinnerMaxIter.setVisible(show);

            // regulation Name:
            // update content of jCombobox
            updateJComboBoxRglName(inputParam, service);

            // regulation Weight:
            jCheckBoxAutoWgt.setSelected(inputParam.isAutoWgt());
            show = service.supportsStandardKeyword(ImageOiConstants.KEYWORD_AUTO_WGT);

            jLabelAutoWgt.setVisible(show);

            jCheckBoxAutoWgt.setVisible(show);

            jFormattedTextFieldRglWgt.setValue(inputParam.getRglWgt());
            show = service.supportsStandardKeyword(ImageOiConstants.KEYWORD_RGL_WGT);

            jLabelRglWgt.setVisible(show);

            jFormattedTextFieldRglWgt.setVisible(show);
            // change visibility / enabled if RglWgt keyword exists (bsmem auto)
            final boolean enabled = inputParam.hasKeywordMeta(ImageOiConstants.KEYWORD_RGL_WGT);

            jLabelRglWgt.setEnabled(enabled);

            jFormattedTextFieldRglWgt.setEnabled(enabled);

            // flux:
            jFormattedTextFieldFlux.setValue(inputParam.getFlux());
            show = service.supportsStandardKeyword(ImageOiConstants.KEYWORD_FLUX);

            jLabelFlux.setVisible(show);

            jFormattedTextFieldFlux.setVisible(show);

            // flux Err:
            jFormattedTextFieldFluxErr.setValue(inputParam.getFluxErr());
            show = service.supportsStandardKeyword(ImageOiConstants.KEYWORD_FLUXERR);

            jLabelFluxErr.setVisible(show);

            jFormattedTextFieldFluxErr.setVisible(show);

            // validate
            service.validate(inputParam, failures);

            // identity check on singletons:
            if (service
                    != jComboBoxSoftware.getSelectedItem()) {
                jComboBoxSoftware.setSelectedItem(service);
            }

            // CLI Options:
            final String cliOptions = irModel.getCliOptions();

            if (!jTextAreaOptions.getText()
                    .equals(cliOptions)) {
                jTextAreaOptions.setText(cliOptions == null ? "" : cliOptions);
            }
        } finally {
            syncingUI = false;
        }
    }

    protected void updateModel() {
        updateModel(false);
    }

    protected void updateModel(final boolean forceChange) {
        if (syncingUI) {
            logger.debug("updateModel discarded: syncUI.");
            return;
        }
        if (mainPanel != null) {
            mainPanel.updateModel(forceChange);
        }
    }

    boolean updateModel(final IRModel irModel) {
        final ImageOiInputParam inputParam = irModel.getImageOiData().getInputParam();

        // Update if model_values != swing_values and detect change if one or more values change
        boolean changedService = false;
        boolean changed = false;
        double mDouble, wDouble;
        String mString, wString;
        int mInt, wInt;

        // Selected software
        final Service guiService = (Service) jComboBoxSoftware.getSelectedItem();
        final Service modelSoftware = irModel.getSelectedService();
        if (guiService != null && !guiService.equals(modelSoftware)) {
            changedService = !modelSoftware.isCompatibleParams(guiService);
            irModel.setSelectedService(guiService);
            changed = true;
        }

        // Init Image
        final FitsImageHDU comboBoxInitImage = (FitsImageHDU) jComboBoxImage.getSelectedItem();

        if (!modelSoftware.supportsMissingKeyword(KEYWORD_INIT_IMG)
                && (comboBoxInitImage == null || comboBoxInitImage == NULL_IMAGE_HDU)) {
            logger.error("INIT_IMG should not be null because keyword is mandatory.");
        } else {
            if (irModel.getSelectedInputImageHDU() != comboBoxInitImage) {
                irModel.setSelectedInputImageHDU(comboBoxInitImage);
                changed = true;
            }
        }

        // RGL PRIO Image Fits
        if (modelSoftware.supportsStandardKeyword(KEYWORD_RGL_PRIO)) {

            final FitsImageHDU comboBoxRglPrioImage = (FitsImageHDU) jComboBoxRglPrio.getSelectedItem();

            if (!modelSoftware.supportsMissingKeyword(KEYWORD_RGL_PRIO)
                    && (comboBoxRglPrioImage == null || comboBoxRglPrioImage == NULL_IMAGE_HDU)) {
                logger.error("RGL PRIO should not be null because keyword is mandatory.");
            } else {
                if (irModel.getSelectedRglPrioImage() != comboBoxRglPrioImage) {
                    irModel.setSelectedRglPrioImage(comboBoxRglPrioImage);
                    changed = true;
                }
            }
        }

        // lastImageDisplay
        irModel.setLastImageChanged(lastImageChanged);

        // max iter
        try {
            // guarantee last user value
            jSpinnerMaxIter.commitEdit();
        } catch (ParseException pe) {
            logger.warn("jSpinnerMaxIter parsing failed:", pe);
        }
        mInt = inputParam.getMaxiter();
        wInt = (Integer) jSpinnerMaxIter.getValue();
        if (mInt != wInt) {
            inputParam.setMaxiter(wInt);
            changed = true;
        }

        // regularization
        mString = inputParam.getRglName();

        if (jComboBoxRglName.getSelectedItem()
                != null) {
            wString = (String) jComboBoxRglName.getSelectedItem();
            if (!wString.equals(mString)) {
                inputParam.setRglName(wString);
                irModel.initSpecificParams(false); // update call required to apply on fly specific param handling
                changed = true;
            }
        }

        // regulation Weight:
        if (jCheckBoxAutoWgt.isSelected()
                != inputParam.isAutoWgt()) {
            inputParam.setAutoWgt(jCheckBoxAutoWgt.isSelected());
            irModel.initSpecificParams(false); // update call required to apply on fly specific param handling
            changed = true;
        }

        mDouble = inputParam.getRglWgt();

        if (jFormattedTextFieldRglWgt.getValue()
                != null) {
            wDouble = ((Number) jFormattedTextFieldRglWgt.getValue()).doubleValue();
            if (mDouble != wDouble) {
                inputParam.setRglWgt(wDouble);
                changed = true;
            }
        }

        mDouble = inputParam.getFlux();

        if (jFormattedTextFieldFlux.getValue()
                != null) {
            wDouble = ((Number) jFormattedTextFieldFlux.getValue()).doubleValue();
            if (mDouble != wDouble) {
                inputParam.setFlux(wDouble);
                changed = true;
            }
        }

        mDouble = inputParam.getFluxErr();

        if (jFormattedTextFieldFluxErr.getValue()
                != null) {
            wDouble = ((Number) jFormattedTextFieldFluxErr.getValue()).doubleValue();
            if (mDouble != wDouble) {
                inputParam.setFluxErr(wDouble);
                changed = true;
            }
        }

        // cliOptions
        mString = irModel.getCliOptions();
        wString = jTextAreaOptions.getText();

        if (!wString.equals(mString)) {
            irModel.setCliOptions(wString);
            changed = true;
        }

        if (changedService) {
            logger.info("changedService: {}", guiService);

            irModel.initSpecificParams(true); // update call required to reset params to software defaults

            // reset cli options:
            irModel.setCliOptions(guiService.getDefaultCliOptions());
        }

        return changed;
    }

    private void updateJComboBoxRglName(final ImageOiInputParam inputParam, final Service service) {
        jComboBoxRglName.removeAllItems();
        for (String v : service.getSupported_RGL_NAME()) {
            jComboBoxRglName.addItem(v);
        }
        String rglName = inputParam.getRglName();
        if (rglName != null) {
            jComboBoxRglName.setSelectedItem(rglName);
        }
        final boolean show = service.supportsStandardKeyword(ImageOiConstants.KEYWORD_RGL_NAME);
        jLabelRglName.setVisible(show);
        jComboBoxRglName.setVisible(show);
    }

    private static String getTooltip(final String name) {
        return ImageOiInputParam.getDescription(name);
    }
}
