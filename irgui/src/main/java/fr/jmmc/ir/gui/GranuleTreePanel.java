/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.gui;

import fr.jmmc.jmal.ALX;
import fr.jmmc.jmcs.gui.component.GenericJTree;
import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.oiexplorer.core.model.InstrumentModeManager;
import fr.jmmc.oiexplorer.core.model.OIFitsCollection;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEvent;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventListener;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventType;
import fr.jmmc.oiexplorer.core.model.TargetManager;
import fr.jmmc.oiexplorer.core.model.util.GranuleComparator;
import fr.jmmc.oiexplorer.core.model.util.MJDConverter;
import fr.jmmc.oiexplorer.core.model.util.OITableByFileComparator;
import fr.jmmc.oitools.model.Granule;
import fr.jmmc.oitools.model.Granule.GranuleField;
import fr.jmmc.oitools.model.InstrumentMode;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OITable;
import fr.jmmc.oitools.model.Target;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This panel contains the data tree arranged by granules
 * 
 * // TODO: support multiple table selections
 * 
 * @author bourgesl, mellag
 */
public final class GranuleTreePanel extends javax.swing.JPanel implements OIFitsCollectionManagerEventListener {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(GranuleTreePanel.class);

    /* constants */
    /** Show root of granule tree */
    private static final boolean SHOW_DATATREE_ROOTVISIBLE = false;

    /* members */
    /** OIFitsCollectionManager singleton */
    private final OIFitsCollectionManager ocm = OIFitsCollectionManager.getInstance();
    /** Swing data tree */
    private GenericJTree<Object> dataTree;
    /** temporary buffer */
    private final StringBuilder tmpBuf = new StringBuilder(64);

