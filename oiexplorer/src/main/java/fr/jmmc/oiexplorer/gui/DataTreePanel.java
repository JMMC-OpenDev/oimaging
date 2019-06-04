/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.gui;

import fr.jmmc.jmal.ALX;
import fr.jmmc.jmcs.gui.component.GenericJTree;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEvent;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventListener;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventType;
import fr.jmmc.oiexplorer.core.model.oi.OIDataFile;
import fr.jmmc.oiexplorer.core.model.oi.Plot;
import fr.jmmc.oiexplorer.core.model.oi.SubsetDefinition;
import fr.jmmc.oiexplorer.core.model.oi.SubsetFilter;
import fr.jmmc.oiexplorer.core.model.oi.TableUID;
import fr.jmmc.oitools.model.Granule;
import fr.jmmc.oitools.model.InstrumentMode;
import fr.jmmc.oitools.model.InstrumentModeManager;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsCollection;
import fr.jmmc.oitools.model.OITable;
import fr.jmmc.oitools.model.Target;
import fr.jmmc.oitools.model.TargetManager;
import fr.jmmc.oitools.util.GranuleComparator;
import java.awt.Component;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This panel contains the data tree
 * 
 * // TODO: support multiple table selections
 * 
 * @author mella
 */
public final class DataTreePanel extends javax.swing.JPanel implements TreeSelectionListener, OIFitsCollectionManagerEventListener {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(DataTreePanel.class);
    /** singleton instance */
    public static final GranuleComparator CMP_TARGET_INSMODE = new GranuleComparator(
            Arrays.asList(
                    Granule.GranuleField.TARGET,
                    Granule.GranuleField.INS_MODE
            )
    );

    /* members */
    /** OIFitsCollectionManager singleton reference */
    private final static OIFitsCollectionManager ocm = OIFitsCollectionManager.getInstance();
    /** subset identifier */
    private String subsetId = OIFitsCollectionManager.CURRENT_SUBSET_DEFINITION;
    /** Swing data tree */
    private GenericJTree<Object> dataTree;

    /** Creates new form DataTreePanel */
    public DataTreePanel() {
        // always bind at the beginning of the constructor (to maintain correct ordering):
        ocm.bindCollectionChangedEvent(this);
        ocm.getActivePlotChangedEventNotifier().register(this);

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

        // dataTree contains TargetUID, InsModeUID or OITable objects:
        dataTree = createTree();

        // Define root node once:
        final DefaultMutableTreeNode rootNode = dataTree.getRootNode();
        rootNode.setUserObject("Targets");

        ToolTipManager.sharedInstance().registerComponent(dataTree);

        dataTree.setCellRenderer(new TooltipTreeCellRenderer());

        // tree selection listener :
        dataTree.addTreeSelectionListener(this);

        genericTreePanel.add(dataTree);
    }

