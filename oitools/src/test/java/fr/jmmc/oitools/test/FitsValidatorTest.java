/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.test;

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

      // TODO port OifitsValidator in oitools
//      OifitsValidator.main(new String[] {absFilePath});

    } catch (Throwable th) {
      logger.log(Level.SEVERE, "IO failure occured while reading file : " + absFilePath, th);
      error = 1;
    }
    return error;
  }
}
