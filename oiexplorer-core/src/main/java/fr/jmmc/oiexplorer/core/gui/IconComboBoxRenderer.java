/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;

/**
 *
 * @author bourgesl
 */
public abstract class IconComboBoxRenderer extends DefaultListCellRenderer {

    private static final long serialVersionUID = 1L;

    // members:
    private final Map<String, Icon> cachedIcons = new HashMap<String, Icon>(64);

    public IconComboBoxRenderer() {
        super();
        final Dimension dim = new Dimension(380, 24);
        setMinimumSize(dim);
        setPreferredSize(dim);
    }

    /**
     * Return the Image corresponding to the given name
     * @param name
     * @return BufferedImage or null if the given name is not found
     */
    protected abstract Image getImage(final String name);

    @Override
    public final Component getListCellRendererComponent(final JList list,
                                                        final Object value,
                                                        final int index,
                                                        final boolean isSelected,
                                                        final boolean cellHasFocus) {

        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        // Get
        final String name = (String) list.getModel().getElementAt(index);

        Icon icon = cachedIcons.get(name);
        if (icon == null) {
            if (name != null) {
                final Image image = getImage(name);
                if (image != null) {
                    icon = new ImageIcon(image);
                    // cache icon:
                    cachedIcons.put(name, icon);
                }
            }
        }
        setIcon(icon);

        return this;
    }
}
