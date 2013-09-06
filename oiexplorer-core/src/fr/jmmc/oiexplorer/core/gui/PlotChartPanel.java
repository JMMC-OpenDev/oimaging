/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui;

import fr.jmmc.jmal.image.ColorModels;
import fr.jmmc.jmal.image.ImageUtils;
import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.jmcs.util.SpecialChars;
import fr.jmmc.jmcs.util.StringUtils;
import fr.jmmc.oiexplorer.core.function.Converter;
import fr.jmmc.oiexplorer.core.function.ConverterFactory;
import fr.jmmc.oiexplorer.core.gui.action.ExportPDFAction;
import fr.jmmc.oiexplorer.core.gui.chart.BoundedLogAxis;
import fr.jmmc.oiexplorer.core.gui.chart.BoundedNumberAxis;
import fr.jmmc.oiexplorer.core.gui.chart.ChartMouseSelectionListener;
import fr.jmmc.oiexplorer.core.gui.chart.ChartUtils;
import fr.jmmc.oiexplorer.core.gui.chart.ColorPalette;
import fr.jmmc.oiexplorer.core.gui.chart.CombinedCrosshairOverlay;
import fr.jmmc.oiexplorer.core.gui.chart.EnhancedChartMouseListener;
import fr.jmmc.oiexplorer.core.gui.chart.FastXYErrorRenderer;
import fr.jmmc.oiexplorer.core.gui.chart.PDFOptions;
import fr.jmmc.oiexplorer.core.gui.chart.SelectionOverlay;
import fr.jmmc.oiexplorer.core.gui.chart.dataset.FastIntervalXYDataset;
import fr.jmmc.oiexplorer.core.gui.chart.dataset.OITableSerieKey;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEvent;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventListener;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventType;
import fr.jmmc.oiexplorer.core.model.oi.Plot;
import fr.jmmc.oiexplorer.core.model.plot.Axis;
import fr.jmmc.oiexplorer.core.model.plot.ColorMapping;
import fr.jmmc.oiexplorer.core.model.plot.PlotDefinition;
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
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.Layer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This panel provides the chart panel representing one OIFitsExplorer plot instance (using its subset and plot definition)
 * 
 * @author bourgesl
 */
