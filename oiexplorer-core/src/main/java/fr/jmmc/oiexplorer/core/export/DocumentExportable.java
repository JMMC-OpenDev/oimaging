/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.export;

import fr.jmmc.oiexplorer.core.gui.action.ExportDocumentAction;
import org.jfree.ui.Drawable;

/**
 * This interface defines a simple method to export a chart as document
 * @author bourgesl
 */
public interface DocumentExportable {

    /**
     * Export the component as a document using the given action:
     * the component should check if there is something to export ?
     * @param action export action to perform the export action
     */
    public void performAction(final ExportDocumentAction action);

    /**
     * Return the default file name
     * @param fileExtension  document's file extension
     * @return default file name
     */
    public String getDefaultFileName(final String fileExtension);

    /**
     * Prepare the page layout before doing the export:
     * Performs layout and modifies the given options
     * @param options document options used to prepare the document
     */
    public void prepareExport(final DocumentOptions options);

    /**
     * Return the page to export given its page index
     * @param pageIndex page index (1..n)
     * @return Drawable array to export on this page
     */
    public Drawable[] preparePage(final int pageIndex);

    /**
     * Callback indicating the export is done to reset the component's state
     */
    public void postExport();
}
