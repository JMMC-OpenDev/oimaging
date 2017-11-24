/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.chart;

import fr.jmmc.jmal.util.MathUtils;
import fr.jmmc.jmcs.util.FileUtils;
import fr.jmmc.jmcs.util.StringUtils;
import fr.jmmc.oiexplorer.core.util.ColorUtils;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Very simple color palette
 * @author bourgesl
 */
public class ColorPalette {

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(ColorPalette.class.getName());

    /** default opacity = 80% */
    public final static float DEFAULT_OPACITY = 0.80f;

    /** current color palette name */
    private static String COLOR_PALETTE_NAME;
    /** default color palette */
    private static ColorPalette DEFAULT_COLOR_PALETTE;
    /** default color palette with the default opacity */
    private static ColorPalette DEFAULT_COLOR_PALETTE_ALPHA;
    /** Color palette names (sorted) */
    private final static Vector<String> colorPaletteNames = new Vector<String>(64);
    /** Color palettes keyed by names */
    private final static Map<String, ColorPalette> colorPalettes = new HashMap<String, ColorPalette>(64);
    /** Images of each Color palettes keyed by names */
    private final static Map<String, BufferedImage> colorPaletteImages = new HashMap<String, BufferedImage>(64);

    /** Default color palette (ColorPalette_fixed.pal) */
    public final static String DEFAULT_COLOR_PALETTE_NAME = "fixed";
    /**
     * Generated array of palette file names in the folder fr/jmmc/oiexplorer/core/resource/palette/
     */
    private final static String[] PAL_FILES = {
        "ColorPalette_Armytage.pal",
        "ColorPalette_Kelly.pal",
        "ColorPalette_gilles.pal",
        "ColorPalette_fixed.pal",
        "ColorPalette_initial.pal"
    };
    private final static String CLASS_PATH = "/fr/jmmc/oiexplorer/core/resource/palette/";

    static {
        COLOR_PALETTE_NAME = null;
        DEFAULT_COLOR_PALETTE = null;
        DEFAULT_COLOR_PALETTE_ALPHA = null;

        final long start = System.nanoTime();

        // Initialize color palettes at startup:
        String path = null;
        try {

            // color models from lut files :
            for (String name : PAL_FILES) {
                path = CLASS_PATH + name;
                final ColorPalette palette = load(path);
                if (palette != null) {
                    addColorPalette(name.substring(name.indexOf('_') + 1, name.indexOf('.')), palette);
                }
            }
        } catch (IOException ioe) {
            throw new IllegalStateException("Unable to load resource: " + path, ioe);
        }

        Collections.sort(colorPaletteNames);

        if (logger.isInfoEnabled()) {
            logger.info("ColorModels [{} available] : duration = {} ms.", colorPaletteNames.size(), 1e-6d * (System.nanoTime() - start));
        }

        // Initialize default color palette:
        setColorPalettes(DEFAULT_COLOR_PALETTE_NAME);

        if (DEFAULT_COLOR_PALETTE == null) {
            throw new IllegalStateException("Unable to get the default color palette: " + DEFAULT_COLOR_PALETTE_NAME);
        }
    }

    public static void setColorPalettes(final String nameOrFilePath) {
        String name = nameOrFilePath;
        ColorPalette palette = ColorPalette.getExistingColorPalette(name);

        if (palette == null) {
            // try loading a file:
            final File palFile = new File(name);
            try {
                palette = load(palFile);
                if (palette != null) {
                    // Fix name:
                    name = "User: " + palFile.getName();
                    addColorPalette(name, palette);
                }
            } catch (IOException ioe) {
                logger.warn("Unable to load file: {}", palFile, ioe);
            }
        }
        if (palette != null) {
            setColorPalettes(name, palette);
        }
    }

    private static synchronized void setColorPalettes(final String name, final ColorPalette palette) {
        if (palette != DEFAULT_COLOR_PALETTE) {
            logger.info("Set default Color palette to '{}'", name);
            COLOR_PALETTE_NAME = name;
            DEFAULT_COLOR_PALETTE = palette;
            DEFAULT_COLOR_PALETTE_ALPHA = new ColorPalette(palette.getColors(), 0.80f);
        }
    }

    private static void addColorPalette(final String name, final ColorPalette palette) {
        colorPaletteNames.add(name);
        colorPalettes.put(name, palette);
        colorPaletteImages.put(name, palette.createImage());
    }

    /**
     * Return the color palette name
     * @return color palette name
     */
    public static String getColorPaletteName() {
        return COLOR_PALETTE_NAME;
    }

    /**
     * Return the color palette
     * @return color palette
     */
    public static ColorPalette getColorPalette() {
        return DEFAULT_COLOR_PALETTE;
    }

