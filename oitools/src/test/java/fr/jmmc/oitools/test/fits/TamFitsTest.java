/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.test.fits;

import fr.jmmc.oitools.fits.FitsUtils;
import fr.jmmc.oitools.test.TestEnv;
import fr.nom.tam.fits.BasicHDU;
import fr.nom.tam.fits.BinaryTable;
import fr.nom.tam.fits.BinaryTableHDU;
import fr.nom.tam.fits.Fits;
import fr.nom.tam.fits.FitsException;
import fr.nom.tam.fits.FitsFactory;
import fr.nom.tam.fits.Header;
import fr.nom.tam.fits.HeaderCard;
import fr.nom.tam.util.ArrayFuncs;
import fr.nom.tam.util.BufferedFile;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;

/**
 * This class makes several tests on nom.tam fits library
 * @author bourgesl
 */
public class TamFitsTest implements TestEnv {

    /** flag to disable infoFile() */
    private final static boolean INFO_ENABLE = false;
    /** flag to dump column content */
    private final static boolean PRINT_COL = false;
    /** flag to compare keyword comments */
    private static boolean COMPARE_KEYWORD_COMMENTS = false;
    /** flag to enable HIERARCH keyword support */
    private final static boolean USE_HIERARCH_FITS_KEYWORDS = true;
    /** flag to enable strict comparison */
    private static boolean STRICT = true;

    static {
        // enable / disable HIERARCH keyword support :
        FitsFactory.setUseHierarch(USE_HIERARCH_FITS_KEYWORDS);
    }

    public static boolean isStrict() {
        return STRICT;
    }

    public static void setStrict(boolean strict) {
        TamFitsTest.STRICT = strict;
    }

    /**
     * Forbidden constructor
     */
    private TamFitsTest() {
        super();
    }

    public static int infoFile(final String absFilePath) {
        if (!INFO_ENABLE) {
            return 0;
        }
        int error = 0;

        try {
            logger.info("Reading file : " + absFilePath);

            final long start = System.nanoTime();

            final Fits f = new Fits(absFilePath);

            BasicHDU h;

            int i = 0;
            do {
                h = f.readHDU();
                if (h != null) {
                    if (i == 0) {
                        logger.info("\n\nPrimary header:\n");
                    } else {
                        logger.info("\n\nExtension " + i + ":\n");
                    }
                    i++;

                    h.info();
                }
            } while (h != null);

            logger.info("infoFile : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");

        } catch (Throwable th) {
            logger.log(Level.SEVERE, "infoFile : IO failure occured while reading file : " + absFilePath, th);
            error = 1;
        }
        return error;
    }

    public static int dumpFile(final String absFilePath) {
        return FitsUtils.dumpFile(absFilePath, PRINT_COL) ? 1 : 0;
    }

