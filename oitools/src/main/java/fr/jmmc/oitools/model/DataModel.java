/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.image.FitsImageHDU;
import fr.jmmc.oitools.meta.CellMeta;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.DataRange;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.OIFitsStandard;
import fr.jmmc.oitools.meta.Units;
import fr.jmmc.oitools.meta.WaveColumnMeta;
import static fr.jmmc.oitools.model.XmlOutputVisitor.encodeTagContent;
import fr.jmmc.oitools.util.FileUtils;
import fr.nom.tam.fits.FitsException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

/**
 *
 * @author bourgesl, mellag
 */
public class DataModel {

    /**
     * Logger associated to test classes
     */
    public final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(DataModel.class.getName());

    // members:
    private final static short MAGIC_TARGET_ID = -4;
    private final static short MAGIC_STA_INDEX = -6;

    /** 
     * flag to globally support or not additional columns for Image Reconstruction Imaging (disabled by default)
     * @see https://github.com/emmt/OI-Imaging-JRA
     */
    private static boolean oiModelColumnsSupport = false;
    /** flag to globally support OI_VIS Complex visibility columns (disabled by default) */
    private static boolean oiVisComplexSupport = false;

    private static final ArrayList<Rule> SORTED_RULES = new ArrayList<Rule>(64);
    private static final ArrayList<String> SORTED_SOURCES = new ArrayList<String>(64);

    /**
     * Get support of additional columns for Image Reconstruction Imaging.
     * @return true if columns are present in the datamodel else false.
     */
    public static boolean hasOiModelColumnsSupport() {
        return oiModelColumnsSupport;
    }

    /**
     * Set support of additional columns for Image Reconstruction Imaging.
     *
     * @param oiModelColumnsSupport true will include optional columns in the datamodel, else will ignore them.
     */
    public static void setOiModelColumnsSupport(boolean oiModelColumnsSupport) {
        DataModel.oiModelColumnsSupport = oiModelColumnsSupport;
    }

    public static boolean hasOiVisComplexSupport() {
        return oiVisComplexSupport;
    }

    public static void setOiVisComplexSupport(boolean oiVisComplexSupport) {
        DataModel.oiVisComplexSupport = oiVisComplexSupport;
    }

    public static void dumpDataModelV1() throws IOException, MalformedURLException, FitsException {
        final int nRows = 1;
        final int nWLen = 1;

        //load file V1
        String absFilePath = "src/test/resources/oifits/TEST_CreateOIFileV1.fits";
        OIFitsLoader.loadOIFits(absFilePath);

        // fake data model:
        final OIFitsFile oiFitsFile = new OIFitsFile(OIFitsStandard.VERSION_1);

        // OITarget:
        final OITarget oiTarget = new OITarget(oiFitsFile, nRows);
        oiTarget.getTargetId()[0] = MAGIC_TARGET_ID;
        oiFitsFile.addOiTable(oiTarget);

        // OIArray:
        final String arrName = "[[ARRNAME]]";
        final OIArray oiArray = new OIArray(oiFitsFile, 1);
        oiArray.setArrName(arrName);
        oiArray.getStaIndex()[0] = MAGIC_STA_INDEX;
        oiFitsFile.addOiTable(oiArray);

        // OIWavelength:
        final String insName = "[[INSNAME]]";
        final OIWavelength oiWaveLength = new OIWavelength(oiFitsFile, nWLen);
        oiWaveLength.setInsName(insName);
        oiFitsFile.addOiTable(oiWaveLength);

        // Data:
        // OIVis:
        final OIVis oiVis = new OIVis(oiFitsFile, insName, nRows);
        oiVis.setArrName(arrName);
        oiFitsFile.addOiTable(oiVis);

        final OIVis2 oiVis2 = new OIVis2(oiFitsFile, insName, nRows);
        oiVis2.setArrName(arrName);
        oiFitsFile.addOiTable(oiVis2);

        final OIT3 oit3 = new OIT3(oiFitsFile, insName, nRows);
        oit3.setArrName(arrName);
        oiFitsFile.addOiTable(oit3);

        writeDataModel(oiFitsFile, "rules/DataModelV1.xml");
    }

