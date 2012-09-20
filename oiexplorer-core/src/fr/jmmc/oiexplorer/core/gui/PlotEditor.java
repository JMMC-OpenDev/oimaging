/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui;

import fr.jmmc.jmcs.gui.component.GenericListModel;
import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManager;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEvent;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventListener;
import fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventType;
import fr.jmmc.oiexplorer.core.model.PlotDefinitionFactory;
import fr.jmmc.oiexplorer.core.model.oi.Plot;
import fr.jmmc.oiexplorer.core.model.oi.SubsetDefinition;
import fr.jmmc.oiexplorer.core.model.plot.PlotDefinition;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mella
 */
public final class PlotEditor extends javax.swing.JPanel implements OIFitsCollectionManagerEventListener {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(PlotEditor.class.getName());

    /* members */
    /** OIFitsCollectionManager singleton reference */
    private final OIFitsCollectionManager ocm = OIFitsCollectionManager.getInstance();
    /** Associated plot identifier */
    private String plotId = null;

    /** Creates new form PlotEditor */
    public PlotEditor() {
        // always bind at the beginning of the constructor (to maintain correct ordering):
        ocm.bindSubsetDefinitionListChangedEvent(this);
        ocm.bindPlotDefinitionListChangedEvent(this);
        ocm.getPlotChangedEventNotifier().register(this);

        initComponents();
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
     * Initialize the widget for the given plot identifier
     * @param plotId plot identifier
     */
    public void initialize(final String plotId) {
        logger.debug("initialize {}", plotId);

        // TODO: this can be done automatically when this instance registers !
        // => fire initial events !

        // fire PLOT_DEFINITION_LIST_CHANGED event to initialize correctly the widget:
        ocm.firePlotDefinitionListChanged(null, this); // null forces different source

        setPlotId(plotId);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        subsetComboBox = new javax.swing.JComboBox();
        plotDefinitionComboBox = new javax.swing.JComboBox();
        subsetLabel = new javax.swing.JLabel();
        plotDefinitionLabel = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        subsetComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                subsetComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        add(subsetComboBox, gridBagConstraints);

        plotDefinitionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                plotDefinitionComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        add(plotDefinitionComboBox, gridBagConstraints);

        subsetLabel.setText("subset:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        add(subsetLabel, gridBagConstraints);

        plotDefinitionLabel.setText("plot config:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        add(plotDefinitionLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void subsetComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subsetComboBoxActionPerformed
        final String subsetId = (String) subsetComboBox.getSelectedItem();

        if (subsetId == null) {
            logger.debug("[{}] subsetComboBoxActionPerformed() event ignored : no current selection", plotId);
            return;
        }

        final Plot plotCopy = getPlot();
        if (plotCopy != null) {
            plotCopy.setSubsetDefinition(ocm.getSubsetDefinitionRef(subsetId));
            ocm.updatePlot(this, plotCopy);
        }
    }//GEN-LAST:event_subsetComboBoxActionPerformed

    private void plotDefinitionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_plotDefinitionComboBoxActionPerformed
        final String plotDefId = (String) plotDefinitionComboBox.getSelectedItem();

        if (plotDefId == null) {
            logger.debug("[{}] plotDefinitionComboBoxActionPerformed() event ignored : no current selection", plotId);
            return;
        }

        // get copy:
        final Plot plotCopy = getPlot();

        if (plotCopy != null) {

            if (ocm.hasPlotDefinition(plotDefId)) {
                // collection has it: use it
                plotCopy.setPlotDefinition(ocm.getPlotDefinitionRef(plotDefId));
            } else {
                // clone preset (same id):
                final PlotDefinition plotDefCopy = (PlotDefinition) PlotDefinitionFactory.getInstance().getDefault(plotDefId).clone();

                ocm.addPlotDefinition(plotDefCopy);

                plotCopy.setPlotDefinition(plotDefCopy);
            }

            ocm.updatePlot(this, plotCopy);
        }
    }//GEN-LAST:event_plotDefinitionComboBoxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox plotDefinitionComboBox;
    private javax.swing.JLabel plotDefinitionLabel;
    private javax.swing.JComboBox subsetComboBox;
    private javax.swing.JLabel subsetLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Define the plot identifier, reset plot and firePlotChanged on this instance if the plotId changed
     * @param plotId plot identifier
     */
    public void setPlotId(final String plotId) {
        final String prevPlotId = this.plotId;
        this.plotId = plotId;

        if (plotId != null && !ObjectUtils.areEquals(prevPlotId, plotId)) {
            logger.debug("setPlotId {}", plotId);

            // fire PlotChanged event to initialize correctly the widget:
            ocm.firePlotChanged(null, plotId, this); // null forces different source
        }
    }

    /**
     * Return a new copy of the Plot given its identifier (to update it)
     * @return copy of the Plot or null if not found
     */
    private Plot getPlot() {
        return ocm.getPlot(plotId);
    }

    private void refreshSubsetNames(final List<SubsetDefinition> subsetDefinitionList) {
        logger.debug("refreshSubsetNames: {}", plotId);

        // Put all subset references:
        final List<String> subsetNames = new ArrayList<String>();
        for (SubsetDefinition subset : subsetDefinitionList) {
            subsetNames.add(subset.getName());
        }

        final Object oldValue = subsetComboBox.getSelectedItem();

        subsetComboBox.setModel(new GenericListModel<String>(subsetNames, true));

        // restore previous selection: TODO: handle case where it becomes invalid.
        if (oldValue != null) {
            if (subsetNames.contains(oldValue.toString())) {
                subsetComboBox.setSelectedItem(oldValue);
            } else {
                // TODO: handle case where it becomes invalid.
                logger.warn("refreshSubsetNames: {} - invalid subset {}", plotId, oldValue);
            }
        }

        // hide subset combo if only 1
        final boolean showSubsets = (subsetComboBox.getModel().getSize() > 1);
        subsetLabel.setVisible(showSubsets);
        subsetComboBox.setVisible(showSubsets);
    }

    private void refreshPlotDefinitionNames(final List<PlotDefinition> plotDefinitionList) {
        logger.debug("refreshPlotDefinitionNames: {}", plotId);

        // use identifiers to keep unique values:
        final Set<String> plotDefNames = new LinkedHashSet<String>();
        for (PlotDefinition plotDef : plotDefinitionList) {
            plotDefNames.add(plotDef.getName());
        }
        for (PlotDefinition plotDef : PlotDefinitionFactory.getInstance().getDefaults()) {
            plotDefNames.add(plotDef.getName());
        }

        final Object oldValue = plotDefinitionComboBox.getSelectedItem();

        plotDefinitionComboBox.setModel(new GenericListModel<String>(new ArrayList<String>(plotDefNames), true));

        // restore previous selection: TODO: handle case where it becomes invalid.
        if (oldValue != null) {
            if (plotDefNames.contains(oldValue.toString())) {
                plotDefinitionComboBox.setSelectedItem(oldValue);
            } else {
                // TODO: handle case where it becomes invalid.
                logger.warn("refreshPlotDefinitionNames: {} - invalid plot def {}", plotId, oldValue);
            }
        }
    }

    private void refreshPlot(final Plot plotRef) {
        logger.debug("refreshPlot: {}", plotId);

        if (plotRef != null) {
            subsetComboBox.setSelectedItem((plotRef.getSubsetDefinition() != null) ? plotRef.getSubsetDefinition().getName() : null);
            plotDefinitionComboBox.setSelectedItem((plotRef.getPlotDefinition() != null) ? plotRef.getPlotDefinition().getName() : null);
        }
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
            case SUBSET_LIST_CHANGED:
                // accept all
                return null;
            case PLOT_DEFINITION_LIST_CHANGED:
                // accept all
                return null;
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
            case SUBSET_LIST_CHANGED:
                refreshSubsetNames(event.getSubsetDefinitionList());
                break;
            case PLOT_DEFINITION_LIST_CHANGED:
                refreshPlotDefinitionNames(event.getPlotDefinitionList());
                break;
            case PLOT_CHANGED:
                refreshPlot(event.getPlot());
                break;
            default:
        }
        logger.debug("onProcess {} - done", event);
    }
}
