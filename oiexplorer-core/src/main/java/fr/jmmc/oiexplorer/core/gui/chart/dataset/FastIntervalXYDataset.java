/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.chart.dataset;

import fr.jmmc.jmcs.util.NumberUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.util.PublicCloneable;

/**
 * An efficient implementation of jFreeChart's DefaultIntervalXYDataset
 * @author bourgesl
 * @param <K> key type
 * @param <V> comparable type
 */
public final class FastIntervalXYDataset<K extends Comparable<V>, V> extends AbstractIntervalXYDataset implements PublicCloneable {

    /** default serial UID for Serializable interface */
    private static final long serialVersionUID = 1;
    /** initial capacity for internal collections */
    private final static int INITIAL_CAPACITY = 256;
    /* members */
    /** number of series */
    private int seriesCount = 0;
    /** Storage for the series keys */
    private final HashMap<K, Integer> seriesKeys;
    /** Storage for the indexes of series keys */
    private final HashMap<Integer, K> keysIndexes;
    /**
     * Storage for the series's double values in the dataset.  We use a list because the
     * order of the series is significant.  This list must be kept in sync
     * with the seriesKeys list.
     */
    private final ArrayList<int[][]> seriesIntDataList;
    /**
     * Storage for the series's double values in the dataset.  We use a list because the
     * order of the series is significant.  This list must be kept in sync
     * with the seriesKeys list.
     */
    private final ArrayList<double[][]> seriesDblDataList;

    /**
     * Creates a new <code>FastIntervalXYDataset</code> instance, initially
     * containing no data.
     */
    public FastIntervalXYDataset() {
        this.seriesKeys = new HashMap<K, Integer>(INITIAL_CAPACITY);
        this.keysIndexes = new HashMap<Integer, K>(INITIAL_CAPACITY);
        this.seriesIntDataList = new ArrayList<int[][]>(INITIAL_CAPACITY);
        this.seriesDblDataList = new ArrayList<double[][]>(INITIAL_CAPACITY);
    }

    /**
     * Increases the capacity of the list of series, if
     * necessary, to ensure that it can hold at least the number of elements
     * specified by the minimum capacity argument.
     *
     * @param   minCapacity   the desired minimum capacity
     */
    public void ensureCapacity(final int minCapacity) {
        // no way to ensure capacity in map (see HashMap.resize package visible)
        this.seriesIntDataList.ensureCapacity(minCapacity);
        this.seriesDblDataList.ensureCapacity(minCapacity);
    }

    /**
     * Returns the number of series in the dataset.
     *
     * @return The series count.
     */
    @Override
    public int getSeriesCount() {
        // Optimized code (use int):
        return seriesCount;
    }

    private void checkIndex(final int series) {
        if (series < 0 || series >= seriesCount) {
            throw new IllegalArgumentException("Series index out of bounds");
        }
    }

    /**
     * Returns the key for a series.
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     *
     * @return The key for the series.
     *
     * @throws IllegalArgumentException if <code>series</code> is not in the
     *     specified range.
     */
    @Override
    public Comparable<V> getSeriesKey(final int series) {
        checkIndex(series);
        return this.keysIndexes.get(NumberUtils.valueOf(series));
    }

    /**
     * Returns the number of items in the specified series.
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     *
     * @return The item count.
     *
     * @throws IllegalArgumentException if <code>series</code> is not in the
     *     specified range.
     */
    @Override
    public int getItemCount(final int series) {
        checkIndex(series);
        final double[][] seriesData = this.seriesDblDataList.get(series);
        return seriesData[0].length;
    }

    /**
     * Returns the index of the named series, or -1.
     *
     * @param seriesKey  the series key (<code>null</code> permitted).
     *
     * @return The index.
     */
    @Override
    public int indexOf(final Comparable seriesKey) {
        // Optimized code (use key map):
        final Integer seriesIndex = this.seriesKeys.get(seriesKey);

        return (seriesIndex != null) ? seriesIndex.intValue() : -1;
    }

    /**
     * Returns the x-value for an item within a series.
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     * @param item  the item index (in the range <code>0</code> to
     *     <code>getItemCount(series)</code>).
     *
     * @return The x-value.
     *
     * @throws ArrayIndexOutOfBoundsException if <code>series</code> is not
     *     within the specified range.
     * @see #getX(int, int)
     */
    @Override
    public double getXValue(final int series, final int item) {
        final double[][] seriesData = this.seriesDblDataList.get(series);
        return seriesData[0][item];
    }

