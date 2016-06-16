/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.ir.model;

import java.beans.PropertyChangeListener;

/**
 *
 * @author bourgesl
 */
public interface LoadIRModelListener extends PropertyChangeListener {
    
    public void done(final boolean cancelled);
}
