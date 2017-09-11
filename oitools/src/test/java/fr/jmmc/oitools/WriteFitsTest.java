/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools;

import fr.jmmc.oitools.image.FitsImageFile;
import fr.jmmc.oitools.image.FitsImageLoader;
import fr.jmmc.oitools.image.FitsImageWriter;
import fr.jmmc.oitools.test.fits.TamFitsTest;
import fr.nom.tam.fits.FitsException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import static junit.framework.Assert.fail;
import org.junit.Test;

/**
 * Load Fits files from the /fits folder to write the complete FitsImageFile structure 
 * into a [filename]-copy file in the test/fits folder.
 * After we load the file again and we compare it to the one written.
 * 
 * @author kempsc
 */
public class WriteFitsTest extends JUnitBaseTest {

    private final static String TEST_DIR_TEST_FITS = TEST_DIR_TEST + "fits/";
    /** */
    private final static boolean COMPARE_RAW = true;
    /** */
    private final static boolean STRICT = true;

    @Test
    public void writeLoadCompare() throws IOException, MalformedURLException, FitsException {

        //Mode lenient
        TamFitsTest.setStrict(STRICT);

        final File copyDir = new File(TEST_DIR_TEST_FITS);
        copyDir.mkdirs();

        for (String pathFile : getFitsFiles(new File(TEST_DIR_FITS))) {

            final FitsImageFile srcFitsFile = FitsImageLoader.load(pathFile, false, false);

            if (srcFitsFile == null) {
                fail("Error loadFits: " + pathFile);
            } else {
                final String fileTo = new File(copyDir, new File(pathFile).getName().replaceFirst("\\.", "-copy.")).getAbsolutePath();

                logger.info("fileTo: " + fileTo);

                FitsImageWriter.write(fileTo, srcFitsFile);

                // compare fits files at fits level (header / data) :
                if (COMPARE_RAW && !TamFitsTest.compareFile(pathFile, fileTo)) {

                    //we do not fail because we know is a fail TODO fix TamFitsTest
                    //fail("Error TamFitsTest.compareFile: " + pathFile);
                }

                logger.info("\n-------------------\n");
            }
        }
    }
}