    public static void dumpDataModelV2() throws IOException, FitsException {
        final int nRows = 1;
        final int nWLen = 1;

        //load file
        String absFilePath = "src/test/resources/oifits/TEST_CreateOIFileV2.fits";
        OIFitsLoader.loadOIFits(absFilePath);

        // fake data model:
        final OIFitsFile oiFitsFile = new OIFitsFile(OIFitsStandard.VERSION_2);

        //PrimaryHDU:
        final OIPrimaryHDU imageHDU = new OIPrimaryHDU();
        imageHDU.setOrigin("ESO");
        imageHDU.setDate("2017-12-06");
        oiFitsFile.setPrimaryImageHdu(imageHDU);

        // OITarget:
        final OITarget oiTarget = new OITarget(oiFitsFile, nRows);
        oiTarget.getTargetId()[0] = MAGIC_TARGET_ID;
        oiFitsFile.addOiTable(oiTarget);

        // OIArray:
        final String arrName = "[[ARRNAME]]";
        final OIArray oiArray = new OIArray(oiFitsFile, 1);
        oiArray.setArrName(arrName);
        oiArray.getStaIndex()[0] = MAGIC_STA_INDEX;
        oiFitsFile.addOiTable(oiArray);

        // OIWavelength:
        final String insName = "[[INSNAME]]";
        final OIWavelength oiWaveLength = new OIWavelength(oiFitsFile, nWLen);
        oiWaveLength.setInsName(insName);
        oiFitsFile.addOiTable(oiWaveLength);

        // OICorr:
        final String corrname = "[[CORRNAME]]";
        final OICorr oicorr = new OICorr(oiFitsFile, nRows);
        oicorr.setCorrName(corrname);
        oiFitsFile.addOiTable(oicorr);

        // OIInspol:
        final OIInspol oiinspol = new OIInspol(oiFitsFile, arrName, nRows, nWLen);
        oiFitsFile.addOiTable(oiinspol);

        // Data:
        // OIVis:
        final OIVis oiVis = new OIVis(oiFitsFile, insName, nRows);
        oiVis.setArrName(arrName);
        oiVis.setCorrName(corrname);
        oiFitsFile.addOiTable(oiVis);

        final OIVis2 oiVis2 = new OIVis2(oiFitsFile, insName, nRows);
        oiVis2.setArrName(arrName);
        oiVis2.setCorrName(corrname);
        oiFitsFile.addOiTable(oiVis2);

        final OIT3 oit3 = new OIT3(oiFitsFile, insName, nRows);
        oit3.setArrName(arrName);
        oit3.setCorrName(corrname);
        oiFitsFile.addOiTable(oit3);

        final OISpectrum oispect = new OISpectrum(oiFitsFile, insName, nRows);
        oispect.setArrName(arrName);
        oispect.setCorrName(corrname);
        oiFitsFile.addOiTable(oispect);

        writeDataModel(oiFitsFile, "rules/DataModelV2.xml");
    }

    private static void writeDataModel(final OIFitsFile oiFitsFile, final String file) throws IOException {

        final OIFitsChecker checker = new OIFitsChecker();
        oiFitsFile.check(checker);

        // validation results
        logger.log(Level.INFO, "validation results\n{0}", checker.getCheckReport());

        final StringBuilder sb = new StringBuilder(32 * 1024);
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");

        sb.append("<datamodel>\n");

        writeRules(sb, oiFitsFile);

        if (oiFitsFile.getPrimaryImageHDU() != null) {
            dumpHDU(oiFitsFile.getPrimaryImageHDU(), sb);
        }
        dumpTables(oiFitsFile.getOITableList(), sb);

        sb.append("</datamodel>\n");

        logger.log(Level.INFO, "DataModel: \n{0}", sb);

        FileUtils.writeFile(new File(file), sb.toString());
    }

