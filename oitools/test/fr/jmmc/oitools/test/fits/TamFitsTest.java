/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.test.fits;

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
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.logging.Level;

/**
 * This class makes several tests on nom.tam fits library
 * @author bourgesl
 */
public class TamFitsTest implements TestEnv {

    /** flag to disable infoFile() */
    private final static boolean INFO_ENABLE = true;
    /** flag to test complex data bug */
    private final static boolean TEST_COMPLEX_BUG = false;
    /** flag to dump column content */
    private final static boolean PRINT_COL = true;
    /** flag to compare keyword comments */
    private final static boolean COMPARE_KEYWORD_COMMENTS = false;
    /** flag to enable HIERARCH keyword support */
    private final static boolean USE_HIERARCH_FITS_KEYWORDS = true;

    /**
     * Forbidden constructor
     */
    private TamFitsTest() {
        super();
    }

    public static void main(String[] args) {

        // Set the default locale to en-US locale (for Numerical Fields "." ",")
        Locale.setDefault(Locale.US);

        int errors = 0;

        // enable / disable HIERARCH keyword support :
        FitsFactory.setUseHierarch(USE_HIERARCH_FITS_KEYWORDS);

        if (true) {
            String refFile, testFile;

            // Compare Reference & Test file (Aspro 0.8.1 / 0.8.2 beta):
            // OIFits files for AMBER:

            // Aspro2_FUN3.asprox

            // HIP_1234: Elongated disk model

            // Noise = false:
            refFile = TEST_DIR + "/Aspro2/Aspro2_HIP1234_AMBER_A1-G1-I1_2011-10-16_PROD.fits";
            testFile = TEST_DIR + "/Aspro2/Aspro2_HIP1234_AMBER_A1-G1-I1_2011-10-16_LATEST.fits";

            if (!compareFile(refFile, testFile)) {
                errors++;
            }

            // ETA TAU: Punct model

            // Noise = true:
            refFile = TEST_DIR + "/Aspro2/Aspro2_ETA_TAU_AMBER_A1-G1-I1_2011-10-16_PROD.fits";
            testFile = TEST_DIR + "/Aspro2/Aspro2_ETA_TAU_AMBER_A1-G1-I1_2011-10-16_LATEST.fits";

            if (!compareFile(refFile, testFile)) {
                errors++;
            }

            // HD_1234: Complex model (3 components):

            // Noise = true:
            refFile = TEST_DIR + "/Aspro2/Aspro2_HD_1234_AMBER_A1-G1-I1_2011-10-16_PROD.fits";
            testFile = TEST_DIR + "/Aspro2/Aspro2_HD_1234_AMBER_A1-G1-I1_2011-10-16_LATEST.fits";

            if (!compareFile(refFile, testFile)) {
                errors++;
            }

            // Medium resolution + Noise = true:
            refFile = TEST_DIR + "/Aspro2/Aspro2_HD_1234_AMBER_A1-G1-I1_2011-10-16_MED_PROD.fits";
            testFile = TEST_DIR + "/Aspro2/Aspro2_HD_1234_AMBER_A1-G1-I1_2011-10-16_MED_LATEST.fits";

            if (!compareFile(refFile, testFile)) {
                errors++;
            }

        }

        if (false) {
            final String file = TEST_DIR + "test_2010-12-02_HD37806_Pionier.fits";
            errors += infoFile(file);
            errors += dumpFile(file);
        }

        if (false) {
            // 1 extra byte at the End of file :
            final String file = TEST_DIR + "Mystery-Med_H-AmberVISPHI.oifits.gz";
//      final String file = TEST_DIR + "Mystery-Med_H-AmberVISPHI-copy.oifits";
            errors += infoFile(file);
            errors += dumpFile(file);
        }

        if (false) {
            // Complex Data (VISDATA) :

            // VISDATA is full of [0.0 0.0]
            //    dumpFile(TEST_DIR + "Theta1Ori2007Dec05_2.fits");

//      final String file = TEST_DIR + "ASPRO-STAR_1-AMBER-08-OCT-2009T08:17:39.fits";
//      final String file = TEST_DIR + "copy-ASPRO-STAR_1-AMBER.fits";
//      final String file = COPY_DIR + "2008-Contest1_H-copy.oifits";

            final String file = COPY_DIR + "test-create.oifits";

            errors += infoFile(file);
            errors += dumpFile(file);
        }

        if (false) {
            final String src = TEST_DIR + "ASPRO-STAR_1-AMBER-08-OCT-2009T08:17:39.fits";
            final String dst = TEST_DIR + "copy-ASPRO-STAR_1-AMBER.fits";

            copyFile(src, dst);

            if (!compareFile(src, dst)) {
                errors++;
            }
        }

        if (false) {
            final File directory = new File(TEST_DIR);
            if (directory.exists() && directory.isDirectory()) {

                final long start = System.nanoTime();

                final File[] files = directory.listFiles();

                for (File f : files) {
                    if (f.isFile() && (f.getName().endsWith("fits") || f.getName().endsWith("fits.gz"))) {
                        errors += infoFile(f.getAbsolutePath());
                        errors += dumpFile(f.getAbsolutePath());
                    }
                }

                logger.info("dumpDirectory : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");
            }
        }
        logger.info("Errors = " + errors);
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
            BinaryTableHDU bh;

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

                    if (h instanceof BinaryTableHDU) {
                        bh = (BinaryTableHDU) h;

                        if (TEST_COMPLEX_BUG) {
                            testComplexData(bh);
                        }
                    }

                }
            } while (h != null);

            logger.info("infoFile : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");

        } catch (Throwable th) {
            logger.log(Level.SEVERE, "infoFile : IO failure occured while reading file : " + absFilePath, th);
            error = 1;
        }
        return error;
    }

    private static void testComplexData(final BinaryTableHDU bh) throws FitsException {
        // test VISDATA column (complex data) :
        int idx = bh.findColumn("VISDATA");

        if (idx != -1) {
            // show content :

            BinaryTable bt = (BinaryTable) bh.getData();

            Object res;

            res = bt.getFlattenedColumn(idx);
            if (PRINT_COL) {
                logger.info("VISDATA (flat) = " + arrayToString(res));
            }

            res = bh.getColumn(idx);
            if (PRINT_COL) {
                logger.info("VISDATA (curl) = " + arrayToString(res));
            }
        }
    }

    private static int dumpFile(final String absFilePath) {
        int error = 0;

        logger.info("Dump file : " + absFilePath);

        final StringBuilder sb = new StringBuilder(16384);

        final long start = System.nanoTime();
        try {

            final Fits s = new Fits(absFilePath);

            final BasicHDU[] hdus = s.read();

            final int len = hdus.length;
            sb.append("HDUs = ").append(len).append("\n");

            BasicHDU hdu;
            for (int i = 0; i < len; i++) {
                hdu = hdus[i];

                // dump HDU:
                dumpHDU(sb, hdu);
            }

        } catch (Throwable th) {
            logger.log(Level.SEVERE, "dumpFile : failure occured while dumping file : " + absFilePath, th);
            error = 1;
        } finally {
            final long end = System.nanoTime();

            logger.info(sb.toString());
            logger.info("buffer len = " + sb.length());
            logger.info("dumpFile : duration = " + 1e-6d * (end - start) + " ms.");
        }

        return error;
    }

    private static void dumpHDU(final StringBuilder sb, final BasicHDU hdu) throws FitsException {
        dumpHeader(sb, hdu.getHeader());

        // dump binary HDU only :
        if (hdu instanceof BinaryTableHDU) {
            dumpData(sb, (BinaryTableHDU) hdu);
        } else {
            logger.warning("Unsupported HDU: " + hdu.getClass());
        }
    }

    private static void dumpHeader(final StringBuilder sb, final Header header) {

        final String extName = header.getTrimmedStringValue("EXTNAME");

        sb.append("--------------------------------------------------------------------------------\n");
        if (extName != null) {
            sb.append("EXTNAME = ").append(extName).append("\n");
        }

        final int nCards = header.getNumberOfCards();

        sb.append("KEYWORDS = ").append(nCards).append("\n");

        HeaderCard card;
        String key;
        for (Iterator<?> it = header.iterator(); it.hasNext();) {
            card = (HeaderCard) it.next();

            key = card.getKey();

            if ("END".equals(key)) {
                break;
            }

            sb.append("KEYWORD ").append(key).append(" = ");
            if (card.getValue() != null) {
                sb.append("'").append(card.getValue()).append("'");
            }
            sb.append("\t// ");
            if (card.getComment() != null) {
                sb.append(card.getComment());
            }
            sb.append("\n");
        }
    }

    private static void dumpData(final StringBuilder sb, final BinaryTableHDU hdu) throws FitsException {

        final BinaryTable data = (BinaryTable) hdu.getData();

        final int nCols = data.getNCols();

        sb.append("--------------------------------------------------------------------------------\n");
        sb.append("NCOLS = ").append(nCols).append("\n");

        final int nRows = data.getNRows();

        sb.append("NROWS = ").append(nRows).append("\n");

        String unit;
        Object array;
        for (int i = 0; i < nCols; i++) {
            // read all data and convert them to arrays[][] :
            array = data.getColumn(i);
            /*
             array = data.getFlattenedColumn(i);
             */

            sb.append("COLUMN ").append(hdu.getColumnName(i)).append(" [");
            sb.append(hdu.getColumnLength(i));
            sb.append(" ");
            sb.append(hdu.getColumnType(i));
            sb.append("] (");
            unit = hdu.getColumnUnit(i);
            if (unit != null) {
                sb.append(unit);
            }
            sb.append(")\t").append(ArrayFuncs.arrayDescription(array));

            if (PRINT_COL) {
                sb.append("\n").append(arrayToString(array));
            }
            sb.append("\n");
        }
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
                logger.info("ERROR:  different number of header card " + sCard + " <> " + dCard);
                res = false;
            }
            logger.info("KEYWORDS = " + sCard);

            HeaderCard srcCard, dstCard;
            String key;
            for (Iterator<?> it = srcHeader.iterator(); it.hasNext();) {
                srcCard = (HeaderCard) it.next();

                key = srcCard.getKey();

                if ("END".equals(key)) {
                    break;
                }

                dstCard = dstHeader.findCard(key);

                if (dstCard == null) {
                    logger.info("ERROR:  Missing header card " + key);
                    res = false;
                } else {
                    logger.info("KEYWORD " + key + " = " + (srcCard.getValue() != null ? "'" + srcCard.getValue() + "'" : "") + "\t// " + srcCard.getComment());

                    if (!srcCard.getValue().equals(dstCard.getValue())) {

                        if (key.startsWith("TUNIT")) {
                            logger.info("WARNING:  different value   of header card[" + key + "] '" + srcCard.getValue() + "' <> '" + dstCard.getValue() + "'");
                        } else if (key.startsWith("TFORM") && ("1" + srcCard.getValue()).equals(dstCard.getValue())) {
                            logger.info("INFO:  different value   of header card[" + key + "] '" + srcCard.getValue() + "' <> '" + dstCard.getValue() + "'");
                        } else {
                            logger.info("ERROR:  different value   of header card[" + key + "] '" + srcCard.getValue() + "' <> '" + dstCard.getValue() + "'");
                            res = false;
                        }
                    } else if (COMPARE_KEYWORD_COMMENTS && isChanged(srcCard.getComment(), dstCard.getComment())) {
                        logger.info("ERROR:  different comment of header card[" + key + "] '" + srcCard.getComment() + "' <> '" + dstCard.getComment() + "'");
                        res = false;
                    }
                }
            }
        }

        return res;
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
            logger.info("ERROR:  different number of columns " + sCol + " <> " + dCol);
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
                        logger.info("ERROR:  different values for column[" + srcHdu.getColumnName(i) + "]\nSRC=" + arrayToString(sArray) + "\nDST=" + arrayToString(dArray));
                        res = false;
                    } else {
                        if (PRINT_COL) {
                            logger.info("COLUMN " + srcHdu.getColumnName(i) + "\t" + ArrayFuncs.arrayDescription(sArray) + "\n" + arrayToString(sArray));
                        } else {
                            logger.info("COLUMN " + srcHdu.getColumnName(i) + "\t" + ArrayFuncs.arrayDescription(sArray));
                        }
                    }
                }
            }
        }

        return res;
    }

    public static String arrayToString(final Object o) {

        if (o == null) {
            return "null";
        }

        final Class<?> oClass = o.getClass();

        if (!oClass.isArray()) {
            return o.toString();
        }

        if (oClass == double[].class) {
            return Arrays.toString((double[]) o);
        } else if (oClass == float[].class) {
            return Arrays.toString((float[]) o);
        } else if (oClass == int[].class) {
            return Arrays.toString((int[]) o);
        } else if (oClass == long[].class) {
            return Arrays.toString((long[]) o);
        } else if (oClass == boolean[].class) {
            return Arrays.toString((boolean[]) o);
        } else if (oClass == short[].class) {
            return Arrays.toString((short[]) o);
        } else if (oClass == char[].class) {
            return Arrays.toString((char[]) o);
        } else if (oClass == byte[].class) {
            return Arrays.toString((byte[]) o);
        } else {
            /*
             if (oClass == String[].class) {
             StringBuilder sb = new StringBuilder(128).append("[");
             for (String val : (String[])o) {
             sb.append("'").append(val).append("'").append("=[");

             for (char ch : val.toCharArray()) {
             sb.append("0x").append(Integer.toHexString(ch)).append(' ');
             }

             sb.append("], ");
             }
             return sb.append("]").toString();
             }
             */
            // Non-primitive and multidimensional arrays can be
            // cast to Object[]
            final Object[] objArray = (Object[]) o;
            return Arrays.deepToString(objArray);
        }
    }
}
