/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ***************************************************************************** */
package fr.jmmc.oimaging.model;

import fr.jmmc.oimaging.gui.StarRater;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * Class which combines the Component, Renderer and Editor for the Rating cell
 *
 * @author martin
 */
public class RatingCell extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

    private int rating;
    private StarRater starRater;
    private final JPanel panel;

    public RatingCell() {
        rating = 0;
        starRater = new StarRater(5, rating);
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
    }

    @Override
    public Object getCellEditorValue() {
        return rating;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        rating = (int) value;
        starRater.setRating(rating);
        panel.add(starRater);
        return panel;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        rating = (int) value;
        starRater.setRating(rating);
        panel.add(starRater);
        return panel;
    }
}
