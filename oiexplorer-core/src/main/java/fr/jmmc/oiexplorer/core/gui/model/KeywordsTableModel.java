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

    /* FITS hdu reference */
    private final FitsHDU hdu;

    public KeywordsTableModel(final FitsHDU hdu) {
        this.hdu = hdu;
    }

    @Override
    public int getRowCount() {
        return hdu.getHeaderCards().size() + hdu.getKeywordsDesc().size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex) {
        final List<FitsHeaderCard> headerCards = hdu.getHeaderCards();
        final int nHeaderCards = headerCards.size();

        if (rowIndex < nHeaderCards) {
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
