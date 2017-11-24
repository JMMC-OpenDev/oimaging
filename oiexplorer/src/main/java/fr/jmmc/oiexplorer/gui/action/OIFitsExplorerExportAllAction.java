/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.gui.action;

import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.oiexplorer.OIFitsExplorer;
import fr.jmmc.oiexplorer.core.export.DocumentExportable;
import fr.jmmc.oiexplorer.core.gui.action.ExportDocumentAction;

/**
 * This registered action represents a File Menu entry to export ALL charts in a single document.
 *
 * @author bourgesl
 */
public final class OIFitsExplorerExportAllAction extends ExportDocumentAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class name. This name is used to register to the ActionRegistrar */
    protected final static String className = OIFitsExplorerExportAllAction.class.getName();

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     * @param mimeType exported document's MimeType
     */
    public OIFitsExplorerExportAllAction(final MimeType mimeType) {
        super(mimeType, className);
    }

    /**
     * Return the main panel to determine if it is possible to export it (DocumentExportable)
     * @return main panel
     */
    @Override
    protected DocumentExportable getSelectedComponent() {
        return OIFitsExplorer.getInstance().getMainPanel();
    }
}
