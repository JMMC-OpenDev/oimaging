/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools;

import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.WaveColumnMeta;
import fr.jmmc.oitools.model.OIData;
import fr.jmmc.oitools.model.OIFitsChecker;
import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OIFitsLoader;
import fr.jmmc.oitools.model.OITable;
import fr.nom.tam.fits.FitsException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.logging.Level;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Load OIFits files from the test/oifits folder to dump the complete OIFITSFile structure 
 * into a [filename].properties file (key / value pairs) in the test/test folder.
 * 
 * @see LoadOIFitsTest
 * @author kempsc
 */
public class DumpOIFitsTest extends AbstractFileBaseTest {

    // constants:
    public final static String COLUMN_SEC_2000 = "SEC_2000";
    public final static String COLUMN_SEC_2000_EXPR = "(MJD - 51544.5) * 86400.0";

    // members:
    private static OIFitsFile OIFITS = null;

    @BeforeClass
    public static void setupClass() {
        initializeTest();
    }

    @AfterClass
    public static void tearDownClass() {
        OIFITS = null;
        shutdownTest();
    }

    @Test
    public void dumpFile() throws IOException, FitsException {

        final OIFitsChecker checker = new OIFitsChecker();
        try {
            for (String f : getFitsFiles(new File(TEST_DIR_OIFITS))) {

                // reset properties anyway
                reset();

                dumpOIFits(checker, f);

                save(new File(TEST_DIR_TEST, OIFITS.getFileName() + ".properties"));
            }
        } finally {
            // validation results
            logger.log(Level.INFO, "validation results\n{0}", checker.getCheckReport());
        }

        // validation fail if SEVERE ERRORS
        if (false) {
            Assert.assertEquals("validation failed", 0, checker.getNbSeveres());
        }
    }

    private void dumpOIFits(final OIFitsChecker checker, String absFilePath) throws IOException, FitsException {
        checker.info("Checking file: " + absFilePath);

        OIFITS = OIFitsLoader.loadOIFits(checker, absFilePath);
        OIFITS.analyze();

        computeCustomExp(OIFITS.getOiDatas());

        DumpFitsTest.dump(OIFITS.getPrimaryImageHDU());

        put("FILENAME", OIFITS.getFileName());
        putInt("OI_ARRAY.COUNT", OIFITS.getNbOiArrays());
        putInt("OI_TARGET.COUNT", OIFITS.hasOiTarget() ? 1 : 0);
        putInt("OI_WAVELENGTH.COUNT", OIFITS.getNbOiWavelengths());
        putInt("OI_VIS.COUNT", OIFITS.getNbOiVis());
        putInt("OI_VIS2.COUNT", OIFITS.getNbOiVis2());
        putInt("OI_T3.COUNT", OIFITS.getNbOiT3());

        for (OITable oitable : OIFITS.getOiTables()) {
            dumpTable(oitable);
        }
    }

    public static void computeCustomExp(OIData[] datas) {
        // Compute custom expression on OIData tables:
        for (OIData oiData : datas) {
            oiData.checkExpression(COLUMN_SEC_2000, COLUMN_SEC_2000_EXPR);
            // Define the computed column:
            oiData.updateExpressionColumn(COLUMN_SEC_2000, COLUMN_SEC_2000_EXPR);
        }
    }

    private static void dumpTable(FitsTable table) {
        logger.log(Level.INFO, "Table: {0}", table.idToString());
        logger.log(Level.INFO, "nbRows: {0}", table.getNbRows());

        final String prefix = getHduId(table);

        putInt(prefix + ".NBRow", table.getNbRows());

        DumpFitsTest.dumpKeywords(table, getHduId(table));
        dumpColumns(table);
        dumpHeaderCards(table);

        // Specific tests:
        if (table instanceof OIData) {
            dumpOIData((OIData) table);
        }
    }

    private static void dumpOIData(OIData oidata) {

        final String hduId = getHduId(oidata);
        /** internal buffer */
        StringBuilder buffer = new StringBuilder();

        logger.log(Level.INFO, "Table: {0}", oidata.idToString());
        logger.log(Level.INFO, "nWaves: {0}", oidata.getNWave());
        putInt(hduId + ".nWave", oidata.getNWave());
        logger.log(Level.INFO, "nFlagged: {0}", oidata.getNFlagged());
        putInt(hduId + ".nFlagged", oidata.getNFlagged());

        Set<short[]> staIndexes = oidata.getDistinctStaIndex();
        logger.info("Distinct staIndexes:");

        for (short[] staIndex : staIndexes) {
            logger.log(Level.INFO, "{0} = {1}", new Object[]{Arrays.toString(staIndex), oidata.getStaNames(staIndex)});
            buffer.append(oidata.getStaNames(staIndex));
            buffer.append(", ");
        }
        put(hduId + ".staIndex", buffer.toString());

        buffer.setLength(0);

        Set<short[]> staConfs = oidata.getDistinctStaConf();
        logger.info("Distinct staConfs:");

        for (short[] staConf : staConfs) {
            logger.log(Level.INFO, "{0} = {1}", new Object[]{Arrays.toString(staConf), oidata.getStaNames(staConf)});
            buffer.append(oidata.getStaNames(staConf));
        }
        put(hduId + ".staConf", buffer.toString());

        logger.info("oidata");
    }

