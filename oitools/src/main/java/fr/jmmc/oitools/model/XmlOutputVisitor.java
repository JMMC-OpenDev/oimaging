/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.fits.FitsHeaderCard;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.KeywordMeta;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This visitor implementation produces an XML output of the OIFits file structure
 * @author bourgesl, mella
 */
public final class XmlOutputVisitor implements ModelVisitor {

    /* constants */
    /** US number format symbols */
    private final static DecimalFormatSymbols US_SYMBOLS = new DecimalFormatSymbols(Locale.US);
    /** beautifier number formatter for standard values > 1e-2 and < 1e7 */
    private final static NumberFormat DF_BEAUTY_STD = new DecimalFormat("#0.###", US_SYMBOLS);
    /** beautifier number formatter for other values */
    private final static NumberFormat DF_BEAUTY_SCI = new DecimalFormat("0.###E0", US_SYMBOLS);
    /** regexp expression to SGML entities */
    private final static Pattern patternAMP = Pattern.compile("&");
    /** regexp expression to start tag */
    private final static Pattern patternLT = Pattern.compile("<");
    /** regexp expression to end tag */
    private final static Pattern patternGT = Pattern.compile(">");
    /* members */
    /** flag to enable/disable the number formatter */
    private boolean format;
    /** flag to enable/disable the verbose output */
    private boolean verbose;
    /** internal buffer */
    private StringBuilder buffer;
    /** checker used to store checking messages */
    private final OIFitsChecker checker;

    /**
     * Return one XML string with complete OIFitsFile information
     * @param oiFitsFile OIFitsFile model to process
     * @param verbose if true the result will contain the table content
     * @return the XML description
     */
    public static String getXmlDesc(final OIFitsFile oiFitsFile, final boolean verbose) {
        return getXmlDesc(oiFitsFile, false, verbose);
    }

    /**
     * Return one XML string with complete OIFitsFile information
     * @param oiFitsFile OIFitsFile model to process
     * @param format flag to represent data with less accuracy but a better string representation
     * @param verbose if true the result will contain the table content
     * @return the XML description
     */
    public static String getXmlDesc(final OIFitsFile oiFitsFile, final boolean format, final boolean verbose) {
        final XmlOutputVisitor xmlSerializer = new XmlOutputVisitor(format, verbose);
        oiFitsFile.accept(xmlSerializer);
        return xmlSerializer.toString();
    }

    /**
     * Return one XML string with OITable information
     * @param oiTable OITable model to process
     * @param verbose if true the result will contain the table content
     * @return the XML description
     */
    public static String getXmlDesc(final OITable oiTable, final boolean verbose) {
        return getXmlDesc(oiTable, false, verbose);
    }

    /**
     * Return one XML string with OITable information
     * @param oiTable OITable model to process
     * @param format flag to represent data with less accuracy but a better string representation
     * @param verbose if true the result will contain the table content
     * @return the XML description
     */
    public static String getXmlDesc(final OITable oiTable, final boolean format, final boolean verbose) {
        final XmlOutputVisitor xmlSerializer = new XmlOutputVisitor(format, verbose);
        oiTable.accept(xmlSerializer);
        return xmlSerializer.toString();
    }

    /**
     * Create a new XmlOutputVisitor using default options (not verbose and no formatter used)
     */
    public XmlOutputVisitor() {
        this(false, false);
    }

    /**
     * Create a new XmlOutputVisitor with verbose output i.e. with table data (no formatter used)
     * @param verbose if true the result will contain the table content
     */
    public XmlOutputVisitor(final boolean verbose) {
        this(false, verbose);
    }

    /**
     * Create a new XmlOutputVisitor with verbose output i.e. with table data (no formatter used)
     * @param format flag to represent data with less accuracy but a better string representation
     * @param verbose if true the result will contain the table content
     */
    public XmlOutputVisitor(final boolean format, final boolean verbose) {
        this(format, verbose, null);
    }

    /**
     * Create a new XmlOutputVisitor with verbose output i.e. with table data (no formatter used)
     * @param format flag to represent data with less accuracy but a better string representation
     * @param verbose if true the result will contain the table content
     * @param checker optional OIFitsChecker to dump its report
     */
    public XmlOutputVisitor(final boolean format, final boolean verbose, final OIFitsChecker checker) {
        this.format = format;
        this.verbose = verbose;
        this.checker = checker;

        // allocate buffer size (32K or 128K):
        this.buffer = new StringBuilder(((verbose) ? 128 : 32) * 1024);
    }