    /** Creates new GranuleTreePanel */
    public GranuleTreePanel() {
        // always bind at the beginning of the constructor (to maintain correct ordering):
        ocm.bindCollectionChangedEvent(this);

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
     * This method is useful to set the models and specific features of initialized swing components :
     */
    private void postInit() {

        // dataTree contains Target/InstrumentMode/Integer or OITable objects:
        dataTree = new GenericJTree<Object>(null) {
            /** default serial UID for Serializable interface */
            private static final long serialVersionUID = 1;

            @Override
            protected String convertUserObjectToString(final Object userObject) {
                if (userObject instanceof Target) {
                    // target name
                    return ((Target) userObject).getTarget();
                }
                if (userObject instanceof InstrumentMode) {
                    // instrument name
                    return ((InstrumentMode) userObject).getInsName();
                }
                if (userObject instanceof Integer) {
                    // nightId
                    final Integer nightId = (Integer) userObject;
                    // convert as date:
                    return MJDConverter.mjdToString(nightId);
                }
                if (userObject instanceof OITable) {
                    return getDisplayLabel((OITable) userObject, tmpBuf);
                }
                if (userObject instanceof StatisticatedObject) {
                    // use converter of mainObject
                    return convertUserObjectToString(((StatisticatedObject) userObject).getMainObject());
                }
                return toString(userObject);
            }
        };

        ToolTipManager.sharedInstance().registerComponent(dataTree);

        dataTree.setCellRenderer(new TooltipTreeCellRenderer());

        dataTree.setRootVisible(SHOW_DATATREE_ROOTVISIBLE);

        // Define root node once:
        final DefaultMutableTreeNode rootNode = dataTree.getRootNode();
        rootNode.setUserObject("Granules");

        genericTreePanel.add(dataTree);

        jTableCols.getColumnModel().addColumnModelListener(new TableColumnModelListener() {
            @Override
            public void columnAdded(TableColumnModelEvent e) {
            }

            @Override
            public void columnRemoved(TableColumnModelEvent e) {
            }

            @Override
            public void columnMoved(TableColumnModelEvent e) {
                if (e.getFromIndex() != e.getToIndex()) {
                    logger.info("column changed");
                    updateOIFitsCollection();
                }
            }

            @Override
            public void columnMarginChanged(ChangeEvent e) {
            }

            @Override
            public void columnSelectionChanged(ListSelectionEvent e) {
            }
        });

        jTableCols.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                logger.info("tableChanged");
                updateOIFitsCollection();
            }
        });

        // fire node structure changed :
        dataTree.fireNodeChanged(rootNode);
    }

    /**
     * Update the data tree if any swing widget changes
     */
    private void updateOIFitsCollection() {
        updateOIFitsCollection(ocm.getOIFitsCollection());
    }

    /**
     * Update the data tree
     * @param oiFitsCollection OIFitsCollection to process
     */
    private void updateOIFitsCollection(final OIFitsCollection oiFitsCollection) {
        generateTree(oiFitsCollection);
    }

    /**
     * Generate the tree from the current edited list of targets
     * @param oiFitsCollection OIFitsCollection to process
     */
    private void generateTree(final OIFitsCollection oiFitsCollection) {

        final boolean showFile = this.jRadioButtonFile.isSelected();
        final boolean showOITable = this.jRadioButtonOITable.isSelected();

        final TableColumnModel tcm = jTableCols.getColumnModel();
        final TableModel tm = jTableCols.getModel();

        final List<GranuleField> selectedFields = new ArrayList<GranuleField>(4);
        for (int i = 0, len = tcm.getColumnCount(); i < len; i++) {
            final TableColumn col = tcm.getColumn(i);
            final Boolean selected = (Boolean) tm.getValueAt(0, col.getModelIndex());
            // TODO check if none is selected.
            if (selected) {
                String colName = (String) tcm.getColumn(i).getHeaderValue();
                logger.info("column: {}", colName);

                if ("Target".equals(colName)) {
                    selectedFields.add(GranuleField.TARGET);
                } else if ("Ins. mode".equals(colName)) {
                    selectedFields.add(GranuleField.INS_MODE);
                } else if ("Night".equals(colName)) {
                    selectedFields.add(GranuleField.NIGHT);
                } else {
                    System.out.println("unsupported col: " + colName);
                }
            }
        }

        // Sort granule by criteria (target / insMode / night):
        final GranuleComparator comparator = (selectedFields.isEmpty()) ? GranuleComparator.DEFAULT
                : new GranuleComparator(selectedFields);

        // Reset root content of datatree:
        final DefaultMutableTreeNode rootNode = dataTree.getRootNode();
        rootNode.removeAllChildren();

        final Map<Granule, OIFitsFile> oiFitsPerGranule = oiFitsCollection.getOiFitsPerGranule();

        final List<Granule> granules = new ArrayList<Granule>(oiFitsPerGranule.size());
        granules.addAll(oiFitsPerGranule.keySet());
        Collections.sort(granules, comparator);

        logger.debug("granules sorted: {}", granules);

        // Add nodes and their data tables:
        final List<GranuleField> fields = comparator.getSortDirectives();
        final int fieldsLen = fields.size();

        final DefaultMutableTreeNode[] pathNodes = new DefaultMutableTreeNode[fieldsLen + 1];
        int level;
        GranuleField field;
        Object value, other;

        pathNodes[0] = rootNode;

        for (Granule granule : granules) {

            // loop on fields:

            for (level = 1; level <= fieldsLen; level++) {
                field = fields.get(level - 1);
                value = granule.getField(field);

                DefaultMutableTreeNode prevNode = pathNodes[level];
                if (prevNode != null) {
                    // compare ?
                    other = prevNode.getUserObject();

// note: equals uses custom implementation in Target / InstrumentMode / Integer (all members are equals)
                    // equals method must be called on other to support proxy object (value.equals(other) may be different)
                    if (other.equals(value)) {
                        continue;
                    } else {
                        // different:
                        for (int i = level + 1; i <= fieldsLen; i++) {
                            // clear previous nodes:
                            pathNodes[i] = null;
                        }
                    }
                }

                // insert original granule value if we are on the deepest level (no file and no table)
                // or use a proxy object enriched by material that need to be used by tooltip (and probably more in the future)
                if (level < fieldsLen || showFile || showOITable) {
                    pathNodes[level] = dataTree.addNode(pathNodes[level - 1], value);
                } else {
                    StatisticatedObject sobject = new StatisticatedObject(value);
                    pathNodes[level] = dataTree.addNode(pathNodes[level - 1], sobject);                    
                    // reference on table will be appent below
                    // we could add more information comming from current granule
                }
            }

            final DefaultMutableTreeNode parent = pathNodes[level - 1];

            // if parent store a proxy object, add reference on granule
            if (parent.getUserObject() instanceof StatisticatedObject) {
                ((StatisticatedObject) parent.getUserObject()).addGranule(granule);
            }

            // Leaf:
            final OIFitsFile dataForGranule = oiFitsPerGranule.get(granule);
            if (dataForGranule != null) {                
                if (showFile) {
                    // insert node per OIFits File:
                    final List<OIData> sortedByFile = new ArrayList<OIData>(dataForGranule.getOiDataList());
                    Collections.sort(sortedByFile, OITableByFileComparator.INSTANCE);

                    DefaultMutableTreeNode current = parent;

                    String fileName, prev = null;

                    // for now per OIData:
                    for (OITable table : sortedByFile) {
                        fileName = OITableByFileComparator.getFileName(table);

                        if (!fileName.equals(prev)) {
                            prev = fileName;
                            if (showOITable) {
                                current = dataTree.addNode(parent, fileName);
                            } else {
                                StatisticatedObject sobject = new StatisticatedObject(fileName);
                                current = dataTree.addNode(parent, sobject);
                                sobject.addGranule(granule);
                            }
                        }
                        if (showOITable) {
                            dataTree.addNode(current, table);
                        } else {
                            //add reference on table + other stat info into userObject of current
                            StatisticatedObject sobject = (StatisticatedObject) current.getUserObject();
                            sobject.addOITable(table);
                        }
                    }
                } else {
                    if (showOITable) {
                        // for now per OIData:
                        for (OITable table : dataForGranule.getOiDataList()) {
                            dataTree.addNode(parent, table);
                        }
                    } else {
                        // add reference on table + other stat info into userObject of parent
                        StatisticatedObject sobject = (StatisticatedObject) parent.getUserObject();
                        for (OITable table : dataForGranule.getOiDataList()) {
                            sobject.addOITable(table);
                        }
                    }
                }
            }
        }

        jLabelStats.setText(granules.size() + " granules, " + oiFitsCollection.getOIFitsFiles().size() + " oifits");        

        // fire node structure changed :
        dataTree.fireNodeChanged(rootNode);

        if (jToggleButtonExpandTree.isSelected()) {
            dataTree.expandAll(true);
        } else if (jToggleButtonCollapseTree.isSelected()) {
            dataTree.expandAll(false);
        }
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

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jRadioButtonFile = new javax.swing.JRadioButton();
        jRadioButtonOITable = new javax.swing.JRadioButton();
        jScrollPane = new javax.swing.JScrollPane();
        genericTreePanel = new javax.swing.JPanel();
        jPanelTable = new javax.swing.JPanel();
        jScrollPaneTable = new javax.swing.JScrollPane();
        jTableCols = new javax.swing.JTable();
        jLabelStats = new javax.swing.JLabel();
        jToggleButtonExpandTree = new javax.swing.JToggleButton();
        jToggleButtonCollapseTree = new javax.swing.JToggleButton();

        setLayout(new java.awt.GridBagLayout());

        jRadioButtonFile.setText("Files");
        jRadioButtonFile.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jRadioButtonFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonFileActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(jRadioButtonFile, gridBagConstraints);

        jRadioButtonOITable.setText("Tables");
        jRadioButtonOITable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonOITableActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(jRadioButtonOITable, gridBagConstraints);

        genericTreePanel.setLayout(new java.awt.BorderLayout());
        jScrollPane.setViewportView(genericTreePanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane, gridBagConstraints);

        jPanelTable.setPreferredSize(new java.awt.Dimension(452, 12));
        jPanelTable.setLayout(new java.awt.BorderLayout());

        jScrollPaneTable.setPreferredSize(null);

        jTableCols.setAutoCreateRowSorter(true);
        jTableCols.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                { new Boolean(true),  new Boolean(true),  new Boolean(true)}
            },
            new String [] {
                "Target", "Ins. mode", "Night"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.Boolean.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jTableCols.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        jTableCols.setAutoscrolls(false);
        jTableCols.setMaximumSize(new java.awt.Dimension(225, 18));
        jTableCols.setRowSelectionAllowed(false);
        jTableCols.setShowVerticalLines(false);
        jScrollPaneTable.setViewportView(jTableCols);

        jPanelTable.add(jScrollPaneTable, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        add(jPanelTable, gridBagConstraints);

        jLabelStats.setText("jLabel1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabelStats, gridBagConstraints);

        jToggleButtonExpandTree.setSelected(true);
        jToggleButtonExpandTree.setText("Expand");
        jToggleButtonExpandTree.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonExpandTreeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(jToggleButtonExpandTree, gridBagConstraints);

        jToggleButtonCollapseTree.setText("Collapse");
        jToggleButtonCollapseTree.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButtonCollapseTreeActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jToggleButtonCollapseTree, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void jRadioButtonFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonFileActionPerformed
        logger.info("jRadioButtonFileActionPerformed");
        updateOIFitsCollection();
    }//GEN-LAST:event_jRadioButtonFileActionPerformed

    private void jRadioButtonOITableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonOITableActionPerformed
        logger.info("jRadioButtonOITableActionPerformed");
        updateOIFitsCollection();
    }//GEN-LAST:event_jRadioButtonOITableActionPerformed

    private void jToggleButtonExpandTreeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonExpandTreeActionPerformed
        if (jToggleButtonExpandTree.isSelected()) {
            dataTree.expandAll(true);
            jToggleButtonCollapseTree.setSelected(false);
        }
    }//GEN-LAST:event_jToggleButtonExpandTreeActionPerformed

    private void jToggleButtonCollapseTreeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButtonCollapseTreeActionPerformed
        if(jToggleButtonCollapseTree.isSelected()){
            dataTree.expandAll(false);
            jToggleButtonExpandTree.setSelected(false);
        }
    }//GEN-LAST:event_jToggleButtonCollapseTreeActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel genericTreePanel;
    private javax.swing.JLabel jLabelStats;
    private javax.swing.JPanel jPanelTable;
    private javax.swing.JRadioButton jRadioButtonFile;
    private javax.swing.JRadioButton jRadioButtonOITable;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JScrollPane jScrollPaneTable;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTableCols;
    private javax.swing.JToggleButton jToggleButtonCollapseTree;
    private javax.swing.JToggleButton jToggleButtonExpandTree;
    // End of variables declaration//GEN-END:variables

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
        // accept all
        return null;
    }

    /**
     * Handle the given OIFits collection event
     * @param event OIFits collection event
     */
    @Override
    public void onProcess(final OIFitsCollectionManagerEvent event) {
        logger.debug("onProcess {}", event);

        switch (event.getType()) {
            case COLLECTION_CHANGED:
                updateOIFitsCollection(event.getOIFitsCollection());
                break;
            default:
        }
        logger.debug("onProcess {} - done", event);
    }

    /**
     * Return the label displayed in the data tree
     * @param table OITable to display
     * @param sb temporary buffer
     * @return label
     */
    private static String getDisplayLabel(final OITable table, final StringBuilder sb) {
        if (table instanceof OIData) {
            final OIData oiData = (OIData) table;
            sb.setLength(0);
            sb.append(table.getExtName());
            sb.append('#');
            sb.append(table.getExtNb());
            final String dateObs = oiData.getDateObs();
            if (!StringUtils.isEmpty(dateObs)) {
                sb.append(' ').append(dateObs);
            }
            sb.append(' ').append(oiData.getInsName());
            return sb.toString();
        }
        return table.toString();
    }

    /**
     * return the String for a given object comming from the tree.
     * @param value
     * @param sb
     * @param completeMode true avoid reset of stringbuilder and html markup generation, else generate a full html fragment
     * @return the tooltip content
     * @todo find a better name fo completeMode parameter.
    */
    private String getTreeTooltipText(final Object value, final StringBuilder sb, final boolean completeMode) {
        if (!completeMode) {
            sb.setLength(0);
        }

        if (value instanceof StatisticatedObject) {
            final StatisticatedObject statisticatedObject = (StatisticatedObject) value;
            final Object mainObject = statisticatedObject.getMainObject();         
            // TODO display statistics....
            // by now, just append orinal tooltip
            final String mainObjectToolTip = getTreeTooltipText(mainObject, new StringBuilder(), true);
            if (mainObjectToolTip != null) {
                sb.append(mainObjectToolTip);
            }
            sb.append("<hr>This node gater following material from ").append(statisticatedObject.getGranules().size()).append(" granule(s)");
            for (OITable oitable : statisticatedObject.getOITables()) {
                sb.append("<br><br>").append(oitable).append("<br>");
                getTreeTooltipText(oitable, sb, true);
            }
        }
        else if (value instanceof Target) {
            final Target t = (Target) value;
            sb.append("<b>name:</b> ").append(t.getTarget());

            final List<String> aliases = TargetManager.getInstance().getSortedUniqueAliases(t);
            if (aliases != null) {
                sb.append("<hr>");
                sb.append("<b>Aliases:</b><br>");
                for (int j = 0, end = aliases.size(); j < end; j++) {
                    if (j != 0) {
                        sb.append("<br>");
                    }
                    sb.append("- ").append(aliases.get(j));
                }
                sb.append("<hr>");
            } else {
                sb.append("<br>");
            }
            sb.append("<b>Coords:</b> ");
            ALX.toHMS(sb, t.getRaEp0());
            sb.append(' ');
            ALX.toDMS(sb, t.getDecEp0());

            // TODO: check units
            if (!Double.isNaN(t.getPmRa()) && !Double.isNaN(t.getPmDec())) {
                // convert deg/year in mas/year :
                sb.append("<br><b>Proper motion</b> (mas/yr): ").append(t.getPmRa() * ALX.DEG_IN_MILLI_ARCSEC)
                        .append(' ').append(t.getPmDec() * ALX.DEG_IN_MILLI_ARCSEC);
            }
            if (!Double.isNaN(t.getParallax()) && !Double.isNaN(t.getParaErr())) {
                sb.append("<br><b>Parallax</b> (mas): ").append(t.getParallax() * ALX.DEG_IN_MILLI_ARCSEC)
                        .append(" [").append(t.getParaErr() * ALX.DEG_IN_MILLI_ARCSEC).append(']');
            }
            if (t.getSpecTyp() != null && !t.getSpecTyp().isEmpty()) {
                sb.append("<br><b>Spectral types</b>: ").append(t.getSpecTyp());
            }
        }
        else if (value instanceof InstrumentMode) {
            final InstrumentMode i = (InstrumentMode) value;
            sb.append("<b>name:</b> ").append(i.getInsName());

            final List<String> aliases = InstrumentModeManager.getInstance().getSortedUniqueAliases(i);
            if (aliases != null) {
                sb.append("<hr>");
                sb.append("<b>Aliases:</b><br>");
                for (int j = 0, end = aliases.size(); j < end; j++) {
                    if (j != 0) {
                        sb.append("<br>");
                    }
                    sb.append("- ").append(aliases.get(j));
                }
                sb.append("<hr>");
            } else {
                sb.append("<br>");
            }
            sb.append("<b>Nb channels:</b> ").append(i.getNbChannels());
            sb.append("<br><b>Lambda min:</b> ").append(i.getLambdaMin());
            sb.append("<br><b>Lambda max:</b> ").append(i.getLambdaMax());
            sb.append("<br><b>Resolution:</b> ").append(i.getResPower());
        }
        else if (value instanceof OIData) {
            final OIData o = (OIData) value;
            sb.append("<html>");
            sb.append("<b>Table:</b> ").append(o.getExtName()).append('#').append(o.getExtNb());
            sb.append("<br><b>OIFits:</b> ").append(o.getOIFitsFile().getName());
            sb.append("<br><b>DATE-OBS:</b> ").append(o.getDateObs());
            sb.append("<br><b>ARRNAME:</b> ").append(o.getArrName());
            sb.append("<br><b>INSNAME:</b> ").append(o.getInsName());
            sb.append("<br><b>NB_MEASUREMENTS:</b> ").append(o.getNbMeasurements());

            sb.append("<br><b>Configurations:</b> ");
            for (short[] staConf : o.getDistinctStaConf()) {
                sb.append(o.getStaNames(staConf)); // cached
            }
        }
        if (sb.length() == 0) {
            return null;
        } else if (!completeMode) {
            sb.insert(0, "<html>");
            sb.append("</html>");
        }
        return sb.toString();
    }

    private class StatisticatedObject {

        private final Object mainObject;
        private final Set<OITable> oiTables = new LinkedHashSet<OITable>();
        private final Set<Granule> granules = new LinkedHashSet<Granule>();


        public StatisticatedObject(Object mainObject) {
            this.mainObject = mainObject;
        }

        public Object getMainObject() {
            return this.mainObject;
        }

        public Set<OITable> getOITables() {
            return this.oiTables;
        }

        public void addOITable(OITable table){
            this.oiTables.add(table);
        }

        public Set<Granule> getGranules() {
            return this.granules;
        }
        private void addGranule(Granule granule) {
            this.granules.add(granule);
        }

        @Override
       public boolean equals(Object o){
           if(o instanceof StatisticatedObject){
               return this.mainObject.equals(((StatisticatedObject)o).getMainObject());
           }
           return this.mainObject.equals(o);
       }
    }

    private class TooltipTreeCellRenderer extends DefaultTreeCellRenderer {

        private static final long serialVersionUID = 1L;

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {

            if (value != null) {
                final Object userObject;
                if (value instanceof DefaultMutableTreeNode) {
                    userObject = ((DefaultMutableTreeNode) value).getUserObject();
                    setToolTipText(getTreeTooltipText(userObject, tmpBuf, false));
                }
            }
            return super.getTreeCellRendererComponent(tree, value, sel,
                    expanded, leaf, row, hasFocus);
        }
    }
}
