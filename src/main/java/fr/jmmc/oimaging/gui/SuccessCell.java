/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.gui;

import fr.jmmc.jmcs.gui.util.ResourceImage;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author martin
 */
public class SuccessCell extends JPanel implements TableCellRenderer {

    private static final long serialVersionUID = 1L;

    /* member */
    private boolean success = false;

    public SuccessCell() {
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        success = (value == null) ? false : (boolean) value;
        return this;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage((success) ? ResourceImage.OK_MARK.icon().getImage() : ResourceImage.KO_MARK.icon().getImage(), this.getWidth() / 2, 2, this);
    }

}
