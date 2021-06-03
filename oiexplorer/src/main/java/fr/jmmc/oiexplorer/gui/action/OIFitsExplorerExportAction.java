/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.gui.action;

import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.oiexplorer.OIFitsExplorer;
import fr.jmmc.oiexplorer.core.export.DocumentExportable;
import fr.jmmc.oiexplorer.core.gui.action.ExportDocumentAction;

/**
 * This registered action represents a File Menu entry to export the selected view as a document.
 *
 * @author bourgesl
 */
public final class OIFitsExplorerExportAction extends ExportDocumentAction {

    /**
     * default serial UID for Serializable interface
     */
    private static final long serialVersionUID = 1;

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     * @param mimeType exported document's MimeType
     */
    public OIFitsExplorerExportAction(final MimeType mimeType) {
        super(mimeType);
    }

    /**
     * Return the currently selected GUI panel (JPanel ...) to determine if it is possible to export it (DocumentExportable)
     *
     * @return selected GUI panel
     */
    @Override
    protected DocumentExportable getSelectedComponent() {
        return OIFitsExplorer.getInstance().getMainPanel().getCurrentExportableView();
    }
}
