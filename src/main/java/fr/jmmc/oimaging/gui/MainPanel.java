/**
 * *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 *****************************************************************************
 */
package fr.jmmc.oimaging.gui;

import fr.jmmc.jmcs.App;
import fr.jmmc.jmcs.Bootstrapper;
import fr.jmmc.jmcs.gui.action.ActionRegistrar;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.task.TaskSwingWorkerExecutor;
import fr.jmmc.jmcs.gui.util.FieldSliderAdapter;
import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.oimaging.gui.action.DeleteSelectionAction;
import fr.jmmc.oimaging.gui.action.ExportFitsImageAction;
import fr.jmmc.oimaging.gui.action.ExportOIFitsAction;
import fr.jmmc.oimaging.gui.action.LoadOIFitsAction;
import fr.jmmc.oimaging.gui.action.RunAction;
import fr.jmmc.oimaging.interop.SendFitsAction;
import fr.jmmc.oimaging.interop.SendOIFitsAction;
import fr.jmmc.oimaging.model.IRModel;
import fr.jmmc.oimaging.model.IRModelEvent;
import fr.jmmc.oimaging.model.IRModelEventListener;
import fr.jmmc.oimaging.model.IRModelEventType;
import fr.jmmc.oimaging.model.IRModelManager;
import fr.jmmc.oimaging.services.ServiceResult;
import fr.jmmc.oitools.image.FitsUnit;
import fr.jmmc.oitools.image.ImageOiConstants;
import fr.jmmc.oitools.image.ImageOiInputParam;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.range.Range;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main container of OIFits Explorer App
 *
 * @author mella
 */
public class MainPanel extends javax.swing.JPanel implements IRModelEventListener, ListSelectionListener {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(MainPanel.class);

    /** micrometres to meter */
    public final static double MICRO_METER = 1e-6;

    /**
     * default serial UID for Serializable interface
     */
    private static final long serialVersionUID = 1;
    /** default mouse cursor refresh period = 100 ms */
    private static final int REFRESH_PERIOD = 100;

    /** Enum used for indexes of tabs.
     * Caution: Must be in sync with the order in which the tabs are added in jTabbedPaneTwoTabsDisplay.
     */
    private static enum TABS {
        INPUT, RESULTS
    };

    /* members */
 /* actions */
    private DeleteSelectionAction deleteSelectionAction;
    private RunAction runAction;
    private Action exportOiFitsAction;
    private Action sendOiFitsAction;
    private Action exportFitsImageAction;
    private Action sendFitsAction;

    /** Flag set to true while the GUI is being updated by model else false. */
    private boolean syncingUI = false;

    private FieldSliderAdapter fieldSliderAdapterWaveMin;
    private FieldSliderAdapter fieldSliderAdapterWaveMax;
    /** timeline refresh Swing timer */
    private final Timer timerMouseCursorRefresh;
    private IRModel currentModel;

    private JSlider jSliderResults;
    /** last slider index */
    private int sliderResultLastIndex = -1;

    /**
     * Creates new form MainPanel
     */
    public MainPanel() {

        // Build GUI
        initComponents();

        // Create the timeline refresh timer:
        this.timerMouseCursorRefresh = new Timer(REFRESH_PERIOD, new ActionListener() {
            /**
             * Invoked when the timer action occurs.
             */
            @Override
            public void actionPerformed(final ActionEvent ae) {
                final JFrame appFrame = App.getFrame();
                final Cursor currentCursor = appFrame.getCursor();

                final Cursor newCursor = (TaskSwingWorkerExecutor.isTaskRunning()) ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                        : Cursor.getDefaultCursor();

                if (newCursor != currentCursor) {
                    appFrame.setCursor(newCursor);
                }
            }
        });

        // Finish init
        postInit();

        // anyway enable mouse cursor timer:
        enableMouseCursorRefreshTimer(true);
    }

    /**
     * Start/Stop the internal mouse cursor Refresh timer
     * @param enable true to enable it, false otherwise
     */
    private void enableMouseCursorRefreshTimer(final boolean enable) {
        if (enable) {
            if (!this.timerMouseCursorRefresh.isRunning()) {
                logger.debug("Starting timer: {}", this.timerMouseCursorRefresh);

                this.timerMouseCursorRefresh.start();
            }
        } else if (this.timerMouseCursorRefresh.isRunning()) {
            logger.debug("Stopping timer: {}", this.timerMouseCursorRefresh);

            this.timerMouseCursorRefresh.stop();
        }
    }

