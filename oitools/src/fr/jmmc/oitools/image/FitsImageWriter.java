/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.image;

import fr.jmmc.oitools.fits.FitsConstants;
import fr.jmmc.oitools.fits.FitsHeaderCard;
import fr.jmmc.oitools.fits.FitsUtils;
import fr.nom.tam.fits.BasicHDU;
import fr.nom.tam.fits.Data;
import fr.nom.tam.fits.Fits;
import fr.nom.tam.fits.FitsException;
import fr.nom.tam.fits.Header;
import fr.nom.tam.fits.HeaderCard;
import fr.nom.tam.fits.ImageData;
import fr.nom.tam.fits.ImageHDU;
import fr.nom.tam.util.BufferedFile;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * This stateless class writes is an FitsImageFile structure into an Fits image and cube
 *
 * @author bourgesl
 */
public final class FitsImageWriter {
    /* constants */

    /** Logger associated to meta model classes */
    private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FitsImageWriter.class.getName());
    /** skip keywords when copying fits header cards present in FitsImage */
    private final static Set<String> SKIP_KEYWORDS = new HashSet<String>(32);

    static {
        SKIP_KEYWORDS.addAll(FitsUtils.FITS_STANDARD_KEYWORDS);

        SKIP_KEYWORDS.add(FitsImageConstants.KEYWORD_CRPIX1);
        SKIP_KEYWORDS.add(FitsImageConstants.KEYWORD_CRPIX2);

        SKIP_KEYWORDS.add(FitsImageConstants.KEYWORD_CRVAL1);
        SKIP_KEYWORDS.add(FitsImageConstants.KEYWORD_CRVAL2);

        SKIP_KEYWORDS.add(FitsImageConstants.KEYWORD_CDELT1);
        SKIP_KEYWORDS.add(FitsImageConstants.KEYWORD_CDELT2);
    }

    /**
     * Private constructor
     */
    private FitsImageWriter() {
        super();
    }

    /**
     * Main method to write an FitsImageFile structure
     * @param absFilePath absolute File path on file system (not URL)
     * @param imgFitsFile FitsImageFile structure to write
     * @throws FitsException if the fits can not be written
     * @throws IOException IO failure
     */
    public static void write(final String absFilePath, final FitsImageFile imgFitsFile) throws IOException, FitsException {
        imgFitsFile.setAbsoluteFilePath(absFilePath);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("writing " + absFilePath);
        }

        BufferedFile bf = null;
        try {
            final long start = System.nanoTime();

            // create the fits model :
            final Fits fitsFile = new Fits();

            // process all OI_* tables :
            createHDUnits(imgFitsFile, fitsFile);

            bf = new BufferedFile(absFilePath, "rw");

            // write the fits file :
            fitsFile.write(bf);

            // flush and close :
            bf.close();
            bf = null;

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("write : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");
            }

        } catch (FitsException fe) {
            logger.log(Level.SEVERE, "Unable to write the file : " + absFilePath, fe);

            throw fe;
        } finally {
            if (bf != null) {
                // flush and close :
                bf.close();
            }
        }
    }

    /**
     * Create all Fits HD units corresponding to Fits images
     * @param imgFitsFile FitsImageFile structure to write
     * @param fitsFile fits file
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     */
    private static void createHDUnits(final FitsImageFile imgFitsFile, final Fits fitsFile) throws FitsException, IOException {
        int i = 0;
        BasicHDU hdu;
        for (FitsImage image : imgFitsFile.getFitsImages()) {

            // update the fits image identifier:
            image.setFitsImageIdentifier(imgFitsFile.getFileName() + "#" + i);

            // add HDU to the fits file :
            hdu = createImage(image);

            // update checksum:
            image.setChecksum(FitsImageLoader.updateChecksum(hdu));

            fitsFile.addHDU(hdu);

            i++;
        }
    }

    /**
     * Create an image HDU using the given FitsImage
     * @param image FitsImage
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     * @return image HDU
     */
    private static BasicHDU createImage(final FitsImage image) throws FitsException, IOException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("createImage : " + image.toString());
        }

        if (image.getData() == null) {
            throw new FitsException("No image data in FitsImage !");
        }
        // Prepare the image data to create HDU :
        final Data fitsData = new ImageData(image.getData());

        // Generate the header from the binary table :
        final Header header = ImageHDU.manufactureHeader(fitsData);

        // create HDU :
        final ImageHDU hdu = new ImageHDU(header, fitsData);

        // Finalize Header :

        // Add fits header cards in the header :
        processKeywords(header, image);

        return hdu;
    }

    /**
     * Process the image header to set keywords defined in the FitsImage
     * @param header binary table header
     * @param image FitsImage
     * @throws FitsException if any FITS error occurred
     */
    private static void processKeywords(final Header header, final FitsImage image) throws FitsException {
        // Note : a fits keyword has a KEY, VALUE AND COMMENT

        // skip x-y axes dimensions (handled by nom.tam.Fits) :
        /*
         KEYWORD NAXIS = '2'	// Number of axes
         KEYWORD NAXIS1 = '512'	// Axis length
         KEYWORD NAXIS2 = '512'	// Axis length
         */

        // Process reference pixel:
        /*
         KEYWORD CRPIX1 = '256.'	// Reference pixel
         KEYWORD CRPIX2 = '256.'	// Reference pixel
         */
        header.addValue(FitsImageConstants.KEYWORD_CRPIX1, image.getPixRefCol(), "Reference pixel");
        header.addValue(FitsImageConstants.KEYWORD_CRPIX2, image.getPixRefRow(), "Reference pixel");

        // Process coordinates at the reference pixel:
        // note: units are ignored
        /*
         KEYWORD CRVAL1 = '0.'	// Coordinate at reference pixel
         KEYWORD CRVAL2 = '0.'	// Coordinate at reference pixel
        
         KEYWORD CTYPE1 = ''	//  Units of coordinate
         KEYWORD CTYPE2 = ''	//  Units of coordinate
         */
        header.addValue(FitsImageConstants.KEYWORD_CRVAL1, image.getValRefCol(), "Coordinate at reference pixel (rad)");
        header.addValue(FitsImageConstants.KEYWORD_CRVAL2, image.getValRefRow(), "Coordinate at reference pixel (rad)");

        // Process increments along axes:
        /*
         KEYWORD CDELT1 = '-1.2E-10'	// Coord. incr. per pixel (original value)
         KEYWORD CDELT2 = '1.2E-10'	// Coord. incr. per pixel (original value)
         */
        header.addValue(FitsImageConstants.KEYWORD_CDELT1, image.getSignedIncCol(), "Coord. incr. per pixel (rad)");
        header.addValue(FitsImageConstants.KEYWORD_CDELT2, image.getSignedIncRow(), "Coord. incr. per pixel (rad)");

        // Process data min/max:
        /*
         KEYWORD DATAMAX = '5120.758'	// Maximum data value
         KEYWORD DATAMIN = '0.0'	// Minimum data value
         */
        // note: data min/max are later recomputed (missing / invalid values or bad precision)
        if (!Double.isNaN(image.getDataMin())) {
            header.addValue(FitsConstants.KEYWORD_DATAMIN, image.getDataMin(), "Minimum data value");
        }
        if (!Double.isNaN(image.getDataMax())) {
            header.addValue(FitsConstants.KEYWORD_DATAMAX, image.getDataMax(), "Maximum data value");
        }

        // Copy all header cards:
        final List<FitsHeaderCard> imgHeaderCards = image.getHeaderCards();

        if (imgHeaderCards != null && !imgHeaderCards.isEmpty()) {
            String key;
            for (FitsHeaderCard headerCard : imgHeaderCards) {
                key = headerCard.getKey();

                // skip already handled keywords:
                if (!SKIP_KEYWORDS.contains(key)) {
                    // support repeated keywords
                    header.addLine(new HeaderCard(key, headerCard.getValue(), headerCard.getComment()));
                }
            }
        }
    }
}
