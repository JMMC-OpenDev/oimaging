/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: OITable.java,v 1.5 2010-05-28 14:57:05 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.4  2010/05/27 16:13:29  bourgesl
 * javadoc + small refactoring to expose getters/setters for keywords and getters for columns
 *
 * Revision 1.3  2010/05/03 14:29:43  bourgesl
 * refactored column checks (type, repeat, int or string values) to analyse Object value instead of FitsColumn
 *
 * Revision 1.2  2010/04/29 15:47:02  bourgesl
 * use OIFitsChecker instead of CheckLogger / Handler to make OIFits validation
 *
 * Revision 1.1  2010/04/28 14:47:38  bourgesl
 * refactored OIValidator classes to represent the OIFits data model
 *
 * Revision 1.15  2009/09/15 12:00:15  mella
 * add more informations for oi_array
 *
 * Revision 1.14  2009/09/09 06:43:06  mella
 * add  type and unit into xml desc of all column desc
 *
 * Revision 1.13  2009/09/09 06:40:40  mella
 * add  type and unit into xml desc of all cell desc
 *
 * Revision 1.12  2009/09/08 16:10:42  mella
 * add same block for all oitable after optionnal specific data
 *
 * Revision 1.11  2009/08/25 12:45:51  mella
 * define abstract method getXmlDesc
 *
 * Revision 1.10  2009/01/06 13:30:09  mella
 * Add log and accessor method for boolean column content
 *
 * Revision 1.9  2008/10/28 08:22:06  mella
 * Add javadoc
 *
 * Revision 1.8  2008/04/08 14:22:16  mella
 * Include Evelyne comments
 *
 * Revision 1.7  2008/03/28 09:03:39  mella
 * CheckLogger now forward message to parent logger
 *
 * Revision 1.6  2008/03/20 14:25:06  mella
 * First semantic step
 *
 * Revision 1.5  2008/03/18 13:23:55  mella
 * cosmetic changes
 *
 * Revision 1.4  2008/03/13 07:25:48  mella
 * General commit after first keywords and columns definitions
 *
 * Revision 1.3  2008/03/11 14:48:52  mella
 * commit when evening is comming
 *
 * Revision 1.2  2008/02/28 14:06:30  mella
 * Add beginning of keywords check
 *
 * Revision 1.1  2008/02/28 08:10:40  mella
 * First revision
 *
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import nom.tam.util.ArrayFuncs;

/**
 * Base Class for all OI_* tables.
 */
public class OITable extends ModelBase {

  /* constants */

  /* static descriptors */
  /** NAXIS2 keyword descriptor */
  private final static KeywordMeta KEYWORD_NAXIS2 = new KeywordMeta(OIFitsConstants.KEYWORD_NAXIS2,
          "number of table rows", Types.TYPE_INT);
  /** EXTNAME keyword descriptor */
  private final static KeywordMeta KEYWORD_EXTNAME = new KeywordMeta(OIFitsConstants.KEYWORD_EXT_NAME,
          "extension name", Types.TYPE_CHAR,
          new String[]{OIFitsConstants.TABLE_OI_ARRAY, OIFitsConstants.TABLE_OI_TARGET, OIFitsConstants.TABLE_OI_WAVELENGTH,
                       OIFitsConstants.TABLE_OI_VIS, OIFitsConstants.TABLE_OI_VIS2, OIFitsConstants.TABLE_OI_T3});
  /** EXTVER keyword descriptor */
  private final static KeywordMeta KEYWORD_EXTVER = new KeywordMeta(OIFitsConstants.KEYWORD_EXT_VER,
          "extension version", Types.TYPE_INT, 0);
  /** OI_REVN keyword descriptor */
  private final static KeywordMeta KEYWORD_OI_REVN = new KeywordMeta(OIFitsConstants.KEYWORD_OI_REVN,
          "revision number of the table definition", Types.TYPE_INT,
          new short[]{OIFitsConstants.KEYWORD_OI_REVN_1});

