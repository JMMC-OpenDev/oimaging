/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: TamFitsTest.java,v 1.1 2010-04-28 14:41:13 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 */
package fr.jmmc.oitools.test.fits;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTable;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.util.ArrayFuncs;
import nom.tam.util.BufferedFile;

/**
 * This class makes several tests on nom.tam fits library
 * @author bourgesl
 */
public class TamFitsTest {

  /** folder containing oidata test files. By default $home/oidata/ */
  private final static String TEST_DIR = System.getProperty("user.home") + "/oidata/";
  /** flag to disable infoFile() */
  private final static boolean INFO_ENABLE = false;
  /** flag to test complex data bug */
  private final static boolean TEST_COMPLEX_BUG = true;
  /** flag to dump column content */
  private final static boolean PRINT_COL = false;
  /** flag to enable HIERARCH keyword support */
  private final static boolean USE_HIERARCH_FITS_KEYWORDS = true;

  private TamFitsTest() {
    super();
  }

  public static void main(String[] args) {
    int errors = 0;

    // enable / disable HIERARCH keyword support :
    FitsFactory.setUseHierarch(USE_HIERARCH_FITS_KEYWORDS);

    if (false) {
      // 1 extra byte at the End of file :
      final String file = TEST_DIR + "Mystery-Med_H-AmberVISPHI.oifits.gz";
      errors += infoFile(file);
      errors += dumpFile(file);
    }


    if (false) {
      // Complex Data (VISDATA) :

      // VISDATA is full of [0.0 0.0]
      //    dumpFile(TEST_DIR + "Theta1Ori2007Dec05_2.fits");

      final String file = TEST_DIR + "ASPRO-STAR_1-AMBER-08-OCT-2009T08:17:39.fits";
      errors += infoFile(file);
      errors += dumpFile(file);
    }

    if (false) {
      final String src = TEST_DIR + "ASPRO-STAR_1-AMBER-08-OCT-2009T08:17:39.fits";
      final String dest = TEST_DIR + "copy-ASPRO-STAR_1-AMBER.fits";

      copyFile(src, dest);

      if (!compareFile(src, dest)) {
        errors += 1;
      }
    }

    if (true) {
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

        System.out.println("dumpDirectory : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");
      }
    }
    System.out.println("Errors = " + errors);
  }

  public static int infoFile(final String absFilePath) {
    if (!INFO_ENABLE) {
      return 0;
    }
    int error = 0;

    try {
      System.out.println("Reading file : " + absFilePath);

      final long start = System.nanoTime();

      final Fits f = new Fits(absFilePath);

      BasicHDU h;
      BinaryTableHDU bh;

      int i = 0;
      do {
        h = f.readHDU();
        if (h != null) {
          if (i == 0) {
            System.out.println("\n\nPrimary header:\n");
          } else {
            System.out.println("\n\nExtension " + i + ":\n");
          }
          i += 1;

          h.info();

          if (h instanceof BinaryTableHDU) {
            bh = (BinaryTableHDU) h;

            if (TEST_COMPLEX_BUG) {
              testComplexData(bh);
            }
          }

        }
      } while (h != null);

      System.out.println("infoFile : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");

    } catch (Throwable th) {
      System.out.println("infoFile : IO failure occured while reading file : " + absFilePath);
      th.printStackTrace(System.out);
      if (th.getCause() != null) {
        th.getCause().printStackTrace(System.out);
      }
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
        System.out.println("VISDATA (flat) = " + arrayToString(res));
      }

      res = bh.getColumn(idx);
      if (PRINT_COL) {
        System.out.println("VISDATA (curl) = " + arrayToString(res));
      }
    }
  }

  private static int dumpFile(final String absFilePath) {
    int error = 0;

    System.out.println("Dump file : " + absFilePath);

    final StringBuilder sb = new StringBuilder(16384);

    final long start = System.nanoTime();
    try {

      final Fits s = new Fits(absFilePath);

      final BasicHDU[] sHdu = s.read();

      final int len = sHdu.length;
      sb.append("HDUs = ").append(len).append("\n");

      BasicHDU sH;
      for (int i = 0; i < len; i++) {
        sH = sHdu[i];

        // compare binary HDU only :
        if (sH instanceof BinaryTableHDU) {
          dumpHDU(sb, (BinaryTableHDU) sH);
        } else {
          dumpHDU(sb, sH);
        }
      }

    } catch (Throwable th) {
      System.out.println("dumpFile : failure occured while dumping file : " + absFilePath);
      th.printStackTrace(System.out);
      error = 1;
    } finally {
      final long end = System.nanoTime();

      System.out.println(sb.toString());
      System.out.println("buffer len = " + sb.length());
      System.out.println("dumpFile : duration = " + 1e-6d * (end - start) + " ms.");
    }

    return error;
  }

  private static void dumpHDU(final StringBuilder sb, final BasicHDU hdu) throws FitsException {
    dumpHeader(sb, hdu.getHeader());
  }

  private static void dumpHDU(final StringBuilder sb, final BinaryTableHDU sH) throws FitsException {
    dumpHeader(sb, sH.getHeader());

    dumpData(sb, sH);
  }

  private static void dumpHeader(final StringBuilder sb, final Header sHeader) {

    final String sExtName = sHeader.getTrimmedStringValue("EXTNAME");

    sb.append("--------------------------------------------------------------------------------\n");
    sb.append("EXTNAME = ").append(sExtName).append("\n");

    final int sCard = sHeader.getNumberOfCards();

    sb.append("KEYWORDS = ").append(sCard).append("\n");

    HeaderCard sHc;
    String key;
    for (Iterator<?> it = sHeader.iterator(); it.hasNext();) {
      sHc = (HeaderCard) it.next();

      key = sHc.getKey();

      if ("END".equals(key)) {
        break;
      }

      sb.append("KEYWORD ").append(key).append(" = ");
      if (sHc.getValue() != null) {
        sb.append("'").append(sHc.getValue()).append("'");
      }
      sb.append("\t// ").append(sHc.getComment()).append("\n");
    }
  }

  private static void dumpData(final StringBuilder sb, final BinaryTableHDU sH) throws FitsException {

    final BinaryTable sData = (BinaryTable) sH.getData();

    final int sCol = sData.getNCols();

    sb.append("--------------------------------------------------------------------------------\n");
    sb.append("NCOLS = ").append(sCol).append("\n");

    final int sRow = sData.getNRows();

    sb.append("NROWS = ").append(sRow).append("\n");

    String unit;
    Object sArray;
    for (int i = 0; i < sCol; i++) {
      // read all data and convert them to arrays[][] :
      sArray = sData.getColumn(i);
      /*
      sArray = sData.getFlattenedColumn(i);
       */

      sb.append("COLUMN ").append(sH.getColumnName(i)).append(" [");
      sb.append(sH.getColumnLength(i));
      sb.append(" ");
      sb.append(sH.getColumnType(i));
      sb.append("] (");
      unit = sH.getColumnUnit(i);
      if (unit != null) {
        sb.append(unit);
      }
      sb.append(")\t").append(ArrayFuncs.arrayDescription(sArray));

      if (PRINT_COL) {
        sb.append("\n").append(arrayToString(sArray));
      }
      sb.append("\n");
    }
  }

  public static int copyFile(final String absSrcPath, final String absDestPath) {
    int error = 0;

    BufferedFile bf = null;
    try {
      System.out.println("Copying file : " + absSrcPath + " to " + absDestPath);

      final long start = System.nanoTime();

      final Fits f = new Fits(absSrcPath);

      // read the complete file in memory :
      f.read();

      bf = new BufferedFile(absDestPath, "rw");

      f.write(bf);
      bf.close();
      bf = null;

      System.out.println("copyFile : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");

    } catch (Exception e) {
      System.out.println("copyFile : IO failure occured while copying file : " + absSrcPath);
      e.printStackTrace(System.out);
      error = 1;
    } finally {
      if (bf != null) {
        try {
          bf.close();
        } catch (IOException ioe) {
          System.out.println("copyFile : IO failure occured while closing file : " + absDestPath);
          ioe.printStackTrace(System.out);
          error = 1;
        }
      }
    }
    return error;
  }

  private static boolean compareFile(final String absSrcPath, final String absDestPath) {
    boolean res = false;

    try {
      System.out.println("Comparing files : " + absSrcPath + ", " + absDestPath);

      final Fits s = new Fits(absSrcPath);
      final Fits d = new Fits(absDestPath);

      final BasicHDU[] sHdu = s.read();
      final BasicHDU[] dHdu = d.read();

      if (sHdu.length != dHdu.length) {
        System.out.println("ERROR:  different number of hdu " + sHdu.length + " <> " + dHdu.length);
      } else {
        final int len = sHdu.length;
        System.out.println("HDUs = " + len);

        BasicHDU sH, dH;
        for (int i = 0; i < len; i++) {
          sH = sHdu[i];
          dH = dHdu[i];

          if (sH.getClass() != dH.getClass()) {
            System.out.println("ERROR:  different type of hdu " + sH.getClass() + " <> " + dH.getClass());
          } else {
            if (sH instanceof BinaryTableHDU) {
              res = compareHDU((BinaryTableHDU) sH, (BinaryTableHDU) dH);
            } else {
              res = compareHDU(sH, dH);
            }
          }
        }
      }

    } catch (Throwable th) {
      System.out.println("compareFile : failure occured while comparing files : " + absSrcPath + ", " + absDestPath);
      th.printStackTrace(System.out);
      res = false;
    }

    return res;
  }

  private static boolean compareHDU(final BasicHDU sH, final BasicHDU dH) throws FitsException {
    return compareHeader(sH.getHeader(), dH.getHeader());
  }

  private static boolean compareHDU(final BinaryTableHDU sH, final BinaryTableHDU dH) throws FitsException {

    // Headers :
    boolean res = compareHeader(sH.getHeader(), dH.getHeader());

    res = compareData(sH, dH);

    return res;
  }

  private static boolean compareHeader(final Header sHeader, final Header dHeader) {
    boolean res = true;

    final String sExtName = sHeader.getTrimmedStringValue("EXTNAME");
    final String dExtName = dHeader.getTrimmedStringValue("EXTNAME");

    if (sExtName != null && !sExtName.equals(dExtName)) {
      System.out.println("ERROR:  different extension name " + sExtName + " <> " + dExtName);
      res = false;
    } else {
      System.out.println("--------------------------------------------------------------------------------");
      System.out.println("EXTNAME = " + sExtName);

      final int sCard = sHeader.getNumberOfCards();
      final int dCard = dHeader.getNumberOfCards();

      if (sCard != dCard) {
        System.out.println("ERROR:  different number of header card " + sCard + " <> " + dCard);
        res = false;
      } else {
        System.out.println("KEYWORDS = " + sCard);

        HeaderCard sHc, dHc;
        String key;
        for (Iterator<?> it = sHeader.iterator(); it.hasNext();) {
          sHc = (HeaderCard) it.next();

          key = sHc.getKey();

          if ("END".equals(key)) {
            break;
          }

          dHc = dHeader.findCard(key);

          if (dHc == null) {
            System.out.println("ERROR:  Missing header card " + key);
            res = false;
          } else {
            System.out.println("KEYWORD " + key + " = " + (sHc.getValue() != null ? "'" + sHc.getValue() + "'" : "") + "\t// " + sHc.getComment());

            if (!sHc.getValue().equals(dHc.getValue())) {
              System.out.println("ERROR:  different value   of header card[" + key + "] '" + sHc.getValue() + "' <> '" + dHc.getValue() + "'");
              res = false;
            } else if (!sHc.getComment().trim().equals(dHc.getComment().trim())) {
              System.out.println("ERROR:  different comment of header card[" + key + "] '" + sHc.getComment() + "' <> '" + dHc.getComment() + "'");
              res = false;
            }
          }
        }
      }
    }

    return res;
  }

  private static boolean compareData(final BinaryTableHDU sH, final BinaryTableHDU dH) throws FitsException {

    final BinaryTable sData = (BinaryTable) sH.getData();
    final BinaryTable dData = (BinaryTable) dH.getData();

    boolean res = true;

    final int sCol = sData.getNCols();
    final int dCol = dData.getNCols();

    if (sCol != dCol) {
      System.out.println("ERROR:  different number of columns " + sCol + " <> " + dCol);
      res = false;
    } else {
      System.out.println("--------------------------------------------------------------------------------");
      System.out.println("NCOLS = " + sCol);

      final int sRow = sData.getNRows();
      final int dRow = dData.getNRows();

      if (sCol != dCol) {
        System.out.println("ERROR:  different number of rows " + sRow + " <> " + dRow);
        res = false;
      } else {
        System.out.println("NROWS = " + sRow);

        Object sArray, dArray;
        for (int i = 0; i < sCol; i++) {
          sArray = sData.getColumn(i);
          dArray = dData.getColumn(i);
          /*
          sArray = sData.getFlattenedColumn(i);
          dArray = dData.getFlattenedColumn(i);
           */
          if (!ArrayFuncs.arrayEquals(sArray, dArray)) {
            System.out.println("ERROR:  different values for column[" + sH.getColumnName(i) + "]\nSRC=" + arrayToString(sArray) + "\nDST=" + arrayToString(dArray));
            res = false;
          } else {
            if (PRINT_COL) {
              System.out.println("COLUMN " + sH.getColumnName(i) + "\t" + ArrayFuncs.arrayDescription(sArray) + "\n" + arrayToString(sArray));
            } else {
              System.out.println("COLUMN " + sH.getColumnName(i) + "\t" + ArrayFuncs.arrayDescription(sArray));
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
      // Non-primitive and multidimensional arrays can be
      // cast to Object[]
      final Object[] objArray = (Object[]) o;
      return Arrays.deepToString(objArray);
    }

  }
}
