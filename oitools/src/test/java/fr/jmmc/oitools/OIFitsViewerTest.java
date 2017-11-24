/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools;

import fr.jmmc.oitools.util.FileUtils;
import fr.nom.tam.fits.FitsException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author bourgesl
 */
public class OIFitsViewerTest extends AbstractFileBaseTest {

    private static final String[] OIFITS_FILENAME = new String[]{
        "TEST_CreateOIFileV2.fits",
        "2008-Contest_Binary.oifits",
        "GRAVI.2016-06-23T03:10:17.458_singlesciviscalibrated.fits",
        "A-CLUSTER__2T3T__1-PHASEREF__SIMPLE_nsr0.05__20160812_193521_1.image-oi.oifits",
        "2012-03-24_ALL_oiDataCalib.fits",
        "testdata_opt.fits"
    };
    private final static int REMOTE_COUNT = 1;
    private final static String OIFITS_URL = "http://apps.jmmc.fr/oidata/BeautyContest/2008-Contest_Binary.oifits";

    @Test
    public void useOIFitsViewer() {
        for (String fileName : OIFITS_FILENAME) {
            String[] args = new String[]{
                "-t",
                "-c",
                "-v",
                /* file */
                TEST_DIR_OIFITS + fileName
            };
            OIFitsViewer.main(args);
        }
    }

    @Test
    public void dumpTSV() throws IOException, FitsException {
        final OIFitsViewer viewer = new OIFitsViewer(false, true, false, true);
        testProcessFiles("dumpTSV", ".csv", viewer);
    }

    @Test
    public void dumpXML() throws IOException, FitsException {
        final OIFitsViewer viewer = new OIFitsViewer(true, false, false, true);
        testProcessFiles("dumpXML", ".xml", viewer);
    }

    @Test
    public void dumpFormattedXML() throws IOException, FitsException {
        final OIFitsViewer viewer = new OIFitsViewer(true, false, true, true);
        testProcessFiles("dumpFormattedXML", "-formatted.xml", viewer);
    }

    @Test
    public void existDB_check() throws IOException, FitsException {
        for (String fileName : OIFITS_FILENAME) {
            existDB_process(TEST_DIR_OIFITS + fileName, false);
        }
    }

    @Test
    public void existDB_to_xml() throws IOException, FitsException {
        for (String fileName : OIFITS_FILENAME) {
            try {
                final String result = existDB_process(TEST_DIR_OIFITS + fileName, true);
                logger.log(Level.INFO, "existDB_to_xml:\n{0}", result);

                try {
                    DocumentBuilderFactory dBF = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = dBF.newDocumentBuilder();
                    InputSource is = new InputSource(new StringReader(result));
                    builder.parse(is);
                } catch (ParserConfigurationException ex) {
                    logger.log(Level.INFO, "existDB_to_xml:", ex);
                    // do not consider this case as a faulty xml output
                } catch (SAXException ex) {
                    logger.log(Level.INFO, "existDB_to_xml:", ex);
                    Assert.fail("OIFitsViewer xml output is not well formed");
                }

            } catch (RuntimeException re) {
                logger.log(Level.INFO, "existDB_to_xml:", re);
                // unexpected
                Assert.fail("exception not expected");
            }
        }
    }

    @Test
    public void existDB_unknown() throws IOException, FitsException {
        for (String fileName : OIFITS_FILENAME) {
            try {
                existDB_process(TEST_DIR_OIFITS + fileName + "UNKNOWN", true);
                Assert.fail("exception expected");
            } catch (RuntimeException re) {
                logger.log(Level.INFO, "existDB_unknown failure as expected:", re);
                // expected
            }
        }
    }

    @Test
    public void existDB_remote_to_xml() throws IOException, FitsException {
        try {
            for (int i = 0; i < REMOTE_COUNT; i++) {
                final String result = existDB_process(OIFITS_URL, true);
                logger.log(Level.INFO, "existDB_remote_to_xml:\n{0}", result);
                try {
                    Thread.sleep(10l);
                } catch (InterruptedException ie) {
                    logger.log(Level.INFO, "Interrupted");
                    break;
                }
            }
        } catch (RuntimeException re) {
            logger.log(Level.INFO, "existDB_remote_to_xml:", re);
            // unexpected
            Assert.fail("exception not expected");
        }
    }

    private static String existDB_process(final String filename, final boolean outputXml) {
        String output = null;
        try {
            logger.log(Level.INFO, "Process data from {0}", filename);
            final OIFitsViewer viewer = new OIFitsViewer(outputXml, true, false);
            output = viewer.process(filename);
        } catch (final Exception e) {
            throw new RuntimeException("Can't read oifits properly: " + e.getMessage(), e);
        }
        return output;
    }

    private void testProcessFiles(final String testName, final String extension, final OIFitsViewer viewer) throws IOException, FitsException {
        String failureMsg = "";
        for (String fileName : OIFITS_FILENAME) {
            try {
                String result = viewer.process(TEST_DIR_OIFITS + fileName);

                // Remove any absolute path (depending on the local machine):
                result = result.replaceAll(TEST_DIR_OIFITS, "");

                saveAndCompare(testName, fileName + extension, result);
            } catch (FileNotFoundException fnfe) {
                // log error:
                failureMsg += fnfe.getMessage() + "\n";
            } catch (AssertionError assertFailed) {
                // log error:
                failureMsg += "FILE: " + fileName + "\n" + assertFailed.getMessage() + "\n";
            }
        }
        if (!failureMsg.isEmpty()) {
            logger.log(Level.WARNING, "{0} failed:", testName);
            logger.warning(failureMsg);
            // you might want to collect more data
            Assert.fail(testName + " failed; see log for details");
        }
    }

    private void saveAndCompare(final String testName, final String fileName, final String result) throws IOException {
        logger.log(Level.FINE, "{0}:\n{1}", new Object[]{testName, result});
        FileUtils.writeFile(new File(TEST_DIR_TEST + fileName), result);

        String expected = FileUtils.readFile(new File(TEST_DIR_REF + fileName));
        assertEquals(expected, result);
    }

}
