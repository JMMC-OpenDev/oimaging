/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model;

import static fr.jmmc.oiexplorer.core.model.OIFitsCollectionManagerEventType.PLOT_CHANGED;
import fr.jmmc.oiexplorer.core.model.event.GenericEvent;
import fr.jmmc.oiexplorer.core.model.oi.Plot;
import fr.jmmc.oiexplorer.core.model.oi.SubsetDefinition;
import fr.jmmc.oiexplorer.core.model.plot.PlotDefinition;
import java.util.List;

/**
 * Base class for OIFits collection events consumed by OIFitsCollectionManagerEventListener
 * 
 * Note: <O> is Object as this listener can be used to provide different arguments depending on OIFitsCollectionManagerEventType:
 * - COLLECTION_CHANGED              OIFitsCollection
 * - SUBSET_LIST_CHANGED             List<SubsetDefinition>
 * - SUBSET_CHANGED                  SubsetDefinition
 * - PLOT_DEFINITION_LIST_CHANGED    List<PlotDefinition>
 * - PLOT_DEFINITION_CHANGED         PlotDefinition
 * - PLOT_LIST_CHANGED               List<Plot>
 * - PLOT_CHANGED                    Plot
 * - ACTIVE_PLOT_CHANGED             Plot
 * @author bourgesl
 */
public final class OIFitsCollectionManagerEvent extends GenericEvent<OIFitsCollectionManagerEventType, Object> {

    /**
     * Public constructor dealing with an OIFits collection 
     * @param type event type
     * @param subjectId optional related object id
     */
    public OIFitsCollectionManagerEvent(final OIFitsCollectionManagerEventType type, final String subjectId) {
        super(type, subjectId);
    }

    /**
     * Resolve subject value using its subject id and event type
     */
    @Override
    protected void resolveSubjectValue() {
        final Object value;
        switch (getType()) {
            case COLLECTION_CHANGED:
                value = OIFitsCollectionManager.getInstance().getOIFitsCollection();
                break;
            case SUBSET_LIST_CHANGED:
                value = OIFitsCollectionManager.getInstance().getSubsetDefinitionList();
                break;
            case SUBSET_CHANGED:
                value = OIFitsCollectionManager.getInstance().getSubsetDefinitionRef(getSubjectId());
                break;
            case PLOT_DEFINITION_LIST_CHANGED:
                value = OIFitsCollectionManager.getInstance().getPlotDefinitionList();
                break;
            case PLOT_DEFINITION_CHANGED:
                value = OIFitsCollectionManager.getInstance().getPlotDefinitionRef(getSubjectId());
                break;
            case PLOT_LIST_CHANGED:
                value = OIFitsCollectionManager.getInstance().getPlotList();
                break;
            case PLOT_CHANGED:
                value = OIFitsCollectionManager.getInstance().getPlotRef(getSubjectId());
                break;
            case ACTIVE_PLOT_CHANGED:
                value = OIFitsCollectionManager.getInstance().getPlotRef(getSubjectId());
                break;
            default:
                value = null;
        }
        setSubjectValue(value);
    }

    /* 
     * helper methods to get correct type depending on the event type 
     */
    /**
     * Return the OIFitsCollection if this event is only COLLECTION_CHANGED
     * @return OIFitsCollection or null if wrong event type
     */
    public OIFitsCollection getOIFitsCollection() {
        if (getType() == OIFitsCollectionManagerEventType.COLLECTION_CHANGED) {
            return (OIFitsCollection) getSubjectValue();
        }
        return null;
    }

    /**
     * Return the List<SubsetDefinition> if this event is only SUBSET_LIST_CHANGED
     * @return List<SubsetDefinition> or null if wrong event type
     */
    @SuppressWarnings("unchecked")
    public List<SubsetDefinition> getSubsetDefinitionList() {
        if (getType() == OIFitsCollectionManagerEventType.SUBSET_LIST_CHANGED) {
            return (List<SubsetDefinition>) getSubjectValue();
        }
        return null;
    }

    /**
     * Return the SubsetDefinition if this event is only SUBSET_CHANGED
     * @return SubsetDefinition or null if wrong event type
     */
    @SuppressWarnings("unchecked")
    public SubsetDefinition getSubsetDefinition() {
        if (getType() == OIFitsCollectionManagerEventType.SUBSET_CHANGED) {
            return (SubsetDefinition) getSubjectValue();
        }
        return null;
    }

    /**
     * Return the List<PlotDefinition> if this event is only PLOT_DEFINITION_LIST_CHANGED
     * @return List<PlotDefinition> or null if wrong event type
     */
    @SuppressWarnings("unchecked")
    public List<PlotDefinition> getPlotDefinitionList() {
        if (getType() == OIFitsCollectionManagerEventType.PLOT_DEFINITION_LIST_CHANGED) {
            return (List<PlotDefinition>) getSubjectValue();
        }
        return null;
    }

    /**
     * Return the PlotDefinition if this event is only PLOT_DEFINITION_CHANGED
     * @return PlotDefinition or null if wrong event type
     */
    @SuppressWarnings("unchecked")
    public PlotDefinition getPlotDefinition() {
        if (getType() == OIFitsCollectionManagerEventType.PLOT_DEFINITION_CHANGED) {
            return (PlotDefinition) getSubjectValue();
        }
        return null;
    }

    /**
     * Return the List<Plot> if this event is only PLOT_LIST_CHANGED
     * @return List<Plot> or null if wrong event type
     */
    @SuppressWarnings("unchecked")
    public List<Plot> getPlotList() {
        if (getType() == OIFitsCollectionManagerEventType.PLOT_LIST_CHANGED) {
            return (List<Plot>) getSubjectValue();
        }
        return null;
    }

    /**
     * Return the Plot if this event is only PLOT_CHANGED
     * @return Plot or null if wrong event type
     */
    @SuppressWarnings("unchecked")
    public Plot getPlot() {
        if (getType() == OIFitsCollectionManagerEventType.PLOT_CHANGED) {
            return (Plot) getSubjectValue();
        }
        return null;
    }
    
    /**
     * Return the active Plot if this event is only ACTIVE_PLOT_CHANGED
     * @return Plot or null if wrong event type
     */
    @SuppressWarnings("unchecked")
    public Plot getActivePlot() {
        if (getType() == OIFitsCollectionManagerEventType.ACTIVE_PLOT_CHANGED) {
            return (Plot) getSubjectValue();
        }
        return null;
    }
}
