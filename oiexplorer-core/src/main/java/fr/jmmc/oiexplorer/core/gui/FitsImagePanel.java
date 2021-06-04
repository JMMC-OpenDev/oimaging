/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui;

import fr.jmmc.jmal.ALX;
import fr.jmmc.jmal.image.ColorModels;
import fr.jmmc.jmal.image.ColorScale;
import fr.jmmc.jmal.image.ImageUtils;
import fr.jmmc.jmal.image.ImageUtils.ImageInterpolation;
import fr.jmmc.jmcs.gui.component.Disposable;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.task.Task;
import fr.jmmc.jmcs.gui.task.TaskSwingWorker;
import fr.jmmc.jmcs.gui.task.TaskSwingWorkerExecutor;
import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.jmcs.util.SpecialChars;
import fr.jmmc.jmcs.util.StringUtils;
import fr.jmmc.oiexplorer.core.Preferences;
import fr.jmmc.oiexplorer.core.export.DocumentExportable;
import fr.jmmc.oiexplorer.core.export.DocumentOptions;
import fr.jmmc.oiexplorer.core.function.ConverterFactory;
import fr.jmmc.oiexplorer.core.gui.action.ExportDocumentAction;
import fr.jmmc.oiexplorer.core.gui.chart.ChartUtils;
import fr.jmmc.oiexplorer.core.gui.chart.ColorModelPaintScale;
import fr.jmmc.oiexplorer.core.gui.chart.PaintLogScaleLegend;
import fr.jmmc.oiexplorer.core.gui.chart.SquareChartPanel;
import fr.jmmc.oiexplorer.core.gui.chart.SquareXYPlot;
import fr.jmmc.oiexplorer.core.gui.chart.ZoomEvent;
import fr.jmmc.oiexplorer.core.gui.chart.ZoomEventListener;
import fr.jmmc.oiexplorer.core.util.FitsImageUtils;
import static fr.jmmc.oiexplorer.core.util.FitsImageUtils.checkBounds;
import fr.jmmc.oitools.image.FitsImage;
import fr.jmmc.oitools.image.FitsImageHDU;
import fr.jmmc.oitools.image.FitsUnit;
import fr.jmmc.oitools.processing.Resampler;
import fr.jmmc.oitools.processing.Resampler.Filter;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.LogarithmicAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.ColumnArrangement;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.title.CompositeTitle;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.data.Range;
import org.jfree.chart.ui.Drawable;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This panel represents a FitsImage plot
 * @author bourgesl
 */
