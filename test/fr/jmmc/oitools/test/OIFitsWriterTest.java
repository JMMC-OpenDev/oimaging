/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: OIFitsWriterTest.java,v 1.1 2010-05-28 14:57:45 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 */
package fr.jmmc.oitools.test;

import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OIFitsWriter;

/**
 * This class provides test cases for the OIFitsWriter class
 * @author bourgesl
 */
public class OIFitsWriterTest implements TestEnv {

  /**
   * Forbidden constructor
   */
  private OIFitsWriterTest() {
    super();
  }

  public static void main(String[] args) {
    int n = 0;
    int errors = 0;

    if (true) {
      // Complex VISDATA :
//      final String file = TEST_DIR + "ASPRO-STAR_1-AMBER-08-OCT-2009T08:17:39.fits";

      // 1 extra byte at the End of file + NaN in vis* data :
      // missing TEL_NAME (empty values) :
//      final String fileSrc = TEST_DIR + "Mystery-Med_H-AmberVISPHI";
//      final String ext = ".oifits.gz";

      final String fileSrc = TEST_DIR + "2008-Contest2_H";
      final String ext = ".oifits";

      // Bug (Fits image):
//      final String file = TEST_DIR + "YSO_disk.fits.gz";

      n++;
      OIFitsFile oiFitsFile = load(fileSrc + ext);
      if (oiFitsFile == null) {
        errors++;
      } else {
        final String fileTo = fileSrc + "-copy.oifits";

        errors += write(fileTo, oiFitsFile);

        // verify and check :
        oiFitsFile = load(fileTo);
        if (oiFitsFile == null) {
          errors++;
        }
      }
    }

    System.out.println("Errors = " + errors + " on " + n + " files.");
  }

  private static OIFitsFile load(final String absFilePath) {
    OIFitsFile oiFitsFile = null;
    try {
      System.out.println("Loading file : " + absFilePath);

      final long start = System.nanoTime();

      oiFitsFile = OIFitsLoader.loadOIFits(absFilePath);

      System.out.println("load : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");

    } catch (Throwable th) {
      System.out.println("load : IO failure occured while reading file : " + absFilePath);
      th.printStackTrace(System.out);
      if (th.getCause() != null) {
        th.getCause().printStackTrace(System.out);
      }
    }
    return oiFitsFile;
  }

  private static int write(final String absFilePath, final OIFitsFile oiFitsFile) {
    int error = 0;
    try {
      System.out.println("Writing file : " + absFilePath);

      final long start = System.nanoTime();

      OIFitsWriter.writeOIFits(absFilePath, oiFitsFile);

      System.out.println("write : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");

    } catch (Throwable th) {
      System.out.println("write : IO failure occured while writing file : " + absFilePath);
      th.printStackTrace(System.out);
      if (th.getCause() != null) {
        th.getCause().printStackTrace(System.out);
      }
      error = 1;
    }
    return error;
  }
}