    /**
     * Return the flag to enable/disable the number formatter
     * @return flag to enable/disable the number formatter
     */
    public boolean isFormat() {
        return format;
    }

    /**
     * Define the flag to enable/disable the number formatter
     * @param format flag to enable/disable the number formatter
     */
    public void setFormat(final boolean format) {
        this.format = format;
    }

    /**
     * Return the flag to enable/disable the verbose output
     * @return flag to enable/disable the verbose output
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Define the flag to enable/disable the verbose output
     * @param verbose flag to enable/disable the verbose output
     */
    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Clear the internal buffer for later reuse
     */
    public void reset() {
        // recycle buffer :
        this.buffer.setLength(0);
    }

    /**
     * Return the buffer content as a string
     * @return buffer content
     */
    @Override
    public String toString() {
        final String result = this.buffer.toString();

        // reset the buffer content
        reset();

        return result;
    }

    /**
     * Process the given OIFitsFile element with this visitor implementation :
     * fill the internal buffer with file information
     * @param oiFitsFile OIFitsFile element to visit
     */
    @Override
    public void visit(final OIFitsFile oiFitsFile) {

        enterOIFitsFile(oiFitsFile);

        // force verbosity to true for OIArray / OIWaveLength tables (to dump their data):
        final boolean verbosity = this.verbose;
        this.verbose = true;

        String[] strings;

        // arrnames
        strings = oiFitsFile.getAcceptedArrNames();
        for (int i = 0, len = strings.length; i < len; i++) {
            final OITable oiTable = oiFitsFile.getOiArray(strings[i]);
            if (oiTable != null) {
                oiTable.accept(this);
            }
        }

        // insnames
        strings = oiFitsFile.getAcceptedInsNames();
        for (int i = 0, len = strings.length; i < len; i++) {
            final OITable oiTable = oiFitsFile.getOiWavelength(strings[i]);
            if (oiTable != null) {
                oiTable.accept(this);
            }
        }

        // targets
        final OITarget oiTarget = oiFitsFile.getOiTarget();
        if (oiTarget != null) {
            oiTarget.accept(this);
            printMetadata(oiFitsFile);
        }

        // restore verbosity :
        this.verbose = verbosity;

        // data tables
        for (final OIData oiData : oiFitsFile.getOiDataList()) {
            oiData.accept(this);
        }

        // report check message if one checker was given
        if (checker != null) {
            this.buffer.append("<checkReport>\n").append(checker.getCheckReport()).append("\n</checkReport>");
        }

        exitOIFitsFile();
    }

    /**
     * Open the oifits tag with OIFitsFile description
     * @param oiFitsFile OIFitsFile to get its description (file name)
     */
    private void enterOIFitsFile(final OIFitsFile oiFitsFile) {
        this.buffer.append("<oifits>\n");
        if (oiFitsFile != null && oiFitsFile.getAbsoluteFilePath() != null) {
            if(oiFitsFile.getSourceURI() == null){
                this.buffer.append("<filename>").append(oiFitsFile.getAbsoluteFilePath()).append("</filename>\n");                
            }else{
                this.buffer.append("<filename>").append(oiFitsFile.getSourceURI().toString()).append("</filename>\n");                                                
            }
            this.buffer.append("<local_filename>").append(oiFitsFile.getAbsoluteFilePath()).append("</local_filename>\n");            
            this.buffer.append("<size>").append(oiFitsFile.getSize()).append("</size>\n");
        }
    }

    /**
     * Close the oifits tag
     */
    private void exitOIFitsFile() {
        this.buffer.append("</oifits>\n");
    }

