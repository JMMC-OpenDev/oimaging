/**
 * *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 *****************************************************************************
 */
package fr.jmmc.oimaging.gui;

import fr.jmmc.jmcs.App;
import fr.jmmc.jmcs.gui.action.ActionRegistrar;
import fr.jmmc.jmcs.gui.component.GenericListModel;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.gui.task.TaskSwingWorkerExecutor;
import fr.jmmc.jmcs.gui.util.FieldSliderAdapter;
import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.jmcs.util.SpecialChars;
import fr.jmmc.oimaging.gui.action.ExportFitsImageAction;
import fr.jmmc.oimaging.gui.action.ExportOIFitsAction;
import fr.jmmc.oimaging.gui.action.LoadFitsImageAction;
import fr.jmmc.oimaging.gui.action.LoadOIFitsAction;
import fr.jmmc.oimaging.gui.action.RunAction;
import fr.jmmc.oimaging.model.IRModel;
import fr.jmmc.oimaging.model.IRModelEvent;
import fr.jmmc.oimaging.model.IRModelEventListener;
import fr.jmmc.oimaging.model.IRModelEventType;
import fr.jmmc.oimaging.model.IRModelManager;
import fr.jmmc.oimaging.services.Service;
import fr.jmmc.oimaging.services.ServiceList;
import fr.jmmc.oimaging.services.ServiceResult;
import fr.jmmc.oitools.image.FitsImageHDU;
import fr.jmmc.oitools.image.ImageOiInputParam;
import fr.jmmc.oitools.model.OIFitsFile;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.Timer;
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

    /** micrometres to meter */
    public final static double MICRO_METER = 1e-6;

    /**
     * default serial UID for Serializable interface
     */
    private static final long serialVersionUID = 1;
    /** default mouse cursor refresh period = 100 ms */
    private static final int REFRESH_PERIOD = 100;

    // if multi page activated the export file will containt global view + each plot on a page/image
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(MainPanel.class);

    private final List<String> hduNameList = new ArrayList<String>(5);

    private final List<ServiceResult> resultSetList = new ArrayList<ServiceResult>(5);

    /** ResultSet list model */
    GenericListModel<ServiceResult> resultSetListModel = new GenericListModel<ServiceResult>(resultSetList);

    /* members */
    /** actions */
    private Action loadFitsImageAction;
    private RunAction runAction;
    private AbstractAction exportOiFitsAction;
    private AbstractAction exportFitsImageAction;

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

        jListResultSet.setCellRenderer(new ServiceResultCellRenderer());

        jLabelWaveMin.setText("WAVE_MIN [" + SpecialChars.UNIT_MICRO_METER + "]");
        jLabelWaveMax.setText("WAVE_MAX [" + SpecialChars.UNIT_MICRO_METER + "]");

        // associate sliders and fields
        fieldSliderAdapterWaveMin = new FieldSliderAdapter(jSliderWaveMin, jFormattedTextFieldWaveMin, 0, 1, 0);
        fieldSliderAdapterWaveMax = new FieldSliderAdapter(jSliderWaveMax, jFormattedTextFieldWaveMax, 0, 1, 0);

        // become widget listener
        jListResultSet.addListSelectionListener((ListSelectionListener) this);

        // init viewer Panel
        viewerPanel.displayModel(null);
    }

    /**
     * Create the main actions
     */
    private void registerActions() {
        // Map actions to widgets

        jButtonLoadData.setAction(ActionRegistrar.getInstance().get(LoadOIFitsAction.className, LoadOIFitsAction.actionName));

        loadFitsImageAction = ActionRegistrar.getInstance().get(LoadFitsImageAction.className, LoadFitsImageAction.actionName);
        jButtonLoadFitsImage.setAction(loadFitsImageAction);
        jButtonLoadFitsImage.setText((String) loadFitsImageAction.getValue(Action.SHORT_DESCRIPTION));

        runAction = (RunAction) ActionRegistrar.getInstance().get(RunAction.className, RunAction.actionName);
        jButtonRun.setAction(runAction);
        //  TODO fix next call      jButtonRun.setText((String) runAction.getValue(Action.SHORT_DESCRIPTION));
        jButtonRun.setText("Run");

        exportOiFitsAction = ActionRegistrar.getInstance().get(ExportOIFitsAction.className, ExportOIFitsAction.actionName);
        jButtonExportOIFits.setAction(exportOiFitsAction);
        //jButtonExportOIFits.setText((String) exportOiFitsAction.getValue(Action.SHORT_DESCRIPTION));
        jButtonExportOIFits.setText("Save OIFits");

        exportFitsImageAction = ActionRegistrar.getInstance().get(ExportFitsImageAction.className, ExportFitsImageAction.actionName);
        jButtonExportImage.setAction(exportFitsImageAction);
        //jButtonExportImage.setText((String) exportFitsImageAction.getValue(Action.SHORT_DESCRIPTION));
        jButtonExportImage.setText("Save image");

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
                        Object selectedItem = getModel().getElementAt(index);
                        if (selectedItem instanceof FitsImageHDUDecorator) {
                            final FitsImageHDU fih = ((FitsImageHDUDecorator) selectedItem).getFitsImageHDU();
                            if (fih != null) {
                                fih.toHtml(sbToolTip);
                            }
                        }
                        // ignore other object

                        lastIndex = index;
                        lastTooltip = tooltip;

                        // Return the tool tip text :
                        return sbToolTip.toString();
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

        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jPanelDataSelection = new javax.swing.JPanel();
        jLabelOifitsFile = new javax.swing.JLabel();
        jButtonLoadData = new javax.swing.JButton();
        jLabelWaveMin = new javax.swing.JLabel();
        jLabelWaveMax = new javax.swing.JLabel();
        jCheckBoxUseVis = new javax.swing.JCheckBox();
        jCheckBoxUseVis2 = new javax.swing.JCheckBox();
        jCheckBoxUseT3 = new javax.swing.JCheckBox();
        jSliderWaveMin = new javax.swing.JSlider();
        jSliderWaveMax = new javax.swing.JSlider();
        jFormattedTextFieldWaveMin = new javax.swing.JFormattedTextField();
        jFormattedTextFieldWaveMax = new javax.swing.JFormattedTextField();
        jPanel1 = new javax.swing.JPanel();
        jLabelTarget = new javax.swing.JLabel();
        jComboBoxTarget = new javax.swing.JComboBox();
        jPanelAlgorithmSettings = new javax.swing.JPanel();
        jLabelInitImg = new javax.swing.JLabel();
        jLabelMaxIter = new javax.swing.JLabel();
        jLabelRglName = new javax.swing.JLabel();
        jLabelRglWgt = new javax.swing.JLabel();
        jLabelRglAlph = new javax.swing.JLabel();
        jLabelRglBeta = new javax.swing.JLabel();
        jLabelRglPrio = new javax.swing.JLabel();
        jComboBoxSoftware = new javax.swing.JComboBox();
        jComboBoxImage = new javax.swing.JComboBox();
        jSpinnerMaxIter = new javax.swing.JSpinner();
        jComboBoxRglName = new javax.swing.JComboBox();
        jFormattedTextFieldRglWgt = new javax.swing.JFormattedTextField();
        jFormattedTextFieldRglAlph = new javax.swing.JFormattedTextField();
        jFormattedTextFieldRglBeta = new javax.swing.JFormattedTextField();
        jComboBoxRglPrio = new javax.swing.JComboBox();
        jButtonRemoveFitsImage = new javax.swing.JButton();
        jButtonLoadFitsImage = new javax.swing.JButton();
        jCheckBoxAutoWgt = new javax.swing.JCheckBox();
        jLabelFluxErr = new javax.swing.JLabel();
        jFormattedTextFieldFluxErr = new javax.swing.JFormattedTextField();
        jPanelExecutionLog = new javax.swing.JPanel();
        jButtonRun = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane = new javax.swing.JEditorPane();
        jButtonExportOIFits = new javax.swing.JButton();
        jButtonExportImage = new javax.swing.JButton();
        jPanelResults = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jListResultSet = createCustomList();
        viewerPanel = new fr.jmmc.oimaging.gui.ViewerPanel();

        setMaximumSize(new java.awt.Dimension(100, 100));
        setLayout(new java.awt.GridBagLayout());

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jPanelDataSelection.setBorder(javax.swing.BorderFactory.createTitledBorder("Data selection"));
        jPanelDataSelection.setLayout(new java.awt.GridBagLayout());

        jLabelOifitsFile.setText("oifits Label");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 2;
        jPanelDataSelection.add(jLabelOifitsFile, gridBagConstraints);

        jButtonLoadData.setText("load oifits");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanelDataSelection.add(jButtonLoadData, gridBagConstraints);

        jLabelWaveMin.setText("WAVE_MIN");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelDataSelection.add(jLabelWaveMin, gridBagConstraints);

        jLabelWaveMax.setText("WAVE_MAX");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelDataSelection.add(jLabelWaveMax, gridBagConstraints);

        jCheckBoxUseVis.setText("USE_VIS");
        jCheckBoxUseVis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        jPanelDataSelection.add(jCheckBoxUseVis, gridBagConstraints);

        jCheckBoxUseVis2.setText("USE_VIS2");
        jCheckBoxUseVis2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        jPanelDataSelection.add(jCheckBoxUseVis2, gridBagConstraints);

        jCheckBoxUseT3.setText("USE_T3");
        jCheckBoxUseT3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        jPanelDataSelection.add(jCheckBoxUseT3, gridBagConstraints);

        jSliderWaveMin.setPaintTicks(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        jPanelDataSelection.add(jSliderWaveMin, gridBagConstraints);

        jSliderWaveMax.setPaintTicks(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        jPanelDataSelection.add(jSliderWaveMax, gridBagConstraints);

        jFormattedTextFieldWaveMin.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter()));
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
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelDataSelection.add(jFormattedTextFieldWaveMin, gridBagConstraints);

        jFormattedTextFieldWaveMax.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter()));
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
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelDataSelection.add(jFormattedTextFieldWaveMax, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabelTarget.setText("TARGET");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanel1.add(jLabelTarget, gridBagConstraints);

        jComboBoxTarget.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanel1.add(jComboBoxTarget, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        jPanelDataSelection.add(jPanel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel3.add(jPanelDataSelection, gridBagConstraints);

        jPanelAlgorithmSettings.setBorder(javax.swing.BorderFactory.createTitledBorder("Algorithm settings"));
        jPanelAlgorithmSettings.setLayout(new java.awt.GridBagLayout());

        jLabelInitImg.setText("INIT_IMG");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelAlgorithmSettings.add(jLabelInitImg, gridBagConstraints);

        jLabelMaxIter.setText("MAXITER");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelAlgorithmSettings.add(jLabelMaxIter, gridBagConstraints);

        jLabelRglName.setText("RGL_NAME");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelAlgorithmSettings.add(jLabelRglName, gridBagConstraints);

        jLabelRglWgt.setText("RGL_WGT");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelAlgorithmSettings.add(jLabelRglWgt, gridBagConstraints);

        jLabelRglAlph.setText("RGL_ALPH");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelAlgorithmSettings.add(jLabelRglAlph, gridBagConstraints);

        jLabelRglBeta.setText("RGL_BETA");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelAlgorithmSettings.add(jLabelRglBeta, gridBagConstraints);

        jLabelRglPrio.setText("RGL_PRIO");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weighty = 0.1;
        jPanelAlgorithmSettings.add(jLabelRglPrio, gridBagConstraints);

        jComboBoxSoftware.setModel(ServiceList.getAvailableServices());
        jComboBoxSoftware.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxSoftwareActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelAlgorithmSettings.add(jComboBoxSoftware, gridBagConstraints);

        jComboBoxImage.setMinimumSize(new java.awt.Dimension(140, 28));
        jComboBoxImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxImageActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelAlgorithmSettings.add(jComboBoxImage, gridBagConstraints);

        jSpinnerMaxIter.setModel(new javax.swing.SpinnerNumberModel(0, -1, null, 5));
        jSpinnerMaxIter.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerMaxIterStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelAlgorithmSettings.add(jSpinnerMaxIter, gridBagConstraints);

        jComboBoxRglName.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "mem_prior" }));
        jComboBoxRglName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxRglNameActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelAlgorithmSettings.add(jComboBoxRglName, gridBagConstraints);

        jFormattedTextFieldRglWgt.setFormatterFactory(getFormatterFactory());
        jFormattedTextFieldRglWgt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextFieldActionPerformed(evt);
            }
        });
        jFormattedTextFieldRglWgt.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jFormattedTextFieldPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelAlgorithmSettings.add(jFormattedTextFieldRglWgt, gridBagConstraints);

        jFormattedTextFieldRglAlph.setFormatterFactory(getFormatterFactory());
        jFormattedTextFieldRglAlph.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextFieldActionPerformed(evt);
            }
        });
        jFormattedTextFieldRglAlph.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jFormattedTextFieldPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelAlgorithmSettings.add(jFormattedTextFieldRglAlph, gridBagConstraints);

        jFormattedTextFieldRglBeta.setFormatterFactory(getFormatterFactory());
        jFormattedTextFieldRglBeta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextFieldActionPerformed(evt);
            }
        });
        jFormattedTextFieldRglBeta.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jFormattedTextFieldPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelAlgorithmSettings.add(jFormattedTextFieldRglBeta, gridBagConstraints);

        jComboBoxRglPrio.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelAlgorithmSettings.add(jComboBoxRglPrio, gridBagConstraints);

        jButtonRemoveFitsImage.setText("-");
        jButtonRemoveFitsImage.setEnabled(false);
        jButtonRemoveFitsImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveFitsImageActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        jPanelAlgorithmSettings.add(jButtonRemoveFitsImage, gridBagConstraints);

        jButtonLoadFitsImage.setText("+");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        jPanelAlgorithmSettings.add(jButtonLoadFitsImage, gridBagConstraints);

        jCheckBoxAutoWgt.setText("AUTO");
        jCheckBoxAutoWgt.setToolTipText("Automatic regularization weight");
        jCheckBoxAutoWgt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxAutoWgtActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelAlgorithmSettings.add(jCheckBoxAutoWgt, gridBagConstraints);

        jLabelFluxErr.setText("FLUXERR");
        jLabelFluxErr.setToolTipText("Error on zero-baseline squared visibility point (used to enforce flux   normalisation)");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelAlgorithmSettings.add(jLabelFluxErr, gridBagConstraints);

        jFormattedTextFieldFluxErr.setFormatterFactory(getFormatterFactory());
        jFormattedTextFieldFluxErr.setToolTipText("Error on zero-baseline squared visibility point (used to enforce flux   normalisation)");
        jFormattedTextFieldFluxErr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFormattedTextFieldActionPerformed(evt);
            }
        });
        jFormattedTextFieldFluxErr.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jFormattedTextFieldPropertyChange(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanelAlgorithmSettings.add(jFormattedTextFieldFluxErr, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        jPanel3.add(jPanelAlgorithmSettings, gridBagConstraints);

        jPanelExecutionLog.setBorder(javax.swing.BorderFactory.createTitledBorder("Action panel"));
        jPanelExecutionLog.setPreferredSize(new java.awt.Dimension(82, 100));
        jPanelExecutionLog.setLayout(new java.awt.GridBagLayout());

        jButtonRun.setText("Run");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelExecutionLog.add(jButtonRun, gridBagConstraints);

        jEditorPane.setContentType("text/html"); // NOI18N
        jScrollPane1.setViewportView(jEditorPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelExecutionLog.add(jScrollPane1, gridBagConstraints);

        jButtonExportOIFits.setText("Save Oifits");
        jButtonExportOIFits.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        jPanelExecutionLog.add(jButtonExportOIFits, gridBagConstraints);

        jButtonExportImage.setText("Save image");
        jButtonExportImage.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        jPanelExecutionLog.add(jButtonExportImage, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.2;
        jPanel3.add(jPanelExecutionLog, gridBagConstraints);

        jPanelResults.setBorder(javax.swing.BorderFactory.createTitledBorder("Result sets"));
        jPanelResults.setLayout(new javax.swing.BoxLayout(jPanelResults, javax.swing.BoxLayout.LINE_AXIS));

        jScrollPane4.setViewportView(jListResultSet);

        jPanelResults.add(jScrollPane4);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.6;
        jPanel3.add(jPanelResults, gridBagConstraints);

        jSplitPane1.setLeftComponent(jPanel3);
        jSplitPane1.setRightComponent(viewerPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        add(jSplitPane1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxSoftwareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxSoftwareActionPerformed
        updateModel();
    }//GEN-LAST:event_jComboBoxSoftwareActionPerformed

    private void jComboBoxImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxImageActionPerformed
        updateModel();

        // avoid selection in the result list
        jListResultSet.clearSelection();

        // and display input data in the viewer part
        viewerPanel.displayModel(currentModel);
        viewerPanel.selectImageViewer();
    }//GEN-LAST:event_jComboBoxImageActionPerformed

    private void jCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxActionPerformed
        updateModel();
    }//GEN-LAST:event_jCheckBoxActionPerformed

    private void jFormattedTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextFieldActionPerformed
        updateModel();
    }//GEN-LAST:event_jFormattedTextFieldActionPerformed

    private void jFormattedTextFieldPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jFormattedTextFieldPropertyChange
        updateModel();
    }//GEN-LAST:event_jFormattedTextFieldPropertyChange

    private void jButtonRemoveFitsImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveFitsImageActionPerformed
        // TODO
    }//GEN-LAST:event_jButtonRemoveFitsImageActionPerformed

    private void jComboBoxRglNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxRglNameActionPerformed
        updateModel();
    }//GEN-LAST:event_jComboBoxRglNameActionPerformed

    private void jSpinnerMaxIterStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerMaxIterStateChanged
        updateModel();
    }//GEN-LAST:event_jSpinnerMaxIterStateChanged

    private void jCheckBoxAutoWgtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxAutoWgtActionPerformed
        updateModel();
    }//GEN-LAST:event_jCheckBoxAutoWgtActionPerformed

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
        if (e.getSource() == jListResultSet) {
            viewerPanel.displayResult((ServiceResult) jListResultSet.getSelectedValue());
        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonExportImage;
    private javax.swing.JButton jButtonExportOIFits;
    private javax.swing.JButton jButtonLoadData;
    private javax.swing.JButton jButtonLoadFitsImage;
    private javax.swing.JButton jButtonRemoveFitsImage;
    private javax.swing.JButton jButtonRun;
    private javax.swing.JCheckBox jCheckBoxAutoWgt;
    private javax.swing.JCheckBox jCheckBoxUseT3;
    private javax.swing.JCheckBox jCheckBoxUseVis;
    private javax.swing.JCheckBox jCheckBoxUseVis2;
    private javax.swing.JComboBox jComboBoxImage;
    private javax.swing.JComboBox jComboBoxRglName;
    private javax.swing.JComboBox jComboBoxRglPrio;
    private javax.swing.JComboBox jComboBoxSoftware;
    private javax.swing.JComboBox jComboBoxTarget;
    private javax.swing.JEditorPane jEditorPane;
    private javax.swing.JFormattedTextField jFormattedTextFieldFluxErr;
    private javax.swing.JFormattedTextField jFormattedTextFieldRglAlph;
    private javax.swing.JFormattedTextField jFormattedTextFieldRglBeta;
    private javax.swing.JFormattedTextField jFormattedTextFieldRglWgt;
    private javax.swing.JFormattedTextField jFormattedTextFieldWaveMax;
    private javax.swing.JFormattedTextField jFormattedTextFieldWaveMin;
    private javax.swing.JLabel jLabelFluxErr;
    private javax.swing.JLabel jLabelInitImg;
    private javax.swing.JLabel jLabelMaxIter;
    private javax.swing.JLabel jLabelOifitsFile;
    private javax.swing.JLabel jLabelRglAlph;
    private javax.swing.JLabel jLabelRglBeta;
    private javax.swing.JLabel jLabelRglName;
    private javax.swing.JLabel jLabelRglPrio;
    private javax.swing.JLabel jLabelRglWgt;
    private javax.swing.JLabel jLabelTarget;
    private javax.swing.JLabel jLabelWaveMax;
    private javax.swing.JLabel jLabelWaveMin;
    private javax.swing.JList jListResultSet;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanelAlgorithmSettings;
    private javax.swing.JPanel jPanelDataSelection;
    private javax.swing.JPanel jPanelExecutionLog;
    private javax.swing.JPanel jPanelResults;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSlider jSliderWaveMax;
    private javax.swing.JSlider jSliderWaveMin;
    private javax.swing.JSpinner jSpinnerMaxIter;
    private javax.swing.JSplitPane jSplitPane1;
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
                syncUI(event.getIrModel());
                viewerPanel.displayModel(currentModel);
                break;
            case IRMODEL_UPDATED:
                syncUI(event.getIrModel());
                jListResultSet.setSelectedIndex(0);
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
    private void updateModel() {

        if (syncingUI) {
            return;
        }

        // TODO may be a copy dedicated to this class
        IRModel irModel = IRModelManager.getInstance().getIRModel();
        currentModel = irModel;

        ImageOiInputParam params = irModel.getImageOiData().getInputParam();

        // Update if model_values != swing_values and detect change if one or more values change
        boolean changed = false;
        double mDouble, wDouble;
        String mString, wString;
        boolean mFlag, wFlag;
        int mInt, wInt;

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

        // Selected software
        final Service guiService = (Service) jComboBoxSoftware.getSelectedItem();
        final Service modelSoftware = irModel.getSelectedService();
        if (guiService != null && !guiService.equals(modelSoftware)) {
            irModel.setSelectedSoftware(guiService);
            changed = true;
        }

        // Init Image
        final FitsImageHDU mFitsImageHDU = irModel.getSelectedInputImageHDU();
        final FitsImageHDUDecorator fihd = (FitsImageHDUDecorator) this.jComboBoxImage.getSelectedItem();
        final FitsImageHDU sFitsImageHDU = fihd == null ? null : fihd.getFitsImageHDU();
        if (sFitsImageHDU != null && !(sFitsImageHDU == mFitsImageHDU)) {
            irModel.setSelectedInputImageHDU(fihd.getFitsImageHDU());
            changed = true;
        }

        // max iter
        try {
            // guarantee last user value
            jSpinnerMaxIter.commitEdit();
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(MainPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        mInt = params.getMaxiter();
        wInt = (Integer) jSpinnerMaxIter.getValue();
        if (mInt != wInt) {
            params.setMaxiter(wInt);
            changed = true;
        }

        // regularization
        mString = params.getRglName();
        if (jComboBoxRglName.getSelectedItem() != null) {
            wString = (String) jComboBoxRglName.getSelectedItem();
            if (!wString.equals(mString)) {
                params.setRglName(wString);
                changed = true;
            }
        }

        mDouble = params.getRglWgt();
        if (jFormattedTextFieldRglWgt.getValue() != null) {
            wDouble = ((Number) jFormattedTextFieldRglWgt.getValue()).doubleValue();
            if (mDouble != wDouble) {
                params.setRglWgt(wDouble);
                changed = true;
            }
        }
        mFlag = params.useAutoWgt();
        wFlag = jCheckBoxAutoWgt.isSelected();
        if (mFlag != wFlag) {
            params.useAutoWgt(wFlag);
            changed = true;
        }
        jFormattedTextFieldRglWgt.setEnabled(!wFlag);
        jLabelRglWgt.setEnabled(!wFlag);

        mDouble = params.getRglAlph();
        if (jFormattedTextFieldRglAlph.getValue() != null) {
            wDouble = ((Number) jFormattedTextFieldRglAlph.getValue()).doubleValue();
            if (mDouble != wDouble) {
                params.setRglAlph(wDouble);
                changed = true;
            }
        }

        mDouble = params.getRglBeta();
        if (jFormattedTextFieldRglBeta.getValue() != null) {
            wDouble = ((Number) jFormattedTextFieldRglBeta.getValue()).doubleValue();
            if (mDouble != wDouble) {
                params.setRglBeta(wDouble);
                changed = true;
            }
        }

        mString = params.getRglPrio();
        if (jComboBoxRglPrio.getSelectedItem() != null) {
            wString = (String) jComboBoxRglPrio.getSelectedItem();
            if (!wString.equals(mString)) {
                params.setRglPrio(wString);
                changed = true;
            }
        }

        mDouble = params.getFluxErr();
        if (jFormattedTextFieldFluxErr.getValue() != null) {
            wDouble = ((Number) jFormattedTextFieldFluxErr.getValue()).doubleValue();
            if (mDouble != wDouble) {
                params.setFluxErr(wDouble);
                changed = true;
            }
        }

        // some values have changed
        if (changed) {
            // notify to other listener - if any in the future

            StatusBar.show("GUI updated");
            IRModelManager.getInstance().fireIRModelChanged(this, null);
        }

    }

    /**
     * Update swing widgets with content of given irModel.
     * @param irModel master model to synchronize with.
     */
    private void syncUI(IRModel irModel) {
        syncingUI = true;
        currentModel = irModel;

        ImageOiInputParam inputParam = irModel.getImageOiData().getInputParam();

        // associate target list
        jComboBoxTarget.setModel(irModel.getTargetListModel());
        Object sel = inputParam.getTarget();
        jComboBoxTarget.setSelectedItem(sel);

        boolean hasOiFits = currentModel.getOifitsFile() != null;

        // TODO Update OIFitsViewer:
        // arrange target
        jComboBoxTarget.setEnabled(hasOiFits);

        // arrange wavelength filters
        jSliderWaveMin.setEnabled(hasOiFits);
        jSliderWaveMax.setEnabled(hasOiFits);

        fieldSliderAdapterWaveMin.reset(irModel.getMinWavelentghBound() / MICRO_METER, irModel.getMaxWavelentghBound() / MICRO_METER, inputParam.getWaveMin() / MICRO_METER);
        fieldSliderAdapterWaveMax.reset(irModel.getMinWavelentghBound() / MICRO_METER, irModel.getMaxWavelentghBound() / MICRO_METER, inputParam.getWaveMax() / MICRO_METER);

        jFormattedTextFieldWaveMin.setEnabled(hasOiFits);
        jFormattedTextFieldWaveMax.setEnabled(hasOiFits);
        jFormattedTextFieldWaveMin.setValue(inputParam.getWaveMin() / MICRO_METER);
        jFormattedTextFieldWaveMax.setValue(inputParam.getWaveMax() / MICRO_METER);

        // arrange observable checkboxes
        jCheckBoxUseVis.setEnabled(hasOiFits && irModel.getOifitsFile().hasOiVis());
        jCheckBoxUseVis2.setEnabled(hasOiFits && irModel.getOifitsFile().hasOiVis2());
        jCheckBoxUseT3.setEnabled(hasOiFits && irModel.getOifitsFile().hasOiT3());
        jCheckBoxUseVis.setSelected(hasOiFits && inputParam.useVis());
        jCheckBoxUseVis2.setSelected(hasOiFits && inputParam.useVis2());
        jCheckBoxUseT3.setSelected(hasOiFits && inputParam.useT3());

        // image combo
        jComboBoxImage.removeAllItems();
        for (FitsImageHDU fitsImageHDU : irModel.getFitsImageHDUs()) {
            FitsImageHDUDecorator d = new FitsImageHDUDecorator(fitsImageHDU);
            jComboBoxImage.addItem(d);
        }

        // resultSet List
        resultSetListModel.clear();
        resultSetListModel.add(irModel.getResultSets());
        jListResultSet.setModel(resultSetListModel);

        // max iter
        jSpinnerMaxIter.setValue(new Integer(inputParam.getMaxiter()));

        // regulation
        String rglName = inputParam.getRglName();
        if (rglName != null) {
            jComboBoxRglName.setSelectedItem(rglName);
        }
        jFormattedTextFieldRglWgt.setValue(inputParam.getRglWgt());
        jFormattedTextFieldRglAlph.setValue(inputParam.getRglAlph());
        jFormattedTextFieldRglBeta.setValue(inputParam.getRglBeta());

        String rglPrio = inputParam.getRglPrio();
        if (rglPrio != null) {
            jComboBoxRglPrio.setSelectedItem(rglPrio);
        }

        boolean wFlag = inputParam.useAutoWgt();
        jCheckBoxAutoWgt.setSelected(wFlag);
        jFormattedTextFieldRglWgt.setEnabled(!wFlag);
        jLabelRglWgt.setEnabled(!wFlag);

        double fluxerr = inputParam.getFluxErr();
        jFormattedTextFieldFluxErr.setValue(inputParam.getFluxErr());

        // perform analysis:
        List<String> failures = new LinkedList<String>();
        failures.clear();

        if (fluxerr < 1e-5) {
            failures.add("FluxErr must be greater than 1e-5");
        } else if (fluxerr > 1) {
            failures.add("FluxErr must be smaller than 1");
        }

        if (irModel.getSelectedService() == null) {
            jComboBoxSoftware.setSelectedItem(ServiceList.getPreferedService());
            irModel.setSelectedSoftware(ServiceList.getPreferedService());
            //failures.add("Please select the algorithm you want to run");
        }

        OIFitsFile oifitsFile = irModel.getOifitsFile();
        jLabelOifitsFile.setText(oifitsFile == null ? "" : oifitsFile.getName());
        if (oifitsFile == null) {
            failures.add("Missing OIFits");
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

        FitsImageHDU selectedFitsImageHDU = irModel.getSelectedInputImageHDU();
        if (selectedFitsImageHDU != null) {
            FitsImageHDUDecorator fihd = new FitsImageHDUDecorator(selectedFitsImageHDU);
            jComboBoxImage.getModel().setSelectedItem(fihd);
        } else {
            failures.add(irModel.getelectedInputFitsImageError());
        }

        // if nothing is wrong allow related actions
        final boolean modelOk = failures.size() == 0;
        runAction.setEnabled(modelOk);

        StringBuffer sb = new StringBuffer(200);
        for (String fail : failures) {
            sb.append("<li><font color=red>").append(fail).append("</font></li>");
        }

        if (failures.size() == 0) {
            sb.append("<li><font color=green>Ready to spawn process</font></li>");
        }

        jEditorPane.setText("<html><ul>" + sb.toString() + "</ul></html>");

        syncingUI = false;
    }

    public ViewerPanel getViewerPanel() {
        return viewerPanel;
    }

    private JFormattedTextField.AbstractFormatterFactory getFormatterFactory() {
        return new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.#####")));
    }

    // Make it generic
    private class FitsImageHDUDecorator {

        private final FitsImageHDU fitsImageHDU;
        private final String label;

        public FitsImageHDUDecorator(FitsImageHDU fitsImageHDU) {
            this.fitsImageHDU = fitsImageHDU;
            this.label = fitsImageHDU.getHduName();
        }

        public FitsImageHDU getFitsImageHDU() {
            return fitsImageHDU;
        }

        @Override
        public String toString() {
            return label;
        }
    };

    public class ServiceResultCellRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(
                    list, value, index,
                    isSelected, cellHasFocus);
            ServiceResult serviceResult = (ServiceResult) value;

            setText("Run " + (serviceResult.isValid() ? "ok" : "ko") + " @ " + serviceResult.getStartTime());

            if (!serviceResult.isValid()) {
                setForeground(Color.RED);
            }
            return this;
        }
    }

}
