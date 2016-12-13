/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.gui.model;

import fr.jmmc.oitools.fits.FitsHeaderCard;
import fr.jmmc.oitools.model.Table;
import javax.swing.table.AbstractTableModel;

/**
 * Quick and dirty table model to display header cards and keywords.
 * TODO remove use of FitsCardHeader.
 * @author mellag
 */
public class KeywordsTableModel extends AbstractTableModel {

    private final Table table;

    public KeywordsTableModel(Table table) {
        this.table = table;
    }

    @Override
    public int getRowCount() {
        return table.getHeaderCards().size() + table.getKeywordsDesc().size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < table.getHeaderCards().size()) {
            FitsHeaderCard headerCard = table.getHeaderCards().get(rowIndex);
            if (columnIndex == 0) {
                return headerCard.getKey();
            } else if (columnIndex == 1) {
                return headerCard.getValue();
            } else if (columnIndex == 2) {
                return headerCard.getComment();
            }
        } else {
            int row = rowIndex - table.getHeaderCards().size();
            Object key = table.getKeywordsDesc().keySet().toArray()[row];
            if (columnIndex == 0) {
                return key;
            } else if (columnIndex == 1) {
                return table.getKeywordValue(table.getKeywordsDesc().get(key).getName());
            } else if (columnIndex == 2) {
                return table.getKeywordsDesc().get(key).getDescription();
            }
        }
        return "Undefined??";

    }
}
