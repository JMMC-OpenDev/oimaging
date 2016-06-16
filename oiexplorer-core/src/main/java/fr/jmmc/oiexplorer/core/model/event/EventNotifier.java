/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model.event;

import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.jmcs.util.ObjectUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is dedicated to event dispatching
 * @param <K> event class
 * @param <V> event type class
 * @param <O> object's value class
 * 
 * @author bourgesl
 */
public final class EventNotifier<K extends GenericEvent<V, O>, V, O> implements Comparable<EventNotifier<?, ?, ?>> {

    /** flag to log ONLY information when firing events (less verbose) */
    private static final boolean LOG_FIRE_EVENT = false;
    /** flag to use full verbosity in toString() implementation of EventContext / GenericEvent i.e. show subjectValue */
    public static final boolean TO_STRING_VERBOSITY = true;
    /** flag to log a stack trace in method register/unregister to debug registration */
    private static final boolean DEBUG_LISTENER = false;
    /** flag to log information useful to debug events */
    private static final boolean DEBUG_FIRE_EVENT = false;
    /** flag to log also a stack trace to debug events */
    private static final boolean DEBUG_STACK = false;
    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(EventNotifier.class);
    /** EventNotifierController singleton */
    private static final EventNotifierController globalController = new EventNotifierController();
    /** flag to detect new registered listener(s) while queueEvent runs to fire events to them also */
    private static final boolean FIRE_NEW_REGISTERED_LISTENER = true;
    /* members */
    /** name used for debugging purposes only */
    private final String name;
    /** event notifier's priority (compare to other event notifiers): lower values means higher priority */
    private final int priority;
    /** flag to disable listener notification if it is the source of the event */
    private final boolean skipSourceListener;
    /** event listeners using WeakReferences to avoid memory leaks */
    /* may detect widgets waiting for events on LOST subject objects ?? */
    private final CopyOnWriteArrayList<WeakReference<GenericEventListener<K, V, O>>> listeners = new CopyOnWriteArrayList<WeakReference<GenericEventListener<K, V, O>>>();
    /** queued events and contexts delivered asap by EDT (ordered by insertion order) */
    private final Map<K, EventContext<K, V, O>> eventQueue = new LinkedHashMap<K, EventContext<K, V, O>>();

    /** 
     * Public Constructor
     * @param name used for debugging purposes only
     * @param priority event notifier's priority
     */
    public EventNotifier(final String name, final int priority) {
        this(name, priority, true);
    }

    /** 
     * Public Constructor
     * @param name used for debugging purposes only
     * @param skipSourceListener flag to disable listener notification if it is the source of the event
     */
    public EventNotifier(final String name, final boolean skipSourceListener) {
        this(name, 0, skipSourceListener);
    }

    /** 
     * Public Constructor
     * @param name used for debugging purposes only
     * @param priority event notifier's priority
     * @param skipSourceListener flag to disable listener notification if it is the source of the event
     */
    public EventNotifier(final String name, final int priority, final boolean skipSourceListener) {
        this.name = name;
        this.priority = priority;
        this.skipSourceListener = skipSourceListener;
    }

    /**
     * Return the event notifier's priority
     * @return event notifier's priority
     */
    private int getPriority() {
        return priority;
    }

    /**
     * Compare this event notifier with another
     * @param other another event notifier
     * @return  a negative integer, zero, or a positive integer as this object
     *          is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(final EventNotifier<?, ?, ?> other) {
        final int otherPriority = other.getPriority();
        return (priority < otherPriority) ? -1 : ((priority == otherPriority) ? 0 : 1);
    }

    /**
     * Register the given event listener
     * @param listener event listener
     */
    public void register(final GenericEventListener<K, V, O> listener) {
        if (DEBUG_LISTENER) {
            logger.warn("REGISTER {} IN {}", ObjectUtils.getObjectInfo(listener), this, (DEBUG_STACK) ? new Throwable() : null);
        }
        final int pos = findListener(listener);
        if (pos == -1) {
            this.listeners.add(new WeakReference<GenericEventListener<K, V, O>>(listener));
        }
    }

    /**
     * Unregister the given event listener
     * @param listener event listener
     */
    public void unregister(final GenericEventListener<K, V, O> listener) {
        final int pos = findListener(listener);
        if (pos != -1) {
            if (DEBUG_LISTENER) {
                logger.warn("UNREGISTER {} FROM {}", ObjectUtils.getObjectInfo(listener), this, (DEBUG_STACK) ? new Throwable() : null);
            }
            this.listeners.remove(pos);
        }
    }