    public static void dumpHDU(final FitsImageHDU imageHDU, final StringBuilder sb) {
        sb.append("<table name=\"PrimaryHDU\">\n");
        for (KeywordMeta keyword : imageHDU.getKeywordDescCollection()) {
            dumpKeyword(keyword, sb);
        }
        sb.append("</table>\n");
    }

    public static void dumpTables(final List<OITable> tables, final StringBuilder sb) {

        for (OITable table : tables) {
            sb.append("<table name=\"").append(table.getExtName()).append("\">\n");

            if (table.getApplyRules() != null) {
                sb.append("  <rules>\n");

                SORTED_RULES.clear();
                SORTED_RULES.addAll(table.getApplyRules());
                Collections.sort(SORTED_RULES, Rule.getComparatorByName());

                for (Rule rule : SORTED_RULES) {
                    sb.append("    <applyrule>").append(rule.name()).append("</applyrule>\n");
                }
                SORTED_RULES.clear();

                sb.append("  </rules>\n");
            }

            for (KeywordMeta keyword : table.getKeywordDescCollection()) {
                dumpKeyword(keyword, sb);
            }

            for (ColumnMeta column : table.getColumnDescCollection()) {
                dumpColumn(column, sb);
            }

            sb.append("</table>\n");
        }
    }

    public static void dumpKeyword(final KeywordMeta keyword, final StringBuilder sb) {
        sb.append("  <keyword>\n");
        dumpMeta(keyword, sb);
        sb.append("  </keyword>\n");
    }

    public static void dumpColumn(final ColumnMeta column, final StringBuilder sb) {
        sb.append("  <column>\n");
        dumpMeta(column, sb);
        // specific members :

        if (column.getAlias() != null) {
            sb.append("    <alias>").append(column.getAlias()).append("</alias>\n");
        }
        sb.append("    <repeat>");
        if (column instanceof WaveColumnMeta) {
            sb.append("[[NWAVE");
            if (column.is3D()) {
                sb.append(",NWAVE");
            }
            sb.append("]]");
        } else {
            sb.append(column.getRepeat());
        }
        sb.append("</repeat>\n");

        sb.append("    <array>").append(column.isArray()).append("</array>\n");

        if (column.getErrorColumnName() != null) {
            sb.append("    <errorcolumn>").append(column.getErrorColumnName()).append("</errorcolumn>\n");
        }
        if (column.getDataRange() != null) {
            sb.append("    <datarange>\n");
            final DataRange range = column.getDataRange();

            sb.append("      <name>").append(range.getName()).append("</name>\n");
            final double min = range.getMin();
            if (!Double.isNaN(min)) {
                sb.append("      <min>").append(min).append("</min>\n");
            }
            final double max = range.getMax();
            if (!Double.isNaN(max)) {
                sb.append("      <max>").append(max).append("</max>\n");
            }
            sb.append("    </datarange>\n");
        }

        if (column instanceof WaveColumnMeta) {
            final WaveColumnMeta waveColumnMeta = (WaveColumnMeta) column;
            if (waveColumnMeta.getExpression() != null) {
                sb.append("    <expression>").append(encodeTagContent(waveColumnMeta.getExpression())).append("</expression>\n");
            }
        }
        sb.append("  </column>\n");
    }

