/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: FitsViewerTest.java,v 1.2 2010-04-29 14:16:24 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2010/04/28 14:39:19  bourgesl
 * basic test cases for OIValidator Viewer/Validator and new OIFitsLoader
 *
 */
package fr.jmmc.oitools.test;

import fr.jmmc.oifits.visualizer.OifitsViewer;
import fr.jmmc.oitools.OIFitsViewer;
import java.io.File;

/**
 * This class contains test cases for the OIFitsViewer component
 * @author bourgesl
 */
public class FitsViewerTest implements TestEnv {
  /** flag to use OITools impl instead of OIValidator */
  private final static boolean USE_OITOOLS = true;

  private FitsViewerTest() {
    super();
  }

  public static void main(String[] args) {
    int n = 0;
    int errors = 0;

    if (false) {
      // Complex VISDATA :
//      final String file = TEST_DIR + "ASPRO-STAR_1-AMBER-08-OCT-2009T08:17:39.fits";

      // 1 extra byte at the End of file :
//      final String file = TEST_DIR + "Mystery-Med_H-AmberVISPHI.oifits.gz";

      // Bug :
      final String file = TEST_DIR + "YSO_disk.fits.gz";

      n++;
      errors += dumpFile(file);
    }

    if (true) {
      final File directory = new File(TEST_DIR);
      if (directory.exists() && directory.isDirectory()) {

        final long start = System.nanoTime();

        final File[] files = directory.listFiles();

        for (File f : files) {
          if (f.isFile() && (f.getName().endsWith("fits") || f.getName().endsWith("fits.gz"))) {
            n++;
            errors += dumpFile(f.getAbsolutePath());
          }
        }

        System.out.println("dumpDirectory : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");

      }
    }
    System.out.println("Errors = " + errors + " on " + n + " files.");
  }

  public static int dumpFile(final String absFilePath) {
    int error = 0;

    try {
      System.out.println("Reading file : " + absFilePath);

      final long start = System.nanoTime();

      if (USE_OITOOLS) {
        new OIFitsViewer(absFilePath);
      } else {
        new OifitsViewer(absFilePath);
      }

      System.out.println("dumpFile : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");

    } catch (Throwable th) {
      System.out.println("IO failure occured while reading file : " + absFilePath);
      th.printStackTrace(System.out);
      error = 1;
    }
    return error;
  }
}