    /**
     * Return the position of the given listener
     * @param listener listener to look for
     * @return position of the given listener or -1 if not found
     */
    private int findListener(final GenericEventListener<K, V, O> listener) {
        WeakReference<GenericEventListener<K, V, O>> ref;
        GenericEventListener<K, V, O> l;

        for (int i = 0, size = this.listeners.size(); i < size; i++) {
            ref = this.listeners.get(i);
            l = ref.get();

            if (l == null) {
                // remove empty reference (GC):
                this.listeners.remove(i);
                size--;
                i--;
            } else if (l == listener) {
                // identity comparison:
                return i;
            }
        }
        return -1;
    }

    /**
     * Queue the given event to the registered listeners.
     * @param source event source
     * @param event event to fire
     * 
     * @throws IllegalStateException if this method is not called by Swing EDT
     */
    public void queueEvent(final Object source, final K event) throws IllegalStateException {
        queueEvent(source, event, null);
    }

    /**
     * Queue the given event to the registered listeners.
     * @param source event source
     * @param event event to fire
     * @param destination optional destination listeners (null means all)
     * 
     * @throws IllegalStateException if this method is not called by Swing EDT
     */
    public void queueEvent(final Object source, final K event,
            final GenericEventListener<? extends GenericEvent<V, O>, V, O> destination) throws IllegalStateException {

        // ensure events are fired by Swing EDT:
        if (!SwingUtils.isEDT()) {
            throw new IllegalStateException("invalid thread : use EDT", new Throwable());
        }

        // queue this event to avoid concurrency issues and repeated event notifications (thanks to MAP - see hashcode):

        // Get previous context:
        EventContext<K, V, O> context = this.eventQueue.get(event);

        final boolean merged;

        if (context == null) {
            merged = false;

            // create new context:
            context = new EventContext<K, V, O>(event);

            // add context:
            this.eventQueue.put(event, context);
        } else {
            merged = true;
        }

        // update source(s) and destination(s):
        context.addSource(source);
        context.addDestination(destination);

        if (DEBUG_FIRE_EVENT) {
            logger.warn("QUEUE {} EVENT {}", (merged) ? "MERGED" : "NEW", context, (DEBUG_STACK) ? new Throwable() : null);
        }

        // register this notifier in EDT:
        globalController.queueEventNotifier(this);
    }

