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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class which combines the Component, Renderer and Editor for the Rating cell
 *
 * @author martin
 */
public final class RatingCell extends AbstractCellEditor implements TableCellEditor, TableCellRenderer, StarRater.StarListener {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(IRModel.class);

    /* undefined rating value */
    private static final int UNDEFINED = -1;

    /** edited value */
    private int rating;
    private final StarRater starRater;

    public RatingCell() {
        rating = UNDEFINED;
        starRater = new StarRater(5, 0);
        starRater.addStarListener(this);
    }

    /** 
     * Called once StarRater has edited value
     * @param selection edited value
     */
    @Override
    public void handleSelection(final int selection) {
        // keep value to return in getCellEditorValue()
        rating = selection;
        if (logger.isDebugEnabled()) {
            logger.debug("handleSelection: {}", rating);
        }
        // Indicate editing is done:
        fireEditingStopped();
    }

    @Override
    public Object getCellEditorValue() {
        if (logger.isDebugEnabled()) {
            logger.debug("getCellEditorValue: {} vs {}", rating, starRater.getSelection());
        }
        return rating;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        rating = UNDEFINED;
        starRater.setSelection((value == null) ? 0 : (int) value);
        if (logger.isDebugEnabled()) {
            logger.debug("getTableCellEditorComponent {}", starRater.getSelection());
        }
        return starRater;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        rating = UNDEFINED;
        starRater.setSelection((value == null) ? 0 : (int) value);
        if (logger.isDebugEnabled()) {
            logger.debug("getTableCellRendererComponent {}", starRater.getSelection());
        }
        return starRater;
    }
}