    /**
     * Returns the starting x-value for an item within a series.
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     * @param item  the item index (in the range <code>0</code> to
     *     <code>getItemCount(series)</code>).
     *
     * @return The starting x-value.
     *
     * @throws ArrayIndexOutOfBoundsException if <code>series</code> is not
     *     within the specified range.
     * @see #getStartX(int, int)
     */
    @Override
    public double getStartXValue(final int series, final int item) {
        final double[][] seriesData = this.seriesDblDataList.get(series);
        return seriesData[1][item];
    }

    /**
     * Returns the ending x-value for an item within a series.
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     * @param item  the item index (in the range <code>0</code> to
     *     <code>getItemCount(series)</code>).
     *
     * @return The ending x-value.
     *
     * @throws ArrayIndexOutOfBoundsException if <code>series</code> is not
     *     within the specified range.
     * @see #getEndX(int, int)
     */
    @Override
    public double getEndXValue(final int series, final int item) {
        final double[][] seriesData = this.seriesDblDataList.get(series);
        return seriesData[2][item];
    }

    /**
     * Returns the y-value for an item within a series.
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     * @param item  the item index (in the range <code>0</code> to
     *     <code>getItemCount(series)</code>).
     *
     * @return The y-value.
     *
     * @throws ArrayIndexOutOfBoundsException if <code>series</code> is not
     *     within the specified range.
     * @see #getY(int, int)
     */
    @Override
    public double getYValue(final int series, final int item) {
        final double[][] seriesData = this.seriesDblDataList.get(series);
        return seriesData[3][item];
    }

    /**
     * Returns the starting y-value for an item within a series.
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     * @param item  the item index (in the range <code>0</code> to
     *     <code>getItemCount(series)</code>).
     *
     * @return The starting y-value.
     *
     * @throws ArrayIndexOutOfBoundsException if <code>series</code> is not
     *     within the specified range.
     * @see #getStartY(int, int)
     */
    @Override
    public double getStartYValue(final int series, final int item) {
        final double[][] seriesData = this.seriesDblDataList.get(series);
        return seriesData[4][item];
    }

    /**
     * Returns the ending y-value for an item within a series.
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     * @param item  the item index (in the range <code>0</code> to
     *     <code>getItemCount(series)</code>).
     *
     * @return The ending y-value.
     *
     * @throws ArrayIndexOutOfBoundsException if <code>series</code> is not
     *     within the specified range.
     * @see #getEndY(int, int)
     */
    @Override
    public double getEndYValue(final int series, final int item) {
        final double[][] seriesData = this.seriesDblDataList.get(series);
        return seriesData[5][item];
    }

    /**
     * Returns the ending x-value for an item within a series.
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     * @param item  the item index (in the range <code>0</code> to
     *     <code>getItemCount(series)</code>).
     *
     * @return The ending x-value.
     *
     * @throws ArrayIndexOutOfBoundsException if <code>series</code> is not
     *     within the specified range.
     * @see #getEndXValue(int, int)
     */
    @Override
    public Number getEndX(final int series, final int item) {
        throw new IllegalStateException("Not Implemented !");
//        return Double.valueOf(getEndXValue(series, item));
    }

    /**
     * Returns the ending y-value for an item within a series.
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     * @param item  the item index (in the range <code>0</code> to
     *     <code>getItemCount(series)</code>).
     *
     * @return The ending y-value.
     *
     * @throws ArrayIndexOutOfBoundsException if <code>series</code> is not
     *     within the specified range.
     * @see #getEndYValue(int, int)
     */
    @Override
    public Number getEndY(final int series, final int item) {
        throw new IllegalStateException("Not Implemented !");
//        return Double.valueOf(getEndYValue(series, item));
    }

    /**
     * Returns the starting x-value for an item within a series.
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     * @param item  the item index (in the range <code>0</code> to
     *     <code>getItemCount(series)</code>).
     *
     * @return The starting x-value.
     *
     * @throws ArrayIndexOutOfBoundsException if <code>series</code> is not
     *     within the specified range.
     * @see #getStartXValue(int, int)
     */
    @Override
    public Number getStartX(final int series, final int item) {
        throw new IllegalStateException("Not Implemented !");
//        return Double.valueOf(getStartXValue(series, item));
    }

