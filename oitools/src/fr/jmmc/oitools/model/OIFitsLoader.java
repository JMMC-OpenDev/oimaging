/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.fits.FitsConstants;
import fr.jmmc.oitools.fits.FitsHeaderCard;
import fr.jmmc.oitools.fits.FitsUtils;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.meta.Units;
import fr.jmmc.oitools.meta.WaveColumnMeta;
import fr.jmmc.oitools.util.FileUtils;
import fr.nom.tam.fits.BasicHDU;
import fr.nom.tam.fits.BinaryTableHDU;
import fr.nom.tam.fits.Fits;
import fr.nom.tam.fits.FitsException;
import fr.nom.tam.fits.Header;
import fr.nom.tam.fits.HeaderCard;
import fr.nom.tam.util.ArrayFuncs;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * This state-full class loads an OIFits file to the OIFits data model with optional integrity checks
 * @author bourgesl
 */
public class OIFitsLoader {
    /* constants */

    /** Logger associated to meta model classes */
    protected final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(OIFitsLoader.class.getName());

    /* members */
    /** flag to show validation report */
    private final boolean showValidation;
    /** checker */
    private final OIFitsChecker checker;
    /** OIFits data model */
    private OIFitsFile oiFitsFile = null;

    /**
     * Main method to load an OI Fits File
     * @param fileLocation absolute File Path or remote URL
     * @return OIFits data model
     * @throws MalformedURLException invalid url format
     * @throws FitsException if the fits can not be opened
     * @throws IOException IO failure
     */
    public static OIFitsFile loadOIFits(final String fileLocation) throws MalformedURLException, IOException, FitsException {
        return loadOIFits(null, fileLocation);
    }

    /**
     * Main method to load an OI Fits File with the given checker component
     * @param checker checker component
     * @param fileLocation absolute File Path or remote URL
     * @return OIFits data model
     * @throws MalformedURLException invalid url format
     * @throws FitsException if the fits can not be opened
     * @throws IOException IO failure
     */
    public static OIFitsFile loadOIFits(final OIFitsChecker checker, final String fileLocation) throws MalformedURLException, IOException, FitsException {
        String tmpFilename = fileLocation;

        boolean remote = false;
        // If the given file is remote :
        if (fileLocation.contains("://")) {
            // Only remote files are retrieved :
            remote = true;

            // Load it and unarchive it as the Fits library does not manage remote file.
            tmpFilename = FileUtils.saveToFile(fileLocation);
        }

        final OIFitsLoader loader = new OIFitsLoader(checker);

        loader.load(tmpFilename);

        if (remote) {
            // store the URL instead of the temporary file location :
            loader.getOIFitsFile().setAbsoluteFilePath(fileLocation);
        }

        return loader.getOIFitsFile();
    }

    /**
     * Private constructor
     */
    private OIFitsLoader() {
        // How to get back validation results ?
        this(null);
    }

    /**
     * Custom constructor to give a checker instance (multiple file load / validation)
     * @param checker 
     */
    private OIFitsLoader(final OIFitsChecker checker) {
        super();
        if (checker != null) {
            this.checker = checker;
            this.showValidation = false;
        } else {
            this.checker = new OIFitsChecker();
            this.showValidation = true;
        }
    }