    /**
     * Return the color palette with the default opacity (typically 80%)
     * @return color palette with the default opacity (typically 80%)
     */
    public static ColorPalette getColorPaletteAlpha() {
        return DEFAULT_COLOR_PALETTE_ALPHA;
    }

    /**
     * Return the Color model names (sorted)
     * @return Color model names (sorted)
     */
    public static Vector<String> getColorModelNames() {
        return colorPaletteNames;
    }

    /**
     * Return the ColorPalette given its name
     * @param name
     * @return ColorPalette or the default ColorPalette if the name was not found
     */
    public static ColorPalette getColorPalette(final String name) {
        ColorPalette palette = getExistingColorPalette(name);
        if (palette == null) {
            return getColorPalette();
        }
        return palette;
    }

    /**
     * Return the ColorPalette given its name
     * @param name
     * @return ColorPalette or null if the name was not found
     */
    public static ColorPalette getExistingColorPalette(final String name) {
        return colorPalettes.get(name);
    }

    /**
     * Return the image of the ColorPalette given its name
     * @param name
     * @return BufferedImage or null if the name was not found
     */
    public static BufferedImage getColorPaletteImage(final String name) {
        return colorPaletteImages.get(name);
    }

    public static ColorPalette load(final File inputFile) throws IOException {
        return load(new FileInputStream(inputFile));
    }

    public static ColorPalette load(final String name) throws IOException {
        return load(ColorPalette.class.getResourceAsStream(name));
    }

    private static ColorPalette load(final InputStream inputStream) throws IOException {
        final String lines = FileUtils.readStream(inputStream);

        logger.debug("load:\n{}", lines);

        // TODO: handle R G B (triplets)
        final List<Color> colors = new ArrayList<Color>(20);

        for (String line : lines.split("\n")) {
            if (!StringUtils.isEmpty(line)) {
                line = line.trim();

                if (line.length() != 0) {
                    final char first = line.charAt(0);
                    if (line.charAt(0) == '#') {
                        try {
                            final int rgb = Integer.parseInt(line.substring(1), 16); // HEX

                            colors.add(new Color(rgb));
                        } catch (NumberFormatException nfe) {
                            logger.info("invalid rgb: {}", line);
                        }
                    } else if (Character.isDigit(first)) {
                        // R G B triplets:
                        line = StringUtils.replaceNonNumericChars(line, StringUtils.STRING_SPACE);

                        final StringTokenizer tok = new StringTokenizer(line, StringUtils.STRING_SPACE);

                        if (tok.countTokens() == 3) {
                            try {
                                // decimal values:
                                final int r = Integer.parseInt(tok.nextToken());
                                final int g = Integer.parseInt(tok.nextToken());
                                final int b = Integer.parseInt(tok.nextToken());

                                colors.add(new Color(r, g, b));
                            } catch (NumberFormatException nfe) {
                                logger.info("invalid triplet (R G B): {}", line);
                            }
                        }
                    } else {
                        logger.debug("skip : {}", line);
                    }
                }
            }
        }

        return new ColorPalette(colors.toArray(new Color[colors.size()]));
    }

    public static void write(final ColorPalette palette, final File outputFile) throws IOException {
        write(palette, outputFile, true);
    }

    public static void write(final ColorPalette palette, final File outputFile, final boolean hex) throws IOException {
        final StringBuilder sb = new StringBuilder(1024);

        for (Color color : palette.getColors()) {
            if (hex) {
                toHexString(color, sb);
            } else {
                toRGBString(color, sb);
            }
            sb.append('\n');
        }

        final String lines = sb.toString();

        logger.debug("write:\n{}", lines);

        FileUtils.writeFile(outputFile, lines);
    }

    private static StringBuilder toHexString(final Color color, final StringBuilder sb) {
        sb.append('#');

        final int rgb = color.getRGB() & 0x00FFFFFF; // no alpha

        final String hex = Integer.toHexString(rgb).toUpperCase();

        final int n = 6 - hex.length();

        for (int i = 0; i < n; i++) {
            sb.append('0');
        }

        return sb.append(hex);
    }

    private static StringBuilder toRGBString(final Color color, final StringBuilder sb) {
        return sb.append(color.getRed()).append(' ').append(color.getGreen()).append(' ').append(color.getBlue());
    }

    /* members */
    /** palette colors */
    private final Color[] colors;

    /**
     * Constructor
     * @param colors colors to use
     */
    public ColorPalette(final Color[] colors) {
        this.colors = colors;
    }

    /**
     * Constructor
     * @param colors colors to use
     * @param alpha transparency between 0. and 1.
     */
    public ColorPalette(final Color[] colors, final float alpha) {
        final int len = colors.length;
        final Color[] alphaColors = new Color[len];

        final int alphaMask = Math.round(255 * alpha) << 24;

        for (int i = 0; i < len; i++) {
            alphaColors[i] = new Color(colors[i].getRGB() & 0x00ffffff | alphaMask, true);
        }

        this.colors = alphaColors;
    }

