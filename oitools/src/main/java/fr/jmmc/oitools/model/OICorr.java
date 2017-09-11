/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.DataRange;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.meta.Units;

/**
 *
 * @author kempsc
 */
public final class OICorr extends OITable {

    /* constants */
 /* static descriptors */
    /** CORRNAME keyword descriptor */
    private final static KeywordMeta KEYWORD_CORRNAME = new KeywordMeta(OIFitsConstants.KEYWORD_CORRNAME,
            "name of correlated data set", Types.TYPE_CHAR);
    /** NDATA keyword descriptor */
    private final static KeywordMeta KEYWORD_NDATA = new KeywordMeta(OIFitsConstants.KEYWORD_NDATA,
            "Number of correlated data", Types.TYPE_INT);

    /** IINDX  keyword descriptor */
    private final static ColumnMeta COLUMN_IINDX = new ColumnMeta(OIFitsConstants.COLUMN_IINDX,
            "Frist index of correlation matrix element", Types.TYPE_INT);

    /** JINDX  keyword descriptor */
    private final static ColumnMeta COLUMN_JINDX = new ColumnMeta(OIFitsConstants.COLUMN_JINDX,
            "Second index of correlation matrix element", Types.TYPE_INT);

    /** CORR  keyword descriptor */
    private final static ColumnMeta COLUMN_CORR = new ColumnMeta(OIFitsConstants.COLUMN_CORR,
            "Matrix element (IINDX, JINDX)", Types.TYPE_DBL, Units.NO_UNIT, DataRange.RANGE_POSITIVE_STRICT);

    /**
     * Public OICorr class constructor
     * @param oifitsFile main OifitsFile
     */
    public OICorr(final OIFitsFile oifitsFile) {
        super(oifitsFile);

        // CORRNAME  keyword definition
        addKeywordMeta(KEYWORD_CORRNAME);

        // NDATA  keyword definition
        addKeywordMeta(KEYWORD_NDATA);

        // IINDX  keyword definition
        addColumnMeta(COLUMN_IINDX);

        // JINDX  keyword definition
        addColumnMeta(COLUMN_JINDX);

        // CORR  keyword definition
        addColumnMeta(COLUMN_CORR);
    }

    /**
     * Public OICorr class constructor to create a new table
     * @param oifitsFile main OifitsFile
     * @param nbRows number of rows i.e. the Fits NAXIS2 keyword value
     */
    public OICorr(final OIFitsFile oifitsFile, final int nbRows) {
        this(oifitsFile);

        this.initializeTable(nbRows);
    }

    /* --- keywords --- */
    /**
     * Get the CORRNAME keyword value.
     * @return the value of CORRNAME keyword
     */
    public String getCorrName() {
        return getKeyword(OIFitsConstants.KEYWORD_CORRNAME);
    }

    /**
     * Define the CORRNAME keyword value
     * @param corrName value of CORRNAME keyword
     */
    public void setCorrName(final String corrName) {
        setKeyword(OIFitsConstants.KEYWORD_CORRNAME, corrName);
    }

    /**
     * Get the value of NDATA keyword
     * @return the value of NDATA keyword
     */
    public int getNData() {
        return getKeywordInt(OIFitsConstants.KEYWORD_NDATA);
    }

    /**
     * Define the NDATA keyword value
     * @param nData value of NDATA keyword
     */
    public void setNData(final int nData) {
        setKeywordInt(OIFitsConstants.KEYWORD_NDATA, nData);
    }

    /* --- column --- */
    /**
     * Return the IINDX column.
     * @return the IINDX column.
     */
    public int[] getIindx() {
        return this.getColumnInt(OIFitsConstants.COLUMN_IINDX);
    }

    /**
     * Return the JINDX column.
     * @return the JINDX column.
     */
    public int[] getJindx() {
        return this.getColumnInt(OIFitsConstants.COLUMN_JINDX);
    }

    /**
     * Return the CORR column.
     * @return the CORR column.
     */
    public double[] getCorr() {
        return this.getColumnAsDouble(OIFitsConstants.COLUMN_CORR);
    }

    /* --- Other methods --- */
    /**
     * Returns a string representation of this table
     * @return a string representation of this table
     */
    @Override
    public String toString() {
        return super.toString() + " [ CORRNAME=" + getCorrName() + " | " + getNData() + " Number off correlation data ]";
    }

    /**
     * Do syntactical analysis.
     * @param checker checker component
     */
    @Override
    public void checkSyntax(final OIFitsChecker checker) {
        super.checkSyntax(checker);

        if (getCorrName() != null && getCorrName().length() == 0) {
            checker.severe("CORRNAME identifier has blank value");
        }
        final int nRows = getNbRows();

        // ndata gives the square matrix dimensions [N x N]
        final int ndata = getNData();

        final int[] iIndx = getIindx();
        final int[] jIndx = getJindx();

        for (int i = 0, j; i < nRows; i++) {
            final int idxI = iIndx[i];
            final int idxJ = jIndx[i];

            // rule [OI_CORR_1] check I >= 1 (J >= 2 redundant below)
            if (idxI < 1) {
                checker.severe("IINDX index [" + idxI + "] cannot be < 1 at row " + i);
            }
            // rule [OI_CORR_2] check J > I
            if (idxJ <= idxI) {
                checker.severe("JINDX index [" + idxJ + "] cannot be <= IINDX index [" + idxI + "] at row " + i);
            }
            // rule [OI_CORR_3] check I <= ndata and J <= ndata
            if (idxI > ndata) {
                checker.severe("IINDX index [" + idxI + "] cannot be > NDATA [" + ndata + "] at row " + i);
            }
            if (idxI > ndata || idxJ > ndata) {
                checker.severe("JINDX index [" + idxJ + "] cannot be > NDATA [" + ndata + "] at row " + i);
            }
        }

        getOIFitsFile().checkCrossReference(this, checker);

    }
}
