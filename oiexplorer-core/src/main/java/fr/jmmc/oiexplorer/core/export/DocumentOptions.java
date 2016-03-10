/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.export;

import fr.jmmc.jmcs.data.MimeType;
import java.awt.geom.Rectangle2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bourgesl
 */
public abstract class DocumentOptions {

    /** Class logger */
    protected static final Logger logger = LoggerFactory.getLogger(DocumentOptions.class.getName());

    // Factory pattern
    /**
     * Create the options of the export document
     *@param mimeType mime type of the export document
     *@return options of the export document depending on the mime type
     *@throws IllegalArgumentException
     */
    public static DocumentOptions createInstance(final MimeType mimeType) throws IllegalArgumentException {
        if (mimeType == MimeType.PDF) {
            return new PDFOptions(mimeType);
        }
        if (mimeType == MimeType.PNG || mimeType == MimeType.JPG) {
            return new ImageOptions(mimeType);
        }
        throw new IllegalArgumentException("Unsupported mimetype : " + mimeType);
    }

    /* members */
    /** mime type */
    private final MimeType mimeType;
    /** Mode of exportation (single/multi/default) */
    private DocumentMode mode = null;
    /** Page size */
    private DocumentSize documentSize = null;
    /** Page orientation */
    private Orientation orientation = null;
    /** number of pages */
    private int numberOfPages = 0;

    protected DocumentOptions(final MimeType mimeType) {
        assert (mimeType != null);
        this.mimeType = mimeType;
    }

    /**
     * Adjusts the size of the document depending on the nature of the document
     * @return a rectangle with calculated size
     */
    public abstract Rectangle2D.Float adjustDocumentSize();

    /**
     *Overwrite options of the method parameter if the command options are not null or default
     * @param other default options of exporting
     */
    public void merge(DocumentOptions other) {
        logger.debug("merge: other : {} this : {}", other, this);
        if (this.getClass() != other.getClass()) {
            throw new IllegalStateException("Incompatible DocumentOptions class !");
        }
        if (this.getMimeType() != other.getMimeType()) {
            throw new IllegalStateException("Incompatible mimetype !");
        }
        if (other.getMode() != null) {
            this.setMode(other.getMode());
        }
        if (other.getDocumentSize() != null) {
            this.setDocumentSize(other.getDocumentSize());
        }
        if (other.getOrientation() != null) {
            this.setOrientation(other.getOrientation());
        }
        if (other.getNumberOfPages() != 0) {
            this.setNumberOfPages(other.getNumberOfPages());
        }
        logger.debug("merge(DocumentOptions): this: {}", this);
    }
    
    /**
     * Set default options: DocumentSize: Normal, Orientation: Landscape, 1 page
    */
    public final void setNormalDefaults() {
        setDocumentSize(DocumentSize.NORMAL)
                .setOrientation(Orientation.Landscape)
                .setNumberOfPages(1);        
    }
    /**
     * Set default options: DocumentSize: Normal, Orientation: Landscape, 1 page
    */
    public final void setSmallDefaults() {
        setDocumentSize(DocumentSize.SMALL)
                .setOrientation(Orientation.Landscape)
                .setNumberOfPages(1);        
    }

    /** Return the mime type
     @return mime type
     */
    public MimeType getMimeType() {
        return mimeType;
    }

    /** Return the mode
     * @return the mode
     */
    public DocumentMode getMode() {
        return mode;
    }

    /** Set the mode within a Document mode parameter
     * @param mode
     * @return this (fluent API)
     */
    public DocumentOptions setMode(DocumentMode mode) {
        this.mode = mode;
        return this;
    }

    /** Set the mode of exportation (single/multi/default)
     * @param mode
     * @return the mode
     * @throws IllegalArgumentException
     */
    public DocumentOptions setMode(String mode) throws IllegalArgumentException {
        //mode verifications
        if (mode == null) {
            return setMode(DocumentMode.DEFAULT);
        }
        return setMode(DocumentMode.parse(mode));
    }

    /**
     * Return the page size
     * @return page size
     */
    public DocumentSize getDocumentSize() {
        return documentSize;
    }

    /**
     * @param documentSize
     * @return this (fluent API)
     */
    public DocumentOptions setDocumentSize(DocumentSize documentSize) {
        this.documentSize = documentSize;
        return this;
    }

    /**
     * Return the page orientation
     * @return page orientation
     */
    public Orientation getOrientation() {
        return orientation;
    }

    /**
     * Set the page orientation
     * @param orientation
     * @return this (fluent API)
     */
    public DocumentOptions setOrientation(Orientation orientation) {
        this.orientation = orientation;
        return this;
    }

    /**
     * Return the number of pages
     * @return number of pages
     */
    public int getNumberOfPages() {
        return numberOfPages;
    }

    /**
     * Set the number of pages
     * @param numberOfPages
     * @return this (fluent API)
     */
    public DocumentOptions setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
        return this;
    }

    @Override
    public String toString() {
        return "mimeType: " + getMimeType()
                + " mode: " + getMode() + " documentSize: " + getDocumentSize()
                + " orientation: " + getOrientation() + " numberOfPages: " + getNumberOfPages();
    }
}
