/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: OIFitsLoader.java,v 1.4 2010-05-27 16:13:29 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.3  2010/05/03 14:29:14  bourgesl
 * added column checks (type, repeat, units)
 *
 * Revision 1.2  2010/04/29 15:47:02  bourgesl
 * use OIFitsChecker instead of CheckLogger / Handler to make OIFits validation
 *
 * Revision 1.1  2010/04/28 14:47:38  bourgesl
 * refactored OIValidator classes to represent the OIFits data model
 *
 */
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.meta.Units;
import fr.jmmc.oitools.util.FileUtils;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.logging.Level;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.util.ArrayFuncs;

/**
 * This class loads an OIFits file to the OIFitsFile model
 * @author bourgesl
 */
public class OIFitsLoader {
  /* constants */

  /** Logger associated to meta model classes */
  protected final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
          "fr.jmmc.oitools.model.OIFitsLoader");

  /* members */
  /** checker */
  private final OIFitsChecker checker;
  /** OIFits data model */
  private OIFitsFile oiFitsFile = null;

  static {
    // enable ESO HIERARCH keyword support in nom.tam Fits library :
    FitsFactory.setUseHierarch(true);
  }

  /**
   * Main method to load an OI Fits File
   * @param fileLocation absolute File Path or remote URL
   * @return OI Fits File
   * @throws MalformedURLException invalid url format
   * @throws IOException IO failure
   * @throws FitsException if the fits can not be opened
   */
  public static OIFitsFile loadOIFits(final String fileLocation) throws MalformedURLException, IOException, FitsException {
    return loadOIFits(null, fileLocation);
  }

  /**
   * Main method to load an OI Fits File with the given checker component
   * @param checker checker component
   * @param fileLocation absolute File Path or remote URL
   * @return OI Fits File
   * @throws MalformedURLException invalid url format
   * @throws IOException IO failure
   * @throws FitsException if the fits can not be opened
   */
  public static OIFitsFile loadOIFits(final OIFitsChecker checker, final String fileLocation) throws MalformedURLException, IOException, FitsException {
    String tmpFilename = fileLocation;

    // If the given file is remote :
    if (fileLocation.contains("://")) {
      // Only remote files are retrieved :

      // Load it and unarchive it as the Fits library does not manage remote file.
      tmpFilename = FileUtils.saveToFile(fileLocation);
    }

    final OIFitsLoader loader = new OIFitsLoader(checker);

    loader.load(tmpFilename);

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
    } else {
      this.checker = new OIFitsChecker();
    }
  }

  /**
   * Load the given file in memory
   * @param absFilePath absolute File path on file system (not URL)
   * @throws FitsException if any FITS error occured
   */
  private void load(final String absFilePath) throws FitsException {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("loading " + absFilePath);
    }

    this.checker.info("Loading File: " + absFilePath);

    // create new OIFits data model
    this.oiFitsFile = new OIFitsFile(absFilePath);

    try {
      final long start = System.nanoTime();

      // open the fits file :
      final Fits fitsFile = new Fits(absFilePath);

      // read the complete file structure :
      final BasicHDU[] hdus = fitsFile.read();

      // process all HD units :
      if (hdus != null) {
        this.processHDUnits(hdus);
      }

      if (logger.isLoggable(Level.FINE)) {
        logger.fine("load : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");
      }

// TODO : remove test code :
      this.oiFitsFile.check(this.checker);

// validation results
      if (logger.isLoggable(Level.INFO)) {
        logger.info("validation results\n" + this.checker.getCheckReport());
      }

// HERE !

    } catch (FitsException fe) {
      logger.log(Level.SEVERE, "load failed ", fe);
      throw fe;
    }

  }

  /**
   * Process all Fits HD units to load OI_* tables (skip other tables)
   * and check at least one data table is present.
   * @param hdus array of hd unit
   * @throws FitsException if any FITS error occured
   */
  private void processHDUnits(final BasicHDU[] hdus) throws FitsException {

    final int nbHDU = hdus.length;

    /*
     * ? Maybe process the primary HDU to load all keywords (ESO HIERARCH ...) ?
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

      extName = hdu.getTrimmedString(OIFitsConstants.KEYWORD_EXT_NAME);

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
          // define the extension number :
          oiTable.setExtNb(i);

          this.checker.info("Analysing table [" + i + "]: " + extName);
          // load table :
          this.processTable(bh, oiTable);

          // register the table :
          this.oiFitsFile.registerOiTable(oiTable);
        }
      }
    }

    // Second loop : build and check presence of
    // OI_VIS, OI_VIS2, OI_T3 tables
    boolean hasDataHdu = false;

    for (int i = 1; i < nbHDU; i++) {
      hdu = hdus[i];

      extName = hdu.getTrimmedString(OIFitsConstants.KEYWORD_EXT_NAME);

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

    if (!hasDataHdu) {
      this.checker.severe("No OI_VIS, OI_VIS2, OI_T3 table found: one or more of them must be present");
    }
  }

  /**
   * Process a given Fits binary table to fill the given OI table object with keyword and column data
   * @param hdu binary table HDU
   * @param table OITable object
   * @throws FitsException if any FITS error occured
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
   * @param table OITable object
   * @throws FitsException if any FITS error occured
   */
  private void processKeywords(final Header header, final OITable table) throws FitsException {
    // Note : a fits keyword has a KEY, VALUE AND COMMENT

    // Get Keyword descriptors :
    final Map<String, KeywordMeta> keywordsDesc = table.getKeywordsDesc();

    // Dump table descriptors :
    if (logger.isLoggable(Level.FINEST)) {
      logger.finest("table keywords :");
      for (KeywordMeta keyword : keywordsDesc.values()) {
        logger.finest(keyword.toString());
      }
    }

    String name;
    Object value;
    for (KeywordMeta keyword : keywordsDesc.values()) {
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
  }

  /**
   * Parse the keyword value and check its format
   * @param keyword keyword descriptor
   * @param keywordValue keyword raw string value
   * @return converted keyword value or null
   */
  private Object parseKeyword(final KeywordMeta keyword, final String keywordValue) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("parseKeyword : " + keyword.getName() + " = '" + keywordValue + "'");
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
   * @param table OITable object
   * @throws FitsException if any FITS error occured
   */
  private void processData(final BinaryTableHDU hdu, final OITable table) throws FitsException {

    if (logger.isLoggable(Level.FINE)) {
      logger.fine("ROWS = " + hdu.getNRows());
    }

    // Get Column descriptors :
    final Map<String, ColumnMeta> columnsDesc = table.getColumnsDesc();

    if (logger.isLoggable(Level.FINEST)) {
      logger.finest("table columns :");
      for (ColumnMeta column : columnsDesc.values()) {
        logger.finest(column.toString());
      }
    }

    // keep only OIFits columns i.e. extra column(s) are skipped :

    int idx;
    String name;
    Object value;
    for (ColumnMeta column : columnsDesc.values()) {
      name = column.getName();

      idx = hdu.findColumn(name);

      if (idx == -1) {
        /* No column with columnName name */
        this.checker.severe("Missing column '" + name + "'");
      } else {

        if (logger.isLoggable(Level.FINE)) {
          logger.fine("COLUMN [" + name + "] [" + hdu.getColumnLength(idx) + " " + hdu.getColumnType(idx) + "]");
        }

        // read all data and convert them to arrays[][] :
        // parse column value :
        value = parseColumn(column, hdu.getColumnType(idx), hdu.getColumnLength(idx), hdu.getColumnUnit(idx), hdu.getColumn(idx));

        // store key and value :
        if (value != null) {
          table.setColumnValue(name, value);
        }
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
            checker.warning("Invalid format for column '" + column.getName() + "', found '" + columnRepeat + columnType +
                    "' should be '" + descRepeat + descType + "'");
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
        checker.severe("Invalid format for column '" + column.getName() + "', found '" + columnRepeat + columnType +
                "' should be '" + descRepeat + descType + "'");
      }
    } else {
      checker.warning("Can't check repeat for column '" + column.getName() + "'");

      if (columnType != descType) {
        checker.severe("Invalid format for column '" + column.getName() + "', found '" + columnType +
                "' should be '" + descType + "'");
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

    // TODO : convert fits data type to expected data model type :
//        column.getDataType()

    // ?? short[] => stored in memory as int[] ?
    // ?? float/double[] => stored in memory as double[] ?

    return columnValue;
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