    /**
     * This method is useful to set the models and specific features of initialized swing components :
     */
    private void postInit() {

        registerActions();

        IRModelManager.getInstance().bindIRModelChangedEvent(this);

        // associate sliders and fields
        fieldSliderAdapterWaveMin = new FieldSliderAdapter(jSliderWaveMin, jFormattedTextFieldWaveMin, 0, 1, 0);
        fieldSliderAdapterWaveMax = new FieldSliderAdapter(jSliderWaveMax, jFormattedTextFieldWaveMax, 0, 1, 0);

        // become widget listener
        jTablePanel.getSelectionModel().addListSelectionListener(this);

        // init viewer Panel
        viewerPanelInput.displayModel(null);

        // create image slider:
        jSliderResults = new JSlider();
        jSliderResults.setMinimum(-1);
        jSliderResults.setMaximum(-1);
        jSliderResults.setValue(-1);
        jSliderResults.addChangeListener(new javax.swing.event.ChangeListener() {
            @Override
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderResultsStateChanged(evt);
            }
        });

        jTablePanel.addControlComponent(jButtonCompare);
        jTablePanel.addControlComponent(new JButton(deleteSelectionAction));
        jTablePanel.addControlComponent(jSliderResults);

        // to ensure jsplit pane will be given 90% once it becomes visible:
        showTablePanel(false);

