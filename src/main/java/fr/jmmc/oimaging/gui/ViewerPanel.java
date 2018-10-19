/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.gui;

import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.jmcs.gui.FeedbackReport;
import fr.jmmc.jmcs.gui.action.ActionRegistrar;
import fr.jmmc.jmcs.gui.component.FileChooser;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.util.FileUtils;
import fr.jmmc.jmcs.util.StringUtils;
import fr.jmmc.oiexplorer.core.gui.FitsImagePanel;
import fr.jmmc.oiexplorer.core.gui.model.KeywordsTableModel;
import fr.jmmc.oiexplorer.core.util.FitsImageUtils;
import fr.jmmc.oimaging.Preferences;
import fr.jmmc.oimaging.gui.action.ExportFitsImageAction;
import fr.jmmc.oimaging.gui.action.ExportOIFitsAction;
import fr.jmmc.oimaging.interop.SendFitsAction;
import fr.jmmc.oimaging.interop.SendOIFitsAction;
import fr.jmmc.oimaging.model.IRModel;
import fr.jmmc.oimaging.services.ServiceResult;
import fr.jmmc.oitools.image.FitsImage;
import fr.jmmc.oitools.image.FitsImageFile;
import fr.jmmc.oitools.image.FitsImageHDU;
import fr.jmmc.oitools.image.FitsImageWriter;
import fr.jmmc.oitools.image.ImageOiData;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsWriter;
import fr.jmmc.oitools.processing.Resampler;
import fr.nom.tam.fits.FitsException;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main tab pane that displays Image, OIdata, Parameters or Log Reports.
 * @author mellag
 */
public class ViewerPanel extends javax.swing.JPanel implements ChangeListener {

    private static final long serialVersionUID = 1L;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(ViewerPanel.class);
    /** fits extension including '.' (dot) character ie '.fits' */
    public final static String FITS_EXTENSION = "." + MimeType.OIFITS.getExtension();

    /** Fits image panel */
    private final FitsImagePanel fitsImagePanel;

    /** OIFits viewer panel */
    private final OIFitsViewPanel oifitsViewPanel;

    private final Action exportOiFitsAction;
    private final Action sendOiFitsAction;
    private final Action exportFitsImageAction;
    private final Action sendFitsAction;
    private Component lastModelPanel;
    private Component lastResultPanel;

    /** Flag set to true while the GUI is being updated by model else false. */
    private boolean syncingUI = false;

    private enum SHOW_MODE {
        MODEL,
        RESULT;
    }
    private SHOW_MODE showMode;

    /** Creates new form ViewerPanel */
    public ViewerPanel() {
        initComponents();

        // Fix row height:
        SwingUtils.adjustRowHeight(jTableOutputParamKeywords);
        SwingUtils.adjustRowHeight(jTableInputParamKeywords);

        jTabbedPaneVizualizations.addChangeListener(this);

        fitsImagePanel = new FitsImagePanel(Preferences.getInstance(), true, true, null);
        jPanelImage.add(fitsImagePanel);

        oifitsViewPanel = new OIFitsViewPanel();
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.weighty = 1.0;
        jPanelOIFitsViewer.add(oifitsViewPanel, gridBagConstraints);

        exportOiFitsAction = ActionRegistrar.getInstance().get(ExportOIFitsAction.className, ExportOIFitsAction.actionName);
        sendOiFitsAction = ActionRegistrar.getInstance().get(SendOIFitsAction.className, SendOIFitsAction.actionName);
        exportFitsImageAction = ActionRegistrar.getInstance().get(ExportFitsImageAction.className, ExportFitsImageAction.actionName);
        sendFitsAction = ActionRegistrar.getInstance().get(SendFitsAction.className, SendFitsAction.actionName);

        jComboBoxImage.setRenderer(new OiCellRenderer());
    }

    private void displayExecutionLog() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void displayImage(List<FitsImageHDU> imageHdus, FitsImageHDU imageHDU) {
        // Todo Build selector

        // image combo
        jComboBoxImage.removeAllItems();

        if (imageHdus != null) {
            for (FitsImageHDU fitsImageHDU : imageHdus) {
                if (fitsImageHDU.hasImages()) {
                    jComboBoxImage.addItem(fitsImageHDU);
                }
            }
        }

        if (jComboBoxImage.getItemCount() != 0) {
            if (imageHDU != null && imageHDU.hasImages()) {
                jComboBoxImage.setSelectedItem(imageHDU);
                displaySelection(imageHDU);
            }
        } else {
            logger.debug("Remove image panel");
            jPanelImage.remove(fitsImagePanel);
        }
    }

