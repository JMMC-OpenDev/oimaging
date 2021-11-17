/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging;

import fr.jmmc.jmcs.gui.component.ResizableTextViewFactory;
import fr.jmmc.jmcs.util.FileUtils;
import java.io.File;
import java.io.IOException;
import org.junit.Test;

/**
 *
 * @author bourgesl
 */
public class TestDoc {

    /** absolute path to test folder to load test resources */
    protected final static String TEST_FOLDER = getProjectFolderPath() + "/doc/";

    private static String getProjectFolderPath() {
        try {
            return new File(".").getCanonicalPath();
        } catch (IOException ioe) {
            throw new RuntimeException("unable to get project folder: ", ioe);
        }
    }

    @Test
    public void testDocMira() throws Exception {
        System.out.println("testDocMira");

        final String doc = "mira.html";
        final String html = FileUtils.readFile(new File(TEST_FOLDER, doc));

        ResizableTextViewFactory.createHtmlWindow(html, doc, true);
        System.exit(0);
    }
}
