/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core;

import fr.jmmc.oiexplorer.core.gui.chart.ColorPalette;
import static fr.jmmc.oiexplorer.core.gui.chart.ColorPalette.SORT_DIVERGING;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;
import org.jfree.chart.ChartColor;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bourgesl
 */
public class ColorPaletteTest {

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(ColorPaletteTest.class.getName());

    private final static Color[] INITIAL_COLORS = {
        ChartColor.LIGHT_RED,
        ChartColor.LIGHT_BLUE,
        ChartColor.LIGHT_GREEN,
        ChartColor.LIGHT_YELLOW,
        ChartColor.LIGHT_MAGENTA,
        ChartColor.LIGHT_CYAN,
        ChartColor.VERY_LIGHT_RED,
        ChartColor.VERY_LIGHT_BLUE,
        ChartColor.VERY_LIGHT_GREEN,
        ChartColor.VERY_LIGHT_YELLOW,
        ChartColor.VERY_LIGHT_MAGENTA,
        ChartColor.VERY_LIGHT_CYAN,
        ChartColor.DARK_RED,
        ChartColor.DARK_BLUE,
        ChartColor.DARK_GREEN,
        ChartColor.DARK_YELLOW,
        ChartColor.DARK_MAGENTA,
        ChartColor.DARK_CYAN,
        ChartColor.VERY_DARK_RED,
        ChartColor.VERY_DARK_BLUE,
        ChartColor.VERY_DARK_GREEN,
        ChartColor.VERY_DARK_YELLOW,
        ChartColor.VERY_DARK_MAGENTA,
        ChartColor.VERY_DARK_CYAN
    };

    private final static Color[] ADJUST_COLORS = {
        ChartColor.LIGHT_RED,
        ChartColor.LIGHT_BLUE,
        ChartColor.LIGHT_GREEN,
        ChartColor.LIGHT_YELLOW,
        ChartColor.LIGHT_MAGENTA,
        ChartColor.LIGHT_CYAN,
        new Color(254, 137, 0), /* ORANGE */
        ChartColor.VERY_LIGHT_RED,
        ChartColor.VERY_LIGHT_BLUE,
        ChartColor.DARK_GREEN,
        Color.DARK_GRAY,
        ChartColor.VERY_LIGHT_MAGENTA,
        ChartColor.DARK_CYAN,
        new Color(119, 77, 0), /* DARK BROWN */
        ChartColor.DARK_MAGENTA
    };

    // Color brewer:
    private final static Color[] COLOR_BREWER_COLORS = {
        new Color(211, 73, 49),
        new Color(94, 108, 192),
        new Color(94, 194, 144),
        new Color(155, 72, 198),
        new Color(117, 193, 71),
        new Color(225, 68, 121),
        new Color(82, 118, 52),
        new Color(218, 150, 49),
        new Color(91, 175, 214),
        new Color(212, 133, 204),
        new Color(186, 180, 81),
        new Color(138, 60, 126),
        new Color(139, 89, 37),
        new Color(158, 63, 73),
        new Color(222, 143, 112)
    };

    private final static Color[] COLOR_BREWER_COLORS_2 = {
        new Color(228, 26, 28),
        new Color(55, 126, 184),
        new Color(77, 175, 74),
        new Color(152, 78, 163),
        new Color(255, 127, 0),
        new Color(255, 255, 51),
        new Color(166, 86, 40),
        new Color(247, 129, 191),
        // ColorBrewer 8-class Accent
        new Color(127, 201, 127),
        new Color(190, 174, 212),
        new Color(253, 192, 134),
        new Color(255, 255, 153),
        new Color(56, 108, 176),
        new Color(240, 2, 127),
        new Color(191, 91, 23),
        new Color(102, 102, 102)
    };

