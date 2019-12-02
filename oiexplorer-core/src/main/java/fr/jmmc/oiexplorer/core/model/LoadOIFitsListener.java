/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model;

import java.beans.PropertyChangeListener;

/**
 *
 * @author bourgesl
 */
public interface LoadOIFitsListener extends PropertyChangeListener {
    
    public void done(final boolean cancelled);
}
