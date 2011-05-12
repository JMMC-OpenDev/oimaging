/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.test;

/**
 * This interface holds several constants
 * @author bourgesl
 */
public interface TestEnv {
  /** folder containing oidata test files. By default $home/oidata/ */
  public final static String TEST_DIR = System.getProperty("user.home") + "/oidata/";
  /** folder containing copied oidata files. By default $home/oidata/copy/ */
  public final static String COPY_DIR = TEST_DIR + "copy/";

  /* constants */

  /** Logger associated to test classes */
  public final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
          "fr.jmmc.oitools.test");
}
