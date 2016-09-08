/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.util;

import fr.jmmc.jmal.image.job.ImageMinMaxJob;
import fr.jmmc.oitools.image.FitsImage;
import fr.jmmc.oitools.image.FitsImageFile;
import fr.jmmc.oitools.image.FitsImageHDU;
import fr.jmmc.oitools.image.FitsImageLoader;
import fr.nom.tam.fits.FitsException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This utility class provides several helper methods over FitsImage class
 * 
 * TODO: enhance profile usage and add new dynamic histogram (log(value))
 * 
 * @author bourgesl
 */
public final class FitsImageUtils {
    /* constants */

    /** Logger associated to image classes */
    private final static Logger logger = LoggerFactory.getLogger(FitsImageUtils.class.getName());

    /**
     * Forbidden constructor
     */
    private FitsImageUtils() {
        super();
    }

    /**
     * Create a new FitsImage given its data and updates dataMin/Max
     * @param data image data as float[nbRows][nbCols] ie [Y][X]
     * @return new FitsImage
     */
    public static FitsImage createFitsImage(final float[][] data) {
        final FitsImage image = new FitsImage();

        updateFitsImage(image, data);

        return image;
    }

    /**
     * Update data of the given FitsImage given its data and updates dataMin/Max
     * @param image FitsImage to update
     * @param data image data as float[nbRows][nbCols] ie [Y][X]
     */
    public static void updateFitsImage(final FitsImage image, final float[][] data) {
        image.setData(data);

        // update dataMin/Max:
        updateDataRangeExcludingZero(image);
    }

    /**
     * Create a new FitsImage given its data and updates dataMin/Max
     * @param data image data as float[nbRows][nbCols] ie [Y][X]
     * @param dataMin minimum value in data
     * @param dataMax maximum value in data
     * @return new FitsImage
     */
    public static FitsImage createFitsImage(final float[][] data,
            final double dataMin, final double dataMax) {
        final FitsImage image = new FitsImage();

        updateFitsImage(image, data, dataMin, dataMax);

        return image;
    }

    /**
     * Update data of the given FitsImage given its data and updates dataMin/Max
     * @param image FitsImage to update
     * @param data image data as float[nbRows][nbCols] ie [Y][X]
     * @param dataMin minimum value in data
     * @param dataMax maximum value in data
     */
    public static void updateFitsImage(final FitsImage image, final float[][] data,
            final double dataMin, final double dataMax) {
        image.setData(data);

        image.setDataMin(dataMin);
        image.setDataMax(dataMax);
    }

    /**
     * Create a new FitsImage given its data and coordinate informations
     * and updates dataMin/Max
     * @param data image data as float[nbRows][nbCols] ie [Y][X]
     * @param pixRefRow row index of the reference pixel (real starting from 1.0)
     * @param pixRefCol column position of the reference pixel (real starting from 1.0)
     * @param incRow signed coordinate increment along the row axis in radians
     * @param incCol signed coordinate increment along the column axis in radians
     * @return new FitsImage
     */
    public static FitsImage createFitsImage(final float[][] data,
            final double pixRefRow, final double pixRefCol,
            final double incRow, final double incCol) {

        final FitsImage image = createFitsImage(data);

        image.setPixRefRow(pixRefRow);
        image.setPixRefCol(pixRefCol);

        image.setSignedIncRow(incRow);
        image.setSignedIncCol(incCol);

        return image;
    }

    /**
     * Create a new FitsImage given its data and coordinate informations
     * and updates dataMin/Max
     * @param data image data as float[nbRows][nbCols] ie [Y][X]
     * @param dataMin minimum value in data
     * @param dataMax maximum value in data
     * @param pixRefRow row index of the reference pixel (real starting from 1.0)
     * @param pixRefCol column position of the reference pixel (real starting from 1.0)
     * @param incRow signed coordinate increment along the row axis in radians
     * @param incCol signed coordinate increment along the column axis in radians
     * @return new FitsImage
     */
    public static FitsImage createFitsImage(final float[][] data,
            final double dataMin, final double dataMax,
            final double pixRefRow, final double pixRefCol,
            final double incRow, final double incCol) {

        final FitsImage image = new FitsImage();

        updateFitsImage(image, data, dataMin, dataMax);

        image.setPixRefRow(pixRefRow);
        image.setPixRefCol(pixRefCol);

        image.setSignedIncRow(incRow);
        image.setSignedIncCol(incCol);

        return image;
    }

    /**
     * Load the given file and return a FitsImageFile structure.
     * This methods updates dataMin/Max of each FitsImage
     *
     * @param absFilePath absolute File path on file system (not URL)
     * @param firstOnly load only the first valid Image HDU
     * @return FitsImageFile structure on success
     * 
     * @throws FitsException if any FITS error occured
     * @throws IOException IO failure
     * @throws IllegalArgumentException if unsupported unit or unit conversion is not allowed
     */
    public static FitsImageFile load(final String absFilePath, final boolean firstOnly) throws FitsException, IOException, IllegalArgumentException {
        final FitsImageFile imgFitsFile = FitsImageLoader.load(absFilePath, firstOnly);

        for (FitsImageHDU fitsImageHDU : imgFitsFile.getFitsImageHDUs()) {
            for (FitsImage fitsImage : fitsImageHDU.getFitsImages()) {
                // update boundaries excluding zero values:
                updateDataRangeExcludingZero(fitsImage);
            }
        }

        return imgFitsFile;
    }

    /** 
     * Update the data Min/Max of the given fitsImage
     * @param fitsImage fitsImage to process and update
     */
    public static void updateDataRange(final FitsImage fitsImage) {
        updateDataRange(fitsImage, false);
    }

    /** 
     * Update the data Min/Max of the given fitsImage excluding values equals to zero
     * @param fitsImage fitsImage to process and update
     */
    public static void updateDataRangeExcludingZero(final FitsImage fitsImage) {
        updateDataRange(fitsImage, true);
    }

    /** 
     * Update the data Min/Max of the given fitsImage
     * @param image fits image to process and update
     * @param excludeZero true to indicate to ignore zero values
     */
    private static void updateDataRange(final FitsImage image, final boolean excludeZero) {
        // update min/max ignoring zero:
        final ImageMinMaxJob minMaxJob = new ImageMinMaxJob(image.getData(),
                image.getNbCols(), image.getNbRows(), excludeZero);

        minMaxJob.forkAndJoin();

        if (logger.isInfoEnabled()) {
            logger.info("ImageMinMaxJob min: {} - max: {} - nData: {} - sum: {}",
                    minMaxJob.getMin(), minMaxJob.getMax(), minMaxJob.getNData(), minMaxJob.getSum());
        }

        // update nData:
        image.setNData(minMaxJob.getNData());

        // update dataMin/dataMax:
        image.setDataMin(minMaxJob.getMin());
        image.setDataMax(minMaxJob.getMax());
        // update sum:
        image.setSum(minMaxJob.getSum());
    }
}
