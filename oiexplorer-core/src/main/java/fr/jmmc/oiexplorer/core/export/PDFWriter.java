/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.export;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.FontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.jmcs.data.app.ApplicationDescription;
import fr.jmmc.jmcs.util.FileUtils;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.jfree.chart.ui.Drawable;

/**
 * This class is dedicated to export charts as PDF documents
 * @author bourgesl
 */
public final class PDFWriter extends Writer {

    /**
     * Force text rendering to use java rendering as Shapes.
     * This is a workaround to get Unicode greek characters rendered properly.
     * Embedding fonts in the PDF may depend on the Java/OS font configuration ...
     */
    public final static boolean RENDER_TEXT_AS_SHAPES = true;

    /**
     * protected Constructor
     */
    PDFWriter() {
        // no-op
    }

    /**
     * Save the given chart as a PDF document in the given file
     * @param pdfFile PDF file to create
     * @param exportable exportable component
     * @param options PDF options
     *
     * @throws IOException if the file exists but is a directory
     *                   rather than a regular file, does not exist but cannot
     *                   be created, or cannot be opened for any other reason
     * @throws IllegalStateException if a PDF document exception occurred
     */
    @Override
    public void write(final File pdfFile, final DocumentExportable exportable, final DocumentOptions options)
            throws IOException, IllegalStateException {

        // Fix file extension:
        final File file = MimeType.PDF.checkFileExtension(pdfFile);

        final long start = System.nanoTime();

        BufferedOutputStream bo = null;
        try {
            bo = new BufferedOutputStream(new FileOutputStream(file));

            writeChartAsPDF(bo, exportable, options);

        } finally {
            FileUtils.closeStream(bo);

            if (logger.isInfoEnabled()) {
                logger.info("write[{}] : duration = {} ms.", file, 1e-6d * (System.nanoTime() - start));
            }
        }
    }

    /**
     * Create a PDF document with the given chart and save it in the given stream
     * @param outputStream output stream
     * @param exportable exportable component
     * @param options PDF options
     *
     * @throws IllegalStateException if a PDF document exception occurred
     */
    private static void writeChartAsPDF(final OutputStream outputStream,
            final DocumentExportable exportable, final DocumentOptions options) throws IllegalStateException {

        Graphics2D g2 = null;

        // adjust document size (A4, A3, A2) and orientation according to the options :
        final Rectangle2D.Float documentPage = options.adjustDocumentSize();

        final Document document = new Document(new Rectangle(documentPage.x,
                documentPage.y, documentPage.width, documentPage.height));

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
        final float marginCM = 0.5f;
        // in points :
        final float margin = marginCM * 72f / 2.54f;

        final float innerWidth = width - 2 * margin;
        final float innerHeight = height - 2 * margin;

        try {
            final PdfWriter writer = PdfWriter.getInstance(document, outputStream);

            document.open();

            definePDFProperties(document);

            PdfTemplate pdfTemplate;
            Drawable[] drawables;

            final PdfContentByte pdfContentByte = writer.getDirectContent();

            for (int pageIndex = 1, numberOfPages = options.getNumberOfPages(); pageIndex <= numberOfPages; pageIndex++) {
                // new page:
                document.newPage();

                pdfTemplate = pdfContentByte.createTemplate(width, height);

                if (RENDER_TEXT_AS_SHAPES) {
                    // text rendered as shapes so the file is bigger but correct
                    g2 = pdfTemplate.createGraphicsShapes(innerWidth, innerHeight);
                } else {
                    // depending on the font mapper, special characters like greek chars are not rendered:
                    g2 = pdfTemplate.createGraphics(innerWidth, innerHeight, getFontMapper());
                }

                // Get Drawables:
                drawables = exportable.preparePage(pageIndex);

                draw(drawables, g2, innerWidth, innerHeight);

                pdfContentByte.addTemplate(pdfTemplate, margin, margin);

                // free graphics:
                g2.dispose();
                g2 = null;
            }

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
        document.addCreator(ApplicationDescription.getInstance().getProgramNameWithVersion());
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