    /**
     * Send an event to the registered listeners.
     * @param context event context to use
     * @throws IllegalStateException if this method is not called by Swing EDT
     */
    private void fireEvent(final EventContext<K, V, O> context) throws IllegalStateException {
        // ensure events are fired by Swing EDT:
        if (!SwingUtils.isEDT()) {
            throw new IllegalStateException("invalid thread : use EDT", new Throwable());
        }

        // check listeners just before firing events:
        if (this.listeners.isEmpty()) {
            if (DEBUG_FIRE_EVENT) {
                logger.warn("FIRE {} - NO LISTENER", context, (DEBUG_STACK) ? new Throwable() : null);
            }
            return;
        }

        // Fire events:
        if (DEBUG_FIRE_EVENT) {
            logger.warn("START FIRE {}", context, (DEBUG_STACK) ? new Throwable() : null);
        }

        logger.debug("fireEvent: {}", context);

        final K event = context.getEvent();

        // Resolve event subject value:
        event.resolveSubjectValue();

        // subjectId filter:
        final String subjectId = event.getSubjectId();

        // event source(s):
        final Set<Object> sources = context.getSources();

        // optional event destination(s):
        final Set<GenericEventListener<? extends GenericEvent<V, O>, V, O>> destinations = context.getDestinations();

        final long start = System.nanoTime();

        // used listeners:
        final Set<GenericEventListener<K, V, O>> firedListeners = new HashSet<GenericEventListener<K, V, O>>(this.listeners.size());
        boolean done;

        WeakReference<GenericEventListener<K, V, O>> ref;
        GenericEventListener<K, V, O> listener;
        String listenerSubjectId;

        do {
            // multiple pass until all listener fired:
            for (int i = 0; i < this.listeners.size(); i++) {
                ref = this.listeners.get(i);
                listener = ref.get();

                if (listener == null) {
                    // remove empty reference (GC):
                    this.listeners.remove(i);
                    i--;
                } else if (!firedListeners.contains(listener)) {

                    // check destinations (null means all):
                    if (destinations == null || destinations.contains(listener)) {
                        firedListeners.add(listener);

                        // do not fire event to the listener if it is also the source of this event:
                        if ((!skipSourceListener) || (!sources.contains(listener))) {

                            if (subjectId == null) {
                                if (DEBUG_FIRE_EVENT || LOG_FIRE_EVENT) {
                                    logger.warn("  FIRE {} TO {}", context, ObjectUtils.getObjectInfo(listener));
                                }
                                listener.onProcess(event);
                            } else {
                                // TODO: use listener binding (weak hash map ?)
                                listenerSubjectId = listener.getSubjectId(event.getType());

                                // check if the listener is accepting this subject id or any (null):
                                if ((listenerSubjectId == null) || (subjectId.equals(listenerSubjectId))) {
                                    if (DEBUG_FIRE_EVENT || LOG_FIRE_EVENT) {
                                        logger.warn("  FIRE {} TO {}", context, ObjectUtils.getObjectInfo(listener));
                                    }
                                    listener.onProcess(event);

                                } else if (DEBUG_FIRE_EVENT) {
                                    logger.warn("Skip Listener {} because subjectId does not match: {} != {}",
                                            ObjectUtils.getObjectInfo(listener), event.getSubjectId(), listenerSubjectId);
                                }
                            }
                        } else if (DEBUG_FIRE_EVENT) {
                            logger.warn("Skip Listener {} because is in sources: {}", ObjectUtils.getObjectInfo(listener), context);
                        }
                    } else if (DEBUG_FIRE_EVENT) {
                        logger.warn("Skip Listener {} because is not in destinations: {}", ObjectUtils.getObjectInfo(listener), context);
                    }
                }
            }

            // check if new listener(s) registered meanwhile:
            done = true;

            if (FIRE_NEW_REGISTERED_LISTENER) {
                for (int i = 0, size = this.listeners.size(); i < size; i++) {
                    ref = this.listeners.get(i);
                    listener = ref.get();
                    if (listener == null) {
                        // remove empty reference (GC):
                        this.listeners.remove(i);
                        size--;
                        i--;
                    } else {
                        // check destinations (null means all):
                        if (destinations == null || destinations.contains(listener)) {
                            if (!firedListeners.contains(listener)) {
                                // there is still one listener not fired:
                                done = false;
                                break;
                            }
                        }
                    }
                }
            }

        } while (!done);

        if (logger.isDebugEnabled()) {
            logger.debug("fireEvent: duration = {} ms.", 1e-6d * (System.nanoTime() - start));
        }
        if (DEBUG_FIRE_EVENT) {
            logger.warn("END FIRE {} : duration = {} ms.", context, 1e-6d * (System.nanoTime() - start));
        }
    }

    /**
     * Fire queued events
     */
    private void fireQueuedEvents() {

        // Copy the queued event set and clear it (available):
        if (DEBUG_FIRE_EVENT) {
            logger.warn("START FIRE QUEUED EVENTS {}", eventQueue.values());
        }

        // use only values (up to date event arguments):
        final List<EventContext<K, V, O>> events = new ArrayList<EventContext<K, V, O>>(eventQueue.values());

        eventQueue.clear();

        for (final EventContext<K, V, O> context : events) {
            fireEvent(context);
        }

        if (DEBUG_FIRE_EVENT) {
            logger.warn("END FIRE QUEUED EVENTS");
        }
    }

    /**
     * Add callback executed once all queued events are fired
     * @param callback runnable task
     */
    public static void addCallback(final Runnable callback) {
        globalController.addCallback(callback);
    }

    /**
     * This class represents the event notifier controller i.e. avoids concurrent notifications
     */
    private final static class EventNotifierController implements Runnable {

        /* members */
        /** flag indicating that this task is registered in EDT */
        private boolean registeredEDT = false;
        /** queued event notifiers (ordered by insertion order) */
        private final Set<EventNotifier<?, ?, ?>> queuedNotifiers = new LinkedHashSet<EventNotifier<?, ?, ?>>();
        /** callback list */
        private final List<Runnable> callbacks = new ArrayList<Runnable>(4);
        /** temporary storage for queuedNotifiers */
        private final List<EventNotifier<?, ?, ?>> queuedNotifiersCopy = new ArrayList<EventNotifier<?, ?, ?>>();

