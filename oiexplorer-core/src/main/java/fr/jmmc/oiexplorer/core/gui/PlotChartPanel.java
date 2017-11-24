/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui;

import fr.jmmc.jmal.image.ColorModels;
import fr.jmmc.jmal.image.ColorScale;
import fr.jmmc.jmal.image.ImageUtils;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.jmcs.util.StringUtils;
import fr.jmmc.oiexplorer.core.export.DocumentExportable;
import fr.jmmc.oiexplorer.core.export.DocumentOptions;
import fr.jmmc.oiexplorer.core.function.Converter;
import fr.jmmc.oiexplorer.core.function.ConverterFactory;
import fr.jmmc.oiexplorer.core.gui.action.ExportDocumentAction;
import fr.jmmc.oiexplorer.core.gui.chart.BoundedLogAxis;
import fr.jmmc.oiexplorer.core.gui.chart.BoundedNumberAxis;
import fr.jmmc.oiexplorer.core.gui.chart.ChartMouseSelectionListener;
import fr.jmmc.oiexplorer.core.gui.chart.ChartUtils;
import fr.jmmc.oiexplorer.core.gui.chart.ColorModelPaintScale;
import fr.jmmc.oiexplorer.core.gui.chart.CombinedCrosshairOverlay;
import fr.jmmc.oiexplorer.core.gui.chart.EnhancedChartMouseListener;
import fr.jmmc.oiexplorer.core.gui.chart.EnhancedCombinedDomainXYPlot;
import fr.jmmc.oiexplorer.core.gui.chart.FastXYErrorRenderer;
import fr.jmmc.oiexplorer.core.gui.chart.SelectionOverlay;
import fr.jmmc.oiexplorer.core.gui.chart.dataset.FastIntervalXYDataset;
import fr.jmmc.oiexplorer.core.gui.chart.dataset.OITableSerieKey;
import fr.jmmc.oiexplorer.core.gui.chart.dataset.SharedSeriesAttributes;
import fr.jmmc.oiexplorer.core.gui.selection.DataPoint;
import fr.jmmc.oiexplorer.core.gui.selection.DataPointInfo;
import fr.jmmc.oiexplorer.core.gui.selection.DataPointer;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEvent;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventListener;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventType;
import fr.jmmc.oiexplorer.core.model.oi.Plot;
import fr.jmmc.oiexplorer.core.model.plot.Axis;
import fr.jmmc.oiexplorer.core.model.plot.AxisRangeMode;
import fr.jmmc.oiexplorer.core.model.plot.ColorMapping;
import static fr.jmmc.oiexplorer.core.model.plot.ColorMapping.CONFIGURATION;
import static fr.jmmc.oiexplorer.core.model.plot.ColorMapping.OBSERVATION_DATE;
import static fr.jmmc.oiexplorer.core.model.plot.ColorMapping.STATION_INDEX;
import static fr.jmmc.oiexplorer.core.model.plot.ColorMapping.WAVELENGTH_RANGE;
import fr.jmmc.oiexplorer.core.model.plot.PlotDefinition;
import fr.jmmc.oiexplorer.core.model.util.StationNamesComparator;
import fr.jmmc.oiexplorer.core.util.Constants;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.DataRange;
import fr.jmmc.oitools.meta.Units;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsFile;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.IndexColorModel;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Drawable;
import org.jfree.ui.Layer;
import org.jfree.ui.RectangleEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This panel provides the chart panel representing one OIFitsExplorer plot instance (using its subset and plot definition)
 *
 * @author bourgesl
 */
