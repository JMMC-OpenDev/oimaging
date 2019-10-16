/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.export;

import fr.jmmc.jmcs.data.MimeType;
import fr.jmmc.jmcs.util.FileUtils;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.jfree.chart.ui.Drawable;

/**
 * This class is dedicated to export charts as images
 * @author Jean-Philippe GROS.
 */
public final class ImageWriter extends Writer {

    /**
     * Protected Constructor
     */
    ImageWriter() {
        // no-op
    }

    /**
     * Create an image with the given chart and save it in the given stream
     * @param imgBaseFile output stream
     * @param exportable exportable component
     * @param options image options
     *
     * @throws IllegalStateException if a document exception occurred
     */
    @Override
    public void write(final File imgBaseFile,
            final DocumentExportable exportable,
            final DocumentOptions options) throws IllegalStateException {

        final long start = System.nanoTime();

        Graphics2D g2 = null;

        // adjust document size (A4, A3, A2) and orientation according to the options :
        final Rectangle2D documentPage = options.adjustDocumentSize();

        final int width = (int) Math.round(documentPage.getWidth());
        final int height = (int) Math.round(documentPage.getHeight());

        logger.debug("Image size: [{} x {}]", width, height);

        final float innerWidth = width;
        final float innerHeight = height;

        final MimeType mimeType = options.getMimeType();

        try {
            for (int pageIndex = 1, numberOfPages = options.getNumberOfPages(); pageIndex <= numberOfPages; pageIndex++) {

                // force RGB for JPEG (RGBA encoder bug):
                final BufferedImage image = prepareImage(width, height,
                        (MimeType.JPG == mimeType));

                g2 = prepareGraphics2D(image, width, height);

                // Get Drawables:
                final Drawable[] drawables = exportable.preparePage(pageIndex);

                draw(drawables, g2, innerWidth, innerHeight);

                final File imgFinalFile = generateFile(imgBaseFile, pageIndex, mimeType);

                logger.info("writing image: {}", imgFinalFile.getAbsolutePath());

                // TODO later: compression options ...
                // Supported Formats: [jpg, bmp, gif, png]
                ImageIO.write(image, mimeType.getExtension(), imgFinalFile);

                // free graphics:
                g2.dispose();
                g2 = null;
            }
        } catch (IOException ioe) {
            throw new IllegalStateException("IO exception : ", ioe);
        } finally {
            if (g2 != null) {
                g2.dispose();
            }
            if (logger.isInfoEnabled()) {
                logger.info("write[{}] : duration = {} ms.", imgBaseFile, 1e-6d * (System.nanoTime() - start));
            }
        }
    }

    static BufferedImage prepareImage(final int width, final int height, final boolean useRGB) {
        return new BufferedImage(width, height,
                (useRGB) ? BufferedImage.TYPE_INT_RGB : BufferedImage.TYPE_INT_ARGB_PRE);
    }

    static Graphics2D prepareGraphics2D(final BufferedImage image, final int width, final int height) {
        final Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setBackground(Color.WHITE);
        g2d.clearRect(0, 0, width, height);

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        return g2d;
    }

    /**
     * Check if the given file has an accepted extension.
     * If not, return a new file with the first accepted extension
     * @param imgBaseFile file to check
     * @param pageIndex index of the page which will be saved
     * @param mimeType MimeType to get its file extension
     * @return given file or new file with the first accepted extension
     */
    static File generateFile(final File imgBaseFile, int pageIndex, final MimeType mimeType) {
        // add or replace current extension by the first accepted extension:
        final String fileNamePart = FileUtils.getFileNameWithoutExtension(imgBaseFile);
        // add page index if page index > 1
        if (pageIndex > 1) {
            return new File(imgBaseFile.getParentFile(),
                    fileNamePart + '-' + pageIndex + '.' + mimeType.getExtension());
        }
        return new File(imgBaseFile.getParentFile(),
                fileNamePart + '.' + mimeType.getExtension());
    }

}