    private final static Color[] WANT_HUE_COLORS = {
        new Color(217, 79, 52),
        new Color(104, 133, 208),
        new Color(198, 206, 123),
        new Color(116, 72, 206),
        new Color(121, 221, 83),
        new Color(205, 74, 193),
        new Color(209, 209, 61),
        new Color(106, 59, 128),
        new Color(94, 159, 65),
        new Color(206, 72, 116),
        new Color(103, 208, 153),
        new Color(83, 201, 207),
        new Color(143, 66, 55),
        new Color(202, 140, 69),
        new Color(210, 137, 198),
        new Color(85, 108, 45)
    };

    private final static Color[] WANT_HUE_COLORS_2 = {
        new Color(211, 73, 49),
        new Color(94, 108, 192),
        new Color(94, 194, 144),
        new Color(155, 72, 198),
        new Color(117, 193, 71),
        new Color(225, 68, 121),
        new Color(82, 118, 52),
        new Color(218, 150, 49),
        new Color(91, 175, 214),
        new Color(212, 133, 204),
        new Color(186, 180, 81),
        new Color(138, 60, 126),
        new Color(139, 89, 37),
        new Color(158, 63, 73),
        new Color(222, 143, 112)
    };

    private final static Color[] DEFAULT_COLORS_64 = {
        new Color(0, 0, 0),
        new Color(1, 0, 103),
        new Color(213, 255, 0),
        new Color(255, 0, 86),
        new Color(158, 0, 142),
        new Color(14, 76, 161),
        new Color(255, 229, 2),
        new Color(0, 95, 57),
        new Color(0, 255, 0),
        new Color(149, 0, 58),
        new Color(255, 147, 126),
        new Color(164, 36, 0),
        new Color(0, 21, 68),
        new Color(145, 208, 203),
        new Color(98, 14, 0),
        new Color(107, 104, 130),
        new Color(0, 0, 255),
        new Color(0, 125, 181),
        new Color(106, 130, 108),
        new Color(0, 174, 126),
        new Color(194, 140, 159),
        new Color(190, 153, 112),
        new Color(0, 143, 156),
        new Color(95, 173, 78),
        new Color(255, 0, 0),
        new Color(255, 0, 246),
        new Color(255, 2, 157),
        new Color(104, 61, 59),
        new Color(255, 116, 163),
        new Color(150, 138, 232),
        new Color(152, 255, 82),
        new Color(167, 87, 64),
        new Color(1, 255, 254),
        new Color(255, 238, 232),
        new Color(254, 137, 0),
        new Color(189, 198, 255),
        new Color(1, 208, 255),
        new Color(187, 136, 0),
        new Color(117, 68, 177),
        new Color(165, 255, 210),
        new Color(255, 166, 254),
        new Color(119, 77, 0),
        new Color(122, 71, 130),
        new Color(38, 52, 0),
        new Color(0, 71, 84),
        new Color(67, 0, 44),
        new Color(181, 0, 255),
        new Color(255, 177, 103),
        new Color(255, 219, 102),
        new Color(144, 251, 146),
        new Color(126, 45, 210),
        new Color(189, 211, 147),
        new Color(229, 111, 254),
        new Color(222, 255, 116),
        new Color(0, 255, 120),
        new Color(0, 155, 255),
        new Color(0, 100, 1),
        new Color(0, 118, 255),
        new Color(133, 169, 0),
        new Color(0, 185, 23),
        new Color(120, 130, 49),
        new Color(0, 255, 198),
        new Color(255, 110, 65),
        new Color(232, 94, 190)
    };

