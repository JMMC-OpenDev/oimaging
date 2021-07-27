/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ***************************************************************************** */
package fr.jmmc.oimaging.model;

import fr.jmmc.oimaging.gui.StarRater;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.AbstractCellEditor;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * Class which combines the Component, Renderer and Editor for the Rating cell
 *
 * @author martin
 */
public class RatingCell extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

    private StarRater ratingComponent;

    public RatingCell() {
        ratingComponent = new StarRater();
        ratingComponent.addStarListener(System.out::println);
    }

    @Override
    public Object getCellEditorValue() {
        return ratingComponent;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return (StarRater) value;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return (StarRater) value;
    }
}
