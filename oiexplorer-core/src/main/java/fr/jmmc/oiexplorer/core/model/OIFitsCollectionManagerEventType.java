/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model;

/**
 * This enumeration defines all types of OIFitsManager events
 */
public enum OIFitsCollectionManagerEventType {

    /** OIFits collection changed */
    COLLECTION_CHANGED,
    /** plot collection changed (first as it create PlotViews) */
    PLOT_LIST_CHANGED,
    /** subset collection changed */
    SUBSET_LIST_CHANGED,
    /** plot definition collection changed */
    PLOT_DEFINITION_LIST_CHANGED,
    /** subset changed */
    SUBSET_CHANGED,
    /** plot definition changed */
    PLOT_DEFINITION_CHANGED,
    /** plot changed (last as SUBSET_CHANGED and PLOT_DEFINITION_CHANGED fire PLOT_CHANGED) */
    PLOT_CHANGED,
    /** active plot changed (ie. user is working/looking on a new plot) */
    ACTIVE_PLOT_CHANGED,
    /** last event type = ready */
    READY
}