    private void displaySelection(final FitsImageHDU imageHDU) {
        if (imageHDU != null) {
            FitsImage image = imageHDU.getFitsImages().get(0);
            FitsImageUtils.updateDataRangeExcludingZero(image);
            fitsImagePanel.setFitsImage(image);
            jPanelImage.add(fitsImagePanel);
            logger.info("Display image HDU '{}'", imageHDU.getHduName());
        }
    }

    // Display Oifits and Params
    private void displayOiFitsAndParams(OIFitsFile oifitsFile, String targetName) {
        if (oifitsFile != null) {
            oifitsViewPanel.plot(oifitsFile, targetName);
            jPanelOIFits.add(oifitsViewPanel);
            final ImageOiData imageOiData = oifitsFile.getImageOiData();
            // init Param Tables
            ((KeywordsTableModel) jTableOutputParamKeywords.getModel()).setFitsHdu(imageOiData.getOutputParam());
            ((KeywordsTableModel) jTableInputParamKeywords.getModel()).setFitsHdu(imageOiData.getInputParam());
        } else {
            jPanelOIFits.remove(oifitsViewPanel);
            // reset Param Tables
            ((KeywordsTableModel) jTableOutputParamKeywords.getModel()).setFitsHdu(null);
            ((KeywordsTableModel) jTableInputParamKeywords.getModel()).setFitsHdu(null);
        }
    }

    private void setTabMode(SHOW_MODE mode) {
        syncingUI = true;

        // change border title
        if (mode.equals(SHOW_MODE.MODEL)) {
            setBorder(javax.swing.BorderFactory.createTitledBorder("Data Visualisation (INPUT)"));
        } else {
            setBorder(javax.swing.BorderFactory.createTitledBorder("Data Visualisation (RESULT)"));
        }

        // switch tab arrangement only if we switch between model display or result display
        if ((mode.equals(SHOW_MODE.MODEL) && (jTabbedPaneVizualizations.getComponentCount() > 3))
                || (mode.equals(SHOW_MODE.RESULT) && (jTabbedPaneVizualizations.getComponentCount() == 3))) {
            jTabbedPaneVizualizations.removeAll();
            jTabbedPaneVizualizations.add("Image", jPanelImageViewer);
            jTabbedPaneVizualizations.add("OIFits", jPanelOIFitsViewer);
            jTabbedPaneVizualizations.add("Parameters", jPanelOutputParamViewer);
            if (mode.equals(SHOW_MODE.RESULT)) {
                jTabbedPaneVizualizations.add("Execution log", jPanelLogViewer);
            }
        }
        syncingUI = false;

        enableActions();
        restoreLastShownPanel();
    }

    public void displayModel(IRModel irModel) {
        showMode = SHOW_MODE.MODEL;
        if (irModel != null) {
            displayOiFitsAndParams(irModel.getOifitsFile(), irModel.getImageOiData().getInputParam().getTarget());
            displayImage(irModel.getFitsImageHDUs(), irModel.getSelectedInputImageHDU());
        }
        setTabMode(SHOW_MODE.MODEL);
    }

    public void displayResult(ServiceResult result) {
        showMode = SHOW_MODE.RESULT;

        OIFitsFile oifitsFile = null;
        String target = null;
        List<FitsImageHDU> imageHdus = null;
        FitsImageHDU imageHduToShow = null;

        if (result != null) {
            // execution log
            jEditorPaneExecutionLog.setText(result.getExecutionLog());

            try {
                oifitsFile = result.getOifitsFile();

                // TODO have a look in the ouput param to look at right image ?
                // show first one :
                imageHdus = oifitsFile.getFitsImageHDUs();
                imageHduToShow = imageHdus.isEmpty() ? null : imageHdus.get(0);
                target = oifitsFile.getImageOiData().getInputParam().getTarget();

            } catch (IOException ex) {
                logger.error("Can't retrieve result oifile", ex);
            } catch (FitsException ex) {
                logger.error("Can't retrieve result oifile", ex);
            }

            displayImage(imageHdus, imageHduToShow);
            displayOiFitsAndParams(oifitsFile, target);

            if (oifitsFile == null) {
                lastResultPanel = jPanelLogViewer;
            }

            setTabMode(SHOW_MODE.RESULT);
        }
    }

