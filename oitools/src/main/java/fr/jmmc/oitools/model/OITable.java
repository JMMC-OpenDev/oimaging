/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.fits.FitsConstants;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;

/**
 * Base Class for all OI_* tables.
 */
public class OITable extends Table {

    /* constants */

    /* static descriptors */
        /** EXTNAME keyword descriptor */
    private final static KeywordMeta KEYWORD_EXTNAME = new KeywordMeta(FitsConstants.KEYWORD_EXT_NAME, "extension name", Types.TYPE_CHAR,
            new String[]{OIFitsConstants.TABLE_OI_ARRAY, OIFitsConstants.TABLE_OI_TARGET, OIFitsConstants.TABLE_OI_WAVELENGTH,
                OIFitsConstants.TABLE_OI_VIS, OIFitsConstants.TABLE_OI_VIS2, OIFitsConstants.TABLE_OI_T3, 
                OIFitsConstants.TABLE_OI_SPECTRUM, OIFitsConstants.TABLE_OI_FLUX
            });
    /** OI_REVN keyword descriptor */
    private final static KeywordMeta KEYWORD_OI_REVN = new KeywordMeta(OIFitsConstants.KEYWORD_OI_REVN, "revision number of the table definition", Types.TYPE_INT,
            new short[]{OIFitsConstants.KEYWORD_OI_REVN_1, OIFitsConstants.KEYWORD_OI_REVN_2});

    /* members */
    /** Main OIFitsFile */
    private final OIFitsFile oifitsFile;
    
    /**
     * Protected OITable class constructor
     * @param oifitsFile main OifitsFile
     */
    protected OITable(final OIFitsFile oifitsFile) {
        super();
        this.oifitsFile = oifitsFile;

        // overwrite previously set in Table
        // EXTNAME  keyword definition
        addKeywordMeta(KEYWORD_EXTNAME);

        // OI_REVN   keyword definition
        addKeywordMeta(KEYWORD_OI_REVN);

        // De
        String extName = null;
        if (this instanceof OITarget) {
            extName = OIFitsConstants.TABLE_OI_TARGET;
        } else if (this instanceof OIWavelength) {
            extName = OIFitsConstants.TABLE_OI_WAVELENGTH;
        } else if (this instanceof OIArray) {
            extName = OIFitsConstants.TABLE_OI_ARRAY;
        } else if (this instanceof OIVis) {
            extName = OIFitsConstants.TABLE_OI_VIS;
        } else if (this instanceof OIVis2) {
            extName = OIFitsConstants.TABLE_OI_VIS2;
        } else if (this instanceof OIT3) {
            extName = OIFitsConstants.TABLE_OI_T3;
        } else if (this instanceof OISpectrum) {
            extName = OIFitsConstants.TABLE_OI_SPECTRUM;
        }
        this.setExtName(extName);
        this.setOiRevn(OIFitsConstants.KEYWORD_OI_REVN_1);

    }    

    /**
     * Return the main OIFitsFile
     * @return OIFitsFile
     */
    public final OIFitsFile getOIFitsFile() {
        return this.oifitsFile;
    }

}
/*___oOo___*/
