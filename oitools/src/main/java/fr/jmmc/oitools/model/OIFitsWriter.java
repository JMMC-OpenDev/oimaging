/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oitools.fits.FitsConstants;
import fr.jmmc.oitools.fits.FitsUtils;
import fr.jmmc.oitools.image.FitsImage;
import fr.jmmc.oitools.image.FitsImageHDU;
import static fr.jmmc.oitools.image.FitsImageWriter.createImage;
import fr.jmmc.oitools.image.ImageOiData;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import static fr.jmmc.oitools.meta.Types.TYPE_CHAR;
import static fr.jmmc.oitools.meta.Types.TYPE_DBL;
import static fr.jmmc.oitools.meta.Types.TYPE_INT;
import fr.nom.tam.fits.BasicHDU;
import fr.nom.tam.fits.BinaryTable;
import fr.nom.tam.fits.BinaryTableHDU;
import fr.nom.tam.fits.Data;
import fr.nom.tam.fits.Fits;
import fr.nom.tam.fits.FitsException;
import fr.nom.tam.fits.Header;
import fr.nom.tam.util.BufferedFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * This state-full class writes an OIFits file from the OIFitsFile model
 *
 * @author bourgesl
 */
public class OIFitsWriter {
    /* constants */

    /** Logger associated to meta model classes */
    protected final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(OIFitsWriter.class.getName());

    static {
        FitsUtils.setup();
    }

    /* members */
    /** OIFits data model */
    private final OIFitsFile oiFitsFile;

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
     * @throws FitsException if any FITS error occurred
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
     * Create all Fits HD units corresponding to OI_* tables, and additional HDU for IMAGE-OI (in first place) if any.
     * Primary HDU Keywords are not -yet- serialized.
     *
     * @param fitsFile fits file
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     */
    private void createHDUnits(final Fits fitsFile) throws FitsException, IOException {

        // TODO implement first HDU keyword serialization
        // Add IMAGE-OI table if any
        if (this.oiFitsFile.getImageOiData() != null) {
            createImageOiHDUnits(fitsFile, this.oiFitsFile.getImageOiData());
        }

        // Add OiTables
        for (OITable oiTable : this.oiFitsFile.getOITableList()) {
            // add HDU to the fits file :
            fitsFile.addHDU(createBinaryTable(oiTable));
        }        

    }

    /**
     * Create all Fits HD units corresponding to IMAGE-OI content.
     * @param fitsFile fits file
     * @param imageOiData IMAGE-OI data container of images and parameters to write into give fits file.
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     */
    private void createImageOiHDUnits(final Fits fitsFile, ImageOiData imageOiData) throws FitsException, IOException {

        // Add image tables
        List< FitsImageHDU> fitsImageHDUs = imageOiData.getFitsImageHDUs();
        for (FitsImageHDU fitsImageHDU : fitsImageHDUs) {

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("createHDUnits with optional table : " + fitsImageHDU.toString());
            }

            // TODO: support fits cube ?
            for (FitsImage image : fitsImageHDU.getFitsImages()) {
                    // TODO setFitsImageIdentifier ?
                // image.setFitsImageIdentifier(this.oiFitsFile.getName() + "#TBD");

                // add HDU to the fits file :
                final BasicHDU hdu = createImage(image);
                fitsFile.addHDU(hdu);
            }
        }

