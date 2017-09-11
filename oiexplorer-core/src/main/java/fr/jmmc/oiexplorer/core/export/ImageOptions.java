/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.export;

import fr.jmmc.jmcs.data.MimeType;
import java.awt.geom.Rectangle2D;

/**
 This class provides the document parameters for images.
 */
public final class ImageOptions extends DocumentOptions {

    private int width = 0;
    private int height = 0;

    // TODO: compression parameters ?
    protected ImageOptions(final MimeType mimeType) {
        super(mimeType);
    }

    @Override
    public Rectangle2D.Float adjustDocumentSize() {
        final float w, h;
        Rectangle2D.Float documentPage;

        if (width > 0 || height > 0) {
            w = width;
            h = height;
            documentPage = new Rectangle2D.Float(0f, 0f, w, h);
        } else {
            // Base pixel dimensions for SMALL:
            final float longEdge = pt(29.7);
            final float shortEdge = pt(21.0);

            if (Orientation.Landscape == getOrientation()) {
                w = longEdge;
                h = shortEdge;
            } else {
                w = shortEdge;
                h = longEdge;
            }
            switch (getDocumentSize()) {
                case LARGE:
                    documentPage = new Rectangle2D.Float(0f, 0f, 4f * w, 4f * h);
                    break;
                case NORMAL:
                    documentPage = new Rectangle2D.Float(0f, 0f, 2f * w, 2f * h);
                    break;
                default:
                case SMALL:
                    documentPage = new Rectangle2D.Float(0f, 0f, w, h);
                    break;
            }
        }
        return documentPage;
    }

    /**
     *Overwrite options of the method parameter if the command options are not null or default
     * @param otherOptions
     */
    @Override
    public void merge(final DocumentOptions otherOptions) {
        super.merge(otherOptions);

        final ImageOptions other = (ImageOptions) otherOptions;

        if (other.getWidth() != 0) {
            this.setWidth(other.getWidth());
        }
        if (other.getHeight() != 0) {
            this.setHeight(other.getHeight());
        }
        logger.debug("merge(ImageOptions): this: {}", this);
    }

    /**
     * Checks if the arguments are correct and sets width / height
     * @param dimensions dimensions of the export file (width,height)
     * @return the document option
     * @throws IllegalStateException
     */
    public ImageOptions setDimensions(final String dimensions) throws IllegalArgumentException {
        int w, h;
        if (dimensions == null) {
            w = 0;
            h = 0;
        } else if (!dimensions.matches("[0-9]{2,}[,][0-9]{2,}")) {
            throw new IllegalArgumentException("wrong format for dimensions: '" + dimensions + "' must be of form [int,int] !");
        } else {
            final String[] dims = dimensions.split(",");
            w = Integer.parseInt(dims[0]);
            h = Integer.parseInt(dims[1]);
            if (w < 250 || h < 250 || w > 2500 || h > 2500) {
                throw new IllegalArgumentException("Dimensions error, size must be between 250 and 2500 (pixels)");
            }
        }

        return this.setWidth(w).setHeight(h); // fluent API
    }

    /** set the width
     @param width
     @return this (fluent API)
     */
    public ImageOptions setWidth(int width) {
        this.width = width;
        return this;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /** set the height
     @param height
     @return this (fluent API)
     */
    public ImageOptions setHeight(int height) {
        this.height = height;
        return this;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return super.toString() + "width: " + getWidth() + " height: " + getHeight();
    }

    static float pt(final double cm) {
        return (float) (30.0 * cm); // ~ 72 dpi
    }
}
