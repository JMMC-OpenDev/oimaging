/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.chart;

import java.awt.Color;
import org.jfree.chart.ChartColor;

/**
 * Very simple color palette
 * @author bourgesl
 */
public class ColorPalette {

    /** default colors */
    private final static Color[] DEFAULT_COLORS = {
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
    /** default color palette */
    private static ColorPalette DEFAULT_COLOR_PALETTE = new ColorPalette(DEFAULT_COLORS);
    /** default color palette with 80% opacity */
    private static ColorPalette DEFAULT_COLOR_PALETTE_ALPHA = new ColorPalette(DEFAULT_COLORS, 0.8f);

    /**
     * Return the default color palette
     * @return default color palette
     */
    public static ColorPalette getDefaultColorPalette() {
        return DEFAULT_COLOR_PALETTE;
    }

    /**
     * Return the default color palette with 80% opacity
     * @return default color palette with 80% opacity
     */
    public static ColorPalette getDefaultColorPaletteAlpha() {
        return DEFAULT_COLOR_PALETTE_ALPHA;
    }

    /* members */
    /** palette colors */
    private Color[] colors;

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
     * Return a Color for the given index
     * @param index
     * @return Color
     */
    public Color getColor(final int index) {
        return this.colors[index % this.colors.length];
    }
}
