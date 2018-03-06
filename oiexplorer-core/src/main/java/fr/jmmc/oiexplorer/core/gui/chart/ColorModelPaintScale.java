/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.chart;

import fr.jmmc.jmal.image.ColorScale;
import fr.jmmc.jmal.image.ImageUtils;
import java.awt.Color;
import java.awt.Paint;
import java.awt.image.IndexColorModel;
import java.io.Serializable;
import org.jfree.chart.HashUtils;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.util.PublicCloneable;

/**
 * This custom Paint scale returns colors using linear mapping defined by an indexed color model:
 * see ImageUtils.
 */
public class ColorModelPaintScale
        implements PaintScale, PublicCloneable, Serializable {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1L;
    /** The lower bound. */
    private final double lowerBound;
    /** The upper bound. */
    private final double upperBound;
    /** color model (lut) */
    transient final IndexColorModel colorModel;
    /** color scaling method */
    private transient final ColorScale colorScale;
    /* internal */
    /** index of the highest color */
    private final int iMaxColor;
    /** data to color linear scaling factor */
    private final float scalingFactor;
    /** scaled minimum value */
    private final float scaledMin;
    /** flag to invert the palette direction */
    private final boolean inverted;

    /**
     * Creates a new paint scale for values in the specified range.
     *
     * @param lowerBound  the lower bound.
     * @param upperBound  the upper bound.
     * @param colorModel color model
     * @param colorScale color scaling method
     *
     * @throws IllegalArgumentException if <code>lowerBound</code> is not
     *       less than <code>upperBound</code>.
     */
    public ColorModelPaintScale(final double lowerBound, final double upperBound,
                                final IndexColorModel colorModel, final ColorScale colorScale) {
        this(lowerBound, upperBound, colorModel, colorScale, false);
    }

    /**
     * Creates a new paint scale for values in the specified range.
     *
     * @param lowerBound  the lower bound.
     * @param upperBound  the upper bound.
     * @param colorModel color model
     * @param colorScale color scaling method
     * @param inverted true to invert the palette direction
     *
     * @throws IllegalArgumentException if <code>lowerBound</code> is not
     *       less than <code>upperBound</code>.
     */
    public ColorModelPaintScale(final double lowerBound, final double upperBound,
                                final IndexColorModel colorModel, final ColorScale colorScale, final boolean inverted) {
        if (lowerBound >= upperBound) {
            throw new IllegalArgumentException("Requires lowerBound < upperBound.");
        }
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.colorModel = colorModel;
        this.colorScale = colorScale;
        this.inverted = inverted;

        // prepare other variables:
        this.iMaxColor = colorModel.getMapSize() - 1;

        final float[] scaledMinMax = ImageUtils.scaleMinMax((float) this.lowerBound, (float) this.upperBound, colorScale);

        this.scaledMin = scaledMinMax[0];
        this.scalingFactor = ImageUtils.computeScalingFactor(scaledMinMax[0], scaledMinMax[1], colorModel.getMapSize());
    }

    /**
     * Returns the lower bound.
     *
     * @return The lower bound.
     *
     * @see #getUpperBound()
     */
    @Override
    public double getLowerBound() {
        return this.lowerBound;
    }

    /**
     * Returns the upper bound.
     *
     * @return The upper bound.
     *
     * @see #getLowerBound()
     */
    @Override
    public double getUpperBound() {
        return this.upperBound;
    }

    /**
     * Returns a paint for the specified value.
     *
     * @param value  the value (must be within the range specified by the lower and upper bounds for the scale).
     *
     * @return A paint for the specified value.
     */
    @Override
    public Paint getPaint(final double value) {
        final int colorIdx = ImageUtils.getColor(this.colorModel, this.iMaxColor,
                ImageUtils.getScaledValue((colorScale == ColorScale.LOGARITHMIC), scaledMin, scalingFactor, (float) value));
        
        return new Color(this.colorModel.getRGB((inverted) ? (iMaxColor - colorIdx) : colorIdx));
    }

    /**
     * Tests this <code>ColorModelPaintScale</code> instance for equality with an
     * arbitrary object.  This method returns <code>true</code> if and only
     * if:
     * <ul>
     * <li><code>obj</code> is not <code>null</code>;</li>
     * <li><code>obj</code> is an instance of <code>ColorModelPaintScale</code>;</li>
     * </ul>
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ColorModelPaintScale)) {
            return false;
        }
        ColorModelPaintScale that = (ColorModelPaintScale) obj;
        if (this.lowerBound != that.getLowerBound()) {
            return false;
        }
        if (this.upperBound != that.getUpperBound()) {
            return false;
        }
        if (!this.colorModel.equals(that.colorModel)) {
            return false;
        }
        return true;
    }

    /**
     * Returns a hash code for this instance.
     *
     * @return A hash code.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = HashUtils.hashCode(hash, this.lowerBound);
        hash = HashUtils.hashCode(hash, this.upperBound);
        hash = 43 * hash;
        return hash;
    }

    /**
     * Returns a clone of this <code>ColorModelPaintScale</code> instance.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if there is a problem cloning this
     *     instance.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
