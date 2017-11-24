/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools;

import fr.jmmc.oitools.model.DataModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

/**
 * Base class for JUnit tests to share utility methods
 *
 * @author bourgesl
 */
public class JUnitBaseTest {

    /* constants */
    /**
     * Logger associated to test classes
     */
    public final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(JUnitBaseTest.class.getName());

    /**
     * absolute path to test folder to load test resources
     */
    public final static String TEST_DIR = getProjectFolderPath() + "src/test/resources/";

    /**
     * absolute path to test folder to load FITS test resources
     */
    public final static String TEST_DIR_FITS = TEST_DIR + "fits/";

    /**
     * absolute path to test folder to load OIFITS test resources
     */
    public final static String TEST_DIR_OIFITS = TEST_DIR + "oifits/";

    /**
     * absolute path to test folder to load reference files
     */
    public final static String TEST_DIR_REF = TEST_DIR + "ref/";

    /**
     * absolute path to test folder to save test files
     */
    public final static String TEST_DIR_TEST = TEST_DIR + "test/";

    static {
        Locale.setDefault(Locale.US);

        /*
        * Enable support for OI_VIS Complex visibility columns
         */
        DataModel.setOiVisComplexSupport(true);
    }

    /**
     * Return the project folder path
     *
     * @return project folder path
     */
    public static String getProjectFolderPath() {
        try {
            String projectFolder = new File(".").getCanonicalFile().getCanonicalPath() + File.separatorChar;

            logger.log(Level.INFO, "project folder = {0}", projectFolder);

            return projectFolder;

        } catch (IOException ioe) {
            throw new RuntimeException("unable to get project folder: ", ioe);
        }
    }

    /**
     * Scan recusively the given folder to locate all fits files
     * @param directory folder to scan
     * @return list of absolute file paths
     */
    public static List<String> getFitsFiles(final File directory) {
        final List<String> listFiles = new ArrayList<String>();
        scanDirectory(directory, listFiles);
        return listFiles;
    }

    private static void scanDirectory(final File directory, final List<String> listFiles) {
        if (directory.exists() && directory.isDirectory()) {
            for (File f : directory.listFiles()) {
                if (f.isDirectory()) {
                    scanDirectory(f, listFiles);
                } else if (isFitsFile(f)) {
                    listFiles.add(f.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Return true if the given file is a FITS file i.e. its file name ends with 'fits' or 'fits.gz'
     * @param file file to test
     * @return true if the given file is a FITS file
     */
    public static boolean isFitsFile(final File file) {
        return file.isFile() && (file.getName().endsWith("fits") || file.getName().endsWith("fits.gz"));
    }
}
