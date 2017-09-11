/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools;

import fr.nom.tam.fits.FitsException;
import java.io.IOException;
import java.util.logging.Level;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author bourgesl
 */
public class OIFitsViewerTest extends JUnitBaseTest {

    private final static String OIFITS_FILE = TEST_DIR_OIFITS + "2008-Contest_Binary.oifits";

    @Test
    public void useOIFitsViewer() {
        final String[] args = new String[]{
            "-t",
            "-c",
            "-v",
            /* file */
            OIFITS_FILE
        };
        OIFitsViewer.main(args);
    }

    @Test
    public void dumpTSV() throws IOException, FitsException {
        final OIFitsViewer viewer = new OIFitsViewer(false, true, false, true);
        final String result = viewer.process(OIFITS_FILE);
        logger.log(Level.INFO, "dumpTSV:\n{0}", result);
        // TODO: save and compare result
    }

    @Test
    public void dumpXML() throws IOException, FitsException {
        final OIFitsViewer viewer = new OIFitsViewer(true, false, false, true);
        final String result = viewer.process(OIFITS_FILE);
        logger.log(Level.INFO, "dumpXML:\n{0}", result);
        // TODO: save and compare result
    }

    @Test
    public void dumpFormattedXML() throws IOException, FitsException {
        final OIFitsViewer viewer = new OIFitsViewer(true, false, true, true);
        final String result = viewer.process(OIFITS_FILE);
        logger.log(Level.INFO, "dumpFormattedXML:\n{0}", result);
        // TODO: save and compare result
    }

    @Test
    public void existDB_check() throws IOException, FitsException {
        final String result = existDB_process(OIFITS_FILE, false);
        logger.log(Level.INFO, "existDB_check:\n{0}", result);
        // TODO: save and compare result
    }

    @Test
    public void existDB_to_xml() throws IOException, FitsException {
        try {
            existDB_process(OIFITS_FILE, true);
        } catch (RuntimeException re) {
            logger.log(Level.INFO, "existDB_to_xml:", re);
            // unexpected
            Assert.fail("exception not expected");
        }
        try {
            existDB_process(OIFITS_FILE + "UNKNOWN", true);

            Assert.fail("exception expected");
        } catch (RuntimeException re) {
            logger.log(Level.INFO, "existDB_to_xml failure as expected:", re);
            // expected
        }
    }

    private static String existDB_process(final String filename, final boolean outputXml) {
        // Get our viewer reference
        final OIFitsViewer v = new OIFitsViewer(outputXml, true, false);

        String output = null;
        try {
            logger.log(Level.INFO, "Process data from {0}", filename);
            output = v.process(filename);
        } catch (final Exception e) {
            throw new RuntimeException("Can't read oifits properly: " + e.getMessage(), e);
        }
        return output;
    }

}
