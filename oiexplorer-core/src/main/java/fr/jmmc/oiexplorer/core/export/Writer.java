/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.export;

import fr.jmmc.jmcs.data.MimeType;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import org.jfree.ui.Drawable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author grosje
 */
public abstract class Writer {

    /** Class logger */
    protected static final Logger logger = LoggerFactory.getLogger(Writer.class.getName());

    /** flag to draw a red border around the rectangle area */
    private final static boolean DEBUG_RECTANGLE = false;

    /**
     * Give the instance that corresponds to the mime type
     * @param mimeType mime type
     * @return Wanted instance
     */
    public static Writer getInstance(final MimeType mimeType) {
        if (mimeType == MimeType.PDF) {
            return new PDFWriter();
        }
        if (mimeType == MimeType.PNG || mimeType == MimeType.JPG) {
            return new ImageWriter();
        }
        throw new IllegalStateException("Unsuported Mime type [" + mimeType.getId()
                + "] (only PDF, PNG or JPG expected) !");
    }

    /**
     * Save the given exportable component as a document in the given file
     * @param file file to create
     * @param exportable exportable component
     * @param options options
     *
     * @throws IOException if the file exists but is a directory
     *                   rather than a regular file, does not exist but cannot
     *                   be created, or cannot be opened for any other reason
     * @throws IllegalStateException if a document exception occurred
     */
    public abstract void write(final File file,
            final DocumentExportable exportable,
            final DocumentOptions options) throws IOException, IllegalStateException;

    /**
     * Draw the given Drawable instances on the given Graphics2D
     * @param drawables array of Drawable
     * @param g2 graphic object
     * @param width of the page
     * @param height of the page
     */
    public static void draw(Drawable[] drawables, Graphics2D g2,
            final float width, final float height) {

        final int nCharts = drawables.length;
        final int nbCol = (nCharts > 1) ? 2 : 1;
        final int nbLine = (nCharts / nbCol) + (nCharts % nbCol);

        for (int i = 0; i < nCharts; i++) {
            final int heightAdjust = i / nbCol;

            final Rectangle2D drawArea = new Rectangle2D.Double(
                    (i % nbCol) * width / nbCol,
                    (heightAdjust * height) / nbLine,
                    width / nbCol,
                    height / nbLine);

            //draw chart:
            drawables[i].draw(g2, drawArea);

            if (DEBUG_RECTANGLE) {
                g2.setColor(Color.RED);
                g2.draw(drawArea);
            }
        }
    }

}
