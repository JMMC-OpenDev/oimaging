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
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main container of OIFits Explorer App
 *
 * @author mella
 */
public class MainPanel extends javax.swing.JPanel implements IRModelEventListener, ListSelectionListener, ChangeListener {

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

    /** Image HDU list model */
    GenericListModel<String> hduNameListModel = new GenericListModel<String>(hduNameList);

    private final List<ServiceResult> resultSetList = new ArrayList<ServiceResult>(5);

    /** ResultSet list model */
    GenericListModel<ServiceResult> resultSetListModel = new GenericListModel<ServiceResult>(resultSetList);

    /* members */
    /** Load fits image action */
    private Action loadFitsImageAction;

    /** Run action */
    private RunAction runAction;

    /** Flag set to true while the GUI is being updated by model else false. */
    private boolean syncingUI = false;

    FieldSliderAdapter fieldSliderAdapterWaveMin;
    FieldSliderAdapter fieldSliderAdapterWaveMax;
    /** timeline refresh Swing timer */
    private final Timer timerMouseCursorRefresh;

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

        jLabelWaveMin.setText("WAVE_MIN [" + SpecialChars.UNIT_MICRO_METER + "]");
        jLabelWaveMax.setText("WAVE_MAX [" + SpecialChars.UNIT_MICRO_METER + "]");

        // become widget listener
        jListImageHDUs.addListSelectionListener((ListSelectionListener) this);
        jSpinnerMaxIter.addChangeListener(this);

        // associate sliders and fields
        fieldSliderAdapterWaveMin = new FieldSliderAdapter(jSliderWaveMin, jFormattedTextFieldWaveMin, 0, 1, 0);
        fieldSliderAdapterWaveMax = new FieldSliderAdapter(jSliderWaveMax, jFormattedTextFieldWaveMax, 0, 1, 0);

        fieldSliderAdapterWaveMin.addChangeListener(this);
        fieldSliderAdapterWaveMax.addChangeListener(this);

