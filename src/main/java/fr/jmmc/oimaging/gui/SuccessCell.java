/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/


package fr.jmmc.oimaging.gui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author martin
 */
public class SuccessCell extends JLabel implements TableCellRenderer {

    public SuccessCell() {
        super.setOpaque(true);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.setBackground((boolean) value ? Color.GREEN : Color.RED);
        return this;
    }
    
}
