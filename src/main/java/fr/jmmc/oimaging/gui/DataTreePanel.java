/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.gui;

import fr.jmmc.jmcs.gui.component.GenericJTree;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.oiexplorer.core.model.OIFitsCollection;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEvent;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventListener;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventType;
import fr.jmmc.oiexplorer.core.model.oi.OIDataFile;
import fr.jmmc.oiexplorer.core.model.oi.Plot;
import fr.jmmc.oiexplorer.core.model.oi.SubsetDefinition;
import fr.jmmc.oiexplorer.core.model.oi.TableUID;
import fr.jmmc.oiexplorer.core.model.oi.TargetUID;
import fr.jmmc.oiexplorer.core.model.util.TargetUIDComparator;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OITable;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    /* members */
    /** OIFitsCollectionManager singleton */
    private final OIFitsCollectionManager ocm = OIFitsCollectionManager.getInstance();
    /** subset identifier */
    private String subsetId = OIFitsCollectionManager.CURRENT_SUBSET_DEFINITION;
    /** Swing data tree */
    private GenericJTree<Object> dataTree;
    /** temporary buffer */
    private final StringBuilder tmpBuf = new StringBuilder(64);

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

        // dataTree contains TargetUID or OITable objects:
        dataTree = new GenericJTree<Object>(null) {
            /** default serial UID for Serializable interface */
            private static final long serialVersionUID = 1;

            @Override
            protected String convertUserObjectToString(final Object userObject) {
                if (userObject instanceof TargetUID) {
                    // target name
                    // TODO: add all aliases
                    return ((TargetUID) userObject).getTarget();
                }
                if (userObject instanceof OITable) {
                    return getDisplayLabel((OITable) userObject, tmpBuf);
                }
                return toString(userObject);
            }
        };

        ToolTipManager.sharedInstance().registerComponent(dataTree);

        dataTree.setCellRenderer(new TooltipTreeCellRenderer());

        // Define root node once:
        final DefaultMutableTreeNode rootNode = dataTree.getRootNode();
        rootNode.setUserObject("Targets");

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
            processTargetSelection(null);
        } else {
            boolean found = false;

            // Restore subset selection:
            final SubsetDefinition subsetRef = getSubsetDefinitionRef();

            if (subsetRef != null && subsetRef.getTarget() != null) {
                final DefaultMutableTreeNode targetTreeNode = dataTree.findTreeNode(subsetRef.getTarget());

                if (targetTreeNode != null) {
                    DefaultMutableTreeNode tableTreeNode = null;

                    if (!subsetRef.getTables().isEmpty()) {
                        // TODO: support multi selection:
                        final TableUID tableUID = subsetRef.getTables().get(0);
                        final String filePath = tableUID.getFile().getFile();
                        final Integer extNb = tableUID.getExtNb();

                        for (int i = 0, size = targetTreeNode.getChildCount(); i < size; i++) {
                            final DefaultMutableTreeNode node = (DefaultMutableTreeNode) targetTreeNode.getChildAt(i);
                            final OITable oiTable = (OITable) node.getUserObject();

                            if (filePath.equals(oiTable.getOIFitsFile().getAbsoluteFilePath())) {
                                if (extNb != null && extNb.intValue() == oiTable.getExtNb()) {
                                    tableTreeNode = node;
                                    break;
                                }
                            }
                        }

                    }
                    found = true;

                    if (tableTreeNode != null) {
                        dataTree.selectPath(new TreePath(tableTreeNode.getPath()));
                    } else {
                        dataTree.selectPath(new TreePath(targetTreeNode.getPath()));
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

        final Map<TargetUID, OIFitsFile> oiFitsPerTarget = oiFitsCollection.getOiFitsPerTarget();

        // Sort target by their name:
        final List<TargetUID> targetUIDs = new ArrayList<TargetUID>(oiFitsPerTarget.size());
        targetUIDs.addAll(oiFitsPerTarget.keySet());
        Collections.sort(targetUIDs, TargetUIDComparator.INSTANCE);

        // Add targets and their data tables:
        for (TargetUID target : targetUIDs) {
            final DefaultMutableTreeNode targetTreeNode = dataTree.addNode(rootNode, target);

            final OIFitsFile dataForTarget = oiFitsPerTarget.get(target);
            if (dataForTarget != null) {
                for (OITable table : dataForTarget.getOiDataList()) {
                    dataTree.addNode(targetTreeNode, table);
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

                    if (userObject instanceof TargetUID) {
                        final TargetUID target = (TargetUID) userObject;

                        processTargetSelection(target);
                    } else if (userObject instanceof OITable) {
                        final OITable oiTable = (OITable) userObject;

                        final DefaultMutableTreeNode parentNode = dataTree.getParentNode(currentNode);

                        if (parentNode != null && parentNode.getUserObject() instanceof TargetUID) {
                            final TargetUID parentTarget = (TargetUID) parentNode.getUserObject();

                            processTableSelection(parentTarget, oiTable);
                        }
                    }
                }
            });
        }
    }

    /**
     * Update the UI when a target is selected in the data tree
     * @param target selected target
     */
    private void processTargetSelection(final TargetUID target) {
        logger.debug("processTargetSelection: {}", target);

        // update subset definition (copy):
        final SubsetDefinition subsetCopy = getSubsetDefinition();
        if (subsetCopy != null) {
            subsetCopy.setTarget(target);
            subsetCopy.getTables().clear(); // means all

            // fire subset changed event:
            ocm.updateSubsetDefinition(this, subsetCopy);
        }
    }

    /**
     * Update the UI when a OITable is selected in the data tree
     * @param target selected target
     * @param oiTable selected table
     */
    private void processTableSelection(final TargetUID target, final OITable oiTable) {
        logger.debug("processTableSelection: {}", oiTable);

        // update subset definition (copy):
        final SubsetDefinition subsetCopy = getSubsetDefinition();
        if (subsetCopy != null) {
            subsetCopy.setTarget(target);
            final List<TableUID> tables = subsetCopy.getTables();
            tables.clear();

            final OIDataFile dataFile = ocm.getOIDataFile(oiTable.getOIFitsFile());
            if (dataFile != null) {
                tables.add(new TableUID(dataFile, oiTable.getExtName(), oiTable.getExtNb()));
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

    private String getTreeTooltipText(final Object value, final StringBuilder sb) {
        sb.setLength(0);
        if (value instanceof TargetUID) {
            final TargetUID targetUID = (TargetUID) value;
            sb.append(targetUID.getTarget());
//            sb.append(" TODO (add aliases, coordinates ...)");
            return sb.toString();
        }
        if (value instanceof OIData) {
            final OIData oiData = (OIData) value;
            sb.append("<html>");
            sb.append("<b>Table:</b> ").append(oiData.getExtName()).append('#').append(oiData.getExtNb());
            sb.append("<br><b>OIFits:</b> ").append(oiData.getOIFitsFile().getName());
            sb.append("<br><b>DATE-OBS:</b> ").append(oiData.getDateObs());
            sb.append("<br><b>ARRNAME:</b> ").append(oiData.getArrName());
            sb.append("<br><b>INSNAME:</b> ").append(oiData.getInsName());
            sb.append("<br><b>NB_MEASUREMENTS:</b> ").append(oiData.getNbMeasurements());

            sb.append("<br><b>Configurations:</b> ");
            for (short[] staConf : oiData.getDistinctStaConf()) {
                sb.append(oiData.getStaNames(staConf)); // cached
            }
            sb.append("</html>");
            return sb.toString();
        }
        return null;
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
                    setToolTipText(getTreeTooltipText(userObject, tmpBuf));
                }
            }
            return super.getTreeCellRendererComponent(tree, value, sel,
                    expanded, leaf, row, hasFocus);
        }
    }
}
