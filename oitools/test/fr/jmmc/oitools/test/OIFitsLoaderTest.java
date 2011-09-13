/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.test;

import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.XmlOutputVisitor;
import java.io.File;
import java.util.logging.Level;

/**
 * This class provides test cases for the OIFitsLoader class
 * @author bourgesl
 */
public class OIFitsLoaderTest implements TestEnv {

  /**
   * Forbidden constructor
   */
  private OIFitsLoaderTest() {
    super();
  }

  public static void main(String[] args) {
    int n = 0;
    int errors = 0;

    if (false) {
      // Bad File path :
//      final String file = TEST_DIR + "toto";

      // Invalid OI Fits (Fits image) :
//      final String file = TEST_DIR + "other/YSO_disk.fits.gz";

      // Complex visibilities in VISDATA / VISERR (OI_VIS table) :
//      final String file = TEST_DIR + "ASPRO-STAR_1-AMBER-08-OCT-2009T08:17:39.fits";

      // 1 extra byte at the End of file + NaN in vis* data :
//      final String file = TEST_DIR + "Mystery-Med_H-AmberVISPHI.oifits.gz";


      final String file = "/home/bourgesl/Documents/aspro-docs/aspro/ASPRO-ETA_TAU-AMBER-23-JUN-2010T15:26:46.fits";

      // Single Wave Length => NWAVE = 1 => 1D arrays instead of 2D arrays :
//      final String file = TEST_DIR + "2004-data2.fits";

      n++;
      errors += load(file);
    }

    if (true) {
      final File directory = new File(TEST_DIR);
      if (directory.exists() && directory.isDirectory()) {

        final long start = System.nanoTime();

        final File[] files = directory.listFiles();

        for (File f : files) {
          if (f.isFile() && (f.getName().endsWith("fits") || f.getName().endsWith("fits.gz"))) {
            n++;
            errors += load(f.getAbsolutePath());
          }
        }

        logger.info("dumpDirectory : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");
      }
    }

    logger.info("Errors = " + errors + " on " + n + " files.");
  }

  private static int load(final String absFilePath) {
    int error = 0;
    try {
      logger.info("Loading file : " + absFilePath);

      final long start = System.nanoTime();

      final OIFitsFile oiFitsFile = OIFitsLoader.loadOIFits(absFilePath);

      logger.info("load : toString : \n" + oiFitsFile.toString());

      if (false) {
        final boolean detailled = true;
        logger.info("load : XML DESC : \n" + XmlOutputVisitor.getXmlDesc(oiFitsFile, detailled));
      }

      logger.info("load : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");

    } catch (Throwable th) {
      logger.log(Level.SEVERE, "load : IO failure occured while reading file : " + absFilePath, th);
      error = 1;
    }
    return error;
  }
}
