/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fest;

import com.mortennobel.imagescaling.AdvancedResizeOp;
import com.mortennobel.imagescaling.ResampleOp;
import fest.common.JmcsFestSwingJUnitTestCase;
import fr.jmmc.jmcs.App;
import fr.jmmc.jmcs.Bootstrapper;
import fr.jmmc.jmcs.data.preference.CommonPreferences;
import fr.jmmc.jmcs.data.preference.PreferencesException;
import fr.jmmc.jmcs.data.preference.SessionSettingsPreferences;
import fr.jmmc.oimaging.Preferences;
import fr.jmmc.oimaging.gui.ViewerPanel;
import fr.jmmc.oimaging.services.ServiceList;
import java.awt.Dimension;
import static java.awt.event.KeyEvent.*;
import java.awt.image.BufferedImage;
import java.io.File;
import org.fest.swing.annotation.GUITest;
import static org.fest.swing.core.KeyPressInfo.*;
import static org.fest.swing.core.matcher.DialogMatcher.*;
import org.fest.swing.core.matcher.JTextComponentMatcher;
import org.fest.swing.fixture.DialogFixture;
import org.fest.swing.fixture.JTextComponentFixture;
import org.fest.swing.util.Platform;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * This simple tests takes screenshots to complete the OImaging documentation
 * 
 * @author bourgesl
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class OImagingDocJUnitTest extends JmcsFestSwingJUnitTestCase {

    /** absolute path to sample folder to load test resources */
    private final static String SAMPLE_FOLDER = getProjectFolderPath() + "samples/";
    private final static String INPUT_FOLDER = SAMPLE_FOLDER + "input/";

    private final static String FAKE_EMAIL = "FAKE_EMAIL";
    private static String CURRENT_EMAIL = "";

    private static void defineEmailPref(final String email) {
        try {
            final CommonPreferences prefs = CommonPreferences.getInstance();

            CURRENT_EMAIL = prefs.getPreference(CommonPreferences.FEEDBACK_REPORT_USER_EMAIL);

            prefs.setPreference(CommonPreferences.FEEDBACK_REPORT_USER_EMAIL, email);
        } catch (PreferencesException pe) {
            logger.error("setPreference failed", pe);
        }
    }

    /**
     * Initialize system properties & static variables and finally starts the application
     */
    @BeforeClass
    public static void intializeAndStartApplication() {
        // Hack to reset LAF & ui scale:
        CommonPreferences.getInstance().resetToDefaultPreferences();

        // invoke Bootstrapper method to initialize logback now:
        Bootstrapper.getState();

        // reset Preferences:
        Preferences.getInstance().resetToDefaultPreferences();
        SessionSettingsPreferences.getInstance().resetToDefaultPreferences();
        try {
            CommonPreferences.getInstance().setPreference(CommonPreferences.SHOW_STARTUP_SPLASHSCREEN, false);
        } catch (PreferencesException pe) {
            logger.error("setPreference failed", pe);
        }
        defineEmailPref(FAKE_EMAIL);

        // define robot delays :
        defineRobotDelayBetweenEvents(SHORT_DELAY);

        // define delay before taking screenshot :
        defineScreenshotDelay(SHORT_DELAY);

        // disable tooltips :
        enableTooltips(false);

        // Set special system properties:
        System.setProperty("oimaging.devMode", "false");

        // Start application:
        JmcsFestSwingJUnitTestCase.startApplication(
                fr.jmmc.oimaging.OImaging.class);
    }

    @Test
    @GUITest
    public void m001_shouldStart() {
        // increase window size at startup:
        final Dimension dim = new Dimension(1400, 1000);
        App.getFrame().setPreferredSize(dim);
    }

    @Test
    @GUITest
    public void m011_loadInputAndRunWisard() {
        loadInputAndRunAlgorithm(ServiceList.SERVICE_BSMEM, false);
    }

    @Test
    @GUITest
    public void m012_loadInputAndRunWisard() {
        loadInputAndRunAlgorithm(ServiceList.SERVICE_MIRA, false);
    }

    @Test
    @GUITest
    public void m013_loadInputAndRunWisard() {
        loadInputAndRunAlgorithm(ServiceList.SERVICE_SPARCO, false);
    }

    @Test
    @GUITest
    public void m014_loadInputAndRunWisard() {
        loadInputAndRunAlgorithm(ServiceList.SERVICE_WISARD, true);
    }

    private void loadInputAndRunAlgorithm(final String algorithm, final boolean small) {
        resetGui();
        loadInput("2004-FKV1137-L1L2-example.image-oi.fits");

        window.comboBox("jComboBoxSoftware").selectItem(algorithm);
        pauseShort();

        saveScreenshot(window, "OImaging-input-" + algorithm + ".png");

        window.button("jButtonRun").click();

        // waits for computation to finish :
        OImagingTestUtils.checkRunningTasks();

        pauseMedium();

        window.tabbedPane("jTabbedPaneVizualizations").selectTab(ViewerPanel.TAB_LABEL_IMAGES);
        saveScreenshot(window, "OImaging-output-" + algorithm + ".png");

        if (small) {
            // Capture initial screenshot:
            final BufferedImage image = takeScreenshotOf(window);

            // miniature for web page : 350px width :
            final int width = 350;
            final int height = Math.round(1f * width * image.getHeight() / image.getWidth());

            // use Lanczos3 resampler and soft unsharp mask :
            final ResampleOp resampleOp = new ResampleOp(width, height);
            resampleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Soft);

            final BufferedImage rescaledImage = resampleOp.filter(image, null);
            saveImage(rescaledImage, "OImaging-output-" + algorithm + "-small.png");
        }

        window.tabbedPane("jTabbedPaneVizualizations").selectTab(ViewerPanel.TAB_LABEL_OIFITS);
        saveScreenshot(window, "OImaging-output-" + algorithm + "-oifits.png");

        window.tabbedPane("jTabbedPaneVizualizations").selectTab(ViewerPanel.TAB_LABEL_PARAMS);
        saveScreenshot(window, "OImaging-output-" + algorithm + "-parameters.png");

        window.tabbedPane("jTabbedPaneVizualizations").selectTab(ViewerPanel.TAB_LABEL_EXECLOG);
        saveScreenshot(window, "OImaging-output-" + algorithm + "-log.png");
    }

    /**
     * Test Feedback report
     */
    @Test
    @GUITest
    public void m100_shouldOpenFeedbackReport() {

        // hack to solve focus trouble in menu items :
        window.menuItemWithPath("Help").focus();

        window.menuItemWithPath("Help", "Report Feedback to JMMC...").click();

        final DialogFixture dialog = window.dialog(withTitle("Feedback Report ").andShowing());

        dialog.requireVisible();
        dialog.moveToFront();

        final JTextComponentFixture emailField = dialog.textBox(JTextComponentMatcher.withText(FAKE_EMAIL));

        // hide my email address :
        emailField.setText("type your email address here");

        saveScreenshot(dialog, "OImaging-FeebackReport.png");

        // restore my preferences :
        emailField.setText(FAKE_EMAIL);

        // close dialog :
        dialog.close();

        // reset email preference:
        defineEmailPref(CURRENT_EMAIL);
    }

    /**
     * Test the application exit sequence : ALWAYS THE LAST TEST
     */
    @Test
    @GUITest
    public void m999_shouldExit() {
        logger.info("shouldExit test");

        window.close();

        confirmDialogDontSave();
    }

    /* 
     --- Utility methods  ---------------------------------------------------------
     */
    private void resetGui() {
        // hack to solve focus trouble in menu items :
        window.menuItemWithPath("File").focus();
        window.menuItemWithPath("File", "New OI Image file").click();

        confirmDialogDontSave();

        pauseShort();
    }

    private void loadInput(final String fileName) {
        // hack to solve focus trouble in menu items :
        window.menuItemWithPath("File").focus();
        window.menuItemWithPath("File", "Load OIFits file").click();

        confirmDialogDontSave();

        window.fileChooser().selectFile(new File(INPUT_FOLDER + fileName)).approve();

        pauseShort();
    }

    /**
     * Export the current plot to PDF
     */
    private void exportPDF() {
        // export PDF :
        window.pressAndReleaseKey(keyCode(VK_P).modifiers(Platform.controlOrCommandMask()));

        // use image folder to store PDF and validate :
        window.fileChooser().setCurrentDirectory(getScreenshotFolder()).approve();

        // overwrite any existing file :
        confirmDialogFileOverwrite();
    }
}
