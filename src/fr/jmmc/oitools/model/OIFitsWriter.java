/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: OIFitsWriter.java,v 1.1 2010-05-28 14:57:45 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 */
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import nom.tam.fits.BinaryTableHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.FitsFactory;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.util.BufferedFile;

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
   * Write the OI Fits data model into the given file
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
      logger.log(Level.SEVERE, "write failed ", fe);
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
      createBinaryTable(fitsFile, oiTable);
    }
  }

  /**
   * Create a binary table HDU using the given OI table
   * @param fitsFile fits file
   * @param table OI table
   * @throws FitsException if any FITS error occured
   * @throws IOException IO failure
   */
  private void createBinaryTable(final Fits fitsFile, final OITable table) throws FitsException, IOException {

    logger.severe("createBinaryTable : " + table.toString());

    final int nbCols = table.getNbColumns();

    // Get Column descriptors :
    final Collection<ColumnMeta> columnsDesc = table.getColumnDescCollection();

    // Prepare data to create HDU :
    final Object[] data = new Object[nbCols];

    int i = 0;
    for (ColumnMeta column : columnsDesc) {
      data[i++] = table.getColumnValue(column.getName());
    }

    // create HDU from data :
    fitsFile.addHDU(Fits.makeHDU(data));

    final BinaryTableHDU hdu = (BinaryTableHDU) fitsFile.getHDU(fitsFile.getNumberOfHDUs() - 1);

    // Define column headers :
    i = 0;
    for (ColumnMeta column : columnsDesc) {

     logger.severe("COLUMN [" + column.getName() + "] [" + hdu.getColumnLength(i) + " " + hdu.getColumnType(i) + "]");

      hdu.setColumnName(i++, column.getName(), column.getDescription(), column.getUnits().getStandardRepresentation());
    }

    // Header :
    final Header header = hdu.getHeader();

    // fix column header for string length or complex types ...
/*
    int len = 0;
    i = 0;
    for (ColumnMeta column : columnsDesc) {
      if (column.getDataType() == Types.TYPE_CHAR) {
        logger.severe("old size = " + hdu.getColumnLength(i));
        len -= hdu.getColumnLength(i);

        header.addValue("TFORM"+ (i + 1), column.getRepeat() + "A", "Character string");

        len += column.getRepeat();

        logger.severe("new size = " + hdu.getColumnLength(i));
      }
      i++;
    }

    logger.severe("len = " + len);
    logger.severe("nAxis1 = " + header.getIntValue("NAXIS1"));

    header.setNaxis(1, header.getIntValue("NAXIS1") + len);

    logger.severe("nAxis1 = " + header.getIntValue("NAXIS1"));

    hdu.info();

    */

    // Add keywords :
    processKeywords(header, table);
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

      logger.severe("KEYWORD " + keyword.getName() + " = '" + keywordValue + "'");

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