    public static int copyFile(final String absSrcPath, final String absDestPath) {
        int error = 0;

        BufferedFile bf = null;
        try {
            logger.info("Copying file : " + absSrcPath + " to " + absDestPath);

            final long start = System.nanoTime();

            final Fits f = new Fits(absSrcPath);

            // read the complete file in memory :
            f.read();

            bf = new BufferedFile(absDestPath, "rw");

            f.write(bf);
            bf.close();
            bf = null;

            logger.info("copyFile : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "copyFile : IO failure occured while copying file : " + absSrcPath, e);
            error = 1;
        } finally {
            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException ioe) {
                    logger.log(Level.SEVERE, "copyFile : IO failure occured while closing file : " + absDestPath, ioe);
                    error = 1;
                }
            }
        }
        return error;
    }

    public static boolean compareFile(final String absSrcPath, final String absDestPath) {
        boolean res = true;

        try {
            logger.info("Comparing files : " + absSrcPath + ", " + absDestPath);

            final Fits s = new Fits(absSrcPath);
            final Fits d = new Fits(absDestPath);

            final BasicHDU[] srcHdus = s.read();
            final BasicHDU[] dstHdus = d.read();

            if (srcHdus.length != dstHdus.length) {
                logger.info("ERROR:  different number of hdu " + srcHdus.length + " <> " + dstHdus.length);
            } else {
                final int len = srcHdus.length;
                logger.info("HDUs = " + len);

                BasicHDU srcHdu, dstHdu;
                for (int i = 0; i < len; i++) {
                    srcHdu = srcHdus[i];
                    dstHdu = dstHdus[i];

                    if (srcHdu.getClass() != dstHdu.getClass()) {
                        logger.info("ERROR:  different type of hdu " + srcHdu.getClass() + " <> " + dstHdu.getClass());
                    } else {
                        res &= compareHDU(srcHdu, dstHdu);
                    }
                }
            }

        } catch (Throwable th) {
            logger.log(Level.SEVERE, "compareFile : failure occured while comparing files : " + absSrcPath + ", " + absDestPath, th);
            res = false;
        }

        return res;
    }

    private static boolean compareHDU(final BasicHDU srcHdu, final BasicHDU dstHdu) throws FitsException {

        if (!isStrict()) {
            if (!(srcHdu instanceof BinaryTableHDU) || !(dstHdu instanceof BinaryTableHDU)) {
                // if not strict ignore not binary table
                return true;
            }

        }
        // Headers:
        boolean res = compareHeader(srcHdu.getHeader(), dstHdu.getHeader());

        // Datas:
        if (srcHdu instanceof BinaryTableHDU && dstHdu instanceof BinaryTableHDU) {
            res &= compareData((BinaryTableHDU) srcHdu, (BinaryTableHDU) dstHdu);
        } else {
            logger.warning("Unsupported HDU: " + srcHdu.getClass());
        }

        return res;
    }

    private static boolean compareHeader(final Header srcHeader, final Header dstHeader) {
        boolean res = true;

        final String sExtName = srcHeader.getTrimmedStringValue("EXTNAME");
        final String dExtName = dstHeader.getTrimmedStringValue("EXTNAME");

        if (sExtName != null && !sExtName.equals(dExtName)) {
            logger.info("ERROR:  different extension name " + sExtName + " <> " + dExtName);
            res = false;
        } else {
            logger.info("--------------------------------------------------------------------------------");
            logger.info("EXTNAME = " + sExtName);

            final int sCard = srcHeader.getNumberOfCards();
            final int dCard = dstHeader.getNumberOfCards();

            if (sCard != dCard) {
                logger.info(errorPrefix(sExtName) + " different number of header card " + sCard + " <> " + dCard);
                res = false;
            }
            logger.info("KEYWORDS = " + sCard);

            HeaderCard srcCard, dstCard;
            String key;
            for (Iterator<?> it = srcHeader.iterator(); it.hasNext();) {
                srcCard = (HeaderCard) it.next();

                key = srcCard.getKey();

                if (key.equals("END")) {
                    break;
                }

                dstCard = dstHeader.findCard(key);

                if (dstCard == null) {
                    logger.info(errorPrefix(sExtName) + " Missing header card " + key + " was = " + srcCard.getValue());
                    res = false;
                } else {
                    logger.info("KEYWORD " + key + " = " + (srcCard.getValue() != null ? "'" + srcCard.getValue() + "'" : "")
                            + "\t// " + srcCard.getComment());
                    if (!srcCard.getValue().equals(dstCard.getValue())) {

                        res = particularCase(res, key, srcCard, dstCard, sExtName);

                    } else if (COMPARE_KEYWORD_COMMENTS && isChanged(srcCard.getComment(), dstCard.getComment())) {
                        logger.info("ERROR:  different comment of header card[" + key + "] '"
                                + srcCard.getComment() + "' <> '" + dstCard.getComment() + "'");
                        res = false;
                    }
                }
            }
        }

        return res;
    }

    private static boolean particularCase(boolean res, String key, HeaderCard srcCard, HeaderCard dstCard, String sExtName) {
        if (key.startsWith("TUNIT")) {
            logger.info("WARNING:  different value   of header card[" + key + "] '" + srcCard.getValue() + "' <> '" + dstCard.getValue() + "'");
            res = true;
        } else if (key.startsWith("TFORM") && ("1" + srcCard.getValue()).equals(dstCard.getValue())) {
            logger.info("INFO:  different value   of header card[" + key + "] '" + srcCard.getValue() + "' <> '" + dstCard.getValue() + "'");
        } else if (!isStrict() && (key.startsWith("ARRAY") || key.startsWith("NAXIS1")
                || key.startsWith("TFORM"))) {
            //if we are not strict, we chose to ignore the following cases: 
            //ARRAY XYZ because the voluntary error comes from a correction of the OIFits format 
            //NAXIS1 never be right if we have modification and correction in the write
            //TFORM because is a correction in the write for respect the OIFits format 
            res = true;
        } else {
            logger.info(errorPrefix(sExtName) + "different value   of header card[" + key + "] '"
                    + srcCard.getValue() + "' <> '" + dstCard.getValue());
            res = false;
        }
        return res;
    }
    
    private static String errorPrefix(final String sExtName) {
        if (sExtName != null) {
            return "ERROR: " + sExtName;
        }
        return "ERROR: ";
    }

    private static boolean isChanged(final String value1, final String value2) {
        return (value1 == null && value2 != null) || (value1 != null && value2 == null) || (value1 != null && value2 != null && !value1.trim().equalsIgnoreCase(value2.trim()));
    }

    private static boolean compareData(final BinaryTableHDU srcHdu, final BinaryTableHDU dstHdu) throws FitsException {

        final BinaryTable sData = (BinaryTable) srcHdu.getData();
        final BinaryTable dData = (BinaryTable) dstHdu.getData();

        boolean res = true;

        final int sCol = sData.getNCols();
        final int dCol = dData.getNCols();

        if (sCol != dCol) {
            logger.info("ERROR:  different number of columns " + sCol + " <> " + dCol + " in " + srcHdu.getColumnName(dCol));
            res = false;
        } else {
            logger.info("--------------------------------------------------------------------------------");
            logger.info("NCOLS = " + sCol);

            final int sRow = sData.getNRows();
            final int dRow = dData.getNRows();

            if (sCol != dCol) {
                logger.info("ERROR:  different number of rows " + sRow + " <> " + dRow);
                res = false;
            } else {
                logger.info("NROWS = " + sRow);

                Object sArray, dArray;
                for (int i = 0; i < sCol; i++) {
                    sArray = sData.getColumn(i);
                    dArray = dData.getColumn(i);
                    /*
                     sArray = sData.getFlattenedColumn(i);
                     dArray = dData.getFlattenedColumn(i);
                     */
                    if (!ArrayFuncs.arrayEquals(sArray, dArray)) {
                        logger.info("ERROR:  different values for column[" + srcHdu.getColumnName(i) + "]\nSRC="
                                + FitsUtils.arrayToString(sArray) + "\nDST=" + FitsUtils.arrayToString(dArray));
                        res = false;
                    } else {
                        if (PRINT_COL) {
                            logger.info("COLUMN " + srcHdu.getColumnName(i) + "\t" + ArrayFuncs.arrayDescription(sArray)
                                    + "\n" + FitsUtils.arrayToString(sArray));
                        } else {
                            logger.info("COLUMN " + srcHdu.getColumnName(i) + "\t" + ArrayFuncs.arrayDescription(sArray));
                        }
                    }
                }
            }
        }

        return res;
    }
}
