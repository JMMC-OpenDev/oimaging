/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: OITable.java,v 1.3 2010-05-03 14:29:43 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import nom.tam.util.ArrayFuncs;

/**
 * Class for OI_* tables.
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
  protected final OIFitsFile oifitsFile;
  /** Map storing keyword definitions ordered according to OIFits specification */
  protected final Map<String, KeywordMeta> keywordsDesc = new LinkedHashMap<String, KeywordMeta>();
  /** Map storing column definitions ordered according to OIFits specification */
  protected final Map<String, ColumnMeta> columnsDesc = new LinkedHashMap<String, ColumnMeta>();
  /** Fits extension number */
  protected int extNb;
  /** Map storing keyword values */
  protected final Map<String, Object> keywordsValue = new HashMap<String, Object>();
  /** Map storing column values */
  protected final Map<String, Object> columnsValue = new HashMap<String, Object>();

  /**
   * OITable class constructor.
   *
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
   * Add the given keyword descriptor
   * @param meta keyword descriptor
   */
  protected final void addKeywordMeta(final KeywordMeta meta) {
    keywordsDesc.put(meta.getName(), meta);
  }

  /**
   * Return the Map storing keyword definitions
   * @return Map storing keyword definitions
   */
  protected final Map<String, KeywordMeta> getKeywordsDesc() {
    return keywordsDesc;
  }

  /**
   * Add the given column descriptor
   * @param meta column descriptor
   */
  protected final void addColumnMeta(final ColumnMeta meta) {
    columnsDesc.put(meta.getName(), meta);
  }

  /**
   * Return the Map storing column definitions
   * @return Map storing column definitions
   */
  protected final Map<String, ColumnMeta> getColumnsDesc() {
    return columnsDesc;
  }

  /**
   * Return the Map storing keyword values
   * @return Map storing keyword values
   */
  protected final Map<String, Object> getKeywordsValue() {
    return keywordsValue;
  }

  protected final void setKeywordValue(final String key, final Object value) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("KEYWORD [" + key + "] = '" + value + "' [" + ((value != null) ? value.getClass().getSimpleName() : "") + "]");
    }
    keywordsValue.put(key, value);
  }

  protected final Object getKeywordValue(final String key) {
    return keywordsValue.get(key);
  }

  protected final String getKeyword(final String key) {
    return (String) getKeywordValue(key);
  }

  protected final int getKeywordInt(final String key) {
    return ((Number) getKeywordValue(key)).intValue();
  }

  protected final double getKeywordDouble(final String key) {
    return ((Number) getKeywordValue(key)).doubleValue();
  }

  /**
   * Return the Map storing column values
   * @return Map storing column values
   */
  protected final Map<String, Object> getColumnsValue() {
    return columnsValue;
  }

  protected final void setColumnValue(final String key, final Object value) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("COLUMN [" + key + "] = " + ((value != null) ? ArrayFuncs.arrayDescription(value) : ""));
    }
    columnsValue.put(key, value);
  }

  protected final Object getColumnValue(final String key) {
    return columnsValue.get(key);
  }

  protected final String[] getColumnString(final String key) {
    return (String[]) getColumnValue(key);
  }

  protected final short[] getColumnShort(final String key) {
    return (short[]) getColumnValue(key);
  }

  protected final short[][] getColumnShorts(final String key) {
    return (short[][]) getColumnValue(key);
  }

  protected final float[] getColumnFloat(final String key) {
    return (float[]) getColumnValue(key);
  }

  protected final double[] getColumnDouble(final String key) {
    return (double[]) getColumnValue(key);
  }

  protected final double[][] getColumnDoubles(final String key) {
    return (double[][]) getColumnValue(key);
  }

  protected final boolean[][] getColumnBooleans(final String key) {
    return (boolean[][]) getColumnValue(key);
  }

  /**
   * Get extension number
   *
   * @return the extension number.
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

  /**
   * Get EXTNAME value
   *
   * @return value of EXTNAME keyword.
   */
  public final String getExtName() {
    return getKeyword(OIFitsConstants.KEYWORD_EXT_NAME);
  }

  /**
   * Get OI_REVN value
   *
   * @return value of OI_REVN keyword
   */
  public final int getOiRevn() {
    return getKeywordInt(OIFitsConstants.KEYWORD_OI_REVN);
  }

  /**
   * Return the number of rows.
   *
   * @return the number of rows.
   */
  public final int getNbRows() {
    return getKeywordInt(OIFitsConstants.KEYWORD_NAXIS2);
  }

  /**
   * Returns a string representation of this component.
   *
   * @return a string representation of this component.
   */
  @Override
  public String toString() {
    return getExtName() + "#" + getExtNb();
  }

  /**
   * Do syntactical analysis of the table.
   *
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
    for (KeywordMeta keyword : keywordsDesc.values()) {
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
    for (ColumnMeta c : columnsDesc.values()) {
      columnName = c.getName();
      value = getColumnValue(columnName);

      if (value == null) {
        /* No column with columnName name */
        checker.severe("Missing column '" + columnName + "'");
      } else {
        /* Check the column validity */
        c.check(value, getNbRows(), checker);
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
    for (KeywordMeta keyword : keywordsDesc.values()) {
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

    for (ColumnMeta column : columnsDesc.values()) {
      sb.append("<column><name>").append(column.getName()).append("</name>");
      sb.append("<description>").append(column.getDescription()).append("</description>");
      sb.append("<type>").append(column.getType()).append("</type>");
      sb.append("<unit>").append(column.getUnit()).append("</unit>");
      sb.append("</column>\n");
    }

    sb.append("</columns>\n");

    if (detailled) {
      sb.append("<table>\n<tr>\n");

      for (ColumnMeta column : columnsDesc.values()) {
        sb.append("<th>").append(column.getName()).append("</th>");
      }
      sb.append("</tr>\n");

      for (int rowIndex = 0, len = getNbRows(); rowIndex < len; rowIndex++) {
        sb.append("<tr>");

        for (ColumnMeta column : columnsDesc.values()) {
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
