/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.model;

import fr.jmmc.oiexplorer.core.model.event.GenericEvent;

/**
 * Base class for IRModel events consumed by IRModelEventListener
 * 
 * Note: <O> is Object as this listener can be used to provide different arguments depending on IRModelEventType:
 - IRMODEL_CHANGED              IRModel
 * @author mellag, bourgesl
 */
public final class IRModelEvent extends GenericEvent<IRModelEventType, Object> {

    /**
     * Public constructor dealing with an IR Model 
     * @param type event type
     */
    public IRModelEvent(final IRModelEventType type) {
        this(type, null);
    }

    /**
     * Private constructor dealing with an IR Model 
     * @param type event type
     * @param subjectId optional related object id
     */
    private IRModelEvent(final IRModelEventType type, final String subjectId) {
        super(type, subjectId);
    }

    /**
     * Resolve subject value using its subject id and event type
     */
    @Override
    protected void resolveSubjectValue() {
        final Object value;
        switch (getType()) {
            case IRMODEL_CHANGED:
            case IRMODEL_RESULT_LIST_CHANGED:
                value = IRModelManager.getInstance().getIRModel();
                break;
            default:
                value = null;
        }
        setSubjectValue(value);
    }

    /* 
     * helper methods to get correct type depending on the event type 
     */
    public IRModel getIrModel() {
        return (IRModel) getSubjectValue();
    }
}
