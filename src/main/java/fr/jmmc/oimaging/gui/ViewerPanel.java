/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.gui;

import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.jmcs.gui.FeedbackReport;
import fr.jmmc.jmcs.gui.action.ActionRegistrar;
import fr.jmmc.jmcs.gui.component.FileChooser;
import fr.jmmc.jmcs.gui.util.AutofitTableColumns;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.util.FileUtils;
import fr.jmmc.jmcs.util.StringUtils;
import fr.jmmc.oiexplorer.core.gui.FitsImagePanel;
import fr.jmmc.oiexplorer.core.gui.SliderPanel;
import fr.jmmc.oiexplorer.core.gui.model.KeywordsTableModel;
import fr.jmmc.oimaging.Preferences;
import fr.jmmc.oimaging.gui.action.ExportFitsImageAction;
import fr.jmmc.oimaging.gui.action.ExportOIFitsAction;
import fr.jmmc.oimaging.interop.SendFitsAction;
import fr.jmmc.oimaging.interop.SendOIFitsAction;
import fr.jmmc.oimaging.model.IRModel;
import fr.jmmc.oimaging.model.IRModelManager;
import fr.jmmc.oimaging.services.ServiceResult;
import fr.jmmc.oitools.image.FitsImage;
import fr.jmmc.oitools.image.FitsImageFile;
import fr.jmmc.oitools.image.FitsImageHDU;
import fr.jmmc.oitools.image.FitsImageWriter;
import static fr.jmmc.oitools.image.ImageOiConstants.KEYWORD_INIT_IMG;
import static fr.jmmc.oitools.image.ImageOiConstants.KEYWORD_RGL_PRIO;
import fr.jmmc.oitools.image.ImageOiData;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsWriter;
import fr.nom.tam.fits.FitsException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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

    /** flag to show debug information (label) */
    private final static boolean SHOW_DEBUG_INFO = false;
    /** flag to trace display calls */
    private final static AtomicInteger DEBUG_N_FRAME = (SHOW_DEBUG_INFO) ? new AtomicInteger() : null;

    /** flag to trace display calls */
    private final static boolean TRACE_DISPLAY = false;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(ViewerPanel.class);
    /** fits extension including '.' (dot) character ie '.fits' */
    public final static String FITS_EXTENSION = "." + MimeType.OIFITS.getExtension();

    /** Fits image panel */
    private final FitsImagePanel fitsImagePanel;

    /** OIFits viewer panel */
    private final OIFitsViewPanel oifitsViewPanel;

    /** Slider panel */
    private final SliderPanel sliderPanel;

    private final Action exportOiFitsAction;
    private final Action sendOiFitsAction;
    private final Action exportFitsImageAction;
    private final Action sendFitsAction;
    private Component lastModelPanel;
    private Component lastResultPanel;
    private Component lastGridPanel;

    /** Flag set to true while the GUI is being updated by model else false. */
    private boolean syncingUI = false;

    public enum SHOW_MODE {
        MODEL,
        RESULT,
        GRID;
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

        sliderPanel = new SliderPanel(fitsImagePanel);
        fitsImagePanel.addOptionPanel(sliderPanel);

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

        jLabelImageDebug.setVisible(SHOW_DEBUG_INFO);
    }

    private void displayImage(List<FitsImageHDU> imageHdus, FitsImageHDU imageHDU) {
        syncingUI = true;
        try {
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
        } finally {
            syncingUI = false;
        }
    }

    private void displaySelection(final FitsImageHDU imageHDU) {
        if (TRACE_DISPLAY) {
            logger.info("displaySelection: {}", imageHDU, new Throwable());
        }
        if (SHOW_DEBUG_INFO) {
            final int frame = DEBUG_N_FRAME.getAndIncrement();
            jLabelImageDebug.setText("Frame: " + frame);
        }
        if (imageHDU != null) {
            sliderPanel.setVisible(false);
            if (imageHDU.getImageCount() > 1) {
                sliderPanel.setFitsImages(imageHDU.getFitsImages());
                sliderPanel.setVisible(true);
            }
            FitsImage image = imageHDU.getFitsImages().get(0);
            fitsImagePanel.setFitsImage(image);
            jPanelImage.add(fitsImagePanel);
            logger.debug("Display image HDU '{}'", imageHDU.getHduName());
        } else {
            // reset anyway
            fitsImagePanel.setFitsImage(null);
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

            AutofitTableColumns.autoResizeTable(jTableOutputParamKeywords, true, true); // include header width
            AutofitTableColumns.autoResizeTable(jTableInputParamKeywords, true, true); // include header width

        } else {
            jPanelOIFits.remove(oifitsViewPanel);
            // reset Param Tables
            ((KeywordsTableModel) jTableOutputParamKeywords.getModel()).setFitsHdu(null);
            ((KeywordsTableModel) jTableInputParamKeywords.getModel()).setFitsHdu(null);
        }
    }

    private void setTabMode(SHOW_MODE mode) {
        syncingUI = true;
        try {
            // change border title
            switch (mode) {
                case MODEL:
                    setBorder(javax.swing.BorderFactory.createTitledBorder("Data Visualisation (INPUT)"));
                    break;
                case RESULT:
                    setBorder(javax.swing.BorderFactory.createTitledBorder("Data Visualisation (RESULT)"));
                    break;
                case GRID:
                    setBorder(javax.swing.BorderFactory.createTitledBorder("Data Visualisation (GRID)"));
                    break;
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
        } finally {
            syncingUI = false;
        }

        enableActions();
        restoreLastShownPanel();
    }

    public void displayModel(IRModel irModel) {
        showMode = SHOW_MODE.MODEL;
        if (irModel != null) {
            String inputImageView = irModel.getInputImageView();
            displayOiFitsAndParams(irModel.getOifitsFile(), irModel.getImageOiData().getInputParam().getTarget());
            // only list image HDUs present in the input file
            // choose the image which was changed lastly
            if (inputImageView == null || inputImageView.equals(KEYWORD_INIT_IMG)) {
                displayImage(irModel.getOifitsFile().getFitsImageHDUs(), irModel.getSelectedInputImageHDU());
            } else if (inputImageView.equals(KEYWORD_RGL_PRIO)) {
                // only list image HDUs present in the input file:
                displayImage(irModel.getOifitsFile().getFitsImageHDUs(), irModel.getSelectedRglPrioImage());
            }
        }
        setTabMode(SHOW_MODE.MODEL);
    }

    public void displayResult(ServiceResult result) {
        showMode = SHOW_MODE.RESULT;

        // Remove the grid view if any
        jPanelImage.removeAll();
        jPanelImage.setLayout(new BorderLayout());

        if (result != null) {
            // execution log
            jEditorPaneExecutionLog.setText(result.getExecutionLog());

            if (result.isValid()) {
                final OIFitsFile oifitsFile = result.getOifitsFile();

                // TODO have a look in the ouput param to look at right image ?
                // show first one :
                final List<FitsImageHDU> imageHdus = oifitsFile.getFitsImageHDUs();
                final FitsImageHDU imageHDU = imageHdus.isEmpty() ? null : imageHdus.get(0);
                final String target = oifitsFile.getImageOiData().getInputParam().getTarget();

                displayImage(imageHdus, imageHDU);
                displayOiFitsAndParams(oifitsFile, target);
            } else {
                lastResultPanel = jPanelLogViewer;
            }

            setTabMode(SHOW_MODE.RESULT);
        }
    }

    private int calculateGridSize(int resultsSize) {

        int i = resultsSize;
        double size;

        // TODO: should be easier to find out the size ...
        while (true) {
            size = Math.sqrt(i);
            if (size - Math.floor(size) == 0) {
                return (int) size;
            }
            i++;
        }
    }

    public void displayGrid(List<ServiceResult> results) {
        showMode = SHOW_MODE.GRID;

        if (!results.isEmpty()) {
            final int gridSize = calculateGridSize(results.size());
            jPanelImage.removeAll();
            jPanelImage.setLayout(new GridLayout(gridSize, gridSize));

            // Get min/max range over all images:
            final float[] globalDataRange = new float[2];
            globalDataRange[0] = Float.POSITIVE_INFINITY;
            globalDataRange[1] = Float.NEGATIVE_INFINITY;

            for (ServiceResult result : results) {
                // TODO: generalize for comparison (sliders on cube or table)
                final OIFitsFile oifitsFile = result.getOifitsFile();

                if (result.isValid()) {
                    // TODO have a look in the ouput param to look at right image ?
                    // use first one :
                    final List<FitsImageHDU> imageHdus = oifitsFile.getFitsImageHDUs();
                    final FitsImageHDU imageHDU = imageHdus.isEmpty() ? null : imageHdus.get(0);

                    if (imageHDU != null) {
                        final FitsImage image = imageHDU.getFitsImages().get(0);

                        final float min = (float) image.getDataMin();
                        final float max = (float) image.getDataMax();

                        logger.debug("image data range: [{} - {}]", min, max);

                        // update data range:
                        if (globalDataRange[0] > min) {
                            globalDataRange[0] = min;
                        }
                        if (globalDataRange[1] < max) {
                            globalDataRange[1] = max;
                        }
                    }
                }
            }
            logger.debug("displayGrid: global data range: [{} - {}]", globalDataRange[0], globalDataRange[1]);

            for (ServiceResult result : results) {
                final OIFitsFile oifitsFile = result.getOifitsFile();

                if (result.isValid()) {
                    final FitsImagePanel panel = new FitsImagePanel(Preferences.getInstance(), true, false, globalDataRange);
                    jPanelImage.add(panel);

                    // TODO have a look in the ouput param to look at right image ?
                    // show first one :
                    final List<FitsImageHDU> imageHdus = oifitsFile.getFitsImageHDUs();
                    final FitsImageHDU imageHDU = imageHdus.isEmpty() ? null : imageHdus.get(0);

                    if (imageHDU != null) {
                        final FitsImage image = imageHDU.getFitsImages().get(0);
                        panel.setFitsImage(image);
                    }

                    // TODO FIX: not working !!
                    // see JFreeChart listeners ?
                    panel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (e.getClickCount() == 2) {
                                System.out.println("double clicked");
                                jPanelImage.removeAll();
                                displayResult(result);
                            }
                            System.out.println("not double clicked");
                        }
                    });
                }
            }
            setTabMode(SHOW_MODE.GRID);
        }
    }

    // TODO move out of this class
    public File exportOIFits(final boolean useFileChooser) {
        final OIFitsFile oifitsFile = oifitsViewPanel.getOIFitsData();
        // store original filename
        final String originalAbsoluteFilePath = oifitsFile.getAbsoluteFilePath();
        String name = oifitsFile.getFileName();
        // name can not be null below:
        if (name == null) {
            name = "";
        }
        final File file;

        if (useFileChooser) {
            final File dir = FileUtils.getDirectory(originalAbsoluteFilePath); // may be null

            if (!name.isEmpty() && !name.contains(".image-oi")) {
                name = FileUtils.getFileNameWithoutExtension(name) + ".image-oi." + FileUtils.getExtension(oifitsFile.getFileName());
            }

            file = FileChooser.showSaveFileChooser("Choose destination to write the OIFits file", dir, MimeType.OIFITS, name);
        } else {
            file = FileUtils.getTempFile(name, FITS_EXTENSION);
        }

        // Cancel
        if (file == null) {
            return null;
        }

        try {
            OIFitsWriter.writeOIFits(file.getAbsolutePath(), oifitsFile);
        } catch (IOException ioe) {
            // Show the feedback report (modal) :
            FeedbackReport.openDialog(true, ioe);
        } catch (FitsException fe) {
            // Show the feedback report (modal) :
            FeedbackReport.openDialog(true, fe);
        } finally {
            if (!useFileChooser && !StringUtils.isEmpty(originalAbsoluteFilePath)) {
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
        } catch (IOException ioe) {
            // Show the feedback report (modal) :
            FeedbackReport.openDialog(true, ioe);
        } catch (FitsException fe) {
            // Show the feedback report (modal) :
            FeedbackReport.openDialog(true, fe);
        } finally {
            if (!StringUtils.isEmpty(originalImageIdentifier)) {
                // restore identifier
                fitsImage.setFitsImageIdentifier(originalImageIdentifier);
            }
        }
        return file;
    }

    public void changeViewportFitsImage() {
        final FitsImage fitsImage = fitsImagePanel.getFitsImage();
        if (fitsImage != null) {
            if (fitsImagePanel.changeViewportFitsImage()) {
                displaySelection(fitsImage.getFitsImageHDU());
            }
        }
    }

    public void resampleFitsImage() {
        final FitsImage fitsImage = fitsImagePanel.getFitsImage();
        if (fitsImage != null) {
            if (fitsImagePanel.resampleFitsImage()) {
                displaySelection(fitsImage.getFitsImageHDU());
            }
        }
    }

    public void resampleFitsImageV2() {
        final FitsImage fitsImage = fitsImagePanel.getFitsImage();

        // some checks
        if (fitsImage == null || fitsImage.getFitsImageHDU() == null) {
            return;
        }

        final FitsImageHDU oldFitsImageHDU = fitsImage.getFitsImageHDU();

        try {
            final FitsImageHDU clonedFitsImageHDU = oldFitsImageHDU.clone();
            clonedFitsImageHDU.setHduName("resampled-" + clonedFitsImageHDU.getHduName());

            displayImage(Arrays.asList(clonedFitsImageHDU), clonedFitsImageHDU);

            if (fitsImagePanel.resampleFitsImageV2()) {
                // TODO: fix this dirty hack ; the checksum does not update itself
                clonedFitsImageHDU.setChecksum(clonedFitsImageHDU.getChecksum() + Instant.now().getEpochSecond());

                displayImage(Arrays.asList(clonedFitsImageHDU), clonedFitsImageHDU);

                IRModelManager.getInstance().getIRModel().addFitsImageHDU(clonedFitsImageHDU);
                IRModelManager.getInstance().fireIRModelChanged(this, null);
            } else {
                displaySelection(oldFitsImageHDU);
            }
        } catch (CloneNotSupportedException e) {
            logger.info("Could not clone: {}", e.getMessage());
        }
    }

    public void rescaleFitsImage() {
        final FitsImage fitsImage = fitsImagePanel.getFitsImage();
        if (fitsImage != null) {
            if (fitsImagePanel.rescaleFitsImage()) {
                displaySelection(fitsImage.getFitsImageHDU());
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

        jTabbedPaneVizualizations = new javax.swing.JTabbedPane();
        jPanelOIFitsViewer = new javax.swing.JPanel();
        jPanelOIFits = new javax.swing.JPanel();
        jPanelImageViewer = new javax.swing.JPanel();
        jPanelImageSelector = new javax.swing.JPanel();
        jComboBoxImage = new javax.swing.JComboBox();
        jButtonViewport = new javax.swing.JButton();
        jButtonResample = new javax.swing.JButton();
        jButtonResampleV2 = new javax.swing.JButton();
        jButtonRescale = new javax.swing.JButton();
        jLabelImageDebug = new javax.swing.JLabel();
        jPanelImage = new javax.swing.JPanel();
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

        jTabbedPaneVizualizations.setName("jTabbedPaneVizualizations"); // NOI18N

        jPanelOIFitsViewer.setLayout(new java.awt.GridBagLayout());

        jPanelOIFits.setLayout(new java.awt.BorderLayout());
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

        jComboBoxImage.setPrototypeDisplayValue("XXXX");
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
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelImageViewer.add(jPanelImageSelector, gridBagConstraints);

        jButtonViewport.setText("Viewport");
        jButtonViewport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonViewportActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelImageViewer.add(jButtonViewport, gridBagConstraints);

        jButtonResample.setText("Resample");
        jButtonResample.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResampleActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelImageViewer.add(jButtonResample, gridBagConstraints);

        jButtonResampleV2.setText("Resample");
        jButtonResampleV2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResampleV2ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelImageViewer.add(jButtonResampleV2, gridBagConstraints);

        jButtonRescale.setText("Rescale");
        jButtonRescale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRescaleActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelImageViewer.add(jButtonRescale, gridBagConstraints);

        jLabelImageDebug.setForeground(java.awt.Color.red);
        jLabelImageDebug.setText("Debug");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        jPanelImageViewer.add(jLabelImageDebug, gridBagConstraints);

        jPanelImage.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelImageViewer.add(jPanelImage, gridBagConstraints);

        jTabbedPaneVizualizations.addTab("Images", jPanelImageViewer);

        jPanelLogViewer.setLayout(new java.awt.BorderLayout());

        jEditorPaneExecutionLog.setEditable(false);
        jScrollPaneLog.setViewportView(jEditorPaneExecutionLog);

        jPanelLogViewer.add(jScrollPaneLog, java.awt.BorderLayout.CENTER);

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

        jScrollPaneTableOutput.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTableOutputParamKeywords.setModel(new KeywordsTableModel());
        jTableOutputParamKeywords.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
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

        jScrollPaneTableInput.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jTableInputParamKeywords.setModel(new KeywordsTableModel());
        jTableInputParamKeywords.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
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
        if (syncingUI) {
            return;
        }
        displaySelection((FitsImageHDU) jComboBoxImage.getSelectedItem());
    }//GEN-LAST:event_jComboBoxImageActionPerformed

    private void jButtonRescaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRescaleActionPerformed
        rescaleFitsImage();
    }//GEN-LAST:event_jButtonRescaleActionPerformed

    private void jButtonViewportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonViewportActionPerformed
        changeViewportFitsImage();
    }//GEN-LAST:event_jButtonViewportActionPerformed

    private void jButtonResampleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResampleActionPerformed
        resampleFitsImage();
    }//GEN-LAST:event_jButtonResampleActionPerformed

    private void jButtonResampleV2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResampleV2ActionPerformed
        resampleFitsImageV2();
    }//GEN-LAST:event_jButtonResampleV2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonResample;
    private javax.swing.JButton jButtonResampleV2;
    private javax.swing.JButton jButtonRescale;
    private javax.swing.JButton jButtonViewport;
    private javax.swing.JComboBox jComboBoxImage;
    private javax.swing.JEditorPane jEditorPaneExecutionLog;
    private javax.swing.JLabel jLabelImageDebug;
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
        final OIFitsFile oiFitsFile = oifitsViewPanel.getOIFitsData();
        final boolean enableExportOiFits = (oiFitsFile != null) && (oiFitsFile.getNbOiTables() > 0);
        exportOiFitsAction.setEnabled(enableExportOiFits);
        sendOiFitsAction.setEnabled(enableExportOiFits);

        final boolean enableExportImage = (fitsImagePanel.getFitsImage() != null);
        exportFitsImageAction.setEnabled(enableExportImage);
        sendFitsAction.setEnabled(enableExportImage);
    }

    private void storeLastPanel() {
        if (syncingUI || jTabbedPaneVizualizations.getSelectedComponent() == null) {
            return;
        }

        switch (getShowMode()) {
            case MODEL:
                lastModelPanel = jTabbedPaneVizualizations.getSelectedComponent();
                break;
            case RESULT:
                lastResultPanel = jTabbedPaneVizualizations.getSelectedComponent();
                break;
            case GRID:
                lastGridPanel = jTabbedPaneVizualizations.getSelectedComponent();
                break;
        }
    }

    private void restoreLastShownPanel() {
        // last panel refs may be null during program startup...
        if (getShowMode() == SHOW_MODE.MODEL && lastModelPanel != null) {
            jTabbedPaneVizualizations.setSelectedComponent(lastModelPanel);
        } else if (getShowMode() == SHOW_MODE.RESULT & lastResultPanel != null) {
            jTabbedPaneVizualizations.setSelectedComponent(lastResultPanel);
        } else if (getShowMode() == SHOW_MODE.GRID && lastGridPanel != null) {
            jTabbedPaneVizualizations.setSelectedComponent(lastGridPanel);
        }
    }

    /**
     * @return the showMode
     */
    public SHOW_MODE getShowMode() {
        return showMode;
    }
}
