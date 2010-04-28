/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: OIFitsLoader.java,v 1.1 2010-04-28 14:47:38 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 */
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.util.FileUtils;
import fr.jmmc.oitools.validator.CheckHandler;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import nom.tam.fits.BasicHDU;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;

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
  /** OIFits data model */
  private OIFitsFile oiFitsFile = null;
  /** OIFits file */
  private Fits fitsFile = null;
  /** Specific logger dedicated to validation */
  private final Logger checkLogger;
  /** Handler associated to check logger */
  private final CheckHandler checkHandler;

  static {

    // enable / disable ESO HIERARCH keyword support :
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
    String tmpFilename = fileLocation;

    // If the given file is remote :
    if (fileLocation.contains("://")) {
      // Only remote files are retrieved :

      // Load it and unarchive it as the Fits library does not manage remote file.
      tmpFilename = FileUtils.saveToFile(fileLocation);
    }

    final OIFitsLoader loader = new OIFitsLoader();

    loader.load(tmpFilename);

    return loader.getOIFitsFile();
  }

  /**
   * Private constructor
   */
  private OIFitsLoader() {

    /* Build logger that will collect checking treatment results */
    checkLogger = Logger.getLogger("checkLogger.oifits");
    checkLogger.setUseParentHandlers(false);

    /* Build and associate new specific handler to handle output */
    checkHandler = new CheckHandler(true);
    checkLogger.addHandler(checkHandler);

    // TODO : see if each OI table really needs its logger / handler ?

  }

  /**
   * Load the given file in memory
   * @param absFilePath absolute File path on file system (not URL)
   * @throws FitsException if the fits can not be opened
   */
  private void load(final String absFilePath) throws FitsException {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("loading " + absFilePath);
    }
    // create new OIFits data model
    this.oiFitsFile = new OIFitsFile(absFilePath);

    try {
      final long start = System.nanoTime();

      // open the fits file :
      this.fitsFile = new Fits(absFilePath);

      // read the complete file structure :
      final BasicHDU[] hdus = this.fitsFile.read();

      // process all HD units :
      this.processHDUnits(hdus);

      if (logger.isLoggable(Level.FINE)) {
        logger.fine("load : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");
      }

    } catch (FitsException fe) {
      logger.log(Level.SEVERE, "load failed ", fe);
      throw fe;
    }

  }

  /**
   * Process all Fits HD units to load OI_* tables (skip other tables)
   * and check at least one data table is present.
   * @param hdus array of hd unit
   * @throws FitsException if the fits structure is incorrect
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

    // ? TODO : use a single step as there are no more dependency at loading time
    // between referenced tables and data tables

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

          // load table :
          this.processTable(bh, oiTable);

          this.oiFitsFile.registerOiTable(oiTable);

          hasDataHdu = true;
        }
      }
    }

    if (!hasDataHdu) {
      checkLogger.severe("No OI_VIS, OI_VIS2, OI_T3 table found: one or more of them must be present");
    }
  }

  private void processTable(final BinaryTableHDU hdu, final OITable table) throws FitsException {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("processTable : " + table.getClass().getName());
    }
    this.processKeywords(hdu, table);

    this.processData(hdu, table);
  }

  private void processKeywords(final BinaryTableHDU hdu, final OITable table) throws FitsException {
    // get Fits table  header :
    final Header header = hdu.getHeader();
    // Note : a fits keyword has a KEY, VALUE AND COMMENT

    // Get Keyword descriptors :
    final Map<String, KeywordMeta> keywordsDesc = table.getKeywordsDesc();

    if (logger.isLoggable(Level.FINEST)) {
      logger.finest("table keywords :");
      for (KeywordMeta keyword : keywordsDesc.values()) {
        logger.finest(keyword.toString());
      }
    }

    String key;
    Object value;
    for (KeywordMeta keyword : keywordsDesc.values()) {
      key = keyword.getName();

      if (header.containsKey(key)) {
        switch (keyword.getDataType()) {
          case TYPE_CHAR:
            value = header.getTrimmedStringValue(key);
            break;

          case TYPE_INT:
            // 0 if not found
            value = Integer.valueOf(header.getIntValue(key, 0));
            break;

          case TYPE_DBL:
            // 0d if not found
            value = Double.valueOf(header.getDoubleValue(key, 0d));
            break;

          case TYPE_REAL:
            // UNUSED
            // 0f if not found
            value = Float.valueOf(header.getFloatValue(key, 0f));
            break;

          case TYPE_LOGICAL:
            // UNUSED
            // false if not found
            value = Boolean.valueOf(header.getBooleanValue(key, false));
            break;

          default:
            throw new IllegalStateException("unknown type : " + keyword.getDataType());
        }
        // store key and value :
        table.setKeywordValue(key, value);
      }
    }
  }

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

    int idx;
    String key;
    Object value;
    for (ColumnMeta column : columnsDesc.values()) {
      key = column.getName();

      idx = hdu.findColumn(key);

      // keep only OIFits columns i.e. extra column(s) are skipped :

      if (idx == -1) {
        // TODO : check logger !
        if (logger.isLoggable(Level.WARNING)) {
          logger.warning("missing column [" + key + "] in " + table);
        }
      } else {
        // read all data and convert them to arrays[][] :
        value = hdu.getColumn(idx);

        if (logger.isLoggable(Level.FINE)) {
          logger.fine("COLUMN [" + key + "] [" + hdu.getColumnLength(idx) + " " + hdu.getColumnType(idx) + "]");
        }

        // TODO : convert fits data type to expected data model type :
//        column.getDataType()

        // ?? short[] => stored in memory as int[] ?
        // ?? float/double[] => stored in memory as double[] ?

        // store key and value :
        table.setColumnValue(key, value);
      }
    }
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

  /**
   * Return the checkHandler that get informations about checking process.
   *
   * @return the checkHandler
   */
  public CheckHandler getCheckHandler() {
    return checkHandler;
  }

  /**
   * Get check handler status
   *
   * @return a string containing the status
   */
  public String getCheckStatus() {
    return checkHandler.getStatus();
  }

  /**
   * Get ckeck report message
   *
   * @return a string containing the analysis report
   */
  public String getCheckReport() {
    return checkHandler.getReport();
  }

  /**
   * Clear the report message informations
   */
  public void clearCheckReport() {
    checkHandler.clearReport();
  }
}
