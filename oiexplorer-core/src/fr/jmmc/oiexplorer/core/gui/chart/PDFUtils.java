/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.chart;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.FontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import fr.jmmc.jmcs.App;
import fr.jmmc.jmcs.util.FileUtils;
import fr.jmmc.oiexplorer.core.gui.chart.PDFOptions.Orientation;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.jfree.chart.JFreeChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is dedicated to export charts as PDF documents
 * @author bourgesl
 */
public final class PDFUtils {

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(PDFUtils.class.getName());
    /** 
     * Force text rendering to use java rendering as Shapes.
     * This is a workaround to get Unicode greek characters rendered properly.
     * Embedding fonts in the PDF may depend on the Java/OS font configuration ...
     */
    public final static boolean RENDER_TEXT_AS_SHAPES = true;

    /**
     * Private Constructor
     */
    private PDFUtils() {
        // no-op
    }

    /**
     * Save the given chart as a PDF document in the given file
     * @param file PDF file to create
     * @param chart chart to export
     * @param options PDF options
     *
     * @throws IOException if the file exists but is a directory
     *                   rather than a regular file, does not exist but cannot
     *                   be created, or cannot be opened for any other reason
     * @throws IllegalStateException if a PDF document exception occurred
     */
    public static void saveChartAsPDF(final File file, final JFreeChart chart, final PDFOptions options)
            throws IOException, IllegalStateException {

        final long start = System.nanoTime();

        BufferedOutputStream bo = null;
        try {
            bo = new BufferedOutputStream(new FileOutputStream(file));

            writeChartAsPDF(bo, chart, options);

        } finally {
            FileUtils.closeStream(bo);

            if (logger.isInfoEnabled()) {
                logger.info("saveChartAsPDF[{}] : duration = {} ms.", file, 1e-6d * (System.nanoTime() - start));
            }
        }
    }

    /**
     * Create a PDF document with the given chart and save it in the given stream
     * @param outputStream output stream
     * @param chart chart instance
     * @param options PDF options
     * 
     * @throws IllegalStateException if a PDF document exception occurred
     */
    private static void writeChartAsPDF(final OutputStream outputStream, final JFreeChart chart,
            final PDFOptions options) throws IllegalStateException {

        Graphics2D g2 = null;

        // adjust document size (A4, A3, A2) and orientation according to the options :
        final Rectangle documentPage;
        switch (options.getPageSize()) {
            case A2:
                documentPage = com.lowagie.text.PageSize.A2;
                break;
            case A3:
                documentPage = com.lowagie.text.PageSize.A3;
                break;
            default:
            case A4:
                documentPage = com.lowagie.text.PageSize.A4;
                break;
        }

        final Document document;
        if (Orientation.Landscape == options.getOrientation()) {
            document = new Document(documentPage.rotate());
        } else {
            document = new Document(documentPage);
        }

        final Rectangle pdfRectangle = document.getPageSize();

        final float width = (int) pdfRectangle.getWidth();
        final float height = (int) pdfRectangle.getHeight();

        /*
         Measurements
         When creating a rectangle or choosing a margin, you might wonder what measurement unit is used:
         centimeters, inches or pixels.
         In fact, the default measurement system roughly corresponds to the various definitions of the typographic
         unit of measurement known as the point. There are 72 points in 1 inch (2.54 cm).
         */

        // margin = 1 cm :
        final float marginCM = 1f;
        // in points :
        final float margin = marginCM * 72f / 2.54f;

        final float innerWidth = width - 2 * margin;
        final float innerHeight = height - 2 * margin;

        try {
            final PdfWriter writer = PdfWriter.getInstance(document, outputStream);

            document.open();

            definePDFProperties(document);

            final PdfContentByte pdfContentByte = writer.getDirectContent();

            final PdfTemplate pdfTemplate = pdfContentByte.createTemplate(width, height);

            if (RENDER_TEXT_AS_SHAPES) {
                // text rendered as shapes so the file is bigger but correct
                g2 = pdfTemplate.createGraphicsShapes(innerWidth, innerHeight);
            } else {
                // depending on the font mapper, special characters like greek chars are not rendered:
                g2 = pdfTemplate.createGraphics(innerWidth, innerHeight, getFontMapper());
            }

            final Rectangle2D.Float drawArea = new Rectangle2D.Float(0F, 0F, innerWidth, innerHeight);

            chart.draw(g2, drawArea);

            pdfContentByte.addTemplate(pdfTemplate, margin, margin);

        } catch (DocumentException de) {
            throw new IllegalStateException("PDF document exception : ", de);
        } finally {
            if (g2 != null) {
                g2.dispose();
            }
            document.close();
        }
    }

    /**
     * Define PDF properties (margins, author, creator ...)
     * @param document pdf document
     */
    private static void definePDFProperties(final Document document) {
        document.addCreator(App.getSharedApplicationDataModel().getProgramName() + " v" + App.getSharedApplicationDataModel().getProgramVersion());
    }

    /**
     * Return the font mapper used to translate Java2D Fonts to PDF Fonts (virtual or embedded)
     * @return font mapper
     */
    private static FontMapper getFontMapper() {

        // ChartFontMapper (test) substitutes SansSerif fonts (plain and bold) by DejaVu fonts which supports both unicode and greek characters
        // However, fonts must be distributed (GPL) and many problems can happen...
    /* return new ChartFontMapper(); */

        // default font mapper uses Helvetica (Cp1252) which DOES NOT SUPPORT UNICODE CHARACTERS:
        return new DefaultFontMapper();
    }
}
