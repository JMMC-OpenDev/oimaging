/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.gui;

import fr.jmmc.jmcs.util.ImageUtils;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author martin
 */
public class SuccessCell extends JPanel implements TableCellRenderer {

    private static final long serialVersionUID = 1L;

    /** Common resource directory containing icon files */
    private final static String IMAGE_RESOURCE_COMMON_PATH = "fr/jmmc/oimaging/resource/image/";

    private final static ImageIcon IMG_OK = ImageUtils.loadResourceIcon(IMAGE_RESOURCE_COMMON_PATH + "ok-16.png");
    private final static ImageIcon IMG_KO = ImageUtils.loadResourceIcon(IMAGE_RESOURCE_COMMON_PATH + "x-mark-3-16.png");

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
        g.drawImage((success) ? IMG_OK.getImage() : IMG_KO.getImage(), this.getWidth() / 2, 2, this);
    }

}
