/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.fits;

/**
 * This class represents one Fits header card (key, value, comment)
 * @author bourgesl
 */
public final class FitsHeaderCard {

    /** header card key */
    private final String key;
    /** optional header card value */
    private String value;
    /** optional header card comment */
    private String comment;

    /**
     * Protected constructor
     * @param key header card key
     * @param value optional header card value
     * @param comment optional header card comment
     */
    public FitsHeaderCard(final String key, final String value, final String comment) {
        this.key = key;
        this.value = value;
        this.comment = comment;
    }

    /**
     * Return the header card key
     * @return header card key
     */
    public String getKey() {
        return key;
    }

    /**
     * Return the optional header card value
     * @return optional header card value
     */
    public String getValue() {
        return value;
    }

    /**
     * Return the optional header card comment
     * @return optional header card comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * Returns a string representation of this Fits header card
     * @return a string representation of this Fits header card
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(80);
        toString(sb);
        return sb.toString();
    }

    /**
     * Returns a string representation of this table
     * @param sb string builder to append to
     */
    public void toString(final StringBuilder sb) {
        sb.append(key);
        if (value != null) {
            sb.append(" = ").append(value);
        }
        if (comment != null) {
            sb.append(" // ").append(comment);
        }
    }
}
