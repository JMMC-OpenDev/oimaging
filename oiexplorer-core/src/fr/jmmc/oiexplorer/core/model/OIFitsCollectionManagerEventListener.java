/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model;

import fr.jmmc.jmcs.gui.component.Disposable;
import fr.jmmc.oiexplorer.core.model.event.GenericEventListener;

/**
 * This interface define the methods to be implemented by OIFitsCollectionManagerEvent listener implementations
 * and force listener to be disposable to unregister them
 * 
 * @author bourgesl
 */
public interface OIFitsCollectionManagerEventListener
        extends Disposable, GenericEventListener<OIFitsCollectionManagerEvent, OIFitsCollectionManagerEventType, Object> {
}