    /**
     * Load the given file into the OI Fits data model
     *
     * @param absFilePath absolute File path on file system (not URL)
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     */
    private void load(final String absFilePath) throws FitsException, IOException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("loading " + absFilePath);
        }

        this.checker.info("Loading File: " + absFilePath);

        // Check if the given file exists :
        if (!new File(absFilePath).exists()) {
            this.checker.severe("File not found : " + absFilePath);
            throw new IOException("File not found : " + absFilePath);
        }

        Fits fitsFile = null;
        try {
            // create new OIFits data model
            this.oiFitsFile = new OIFitsFile(absFilePath);

            final long start = System.nanoTime();

            // open the fits file :
            fitsFile = new Fits(absFilePath);

            if (fitsFile != null) {
                // read the complete file structure :
                final BasicHDU[] hdus = fitsFile.read();

                // process all HD units:
                // note: not null means hdus array is not empty!
                if (hdus != null) {
                    this.processHDUnits(hdus);
                }
            }

            if (logger.isLoggable(Level.INFO)) {
                logger.info("load : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");
            }

        } catch (FitsException fe) {
            logger.log(Level.SEVERE, "Unable to load the file : " + absFilePath, fe);
            this.checker.severe("Unable to load the file " + absFilePath + " : " + fe.getMessage());

            throw fe;
        } finally {
            if (fitsFile != null && fitsFile.getStream() != null) {
                try {
                    fitsFile.getStream().close();
                } catch (IOException ioe) {
                    logger.log(Level.FINE, "Closing Fits file", ioe);
                }
            }
        }

        if (this.oiFitsFile.getNbOiTables() == 0) {
            throw new FitsException("Invalid OIFits format (no OI table found) !");
        }

        // TODO : remove test code :
        this.oiFitsFile.check(this.checker);

        // show validation results
        if (this.showValidation && logger.isLoggable(Level.INFO)) {
            logger.info("validation results\n" + this.checker.getCheckReport());
        }
    }

    /**
     * Process all Fits HD units to load OI_* tables (skip other tables)
     * and check at least one data table is present.
     * @param hdus array of hd unit
     * @throws FitsException if any FITS error occurred
     */
    private void processHDUnits(final BasicHDU[] hdus) throws FitsException {

        final int nbHDU = hdus.length;

        // flag indicating that the HDU was processed:
        final boolean[] processed = new boolean[nbHDU];
        processed[0] = true;

        /*
         * TODO: process the primary HDU to load all keywords (ESO HIERARCH ...) ?
         *
         *   // enable ESO HIERARCH keyword support in nom.tam Fits library :
         *   FitsFactory.setUseHierarch(true);
         */

        // Building process will be done in 2 steps

        // First loop : build and check presence of
        // OI_TARGET, OI_ARRAY, OI_WAVELENGTH tables
        // ie reference tables

        OITable oiTable;
        String extName;
        BasicHDU hdu;
        BinaryTableHDU bh;

        // skip Primary HDU : so start at index = 1
        for (int i = 1; i < nbHDU; i++) {
            hdu = hdus[i];

            extName = hdu.getTrimmedString(FitsConstants.KEYWORD_EXT_NAME);

            if (hdu instanceof BinaryTableHDU) {
                oiTable = null;
                bh = (BinaryTableHDU) hdu;

                if (OIFitsConstants.TABLE_OI_TARGET.equals(extName)) {
                    oiTable = new OITarget(this.oiFitsFile);
                } else if (OIFitsConstants.TABLE_OI_ARRAY.equals(extName)) {
                    oiTable = new OIArray(this.oiFitsFile);
                } else if (OIFitsConstants.TABLE_OI_WAVELENGTH.equals(extName)) {
                    oiTable = new OIWavelength(this.oiFitsFile);
                }

                if (oiTable != null) {
                    // flag HDU:
                    processed[i] = true;

                    // define the extension number :
                    oiTable.setExtNb(i);

                    this.checker.info("Analysing table [" + i + "]: " + extName);

                    // load table :
                    this.processTable(bh, oiTable);

                    // register the table :
                    this.oiFitsFile.registerOiTable(oiTable);
                }
            } else {
                this.checker.severe("Unsupported table [" + i + "]: " + extName + " format: " + hdu.getClass().getSimpleName());
            }
        }

        // Second loop : build and check presence of
        // OI_VIS, OI_VIS2, OI_T3 tables
        boolean hasDataHdu = false;

        // skip Primary HDU : so start at index = 1
        for (int i = 1; i < nbHDU; i++) {
            hdu = hdus[i];

            extName = hdu.getTrimmedString(FitsConstants.KEYWORD_EXT_NAME);

            if (hdu instanceof BinaryTableHDU) {
                oiTable = null;
                bh = (BinaryTableHDU) hdu;

                if (OIFitsConstants.TABLE_OI_VIS.equals(extName)) {
                    oiTable = new OIVis(this.oiFitsFile);
                } else if (OIFitsConstants.TABLE_OI_VIS2.equals(extName)) {
                    oiTable = new OIVis2(this.oiFitsFile);
                } else if (OIFitsConstants.TABLE_OI_T3.equals(extName)) {
                    oiTable = new OIT3(this.oiFitsFile);
                }
                if (oiTable != null) {
                    // flag HDU:
                    processed[i] = true;

                    // define the extension number :
                    oiTable.setExtNb(i);

                    this.checker.info("Analysing table [" + i + "]: " + extName);

                    // load table :
                    this.processTable(bh, oiTable);

                    this.oiFitsFile.registerOiTable(oiTable);

                    hasDataHdu = true;
                }
            }
        }

        // report any non-standard OIFits HDU:
        for (int i = 0; i < nbHDU; i++) {
            if (!processed[i]) {
                hdu = hdus[i];

                extName = hdu.getTrimmedString(FitsConstants.KEYWORD_EXT_NAME);

                this.checker.warning("Skipping non-standard OIFITS table [" + i + "]: " + extName);

                if (logger.isLoggable(Level.INFO)) {
                    logger.info("Skipping non-standard OIFITS table " + extName + "#" + i + ":\n" + FitsUtils.dumpHDU(hdu, false));
                }
            }
        }

        if (!hasDataHdu) {
            this.checker.severe("No OI_VIS, OI_VIS2, OI_T3 table found: one or more of them must be present");
        }
    }

    /**
     * Process a given Fits binary table to fill the given OI table object with keyword and column data
     * @param hdu binary table HDU
     * @param table OITable object
     * @throws FitsException if any FITS error occurred
     */
    private void processTable(final BinaryTableHDU hdu, final OITable table) throws FitsException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("processTable : " + table.getClass().getName());
        }

        // get Fits table header :
        this.processKeywords(hdu.getHeader(), table);

        this.processData(hdu, table);
    }

    /**
     * Process the binary table header to get keywords used by the OITable (see keyword descriptors)
     * and check missing keywords and their formats
     * @param header binary table header
     * @param table OI table
     * @throws FitsException if any FITS error occurred
     */
    private void processKeywords(final Header header, final OITable table) throws FitsException {
        // Note : a fits keyword has a KEY, VALUE AND COMMENT

        // Get Keyword descriptors :
        final Collection<KeywordMeta> keywordsDescCollection = table.getKeywordDescCollection();

        // Dump table descriptors :
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("table keywords :");
            for (KeywordMeta keyword : keywordsDescCollection) {
                logger.finest(keyword.toString());
            }
        }

        String name;
        Object value;
        for (KeywordMeta keyword : keywordsDescCollection) {
            name = keyword.getName();

            // check mandatory keywords :
            if (!header.containsKey(name)) {
                if (keyword.isMandatory()) {
                    /* No keyword with keywordName name */
                    this.checker.severe("Missing keyword '" + name + "'");
                }
            } else {

                // parse keyword value :
                value = parseKeyword(keyword, header.getValue(name));

                // store key and value :
                if (value != null) {
                    table.setKeywordValue(name, value);
                }
            }
        }

        // Copy all EXTRA header cards:
        final List<FitsHeaderCard> headerCards = table.getHeaderCards(header.getNumberOfCards());

        final Map<String, KeywordMeta> keywordsDesc = table.getKeywordsDesc();

        HeaderCard card;
        String key;
        for (Iterator<?> it = header.iterator(); it.hasNext();) {
            card = (HeaderCard) it.next();

            key = card.getKey();

            if ("END".equals(key)) {
                break;
            }

            // Keep history:
            if (!keywordsDesc.containsKey(key)
                    && (FitsConstants.KEYWORD_HISTORY.equals(key) || !FitsUtils.isStandardKeyword(key))) {
                headerCards.add(new FitsHeaderCard(key, card.getValue(), card.getComment()));
            }
        }

        table.trimHeaderCards();

        if (logger.isLoggable(Level.FINE) && table.hasHeaderCards()) {
            logger.fine("Table[" + table.idToString() + "] contains extra keywords:\n" + table.getHeaderCardsAsString("\n"));
        }
    }

    /**
     * Parse the keyword value and check its format
     * @param keyword keyword descriptor
     * @param keywordValue keyword raw string value
     * @return converted keyword value or null
     */
    private Object parseKeyword(final KeywordMeta keyword, final String keywordValue) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("KEYWORD " + keyword.getName() + " = '" + keywordValue + "'");
        }
        final Types dataType = keyword.getDataType();
        Types kDataType = Types.TYPE_CHAR;

        // Note : OIFits keywords only use 'A', 'I', 'D' types :

        if (dataType == Types.TYPE_CHAR) {
            return keywordValue.trim();
        } else {
            Number value = null;
            if (keywordValue.indexOf('.') == -1) {
                // check for Integers :
                value = parseInteger(keywordValue);
            }
            if (value != null) {
                kDataType = Types.TYPE_INT;
            } else {
                // check for Doubles :
                value = parseDouble(keywordValue);
                if (value != null) {
                    kDataType = Types.TYPE_DBL;
                }
            }
            if (kDataType != keyword.getDataType()) {
                this.checker.severe("Invalid format for keyword '" + keyword.getName() + "', found '" + kDataType.getRepresentation() + "' should be '" + keyword.getType() + "'");
            }
            if (value == null) {
                // default value if keyword value is not a number :
                value = Double.valueOf(0d);
            }
            // cross conversion :
            if (dataType == Types.TYPE_INT) {
                return Integer.valueOf(value.intValue());
            }
            return Double.valueOf(value.doubleValue());
        }
    }

    /**
     * Process the binary table to get columns used by the OITable (see column descriptors)
     * and check missing keywords and their formats
     * @param hdu binary table
     * @param table OI table
     * @throws FitsException if any FITS error occurred
     */
    private void processData(final BinaryTableHDU hdu, final OITable table) throws FitsException {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("ROWS = " + hdu.getNRows());
        }

        // Get Column descriptors :
        final Collection<ColumnMeta> columnsDescCollection = table.getColumnDescCollection();

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("table columns :");
            for (ColumnMeta column : columnsDescCollection) {
                logger.finest(column.toString());
            }
        }

        // keep only OIFits columns i.e. extra column(s) are skipped :

        int idx;
        String name;
        Object value;
        for (ColumnMeta column : columnsDescCollection) {
            name = column.getName();

            idx = hdu.findColumn(name);

            if (idx == -1) {
                if (!column.isOptional()) {
                    /* No column with columnName name */
                    this.checker.severe("Missing column '" + name + "'");
                }
            } else {

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("COLUMN [" + name + "] [" + hdu.getColumnLength(idx) + " " + hdu.getColumnType(idx) + "]");
                }

                // read all data and convert them to arrays[][] :
                // parse column value :
                value = parseColumn(
                        column,
                        hdu.getColumnType(idx),
                        hdu.getColumnLength(idx),
                        hdu.getColumnUnit(idx),
                        hdu.getColumn(idx));

                // store key and value :
                if (value != null) {
                    table.setColumnValue(name, value);
                }
            }
        }

        // Report any EXTRA column:

        final Map<String, ColumnMeta> columnsDesc = table.getColumnsDesc();

        for (int i = 0, nCols = hdu.getNCols(); i < nCols; i++) {
            name = hdu.getColumnName(i);

            if (!columnsDesc.containsKey(name)) {
                this.checker.warning("Skipping non-standard OIFITS column '" + table.getExtName() + "." + name + "' ["
                        + hdu.getColumnLength(i) + " " + hdu.getColumnType(i) + "]");
            }
        }
    }

    /**
     * Parse the column value and check its format (data type, repeat, units)
     * @param column column descriptor
     * @param columnType fits column type
     * @param columnRepeat fits column repeat (cardinality)
     * @param columnUnit fits column unit
     * @param columnValue column raw value
     * @return converted column value or null
     */
    private Object parseColumn(final ColumnMeta column, final char columnType,
            final int columnRepeat, final String columnUnit,
            final Object columnValue) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("parseColumn : " + column.getName() + " = " + ArrayFuncs.arrayDescription(columnValue));
        }

        // Check type and cardinality

        // Note : ColumnMeta.getRepeat() is lazily computed from table references
        final char descType = column.getType();
        final int descRepeat = column.getRepeat();

        if (descRepeat > 0) {
            boolean severe = false;

            if (columnType != descType) {
                severe = true;
            } else {
                if (columnType == Types.TYPE_CHAR.getRepresentation()) {
                    if (columnRepeat < descRepeat) {
                        checker.warning("Invalid format for column '" + column.getName() + "', found '" + columnRepeat + columnType
                                + "' should be '" + descRepeat + descType + "'");
                    } else if (columnRepeat > descRepeat) {
                        severe = true;
                    }
                } else {
                    if (columnRepeat != descRepeat) {
                        severe = true;
                    }
                }
            }

            if (severe) {
                checker.severe("Invalid format for column '" + column.getName() + "', found '" + columnRepeat + columnType
                        + "' should be '" + descRepeat + descType + "'");
            }
        } else {
            checker.warning("Can't check repeat for column '" + column.getName() + "'");

            if (columnType != descType) {
                checker.severe("Invalid format for column '" + column.getName() + "', found '" + columnType
                        + "' should be '" + descType + "'");
            }
        }

        // Check unit
        if (column.getUnits() != Units.parseUnit(columnUnit)) {
            if (columnUnit == null || columnUnit.length() == 0) {
                checker.warning("Missing unit for column '" + column.getName() + "', should be '" + column.getUnit() + "'");
            } else {
                checker.warning("Invalid unit for column '" + column.getName() + "', found '" + columnUnit + "' should be '" + column.getUnit() + "'");
            }
        }

        // column value :
        Object value = columnValue;

        if (columnType != descType) {
            // convert fits data type to expected data model type :

            switch (column.getDataType()) {
                case TYPE_INT:
                    value = ArrayFuncs.convertArray(value, short.class, true);
                    break;

                case TYPE_DBL:
                    value = ArrayFuncs.convertArray(value, double.class, true);
                    break;

                case TYPE_REAL:
                    value = ArrayFuncs.convertArray(value, float.class, true);
                    break;

                case TYPE_COMPLEX:
                    // Special case for complex visibilities :
                    value = ArrayFuncs.convertArray(value, float.class, true);
                    break;

                case TYPE_LOGICAL:
                    // Try to convert what is convertible else false are returned
                    value = ArrayFuncs.convertArray(value, boolean.class, true);
                    break;

                case TYPE_CHAR:
                // only numeric types
                default:
                // nothing to do
            }
        }

        if (column instanceof WaveColumnMeta && columnRepeat == 1) {
            // Note : If TDIM keyword is present, the value is already a 2D array except for complex column.

            final int[] dims = ArrayFuncs.getDimensions(value);

            // Check the dimensions of the converted value :
            if (dims != null) {
                if (column.getDataType() == Types.TYPE_COMPLEX && dims.length == 2) {
                    // Special case : NWave = 1 means that Fits gives 2D arrays instead of 3D arrays :
                    // Applies to Complex types :
                    final Object flattened = ArrayFuncs.flatten(value);
                    final int[] newDims = {dims[0], 1, 2};
                    value = ArrayFuncs.curl(flattened, newDims);
                } else if (dims.length == 1) {
                    // Special case : NWave = 1 means that Fits gives 1D arrays instead of 2D arrays :
                    // Applies to Double and Logical types :
                    final int[] newDims = {Array.getLength(columnValue), 1};
                    value = ArrayFuncs.curl(value, newDims);
                }
            }
        }

        return value;
    }

    /**
     * Parse the String value as a double
     * @param value string value
     * @return Double or null if number format exception
     */
    private Double parseDouble(final String value) {
        Double res = null;
        try {
            res = Double.valueOf(value);
        } catch (NumberFormatException nfe) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("parseDouble failure : " + value);
            }
        }
        return res;
    }

    /**
     * Parse the String value as an integer
     * @param value string value
     * @return Integer or null if number format exception
     */
    private Integer parseInteger(final String value) {
        Integer res = null;
        try {
            res = Integer.valueOf(value);
        } catch (NumberFormatException nfe) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("parseInteger failure : " + value);
            }
        }
        return res;
    }

    /*
     * Getter - Setter -----------------------------------------------------------
     */
    /**
     * Return the OIFits data model
     * @return OIFits data model
     */
    public OIFitsFile getOIFitsFile() {
        return oiFitsFile;
    }
}