        jListResultSet.addListSelectionListener((ListSelectionListener) this);

    }

    /**
     * Create the main actions and/or present in the toolbar
     */
    private void registerActions() {
        // Build toolBar
        jButtonLoadData.setAction(ActionRegistrar.getInstance().get(LoadOIFitsAction.className, LoadOIFitsAction.actionName));

        loadFitsImageAction = ActionRegistrar.getInstance().get(LoadFitsImageAction.className, LoadFitsImageAction.actionName);
        jButtonLoadFitsImage.setAction(loadFitsImageAction);
        jButtonLoadFitsImage.setText((String) loadFitsImageAction.getValue(Action.SHORT_DESCRIPTION));

        runAction = (RunAction) ActionRegistrar.getInstance().get(RunAction.className, RunAction.actionName);
        jButtonRun.setAction(runAction);
        //  TODO fix next call      jButtonRun.setText((String) runAction.getValue(Action.SHORT_DESCRIPTION));
        jButtonRun.setText("Run");

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

        jPanelDataSelection = new javax.swing.JPanel();
        jLabelTarget = new javax.swing.JLabel();
        jComboBoxTarget = new javax.swing.JComboBox();
        jButtonLoadData = new javax.swing.JButton();
        jLabelOifitsFile = new javax.swing.JLabel();
        jLabelWaveMin = new javax.swing.JLabel();
        jLabelWaveMax = new javax.swing.JLabel();
        jCheckBoxUseVis = new javax.swing.JCheckBox();
        jCheckBoxUseVis2 = new javax.swing.JCheckBox();
        jCheckBoxUseT3 = new javax.swing.JCheckBox();
        jSliderWaveMin = new javax.swing.JSlider();
        jSliderWaveMax = new javax.swing.JSlider();
        jFormattedTextFieldWaveMin = new javax.swing.JFormattedTextField();
        jFormattedTextFieldWaveMax = new javax.swing.JFormattedTextField();
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
        jPanelExecutionLog = new javax.swing.JPanel();
        jButtonRun = new javax.swing.JButton();
        jButtonExport = new javax.swing.JButton();
        jButtonStop = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane = new javax.swing.JEditorPane();
        jPanelResults = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jListResultSet = createCustomList();
        jPanelInputImages = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListImageHDUs = createCustomList();
        jButtonLoadFitsImage = new javax.swing.JButton();
        jButtonRemoveFitsImage = new javax.swing.JButton();
        viewerPanel = new fr.jmmc.oimaging.gui.ViewerPanel();

        setMaximumSize(new java.awt.Dimension(100, 100));
        setLayout(new java.awt.GridBagLayout());

        jPanelDataSelection.setBorder(javax.swing.BorderFactory.createTitledBorder("Data selection"));
        jPanelDataSelection.setLayout(new java.awt.GridBagLayout());

        jLabelTarget.setText("TARGET");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        jPanelDataSelection.add(jLabelTarget, gridBagConstraints);

        jComboBoxTarget.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelDataSelection.add(jComboBoxTarget, gridBagConstraints);

        jButtonLoadData.setText("load oifits");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        jPanelDataSelection.add(jButtonLoadData, gridBagConstraints);

        jLabelOifitsFile.setText("oifits Label");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        jPanelDataSelection.add(jLabelOifitsFile, gridBagConstraints);

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

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jPanelDataSelection, gridBagConstraints);

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
        gridBagConstraints.gridwidth = 2;
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
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelAlgorithmSettings.add(jComboBoxRglName, gridBagConstraints);

        jFormattedTextFieldRglWgt.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter()));
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
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelAlgorithmSettings.add(jFormattedTextFieldRglWgt, gridBagConstraints);

        jFormattedTextFieldRglAlph.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter()));
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
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelAlgorithmSettings.add(jFormattedTextFieldRglAlph, gridBagConstraints);

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
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelAlgorithmSettings.add(jFormattedTextFieldRglBeta, gridBagConstraints);

        jComboBoxRglPrio.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelAlgorithmSettings.add(jComboBoxRglPrio, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jPanelAlgorithmSettings, gridBagConstraints);

        jPanelExecutionLog.setBorder(javax.swing.BorderFactory.createTitledBorder("Execution log"));
        jPanelExecutionLog.setPreferredSize(new java.awt.Dimension(82, 100));
        jPanelExecutionLog.setLayout(new java.awt.GridBagLayout());

        jButtonRun.setText("Run");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelExecutionLog.add(jButtonRun, gridBagConstraints);

        jButtonExport.setText("Export");
        jButtonExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExportActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelExecutionLog.add(jButtonExport, gridBagConstraints);

        jButtonStop.setText("Stop");
        jButtonStop.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelExecutionLog.add(jButtonStop, gridBagConstraints);

        jEditorPane.setContentType("text/html"); // NOI18N
        jScrollPane1.setViewportView(jEditorPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        jPanelExecutionLog.add(jScrollPane1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(jPanelExecutionLog, gridBagConstraints);

        jPanelResults.setBorder(javax.swing.BorderFactory.createTitledBorder("Result sets"));
        jPanelResults.setLayout(new java.awt.BorderLayout());

        jScrollPane4.setViewportView(jListResultSet);

        jPanelResults.add(jScrollPane4, java.awt.BorderLayout.PAGE_START);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        add(jPanelResults, gridBagConstraints);

        jPanelInputImages.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("Input Images")));
        jPanelInputImages.setLayout(new java.awt.GridBagLayout());

        jScrollPane2.setPreferredSize(new java.awt.Dimension(25, 100));

        jListImageHDUs.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListImageHDUs.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jListImageHDUsMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(jListImageHDUs);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        jPanelInputImages.add(jScrollPane2, gridBagConstraints);

        jButtonLoadFitsImage.setText("load fits");
        jPanelInputImages.add(jButtonLoadFitsImage, new java.awt.GridBagConstraints());

        jButtonRemoveFitsImage.setText("-");
        jButtonRemoveFitsImage.setEnabled(false);
        jButtonRemoveFitsImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveFitsImageActionPerformed(evt);
            }
        });
        jPanelInputImages.add(jButtonRemoveFitsImage, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 0.2;
        add(jPanelInputImages, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.1;
        add(viewerPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxSoftwareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxSoftwareActionPerformed
        updateModel();
    }//GEN-LAST:event_jComboBoxSoftwareActionPerformed

    private void jComboBoxImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxImageActionPerformed
        updateModel();
    }//GEN-LAST:event_jComboBoxImageActionPerformed

    private void jCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxActionPerformed
        updateModel();
    }//GEN-LAST:event_jCheckBoxActionPerformed

    private void jFormattedTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormattedTextFieldActionPerformed
        updateModel();
    }//GEN-LAST:event_jFormattedTextFieldActionPerformed

    private void jListImageHDUsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jListImageHDUsMouseClicked
        // load new data if single count and empty or double click
        if ((jListImageHDUs.getSelectedValue() == null && jListImageHDUs.getModel().getSize() == 0) || evt.getClickCount() == 2) {
            loadFitsImageAction.actionPerformed(null);
        }
    }//GEN-LAST:event_jListImageHDUsMouseClicked

    private void jFormattedTextFieldPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jFormattedTextFieldPropertyChange
        updateModel();
    }//GEN-LAST:event_jFormattedTextFieldPropertyChange

    private void jButtonExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExportActionPerformed
        IRModelManager.getInstance().getIRModel().exportOIFits();
    }//GEN-LAST:event_jButtonExportActionPerformed

    private void jButtonRemoveFitsImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveFitsImageActionPerformed
        // TODO
    }//GEN-LAST:event_jButtonRemoveFitsImageActionPerformed

    private void jComboBoxRglNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxRglNameActionPerformed
        updateModel();
    }//GEN-LAST:event_jComboBoxRglNameActionPerformed

    private void jSpinnerMaxIterStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerMaxIterStateChanged
        updateModel();
    }//GEN-LAST:event_jSpinnerMaxIterStateChanged

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
        if (e.getSource() == jListImageHDUs) {
            FitsImageHDUDecorator fitsImageHDUDecorator = (FitsImageHDUDecorator) jListImageHDUs.getSelectedValue();
            if (fitsImageHDUDecorator != null) {
                viewerPanel.displayImage(fitsImageHDUDecorator.getFitsImageHDU().getFitsImages().get(0));
            } else {
                viewerPanel.displayImage(null);
            }
        }

        if (e.getSource() == jListResultSet) {
            ServiceResultDecorator serviceResultDecorator = jListResultSet.getSelectedValue();

            viewerPanel.displayResult(serviceResultDecorator.getServiceResult());
        }

    }

    @Override
    public void stateChanged(ChangeEvent e) {
        //TODO implement for spinner changes..
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonExport;
    private javax.swing.JButton jButtonLoadData;
    private javax.swing.JButton jButtonLoadFitsImage;
    private javax.swing.JButton jButtonRemoveFitsImage;
    private javax.swing.JButton jButtonRun;
    private javax.swing.JButton jButtonStop;
    private javax.swing.JCheckBox jCheckBoxUseT3;
    private javax.swing.JCheckBox jCheckBoxUseVis;
    private javax.swing.JCheckBox jCheckBoxUseVis2;
    private javax.swing.JComboBox jComboBoxImage;
    private javax.swing.JComboBox jComboBoxRglName;
    private javax.swing.JComboBox jComboBoxRglPrio;
    private javax.swing.JComboBox jComboBoxSoftware;
    private javax.swing.JComboBox jComboBoxTarget;
    private javax.swing.JEditorPane jEditorPane;
    private javax.swing.JFormattedTextField jFormattedTextFieldRglAlph;
    private javax.swing.JFormattedTextField jFormattedTextFieldRglBeta;
    private javax.swing.JFormattedTextField jFormattedTextFieldRglWgt;
    private javax.swing.JFormattedTextField jFormattedTextFieldWaveMax;
    private javax.swing.JFormattedTextField jFormattedTextFieldWaveMin;
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
    private javax.swing.JList jListImageHDUs;
    private javax.swing.JList<ServiceResultDecorator> jListResultSet;
    private javax.swing.JPanel jPanelAlgorithmSettings;
    private javax.swing.JPanel jPanelDataSelection;
    private javax.swing.JPanel jPanelExecutionLog;
    private javax.swing.JPanel jPanelInputImages;
    private javax.swing.JPanel jPanelResults;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSlider jSliderWaveMax;
    private javax.swing.JSlider jSliderWaveMin;
    private javax.swing.JSpinner jSpinnerMaxIter;
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
                break;
            case IRMODEL_UPDATED:
                syncUI(event.getIrModel());
                jListResultSet.setSelectedIndex(jListResultSet.getModel().getSize() - 1);

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

        ImageOiInputParam inputParam = irModel.getImageOiData().getInputParam();

        // associate target list
        jComboBoxTarget.setModel(irModel.getTargetListModel());
        Object sel = inputParam.getTarget();
        jComboBoxTarget.setSelectedItem(sel);

        boolean hasOiFits = irModel.getOifitsFile() != null;

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
        hduNameListModel.clear();
        for (FitsImageHDU fitsImageHDU : irModel.getFitsImageHDUs()) {
            FitsImageHDUDecorator d = new FitsImageHDUDecorator(fitsImageHDU);
            jComboBoxImage.addItem(d);
            hduNameListModel.addElement(d);
        }
        jListImageHDUs.setModel(hduNameListModel);

        // resultSet List
        resultSetListModel.clear();
        for (ServiceResult result : irModel.getResultSets()) {
            ServiceResultDecorator d = new ServiceResultDecorator(result);
            jComboBoxImage.addItem(d);
            resultSetListModel.addElement(d);
        }
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

        // perform analysis:
        List<String> failures = new LinkedList<String>();
        failures.clear();

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
            // TODO choose fits image
            if (jListImageHDUs.getSelectedIndex() == -1) {
                jListImageHDUs.setSelectedIndex(jComboBoxImage.getSelectedIndex());
                viewerPanel.displayImage(selectedFitsImageHDU.getFitsImages().get(0));
            }
        } else {
            failures.add(irModel.getelectedInputFitsImageError());
        }

        // if nothing is wrong allow related actions
        final boolean modelOk = failures.size() == 0;
        runAction.setEnabled(modelOk);
        jButtonExport.setEnabled(modelOk);

        StringBuffer sb = new StringBuffer(200);
        for (String fail : failures) {
            sb.append("<li><font color=red>").append(fail).append("</font></li>");
        }

        if (failures.size() == 0) {
            sb.append("<li><font color=green>Ready to spawn process</font></li>");
        }

        // Add TODO:
        sb.append("</ul><br>TODO:<ul>");
        sb.append("<li>Handle IMAGE-OI OUPUT PARAM result table</li>");
        sb.append("<li>Provide a button to display execution logs not only for bad cases</li>");

        jEditorPane.setText("<html><ul>" + sb.toString() + "</ul></html>");

        syncingUI = false;
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

    // Make it generic
    private class ServiceResultDecorator {

        private final ServiceResult serviceResult;
        private final String label;

        public ServiceResultDecorator(ServiceResult serviceResult) {
            this.serviceResult = serviceResult;
            this.label = "Run @ " + serviceResult.getStartTime();
        }

        public ServiceResult getServiceResult() {
            return serviceResult;
        }

        @Override
        public String toString() {
            return label;
        }
    };

}
