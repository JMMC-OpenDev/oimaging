/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools;

import static fr.jmmc.oitools.AbstractFileBaseTest.reset;
import static fr.jmmc.oitools.DumpOIFitsTest.FILE_VALIDATION;
import static fr.jmmc.oitools.DumpOIFitsTest.computeCustomExp;
import static fr.jmmc.oitools.DumpOIFitsTest.getHduId;
import static fr.jmmc.oitools.JUnitBaseTest.logger;
import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.meta.ColumnMeta;
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
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Load OIFits files from the test/oifits folder and load properties file from test/ref (reference files)
 * to compare the complete OIFITSFile structure with the stored (key / value) pairs.
 * @author kempsc
 */
public class LoadOIFitsTest extends AbstractFileBaseTest {

    // members:
    private static OIFitsFile OIFITS = null;

    @BeforeClass
    public static void setUpClass() {
        initializeTest();
    }

    @AfterClass
    public static void tearDownClass() {
        OIFITS = null;
        shutdownTest();
    }

    @Test
    public void compareFiles() throws IOException, FitsException {

        final OIFitsChecker checker = new OIFitsChecker();
        try {
            for (String f : getFitsFiles(new File(TEST_DIR_OIFITS))) {

                // reset properties anyway
                reset();

                compareOIFits(checker, f);

                checkAssertCount();
            }
        } finally {
            // validation results
            logger.log(Level.INFO, "validation results\n{0}", checker.getCheckReport());
        }

        // reset properties anyway
        reset();

        // Load property file to map
        load(new File(TEST_DIR_REF, FILE_VALIDATION));

        assertEqualsInt(get("SEVERE.COUNT"), checker.getNbSeveres());
        assertEqualsInt(get("WARNING.COUNT"), checker.getNbWarnings());
//        assertEquals(get("REPORT"), checker.getCheckReport());
    }

    private void compareOIFits(final OIFitsChecker checker, String f) throws IOException, FitsException {

        OIFITS = OIFitsLoader.loadOIFits(checker, f);
        OIFITS.analyze();

        // Load property file to map
        load(new File(TEST_DIR_REF, OIFITS.getFileName() + ".properties"));

        computeCustomExp(OIFITS.getOiDatas());

        LoadFitsTest.compare(OIFITS.getPrimaryImageHDU());

        assertEquals(get("FILENAME"), OIFITS.getFileName());
        assertEqualsInt(get("OI_ARRAY.COUNT"), OIFITS.getNbOiArrays());
        assertEqualsInt(get("OI_TARGET.COUNT"), OIFITS.hasOiTarget() ? 1 : 0);
        assertEqualsInt(get("OI_WAVELENGTH.COUNT"), OIFITS.getNbOiWavelengths());
        assertEqualsInt(get("OI_VIS.COUNT"), OIFITS.getNbOiVis());
        assertEqualsInt(get("OI_VIS2.COUNT"), OIFITS.getNbOiVis2());
        assertEqualsInt(get("OI_T3.COUNT"), OIFITS.getNbOiT3());

        for (OITable oitable : OIFITS.getOiTables()) {
            compareTable(oitable);
        }
    }

    private static void compareTable(FitsTable table) {

        final String prefix = getHduId(table);

        assertEqualsInt(get(prefix + ".NBRow"), table.getNbRows());

        LoadFitsTest.compareKeywords(table, getHduId(table));
        compareColumns(table);
        compareHeaderCards(table);

        // Specific tests:
        if (table instanceof OIData) {
            compareOIData((OIData) table);
        }
    }

    private static void compareOIData(OIData oidata) {

        final String hduId = getHduId(oidata);
        /** internal buffer */
        StringBuilder buffer = new StringBuilder();

        assertEqualsInt(get(hduId + ".nWave"), oidata.getNWave());
        assertEqualsInt(get(hduId + ".nFlagged"), oidata.getNFlagged());

        Set<short[]> staIndexes = oidata.getDistinctStaIndex();
        for (short[] staIndex : staIndexes) {
            buffer.append(oidata.getStaNames(staIndex));
            buffer.append(", ");
        }
        assertEquals(get(hduId + ".staIndex"), buffer.toString());

        buffer.setLength(0);

        Set<short[]> staConfs = oidata.getDistinctStaConf();
        for (short[] staConf : staConfs) {
            buffer.append(oidata.getStaNames(staConf));
        }
        assertEquals(get(hduId + ".staConf"), buffer.toString());
    }

    private static void compareColumns(FitsTable table) {

        Object expected;
        final String hduId = getHduId(table);
        final String prefixC = hduId + ".C.";
        final String prefixDC = hduId + ".DC.";
        final String prefixMM = hduId + ".MM.";

        for (ColumnMeta columnMeta : table.getAllColumnDescCollection()) {

            if (table.getColumnDerivedDesc(columnMeta.getName()) != null) {

                expected = get(prefixDC + columnMeta.getName());
                compareColumnValues(table, columnMeta, expected);
                // No MinMax for derived columns
            } else {
                expected = get(prefixC + columnMeta.getName());
                compareColumnValues(table, columnMeta, expected);

                expected = get(prefixMM + columnMeta.getName());
                Object minmax = table.getMinMaxColumnValue(columnMeta.getName());
                compareColumnMinmax(minmax, columnMeta, expected);
            }
        }
    }

    private static void compareColumnValues(FitsTable table, ColumnMeta columnMeta, Object expected) {
        String propValue = null;

        switch (columnMeta.getDataType()) {
            case TYPE_CHAR:
                String[] svalue = table.getColumnString(columnMeta.getName());
                propValue = Arrays.deepToString(svalue);
                break;
            case TYPE_SHORT:
                if (columnMeta.isArray()) {
                    short[][] shvalues = table.getColumnAsShorts(columnMeta.getName());
                    propValue = Arrays.deepToString(shvalues);
                } else {
                    short[] shvalue = table.getColumnShort(columnMeta.getName());
                    propValue = Arrays.toString(shvalue);
                }
                break;
            case TYPE_INT:
                if (columnMeta.isArray()) {
                    int[][] ivalues = table.getColumnInts(columnMeta.getName());
                    propValue = Arrays.deepToString(ivalues);
                } else {
                    int[] ivalue = table.getColumnInt(columnMeta.getName());
                    propValue = Arrays.toString(ivalue);
                }
                break;
            case TYPE_DBL:
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
        assertEquals(expected, (propValue != null) ? propValue : "null");

    }

    private static void compareColumnMinmax(Object minmax, ColumnMeta columnMeta, Object expected) {
        String propValue = null;

        if (minmax != null) {
            switch (columnMeta.getDataType()) {
                case TYPE_CHAR:
                    // Not Applicable
                    break;

                case TYPE_SHORT:
                    short[] shrange = (short[]) minmax;
                    propValue = Arrays.toString(shrange);
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

        assertEquals(expected, (propValue != null) ? propValue : "null");
    }

    private static void compareHeaderCards(FitsTable table) {
        if (table.hasHeaderCards()) {
            final String hduId = getHduId(table);

            LoadFitsTest.compareHeaderCards(hduId, table.getHeaderCards());
        }
    }

}
