/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.image;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * This class describes an astronomical image (2D) with its coordinates, orientation, scale ...
 * 
 * @author bourgesl
 */
public final class FitsImage {

    /** Logger associated to image classes */
    private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FitsImage.class.getName());
    /* members */
    /** image identifier */
    private String imageIdentifier = null;
    /** number of columns */
    private int nbCols;
    /** number of rows */
    private int nbRows;
    /** column position of the reference pixel (real starting from 1.0) */
    private double pixRefCol = 1d;
    /** row position of the reference pixel (real starting from 1.0) */
    private double pixRefRow = 1d;
    /** coordinate value at the reference pixel column (radians) */
    private double valRefCol = 0d;
    /** coordinate value at the reference pixel row (radians) */
    private double valRefRow = 0d;
    /** sign flag of the coordinate increment along the column axis (true means positive or false negative) */
    private boolean incColPositive = true;
    /** absolute coordinate increment along the column axis (radians per pixel) */
    private double incCol = 1d;
    /** sign flag of the coordinate increment along the row axis (true means positive or false negative) */
    private boolean incRowPositive = true;
    /** absolute coordinate increment along the row axis (radians per pixel) */
    private double incRow = 1d;
    /** image area coordinates (radians) */
    private Rectangle2D.Double area = null;
    /** optional list of header cards */
    private List<FitsHeaderCard> headerCards = null;
    /** image data as float[nbRows][nbCols] ie [Y][X] */
    private float[][] data = null;
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
     * Define the fits image identifier = 'FileName#ExtNb'
     * @param imageIdentifier image identifier 
     */
    public void setFitsImageIdentifier(final String imageIdentifier) {
        this.imageIdentifier = imageIdentifier;
    }

    /**
     * Return the fits image identifier = 'FileName#ExtNb'
     * @return fits image identifier or null if undefined
     */
    public String getFitsImageIdentifier() {
        return this.imageIdentifier;
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
        this.area = null; // reset area
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
        this.area = null; // reset area
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
        this.area = null; // reset area
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
        this.area = null; // reset area
    }

    /**
     * Return the coordinate value at the reference pixel column in radians
     * @return coordinate value at the reference pixel column in radians
     */
    public double getValRefCol() {
        return valRefCol;
    }

    /**
     * Define the coordinate value at the reference pixel column in radians
     * @param valRefCol coordinate value at the reference pixel column in radians
     */
    public void setValRefCol(final double valRefCol) {
        this.valRefCol = valRefCol;
        this.area = null; // reset area
    }

    /**
     * Return the coordinate value at the reference pixel row in radians
     * @return coordinate value at the reference pixel row in radians
     */
    public double getValRefRow() {
        return valRefRow;
    }

    /**
     * Define the coordinate value at the reference pixel row in radians
     * @param valRefRow coordinate value at the reference pixel row in radians
     */
    public void setValRefRow(final double valRefRow) {
        this.valRefRow = valRefRow;
        this.area = null; // reset area
    }

    /**
     * Return the sign flag of the coordinate increment along the column axis (true means positive or false negative)
     * @return sign flag of the coordinate increment along the column axis (true means positive or false negative)
     */
    public boolean isIncColPositive() {
        return incColPositive;
    }

    /**
     * Return the signed coordinate increment along the column axis in radians
     * @return signed coordinate increment along the column axis in radians
     */
    public double getSignedIncCol() {
        return (incColPositive) ? incCol : -incCol;
    }

    /**
     * Define the absolute and sign flag of the coordinate increment along the column axis
     * @param incCol signed coordinate increment along the column axis in radians
     */
    public void setSignedIncCol(final double incCol) {
        this.incColPositive = (incCol >= 0d);
        this.incCol = (incColPositive) ? incCol : -incCol;
        this.area = null; // reset area
    }

    /**
     * Return the absolute coordinate increment along the column axis in radians
     * @return absolute coordinate increment along the column axis in radians
     */
    public double getIncCol() {
        return incCol;
    }

    /**
     * Return the sign flag of the coordinate increment along the row axis (true means positive or false negative)
     * @return sign flag of the coordinate increment along the row axis (true means positive or false negative)
     */
    public boolean isIncRowPositive() {
        return incRowPositive;
    }

    /**
     * Return the signed coordinate increment along the row axis
     * @return signed coordinate increment along the row axis in radians
     */
    public double getSignedIncRow() {
        return (incRowPositive) ? incRow : -incRow;
    }

    /**
     * Define the absolute and sign flag of the coordinate increment along the row axis
     * @param incRow signed coordinate increment along the row axis in radians
     */
    public void setSignedIncRow(final double incRow) {
        this.incRowPositive = (incRow >= 0d);
        this.incRow = (incRowPositive) ? incRow : -incRow;
        this.area = null; // reset area
    }

    /**
     * Return the absolute coordinate increment along the row axis in radians
     * @return absolute coordinate increment along the row axis in radians
     */
    public double getIncRow() {
        return incRow;
    }

    /**
     * Return the image area coordinates in radians
     * @return image area coordinates in radians
     */
    public Rectangle2D.Double getArea() {
        if (area == null) {
            updateArea();
        }
        return area;
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

            // reset data min/max:
            setDataMin(Double.NaN);
            setDataMax(Double.NaN);
        }
    }

    /**
     * Return true if the minimum and maximum value in data are defined
     * @return true if the minimum and maximum value in data are defined 
     */
    public boolean isDataRangeDefined() {
        return (!Double.isNaN(dataMin) && Double.isNaN(dataMax));
    }

    /**
     * Return the minimum value in data
     * @return minimum value in data or Double.NaN if undefined
     */
    public double getDataMin() {
        return dataMin;
    }

    /**
     * Define the minimum value in data
     * @param dataMin minimum value in data
     */
    public void setDataMin(final double dataMin) {
        this.dataMin = dataMin;
    }

    /**
     * Return the maximum value in data
     * @return maximum value in data or Double.NaN if undefined
     */
    public double getDataMax() {
        return dataMax;
    }

    /**
     * Define the maximum value in data
     * @param dataMax maximum value in data
     */
    public void setDataMax(final double dataMax) {
        this.dataMax = dataMax;
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
     * Return the viewed angle along column axis in radians
     * @return viewed angle along column axis in radians
     */
    public double getAngleCol() {
        return nbCols * incCol;
    }

    /**
     * Return the viewed angle along row axis in radians
     * @return viewed angle along row axis in radians
     */
    public double getAngleRow() {
        return nbRows * incRow;
    }

    /**
     * Return the minimum view angle in radians
     * @return minimum view angle in radians
     */
    public double getMinAngle() {
        return Math.min(getAngleCol(), getAngleRow());
    }

    /**
     * Return the maximum view angle in radians
     * @return maximum view angle in radians
     */
    public double getMaxAngle() {
        return Math.max(getAngleCol(), getAngleRow());
    }

    /**
     * Update image area coordinates in radians
     */
    private void updateArea() {
        area = new Rectangle2D.Double(
                valRefCol - (pixRefCol - 1d) * incCol,
                valRefRow - (pixRefRow - 1d) * incRow,
                nbCols * incCol,
                nbRows * incRow);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("updateArea: " + area);
            logger.fine("updateArea: (" + getAngleAsString(area.getX()) + ", " + getAngleAsString(area.getY()) + ") ["
                    + getAngleAsString(area.getWidth()) + ", " + getAngleAsString(area.getHeight()) + "]");
        }
    }

    // toString helpers:
    /**
     * Return a string representation of the given angle in degrees using appropriate unit (deg/arcmin/arcsec/milli arcsec)
     * @param angle angle in radians
     * @return string representation of the given angle
     */
    public String getAngleAsString(final double angle) {
        if (Double.isNaN(angle)) {
            return "NaN";
        }
        double tmp = Math.toDegrees(angle);
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
        return toString(true);
    }

    /**
     * Returns a string representation of this Fits image
     * @param dumpHeaderCards true to dump also header cards
     * @return a string representation of this Fits image
     */
    public String toString(final boolean dumpHeaderCards) {
        return "FitsImage[" + getFitsImageIdentifier() + "][" + getNbCols() + " x " + getNbRows() + "]"
                + " RefPix (" + getPixRefCol() + ", " + getPixRefRow() + ")"
                + " RefVal (" + getValRefCol() + ", " + getValRefRow() + ")"
                + " Increments (" + getSignedIncCol() + ", " + getSignedIncRow() + ")"
                + " Max view angle (" + getAngleAsString(getMaxAngle()) + ")"
                + " Area " + getArea()
                + ((dumpHeaderCards) ? "{\n" + getHeaderCardsAsString("\n") + "}" : "");
    }
}
