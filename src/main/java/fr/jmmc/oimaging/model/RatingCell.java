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

    private JPanel panel;

    public RatingCell() {
        StarRater ratingComponent = new StarRater();
        ratingComponent.addStarListener(System.out::println);
        panel = new JPanel(new BorderLayout());
        panel.add(ratingComponent);
    }

    public void updateData(JPanel rating) {
        this.panel = rating;
    }
    
    public JPanel getCellPanel() {
        return panel;
    }

    @Override
    public Object getCellEditorValue() {
        return panel;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        JPanel rating = (JPanel) value;
        updateData(rating);
        return panel;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JPanel rating = (JPanel) value;
        updateData(rating);
        return panel;
    }
}