public final class PlotChartPanel extends javax.swing.JPanel implements ChartProgressListener, EnhancedChartMouseListener, ChartMouseSelectionListener,
                                                                        PDFExportable, OIFitsCollectionManagerEventListener {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** true to use System.arraycopy instead of manual copy */
    private static final boolean USE_SYSTEM_ARRAY_COPY = false;
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(PlotChartPanel.class.getName());
    /** data margin in percents (5%) */
    private final static double MARGIN_PERCENTS = 5d / 100d;
    /** double formatter for wave lengths */
    private final static NumberFormat df4 = new DecimalFormat("0.000#");
    private static final Shape shapePointValid;
    private static final Shape shapePointInvalid;

    static {
        // initialize point shapes:
        shapePointValid = new Rectangle(-3, -3, 6, 6) {
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
        ypoints[0] = -4;
        xpoints[1] = 3;
        ypoints[1] = 2;
        xpoints[2] = -3;
        ypoints[2] = 2;

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
    /* TODO: List<XYPlot> */
    /** xy plot instance 1 */
    private XYPlot xyPlotPlot1;
    /** xy plot instance 2 */
    private XYPlot xyPlotPlot2;
    /* TODO: List<XYTextAnnotation> */
    /** JMMC annotation */
    private XYTextAnnotation aJMMCPlot1 = null;
    /** JMMC annotation */
    private XYTextAnnotation aJMMCPlot2 = null;

    /**
     * Constructor
     */
    public PlotChartPanel() {
        ocm.getPlotChangedEventNotifier().register(this);

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

        jLabelNoData = new javax.swing.JLabel();

        setBackground(new java.awt.Color(255, 255, 255));
        setLayout(new java.awt.BorderLayout());

        jLabelNoData.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelNoData.setText("No data to plot.");
        add(jLabelNoData, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Export the chart component as a PDF document
     */
    @Override
    public void performPDFAction() {
        // if no OIFits data, discard action:
        if (getOiFitsSubset() != null && isHasData()) {
            ExportPDFAction.exportPDF(this);
        }
    }

    /**
     * Return the PDF default file name
     * [Vis2_<TARGET>_<INSTRUMENT>_<CONFIGURATION>_<DATE>]
     * @return PDF default file name
     */
    @Override
    public String getPDFDefaultFileName() {

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

            sb.append('.').append(PDF_EXT);

            return sb.toString();
        }
        return null;
    }

    /**
     * Prepare the chart(s) before exporting them as a PDF document:
     * Performs layout and return PDF options
     * @return PDF options
     */
    public PDFOptions preparePDFExport() {
        return PDFOptions.PDF_A3_LANDSCAPE;
    }

    /**
     * Return the chart to export on the given page index
     * @param pageIndex page index (1..n)
     * @return chart
     */
    @Override
    public JFreeChart prepareChart(final int pageIndex) {
        return this.chart;
    }

    /**
     * Callback indicating the chart was processed by the PDF engine
     */
    @Override
    public void postPDFExport() {
        // no-op
    }

    /**
     * This method is useful to set the models and specific features of initialized swing components :
     */
    private void postInit() {

        final boolean usePlotCrossHairSupport = false;
        final boolean useSelectionSupport = false; // TODO: enable selection ASAP (TODO sub plot support)

        // create chart and add listener :
        this.combinedXYPlot = new CombinedDomainXYPlot(ChartUtils.createAxis(""));
        this.combinedXYPlot.setGap(10.0D);
        this.combinedXYPlot.setOrientation(PlotOrientation.VERTICAL);

        // enlarge right margin to have last displayed value:
        this.combinedXYPlot.setInsets(ChartUtils.NORMAL_PLOT_INSETS);

        configureCrosshair(this.combinedXYPlot, usePlotCrossHairSupport);

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

        // Create sub plots (TODO externalize):

        this.xyPlotPlot1 = createScientificScatterPlot(null, "", usePlotCrossHairSupport);

        this.aJMMCPlot1 = ChartUtils.createJMMCAnnotation(Constants.JMMC_ANNOTATION);
        this.xyPlotPlot1.getRenderer().addAnnotation(this.aJMMCPlot1, Layer.BACKGROUND);

        this.xyPlotPlot2 = createScientificScatterPlot(null, "", usePlotCrossHairSupport);

        this.aJMMCPlot2 = ChartUtils.createJMMCAnnotation(Constants.JMMC_ANNOTATION);
        this.xyPlotPlot2.getRenderer().addAnnotation(this.aJMMCPlot2, Layer.BACKGROUND);

        if (!usePlotCrossHairSupport) {
            Integer plotIndex = NumberUtils.valueOf(1);
            crosshairOverlay.addDomainCrosshair(plotIndex, createCrosshair());
            crosshairOverlay.addRangeCrosshair(plotIndex, createCrosshair());

            plotIndex = NumberUtils.valueOf(2);
            crosshairOverlay.addDomainCrosshair(plotIndex, createCrosshair());
            crosshairOverlay.addRangeCrosshair(plotIndex, createCrosshair());
        }

        resetPlot();
    }

    private static Crosshair createCrosshair() {
        final Crosshair crosshair = new Crosshair(Double.NaN);
        crosshair.setPaint(Color.BLUE);
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
        return (eventType == EnhancedChartMouseListener.EVENT_CLICKED);
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

            final Integer plotIndex = NumberUtils.valueOf(subplotIndex + 1);

            final XYPlot xyPlot = this.plotIndexMapping.get(plotIndex);
            if (xyPlot == null) {
                return;
            }

            final ValueAxis domainAxis = xyPlot.getDomainAxis();
            final double domainValue = domainAxis.java2DToValue(point2D.getX(), dataArea, xyPlot.getDomainAxisEdge());

            final ValueAxis rangeAxis = xyPlot.getRangeAxis();
            final double rangeValue = rangeAxis.java2DToValue(point2D.getY(), dataArea, xyPlot.getRangeAxisEdge());

            if (logger.isDebugEnabled()) {
                logger.debug("Mouse coordinates are (" + i + ", " + j + "), in data space = (" + domainValue + ", " + rangeValue + ")");
            }

            // aspect ratio:
            final double xRatio = dataArea.getWidth() / Math.abs(domainAxis.getUpperBound() - domainAxis.getLowerBound());
            final double yRatio = dataArea.getHeight() / Math.abs(rangeAxis.getUpperBound() - rangeAxis.getLowerBound());

            // find matching data ie. closest data point according to its screen distance to the mouse clicked point:
            Point2D dataPoint = findDataPoint(xyPlot, domainValue, rangeValue, xRatio, yRatio);

            List<Crosshair> xCrosshairs = this.crosshairOverlay.getDomainCrosshairs(plotIndex);
            if (xCrosshairs.size() == 1) {
                xCrosshairs.get(0).setValue(dataPoint.getX());
            }
            List<Crosshair> yCrosshairs = this.crosshairOverlay.getRangeCrosshairs(plotIndex);
            if (yCrosshairs.size() == 1) {
                yCrosshairs.get(0).setValue(dataPoint.getY());
            }

            // update other plot crosshairs:
            for (Integer index : this.plotIndexMapping.keySet()) {
                if (index != plotIndex) {
                    final XYPlot otherPlot = this.plotIndexMapping.get(index);
                    if (otherPlot != null) {
                        xCrosshairs = this.crosshairOverlay.getDomainCrosshairs(index);
                        if (xCrosshairs.size() == 1) {
                            xCrosshairs.get(0).setValue(dataPoint.getX());
                        }
                        yCrosshairs = this.crosshairOverlay.getRangeCrosshairs(index);
                        if (yCrosshairs.size() == 1) {
                            yCrosshairs.get(0).setValue(Double.NaN);
                        }
                    }
                }
            }
        }
    }

    /**
     * Not implemented
     * @param chartMouseEvent useless
     */
    @Override
    public void chartMouseMoved(final ChartMouseEvent chartMouseEvent) {
        // enable this to move crosshair when the mouse is over the plot:
        if (false) {
            chartMouseClicked(chartMouseEvent);
        }
    }

    /**
     * Handle rectangular selection event
     *
     * @param selection the selected region.
     */
    @Override
    public void mouseSelected(final Rectangle2D selection) {
        logger.debug("mouseSelected: rectangle {}", selection);

        // TODO: determine which plot to use ?

        // find data points:
        final List<Point2D> points = findDataPoints(selection);

        // push data points to overlay for rendering:
        this.selectionOverlay.setPoints(points);
    }

    /**
     * Find data point closest in FIRST dataset to the given coordinates X / Y
     * @param xyPlot xy plot to get its dataset
     * @param anchorX domain axis coordinate
     * @param anchorY range axis coordinate
     * @param xRatio pixels per data on domain axis
     * @param yRatio pixels per data on range axis
     * @return found Point2D (data coordinates) or Point2D(NaN, NaN)
     */
    private static Point2D findDataPoint(final XYPlot xyPlot, final double anchorX, final double anchorY, final double xRatio, final double yRatio) {
        final XYDataset dataset = xyPlot.getDataset();

        if (dataset != null) {

            // TODO: move such code elsewhere : ChartUtils or XYDataSetUtils ?

            final long startTime = System.nanoTime();

            double minDistance = Double.POSITIVE_INFINITY;
            int matchSerie = -1;
            int matchItem = -1;

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

            if (matchItem != -1) {
                final double matchX = dataset.getXValue(matchSerie, matchItem);
                final double matchY = dataset.getYValue(matchSerie, matchItem);

                if (logger.isDebugEnabled()) {
                    logger.debug("Matching item [serie = " + matchSerie + ", item = " + matchItem + "] : (" + matchX + ", " + matchY + ")");
                }

                return new Point2D.Double(matchX, matchY);
            }
        }

        logger.debug("No Matching item.");

        return new Point2D.Double(Double.NaN, Double.NaN);
    }

    /**
     * Find data points inside the given Shape (data coordinates)
     * @param shape shape to use
     * @return found list of Point2D (data coordinates) or empty list
     */
    private List<Point2D> findDataPoints(final Shape shape) {
        // TODO: generalize which plot use
        final XYDataset dataset = this.xyPlotPlot1.getDataset();

        final List<Point2D> points = new ArrayList<Point2D>();

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
        this.xyPlotPlot1.setNotify(false);
        this.xyPlotPlot2.setNotify(false);
        try {
            // reset title:
            ChartUtils.clearTextSubTitle(this.chart);

            removeAllSubPlots();

            // reset plots:
            resetXYPlot(this.xyPlotPlot1);
            resetXYPlot(this.xyPlotPlot2);

            showPlot(isHasData());

        } finally {
            // restore chart & plot notifications:
            this.xyPlotPlot2.setNotify(true);
            this.xyPlotPlot1.setNotify(true);
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
        for (Object subPlot : this.combinedXYPlot.getSubplots().toArray()) {
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
        this.xyPlotPlot1.setNotify(false);
        this.xyPlotPlot2.setNotify(false);

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

                sb.append(" ");

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
            this.xyPlotPlot2.setNotify(true);
            this.xyPlotPlot1.setNotify(true);
            this.chart.setNotify(true);
        }

        logger.info("plot : duration = {} ms.", 1e-6d * (System.nanoTime() - start));
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
        // reset crossHairs:
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

        // reset selection:
        if (this.selectionOverlay != null) {
            this.selectionOverlay.reset();
        }
    }

    /**
     * Update the datasets
     */
    private void updateChart() {
        logger.debug("updateChart: plot {}", this.plotId);

        final OIFitsFile oiFitsSubset = getOiFitsSubset();
        final PlotDefinition plotDef = getPlotDefinition();

        // Get distinct station indexes from OIFits subset (not filtered):
        final List<String> distinctStaIndexNames = new ArrayList<String>(getDistinctStaNames(oiFitsSubset.getOiDataList(), new LinkedHashSet<String>()));

        // Get distinct station configuration from OIFits subset (not filtered):
        final List<String> distinctStaConfNames = new ArrayList<String>(getDistinctStaConfs(oiFitsSubset.getOiDataList(), new LinkedHashSet<String>()));

        final Range waveLengthRange = getWaveLengthRange(oiFitsSubset.getOiDataList());

        logger.debug("distinctStaIndexNames: {}", distinctStaIndexNames);
        logger.debug("distinctStaConfNames: {}", distinctStaConfNames);
        logger.debug("waveLengthRange: {}", waveLengthRange);

        boolean showPlot1 = false;
        boolean showPlot2 = false;

        ColumnMeta xMeta = null;

        BoundedNumberAxis axis;

        // Global converter (symmetry)
        Converter xConverter = null, yConverter = null;

        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        boolean xUseLog = false;
        String xUnit = null;

        // reset plot anyway (so free memory):
        resetXYPlot(this.xyPlotPlot1);

        if (!plotDef.getYAxes().isEmpty()) {

            if (oiFitsSubset.getNbOiTables() != 0) {
                final FastIntervalXYDataset<OITableSerieKey, OITableSerieKey> dataset = new FastIntervalXYDataset<OITableSerieKey, OITableSerieKey>();

                final PlotInfo info = new PlotInfo();
                info.distinctStaIndexNames = distinctStaIndexNames;
                info.distinctStaConfNames = distinctStaConfNames;
                info.waveLengthRange = waveLengthRange;

                int tableIndex = 0;

                // Use symmetry for spatial frequencies:
                if (useSymmetry(plotDef.getXAxis(), plotDef.getYAxes().get(0))) {
                    xConverter = yConverter = ConverterFactory.getInstance().getDefault(ConverterFactory.CONVERTER_REFLECT);

                    for (OIData oiData : oiFitsSubset.getOiDataList()) {

                        // process data and add data series into given dataset:
                        updatePlot(this.xyPlotPlot1, oiData, tableIndex, plotDef, 0, dataset, xConverter, yConverter, info);

                        tableIndex++;
                    }
                    xConverter = yConverter = null;
                }

                for (OIData oiData : oiFitsSubset.getOiDataList()) {

                    // process data and add data series into given dataset:
                    updatePlot(this.xyPlotPlot1, oiData, tableIndex, plotDef, 0, dataset, xConverter, yConverter, info);

                    tableIndex++;
                }

                if (info.hasPlotData) {
                    showPlot1 = true;

                    if (logger.isDebugEnabled()) {
                        logger.debug("xyPlotPlot1: nData = {}", info.nData);
                        logger.debug("xyPlotPlot1: nbSeries = {}", dataset.getSeriesCount());
                    }

                    // add plot info:
                    getPlotInfos().add(info);

                    // update X axis information:
                    if (xMeta == null && info.xAxisInfo.columnMeta != null) {
                        xMeta = info.xAxisInfo.columnMeta;
                        xUseLog = info.xAxisInfo.useLog;
                        xUnit = info.xAxisInfo.unit;
                    }

                    // update X range:
                    minX = Math.min(minX, info.xAxisInfo.dataRange.getLowerBound());
                    maxX = Math.max(maxX, info.xAxisInfo.dataRange.getUpperBound());

                    boolean yUseLog = false;
                    String yUnit = null;
                    ColumnMeta yMeta = null;

                    // update Y axis information:
                    if (info.yAxisInfo.columnMeta != null) {
                        yMeta = info.yAxisInfo.columnMeta;
                        yUseLog = info.yAxisInfo.useLog;
                        yUnit = info.yAxisInfo.unit;
                    }

                    // get Y range:
                    double minY = info.yAxisInfo.dataRange.getLowerBound();
                    double maxY = info.yAxisInfo.dataRange.getUpperBound();

                    logger.debug("rangeAxis: {} - {}", minY, maxY);

                    // Add margin:
                    if (yUseLog) {
                        double minTen = Math.floor(Math.log10(minY));
                        double maxTen = Math.ceil(Math.log10(maxY));

                        if (maxTen == minTen) {
                            minTen -= MARGIN_PERCENTS;
                        }

                        minY = Math.pow(10d, minTen); // lower power of ten
                        maxY = Math.pow(10d, maxTen); // upper power of ten
                    } else {
                        // use column meta's default range:
                        if (yMeta != null && yMeta.getDataRange() != null) {
                            final DataRange dataRange = yMeta.getDataRange();

                            if (!Double.isNaN(dataRange.getMin())) {
                                minY = Math.min(minY, dataRange.getMin());
                            }

                            if (!Double.isNaN(dataRange.getMax())) {
                                maxY = Math.max(maxY, dataRange.getMax());
                            }
                        }

                        final double marginY = (maxY - minY) * MARGIN_PERCENTS;
                        if (marginY > 0d) {
                            minY -= marginY;
                            maxY += marginY;
                        } else {
                            minY -= Math.abs(minY) * MARGIN_PERCENTS;
                            maxY += Math.abs(maxY) * MARGIN_PERCENTS;
                        }
                        if (maxY == minY) {
                            maxY = minY + 1d;
                        }
                    }
                    logger.debug("fixed rangeAxis: {} - {}", minY, maxY);

                    // update view range:
                    info.yAxisInfo.viewRange = new Range(minY, maxY);

                    // Update Y axis:
                    if (!yUseLog) {
                        this.xyPlotPlot1.setRangeAxis(ChartUtils.createAxis(""));
                    }
                    if (this.xyPlotPlot1.getRangeAxis() instanceof BoundedNumberAxis) {
                        axis = (BoundedNumberAxis) this.xyPlotPlot1.getRangeAxis();
                        axis.setBounds(new Range(minY, maxY));
                        axis.setRange(minY, maxY);
                    }

                    // update Y axis Label:
                    String label = "";
                    if (yMeta != null) {
                        label = yMeta.getName();
                        if (yUnit != null) {
                            label += " (" + yUnit + ")";
                        } else if (yMeta.getUnits() != Units.NO_UNIT) {
                            label += " (" + yMeta.getUnits().getStandardRepresentation() + ")";
                        }
                        this.xyPlotPlot1.getRangeAxis().setLabel(label);
                    }

                    if (yUseLog) {
                        final BoundedLogAxis logAxis = new BoundedLogAxis("log " + label);
                        logAxis.setExpTickLabelsFlag(true);
                        logAxis.setAutoRangeNextLogFlag(true);

                        logAxis.setBounds(new Range(minY, maxY));
                        logAxis.setRange(minY, maxY);

                        this.xyPlotPlot1.setRangeAxis(logAxis);
                    }
                    // tick color :
                    this.xyPlotPlot1.getRangeAxis().setTickMarkPaint(Color.BLACK);

                    // update plot's renderer before dataset (avoid notify events):
                    final FastXYErrorRenderer renderer = (FastXYErrorRenderer) this.xyPlotPlot1.getRenderer();

                    // enable/disable X error rendering (performance):
                    renderer.setDrawXError(info.xAxisInfo.hasDataErrorX);

                    // enable/disable Y error rendering (performance):
                    renderer.setDrawYError(info.yAxisInfo.hasDataErrorY);

                    // use deprecated method but defines shape once for ALL series (performance):
                    // define base shape as valid point (fallback):
                    renderer.setBaseShape(shapePointValid, false);

                    renderer.setLinesVisible(plotDef.isDrawLine());

                    // update plot's dataset (notify events):
                    this.xyPlotPlot1.setDataset(dataset);
                }
            }
        }

        if (showPlot1) {
            this.combinedXYPlot.add(this.xyPlotPlot1, 1);

            final Integer plotIndex = NumberUtils.valueOf(1);
            this.plotMapping.put(this.xyPlotPlot1, plotIndex);
            this.plotIndexMapping.put(plotIndex, this.xyPlotPlot1);
        }


        // reset plot anyway (so free memory):
        resetXYPlot(this.xyPlotPlot2);

        if (plotDef.getYAxes().size() > 1) {
            if (oiFitsSubset.getNbOiTables() != 0) {
                final FastIntervalXYDataset<OITableSerieKey, OITableSerieKey> dataset = new FastIntervalXYDataset<OITableSerieKey, OITableSerieKey>();

                final PlotInfo info = new PlotInfo();
                info.distinctStaIndexNames = distinctStaIndexNames;
                info.distinctStaConfNames = distinctStaConfNames;
                info.waveLengthRange = waveLengthRange;

                int tableIndex = 0;

                // Use symmetry for spatial frequencies:
                if (useSymmetry(plotDef.getXAxis(), plotDef.getYAxes().get(1))) {
                    xConverter = yConverter = ConverterFactory.getInstance().getDefault(ConverterFactory.CONVERTER_REFLECT);

                    for (OIData oiData : oiFitsSubset.getOiDataList()) {

                        // process data and add data series into given dataset:
                        updatePlot(this.xyPlotPlot2, oiData, tableIndex, plotDef, 1, dataset, xConverter, yConverter, info);

                        tableIndex++;
                    }
                    xConverter = yConverter = null;
                }

                for (OIData oiData : oiFitsSubset.getOiDataList()) {

                    // process data and add data series into given dataset:
                    updatePlot(this.xyPlotPlot2, oiData, tableIndex, plotDef, 1, dataset, xConverter, yConverter, info);

                    tableIndex++;
                }

                if (info.hasPlotData) {
                    showPlot2 = true;

                    if (logger.isDebugEnabled()) {
                        logger.debug("xyPlotPlot2: nData = {}", info.nData);
                        logger.debug("xyPlotPlot2: nbSeries = {}", dataset.getSeriesCount());
                    }

                    // add plot info:
                    getPlotInfos().add(info);

                    // update X axis information:
                    if (xMeta == null && info.xAxisInfo.columnMeta != null) {
                        xMeta = info.xAxisInfo.columnMeta;
                        xUseLog = info.xAxisInfo.useLog;
                        xUnit = info.xAxisInfo.unit;
                    }

                    // update X range:
                    minX = Math.min(minX, info.xAxisInfo.dataRange.getLowerBound());
                    maxX = Math.max(maxX, info.xAxisInfo.dataRange.getUpperBound());

                    boolean yUseLog = false;
                    String yUnit = null;
                    ColumnMeta yMeta = null;

                    // update Y axis information:
                    if (info.yAxisInfo.columnMeta != null) {
                        yMeta = info.yAxisInfo.columnMeta;
                        yUseLog = info.yAxisInfo.useLog;
                        yUnit = info.yAxisInfo.unit;
                    }

                    // get Y range:
                    double minY = info.yAxisInfo.dataRange.getLowerBound();
                    double maxY = info.yAxisInfo.dataRange.getUpperBound();

                    logger.debug("rangeAxis: {} - {}", minY, maxY);

                    // Add margin:
                    if (yUseLog) {
                        double minTen = Math.floor(Math.log10(minY));
                        double maxTen = Math.ceil(Math.log10(maxY));

                        if (maxTen == minTen) {
                            minTen -= MARGIN_PERCENTS;
                        }

                        minY = Math.pow(10d, minTen); // lower power of ten
                        maxY = Math.pow(10d, maxTen); // upper power of ten
                    } else {
                        // use column meta's default range:
                        if (yMeta != null && yMeta.getDataRange() != null) {
                            final DataRange dataRange = yMeta.getDataRange();

                            if (!Double.isNaN(dataRange.getMin())) {
                                minY = Math.min(minY, dataRange.getMin());
                            }

                            if (!Double.isNaN(dataRange.getMax())) {
                                maxY = Math.max(maxY, dataRange.getMax());
                            }
                        }

                        final double marginY = (maxY - minY) * MARGIN_PERCENTS;
                        if (marginY > 0d) {
                            minY -= marginY;
                            maxY += marginY;
                        } else {
                            minY -= Math.abs(minY) * MARGIN_PERCENTS;
                            maxY += Math.abs(maxY) * MARGIN_PERCENTS;
                        }
                        if (maxY == minY) {
                            maxY = minY + 1d;
                        }
                    }
                    logger.debug("fixed rangeAxis: {} - {}", minY, maxY);

                    // update view range:
                    info.yAxisInfo.viewRange = new Range(minY, maxY);

                    // Update Y axis:
                    if (!yUseLog) {
                        this.xyPlotPlot2.setRangeAxis(ChartUtils.createAxis(""));
                    }
                    if (this.xyPlotPlot2.getRangeAxis() instanceof BoundedNumberAxis) {
                        axis = (BoundedNumberAxis) this.xyPlotPlot2.getRangeAxis();
                        axis.setBounds(new Range(minY, maxY));
                        axis.setRange(minY, maxY);
                    }

                    // update Y axis Label:
                    String label = "";
                    if (yMeta != null) {
                        label = yMeta.getName();
                        if (yUnit != null) {
                            label += " (" + yUnit + ")";
                        } else if (yMeta.getUnits() != Units.NO_UNIT) {
                            label += " (" + yMeta.getUnits().getStandardRepresentation() + ")";
                        }
                        this.xyPlotPlot2.getRangeAxis().setLabel(label);
                    }

                    if (yUseLog) {
                        final BoundedLogAxis logAxis = new BoundedLogAxis("log " + label);
                        logAxis.setExpTickLabelsFlag(true);
                        logAxis.setAutoRangeNextLogFlag(true);

                        logAxis.setBounds(new Range(minY, maxY));
                        logAxis.setRange(minY, maxY);

                        this.xyPlotPlot2.setRangeAxis(logAxis);
                    }
                    // tick color :
                    this.xyPlotPlot2.getRangeAxis().setTickMarkPaint(Color.BLACK);

                    // update plot's renderer before dataset (avoid notify events):
                    final FastXYErrorRenderer renderer = (FastXYErrorRenderer) this.xyPlotPlot2.getRenderer();

                    // enable/disable X error rendering (performance):
                    renderer.setDrawXError(info.xAxisInfo.hasDataErrorX);

                    // enable/disable Y error rendering (performance):
                    renderer.setDrawYError(info.yAxisInfo.hasDataErrorY);

                    // use deprecated method but defines shape once for ALL series (performance):
                    // define base shape as valid point (fallback):
                    renderer.setBaseShape(shapePointValid, false);

                    renderer.setLinesVisible(plotDef.isDrawLine());

                    // update plot's dataset at the end (notify events):
                    this.xyPlotPlot2.setDataset(dataset);
                }
            }
        }

        if (showPlot2) {
            this.combinedXYPlot.add(this.xyPlotPlot2, 1);

            final Integer plotIndex = (showPlot1) ? NumberUtils.valueOf(2) : NumberUtils.valueOf(1);
            this.plotMapping.put(this.xyPlotPlot2, plotIndex);
            this.plotIndexMapping.put(plotIndex, this.xyPlotPlot2);
        }

        if (!showPlot1 && !showPlot2) {
            return;
        }

        logger.debug("domainAxis: {} - {}", minX, maxX);

        // TODO: keep data info to help user define its own range

        // Add margin:
        if (xUseLog) {
            double minTen = Math.floor(Math.log10(minX));
            double maxTen = Math.ceil(Math.log10(maxX));

            if (maxTen == minTen) {
                minTen -= MARGIN_PERCENTS;
            }

            minX = Math.pow(10d, minTen); // lower power of ten
            maxX = Math.pow(10d, maxTen); // upper power of ten
        } else {
            boolean fixMin = false;
            boolean fixMax = false;

            // use column meta's default range:
            if (xMeta != null && xMeta.getDataRange() != null) {
                final DataRange dataRange = xMeta.getDataRange();

                if (!Double.isNaN(dataRange.getMin())) {
                    fixMin = true;
                    minX = Math.min(minX, dataRange.getMin());
                }

                if (!Double.isNaN(dataRange.getMax())) {
                    fixMax = true;
                    maxX = Math.max(maxX, dataRange.getMax());
                }
            }
            // use x-axis's include zero flag:
            if (plotDef.getXAxis().isIncludeZero()) {
                if (minX > 0d) {
                    fixMin = true;
                    minX = 0d;
                } else if (maxX < 0d) {
                    fixMax = true;
                    maxX = 0d;
                }
            }

            // adjust x-axis margins:
            final double marginX = (maxX - minX) * MARGIN_PERCENTS;
            if (marginX > 0d) {
                if (!fixMin) {
                    minX -= marginX;
                }
                if (!fixMax) {
                    maxX += marginX;
                }
            } else {
                minX -= Math.abs(minX) * MARGIN_PERCENTS;
                maxX += Math.abs(maxX) * MARGIN_PERCENTS;
            }
            if (maxX == minX) {
                maxX = minX + 1d;
            }
        }
        logger.debug("fixed domainAxis: {} - {}", minX, maxX);

        // update view range:
        for (PlotInfo info : getPlotInfos()) {
            info.xAxisInfo.viewRange = new Range(minX, maxX);
        }

        if (!xUseLog) {
            this.combinedXYPlot.setDomainAxis(ChartUtils.createAxis(""));
        }
        if (this.combinedXYPlot.getDomainAxis() instanceof BoundedNumberAxis) {
            axis = (BoundedNumberAxis) this.combinedXYPlot.getDomainAxis();
            axis.setBounds(new Range(minX, maxX));
            axis.setRange(minX, maxX);
        }

        // update X axis Label:
        String label = "";
        if (xMeta != null) {
            label = xMeta.getName();
            if (xUnit != null) {
                label += " (" + xUnit + ")";
            } else if (xMeta.getUnits() != Units.NO_UNIT) {
                label += " (" + xMeta.getUnits().getStandardRepresentation() + ")";
            }
            this.combinedXYPlot.getDomainAxis().setLabel(label);
        }

        if (xUseLog) {
            final BoundedLogAxis logAxis = new BoundedLogAxis("log " + label);
            logAxis.setExpTickLabelsFlag(true);
            logAxis.setAutoRangeNextLogFlag(true);

            logger.debug("logAxis domain: [{} - {}]", minX, maxX);

            logAxis.setBounds(new Range(minX, maxX));
            logAxis.setRange(minX, maxX);

            this.combinedXYPlot.setDomainAxis(logAxis);
        }
        // tick color :
        this.combinedXYPlot.getDomainAxis().setTickMarkPaint(Color.BLACK);

        // define custom legend:
        final ColorPalette palette = ColorPalette.getDefaultColorPaletteAlpha();

        LegendItemCollection legendCollection = new LegendItemCollection();

        if (ColorMapping.STATION_INDEX == plotDef.getColorMapping()) {
            // merge all used staIndex names:
            final Set<String> distinctUsedStaIndexNames = new LinkedHashSet<String>();

            for (PlotInfo info : getPlotInfos()) {
                distinctUsedStaIndexNames.addAll(info.usedStaIndexNames);
            }

            int n = 0;
            for (String staIndexName : distinctUsedStaIndexNames) {
                legendCollection.add(ChartUtils.createLegendItem(staIndexName, palette.getColor(n)));

                n++;
            }
        } else if (ColorMapping.CONFIGURATION == plotDef.getColorMapping()) {

            // merge all used staConf names:
            final Set<String> distinctUsedStaConfNames = new LinkedHashSet<String>();

            for (PlotInfo info : getPlotInfos()) {
                distinctUsedStaConfNames.addAll(info.usedStaConfNames);
            }

            int n = 0;
            for (String staConfName : distinctUsedStaConfNames) {
                legendCollection.add(ChartUtils.createLegendItem(staConfName, palette.getColor(n)));

                n++;
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

        this.combinedXYPlot.setFixedLegendItems(legendCollection);
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
            logger.debug("xMeta:{}", yMeta);
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


        // Color mapping:
        final ColorPalette palette = ColorPalette.getDefaultColorPaletteAlpha();

        // Station configurations:
        // Use staConf (configuration) on each data row ?
        if (isLogDebug) {
            logger.debug("useStaConfColors: {}", (colorMapping == ColorMapping.CONFIGURATION));
        }

        final Set<String> usedStaConfNames = info.usedStaConfNames;
        final Map<short[], Color> mappingStaConfs;

        if (colorMapping == ColorMapping.CONFIGURATION) {
            mappingStaConfs = new IdentityHashMap<short[], Color>(oiData.getDistinctStaConfCount());

            String staConfName;
            for (short[] staConf : oiData.getDistinctStaConf()) {
                staConfName = oiData.getStaNames(staConf); // cached

                // find index in distinctStaConfNames:
                int pos = info.distinctStaConfNames.indexOf(staConfName);

                if (pos != -1) {
                    // note: color mapping is defined for all possible station configuration:
                    mappingStaConfs.put(staConf, palette.getColor(pos));
                } else {
                    throw new IllegalStateException("bad case");
                }
            }
        } else {
            mappingStaConfs = null;
        }

        if (isLogDebug) {
            logger.debug("useStaIndexColors: {}", (colorMapping == ColorMapping.STATION_INDEX));
        }

        final Set<String> usedStaIndexNames = info.usedStaIndexNames;
        final Map<short[], Color> mappingStaIndexes;

        if (colorMapping == ColorMapping.STATION_INDEX) {
            mappingStaIndexes = new IdentityHashMap<short[], Color>(oiData.getDistinctStaIndexCount());

            String staIndexName;
            for (short[] staIndex : oiData.getDistinctStaIndex()) {
                staIndexName = oiData.getStaNames(staIndex); // cached

                // find index in distinctStaIndexNames:
                int pos = info.distinctStaIndexNames.indexOf(staIndexName);

                if (pos != -1) {
                    // note: color mapping is defined for all possible station configuration:
                    mappingStaIndexes.put(staIndex, palette.getColor(pos));
                } else {
                    throw new IllegalStateException("bad case");
                }
            }
        } else {
            mappingStaIndexes = null;
        }


        // TODO: use an XYZ dataset to have a color axis (z) and then use linear or custom z conversion to colors.

        if (isLogDebug) {
            logger.debug("useWaveLengthColors: {}", (colorMapping == ColorMapping.WAVELENGTH_RANGE));
        }

        final Color[] mappingWaveLengthColors;

        if (colorMapping == ColorMapping.WAVELENGTH_RANGE) {
            final float[] effWaveRange = oiData.getEffWaveRange();

            // scale and offset between [0;1]:
            final float scale = (float) ((effWaveRange[1] - effWaveRange[0]) / info.waveLengthRange.getLength());
            final float offset = (float) ((effWaveRange[0] - info.waveLengthRange.getLowerBound()) / info.waveLengthRange.getLength());

            final IndexColorModel colorModel = ColorModels.getColorModel(ColorModels.COLOR_MODEL_RAINBOW_ALPHA);
            final int iMaxColor = colorModel.getMapSize() - 1;

            mappingWaveLengthColors = new Color[nWaves];

            final float factor = (nWaves > 1) ? scale / (nWaves - 1) : scale;
            float value;

            final float alpha = 0.8f;
            final int alphaMask = Math.round(255 * alpha) << 24;

            for (int i = 0; i < nWaves; i++) {
                // invert palette to have (VIOLET - BLUE - GREEN - RED) ie color spectrum:
                value = (float) iMaxColor - (factor * i + offset) * iMaxColor;

                mappingWaveLengthColors[i] = new Color(ImageUtils.getRGB(colorModel, iMaxColor, value, alphaMask), true);
            }
        } else {
            mappingWaveLengthColors = null;
        }

        // TODO: adjust renderer settings per Serie (color, shape ...) per series and item at higher level using dataset fields
        final FastXYErrorRenderer renderer = (FastXYErrorRenderer) plot.getRenderer();

        // try to fill dataset:

        // avoid loop on wavelength if no 2D data:
        final int nWaveChannels = (isXData2D || isYData2D) ? nWaves : 1;

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
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        double[] xValue, xLower, xUpper, yValue, yLower, yUpper;
        Shape[] itemShapes;

        boolean recycleArray = false;
        double[][] arrayPool = new double[6][];
        Shape[] shapePool = null;

        double x, xErr, y, yErr;

        OITableSerieKey serieKey;

        short[] currentStaIndex;
        short[] currentStaConf;

        int nSkipTarget = 0;
        int nSkipFlag = 0;
        boolean isFlag, isXErrValid, isYErrValid;

        int nData = 0;

        // TODO: unroll loops (wave / baseline) ... and avoid repeated checks on rows (targetId, baseline ...)

        Color serieColor;

        // fast access to NaN value:
        final double NaN = Double.NaN;

        // Iterate on wave channels (j):
        for (int i, j = 0, k, idx; j < nWaveChannels; j++) {

            // Iterate on baselines (k):
            for (k = 0; k < nStaIndexes; k++) {

                // get the sta index array:
                currentStaIndex = distinctStaIndexes[k];
                currentStaConf = null;

                // 1 serie per baseline and per spectral channel:
                if (recycleArray) {
                    recycleArray = false;
                    xValue = arrayPool[0];
                    xLower = arrayPool[1];
                    xUpper = arrayPool[2];
                    yValue = arrayPool[3];
                    yLower = arrayPool[4];
                    yUpper = arrayPool[5];
                    itemShapes = shapePool;
                } else {
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
                            // data row does not coorespond to current baseline so skip it:
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

                    // Process Y value:
                    y = (isYData2D) ? yData2D[i][j] : yData1D[i];

                    if (yUseLog && y < 0d) {
                        // keep only positive data:
                        y = NaN;
                    }

                    if (!Double.isNaN(y)) {
                        // convert y value:
                        if (doConvertY) {
                            y = initialXConverter.evaluate(y);
                        }
                        if (doScaleY) {
                            y = yConverter.evaluate(y);
                        }

                        // Process X value:
                        x = (isXData2D) ? xData2D[i][j] : xData1D[i];

                        if (xUseLog && x < 0d) {
                            // keep only positive data:
                            x = NaN;
                        }

                        if (!Double.isNaN(x)) {
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

                            isXErrValid = isYErrValid = true;

                            // Define Y data:
                            if (Double.isNaN(yErr)) {
                                yValue[idx] = y;
                                yLower[idx] = NaN;
                                yUpper[idx] = NaN;

                                // update Y boundaries:
                                if (y < minY) {
                                    minY = y;
                                }
                                if (y > maxY) {
                                    maxY = y;
                                }
                            } else {
                                hasDataErrorY = true;

                                // ensure error is valid ie positive:
                                if (yErr >= 0d) {
                                    // convert yErr value:
                                    if (doConvertY) {
                                        yErr = initialXConverter.evaluate(yErr);
                                    }
                                    if (doScaleY) {
                                        yErr = yConverter.evaluate(yErr);
                                    }
                                } else {
                                    yErr = Double.POSITIVE_INFINITY;
                                    isYErrValid = false;
                                }

                                // useLog: check if y - err < 0:
                                yValue[idx] = y;
                                yLower[idx] = (yUseLog && (y - yErr) < 0d) ? Double.MIN_VALUE : (y - yErr);
                                yUpper[idx] = y + yErr;

                                if (isYErrValid) {
                                    // update Y boundaries including error:
                                    if (yLower[idx] < minY) {
                                        minY = yLower[idx];
                                    }
                                    if (yUpper[idx] > maxY) {
                                        maxY = yUpper[idx];
                                    }
                                } else {
                                    if (y < minY) {
                                        minY = y;
                                    }
                                    if (y > maxY) {
                                        maxY = y;
                                    }
                                }
                            }

                            // Define X data:
                            if (Double.isNaN(xErr)) {
                                xValue[idx] = x;
                                xLower[idx] = NaN;
                                xUpper[idx] = NaN;

                                // update X boundaries:
                                if (x < minX) {
                                    minX = x;
                                }
                                if (x > maxX) {
                                    maxX = x;
                                }
                            } else {
                                hasDataErrorX = true;

                                // ensure error is valid ie positive:
                                if (xErr >= 0d) {
                                    // convert xErr value:
                                    if (doConvertX) {
                                        xErr = initialXConverter.evaluate(xErr);
                                    }
                                    if (doScaleX) {
                                        xErr = xConverter.evaluate(xErr);
                                    }
                                } else {
                                    xErr = Double.POSITIVE_INFINITY;
                                    isXErrValid = false;
                                }

                                xValue[idx] = x;
                                xLower[idx] = (xUseLog && (x - xErr) < 0d) ? Double.MIN_VALUE : (x - xErr);
                                xUpper[idx] = x + xErr;

                                if (isXErrValid) {
                                    // update X boundaries including error:
                                    if (xLower[idx] < minX) {
                                        minX = xLower[idx];
                                    }
                                    if (xUpper[idx] > maxX) {
                                        maxX = xUpper[idx];
                                    }
                                } else {
                                    if (x < minX) {
                                        minX = x;
                                    }
                                    if (x > maxX) {
                                        maxX = x;
                                    }
                                }
                            }


                            // TODO: adjust renderer settings per Serie (color, shape, shape size, outline ....) !
                            // ~ new custom axis (color, size, shape) 

                            // Define item shape:
                            // invalid shape if flagged or invalid error value
                            itemShapes[idx] = getPointShape(isYErrValid && isXErrValid && !isFlag);

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
                        arrayPool[0] = xValue;
                        arrayPool[1] = xLower;
                        arrayPool[2] = xUpper;
                        arrayPool[3] = yValue;
                        arrayPool[4] = yLower;
                        arrayPool[5] = yUpper;
                        shapePool = itemShapes;

                        xValue = extract(xValue, idx);
                        xLower = extract(xLower, idx);
                        xUpper = extract(xUpper, idx);
                        yValue = extract(yValue, idx);
                        yLower = extract(yLower, idx);
                        yUpper = extract(yUpper, idx);
                        itemShapes = extract(itemShapes, idx);
                    }

                    // TODO: add oiTable, i (row), j (nWave) in dataset:
                    serieKey = new OITableSerieKey(tableIndex, k, j);

                    // Avoid any key conflict:
                    dataset.addSeries(serieKey, new double[][]{xValue, xLower, xUpper, yValue, yLower, yUpper});

                    // TODO: adjust renderer settings per Serie (color, shape ...) per series and item at higher level using dataset fields

                    // Use special fields into dataset to encode color mapping (color value as double ?)

                    // use colormapping enum:
                    switch (colorMapping) {
                        case WAVELENGTH_RANGE:
                        // wavelength is default:
                        case OBSERVATION_DATE:
                        // not implemented still
                        default:
                            serieColor = mappingWaveLengthColors[j];
                            break;
                        case CONFIGURATION:
                            serieColor = mappingStaConfs.get(currentStaConf);
                            break;
                        case STATION_INDEX:
                            serieColor = mappingStaIndexes.get(currentStaIndex);
                            break;
                    }
                    renderer.setSeriesPaint(seriesCount, serieColor, false);

                    // define shape per item in serie:
                    renderer.setItemShapes(seriesCount, itemShapes);

                    // increase series count:
                    seriesCount++;

                    // Add staIndex into used station indexes anyway:
                    if (currentStaIndex != null) {
                        usedStaIndexNames.add(oiData.getStaNames(currentStaIndex)); // cached
                    }

                    // Add staConf into used station configurations anyway:
                    if (currentStaConf != null) {
                        usedStaConfNames.add(oiData.getStaNames(currentStaConf)); // cached
                    }
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

            logger.debug("nSeries {} vs {}", seriesCount, dataset.getSeriesCount());
        }

        // update plot information (should be consistent between calls):
        info.hasPlotData = true;
        info.nData += nData;
        info.hasDataFlag |= hasDataFlag; // logical OR
        info.yAxisIndex = yAxisIndex;
        // add given table:
        info.oidataList.add(oiData);

        AxisInfo axisInfo = info.xAxisInfo;
        if (axisInfo.dataRange != null) {
            // combine X range:
            minX = Math.min(minX, axisInfo.dataRange.getLowerBound());
            maxX = Math.max(maxX, axisInfo.dataRange.getUpperBound());
        }
        axisInfo.dataRange = new Range(minX, maxX);
        axisInfo.columnMeta = xMeta;
        axisInfo.unit = (doScaleX) ? xConverter.getUnit() : null;
        axisInfo.useLog = xUseLog;
        axisInfo.hasDataErrorX |= hasDataErrorX; // logical OR

        axisInfo = info.yAxisInfo;
        if (axisInfo.dataRange != null) {
            // combine Y range:
            minY = Math.min(minY, axisInfo.dataRange.getLowerBound());
            maxY = Math.max(maxY, axisInfo.dataRange.getUpperBound());
        }
        axisInfo.dataRange = new Range(minY, maxY);
        axisInfo.columnMeta = yMeta;
        axisInfo.unit = (doScaleY) ? yConverter.getUnit() : null;
        axisInfo.useLog = yUseLog;
        axisInfo.hasDataErrorY |= hasDataErrorY; // logical OR
    }

    private double[] extract(final double[] input, final int len) {
        final double[] output = new double[len];
        if (USE_SYSTEM_ARRAY_COPY) {
            System.arraycopy(input, 0, output, 0, len);
        } else {
            // manual array copy is faster on recent machine (64bits / hotspot server compiler)
            for (int i = 0; i < len; i++) {
                output[i] = input[i];
            }
        }
        return output;
    }

    private Shape[] extract(final Shape[] input, final int len) {
        final Shape[] output = new Shape[len];
        if (USE_SYSTEM_ARRAY_COPY) {
            System.arraycopy(input, 0, output, 0, len);
        } else {
            // manual array copy is faster on recent machine (64bits / hotspot server compiler)
            for (int i = 0; i < len; i++) {
                output[i] = input[i];
            }
        }
        return output;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabelNoData;
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

        // DEBUG (TODO KILL ASAP):
        if (true) {
            switch (event.getType()) {
                case ChartProgressEvent.DRAWING_STARTED:
                    this.chartDrawStartTime = System.nanoTime();
                    break;
                case ChartProgressEvent.DRAWING_FINISHED:
                    logger.warn("Drawing chart time[{}] = {} ms.", getTargetName(), 1e-6d * (System.nanoTime() - this.chartDrawStartTime));
                    this.chartDrawStartTime = 0l;
                    break;
                default:
            }
        }

        // Perform custom operations before/after chart rendering:
        // move JMMC annotations:
        if (this.xyPlotPlot1.getDomainAxis() != null) {
            this.aJMMCPlot1.setX(this.xyPlotPlot1.getDomainAxis().getUpperBound());
            this.aJMMCPlot1.setY(this.xyPlotPlot1.getRangeAxis().getLowerBound());
        }
        if (this.xyPlotPlot2.getDomainAxis() != null) {
            this.aJMMCPlot2.setX(this.xyPlotPlot2.getDomainAxis().getUpperBound());
            this.aJMMCPlot2.setY(this.xyPlotPlot2.getRangeAxis().getLowerBound());
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
     * Return the unique staNames values from given OIData tables
     * @param oiDataList OIData tables
     * @param set set instance to use
     * @return given set instance
     */
    private static Set<String> getDistinctStaNames(final List<OIData> oiDataList, final Set<String> set) {
        String staNames;
        for (OIData oiData : oiDataList) {
            for (short[] staIndexes : oiData.getDistinctStaIndex()) {
                staNames = oiData.getStaNames(staIndexes);

                logger.debug("staNames : {}", staNames);

                set.add(staNames);
            }
        }
        return set;
    }

    /**
     * Return the unique staConfs values from given OIData tables
     * @param oiDataList OIData tables
     * @param set set instance to use
     * @return given set instance
     */
    private static Set<String> getDistinctStaConfs(final List<OIData> oiDataList, final Set<String> set) {
        String staNames;
        for (OIData oiData : oiDataList) {
            for (short[] staConf : oiData.getDistinctStaConf()) {
                staNames = oiData.getStaNames(staConf);

                logger.debug("staConf : {}", staNames);

                set.add(staNames);
            }
        }
        return set;
    }

    /**
     * Return the unique wave length ranges from given OIData tables
     * @param oiDataList OIData tables
     * @param set set instance to use
     */
    private static void getDistinctWaveLengthRange(final List<OIData> oiDataList, final Set<String> set) {
        final StringBuilder sb = new StringBuilder();

        String wlenRange;
        float[] effWaveRange;
        for (OIData oiData : oiDataList) {
            effWaveRange = oiData.getEffWaveRange();

            if (effWaveRange != null) {
                sb.append("[").append(df4.format(1e6f * effWaveRange[0])).append(" ").append(SpecialChars.UNIT_MICRO_METER);
                sb.append(" - ").append(df4.format(1e6f * effWaveRange[1])).append(" ").append(SpecialChars.UNIT_MICRO_METER).append("]");

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

    /**
     * Plot information used during data processing
     */
    private static class PlotInfo {

        /** flag indicating that this table has data to plot */
        boolean hasPlotData = false;
        /** total number of data points */
        int nData = 0;
        /** flag indicating that the dataset contains flagged data */
        boolean hasDataFlag = false;
        /** y axis index in plot definition */
        int yAxisIndex = -1;
        /** list of OIData tables used */
        List<OIData> oidataList;
        /** all distinct station indexes from OIData tables (not filtered) */
        List<String> distinctStaIndexNames;
        /** used distinct station indexes in the plot (filtered) */
        Set<String> usedStaIndexNames;
        /** all distinct station configuration from OIData tables (not filtered) */
        List<String> distinctStaConfNames;
        /** used distinct station configuration in the plot (filtered) */
        Set<String> usedStaConfNames;
        /** largest wave length range (not filtered) */
        Range waveLengthRange = null;
        /** x axis information */
        AxisInfo xAxisInfo;
        /** y axis information */
        AxisInfo yAxisInfo;

        PlotInfo() {
            oidataList = new ArrayList<OIData>();
            usedStaIndexNames = new LinkedHashSet<String>();
            usedStaConfNames = new LinkedHashSet<String>();
            xAxisInfo = new AxisInfo();
            yAxisInfo = new AxisInfo();
        }
    }

    private static class AxisInfo {

        /** colum meta data */
        ColumnMeta columnMeta = null;
        /** data range */
        Range dataRange = null;
        /** view range (with margin) */
        Range viewRange = null;
        /** converter unit */
        String unit = null;
        /** is log axis */
        boolean useLog = false;
        /** flag indicating that the dataset has data with error on X axis */
        boolean hasDataErrorX = false;
        /** flag indicating that the dataset has data with error on Y axis */
        boolean hasDataErrorY = false;
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
        return ConverterFactory.CONVERTER_MEGA_LAMBDA.equals(xAxis.getConverter())
                && ConverterFactory.CONVERTER_MEGA_LAMBDA.equals(yAxis.getConverter());
    }
}
