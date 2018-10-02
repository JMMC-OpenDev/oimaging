/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.gui;

import fr.jmmc.oimaging.services.ServiceResult;
import fr.jmmc.oitools.image.FitsImageHDU;
import java.awt.Color;
import java.awt.Component;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * CellRenderer of various OImaging class used by JList of Combobox.
 * @author mellag
 */
public class OiCellRenderer extends DefaultListCellRenderer {

    public final static DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(
                list, value, index,
                isSelected, cellHasFocus);
        if (value == null) {

            setText("");

        } else if (value instanceof ServiceResult) {

            final ServiceResult serviceResult = (ServiceResult) value;
            setText(serviceResult.getService().getName() + " run " + (serviceResult.isValid() ? "ok" : "ko")
                    + " @ " + timeFormat.format(serviceResult.getStartTime()));
            if (!serviceResult.isValid()) {
                setForeground(Color.RED);
            }

        } else if (value instanceof FitsImageHDU) {

            FitsImageHDU fitsImageHDU = (FitsImageHDU) value;
            setText(fitsImageHDU.getHduName());

        } else {

            setText(value.getClass() + " not implemented by OiCellRenderer");

        }
        return this;
    }
}