public class FitsImagePanel extends javax.swing.JPanel implements Disposable, ChartProgressListener, ZoomEventListener,
                                                                  Observer, DocumentExportable {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1L;
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(FitsImagePanel.class.getName());
    /** chart padding (right = 10px) */
    private final static RectangleInsets CHART_PADDING = new RectangleInsets(0d, 0d, 0d, 10d);
    /** image task prefix 'convertFitsImage-' */
    private static final String PREFIX_IMAGE_TASK = "convertFitsImage-";
    /** global thread counter */
    private final static AtomicInteger panelCounter = new AtomicInteger(1);
    /* members */
    /** show the image identifier */
    private final boolean showId;
    /** show the image options (LUT, color scale) */
    private final boolean showOptions;
    /** optional minimum range for data */
    private final float[] minDataRange;
    /** image convert task */
    final Task task;
    /** fits image to plot */
    private FitsImage fitsImage = null;
    /** preference singleton */
    private final Preferences myPreferences;
    /** flag to enable / disable the automatic refresh of the plot when any swing component changes */
    private boolean doAutoRefresh = true;
    /** jFreeChart instance */
    private JFreeChart chart;
    /** xy plot instance */
    private SquareXYPlot xyPlot;
    /** image scale legend */
    private PaintScaleLegend mapLegend = null;
    /** formatter for legend title / scale */
    private final DecimalFormat df = new DecimalFormat("0.0#E0");
    /** angle formatter for legend title */
    private final DecimalFormat df3 = new DecimalFormat("0.0##");
    /* plot data */
    /** last zoom event to check if the zoom area changed */
    private ZoomEvent lastZoomEvent = null;
    /** last axis unit to define the displayed image area */
    private FitsUnit lastAxisUnit = null;
    /** chart data */
    private ImageChartData chartData = null;
    /* swing */
    /** chart panel */
    private SquareChartPanel chartPanel;

    /**
     * Constructor
     * @param prefs Preferences instance
     */
    public FitsImagePanel(final Preferences prefs) {
        this(prefs, true, false, null);
    }

    /**
     * Constructor
     * @param prefs Preferences instance
     * @param showId true to show the image identifier
     * @param showOptions true to show the image options (LUT, color scale)
     */
    public FitsImagePanel(final Preferences prefs, final boolean showId, final boolean showOptions) {
        this(prefs, showId, showOptions, null);
    }

    /**
     * Constructor
     * @param prefs Preferences instance
     * @param showId true to show the image identifier
     * @param showOptions true to show the image options (LUT, color scale)
     * @param minDataRange optional minimal range for data
     */
    public FitsImagePanel(final Preferences prefs, final boolean showId, final boolean showOptions,
                          final float[] minDataRange) {
        this.myPreferences = prefs;
        this.showId = showId;
        this.showOptions = showOptions;
        this.minDataRange = minDataRange;
        this.task = new Task(PREFIX_IMAGE_TASK + panelCounter.getAndIncrement());

        initComponents();

        postInit();
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

        jPanelResample = new javax.swing.JPanel();
        jLabelInfo = new javax.swing.JLabel();
        jFormattedTextFieldNewSize = new javax.swing.JFormattedTextField();
        jLabelNewSize = new javax.swing.JLabel();
        jLabelOldSize = new javax.swing.JLabel();
        jLabelFilter = new javax.swing.JLabel();
        jComboBoxFilter = new javax.swing.JComboBox();
        jPanelViewport = new javax.swing.JPanel();
        jLabelViewport = new javax.swing.JLabel();
        jLabelDeltaRA = new javax.swing.JLabel();
        jFormattedTextFieldRAMin = new javax.swing.JFormattedTextField();
        jFormattedTextFieldRAMax = new javax.swing.JFormattedTextField();
        jLabelDeltaDE = new javax.swing.JLabel();
        jFormattedTextFieldDEMin = new javax.swing.JFormattedTextField();
        jFormattedTextFieldDEMax = new javax.swing.JFormattedTextField();
        jButtonPick = new javax.swing.JButton();
        jButtonOrig = new javax.swing.JButton();
        jLabelResampleFov = new javax.swing.JLabel();
        jFormattedTextFieldResampleFov = new javax.swing.JFormattedTextField();
        jButtonResampleFovUpdate = new javax.swing.JButton();
        jPanelRescale = new javax.swing.JPanel();
        jLabelRescale = new javax.swing.JLabel();
        jLabelRescaleFov = new javax.swing.JLabel();
        jFormattedTextFieldRescaleFov = new javax.swing.JFormattedTextField();
        jLabelScale = new javax.swing.JLabel();
        jFormattedTextFieldScaleX = new javax.swing.JFormattedTextField();
        jPanelOptions = new javax.swing.JPanel();
        jLabelLutTable = new javax.swing.JLabel();
        jComboBoxLUT = new javax.swing.JComboBox();
        jLabelColorScale = new javax.swing.JLabel();
        jComboBoxColorScale = new javax.swing.JComboBox();
        jButtonDisplayKeywords = new javax.swing.JButton();

        jPanelResample.setLayout(new java.awt.GridBagLayout());

        jLabelInfo.setText("Old size");
        jLabelInfo.setToolTipText("Current image size in pixels");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelResample.add(jLabelInfo, gridBagConstraints);

        jFormattedTextFieldNewSize.setColumns(8);
        jFormattedTextFieldNewSize.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jFormattedTextFieldNewSize.setToolTipText("");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelResample.add(jFormattedTextFieldNewSize, gridBagConstraints);

        jLabelNewSize.setText("New size");
        jLabelNewSize.setToolTipText("New image size in pixels");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelResample.add(jLabelNewSize, gridBagConstraints);

        jLabelOldSize.setText("jLabel2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelResample.add(jLabelOldSize, gridBagConstraints);

        jLabelFilter.setText("Resampling Filter");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelResample.add(jLabelFilter, gridBagConstraints);

        jComboBoxFilter.setModel(new DefaultComboBoxModel(Filter.values()));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelResample.add(jComboBoxFilter, gridBagConstraints);

        jPanelViewport.setLayout(new java.awt.GridBagLayout());

        jLabelViewport.setText("TODO");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelViewport.add(jLabelViewport, gridBagConstraints);

        jLabelDeltaRA.setText("Delta RA:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelViewport.add(jLabelDeltaRA, gridBagConstraints);

        jFormattedTextFieldRAMin.setColumns(8);
        jFormattedTextFieldRAMin.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0##"))));
        jFormattedTextFieldRAMin.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                viewportFormCoordPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelViewport.add(jFormattedTextFieldRAMin, gridBagConstraints);

        jFormattedTextFieldRAMax.setColumns(8);
        jFormattedTextFieldRAMax.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0##"))));
        jFormattedTextFieldRAMax.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                viewportFormCoordPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelViewport.add(jFormattedTextFieldRAMax, gridBagConstraints);

        jLabelDeltaDE.setText("Delta RE:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelViewport.add(jLabelDeltaDE, gridBagConstraints);

        jFormattedTextFieldDEMin.setColumns(8);
        jFormattedTextFieldDEMin.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0##"))));
        jFormattedTextFieldDEMin.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                viewportFormCoordPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelViewport.add(jFormattedTextFieldDEMin, gridBagConstraints);

        jFormattedTextFieldDEMax.setColumns(8);
        jFormattedTextFieldDEMax.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0##"))));
        jFormattedTextFieldDEMax.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                viewportFormCoordPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelViewport.add(jFormattedTextFieldDEMax, gridBagConstraints);

        jButtonPick.setText("pick");
        jButtonPick.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPickActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelViewport.add(jButtonPick, gridBagConstraints);

        jButtonOrig.setText("orig");
        jButtonOrig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOrigActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelViewport.add(jButtonOrig, gridBagConstraints);

        jLabelResampleFov.setText("Image FOV:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelViewport.add(jLabelResampleFov, gridBagConstraints);

        jFormattedTextFieldResampleFov.setColumns(8);
        jFormattedTextFieldResampleFov.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0##"))));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelViewport.add(jFormattedTextFieldResampleFov, gridBagConstraints);

        jButtonResampleFovUpdate.setText("update");
        jButtonResampleFovUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResampleFovUpdateActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanelViewport.add(jButtonResampleFovUpdate, gridBagConstraints);

        jPanelRescale.setLayout(new java.awt.GridBagLayout());

        jLabelRescale.setText("TODO");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelRescale.add(jLabelRescale, gridBagConstraints);

        jLabelRescaleFov.setText("Image FOV:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        jPanelRescale.add(jLabelRescaleFov, gridBagConstraints);

        jFormattedTextFieldRescaleFov.setEditable(false);
        jFormattedTextFieldRescaleFov.setColumns(8);
        jFormattedTextFieldRescaleFov.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.0##"))));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelRescale.add(jFormattedTextFieldRescaleFov, gridBagConstraints);

        jLabelScale.setText("Scale");
        jLabelScale.setToolTipText("image increments in mas");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        jPanelRescale.add(jLabelScale, gridBagConstraints);

        jFormattedTextFieldScaleX.setColumns(10);
        jFormattedTextFieldScaleX.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("0.00##E0"))));
        jFormattedTextFieldScaleX.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jFormattedTextFieldScaleXPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelRescale.add(jFormattedTextFieldScaleX, gridBagConstraints);

        setLayout(new java.awt.BorderLayout());

        jPanelOptions.setLayout(new java.awt.GridBagLayout());

        jLabelLutTable.setText("LUT table");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        jPanelOptions.add(jLabelLutTable, gridBagConstraints);

        jComboBoxLUT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxLUTActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelOptions.add(jComboBoxLUT, gridBagConstraints);

        jLabelColorScale.setText("Color scale");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 6);
        jPanelOptions.add(jLabelColorScale, gridBagConstraints);

        jComboBoxColorScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxColorScaleActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(2, 8, 2, 2);
        jPanelOptions.add(jComboBoxColorScale, gridBagConstraints);

        jButtonDisplayKeywords.setText("Display keywords");
        jButtonDisplayKeywords.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDisplayKeywordsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 0);
        jPanelOptions.add(jButtonDisplayKeywords, gridBagConstraints);

        add(jPanelOptions, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

  private void jComboBoxColorScaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxColorScaleActionPerformed
      refreshPlot();
  }//GEN-LAST:event_jComboBoxColorScaleActionPerformed

  private void jComboBoxLUTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxLUTActionPerformed
      refreshPlot();
  }//GEN-LAST:event_jComboBoxLUTActionPerformed

    private void jButtonDisplayKeywordsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDisplayKeywordsActionPerformed
        if (this.fitsImage != null) {
            FitsImageHDU fitsImageHDU = this.fitsImage.getFitsImageHDU();
            // note: only possible with one Fits image or one Fits cube (single HDU):
            final String hduHeader = "ImageHDU#" + fitsImageHDU.getExtNb() + " has " + fitsImageHDU.getImageCount() + " images.\n\n" + fitsImageHDU.getHeaderCardsAsString("\n");
            MessagePane.showMessage(hduHeader);
        } else {
            MessagePane.showMessage("Sorry, no associated Fits HDU to display.");
        }
    }//GEN-LAST:event_jButtonDisplayKeywordsActionPerformed

    private void jButtonPickActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPickActionPerformed
        setCurrentViewportForm();
    }//GEN-LAST:event_jButtonPickActionPerformed

    private void jButtonOrigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOrigActionPerformed
        setOriginalViewportForm();
    }//GEN-LAST:event_jButtonOrigActionPerformed

    private void jButtonResampleFovUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResampleFovUpdateActionPerformed
        setFovInViewportForm();
    }//GEN-LAST:event_jButtonResampleFovUpdateActionPerformed

    private void viewportFormCoordPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_viewportFormCoordPropertyChange
        if ("value".equals(evt.getPropertyName())) {
            updateFovInViewportForm();
        }
    }//GEN-LAST:event_viewportFormCoordPropertyChange

    private void jFormattedTextFieldScaleXPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jFormattedTextFieldScaleXPropertyChange
        if ("value".equals(evt.getPropertyName())) {
            updateFovInRescaleForm();
        }
    }//GEN-LAST:event_jFormattedTextFieldScaleXPropertyChange

    /**
     * Export the component as a document using the given action:
     * the component should check if there is something to export ?
     * @param action export action to perform the export action
     */
    @Override
    public void performAction(final ExportDocumentAction action) {
        action.process(this);
    }

    /**
     * Return the default file name
     * @param fileExtension  document's file extension
     * @return default file name
     */
    @Override
    public String getDefaultFileName(final String fileExtension) {
        return null;
    }

    /**
     * Prepare the page layout before doing the export:
     * Performs layout and modifies the given options
     * @param options document options used to prepare the document
     */
    @Override
    public void prepareExport(final DocumentOptions options) {
        options.setSmallDefaults();
    }

    /**
     * Return the page to export given its page index
     * @param pageIndex page index (1..n)
     * @return Drawable array to export on this page
     */
    @Override
    public Drawable[] preparePage(final int pageIndex) {
        return new Drawable[]{this.chart};
    }

    /**
     * Callback indicating the export is done to reset the component's state
     */
    @Override
    public void postExport() {
        // no-op
    }

    /**
     * This method is useful to set the models and specific features of initialized swing components :
     */
    private void postInit() {
        this.chart = ChartUtils.createSquareXYLineChart(SpecialChars.DELTA_UPPER + "RA - [North]", SpecialChars.DELTA_UPPER + "DE - [East]", false);
        this.chart.setPadding(CHART_PADDING);

        this.xyPlot = (SquareXYPlot) this.chart.getPlot();

        // Move RA axis to top:
        this.xyPlot.setDomainAxisLocation(AxisLocation.TOP_OR_LEFT);

        this.xyPlot.getDomainAxis().setPositiveArrowVisible(true);
        this.xyPlot.getRangeAxis().setPositiveArrowVisible(true);

        // Adjust background settings :
        this.xyPlot.setBackgroundImageAlpha(1.0f);

        // add listener :
        this.chart.addProgressListener(this);
        this.chartPanel = ChartUtils.createSquareChartPanel(this.chart);

        // define zoom listener :
        this.chartPanel.setZoomEventListener(this);

        this.add(this.chartPanel);

        // register this instance as a Preference Observer :
        this.myPreferences.addObserver(this);

        // disable the automatic refresh :
        final boolean prevAutoRefresh = setAutoRefresh(false);
        try {
            // define custom models :
            this.jComboBoxLUT.setModel(new DefaultComboBoxModel(ColorModels.getColorModelNames()));
            this.jComboBoxColorScale.setModel(new DefaultComboBoxModel(ColorScale.values()));

            // update selected items:
            this.jComboBoxLUT.setSelectedItem(this.myPreferences.getPreference(Preferences.MODEL_IMAGE_LUT));
            this.jComboBoxColorScale.setSelectedItem(this.myPreferences.getImageColorScale());

            this.jComboBoxFilter.setSelectedItem(Resampler.FILTER_DEFAULT);
        } finally {
            // restore the automatic refresh :
            setAutoRefresh(prevAutoRefresh);
        }
        // show / hide the option panel:
        this.jPanelOptions.setVisible(this.showOptions);
    }

    /**
     * Free any ressource or reference to this instance :
     * remove this instance form Preference Observers
     */
    @Override
    public void dispose() {
        if (logger.isDebugEnabled()) {
            logger.debug("dispose: {}", ObjectUtils.getObjectInfo(this));
        }

        // Cancel any running task:
        TaskSwingWorkerExecutor.cancelTask(this.task);

        // unregister this instance as a Preference Observer :
        this.myPreferences.deleteObserver(this);
    }

    /**
     * Overriden method to give object identifier
     * @return string identifier
     */
    @Override
    public String toString() {
        return "FitsImagePanel@" + Integer.toHexString(hashCode());
    }

    public void addOptionPanel(final JPanel optionPanel) {
        if (this.showOptions && optionPanel != null) {
            final GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 6);
            this.jPanelOptions.add(optionPanel, gridBagConstraints);
            this.jPanelOptions.revalidate();
        }
    }

    public void removeOptionPanel(final JPanel optionPanel) {
        if (this.showOptions && optionPanel != null) {
            this.jPanelOptions.remove(optionPanel);
            this.jPanelOptions.revalidate();
        }
    }

    /**
     * Listen to preferences changes
     * @param o Preferences
     * @param arg unused
     */
    @Override
    public void update(final Observable o, final Object arg) {
        logger.debug("Preferences updated on : {}", this);

        final String colorModelPref = this.myPreferences.getPreference(Preferences.MODEL_IMAGE_LUT);
        final ColorScale colorScalePref = this.myPreferences.getImageColorScale();
        final ImageInterpolation interpolationPref = this.myPreferences.getImageInterpolation();

        // disable the automatic refresh :
        final boolean prevAutoRefresh = setAutoRefresh(false);
        try {
            // update selected items:
            this.jComboBoxLUT.setSelectedItem(colorModelPref);
            this.jComboBoxColorScale.setSelectedItem(colorScalePref);
        } finally {
            // restore the automatic refresh :
            setAutoRefresh(prevAutoRefresh);
        }

        final IndexColorModel colorModel = ColorModels.getColorModel(colorModelPref);

        if (getChartData() != null && !getChartData().isCompatible(colorModel, colorScalePref, interpolationPref)) {
            refreshPlot();
        }
    }

    /**
     * Update the fits image to plot
     * @param image image to plot
     */
    public void setFitsImage(final FitsImage image) {
        this.fitsImage = image;
        refreshPlot();
    }

    /**
     * Get the plotted fits image
     * @return the fits image
     */
    public FitsImage getFitsImage() {
        return this.fitsImage;
    }

    public boolean resampleFitsImage() {
        if (this.fitsImage == null) {
            return false;
        }
        // initialise the form:
        final int nbRows = fitsImage.getNbRows();
        this.jLabelOldSize.setText(String.valueOf(nbRows));
        this.jFormattedTextFieldNewSize.setValue(nbRows);

        if (MessagePane.showDialogPanel("Resample Image", this.jPanelResample)
                && !jFormattedTextFieldNewSize.getText().isEmpty()) {

            final int newSize = (int) Math.round(parseDouble(jFormattedTextFieldNewSize.getText()));
            final Filter filter = (Filter) jComboBoxFilter.getSelectedItem();
            logger.debug("resampleImages: newSize: {} - filter: {}", newSize, filter);

            boolean ok = false;
            try {
                FitsImageUtils.resampleImages(fitsImage.getFitsImageHDU(), newSize, filter);
                ok = true;
            } catch (IllegalArgumentException iae) {
                MessagePane.showErrorMessage("Unable to resample image", iae);
            } catch (IllegalStateException ise) {
                MessagePane.showErrorMessage("Unable to resample image", ise);
            }
            return ok;
        }
        return false;
    }

    public boolean rescaleFitsImage() {
        if (this.fitsImage == null) {
            return false;
        }
        final FitsUnit axisUnit = this.lastAxisUnit;
        if (axisUnit == null) {
            return false;
        }

        // initialise the form:
        // TODO: handle X/Y ie col/row increments (not linked) ?
        // update the form:
        this.jLabelRescale.setText("Enter increments in " + axisUnit.getStandardRepresentation());
        this.jFormattedTextFieldScaleX.setValue(FitsUnit.ANGLE_RAD.convert(fitsImage.getIncCol(), axisUnit));
        updateFovInRescaleForm();

        if (MessagePane.showDialogPanel("Rescale Image", this.jPanelRescale)
                && !jFormattedTextFieldScaleX.getText().isEmpty()) {

            final double value = parseDouble(jFormattedTextFieldScaleX.getText());
            if (value > 0.0) {
                final double inc = axisUnit.convert(value, FitsUnit.ANGLE_RAD);
                logger.debug("rescaleFitsImage: inc: {}", inc);

                boolean ok = false;
                try {
                    FitsImageUtils.rescaleImages(fitsImage.getFitsImageHDU(), inc, inc);
                    ok = true;
                } catch (IllegalArgumentException iae) {
                    MessagePane.showErrorMessage("Unable to rescale image", iae);
                } catch (IllegalStateException ise) {
                    MessagePane.showErrorMessage("Unable to rescale image", ise);
                }
                return ok;
            }
        }
        return false;
    }

    private void updateFovInRescaleForm() {
        final FitsUnit axisUnit = this.lastAxisUnit;
        if (axisUnit == null) {
            return;
        }
        final int nbRows = fitsImage.getNbRows();
        final int nbCols = fitsImage.getNbCols();

        final double value = parseDouble(jFormattedTextFieldScaleX.getText());
        if (value > 0.0) {
            final double inc = axisUnit.convert(value, FitsUnit.ANGLE_RAD);
            final double fov = Math.max(nbRows * inc, nbCols * inc);
            logger.debug("updateFovInRescaleForm: fov: {}", fov);

            if (!Double.isNaN(fov)) {
                updateRescaleFormFov(fov, axisUnit);
            }
        }
    }

    private void updateRescaleFormFov(final double fov, final FitsUnit axisUnit) {
        this.jFormattedTextFieldRescaleFov.setValue(FitsUnit.ANGLE_RAD.convert(
                fov, axisUnit));
    }

    public boolean changeViewportFitsImage() {
        final FitsUnit axisUnit = this.lastAxisUnit;

        if (this.fitsImage == null || axisUnit == null) {
            return false;
        }
        // initialise the form:
        setCurrentViewportForm();

        if (MessagePane.showDialogPanel("Change Image viewport", this.jPanelViewport)) {
            final Rectangle2D.Double newArea = getEditedViewportArea();
            logger.debug("changeViewportFitsImage: newArea: {}", newArea);

            if (newArea == null || !newArea.intersects(fitsImage.getArea())) {
                MessagePane.showMessage("Invalid coordinate ranges, no data !");
            } else {
                boolean ok = false;
                try {
                    FitsImageUtils.changeViewportImages(fitsImage.getFitsImageHDU(), newArea);
                    ok = true;
                } catch (IllegalArgumentException iae) {
                    MessagePane.showErrorMessage("Unable to change image viewport", iae);
                } catch (IllegalStateException ise) {
                    MessagePane.showErrorMessage("Unable to change image viewport", ise);
                }
                return ok;
            }
        }
        return false;
    }

    private Rectangle2D.Double getEditedViewportArea() {
        final FitsUnit axisUnit = this.lastAxisUnit;
        if (axisUnit == null) {
            return null;
        }
        final double ra0 = parseDouble(jFormattedTextFieldRAMin.getText());
        final double ra1 = parseDouble(jFormattedTextFieldRAMax.getText());
        final double de0 = parseDouble(jFormattedTextFieldDEMin.getText());
        final double de1 = parseDouble(jFormattedTextFieldDEMax.getText());

        if ((ra0 < ra1) && (de0 < de1)) {
            final Rectangle2D.Double area = new Rectangle2D.Double();
            area.setFrameFromDiagonal(
                    axisUnit.convert(ra0, FitsUnit.ANGLE_RAD),
                    axisUnit.convert(de0, FitsUnit.ANGLE_RAD),
                    axisUnit.convert(ra1, FitsUnit.ANGLE_RAD),
                    axisUnit.convert(de1, FitsUnit.ANGLE_RAD)
            );
            return area;
        }
        return null;
    }

    private void setOriginalViewportForm() {
        final FitsUnit axisUnit = this.lastAxisUnit;

        if (this.fitsImage == null || axisUnit == null) {
            return;
        }
        final Rectangle2D.Double area = fitsImage.getArea();
        logger.debug("setOriginalViewportForm: original area: {}", area);

        // update the form:
        updateViewportForm(area, axisUnit);
    }

    private void setCurrentViewportForm() {
        final FitsUnit axisUnit = this.lastAxisUnit;
        if (axisUnit == null) {
            return;
        }
        final Rectangle2D.Double area = getCurrentZoomRect();

        if (area != null) {
            logger.debug("setCurrentViewportForm: image area: {}", area);

            // update the form:
            updateViewportForm(area, axisUnit);
        } else {
            setOriginalViewportForm();
        }
    }

    private void setFovInViewportForm() {
        final FitsUnit axisUnit = this.lastAxisUnit;
        if (axisUnit == null) {
            return;
        }
        final double fov = parseDouble(jFormattedTextFieldResampleFov.getText());
        if (fov > 0.0) {
            final double halfFov = 0.5 * axisUnit.convert(fov, FitsUnit.ANGLE_RAD);
            logger.debug("setFovInViewportForm: halfFov: {}", halfFov);

            final Rectangle2D.Double area = getEditedViewportArea();
            logger.debug("setFovInViewportForm: area: {}", area);

            if (area != null) {
                final double cx = area.getCenterX();
                final double cy = area.getCenterY();
                area.setFrameFromDiagonal(cx - halfFov, cy - halfFov, cx + halfFov, cy + halfFov);

                logger.debug("setFovInViewportForm: fov area: {}", area);

                // update the form:
                updateViewportForm(area, axisUnit);
            }
        }
    }

    private void updateFovInViewportForm() {
        final FitsUnit axisUnit = this.lastAxisUnit;
        if (axisUnit == null) {
            return;
        }
        final Rectangle2D.Double area = getEditedViewportArea();
        logger.debug("updateFovInViewportForm: area: {}", area);

        if (area != null) {
            updateViewportFormFov(area, axisUnit);
        }
    }

    private void updateViewportForm(final Rectangle2D.Double area, final FitsUnit axisUnit) {
        // update the form:
        this.jLabelViewport.setText("Enter coordinates in " + axisUnit.getStandardRepresentation());
        this.jFormattedTextFieldRAMin.setValue(FitsUnit.ANGLE_RAD.convert(area.getMinX(), axisUnit));
        this.jFormattedTextFieldRAMax.setValue(FitsUnit.ANGLE_RAD.convert(area.getMaxX(), axisUnit));
        this.jFormattedTextFieldDEMin.setValue(FitsUnit.ANGLE_RAD.convert(area.getMinY(), axisUnit));
        this.jFormattedTextFieldDEMax.setValue(FitsUnit.ANGLE_RAD.convert(area.getMaxY(), axisUnit));
        updateViewportFormFov(area, axisUnit);
    }

    private void updateViewportFormFov(final Rectangle2D.Double area, final FitsUnit axisUnit) {
        this.jFormattedTextFieldResampleFov.setValue(FitsUnit.ANGLE_RAD.convert(
                Math.max(area.getWidth(), area.getHeight()), axisUnit));
    }

    /**
     * Refresh the plot when an UI widget changes.
     * Check the doAutoRefresh flag to avoid unwanted refresh
     */
    private void refreshPlot() {
        if (this.doAutoRefresh) {
            logger.debug("refreshPlot");
            this.plot();
        }
    }

    /**
     * Plot the image using a SwingWorker to do the computation in the background.
     * This code is executed by the Swing Event Dispatcher thread (EDT)
     */
    private void plot() {
        logger.debug("plot : {}", this.fitsImage);

        // check if fits image is available :
        if (this.fitsImage == null) {
            resetPlot();
        } else {

            // Use model image Preferences :
            final IndexColorModel colorModel = ColorModels.getColorModel((String) this.jComboBoxLUT.getSelectedItem());
            final ColorScale colorScale = (ColorScale) this.jComboBoxColorScale.getSelectedItem();

            // Create image convert task worker :
            // Cancel other tasks and execute this new task :
            new ConvertFitsImageSwingWorker(this, this.fitsImage, this.minDataRange, colorModel, colorScale).executeTask();
        }
    }

    /**
     * TaskSwingWorker child class to compute an image from the given fits image
     */
    private final static class ConvertFitsImageSwingWorker extends TaskSwingWorker<ImageChartData> {

        /* members */
        /** fits panel used for refreshUI callback */
        private final FitsImagePanel fitsPanel;
        /** fits image */
        private final FitsImage fitsImage;
        /** optional minimum range for data */
        private final float[] minDataRange;
        /** image color model */
        private final IndexColorModel colorModel;
        /** color scaling method */
        private final ColorScale colorScale;

        /**
         * Hidden constructor
         *
         * @param fitsPanel fits panel
         * @param fitsImage fits image
         * @param minDataRange optional minimal range for data
         * @param colorModel color model to use
         * @param colorScale color scaling method
         */
        private ConvertFitsImageSwingWorker(final FitsImagePanel fitsPanel, final FitsImage fitsImage, final float[] minDataRange,
                                            final IndexColorModel colorModel, final ColorScale colorScale) {
            // get current observation version :
            super(fitsPanel.task);
            this.fitsPanel = fitsPanel;
            this.fitsImage = fitsImage;
            this.minDataRange = minDataRange;
            this.colorModel = colorModel;
            this.colorScale = colorScale;
        }

        /**
         * Compute the image in background
         * This code is executed by a Worker thread (Not Swing EDT)
         * @return computed image data
         */
        @Override
        public ImageChartData computeInBackground() {

            // Start the computations :
            final long start = System.nanoTime();

            float min = (float) this.fitsImage.getDataMin();
            float max = (float) this.fitsImage.getDataMax();

            if (this.minDataRange != null) {
                // check minimum data range:
                if (min > this.minDataRange[0]) {
                    min = this.minDataRange[0];
                }
                if (max < this.minDataRange[1]) {
                    max = this.minDataRange[1];
                }
            }

            final ColorScale usedColorScale;
            if (colorScale == ColorScale.LOGARITHMIC
                    && (min <= 0f || max <= 0f || min == max || Float.isInfinite(min) || Float.isInfinite(max))) {
                usedColorScale = ColorScale.LINEAR;

                // update min/max:
                FitsImageUtils.updateDataRange(fitsImage);
                min = (float) this.fitsImage.getDataMin();
                max = (float) this.fitsImage.getDataMax();

                if (min == max) {
                    max = min + 1f;
                }
            } else if (colorScale == ColorScale.LINEAR
                    && (min <= 0f || max <= 0f || min == max || Float.isInfinite(min) || Float.isInfinite(max))) {
                usedColorScale = ColorScale.LINEAR;

                // update min/max:
                FitsImageUtils.updateDataRange(fitsImage);
                min = (float) this.fitsImage.getDataMin();
                max = (float) this.fitsImage.getDataMax();

                if (min == max) {
                    max = min + 1f;
                }
            } else {
                usedColorScale = colorScale;
            }

            // throws InterruptedJobException if the current thread is interrupted (cancelled):
            final BufferedImage image = ImageUtils.createImage(this.fitsImage.getNbCols(), this.fitsImage.getNbRows(),
                    this.fitsImage.getData(), min, max,
                    this.colorModel, usedColorScale);

            // fast interrupt :
            if (Thread.currentThread().isInterrupted()) {
                return null;
            }

            AffineTransform at = null;

            int sx = 1, sy = 1;
            if (fitsImage.isIncColPositive() || !fitsImage.isIncRowPositive()) {
                int tx = 0, ty = 0;
                if (fitsImage.isIncColPositive()) {
                    // Flip the image horizontally to have RA orientation = East is towards the left:
                    sx = -1;
                    tx = -image.getWidth();
                }
                if (!fitsImage.isIncRowPositive()) {
                    // Flip the image vertically to have DEC orientation = North is towards the top:
                    sy = -1;
                    ty = -image.getHeight();
                }
                at = AffineTransform.getScaleInstance(sx, sy);
                at.translate(tx, ty);
            }

            if (fitsImage.isRotAngleDefined()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("rotate image: {} deg", fitsImage.getRotAngle());
                }
                // angle sign is inverted:
                final double theta = -(sx * sy) * Math.toRadians(fitsImage.getRotAngle());
                // should rotation happen at the pixel center or at the image center ?
                final double anchorx = image.getWidth() / 2.0;
                final double anchory = image.getHeight() / 2.0;

                if (at == null) {
                    at = new AffineTransform();
                }
                // see AffineTransform.getRotateInstance(theta, anchorx, anchory)
                at.translate(anchorx, anchory);    // S3: final translation
                at.rotate(theta);                  // S2: rotate around anchor
                at.translate(-anchorx, -anchory);  // S1: translate anchor to origin
            }

            final ImageInterpolation usedInterpolation = ImageUtils.getImageInterpolation();

            final BufferedImage displayedImage;
            if (at != null) {
                // Compute output bounding box:
                final Rectangle2D bbox = new Rectangle(0, 0, image.getWidth(), image.getHeight());
                // enlarge output image if rotation defined:
                final Rectangle2D outbbox = (fitsImage.isRotAngleDefined()) ? ImageUtils.getBoundingBox(at, bbox) : bbox;

                if (logger.isDebugEnabled()) {
                    logger.debug("bbox:  {}", bbox);
                    logger.debug("tbbox: {}", outbbox);
                }

                final int w = (int) Math.ceil(outbbox.getWidth());
                final int h = (int) Math.ceil(outbbox.getHeight());

                displayedImage = ImageUtils.transformImage(image, this.colorModel, at, w, h);
            } else {
                displayedImage = image;
            }

            logger.info("compute[ImageChartData]: duration = {} ms.", 1e-6d * (System.nanoTime() - start));

            // Adjust viewed area:
            Rectangle2D.Double imgRectRef = fitsImage.getArea();

            if (fitsImage.isRotAngleDefined()) {
                // angle sign is same direction (North -> East):
                final double theta = Math.toRadians(fitsImage.getRotAngle());
                // should rotation happen at the pixel center or at the image center ?
                final double anchorx = imgRectRef.getCenterX();
                final double anchory = imgRectRef.getCenterY();

                at = AffineTransform.getRotateInstance(theta, anchorx, anchory);

                if (logger.isDebugEnabled()) {
                    logger.debug("area: {}", imgRectRef);
                }

                imgRectRef = ImageUtils.getBoundingBox(at, imgRectRef);

                if (logger.isDebugEnabled()) {
                    logger.debug("rotated area: {}", imgRectRef);
                }
            }

            return new ImageChartData(fitsImage, colorModel, usedColorScale, usedInterpolation, min, max, displayedImage, imgRectRef);
        }

        /**
         * Refresh the plot using the computed image.
         * This code is executed by the Swing Event Dispatcher thread (EDT)
         * @param imageData computed image data
         */
        @Override
        public void refreshUI(final ImageChartData imageData) {
            // Refresh the GUI using coherent data :
            this.fitsPanel.updatePlot(imageData);
        }

        /**
         * Handle the execution exception that occured in the compute operation @see #computeInBackground()
         * This implementation resets the plot and opens a message dialog or the feedback report depending on the cause.
         *
         * @param ee execution exception
         */
        @Override
        public void handleException(final ExecutionException ee) {
            this.fitsPanel.resetPlot();
            if (ee.getCause() instanceof IllegalArgumentException) {
                MessagePane.showErrorMessage(ee.getCause().getMessage());
            } else {
                super.handleException(ee);
            }
        }
    }

    /**
     * Return the chart data
     * @return chart data
     */
    private ImageChartData getChartData() {
        return this.chartData;
    }

    /**
     * Define the chart data
     * @param chartData chart data
     */
    private void setChartData(final ImageChartData chartData) {
        this.chartData = chartData;
    }

    /**
     * Reset the plot in case of model or image processing exception
     */
    protected void resetPlot() {
        ChartUtils.clearTextSubTitle(this.chart);

        this.lastZoomEvent = null;
        this.lastAxisUnit = null;
        this.chartData = null;

        // update the background image :
        this.updatePlotImage(null);

        // reset bounds to [-1;1] (before setDataset) :
        this.xyPlot.defineBounds(1d);
        // reset dataset for baseline limits :
        this.xyPlot.setDataset(null);

        // update theme at end :
        org.jfree.chart.ChartUtils.applyCurrentTheme(this.chart);
    }

    /**
     * Refresh the plot using the given image.
     * This code is executed by the Swing Event Dispatcher thread (EDT)
     *
     * @param imageData computed image data
     */
    private void updatePlot(final ImageChartData imageData) {
        // memorize image (used by zoom handling) :
        setChartData(imageData);

        // reset zoom cache :
        this.lastZoomEvent = null;

        // title :
        ChartUtils.clearTextSubTitle(this.chart);

        final FitsImage lFitsImage = imageData.getFitsImage();

        if (this.showId && lFitsImage.getFitsImageIdentifier() != null) {
            ChartUtils.addSubtitle(this.chart, "Id: " + lFitsImage.getFitsImageIdentifier());
        }

        final Title infoTitle;

        if (!(lFitsImage.isIncRowDefined() && lFitsImage.isIncColDefined())
                && Double.isNaN(lFitsImage.getWaveLength()) && lFitsImage.getImageCount() <= 1) {
            infoTitle = null;
        } else {
            final BlockContainer infoBlock = new BlockContainer(new ColumnArrangement());

            if (lFitsImage.isIncRowDefined() && lFitsImage.isIncColDefined()) {
                infoBlock.add(createText("Coordinates:"));
                infoBlock.add(createText("RA: " + ALX.toHMS(Math.toDegrees(lFitsImage.getValRefCol()))));
                infoBlock.add(createText("DE: " + ALX.toDMS(Math.toDegrees(lFitsImage.getValRefRow()))));

                infoBlock.add(createText("\nIncrements:"));
                infoBlock.add(createText("RA: " + FitsImage.getAngleAsString(lFitsImage.getIncCol(), df)));
                infoBlock.add(createText("DE: " + FitsImage.getAngleAsString(lFitsImage.getIncRow(), df)));

                infoBlock.add(createText("\nImage FOV:"));
                if (lFitsImage.isRotAngleDefined()) {
                    // FOV depends on the rotation angle
                    final BufferedImage image = imageData.getImage();
                    infoBlock.add(createText(FitsImage.getAngleAsString(lFitsImage.getMaxAngle(image.getWidth(), image.getHeight()), df3)));
                } else {
                    infoBlock.add(createText(FitsImage.getAngleAsString(lFitsImage.getMaxAngle(), df3)));
                }
                infoBlock.add(createText("\nPixels:"));
                infoBlock.add(createText(lFitsImage.getNbCols() + " x " + lFitsImage.getNbRows()));
            }

            if (lFitsImage.getImageCount() > 1) {
                infoBlock.add(createText("\nImage:" + lFitsImage.getImageIndex() + '/' + lFitsImage.getImageCount()));
            }

            if (!Double.isNaN(lFitsImage.getWaveLength())) {
                infoBlock.add(createText("\nModel " + SpecialChars.LAMBDA_LOWER + ":"));
                infoBlock.add(createText(NumberUtils.trimTo3Digits(ConverterFactory.CONVERTER_MICRO_METER.evaluate(lFitsImage.getWaveLength()))
                        + " " + ConverterFactory.CONVERTER_MICRO_METER.getUnit()));
            }

            infoTitle = new CompositeTitle(infoBlock);
            infoTitle.setFrame(new BlockBorder(Color.BLACK));
            infoTitle.setMargin(1d, 1d, 1d, 1d);
            infoTitle.setPadding(5d, 5d, 5d, 5d);
            infoTitle.setPosition(RectangleEdge.RIGHT);
        }

        // define axis boundaries:
        final Rectangle2D.Double imgRectRef = imageData.getImgRectRef();

        final FitsUnit axisUnit = FitsUnit.getAngleUnit(Math.min(imgRectRef.width, imgRectRef.height));

        this.xyPlot.defineBounds(
                new Range(
                        FitsUnit.ANGLE_RAD.convert(imgRectRef.getMinX(), axisUnit),
                        FitsUnit.ANGLE_RAD.convert(imgRectRef.getMaxX(), axisUnit)
                ),
                new Range(
                        FitsUnit.ANGLE_RAD.convert(imgRectRef.getMinY(), axisUnit),
                        FitsUnit.ANGLE_RAD.convert(imgRectRef.getMaxY(), axisUnit)
                ));

        this.xyPlot.restoreAxesBounds();

        // define axis orientation:
        // RA: East is positive at left:
        ValueAxis axis = this.xyPlot.getDomainAxis();
        axis.setLabel(SpecialChars.DELTA_UPPER + "RA (" + axisUnit.getStandardRepresentation() + ") - [North]");
        axis.setInverted(true);

        // DEC: North is positive at top:
        axis = this.xyPlot.getRangeAxis();
        axis.setLabel(SpecialChars.DELTA_UPPER + "DE (" + axisUnit.getStandardRepresentation() + ") - [East]");
        axis.setInverted(false);

        // memorize the axis unit:
        this.lastAxisUnit = axisUnit;

        // update the background image and legend:
        updateImage(imageData);

        // update theme at end :
        org.jfree.chart.ChartUtils.applyCurrentTheme(this.chart);

        if (infoTitle != null) {
            // after theme:
            chart.addSubtitle(infoTitle);
        }

        // disable the automatic refresh :
        final boolean prevAutoRefresh = setAutoRefresh(false);
        try {
            // update color scale if changed during image computation (logarithmic to linear):
            this.jComboBoxColorScale.setSelectedItem(imageData.getColorScale());
        } finally {
            // restore the automatic refresh :
            setAutoRefresh(prevAutoRefresh);
        }
    }

    private static TextTitle createText(final String label) {
        return new TextTitle(label, ChartUtils.DEFAULT_FONT_MEDIUM);
    }

    /**
     * Process the zoom event to refresh the image according to the new coordinates
     * @param ze zoom event
     */
    @Override
    public void chartChanged(final ZoomEvent ze) {
        // check if the zoom changed :
        if (!ze.equals(this.lastZoomEvent)) {
            this.lastZoomEvent = ze;

            if (this.getChartData() != null) {
                final Rectangle2D.Double imgRect = getCurrentZoomRect();

                // compute an approximated image from the reference image :
                if (imgRect != null && computeSubImage(this.getChartData(), imgRect)) {
                    final FitsUnit axisUnit = this.lastAxisUnit;

                    // adjust axis bounds to exact viewed rectangle (i.e. avoid rounding errors)
                    ValueAxis axis = this.xyPlot.getDomainAxis();
                    axis.setRange(
                            FitsUnit.ANGLE_RAD.convert(imgRect.getMinX(), axisUnit),
                            FitsUnit.ANGLE_RAD.convert(imgRect.getMaxX(), axisUnit)
                    );
                    axis = this.xyPlot.getRangeAxis();
                    axis.setRange(
                            FitsUnit.ANGLE_RAD.convert(imgRect.getMinY(), axisUnit),
                            FitsUnit.ANGLE_RAD.convert(imgRect.getMaxY(), axisUnit)
                    );
                }
            }
        }
    }

    private Rectangle2D.Double getCurrentZoomRect() {
        final FitsUnit axisUnit = this.lastAxisUnit;
        final ZoomEvent ze = this.lastZoomEvent;

        if (ze == null || axisUnit == null) {
            return null;
        }

        final Rectangle2D.Double imgRect = new Rectangle2D.Double();
        imgRect.setFrameFromDiagonal(
                axisUnit.convert(ze.getDomainLowerBound(), FitsUnit.ANGLE_RAD),
                axisUnit.convert(ze.getRangeLowerBound(), FitsUnit.ANGLE_RAD),
                axisUnit.convert(ze.getDomainUpperBound(), FitsUnit.ANGLE_RAD),
                axisUnit.convert(ze.getRangeUpperBound(), FitsUnit.ANGLE_RAD)
        );
        return imgRect;
    }

    /**
     * Compute a sub image for the image given the new area
     * @param imageData computed image data
     * @param imgRect new image area
     * @return true if the given image rectangle is smaller than rectangle of the reference image
     */
    private boolean computeSubImage(final ImageChartData imageData, final Rectangle2D.Double imgRect) {
        final BufferedImage image = imageData.getImage();

        final int imageWidth = image.getWidth();
        final int imageHeight = image.getHeight();

        // area reference :
        final Rectangle2D.Double imgRectRef = imageData.getImgRectRef();

        if (logger.isDebugEnabled()) {
            logger.debug("image rect     = {}", imgRect);
            logger.debug("image rect REF = {}", imgRectRef);
        }

        final double pixRatioX = ((double) imageWidth) / imgRectRef.getWidth();
        final double pixRatioY = ((double) imageHeight) / imgRectRef.getHeight();

        // note : floor/ceil to be sure to have at least 1x1 pixel image
        int x = (int) Math.floor(pixRatioX * (imgRect.getX() - imgRectRef.getX()));
        int y = (int) Math.floor(pixRatioY * (imgRect.getY() - imgRectRef.getY()));
        int w = (int) Math.ceil(pixRatioX * imgRect.getWidth());
        int h = (int) Math.ceil(pixRatioY * imgRect.getHeight());

        // check bounds:
        x = checkBounds(x, 0, imageWidth - 1);
        y = checkBounds(y, 0, imageHeight - 1);
        w = checkBounds(w, 1, imageWidth - x);
        h = checkBounds(h, 1, imageHeight - y);

        final boolean doCrop = ((x != 0) || (y != 0) || (w != imageWidth) || (h != imageHeight));

        if (logger.isDebugEnabled()) {
            logger.debug("sub image [{}, {} - {}, {}] - doCrop = {}", new Object[]{x, y, w, h, doCrop});
        }

        // check reset zoom to avoid computing sub image == ref image:
        if (doCrop) {
            // adjust rounded data coords:
            logger.debug("image rect (IN) = {}", imgRect);

            imgRect.setRect(
                    ((double) x) / pixRatioX + imgRectRef.getX(),
                    ((double) y) / pixRatioY + imgRectRef.getY(),
                    ((double) w) / pixRatioX,
                    ((double) h) / pixRatioY
            );
            logger.debug("image rect (OUT) = {}", imgRect);

            // Note : the image is processed to stay oriented: North (top ie inverted Y axis) and East (left ie inverted X axis):
            // Inverse X axis issue :
            x = imageWidth - x - w;

            // Inverse Y axis issue :
            y = imageHeight - y - h;

            // crop a small sub image:
            final Image subImage = image.getSubimage(x, y, w, h);

            // update the background image :
            updatePlotImage(subImage);
        } else {
            imgRect.setRect(imgRectRef);

            // update the background image :
            updatePlotImage(image);
        }

        return doCrop;
    }

    /**
     * Update the background image of the chart with the given image and its legend
     * @param imageData computed image data or null
     */
    private void updateImage(final ImageChartData imageData) {

        if (mapLegend != null) {
            this.chart.removeSubtitle(mapLegend);
        }

        if (imageData != null) {
            final double min = imageData.getMin();
            final double max = imageData.getMax();
            final IndexColorModel colorModel = imageData.getColorModel();
            final ColorScale colorScale = imageData.getColorScale();

            final NumberAxis uvMapAxis;
            if (colorScale == ColorScale.LINEAR) {
                uvMapAxis = new NumberAxis();
                if (max < 1e-3d) {
                    uvMapAxis.setNumberFormatOverride(df);
                }
                mapLegend = new PaintScaleLegend(new ColorModelPaintScale(min, max, colorModel, colorScale), uvMapAxis);
            } else {
                uvMapAxis = new LogarithmicAxis(null);
                ((LogarithmicAxis) uvMapAxis).setExpTickLabelsFlag(true);
                mapLegend = new PaintLogScaleLegend(new ColorModelPaintScale(min, max, colorModel, colorScale), uvMapAxis);
            }

            uvMapAxis.setTickLabelFont(ChartUtils.DEFAULT_FONT);
            uvMapAxis.setAxisLinePaint(Color.BLACK);
            uvMapAxis.setTickMarkPaint(Color.BLACK);

            mapLegend.setPosition(RectangleEdge.LEFT);
            mapLegend.setStripWidth(15d);
            mapLegend.setStripOutlinePaint(Color.BLACK);
            mapLegend.setStripOutlineVisible(true);
            mapLegend.setSubdivisionCount(colorModel.getMapSize());
            mapLegend.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
            mapLegend.setFrame(new BlockBorder(Color.BLACK));
            mapLegend.setMargin(1d, 1d, 1d, 1d);
            mapLegend.setPadding(10d, 10d, 10d, 10d);

            this.chart.addSubtitle(mapLegend);

            updatePlotImage(imageData.getImage());

        } else {
            updatePlotImage(null);
        }
    }

    /**
     * Update the background image of the chart with the given image
     * @param image image or null
     */
    private void updatePlotImage(final Image image) {
        if (image != null) {
            // check that the uvMap is different than currently displayed one:
            final Image bckgImg = this.xyPlot.getBackgroundImage();
            if (image != bckgImg) {
                // Recycle previous image:
                if (bckgImg instanceof BufferedImage) {
                    final BufferedImage bi = (BufferedImage) bckgImg;
                    // avoid sub images (child raster):
                    if (bi.getRaster().getParent() == null
                            && this.chartData != null && this.chartData.getImage() != null) {
                        // check if this is the reference image:
                        if (bckgImg != this.chartData.getImage()) {
                            // recycle previous images:
                            ImageUtils.recycleImage(bi);
                        }
                    }
                }
                if (logger.isDebugEnabled() && image instanceof BufferedImage) {
                    final BufferedImage bi = (BufferedImage) image;
                    logger.debug("display Image[{} x {}] @ {}", bi.getWidth(), bi.getHeight(), bi.hashCode());
                }
                this.xyPlot.setBackgroundPaint(null);
                this.xyPlot.setBackgroundImage(image);
            }
        } else {
            this.xyPlot.setBackgroundPaint(Color.lightGray);
            this.xyPlot.setBackgroundImage(null);
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonDisplayKeywords;
    private javax.swing.JButton jButtonOrig;
    private javax.swing.JButton jButtonPick;
    private javax.swing.JButton jButtonResampleFovUpdate;
    private javax.swing.JComboBox jComboBoxColorScale;
    private javax.swing.JComboBox jComboBoxFilter;
    private javax.swing.JComboBox jComboBoxLUT;
    private javax.swing.JFormattedTextField jFormattedTextFieldDEMax;
    private javax.swing.JFormattedTextField jFormattedTextFieldDEMin;
    private javax.swing.JFormattedTextField jFormattedTextFieldNewSize;
    private javax.swing.JFormattedTextField jFormattedTextFieldRAMax;
    private javax.swing.JFormattedTextField jFormattedTextFieldRAMin;
    private javax.swing.JFormattedTextField jFormattedTextFieldResampleFov;
    private javax.swing.JFormattedTextField jFormattedTextFieldRescaleFov;
    private javax.swing.JFormattedTextField jFormattedTextFieldScaleX;
    private javax.swing.JLabel jLabelColorScale;
    private javax.swing.JLabel jLabelDeltaDE;
    private javax.swing.JLabel jLabelDeltaRA;
    private javax.swing.JLabel jLabelFilter;
    private javax.swing.JLabel jLabelInfo;
    private javax.swing.JLabel jLabelLutTable;
    private javax.swing.JLabel jLabelNewSize;
    private javax.swing.JLabel jLabelOldSize;
    private javax.swing.JLabel jLabelResampleFov;
    private javax.swing.JLabel jLabelRescale;
    private javax.swing.JLabel jLabelRescaleFov;
    private javax.swing.JLabel jLabelScale;
    private javax.swing.JLabel jLabelViewport;
    private javax.swing.JPanel jPanelOptions;
    private javax.swing.JPanel jPanelResample;
    private javax.swing.JPanel jPanelRescale;
    private javax.swing.JPanel jPanelViewport;
    // End of variables declaration//GEN-END:variables
    /** drawing started time value */
    private long chartDrawStartTime = 0l;

    /**
     * Handle the chart progress event to log the chart rendering delay
     * @param event chart progress event
     */
    @Override
    public void chartProgress(final ChartProgressEvent event) {
        if (logger.isDebugEnabled()) {
            switch (event.getType()) {
                case ChartProgressEvent.DRAWING_STARTED:
                    this.chartDrawStartTime = System.nanoTime();
                    break;
                case ChartProgressEvent.DRAWING_FINISHED:
                    logger.debug("Drawing chart time = {} ms.", 1e-6d * (System.nanoTime() - this.chartDrawStartTime));
                    this.chartDrawStartTime = 0l;
                    break;
                default:
            }
        }
    }

    /**
     * Enable / Disable the automatic refresh of the plot when any swing component changes.
     * Return its previous value.
     *
     * Typical use is as following :
     * // disable the automatic refresh :
     * final boolean prevAutoRefresh = this.setAutoRefresh(false);
     * try {
     *   // operations ...
     *
     * } finally {
     *   // restore the automatic refresh :
     *   this.setAutoRefresh(prevAutoRefresh);
     * }
     *
     * @param value new value
     * @return previous value
     */
    private boolean setAutoRefresh(final boolean value) {
        // first backup the state of the automatic update observation :
        final boolean previous = this.doAutoRefresh;

        // then change its state :
        this.doAutoRefresh = value;

        // return previous state :
        return previous;
    }

    /**
     * This class contains image data (fits image, image, colorModel ...) for consistency
     */
    private static final class ImageChartData {

        /** fits image */
        private final FitsImage fitsImage;
        /** image color model */
        private final IndexColorModel colorModel;
        /** java2D image */
        private final BufferedImage image;
        /** color scaling method */
        private final ColorScale colorScale;
        /** image interpolation */
        private final ImageInterpolation interpolation;
        /** minimum value used by color conversion */
        private final float min;
        /** maximum value used by color conversion */
        private final float max;
        /** image physical area */
        private final Rectangle2D.Double imgRectRef;

        /**
         * Protected constructor
         * @param fitsImage fits image
         * @param colorModel image color model
         * @param colorScale color scaling method
         * @param interpolation image interpolation
         * @param min minimum value used by color conversion
         * @param max maximum value used by color conversion
         * @param image java2D image
         * @param imgRectRef image physical area
         */
        ImageChartData(final FitsImage fitsImage, final IndexColorModel colorModel, final ColorScale colorScale,
                       final ImageInterpolation interpolation,
                       final float min, final float max,
                       final BufferedImage image, final Rectangle2D.Double imgRectRef) {
            this.fitsImage = fitsImage;
            this.colorModel = colorModel;
            this.colorScale = colorScale;
            this.interpolation = interpolation;
            this.min = min;
            this.max = max;
            this.image = image;
            this.imgRectRef = imgRectRef;
        }

        public boolean isCompatible(final IndexColorModel colorModel, final ColorScale colorScale,
                                    final ImageInterpolation interpolation) {
            return (getColorModel() == colorModel && getColorScale() == colorScale && getInterpolation() == interpolation);
        }

        /**
         * Return the fits image
         * @return fits image
         */
        FitsImage getFitsImage() {
            return fitsImage;
        }

        /**
         * Return the image color model
         * @return image color model
         */
        IndexColorModel getColorModel() {
            return colorModel;
        }

        /**
         * Return the color scaling method
         * @return color scaling method
         */
        ColorScale getColorScale() {
            return colorScale;
        }

        /**
         * Return the image interpolation
         * @return image interpolation
         */
        public ImageInterpolation getInterpolation() {
            return interpolation;
        }

        /**
         * Return the java2D image
         * @return java2D image
         */
        BufferedImage getImage() {
            return image;
        }

        /**
         * Return the minimum value used by color conversion
         * @return minimum value used by color conversion
         */
        public float getMin() {
            return min;
        }

        /**
         * Return the maximum value used by color conversion
         * @return maximum value used by color conversion
         */
        public float getMax() {
            return max;
        }

        /**
         * Return the image physical area
         * @return image physical area
         */
        public Rectangle2D.Double getImgRectRef() {
            return imgRectRef;
        }
    }

    private static double parseDouble(final String text) {
        if (!StringUtils.isEmpty(text)) {
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException nfe) {
                logger.debug("Bad value: ", nfe);
            }
        }
        return Double.NaN;
    }
}
