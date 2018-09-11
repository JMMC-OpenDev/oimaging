/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core;

import fr.jmmc.jmal.image.ColorScale;
import fr.jmmc.oiexplorer.core.gui.FitsImagePanel;
import fr.jmmc.oiexplorer.core.util.FitsImageUtils;
import fr.jmmc.jmcs.Bootstrapper;
import fr.jmmc.jmcs.data.preference.PreferencesException;
import fr.jmmc.jmcs.gui.util.SwingUtils;
import fr.jmmc.oitools.image.FitsImage;
import fr.jmmc.oitools.image.FitsImageFile;
import fr.jmmc.oitools.image.FitsImageHDU;
import java.awt.Dimension;
import java.io.File;
import javax.swing.JFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class makes several image tests
 * @author bourgesl
 */
public final class ImageFitsTest {

    private static final boolean TEST_ANGLES = false;

    private static final double ROTATION_ANGLE = 20.0;

//    private static final String FOLDER = "/ASPRO2/fits/tests/";
    private static final String FOLDER = "/ASPRO2/fits/tmp/";

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(ImageFitsTest.class.getName());

    /**
     * Forbidden constructor
     */
    private ImageFitsTest() {
        super();
    }

    /**
     * Main tests
     * @param args unused arguments 
     */
    public static void main(String[] args) {

        try {
            TestPreferences.getInstance().setPreference(Preferences.MODEL_IMAGE_SCALE, ColorScale.LOGARITHMIC.toString());
        } catch (PreferencesException pe) {
            // noop;
        }

        Bootstrapper.getState();

        final String userHome = System.getProperty("user.home");

        if (true) {
            final File directory = new File(userHome + FOLDER);
            if (directory.exists() && directory.isDirectory()) {

                final long start = System.nanoTime();

                // load and prepare images:
                for (File f : directory.listFiles()) {
                    if (f.isFile() && !f.getName().startsWith("COPY_")
                            && (f.getName().endsWith("fits") || f.getName().endsWith("fits.gz"))) {

                        cleanup();

                        final String file = f.getAbsolutePath();
                        try {
                            logger.info("Loading file: " + file);

                            // load and prepare images:
                            final FitsImageFile imgFitsFile = FitsImageUtils.load(file, true);

                            for (FitsImageHDU fitsImageHDU : imgFitsFile.getFitsImageHDUs()) {
                                for (FitsImage fitsImage : fitsImageHDU.getFitsImages()) {
                                    logger.info("Loaded FitsImage: " + fitsImage.toString());

                                    if ("img_ellipse_5_ang_20.fits".equals(f.getName())) {
                                        showFitsPanel(fitsImage.getFitsImageIdentifier(), fitsImage);
                                    } else {
                                        if (TEST_ANGLES) {
                                            // Test rotation:
                                            for (int i = 3; i <= 180; i += 7) {
                                                final FitsImage copy = fitsImage.clone();
                                                copy.setRotAngle(i);
                                                showFitsPanel(copy.getFitsImageIdentifier() + " ang: " + i, copy);
                                            }
                                        } else {
                                            showFitsPanel(fitsImage.getFitsImageIdentifier(), fitsImage);

                                            final FitsImage copy = fitsImage.clone();
                                            copy.setRotAngle(ROTATION_ANGLE);
                                            showFitsPanel(fitsImage.getFitsImageIdentifier() + " ang: " + ROTATION_ANGLE, copy);
                                        }
                                    }
                                }
                            }

                        } catch (Exception e) {
                            logger.error("An exception occured while loading file: " + file, e);
                        }
                    }
                }

                logger.info("showDirectory: duration = {} ms.", 1e-6d * (System.nanoTime() - start));
            }
        }
    }

    /**
     * Show the given fits image
     * @param name name of the frame
     * @param fitsImage fits image structure
     */
    private static void showFitsPanel(final String name, final FitsImage fitsImage) {
        showFitsPanel(name, fitsImage, null);
    }

    /**
     * Show the given fits image
     * @param name name of the frame
     * @param fitsImage fits image structure
     * @param minDataRange optional minimal range for data
     */
    private static void showFitsPanel(final String name, final FitsImage fitsImage, final float[] minDataRange) {
        SwingUtils.invokeEDT(new Runnable() {
            @Override
            public void run() {
                final FitsImagePanel panel = new FitsImagePanel(TestPreferences.getInstance(), true, false, minDataRange);
                panel.setFitsImage(fitsImage);

                final JFrame frame = new JFrame(name);
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                frame.setMinimumSize(new Dimension(1200, 1200));

                frame.add(panel);
                frame.pack();
                frame.setVisible(true);
            }
        });
    }

    /**
     * Cleanup (GC + 100ms pause)
     */
    private static void cleanup() {
        // Perform GC:
        System.gc();

        // pause for 100 ms :
        try {
            Thread.sleep(100l);
        } catch (InterruptedException ex) {
            logger.info("thread interrupted", ex);
        }
    }

}
