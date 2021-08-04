/**
 * *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 *****************************************************************************
 */
package fr.jmmc.oimaging.gui;

import fr.jmmc.jmcs.App;
import fr.jmmc.jmcs.gui.action.ActionRegistrar;
import fr.jmmc.jmcs.gui.component.GenericListModel;
import fr.jmmc.jmcs.gui.task.TaskSwingWorkerExecutor;
import fr.jmmc.jmcs.gui.util.FieldSliderAdapter;
import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.jmcs.util.SpecialChars;
import fr.jmmc.oimaging.gui.action.DeleteSelectionAction;
import fr.jmmc.oimaging.gui.action.ExportFitsImageAction;
import fr.jmmc.oimaging.gui.action.ExportOIFitsAction;
import fr.jmmc.oimaging.gui.action.LoadOIFitsAction;
import fr.jmmc.oimaging.gui.action.RunAction;
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
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.event.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main container of OIFits Explorer App
 *
 * @author mella
 */
public class MainPanel extends javax.swing.JPanel implements IRModelEventListener, ListSelectionListener, TableModelListener {

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

    // if multi page activated the export file will containt global view + each plot on a page/image
    private final List<String> hduNameList = new ArrayList<String>(5);

    private final List<ServiceResult> resultSetList = new ArrayList<ServiceResult>(5);

    /** ResultSet list model */
    GenericListModel<ServiceResult> resultSetListModel = new GenericListModel<ServiceResult>(resultSetList);
        
    /* members */
    /** actions */
    private DeleteSelectionAction deleteSelectionAction;
    private RunAction runAction;
    private Action exportOiFitsAction;
    private Action exportFitsImageAction;

    /** Flag set to true while the GUI is being updated by model else false. */
    private boolean syncingUI = false;

    FieldSliderAdapter fieldSliderAdapterWaveMin;
    FieldSliderAdapter fieldSliderAdapterWaveMax;
    /** timeline refresh Swing timer */
    private final Timer timerMouseCursorRefresh;
    private IRModel currentModel;

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

        jListResults.setCellRenderer(new OiCellRenderer());

        jLabelWaveMin.setText("WAVE_MIN [" + SpecialChars.UNIT_MICRO_METER + ']');
        jLabelWaveMax.setText("WAVE_MAX [" + SpecialChars.UNIT_MICRO_METER + ']');

        // associate sliders and fields
        fieldSliderAdapterWaveMin = new FieldSliderAdapter(jSliderWaveMin, jFormattedTextFieldWaveMin, 0, 1, 0);
        fieldSliderAdapterWaveMax = new FieldSliderAdapter(jSliderWaveMax, jFormattedTextFieldWaveMax, 0, 1, 0);

        // become widget listener
        jListResults.addListSelectionListener((ListSelectionListener) this);
        jTablePanel.getTable().getModel().addTableModelListener((TableModelListener) this);
        
        // init viewer Panel
        viewerPanel.displayModel(null);
        
        jTablePanel.addControlComponent(jButtonCompare);
        jTablePanel.addControlComponent(jButtonDelete);
        jTablePanel.addControlComponent(jSliderResults);

        jSplitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, new PropertyChangeListener() {

            @Override
            public void propertyChange(final PropertyChangeEvent changeEvent) {
                final Integer last = (Integer) changeEvent.getNewValue();
                final Integer priorLast = (Integer) changeEvent.getOldValue();

                if (last < priorLast) {
                    // restore free space ie avoid having the left panel too large:
                    jScrollPane.revalidate();
                }
            }
        });
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
        //  TODO fix next call      jButtonRun.setText((String) runAction.getValue(Action.SHORT_DESCRIPTION));
        jButtonRun.setText("Run");

        exportOiFitsAction = ActionRegistrar.getInstance().get(ExportOIFitsAction.className, ExportOIFitsAction.actionName);
        jButtonExportOIFits.setAction(exportOiFitsAction);

        exportFitsImageAction = ActionRegistrar.getInstance().get(ExportFitsImageAction.className, ExportFitsImageAction.actionName);
        jButtonExportImage.setAction(exportFitsImageAction);

    }

    public void dispose() {
        if (logger.isDebugEnabled()) {
            logger.debug("dispose: {}", ObjectUtils.getObjectInfo(this));
        }

        // TODO release child resource if any
    }
    
    private JList createCustomList() {
        final JList list = new JList() {
            /** default serial UID for Serializable interface */
            private static final long serialVersionUID = 1;
            /* members */
            /** tooltip buffer */
            private final StringBuffer sbToolTip = new StringBuffer(512);
            /** last item index at the mouse position */
            private int lastIndex;
            /** last tooltip at item index */
            private String lastTooltip;

            /** update list model and reset last tooltip */
            @Override
            public void setModel(final ListModel model) {
                super.setModel(model);

                // reset last tooltip:
                lastIndex = -1;
                lastTooltip = null;
            }

            /** This method is called as the cursor moves within the list */
            @Override
            public String getToolTipText(final MouseEvent evt) {
                // Get item index :
                final int index = locationToIndex(evt.getPoint());
                if (index != -1) {
                    String tooltip = null;

                    if (lastIndex == index) {
                        // use last tooltip:
                        tooltip = lastTooltip;
                    } else {
                        final ServiceResult serviceResult = (ServiceResult) getModel().getElementAt(index);

                        final Date start = serviceResult.getStartTime();
                        final Date end = serviceResult.getEndTime();

                        if (start != null && end != null) {
                            final long duration = (end.getTime() - start.getTime());
                            final long sec = duration / 1000l;
                            final long ms = duration - sec * 1000l;
                            sbToolTip.setLength(0); // clear
                            tooltip = sbToolTip.append("Elapsed time: ").append(sec).append('.').append(ms).append(" s").toString();
                        }

                        lastIndex = index;
                        lastTooltip = tooltip;
                        return tooltip;
                    }
                    return tooltip;
                }
                return getToolTipText();
            }
        };

        return list;
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
        jButtonDelete = new javax.swing.JButton();
        jSplitPaneGlobal = new javax.swing.JSplitPane();
        jSplitPane = new javax.swing.JSplitPane();
        viewerPanel = new fr.jmmc.oimaging.gui.ViewerPanel();
        jScrollPane = new javax.swing.JScrollPane();
        jPanelLeft = new javax.swing.JPanel();
        jPanelDataSelection = new javax.swing.JPanel();
        jComboBoxTarget = new javax.swing.JComboBox();
        jLabelTarget = new javax.swing.JLabel();
        jLabelOifitsFile = new javax.swing.JLabel();
        jButtonLoadData = new javax.swing.JButton();
        jLabelWaveMin = new javax.swing.JLabel();
        jLabelWaveMax = new javax.swing.JLabel();
        jCheckBoxUseVis = new javax.swing.JCheckBox();
        jCheckBoxUseVis2 = new javax.swing.JCheckBox();
        jCheckBoxUseT3 = new javax.swing.JCheckBox();
        jSliderWaveMin = new javax.swing.JSlider();
        jSliderWaveMax = new javax.swing.JSlider();
        jSliderResults = new javax.swing.JSlider();
        jFormattedTextFieldWaveMin = new javax.swing.JFormattedTextField();
        jFormattedTextFieldWaveMax = new javax.swing.JFormattedTextField();
        softwareSettingsPanel = new fr.jmmc.oimaging.gui.SoftwareSettingsPanel();
        jPanelExecutionLog = new javax.swing.JPanel();
        jButtonRun = new javax.swing.JButton();
        jButtonExportImage = new javax.swing.JButton();
        jButtonExportOIFits = new javax.swing.JButton();
        jScrollPaneEditor = new javax.swing.JScrollPane();
        jEditorPane = new javax.swing.JEditorPane();
        jPanelResults = new javax.swing.JPanel();
        jScrollPaneResults = new javax.swing.JScrollPane();
        jListResults = createCustomList();
        jResultsTableShowButton = new javax.swing.JButton();
        jTablePanel = new fr.jmmc.oimaging.gui.TablePanel();

        jButtonCompare.setText("Compare");
        jButtonCompare.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCompareActionPerformed(evt);
            }
        });

        jButtonDelete.setText("Delete");
        jButtonDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeleteActionPerformed(evt);
            }
        });

        jSliderResults.setMinimum(-1);
        jSliderResults.setMaximum(-1);
        jSliderResults.setValue(-1);
        jSliderResults.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderResultsStateChanged(evt);
            }
        });

        setLayout(new java.awt.BorderLayout());

        jSplitPaneGlobal.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPaneGlobal.setMinimumSize(new java.awt.Dimension(900, 30));
        jSplitPaneGlobal.setDividerLocation(100);

        jSplitPane.setResizeWeight(0.01);
        jSplitPane.setContinuousLayout(true);
        jSplitPane.setMinimumSize(new java.awt.Dimension(900, 600));
        jSplitPane.setPreferredSize(new java.awt.Dimension(900, 600));
        jSplitPane.setRightComponent(viewerPanel);

        jScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jScrollPane.setViewportView(jPanelLeft);

        jPanelLeft.setLayout(new java.awt.GridBagLayout());

        jPanelDataSelection.setBorder(javax.swing.BorderFactory.createTitledBorder("Data selection"));
        jPanelDataSelection.setLayout(new java.awt.GridBagLayout());

        jComboBoxTarget.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxTarget.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_TARGET));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelDataSelection.add(jComboBoxTarget, gridBagConstraints);

        jLabelTarget.setText("TARGET");
        jLabelTarget.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_TARGET));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelDataSelection.add(jLabelTarget, gridBagConstraints);

        jLabelOifitsFile.setText("OIFITS Label");
        jLabelOifitsFile.setMinimumSize(new java.awt.Dimension(100, 40));
        jLabelOifitsFile.setPreferredSize(new java.awt.Dimension(100, 40));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanelDataSelection.add(jLabelOifitsFile, gridBagConstraints);

        jButtonLoadData.setText("load oifits");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 4);
        jPanelDataSelection.add(jButtonLoadData, gridBagConstraints);

        jLabelWaveMin.setText("WAVE_MIN");
        jLabelWaveMin.setToolTipText("<html>" + getTooltip(ImageOiConstants.KEYWORD_WAVE_MIN) + "<br/><b>Editor unit is '" + FitsUnit.WAVELENGTH_MICRO_METER.getRepresentation() + "'</b></html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelDataSelection.add(jLabelWaveMin, gridBagConstraints);

        jLabelWaveMax.setText("WAVE_MAX");
        jLabelWaveMax.setToolTipText("<html>" + getTooltip(ImageOiConstants.KEYWORD_WAVE_MAX) + "<br/><b>Editor unit is '" + FitsUnit.WAVELENGTH_MICRO_METER.getRepresentation() + "'</b></html>");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelDataSelection.add(jLabelWaveMax, gridBagConstraints);

        jCheckBoxUseVis.setText("USE_VIS");
        jCheckBoxUseVis.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_USE_VIS));
        jCheckBoxUseVis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        jPanelDataSelection.add(jCheckBoxUseVis, gridBagConstraints);

        jCheckBoxUseVis2.setText("USE_VIS2");
        jCheckBoxUseVis2.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_USE_VIS2));
        jCheckBoxUseVis2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        jPanelDataSelection.add(jCheckBoxUseVis2, gridBagConstraints);

        jCheckBoxUseT3.setText("USE_T3");
        jCheckBoxUseT3.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_USE_T3));
        jCheckBoxUseT3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        jPanelDataSelection.add(jCheckBoxUseT3, gridBagConstraints);

        jSliderWaveMin.setPaintTicks(true);
        jSliderWaveMin.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_WAVE_MIN));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        jPanelDataSelection.add(jSliderWaveMin, gridBagConstraints);

        jSliderWaveMax.setPaintTicks(true);
        jSliderWaveMax.setToolTipText(getTooltip(ImageOiConstants.KEYWORD_WAVE_MAX));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        jPanelDataSelection.add(jSliderWaveMax, gridBagConstraints);

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
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 4);
        jPanelDataSelection.add(jFormattedTextFieldWaveMin, gridBagConstraints);

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
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 4);
        jPanelDataSelection.add(jFormattedTextFieldWaveMax, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanelLeft.add(jPanelDataSelection, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanelLeft.add(softwareSettingsPanel, gridBagConstraints);

        jPanelExecutionLog.setBorder(javax.swing.BorderFactory.createTitledBorder("Action panel"));
        jPanelExecutionLog.setLayout(new java.awt.GridBagLayout());

        jButtonRun.setText("Run");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelExecutionLog.add(jButtonRun, gridBagConstraints);

        jButtonExportImage.setText("Save image");
        jButtonExportImage.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelExecutionLog.add(jButtonExportImage, gridBagConstraints);

        jButtonExportOIFits.setText("Save Oifits");
        jButtonExportOIFits.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
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
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelExecutionLog.add(jScrollPaneEditor, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.2;
        jPanelLeft.add(jPanelExecutionLog, gridBagConstraints);

        jPanelResults.setBorder(javax.swing.BorderFactory.createTitledBorder("Result sets"));
        jPanelResults.setPreferredSize(new java.awt.Dimension(100, 100));
        jPanelResults.setLayout(new java.awt.BorderLayout());

        jScrollPaneResults.setMinimumSize(new java.awt.Dimension(100, 200));
        jScrollPaneResults.setPreferredSize(new java.awt.Dimension(100, 200));

        jScrollPaneResults.setViewportView(jListResults);

        jPanelResults.add(jScrollPaneResults, java.awt.BorderLayout.CENTER);

        jResultsTableShowButton.setText("Hide details");
        jResultsTableShowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jResultsTableShowButtonActionPerformed(evt);
            }
        });
        jPanelResults.add(jResultsTableShowButton, java.awt.BorderLayout.PAGE_END);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.5;
        jPanelLeft.add(jPanelResults, gridBagConstraints);

        jScrollPane.setViewportView(jPanelLeft);

        jSplitPane.setLeftComponent(jScrollPane);

        jSplitPaneGlobal.setLeftComponent(jSplitPane);

        jTablePanel.setPreferredSize(new java.awt.Dimension(1754, 10));
        jSplitPaneGlobal.setRightComponent(jTablePanel);

        add(jSplitPaneGlobal, java.awt.BorderLayout.CENTER);
        jSplitPaneGlobal.setTopComponent(jSplitPane);
        jSplitPaneGlobal.setBottomComponent(jTablePanel);
    }// </editor-fold>//GEN-END:initComponents

    private void jSliderResultsStateChanged(ChangeEvent evt) {
        if (jSliderResults.getValue() != -1) {
            viewerPanel.displayResult(resultSetList.get(jSliderResults.getMaximum() - jSliderResults.getValue()));
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

    private void jResultsTableShowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jResultsTableShowButtonActionPerformed
        if (this.jTablePanel.isVisible()) {
            this.jTablePanel.setVisible(false);
            this.jResultsTableShowButton.setText("Show details");
        } else {
            this.jTablePanel.setVisible(true);
            this.jResultsTableShowButton.setText("Hide details");
        }
    }//GEN-LAST:event_jResultsTableShowButtonActionPerformed

    private void jButtonCompareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCompareActionPerformed
        List<ServiceResult> resultsToCompare = new ArrayList<>();
        for (Integer index : jTablePanel.getTable().getSelectedRows()) {
            resultsToCompare.add(resultSetList.get(index));
        }
        viewerPanel.displayGrid(resultsToCompare);
    }//GEN-LAST:event_jButtonCompareActionPerformed

    private void jButtonDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeleteActionPerformed
        JTable table = jTablePanel.getTable();
        
        if (table.getSelectedRowCount() != 0) {
            List<Integer> rowsToDelete = Arrays.stream(table.getSelectedRows()).boxed().collect(Collectors.toList());
            Collections.reverse(rowsToDelete);
            rowsToDelete.forEach((Integer index) -> {
                currentModel.removeServiceResult(resultSetList.get(index));
            });
        }
    }//GEN-LAST:event_jButtonDeleteActionPerformed

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
        if (e.getSource() == jListResults) {
            viewerPanel.displayResult((ServiceResult) jListResults.getSelectedValue());
            deleteSelectionAction.watchResultsSelection(currentModel, jListResults);
        }

    }
    
    /**
     * Listen for table selection changes
     * 
     * @param e 
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        if (e.getSource() == jTablePanel.getTable()) {
            viewerPanel.displayResult((ServiceResult) jListResults.getModel().getElementAt(jTablePanel.getTable().getSelectedRow()));
            deleteSelectionAction.watchResultsSelection(currentModel, jListResults);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonCompare;
    private javax.swing.JButton jButtonDelete;
    private javax.swing.JButton jButtonExportImage;
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
    private javax.swing.JLabel jLabelTarget;
    private javax.swing.JLabel jLabelWaveMax;
    private javax.swing.JLabel jLabelWaveMin;
    private javax.swing.JList jListResults;
    private javax.swing.JPanel jPanelDataSelection;
    private javax.swing.JPanel jPanelExecutionLog;
    private javax.swing.JPanel jPanelLeft;
    private javax.swing.JPanel jPanelResults;
    private javax.swing.JButton jResultsTableShowButton;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JScrollPane jScrollPaneEditor;
    private javax.swing.JScrollPane jScrollPaneResults;
    private javax.swing.JSlider jSliderWaveMax;
    private javax.swing.JSlider jSliderWaveMin;
    private javax.swing.JSlider jSliderResults;
    private javax.swing.JSplitPane jSplitPane;
    private javax.swing.JSplitPane jSplitPaneGlobal;
    private fr.jmmc.oimaging.gui.TablePanel jTablePanel;
    private fr.jmmc.oimaging.gui.SoftwareSettingsPanel softwareSettingsPanel;
    private fr.jmmc.oimaging.gui.ViewerPanel viewerPanel;
    // End of variables declaration//GEN-END:variables

    @Override
    public String getSubjectId(IRModelEventType type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void onProcess(final IRModelEvent event) {
        logger.debug("onProcess {}", event);

        switch (event.getType()) {
            case IRMODEL_CHANGED:
                syncUI(event);
                break;
            case IRMODEL_UPDATED:
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

        // TODO may be a copy dedicated to this class
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

            // resultSet List
            resultSetListModel.clear();
            resultSetListModel.add(currentModel.getResultSets());
            jListResults.setModel(resultSetListModel);
            
            // resultSet Table
            jTablePanel.getTableModel().clear();
            jTablePanel.getTableModel().addResult(currentModel.getResultSets());

            // set the slider results boundaries
            if (resultSetList.size() > 1) {
                jSliderResults.setMinimum(1);
                jSliderResults.setMaximum(resultSetList.size());
                jSliderResults.setVisible(true);
            }
            else {
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

            if (event.getType() == IRModelEventType.IRMODEL_CHANGED || jListResults.getModel().getSize() == 0) {
                viewerPanel.displayModel(currentModel);
            } else {
                jListResults.setSelectedIndex(0);
            }
        } finally {
            syncingUI = false;
        }
    }

    public ViewerPanel getViewerPanel() {
        return viewerPanel;

    }

    private static String getTooltip(final String name) {
        return ImageOiInputParam.getDescription(name);
    }
}