    public static void dumpMeta(final CellMeta meta, final StringBuilder sb) {
        sb.append("    <name>").append(meta.getName()).append("</name>\n");
        sb.append("    <datatype>").append(meta.getDataType()).append("</datatype>\n");
        sb.append("    <description>").append(encodeTagContent(meta.getDescription())).append("</description>\n");
        sb.append("    <optional>").append(meta.isOptional()).append("</optional>\n");

        if (meta.getUnits() != Units.NO_UNIT) {
            sb.append("    <unit>").append(meta.getUnits()).append("</unit>\n");
        }

        final short[] intAcceptedValues = meta.getIntAcceptedValues();

        if (intAcceptedValues.length != 0) {
            sb.append("    <values>\n");

            for (int i = 0, len = intAcceptedValues.length; i < len; i++) {
                sb.append("      <short>");
                switch (intAcceptedValues[i]) {
                    case MAGIC_TARGET_ID:
                        sb.append("[[REF::OI_TARGET.TARGET_ID]");
                        break;
                    case MAGIC_STA_INDEX:
                        sb.append("[[REF::OI_ARRAY.STA_INDEX]");
                        break;
                    default:
                        sb.append(intAcceptedValues[i]);
                }
                sb.append("</short>\n");
            }

            sb.append("    </values>\n");
        }

        final String[] stringAcceptedValues = meta.getStringAcceptedValues();

        if (stringAcceptedValues.length != 0) {
            sb.append("    <values>\n");

            for (String stringAcceptedValue : stringAcceptedValues) {
                sb.append("      <string>").append(encodeTagContent(stringAcceptedValue)).append("</string>\n");
            }

            sb.append("    </values>\n");
        }

        if (meta.getApplyRules() != null) {
            sb.append("    <rules>\n");

            SORTED_RULES.clear();
            SORTED_RULES.addAll(meta.getApplyRules());
            Collections.sort(SORTED_RULES, Rule.getComparatorByName());

            for (Rule rule : SORTED_RULES) {
                sb.append("      <applyrule>").append(rule.name()).append("</applyrule>\n");
            }
            SORTED_RULES.clear();

            sb.append("    </rules>\n");
        }
    }

    private static void writeRules(final StringBuilder sb, final OIFitsFile oiFitsFile) {
        sb.append("<rules>\n");

        final OIFitsStandard ignore = (!oiFitsFile.isOIFits2()) ? OIFitsStandard.VERSION_2 : null;

        for (Rule rule : Rule.values()) {
            if (rule.getStandard().isEmpty()) {
                logger.warning("Missing standard set for " + rule);
            } else if (rule.getStandard().contains(oiFitsFile.getVersion())) {
                dumpRules(rule, sb, ignore);
            }
        }
        sb.append("</rules>\n");
    }

    public static void dumpRules(final Rule rule, final StringBuilder sb, final OIFitsStandard ignore) {
        sb.append("  <rule>\n");
        sb.append("    <name>").append(rule.name()).append("</name>\n");
        sb.append("    <description>").append(encodeTagContent(rule.getDescription())).append("</description>\n");
        sb.append("    <paragraph>").append(rule.getParagraph()).append("</paragraph>\n");

        sb.append("    <subjects>\n");

        SORTED_SOURCES.clear();
        SORTED_SOURCES.addAll(rule.getApplyTo());
        Collections.sort(SORTED_SOURCES);

        for (String applyTo : SORTED_SOURCES) {
            sb.append("      <apply>").append(applyTo).append("</apply>\n");
        }
        SORTED_SOURCES.clear();

        sb.append("    </subjects>\n");

        sb.append("    <standards>\n");
        for (OIFitsStandard standard : rule.getStandard()) {
            if (standard != ignore) {
                sb.append("      <standard>").append(standard).append("</standard>\n");
            }
        }
        sb.append("    </standards>\n");
        sb.append("  </rule>\n");
    }

    public static void main(String[] unused) {
        // Only standard columns in datamodel xml files:
        setOiModelColumnsSupport(false);
        setOiVisComplexSupport(false);

        OIFitsChecker.setInspectRules(true);
        try {
            dumpDataModelV1();
            dumpDataModelV2();
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "IO failure", ioe);
        } catch (FitsException ex) {
            logger.log(Level.SEVERE, "Fits File failure", ex);
        } finally {
            OIFitsChecker.setInspectRules(false);
        }
    }
}