    /**
     * Return a Color for the given index (cyclic)
     * @param index color index [0..N]
     * @return Color color at the given index (modulo colors.length)
     */
    public Color getColor(final int index) {
        return this.colors[index % this.colors.length];
    }

    public Color[] getColors() {
        return this.colors;
    }

    public BufferedImage createImage() {
        final int maxWidth = 256;
        final int w = Math.round(maxWidth / colors.length);
        return createImage(maxWidth, w, 16, false);
    }

    public BufferedImage createImage(final int maxWidth,
                                     final int w, final int h,
                                     final boolean drawLabel) {

        final int len = colors.length;

        if (len == 0) {
            return null;
        }

        final int nRows = (int) Math.ceil(((float) (len * w)) / maxWidth);

        final int width = (nRows > 1) ? maxWidth : (len * w);
        final int height = nRows * h;

        final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        final Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        try {
            final int tx = 2;
            final int ty = (h / 2) + 4;

            final StringBuilder sb = (drawLabel) ? new StringBuilder(8) : null;
            final float[] c4 = (drawLabel) ? new float[4] : null;

            int x = 0, x0 = 0;
            int y = h, y0 = 0;

            for (int n = 0; n < len; n++) {
                final Color color = colors[n];

                // paint color:
                g2d.setColor(color);

                x += w;
                g2d.fillRect(x0, y0, x, y);

                // draw label:
                if (drawLabel) {
                    g2d.setColor((lum(color.getRGB(), c4) > 0.5f) ? Color.BLACK : Color.WHITE);

                    sb.setLength(0);
                    final String label = ColorPalette.toHexString(color, sb).toString();

                    g2d.drawString(label, x0 + tx, y0 + ty);
                }

                x0 = x;
                if (x0 >= maxWidth) {
                    y0 = y;
                    y = y0 + h;
                    x = x0 = 0;
                }
            }

            if (x0 != maxWidth) {
                g2d.setColor(Color.BLACK);
                g2d.fillRect(x0, y0, maxWidth, y);
            }
        } finally {
            g2d.dispose();
        }
        return image;
    }

    private static float lum(final int rgb, final float[] c4) {
        // convert sRGB to Lab (float)
        ColorUtils.sRGB_to_Lab(rgb, c4);

        // Convert L to Y (luminance in [0..1])
        return ColorUtils.L_to_Y(c4[0]);
    }

    public ColorPalette sortByDeltaE() {
        return sortByDistance(0.5, 0.5, 1.0);
    }

    public ColorPalette sortByLuminance() {
        return sortByDistance(1.0, 0.0, 0.0);
    }

    public ColorPalette sortByChroma() {
        return sortByDistance(0.0, 1.0, 0.0);
    }

    public ColorPalette sortByHue() {
        return sortByDistance(0.0, 0.0, 1.0);
    }

