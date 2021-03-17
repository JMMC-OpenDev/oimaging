/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oiexplorer.core.gui.model;

import fr.jmmc.oitools.fits.FitsHeaderCard;
import fr.jmmc.oitools.fits.FitsHDU;
import fr.jmmc.oitools.meta.KeywordMeta;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Basic table model to display header cards and keywords of any Fits table (read-only).
 * @author mellag
 */
public final class KeywordsTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private static final String[] COLUMN_NAMES = new String[]{"Keyword Name", "Value", "Description"};
    private static final Class<?>[] COLUMN_TYPES = new Class<?>[]{String.class, Object.class, String.class};

    /** FITS hdu reference */
    private FitsHDU hdu = null;

    public KeywordsTableModel() {
    }

    public void setFitsHdu(final FitsHDU hdu) {
        this.hdu = hdu;
        fireTableStructureChanged();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(final int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(final int column) {
        return COLUMN_TYPES[column];
    }

    @Override
    public int getRowCount() {
        if (hdu != null) {
            final int nHeaderCards = hdu.hasHeaderCards() ? hdu.getHeaderCards().size() : 0;
            return nHeaderCards + hdu.getKeywordsDesc().size();
        }
        return 0;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        if (hdu == null) {
            return null;
        }
        final int nHeaderCards = hdu.hasHeaderCards() ? hdu.getHeaderCards().size() : 0;

        if (rowIndex < nHeaderCards) {
            final List<FitsHeaderCard> headerCards = hdu.getHeaderCards();
            // header cards first
            final FitsHeaderCard headerCard = headerCards.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return headerCard.getKey();
                case 1:
                    return headerCard.getValue();
                case 2:
                    return headerCard.getComment();
                default:
                    break;
            }
        } else {
            // keywords
            final int keywordIndex = rowIndex - nHeaderCards;

            final KeywordMeta meta = hdu.getKeywordDesc(keywordIndex);
            final String key = meta.getName();
            switch (columnIndex) {
                case 0:
                    return key;
                case 1:
                    return hdu.getKeywordValue(key);
                case 2:
                    return meta.getDescription();
                default:
                    break;
            }
        }
        return "?Undefined?";
    }
}
