/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: FitsIO.java,v 1.1 2010-04-28 14:40:02 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 */
package fr.jmmc.oitools.test.fits;

import java.io.File;
import java.io.IOException;
import org.eso.fits.FitsException;
import org.eso.fits.FitsFile;

/**
 * This class provides the FITS API as a statefull bean
 * @author bourgesl
 */
public class FitsIO {

  /* members */
  private final FitsFile fitsFile;

  /**
   * Public constructor
   * @param file fits file to open
   * @throws IllegalStateException if any IO error occured
   */
  public FitsIO(final File file) {
    this.fitsFile = openFile(file);
  }

  /**
   * Open the given Fits file
   * @param file file to open
   * @return fits file
   * @throws IllegalStateException if any IO error occured
   */
  private FitsFile openFile(final File file) throws IllegalStateException {
    try {
      return new FitsFile(file);
    } catch (IOException ioe) {
      throw new IllegalStateException("unable to open fits file : " + file, ioe);
    } catch (FitsException fe) {
      throw new IllegalStateException("unable to open fits file : " + file, fe);
    }
  }

  /**
   * Close the Fits file
   */
  public void close() {
    this.fitsFile.closeFile();
  }

  /**
   * Get the canonical path of the Fits file
   * @return canonical path of the Fits file
   */
  public String getFilePath() {
    return this.fitsFile.getName();
  }

  public FitsFile getFitsFile() {
    return this.fitsFile;
  }

}