        // Add IMAGE-OI INPUT PARAM table
        createImageOiInputParamTable(fitsFile, imageOiData);

    }

    /**
     * Create binary table with main iput parameters for IMAGE-OI
     * @param fitsFile fits file
     * @param imageOiData IMAGE-OI data to look for input param.
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     */
    private void createImageOiInputParamTable(final Fits fitsFile, ImageOiData imageOiData) throws FitsException {
        // TODO add  bintable ( if IMAGE-OI attribute is set in OIFitsFile )
        Header hdr = new Header();
        // TODO  define a constant for xtension value in ImageOiConstants
        hdr.addValue(FitsConstants.KEYWORD_XTENSION, "BINARY", "binary table extension");
        hdr.addValue(FitsConstants.KEYWORD_BITPIX, 8l, "number of bits per data pixel");
        hdr.addValue(FitsConstants.KEYWORD_NAXIS, 0l, "dimensionality");
        hdr.addValue(FitsConstants.KEYWORD_EXT_NAME, "IMAGE-OI INPUT PARAM", "extension name");

        // Append imageOiData.inputParam
        processKeywords(hdr, imageOiData.getInputParam().getKeywordDescCollection(), imageOiData.getInputParam().getKeywordsValue());

        BinaryTable table = new BinaryTable();
        BinaryTableHDU tableHDU = new BinaryTableHDU(hdr, table);

        fitsFile.addHDU(tableHDU);
    }

    /**
     * Create a binary table HDU using the given OI table
     * @param table OI table
     * @throws FitsException if any FITS error occurred
     * @throws IOException IO failure
     * @return binary table HDU
     */
    private BasicHDU createBinaryTable(final OITable table) throws FitsException, IOException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("createBinaryTable : " + table.toString());
        }

        // Get Column descriptors :
        final Collection<ColumnMeta> columnsDescCollection = table.getColumnDescCollection();
        final int size = columnsDescCollection.size();

        // data list containing column data (not null) :
        final List<Object> dataList = new ArrayList<Object>(size);

        // index map storing the column index keyed by column name :
        final Map<String, Integer> columnIndex = new HashMap<String, Integer>(size);
        // backup of modified String keyed by column name :
        final Map<String, String> backupFirstString = new HashMap<String, String>(size);

        // define both data list and index map :
        int i = 0;
        Integer idx;
        String name;
        Object value;
        String[] values;
        String val;
        for (ColumnMeta column : columnsDescCollection) {
            name = column.getName();
            value = table.getColumnValue(name);

            if (value != null) {
                // fix string length to have correct header length ('0A' issue) :
                if (column.getDataType() == Types.TYPE_CHAR) {
                    values = (String[]) value;

                    if (values.length > 0) {
                        val = values[0];

                        if (val == null) {
                            val = "";
                        }

                        // backup that string to restore it after HDU creation :
                        backupFirstString.put(name, val);

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
                columnIndex.put(name, NumberUtils.valueOf(i));
                i++;
            }
        }

        // Prepare the binary table to create HDU :
        final Data fitsData = new BinaryTable(dataList.toArray());

        // Generate the header from the binary table :
        final Header header = BinaryTableHDU.manufactureHeader(fitsData);

        // create HDU :
        final BinaryTableHDU hdu = new BinaryTableHDU(header, fitsData);

        // Restore first String data :
        for (Map.Entry<String, String> e : backupFirstString.entrySet()) {
            // restore in OI Table the initial string value :
            table.getColumnString(e.getKey())[0] = e.getValue();
        }

        // Finalize Header :
        // Define column information (name, description, unit) :
        for (ColumnMeta column : columnsDescCollection) {
            name = column.getName();
            idx = columnIndex.get(name);

            // column in use :
            if (idx != null) {
                i = idx.intValue();

                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("COLUMN [" + name + "] [" + hdu.getColumnLength(i) + " " + hdu.getColumnType(i) + "]");
                }
                hdu.setColumnName(i, name, column.getDescription(), column.getUnits().getStandardRepresentation());
            }
        }

        // Add keywords after column definition in the header :
        processKeywords(header, table);

        // Fix header for Fits complex columns :
        for (ColumnMeta column : columnsDescCollection) {
            if (column.getDataType() == Types.TYPE_COMPLEX) {
                idx = columnIndex.get(column.getName());

                // column in use :
                if (idx != null) {
                    i = idx.intValue();

                    // change the 2D float column to complex type :
                    hdu.setComplexColumn(i);
                }
            }
        }
        return hdu;
    }

    /**
     * Process the binary table header to set keywords defined in the OITable (see keyword descriptors)
     * @param header binary table header
     * @param table OI table
     * @throws FitsException if any FITS error occurred
     */
    private void processKeywords(final Header header, final OITable table) throws FitsException {
        processKeywords(header, table.getKeywordDescCollection(), table.getKeywordsValue());
    }

    /**
     * Process the binary table header to set keywords defined in the OITable (see keyword descriptors)
     * @param header binary table header
     * @param keywordDescs collection of keyword descriptions
     * @param keywordValues map of keyword names-values
     * @throws FitsException if any FITS error occurred
     */
    private void processKeywords(final Header header, final Collection<KeywordMeta> keywordDescs, final Map<String, Object> keywordValues) throws FitsException {

        // Note : OIFits keywords only use 'A', 'I', 'D' types :
        String keywordName;
        Object keywordValue;

        for (KeywordMeta keyword : keywordDescs) {
            keywordName = keyword.getName();

            if (FitsConstants.KEYWORD_NAXIS2.equals(keywordName)) {
                // skip NAXIS2 keyword : already managed by the Fits library
                continue;
            }

            // get keyword value :
            keywordValue = keywordValues.get(keywordName);

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
                case TYPE_LOGICAL:
                    header.addValue(keywordName, ((Boolean) keywordValue).booleanValue(), keyword.getDescription());
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
