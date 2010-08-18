/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: OIFitsViewer.java,v 1.5 2010-08-18 09:39:56 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.4  2010/08/18 08:37:10  mella
 * Change default output format
 *
 * Revision 1.3  2010/08/17 14:17:53  mella
 * Output data in extended mode
 *
 * Revision 1.2  2010/05/18 07:19:17  mella
 * remove import of one old Class
 *
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

import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * This command line program loads OIFits files given as arguments
 * and print their XML description in the system out stream
 */
public final class OIFitsViewer {

  /* members */
  /** use number formatter */
  private final boolean format;
  /** use verbose output */
  private final boolean verbose;
  /** internal OIFits checker */
  private final OIFitsChecker checker;

  /**
   * Creates a new OifitsViewer object with default options.
   */
  public OIFitsViewer() {
    this(false, false);
  }

  /**
   * Creates a new OifitsViewer object.
   *
   * @param format use number formatter
   * @param verbose use verbose output
   */
  public OIFitsViewer(final boolean format, final boolean verbose) {
    this.format = format;
    this.verbose = verbose;

    this.checker = new OIFitsChecker();
  }

  /**
   * Process the given file
   *
   * @param filename name of the file to vizualize its content.
   */
  public void process(final String filename) {
    try {
      final OIFitsFile oiFitsFile = OIFitsLoader.loadOIFits(this.checker, filename);
      // output the minimal information in xml format
      System.out.println(oiFitsFile.getXmlDesc(this.verbose, this.format));

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
  public static void main(final String[] args) {

    boolean format = false;
    boolean verbose = false;

    final List<String> fileNames = new ArrayList<String>(args.length);

    // parse command line arguments :
    for (final String arg : args) {
      if (arg.startsWith("-")) {
        if (arg.equals("-f") || arg.equals("-format")) {
          format = true;
        } else if (arg.equals("-v") || arg.equals("-verbose")) {
          verbose = true;
        } else if (arg.equals("-h") || arg.equals("-help")) {
          showArgumentsHelp();
          System.exit(0);
        } else {
          error("'" + arg + "' option not supported.");
        }
      } else {
        fileNames.add(arg);
      }
    }

    if (fileNames.isEmpty()) {
      error("Missing file name argument.");
    }

    final OIFitsViewer viewer = new OIFitsViewer(format, verbose);

    for (String fileName : fileNames) {
      viewer.process(fileName);
    }
  }

  /**
   * Print an error message when parsing the command line arguments
   * @param message message to print
   */
  private static void error(final String message) {
    System.err.println(message);
    showArgumentsHelp();
    System.exit(1);
  }

  /** Show command arguments help */
  private static void showArgumentsHelp() {
    System.out.println(
            "-------------------------------------------------------------------------");
    System.out.println(
            "Usage: OIFitsViewer [-f|-format] [-v|-verbose] <file names>");
    System.out.println(
            "------------- Arguments help --------------------------------------------");
    System.out.println(
            "| Key          Value           Description                              |");
    System.out.println(
            "|-----------------------------------------------------------------------|");
    System.out.println(
            "| [-f] or [-format]            Use the number foramtter                 |");
    System.out.println(
            "| [-v] or [-verbose]           Dump all column data                     |");
    System.out.println(
            "| [-h|-help]                   Show arguments help                      |");
    System.out.println(
            "-------------------------------------------------------------------------");
  }
}
/*___oOo___*/
