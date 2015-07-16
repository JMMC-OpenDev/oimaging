/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.action;

import fr.jmmc.jmcs.gui.action.ActionRegistrar;
import fr.jmmc.jmcs.gui.component.FileChooser;
import fr.jmmc.jmcs.gui.component.MessagePane;
import fr.jmmc.jmcs.gui.component.StatusBar;
import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.oiexplorer.core.gui.PDFExportable;
import fr.jmmc.oiexplorer.core.gui.chart.PDFOptions;
import fr.jmmc.oiexplorer.core.gui.chart.PDFUtils;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This registered action represents a File Menu entry to export any chart as a PDF document.
 *
 * @author bourgesl
 */
public abstract class ExportPDFAction extends WaitingTaskAction {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** Class name. This name is used to register to the ActionRegistrar */
    protected final static String className = ExportPDFAction.class.getName();
    /** Class logger */
    protected static final Logger logger = LoggerFactory.getLogger(className);
    /** Action name. This name is used to register to the ActionRegistrar */
    public final static String actionName = "exportPDF";
    /** PDF MimeType */
    private final static MimeType mimeType = MimeType.PDF;
    /** flag to avoid use StatusBar (JUnit) */
    private static boolean avoidUseStatusBar = false;

    /**
     * Return the singleton ExportPDFAction instance
     * @return ExportPDFAction instance
     */
    public static ExportPDFAction getInstance() {
        return (ExportPDFAction) ActionRegistrar.getInstance().get(className, actionName);
    }

    /**
     * Export the given exportable chart as a PDF document
     *
     * @param exportable component
     */
    public static void exportPDF(final PDFExportable exportable) {
        getInstance().process(exportable);
    }

    /**
     * Define the flag to avoid use StatusBar (JUnit)
     * @param flag true to avoid use StatusBar
     */
    public static void setAvoidUseStatusBar(final boolean flag) {
        avoidUseStatusBar = flag;
    }

    /**
     * Public constructor that automatically register the action in RegisteredAction.
     */
    public ExportPDFAction() {
        super(className, actionName);
    }

    /**
     * Handle the action event
     */
    @Override
    public void actionPerformed() {
        logger.debug("actionPerformed");

        final Component selectedPanel = getSelectedComponent();

        // be sure the selected panel implements PDFExportable (not null of course) :
        if (selectedPanel instanceof PDFExportable) {
            ((PDFExportable) selectedPanel).performPDFAction();
        }
    }

    /**
     * Return the currently selected GUI panel (JPanel ...) to determine if it is possible to export it (PDFExportable)
     * @return selected GUI panel
     */
    protected abstract Component getSelectedComponent();

    /**
     * Export the given chart as a PDF document
     * @param exportable exportable component
     */
    public void process(final PDFExportable exportable) {
        logger.debug("process");

        final File file = FileChooser.showSaveFileChooser("Export the plot to PDF", null, mimeType, exportable.getPDFDefaultFileName());

        // If a file was defined (No cancel in the dialog)
        if (file != null) {

            // prepare PDF export (layout and options):
            final PDFOptions options = exportable.preparePDFExport();
            try {
                PDFUtils.savePDF(file, exportable, options);

                if (!avoidUseStatusBar) {
                    StatusBar.show(file.getName() + " created.");
                }

            } catch (IOException ioe) {
                MessagePane.showErrorMessage("Could not write to file : " + file.getAbsolutePath(), ioe);
            } finally {
                // post PDF export: restore Chart state if modified:
                exportable.postPDFExport();
            }
        }
    }
}