    /**
     * Process the given OITable element with this visitor implementation :
     * fill the internal buffer with table information
     * @param oiTable OITable element to visit
     */
    @Override
    public void visit(final OITable oiTable) {

        final boolean doOIFitsFile = (this.buffer.length() == 0);

        if (doOIFitsFile) {
            enterOIFitsFile(oiTable.getOIFitsFile());
        }

        this.buffer.append("<").append(oiTable.getExtName()).append(">\n");

        // Print keywords
        this.buffer.append("<keywords>\n");

        Object val;
        for (final KeywordMeta keyword : oiTable.getKeywordDescCollection()) {
            val = oiTable.getKeywordValue(keyword.getName());
            // skip missing keywords :
            if (val != null) {
                this.buffer.append("<keyword><name>").append(keyword.getName()).append("</name><value>").append(val);
                this.buffer.append("</value><description>").append(keyword.getDescription()).append("</description><type>");
                this.buffer.append(keyword.getType()).append("</type><unit>").append(keyword.getUnit()).append("</unit></keyword>\n");
            }
        }
        // Extra keywords:
        if (oiTable.hasHeaderCards()) {
            String str;
            for (final FitsHeaderCard card : oiTable.getHeaderCards()) {
                this.buffer.append("<keyword><name>").append(card.getKey()).append("</name><value>");
                str = card.getValue();
                if (str != null) {
                    this.buffer.append(encodeTagContent(str));
                }
                this.buffer.append("</value><description>");
                str = card.getComment();
                if (str != null) {
                    this.buffer.append(encodeTagContent(str));
                }
                this.buffer.append("</description><type>A</type><unit></unit></keyword>\n");
            }
        }
        this.buffer.append("</keywords>\n");

        // Print nb of rows
        this.buffer.append("<rows>").append(oiTable.getNbRows()).append("</rows>");

        // Print columns
        this.buffer.append("<columns>\n");

        final Collection<ColumnMeta> columnsDescCollection = oiTable.getColumnDescCollection();
        for (ColumnMeta column : columnsDescCollection) {
            if (oiTable.hasColumn(column)) {
                this.buffer.append("<column><name>").append(column.getName()).append("</name>");
                this.buffer.append("<description>").append(column.getDescription()).append("</description>");
                this.buffer.append("<type>").append(column.getType()).append("</type>");
                this.buffer.append("<unit>").append(column.getUnit()).append("</unit>");

                Object range = oiTable.getMinMaxColumnValue(column.getName());
                if (range != null) {

                    switch (column.getDataType()) {
                        case TYPE_CHAR:
                            // Not Applicable
                            break;

                        case TYPE_INT:
                            int[] irange = (int[]) range;
                            this.buffer.append("<min>").append(irange[0]).append("</min>");
                            this.buffer.append("<max>").append(irange[1]).append("</max>");
                            break;

                        case TYPE_DBL:
                            double[] drange = (double[]) range;
                            this.buffer.append("<min>").append(drange[0]).append("</min>");
                            this.buffer.append("<max>").append(drange[1]).append("</max>");
                            break;

                        case TYPE_REAL:
                            float[] frange = (float[]) range;
                            this.buffer.append("<min>").append(frange[0]).append("</min>");
                            this.buffer.append("<max>").append(frange[1]).append("</max>");
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

                this.buffer.append("</column>\n");
            }
        }

        this.buffer.append("</columns>\n");

        if (this.verbose) {
            this.buffer.append("<table>\n<tr>\n");

            for (ColumnMeta column : columnsDescCollection) {
                if (oiTable.hasColumn(column)) {
                    this.buffer.append("<th>").append(column.getName()).append("</th>");
                }
            }
            this.buffer.append("</tr>\n");

            for (int rowIndex = 0, len = oiTable.getNbRows(); rowIndex < len; rowIndex++) {
                this.buffer.append("<tr>");

                for (ColumnMeta column : columnsDescCollection) {
                    if (oiTable.hasColumn(column)) {
                        this.buffer.append("<td>");

                        this.dumpColumnRow(oiTable, column, rowIndex);

                        this.buffer.append("</td>");
                    }
                }
                this.buffer.append("</tr>\n");
            }

            this.buffer.append("</table>\n");
        }
        this.buffer.append("</").append(oiTable.getExtName()).append(">\n");

        if (doOIFitsFile) {
            exitOIFitsFile();
        }
    }

    /**
     * Append the string representation (String or array) of the column value at the given row index
     * @param oiTable OITable element to use
     * @param column column descriptor
     * @param rowIndex row index
     */
    private void dumpColumnRow(final OITable oiTable, final ColumnMeta column, final int rowIndex) {
        switch (column.getDataType()) {
            case TYPE_CHAR:
                final String[] sValues = oiTable.getColumnString(column.getName());
                // append value :
                this.buffer.append(sValues[rowIndex]);
                break;

            case TYPE_INT:
                if (column.isArray()) {
                    final short[][] iValues = oiTable.getColumnShorts(column.getName());
                    final short[] rowValues = iValues[rowIndex];
                    // append values :
                    for (int i = 0, len = rowValues.length; i < len; i++) {
                        if (i > 0) {
                            this.buffer.append(" ");
                        }
                        this.buffer.append(rowValues[i]);
                    }
                    break;
                }
                final short[] iValues = oiTable.getColumnShort(column.getName());
                // append value :
                this.buffer.append(iValues[rowIndex]);
                break;

            case TYPE_DBL:
                if (column.isArray()) {
                    final double[][] dValues = oiTable.getColumnDoubles(column.getName());
                    final double[] rowValues = dValues[rowIndex];
                    // append values :
                    for (int i = 0, len = rowValues.length; i < len; i++) {
                        if (i > 0) {
                            this.buffer.append(" ");
                        }
                        if (this.format) {
                            this.buffer.append(format(rowValues[i]));
                        } else {
                            this.buffer.append(rowValues[i]);
                        }
                    }
                    break;
                }
                final double[] dValues = oiTable.getColumnDouble(column.getName());
                // append value :
                if (this.format) {
                    this.buffer.append(format(dValues[rowIndex]));
                } else {
                    this.buffer.append(dValues[rowIndex]);
                }
                break;

            case TYPE_REAL:
                if (column.isArray()) {
                    // Impossible case in OIFits
                    this.buffer.append("...");
                    break;
                }
                final float[] fValues = oiTable.getColumnFloat(column.getName());
                // append value :
                if (this.format) {
                    this.buffer.append(format(fValues[rowIndex]));
                } else {
                    this.buffer.append(fValues[rowIndex]);
                }
                break;

            case TYPE_COMPLEX:
                // Special case for complex visibilities :
                if (column.isArray()) {
                    final float[][][] cValues = oiTable.getColumnComplexes(column.getName());
                    final float[][] rowValues = cValues[rowIndex];
                    // append values :
                    for (int i = 0, len = rowValues.length; i < len; i++) {
                        if (i > 0) {
                            this.buffer.append(" ");
                        }
                        // real,img pattern for complex values :
                        if (this.format) {
                            this.buffer.append(format(rowValues[i][0])).append(",").append(format(rowValues[i][1]));
                        } else {
                            this.buffer.append(rowValues[i][0]).append(",").append(rowValues[i][1]);
                        }
                    }
                    break;
                }
                // Impossible case in OIFits
                this.buffer.append("...");
                break;

            case TYPE_LOGICAL:
                if (column.isArray()) {
                    final boolean[][] bValues = oiTable.getColumnBooleans(column.getName());
                    final boolean[] rowValues = bValues[rowIndex];
                    // append values :
                    for (int i = 0, len = rowValues.length; i < len; i++) {
                        if (i > 0) {
                            this.buffer.append(" ");
                        }
                        if (this.format) {
                            if (rowValues[i]) {
                                this.buffer.append("T");
                            } else {
                                this.buffer.append("F");
                            }
                        } else {
                            this.buffer.append(rowValues[i]);
                        }
                    }
                    break;
                }
                // Impossible case in OIFits
                this.buffer.append("...");
                break;

            default:
                // Bad type
                this.buffer.append("...");
        }
    }

    /**
     * Format the given number using the beautifier formatter
     * @param value any float or double value
     * @return string representation
     */
    private static String format(final double value) {
        final double v = (value >= 0d) ? value : -value;
        if (v == 0d) {
            return "0";
        }
        if (Double.isNaN(value)) {
            return "NaN";
        }
        if (v > 1e-2d && v < 1e7d) {
            synchronized (DF_BEAUTY_STD) {
                return DF_BEAUTY_STD.format(value);
            }
        }
        synchronized (DF_BEAUTY_SCI) {
            return DF_BEAUTY_SCI.format(value);
        }
    }

    /**
     * Encode special characters to entities
     * @see jmcs StringUtils.encodeTagContent(String)
     * 
     * @param src input string
     * @return encoded value
     */
    public static String encodeTagContent(final String src) {
        String out = patternAMP.matcher(src).replaceAll("&amp;"); // Character [&] (xml restriction)
        out = patternLT.matcher(out).replaceAll("&lt;"); // Character [<] (xml restriction)
        out = patternGT.matcher(out).replaceAll("&gt;"); // Character [>] (xml restriction)
        return out;
    }

    private void printMetadata(final OIFitsFile oiFitsFile) {
        /* analyze structure of file to browse by target */
        oiFitsFile.analyze();
        this.buffer.append("<metadata>\n");
        for (int i = 0; i < oiFitsFile.getOiTarget().getNbTargets(); i++) {
            this.buffer.append(targetMetadata(oiFitsFile, i));
        }
        this.buffer.append("</metadata>\n");
    }

    private StringBuilder targetMetadata(final OIFitsFile oiFitsFile, final int index) {
        final StringBuilder target = new StringBuilder(1024);

        final OITarget oiTarget = oiFitsFile.getOiTarget();
        
        final short targetId = oiTarget.getTargetId()[index];
        final String targetName = oiTarget.getTarget()[index];
        final double targetRa = oiTarget.getRaEp0()[index];
        final double targetDec = oiTarget.getDecEp0()[index];

        for (final String insName : oiFitsFile.getAcceptedInsNames()) {
            double resPower = 0d;

            /* data from the OIWavelength */
            final OIWavelength oiWavelength = oiFitsFile.getOiWavelength(insName);
            float[] frange = (float[]) oiWavelength.getMinMaxColumnValue(OIFitsConstants.COLUMN_EFF_WAVE);
            /* TODO: wavelength for flagged values ? */
            float minWavelength = frange[0];
            float maxWavelength = frange[1];
            int nbChannels = oiWavelength.getNWave();

            /* build a list of different night ids for (target,insname) */
            final Map<Double, HashSet<OIData>> oiDataPerNightId = new HashMap<Double, HashSet<OIData>>();
            for (final OIData oiData : oiFitsFile.getOiDataList()) {
                if (oiData.getInsName().equals(insName)) {
                    final short[] targetIds = oiData.getTargetId();
                    final double[] nightIds = oiData.getNightId();

                    for (int i = 0; i < targetIds.length; i++) {
                        /* TODO: target aliases */
                        if (targetIds[i] == targetId) {
                            final double nightId = nightIds[i];
                            HashSet<OIData> tables = oiDataPerNightId.get(nightId);
                            if (tables == null) {
                                tables = new HashSet<OIData>();
                                oiDataPerNightId.put(nightId, tables);
                            }
                            tables.add(oiData);
                        }
                    }
                }
            }

            for (Map.Entry<Double, HashSet<OIData>> entry : oiDataPerNightId.entrySet()) {
                double nightId = entry.getKey();
                HashSet<OIData> oiDataTables = entry.getValue();

                int nbVis = 0, nbVis2 = 0, nbT3 = 0;
                double tMin = Double.POSITIVE_INFINITY, tMax = Double.NEGATIVE_INFINITY;
                double intTime = Double.POSITIVE_INFINITY;
                String facilityName = "";

                for (OIData oiData : oiDataTables) {
                    /* one oiData table, search for target by targetid (and nightid) */
                    short idx = 0;
                    for (short id : oiData.getTargetId()) {
                        if (id == targetId && oiData.getNightId()[idx] == nightId) {
                            // TODO: count flag? what to do with flagged measures?
                            if (oiData instanceof OIVis) {
                                nbVis += 1;
                            } else if (oiData instanceof OIVis2) {
                                nbVis2 += 1;
                            } else if (oiData instanceof OIT3) {
                                nbT3 += 1;
                            }

                            /* search for minimal and maximal MJD for target */
                            /* TODO: make use of DATE-OBS+TIME[idx] if no MJD */
                            double mjd = oiData.getMjd()[idx];
                            if (mjd < tMin) {
                                tMin = mjd;
                            }
                            if (mjd > tMax) {
                                tMax = mjd;
                            }

                            /* search for minimal (?) INT_TIME for target */
                            double t = oiData.getIntTime()[idx];
                            if (t < intTime) {
                                intTime = t;
                            }
                        }
                        idx++;

                        if (facilityName.isEmpty() && oiData.getArrName() != null) {
                            facilityName = oiData.getArrName();
                        }
                    }
                }

                /* the target has been observed by the current instrument */
                target.append("  <target>\n")
                        .append("    <target_name>").append(targetName).append("</target_name>\n")
                        .append("    <s_ra>").append(targetRa).append("</s_ra>\n")
                        .append("    <s_dec>").append(targetDec).append("</s_dec>\n")
                        .append("    <t_exptime>").append(intTime).append("</t_exptime>\n")
                        .append("    <t_min>").append(tMin).append("</t_min>\n")
                        .append("    <t_max>").append(tMax).append("</t_max>\n")
                        .append("    <em_res_power>").append(resPower).append("</em_res_power>\n")
                        .append("    <facility_name>").append(facilityName).append("</facility_name>\n")
                        .append("    <instrument_name>").append(insName).append("</instrument_name>\n")
                        .append("    <nb_vis>").append(nbVis).append("</nb_vis>\n")
                        .append("    <nb_vis2>").append(nbVis2).append("</nb_vis2>\n")
                        .append("    <nb_t3>").append(nbT3).append("</nb_t3>\n")
                        .append("    <nb_channels>").append(nbChannels).append("</nb_channels>\n")
                        .append("    <em_min>").append(minWavelength).append("</em_min>\n")
                        .append("    <em_max>").append(maxWavelength).append("</em_max>\n")
                        .append("  </target>\n");
            }
        }

        return target;
    }
}
