/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: FitsInspector.java,v 1.1 2010-04-28 14:40:02 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 */
package fr.jmmc.oitools.test.fits;

import fr.jmmc.oitools.test.TestEnv;
import java.io.File;
import java.util.Enumeration;
import org.eso.fits.Fits;
import org.eso.fits.FitsColumn;
import org.eso.fits.FitsFile;
import org.eso.fits.FitsHDUnit;
import org.eso.fits.FitsHeader;
import org.eso.fits.FitsKeyword;
import org.eso.fits.FitsTable;

/**
 * This class loads an OIFits file to dump its header and tables
 * @author bourgesl
 */
public class FitsInspector implements TestEnv {
  /* constants */

  /** Logger associated to meta model classes */
  protected final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
          "fr.jmmc.oitools.model.OIFitsDumper");

  /* members */
  /** fits io */
  private final FitsIO fits;

  public FitsInspector(final String absPathFile) {
    this.fits = new FitsIO(new File(absPathFile));
  }

  public void info() {
    try {
      logger.info("Opened File : " + this.fits.getFilePath());

      final FitsFile fitsFile = this.fits.getFitsFile();

      // Loop on HDU :
      for (int i = 1; i < fitsFile.getNoHDUnits(); i++) {
        info(i, fitsFile.getHDUnit(i));
      }

    } finally {
      this.fits.close();
    }
  }

  private void info(final int n, final FitsHDUnit hdu) {

    logger.info("HDU [" + n + "] ---");

    final int type = hdu.getType();
    switch (type) {
      case Fits.IMAGE:
        logger.info("IMAGE");
        break;
      case Fits.ATABLE:
        logger.info("ASCII TABLE");
        break;
      case Fits.BTABLE:
        logger.info("BINARY TABLE");
        break;
      default:
    }

    // Header :
    final FitsHeader h = hdu.getHeader();

    logger.info("NAME    = " + h.getName());
    logger.info("VERSION = " + h.getVersion());

    logger.info("KEYWORDS [" + h.getNoKeywords() + "] ---");

    for (Enumeration<?> e = h.getKeywords(); e.hasMoreElements();) {
      FitsKeyword keyword = (FitsKeyword) e.nextElement();

      logger.info("  " + keyword.getName() + " = " + keyword.getString() + " // " + keyword.getComment());
    }
    logger.info("---");

    // Binary table Data :
    if (hdu.getType() == Fits.BTABLE) {
      final FitsTable t = (FitsTable) hdu.getData();
      logger.info("ROWS = " + t.getNoRows());

      logger.info("COLUMNS [" + t.getNoColumns() + "] ---");

      // Loop on Data Columns :
      for (int i = 0; i < t.getNoColumns(); i++) {
        FitsColumn c = t.getColumn(i);

        logger.info("  " + c.getLabel() + " [" + c.getRepeat() + c.getDataType() + "] (" + (c.getUnit() != null ? c.getUnit() : "") + ")");
//        c.
      }
    }
    logger.info("---");
  }

  public static void main(String[] args) {
    final FitsInspector fi = new FitsInspector(TEST_DIR + "2006-03-04.fits");

    fi.info();
  }
}