    private static void dumpColumns(FitsTable table) {

        final String hduId = getHduId(table);
        final String prefixC = hduId + ".C.";
        final String prefixDC = hduId + ".DC.";
        final String prefixMM = hduId + ".MM.";

        for (ColumnMeta columnMeta : table.getAllColumnDescCollection()) {
            dumpColumnMeta(columnMeta);

            if (table.getColumnDerivedDesc(columnMeta.getName()) != null) {
                dumpColumnValues(table, columnMeta, prefixDC);
                // No MinMax for derived columns
                // dumpColunmMinMax(table, columnMeta, prefixMM);
            } else {
                dumpColumnValues(table, columnMeta, prefixC);
                dumpColunmMinMax(table, columnMeta, prefixMM);
            }
        }
    }

    private static void dumpColumnValues(FitsTable table, ColumnMeta columnMeta, String prefix) {
        String propValue = null;

        switch (columnMeta.getDataType()) {
            case TYPE_CHAR:
                String[] chvalue = table.getColumnString(columnMeta.getName());
                propValue = Arrays.deepToString(chvalue);
                break;
            case TYPE_SHORT:
                if (columnMeta.isArray()) {
                    // Use getColumnAsShorts(s) to handle both std & derived columns
                    short[][] svalues = table.getColumnAsShorts(columnMeta.getName());
                    propValue = Arrays.deepToString(svalues);
                } else {
                    short[] svalue = table.getColumnShort(columnMeta.getName());
                    propValue = Arrays.toString(svalue);
                }
                break;
            case TYPE_INT:
                if (columnMeta.isArray()) {
                    // Use getColumnAsShorts(s) to handle both std & derived columns
                    int[][] ivalues = table.getColumnInts(columnMeta.getName());
                    propValue = Arrays.deepToString(ivalues);
                } else {
                    int[] ivalue = table.getColumnInt(columnMeta.getName());
                    propValue = Arrays.toString(ivalue);
                }
                break;
            case TYPE_DBL:
                // Use getColumnAsDouble(s) to handle both std & derived columns
                // If column value dont exist, compute it
                if (columnMeta.isArray()) {
                    double[][] dvalues = table.getColumnAsDoubles(columnMeta.getName());
                    propValue = Arrays.deepToString(dvalues);
                } else {
                    double[] dvalue = table.getColumnAsDouble(columnMeta.getName());
                    propValue = Arrays.toString(dvalue);
                }
                break;
            case TYPE_REAL:
                if (columnMeta.isArray()) {
                    // Impossible case in OIFits
                } else {
                    float[] fvalue = table.getColumnFloat(columnMeta.getName());
                    propValue = Arrays.toString(fvalue);
                }
                break;
            case TYPE_COMPLEX:
                if (columnMeta.isArray()) {
                    float[][][] cvalues = table.getColumnComplexes(columnMeta.getName());
                    propValue = Arrays.deepToString(cvalues);
                } else {
                    // Impossible case in OIFits
                }
                break;
            case TYPE_LOGICAL:
                if (columnMeta.is3D()) {
                    boolean[][][] bvalues = table.getColumnBoolean3D(columnMeta.getName());
                    propValue = Arrays.deepToString(bvalues);
                } else if (columnMeta.isArray()) {
                    boolean[][] bvalues = table.getColumnBooleans(columnMeta.getName());
                    propValue = Arrays.deepToString(bvalues);
                } else {
                    // Impossible case in OIFits
                }
                break;
            default:
                // Not Applicable
                break;
        }
        put(prefix + columnMeta.getName(),
                (propValue != null) ? propValue : "null");
    }

    private static void dumpColunmMinMax(FitsTable table, ColumnMeta columnMeta, String prefix) {
        final Object minmax = table.getMinMaxColumnValue(columnMeta.getName());
        String propValue = null;

        if (minmax != null) {
            switch (columnMeta.getDataType()) {
                case TYPE_CHAR:
                    // Not Applicable
                    break;

                case TYPE_SHORT:
                    short[] srange = (short[]) minmax;
                    propValue = Arrays.toString(srange);
                    break;
                case TYPE_INT:
                    int[] irange = (int[]) minmax;
                    propValue = Arrays.toString(irange);
                    break;

                case TYPE_DBL:
                    double[] drange = (double[]) minmax;
                    propValue = Arrays.toString(drange);
                    break;

                case TYPE_REAL:
                    float[] frange = (float[]) minmax;
                    propValue = Arrays.toString(frange);
                    break;

                case TYPE_COMPLEX:
                    // Not Applicable
                    break;

                case TYPE_LOGICAL:
                    // Not Applicable
                    break;

                default:
                // do nothing
            }
        }
        put(prefix + columnMeta.getName(),
                (propValue != null) ? propValue : "null");
    }

    private static void dumpColumnMeta(ColumnMeta columnMeta) {
        DumpFitsTest.dumpMeta(columnMeta);

        logger.log(Level.INFO, "Alias      : {0}", columnMeta.getAlias());
        logger.log(Level.INFO, "DataRange  : {0}", columnMeta.getDataRange());
        logger.log(Level.INFO, "ErrName    : {0}", columnMeta.getErrorColumnName());
        logger.log(Level.INFO, "Array ?    : {0}", columnMeta.isArray());

        if (columnMeta instanceof WaveColumnMeta) {
            WaveColumnMeta wcm = (WaveColumnMeta) columnMeta;
            logger.log(Level.INFO, "Expr       : {0}", wcm.getExpression());
        }
    }

    private static void dumpHeaderCards(FitsTable table) {
        if (table.hasHeaderCards()) {
            final String hduId = getHduId(table);

            DumpFitsTest.dumpHeaderCards(hduId, table.getHeaderCards());
        }
    }

    public static String getHduId(FitsTable table) {
        return table.getExtName() + '.' + table.getExtNb();
    }
}
