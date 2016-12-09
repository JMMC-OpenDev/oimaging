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

    private FitsImageHDU fitsImageHDU = null;

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

        FitsImage image = imageHDU.getFitsImages().get(0);

        fitsImageHDU = imageHDU;

        if (image != null) {
            FitsImageUtils.updateDataRangeExcludingZero(image);
            fitsImagePanel.setFitsImage(image);
            jPanelImage.add(fitsImagePanel);
            //jTabbedPaneVizualizations.setSelectedComponent(jPanelImageViewer);
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
        }
    }

    private void setTabMode(boolean modelMode) {
        // move to
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

    public void displayModel(IRModel currentModel) {
        if (currentModel == null) {
        }

        //viewerPanel.displayOiFits(irModel.getOifitsFile(), (String) jComboBoxTarget.getSelectedItem());
        //viewerPanel.displayImage(selectedFitsImageHDU.getFitsImages().get(0), currentModel.getOifitsFile());
        setTabMode(true);
    }

    public void displayResult(ServiceResult result) {
        // execution log
        jEditorPaneExecutionLog.setText(result.getExecutionLog());

        final OIFitsFile oifitsFile;
        try {
            oifitsFile = result.getOifitsFile();

            //image
            // show first one :
            displayImage(oifitsFile.getImageOiData().getFitsImageHDUs().get(0));

            displayOiFits(oifitsFile, oifitsFile.getImageOiData().getInputParam().getTarget());

            // get Output Param Table
            // TODO
        } catch (IOException ex) {
            Logger.getLogger(ViewerPanel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FitsException ex) {
            Logger.getLogger(ViewerPanel.class.getName()).log(Level.SEVERE, null, ex);
        }

        setTabMode(false);
    }

    void selectImageViewer() {
        jTabbedPaneVizualizations.setSelectedComponent(jPanelImageViewer);
    }

    public void exportOIFits() {
        OIFitsFile oifitsFile = fitsViewPanel.getOIFitsData();
        File dir = FileUtils.getDirectory(oifitsFile.getAbsoluteFilePath());
        String name = FileUtils.getFileNameWithoutExtension(oifitsFile.getName()) + ".image-oi." + FileUtils.getExtension(oifitsFile.getName());
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

        FitsImageFile fits = new FitsImageFile();
        fits.getFitsImageHDUs().add(fitsImageHDU);

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
        exportFitsImageAction.setEnabled(jTabbedPaneVizualizations.getSelectedComponent() == jPanelImageViewer);
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
        jPanelImageViewer = new javax.swing.JPanel();
        jPanelImage = new javax.swing.JPanel();
        jPanelLogViewer = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPaneExecutionLog = new javax.swing.JEditorPane();
        jPanelOutputParamViewer = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Data Visualisation"));
        setLayout(new java.awt.GridBagLayout());

        jPanelOIFitsViewer.setLayout(new java.awt.GridBagLayout());
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

        jPanelOutputParamViewer.setLayout(new javax.swing.BoxLayout(jPanelOutputParamViewer, javax.swing.BoxLayout.LINE_AXIS));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"to", "do", "ASAP!"}
            },
            new String [] {
                "Keyword name", "Value", "Comment"
            }
        ));
        jScrollPane2.setViewportView(jTable1);

        jPanelOutputParamViewer.add(jScrollPane2);

        jTabbedPaneVizualizations.addTab("Output parameters", jPanelOutputParamViewer);

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
    private javax.swing.JPanel jPanelImage;
    private javax.swing.JPanel jPanelImageViewer;
    private javax.swing.JPanel jPanelLogViewer;
    private javax.swing.JPanel jPanelOIFitsViewer;
    private javax.swing.JPanel jPanelOutputParamViewer;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPaneVizualizations;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void stateChanged(ChangeEvent e) {
        enableActions();
    }
}