  /* members */
  /** Main OIFitsFile */
  private final OIFitsFile oifitsFile;
  /** Fits extension number */
  private int extNb;
  /* descriptors */
  /** Map storing keyword definitions ordered according to OIFits specification */
  private final Map<String, KeywordMeta> keywordsDesc = new LinkedHashMap<String, KeywordMeta>();
  /** Map storing column definitions ordered according to OIFits specification */
  private final Map<String, ColumnMeta> columnsDesc = new LinkedHashMap<String, ColumnMeta>();
  /* data */
  /** Map storing keyword values */
  private final Map<String, Object> keywordsValue = new HashMap<String, Object>();
  /** Map storing column values */
  private final Map<String, Object> columnsValue = new HashMap<String, Object>();

  /**
   * Public OITable class constructor
   * @param oifitsFile main OifitsFile
   */
  public OITable(final OIFitsFile oifitsFile) {
    super();
    this.oifitsFile = oifitsFile;

    // since every class constructor of OI table calls super
    // constructor, next keywords will be common to every subclass :

    // NAXIS2    keyword definition
    addKeywordMeta(KEYWORD_NAXIS2);

    // EXTNAME   keyword definition
    addKeywordMeta(KEYWORD_EXTNAME);

    // EXTVER    keyword definition
    addKeywordMeta(KEYWORD_EXTVER);

    // OI_REVN   keyword definition
    addKeywordMeta(KEYWORD_OI_REVN);
  }

  /**
   * Return the main OIFitsFile
   * @return OIFitsFile
   */
  protected final OIFitsFile getOIFitsFile() {
    return this.oifitsFile;
  }

  /**
   * Return the Map storing keyword definitions
   * @return Map storing keyword definitions
   */
  protected final Map<String, KeywordMeta> getKeywordsDesc() {
    return this.keywordsDesc;
  }

  /**
   * Return the ordered collection of keyword definitions
   * @return ordered collection of keyword definitions
   */
  protected final Collection<KeywordMeta> getKeywordDescCollection() {
    return getKeywordsDesc().values();
  }

  /**
   * Add the given keyword descriptor
   * @param meta keyword descriptor
   */
  protected final void addKeywordMeta(final KeywordMeta meta) {
    this.keywordsDesc.put(meta.getName(), meta);
  }

  /**
   * Return the Map storing column definitions
   * @return Map storing column definitions
   */
  protected final Map<String, ColumnMeta> getColumnsDesc() {
    return this.columnsDesc;
  }

  /**
   * Return the ordered collection of column definitions
   * @return ordered collection of column definitions
   */
  protected final Collection<ColumnMeta> getColumnDescCollection() {
    return getColumnsDesc().values();
  }

  /**
   * Return the number of columns
   * @return the number of columns
   */
  public final int getNbColumns() {
    return this.columnsDesc.size();
  }

  /**
   * Add the given column descriptor
   * @param meta column descriptor
   */
  protected final void addColumnMeta(final ColumnMeta meta) {
    this.columnsDesc.put(meta.getName(), meta);
  }

  /**
   * Return the Map storing keyword values
   * @return Map storing keyword values
   */
  protected final Map<String, Object> getKeywordsValue() {
    return this.keywordsValue;
  }

  /**
   * Return the keyword value given its name
   * The returned value can be null if the keyword is optional or has never been defined
   * @param name keyword name
   * @return any object value or null if undefined
   */
  protected final Object getKeywordValue(final String name) {
    return this.keywordsValue.get(name);
  }

  /**
   * Return the keyword value given its name as a String
   * @param name keyword name
   * @return String value
   */
  protected final String getKeyword(final String name) {
    return (String) getKeywordValue(name);
  }

  /**
   * Return the keyword value given its name as an integer (primitive type)
   * @param name keyword name
   * @return int value or 0 if undefined
   */
  protected final int getKeywordInt(final String name) {
    return getKeywordInt(name, 0);
  }

  /**
   * Return the keyword value given its name as an integer (primitive type)
   * @param name keyword name
   * @param def default value
   * @return int value or def if undefined
   */
  protected final int getKeywordInt(final String name, final int def) {
    final Number value = (Number) getKeywordValue(name);
    if (value == null) {
      return def;
    }
    return value.intValue();
  }

  /**
   * Return the keyword value given its name as a double (primitive type)
   * @param name keyword name
   * @return double value or 0d if undefined
   */
  protected final double getKeywordDouble(final String name) {
    return getKeywordDouble(name, 0d);
  }

