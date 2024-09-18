/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.gui;

import fr.jmmc.jmal.image.ColorModels;
import fr.jmmc.jmal.image.ColorScale;
import fr.jmmc.jmal.image.ImageUtils.ImageInterpolation;
import fr.jmmc.jmcs.data.preference.PreferencesException;
import fr.jmmc.jmcs.gui.action.ActionRegistrar;
import fr.jmmc.oiexplorer.core.gui.IconComboBoxRenderer;
import fr.jmmc.oimaging.Preferences;
import fr.jmmc.oimaging.gui.action.TableEditorAction;
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Observable;
import java.util.Observer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Preferences GUI
 */
public final class PreferencePanel extends javax.swing.JPanel implements Observer {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(PreferencePanel.class.getName());
    /* members */
    /** preference singleton */
    private final Preferences myPreferences = Preferences.getInstance();

    /**
     * Creates a new PreferencePanel
     */
    public PreferencePanel() {
        initComponents();

        postInit();
    }

    /**
     * This method is useful to set the models and specific features of initialized swing components :
     * Update the combo boxes with their models
     */
    private void postInit() {
        // Set the Preferences:
        this.chartPreferencesView.setPreferences(myPreferences);

        final boolean isServerCustom = myPreferences.isServerModeCustom();
        this.jRadioButtonServerCustom.setSelected(isServerCustom);
        this.jRadioButtonServerAuto.setSelected(!isServerCustom);

        this.jTextFieldServer.setText(myPreferences.getPreference(Preferences.SERVER_CUSTOM));
        this.jTextFieldServer.setEnabled(isServerCustom);

        this.jComboBoxLUT.setModel(new DefaultComboBoxModel(ColorModels.getColorModelNames()));
        this.jComboBoxColorScale.setModel(new DefaultComboBoxModel(ColorScale.values()));
        this.jComboBoxInterpolation.setModel(new DefaultComboBoxModel(ImageInterpolation.values()));

        // register this instance as a Preference Observer :
        this.myPreferences.addObserver(this);

        // update GUI
        update(null, null);

        this.jTextFieldServer.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent e) {
                update();
            }

            @Override
            public void removeUpdate(final DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                //Plain text components do not fire these events
            }

