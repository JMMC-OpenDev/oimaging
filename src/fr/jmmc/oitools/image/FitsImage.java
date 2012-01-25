/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.image;

import java.util.ArrayList;
import java.util.List;

/**
 * This class describes an astronomical image (2D) with its coordinates, orientation, scale ...
 * @author bourgesl
 */
public final class FitsImage {

    /** Logger associated to image classes */
    private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FitsImage.class.getName());
    /* members */
    /** Fits extension number (0 means primary) */
    private int extNb;
    /** number of columns */
    private int nbCols;
    /** number of rows */
    private int nbRows;
    /** column position of the reference pixel (real starting from 1.0) */
    private double pixRefCol = 1d;
    /** row position of the reference pixel (real starting from 1.0) */
    private double pixRefRow = 1d;
    /** coordinate value at the reference pixel column */
    private double valRefCol = 0d;
    /** coordinate value at the reference pixel row */
    private double valRefRow = 0d;
    /** coordinate increment along the column axis (degrees per pixel) */
    private double incCol = Double.NaN;
    /** coordinate increment along the row axis (degrees per pixel) */
    private double incRow = Double.NaN;
    /** optional list of header cards */
    private List<FitsHeaderCard> headerCards = null;
    /** image data as float[nbRows][nbCols] ie [Y][X] */
    private float[][] data = null;
    /** flag to indicate that data contains NaN values */
    private boolean hasNaN = false;
    /** minimum value in data */
    private double dataMin = Double.NaN;
    /** maximum value in data */
    private double dataMax = Double.NaN;

    /** 
     * Public FitsImage class constructor
     */
    public FitsImage() {
        super();
    }

    /**
     * Get the extension number
     * @return the extension number
     */
    public int getExtNb() {
        return extNb;
    }

    /**
     * Define the extension number
     * @param extNb extension number
     */
    void setExtNb(final int extNb) {
        this.extNb = extNb;
    }

    /**
     * Return the number of columns i.e. the Fits NAXIS1 keyword value
     * @return the number of columns i.e. the Fits NAXIS1 keyword value
     */
    public int getNbCols() {
        return this.nbCols;
    }

    /**
     * Define the number of columns i.e. the Fits NAXIS1 keyword value
     * @param nbCols number of columns i.e. the Fits NAXIS1 keyword value
     */
    void setNbCols(final int nbCols) {
        this.nbCols = nbCols;
    }

    /**
     * Return the number of rows i.e. the Fits NAXIS2 keyword value
     * @return the number of rows i.e. the Fits NAXIS2 keyword value
     */
    public int getNbRows() {
        return this.nbRows;
    }

    /**
     * Define the number of rows i.e. the Fits NAXIS2 keyword value
     * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
     */
    void setNbRows(final int nbRows) {
        this.nbRows = nbRows;
    }

    /**
     * Return the column position of the reference pixel (real starting from 1.0)
     * @return column position of the reference pixel (real starting from 1.0)
     */
    public double getPixRefCol() {
        return pixRefCol;
    }

    /**
     * Define the column position of the reference pixel (real starting from 1.0)
     * @param pixRefCol column position of the reference pixel (real starting from 1.0)
     */
    public void setPixRefCol(final double pixRefCol) {
        this.pixRefCol = pixRefCol;
    }

    /**
     * Return the row position of the reference pixel (real starting from 1.0)
     * @return row position of the reference pixel (real starting from 1.0)
     */
    public double getPixRefRow() {
        return pixRefRow;
    }

    /**
     * Define the row index of the reference pixel (real starting from 1.0)
     * @param pixRefRow row index of the reference pixel (real starting from 1.0)
     */
    public void setPixRefRow(final double pixRefRow) {
        this.pixRefRow = pixRefRow;
    }

    /**
     * Return the coordinate value at the reference pixel column
     * @return coordinate value at the reference pixel column
     */
    public double getValRefCol() {
        return valRefCol;
    }

    /**
     * Define the coordinate value at the reference pixel column
     * @param valRefCol coordinate value at the reference pixel column
     */
    public void setValRefCol(final double valRefCol) {
        this.valRefCol = valRefCol;
    }

    /**
     * Return the coordinate value at the reference pixel row
     * @return coordinate value at the reference pixel row
     */
    public double getValRefRow() {
        return valRefRow;
    }

    /**
     * Define the coordinate value at the reference pixel row
     * @param valRefRow coordinate value at the reference pixel row
     */
    public void setValRefRow(final double valRefRow) {
        this.valRefRow = valRefRow;
    }

    /**
     * Return the coordinate increment along the column axis
     * @return coordinate increment along the column axis
     */
    public double getIncCol() {
        return incCol;
    }

    /**
     * Define the coordinate increment along the column axis
     * @param incCol coordinate increment along the column axis
     */
    public void setIncCol(final double incCol) {
        this.incCol = incCol;
    }

    /**
     * Return the coordinate increment along the row axis
     * @return coordinate increment along the row axis
     */
    public double getIncRow() {
        return incRow;
    }

    /**
     * Define the coordinate increment along the row axis
     * @param incRow coordinate increment along the row axis
     */
    public void setIncRow(final double incRow) {
        this.incRow = incRow;
    }

    /**
     * Return the image data as float[nbRows][nbCols] ie [Y][X]
     * @return image data as float[nbRows][nbCols] ie [Y][X]
     */
    public float[][] getData() {
        return data;
    }

    /**
     * Define the image data as float[nbRows][nbCols] ie [Y][X].
     * Note: no array copy is performed so do not modify the given array afterwards.
     * 
     * @param data image data as float[nbRows][nbCols] ie [Y][X]
     */
    public void setData(final float[][] data) {
        if (data != null) {
            this.data = data;

            // update nbRows / nbCols:
            final int length = data.length;
            setNbRows(length);
            setNbCols((length > 0) ? data[0].length : 0);

            analyzeData();
        }
    }

    /**
     * Return the minimum value in data
     * @return minimum value in data
     */
    public double getDataMin() {
        return dataMin;
    }

    /**
     * Define the minimum value in data
     * @param dataMin minimum value in data
     */
    void setDataMin(final double dataMin) {
        this.dataMin = dataMin;
    }

    /**
     * Return the maximum value in data
     * @return maximum value in data
     */
    public double getDataMax() {
        return dataMax;
    }

    /**
     * Define the maximum value in data
     * @param dataMax maximum value in data
     */
    void setDataMax(final double dataMax) {
        this.dataMax = dataMax;
    }

    /**
     * Return the flag to indicate that data contains NaN values
     * @return flag to indicate that data contains NaN values
     */
    public boolean isHasNaN() {
        return hasNaN;
    }

    /**
     * Define the flag to indicate that data contains NaN values
     * @param hasNaN flag to indicate that data contains NaN values
     */
    void setHasNaN(final boolean hasNaN) {
        this.hasNaN = hasNaN;
    }

    /**
     * Return the list of header cards
     * @return list of header cards
     */
    public List<FitsHeaderCard> getHeaderCards() {
        return getHeaderCards(10);
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

    // utility methods:
    /**
     * Return the viewed angle along column axis in degrees
     * @return viewed angle along column axis in degrees
     */
    public double getAngleCol() {
        return nbCols * incCol;
    }

    /**
     * Return the viewed angle along row axis in degrees
     * @return viewed angle along row axis in degrees
     */
    public double getAngleRow() {
        return nbRows * incRow;
    }

    /**
     * Return the minimum view angle in degrees
     * @return minimum view angle in degrees
     */
    public double getMinAngle() {
        return Math.min(Math.abs(getAngleCol()), Math.abs(getAngleRow()));
    }

    /**
     * Return the maximum view angle in degrees
     * @return maximum view angle in degrees
     */
    public double getMaxAngle() {
        return Math.max(Math.abs(getAngleCol()), Math.abs(getAngleRow()));
    }

    /**
     * Analyze data i.e. update min/max values and hasNaN flag
     */
    private void analyzeData() {
        logger.info("analyzeData - start");

        boolean isNaN = false;

        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;

        float val;
        float[] row;
        for (int i, j = 0, rows = getNbRows(), cols = getNbCols(); j < rows; j++) {
            row = data[j];
            for (i = 0; i < cols; i++) {
                val = row[i];
                if (Float.isNaN(val)) {
                    isNaN = true;
                } else {
                    if (val < min) {
                        min = val;
                    }

                    if (val > max) {
                        max = val;
                    }
                }
            }
        }
        setHasNaN(isNaN);
        setDataMin(min);
        setDataMax(max);

        logger.info("analyzeData - end: min = " + min + ", max = " + max + " - hasNaN = " + isNaN);
    }

    // toString helpers:
    /**
     * Return a string representation of the given angle in degrees using appropriate unit (deg/arcmin/arcsec/milli arcsec)
     * @param angle angle in degrees
     * @return string representation of the given angle
     */
    public String getAngleAsString(final double angle) {
        if (Double.isNaN(angle)) {
            return "NaN";
        }
        double tmp = Math.abs(angle);
        if (angle > 1e-1d) {
            return tmp + " deg";
        }
        tmp *= 60d;
        if (angle > 1e-1d) {
            return tmp + " arcmin";
        }
        tmp *= 60d;
        if (angle > 1e-1d) {
            return tmp + " arcsec";
        }
        tmp *= 1000d;
        return tmp + " milliarcsec";
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
        return "FitsImage#" + getExtNb() + "[" + getNbCols() + " x " + getNbRows() + "]"
                + " RefPix (" + getPixRefCol() + ", " + getPixRefRow() + ")"
                + " RefVal (" + getValRefCol() + ", " + getValRefRow() + ")"
                + " Increments (" + getIncCol() + ", " + getIncRow() + ")"
                + " Max view angle (" + getAngleAsString(getMaxAngle()) + ")"
                + "{\n" + getHeaderCardsAsString("\n") + "}";
    }
}
