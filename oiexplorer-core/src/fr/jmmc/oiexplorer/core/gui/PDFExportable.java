/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui;

import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.oiexplorer.core.gui.chart.PDFOptions;
import org.jfree.chart.JFreeChart;

/**
 * This interface defines a simple method to export a chart as a PDF document
 * @author bourgesl
 */
public interface PDFExportable {

    /** PDF extension */
    public static final String PDF_EXT = MimeType.PDF.getExtension();

    /**
     * Export the chart component as a PDF document.
     */
    public void performPDFAction();

    /**
     * Return the PDF default file name
     * @return PDF default file name
     */
    public String getPDFDefaultFileName();

    /**
     * Prepare the chart(s) before exporting them as a PDF document:
     * Performs layout and return PDF options
     * @return PDF options
     */
    public PDFOptions preparePDFExport();

    /**
     * Return the chart to export on the given page index
     * @param pageIndex page index (1..n)
     * @return chart
     */
    public JFreeChart prepareChart(final int pageIndex);

    /**
     * Callback indicating the PDF document is done to reset the chart(s)
     */
    public void postPDFExport();
}
