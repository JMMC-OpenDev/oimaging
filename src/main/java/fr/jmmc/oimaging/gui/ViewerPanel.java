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
import fr.jmmc.oimaging.OImaging;
import fr.jmmc.oimaging.Preferences;
import fr.jmmc.oimaging.gui.action.SetAsInitImgAction;
import fr.jmmc.oimaging.model.IRModel;
import fr.jmmc.oimaging.model.IRModelManager;
import fr.jmmc.oimaging.services.ServiceResult;
import fr.jmmc.oitools.image.FitsImage;
import fr.jmmc.oitools.image.FitsImageFile;
import fr.jmmc.oitools.image.FitsImageHDU;
import fr.jmmc.oitools.image.FitsImageLoader;
import fr.jmmc.oitools.image.FitsImageWriter;
import static fr.jmmc.oitools.image.ImageOiConstants.KEYWORD_INIT_IMG;
import static fr.jmmc.oitools.image.ImageOiConstants.KEYWORD_RGL_PRIO;
import fr.jmmc.oitools.image.ImageOiData;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsWriter;
import fr.nom.tam.fits.BasicHDU;
import fr.nom.tam.fits.FitsException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
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

    /* constants for the labels of the tab headers */
    public static final String TAB_LABEL_IMAGES = "Images";
    public static final String TAB_LABEL_OIFITS = "OIFits data";
    public static final String TAB_LABEL_PARAMS = "Parameters";
    public static final String TAB_LABEL_EXECLOG = "Execution log";

    /* members */
    /** reference to parent MainPanel */
    private MainPanel mainPanel;

    /** Fits image panel */
    private final FitsImagePanel fitsImagePanel;

    /** OIFits viewer panel */
    private final OIFitsViewPanel oifitsViewPanel;

    /** Slider panel */
    private final SliderPanel sliderPanel;

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

        jComboBoxImage.setRenderer(new OiCellRenderer());

        jLabelImageDebug.setVisible(SHOW_DEBUG_INFO);

        if (!OImaging.DEV_MODE) {
            // hiding buttons replaced by ModifyImage button
            jButtonResample.setVisible(false);
            jButtonRescale.setVisible(false);
            jButtonViewport.setVisible(false);
        }

        jEditorPaneExecutionLog.setFont(new Font("Monospaced", Font.PLAIN, SwingUtils.adjustUISize(10)));
    }

    /**
     * set the plot id associated to the OIFitsViewPanel.
     *
     * @param plotId the id of the plot
     */
    public void setOIFitsViewPlotId(String plotId) {
        this.oifitsViewPanel.updatePlotId(plotId);
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
                // reset anyway
                fitsImagePanel.setFitsImage(null);
                jPanelImage.remove(fitsImagePanel);
            }
            jComboBoxImage.setEnabled(jComboBoxImage.getItemCount() > 1);
            jButtonModifyImage.setEnabled(jComboBoxImage.getItemCount() != 0);
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
            logger.debug("Display image HDU {} '{}'", this.showMode, imageHDU.getHduName());
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
            ((KeywordsTableModel) jTableOutputParamKeywords.getModel()).setFitsHdu(imageOiData.getExistingOutputParam());
            ((KeywordsTableModel) jTableInputParamKeywords.getModel()).setFitsHdu(imageOiData.getInputParam());

            AutofitTableColumns.autoResizeTable(jTableOutputParamKeywords, true, true); // include header width
            AutofitTableColumns.autoResizeTable(jTableInputParamKeywords, true, true); // include header width
        } else {
            // call to plot with null so it forgets the oifitsfile 
            oifitsViewPanel.plot(null, null);

            jPanelOIFits.remove(oifitsViewPanel);
            // reset Param Tables
            ((KeywordsTableModel) jTableOutputParamKeywords.getModel()).setFitsHdu(null);
            ((KeywordsTableModel) jTableInputParamKeywords.getModel()).setFitsHdu(null);
        }
        // hide output params table if empty:
        final boolean showOutput = ((KeywordsTableModel) jTableOutputParamKeywords.getModel()).getRowCount() != 0;
        jLabelOutput.setVisible(showOutput);
        jScrollPaneTableOutput.setVisible(showOutput);
    }

    public void setTabMode(SHOW_MODE mode) {
        syncingUI = true;
        try {
            // change border title
            switch (mode) {
                case MODEL:
                    this.jButtonSetAsInitImg.setVisible(false);
                    setBorder(javax.swing.BorderFactory.createTitledBorder("Data Visualisation (INPUT)"));
                    break;
                case RESULT:
                    this.jButtonSetAsInitImg.setVisible(true);
                    setBorder(javax.swing.BorderFactory.createTitledBorder("Data Visualisation (RESULT)"));
                    break;
                case GRID:
                    this.jButtonSetAsInitImg.setVisible(true);
                    setBorder(javax.swing.BorderFactory.createTitledBorder("Data Visualisation (GRID)"));
                    break;
            }

            // switch tab arrangement only if we switch between model display or result display
            if ((mode.equals(SHOW_MODE.MODEL) && (jTabbedPaneVizualizations.getComponentCount() > 3))
                    || (mode.equals(SHOW_MODE.RESULT) && (jTabbedPaneVizualizations.getComponentCount() == 3))) {
                jTabbedPaneVizualizations.removeAll();
                jTabbedPaneVizualizations.add(TAB_LABEL_IMAGES, jPanelImageViewer);
                jTabbedPaneVizualizations.add(TAB_LABEL_OIFITS, jPanelOIFitsViewer);
                jTabbedPaneVizualizations.add(TAB_LABEL_PARAMS, jPanelOutputParamViewer);
                if (mode.equals(SHOW_MODE.RESULT)) {
                    jTabbedPaneVizualizations.add(TAB_LABEL_EXECLOG, jPanelLogViewer);
                }
            }
        } finally {
            syncingUI = false;
        }

        enableActions();
        restoreLastShownPanel();
    }

    public void displayModel(IRModel irModel) {
        setShowMode(SHOW_MODE.MODEL);
        if (irModel != null) {
            String inputImageView = irModel.getInputImageView();
            displayOiFitsAndParams(irModel.getOifitsFile(), irModel.getImageOiData().getInputParam().getTarget());
            // only list image HDUs present in the input file
            // choose the image which was changed lastly
            if (inputImageView == null || inputImageView.equals(KEYWORD_INIT_IMG)) {
                displayImage(irModel.getOifitsFile().getFitsImageHDUs(), irModel.getSelectedInputImageHDU());
            } else if (inputImageView.equals(KEYWORD_RGL_PRIO)) {
                // only list image HDUs present in the input file:
                displayImage(irModel.getOifitsFile().getFitsImageHDUs(), irModel.getSelectedRglPrioImageHdu());
            }
        }
        setTabMode(SHOW_MODE.MODEL);
    }

    public void displayResult(ServiceResult result) {
        setShowMode(SHOW_MODE.RESULT);

        // Remove the grid view if any
        jPanelImage.removeAll();
        jPanelImage.setLayout(new BorderLayout());

        if (result == null) {
            jEditorPaneExecutionLog.setText("");
            displaySelection(null);
            displayImage(null, null);
            displayOiFitsAndParams(null, null);
        } else {
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
            }
            /*else {
                lastResultPanel = jPanelLogViewer;
            }*/

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
        setShowMode(SHOW_MODE.GRID);

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
                }
            }
            setTabMode(SHOW_MODE.GRID);
        }
    }

    public OIFitsFile getCurrentOIFitsFile() {
        return oifitsViewPanel.getOIFitsData();
    }

    // TODO move out of this class
    public File exportOIFits(final boolean useFileChooser) {
        final OIFitsFile oifitsFile = getCurrentOIFitsFile();
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

    private void changeViewportFitsImage() {
        processFitsImage(ProcessImageOperation.changeViewport);
    }

    private void resampleFitsImage() {
        processFitsImage(ProcessImageOperation.resample);
    }

    private void rescaleFitsImage() {
        processFitsImage(ProcessImageOperation.rescale);
    }

    private void modifyFitsImage() {
        processFitsImage(ProcessImageOperation.modifyImage);
    }

    public enum ProcessImageOperation {
        changeViewport,
        resample,
        rescale,
        modifyImage;

        public boolean action(final FitsImagePanel fitsImagePanel) {
            switch (this) {
                case changeViewport:
                    return fitsImagePanel.changeViewportFitsImage();
                case resample:
                    return fitsImagePanel.resampleFitsImage();
                case rescale:
                    return fitsImagePanel.rescaleFitsImage();
                case modifyImage:
                    return fitsImagePanel.modifyFitsImage();
                default:
            }
            return false;
        }
    }

    public void processFitsImage(final ProcessImageOperation operation) {
        final FitsImage fitsImage = fitsImagePanel.getFitsImage();

        if ((fitsImage == null) || (fitsImage.getFitsImageHDU() == null)) {
            return;
        }

        final FitsImageHDU fitsImageHDU = fitsImage.getFitsImageHDU();

        // create a shallow-copy of the current image:
        final FitsImageHDU copyFitsImageHDU = new FitsImageHDU(fitsImageHDU);

        String hduName = copyFitsImageHDU.getHduName();
        if (!hduName.startsWith("modified-")) {
            hduName = "modified-" + hduName;
        }
        copyFitsImageHDU.setHduName(hduName);

        // switch image to copied HDU in the fitsImagePanel to be modified in-place:
        displaySelection(copyFitsImageHDU);

        // show dialog and waits for user action (async changes may happen):
        if (operation.action(fitsImagePanel)) {
            // update keywords
            try {
                final BasicHDU basicHdu = FitsImageWriter.createHDUnit(copyFitsImageHDU);
                // clear the header cards, because basicHdu already have all of them updated
                // not clearing them would make processKeywords() to output duplicates header cards.
                copyFitsImageHDU.getHeaderCards().clear();
                FitsImageLoader.processKeywords(null, basicHdu.getHeader(), copyFitsImageHDU);
            } catch (FitsException e) {
                logger.info(e.getMessage());
            }

            // add modified image into image library and select it if appropriate:
            IRModelManager.getInstance().getIRModel().addFitsImageHDUAndSelect(fitsImageHDU, copyFitsImageHDU);
        }

        // restore initial image, but only if initial image have not changed:
        // during the dialog, some async action (like run) could have changed it
        if ((fitsImagePanel.getFitsImage() != null)
                && (copyFitsImageHDU == fitsImagePanel.getFitsImage().getFitsImageHDU())) {
            displaySelection(fitsImageHDU);
        }
    }

    /** Call the dialog for creating an image.
     * Also add the image to the image library, and select it as initial image.
     */
    public void createFitsImage() {
        final IRModelManager irModelManager = IRModelManager.getInstance();
        final IRModel irModel = irModelManager.getIRModel();

        final FitsImageHDU newHDU = fitsImagePanel.createFitsImage();

        if (newHDU != null) {

            // update keywords
            try {
                final BasicHDU basicHdu = FitsImageWriter.createHDUnit(newHDU);
                // clear the header cards, because basicHdu already have all of them updated
                // not clearing them would make processKeywords() to output duplicates header cards.
                newHDU.getHeaderCards().clear();
                FitsImageLoader.processKeywords(null, basicHdu.getHeader(), newHDU);
            } catch (FitsException e) {
                logger.info(e.getMessage());
            }

            // add the FitsImageHDU to the imageLibrary
            final List<FitsImageHDU> libraryHDUs = irModel.addFitsImageHDUs(Arrays.asList(newHDU), "(created)");

            irModel.updateImageIdentifiers(newHDU);

            // selecting first library HDU as inputImageHDU
            final String selection = irModel.getInputImageView();
            if (selection == KEYWORD_INIT_IMG || selection == null) {
                irModel.setSelectedInputImageHDU(libraryHDUs.get(0));
            } else if (selection == KEYWORD_RGL_PRIO) {
                irModel.setSelectedRglPrioImageHdu(libraryHDUs.get(0));
            }

            // notify model update
            irModelManager.fireIRModelChanged(this);
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
        jButtonModifyImage = new javax.swing.JButton();
        jButtonSetAsInitImg = new javax.swing.JButton();
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

        jTabbedPaneVizualizations.addTab(TAB_LABEL_OIFITS, jPanelOIFitsViewer);

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

        jButtonModifyImage.setText("Modify image");
        jButtonModifyImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonModifyImageActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelImageViewer.add(jButtonModifyImage, gridBagConstraints);

        jButtonSetAsInitImg.setAction(ActionRegistrar.getInstance().get(SetAsInitImgAction.CLASS_NAME, SetAsInitImgAction.ACTION_NAME));
        jButtonSetAsInitImg.setText("Set as Init Img");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelImageViewer.add(jButtonSetAsInitImg, gridBagConstraints);

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
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        jPanelImageViewer.add(jLabelImageDebug, gridBagConstraints);

        jPanelImage.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        jPanelImageViewer.add(jPanelImage, gridBagConstraints);

        jTabbedPaneVizualizations.addTab(TAB_LABEL_IMAGES, jPanelImageViewer);

        jPanelLogViewer.setLayout(new java.awt.BorderLayout());

        jEditorPaneExecutionLog.setEditable(false);
        jScrollPaneLog.setViewportView(jEditorPaneExecutionLog);

        jPanelLogViewer.add(jScrollPaneLog, java.awt.BorderLayout.CENTER);

        jTabbedPaneVizualizations.addTab(TAB_LABEL_EXECLOG, jPanelLogViewer);

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

        jTabbedPaneVizualizations.addTab(TAB_LABEL_PARAMS, jPanelOutputParamViewer);

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

    private void jButtonModifyImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonModifyImageActionPerformed
        modifyFitsImage();
    }//GEN-LAST:event_jButtonModifyImageActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonModifyImage;
    private javax.swing.JButton jButtonResample;
    private javax.swing.JButton jButtonRescale;
    private javax.swing.JButton jButtonSetAsInitImg;
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

    /**
     * ChangeListener implementation on jTabbedPaneVizualizations
     * @param e unused
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        if (syncingUI) {
            return;
        }

        enableActions();
        storeLastPanel();
    }

    private void enableActions() {
        // small check to prevent calls too early in the application start
        // mainPanel is given at the end of MainPanel.initComponents()
        if (mainPanel != null) {
            // mainPanel has knowledge of both viewerPanels and will take decision of which actions to enable
            mainPanel.updateEnabledActions();
        }
    }

    void setMainPanel(MainPanel mainPanel) {
        this.mainPanel = mainPanel;
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

    /**
     * @param showMode the showMode to set
     */
    public void setShowMode(SHOW_MODE showMode) {
        this.showMode = showMode;
    }

    /**
     * @return true if no image in fitsImagePanel
     */
    public boolean isFitsImageNull() {
        return this.fitsImagePanel.getFitsImage() == null;
    }

    /**
     * return the FitsImageHDU parent to the FitsImage (potentially) displayed.
     * @return the FitsImageHDU if a FitsImage is displayed, null otherwise.
     */
    public FitsImageHDU getDisplayedFitsImageHDU() {

        final FitsImage fitsImage = fitsImagePanel.getFitsImage();

        if (fitsImage == null) {
            return null;
        } else {
            return fitsImage.getFitsImageHDU();
        }
    }
}