    @Test
    public void test() {
        try {
            ColorPalette.checkLabRange();

            savePalette("64", DEFAULT_COLORS_64);

            savePalette("initial", INITIAL_COLORS);
            savePalette("adjust", ADJUST_COLORS);

            savePalette("ColorBrewer", COLOR_BREWER_COLORS);
            savePalette("ColorBrewer2", COLOR_BREWER_COLORS_2);

            savePalette("WantHue", WANT_HUE_COLORS);
            savePalette("WantHue2", WANT_HUE_COLORS_2);

            savePalette("64", DEFAULT_COLORS_64);

            ColorPalette otherPalette = ColorPalette.load("/fr/jmmc/oiexplorer/core/resource/palette/ColorPalette_initial.pal");
            savePalette("ColorPalette_initial", otherPalette.getColors());

            otherPalette = ColorPalette.load("/fr/jmmc/oiexplorer/core/resource/palette/ColorPalette_fixed.pal");
            savePalette("ColorPalette_fixed", otherPalette.getColors());

            otherPalette = ColorPalette.load("/fr/jmmc/oiexplorer/core/resource/palette/ColorPalette_Armytage.pal");
            savePalette("ColorPalette_Armytage", otherPalette.getColors());

            otherPalette = ColorPalette.load("/fr/jmmc/oiexplorer/core/resource/palette/ColorPalette_Kelly.pal");
            savePalette("ColorPalette_Kelly", otherPalette.getColors());

            otherPalette = ColorPalette.load("/fr/jmmc/oiexplorer/core/resource/palette/ColorPalette_gilles.pal");
            savePalette("ColorPalette_gilles", otherPalette.getColors());

        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private static void savePalette(final String name, final Color[] colors) throws IOException {
        if (colors.length == 0) {
            return;
        }

        int w = 64;
        int maxWidth = 8 * w;
        int h = 24;

        final ColorPalette palette = new ColorPalette(colors);

        // 64px to fit labels:
        BufferedImage image = palette.createImage(maxWidth, w, h, true);
        saveImage(name + "-label", image);

        final String suffix = SORT_DIVERGING ? "dvg" : "sort";

        System.out.println("Mode: " + suffix);

        ColorPalette sortedPalette;

        if (true) {
            sortedPalette = palette.sortByLuminance();
            image = sortedPalette.createImage(maxWidth, w, h, true, 0);
            saveImage(name + "-" + suffix + "L", image);
        }
        if (false) {
            sortedPalette = palette.sortByChroma();
            image = sortedPalette.createImage(maxWidth, w, h, true, 1);
            saveImage(name + "-" + suffix + "C", image);
        }
        if (false) {
            sortedPalette = palette.sortByHue();
            image = sortedPalette.createImage(maxWidth, w, h, true, 2);
            saveImage(name + "-" + suffix + "H", image);
        }
        if (true) {
            sortedPalette = palette.sortByDeltaE();
            image = sortedPalette.createImage(maxWidth, w, h, true);
            saveImage(name + "-" + suffix + "DeltaE", image);
        }
        if (false) {
            sortedPalette = palette.sortByBest();
            image = sortedPalette.createImage(maxWidth, w, h, true);
            saveImage(name + "-" + suffix + "Best", image);
        }

        image = palette.createImage();
        saveImage(name + "-raw", image);

        image = sortedPalette.createImage();
        saveImage(name + "-raw-" + suffix + "DeltaE", image);

        // Write palette file:        
        File palFile = new File("./target/ColorPalette_" + name + "-" + suffix + ".pal");
        ColorPalette.write(sortedPalette, palFile);

        // Write palette file:        
        palFile = new File("./target/ColorPalette_" + name + "-hex.pal");
        ColorPalette.write(palette, palFile);

        // Reload palette:
        ColorPalette otherPalette = ColorPalette.load(palFile);

        if (!Arrays.equals(palette.getColors(), otherPalette.getColors())) {
            logger.error("original: {}", palette.getColors());
            logger.error("loaded: {}", otherPalette.getColors());
            throw new RuntimeException("Mismatch arrays !");
        }

        palFile = new File("./target/ColorPalette_" + name + "-rgb.pal");
        ColorPalette.write(palette, palFile, false);

        // Reload palette:
        otherPalette = ColorPalette.load(palFile);

        if (!Arrays.equals(palette.getColors(), otherPalette.getColors())) {
            logger.error("original: {}", palette.getColors());
            logger.error("loaded: {}", otherPalette.getColors());
            throw new RuntimeException("Mismatch arrays !");
        }
    }

    private static void saveImage(final String name, final BufferedImage image) {
        if (image != null) {
            try {
                final File file = new File("./target/ColorModelsTest-" + name + ".png");

                System.out.println("Writing file: " + file.getAbsolutePath());;
                ImageIO.write(image, "PNG", file);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }
}
