/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;

/**
 * This class gathers file utility methods
 * @author bourgesl
 */
public class FileUtils {
    /* constants */

    /** Logger associated to meta model classes */
    protected final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FileUtils.class.getName());
    
    /**
     * Forbidden constructor
     */
    private FileUtils() {
        super();
    }

    /**
     * Save remote file into a temporary file.
     *
     * @param urlLocation remote filename
     * @return Filename of generated temporary file or null if error occurred
     *
     * @throws MalformedURLException invalid URL format
     * @throws IOException IO failure
     */
    public static String download(final String urlLocation) throws MalformedURLException, IOException {
        InputStream in;
        String filename;

        /* Input parameter is an url */
        final URL url = new URL(urlLocation);
        filename = new File(url.getFile()).getName();
        in = new BufferedInputStream(url.openStream());

        /* Generating temporary file relative to input parameter */
        final File outFile = File.createTempFile(filename, null);
        outFile.deleteOnExit();

        final String outFilename = outFile.getCanonicalPath();

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Creating temp file for " + url + " with filename=" + outFilename);
        }

        final OutputStream out = new BufferedOutputStream(new FileOutputStream(outFile));
        try {
            // Transfer bytes from the compressed file to the output file
            final byte[] buf = new byte[8192]; // 8K
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