    /**
     * Update the data tree
     * @param oiFitsCollection OIFitsCollection to process
     */
    private void updateOIFitsCollection(final OIFitsCollection oiFitsCollection) {
        // force clean up ...
        setSubsetId(subsetId);

        generateTree(oiFitsCollection);

        // ALWAYS select a target
        // TODO: the selection should be in sync with subset modification (load, external updates)
        if (oiFitsCollection.isEmpty()) {
            processSelection(null, null, null);
        } else {
            boolean found = false;

            // Restore subset selection:
            final SubsetDefinition subsetRef = getSubsetDefinitionRef();

            if (subsetRef != null) {
                final SubsetFilter filter = subsetRef.getFilter();

                if (filter.getTargetUID() != null) {
                    final Target target = oiFitsCollection.getTargetManager().getGlobalByUID(filter.getTargetUID());
                    final DefaultMutableTreeNode targetTreeNode = dataTree.findTreeNode(target);

                    if (targetTreeNode != null) {
                        DefaultMutableTreeNode insModeTreeNode = null;
                        DefaultMutableTreeNode tableTreeNode = null;
                        found = true;

                        if (filter.getInsModeUID() != null) {
                            final InstrumentMode insMode = oiFitsCollection.getInstrumentModeManager().getGlobalByUID(filter.getInsModeUID());
                            insModeTreeNode = GenericJTree.findTreeNode(targetTreeNode, insMode);

                            if (!filter.getTables().isEmpty()) {
                                // TODO: support multi selection:
                                final TableUID tableUID = filter.getTables().get(0);
                                final String filePath = tableUID.getFile().getFile();
                                final Integer extNb = tableUID.getExtNb();

                                for (int i = 0, size = insModeTreeNode.getChildCount(); i < size; i++) {
                                    final DefaultMutableTreeNode node = (DefaultMutableTreeNode) insModeTreeNode.getChildAt(i);
                                    final OITable oiTable = (OITable) node.getUserObject();

                                    if (filePath.equals(oiTable.getOIFitsFile().getAbsoluteFilePath())) {
                                        if (extNb != null && extNb.intValue() == oiTable.getExtNb()) {
                                            tableTreeNode = node;
                                            break;
                                        }
                                    }
                                }
                            }
                        }

                        if (tableTreeNode != null) {
                            dataTree.selectPath(new TreePath(tableTreeNode.getPath()));
                        } else if (insModeTreeNode != null) {
                            dataTree.selectPath(new TreePath(insModeTreeNode.getPath()));
                        } else {
                            dataTree.selectPath(new TreePath(targetTreeNode.getPath()));
                        }
                    }
                }
            }
            if (!found) {
                // select first target :
                dataTree.selectFirstChildNode(dataTree.getRootNode());
            }
        }
    }

    /**
     * Update the data tree
     * @param activePlot plot used to initialize tree element.
     */
    private void updateOIFitsCollection(Plot activePlot) {
        if (activePlot != null) {
            SubsetDefinition subset = activePlot.getSubsetDefinition();
            setSubsetId(subset.getId());
            updateOIFitsCollection(ocm.getOIFitsCollection());
        }
    }

    /**
     * Generate the tree from the current edited list of targets
     * @param oiFitsCollection OIFitsCollection to process
     */
    private void generateTree(final OIFitsCollection oiFitsCollection) {

        final DefaultMutableTreeNode rootNode = dataTree.getRootNode();
        rootNode.removeAllChildren();

        // Sort granule by criteria (target / insMode / night):
        final GranuleComparator comparator = CMP_TARGET_INSMODE;

        final List<Granule> granules = oiFitsCollection.getSortedGranules(comparator);
        logger.debug("granules sorted: {}", granules);

        final Map<Granule, Set<OIData>> oiDataPerGranule = oiFitsCollection.getOiDataPerGranule();

        // Add nodes and their data tables:
        final List<Granule.GranuleField> fields = comparator.getSortDirectives();
        final int fieldsLen = fields.size();

        final DefaultMutableTreeNode[] pathNodes = new DefaultMutableTreeNode[fieldsLen + 1];
        int level;
        Granule.GranuleField field;
        Object value, other;

        pathNodes[0] = rootNode;

        for (Granule granule : granules) {

            // loop on fields:
            for (level = 1; level <= fieldsLen; level++) {
                field = fields.get(level - 1);
                value = granule.getField(field);

                if (value == null) {
                    logger.warn("null field value for granule: {}", granule);
                    value = "UNDEFINED";
                }

                DefaultMutableTreeNode prevNode = pathNodes[level];
                if (prevNode != null) {
                    // compare ?
                    other = prevNode.getUserObject();

                    // note: equals uses custom implementation in Target / InstrumentMode / NightId (all members are equals)
                    // equals method must be called on other to support proxy object (value.equals(other) may be different)
                    if (other == null || other.equals(value)) {
                        continue;
                    } else {
                        // different:
                        for (int i = level + 1; i <= fieldsLen; i++) {
                            // clear previous nodes:
                            pathNodes[i] = null;
                        }
                    }
                }

                pathNodes[level] = dataTree.addNode(pathNodes[level - 1], value);
            }

            final DefaultMutableTreeNode parent = pathNodes[level - 1];

            // Leaf:
            final Set<OIData> oiDatas = oiDataPerGranule.get(granule);
            if (oiDatas != null) {
                // for now per OIData:
                for (OITable table : oiDatas) {
                    // Avoid Table duplicates :
                    if (GenericJTree.findTreeNode(parent, table) == null) {
                        dataTree.addNode(parent, table);
                    }
                }
            }
        }

        // fire node structure changed :
        dataTree.fireNodeChanged(rootNode);
    }

