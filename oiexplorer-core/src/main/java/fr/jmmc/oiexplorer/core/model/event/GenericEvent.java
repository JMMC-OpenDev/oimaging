/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model.event;

import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.jmcs.util.ToStringable;

/**
 * Base class for OIFits collection events consumed by OIFitsCollectionListener
 * @param <V> event type class
 * @param <O> object's value class
 * @author bourgesl
 */
public abstract class GenericEvent<V, O> implements ToStringable {

    /* members */
    /** event type */
    private final V type;
    /** subject id i.e. related object id (null allowed) */
    private final String subjectId;
    /** subject value (resolved before firing event) */
    private O subjectValue = null;

    /**
     * Public constructor
     * @param type event type
     * @param subjectId optional related object id
     */
    public GenericEvent(final V type, final String subjectId) {
        if (type == null) {
            throw new IllegalArgumentException("undefined type argument for " + getClass().getSimpleName());
        }
        this.type = type;
        this.subjectId = subjectId;
    }

    /**
     * Return the event type
     * @return event type
     */
    public final V getType() {
        return type;
    }

    /**
     * Return the subject id i.e. related object id
     * @return subject id i.e. related object id
     */
    public final String getSubjectId() {
        return subjectId;
    }

    /**
     * PROTECTED: Resolve subject value using its subject id and event type
     * @see #getSubjectValue() 
     */
    protected abstract void resolveSubjectValue();

    /**
     * PROTECTED: Define the subject value (resolved before firing event)
     * @see #resolveSubjectValue(java.lang.Object, java.lang.String) 
     * @param value subject value
     */
    protected void setSubjectValue(final O value) {
        this.subjectValue = value;
    }

    /**
     * Return the subject value (resolved before firing event)
     * @see #resolveSubjectValue(java.lang.Object, java.lang.String) 
     * 
     * @return subject value (resolved before firing event)
     */
    public O getSubjectValue() {
        return subjectValue;
    }

    /* GenericEvent implements hashCode and equals because events can be postponed ie merged: 
     * only last event of the "same" kind is fired */
    @Override
    public final int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.type.hashCode();
        hash = 97 * hash + ((this.subjectId != null) ? this.type.hashCode() : 0);
        return hash;
    }

    @Override
    public final boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GenericEvent<?, ?> other = (GenericEvent<?, ?>) obj;
        if (this.type != other.getType() && !this.type.equals(other.getType())) {
            return false;
        }
        if ((this.subjectId == null) ? (other.getSubjectId() != null) : !this.subjectId.equals(other.getSubjectId())) {
            return false;
        }
        return true;
    }

    /**
     * toString() implementation wrapper to get complete information
     * Note: prefer using @see #toString(java.lang.StringBuilder) instead
     * @return string representation
     */
    @Override
    public final String toString() {
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
    public final void toString(final StringBuilder sb, final boolean full) {
        ObjectUtils.getObjectInfo(sb, this);

        sb.append("{type=").append(this.type);
        if (this.subjectId != null) {
            sb.append(", subjectId=").append(this.subjectId);
        }

        if (full) {
            if (this.subjectValue != null) {
                sb.append(", subjectValue=");
                ObjectUtils.toString(sb, full, this.subjectValue);
            }
        }
        sb.append('}');
    }
}