        /**
         * Private constructor
         */
        EventNotifierController() {
            super();
        }

        /**
         * Queue the given event notifier
         * @param eventNotifier event notifier to queue
         */
        void queueEventNotifier(final EventNotifier<?, ?, ?> eventNotifier) {
            queuedNotifiers.add(eventNotifier);
            registerEDT();
        }

        /**
         * Register this runnable in EDT (invokeLater)
         */
        private void registerEDT() {
            if (!registeredEDT) {
                registeredEDT = true;

                if (DEBUG_FIRE_EVENT) {
                    logger.warn("REGISTER CONTROLLER IN EDT (invokeLater)");
                }

                // fireQueuedNotifiers by next EDT event (ASYNC):
                SwingUtils.invokeLaterEDT(this);
            }
        }

        /**
         * Add callback executed once all queued events are fired
         * @param callback runnable task
         */
        public void addCallback(final Runnable callback) {
            callbacks.add(callback);
        }

        @Override
        public void run() {
            if (DEBUG_FIRE_EVENT) {
                logger.warn("CONTROLLER EXECUTED BY EDT (invokeLater)");
            }
            boolean done = true;
            try {
                // fire queued events:
                done = fireQueuedNotifiers();

            } finally {
                // at the end: to avoid too much use of invokeLater !
                registeredEDT = false;
            }
            if (!done) {
                // register EDT
                registerEDT();
            } else {
                // run callbacks once all queued notifiers / events are fired:
                runCallbacks();
            }
        }

        /**
         * Fire queued notifiers if possible using EDT (may use interlacing with standard Swing EDT)
         * @return true if queuedNotifiers is empty i.e. done
         */
        private boolean fireQueuedNotifiers() {

            if (queuedNotifiers.isEmpty()) {
                return true;
            }
            if (DEBUG_FIRE_EVENT) {
                logger.warn("START FIRE {} QUEUED NOTIFIERS", queuedNotifiers.size());
            }

            // process only 1 notifier at a time (loop) to maximize event merges of next events:
            queuedNotifiersCopy.clear();
            queuedNotifiersCopy.addAll(queuedNotifiers);

            if (queuedNotifiersCopy.size() > 1) {
                Collections.sort(queuedNotifiersCopy);
            }

            if (DEBUG_FIRE_EVENT) {
                logger.warn("SORTED QUEUED NOTIFIERS {}", queuedNotifiersCopy);
            }

            // Get first (highest priority):
            final EventNotifier<?, ?, ?> eventNotifier = queuedNotifiersCopy.get(0);
            queuedNotifiersCopy.clear();

            // Remove this event notifier in queued notifiers:
            queuedNotifiers.remove(eventNotifier);

            if (DEBUG_FIRE_EVENT) {
                logger.warn("FIRE QUEUED NOTIFIER {}", eventNotifier);
            }

            eventNotifier.fireQueuedEvents();

            if (DEBUG_FIRE_EVENT) {
                logger.warn("END FIRE QUEUED NOTIFIER", eventNotifier);
            }

            final boolean done = queuedNotifiers.isEmpty();
            if (DEBUG_FIRE_EVENT) {
                logger.warn("END FIRE QUEUED NOTIFIERS : {}", done);
            }

            return done;
        }

        /**
         * Run callbacks once all queued notifiers / events are fired
         */
        void runCallbacks() {
            if (callbacks.isEmpty()) {
                return;
            }

            if (DEBUG_FIRE_EVENT) {
                logger.warn("START RUN CALLBACKS");
            }

            for (Iterator<Runnable> it = callbacks.iterator(); it.hasNext();) {
                final Runnable callback = it.next();
                it.remove();

                if (DEBUG_FIRE_EVENT) {
                    logger.warn("RUN CALLBACK {}", ObjectUtils.getFullObjectInfo(callback));
                }

                callback.run();
            }

            if (DEBUG_FIRE_EVENT) {
                logger.warn("END RUN CALLBACKS");
            }
        }
    }

    /**
     * Return a string representation "<simple class name>#<hashCode>"
     * @return "<simple class name>#<hashCode>"
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(32);
        ObjectUtils.getObjectInfo(sb, this);
        sb.append('[').append(name).append(']');
        return sb.toString();
    }
}