    /**
     * Returns the starting y-value for an item within a series.
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     * @param item  the item index (in the range <code>0</code> to
     *     <code>getItemCount(series)</code>).
     *
     * @return The starting y-value.
     *
     * @throws ArrayIndexOutOfBoundsException if <code>series</code> is not
     *     within the specified range.
     * @see #getStartYValue(int, int)
     */
    @Override
    public Number getStartY(final int series, final int item) {
        throw new IllegalStateException("Not Implemented !");
//        return Double.valueOf(getStartYValue(series, item));
    }

    /**
     * Returns the x-value for an item within a series.
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     * @param item  the item index (in the range <code>0</code> to
     *     <code>getItemCount(series)</code>).
     *
     * @return The x-value.
     *
     * @throws ArrayIndexOutOfBoundsException if <code>series</code> is not
     *     within the specified range.
     * @see #getXValue(int, int)
     */
    @Override
    public Number getX(final int series, final int item) {
        throw new IllegalStateException("Not Implemented !");
//        return Double.valueOf(getXValue(series, item));
    }

    /**
     * Returns the y-value for an item within a series.
     *
     * @param series  the series index (in the range <code>0</code> to
     *     <code>getSeriesCount() - 1</code>).
     * @param item  the item index (in the range <code>0</code> to
     *     <code>getItemCount(series)</code>).
     *
     * @return The y-value.
     *
     * @throws ArrayIndexOutOfBoundsException if <code>series</code> is not
     *     within the specified range.
     * @see #getYValue(int, int)
     */
    @Override
    public Number getY(final int series, final int item) {
        throw new IllegalStateException("Not Implemented !");
//        return Double.valueOf(getYValue(series, item));
    }

    public int getDataRow(final int series, final int item) {
        final int[][] seriesData = this.seriesIntDataList.get(series);
        return seriesData[0][item];
    }

    public int getDataCol(final int series, final int item) {
        final int[][] seriesData = this.seriesIntDataList.get(series);
        return seriesData[1][item];
    }

    /**
     * Adds a series or if a series with the same key already exists replaces
     * the data for that series, then sends a {@link DatasetChangeEvent} to
     * all registered listeners.
     *
     * @param seriesKey  the series key (<code>null</code> not permitted).
     * @param dataInt  the integer data (must be an array with length 2, containing two
     *     arrays of equal length, containing the row and column indices
     * @param dataDbl  the double data (must be an array with length 6, containing six
     *     arrays of equal length, containing x, xLow, xUp, y, yLow, yUp
     */
    public void addSeries(final K seriesKey, final int[][] dataInt, final double[][] dataDbl) {
        if (seriesKey == null) {
            throw new IllegalArgumentException("The 'seriesKey' cannot be null.");
        }
        if (dataDbl == null) {
            throw new IllegalArgumentException("The 'data' is null.");
        }
        if (dataInt.length != 2) {
            throw new IllegalArgumentException("The 'dataInt' array must have length == 2.");
        }
        if (dataDbl.length != 6) {
            throw new IllegalArgumentException("The 'dataDbl' array must have length == 6.");
        }
        int length = dataInt[0].length;
        if (length != dataInt[1].length) {
            throw new IllegalArgumentException("The 'dataInt' array must contain two arrays with equal length.");
        }
        length = dataDbl[0].length;
        if (length != dataDbl[1].length
                || length != dataDbl[2].length
                || length != dataDbl[3].length
                || length != dataDbl[4].length
                || length != dataDbl[5].length) {
            throw new IllegalArgumentException("The 'dataDbl' array must contain two arrays with equal length.");
        }
        if (length != dataInt[0].length) {
            throw new IllegalArgumentException("The 'dataInt' and 'dataDbl' arrays must have equal length.");
        }

        final int seriesIndex = indexOf(seriesKey);
        if (seriesIndex == -1) {
            // add a new series:
            this.seriesIntDataList.add(dataInt);
            this.seriesDblDataList.add(dataDbl);

            // cache serie index into maps:
            final Integer seriesIdx = NumberUtils.valueOf(this.seriesCount);
            this.seriesKeys.put(seriesKey, seriesIdx);
            this.keysIndexes.put(seriesIdx, seriesKey);

            // update series count:
            this.seriesCount++;
        } else {
            // replace an existing series:
            this.seriesIntDataList.set(seriesIndex, dataInt);
            this.seriesDblDataList.set(seriesIndex, dataDbl);
        }
        notifyListeners(new DatasetChangeEvent(this, this));
    }

