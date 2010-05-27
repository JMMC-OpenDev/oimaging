/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: FileUtils.java,v 1.3 2010-05-27 14:43:43 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2010/05/03 14:25:46  bourgesl
 * removed comment
 *
 * Revision 1.1  2010/04/28 14:46:13  bourgesl
 * Simple FileUtils to wget remote files
 *
 */
package fr.jmmc.oitools.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

/**
 * This class gathers file utility methods
 * @author bourgesl
 */
public class FileUtils {
  /* constants */

  /** Logger associated to meta model classes */
  protected final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
          "fr.jmmc.oitools.util.FileUtils");

  /**
   * Forbidden constructor
   */
  private FileUtils() {
    super();
  }

  /**
   * Save remote file or GZipped file or file given by its url into a
   * temporary file.
   *
   * @param urlOrGZippedFilename string containing gzippes file name or
   * complete url
   * @return Filename of generated temporary file or null if error occured
   * @todo check if next part of code is not too hacked
   *
   * @throws MalformedURLException invalid url format
   * @throws IOException IO failure
   */
  public static String saveToFile(final String urlOrGZippedFilename) throws MalformedURLException, IOException {
    InputStream in;
    String filename;

    if (urlOrGZippedFilename.contains("://")) {
      /* Input parameter is an url */
      final URL url = new URL(urlOrGZippedFilename);
      filename = new File(url.getFile()).getName();

      if (filename.endsWith(".gz")) {
        /* File is gzipped. First of all, we have to gunzip it */
        in = new GZIPInputStream(url.openStream());
        filename = filename.substring(0, filename.length() - 3);
      } else {
        in = url.openStream();
      }
    } else if (urlOrGZippedFilename.endsWith(".gz")) {
      /* File is gzipped. First of all, we have to gunzip it */
      in = new GZIPInputStream(new FileInputStream(
              urlOrGZippedFilename));
      filename = urlOrGZippedFilename.substring(0,
              urlOrGZippedFilename.length() - 3);
      filename = new File(filename).getName();
    } else {
      /* Input file is neither a gzipped file, nor an url.
      So returning input string itself */
      return urlOrGZippedFilename;
    }

    /* Generating temporary file relative to input parameter */
    final File outFile = File.createTempFile(filename, ".fits");
    final String outFilename = outFile.getCanonicalPath();

    if (logger.isLoggable(Level.FINE)) {
      logger.fine("Creating temp file for " + urlOrGZippedFilename + " with filename=" + outFilename);
    }

    final OutputStream out = new FileOutputStream(outFile);
    try {
      // Transfer bytes from the compressed file to the output file
      final byte[] buf = new byte[1024];
      int len;

      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
    } finally {
      // Close the file and stream
      in.close();
      out.close();
    }

    return outFilename;
  }
}
