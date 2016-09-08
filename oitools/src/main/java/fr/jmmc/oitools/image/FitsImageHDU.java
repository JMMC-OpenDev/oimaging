/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.image;

import fr.jmmc.oitools.fits.FitsHeaderCard;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * This class is a container (HDU) for Fits Image (single or Fits cube)
 * @author bourgesl
 */
public final class FitsImageHDU {
    /* members */

    /** FITS HDU index */
    private int hduIndex = 0;
    /** CRC checksum of the complete HDU */
    private long checksum = 0l;
    /** optional list of header cards */
    private ArrayList<FitsHeaderCard> headerCards = null;
    /** Storage of fits image references */
    private final List<FitsImage> fitsImages = new LinkedList<FitsImage>();
    /** optional HDUNAME value for IMAGE-OI */
    private FitsHeaderCard optionalHduNameHeaderCard = null;

    /**
     * Public FitsImageHDU class constructor
     */
    public FitsImageHDU() {
        super();
    }

    /* image meta data */
    /**
     * Return the FITS HDU index
     * @return FITS HDU index
     */
    public int getHduIndex() {
        return hduIndex;
    }

    /**
     * Define the FITS HDU index
     * @param hduIndex FITS HDU index
     */
    public void setHduIndex(final int hduIndex) {
        this.hduIndex = hduIndex;
    }

    /**
     * Define the HDUNAME keyword for IMAGE-OI model.
     *
     * @param hduName value of keyword or null to remove it.
     */
    public void setHduName(String hduName) {
        if (optionalHduNameHeaderCard != null) {
            getHeaderCards().remove(optionalHduNameHeaderCard);
        }
        if (hduName != null) {
            optionalHduNameHeaderCard = new FitsHeaderCard(ImageOiConstants.KEYWORD_HDUNAME, hduName, ImageOiConstants.KEYWORD_DESCRIPTION_HDUNAME);
            getHeaderCards().add(optionalHduNameHeaderCard);
        }
    }

    /**
     * Get the optional HDUNAME keyword for IMAGE-OI model.
     * @return  the hduName value of null
     */
    public String getHduName() {
        return optionalHduNameHeaderCard == null ? null : optionalHduNameHeaderCard.getValue();
    }

    /**
     * Return the number of Fits images present in this FitsImageFile structure
     * @return number of Fits images
     */
    public int getImageCount() {
        return this.fitsImages.size();
    }

    /**
     * Return the list of Fits images
     * @return list of Fits images
     */
    public List<FitsImage> getFitsImages() {
        return this.fitsImages;
    }

    /**
     * Return true if this image belongs to a Fits cube
     * @return true if this image belongs to a Fits cube
     */
    public boolean isFitsCube() {
        return getImageCount() > 1;
    }

    /**
     * Return the CRC checksum of the complete HDU
     * @return CRC checksum of the complete HDU
     */
    public long getChecksum() {
        return checksum;
    }

    /**
     * Define the CRC checksum of the complete HDU
     * @param checksum CRC checksum of the complete HDU
     */
    public void setChecksum(final long checksum) {
        this.checksum = checksum;
    }

    /**
     * Return the list of header cards
     * @param nCards number of cards to define the initial capacity of the list
     * @return list of header cards
     */
    public List<FitsHeaderCard> getHeaderCards(final int nCards) {
        if (this.headerCards == null) {
            this.headerCards = new ArrayList<FitsHeaderCard>(nCards);
        }
        return this.headerCards;
    }

    /**
     * Trim the list of header cards
     */
    public void trimHeaderCards() {
        if (this.headerCards != null) {
            if (this.headerCards.size() > 0) {
                this.headerCards.trimToSize();
            } else {
                this.headerCards = null;
            }
        }
    }

    /**
     * Return the list of header cards
     * @return list of header cards
     */
    public List<FitsHeaderCard> getHeaderCards() {
        return getHeaderCards(10);
    }

    /**
     * Return a string representation of the list of header cards
     * @param separator separator to use after each header card
     * @return string representation of the list of header cards
     */
    public String getHeaderCardsAsString(final String separator) {
        if (this.headerCards == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(1024);
        for (FitsHeaderCard h : this.headerCards) {
            h.toString(sb);
            sb.append(separator);
        }
        return sb.toString();
    }

    /**
     * Returns a string representation of this Fits image
     * @return a string representation of this Fits image
     */
    @Override
    public String toString() {
        return toString(true);
    }

    /**
     * Returns a string representation of this Fits image
     * @param dumpHeaderCards true to dump also header cards
     * @return a string representation of this Fits image
     */
    public String toString(final boolean dumpHeaderCards) {
        return "FitsImageHDU[HDU#" + getHduIndex() + "][" + getImageCount() + ']'
                + ((dumpHeaderCards) ? "{\n" + getHeaderCardsAsString("\n") + '}' : "")
                + '\n' + getFitsImages();
    }

    /**
     * Return an HTML representation of the fitsImageHDU used by tooltips.
     * @param sb string buffer to use (cleared)
     * @return HTML representation as String
     */
    public final String toHtml(final StringBuffer sb) {
        sb.setLength(0); // clear
        sb.append("<html>");
        toHtml(sb, true);
        sb.append("</html>");
        return sb.toString();
    }

    /**
     * Return an HTML representation of the target used by tooltips in the given string buffer
     * @param sb string buffer to fill
     * @param full flag to display full information
     */
    private final void toHtml(final StringBuffer sb, final boolean full) {
        sb.append("<b>FitsImageHDU[HDU#").append(getHduIndex()).append("][").append(getImageCount()).append(']');
        sb.append("</b><br>Header:<ul>");
        for (FitsHeaderCard h : this.headerCards) {
            sb.append("<li>").append(h.toString()).append("</li>");
        }
        sb.append("</ul>");
    }
}
