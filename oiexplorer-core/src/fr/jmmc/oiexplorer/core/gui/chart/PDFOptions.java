/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.chart;

/**
 *
 * @author bourgesl
 */
public final class PDFOptions {

    /** default PDF options : A4 - Landscape */
    public static final PDFOptions DEFAULT_PDF_OPTIONS = new PDFOptions(PageSize.A4, Orientation.Landscape);

    public enum PageSize {

        A4, A3, A2;
    }

    public enum Orientation {

        Portait, Landscape;
    }
    private final PageSize pageSize;
    private final Orientation orientation;

    public PDFOptions(final PageSize pageSize, final Orientation orientation) {
        this.pageSize = pageSize;
        this.orientation = orientation;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public PageSize getPageSize() {
        return pageSize;
    }

    @Override
    public String toString() {
        return "page: " + getPageSize() + " orientation: " + getOrientation();
    }
}
