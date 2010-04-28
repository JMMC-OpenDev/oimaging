/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: ModelBase.java,v 1.1 2010-04-28 14:47:37 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 */
package fr.jmmc.oitools.model;

/**
 * This is the base class for OIFitsFile and OITable classes
 * @author bourgesl
 */
public class ModelBase {
  /* constants */

  /** Logger associated to meta model classes */
  protected final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
          "fr.jmmc.oitools.meta");

  /** empty int array */
  protected final static short[] EMPTY_SHORT_ARRAY = new short[0];
  /** empty int array */
  protected final static int[] EMPTY_INT_ARRAY = new int[0];
  /** empty float array */
  protected final static float[] EMPTY_FLOAT_ARRAY = new float[0];
  /** empty double array */
  protected final static double[] EMPTY_DBL_ARRAY = new double[0];
  /** empty String array */
  protected final static String[] EMPTY_STRING = new String[0];

  /**
   * Public constructor
   */
  public ModelBase() {
    super();
  }

}
