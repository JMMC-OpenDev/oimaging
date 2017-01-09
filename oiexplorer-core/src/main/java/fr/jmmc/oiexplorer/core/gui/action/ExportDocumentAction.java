/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.action;

import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.jmcs.gui.action.ActionRegistrar;
import fr.jmmc.jmcs.gui.component.FileChooser;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.oiexplorer.core.export.DocumentExportable;
import fr.jmmc.oiexplorer.core.export.DocumentMode;
import fr.jmmc.oiexplorer.core.export.DocumentOptions;
import fr.jmmc.oiexplorer.core.export.Writer;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This registered action represents a File Menu entry to export any chart as a document.
 *
 * @author bourgesl
 */
public abstract class ExportDocumentAction extends WaitingTaskAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class name. This name is used to register to the ActionRegistrar */
    protected final static String className = ExportDocumentAction.class.getName();
    /** Class logger */
    protected static final Logger logger = LoggerFactory.getLogger(className);
    /** Action name. This prefix is used to register to the ActionRegistrar */
    public final static String actionNamePrefix = "export";
    /** flag to avoid use StatusBar (JUnit) */
    private static boolean avoidUseStatusBar = false;

    /**
     * Return the ExportDocumentAction instance given a Mime type
     * @param mimeType exported document's MimeType
     * @return ExportDocumentAction instance or null if undefined
     */
    public static ExportDocumentAction getInstance(final MimeType mimeType) {
        return (ExportDocumentAction) ActionRegistrar.getInstance().get(className, getActionName(mimeType));
    }

    /**
     * Export the given exportable instance as a document
     *
     * @param exportable component
     * @param file file to write into
     * @param options parameters of the document including width, height and the document mode
     */
    public static void export(final DocumentExportable exportable,
            final File file, final DocumentOptions options) {
        getInstance(options.getMimeType()).process(exportable, file, options);
    }

    /**
     * Define the flag to avoid use StatusBar (JUnit)
     * @param flag true to avoid use StatusBar
     */
    public static void setAvoidUseStatusBar(final boolean flag) {
        avoidUseStatusBar = flag;
    }

    /**
     * Generate an action name given a Mime type
     * @param mimeType Mime type to use
     * @return 'export' + MimeType.ID
     */
    public static String getActionName(final MimeType mimeType) {
        return actionNamePrefix + mimeType.getId();
    }

    /* members */
    /** associated MimeType */
    private final MimeType mimeType;

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     * @param mimeType exported document's MimeType
     */
    public ExportDocumentAction(final MimeType mimeType) {
        this(mimeType, className);
    }

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     * @param mimeType exported document's MimeType
     * @param classPath the path of the class containing the field pointing to
     * the action, in the form returned by 'getClass().getName();'.
     */
    public ExportDocumentAction(final MimeType mimeType, final String classPath) {
        super(classPath, getActionName(mimeType));
        this.mimeType = mimeType;
    }

    /**
     * Handle the action event
     */
    @Override
    public void actionPerformed() {
        logger.debug("actionPerformed");

        final DocumentExportable selectedPanel = getSelectedComponent();

        // be sure the selected panel implements DocumentExportable (not null of course) :
        if (selectedPanel != null) {
            selectedPanel.performAction(this);
        }
    }

    /**
     * Return the currently selected GUI panel (JPanel ...) to export it (DocumentExportable)
     * @return selected GUI panel as DocumentExportable or null
     */
    protected abstract DocumentExportable getSelectedComponent();

    /**
     * Export the given exportable as a document in A4/portrait
     *
     * @param exportable component
     */
    public void process(final DocumentExportable exportable) {
        process(exportable, null, null);
    }

    /**
     * Export the given exportable as a document
     * @param exportable exportable component
     * @param proposedFile file to write into or null to open a file chooser
     * @param cmdOptions document options or null to get default output
     */
    public void process(final DocumentExportable exportable, final File proposedFile,
            final DocumentOptions cmdOptions) {

        logger.debug("process");

        final File file;
        if (proposedFile == null) {
            // GUI mode:
            file = FileChooser.showSaveFileChooser("Export the plot to " + mimeType.getId(), null, mimeType,
                    exportable.getDefaultFileName(mimeType.getExtension()));
        } else {
            // command line mode:
            file = proposedFile;
        }

        // If a file was defined (No cancel in the dialog)
        if (file != null) {

            // prepare file export (layout and options):
            final DocumentOptions docOptions = DocumentOptions.createInstance(mimeType);
            // Set document mode used by prepareExport():
            docOptions.setMode((cmdOptions != null) ? cmdOptions.getMode() : DocumentMode.DEFAULT);

            // Perform Layout and get options from component:
            exportable.prepareExport(docOptions);
            try {
                logger.debug("docOptions: {}", docOptions);

                // check if there is anything to export:
                if (docOptions.getNumberOfPages() == 0) {
                    logger.info("Nothing to export to file : {}", file);
                } else {
                    if (cmdOptions != null) {
                        // overwrite options with given command line params:
                        docOptions.merge(cmdOptions);
                    }

                    Writer.getInstance(mimeType).write(file, exportable, docOptions);

                    if (!avoidUseStatusBar) {
                        StatusBar.show(file.getName() + " created.");
                    }
                }

            } catch (IOException ioe) {
                MessagePane.showErrorMessage("Could not write to file : " + file.getAbsolutePath(), ioe);
            } finally {
                // post file export: restore Chart state if modified:
                exportable.postExport();
            }
        }
    }

}
