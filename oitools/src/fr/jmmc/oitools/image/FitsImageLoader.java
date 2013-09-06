/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.image;

import fr.jmmc.oitools.fits.FitsConstants;
import fr.jmmc.oitools.fits.FitsHeaderCard;
import fr.nom.tam.fits.BasicHDU;
import fr.nom.tam.fits.Data;
import fr.nom.tam.fits.Fits;
import fr.nom.tam.fits.FitsException;
import fr.nom.tam.fits.FitsFactory;
import fr.nom.tam.fits.FitsUtil;
import fr.nom.tam.fits.Header;
import fr.nom.tam.fits.HeaderCard;
import fr.nom.tam.fits.ImageData;
import fr.nom.tam.fits.ImageHDU;
import fr.nom.tam.fits.PaddingException;
import fr.nom.tam.util.ArrayDataInput;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

/**
 * This stateless class loads is an Fits image and cube into the FitsImageFile structure
 * 
 * From ASPRO1:
 *  -------About the FITS file Format ------------------------------
 * First, the FITS file should describe a flux distribution on
 * sky, so the 2 first axes are in offset in RADIANS on the sky. The
 * following is an example of a typical header:
 * 
 * NAXIS   =                    2 /2 minimum!
 * NAXIS1  =                  512 /size 1st axis (for example)
 * NAXIS2  =                  512 /size 2nd axis
 * CRVAL1  =  0.0000000000000E+00 / center is at 0
 * CRPIX1  =  0.2560000000000E+03 / reference pixel is 256 in Alpha
 * CDELT1  = -0.4848136811095E-10 / increment is 0.1 milliseconds (radians), 
 * / and 'astronomy oriented' (i.e. 
 * / RA decreases with pixel number)
 * CRVAL2  =  0.0000000000000E+00 / center is at 0
 * CRPIX2  =  0.2560000000000E+03 / reference pixel is 256 in Delta
 * CDELT2  =  0.4848136811095E-10 / increment is 0.1 milliseconds.
 * 
 * The position of the "keyword = value / comment" fields is FIXED by
 * the fits norm. In doubt, see http://www.cv.nrao.edu/fits/aah2901.pdf
 * 
 * Axes increments map pixel images to RA and DEC sky coordinates, which
 * are positive to the West and North (and position angles are counted
 * West of North).
 * 
 * For single (monochromatic) images (NAXIS = 2), ASPRO assume that this
 * image is observed at the current observing wavelength, usually the
 * mean wavelength of the current instrument setup. ASPRO has a support
 * for polychromatic images, as FITS cubes, with a few more keywords
 * (see below), in which case the used wavelengths will be those defined
 * in the FITS file, not those of the current instrument/interferometer.
 * 
 * The file may be a data-cube (N images at different wavelengths) in
 * which case the 3rd axis must be sampled evenly. In the absence of
 * further keywords (see below), it will be assumed that th 3rd axis
 * is in MICRONS as in:
 * 
 * NAXIS   =                    3 /data-cube
 * NAXIS1  =                  512 /size 1st axis (for example)
 * NAXIS2  =                  512 /size 2nd axis
 * NAXIS3  =                   32 /size 2nd axis
 * CRVAL1  =  0                   / center is at 0 RA offset on sky
 * CRPIX1  =  256                 / reference pixel is 256 in Alpha
 * CDELT1  = -0.4848136811095E-10 / increment is -0.1 milliseconds (radians), 
 * CRVAL2  =  0                   / center is at 0 DEC offset on sky
 * CRPIX2  =  256                 / reference pixel is 256 in Delta
 * CDELT2  =  0.4848136811095E-10 / increment is 0.1 milliseconds.
 * CRPIX3  =  1                   / reference pixel(channel) is 1 on 3rd axis
 * CRVAL3  =  2.2                 / 2.2 microns for this pix/channel
 * CDELT3  =  0.01                / microns channel width
 * 
 * However, the additional presence of CTYPE3 can affect
 * the 3rd axis definition:
 * CTYPE3  = 'FREQUENCY'          / means that Cxxxx3 are in Hz
 * or
 * CTYPE3  = 'WAVELENGTH'         / means that Cxxxx3 are in Microns
 * 
 * TODO: interpret CTYPE3 = 'FREQUENCY' or 'WAVELENGTH' ?
 * TODO: interpret BUNIT / BTYPE ?
 * 
 * Added support for CUNITn keywords (aspro_keywords.fits):
 * CRPIX1  = '128.500000'
 * CDELT1  = '0.000750'
 * CRVAL1  = '0.000375'
 * CUNIT1  = '  ARCSEC'
 * CRPIX2  = '128.500000'
 * CDELT2  = '0.000750'
 * CRVAL2  = '0.000375'
 * CUNIT2  = '  ARCSEC'
 * CRPIX3  = '1.000000'
 * CDELT3  = '0.000000'
 * CRVAL3  = '1.600000'
 * CUNIT3  = '  MICRON'
 * BUNIT   = 'JY/PIXEL'
 * BTYPE   = 'INTENSITY'
 * 
 * @author bourgesl
 */
