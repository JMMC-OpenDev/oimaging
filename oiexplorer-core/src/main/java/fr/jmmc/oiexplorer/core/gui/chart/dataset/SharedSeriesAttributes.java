/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.chart.dataset;

import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oiexplorer.core.gui.chart.ColorPalette;
import fr.jmmc.oiexplorer.core.model.util.StationNamesComparator;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global series attributes (names <-> Color, Symbol ...) to have consistent representation across plots based on ordered names
 * @author bourgesl
 */
public final class SharedSeriesAttributes {

    /** Class logger */
    private static final Logger logger = LoggerFactory.getLogger(SharedSeriesAttributes.class.getName());

    /** singleton instance */
    public final static SharedSeriesAttributes INSTANCE = new SharedSeriesAttributes("global");
    /** singleton instance */
    public final static SharedSeriesAttributes INSTANCE_OIXP = new SharedSeriesAttributes("oiexplorer", INSTANCE);

    /* members */
    private final String name;
    private final SharedSeriesAttributes parent;
    private final Set<String> labelNames = new HashSet<String>(64);
    private final Set<WeightedLabel> labels = new HashSet<WeightedLabel>(64);
    private final Map<String, Integer> colorMap = new HashMap<String, Integer>(64);
    private int offsetIdx = 0;
    private int colIdx = 0;

    private SharedSeriesAttributes(final String name) {
        this(name, null);
    }

    private SharedSeriesAttributes(final String name, final SharedSeriesAttributes parent) {
        this.name = name;
        this.parent = parent;
    }

    public void reset() {
        labelNames.clear();
        labels.clear();
        colorMap.clear();
        colIdx = (parent != null) ? parent.getColIdx() : offsetIdx;
    }

    public boolean hasLabel(final String label) {
        if (parent != null && parent.hasLabel(label)) {
            return true;
        }
        return this.labelNames.contains(label);
    }

    public void addLabel(final String label) {
        addLabel(label, null, 0.0);
    }

    public void addLabel(final String label, final double weight) {
        addLabel(label, null, weight);
    }

    public void addLabel(final String label, final String alias) {
        addLabel(label, alias, 0.0);
    }

    public void addLabel(final String label, final String alias, final double weight) {
        if (!hasLabel(label)) {
            labelNames.add(label);
            if (alias != null) {
                labelNames.add(alias);
            }
            labels.add(new WeightedLabel(label, alias, weight));
        }
    }

    public void define() {
        logger.debug("labels (raw):  {}", labels);

        // Sort labels by names and / or weights:
        final List<WeightedLabel> sortedLabels = new ArrayList<WeightedLabel>(labels);
        Collections.sort(sortedLabels);

        logger.debug("labels (sort): {}", sortedLabels);

        // TODO: use usage counts per color (add/remove usages => gaps)
        int c = colIdx;

        for (int n = 0, len = sortedLabels.size(); n < len; n++) {
            final WeightedLabel l = sortedLabels.get(n);
            final Integer idx = NumberUtils.valueOf(c++);

            colorMap.put(l.label, idx);
            if (l.alias != null) {
                colorMap.put(l.alias, idx);
            }
        }
        logger.debug("colorMap:      {}", colorMap);

        colIdx = c;
    }

    public void addColorAlias(final String label, final String alias) {
        if (!label.equals(alias)) {
            final Integer idx = colorMap.get(label);
            if (idx == null) {
                if (parent != null) {
                    parent.addColorAlias(label, alias);
                }
            } else {
                // define alias:
                colorMap.put(alias, idx);
            }
        }
    }

    public Integer getColorIndex(final String label) {
        final Integer idx = colorMap.get(label);
        if (idx == null) {
            if (parent != null) {
                return parent.getColorIndex(label);
            }
            logger.warn("Missing color for label: {}", label);
            // Use first color in the palette:
            return 0; 
        }
        return idx;
    }

    public Color getColor(final String label) {
        return ColorPalette.getColorPalette().getColor(getColorIndex(label));
    }

    public Color getColorAlpha(final String label) {
        return ColorPalette.getColorPaletteAlpha().getColor(getColorIndex(label));
    }

    private int getColIdx() {
        return colIdx;
    }

    public int getOffsetIdx() {
        return offsetIdx;
    }

    public void setOffsetIdx(int offsetIdx) {
        this.offsetIdx = offsetIdx;
    }

    @Override
    public String toString() {
        return "SharedSeriesAttributes{" + "name=" + name + ", parent=" + parent
                + ", labels=" + labels + ", colorMap=" + colorMap + '}';
    }

    private final static class WeightedLabel implements Comparable<WeightedLabel> {

        /* members */
        final String label;
        final String alias;
        final double weight;

        WeightedLabel(final String label, final String alias, final double weight) {
            this.label = label;
            this.alias = alias;
            this.weight = weight;
        }

        @Override
        public int compareTo(final WeightedLabel other) {
            int cmp = Double.compare(weight, other.weight);

            return (cmp == 0) ? StationNamesComparator.INSTANCE.compare(label, other.label) : cmp;
        }

        @Override
        public int hashCode() {
            return this.label.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final WeightedLabel other = (WeightedLabel) obj;
            if ((this.label == null) ? (other.label != null) : !this.label.equals(other.label)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "WeightedLabel{label=" + label + ", alias=" + alias + ", weight=" + weight + '}';
        }

    }
}
