/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.gui;

import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.jmcs.gui.FeedbackReport;
import fr.jmmc.jmcs.gui.action.ActionRegistrar;
import fr.jmmc.jmcs.gui.component.FileChooser;
import fr.jmmc.jmcs.util.FileUtils;
import fr.jmmc.oiexplorer.core.gui.FitsImagePanel;
import fr.jmmc.oiexplorer.core.util.FitsImageUtils;
import fr.jmmc.oimaging.Preferences;
import fr.jmmc.oimaging.gui.action.ExportFitsImageAction;
import fr.jmmc.oimaging.gui.action.ExportOIFitsAction;
import fr.jmmc.oimaging.gui.model.KeywordsTableModel;
import fr.jmmc.oimaging.model.IRModel;
import fr.jmmc.oimaging.services.ServiceResult;
import fr.jmmc.oitools.image.FitsImage;
import fr.jmmc.oitools.image.FitsImageFile;
import fr.jmmc.oitools.image.FitsImageHDU;
import fr.jmmc.oitools.image.FitsImageWriter;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsWriter;
import fr.nom.tam.fits.FitsException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author mellag
 */
public class ViewerPanel extends javax.swing.JPanel implements ChangeListener {

    /** Fits image panel */
    private FitsImagePanel fitsImagePanel;

    /** OIFits viewer panel */
    private OIFitsViewPanel fitsViewPanel;

    private Action exportOiFitsAction;
    private Action exportFitsImageAction;

    /** Creates new form ViewerPanel */
    public ViewerPanel() {
        initComponents();

        jTabbedPaneVizualizations.addChangeListener(this);

        fitsImagePanel = new FitsImagePanel(Preferences.getInstance(), true, true, null);
        jPanelImage.add(fitsImagePanel);

        fitsViewPanel = new OIFitsViewPanel();
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.weighty = 1.0;
        jPanelOIFitsViewer.add(fitsViewPanel, gridBagConstraints);

        exportOiFitsAction = ActionRegistrar.getInstance().get(ExportOIFitsAction.className, ExportOIFitsAction.actionName);
        exportFitsImageAction = ActionRegistrar.getInstance().get(ExportFitsImageAction.className, ExportFitsImageAction.actionName);
    }

    private void displayImage(FitsImageHDU imageHDU) {
        if (imageHDU != null && !imageHDU.getFitsImages().isEmpty()) {
            FitsImage image = imageHDU.getFitsImages().get(0);
            FitsImageUtils.updateDataRangeExcludingZero(image);
            fitsImagePanel.setFitsImage(image);
            jPanelImage.add(fitsImagePanel);
        } else {
            jPanelImage.remove(fitsImagePanel);
        }
    }

    private void displayOiFits(OIFitsFile oifitsFile, String targetName) {
        if (oifitsFile != null) {
            fitsViewPanel.plot(oifitsFile, targetName);
            if (jPanelImage.getComponentCount() == 0) {
                jTabbedPaneVizualizations.setSelectedComponent(jPanelOIFitsViewer);
            }
            jPanelOIFits.add(fitsViewPanel);
        } else {
            jPanelOIFits.remove(fitsViewPanel);
        }
    }

    private void setTabMode(boolean modelMode) {
        if ((modelMode && (jTabbedPaneVizualizations.getComponentCount() > 2))
                || (!modelMode && (jTabbedPaneVizualizations.getComponentCount() == 2))) {
            jTabbedPaneVizualizations.removeAll();
            jTabbedPaneVizualizations.add("Image", jPanelImageViewer);
            jTabbedPaneVizualizations.add("OIFits data", jPanelOIFitsViewer);
            if (!modelMode) {
                jTabbedPaneVizualizations.add("Output parameters", jPanelOutputParamViewer);
                jTabbedPaneVizualizations.add("Execution log", jPanelLogViewer);
            }
        }
        enableActions();
    }

    public void displayModel(IRModel irModel) {
        if (irModel != null && irModel.getOifitsFile() != null) {
            displayOiFits(irModel.getOifitsFile(), irModel.getImageOiData().getInputParam().getTarget());
            displayImage(irModel.getSelectedInputImageHDU());
            if (irModel.getSelectedInputImageHDU() == null) {
                jTabbedPaneVizualizations.setSelectedComponent(jPanelOIFitsViewer);
            }
        }

        setTabMode(true);
    }

