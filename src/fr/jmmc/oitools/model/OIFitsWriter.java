/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import fr.nom.tam.fits.BasicHDU;
import fr.nom.tam.fits.BinaryTable;
import fr.nom.tam.fits.BinaryTableHDU;
import fr.nom.tam.fits.Data;
import fr.nom.tam.fits.Fits;
import fr.nom.tam.fits.FitsException;
import fr.nom.tam.fits.FitsFactory;
import fr.nom.tam.fits.Header;
import fr.nom.tam.util.BufferedFile;

/**
 * This statefull class writes an OIFits file from the OIFitsFile model
 *
 * @author bourgesl
 */
public class OIFitsWriter {
  /* constants */

  /** Logger associated to meta model classes */
  protected final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
          "fr.jmmc.oitools.model.OIFitsWriter");

  /* members */
  /** OIFits data model */
  private final OIFitsFile oiFitsFile;

  static {
    // Force to use binary tables :
    FitsFactory.setUseAsciiTables(false);
  }

  /**
   * Main method to write an OI Fits File
   * @param absFilePath absolute File path on file system (not URL)
   * @param oiFitsFile OIFits data model
   * @throws FitsException if the fits can not be written
   * @throws IOException IO failure
   */
  public static void writeOIFits(final String absFilePath, final OIFitsFile oiFitsFile) throws IOException, FitsException {
    oiFitsFile.setAbsoluteFilePath(absFilePath);

    final OIFitsWriter writer = new OIFitsWriter(oiFitsFile);
    writer.write(absFilePath);
  }

  /**
   * Private constructor
   * @param oiFitsFile OIFits data model
   */
  private OIFitsWriter(final OIFitsFile oiFitsFile) {
    this.oiFitsFile = oiFitsFile;
  }

  /**
   * Write the OI Fits data model into the given file.
   *
   * Note : This method supposes that the OI Fits data model was checked previously
   * i.e. no column is null and values respect the OIFits standard (length, cardinality ...)
   *
   * @param absFilePath absolute File path on file system (not URL)
   * @throws FitsException if any FITS error occured
   * @throws IOException IO failure
   */
  private void write(final String absFilePath) throws FitsException, IOException {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("writing " + absFilePath);
    }

    BufferedFile bf = null;
    try {
      final long start = System.nanoTime();

      // create the fits model :
      final Fits fitsFile = new Fits();

      // process all OI_* tables :
      createHDUnits(fitsFile);

      bf = new BufferedFile(absFilePath, "rw");

      // write the fits file :
      fitsFile.write(bf);

      // flush and close :
      bf.close();
      bf = null;

      if (logger.isLoggable(Level.FINE)) {
        logger.fine("write : duration = " + 1e-6d * (System.nanoTime() - start) + " ms.");
      }

    } catch (FitsException fe) {
      logger.log(Level.SEVERE, "Unable to write the file : " + absFilePath, fe);

      throw fe;
    } finally {
      if (bf != null) {
        // flush and close :
        bf.close();
      }
    }
  }

  /**
   * Create all Fits HD units corresponding to OI_* tables
   * @param fitsFile fits file
   * @throws FitsException if any FITS error occured
   * @throws IOException IO failure
   */
  private void createHDUnits(final Fits fitsFile) throws FitsException, IOException {
    for (OITable oiTable : this.oiFitsFile.getOITableList()) {
      // add HDU to the fits file :
      fitsFile.addHDU(createBinaryTable(oiTable));
    }
  }

  /**
   * Create a binary table HDU using the given OI table
   * @param table OI table
   * @throws FitsException if any FITS error occured
   * @throws IOException IO failure
   * @return binary table HDU
   */
  private BasicHDU createBinaryTable(final OITable table) throws FitsException, IOException {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("createBinaryTable : " + table.toString());
    }

    // Get Column descriptors :
    final Collection<ColumnMeta> columnsDesc = table.getColumnDescCollection();

    // data list containing column data (not null) :
    final List<Object> dataList = new ArrayList<Object>(columnsDesc.size());
    // index map storing the column index keyed by column name :
    final Map<String, Integer> columnIndex = new HashMap<String, Integer>();

    // backup of modified String keyed by column name :
    final Map<String, String> backupString = new HashMap<String, String>();

    // define both data list and index map :
    int i = 0;
    Integer idx;
    Object value;
    for (ColumnMeta column : columnsDesc) {
      value = table.getColumnValue(column.getName());

      if (value != null) {
        // fix string length to have correct header length ('0A' issue) :
        if (column.getDataType() == Types.TYPE_CHAR) {
          final String[] values = (String[]) value;
          if (values.length > 0) {
            String val = values[0];

            // backup that string to restore it after HDU creation :
            backupString.put(column.getName(), val);

            while (val.length() < column.getRepeat()) {
              val += " ";
            }
            // set the first value of a character column to its maximum length :
            values[0] = val;
          }
        }

        // add column value in use :
        dataList.add(value);
        // column index corresponds to the position in the data list :
        columnIndex.put(column.getName(), Integer.valueOf(i));
        i++;
      }
    }

    // Prepare the binary table to create HDU :
    final Data fitsData = new BinaryTable(dataList.toArray());

    // Generate the header from the binary table :
    final Header header = BinaryTableHDU.manufactureHeader(fitsData);

    // create HDU :
    final BinaryTableHDU hdu = new BinaryTableHDU(header, fitsData);

    // Restore String data :
    for (Map.Entry<String, String> e : backupString.entrySet()) {
      // restore in OI Table the initial string value :
      table.getColumnString(e.getKey())[0] = e.getValue();
    }

    // Finalize Header :

    // Define column information (name, description, unit) :
    for (ColumnMeta column : columnsDesc) {
      idx = columnIndex.get(column.getName());

      // column in use :
      if (idx != null) {
        i = idx.intValue();

        if (logger.isLoggable(Level.FINE)) {
          logger.fine("COLUMN [" + column.getName() + "] [" + hdu.getColumnLength(i) + " " + hdu.getColumnType(i) + "]");
        }
        hdu.setColumnName(i, column.getName(), column.getDescription(), column.getUnits().getStandardRepresentation());
      }
    }

    // Add keywords after column definition in the header :
    processKeywords(header, table);

    // Fix header for Fits complex columns :
    for (ColumnMeta column : columnsDesc) {
      if (column.getDataType() == Types.TYPE_COMPLEX) {
        idx = columnIndex.get(column.getName());

        // column in use :
        if (idx != null) {
          i = idx.intValue();

          // change the 2D float column to complex type :
          hdu.setColumnComplex(i);
        }
      }
    }
    return hdu;
  }

  /**
   * Process the binary table header to set keywords defined in the OITable (see keyword descriptors)
   * @param header binary table header
   * @param table OI table
   * @throws FitsException if any FITS error occured
   */
  private void processKeywords(final Header header, final OITable table) throws FitsException {

    // Note : OIFits keywords only use 'A', 'I', 'D' types :

    String keywordName;
    Object keywordValue;

    for (KeywordMeta keyword : table.getKeywordDescCollection()) {
      keywordName = keyword.getName();

      if (OIFitsConstants.KEYWORD_NAXIS2.equals(keywordName)) {
        // skip NAXIS2 keyword : already managed by the Fits library
        continue;
      }

      // get keyword value :
      keywordValue = table.getKeywordValue(keywordName);

      if (keywordValue == null) {
        // skip missing values
        continue;
      }

      if (logger.isLoggable(Level.FINE)) {
        logger.fine("KEYWORD " + keyword.getName() + " = '" + keywordValue + "'");
      }

      switch (keyword.getDataType()) {
        case TYPE_INT:
          header.addValue(keywordName, ((Integer) keywordValue).intValue(), keyword.getDescription());
          break;
        case TYPE_DBL:
          header.addValue(keywordName, ((Double) keywordValue).doubleValue(), keyword.getDescription());
          break;
        case TYPE_CHAR:
        default:
          header.addValue(keywordName, (String) keywordValue, keyword.getDescription());
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
}
