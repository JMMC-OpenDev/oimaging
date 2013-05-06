/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.chart;

/**
 *
 * @author bourgesl
 */
public final class PDFOptions {

    /** PDF options : A4 - Landscape (1 page)*/
    public static final PDFOptions PDF_A4_LANDSCAPE = new PDFOptions(PageSize.A4, Orientation.Landscape);
    /** PDF options : A3 - Landscape (1 page)*/
    public static final PDFOptions PDF_A3_LANDSCAPE = new PDFOptions(PageSize.A3, Orientation.Landscape);
    /** default PDF options : A4 - Landscape */
    public static final PDFOptions DEFAULT_PDF_OPTIONS = PDF_A4_LANDSCAPE;

    /** Page size enumeration */
    public enum PageSize {

        /** A4 */
        A4,
        /** A3 */
        A3,
        /** A2 */
        A2;
    }

    /** Page orientation enumeration */
    public enum Orientation {

        /** Portrait orientation */
        Portait,
        /** Landscape orientation */
        Landscape;
    }
    /* members */
    /** Page size */
    private final PageSize pageSize;
    /** Page orientation */
    private final Orientation orientation;
    /** number of pages */
    private final int numberOfPages;

    /**
     * Public constructor with only 1 page
     * @param pageSize page size
     * @param orientation page orientation
     */
    public PDFOptions(final PageSize pageSize, final Orientation orientation) {
        this(pageSize, orientation, 1);
    }

    /**
     * Public constructor
     * @param pageSize page size
     * @param orientation page orientation
     * @param numberOfPages number of pages
     */
    public PDFOptions(final PageSize pageSize, final Orientation orientation, final int numberOfPages) {
        this.pageSize = pageSize;
        this.orientation = orientation;
        this.numberOfPages = numberOfPages;
    }

    /**
     * Return the page size
     * @return page size
     */
    public PageSize getPageSize() {
        return pageSize;
    }

    /**
     * Return the page orientation
     * @return page orientation
     */
    public Orientation getOrientation() {
        return orientation;
    }

    /**
     * Return the number of pages
     * @return number of pages
     */
    public int getNumberOfPages() {
        return numberOfPages;
    }

    @Override
    public String toString() {
        return "page: " + getPageSize() + " orientation: " + getOrientation() + " numberOfPages: " + getNumberOfPages();
    }
}
