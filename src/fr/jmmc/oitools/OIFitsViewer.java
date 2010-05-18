/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: OIFitsViewer.java,v 1.2 2010-05-18 07:19:17 mella Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2010/04/29 14:14:39  bourgesl
 * refactored OIFitsViewer to use the OITools implementation
 *
 * Revision 1.2  2009/09/08 16:10:42  mella
 * add same block for all oitable after optionnal specific data
 *
 * Revision 1.1  2009/01/19 22:16:45  mella
 * *** empty log message ***
 *
 ******************************************************************************/
package fr.jmmc.oitools;

import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;

/**
 * This command line program loads OIFits files given as arguments
 * and print their XML description in the system out stream
 */
public class OIFitsViewer {

  /**
   * Creates a new OifitsViewer object.
   *
   * @param filename name of file to vizualize content.
   */
  public OIFitsViewer(final String filename) {
    try {
      final OIFitsFile oiFitsFile = OIFitsLoader.loadOIFits(filename);

      System.out.println(oiFitsFile.getXmlDesc());

    } catch (Exception e) {
      e.printStackTrace(System.err);
      System.out.println("Error reading file '" + filename + "'");
    }
  }

  /**
   * Main entry point.
   *
   * @param args command line arguments.
   */
  public static void main(String[] args) {
    for (int i = 0; i < args.length; i++) {
      new OIFitsViewer(args[i]);
    }
  }
}
/*___oOo___*/
