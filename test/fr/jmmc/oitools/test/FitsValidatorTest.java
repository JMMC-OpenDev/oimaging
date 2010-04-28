/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: FitsValidatorTest.java,v 1.1 2010-04-28 14:39:20 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 */
package fr.jmmc.oitools.test;

import fr.jmmc.oifits.validator.OifitsValidator;
import java.io.File;

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
      System.out.println("Errors = " + errors);
    }
  }

  public static int checkFile(final String absFilePath) {
    int error = 0;

    try {
      System.out.println("Checking file : " + absFilePath);

      OifitsValidator.main(new String[] {absFilePath});

    } catch (Throwable th) {
      System.out.println("IO failure occured while reading file : " + absFilePath);
      th.printStackTrace(System.out);
      error = 1;
    }
    return error;
  }
}