  /**
   * Return the keyword value given its name as a double (primitive type)
   * @param name keyword name
   * @param def default value
   * @return double value or 0d if undefined
   */
  protected final double getKeywordDouble(final String name, final double def) {
    final Number value = (Number) getKeywordValue(name);
    if (value == null) {
      return def;
    }
    return value.doubleValue();
  }

  /**
   * Define the keyword value given its name and value
   * @param name keyword name
   * @param value any object value
   */
  protected final void setKeywordValue(final String name, final Object value) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("KEYWORD [" + name + "] = '" + value + "' [" + ((value != null) ? value.getClass().getSimpleName() : "") + "]");
    }
    this.keywordsValue.put(name, value);
  }

  /**
   * Define the keyword value given its name and value as a String
   * @param name keyword name
   * @param value a String value
   */
  protected final void setKeyword(final String name, final String value) {
    setKeywordValue(name, value);
  }

  /**
   * Define the keyword value given its name and value as a String
   * @param name keyword name
   * @param value a String value
   */
  protected final void setKeywordInt(final String name, final int value) {
    setKeywordValue(name, Integer.valueOf(value));
  }

  /**
   * Define the keyword value given its name and value as a String
   * @param name keyword name
   * @param value a String value
   */
  protected final void setKeywordDouble(final String name, final double value) {
    setKeywordValue(name, Double.valueOf(value));
  }

  /**
   * Return the Map storing column values
   * @return Map storing column values
   */
  protected final Map<String, Object> getColumnsValue() {
    return this.columnsValue;
  }

  /**
   * Return the column value given its name
   * The returned value can be null if the column has never been defined
   * @param name column name
   * @return any array value or null if undefined
   */
  protected final Object getColumnValue(final String name) {
    return this.columnsValue.get(name);
  }

  /**
   * Return the column value given its name as a String array
   * The returned value can be null if the column has never been defined
   * @param name column name
   * @return String array or null if undefined
   */
  protected final String[] getColumnString(final String name) {
    return (String[]) getColumnValue(name);
  }

  /**
   * Return the column value given its name as an integer array (short primitive type)
   * The returned value can be null if the column has never been defined
   * @param name column name
   * @return integer array or null if undefined
   */
  protected final short[] getColumnShort(final String name) {
    return (short[]) getColumnValue(name);
  }

  /**
   * Return the column value given its name as a 2D integer array (short primitive type)
   * The returned value can be null if the column has never been defined
   * @param name column name
   * @return 2D integer array or null if undefined
   */
  protected final short[][] getColumnShorts(final String name) {
    return (short[][]) getColumnValue(name);
  }

  /**
   * Return the column value given its name as a float array
   * The returned value can be null if the column has never been defined
   * @param name column name
   * @return float array or null if undefined
   */
  protected final float[] getColumnFloat(final String name) {
    return (float[]) getColumnValue(name);
  }

  /**
   * Return the column value given its name as a double array
   * The returned value can be null if the column has never been defined
   * @param name column name
   * @return double array or null if undefined
   */
  protected final double[] getColumnDouble(final String name) {
    return (double[]) getColumnValue(name);
  }

  /**
   * Return the column value given its name as a 2D double array
   * The returned value can be null if the column has never been defined
   * @param name column name
   * @return 2D double array or null if undefined
   */
  protected final double[][] getColumnDoubles(final String name) {
    return (double[][]) getColumnValue(name);
  }

  /**
   * Return the column value given its name as a 2D boolean array
   * The returned value can be null if the column has never been defined
   * @param name column name
   * @return 2D boolean array or null if undefined
   */
  protected final boolean[][] getColumnBooleans(final String name) {
    return (boolean[][]) getColumnValue(name);
  }

  /**
   * Define the column value given its name and an array value (String[] or a primitive array)
   * @param name column name
   * @param value any array value
   */
  protected final void setColumnValue(final String name, final Object value) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("COLUMN [" + name + "] = " + ((value != null) ? ArrayFuncs.arrayDescription(value) : ""));
    }
    this.columnsValue.put(name, value);
  }

  /**
   * Get the extension number
   * @return the extension number
   */
  public final int getExtNb() {
    return extNb;
  }

  /**
   * Define the extension number
   * @param extNb extension number
   */
  protected final void setExtNb(final int extNb) {
    this.extNb = extNb;
  }

  /* --- Keywords --- */
  /**
   * Get the EXTNAME keyword value
   * @return value of EXTNAME keyword
   */
  public final String getExtName() {
    return getKeyword(OIFitsConstants.KEYWORD_EXT_NAME);
  }

  /**
   * Define the EXTNAME keyword value
   * @param extName value of EXTNAME keyword
   */
  protected final void setExtName(final String extName) {
    setKeyword(OIFitsConstants.KEYWORD_EXT_NAME, extName);
  }

  /**
   * Get the EXTVER keyword value
   * @return value of EXTVER keyword
   */
  public final int getExtVer() {
    return getKeywordInt(OIFitsConstants.KEYWORD_EXT_VER);
  }

  /**
   * Define the EXTVER keyword value
   * @param extVer value of EXTVER keyword
   */
  protected final void setExtVer(final int extVer) {
    setKeywordInt(OIFitsConstants.KEYWORD_EXT_VER, extVer);
  }

  /*
   * TODO : see how to set keywords OI_REVN and NAXIS2
   */
  /**
   * Get the OI_REVN keyword value
   * @return value of OI_REVN keyword
   */
  public final int getOiRevn() {
    return getKeywordInt(OIFitsConstants.KEYWORD_OI_REVN);
  }

  /**
   * Return the number of rows
   * @return the number of rows
   */
  public final int getNbRows() {
    return getKeywordInt(OIFitsConstants.KEYWORD_NAXIS2);
  }

  /* --- Other methods --- */
  /**
   * Returns a string representation of this table
   * @return a string representation of this table
   */
  @Override
  public String toString() {
    return getExtName() + "#" + getExtNb();
  }

  /**
   * Do syntactical analysis of the table
   * @param checker checker component
   */
  public void checkSyntax(final OIFitsChecker checker) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("checkSyntax : " + this.toString());
    }
    checker.info("Analysing table [" + getExtNb() + "]: " + getExtName());

    // First analyse keywords
    checkKeywords(checker);
    // Second analyse columns
    checkColumns(checker);
  }

  /**
   * Check syntax of table's keywords.
   * It consists in checking all mandatory keywords are present, with right
   * name, right format and right values (if they do belong to a given set of
   * accepted values).
   *
   * @param checker checker component
   */
  public void checkKeywords(final OIFitsChecker checker) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("checkKeywords : " + this.toString());
    }
    String keywordName;
    Object value;

    /* Get mandatory keywords names */
    for (KeywordMeta keyword : getKeywordDescCollection()) {
      keywordName = keyword.getName();

      // get keyword value :
      value = getKeywordValue(keywordName);

      if (value == null) {
        if (keyword.isMandatory()) {
          /* No keyword with keywordName name */
          checker.severe("Missing keyword '" + keywordName + "'");
        }
      } else {
        /* Check the keyword validity */
        keyword.check(value, checker);
      }
    }
  }

  /**
   * Check syntax of table's columns.
   * It consists in checking all mandatory columns are present, with right
   * name, right format and right associated unit.
   *
   * @param checker checker component
   */
  public void checkColumns(final OIFitsChecker checker) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("checkColumns : " + this.toString());
    }
    String columnName;
    Object value;

    /* Get mandatory columns names */
    for (ColumnMeta column : getColumnDescCollection()) {
      columnName = column.getName();
      value = getColumnValue(columnName);

      if (value == null) {
        /* No column with columnName name */
        checker.severe("Missing column '" + columnName + "'");
      } else {
        /* Check the column validity */
        column.check(value, getNbRows(), checker);
      }
    }
  }

  /**
   * Return the default simple xml serialisation of the table.
   * @return the xml representation string.
   */
  public final String getXmlDesc() {
    return this.getXmlDesc(false);
  }

  /**
   * Return the default simple xml serialisation of the table.
   * @param detailled if true the result will contain the table content
   * @return the xml representation string.
   */
  public final String getXmlDesc(final boolean detailled) {
    final StringBuilder sb = new StringBuilder(1024);

    // fill the buffer :
    this.getXmlDesc(sb, detailled);

    return sb.toString();
  }

  /**
   * Fill the given buffer with the xml serialisation of the table.
   * @param sb string buffer
   * @param detailled if true the result will contain the table content
   */
  public void getXmlDesc(final StringBuilder sb, final boolean detailled) {

    sb.append("<").append(getExtName()).append(">\n");

    // Print keywords
    sb.append("<keywords>\n");

    Object val;
    for (KeywordMeta keyword : getKeywordDescCollection()) {
      val = getKeywordValue(keyword.getName());
      // skip missing keywords :
      if (val != null) {
        sb.append("<keyword><name>").append(keyword.getName()).append("</name><value>").append(val);
        sb.append("</value><description>").append(keyword.getDescription()).append("</description><type>");
        sb.append(keyword.getType()).append("</type><unit>").append(keyword.getUnit()).append("</unit></keyword>\n");
      }
    }
    sb.append("</keywords>\n");

    // Print columns
    sb.append("<columns>\n");

    for (ColumnMeta column : getColumnDescCollection()) {
      sb.append("<column><name>").append(column.getName()).append("</name>");
      sb.append("<description>").append(column.getDescription()).append("</description>");
      sb.append("<type>").append(column.getType()).append("</type>");
      sb.append("<unit>").append(column.getUnit()).append("</unit>");
      sb.append("</column>\n");
    }

    sb.append("</columns>\n");

    if (detailled) {
      sb.append("<table>\n<tr>\n");

      for (ColumnMeta column : getColumnDescCollection()) {
        sb.append("<th>").append(column.getName()).append("</th>");
      }
      sb.append("</tr>\n");

      for (int rowIndex = 0, len = getNbRows(); rowIndex < len; rowIndex++) {
        sb.append("<tr>");

        for (ColumnMeta column : getColumnDescCollection()) {
          sb.append("<td>");

          this.dumpColumnRow(column, rowIndex, sb);

          sb.append("</td>");
        }
        sb.append("</tr>\n");
      }

      sb.append("</table>\n");
    }
    sb.append("</").append(getExtName()).append(">\n");
  }

  /**
   * Append the string representation (String or array) of the column value at the given row index
   * @param column column descriptor
   * @param rowIndex row index
   * @param sb string buffer
   */
  private void dumpColumnRow(final ColumnMeta column, final int rowIndex, final StringBuilder sb) {
    switch (column.getDataType()) {
      case TYPE_CHAR:
        final String[] sValues = getColumnString(column.getName());
        // append value :
        sb.append(sValues[rowIndex]);
        break;

      case TYPE_INT:
        if (column.isArray()) {
          final short[][] iValues = getColumnShorts(column.getName());
          final short[] rowValues = iValues[rowIndex];
          // append values :
          for (int i = 0, len = rowValues.length; i < len; i++) {
            if (i > 0) {
              sb.append(" ");
            }
            sb.append(rowValues[i]);
          }
          break;
        }
        final short[] iValues = getColumnShort(column.getName());
        // append value :
        sb.append(iValues[rowIndex]);
        break;

      case TYPE_DBL:
        if (column.isArray()) {
          final double[][] dValues = getColumnDoubles(column.getName());
          final double[] rowValues = dValues[rowIndex];
          // append values :
          for (int i = 0, len = rowValues.length; i < len; i++) {
            if (i > 0) {
              sb.append(" ");
            }
            sb.append(rowValues[i]);
          }
          break;
        }
        final double[] dValues = getColumnDouble(column.getName());
        // append value :
        sb.append(dValues[rowIndex]);
        break;

      case TYPE_REAL:
        if (column.isArray()) {
          // Impossible case in OIFits
          sb.append("...");
          break;
        }
        final float[] fValues = getColumnFloat(column.getName());
        // append value :
        sb.append(fValues[rowIndex]);
        break;

      case TYPE_LOGICAL:
        if (column.isArray()) {
          final boolean[][] bValues = getColumnBooleans(column.getName());
          final boolean[] rowValues = bValues[rowIndex];
          // append values :
          for (int i = 0, len = rowValues.length; i < len; i++) {
            if (i > 0) {
              sb.append(" ");
            }
            sb.append(rowValues[i]);
          }
          break;
        }
        // Impossible case in OIFits
        sb.append("...");
        break;
      default:
        sb.append("...");
    }
  }
}
/*___oOo___*/
