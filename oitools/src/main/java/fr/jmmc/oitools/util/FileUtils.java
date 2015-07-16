/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.util;

import fr.jmmc.jmcs.network.NetworkSettings;
import fr.nom.tam.fits.FitsUtil;
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

    static {
        NetworkSettings.defineDefaults();
    }

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
        /* Input parameter is an url */
        final URL url = new URL(urlLocation);

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Download URL: {0}", url);
        }

        // Follow up to 5 redirects:
        final InputStream in = FitsUtil.getURLStream(url, 0);

        /* Generating temporary file relative to input parameter */
        final File outFile = File.createTempFile(new File(url.getFile()).getName(), null);
        outFile.deleteOnExit();

        final String outFilename = outFile.getCanonicalPath();

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Creating temp file for {0} with filename={1}", new Object[]{url, outFilename});
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
