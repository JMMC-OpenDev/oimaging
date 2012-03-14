/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.test;

import fr.jmmc.oitools.OIFitsViewer;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * This class contains test cases for the OIFitsViewer component
 * @author bourgesl
 */
public class FitsViewerTest implements TestEnv {

  private FitsViewerTest() {
    super();
  }

  public static void main(final String[] args) {
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
      errors += dumpFile(args, file);
    }

    if (true) {
      final File directory = new File(TEST_DIR);
      if (directory.exists() && directory.isDirectory()) {

        final long start = System.nanoTime();

        final File[] files = directory.listFiles();

        for (File f : files) {
          if (f.isFile() && (f.getName().endsWith("fits") || f.getName().endsWith("fits.gz"))) {
            n++;
            errors += dumpFile(args, f.getAbsolutePath());
          }
        }

        logger.info("dumpDirectory : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");

      }
    }
    logger.info("Errors = " + errors + " on " + n + " files.");
  }

  public static int dumpFile(final String[] args, final String absFilePath) {
    int error = 0;

    try {
      logger.info("Reading file : " + absFilePath);

      final long start = System.nanoTime();

      final List<String> arguments = new ArrayList<String>();
      arguments.addAll(Arrays.asList(args));
      arguments.add(absFilePath);
      OIFitsViewer.main(arguments.toArray(new String[0]));

      logger.info("dumpFile : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");

    } catch (Throwable th) {
      logger.log(Level.SEVERE, "IO failure occured while reading file : " + absFilePath, th);
      error = 1;
    }
    return error;
  }
}
