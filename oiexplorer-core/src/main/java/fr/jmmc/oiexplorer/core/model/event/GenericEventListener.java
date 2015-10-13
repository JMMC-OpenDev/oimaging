/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model.event;

/**
 * This interface define the onProcess method to handle generic event
 * @param <K> event class
 * @param <V> event type class
 * @param <O> object's value class
 * @author bourgesl
 */
public interface GenericEventListener<K extends GenericEvent<V, O>, V, O> {

    /** 
     * String constant indicating that the event is discarded for this event type
     * @see #getSubjectId(java.lang.Object) 
     */
    public final static String DISCARDED_SUBJECT_ID = "DISCARDED";

    /**
     * Return the optional subject id i.e. related object id that this listener accepts
     * @param type event type
     * @return subject id (null means accept any event) or DISCARDED_SUBJECT_ID to discard event
     */
    public String getSubjectId(final V type);

    /**
     * Handle the given generic event
     * @param event generic event
     */
    public void onProcess(final K event);
}