    // TODO move out of this class
    public File exportOIFits(final boolean useFileChooser) {
        final OIFitsFile oifitsFile = oifitsViewPanel.getOIFitsData();
        // store original filename
        final String originalAbsoluteFilePath = oifitsFile.getAbsoluteFilePath();
        final File file;

        if (useFileChooser) {
            final File dir = FileUtils.getDirectory(oifitsFile.getAbsoluteFilePath());
            String name = oifitsFile.getFileName();

            if (!name.contains(".image-oi")) {
                name = FileUtils.getFileNameWithoutExtension(name) + ".image-oi." + FileUtils.getExtension(oifitsFile.getFileName());
            }

            file = FileChooser.showSaveFileChooser("Choose destination to write the OIFits file", dir, MimeType.OIFITS, name);
        } else {
            file = FileUtils.getTempFile(oifitsFile.getFileName(), FITS_EXTENSION);
        }

        // Cancel
        if (file == null) {
            return null;
        }

        try {
            OIFitsWriter.writeOIFits(file.getAbsolutePath(), oifitsFile);
        } catch (IOException ex) {
            // Show the feedback report (modal) :
            FeedbackReport.openDialog(true, ex);
        } catch (FitsException ex) {
            // Show the feedback report (modal) :
            FeedbackReport.openDialog(true, ex);
        } finally {
            if (!useFileChooser) {
                // restore filename
                oifitsFile.setAbsoluteFilePath(originalAbsoluteFilePath);
            }
        }
        return file;
    }

