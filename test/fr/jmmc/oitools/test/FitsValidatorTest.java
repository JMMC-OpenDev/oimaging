/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: FitsValidatorTest.java,v 1.2 2010-06-02 11:52:27 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2010/04/28 14:39:20  bourgesl
 * basic test cases for OIValidator Viewer/Validator and new OIFitsLoader
 *
 */
package fr.jmmc.oitools.test;

import fr.jmmc.oifits.validator.OifitsValidator;
import java.io.File;
import java.util.logging.Level;

/**
 * This class contains test cases for the OIFitsViewer component
 * @author bourgesl
 */
public class FitsValidatorTest implements TestEnv {

  private FitsValidatorTest() {
    super();
  }

  public static void main(String[] args) {

    final File directory = new File(TEST_DIR);
    if (directory.exists() && directory.isDirectory()) {

      final File[] files = directory.listFiles();

      int errors = 0;
      for (File f : files) {
        if (f.isFile() && f.getName().endsWith("fits")) {
          errors += checkFile(f.getAbsolutePath());
        }
      }
      logger.info("Errors = " + errors);
    }
  }

  public static int checkFile(final String absFilePath) {
    int error = 0;

    try {
      logger.info("Checking file : " + absFilePath);

      OifitsValidator.main(new String[] {absFilePath});

    } catch (Throwable th) {
      logger.log(Level.SEVERE, "IO failure occured while reading file : " + absFilePath, th);
      error = 1;
    }
    return error;
  }
}
