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

    private final static boolean USE_LCH = true;

    public final static boolean SORT_DIVERGING = false;

    public final static double REJECT_TOO_CLOSE = 0.001;

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
        "ColorPalette_initial.pal",
        "ColorPalette_64-sort.pal",
        "ColorPalette_64-dvg.pal"
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

        logger.debug("write [{}]:\n{}", outputFile, lines);

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

        final int alphaMask = Math.round(255f * alpha) << 24;

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
        return createImage(maxWidth, w, h, drawLabel, -1);
    }

    public BufferedImage createImage(final int maxWidth,
                                     final int w, final int h,
                                     final boolean drawLabel,
                                     final int comp) {

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
            final int hw = w / 2;
            final int tx = 2;
            final int ty = (h / 2) + 4;

            // TODO: simplify API to convert colors
            final ColorDistances colDist = (comp != -1) ? new ColorDistances(1.0, 1.0, 1.0) : null;

            final StringBuilder sb = (drawLabel) ? new StringBuilder(8) : null;

            int x = 0, x0 = 0;
            int y = h, y0 = 0;

            for (int n = 0; n < len; n++) {
                final Color color = colors[n];

                // paint color:
                g2d.setColor(color);

                x += w;

                g2d.fillRect(x0, y0, w, h);

                if (comp != -1) {
                    colDist.computeRef(color);
                    float compValue = colDist.getRef()[comp];
                    // normalize to [0..1]:
                    if (USE_LCH) {
                        // Fix angle:
                        if (comp == 2) {
                            compValue /= 360f;
                        }
                    } else {
                        // Fix A/B [-1..1]:
                        if (comp > 0) {
                            compValue /= 2f;
                        }
                    }
                    final int v = Math.round(255f * compValue);
                    // paint color:
                    g2d.setColor(new Color(v, v, v));
                    g2d.fillRect(x0 + hw, y0, hw, h);
                }

                // draw label:
                if (drawLabel) {
                    g2d.setColor((ColorUtils.luminance(color.getRGB()) > 128) ? Color.BLACK : Color.WHITE);

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

    public ColorPalette sortByLuminance() {
        return sortByDistance(1.0, 0.0, 0.0);
    }

    public ColorPalette sortByChroma() {
        return sortByDistance(0.0, 1.0, 0.0);
    }

    public ColorPalette sortByHue() {
        return sortByDistance(0.0, 0.0, 1.0);
    }

    public ColorPalette sortByDeltaE() {
        return sortByDistance(1.0, 1.0, 1.0);
    }

    public ColorPalette sortByBest() {
        return sortByDistance(1.0, 1.0, 50.0);
    }

    public static void checkLabRange() {
        /*
            Fast range:
                vals: [0, 16, 32, 48, 64, 80, 96, 112, 128, 144, 160, 176, 192, 208, 224, 240, 255]
        
        OkLAB:
            Fast range:
            L  range: [0.0, 0.99998826]
            A range:  [-0.2339203, 0.2762803]
            B range:  [-0.31161994, 0.19849062]
        
            Full range:
            L  range: [0.0, 0.99998826]
            C1 range: [-0.2339203, 0.2762803]
            C2 range: [-0.31161994, 0.19849062]        
        
        OkLCH:
            Fast range:
            L  range: [0.0, 0.99998826]
            C range:  [0.0, 0.3226086]
            H range:  [0.0, 359.9851]     
        
            Full range:
            L  range: [0.0, 0.99998826]
            C1 range: [0.0, 0.3226086]
            C2 range: [0.0, 359.99997]        
         */
        final int[] vals;
        if (false) {
            vals = new int[256];
            for (int i = 0; i < 256; i++) {
                vals[i] = i;
            }
        } else {
            vals = new int[1 + (256 / 16)];
            for (int i = 0, n = 0; i <= 256; i += 16) {
                vals[n++] = (i > 0xFF) ? 0xFF : i;
            }
        }
        System.out.println("vals: " + Arrays.toString(vals));

        ColorDistances colDist = new ColorDistances(1.0, 1.0, 1.0, false);

        final float[] compL = new float[2];
        final float[] comp1 = new float[2];
        final float[] comp2 = new float[2];

        compL[0] = Float.POSITIVE_INFINITY;
        compL[1] = Float.NEGATIVE_INFINITY;
        comp1[0] = Float.POSITIVE_INFINITY;
        comp1[1] = Float.NEGATIVE_INFINITY;
        comp2[0] = Float.POSITIVE_INFINITY;
        comp2[1] = Float.NEGATIVE_INFINITY;

        for (int ri = 0; ri < vals.length; ri++) {
            final int r = vals[ri];

            for (int gi = 0; gi < vals.length; gi++) {
                final int g = vals[gi];

                for (int bi = 0; bi < vals.length; bi++) {
                    final int b = vals[bi];

                    final Color color = new Color(r, g, b);
                    colDist.computeRef(color);

                    final float[] fvals = colDist.getRef();

                    float c = fvals[0];
                    if (c < compL[0]) {
                        compL[0] = c;
                    }
                    if (c > compL[1]) {
                        compL[1] = c;
                    }

                    c = fvals[1];
                    if (c < comp1[0]) {
                        comp1[0] = c;
                    }
                    if (c > comp1[1]) {
                        comp1[1] = c;
                    }

                    c = fvals[2];
                    if (c < comp2[0]) {
                        comp2[0] = c;
                    }
                    if (c > comp2[1]) {
                        comp2[1] = c;
                    }
                }
            }
        }

        System.out.println("L  range: " + Arrays.toString(compL));
        System.out.println("C1 range: " + Arrays.toString(comp1));
        System.out.println("C2 range: " + Arrays.toString(comp2));

        colDist = new ColorDistances(1.0, 1.0, 1.0, true); // normalize
        final float[] ref = colDist.getRef();
        final float[] other = colDist.getOther();

        // L:
        Arrays.fill(ref, 0f);
        Arrays.fill(other, 0f);
        ref[0] = 1f;
        System.out.println("L(1.0) distance range: " + colDist.testDistance()); // 0 - 1
        ref[0] = 0.5f;
        System.out.println("L(0.5) distance range: " + colDist.testDistance()); // 0 - 1
        // C:
        Arrays.fill(ref, 0f);
        Arrays.fill(other, 0f);
        ref[1] = 1f;
        System.out.println("C(1.0) distance range: " + colDist.testDistance()); // 0 - 1
        ref[1] = 0.5f;
        System.out.println("C(0.5) distance range: " + colDist.testDistance()); // 0 - 1

        // H:
        Arrays.fill(ref, 0f);
        Arrays.fill(other, 0f);
        ref[2] = 360f;
        System.out.println("H(360-0) distance range: " + colDist.testDistance()); // 0 - 360 deg

        Arrays.fill(ref, 0f);
        Arrays.fill(other, 0f);
        ref[2] = 180f;
        System.out.println("H(180-0) distance range: " + colDist.testDistance()); // 0 - 360 deg

        Arrays.fill(ref, 0f);
        Arrays.fill(other, 0f);
        other[2] = 360f;
        System.out.println("H(0-360) distance range: " + colDist.testDistance()); // 0 - 360 deg

        Arrays.fill(ref, 0f);
        Arrays.fill(other, 0f);
        other[2] = 180f;
        System.out.println("H(0-180) distance range: " + colDist.testDistance()); // 0 - 360 deg
    }

    public ColorPalette sortByDistance(final double wL, final double wC, final double wH) {
        final int len = colors.length;
        final List<Color> sortedColors = new ArrayList<Color>(len);

        final List<Color> cols = new ArrayList<Color>(Arrays.asList(colors));

        final ColorDistances colDist = new ColorDistances(wL, wC, wH);

        final double thReject = colDist.scaleDistance(REJECT_TOO_CLOSE);
        System.out.println("thReject: " + thReject);

        // Find the darkest color or any other criteria ?
        Color current = null;

        double lMin = Double.POSITIVE_INFINITY;

        for (int i = 0, size = cols.size(); i < size; i++) {
            final Color color = cols.get(i);

            colDist.computeRef(color);

            final double l = colDist.getRef()[0];

            if (l < lMin) {
                lMin = l;
                current = color;
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("global L min: ({} for {})", lMin, current);
        }

        sortedColors.add(current);
        cols.remove(current);

        while (!cols.isEmpty()) {
            // Find extremas from current:
            colDist.computeDistances(current, cols);

            final double dist;
            final Color next;

            if (SORT_DIVERGING) {
                dist = colDist.getDistMax();
                next = colDist.getColMax();
            } else {
                dist = colDist.getDistMin();
                next = colDist.getColMin();
            }

            // Reject too close colors (looks bad):
            if (dist < thReject) {
                logger.info("Reject: {} distance from {} : {}  to {}", SORT_DIVERGING ? "max" : "min", current, dist, next);
                cols.remove(next);
            } else {
                // next is new closest match
                if (logger.isDebugEnabled()) {
                    logger.debug("{} distance from {} : {}  to {}", SORT_DIVERGING ? "max" : "min", current, dist, next);
                }

                sortedColors.add(next);
                cols.remove(next);
                current = next;
            }
        }

        // final check:
        Color prev = sortedColors.get(0);

        for (int i = 1, size = sortedColors.size(); i < size; i++) {
            final Color color = sortedColors.get(i);

            final double dist = colDist.computeDistance(prev, color);

            if (logger.isDebugEnabled()) {
                logger.debug("distance[{}]: {} from {} to {}", i, dist, prev, color);
            }
            prev = color;
        }

        logger.info("Sorted Colors ({}) vs initial ({})", sortedColors.size(), len);

        return new ColorPalette(sortedColors.toArray(new Color[sortedColors.size()]));
    }

    public static final class ColorDistances {

        // members:
        private Color colMin;
        private Color colMax;
        private double distMin;
        private double distMax;
        private final double[] deltasMin = new double[3];
        private final double[] deltasMax = new double[3];
        // temporary:
        private final double[] deltas = new double[3];
        private final float[] cRef = new float[4];
        private final float[] cOther = new float[4];

        private final boolean doNorm;
        private final double w2L;
        private final double w2C;
        private final double w2H;

        ColorDistances(final double wL, final double wC, final double wH) {
            this(wL, wC, wH, true);
        }

        ColorDistances(final double wL, final double wC, final double wH, final boolean doNorm) {
            this.doNorm = doNorm;
            this.w2L = wL * wL;
            this.w2C = wC * wC;
            this.w2H = wH * wH;
            reset();
        }

        void reset() {
            colMin = null;
            colMax = null;
            distMin = Double.POSITIVE_INFINITY;
            distMax = Double.NEGATIVE_INFINITY;
            Arrays.fill(deltasMin, Double.NaN);
            Arrays.fill(deltasMax, Double.NaN);
        }

        void computeDistances(final Color ref, final List<Color> colors) {
            reset();
            convert(ref, cRef);

            // Compute distances:
            final int len = colors.size();
            distMin = Double.POSITIVE_INFINITY;
            distMax = Double.NEGATIVE_INFINITY;
            double total = 0.0;

            for (int i = 0; i < len; i++) {
                final Color other = colors.get(i);

                if (ref != other) {
                    convert(other, cOther);
                    final double dist = distance();

                    if (dist < distMin) {
                        distMin = dist;
                        colMin = other;
                        System.arraycopy(deltas, 0, deltasMin, 0, 3);
                    }
                    if (dist > distMax) {
                        distMax = dist;
                        colMax = other;
                        System.arraycopy(deltas, 0, deltasMax, 0, 3);
                    }
                    total += dist;
                }
            }
            final double distMean = total / len;

            if (logger.isDebugEnabled()) {
                logger.debug("Color {} {} distance mean = {}, min = {} at  {} {} ({}), max = {} at  {} {} ({})",
                        Integer.toHexString(ref.getRGB()), Arrays.toString(cRef), distMean,
                        distMin, Integer.toHexString(colMin.getRGB()), Arrays.toString(convert(colMin, cOther)), Arrays.toString(deltasMin),
                        distMax, Integer.toHexString(colMax.getRGB()), Arrays.toString(convert(colMax, cOther)), Arrays.toString(deltasMax)
                );
            }
        }

        void computeRef(final Color ref) {
            convert(ref, cRef);
        }

        double computeDistance(final Color ref, final Color other) {
            convert(ref, cRef);

            final double dist;
            if (ref == other) {
                dist = Double.POSITIVE_INFINITY;
            } else {
                convert(other, cOther);
                dist = distance();
            }
            return dist;
        }

        private final static boolean DO_CHECK_NORM_BOUNDS = false;

        private float[] convert(final Color color, final float[] Lab) {
            // convert sRGB color into OK Lab (better & simple perceptual color space in 2021):
            if (USE_LCH) {
                ColorUtils.sRGB_to_OkLabCH(color.getRGB(), Lab);
            } else {
                ColorUtils.sRGB_to_OkLab(color.getRGB(), Lab);
            }
            // System.out.println("OkLab ref[" + Integer.toHexString(color.getRGB()) + "]: " + Arrays.toString(Lab));
            if (doNorm) {
                if (USE_LCH) {
                    /*
                    OkLCH:
                    Fast range:
                    L  range: [0.0, 0.99998826]
                    C range:  [0.0, 0.3226086]
                    H range:  [0.0, 359.9851]     
                     */
                    // Fix C:
                    Lab[1] /= 0.3226086f;

                    if (DO_CHECK_NORM_BOUNDS) {
                        if (Lab[1] > 1f) {
                            System.out.println("C Out of bounds: " + Lab[1]);
                        }
                    }
                } else {
                    /*
                    OkLAB:
                    Fast range:
                    L  range: [0.0, 0.99998826]
                    A range:  [-0.2339203, 0.2762803]
                    B range:  [-0.31161994, 0.19849062]
                     */
                    // Fix A/B:
                    Lab[1] /= (Lab[1] >= 0f) ? 0.2762803f : 0.2339203f;
                    Lab[2] /= (Lab[2] >= 0f) ? 0.19849062f : 0.31161994f;

                    if (DO_CHECK_NORM_BOUNDS) {
                        if (Math.abs(Lab[1]) > 1f) {
                            System.out.println("A out of bounds: " + Lab[1]);
                        }
                        if (Math.abs(Lab[2]) > 1f) {
                            System.out.println("B out of bounds: " + Lab[2]);
                        }
                    }
                }
                // System.out.println("OkLab ref[" + Integer.toHexString(color.getRGB()) + "] normalized: " + Arrays.toString(Lab));
            }
            return Lab;
        }

        private double distance() {
            // Luminance (L):
            deltas[0] = cRef[0] - cOther[0]; // in [0..1]

            if (USE_LCH) {
                // Lab in LCH form: see https://bottosson.github.io/posts/oklab/
                // Chroma (C): SQRT(a^2 + b^2):
                deltas[1] = cRef[1] - cOther[1];

                // Hue (H): atan2(b,a)
                // Keep boundaries at H=0 and H=360 to preserve color wheel orientation:
                // deltas[2] = ColorUtils.distanceAngle(cRef[2] - cOther[2]) / 180.0; // in [-1..1]
                deltas[2] = ColorUtils.distanceHue(cRef[2] - cOther[2]) / 360.0; // in [-1..1]
                // System.out.println("dH: " + deltas[2] + " for angles: " + cRef[2] + " - " + cOther[2]);
            } else {
                // Chroma (C): SQRT(a^2 + b^2):
                final double C1 = MathUtils.carthesianNorm(cRef[1], cRef[2]);
                final double C2 = MathUtils.carthesianNorm(cOther[1], cOther[2]);
                deltas[1] = C1 - C2;

                // Hue (H): 
                final double dA = cRef[1] - cOther[1];
                final double dB = cRef[2] - cOther[2];
                deltas[2] = Math.sqrt(Math.max(0.0, dA * dA + dB * dB - deltas[1] * deltas[1]));
            }
            // use squared distance (no sqrt):
            return (w2L * (deltas[0] * deltas[0]) + w2C * (deltas[1] * deltas[1]) + w2H * (deltas[2] * deltas[2]));
        }

        public double scaleDistance(final double delta) {
            return (w2L + w2C + w2H) * (delta * delta);
        }

        double testDistance() {
            return distance();
        }

        public Color getColMin() {
            return colMin;
        }

        public Color getColMax() {
            return colMax;
        }

        public double getDistMin() {
            return distMin;
        }

        public double getDistMax() {
            return distMax;
        }

        public double[] getDeltasMin() {
            return deltasMin;
        }

        public double[] getDeltasMax() {
            return deltasMax;
        }

        public float[] getRef() {
            return cRef;
        }

        public float[] getOther() {
            return cOther;
        }
    }

}
