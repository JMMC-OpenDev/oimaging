/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.model.util;

import fr.jmmc.oiexplorer.core.model.plot.ColorMapping;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 *
 * @author mella
 */
public final class ColorMappingListCellRenderer {

    /** singleton */
    private static final ListCellRenderer renderer = new DefaultListCellRenderer() {
        /** default serial UID for Serializable interface */
        private static final long serialVersionUID = 1;

        /**
         * Return a component that has been configured to display the specified
         * value. That component's <code>paint</code> method is then called to
         * "render" the cell.  If it is necessary to compute the dimensions
         * of a list because the list cells do not have a fixed size, this method
         * is called to generate a component on which <code>getPreferredSize</code>
         * can be invoked.
         *
         * @param list The JList we're painting.
         * @param value The value returned by list.getModel().getElementAt(index).
         * @param index The cells index.
         * @param isSelected True if the specified cell was selected.
         * @param cellHasFocus True if the specified cell has the focus.
         * @return A component whose paint() method will render the specified value.
         *
         * @see JList
         * @see ListSelectionModel
         * @see ListModel
         */
        @Override
        public Component getListCellRendererComponent(
                final JList list,
                final Object value,
                final int index,
                final boolean isSelected,
                final boolean cellHasFocus) {
            
            final String val;
            if (value == null) {
                val = null;
            } else if (value instanceof ColorMapping) {                
                val = displayValue((ColorMapping) value);
            } else {
                val = value.getClass().getName();
            }
            return super.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);
        }
    };

    public static ListCellRenderer getListCellRenderer() {
        return renderer;
    }
    
    private ColorMappingListCellRenderer() {
        // utility class
    }
         
    
    /**
     * Return a textual value (e.g. to fill comboboxes ) for the given color mapping.
     * @param c input color mapping 
     * @return the value to be displayed
     */
    public static String displayValue(ColorMapping c) {
        switch (c) {
            case WAVELENGTH_RANGE:
                return "effective wave length";
            case STATION_INDEX:
                return "baseline or triplet";
            case CONFIGURATION:
                return "station configuration";
            case OBSERVATION_DATE:
                return "observation date";
        }
        return null;
    }
    
}
