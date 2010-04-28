/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: TestEnv.java,v 1.1 2010-04-28 14:39:20 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 */
package fr.jmmc.oitools.test;

/**
 * This interface holds several constants
 * @author bourgesl
 */
public interface TestEnv {
  /** folder containing oidata test files. By default $home/oidata/ */
  public final static String TEST_DIR = System.getProperty("user.home") + "/oidata/";

}