public final class FitsImageLoader {
    /* constants */

    /** Logger associated to meta model classes */
    private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FitsImageLoader.class.getName());
    /** undefined image index */
    private final static int UNDEFINED_INDEX = -1;

    static {
        FitsFactory.setUseHierarch(true);
    }

    /**
     * Private constructor
     */
    private FitsImageLoader() {
        super();
    }

    /**
     * Load the given file and return a FitsImageFile structure
     *
     * @param absFilePath absolute File path on file system (not URL)
     * @param firstOnly load only the first valid Image HDU
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     * @return FitsImageFile structure on success
     * @throws IllegalArgumentException if unsupported unit or unit conversion is not allowed
     */
    public static FitsImageFile load(final String absFilePath, final boolean firstOnly) throws FitsException, IOException, IllegalArgumentException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("loading " + absFilePath);
        }

        // Check if the given file exists :
        if (!new File(absFilePath).exists()) {
            throw new IOException("File not found: " + absFilePath);
        }

        Fits fitsFile = null;
        try {
            // create new Fits image structure:
            final FitsImageFile imgFitsFile = new FitsImageFile(absFilePath);

            final long start = System.nanoTime();

            // open the fits file :
            fitsFile = new Fits(absFilePath);

            // read the complete file structure :
            final List<BasicHDU> hduList = read(fitsFile);

            // process all HD units :
            if (!hduList.isEmpty()) {
                processHDUnits(imgFitsFile, hduList, firstOnly);
            }

            if (logger.isLoggable(Level.INFO)) {
                logger.info("load: duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");
            }

            return imgFitsFile;

        } catch (FitsException fe) {
            logger.log(Level.SEVERE, "Unable to load the file: " + absFilePath, fe);
            throw fe;
        } finally {
            if (fitsFile != null && fitsFile.getStream() != null) {
                try {
                    fitsFile.getStream().close();
                } catch (IOException ioe) {
                    logger.log(Level.FINE, "Closing Fits file", ioe);
                }
            }
        }
    }

    /**
     * Update the checksum keyword for the given HDU
     * @param hdu hdu to process
     * @return checksum value
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     */
    public static long updateChecksum(final BasicHDU hdu) throws FitsException, IOException {
        // compute and add checksum into HDU (header):
        return Fits.setChecksum(hdu);
    }

    /**
     * Return all image HDUs read from the given Fits object
     * @param fitsFile Fits object to read
     * @return list of ImageHDU
     * @throws FitsException if any fits or IO exception occurs
     */
    private static List<BasicHDU> read(final Fits fitsFile) throws FitsException {
        final List<BasicHDU> hduList = new LinkedList<BasicHDU>();

        try {
            while (fitsFile.getStream() != null) {
                final BasicHDU hdu = readHDU(fitsFile);
                if (hdu == null) {
                    break;
                }
                hduList.add(hdu);
            }
        } catch (IOException e) {
            throw new FitsException("IO error: " + e);
        }
        return hduList;
    }

    /** 
     * Read the next HDU on the default input stream.
     * Note: it skips truncated HDU
     * 
     * @param fitsFile Fits object to read
     * @return The HDU read, or null if an EOF was detected.
     * Note that null is only returned when the EOF is detected immediately
     * at the beginning of reading the HDU.
     * 
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     */
    private static BasicHDU readHDU(final Fits fitsFile) throws FitsException, IOException {

        final ArrayDataInput dataStr = fitsFile.getStream();
        if (dataStr == null || fitsFile.isAtEOF()) {
            return null;
        }

        if (!fitsFile.isGzipCompressed() && fitsFile.getLastFileOffset() > 0) {
            FitsUtil.reposition(dataStr, fitsFile.getLastFileOffset());
        }

        final Header hdr = Header.readHeader(dataStr);
        if (hdr == null) {
            fitsFile.setAtEOF(true);
            return null;
        }

        // Hack for ImageHDU having NAXIS > 2 and NAXISn=1
        fixAxesInHeader(hdr);

        final Data datum = hdr.makeData();
        try {
            datum.read(dataStr);
        } catch (PaddingException pe) {
            // ignore truncated HDU ...
            fitsFile.setAtEOF(true);
            return null;
        }

        fitsFile.setLastFileOffset(FitsUtil.findOffset(dataStr));

        return FitsFactory.HDUFactory(hdr, datum);
    }

    /**
     * Fix header for degenerated AXES (ie NAXISn = 1) ie remove such axes 
     * to get data arrays having less dimensions
     * @param hdr fits header
     * @throws FitsException if any IO / Fits exception occurs
     */
    private static void fixAxesInHeader(final Header hdr) throws FitsException {
        final int nAxis = hdr.getIntValue(FitsConstants.KEYWORD_NAXIS, 0);
        if (nAxis < 0) {
            throw new FitsException("Negative NAXIS value " + nAxis);
        }
        if (nAxis > 999) {
            throw new FitsException("NAXIS value " + nAxis + " too large");
        }

        if (nAxis == 0) {
            return;
        }

        final int[] axes = new int[nAxis];
        for (int i = 1; i <= nAxis; i++) {
            axes[i - 1] = hdr.getIntValue(FitsConstants.KEYWORD_NAXIS + i, 0);

            if (logger.isLoggable(Level.FINE)) {
                logger.fine(FitsConstants.KEYWORD_NAXIS + i + " = " + axes[i - 1]);
            }
        }

        int newNAxis = 0;
        // Find axes with NAxisn != 1
        for (int i = nAxis - 1; i >= 0; i--) {
            if (axes[i] <= 1) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("remove NAXIS" + (i + 1));
                }
                hdr.removeCard(FitsConstants.KEYWORD_NAXIS + (i + 1));
            } else {
                newNAxis++;
            }
        }

        // Update NAXIS:
        if (newNAxis != nAxis) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("updated NAXIS = " + newNAxis);
            }
            hdr.setNaxes(newNAxis);
        }
    }

    /**
     * Process all Fits HD units to load Fits images (skip other HDU) into the given FitsImageFile structure
     * @param imgFitsFile FitsImageFile structure to use
     * @param hdus list of HD units 
     * @param firstOnly load only the first valid Image HDU
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     * @throws IllegalArgumentException if unsupported unit or unit conversion is not allowed
     */
    private static void processHDUnits(final FitsImageFile imgFitsFile, final List<BasicHDU> hdus, final boolean firstOnly) throws FitsException, IOException, IllegalArgumentException {

        final int nbHDU = hdus.size();
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("processHDUnits: number of HDU = " + nbHDU);
        }

        final List<FitsImageHDU> fitsImageHDUs = imgFitsFile.getFitsImageHDUs();

        // start from Primary HDU
        int i = 0;
        for (BasicHDU hdu : hdus) {

            FitsImageHDU imageHDU = null;

            if (hdu instanceof ImageHDU) {
                final ImageHDU imgHdu = (ImageHDU) hdu;

                final int nAxis = getNAxis(imgHdu);

                if (nAxis > 3) {
                    logger.info("Skipped ImageHDU#" + i + " [" + imgFitsFile.getFileName()
                            + "] - Unsupported NAXIS = " + nAxis);

                } else if (nAxis == 3) {
                    // Fits cube:
                    final int nAxis3 = imgHdu.getHeader().getIntValue(FitsConstants.KEYWORD_NAXIS3);

                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("FITS CUBE ImageHDU#" + i + " [" + imgFitsFile.getFileName() + "] - NAXIS3 = " + nAxis3);
                    }

                    // Create Image HDU:
                    imageHDU = new FitsImageHDU();
                    imageHDU.setHduIndex(i);

                    // load all images in fits cube:
                    for (int imageIndex = 1; imageIndex <= nAxis3; imageIndex++) {
                        final FitsImage image = new FitsImage();
                        // define image HDU:
                        image.setFitsImageHDU(imageHDU);

                        // define the fits image identifier:
                        image.setFitsImageIdentifier(imgFitsFile.getFileName() + '#' + i + '-' + imageIndex + '/' + nAxis3);
                        image.setImageIndex(imageIndex);

                        // load image:
                        processImage(imgHdu, image, imageIndex);

                        // skip empty images:
                        if (image.getNbRows() <= 0 || image.getNbCols() <= 0) {
                            logger.info("Skipped ImageHDU#" + i + " [" + imgFitsFile.getFileName() + "][" + imageIndex + "/" + nAxis3
                                    + "] - Incorrect size = " + image.getNbCols() + " x " + image.getNbRows());
                        } else {
                            // register the image :
                            imageHDU.getFitsImages().add(image);
                        }
                    }

                } else {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("FITS IMAGE ImageHDU#" + i + " [" + imgFitsFile.getFileName() + ']');
                    }

                    // Create Image HDU:
                    imageHDU = new FitsImageHDU();
                    imageHDU.setHduIndex(i);

                    final FitsImage image = new FitsImage();
                    // define image HDU:
                    image.setFitsImageHDU(imageHDU);

                    // define the fits image identifier:
                    image.setFitsImageIdentifier(imgFitsFile.getFileName() + '#' + i);

                    // load image:
                    processImage(imgHdu, image, UNDEFINED_INDEX);

                    // skip empty images:
                    if (image.getNbRows() <= 0 || image.getNbCols() <= 0) {
                        logger.info("Skipped ImageHDU#" + i + " [" + imgFitsFile.getFileName()
                                + "] - Incorrect size = " + image.getNbCols() + " x " + image.getNbRows());
                    } else {
                        // register the image :
                        imageHDU.getFitsImages().add(image);
                    }
                }

                // Finish ImageHDU:
                if (imageHDU != null && imageHDU.getImageCount() != 0) {
                    // update checksum:
                    imageHDU.setChecksum(updateChecksum(imgHdu));

                    // register the image HDU:
                    fitsImageHDUs.add(imageHDU);

                    if (firstOnly) {
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine("First HDU loaded; skipping other HDUs ...");
                        }
                        return;
                    }
                }

            } else {
                logger.info("Skipped " + hdu.getClass().getSimpleName() + '#' + i + " [" + imgFitsFile.getFileName() + "]");
            }

            // increment i:
            i++;
        }
    }

    /**
     * Return the NAXIS keyword value
     * @param hdu image HDU
     * @return NAXIS keyword value
     * @throws FitsException if NAXIS < 0 or > 999
     */
    private static int getNAxis(final ImageHDU hdu) throws FitsException {
        final int nAxis = hdu.getHeader().getIntValue(FitsConstants.KEYWORD_NAXIS, 0);
        if (nAxis < 0) {
            throw new FitsException("Negative NAXIS value " + nAxis);
        }
        if (nAxis > 999) {
            throw new FitsException("NAXIS value " + nAxis + " too large");
        }
        return nAxis;
    }

    /**
     * Process a given Fits image to fill the given FitsImage object with header and image data
     * @param hdu image HDU
     * @param image Fits image
     * @param imageIndex image plane index [1..n] for Fits cube or -1 for Fits image
     * @throws FitsException if any FITS error occurred
     * @throws IllegalArgumentException if unsupported unit or unit conversion is not allowed
     */
    private static void processImage(final ImageHDU hdu, final FitsImage image, final int imageIndex) throws FitsException, IllegalArgumentException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("processImage: " + image);
        }

        // get Fits header :
        processKeywords(hdu.getHeader(), image, imageIndex);

        processData(hdu, image, imageIndex);
    }

    /**
     * Process the image header to get import keywords
     * @param header image header
     * @param image Fits image
     * @param imageIndex image plane index [1..n] for Fits cube or -1 for Fits image
     * @throws FitsException if any FITS error occurred
     */
    private static void processKeywords(final Header header, final FitsImage image, final int imageIndex) throws FitsException {
        // Note : a fits keyword has a KEY, VALUE AND COMMENT

        // Fix image index to 1 for a fits image:
        final int imgIndex = (imageIndex == UNDEFINED_INDEX) ? 1 : imageIndex;

        // Handle x-y axes dimensions:
        /*
         KEYWORD NAXIS = '2'	// Number of axes
         KEYWORD NAXIS1 = '512'	// Axis length
         KEYWORD NAXIS2 = '512'	// Axis length
         */
        // note: x axis has keyword index 1:
        image.setNbCols(header.getIntValue(FitsConstants.KEYWORD_NAXIS1, 0));
        // note: y axis has keyword index 2:
        image.setNbRows(header.getIntValue(FitsConstants.KEYWORD_NAXIS2, 0));

        // Parse all axis units:
        final FitsUnit unit1 = FitsUnit.parseUnit(header.getStringValue(FitsImageConstants.KEYWORD_CUNIT1));
        final FitsUnit unit2 = FitsUnit.parseUnit(header.getStringValue(FitsImageConstants.KEYWORD_CUNIT2));
        final FitsUnit unit3 = FitsUnit.parseUnit(header.getStringValue(FitsImageConstants.KEYWORD_CUNIT3));

        // Process reference pixel:
        /*
         KEYWORD CRPIX1 = '256.'	// Reference pixel
         KEYWORD CRPIX2 = '256.'	// Reference pixel
         KEYWORD CRPIX3 = '1.000000'
         */
        image.setPixRefCol(header.getDoubleValue(FitsImageConstants.KEYWORD_CRPIX1, FitsImageConstants.DEFAULT_CRPIX));
        image.setPixRefRow(header.getDoubleValue(FitsImageConstants.KEYWORD_CRPIX2, FitsImageConstants.DEFAULT_CRPIX));
        image.setPixRefWL(header.getDoubleValue(FitsImageConstants.KEYWORD_CRPIX3, FitsImageConstants.DEFAULT_CRPIX));

        // Process coordinates at the reference pixel:
        // note: units are ignored
        /*
         KEYWORD CRVAL1 = '0.'	// Coordinate at reference pixel
         KEYWORD CRVAL2 = '0.'	// Coordinate at reference pixel
         KEYWORD CRVAL3 = '1.600000'
         */
        image.setValRefCol(unit1.convert(header.getDoubleValue(FitsImageConstants.KEYWORD_CRVAL1, FitsImageConstants.DEFAULT_CRVAL), FitsUnit.ANGLE_RAD));
        image.setValRefRow(unit2.convert(header.getDoubleValue(FitsImageConstants.KEYWORD_CRVAL2, FitsImageConstants.DEFAULT_CRVAL), FitsUnit.ANGLE_RAD));
        image.setValRefWL(unit3.convert(header.getDoubleValue(FitsImageConstants.KEYWORD_CRVAL3, Double.NaN), FitsUnit.WAVELENGTH_METER));

        // Process increments along axes:
        /*
         KEYWORD CDELT1 = '-1.2E-10' // Coord. incr. per pixel (original value)
         KEYWORD CDELT2 = '1.2E-10'	 // Coord. incr. per pixel (original value)
         KEYWORD CDELT3 = '0.000000'
         */
        image.setSignedIncCol(unit1.convert(header.getDoubleValue(FitsImageConstants.KEYWORD_CDELT1, FitsImageConstants.DEFAULT_CDELT), FitsUnit.ANGLE_RAD));
        image.setSignedIncRow(unit2.convert(header.getDoubleValue(FitsImageConstants.KEYWORD_CDELT2, FitsImageConstants.DEFAULT_CDELT), FitsUnit.ANGLE_RAD));
        image.setIncWL(unit3.convert(header.getDoubleValue(FitsImageConstants.KEYWORD_CDELT3, Double.NaN), FitsUnit.WAVELENGTH_METER));

        // TODO: handle CTYPEn keywords:
        /*
         * However, the additional presence of CTYPE3 can affect
         * the 3rd axis definition:
         * CTYPE3  = 'FREQUENCY'          / means that Cxxxx3 are in Hz
         * or
         * CTYPE3  = 'WAVELENGTH'         / means that Cxxxx3 are in Microns
         */
        // Fix missing CUNIT3 but values given in microns instead of meters:
        if (image.getValRefWL() > 1e-1d) {
            image.setValRefWL(image.getValRefWL() * 1e-6d);
            image.setIncWL(image.getIncWL() * 1e-6d);

            if (imageIndex <= 1) {
                // only report on the first image:
                logger.warning("Fixed missing Wavelength unit (microns instead of meter): CRVAL3=" + image.getValRefWL() + " - CDELT3=" + image.getIncWL());
            }
        }

        // Process data min/max:
        /*
         KEYWORD DATAMAX = '5120.758'	// Maximum data value
         KEYWORD DATAMIN = '0.0'	// Minimum data value
         */
        // note: data min/max are later recomputed (missing / invalid values or bad precision)
        image.setDataMin(header.getDoubleValue(FitsConstants.KEYWORD_DATAMIN, FitsImageConstants.DEFAULT_DATA_MIN));
        image.setDataMax(header.getDoubleValue(FitsConstants.KEYWORD_DATAMAX, FitsImageConstants.DEFAULT_DATA_MAX));

        // Copy all header cards:
        final FitsImageHDU imageHDU = image.getFitsImageHDU();
        if (imageHDU != null) {
            final List<FitsHeaderCard> headerCards = imageHDU.getHeaderCards(header.getNumberOfCards());

            // avoid reentrance for Fits cube:
            if (headerCards.isEmpty()) {
                HeaderCard card;
                String key;
                for (Iterator<?> it = header.iterator(); it.hasNext();) {
                    card = (HeaderCard) it.next();

                    key = card.getKey();

                    if ("END".equals(key)) {
                        break;
                    }

                    headerCards.add(new FitsHeaderCard(key, card.getValue(), card.getComment()));
                }

                imageHDU.trimHeaderCards();
            }
        }
    }

    /**
     * Process the image data and store them in the given FitsImage
     * @param hdu image HDU
     * @param image Fits image
     * @param imageIndex image plane index [1..n] for Fits cube or -1 for Fits image
     * @throws FitsException if any FITS error occured
     */
    private static void processData(final ImageHDU hdu, final FitsImage image, final int imageIndex) throws FitsException {

        // load the complete image:
        final ImageData fitsData = (ImageData) hdu.getData();

        final Object allData = fitsData.getData();

        if (allData != null) {

            // interpret also BSCALE / BZERO (BUNIT) if present
            final int bitPix = hdu.getBitPix();
            final double bZero = hdu.getBZero();
            final double bScale = hdu.getBScale();

            final int nbCols = image.getNbCols();
            final int nbRows = image.getNbRows();

            // get image plane:
            final Object planeData = (imageIndex != UNDEFINED_INDEX) ? getPlaneData(allData, bitPix, imageIndex) : allData;

            // convert any data to float[][]:
            final float[][] imgData = getImageData(nbRows, nbCols, bitPix, planeData, bZero, bScale);

            image.setData(imgData);
        }
    }

    /**
     * Extract from the given 3D array the image plane at the given index
     * @param array3D raw data array
     * @param bitpix bit per pixels
     * @param imageIndex image plane index [1..n]
     * @return array2D or null if invalid bitpix or image index
     */
    private static Object getPlaneData(final Object array3D, final int bitpix, final int imageIndex) {
        if (array3D == null || imageIndex < 0) {
            return null;
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("bitPix     = " + bitpix);
            logger.fine("imageIndex = " + imageIndex);
        }

        final int imgIndex = imageIndex - 1;

        switch (bitpix) {
            case BasicHDU.BITPIX_BYTE:
                final byte[][][] bArray = (byte[][][]) array3D;
                if (imgIndex < bArray.length) {
                    return bArray[imgIndex];
                }
                break;
            case BasicHDU.BITPIX_SHORT:
                final short[][][] sArray = (short[][][]) array3D;
                if (imgIndex < sArray.length) {
                    return sArray[imgIndex];
                }
                break;
            case BasicHDU.BITPIX_INT:
                final int[][][] iArray = (int[][][]) array3D;
                if (imgIndex < iArray.length) {
                    return iArray[imgIndex];
                }
                break;
            case BasicHDU.BITPIX_LONG:
                final long[][][] lArray = (long[][][]) array3D;
                if (imgIndex < lArray.length) {
                    return lArray[imgIndex];
                }
                break;
            case BasicHDU.BITPIX_FLOAT:
                final float[][][] fArray = (float[][][]) array3D;
                if (imgIndex < fArray.length) {
                    return fArray[imgIndex];
                }
                break;
            case BasicHDU.BITPIX_DOUBLE:
                final double[][][] dArray = (double[][][]) array3D;
                if (imgIndex < dArray.length) {
                    return dArray[imgIndex];
                }
                break;
            default:
        }
        return null;
    }

    /**
     * Convert and optionaly scale the given array2D to float[][]
     * @param rows number of rows
     * @param cols number of columns
     * @param bitpix bit per pixels
     * @param array2D input array2D to convert
     * @param bZero zero point in scaling equation
     * @param bScale linear factor in scaling equation
     * @return float[][]
     */
    private static float[][] getImageData(final int rows, final int cols, final int bitpix, final Object array2D,
                                          final double bZero, final double bScale) {

        if (array2D == null) {
            return null;
        }

        final boolean doZero = (bZero != 0d);
        final boolean doScaling = (bScale != 1d);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("bitPix    = " + bitpix);
            logger.fine("doZero    = " + doZero);
            logger.fine("doScaling = " + doScaling);
        }

        if (bitpix == BasicHDU.BITPIX_FLOAT && !(doZero || doScaling)) {
            return (float[][]) array2D;
        }

        final float[][] output = new float[rows][cols];

        // 1 - convert data to float[][]
        float[] oRow;
        switch (bitpix) {
            case BasicHDU.BITPIX_BYTE:
                final byte[][] bArray = (byte[][]) array2D;
                byte[] bRow;
                for (int i, j = 0; j < rows; j++) {
                    oRow = output[j];
                    bRow = bArray[j];
                    for (i = 0; i < cols; i++) {
                        oRow[i] = (float) (bRow[i] & 0xFF);
                    }
                }
                break;
            case BasicHDU.BITPIX_SHORT:
                final short[][] sArray = (short[][]) array2D;
                short[] sRow;
                for (int i, j = 0; j < rows; j++) {
                    oRow = output[j];
                    sRow = sArray[j];
                    for (i = 0; i < cols; i++) {
                        oRow[i] = (float) sRow[i];
                    }
                }
                break;
            case BasicHDU.BITPIX_INT:
                final int[][] iArray = (int[][]) array2D;
                int[] iRow;
                for (int i, j = 0; j < rows; j++) {
                    oRow = output[j];
                    iRow = iArray[j];
                    for (i = 0; i < cols; i++) {
                        oRow[i] = (float) iRow[i];
                    }
                }
                break;
            case BasicHDU.BITPIX_LONG:
                final long[][] lArray = (long[][]) array2D;
                long[] lRow;
                for (int i, j = 0; j < rows; j++) {
                    oRow = output[j];
                    lRow = lArray[j];
                    for (i = 0; i < cols; i++) {
                        oRow[i] = (float) lRow[i];
                    }
                }
                break;
            case BasicHDU.BITPIX_FLOAT:
                // nothing to do
                break;
            case BasicHDU.BITPIX_DOUBLE:
                final double[][] dArray = (double[][]) array2D;
                double[] dRow;
                for (int i, j = 0; j < rows; j++) {
                    oRow = output[j];
                    dRow = dArray[j];
                    for (i = 0; i < cols; i++) {
                        oRow[i] = (float) dRow[i];
                    }
                }
                break;

            default:
        }

        // 2 - scale data:
        if (doZero || doScaling) {
            for (int i, j = 0; j < rows; j++) {
                oRow = output[j];
                for (i = 0; i < cols; i++) {
                    if (doScaling) {
                        oRow[i] *= bScale;
                    }
                    if (doZero) {
                        oRow[i] += bZero;
                    }
                }
            }
        }
        return output;
    }
}
