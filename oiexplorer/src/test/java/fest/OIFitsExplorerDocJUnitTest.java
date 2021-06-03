/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fest;

import com.mortennobel.imagescaling.AdvancedResizeOp;
import com.mortennobel.imagescaling.ResampleOp;
import fest.common.JmcsFestSwingJUnitTestCase;
import fr.jmmc.jmcs.Bootstrapper;
import fr.jmmc.jmcs.data.preference.CommonPreferences;
import fr.jmmc.jmcs.data.preference.PreferencesException;
import static java.awt.event.KeyEvent.*;
import java.awt.image.BufferedImage;
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
 * This simple tests takes screenshots to complete the OIFitsExplorer documentation
 * 
 * @author bourgesl
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public final class OIFitsExplorerDocJUnitTest extends JmcsFestSwingJUnitTestCase {

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
//        Preferences.getInstance().resetToDefaultPreferences();
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

        // Start application:
        JmcsFestSwingJUnitTestCase.startApplication(
                fr.jmmc.oiexplorer.OIFitsExplorer.class,
                "-open", TEST_FOLDER + "mystery.oixp");
    }

    /**
     * Test plot (startup)
     */
    @Test
    @GUITest
    public void m01_shouldShowPlot() {

        // Capture UV Coverage plot :
        final BufferedImage image = GetScreenshot();

        saveImage(image, "OIFitsExplorer-screen.png");

        // TODO : refactor that code :
        // miniature for aspro web page : 350px width :
        final int width = 350;
        final int height = Math.round(1f * width * image.getHeight() / image.getWidth());

        // use Lanczos3 resampler and soft unsharp mask :
        final ResampleOp resampleOp = new ResampleOp(width, height);
        resampleOp.setUnsharpenMask(AdvancedResizeOp.UnsharpenMask.Soft);

        final BufferedImage rescaledImage = resampleOp.filter(image, null);

        saveImage(rescaledImage, "OIFitsExplorer-screen-small.png");
    }

    /**
     * Test Feedback report
     */
    @Test
    @GUITest
    public void m02_shouldOpenFeedbackReport() {

        // hack to solve focus trouble in menu items :
        window.menuItemWithPath("Help").focus();

        window.menuItemWithPath("Help", "Report Feedback to JMMC...").click();

        final DialogFixture dialog = window.dialog(withTitle("Feedback Report ").andShowing());

        dialog.requireVisible();
        dialog.moveToFront();

        final JTextComponentFixture emailField = dialog.textBox(JTextComponentMatcher.withText(FAKE_EMAIL));

        // hide my email address :
        emailField.setText("type your email address here");

        saveScreenshot(dialog, "Aspro2-FeebackReport.png");

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
    public void m03_shouldExit() {
        logger.info("shouldExit test");

        window.close();

        confirmDialogDontSave();
    }

    /* 
     --- Utility methods  ---------------------------------------------------------
     */
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

    /**
     * Return the window screenshot
     * @return screenshot image
     */
    private BufferedImage GetScreenshot() {
        final BufferedImage image = takeScreenshotOf(window);

        // export PDF :
        exportPDF();

        return image;
    }
}
