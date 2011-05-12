/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: XmlOutputVisitor.java,v 1.1 2010-08-18 14:29:34 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.KeywordMeta;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Locale;

/**
 * This visitor implementation produces an XML output of the OIFits file structure
 * @author bourgesl
 */
public final class XmlOutputVisitor implements ModelVisitor {

  /* constants */
  /** US number format symbols */
  protected final static DecimalFormatSymbols US_SYMBOLS = new DecimalFormatSymbols(Locale.US);
  /** beautifier number formatter for standard values > 1e-2 and < 1e7 */
  private final static NumberFormat DF_BEAUTY_STD = new DecimalFormat("#0.###", US_SYMBOLS);
  /** beautifier number formatter for other values */
  private final static NumberFormat DF_BEAUTY_SCI = new DecimalFormat("0.###E0", US_SYMBOLS);

  /* members */
  /** use number formatter */
  private final boolean format;
  /** use verbose output */
  private boolean verbose;
  /** internal buffer */
  private StringBuilder buffer;

  /**
   * Return one xml string with file information
   * @param oiFitsFile OIFitsFile model to process
   * @param verbose if true the result will contain the table content
   * @return the xml description
   */
  public static String getXmlDesc(final OIFitsFile oiFitsFile, final boolean verbose) {
    return getXmlDesc(oiFitsFile, false, verbose);
  }

  /**
   * Return one xml string with file information
   * @param oiFitsFile OIFitsFile model to process
   * @param format flag to represent data with less accuracy but a better string representation
   * @param verbose if true the result will contain the table content
   * @return the xml description
   */
  public static String getXmlDesc(final OIFitsFile oiFitsFile, final boolean format, final boolean verbose) {
    final XmlOutputVisitor xmlSerializer = new XmlOutputVisitor(format, verbose);
    oiFitsFile.accept(xmlSerializer);
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
    this.format = format;
    this.verbose = verbose;

    this.buffer = new StringBuilder(16384);
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
    reset();
    return result;
  }

  /**
   * Process the given OIFitsFile element with this visitor implementation :
   * fill the internal buffer with file information
   * @param oiFitsFile OIFitsFile element to visit
   */
  public void visit(final OIFitsFile oiFitsFile) {
    this.buffer.append("<oifits>\n");
    if (oiFitsFile.getName() != null) {
      this.buffer.append("<filename>").append(oiFitsFile.getName()).append("</filename>\n");
    }

    // force verbosity to true :
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
    }

    // restore verbosity :
    this.verbose = verbosity;

    // data tables
    for (final OITable oiTable : oiFitsFile.getOiTables()) {
      if (oiTable instanceof OIData) {
        oiTable.accept(this);
      }
    }

    this.buffer.append("</oifits>\n");
  }

  /**
   * Process the given OITable element with this visitor implementation :
   * fill the internal buffer with table information
   * @param oiTable OITable element to visit
   */
  public void visit(final OITable oiTable) {

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
    this.buffer.append("</keywords>\n");

    // Print columns
    this.buffer.append("<columns>\n");

    final Collection<ColumnMeta> columns = oiTable.getColumnDescCollection();
    for (ColumnMeta column : columns) {
      if (oiTable.hasColumn(column)) {
        this.buffer.append("<column><name>").append(column.getName()).append("</name>");
        this.buffer.append("<description>").append(column.getDescription()).append("</description>");
        this.buffer.append("<type>").append(column.getType()).append("</type>");
        this.buffer.append("<unit>").append(column.getUnit()).append("</unit>");
        this.buffer.append("</column>\n");
      }
    }

    this.buffer.append("</columns>\n");

    if (this.verbose) {
      this.buffer.append("<table>\n<tr>\n");

      for (ColumnMeta column : columns) {
        if (oiTable.hasColumn(column)) {
          this.buffer.append("<th>").append(column.getName()).append("</th>");
        }
      }
      this.buffer.append("</tr>\n");

      for (int rowIndex = 0, len = oiTable.getNbRows(); rowIndex < len; rowIndex++) {
        this.buffer.append("<tr>");

        for (ColumnMeta column : columns) {
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
  }

  /**
   * Append the string representation (String or array) of the column value at the given row index
   * @param oiTable OITable element to use
   * @param column column descriptor
   * @param rowIndex row index
   */
  private final void dumpColumnRow(final OITable oiTable, final ColumnMeta column, final int rowIndex) {
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
}