        jSplitPaneInput.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {

            @Override
            public void propertyChange(final PropertyChangeEvent changeEvent) {
                final Integer last = (Integer) changeEvent.getNewValue();
                final Integer priorLast = (Integer) changeEvent.getOldValue();

                if (last < priorLast) {
                    // restore free space ie avoid having the left panel too large:
                    jScrollPaneInputForm.revalidate();
                    jScrollPaneInputForm.repaint();
                }
            }
        });

        jTabbedPaneTwoTabsDisplay.setTabComponentAt(TABS.INPUT.ordinal(), jLabelTabInput);
        jTabbedPaneTwoTabsDisplay.setTabComponentAt(TABS.RESULTS.ordinal(), jLabelTabResults);

        viewerPanelInput.setMainPanel(this);
        viewerPanelResults.setMainPanel(this);
        
        viewerPanelInput.setTabMode(ViewerPanel.SHOW_MODE.MODEL);
        viewerPanelInput.setShowMode(ViewerPanel.SHOW_MODE.MODEL);
        viewerPanelResults.setTabMode(ViewerPanel.SHOW_MODE.RESULT);
        viewerPanelResults.setShowMode(ViewerPanel.SHOW_MODE.RESULT);
    }

    /**
     * Create the main actions
     */
    private void registerActions() {
        // Map actions to widgets

        jButtonLoadData.setAction(ActionRegistrar.getInstance().get(LoadOIFitsAction.className, LoadOIFitsAction.actionName));
        jButtonLoadData.setHideActionText(true);

        deleteSelectionAction = (DeleteSelectionAction) ActionRegistrar.getInstance().get(DeleteSelectionAction.className, DeleteSelectionAction.actionName);
        runAction = (RunAction) ActionRegistrar.getInstance().get(RunAction.className, RunAction.actionName);
        jButtonRun.setAction(runAction);

        exportOiFitsAction = ActionRegistrar.getInstance().get(ExportOIFitsAction.className, ExportOIFitsAction.actionName);
        jButtonExportOIFits.setAction(exportOiFitsAction);
        
        sendOiFitsAction = ActionRegistrar.getInstance().get(SendOIFitsAction.className, SendOIFitsAction.actionName);
        exportFitsImageAction = ActionRegistrar.getInstance().get(ExportFitsImageAction.className, ExportFitsImageAction.actionName);
        sendFitsAction = ActionRegistrar.getInstance().get(SendFitsAction.className, SendFitsAction.actionName);
    }

    @Override
    public void dispose() {
        if (logger.isDebugEnabled()) {
            logger.debug("dispose: {}", ObjectUtils.getObjectInfo(this));
        }

        // TODO release child resource if any
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jButtonCompare = new javax.swing.JButton();
        jLabelTabInput = new javax.swing.JLabel();
        jLabelTabResults = new javax.swing.JLabel();
        jTabbedPaneTwoTabsDisplay = new javax.swing.JTabbedPane();
        jPanelTabInput = new javax.swing.JPanel();
        jSplitPaneInput = new javax.swing.JSplitPane();
        jScrollPaneInputForm = new javax.swing.JScrollPane();
        jPanelInputForm = new javax.swing.JPanel();
        jPanelDataSelection = new javax.swing.JPanel();
        jCheckBoxUseVis = new javax.swing.JCheckBox();
        jCheckBoxUseVis2 = new javax.swing.JCheckBox();
        jCheckBoxUseT3 = new javax.swing.JCheckBox();
        jPanelTarget = new javax.swing.JPanel();
        jComboBoxTarget = new javax.swing.JComboBox();
        jLabelTarget = new javax.swing.JLabel();
        jButtonLoadData = new javax.swing.JButton();
        jPanelWL = new javax.swing.JPanel();
        jLabelWaveMin = new javax.swing.JLabel();
        jLabelWaveMax = new javax.swing.JLabel();
        jSliderWaveMin = new javax.swing.JSlider();
        jSliderWaveMax = new javax.swing.JSlider();
        jFormattedTextFieldWaveMin = new javax.swing.JFormattedTextField();
        jFormattedTextFieldWaveMax = new javax.swing.JFormattedTextField();
        jLabelOifitsFile = new javax.swing.JLabel();
        softwareSettingsPanel = new fr.jmmc.oimaging.gui.SoftwareSettingsPanel();
        jPanelExecutionLog = new javax.swing.JPanel();
        jButtonRun = new javax.swing.JButton();
        jButtonExportOIFits = new javax.swing.JButton();
        jScrollPaneEditor = new javax.swing.JScrollPane();
        jEditorPane = new javax.swing.JEditorPane();
        viewerPanelInput = new fr.jmmc.oimaging.gui.ViewerPanel();
        jPanelTabResults = new javax.swing.JPanel();
        jSplitPaneResults = new javax.swing.JSplitPane();
        viewerPanelResults = new fr.jmmc.oimaging.gui.ViewerPanel();
        jTablePanel = new fr.jmmc.oimaging.gui.ResultSetTablePanel();

        jButtonCompare.setText("Compare");
        jButtonCompare.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCompareActionPerformed(evt);
            }
        });

        jLabelTabInput.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelTabInput.setText("Input");
        jLabelTabInput.setPreferredSize(new java.awt.Dimension(200, 40));

        jLabelTabResults.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelTabResults.setText("Results");
        jLabelTabResults.setPreferredSize(new java.awt.Dimension(200, 40));

        setLayout(new java.awt.BorderLayout());

        jTabbedPaneTwoTabsDisplay.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPaneTwoTabsDisplayStateChanged(evt);
            }
        });

        jPanelTabInput.setLayout(new java.awt.BorderLayout());

        jSplitPaneInput.setResizeWeight(0.3);
        jSplitPaneInput.setMinimumSize(new java.awt.Dimension(900, 600));

        jScrollPaneInputForm.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        jPanelInputForm.setLayout(new java.awt.GridBagLayout());

        jPanelDataSelection.setBorder(javax.swing.BorderFactory.createTitledBorder("Data selection"));
        jPanelDataSelection.setLayout(new java.awt.GridBagLayout());

        jCheckBoxUseVis.setText("VIS");
        jCheckBoxUseVis.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_USE_VIS));
        jCheckBoxUseVis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.weightx = 0.1;
        jPanelDataSelection.add(jCheckBoxUseVis, gridBagConstraints);

        jCheckBoxUseVis2.setText("VIS2");
        jCheckBoxUseVis2.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_USE_VIS2));
        jCheckBoxUseVis2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.weightx = 0.1;
        jPanelDataSelection.add(jCheckBoxUseVis2, gridBagConstraints);

        jCheckBoxUseT3.setText("T3");
        jCheckBoxUseT3.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_USE_T3));
        jCheckBoxUseT3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.weightx = 0.1;
        jPanelDataSelection.add(jCheckBoxUseT3, gridBagConstraints);

        jPanelTarget.setLayout(new java.awt.GridBagLayout());

        jComboBoxTarget.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "T" }));
        jComboBoxTarget.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_TARGET));
        jComboBoxTarget.setPrototypeDisplayValue("XXXX");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelTarget.add(jComboBoxTarget, gridBagConstraints);

        jLabelTarget.setText("TARGET");
        jLabelTarget.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_TARGET));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelTarget.add(jLabelTarget, gridBagConstraints);

        jButtonLoadData.setText("L");
        jButtonLoadData.setName("jButtonLoadData"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelTarget.add(jButtonLoadData, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanelDataSelection.add(jPanelTarget, gridBagConstraints);

        jPanelWL.setLayout(new java.awt.GridBagLayout());

        jLabelWaveMin.setText("WAVE_MIN");
        jLabelWaveMin.setToolTipText("<html>" + getTooltip(ImageOiConstants.KEYWORD_WAVE_MIN) + "<br/><b>Editor unit is '" + FitsUnit.WAVELENGTH_MICRO_METER.getRepresentation() + "'</b></html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelWL.add(jLabelWaveMin, gridBagConstraints);

        jLabelWaveMax.setText("WAVE_MAX");
        jLabelWaveMax.setToolTipText("<html>" + getTooltip(ImageOiConstants.KEYWORD_WAVE_MAX) + "<br/><b>Editor unit is '" + FitsUnit.WAVELENGTH_MICRO_METER.getRepresentation() + "'</b></html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelWL.add(jLabelWaveMax, gridBagConstraints);

        jSliderWaveMin.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_WAVE_MIN));
        jSliderWaveMin.setPreferredSize(new java.awt.Dimension(30, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        jPanelWL.add(jSliderWaveMin, gridBagConstraints);

        jSliderWaveMax.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_WAVE_MAX));
        jSliderWaveMax.setPreferredSize(new java.awt.Dimension(30, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        jPanelWL.add(jSliderWaveMax, gridBagConstraints);

        jFormattedTextFieldWaveMin.setColumns(2);
        jFormattedTextFieldWaveMin.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00###"))));
        jFormattedTextFieldWaveMin.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_WAVE_MIN));
        jFormattedTextFieldWaveMin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextFieldActionPerformed(evt);
            }
        });
        jFormattedTextFieldWaveMin.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jFormattedTextFieldPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelWL.add(jFormattedTextFieldWaveMin, gridBagConstraints);

        jFormattedTextFieldWaveMax.setColumns(2);
        jFormattedTextFieldWaveMax.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00###"))));
        jFormattedTextFieldWaveMax.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_WAVE_MAX));
        jFormattedTextFieldWaveMax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextFieldActionPerformed(evt);
            }
        });
        jFormattedTextFieldWaveMax.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jFormattedTextFieldPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelWL.add(jFormattedTextFieldWaveMax, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanelDataSelection.add(jPanelWL, gridBagConstraints);

        jLabelOifitsFile.setText("OIFITS Label");
        jLabelOifitsFile.setPreferredSize(new java.awt.Dimension(50, 40));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        jPanelDataSelection.add(jLabelOifitsFile, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        jPanelInputForm.add(jPanelDataSelection, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanelInputForm.add(softwareSettingsPanel, gridBagConstraints);

        jPanelExecutionLog.setBorder(javax.swing.BorderFactory.createTitledBorder("Action panel"));
        jPanelExecutionLog.setLayout(new java.awt.GridBagLayout());

        jButtonRun.setText("[Run]");
        jButtonRun.setName("jButtonRun"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelExecutionLog.add(jButtonRun, gridBagConstraints);

        jButtonExportOIFits.setText("[Save]");
        jButtonExportOIFits.setName("jButtonExportOIFits"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelExecutionLog.add(jButtonExportOIFits, gridBagConstraints);

        jScrollPaneEditor.setMinimumSize(new java.awt.Dimension(100, 100));
        jScrollPaneEditor.setPreferredSize(new java.awt.Dimension(100, 100));

        jEditorPane.setContentType("text/html"); // NOI18N
        jScrollPaneEditor.setViewportView(jEditorPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelExecutionLog.add(jScrollPaneEditor, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.2;
        jPanelInputForm.add(jPanelExecutionLog, gridBagConstraints);

        jScrollPaneInputForm.setViewportView(jPanelInputForm);

        jSplitPaneInput.setLeftComponent(jScrollPaneInputForm);
        jSplitPaneInput.setRightComponent(viewerPanelInput);

        jPanelTabInput.add(jSplitPaneInput, java.awt.BorderLayout.CENTER);

        jTabbedPaneTwoTabsDisplay.addTab("Input", jPanelTabInput);

        jPanelTabResults.setLayout(new java.awt.BorderLayout());

        jSplitPaneResults.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPaneResults.setResizeWeight(0.9);
        jSplitPaneResults.setLeftComponent(viewerPanelResults);

        jTablePanel.setPreferredSize(new java.awt.Dimension(900, 100));
        jSplitPaneResults.setBottomComponent(jTablePanel);

        jPanelTabResults.add(jSplitPaneResults, java.awt.BorderLayout.CENTER);

        jTabbedPaneTwoTabsDisplay.addTab("Results", jPanelTabResults);

        add(jTabbedPaneTwoTabsDisplay, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void jSliderResultsStateChanged(ChangeEvent evt) {
        if (!syncingUI && jSliderResults.getValue() != -1) {
            // get index in [0; n - 1]
            final int index = jSliderResults.getValue() - 1;

            if (index != sliderResultLastIndex) {
                sliderResultLastIndex = index;
                // changing selection will display the corresponding result image:
                // note: use table order:
                jTablePanel.setSelectedViewRow(index);
            }
        }
    }

    private void jCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxActionPerformed
        updateModel();
    }//GEN-LAST:event_jCheckBoxActionPerformed

    private void jFormattedTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextFieldActionPerformed
        updateModel();
    }//GEN-LAST:event_jFormattedTextFieldActionPerformed

    private void jFormattedTextFieldPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jFormattedTextFieldPropertyChange
        updateModel();
    }//GEN-LAST:event_jFormattedTextFieldPropertyChange

    private void showTablePanel(final boolean visible) {
        if (this.jTablePanel.isVisible() != visible) {
            if (visible) {
                // ensure 10% for table (not too large):
                this.jSplitPaneResults.setDividerLocation(0.9);
            }
            this.jTablePanel.setVisible(visible);
        }
    }

    private void jButtonCompareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCompareActionPerformed
        final int selected = jTablePanel.getSelectedRowsCount();

        if (selected == 0) {
            // select all results in table:
            jTablePanel.setSelectedViewAll();
            // note: use table order:
            viewerPanelResults.displayGrid(jTablePanel.getSelectedRows());
        } else if (selected == 1) {
            viewerPanelResults.displayResult(jTablePanel.getSelectedRow());
        } else {
            viewerPanelResults.displayGrid(jTablePanel.getSelectedRows());
        }
    }//GEN-LAST:event_jButtonCompareActionPerformed

    private void jTabbedPaneTwoTabsDisplayStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPaneTwoTabsDisplayStateChanged
        // we need this precaution, unless updateEnabledActions() is called too soon in the 
        // application start, and it provokes a bug.
        if (Bootstrapper.getState().after(App.ApplicationState.GUI_SETUP)) {
            
            // when the tab is changed, another viewerPanel is displayed,
            // so we have to (en/dis)able actions. i.e, if the new viewerPanel has no images,
            // the action exportFitsImage must be disabled.
            updateEnabledActions();
        }
    }//GEN-LAST:event_jTabbedPaneTwoTabsDisplayStateChanged

    public void deleteSelectedRows() {
        final int nSelected = jTablePanel.getSelectedRowsCount();
        if (nSelected != 0) {
            if (MessagePane.showConfirmMessage(this.jTablePanel,
                    "Do you want to delete " + nSelected + " result(s) ?")) {

                currentModel.removeServiceResults(jTablePanel.getSelectedRows());
            }
        }
    }

    /**
     * Listen for list selection changes
     *
     * @param e list selection event
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        // Skip events when the user selection is adjusting :
        if (e.getValueIsAdjusting()) {
            return;
        }
        // This action only update GUI but not the model.

        // enable delete action if the result table has rows selected:
        deleteSelectionAction.setEnabled(jTablePanel.getSelectedRowsCount() != 0);

        if (e.getSource() == jTablePanel.getSelectionModel()) {
            viewerPanelResults.displayResult(jTablePanel.getSelectedRow());
        } else {
            logger.warn("valueChanged: Unsupported component : {}", e.getSource());
        }
    }

    /**
     * Decides if each action should be enabled or not.
     * is called when the selection in the list of results changes,
     * or when the tab (input/results) selection changes,
     * or when the function setTabMode in ViewerPanel is called (which more or less means when
     * the ViewerPanel data has been updated).
     */
    public void updateEnabledActions() {
        ViewerPanel activeViewerPanel = this.getViewerPanelActive();
                        
        final OIFitsFile oiFitsFile = activeViewerPanel.getCurrentOIFitsFile();
        final boolean enableExportOiFits = (oiFitsFile != null) && (oiFitsFile.getNbOiTables() > 0);
        exportOiFitsAction.setEnabled(enableExportOiFits);
        sendOiFitsAction.setEnabled(enableExportOiFits);

        final boolean enableExportImage = (!activeViewerPanel.isFitsImageNull());
        exportFitsImageAction.setEnabled(enableExportImage);
        sendFitsAction.setEnabled(enableExportImage);
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCompare;
    private javax.swing.JButton jButtonExportOIFits;
    private javax.swing.JButton jButtonLoadData;
    private javax.swing.JButton jButtonRun;
    private javax.swing.JCheckBox jCheckBoxUseT3;
    private javax.swing.JCheckBox jCheckBoxUseVis;
    private javax.swing.JCheckBox jCheckBoxUseVis2;
    private javax.swing.JComboBox jComboBoxTarget;
    private javax.swing.JEditorPane jEditorPane;
    private javax.swing.JFormattedTextField jFormattedTextFieldWaveMax;
    private javax.swing.JFormattedTextField jFormattedTextFieldWaveMin;
    private javax.swing.JLabel jLabelOifitsFile;
    private javax.swing.JLabel jLabelTabInput;
    private javax.swing.JLabel jLabelTabResults;
    private javax.swing.JLabel jLabelTarget;
    private javax.swing.JLabel jLabelWaveMax;
    private javax.swing.JLabel jLabelWaveMin;
    private javax.swing.JPanel jPanelDataSelection;
    private javax.swing.JPanel jPanelExecutionLog;
    private javax.swing.JPanel jPanelInputForm;
    private javax.swing.JPanel jPanelTabInput;
    private javax.swing.JPanel jPanelTabResults;
    private javax.swing.JPanel jPanelTarget;
    private javax.swing.JPanel jPanelWL;
    private javax.swing.JScrollPane jScrollPaneEditor;
    private javax.swing.JScrollPane jScrollPaneInputForm;
    private javax.swing.JSlider jSliderWaveMax;
    private javax.swing.JSlider jSliderWaveMin;
    private javax.swing.JSplitPane jSplitPaneInput;
    private javax.swing.JSplitPane jSplitPaneResults;
    private javax.swing.JTabbedPane jTabbedPaneTwoTabsDisplay;
    private fr.jmmc.oimaging.gui.ResultSetTablePanel jTablePanel;
    private fr.jmmc.oimaging.gui.SoftwareSettingsPanel softwareSettingsPanel;
    private fr.jmmc.oimaging.gui.ViewerPanel viewerPanelInput;
    private fr.jmmc.oimaging.gui.ViewerPanel viewerPanelResults;
    // End of variables declaration//GEN-END:variables

    @Override
    public String getSubjectId(IRModelEventType type) {
        return null;
    }

    public void onProcess(final IRModelEvent event) {
        logger.debug("onProcess {}", event);

        switch (event.getType()) {
            case IRMODEL_CHANGED:
                syncUI(event);
                break;
            case IRMODEL_RESULT_LIST_CHANGED:
                syncUI(event);
                break;
            default:
                logger.info("event not handled : {}", event);
        }
        logger.debug("onProcess {} - done", event);
    }

    /**
     * Update model attributes following swing events.
     *
     */
    protected void updateModel() {
        updateModel(false);
    }

    protected void updateModel(final boolean forceChange) {
        if (syncingUI) {
            logger.debug("updateModel discarded: syncUI.");
            return;
        }

        final IRModel irModel = IRModelManager.getInstance().getIRModel();
        currentModel = irModel;

        final ImageOiInputParam params = irModel.getImageOiData().getInputParam();

        // specific params must be updated
        irModel.initSpecificParams(false);

        // Update if model_values != swing_values and detect change if one or more values change
        boolean changed = false;
        double mDouble, wDouble;
        String mString, wString;
        boolean mFlag, wFlag;

        // Target
        mString = params.getTarget();
        wString = (String) jComboBoxTarget.getSelectedItem();
        if ((mString != null && !mString.equals(wString)) || (wString != null && !wString.equals(mString))) {
            params.setTarget(wString);
            changed = true;
        }

        // wavelength
        mDouble = params.getWaveMin();
        if (jFormattedTextFieldWaveMin.getValue() != null) {
            wDouble = ((Number) jFormattedTextFieldWaveMin.getValue()).doubleValue() * MICRO_METER;
            if (mDouble != wDouble) {
                params.setWaveMin(wDouble);
                changed = true;
            }
        }

        mDouble = params.getWaveMax();
        if (jFormattedTextFieldWaveMax.getValue() != null) {
            wDouble = ((Number) jFormattedTextFieldWaveMax.getValue()).doubleValue() * MICRO_METER;
            if (mDouble != wDouble) {
                params.setWaveMax(wDouble);
                changed = true;
            }
        }

        // observables
        mFlag = params.useVis();
        wFlag = jCheckBoxUseVis.isSelected();
        if (mFlag != wFlag) {
            params.useVis(wFlag);
            changed = true;
        }

        mFlag = params.useVis2();
        wFlag = jCheckBoxUseVis2.isSelected();
        if (mFlag != wFlag) {
            params.useVis2(wFlag);
            changed = true;
        }

        mFlag = params.useT3();
        wFlag = jCheckBoxUseT3.isSelected();
        if (mFlag != wFlag) {
            params.useT3(wFlag);
            changed = true;
        }

        // Check if algo settings change given model
        if (softwareSettingsPanel.updateModel(irModel)) {
            changed = true;
        }

        changed |= forceChange;

        // some values have changed
        if (changed) {
            // notify to other listener - if any in the future
            logger.debug("GUI updated");
            IRModelManager.getInstance().fireIRModelChanged(this, null);
        }
    }

    /**
     * Update swing widgets with content of given irModel.
     * @param event model event
     */
    private void syncUI(IRModelEvent event) {
        syncingUI = true;
        try {
            currentModel = event.getIrModel();

            final OIFitsFile oifitsFile = currentModel.getOifitsFile();
            final boolean hasOIData = oifitsFile.hasOiData();
            final ImageOiInputParam inputParam = currentModel.getImageOiData().getInputParam();

            // associate target list
            jComboBoxTarget.setModel(currentModel.getTargetListModel());
            jComboBoxTarget.setSelectedItem(inputParam.getTarget());

            // TODO Update OIFitsViewer:
            // arrange target
            jComboBoxTarget.setEnabled(hasOIData);

            // arrange wavelength filters
            jSliderWaveMin.setEnabled(hasOIData);
            jSliderWaveMax.setEnabled(hasOIData);

            final Range effWaveRange = oifitsFile.getWavelengthRange();

            fieldSliderAdapterWaveMin.reset(effWaveRange.getMin() / MICRO_METER, effWaveRange.getMax() / MICRO_METER,
                    inputParam.getWaveMin() / MICRO_METER);
            fieldSliderAdapterWaveMax.reset(effWaveRange.getMin() / MICRO_METER, effWaveRange.getMax() / MICRO_METER,
                    inputParam.getWaveMax() / MICRO_METER);

            jFormattedTextFieldWaveMin.setEnabled(hasOIData);
            jFormattedTextFieldWaveMax.setEnabled(hasOIData);
            jFormattedTextFieldWaveMin.setValue(inputParam.getWaveMin() / MICRO_METER);
            jFormattedTextFieldWaveMax.setValue(inputParam.getWaveMax() / MICRO_METER);

            // arrange observable checkboxes
            jCheckBoxUseVis.setEnabled(hasOIData && oifitsFile.hasOiVis());
            jCheckBoxUseVis2.setEnabled(hasOIData && oifitsFile.hasOiVis2());
            jCheckBoxUseT3.setEnabled(hasOIData && oifitsFile.hasOiT3());
            jCheckBoxUseVis.setSelected(hasOIData && inputParam.useVis());
            jCheckBoxUseVis2.setSelected(hasOIData && inputParam.useVis2());
            jCheckBoxUseT3.setSelected(hasOIData && inputParam.useT3());

            // model result list:
            final List<ServiceResult> modelResults = currentModel.getResultSets();
            final ServiceResult lastResult = currentModel.getLastResultSet();

            // resultSet Table
            if (event.getType() == IRModelEventType.IRMODEL_RESULT_LIST_CHANGED) {
                jTablePanel.setResults(modelResults);
            }

            // set the slider results boundaries
            if (modelResults.size() > 1) {
                jSliderResults.setMinimum(1);
                jSliderResults.setMaximum(modelResults.size());
                jSliderResults.setVisible(true);
            } else {
                jSliderResults.setVisible(false);
            }

            // perform analysis
            final List<String> failures = new LinkedList<String>();

            softwareSettingsPanel.syncUI(this, currentModel, failures);

            final String fileName = (hasOIData) ? currentModel.getOifitsFile().getFileName() : "";
            jLabelOifitsFile.setText(fileName);
            jLabelOifitsFile.setToolTipText(fileName);

            if (!hasOIData) {
                failures.add("Missing OIData, please load an OIFits");
            } else if (inputParam.getWaveMax() < inputParam.getWaveMin()) {
                failures.add("WAVE_MIN is higher than WAVE_MAX");
            }
            /* Not sure
            for (OIData table : oifitsFile.getOiDataList()) {
                if (table.getOiRevn() > 1) {
                    failures.add("OIFits V2 tables not yet supported (" + table.getExtName() + "#" + table.getExtNb() + ")");
                }
            }
             */

            // if nothing is wrong, allow related actions
            // TODO make this idea more global and on an higher level Manager.setValid(true) e.g. ?
            final boolean modelOk = failures.isEmpty();
            runAction.setEnabled(modelOk);

            final StringBuffer sb = new StringBuffer(256);
            if (modelOk) {
                sb.append("<li><font color=green>Ready to spawn process</font></li>");
            } else {
                for (String fail : failures) {
                    sb.append("<li><font color=red>").append(fail).append("</font></li>");
                }
            }
            jEditorPane.setText("<html><ul>" + sb.toString() + "</ul></html>");

            switch (event.getType()) {
                case IRMODEL_CHANGED:
                    viewerPanelInput.displayModel(currentModel);
                    jTabbedPaneTwoTabsDisplay.setSelectedIndex(TABS.INPUT.ordinal());
                    break;
                case IRMODEL_RESULT_LIST_CHANGED:
                    if (modelResults.isEmpty()) {
                        showTablePanel(false);
                        viewerPanelResults.displayResult(null);
                    } else {
                        showTablePanel(true);
                        if (lastResult != null) {
                            jTablePanel.setSelectedRow(lastResult);

                            // we also update input display because a new run will change init image
                            viewerPanelInput.displayModel(currentModel);
                        }
                    }
                    jTabbedPaneTwoTabsDisplay.setSelectedIndex(TABS.RESULTS.ordinal());
                    break;
                default:
                    break;
            }
        } finally {
            syncingUI = false;
        }
    }

    public ViewerPanel getViewerPanelInput() {
        return viewerPanelInput;
    }

    /** return the active viewer panel. based on active tab.
     * @return viewerPanelInput, or viewerPanelResults, or null when no tabs selected (it should never happen).
     */
    public ViewerPanel getViewerPanelActive() {
        switch (TABS.values()[jTabbedPaneTwoTabsDisplay.getSelectedIndex()]) {
            case INPUT:
                return viewerPanelInput;
            case RESULTS:
                return viewerPanelResults;
        }
        return null;
    }

    private static String getTooltip(final String name) {
        return ImageOiInputParam.getDescription(name);
    }

    /**
     * @return the jTablePanel
     */
    public ResultSetTablePanel getResultSetTablePanel() {
        return jTablePanel;
    }

    /** Switch tab. */
    public void switchTab () {
        // only works when there is only two tabs
        this.jTabbedPaneTwoTabsDisplay.setSelectedIndex(1 - this.jTabbedPaneTwoTabsDisplay.getSelectedIndex());
    }
}