    /**
     * Tests this <code>FastIntervalXYDataset</code> instance for equality
     * with an arbitrary object.  This method returns <code>true</code> if and
     * only if:
     * <ul>
     * <li><code>obj</code> is not <code>null</code>;</li>
     * <li><code>obj</code> is an instance of
     *         <code>FastIntervalXYDataset</code>;</li>
     * <li>both datasets have the same number of series, each containing
     *         exactly the same values.</li>
     * </ul>
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return A boolean.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FastIntervalXYDataset)) {
            return false;
        }
        final FastIntervalXYDataset that = (FastIntervalXYDataset) obj;
        if (!this.seriesKeys.equals(that.seriesKeys)) {
            return false;
        }
        // TODO: compare int[][]
        for (int i = 0, len = this.seriesCount; i < len; i++) {
            double[][] d1 = this.seriesDblDataList.get(i);
            double[][] d2 = (double[][]) that.seriesDblDataList.get(i);
            double[] d1x = d1[0];
            double[] d2x = d2[0];
            if (!Arrays.equals(d1x, d2x)) {
                return false;
            }
            double[] d1xs = d1[1];
            double[] d2xs = d2[1];
            if (!Arrays.equals(d1xs, d2xs)) {
                return false;
            }
            double[] d1xe = d1[2];
            double[] d2xe = d2[2];
            if (!Arrays.equals(d1xe, d2xe)) {
                return false;
            }
            double[] d1y = d1[3];
            double[] d2y = d2[3];
            if (!Arrays.equals(d1y, d2y)) {
                return false;
            }
            double[] d1ys = d1[4];
            double[] d2ys = d2[4];
            if (!Arrays.equals(d1ys, d2ys)) {
                return false;
            }
            double[] d1ye = d1[5];
            double[] d2ye = d2[5];
            if (!Arrays.equals(d1ye, d2ye)) {
                return false;
            }
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
        int result;
        result = this.seriesKeys.hashCode();
        result = 13 * result + this.seriesIntDataList.hashCode();
        result = 29 * result + this.seriesDblDataList.hashCode();
        return result;
    }

    /**
     * Returns a clone of this dataset.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException if the dataset contains a series with
     *         a key that cannot be cloned.
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new IllegalStateException("Not Implemented !");
        // TODO: implement correctly:
        /*
         final FastIntervalXYDataset clone = (FastIntervalXYDataset) super.clone();
         clone.seriesKeys = new java.util.ArrayList(this.seriesKeys);
         clone.seriesList = new ArrayList(this.seriesList.size());
         for (int i = 0; i < this.seriesList.size(); i++) {
         double[][] data = this.seriesList.get(i);
         double[] x = data[0];
         double[] xStart = data[1];
         double[] xEnd = data[2];
         double[] y = data[3];
         double[] yStart = data[4];
         double[] yEnd = data[5];
         double[] xx = new double[x.length];
         double[] xxStart = new double[xStart.length];
         double[] xxEnd = new double[xEnd.length];
         double[] yy = new double[y.length];
         double[] yyStart = new double[yStart.length];
         double[] yyEnd = new double[yEnd.length];
         System.arraycopy(x, 0, xx, 0, x.length);
         System.arraycopy(xStart, 0, xxStart, 0, xStart.length);
         System.arraycopy(xEnd, 0, xxEnd, 0, xEnd.length);
         System.arraycopy(y, 0, yy, 0, y.length);
         System.arraycopy(yStart, 0, yyStart, 0, yStart.length);
         System.arraycopy(yEnd, 0, yyEnd, 0, yEnd.length);
         clone.seriesList.add(i, new double[][]{xx, xxStart, xxEnd, yy, yyStart, yyEnd});
         }
         return clone;
         */
    }
}