    public void displayResult(ServiceResult result) {
        FitsImageHDU imageHdu = null;
        OIFitsFile oifitsFile = null;
        String target = null;

        if (result != null) {
            // execution log
            jEditorPaneExecutionLog.setText(result.getExecutionLog());

            try {
                oifitsFile = result.getOifitsFile();

                // TODO have a look in the ouput param to look at right image ?
                // show first one :
                List<FitsImageHDU> imageHdus = oifitsFile.getImageOiData().getFitsImageHDUs();
                imageHdu = imageHdus.isEmpty() ? null : imageHdus.get(0);
                target = oifitsFile.getImageOiData().getInputParam().getTarget();

                // get Output Param Table
                jTableOutpuParametersKeywords.setModel(new KeywordsTableModel(oifitsFile.getImageOiData().getOutputParam()));
                jTableInpuParametersKeywords.setModel(new KeywordsTableModel(oifitsFile.getImageOiData().getInputParam()));

            } catch (IOException ex) {
                Logger.getLogger(ViewerPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FitsException ex) {
                Logger.getLogger(ViewerPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        displayImage(imageHdu);
        displayOiFits(oifitsFile, target);

        setTabMode(false);
        if (imageHdu == null && oifitsFile == null) {
            jTabbedPaneVizualizations.setSelectedComponent(jPanelLogViewer);
        }
    }

    void selectImageViewer() {
        jTabbedPaneVizualizations.setSelectedComponent(jPanelImageViewer);
    }

    public void exportOIFits() {
        OIFitsFile oifitsFile = fitsViewPanel.getOIFitsData();
        File dir = FileUtils.getDirectory(oifitsFile.getAbsoluteFilePath());
        String name = oifitsFile.getName();

        if (!name.contains(".image-oi")) {
            name = FileUtils.getFileNameWithoutExtension(name) + ".image-oi." + FileUtils.getExtension(oifitsFile.getName());
        }

        File file = FileChooser.showSaveFileChooser("Choose destination to write the OIFits file", dir, MimeType.OIFITS, name);

        // Cancel
        if (file == null) {
            return;
        }

        try {
            OIFitsWriter.writeOIFits(file.getAbsolutePath(), oifitsFile);
        } catch (IOException ex) {
            // Show the feedback report (modal) :
            FeedbackReport.openDialog(true, ex);
        } catch (FitsException ex) {
            // Show the feedback report (modal) :
            FeedbackReport.openDialog(true, ex);
        }
    }

    public void exportFitsImage() {
        // export whole HDU (even if first image is shown) : image has been modified ( and is not the verbatim one).
        FitsImageFile fits = new FitsImageFile();
        fits.getFitsImageHDUs().add(fitsImagePanel.getFitsImage().getFitsImageHDU());

        File file = FileChooser.showSaveFileChooser("Choose destination to write the OIFits file", null, MimeType.FITS_IMAGE, null);

        // Cancel
        if (file == null) {
            return;
        }
        try {
            FitsImageWriter.write(file.getAbsolutePath(), fits);
        } catch (IOException ex) {
            // Show the feedback report (modal) :
            FeedbackReport.openDialog(true, ex);
        } catch (FitsException ex) {
            // Show the feedback report (modal) :
            FeedbackReport.openDialog(true, ex);
        }

    }

    private void enableActions() {
        exportOiFitsAction.setEnabled(jTabbedPaneVizualizations.getSelectedComponent() == jPanelOIFitsViewer && fitsViewPanel.getOIFitsData() != null);
        exportFitsImageAction.setEnabled(jTabbedPaneVizualizations.getSelectedComponent() == jPanelImageViewer && fitsImagePanel.getFitsImage() != null);
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
        jPanelImage = new javax.swing.JPanel();
        jPanelLogViewer = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPaneExecutionLog = new javax.swing.JEditorPane();
        jPanelOutputParamViewer = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTableOutpuParametersKeywords = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTableInpuParametersKeywords = new javax.swing.JTable();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Data Visualisation"));
        setLayout(new java.awt.GridBagLayout());

        jPanelOIFitsViewer.setLayout(new java.awt.GridBagLayout());

        jPanelOIFits.setLayout(new javax.swing.BoxLayout(jPanelOIFits, javax.swing.BoxLayout.LINE_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        jPanelOIFitsViewer.add(jPanelOIFits, gridBagConstraints);

        jTabbedPaneVizualizations.addTab("OIFits data", jPanelOIFitsViewer);

        jPanelImageViewer.setLayout(new java.awt.GridBagLayout());

        jPanelImage.setLayout(new javax.swing.BoxLayout(jPanelImage, javax.swing.BoxLayout.LINE_AXIS));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.weighty = 1.0;
        jPanelImageViewer.add(jPanelImage, gridBagConstraints);

        jTabbedPaneVizualizations.addTab("Images", jPanelImageViewer);

        jPanelLogViewer.setLayout(new javax.swing.BoxLayout(jPanelLogViewer, javax.swing.BoxLayout.LINE_AXIS));

        jEditorPaneExecutionLog.setEditable(false);
        jScrollPane1.setViewportView(jEditorPaneExecutionLog);

        jPanelLogViewer.add(jScrollPane1);

        jTabbedPaneVizualizations.addTab("Execution log", jPanelLogViewer);

        jPanelOutputParamViewer.setLayout(new java.awt.GridBagLayout());

        jLabel2.setText("Output");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanelOutputParamViewer.add(jLabel2, gridBagConstraints);

        jTableOutpuParametersKeywords.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Keyword name", "Value", "Comment"
            }
        ));
        jScrollPane2.setViewportView(jTableOutpuParametersKeywords);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        jPanelOutputParamViewer.add(jScrollPane2, gridBagConstraints);

        jLabel1.setText("Input");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanelOutputParamViewer.add(jLabel1, gridBagConstraints);

        jTableInpuParametersKeywords.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Keyword name", "Value", "Comment"
            }
        ));
        jScrollPane3.setViewportView(jTableInpuParametersKeywords);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        jPanelOutputParamViewer.add(jScrollPane3, gridBagConstraints);

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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane jEditorPaneExecutionLog;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanelImage;
    private javax.swing.JPanel jPanelImageViewer;
    private javax.swing.JPanel jPanelLogViewer;
    private javax.swing.JPanel jPanelOIFits;
    private javax.swing.JPanel jPanelOIFitsViewer;
    private javax.swing.JPanel jPanelOutputParamViewer;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPaneVizualizations;
    private javax.swing.JTable jTableInpuParametersKeywords;
    private javax.swing.JTable jTableOutpuParametersKeywords;
    // End of variables declaration//GEN-END:variables

    @Override
    public void stateChanged(ChangeEvent e) {
        enableActions();
    }
}