    /**
     * Process the tree selection events
     * @param e tree selection event
     */
    @Override
    public void valueChanged(final TreeSelectionEvent e) {
        final DefaultMutableTreeNode currentNode = dataTree.getLastSelectedNode();

        if (currentNode != null) {
            // Use invokeLater to selection change issues with editors :
            SwingUtils.invokeLaterEDT(new Runnable() {
                /**
                 * Update tree selection
                 */
                @Override
                public void run() {
                    // Check if it is the root node :
                    final DefaultMutableTreeNode rootNode = dataTree.getRootNode();
                    if (currentNode == rootNode) {
                        dataTree.selectFirstChildNode(rootNode);
                        return;
                    }

                    /* retrieve the node that was selected */
                    final Object userObject = currentNode.getUserObject();

                    if (userObject instanceof Target) {
                        final Target target = (Target) userObject;
                        processSelection(target.getTarget(), null, null);
                    } else if (userObject instanceof InstrumentMode) {
                        final InstrumentMode insMode = (InstrumentMode) userObject;

                        final DefaultMutableTreeNode parentNode = dataTree.getParentNode(currentNode);

                        if (parentNode != null && parentNode.getUserObject() instanceof Target) {
                            final Target target = (Target) parentNode.getUserObject();

                            processSelection(target.getTarget(), insMode.getInsName(), null);
                        }
                    } else if (userObject instanceof OITable) {
                        final OITable oiTable = (OITable) userObject;

                        DefaultMutableTreeNode parentNode = dataTree.getParentNode(currentNode);

                        if (parentNode != null && parentNode.getUserObject() instanceof InstrumentMode) {
                            final InstrumentMode insMode = (InstrumentMode) parentNode.getUserObject();

                            parentNode = dataTree.getParentNode(parentNode);

                            if (parentNode != null && parentNode.getUserObject() instanceof Target) {
                                final Target target = (Target) parentNode.getUserObject();

                                processSelection(target.getTarget(), insMode.getInsName(), oiTable);
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * Update the SubsetDefinition depending on the data tree selection
     * @param targetUID selected target UID
     * @param insModeUID selected InstrumentMode UID
     * @param oiTable selected table
     */
    private void processSelection(final String targetUID, final String insModeUID, final OITable oiTable) {
        logger.debug("processSelection: {}", targetUID, insModeUID, oiTable);

        // update subset definition (copy):
        final SubsetDefinition subsetCopy = getSubsetDefinition();
        if (subsetCopy != null) {
            final SubsetFilter filter = subsetCopy.getFilter();
            filter.setTargetUID(targetUID);
            filter.setInsModeUID(insModeUID);

            final List<TableUID> tables = filter.getTables();
            tables.clear();

            if (oiTable != null) {
                final OIDataFile dataFile = ocm.getOIDataFile(oiTable.getOIFitsFile());
                if (dataFile != null) {
                    tables.add(new TableUID(dataFile, oiTable.getExtName(), oiTable.getExtNb()));
                }
            }

            // fire subset changed event:
            ocm.updateSubsetDefinition(this, subsetCopy);
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

        jScrollPane = new javax.swing.JScrollPane();
        genericTreePanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        genericTreePanel.setLayout(new java.awt.BorderLayout());
        jScrollPane.setViewportView(genericTreePanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jScrollPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel genericTreePanel;
    private javax.swing.JScrollPane jScrollPane;
    // End of variables declaration//GEN-END:variables

    /**
     * Return a new copy of the SubsetDefinition given its identifier (to update it)
     * @return copy of the SubsetDefinition or null if not found
     */
    private SubsetDefinition getSubsetDefinition() {
        return ocm.getSubsetDefinition(this.subsetId);
    }

    /**
     * Return a the SubsetDefinition reference given its identifier (to read it)
     * @return SubsetDefinition reference or null if not found
     */
    private SubsetDefinition getSubsetDefinitionRef() {
        return ocm.getSubsetDefinitionRef(this.subsetId);
    }

    /**
     * Define the subset identifier and reset subset
     * @param subsetId subset identifier
     */
    public void setSubsetId(final String subsetId) {
        this.subsetId = subsetId;
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
            case ACTIVE_PLOT_CHANGED:
                updateOIFitsCollection(event.getActivePlot());
                break;
            default:
        }
        logger.debug("onProcess {} - done", event);
    }

    private static InstrumentModeManager getInstrumentModeManager() {
        return ocm.getOIFitsCollection().getInstrumentModeManager();
    }

    private static TargetManager getTargetManager() {
        return ocm.getOIFitsCollection().getTargetManager();
    }

    // TODO: share code with GranuleTreePanel ?
    private GenericJTree<Object> createTree() {
        return new GenericJTree<Object>(null) {
            /** default serial UID for Serializable interface */
            private static final long serialVersionUID = 1;

            /** temporary buffer */
            private final StringBuilder tmpBuf = new StringBuilder(64);

            @Override
            protected String convertUserObjectToString(final Object userObject) {
                if (userObject instanceof Target) {
                    return ((Target) userObject).getTarget(); // global UID
                }
                if (userObject instanceof InstrumentMode) {
                    return ((InstrumentMode) userObject).getInsName(); // global UID
                }
                if (userObject instanceof OITable) {
                    return getDisplayLabel((OITable) userObject, tmpBuf);
                }
                return toString(userObject);
            }

            /**
             * Return the label displayed in the data tree
             * @param table OITable to display
             * @param sb temporary buffer
             * @return label
             */
            private String getDisplayLabel(final OITable table, final StringBuilder sb) {
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
        };
    }

    private final static class TooltipTreeCellRenderer extends DefaultTreeCellRenderer {

        private static final long serialVersionUID = 1L;

        /** temporary buffer */
        private final StringBuilder tmpBuf = new StringBuilder(64);

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean sel, boolean expanded, boolean leaf, int row,
                                                      boolean hasFocus) {

            if (value != null) {
                final Object userObject;
                if (value instanceof DefaultMutableTreeNode) {
                    userObject = ((DefaultMutableTreeNode) value).getUserObject();
                    setToolTipText(getTreeTooltipText(userObject, tmpBuf));
                }
            }
            return super.getTreeCellRendererComponent(tree, value, sel,
                    expanded, leaf, row, hasFocus);
        }

        private String getTreeTooltipText(final Object value, final StringBuilder sb) {
            sb.setLength(0);

            if (value instanceof Target) {
                final Target t = (Target) value;
                sb.append("<b>name:</b> ").append(t.getTarget());

                final List<String> aliases = getTargetManager().getSortedUniqueAliases(t);
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
            } else if (value instanceof InstrumentMode) {
                final InstrumentMode i = (InstrumentMode) value;
                sb.append("<b>name:</b> ").append(i.getInsName());

                final List<String> aliases = getInstrumentModeManager().getSortedUniqueAliases(i);
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
            } else if (value instanceof OIData) {
                final OIData oiData = (OIData) value;
                sb.append("<b>Table:</b> ").append(oiData.getExtName()).append('#').append(oiData.getExtNb());
                sb.append("<br><b>OIFits:</b> ").append(oiData.getOIFitsFile().getFileName());
                sb.append("<br><b>DATE-OBS:</b> ").append(oiData.getDateObs());
                sb.append("<br><b>ARRNAME:</b> ").append(oiData.getArrName());
                sb.append("<br><b>INSNAME:</b> ").append(oiData.getInsName());
                sb.append("<br><b>NB_MEASUREMENTS:</b> ").append(oiData.getNbMeasurements());

                sb.append("<br><b>Configurations:</b> ");
                for (short[] staConf : oiData.getDistinctStaConf()) {
                    sb.append(oiData.getStaNames(staConf)).append(' '); // cached
                }
            }
            if (sb.length() == 0) {
                return null;
            } else {
                sb.insert(0, "<html>");
                sb.append("</html>");
            }
            return sb.toString();
        }
    }
}
