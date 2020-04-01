/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model;

import fr.jmmc.jmcs.util.ObjectUtils;
import fr.jmmc.jmcs.util.PublicCloneable;
import fr.jmmc.jmcs.util.ToStringable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a base class for all generated classes in model.oi (Optical Interferometry Data Model)
 * @author bourgesl
 */
public class OIBase implements PublicCloneable, ToStringable {

    /** Class logger for model classes */
    protected static final Logger logger = LoggerFactory.getLogger(OIBase.class.getName());
    /** flag to use full verbosity in toString() implementation */
    public static final boolean TO_STRING_VERBOSITY = true;

    /**
     * Public Constructor
     */
    public OIBase() {
        super();
    }

    /**
     * Perform a deep-copy of the given other instance into this instance
     * 
     * Note: to be override in child class to perform deep-copy of class fields
     * @see OIBase#clone() 
     * 
     * @param other other instance
     */
    public void copy(final OIBase other) {
        // nothing to copy
    }

    /**
     * Return a "deep-copy" of this instance using the clone + copy(OIBase) method
     * @see OIBase#copy(fr.jmmc.oiexplorer.core.model.OIBase) 
     * 
     * @return "deep-copy" of this instance
     */
    @Override
    public final Object clone() {
        try {
            final OIBase other = (OIBase) super.clone();
            other.copy(this);
            return other;
        } catch (CloneNotSupportedException cnse) {
            logger.error("clone failure:", cnse);
        }
        return null;
    }

    /**
     * toString() implementation wrapper to get complete information
     * Note: prefer using @see #toString(java.lang.StringBuilder) instead
     * @return string representation
     */
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder(256);
        toString(sb, TO_STRING_VERBOSITY);
        return sb.toString();
    }

    /**
     * toString() implementation using string builder
     * 
     * Note: to be overriden in child classes to append their fields
     * 
     * @param sb string builder to append to
     * @param full true to get complete information; false to get main information (shorter)
     */
    @Override
    public void toString(final StringBuilder sb, final boolean full) {
        ObjectUtils.getObjectInfo(sb, this);
    }
}