public final class PlotChartPanel extends javax.swing.JPanel implements ChartProgressListener, EnhancedChartMouseListener, ChartMouseSelectionListener,
        DocumentExportable, OIFitsCollectionManagerEventListener {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Debug setting */
    private static final boolean DEBUG = false;
    /** use plot (true) or overlay (false) crosshair support (faster is overlay) */
    private static final boolean usePlotCrossHairSupport = false;
    /** enable mouse selection handling (DEV) TODO: enable selection ASAP (TODO sub plot support) */
    private static final boolean useSelectionSupport = false;
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(PlotChartPanel.class.getName());
    /** data margin in percents (5%) */
    private final static double MARGIN_PERCENTS = 5.0 / 100.0;
    /** double formatter for wave lengths */
    private final static NumberFormat df4 = new DecimalFormat("0.000#");
    /** double formatter for other values */
    private final static NumberFormat df2 = new DecimalFormat("0.00");
    public static final float SYMBOL_SCALE = 1.0f;
    public final static double LAMBDA_EPSILON = 1e-10; // 0.1 nm

    /* shared point shapes */
    private static final Shape shapePointValid;
    private static final Shape shapePointInvalid;

    static {
        // initialize point shapes:
        shapePointValid = new Rectangle(
                scale(-3), scale(-3),
                scale(6), scale(6)) {
            /** default serial UID for Serializable interface */
            private static final long serialVersionUID = 1L;

            /**
             * Overriden to return the same Rectangle2D instance
             */
            @Override
            public Rectangle2D getBounds2D() {
                return this;
            }
        };

        // equilateral triangle centered on its barycenter:
        final int npoints = 3;
        final int[] xpoints = new int[npoints];
        final int[] ypoints = new int[npoints];
        xpoints[0] = 0;
        ypoints[0] = scale(-4);
        xpoints[1] = scale(3);
        ypoints[1] = scale(2);
        xpoints[2] = scale(-3);
        ypoints[2] = scale(2);

        shapePointInvalid = new Polygon(xpoints, ypoints, npoints) {
            /** default serial UID for Serializable interface */
            private static final long serialVersionUID = 1L;

            /**
             * Overriden to return the cached bounds instance
             */
            @Override
            public Rectangle2D getBounds2D() {
                if (bounds != null) {
                    return bounds;
                }
                return super.getBounds2D();
            }
        };
    }

    private static int scale(final int v) {
        return Math.round(ChartUtils.scaleUI(SYMBOL_SCALE * v));
    }

    /* members */
    /** OIFitsCollectionManager singleton */
    private final OIFitsCollectionManager ocm = OIFitsCollectionManager.getInstance();
    /** ConverterFactory singleton */
    private final ConverterFactory cf = ConverterFactory.getInstance();
    /** plot identifier */
    private String plotId = null;
    /** plot object reference (read only) */
    private Plot plot = null;
    /** plot information(s) */
    private final List<PlotInfo> plotInfos = new ArrayList<PlotInfo>();
    /* plot data */
    /** jFreeChart instance */
    private JFreeChart chart;
    /** combined xy plot sharing domain axis */
    private CombinedDomainXYPlot combinedXYPlot;
    /** unmodifiable subplot list from the combined xy plot */
    private List combinedXYPlotList;
    /** mapping between xy plot and subplot index */
    private Map<XYPlot, Integer> plotMapping = new IdentityHashMap<XYPlot, Integer>();
    /** mapping between subplot index and xy plot (reverse) */
    private Map<Integer, XYPlot> plotIndexMapping = new HashMap<Integer, XYPlot>();
    /** chart panel */
    private ChartPanel chartPanel;
    /** crosshair overlay */
    private CombinedCrosshairOverlay crosshairOverlay = null;
    /** selection overlay */
    private SelectionOverlay selectionOverlay = null;
    /** xy plot instances */
    private List<XYPlot> xyPlotList = new ArrayList<XYPlot>();
    /** JMMC annotation */
    private final List<XYTextAnnotation> aJMMCPlots = new ArrayList<XYTextAnnotation>();
    /** last mouse event */
    private ChartMouseEvent lastChartMouseEvent = null;
    /** wavelength scale legend */
    private PaintScaleLegend mapLegend = null;
    /** color model for the wavelength range */
    private final IndexColorModel colorModel = ColorModels.getColorModel(ColorModels.COLOR_MODEL_RAINBOW_ALPHA);

    /**
     * Constructor
     */
    public PlotChartPanel() {
        ocm.getPlotChangedEventNotifier().register(this);
        ocm.getSelectionChangedEventNotifier().register(this);

        initComponents();
        postInit();
    }

    /**
     * Free any ressource or reference to this instance :
     * remove this instance from OIFitsCollectionManager event notifiers
     */
    @Override
    public void dispose() {
        if (logger.isDebugEnabled()) {
            logger.debug("dispose: {}", ObjectUtils.getObjectInfo(this));
        }
        ocm.unbind(this);
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

        jLabelNoData = new javax.swing.JLabel();
        jPanelInfos = new javax.swing.JPanel();
        jPanelCrosshair = new javax.swing.JPanel();
        jButtonHideCrossHair = new javax.swing.JButton();
        jLabelCrosshairInfos = new javax.swing.JLabel();
        jSeparatorHoriz = new javax.swing.JSeparator();
        jPanelMouseInfos = new javax.swing.JPanel();
        jLabelInfos = new javax.swing.JLabel();
        jLabelPoints = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jLabelDataRange = new javax.swing.JLabel();
        jSeparator2 = new javax.swing.JSeparator();
        jLabelDataErrRange = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jLabelMouse = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setCursor(new java.awt.Cursor(java.awt.Cursor.CROSSHAIR_CURSOR));
        setLayout(new java.awt.BorderLayout());

        jLabelNoData.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelNoData.setText("No data to plot.");
        add(jLabelNoData, java.awt.BorderLayout.PAGE_START);

        jPanelInfos.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanelInfos.setLayout(new java.awt.GridBagLayout());

        jPanelCrosshair.setLayout(new java.awt.GridBagLayout());

        jButtonHideCrossHair.setText("hide");
        jButtonHideCrossHair.setToolTipText("Hide the crosshair and its contextual information");
        jButtonHideCrossHair.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButtonHideCrossHair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHideCrossHairActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelCrosshair.add(jButtonHideCrossHair, gridBagConstraints);

        jLabelCrosshairInfos.setText("tooltip");
        jLabelCrosshairInfos.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 6, 2, 2);
        jPanelCrosshair.add(jLabelCrosshairInfos, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanelCrosshair.add(jSeparatorHoriz, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanelInfos.add(jPanelCrosshair, gridBagConstraints);

        jPanelMouseInfos.setLayout(new java.awt.GridBagLayout());

        jLabelInfos.setText("Infos:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelMouseInfos.add(jLabelInfos, gridBagConstraints);

        jLabelPoints.setText("points");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelMouseInfos.add(jLabelPoints, gridBagConstraints);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelMouseInfos.add(jSeparator1, gridBagConstraints);

        jLabelDataRange.setText("data");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelMouseInfos.add(jLabelDataRange, gridBagConstraints);

        jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelMouseInfos.add(jSeparator2, gridBagConstraints);

        jLabelDataErrRange.setText("data+error");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelMouseInfos.add(jLabelDataErrRange, gridBagConstraints);

        jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelMouseInfos.add(jSeparator3, gridBagConstraints);

        jLabelMouse.setText("[mouse]");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.05;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelMouseInfos.add(jLabelMouse, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        jPanelInfos.add(jPanelMouseInfos, gridBagConstraints);

        add(jPanelInfos, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonHideCrossHairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHideCrossHairActionPerformed
        resetCrosshairOverlay();
    }//GEN-LAST:event_jButtonHideCrossHairActionPerformed

    /**
     * Export the chart component as aF document
     */
    @Override
    public void performAction(final ExportDocumentAction action) {
        // if no OIFits data, discard action:
        if (canExportPlotFile()) {
            action.process(this);
        }
    }

    public boolean canExportPlotFile() {
        return (getOiFitsSubset() != null && isHasData());
    }

    /**
     * Return the default file name
     * [Vis2_<TARGET>_<INSTRUMENT>_<CONFIGURATION>_<DATE>]
     * @return default file name
     */
    @Override
    public String getDefaultFileName(final String fileExtension) {

        // TODO: keep values from dataset ONLY:
        // - arrName, insName, dateObs (keywords) = OK
        // - baselines or configurations (rows) = KO ... IF HAS DATA (filtered)
        if (isHasData()) {
            final Set<String> distinct = new LinkedHashSet<String>();

            final StringBuilder sb = new StringBuilder(32);
            AxisInfo axisInfo;

            // add Y axes:
            for (PlotInfo info : getPlotInfos()) {
                axisInfo = info.yAxisInfo;
                distinct.add((axisInfo.useLog) ? "log_" + axisInfo.columnMeta.getName() : axisInfo.columnMeta.getName());
            }
            if (!distinct.isEmpty()) {
                toString(distinct, sb, "_", "_");
            }

            sb.append("_vs_");

            // add X axis:
            axisInfo = getFirstPlotInfo().xAxisInfo;
            sb.append((axisInfo.useLog) ? "log_" + axisInfo.columnMeta.getName() : axisInfo.columnMeta.getName());
            sb.append('_');

            // Add target name:
            final String altName = StringUtils.replaceNonAlphaNumericCharsByUnderscore(getTargetName());

            sb.append(altName).append('_');

            // Add distinct arrNames:
            final GetOIDataString arrNameOperator = new GetOIDataString() {
                @Override
                public String getString(final OIData oiData) {
                    return oiData.getArrName();
                }
            };

            distinct.clear();
            for (PlotInfo info : getPlotInfos()) {
                getDistinct(info.oidataList, distinct, arrNameOperator);
            }
            if (!distinct.isEmpty()) {
                toString(distinct, sb, "_", "_", 3, "MULTI_ARRNAME");
            }

            sb.append('_');

            // Add unique insNames:
            final GetOIDataString insNameOperator = new GetOIDataString() {
                @Override
                public String getString(final OIData oiData) {
                    return oiData.getInsName();
                }
            };

            distinct.clear();
            for (PlotInfo info : getPlotInfos()) {
                getDistinct(info.oidataList, distinct, insNameOperator);
            }
            if (!distinct.isEmpty()) {
                toString(distinct, sb, "_", "_", 3, "MULTI_INSNAME");
            }

            sb.append('_');

            // Add unique configurations (FILTERED):
            distinct.clear();
            for (PlotInfo info : getPlotInfos()) {
                distinct.addAll(info.usedStaConfNames);
            }
            if (!distinct.isEmpty()) {
                toString(distinct, sb, "-", "_", 3, "MULTI_CONF");
            }

            sb.append('_');

            // Add unique dateObs:
            final GetOIDataString dateObsOperator = new GetOIDataString() {
                @Override
                public String getString(final OIData oiData) {
                    return oiData.getDateObs();
                }
            };

            distinct.clear();
            for (PlotInfo info : getPlotInfos()) {
                getDistinct(info.oidataList, distinct, dateObsOperator);
            }
            if (!distinct.isEmpty()) {
                toString(distinct, sb, "_", "_", 3, "MULTI_DATE");
            }

            sb.append('.').append(fileExtension);

            return sb.toString();
        }
        return null;
    }

    /**
     * Prepare the page layout before doing the export:
     * Performs layout and modifies the given options
     * @param options document options used to prepare the document
     */
    @Override
    public void prepareExport(final DocumentOptions options) {
        options.setNormalDefaults();
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
     * Callback indicating the document is done to reset the component's state
     */
    @Override
    public void postExport() {
        // no-op
    }

    /**
     * This method is useful to set the models and specific features of initialized swing components :
     */
    private void postInit() {

        this.jPanelCrosshair.setVisible(false);

        // create chart and add listener :
        this.combinedXYPlot = new EnhancedCombinedDomainXYPlot(ChartUtils.createAxis(""));
        this.combinedXYPlot.setGap(10.0D);
        this.combinedXYPlot.setOrientation(PlotOrientation.VERTICAL);

        // enlarge right margin to have last displayed value:
        this.combinedXYPlot.setInsets(ChartUtils.NORMAL_PLOT_INSETS);

        configureCrosshair(this.combinedXYPlot, usePlotCrossHairSupport);

        // get read-only subplot list:
        this.combinedXYPlotList = this.combinedXYPlot.getSubplots();

        this.chart = ChartUtils.createChart(null, this.combinedXYPlot, true);
        this.chart.addProgressListener(this);
        this.chartPanel = ChartUtils.createChartPanel(this.chart, false);

        // zoom options :
        this.chartPanel.setDomainZoomable(Constants.ENABLE_ZOOM);
        this.chartPanel.setRangeZoomable(Constants.ENABLE_ZOOM);

        // enable mouse wheel:
        this.chartPanel.setMouseWheelEnabled(true);

        if (useSelectionSupport) {
            this.selectionOverlay = new SelectionOverlay(this.chartPanel, this);
            this.chartPanel.addOverlay(this.selectionOverlay);
        }

        if (!usePlotCrossHairSupport) {
            this.crosshairOverlay = new CombinedCrosshairOverlay();
            this.chartPanel.addOverlay(crosshairOverlay);
        }

        if (useSelectionSupport || !usePlotCrossHairSupport) {
            this.chartPanel.addChartMouseListener(this);
        }

        this.add(this.chartPanel, BorderLayout.CENTER);

        // Create sub plots (2 by default):
        addXYPlot();

        resetPlot();
    }

    private void addXYPlot() {
        final XYTextAnnotation aJMMCPlot = ChartUtils.createJMMCAnnotation(Constants.JMMC_ANNOTATION);

        final XYPlot xyPlot = createScientificScatterPlot(null, "", usePlotCrossHairSupport);
        xyPlot.getRenderer().addAnnotation(aJMMCPlot, Layer.BACKGROUND);

        final int size = this.xyPlotList.size();

        // add plot and its annotation:
        this.xyPlotList.add(xyPlot);
        this.aJMMCPlots.add(aJMMCPlot);

        if (!usePlotCrossHairSupport) {
            // enable overlay crosshair support:
            final Integer plotIndex = NumberUtils.valueOf(size);
            crosshairOverlay.addDomainCrosshair(plotIndex, createCrosshair());
            crosshairOverlay.addRangeCrosshair(plotIndex, createCrosshair());
        }
    }

    public JFreeChart getChart() {
        return this.chart;
    }

    public CombinedCrosshairOverlay getCrosshairOverlay() {
        return this.crosshairOverlay;
    }

    private static Crosshair createCrosshair() {
        final Crosshair crosshair = new Crosshair(Double.NaN);
        // crosshair.setPaint(Color.BLUE);
        crosshair.setLabelVisible(true);
        crosshair.setLabelFont(ChartUtils.DEFAULT_TEXT_SMALL_FONT);
        crosshair.setLabelBackgroundPaint(new Color(255, 255, 0, 200));
        return crosshair;
    }

    /**
     * Create custom scatter plot with several display options (error renderer)
     * @param xAxisLabel x axis label
     * @param yAxisLabel y axis label
     * @param usePlotCrossHairSupport flag to use internal crosshair support on plot
     * @return xy plot
     */
    private static XYPlot createScientificScatterPlot(final String xAxisLabel, final String yAxisLabel, final boolean usePlotCrossHairSupport) {

        final XYPlot xyPlot = ChartUtils.createScatterPlot(null, xAxisLabel, yAxisLabel, null, PlotOrientation.VERTICAL, false, false);

        // display axes at [0,0] :
        xyPlot.setDomainZeroBaselineVisible(true);
        xyPlot.setRangeZeroBaselineVisible(true);

        configureCrosshair(xyPlot, usePlotCrossHairSupport);

        final FastXYErrorRenderer renderer = (FastXYErrorRenderer) xyPlot.getRenderer();

        // force to use the base shape
        renderer.setAutoPopulateSeriesShape(false);

        // reset colors :
        renderer.clearSeriesPaints(false);
        // side effect with chart theme :
        renderer.setAutoPopulateSeriesPaint(false);

        // set renderer options for ALL series (performance):
        renderer.setShapesVisible(true);
        renderer.setShapesFilled(true);
        renderer.setDrawOutlines(false);

        // define error bar settings:
        renderer.setErrorStroke(AbstractRenderer.DEFAULT_STROKE);
        renderer.setCapLength(0d);
        renderer.setErrorPaint(new Color(192, 192, 192, 128));

        return xyPlot;
    }

    private static void configureCrosshair(final XYPlot plot, final boolean usePlotCrossHairSupport) {
        // configure xyplot or overlay crosshairs:
        plot.setDomainCrosshairLockedOnData(usePlotCrossHairSupport);
        plot.setDomainCrosshairVisible(usePlotCrossHairSupport);

        plot.setRangeCrosshairLockedOnData(usePlotCrossHairSupport);
        plot.setRangeCrosshairVisible(usePlotCrossHairSupport);
    }

    /* EnhancedChartMouseListener implementation */
    /**
     * Return true if this listener implements / uses this mouse event type
     * @param eventType mouse event type
     * @return true if this listener implements / uses this mouse event type
     */
    @Override
    public boolean support(final int eventType) {
        return true;
    }

    /**
     * Handle click on plot
     * @param chartMouseEvent chart mouse event
     */
    @Override
    public void chartMouseClicked(final ChartMouseEvent chartMouseEvent) {
        final int i = chartMouseEvent.getTrigger().getX();
        final int j = chartMouseEvent.getTrigger().getY();

        if (this.chartPanel.getScreenDataArea().contains(i, j)) {
            final Point2D point2D = this.chartPanel.translateScreenToJava2D(new Point(i, j));

            final PlotRenderingInfo plotInfo = this.chartPanel.getChartRenderingInfo().getPlotInfo();

            final int subplotIndex = plotInfo.getSubplotIndex(point2D);
            if (subplotIndex == -1) {
                return;
            }

            // data area for sub plot:
            final Rectangle2D dataArea = plotInfo.getSubplotInfo(subplotIndex).getDataArea();

            final Integer plotIndex = NumberUtils.valueOf(subplotIndex);

            final XYPlot xyPlot = this.plotIndexMapping.get(plotIndex);
            if (xyPlot == null) {
                return;
            }
            final PlotInfo info = getPlotInfos().get(subplotIndex);

            final double px = point2D.getX();
            final double py = point2D.getY();

            final ValueAxis domainAxis = xyPlot.getDomainAxis();
            final double domainValue = domainAxis.java2DToValue(px, dataArea, xyPlot.getDomainAxisEdge());

            final ValueAxis rangeAxis = xyPlot.getRangeAxis();
            final double rangeValue = rangeAxis.java2DToValue(py, dataArea, xyPlot.getRangeAxisEdge());

            if (logger.isDebugEnabled()) {
                logger.debug("Mouse coordinates are (" + i + ", " + j + "), in data space = (" + domainValue + ", " + rangeValue + ")");
            }

            // Use local approximation (arround anchor) of the scaling ratios
            // providing a good affinity with logarithmic axes:
            final double xRatio = 2.0 / Math.abs(
                    domainAxis.java2DToValue(px + 1.0, dataArea, xyPlot.getDomainAxisEdge())
                    - domainAxis.java2DToValue(px - 1.0, dataArea, xyPlot.getDomainAxisEdge())
            );

            final double yRatio = 2.0 / Math.abs(
                    rangeAxis.java2DToValue(py + 1.0, dataArea, xyPlot.getRangeAxisEdge())
                    - rangeAxis.java2DToValue(py - 1.0, dataArea, xyPlot.getRangeAxisEdge())
            );

            // find matching data ie. closest data point according to its screen distance to the mouse clicked point:
            final DataPoint dataPoint = findDataPoint(info, xyPlot, domainValue, rangeValue, xRatio, yRatio);

            if (dataPoint != DataPoint.UNDEFINED) {
                updateCrosshairs(dataPoint);
            }
        }
    }

    private void updateCrosshairs(final DataPoint dataPoint) {
        // update other plot crosshairs:
        for (Integer index : this.plotIndexMapping.keySet()) {
            List<Crosshair> xCrosshairs = this.crosshairOverlay.getDomainCrosshairs(index);
            List<Crosshair> yCrosshairs = this.crosshairOverlay.getRangeCrosshairs(index);

            final XYPlot xyPlot = this.plotIndexMapping.get(index);
            final PlotInfo info = getPlotInfos().get(index);

            if (xyPlot != null && info != null) {
                if (xCrosshairs.size() == 1) {
                    // check field:
                    xCrosshairs.get(0).setValue(
                            (dataPoint.getxAxisInfo().isCompatible(info.xAxisInfo)) ? dataPoint.getX() : Double.NaN
                    );
                }
                if (yCrosshairs.size() == 1) {
                    // check field:
                    yCrosshairs.get(0).setValue(
                            (dataPoint.getyAxisInfo().isCompatible(info.yAxisInfo)) ? dataPoint.getY() : Double.NaN
                    );
                }
            }
        }

        if (dataPoint instanceof DataPointInfo) {
            this.jPanelCrosshair.setVisible(true);

            // TODO: memorize the data pointer to be able to restore it after plot refresh ?
            final DataPointer ptr = ((DataPointInfo) dataPoint).getDataPointer();

            // memorize the last data pointer:
            ocm.setSelection(this, ptr);

            final String textInfo = "<html>"
                    + " ArrName: " + ptr.getArrName()
                    + " | InsName: " + ptr.getInsName()
                    + " | Date: " + ptr.getOiData().getDateObs()
                    + " | Baseline: " + ptr.getStaIndexName()
                    + " | Config: " + ptr.getStaConfName()
                    + " | Target: " + ptr.getTarget()
                    + "<br>"
                    + ((!Float.isNaN(ptr.getWaveLength()))
                    ? "Wavelength: " + df4.format(ConverterFactory.CONVERTER_MICRO_METER.evaluate(ptr.getWaveLength())) + ' ' + ConverterFactory.CONVERTER_MICRO_METER.getUnit()
                    + ((!Double.isNaN(ptr.getSpatialFreq()))
                    ? " | Spatial Freq: " + df2.format(ConverterFactory.CONVERTER_MEGA_LAMBDA.evaluate(ptr.getSpatialFreq())) + ' ' + ConverterFactory.CONVERTER_MEGA_LAMBDA.getUnit()
                    : "") + " | "
                    : "")
                    + ((!Double.isNaN(ptr.getRadius()))
                    ? "Radius: " + df2.format(ptr.getRadius()) + ' ' + Units.UNIT_METER.getStandardRepresentation()
                    + " | Pos. angle: " + df2.format(ptr.getPosAngle()) + ' ' + Units.UNIT_DEGREE.getStandardRepresentation()
                    : "")
                    + ((!Double.isNaN(ptr.getHourAngle()))
                    ? " | Hour angle: " + df2.format(ptr.getHourAngle()) + ' ' + Units.UNIT_HOUR.getStandardRepresentation()
                    : "")
                    + "<br>Table: " + ptr.getOiData().idToString()
                    + " | Row: " + ptr.getRow()
                    + " | Col: " + ptr.getCol()
                    + " | File: " + ptr.getOIFitsFileName()
                    + "</html>";

            this.jLabelCrosshairInfos.setText(textInfo);
        }
    }

    private void refreshCrosshair(final DataPointer selPtr) {
        if (selPtr != null) {
            logger.debug("refreshCrosshair: plot {} lookup for data pointer: {}", this.plotId, selPtr);

            // find matching data ie. closest data point according to its screen distance to the mouse clicked point:
            final DataPoint dataPoint = findDataPoint(selPtr);

            if (dataPoint == DataPoint.UNDEFINED) {
                resetCrosshairOverlay();
            } else {
                updateCrosshairs(dataPoint);
            }
        }
    }

    /**
     * Update data depending on the mouse position (plot info)
     * @param chartMouseEvent useless
     */
    @Override
    public void chartMouseMoved(final ChartMouseEvent chartMouseEvent) {
        this.lastChartMouseEvent = chartMouseEvent;

        int subplotIndex = -1;
        double domainValue = Double.NaN;
        double rangeValue = Double.NaN;

        // Ensure PlotInfos are defined (non empty plot):
        // may happen when called by chartProgress(lastChartMouseEvent) (EDT later)
        if (isHasData()) {
            final int i = chartMouseEvent.getTrigger().getX();
            final int j = chartMouseEvent.getTrigger().getY();

            if (this.chartPanel.getScreenDataArea().contains(i, j)) {
                final Point2D point2D = this.chartPanel.translateScreenToJava2D(new Point(i, j));

                final PlotRenderingInfo plotInfo = this.chartPanel.getChartRenderingInfo().getPlotInfo();

                subplotIndex = plotInfo.getSubplotIndex(point2D);
                if (subplotIndex != -1) {
                    // data area for sub plot:
                    final Rectangle2D dataArea = plotInfo.getSubplotInfo(subplotIndex).getDataArea();

                    final Integer plotIndex = NumberUtils.valueOf(subplotIndex);

                    final XYPlot xyPlot = this.plotIndexMapping.get(plotIndex);
                    if (xyPlot != null) {
                        final ValueAxis domainAxis = xyPlot.getDomainAxis();
                        domainValue = domainAxis.java2DToValue(point2D.getX(), dataArea, xyPlot.getDomainAxisEdge());

                        final ValueAxis rangeAxis = xyPlot.getRangeAxis();
                        rangeValue = rangeAxis.java2DToValue(point2D.getY(), dataArea, xyPlot.getRangeAxisEdge());

                        if (logger.isDebugEnabled()) {
                            logger.debug("Mouse coordinates are (" + i + ", " + j + "), in data space = (" + domainValue + ", " + rangeValue + ")");
                        }
                    }
                }
            }
        }

        String infoMouse = "";
        String infoPoints = "";
        String infoDataRange = "";
        String infoDataErrRange = "";

        if (subplotIndex != -1) {
            final PlotInfo info = getPlotInfos().get(subplotIndex);
            infoMouse = String.format("[%s, %s]", NumberUtils.format(domainValue), NumberUtils.format(rangeValue));
            infoPoints = String.format("%d / %d points", info.nDisplayedPoints, info.nDataPoints);
            infoDataRange = String.format("Data: X[%s, %s] Y[%s, %s]",
                    NumberUtils.format(info.xAxisInfo.dataRange.getLowerBound()), NumberUtils.format(info.xAxisInfo.dataRange.getUpperBound()),
                    NumberUtils.format(info.yAxisInfo.dataRange.getLowerBound()), NumberUtils.format(info.yAxisInfo.dataRange.getUpperBound())
            );
            infoDataErrRange = String.format("Data+Err: X[%s, %s] Y[%s, %s]",
                    NumberUtils.format(info.xAxisInfo.dataErrRange.getLowerBound()), NumberUtils.format(info.xAxisInfo.dataErrRange.getUpperBound()),
                    NumberUtils.format(info.yAxisInfo.dataErrRange.getLowerBound()), NumberUtils.format(info.yAxisInfo.dataErrRange.getUpperBound())
            );
        }
        this.jLabelMouse.setText(infoMouse);
        this.jLabelPoints.setText(infoPoints);
        this.jLabelDataRange.setText(infoDataRange);
        this.jLabelDataErrRange.setText(infoDataErrRange);
    }

    /**
     * Handle rectangular selection event
     *
     * @param plot the plot or subplot where the selection happened.
     * @param selection the selected region.
     */
    @Override
    public void mouseSelected(final XYPlot plot, final Rectangle2D selection) {
        logger.debug("mouseSelected: rectangle {}", selection);

        // TODO: determine which plot to use ?
        // find data points:
        final List<Point2D> points = findDataPoints(plot, selection);

        // push data points to overlay for rendering:
        this.selectionOverlay.setPoints(points);
    }

    @SuppressWarnings("unchecked")
    private static FastIntervalXYDataset<OITableSerieKey, OITableSerieKey> getDataset(final XYPlot xyPlot) {
        return (FastIntervalXYDataset<OITableSerieKey, OITableSerieKey>) xyPlot.getDataset();
    }

    private DataPoint findDataPoint(final DataPointer selPtr) {
        int matchPlotIndex = -1;
        FastIntervalXYDataset<OITableSerieKey, OITableSerieKey> matchDataset = null;
        int matchSerie = -1;
        int matchItem = -1;

        if (selPtr != null) {
            // TODO: move such code elsewhere : ChartUtils or XYDataSetUtils ?
            final long startTime = System.nanoTime();

            // matching criteria
            final int mRow = selPtr.getRow();

            int nMatchs = 0;

            for (int i = 0, len = this.xyPlotList.size(); i < len; i++) {
                final XYPlot xyPlot = this.xyPlotList.get(i);

                final FastIntervalXYDataset<OITableSerieKey, OITableSerieKey> dataset = getDataset(xyPlot);
                if (dataset != null) {
                    for (int serie = 0, seriesCount = dataset.getSeriesCount(), item, itemCount, row; serie < seriesCount; serie++) {
                        final OITableSerieKey serieKey = (OITableSerieKey) dataset.getSeriesKey(serie);

                        if (serieKey.getDataPointer().isSameCol(selPtr)) {
                            itemCount = dataset.getItemCount(serie);

                            for (item = 0; item < itemCount; item++) {
                                row = dataset.getDataRow(serie, item);

                                if (row == mRow) {
                                    // matching
                                    logger.debug("matching point: serie={} item={}", serie, item);

                                    if (++nMatchs > 2) {
                                        logger.debug("Too much matching items !");
                                        return DataPoint.UNDEFINED;
                                    }

                                    matchPlotIndex = this.plotMapping.get(xyPlot).intValue();
                                    matchDataset = dataset;
                                    matchSerie = serie;
                                    matchItem = item;
                                }
                            }
                        }
                    }
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("findDataPoint: duration = {} ms.", 1e-6d * (System.nanoTime() - startTime));
            }
        }
        // note: if matchPlotIndex = -1 then matchItem = -1 so returns DataPoint.UNDEFINED:
        return createDataPoint((matchPlotIndex != -1) ? getPlotInfos().get(matchPlotIndex) : null, matchDataset, matchSerie, matchItem);
    }

    /**
     * Find data point closest in FIRST dataset to the given coordinates X / Y
     * @param info plot information
     * @param xyPlot xy plot to get its dataset
     * @param anchorX domain axis coordinate
     * @param anchorY range axis coordinate
     * @param xRatio pixels per data on domain axis
     * @param yRatio pixels per data on range axis
     * @return found Point2D (data coordinates) or Point2D(NaN, NaN)
     */
    private static DataPoint findDataPoint(final PlotInfo info, final XYPlot xyPlot,
            final double anchorX, final double anchorY,
            final double xRatio, final double yRatio) {
        int matchSerie = -1;
        int matchItem = -1;

        final FastIntervalXYDataset<OITableSerieKey, OITableSerieKey> dataset = getDataset(xyPlot);

        if (dataset != null) {
            // TODO: move such code elsewhere : ChartUtils or XYDataSetUtils ?
            final long startTime = System.nanoTime();

            double minDistance = Double.POSITIVE_INFINITY;
            double x, y, dx, dy, distance;

            // NOTE: not optimized
            // standard case - plain XYDataset
            for (int serie = 0, seriesCount = dataset.getSeriesCount(), item, itemCount; serie < seriesCount; serie++) {
                itemCount = dataset.getItemCount(serie);

                for (item = 0; item < itemCount; item++) {
                    x = dataset.getXValue(serie, item);
                    y = dataset.getYValue(serie, item);

                    if (!Double.isNaN(x) && !Double.isNaN(y)) {
                        // converted in pixels:
                        dx = (x - anchorX) * xRatio;
                        dy = (y - anchorY) * yRatio;

                        distance = dx * dx + dy * dy;

                        if (distance < minDistance) {
                            minDistance = distance;
                            matchSerie = serie;
                            matchItem = item;
                        }
                    }
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("findDataPoint: duration = {} ms.", 1e-6d * (System.nanoTime() - startTime));
            }
        }

        return createDataPoint(info, dataset, matchSerie, matchItem);
    }

    /**
     * Create the data point given the dataset and corresponding row / col values
     * @param info plot information
     * @param dataset corresponding dataset
     * @param matchSerie index of the series
     * @param matchItem index of the item
     * @return found DataPoint (data coordinates) or DataPoint.UNDEFINED
     */
    private static DataPoint createDataPoint(final PlotInfo info, final FastIntervalXYDataset<OITableSerieKey, OITableSerieKey> dataset,
            final int matchSerie, final int matchItem) {
        if (matchItem != -1) {
            final double matchX = dataset.getXValue(matchSerie, matchItem);
            final double matchY = dataset.getYValue(matchSerie, matchItem);

            if (logger.isDebugEnabled()) {
                logger.debug("Matching item [serie = " + matchSerie + ", item = " + matchItem + "] : (" + matchX + ", " + matchY + ")");
                logger.debug("SeriesKey = {}", dataset.getSeriesKey(matchSerie));
            }

            final OITableSerieKey serieKey = (OITableSerieKey) dataset.getSeriesKey(matchSerie);

            final DataPointer ptrKey = serieKey.getDataPointer();
            final int row = dataset.getDataRow(matchSerie, matchItem);

            // Create a new data pointer with (row, col):
            final DataPointer ptr = new DataPointer(ptrKey.getOiData(), row, ptrKey.getCol());

            return new DataPointInfo(info.xAxisInfo, info.yAxisInfo, matchX, matchY, ptr);
        }
        logger.debug("No Matching item.");
        return DataPoint.UNDEFINED;
    }

    /**
     * Find data points inside the given Shape (data coordinates)
     * @param plot
     * @param shape shape to use
     * @return found list of Point2D (data coordinates) or empty list
     */
    private static List<Point2D> findDataPoints(final XYPlot plot, final Shape shape) {
        final List<Point2D> points = new ArrayList<Point2D>();

        final XYDataset dataset = (plot != null) ? plot.getDataset() : null;

        if (dataset != null) {
            // TODO: move such code elsewhere : ChartUtils or XYDataSetUtils ?

            final long startTime = System.nanoTime();
            /*
             int matchSerie = -1;
             int matchItem = -1;
             */
            double x, y;

            // NOTE: not optimized
            // standard case - plain XYDataset
            for (int serie = 0, seriesCount = dataset.getSeriesCount(), item, itemCount; serie < seriesCount; serie++) {
                itemCount = dataset.getItemCount(serie);
                for (item = 0; item < itemCount; item++) {
                    x = dataset.getXValue(serie, item);
                    y = dataset.getYValue(serie, item);

                    if (!Double.isNaN(x) && !Double.isNaN(y)) {

                        if (shape.contains(x, y)) {
                            // TODO: keep data selection (pointer to real data)
                            /*
                             matchSerie = serie;
                             matchItem = item;
                             */
                            points.add(new Point2D.Double(x, y));
                        }
                    }
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("findDataPoints: duration = {} ms.", 1e-6d * (System.nanoTime() - startTime));
            }
        }
        return points;
    }

    /**
     * Plot the generated file synchronously (useless).
     * This code must be executed by the Swing Event Dispatcher thread (EDT)
     */
    public void plot() {
        logger.debug("plot");
        this.updatePlot();
    }

    /**
     * Reset plot
     */
    private void resetPlot() {
        // clear plot informations
        getPlotInfos().clear();

        // disable chart & plot notifications:
        this.chart.setNotify(false);
        for (int i = 0, len = this.xyPlotList.size(); i < len; i++) {
            final XYPlot xyPlot = this.xyPlotList.get(i);
            xyPlot.setNotify(false);
        }
        try {
            // reset title:
            ChartUtils.clearTextSubTitle(this.chart);

            removeAllSubPlots();

            // reset plots:
            for (int i = 0, len = this.xyPlotList.size(); i < len; i++) {
                final XYPlot xyPlot = this.xyPlotList.get(i);
                resetXYPlot(xyPlot);
            }

            showPlot(isHasData());

        } finally {
            // restore chart & plot notifications:
            for (int i = 0, len = this.xyPlotList.size(); i < len; i++) {
                final XYPlot xyPlot = this.xyPlotList.get(i);
                xyPlot.setNotify(true);
            }
            this.chart.setNotify(true);
        }
    }

    /**
     * Remove all subplots in the combined plot and in the plot index
     */
    private void removeAllSubPlots() {
        this.resetOverlays();

        // remove all sub plots:
        // Note: use toArray() to avoid concurrentModification exceptions:
        for (Object subPlot : this.combinedXYPlotList.toArray()) {
            final XYPlot xyPlot = (XYPlot) subPlot;
            this.combinedXYPlot.remove(xyPlot);

            final Integer index = this.plotMapping.remove(xyPlot);
            this.plotIndexMapping.remove(index);
        }
    }

    /**
     * Refresh the plot using chart data.
     * This code is executed by the Swing Event Dispatcher thread (EDT)
     */
    private void updatePlot() {
        // check subset:
        if (getOiFitsSubset() == null || getPlotDefinition() == null) {
            resetPlot();
            return;
        }

        final long start = System.nanoTime();

        // clear plot informations
        getPlotInfos().clear();

        // disable chart & plot notifications:
        this.chart.setNotify(false);
        for (int i = 0, len = this.xyPlotList.size(); i < len; i++) {
            final XYPlot xyPlot = this.xyPlotList.get(i);
            xyPlot.setNotify(false);
        }

        try {
            // title :
            ChartUtils.clearTextSubTitle(this.chart);

            removeAllSubPlots();

            // computed data are valid :
            // TODO: externalize dataset creation using SwingWorker to be able to
            // - cancel long data processing task
            // - do not block EDT !
            updateChart();

            final boolean hasData = isHasData();

            if (hasData) {
                refreshCrosshair(ocm.getSelection());

                final Set<String> distinct = new LinkedHashSet<String>();

                // TODO: keep values from dataset ONLY:
                // - arrName, insName, dateObs (keywords) = OK
                // - baselines or configurations (rows) = KO ... IF HAS DATA (filtered)
                final StringBuilder sb = new StringBuilder(32);

                // Add distinct arrNames:
                final GetOIDataString arrNameOperator = new GetOIDataString() {
                    @Override
                    public String getString(final OIData oiData) {
                        return oiData.getArrName();
                    }
                };

                distinct.clear();
                for (PlotInfo info : getPlotInfos()) {
                    getDistinct(info.oidataList, distinct, arrNameOperator);
                }
                if (!distinct.isEmpty()) {
                    toString(distinct, sb, " ", " / ", 3, "MULTI ARRAY");
                }

                sb.append(" - ");

                // Add unique insNames:
                final GetOIDataString insNameOperator = new GetOIDataString() {
                    @Override
                    public String getString(final OIData oiData) {
                        return oiData.getInsName();
                    }
                };

                distinct.clear();
                for (PlotInfo info : getPlotInfos()) {
                    getDistinct(info.oidataList, distinct, insNameOperator);
                }
                if (!distinct.isEmpty()) {
                    toString(distinct, sb, " ", " / ", 3, "MULTI INSTRUMENT");
                }

                sb.append(' ');

                // Add wavelength ranges:
                distinct.clear();
                for (PlotInfo info : getPlotInfos()) {
                    getDistinctWaveLengthRange(info.oidataList, distinct);
                }
                if (!distinct.isEmpty()) {
                    toString(distinct, sb, " ", " / ", 3, "MULTI WAVELENGTH RANGE");
                }

                sb.append(" - ");

                // Add unique configurations (FILTERED):
                distinct.clear();
                for (PlotInfo info : getPlotInfos()) {
                    distinct.addAll(info.usedStaConfNames);
                }
                if (!distinct.isEmpty()) {
                    toString(distinct, sb, " ", " / ", 3, "MULTI CONFIGURATION");
                }

                ChartUtils.addSubtitle(this.chart, sb.toString());

                // date - Source:
                sb.setLength(0);
                sb.append("Day: ");

                // Add unique dateObs:
                final GetOIDataString dateObsOperator = new GetOIDataString() {
                    @Override
                    public String getString(final OIData oiData) {
                        return oiData.getDateObs();
                    }
                };
                distinct.clear();
                for (PlotInfo info : getPlotInfos()) {
                    getDistinct(info.oidataList, distinct, dateObsOperator);
                }
                if (!distinct.isEmpty()) {
                    toString(distinct, sb, " ", " / ", 3, "MULTI DATE");
                }

                sb.append(" - Source: ").append(getTargetName());

                ChartUtils.addSubtitle(this.chart, sb.toString());

                ChartUtilities.applyCurrentTheme(this.chart);
            }

            showPlot(hasData);

        } finally {
            // restore chart & plot notifications:
            for (int i = 0, len = this.xyPlotList.size(); i < len; i++) {
                final XYPlot xyPlot = this.xyPlotList.get(i);
                xyPlot.setNotify(true);
            }
            this.chart.setNotify(true);
        }

        logger.info("updatePlot: duration = {} ms.", 1e-6d * (System.nanoTime() - start));
    }

    /**
     * Show the chart panel if it has data or the jLabelNoData
     * @param hasData flag to indicate to show label
     */
    private void showPlot(final boolean hasData) {
        this.jLabelNoData.setVisible(!hasData);
        this.chartPanel.setVisible(hasData);
    }

    /**
     * reset overlays
     */
    private void resetOverlays() {
        resetCrosshairOverlay();

        // reset selection:
        if (this.selectionOverlay != null) {
            this.selectionOverlay.reset();
        }
    }

    private void resetCrosshairOverlay() {
        this.jPanelCrosshair.setVisible(false);

        if (this.crosshairOverlay != null) {
            for (Integer plotIndex : this.plotMapping.values()) {
                for (Crosshair ch : this.crosshairOverlay.getDomainCrosshairs(plotIndex)) {
                    ch.setValue(Double.NaN);
                }
                for (Crosshair ch : this.crosshairOverlay.getRangeCrosshairs(plotIndex)) {
                    ch.setValue(Double.NaN);
                }
            }
        }
    }

    /**
     * Update the datasets
     */
    private void updateChart() {
        logger.debug("updateChart: plot {}", this.plotId);

        final OIFitsFile oiFitsSubset = getOiFitsSubset();
        final PlotDefinition plotDef = getPlotDefinition();

        final Axis xAxis = plotDef.getXAxis();

        // Get Global SharedSeriesAttributes:
        final SharedSeriesAttributes oixpAttrs = SharedSeriesAttributes.INSTANCE_OIXP;

        // TODO: handle reset (depending on the previous view usages)
        oixpAttrs.reset();

        // Get distinct station indexes from OIFits subset (not filtered):
        final List<String> distinctStaIndexNames = getDistinctStaNames(oiFitsSubset.getOiDataList());

        // Get distinct station configuration from OIFits subset (not filtered):
        final List<String> distinctStaConfNames = getDistinctStaConfs(oiFitsSubset.getOiDataList());

        final Range waveLengthRange = getWaveLengthRange(oiFitsSubset.getOiDataList());

        logger.debug("distinctStaIndexNames: {}", distinctStaIndexNames);
        logger.debug("distinctStaConfNames: {}", distinctStaConfNames);
        logger.debug("waveLengthRange: {}", waveLengthRange);

        Range viewBounds, viewRange;

        // Global converter (symmetry)
        Converter xConverter = null, yConverter = null;

        final int nYaxes = plotDef.getYAxes().size();

        // ensure enough plots:
        while (nYaxes > this.xyPlotList.size()) {
            addXYPlot();
        }

        // reset plots anyway (so free memory):
        for (int i = 0, len = this.xyPlotList.size(); i < len; i++) {
            final XYPlot xyPlot = this.xyPlotList.get(i);
            resetXYPlot(xyPlot);
        }

        int nShowPlot = 0;

        // Loop on Y axes:
        for (int i = 0; i < nYaxes; i++) {
            final Axis yAxis = plotDef.getYAxes().get(i);
            final XYPlot xyPlot = this.xyPlotList.get(i);

            boolean showPlot = false;
            final PlotInfo info;

            if (oiFitsSubset.getNbOiTables() == 0) {
                info = null;
            } else {
                final FastIntervalXYDataset<OITableSerieKey, OITableSerieKey> dataset = new FastIntervalXYDataset<OITableSerieKey, OITableSerieKey>();

                info = new PlotInfo();
                info.distinctStaIndexNames = distinctStaIndexNames;
                info.distinctStaConfNames = distinctStaConfNames;
                info.waveLengthRange = waveLengthRange;

                int tableIndex = 0;

                // Use symmetry for spatial frequencies:
                if (useSymmetry(xAxis, yAxis)) {
                    xConverter = yConverter = ConverterFactory.CONVERTER_REFLECT;

                    for (OIData oiData : oiFitsSubset.getOiDataList()) {
                        // process data and add data series into given dataset:
                        updatePlot(xyPlot, oiData, tableIndex, plotDef, i, dataset, xConverter, yConverter, info);

                        tableIndex++;
                    }
                    xConverter = yConverter = null;
                }

                for (OIData oiData : oiFitsSubset.getOiDataList()) {
                    // process data and add data series into given dataset:
                    updatePlot(xyPlot, oiData, tableIndex, plotDef, i, dataset, xConverter, yConverter, info);

                    tableIndex++;
                }

                if (info.hasPlotData) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("xyPlotPlot[{}]: nData = {}", i, info.nDataPoints);
                        logger.debug("xyPlotPlot[{}]: nbSeries = {}", i, dataset.getSeriesCount());
                    }

                    showPlot = true;

                    boolean yUseLog = false;
                    ColumnMeta yMeta = null;
                    String yUnit = null;

                    // update Y axis information:
                    if (info.yAxisInfo.columnMeta != null) {
                        yUseLog = info.yAxisInfo.useLog;
                        yMeta = info.yAxisInfo.columnMeta;
                        yUnit = info.yAxisInfo.unit;
                    }

                    // adjust bounds & view range:
                    adjustAxisRanges(yAxis, info.yAxisInfo);

                    viewBounds = info.yAxisInfo.viewBounds;
                    viewRange = info.yAxisInfo.viewRange;

                    // Update Y axis:
                    if (yUseLog) {
                        if (!(xyPlot.getRangeAxis() instanceof BoundedLogAxis)) {
                            xyPlot.setRangeAxis(new BoundedLogAxis(""));
                        }
                        final BoundedLogAxis axis = (BoundedLogAxis) xyPlot.getRangeAxis();
                        axis.setBounds(viewBounds);
                        axis.setInitial(viewRange);
                        axis.setRange(viewRange);
                    } else {
                        if (!(xyPlot.getRangeAxis() instanceof BoundedNumberAxis)) {
                            xyPlot.setRangeAxis(ChartUtils.createAxis(""));
                        }
                        final BoundedNumberAxis axis = (BoundedNumberAxis) xyPlot.getRangeAxis();
                        axis.setBounds(viewBounds);
                        axis.setInitial(viewRange);
                        axis.setRange(viewRange);
                    }

                    // update Y axis Label:
                    String label = (yUseLog) ? "log " : "";
                    if (yMeta != null) {
                        label += yMeta.getName();
                        if (yUnit != null) {
                            label += " (" + yUnit + ")";
                        } else if (yMeta.getUnits() != Units.NO_UNIT) {
                            label += " (" + yMeta.getUnits().getStandardRepresentation() + ")";
                        }
                        xyPlot.getRangeAxis().setLabel(label);
                    }

                    // adjust arrows:
                    ChartUtils.defineAxisArrows(xyPlot.getRangeAxis());
                    // tick color:
                    xyPlot.getRangeAxis().setTickMarkPaint(Color.BLACK);

                    // update plot's renderer before dataset (avoid notify events):
                    final FastXYErrorRenderer renderer = (FastXYErrorRenderer) xyPlot.getRenderer();

                    // enable/disable X error rendering (performance):
                    renderer.setDrawXError(info.xAxisInfo.hasDataError);

                    // enable/disable Y error rendering (performance):
                    renderer.setDrawYError(info.yAxisInfo.hasDataError);

                    // use deprecated method but defines shape once for ALL series (performance):
                    // define base shape as valid point (fallback):
                    renderer.setBaseShape(shapePointValid, false);

                    renderer.setLinesVisible(plotDef.isDrawLine());

                    // update plot's dataset (notify events):
                    xyPlot.setDataset(dataset);
                }
            }

            if (showPlot) {
                this.combinedXYPlot.add(xyPlot, 1); // weight=1

                final Integer plotIndex = NumberUtils.valueOf(nShowPlot++);
                this.plotMapping.put(xyPlot, plotIndex);
                this.plotIndexMapping.put(plotIndex, xyPlot);

                // add plot info:
                getPlotInfos().add(info);
            }

        } // loop on y axes

        if (nShowPlot == 0) {
            return;
        }

        boolean useWaveLengths = false;
        AxisInfo xCombinedAxisInfo = null;
        boolean xUseLog = false;
        ColumnMeta xMeta = null;
        String xUnit = null;

        for (PlotInfo info : getPlotInfos()) {
            if (xCombinedAxisInfo == null) {
                // create combined X axis information once:
                xCombinedAxisInfo = new AxisInfo(info.xAxisInfo);
                xMeta = xCombinedAxisInfo.columnMeta;
                xUseLog = xCombinedAxisInfo.useLog;
                xUnit = xCombinedAxisInfo.unit;
            } else {
                // combine data ranges:
                xCombinedAxisInfo.combineRanges(info.xAxisInfo);
            }
            useWaveLengths |= info.useWaveLengths;
        }

        // adjust combined bounds & view range:
        adjustAxisRanges(xAxis, xCombinedAxisInfo);

        viewBounds = xCombinedAxisInfo.viewBounds;
        viewRange = xCombinedAxisInfo.viewRange;

        // Update X axis:
        if (xUseLog) {
            if (!(this.combinedXYPlot.getDomainAxis() instanceof BoundedLogAxis)) {
                this.combinedXYPlot.setDomainAxis(new BoundedLogAxis(""));
            }
            final BoundedLogAxis axis = (BoundedLogAxis) this.combinedXYPlot.getDomainAxis();
            axis.setBounds(viewBounds);
            axis.setInitial(viewRange);
            axis.setRange(viewRange);
        } else {
            if (!(this.combinedXYPlot.getDomainAxis() instanceof BoundedNumberAxis)) {
                this.combinedXYPlot.setDomainAxis(ChartUtils.createAxis(""));
            }
            final BoundedNumberAxis axis = (BoundedNumberAxis) this.combinedXYPlot.getDomainAxis();
            axis.setBounds(viewBounds);
            axis.setInitial(viewRange);
            axis.setRange(viewRange);
        }

        // update Y axis Label:
        String label = (xUseLog) ? "log " : "";
        if (xMeta != null) {
            label += xMeta.getName();
            if (xUnit != null) {
                label += " (" + xUnit + ")";
            } else if (xMeta.getUnits() != Units.NO_UNIT) {
                label += " (" + xMeta.getUnits().getStandardRepresentation() + ")";
            }
            this.combinedXYPlot.getDomainAxis().setLabel(label);
        }

        // adjust arrows:
        ChartUtils.defineAxisArrows(this.combinedXYPlot.getDomainAxis());
        // tick color:
        this.combinedXYPlot.getDomainAxis().setTickMarkPaint(Color.BLACK);

        // Define legend:
        LegendItemCollection legendCollection = new LegendItemCollection();

        if (mapLegend != null) {
            this.chart.removeSubtitle(mapLegend);
        }

        // TODO: update colors once:
        if (plotDef.getColorMapping() == ColorMapping.STATION_INDEX
                || plotDef.getColorMapping() == ColorMapping.CONFIGURATION) {

            // Assign ONCE colors to labels automatically:
            oixpAttrs.define();

            for (int i = 0, len = this.xyPlotList.size(); i < len; i++) {
                final XYPlot xyPlot = this.xyPlotList.get(i);

                final FastIntervalXYDataset<OITableSerieKey, OITableSerieKey> dataset = getDataset(xyPlot);
                if (dataset != null) {
                    final FastXYErrorRenderer renderer = (FastXYErrorRenderer) xyPlot.getRenderer();

                    // Apply attributes to dataset:
                    for (int serie = 0, seriesCount = dataset.getSeriesCount(); serie < seriesCount; serie++) {
                        final OITableSerieKey serieKey = (OITableSerieKey) dataset.getSeriesKey(serie);

                        switch (plotDef.getColorMapping()) {
                            case CONFIGURATION:
                                label = serieKey.getStaConfName();
                                break;
                            case STATION_INDEX:
                                label = serieKey.getStaIndexName();
                                break;
                            default:
                                label = null;
                                break;
                        }

                        renderer.setSeriesPaint(serie, oixpAttrs.getColorAlpha(label), false);
                    }
                }

                if (DEBUG) {
                    if (dataset != null) {
                        logger.debug("seriesCount : {}", dataset.getSeriesCount());
                    }
                }
            }

            // define custom legend:
            if (ColorMapping.STATION_INDEX == plotDef.getColorMapping()) {
                // merge all used staIndex names:
                final Set<String> distinctUsedStaIndexNames = new HashSet<String>(32);

                for (PlotInfo info : getPlotInfos()) {
                    distinctUsedStaIndexNames.addAll(info.usedStaIndexNames);
                }

                // Order by used color:
                for (String staIndexName : distinctStaIndexNames) {
                    // is used ?
                    if (distinctUsedStaIndexNames.contains(staIndexName)) {
                        legendCollection.add(ChartUtils.createLegendItem(staIndexName, oixpAttrs.getColorAlpha(staIndexName)));
                    }
                }
            } else if (ColorMapping.CONFIGURATION == plotDef.getColorMapping()) {

                // merge all used staConf names:
                final Set<String> distinctUsedStaConfNames = new LinkedHashSet<String>();

                for (PlotInfo info : getPlotInfos()) {
                    distinctUsedStaConfNames.addAll(info.usedStaConfNames);
                }

                // Order by used color:
                for (String staConfName : distinctStaConfNames) {
                    // is used ?
                    if (distinctUsedStaConfNames.contains(staConfName)) {
                        legendCollection.add(ChartUtils.createLegendItem(staConfName, oixpAttrs.getColorAlpha(staConfName)));
                    }
                }
            }

            // TODO: use ColorScale to paint an horizontal wavelength color scale
            if (legendCollection.getItemCount() > 100) {
                // avoid too many legend items:
                if (logger.isDebugEnabled()) {
                    logger.debug("legend items: {}", legendCollection.getItemCount());
                }

                legendCollection = new LegendItemCollection();
            }
        } else // other cases:
        /*
            case WAVELENGTH_RANGE:
            // wavelength is default:
            case OBSERVATION_DATE:
            // not implemented still
         */ if (useWaveLengths && waveLengthRange.getLength() > LAMBDA_EPSILON) {
            final double min = NumberUtils.trimTo3Digits(ConverterFactory.CONVERTER_MICRO_METER.evaluate(waveLengthRange.getLowerBound()) - 1e-3D); // microns
            final double max = NumberUtils.trimTo3Digits(ConverterFactory.CONVERTER_MICRO_METER.evaluate(waveLengthRange.getUpperBound()) + 1e-3D); // microns

            final NumberAxis lambdaAxis = new NumberAxis();
            // inverted color palette:
            mapLegend = new PaintScaleLegend(new ColorModelPaintScale(min, max, colorModel, ColorScale.LINEAR, true), lambdaAxis);

            lambdaAxis.setTickLabelFont(ChartUtils.DEFAULT_FONT);
            lambdaAxis.setAxisLinePaint(Color.BLACK);
            lambdaAxis.setTickMarkPaint(Color.BLACK);

            mapLegend.setPosition(RectangleEdge.BOTTOM);
            mapLegend.setStripWidth(15d);
            mapLegend.setStripOutlinePaint(Color.BLACK);
            mapLegend.setStripOutlineVisible(true);
            mapLegend.setSubdivisionCount(colorModel.getMapSize());
            mapLegend.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
            mapLegend.setFrame(new BlockBorder(Color.BLACK));
            mapLegend.setMargin(10d, 10d, 10d, 10d);
            mapLegend.setPadding(10d, 25d, 10d, 25d);

            this.chart.addSubtitle(mapLegend);
        }

        this.combinedXYPlot.setFixedLegendItems(legendCollection);
    }

    private static void adjustAxisRanges(final Axis axis, final AxisInfo axisInfo) {

        final boolean modeAuto = (axis.getRangeModeOrDefault() == AxisRangeMode.AUTO);
        final boolean modeRange = (axis.getRangeModeOrDefault() == AxisRangeMode.RANGE);

        // bounds = data+err range:
        double bmin = axisInfo.dataErrRange.getLowerBound();
        double bmax = axisInfo.dataErrRange.getUpperBound();

        // TODO: auto or default range:
        // view = data range:
        double vmin = axisInfo.dataRange.getLowerBound();
        double vmax = axisInfo.dataRange.getUpperBound();

        if (logger.isDebugEnabled()) {
            logger.debug("axis dataErrRange: {} - {}", bmin, bmax);
            logger.debug("axis dataRange:    {} - {}", vmin, vmax);
        }

        if (axisInfo.useLog) {
            // bounds:
            double minTen = Math.floor(Math.log10(bmin));
            double maxTen = Math.ceil(Math.log10(bmax));

            if (maxTen == minTen) {
                minTen -= 1;
            }

            bmin = Math.pow(10.0, minTen); // lower power of ten
            bmax = Math.pow(10.0, maxTen); // upper power of ten

            // view range:
            minTen = Math.floor(Math.log10(vmin) * 2.0);
            maxTen = Math.ceil(Math.log10(vmax) * 2.0);

            if (maxTen == minTen) {
                minTen -= 1;
            }

            vmin = Math.pow(10.0, 0.5 * minTen); // lower power of ten
            vmax = Math.pow(10.0, 0.5 * maxTen); // upper power of ten

        } else {
            boolean fix_bmin = false;
            boolean fix_bmax = false;

            boolean fix_vmin = false;
            boolean fix_vmax = false;

            final ColumnMeta colMeta = axisInfo.columnMeta;

            // use column meta's default range:
            if (colMeta != null && colMeta.getDataRange() != null) {
                final DataRange dataRange = colMeta.getDataRange();

                if (!Double.isNaN(dataRange.getMin())) {
                    final double v = dataRange.getMin();
                    if (v < bmin) {
                        fix_bmin = true;
                        bmin = v;
                    }
                    if (!modeAuto || (v < vmin)) {
                        fix_vmin = true;
                        vmin = v;
                    }
                }

                if (!Double.isNaN(dataRange.getMax())) {
                    final double v = dataRange.getMax();
                    if (v > bmax) {
                        fix_bmax = true;
                        bmax = v;
                    }
                    if (!modeAuto || (v > vmax)) {
                        fix_vmax = true;
                        vmax = v;
                    }
                }
            }

            // use include zero flag:
            if (axis.isIncludeZero()) {
                if (bmin > 0.0) {
                    fix_bmin = true;
                    bmin = 0.0;
                }
                if (vmin > 0.0) {
                    fix_vmin = true;
                    vmin = 0.0;
                }
                if (bmax < 0.0) {
                    fix_bmax = true;
                    vmax = bmax = 0.0;
                }
                if (vmax < 0.0) {
                    fix_vmax = true;
                    vmax = 0.0;
                }
            }

            // handle fixed axis range:
            if (modeRange && axis.getRange() != null) {
                if (!Double.isNaN(axis.getRange().getMin())) {
                    fix_vmin = true;
                    vmin = axis.getRange().getMin();
                }
                if (!Double.isNaN(axis.getRange().getMax())) {
                    fix_vmax = true;
                    vmax = axis.getRange().getMax();
                }
            }

            // ensure vmin < vmax:
            if (vmin > vmax) {
                if (fix_vmin) {
                    vmax = bmax;
                }
                if (fix_vmax) {
                    vmin = bmin;
                }
            }

            // adjust bounds margins:
            double margin = (bmax - bmin) * MARGIN_PERCENTS;
            if (margin > 0.0) {
                if (!fix_bmin) {
                    bmin -= margin;
                }
                if (!fix_bmax) {
                    bmax += margin;
                }
            } else {
                margin = Math.abs(bmin) * MARGIN_PERCENTS;
                bmin -= margin;
                bmax += margin;
            }
            if (bmax == bmin) {
                bmax = bmin + 1d;
            }

            // adjust view margins:
            margin = (vmax - vmin) * MARGIN_PERCENTS;
            if (margin > 0.0) {
                if (!fix_vmin) {
                    vmin -= margin;
                }
                if (!fix_vmax) {
                    vmax += margin;
                }
            } else {
                margin = Math.abs(vmin) * MARGIN_PERCENTS;
                vmin -= margin;
                vmax += margin;
            }
            if (vmax == vmin) {
                vmax = vmin + 1d;
            }

            // ensure bounds > view range:
            bmin = Math.min(bmin, vmin);
            bmax = Math.max(bmax, vmax);
        } // not log

        if (logger.isDebugEnabled()) {
            logger.debug("fixed view bounds: {} - {}", bmin, bmax);
            logger.debug("fixed view range : {} - {}", vmin, vmax);
        }

        // update view bounds & range:
        final Range viewBounds = new Range(bmin, bmax);
        final Range viewRange = new Range(vmin, vmax);
        axisInfo.viewBounds = viewBounds;
        axisInfo.viewRange = viewRange;
    }

    private static void resetXYPlot(final XYPlot plot) {
        // reset plot dataset anyway (so free memory):
        plot.setDataset(null);

        // TODO: adjust renderer settings per Serie (color, shape ...) per series and item at higher level using dataset fields
        final FastXYErrorRenderer renderer = (FastXYErrorRenderer) plot.getRenderer();
        // reset colors :
        renderer.clearSeriesPaints(false);

        // reset item shapes:
        renderer.clearItemShapes();
    }

    /**
     * Update the plot (dataset, axis ranges ...) using the given OIData table
     * TODO use column names and virtual columns (spatial ...)
     * @param plot XYPlot to update (dataset, renderer, axes)
     * @param oiData OIData table to use as data source
     * @param tableIndex table index to ensure serie uniqueness among collection
     * @param plotDef plot definition to use
     * @param yAxisIndex yAxis index to use in plot definition
     * @param dataset FastIntervalXYDataset to fill
     * @param initialXConverter converter to use first on x axis
     * @param initialYConverter converter to use first on Y axis
     * @param info plot information to update
     */
    private void updatePlot(final XYPlot plot, final OIData oiData, final int tableIndex,
            final PlotDefinition plotDef, final int yAxisIndex,
            final FastIntervalXYDataset<OITableSerieKey, OITableSerieKey> dataset,
            final Converter initialXConverter, final Converter initialYConverter,
            final PlotInfo info) {

        final boolean isLogDebug = logger.isDebugEnabled();

        // Get yAxis data:
        final Axis yAxis = plotDef.getYAxes().get(yAxisIndex);
        final String yAxisName = yAxis.getName();

        final ColumnMeta yMeta = oiData.getColumnMeta(yAxisName);

        if (yMeta == null) {
            if (isLogDebug) {
                logger.debug("unsupported yAxis : {} on {}", yAxis.getName(), oiData);
            }
            return;
        }
        if (isLogDebug) {
            logger.debug("yMeta:{}", yMeta);
        }

        final boolean yUseLog = yAxis.isLogScale();
        final boolean doConvertY = (initialYConverter != null);

        final Converter yConverter = cf.getDefault(yAxis.getConverter());
        final boolean doScaleY = (yConverter != null);

        final boolean isYData2D = yMeta.isArray();
        final double[] yData1D;
        final double[] yData1DErr;
        final double[][] yData2D;
        final double[][] yData2DErr;

        if (isYData2D) {
            yData1D = null;
            yData1DErr = null;
            yData2D = oiData.getColumnAsDoubles(yAxisName);
            yData2DErr = oiData.getColumnAsDoubles(yMeta.getErrorColumnName());
        } else {
            yData1D = oiData.getColumnAsDouble(yAxisName);
            yData1DErr = oiData.getColumnAsDouble(yMeta.getErrorColumnName());
            yData2D = null;
            yData2DErr = null;
        }

        final boolean hasErrY = (yData2DErr != null) || (yData1DErr != null);

        // Get xAxis data:
        final Axis xAxis = plotDef.getXAxis();
        final String xAxisName = xAxis.getName();

        final ColumnMeta xMeta = oiData.getColumnMeta(xAxisName);

        if (xMeta == null) {
            if (isLogDebug) {
                logger.debug("unsupported xAxis : {} on {}", xAxis.getName(), oiData);
            }
            return;
        }
        if (isLogDebug) {
            logger.debug("yMeta:{}", yMeta);
        }

        final boolean xUseLog = xAxis.isLogScale();
        final boolean doConvertX = (initialXConverter != null);

        final Converter xConverter = cf.getDefault(xAxis.getConverter());
        final boolean doScaleX = (xConverter != null);

        final boolean isXData2D = xMeta.isArray();
        final double[] xData1D;
        final double[] xData1DErr;
        final double[][] xData2D;
        final double[][] xData2DErr;

        if (isXData2D) {
            xData1D = null;
            xData1DErr = null;
            xData2D = oiData.getColumnAsDoubles(xAxisName);
            xData2DErr = oiData.getColumnAsDoubles(xMeta.getErrorColumnName());
        } else {
            xData1D = oiData.getColumnAsDouble(xAxisName);
            xData1DErr = oiData.getColumnAsDouble(xMeta.getErrorColumnName());
            xData2D = null;
            xData2DErr = null;
        }

        final boolean hasErrX = (xData2DErr != null) || (xData1DErr != null);

        final boolean skipFlaggedData = plotDef.isSkipFlaggedData();

        final ColorMapping colorMapping = (plotDef.getColorMapping() != null) ? plotDef.getColorMapping() : ColorMapping.WAVELENGTH_RANGE;

        // serie count:
        int seriesCount = dataset.getSeriesCount();

        final int nRows = oiData.getNbRows();
        final int nWaves = oiData.getNWave();

        if (isLogDebug) {
            logger.debug("nRows - nWaves : {} - {}", nRows, nWaves);
        }

        // standard columns:
        final short[][] staIndexes = oiData.getStaIndex();
        final short[][] staConfs = oiData.getStaConf();

        // Use staIndex (baseline or triplet) on each data row ?
        final int nStaIndexes = oiData.getDistinctStaIndexCount();
        final boolean checkStaIndex = nStaIndexes > 1;

        if (isLogDebug) {
            logger.debug("nStaIndexes: {}", nStaIndexes);
            logger.debug("checkStaIndex: {}", checkStaIndex);
        }

        // anyway (color mapping or check sta index):
        final short[][] distinctStaIndexes = oiData.getDistinctStaIndexes();

        // Use flags on every 2D data ?
        final int nFlagged = oiData.getNFlagged();
        final boolean checkFlaggedData = (nFlagged > 0) && (isXData2D || isYData2D);

        if (isLogDebug) {
            logger.debug("nFlagged: {}", nFlagged);
            logger.debug("checkFlaggedData: {}", checkFlaggedData);
        }

        final boolean[][] flags = (checkFlaggedData) ? oiData.getFlag() : null;

        // Use targetId on each data row ?
        final boolean checkTargetId = !oiData.hasSingleTarget();

        final short matchTargetId;
        final short[] targetIds;
        if (checkTargetId) {
            // targetID can not be null as the OIData table is supposed to have the target:
            matchTargetId = oiData.getTargetId(getTargetName());
            targetIds = oiData.getTargetId();

            if (isLogDebug) {
                logger.debug("matchTargetId: {}", matchTargetId);
            }
        } else {
            matchTargetId = -1;
            targetIds = null;
        }

        // Get Global SharedSeriesAttributes:
        final SharedSeriesAttributes oixpAttrs = SharedSeriesAttributes.INSTANCE_OIXP;

        // Color mapping:
        // Station configurations:
        // Use staConf (configuration) on each data row ?
        if (isLogDebug) {
            logger.debug("useStaConfColors: {}", (colorMapping == ColorMapping.CONFIGURATION));
            logger.debug("useStaIndexColors: {}", (colorMapping == ColorMapping.STATION_INDEX));
            logger.debug("useWaveLengthColors: {}", (colorMapping == ColorMapping.WAVELENGTH_RANGE));
        }

        // try to fill dataset:
        // avoid loop on wavelength if no 2D data:
        final boolean useWaveLengths = (isXData2D || isYData2D);
        final int nWaveChannels = (useWaveLengths) ? nWaves : 1;

        // TODO: use an XYZ dataset to have a color axis (z) and then use linear or custom z conversion to colors.
        final Color[] mappingWaveLengthColors;

        if (colorMapping == ColorMapping.WAVELENGTH_RANGE) {
            mappingWaveLengthColors = new Color[nWaveChannels];

            final double wlRange = info.waveLengthRange.getLength();

            final float[] effWaveRange = oiData.getEffWaveRange();
            if (!useWaveLengths || (wlRange <= LAMBDA_EPSILON) || (effWaveRange == null)) {
                // single channel or Undefined range: use black:
                Arrays.fill(mappingWaveLengthColors, Color.BLACK);
            } else {
                final double lower = info.waveLengthRange.getLowerBound();
                final int iMaxColor = colorModel.getMapSize() - 1;

                final float[] effWaves = oiData.getOiWavelength().getEffWave();
                float value;

                final float alpha = 0.8f;
                final int alphaMask = Math.round(255 * alpha) << 24;

                for (int i = 0; i < nWaves; i++) {
                    // invert palette to have (VIOLET - BLUE - GREEN - RED) ie color spectrum:
                    value = (float) (iMaxColor * (1.0 - ((effWaves[i] - lower) / wlRange)));

                    mappingWaveLengthColors[i] = new Color(ImageUtils.getRGB(colorModel, iMaxColor, value, alphaMask), true);
                }
            }
        } else {
            mappingWaveLengthColors = null;
        }

        // TODO: adjust renderer settings per Serie (color, shape ...) per series and item at higher level using dataset fields
        final FastXYErrorRenderer renderer = (FastXYErrorRenderer) plot.getRenderer();

        if (isLogDebug) {
            logger.debug("nbSeries to create : {}", nStaIndexes * nWaveChannels);
        }

        // Prepare data models to contain a lot of series:
        final int maxSeriesCount = seriesCount + nStaIndexes * nWaveChannels;
        dataset.ensureCapacity(maxSeriesCount);
        renderer.ensureCapacity(maxSeriesCount);

        // flag indicating that this table has data to plot:
        boolean hasPlotData = false;
        // flag indicating that the dataset contains flagged data:
        boolean hasDataFlag = false;
        // flag indicating that the dataset has data with error on x axis:
        boolean hasDataErrorX = false;
        // flag indicating that the dataset has data with error on y axis:
        boolean hasDataErrorY = false;

        // x and y data ranges:
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minXe = Double.POSITIVE_INFINITY;
        double maxXe = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double minYe = Double.POSITIVE_INFINITY;
        double maxYe = Double.NEGATIVE_INFINITY;

        int[] iRow, iCol;
        double[] xValue, xLower, xUpper, yValue, yLower, yUpper;
        Shape[] itemShapes;

        boolean recycleArray = false;
        final int[][] arrayIntPool = new int[6][];
        final double[][] arrayDblPool = new double[6][];
        Shape[] shapePool = null;

        double x, xErr, y, yErr;

        OITableSerieKey serieKey;

        short[] currentStaIndex;
        short[] currentStaConf;

        String staIndexName, staConfName;

        int nSkipTarget = 0;
        int nSkipFlag = 0;
        boolean isFlag, isXErrValid, isYErrValid, useXErrInBounds, useYErrInBounds;

        int nData = 0;

        // TODO: unroll loops (wave / baseline) ... and avoid repeated checks on rows (targetId, baseline ...)
        // fast access to NaN value:
        final double NaN = Double.NaN;

        // Iterate on wave channels (j):
        for (int i, j = 0, k, idx; j < nWaveChannels; j++) {

            final DataPointer ptr = new DataPointer(oiData,
                    DataPointer.UNDEFINED,
                    (useWaveLengths) ? j : DataPointer.UNDEFINED
            );

            // Iterate on baselines (k):
            for (k = 0; k < nStaIndexes; k++) {

                // get the sta index array:
                currentStaIndex = distinctStaIndexes[k];
                currentStaConf = null;

                // 1 serie per baseline and per spectral channel:
                if (recycleArray) {
                    recycleArray = false;
                    iRow = arrayIntPool[0];
                    iCol = arrayIntPool[1];
                    xValue = arrayDblPool[0];
                    xLower = arrayDblPool[1];
                    xUpper = arrayDblPool[2];
                    yValue = arrayDblPool[3];
                    yLower = arrayDblPool[4];
                    yUpper = arrayDblPool[5];
                    itemShapes = shapePool;
                } else {
                    iRow = new int[nRows];
                    iCol = new int[nRows];
                    xValue = new double[nRows];
                    xLower = new double[nRows];
                    xUpper = new double[nRows];
                    yValue = new double[nRows];
                    yLower = new double[nRows];
                    yUpper = new double[nRows];
                    itemShapes = new Shape[nRows];
                }

                idx = 0;

                // Iterate on table rows (i):
                for (i = 0; i < nRows; i++) {

                    // check sta indexes ?
                    if (checkStaIndex) {
                        // note: sta indexes are compared using pointer comparison:
                        if (currentStaIndex != staIndexes[i]) {
                            // data row does not correspond to current baseline so skip it:
                            continue;
                        }
                    }

                    isFlag = false;
                    if (checkFlaggedData && flags[i][j]) {
                        if (skipFlaggedData) {
                            // data point is flagged so skip it:
                            nSkipFlag++;
                            continue;
                        }
                        hasDataFlag = true;
                        isFlag = true;
                    }

                    if (checkTargetId) {
                        if (matchTargetId != targetIds[i]) {
                            // data row does not coorespond to current target so skip it:
                            nSkipTarget++;
                            continue;
                        }
                    }

                    // staConf corresponds to the baseline also:
                    currentStaConf = staConfs[i];

                    // TODO: filter data (wavelength, baseline, configuration, time ...)
                    // TODO: support function (min, max, mean) applied to array data (2D)
                    // Idea: use custom data consumer (2D, 1D, log or not, error or not)
                    // it will reduce the number of if statements => better performance and simpler code
                    // such data stream could also perform conversion on the fly
                    // and maybe handle symetry (u, -u) (v, -v) ...
                    // Process Y value if not yData is not null:
                    // TODO: remove next test and put it up and out of this loop
                    // or avoid faulty case somewhere else : yData is null if user selects optional column name not present in the file
                    if ((isYData2D) ? yData2D == null : yData1D == null) {
                        y = NaN;
                    } else {
                        y = (isYData2D) ? yData2D[i][j] : yData1D[i];

                        if (yUseLog && (y <= 0.0)) {
                            // keep only strictly positive data:
                            y = NaN;
                        }
                    }

                    if (NumberUtils.isFinite(y)) {
                        // convert y value:
                        if (doConvertY) {
                            y = initialXConverter.evaluate(y);
                        }
                        if (doScaleY) {
                            y = yConverter.evaluate(y);
                        }

                        // Process X value:
                        x = (isXData2D) ? xData2D[i][j] : xData1D[i];

                        if (xUseLog && (x <= 0.0)) {
                            // keep only positive data:
                            x = NaN;
                        }

                        if (NumberUtils.isFinite(x)) {
                            // convert x value:
                            if (doConvertX) {
                                x = initialXConverter.evaluate(x);
                            }
                            if (doScaleX) {
                                x = xConverter.evaluate(x);
                            }

                            // Process X / Y Errors:
                            yErr = (hasErrY) ? ((isYData2D) ? yData2DErr[i][j] : yData1DErr[i]) : NaN;
                            xErr = (hasErrX) ? ((isXData2D) ? xData2DErr[i][j] : xData1DErr[i]) : NaN;

                            // Define Y data:
                            isYErrValid = true;
                            useYErrInBounds = false;

                            if (!NumberUtils.isFinite(yErr)) {
                                yValue[idx] = y;
                                yLower[idx] = NaN;
                                yUpper[idx] = NaN;
                            } else {
                                hasDataErrorY = true;

                                // ensure error is valid ie positive:
                                if (yErr >= 0.0) {
                                    // convert yErr value:
                                    if (doConvertY) {
                                        yErr = initialXConverter.evaluate(yErr);
                                    }
                                    if (doScaleY) {
                                        yErr = yConverter.evaluate(yErr);
                                    }
                                    useYErrInBounds = true;
                                } else {
                                    yErr = Double.POSITIVE_INFINITY;
                                    isYErrValid = false;
                                }

                                yValue[idx] = y;
                                yLower[idx] = y - yErr;
                                yUpper[idx] = y + yErr;

                                // useLog: check if (y - err) <= 0:
                                if (yUseLog && (yLower[idx] <= 0.0)) {
                                    yLower[idx] = Double.MIN_VALUE;
                                    useYErrInBounds = false;
                                }
                            }

                            // update Y boundaries:
                            if (useYErrInBounds) {
                                // update Y boundaries including error:
                                if (yLower[idx] < minYe) {
                                    minYe = yLower[idx];
                                }
                                if (yUpper[idx] > maxYe) {
                                    maxYe = yUpper[idx];
                                }
                            }
                            if (y < minY) {
                                minY = y;
                            }
                            if (y > maxY) {
                                maxY = y;
                            }

                            // Define X data:
                            isXErrValid = true;
                            useXErrInBounds = false;

                            if (!NumberUtils.isFinite(xErr)) {
                                xValue[idx] = x;
                                xLower[idx] = NaN;
                                xUpper[idx] = NaN;
                            } else {
                                hasDataErrorX = true;

                                // ensure error is valid ie positive:
                                if (xErr >= 0.0) {
                                    // convert xErr value:
                                    if (doConvertX) {
                                        xErr = initialXConverter.evaluate(xErr);
                                    }
                                    if (doScaleX) {
                                        xErr = xConverter.evaluate(xErr);
                                    }
                                    useXErrInBounds = true;
                                } else {
                                    xErr = Double.POSITIVE_INFINITY;
                                    isXErrValid = false;
                                }

                                xValue[idx] = x;
                                xLower[idx] = x - xErr;
                                xUpper[idx] = x + xErr;

                                // useLog: check if (x - err) <= 0:
                                if (xUseLog && (xLower[idx] <= 0.0)) {
                                    xLower[idx] = Double.MIN_VALUE;
                                    useXErrInBounds = false;
                                }
                            }

                            // update X boundaries:
                            if (useXErrInBounds) {
                                // update X boundaries including error:
                                if (xLower[idx] < minXe) {
                                    minXe = xLower[idx];
                                }
                                if (xUpper[idx] > maxXe) {
                                    maxXe = xUpper[idx];
                                }
                            }
                            if (x < minX) {
                                minX = x;
                            }
                            if (x > maxX) {
                                maxX = x;
                            }

                            // TODO: adjust renderer settings per Serie (color, shape, shape size, outline ....) !
                            // ~ new custom axis (color, size, shape)
                            // Define item shape:
                            // invalid shape if flagged or invalid error value
                            itemShapes[idx] = getPointShape(isYErrValid && isXErrValid && !isFlag);

                            // Define row / col indices:
                            iRow[idx] = i;
                            iCol[idx] = j;

                            // increment number of valid data in serie arrays:
                            idx++;

                        } // x defined

                    } // y defined

                } // loop on data rows

                if (idx != 0) {
                    hasPlotData = true;
                    nData += idx;

                    // crop data arrays:
                    if (idx < nRows) {
                        recycleArray = true;
                        arrayIntPool[0] = iRow;
                        arrayIntPool[1] = iCol;
                        arrayDblPool[0] = xValue;
                        arrayDblPool[1] = xLower;
                        arrayDblPool[2] = xUpper;
                        arrayDblPool[3] = yValue;
                        arrayDblPool[4] = yLower;
                        arrayDblPool[5] = yUpper;
                        shapePool = itemShapes;

                        iRow = extract(iRow, idx);
                        iCol = extract(iCol, idx);
                        xValue = extract(xValue, idx);
                        xLower = extract(xLower, idx);
                        xUpper = extract(xUpper, idx);
                        yValue = extract(yValue, idx);
                        yLower = extract(yLower, idx);
                        yUpper = extract(yUpper, idx);
                        itemShapes = extract(itemShapes, idx);
                    }

                    // update series index before adding serie:
                    seriesCount = dataset.getSeriesCount();

                    staIndexName = oiData.getStaNames(currentStaIndex); // cached
                    staConfName = oiData.getStaNames(currentStaConf); // cached
                    serieKey = new OITableSerieKey(tableIndex, ptr, k, j, staIndexName, staConfName); // baselines (k) & wave channels (j)

                    // Avoid any key conflict:
                    dataset.addSeries(serieKey,
                            new int[][]{iRow, iCol},
                            new double[][]{xValue, xLower, xUpper, yValue, yLower, yUpper}
                    );

                    // TODO: adjust renderer settings per Serie (color, shape ...) per series and item at higher level using dataset fields
                    // Use special fields into dataset to encode color mapping (color value as double ?)
                    // use colormapping enum:
                    switch (colorMapping) {
                        case WAVELENGTH_RANGE:
                        // wavelength is default:
                        case OBSERVATION_DATE:
                        // not implemented still
                        default:
                            renderer.setSeriesPaint(seriesCount, mappingWaveLengthColors[j], false);
                            break;
                        case CONFIGURATION:
                            oixpAttrs.addLabel(staConfName);
                            break;
                        case STATION_INDEX:
                            oixpAttrs.addLabel(staIndexName);
                            break;
                    }

                    // define shape per item in serie:
                    renderer.setItemShapes(seriesCount, itemShapes);

                    // Add staIndex into the unique used station indexes anyway:
                    info.usedStaIndexNames.add(staIndexName);

                    // Add staConf into the unique used station configurations anyway:
                    info.usedStaConfNames.add(staConfName);
                }

            } // iterate on baselines

        } // iterate on wave channels

        if (!hasPlotData) {
            return;
        }

        if (isLogDebug) {
            if (nSkipFlag != 0) {
                logger.debug("Nb SkipFlag: {}", nSkipFlag);
            }
            if (nSkipTarget != 0) {
                logger.debug("Nb SkipTarget: {}", nSkipTarget);
            }
        }

        // update plot information (should be consistent between calls):
        info.hasPlotData |= true; // logical OR
        info.useWaveLengths |= useWaveLengths; // logical OR
        info.nDataPoints += nData;
        info.hasDataFlag |= hasDataFlag; // logical OR
        info.yAxisIndex = yAxisIndex;
        // add given table:
        info.oidataList.add(oiData);

        AxisInfo axisInfo = info.xAxisInfo;
        axisInfo.columnMeta = xMeta;
        axisInfo.unit = (doScaleX) ? xConverter.getUnit() : null;
        axisInfo.useLog = xUseLog;
        if (axisInfo.dataRange != null) {
            // combine X range:
            minX = Math.min(minX, axisInfo.dataRange.getLowerBound());
            maxX = Math.max(maxX, axisInfo.dataRange.getUpperBound());
        }
        axisInfo.dataRange = new Range(minX, maxX);
        // Ensure Xe range is at least X range:
        minXe = Math.min(minXe, minX);
        maxXe = Math.max(maxXe, maxX);
        if (axisInfo.dataErrRange != null) {
            // combine Xe ranges:
            minXe = Math.min(minXe, axisInfo.dataErrRange.getLowerBound());
            maxXe = Math.max(maxXe, axisInfo.dataErrRange.getUpperBound());
        }
        axisInfo.dataErrRange = new Range(minXe, maxXe);
        axisInfo.hasDataError |= hasDataErrorX; // logical OR

        axisInfo = info.yAxisInfo;
        axisInfo.columnMeta = yMeta;
        axisInfo.unit = (doScaleY) ? yConverter.getUnit() : null;
        axisInfo.useLog = yUseLog;
        if (axisInfo.dataRange != null) {
            // combine Y range:
            minY = Math.min(minY, axisInfo.dataRange.getLowerBound());
            maxY = Math.max(maxY, axisInfo.dataRange.getUpperBound());
        }
        axisInfo.dataRange = new Range(minY, maxY);
        // Ensure Ye range is at least Y range:
        minYe = Math.min(minYe, minY);
        maxYe = Math.max(maxYe, maxY);
        if (axisInfo.dataErrRange != null) {
            // combine Xe ranges:
            minYe = Math.min(minYe, axisInfo.dataErrRange.getLowerBound());
            maxYe = Math.max(maxYe, axisInfo.dataErrRange.getUpperBound());
        }
        axisInfo.dataErrRange = new Range(minYe, maxYe);
        axisInfo.hasDataError |= hasDataErrorY; // logical OR
    }

    private int[] extract(final int[] input, final int len) {
        final int[] output = new int[len];
        // manual array copy is faster on recent machine (64bits / hotspot server compiler)
        for (int i = 0; i < len; i++) {
            output[i] = input[i];
        }
        return output;
    }

    private double[] extract(final double[] input, final int len) {
        final double[] output = new double[len];
        // manual array copy is faster on recent machine (64bits / hotspot server compiler)
        for (int i = 0; i < len; i++) {
            output[i] = input[i];
        }
        return output;
    }

    private Shape[] extract(final Shape[] input, final int len) {
        final Shape[] output = new Shape[len];
        // manual array copy is faster on recent machine (64bits / hotspot server compiler)
        for (int i = 0; i < len; i++) {
            output[i] = input[i];
        }
        return output;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonHideCrossHair;
    private javax.swing.JLabel jLabelCrosshairInfos;
    private javax.swing.JLabel jLabelDataErrRange;
    private javax.swing.JLabel jLabelDataRange;
    private javax.swing.JLabel jLabelInfos;
    private javax.swing.JLabel jLabelMouse;
    private javax.swing.JLabel jLabelNoData;
    private javax.swing.JLabel jLabelPoints;
    private javax.swing.JPanel jPanelCrosshair;
    private javax.swing.JPanel jPanelInfos;
    private javax.swing.JPanel jPanelMouseInfos;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparatorHoriz;
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
                    logger.debug("Drawing chart time[{}] = {} ms.", getTargetName(), 1e-6d * (System.nanoTime() - this.chartDrawStartTime));
                    this.chartDrawStartTime = 0l;
                    break;
                default:
            }
        }

        if (DEBUG) {
            switch (event.getType()) {
                case ChartProgressEvent.DRAWING_STARTED:
                    this.chartDrawStartTime = System.nanoTime();
                    break;
                case ChartProgressEvent.DRAWING_FINISHED:
                    logger.info("Drawing chart time[{}] = {} ms.", getTargetName(), 1e-6d * (System.nanoTime() - this.chartDrawStartTime));
                    this.chartDrawStartTime = 0l;
                    break;
                default:
            }
        }

        if (event.getType() == ChartProgressEvent.DRAWING_STARTED) {
            // Perform custom operations before chart rendering:

            // Get shared domain axis:
            final ValueAxis xAxis = this.combinedXYPlot.getDomainAxis();
            final Range xRange = xAxis.getRange();

            boolean negX = false;
            boolean posX = false;

            for (PlotInfo info : getPlotInfos()) {
                final int plotIndex = info.yAxisIndex;
                final XYPlot xyPlot = xyPlotList.get(plotIndex);

                if (xyPlot != null) {
                    final ValueAxis yAxis = xyPlot.getRangeAxis();
                    final Range yRange = yAxis.getRange();

                    // move JMMC annotations:
                    final XYTextAnnotation aJMMC = this.aJMMCPlots.get(plotIndex);
                    aJMMC.setX(xRange.getUpperBound());
                    aJMMC.setY(yRange.getLowerBound());

                    // Add marks indicating that the current axis is smaller than the data range:
                    Range dataRange = info.xAxisInfo.dataRange;
                    if (dataRange != null) {
                        if (xRange.getLowerBound() > dataRange.getLowerBound()) {
                            negX = true;
                        }
                        if (xRange.getUpperBound() < dataRange.getUpperBound()) {
                            posX = true;
                        }
                    }

                    boolean negY = false;
                    boolean posY = false;

                    dataRange = info.yAxisInfo.dataRange;
                    if (dataRange != null) {
                        if (yRange.getLowerBound() > dataRange.getLowerBound()) {
                            negY = true;
                        }
                        if (yRange.getUpperBound() < dataRange.getUpperBound()) {
                            posY = true;
                        }
                    }

                    if (negY || posY) {
                        ChartUtils.setAxisDecorations(yAxis, ChartColor.DARK_RED, negY, posY);
                    } else {
                        ChartUtils.setAxisDecorations(yAxis, Color.BLACK, false, false);
                    }
                }
            }

            if (negX || posX) {
                ChartUtils.setAxisDecorations(xAxis, ChartColor.DARK_RED, negX, posX);
            } else {
                ChartUtils.setAxisDecorations(xAxis, Color.BLACK, false, false);
            }
        } else {
            // Collect rendered item count:
            for (PlotInfo info : getPlotInfos()) {
                final int plotIndex = info.yAxisIndex;
                final XYPlot xyPlot = xyPlotList.get(plotIndex);

                if (xyPlot != null) {
                    final FastXYErrorRenderer renderer = (FastXYErrorRenderer) xyPlot.getRenderer();
                    info.nDisplayedPoints = renderer.getRenderedItemCount();
                }
            }
            if (lastChartMouseEvent != null) {
                SwingUtils.invokeLaterEDT(new Runnable() {
                    @Override
                    public void run() {
                        chartMouseMoved(lastChartMouseEvent);
                    }
                });
            }
        }
    }

    /**
     * Return the shape used to represent points on the plot
     * @param valid flag indicating if the the point is valid
     * @return shape
     */
    private static Shape getPointShape(final boolean valid) {
        return (valid) ? shapePointValid : shapePointInvalid;
    }

    /* Plot information */
    /**
     * TODO: make PlotInfo public !!
     * @return plotInfo list
     */
    public List<PlotInfo> getPlotInfos() {
        return this.plotInfos;
    }

    /**
     * TODO: make PlotInfo public !!
     * @return first plotInfo
     */
    public PlotInfo getFirstPlotInfo() {
        return getPlotInfos().get(0);
    }

    /**
     * Return true if the plot has data (dataset not empty)
     * @return true if the plot has data
     */
    public boolean isHasData() {
        return !getPlotInfos().isEmpty();
    }


    /* --- OIFits helper : TODO move elsewhere --- */
    /**
     * Return the unique String values from given operator applied on given OIData tables
     * @param oiDataList OIData tables
     * @param set set instance to use
     * @param operator operator to get String values
     * @return unique String values
     */
    private static Set<String> getDistinct(final List<OIData> oiDataList, final Set<String> set, final GetOIDataString operator) {
        String value;
        for (OIData oiData : oiDataList) {
            value = operator.getString(oiData);
            if (value != null) {
                logger.debug("getDistinct: {}", value);

                int pos = value.indexOf('_');

                if (pos != -1) {
                    value = value.substring(0, pos);
                }

                set.add(value);
            }
        }
        return set;
    }

    /**
     * Return the unique staNames values (sorted by name) from given OIData tables
     * @param oiDataList OIData tables
     * @return given set instance
     */
    private static List<String> getDistinctStaNames(final List<OIData> oiDataList) {
        Set<String> set = new HashSet<String>(32);

        String staNames;
        for (OIData oiData : oiDataList) {
            for (short[] staIndexes : oiData.getDistinctStaIndex()) {
                staNames = oiData.getStaNames(staIndexes);
                set.add(staNames);
            }
        }
        // Sort by name (consistent naming & colors):
        final List<String> sortedList = new ArrayList<String>(set);
        Collections.sort(sortedList, StationNamesComparator.INSTANCE);

        logger.debug("getDistinctStaNames : {}", sortedList);
        return sortedList;
    }

    /**
     * Return the unique staConfs values from given OIData tables
     * @param oiDataList OIData tables
     * @return given set instance
     */
    private static List<String> getDistinctStaConfs(final List<OIData> oiDataList) {
        Set<String> set = new HashSet<String>(32);

        String staNames;
        for (OIData oiData : oiDataList) {
            for (short[] staConf : oiData.getDistinctStaConf()) {
                staNames = oiData.getStaNames(staConf);
                set.add(staNames);
            }
        }
        // Sort by name (consistent naming & colors):
        final List<String> sortedList = new ArrayList<String>(set);
        Collections.sort(sortedList, StationNamesComparator.INSTANCE);

        logger.debug("getDistinctStaConfs : {}", sortedList);
        return sortedList;
    }

    /**
     * Return the unique wave length ranges from given OIData tables
     * @param oiDataList OIData tables
     * @param set set instance to use
     */
    private static void getDistinctWaveLengthRange(final List<OIData> oiDataList, final Set<String> set) {
        final StringBuilder sb = new StringBuilder(20);

        String wlenRange;
        float[] effWaveRange;
        for (OIData oiData : oiDataList) {
            effWaveRange = oiData.getEffWaveRange();

            if (effWaveRange != null) {
                sb.append('[').append(df4.format(ConverterFactory.CONVERTER_MICRO_METER.evaluate(effWaveRange[0]))).append(' ').append(ConverterFactory.CONVERTER_MICRO_METER.getUnit());
                sb.append(" - ").append(df4.format(ConverterFactory.CONVERTER_MICRO_METER.evaluate(effWaveRange[1]))).append(' ').append(ConverterFactory.CONVERTER_MICRO_METER.getUnit()).append(']');

                wlenRange = sb.toString();
                sb.setLength(0);

                logger.debug("wlen range : {}", wlenRange);

                set.add(wlenRange);
            }
        }
    }

    /**
     * Return the largest wave length range from given OIData tables
     * @param oiDataList OIData tables
     * @return largest wave length range
     */
    private static Range getWaveLengthRange(final List<OIData> oiDataList) {
        final float[] range = new float[]{Float.NaN, Float.NaN};
        float[] effWaveRange;
        for (OIData oiData : oiDataList) {
            effWaveRange = oiData.getEffWaveRange();

            if (effWaveRange != null) {
                if (Float.isNaN(range[0]) || range[0] > effWaveRange[0]) {
                    range[0] = effWaveRange[0];
                }
                if (Float.isNaN(range[1]) || range[1] < effWaveRange[1]) {
                    range[1] = effWaveRange[1];
                }
            }
        }
        return new Range(range[0], range[1]);
    }

    private static void toString(final Set<String> set, final StringBuilder sb, final String internalSeparator, final String separator) {
        toString(set, sb, internalSeparator, separator, Integer.MAX_VALUE);
    }

    private static void toString(final Set<String> set, final StringBuilder sb, final String internalSeparator, final String separator, final int threshold, final String alternateText) {
        // hard coded limit:
        if (set.size() > threshold) {
            sb.append(alternateText);
        } else {
            toString(set, sb, internalSeparator, separator, Integer.MAX_VALUE);
        }
    }

    private static void toString(final Set<String> set, final StringBuilder sb, final String internalSeparator, final String separator, final int maxLength) {
        int n = 0;
        for (String v : set) {
            sb.append(StringUtils.replaceWhiteSpaces(v, internalSeparator)).append(separator);
            n++;
            if (n > maxLength) {
                return;
            }
        }
        if (n != 0) {
            // remove separator at the end:
            sb.setLength(sb.length() - separator.length());

        }
    }

    /**
     * Get String operator applied on any OIData table
     */
    private interface GetOIDataString {

        /**
         * Return a String value (keyword for example) for the given OIData table
         * @param oiData OIData table
         * @return String value
         */
        public String getString(final OIData oiData);
    }

    private Plot getPlot() {
        if (this.plot == null) {
            this.plot = ocm.getPlotRef(plotId);
        }
        return this.plot;
    }

    /**
     * Define the plot identifier, reset plot and fireOIFitsCollectionChanged on this instance if the plotId changed
     * @param plotId plot identifier
     */
    public void setPlotId(final String plotId) {
        final String prevPlotId = this.plotId;
        this.plotId = plotId;
        // force reset:
        this.plot = null;

        if (plotId != null && !ObjectUtils.areEquals(prevPlotId, plotId)) {
            logger.debug("setPlotId {}", plotId);

            // fire PlotChanged event to initialize correctly the widget:
            ocm.firePlotChanged(null, plotId, this); // null forces different source
        }
    }

    private PlotDefinition getPlotDefinition() {
        if (getPlot() == null) {
            return null;
        }
        return getPlot().getPlotDefinition();
    }

    private OIFitsFile getOiFitsSubset() {
        if (getPlot() == null || getPlot().getSubsetDefinition() == null) {
            return null;
        }
        return getPlot().getSubsetDefinition().getOIFitsSubset();
    }

    private String getTargetName() {
        if (getPlot() == null || getPlot().getSubsetDefinition() == null || getPlot().getSubsetDefinition().getTarget() == null) {
            return null;
        }
        return getPlot().getSubsetDefinition().getTarget().getTarget();

    }

    /*
     * OIFitsCollectionManagerEventListener implementation
     */
    /**
     * Return the optional subject id i.e. related object id that this listener accepts
     * @param type event type
     * @return subject id (null means accept any event) or DISCARDED_SUBJECT_ID to discard event
     */
    @Override
    public String getSubjectId(final OIFitsCollectionManagerEventType type) {
        switch (type) {
            case PLOT_CHANGED:
                return plotId;
            default:
        }
        return DISCARDED_SUBJECT_ID;
    }

    /**
     * Handle the given OIFits collection event
     * @param event OIFits collection event
     */
    @Override
    public void onProcess(final OIFitsCollectionManagerEvent event) {
        logger.debug("onProcess {}", event);

        switch (event.getType()) {
            case PLOT_CHANGED:
                /* store plot instance (reference) */
                plot = event.getPlot();

                updatePlot();
                break;
            case SELECTION_CHANGED:
                refreshCrosshair(event.getSelection());
                break;
            default:
        }
        logger.debug("onProcess {} - done", event);
    }

    /**
     * Return true (use symmetry) if both axis use MegaLambda converter (ie 'are' both spatial frequencies)
     * @param xAxis x axis
     * @param yAxis y axis
     * @return true (use symmetry) if both axis 'are' both spatial frequencies
     */
    private boolean useSymmetry(final Axis xAxis, final Axis yAxis) {
        return ConverterFactory.KEY_MEGA_LAMBDA.equals(xAxis.getConverter())
                && ConverterFactory.KEY_MEGA_LAMBDA.equals(yAxis.getConverter());
    }

}