    // TODO move out of this class
    public File exportFitsImage(final boolean useFileChooser) {
        final FitsImage fitsImage = fitsImagePanel.getFitsImage();
        // store original identifier
        final String originalImageIdentifier = fitsImage.getFitsImageIdentifier();
        final File file;

        if (useFileChooser) {
            file = FileChooser.showSaveFileChooser("Choose destination to write the Fits image file", null, MimeType.FITS_IMAGE, null);
        } else {
            file = FileUtils.getTempFile(StringUtils.replaceNonAlphaNumericCharsByUnderscore(fitsImage.getFitsImageIdentifier()), FITS_EXTENSION);
        }

        // Cancel
        if (file == null) {
            return null;
        }

        try {
            // export whole HDU (even if first image is shown) : image has been modified ( and is not the verbatim one).
            final FitsImageFile fits = new FitsImageFile();
            fits.getFitsImageHDUs().add(fitsImage.getFitsImageHDU());

            FitsImageWriter.write(file.getAbsolutePath(), fits);
        } catch (IOException ex) {
            // Show the feedback report (modal) :
            FeedbackReport.openDialog(true, ex);
        } catch (FitsException ex) {
            // Show the feedback report (modal) :
            FeedbackReport.openDialog(true, ex);
        } finally {
            // restore identifier
            fitsImage.setFitsImageIdentifier(originalImageIdentifier);
        }
        return file;
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

        jTabbedPaneVizualizations = new javax.swing.JTabbedPane();
        jPanelOIFitsViewer = new javax.swing.JPanel();
        jPanelOIFits = new javax.swing.JPanel();
        jPanelImageViewer = new javax.swing.JPanel();
        jPanelImageSelector = new javax.swing.JPanel();
        jComboBoxImage = new javax.swing.JComboBox();
        jPanelImage = new javax.swing.JPanel();
        jButtonResample = new javax.swing.JButton();
        jPanelLogViewer = new javax.swing.JPanel();
        jScrollPaneLog = new javax.swing.JScrollPane();
        jEditorPaneExecutionLog = new javax.swing.JEditorPane();
        jPanelOutputParamViewer = new javax.swing.JPanel();
        jLabelOutput = new javax.swing.JLabel();
        jScrollPaneTableOutput = new javax.swing.JScrollPane();
        jTableOutputParamKeywords = new javax.swing.JTable();
        jLabelInput = new javax.swing.JLabel();
        jScrollPaneTableInput = new javax.swing.JScrollPane();
        jTableInputParamKeywords = new javax.swing.JTable();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Data Visualisation"));
        setLayout(new java.awt.GridBagLayout());

        jPanelOIFitsViewer.setLayout(new java.awt.GridBagLayout());

        jPanelOIFits.setLayout(new javax.swing.BoxLayout(jPanelOIFits, javax.swing.BoxLayout.LINE_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        jPanelOIFitsViewer.add(jPanelOIFits, gridBagConstraints);

        jTabbedPaneVizualizations.addTab("OIFits data", jPanelOIFitsViewer);

        jPanelImageViewer.setLayout(new java.awt.GridBagLayout());

        jPanelImageSelector.setLayout(new java.awt.GridBagLayout());

        jComboBoxImage.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxImageActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        jPanelImageSelector.add(jComboBoxImage, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        jPanelImageViewer.add(jPanelImageSelector, gridBagConstraints);

        jPanelImage.setLayout(new javax.swing.BoxLayout(jPanelImage, javax.swing.BoxLayout.LINE_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.weighty = 1.0;
        jPanelImageViewer.add(jPanelImage, gridBagConstraints);

        jButtonResample.setText("resample to 100x100");
        jButtonResample.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResampleActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelImageViewer.add(jButtonResample, gridBagConstraints);

        jTabbedPaneVizualizations.addTab("Images", jPanelImageViewer);

        jPanelLogViewer.setLayout(new javax.swing.BoxLayout(jPanelLogViewer, javax.swing.BoxLayout.LINE_AXIS));

        jEditorPaneExecutionLog.setEditable(false);
        jScrollPaneLog.setViewportView(jEditorPaneExecutionLog);

        jPanelLogViewer.add(jScrollPaneLog);

        jTabbedPaneVizualizations.addTab("Execution log", jPanelLogViewer);

        jPanelOutputParamViewer.setLayout(new java.awt.GridBagLayout());

        jLabelOutput.setText("Output");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelOutputParamViewer.add(jLabelOutput, gridBagConstraints);

        jTableOutputParamKeywords.setModel(new KeywordsTableModel());
        jScrollPaneTableOutput.setViewportView(jTableOutputParamKeywords);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.1;
        jPanelOutputParamViewer.add(jScrollPaneTableOutput, gridBagConstraints);

        jLabelInput.setText("Input");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelOutputParamViewer.add(jLabelInput, gridBagConstraints);

        jTableInputParamKeywords.setModel(new KeywordsTableModel());
        jScrollPaneTableInput.setViewportView(jTableInputParamKeywords);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.8;
        jPanelOutputParamViewer.add(jScrollPaneTableInput, gridBagConstraints);

        jTabbedPaneVizualizations.addTab("Parameters", jPanelOutputParamViewer);

        jTabbedPaneVizualizations.setSelectedIndex(1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jTabbedPaneVizualizations, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxImageActionPerformed
        displaySelection((FitsImageHDU) jComboBoxImage.getSelectedItem());
    }//GEN-LAST:event_jComboBoxImageActionPerformed

    private void jButtonResampleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResampleActionPerformed
        final FitsImage fitsImage = fitsImagePanel.getFitsImage();
        if (fitsImage != null) {
            final FitsImageHDU imageHDU = fitsImage.getFitsImageHDU();
            final int newSize = 100;
// TODO: select filter        
//        final int filterType = Resampler.FILTER_MITCHELL;
//        final int filterType = Resampler.FILTER_BOX;
            FitsImageUtils.resampleImages(imageHDU, newSize);

            displaySelection(imageHDU);
        }
    }//GEN-LAST:event_jButtonResampleActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonResample;
    private javax.swing.JComboBox jComboBoxImage;
    private javax.swing.JEditorPane jEditorPaneExecutionLog;
    private javax.swing.JLabel jLabelInput;
    private javax.swing.JLabel jLabelOutput;
    private javax.swing.JPanel jPanelImage;
    private javax.swing.JPanel jPanelImageSelector;
    private javax.swing.JPanel jPanelImageViewer;
    private javax.swing.JPanel jPanelLogViewer;
    private javax.swing.JPanel jPanelOIFits;
    private javax.swing.JPanel jPanelOIFitsViewer;
    private javax.swing.JPanel jPanelOutputParamViewer;
    private javax.swing.JScrollPane jScrollPaneLog;
    private javax.swing.JScrollPane jScrollPaneTableInput;
    private javax.swing.JScrollPane jScrollPaneTableOutput;
    private javax.swing.JTabbedPane jTabbedPaneVizualizations;
    private javax.swing.JTable jTableInputParamKeywords;
    private javax.swing.JTable jTableOutputParamKeywords;
    // End of variables declaration//GEN-END:variables

    @Override
    public void stateChanged(ChangeEvent e) {
        if (syncingUI) {
            return;
        }

        enableActions();
        storeLastPanel();
    }

    private void enableActions() {
        exportOiFitsAction.setEnabled(jTabbedPaneVizualizations.getSelectedComponent() == jPanelOIFitsViewer && oifitsViewPanel.getOIFitsData() != null);
        sendOiFitsAction.setEnabled(exportOiFitsAction.isEnabled());
        exportFitsImageAction.setEnabled(jTabbedPaneVizualizations.getSelectedComponent() == jPanelImageViewer && fitsImagePanel.getFitsImage() != null);
        sendFitsAction.setEnabled(exportFitsImageAction.isEnabled());
    }

    private void storeLastPanel() {
        if (syncingUI || jTabbedPaneVizualizations.getSelectedComponent() == null) {
            return;
        }

        if (showMode == SHOW_MODE.MODEL) {
            lastModelPanel = jTabbedPaneVizualizations.getSelectedComponent();
        } else {
            lastResultPanel = jTabbedPaneVizualizations.getSelectedComponent();
        }
    }

    private void restoreLastShownPanel() {
        // last panel refs may be null during program startup...
        if (showMode == SHOW_MODE.MODEL && lastModelPanel != null) {
            jTabbedPaneVizualizations.setSelectedComponent(lastModelPanel);
        } else if (showMode == SHOW_MODE.RESULT & lastResultPanel != null) {
            jTabbedPaneVizualizations.setSelectedComponent(lastResultPanel);
        }
    }
}
