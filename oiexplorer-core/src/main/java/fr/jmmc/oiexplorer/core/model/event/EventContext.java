/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model.event;

import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.jmcs.util.ToStringable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class gathers information related to event firing (event, source(s), destination(s)
 * @param <K> event class
 * @param <V> event type class
 * @param <O> object's value class
 * 
 * @author bourgesl
 */
public final class EventContext<K extends GenericEvent<V, O>, V, O> implements ToStringable {

    /* members */
    /** related event */
    private final K event;
    /** mandatory event source(s) (sender) */
    private final Set<Object> sources = new LinkedHashSet<Object>(4); // small
    /** optional destination listener(s) (null means all) */
    private Set<GenericEventListener<? extends GenericEvent<V, O>, V, O>> destinations = null;
    /** flag indicating if the destination set is defined to distinguish null (means all) with empty */
    private boolean destinationDefined = true;

    /**
     * Protected constructor
     * @param event related event
     */
    EventContext(final K event) {
        if (event == null) {
            throw new IllegalArgumentException("undefined event argument for " + getClass().getSimpleName());
        }
        this.event = event;
    }

    /**
     * Return the related event
     * @return related event
     */
    K getEvent() {
        return event;
    }

    /**
     * Add the source
     * @param source event source
     */
    void addSource(final Object source) {
        // ensure source unicity:
        this.sources.add(source);
    }

    /**
     * Return the event source(s)
     * @return event source(s)
     */
    Set<Object> getSources() {
        return sources;
    }

    /**
     * Return the optional destination listeners (null means all)
     * @return optional destination listeners (null means all)
     */
    Set<GenericEventListener<? extends GenericEvent<V, O>, V, O>> getDestinations() {
        return destinations;
    }

    /**
     * Add the destination listener (null means all)
     * @param destination optional destination listeners (null means all)
     */
    void addDestination(final GenericEventListener<? extends GenericEvent<V, O>, V, O> destination) {
        if (destination == null || (this.destinationDefined && this.destinations == null)) {
            // Note: if there was specific destination(s) or all, send to all:
            this.destinations = null;
        } else {
            if (this.destinations == null) {
                this.destinations = new LinkedHashSet<GenericEventListener<? extends GenericEvent<V, O>, V, O>>(4); // small
            }
            // ensure listener unicity:
            this.destinations.add(destination);
        }
        this.destinationDefined = true;
    }

    /**
     * toString() implementation wrapper to get complete information
     * Note: prefer using @see #toString(java.lang.StringBuilder) instead
     * @return string representation
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(256);
        toString(sb, EventNotifier.TO_STRING_VERBOSITY);
        return sb.toString();
    }

    /**
     * toString() implementation using string builder
     * 
     * @param sb string builder to append to
     * @param full true to get complete information; false to get main information (shorter)
     */
    @Override
    public void toString(final StringBuilder sb, final boolean full) {
        ObjectUtils.getObjectInfo(sb, this);

        sb.append("{source=");
        ObjectUtils.getObjectInfo(sb, this.sources);

        if (this.destinations != null) {
            sb.append(", destinations=");
            ObjectUtils.getObjectInfo(sb, this.destinations);
        }

        sb.append(", event=");
        event.toString(sb, full);
        sb.append('}');
    }
}
