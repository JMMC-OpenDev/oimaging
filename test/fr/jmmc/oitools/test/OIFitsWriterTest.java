/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: OIFitsWriterTest.java,v 1.2 2010-06-02 11:52:27 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2010/05/28 14:57:45  bourgesl
 * first attempt to write OIFits from a loaded OIFitsFile structure
 *
 */
package fr.jmmc.oitools.test;

import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OIFitsWriter;
import fr.jmmc.oitools.test.fits.TamFitsTest;
import java.util.logging.Level;

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
      // Bad File path :
//      final String file = TEST_DIR + "toto";

      // Invalid OI Fits (Fits image) :
//      final String file = TEST_DIR + "other/YSO_disk.fits.gz";

      // Complex visibilities in VISDATA / VISERR (OI_VIS table) :
      final String fileSrc = TEST_DIR + "ASPRO-STAR_1-AMBER-08-OCT-2009T08:17:39";
      final String ext = ".fits";

      // 1 extra byte at the End of file + NaN in vis* data :
      // missing TEL_NAME (empty values) :
//      final String fileSrc = TEST_DIR + "Mystery-Med_H-AmberVISPHI";
//      final String ext = ".oifits.gz";

      // Single Wave Length => NWAVE = 1 => 1D arrays instead of 2D arrays :
//      final String file = TEST_DIR + "2004-data2.fits";


//      final String fileSrc = TEST_DIR + "2008-Contest2_H";
//      final String ext = ".oifits";

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

        // compare fits files :
        if (!TamFitsTest.compareFile(fileSrc + ext, fileTo)) {
          errors++;
        }

      }
    }

    logger.info("Errors = " + errors + " on " + n + " files.");
  }

  private static OIFitsFile load(final String absFilePath) {
    OIFitsFile oiFitsFile = null;
    try {
      logger.info("Loading file : " + absFilePath);

      final long start = System.nanoTime();

      oiFitsFile = OIFitsLoader.loadOIFits(absFilePath);

      logger.info("load : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");

    } catch (Throwable th) {
      logger.log(Level.SEVERE, "load : IO failure occured while reading file : " + absFilePath, th);
    }
    return oiFitsFile;
  }

  private static int write(final String absFilePath, final OIFitsFile oiFitsFile) {
    int error = 0;
    try {
      logger.info("Writing file : " + absFilePath);

      final long start = System.nanoTime();

      OIFitsWriter.writeOIFits(absFilePath, oiFitsFile);

      logger.info("write : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");

    } catch (Throwable th) {
      logger.log(Level.SEVERE, "write : IO failure occured while writing file : " + absFilePath, th);
      error = 1;
    }
    return error;
  }
}
