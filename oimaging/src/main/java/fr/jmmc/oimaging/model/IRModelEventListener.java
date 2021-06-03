/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.model;

import fr.jmmc.jmcs.gui.component.Disposable;
import fr.jmmc.oiexplorer.core.model.event.GenericEventListener;

/**
 * This interface define the methods to be implemented by IRModelEvent listener implementations
 and force listener to be disposable to unregister them
 * 
 * @author bourgesl
 */
public interface IRModelEventListener
        extends Disposable, GenericEventListener<IRModelEvent, IRModelEventType, Object> {
}