/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.export;

import fr.jmmc.jmcs.data.MimeType;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author bourgesl
 */
public final class PDFOptions extends DocumentOptions {

    protected PDFOptions(final MimeType mimeType) {
        super(mimeType);
    }

    @Override
    public Rectangle2D.Float adjustDocumentSize() {
        // adjust document size (SMALL, A3, A2) and orientation according to the options :
        com.lowagie.text.Rectangle documentPage;
        switch (getDocumentSize()) {
            default:
            case SMALL:
                documentPage = com.lowagie.text.PageSize.A4;
                break;
            case NORMAL:
                documentPage = com.lowagie.text.PageSize.A3;
                break;
            case LARGE:
                documentPage = com.lowagie.text.PageSize.A2;
                break;
        }

        if (Orientation.Landscape == getOrientation()) {
            documentPage = documentPage.rotate();
        }
        return new Rectangle2D.Float(documentPage.getLeft(), documentPage.getBottom(),
                documentPage.getWidth(), documentPage.getHeight());
    }

    /**
     * Overwrite options of the method parameter if the command options are not null or default
     * @param otherOptions
     */
    @Override
    public void merge(final DocumentOptions otherOptions) {
        super.merge(otherOptions);
        logger.debug("merge(PDFOptions): this: {}", this);
    }
}
