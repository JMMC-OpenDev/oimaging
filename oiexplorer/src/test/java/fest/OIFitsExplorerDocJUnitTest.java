/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fest;

import com.mortennobel.imagescaling.AdvancedResizeOp;
import com.mortennobel.imagescaling.ResampleOp;
import fest.common.JmcsFestSwingJUnitTestCase;
import fr.jmmc.jmcs.Bootstrapper;
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
import org.junit.Test;

/**
 * This simple tests takes screenshots to complete the OIFitsExplorer documentation
 * 
 * @author bourgesl
 */
public final class OIFitsExplorerDocJUnitTest extends JmcsFestSwingJUnitTestCase {

    /**
     * Initialize system properties & static variables and finally starts the application
     */
    @BeforeClass
    public static void intializeAndStartApplication() {

        // invoke Bootstrapper method to initialize logback now:
        Bootstrapper.getState();

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
    public void shouldShowPlot() {

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
     * Test Preferences
     */
    /*  
     @Test
     @GUITest
     public void shouldOpenPreferences() {
     window.menuItemWithPath("Edit", "Preferences...").click();

     final Frame prefFrame = robot().finder().find(FrameMatcher.withTitle("Preferences"));

     if (prefFrame != null) {
     final FrameFixture frame = new FrameFixture(robot(), prefFrame);

     frame.requireVisible();
     frame.moveToFront();

     saveScreenshot(frame, "OIFitsExplorer-prefs.png");

     // close frame :
     frame.close();
     }
     }
     */
    /**
     * Test Interop menu : Start SearchCal and LITpro manually before this test
     */
    /*    
     @Test
     @GUITest
     public void showInteropMenu() {
     window.menuItemWithPath("Interop").click();
     captureMainForm("OIFitsExplorer-interop-menu.png");

     window.menuItemWithPath("Interop", "Show Hub Status").click();

     final Frame hubFrame = robot().finder().find(FrameMatcher.withTitle("SAMP Status"));

     if (hubFrame != null) {
     final FrameFixture frame = new FrameFixture(robot(), hubFrame);

     frame.requireVisible();
     frame.moveToFront();

     frame.list(new GenericTypeMatcher<JList>(JList.class) {
     @Override
     protected boolean isMatching(JList component) {
     return "org.astrogrid.samp.gui.ClientListCellRenderer".equals(component.getCellRenderer().getClass().getName());
     }
     }).selectItem("OIFitsExplorer");

     saveScreenshot(frame, "OIFitsExplorer-interop-hubStatus.png");

     // close frame :
     frame.close();
     }
     }
     */
    /**
     * Test Feedback report
     */
    @Test
    @GUITest
    public void shouldOpenFeedbackReport() {

        // hack to solve focus trouble in menu items :
        window.menuItemWithPath("Help").focus();

        window.menuItemWithPath("Help", "Report Feedback to JMMC...").click();

        final DialogFixture dialog = window.dialog(withTitle("Feedback Report ").andShowing());

        dialog.requireVisible();
        dialog.moveToFront();

        final String myEmail = "laurent.bourges@obs.ujf-grenoble.fr";

        final JTextComponentFixture emailField = dialog.textBox(JTextComponentMatcher.withText(myEmail));

        // hide my email address :
        emailField.setText("type your email address here");

        saveScreenshot(dialog, "OIFitsExplorer-FeebackReport.png");

        // restore my preferences :
        emailField.setText(myEmail);

        // close dialog :
        dialog.close();
    }

    /**
     * Test the application exit sequence : ALWAYS THE LAST TEST
     */
    @Test
    @GUITest
    public void shouldExit() {
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