            private void update() {
                try {
                    // will fire triggerObserversNotification so update() will be called
                    myPreferences.setPreference(Preferences.SERVER_CUSTOM, jTextFieldServer.getText());
                } catch (PreferencesException pe) {
                    logger.error("property failure : ", pe);
                }
            }
        });

        // Custom renderer for LUT:
        this.jComboBoxLUT.setRenderer(new IconComboBoxRenderer() {
            @Override
            protected Image getImage(final String name) {
                return ColorModels.getColorModelImage(name);
            }
        });

        this.jFieldTargetSep.addPropertyChangeListener("value", new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                final double sepNew = ((Number) jFieldTargetSep.getValue()).doubleValue();

                if (sepNew <= 0.0) {
                    // invalid value :
                    jFieldTargetSep.setValue(myPreferences.getPreferenceAsDouble(Preferences.TARGET_MATCHER_SEPARATION));
                }
                try {
                    // will fire triggerObserversNotification so update() will be called
                    myPreferences.setPreference(Preferences.TARGET_MATCHER_SEPARATION, Double.valueOf(((Number) jFieldTargetSep.getValue()).doubleValue()));
                } catch (PreferencesException pe) {
                    logger.error("property failure : ", pe);
                }
            }
        });
    }

    /**
     * Overriden method to give object identifier
     * @return string identifier
     */
    @Override
    public String toString() {
        return "PreferencesView@" + Integer.toHexString(hashCode());
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

        jRadioButtonGroupServer = new javax.swing.ButtonGroup();
        jLabelLutTable1 = new javax.swing.JLabel();
        jScrollPane = new javax.swing.JScrollPane();
        jPanelLayout = new javax.swing.JPanel();
        jPanelServer = new javax.swing.JPanel();
        jLabelServerMode = new javax.swing.JLabel();
        jPanelServerOptions = new javax.swing.JPanel();
        jRadioButtonServerAuto = new javax.swing.JRadioButton();
        jRadioButtonServerCustom = new javax.swing.JRadioButton();
        jLabelServerURL = new javax.swing.JLabel();
        jTextFieldServer = new javax.swing.JTextField();
        jPanelModelImage = new javax.swing.JPanel();
        jLabelLutTable = new javax.swing.JLabel();
        jComboBoxLUT = new javax.swing.JComboBox();
        jLabelColorScale = new javax.swing.JLabel();
        jComboBoxColorScale = new javax.swing.JComboBox();
        jLabelInterpolation = new javax.swing.JLabel();
        jComboBoxInterpolation = new javax.swing.JComboBox();
        jPanelTableOfResults = new javax.swing.JPanel();
        jLabelResultsTableEditor = new javax.swing.JLabel();
        jButtonResultsTableEditor = new javax.swing.JButton();
        chartPreferencesView = new fr.jmmc.oiexplorer.core.gui.ChartPreferencesView();
        jPanelMatcher = new javax.swing.JPanel();
        jLabelTargetSep = new javax.swing.JLabel();
        jFieldTargetSep = new javax.swing.JFormattedTextField();
        jPanelCommonPreferencesView = new fr.jmmc.jmcs.gui.component.CommonPreferencesView();

        jLabelLutTable1.setText("Remote server type");

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jPanelLayout.setLayout(new javax.swing.BoxLayout(jPanelLayout, javax.swing.BoxLayout.PAGE_AXIS));

        jPanelServer.setBorder(javax.swing.BorderFactory.createTitledBorder("Remote Server"));
        jPanelServer.setLayout(new java.awt.GridBagLayout());

        jLabelServerMode.setText("Host");
        jLabelServerMode.setToolTipText("All related values below this threshold will be flagged out (V2, T3...)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 6);
        jPanelServer.add(jLabelServerMode, gridBagConstraints);

        jPanelServerOptions.setLayout(new java.awt.GridBagLayout());

        jRadioButtonGroupServer.add(jRadioButtonServerAuto);
        jRadioButtonServerAuto.setText("Auto");
        jRadioButtonServerAuto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonServerActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.3;
        jPanelServerOptions.add(jRadioButtonServerAuto, gridBagConstraints);

        jRadioButtonGroupServer.add(jRadioButtonServerCustom);
        jRadioButtonServerCustom.setText("Custom");
        jRadioButtonServerCustom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonServerActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.3;
        jPanelServerOptions.add(jRadioButtonServerCustom, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        jPanelServer.add(jPanelServerOptions, gridBagConstraints);

        jLabelServerURL.setText("Server mode");
        jLabelServerURL.setToolTipText("All related values below this threshold will be flagged out (V2, T3...)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 6);
        jPanelServer.add(jLabelServerURL, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 4);
        jPanelServer.add(jTextFieldServer, gridBagConstraints);

        jPanelLayout.add(jPanelServer);

        jPanelModelImage.setBorder(javax.swing.BorderFactory.createTitledBorder("Image viewer"));
        jPanelModelImage.setLayout(new java.awt.GridBagLayout());

        jLabelLutTable.setText("LUT table");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jPanelModelImage.add(jLabelLutTable, gridBagConstraints);

        jComboBoxLUT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxLUTActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 4);
        jPanelModelImage.add(jComboBoxLUT, gridBagConstraints);

        jLabelColorScale.setText("Color scale");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jPanelModelImage.add(jLabelColorScale, gridBagConstraints);

        jComboBoxColorScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxColorScaleActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 4, 4);
        jPanelModelImage.add(jComboBoxColorScale, gridBagConstraints);

        jLabelInterpolation.setText("Interpolation");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jPanelModelImage.add(jLabelInterpolation, gridBagConstraints);

        jComboBoxInterpolation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxInterpolationActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jPanelModelImage.add(jComboBoxInterpolation, gridBagConstraints);

        jPanelLayout.add(jPanelModelImage);

        jPanelTableOfResults.setBorder(javax.swing.BorderFactory.createTitledBorder("General"));
        jPanelTableOfResults.setLayout(new java.awt.GridBagLayout());

        jLabelResultsTableEditor.setText("Results table:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        jPanelTableOfResults.add(jLabelResultsTableEditor, gridBagConstraints);

        jButtonResultsTableEditor.setText("Table editor");
        jButtonResultsTableEditor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResultsTableEditorActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.5;
        jPanelTableOfResults.add(jButtonResultsTableEditor, gridBagConstraints);

        jPanelLayout.add(jPanelTableOfResults);
        jPanelLayout.add(chartPreferencesView);

        jPanelMatcher.setBorder(javax.swing.BorderFactory.createTitledBorder("Matcher"));
        jPanelMatcher.setLayout(new java.awt.GridBagLayout());

        jLabelTargetSep.setText("Max target separation (as)");
        jLabelTargetSep.setToolTipText("Targets within this separation radius are considered the same object");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 6);
        jPanelMatcher.add(jLabelTargetSep, gridBagConstraints);

        jFieldTargetSep.setColumns(5);
        jFieldTargetSep.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0#"))));
        jFieldTargetSep.setName("jFieldMinElev"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 2);
        jPanelMatcher.add(jFieldTargetSep, gridBagConstraints);

        jPanelLayout.add(jPanelMatcher);
        jPanelLayout.add(jPanelCommonPreferencesView);

        jScrollPane.setViewportView(jPanelLayout);

        add(jScrollPane);
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxLUTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxLUTActionPerformed
        try {
            // will fire triggerObserversNotification so update() will be called
            this.myPreferences.setPreference(Preferences.MODEL_IMAGE_LUT, this.jComboBoxLUT.getSelectedItem());
        } catch (PreferencesException pe) {
            logger.error("property failure : ", pe);
        }
    }//GEN-LAST:event_jComboBoxLUTActionPerformed

    private void jComboBoxColorScaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxColorScaleActionPerformed
        try {
            // will fire triggerObserversNotification so update() will be called
            this.myPreferences.setPreference(Preferences.MODEL_IMAGE_SCALE, this.jComboBoxColorScale.getSelectedItem().toString());
        } catch (PreferencesException pe) {
            logger.error("property failure : ", pe);
        }
    }//GEN-LAST:event_jComboBoxColorScaleActionPerformed

    private void jComboBoxInterpolationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxInterpolationActionPerformed
        try {
            // will fire triggerObserversNotification so update() will be called
            this.myPreferences.setPreference(Preferences.MODEL_IMAGE_INTERPOLATION, this.jComboBoxInterpolation.getSelectedItem().toString());
        } catch (PreferencesException pe) {
            logger.error("property failure : ", pe);
        }
    }//GEN-LAST:event_jComboBoxInterpolationActionPerformed

    private void jButtonResultsTableEditorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResultsTableEditorActionPerformed
        ActionRegistrar.getInstance().get(TableEditorAction.CLASS_NAME, TableEditorAction.ACTION_NAME).actionPerformed(evt);
    }//GEN-LAST:event_jButtonResultsTableEditorActionPerformed

    private void jRadioButtonServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonServerActionPerformed
        try {
            final String value;
            if (this.jRadioButtonServerCustom.isSelected()) {
                value = Preferences.SERVER_MODE_CUSTOM;
            } else {
                value = Preferences.SERVER_MODE_AUTO;
            }
            // will fire triggerObserversNotification so update() will be called
            this.myPreferences.setPreference(Preferences.SERVER_MODE, value);
        } catch (PreferencesException pe) {
            logger.error("property failure : ", pe);
        }
        this.jTextFieldServer.setEnabled(myPreferences.isServerModeCustom());
    }//GEN-LAST:event_jRadioButtonServerActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private fr.jmmc.oiexplorer.core.gui.ChartPreferencesView chartPreferencesView;
    private javax.swing.JButton jButtonResultsTableEditor;
    private javax.swing.JComboBox jComboBoxColorScale;
    private javax.swing.JComboBox jComboBoxInterpolation;
    private javax.swing.JComboBox jComboBoxLUT;
    private javax.swing.JFormattedTextField jFieldTargetSep;
    private javax.swing.JLabel jLabelColorScale;
    private javax.swing.JLabel jLabelInterpolation;
    private javax.swing.JLabel jLabelLutTable;
    private javax.swing.JLabel jLabelLutTable1;
    private javax.swing.JLabel jLabelResultsTableEditor;
    private javax.swing.JLabel jLabelServerMode;
    private javax.swing.JLabel jLabelServerURL;
    private javax.swing.JLabel jLabelTargetSep;
    private fr.jmmc.jmcs.gui.component.CommonPreferencesView jPanelCommonPreferencesView;
    private javax.swing.JPanel jPanelLayout;
    private javax.swing.JPanel jPanelMatcher;
    private javax.swing.JPanel jPanelModelImage;
    private javax.swing.JPanel jPanelServer;
    private javax.swing.JPanel jPanelServerOptions;
    private javax.swing.JPanel jPanelTableOfResults;
    private javax.swing.ButtonGroup jRadioButtonGroupServer;
    private javax.swing.JRadioButton jRadioButtonServerAuto;
    private javax.swing.JRadioButton jRadioButtonServerCustom;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JTextField jTextFieldServer;
    // End of variables declaration//GEN-END:variables

    /**
     * Listen to preferences changes
     * @param o Preferences
     * @param arg unused
     */
    @Override
    public void update(final Observable o, final Object arg) {
        logger.debug("Preferences updated on : {}", this);

        // Image viewer:
        this.jComboBoxLUT.setSelectedItem(this.myPreferences.getPreference(Preferences.MODEL_IMAGE_LUT));
        this.jComboBoxColorScale.setSelectedItem(this.myPreferences.getImageColorScale());
        this.jComboBoxInterpolation.setSelectedItem(this.myPreferences.getImageInterpolation());

        // read prefs to set states of GUI elements
        this.jFieldTargetSep.setValue(this.myPreferences.getPreferenceAsDouble(Preferences.TARGET_MATCHER_SEPARATION));
    }
}
