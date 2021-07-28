/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ***************************************************************************** */
package fr.jmmc.oimaging.model;

import fr.jmmc.oimaging.gui.StarRater;
import java.awt.Component;
import javax.swing.AbstractCellEditor;
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
    }

    @Override
    public Object getCellEditorValue() {
        return ratingComponent;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        ratingComponent = (StarRater) value;
        return ratingComponent;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        ratingComponent = (StarRater) value;
        return ratingComponent;
    }
}