    public ColorPalette sortByDistance(final double wL, final double wC, final double wH) {
        final int len = colors.length;
        final List<Color> sortedColors = new ArrayList<Color>(len);

        final List<Color> cols = new ArrayList<Color>(Arrays.asList(colors));

        final ColorDistances colDist = new ColorDistances(wL, wC, wH);

        // Find minimum:
        double min = Double.POSITIVE_INFINITY;
        Color c1 = null;
        Color c2 = null;

        for (int i = 0, size = cols.size(); i < size; i++) {
            final Color color = cols.get(i);

            colDist.computeDistances(color, cols);

            if (colDist.getDistMin() < min) {
                min = colDist.getDistMin();
                if ((colDist.getDiffL() >= 0.0)) {
                    c1 = colDist.getColorMin(); // darkest
                    c2 = colDist.getColor(); // lightest
                } else {
                    c1 = colDist.getColor(); // darkest
                    c2 = colDist.getColorMin(); // lightest
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("global min: {} from {} to {}", min, c1, c2);
        }

        Color ref1 = c1; // darkest before
        sortedColors.add(ref1);
        cols.remove(ref1);

        Color ref2 = c2; // lightest after
        sortedColors.add(ref2);
        cols.remove(ref2);

        while (!cols.isEmpty()) {

            // Find minimum:
            // Follow chain from ref1:
            colDist.computeDistances(ref1, cols);

            min = colDist.getDistMin();
            c1 = colDist.getColorMin();
            c2 = ref1;

            // Follow chain from ref2:
            colDist.computeDistances(ref2, cols);

            if (colDist.getDistMin() < min) {
                min = colDist.getDistMin();
                c1 = colDist.getColorMin();
                c2 = ref2;
            }

            // c1 is new closest match
            // c2 is ref
            if (logger.isDebugEnabled()) {
                logger.debug("global min: {} from {} to {}", min, c2, c1);
            }

            if (c2 == ref1) {
                // closest to before side:
                sortedColors.add(0, c1);
                cols.remove(c1);
                ref1 = c1;
            } else {
                // closest to after side:
                sortedColors.add(c1);
                cols.remove(c1);
                ref2 = c1;
            }
        }

        return new ColorPalette(sortedColors.toArray(new Color[len]));
    }

    /*
    function sortColors(colors) {
        // Calculate distance between each color
        var distances = [];
        for (var i = 0; i < colors.length; i++) {
            distances[i] = [];
            for (var j = 0; j < i; j++)
                distances.push([colors[i], colors[j], colorDistance(colors[i], colors[j])]);
        }
        distances.sort(function(a, b) {
            return a[2] - b[2];
        });

        // Put each color into separate cluster initially
        var colorToCluster = {};
        for (var i = 0; i < colors.length; i++)
            colorToCluster[colors[i]] = [colors[i]];

        // Merge clusters, starting with lowest distances
        var lastCluster;
        for (var i = 0; i < distances.length; i++) {
            var color1 = distances[i][0];
            var color2 = distances[i][1];
            var cluster1 = colorToCluster[color1];
            var cluster2 = colorToCluster[color2];
            if (!cluster1 || !cluster2 || cluster1 == cluster2)
                continue;

            // Make sure color1 is at the end of its cluster and
            // color2 at the beginning.
            if (color1 != cluster1[cluster1.length - 1])
                cluster1.reverse();
            if (color2 != cluster2[0])
                cluster2.reverse();

            // Merge cluster2 into cluster1
            cluster1.push.apply(cluster1, cluster2);
            delete colorToCluster[color1];
            delete colorToCluster[color2];
            colorToCluster[cluster1[0]] = cluster1;
            colorToCluster[cluster1[cluster1.length - 1]] = cluster1;
            lastCluster = cluster1;
        }

        // By now all colors should be in one cluster
        return lastCluster;
    }
     */
    public static final class ColorDistances {

        // members:
        private Color color;
        private Color colorMin;
        private double distMin;
        private double distMean;
        private double diffL;
        // temporary:
        private final float[] c4_1 = new float[4];
        private final float[] c4_2 = new float[4];

        private final double wL;
        private final double wC;
        private final double wH;

        private double dL;
        private double dC;
        private double dH;

        ColorDistances(final double wL, final double wC, final double wH) {
            this.wL = wL;
            this.wC = wC;
            this.wH = wH;
        }

        void computeDistances(final Color color, final List<Color> colors) {
            this.color = color;

            // convert sRGB (float)
            ColorUtils.sRGB_to_Lab(color.getRGB(), c4_1);

            // Compute distances:
            final int len = colors.size();
            double min = Double.POSITIVE_INFINITY;
            double total = 0.0;
            Color cmin = null;
            double deltaL = 0.0;

            for (int i = 0; i < len; i++) {
                final Color other = colors.get(i);

                final double dist = (color != other) ? distanceLab(other) : Double.POSITIVE_INFINITY;

                if (dist != Double.POSITIVE_INFINITY) {
                    if (dist < min) {
                        min = dist;
                        cmin = other;
                        deltaL = dL; // luminosity
                    }
                    total += dist;
                }
            }
            this.distMean = total / len;
            this.colorMin = cmin;
            this.distMin = min;
            this.diffL = deltaL;

            if (logger.isDebugEnabled()) {
                logger.debug("Color: {} - distance mean = {} min = {} to {} dL = {}", color, distMean, distMin, colorMin, diffL);
            }
        }

        private double distanceLab(final Color other) {
            // convert sRGB (float)
            ColorUtils.sRGB_to_Lab(other.getRGB(), c4_2);

            // Luminance:
            dL = c4_1[0] - c4_2[0];
            // C:
            final double C1 = MathUtils.carthesianNorm(c4_1[1], c4_1[2]);
            final double C2 = MathUtils.carthesianNorm(c4_2[1], c4_2[2]);
            dC = (C1 - C2) / (1.0 + 0.045 * C1);
            // H:
            final double dA = c4_1[1] - c4_2[1];
            final double dB = c4_1[2] - c4_2[2];
            dH = Math.sqrt(dA * dA + dB * dB - dC * dC) / (1.0 + 0.015 * C2);

            return wL * dL * dL + wC * dC * dC + wH * dH * dH;
        }

        public Color getColor() {
            return color;
        }

        public double getDistMin() {
            return distMin;
        }

        public double getDistMean() {
            return distMean;
        }

        public Color getColorMin() {
            return colorMin;
        }

        public double getDiffL() {
            return diffL;
        }

    }

}
