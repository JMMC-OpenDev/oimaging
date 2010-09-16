/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: OITableUtils.java,v 1.1 2010-06-02 15:23:53 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 */
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.test.TestEnv;
import java.util.Arrays;
import java.util.logging.Level;
import nom.tam.util.ArrayFuncs;

/**
 *
 * @author bourgesl
 */
public class OITableUtils implements TestEnv {

  /** flag to dump column content */
  private final static boolean PRINT_COL = false;

  /**
   * Forbidden constructor
   */
  private OITableUtils() {
    super();
  }

  public static boolean compareOIFitsFile(final OIFitsFile srcOIFitsFile, final OIFitsFile destOIFitsFile) {
    boolean res = true;

    try {
      logger.info("Comparing files : " + srcOIFitsFile.getAbsoluteFilePath() + ", " + destOIFitsFile.getAbsoluteFilePath());

      if (srcOIFitsFile.getNbOiTables() != destOIFitsFile.getNbOiTables()) {
        logger.info("ERROR:  different number of hdu " + srcOIFitsFile.getNbOiTables() + " <> " + destOIFitsFile.getNbOiTables());
      } else {
        final int len = srcOIFitsFile.getNbOiTables();
        logger.info("HDUs = " + len);

        OITable srcTable, destTable;
        for (int i = 0; i < len; i++) {
          srcTable = srcOIFitsFile.getOiTable(i);
          destTable = destOIFitsFile.getOiTable(i);

          if (srcTable.getClass() != destTable.getClass()) {
            logger.info("ERROR:  different type of OI_Table " + srcTable.getClass() + " <> " + destTable.getClass());
          } else {
            res &= compareTable(srcTable, destTable);
          }
        }
      }

    } catch (Throwable th) {
      logger.log(Level.SEVERE, "compareFile : failure occured while comparing files : " + srcOIFitsFile.getAbsoluteFilePath() + ", " + destOIFitsFile.getAbsoluteFilePath(), th);
      res = false;
    }

    return res;
  }

  public static boolean compareTable(final OITable srcTable, final OITable destTable) {

    // Headers :
    boolean res = compareHeader(srcTable, destTable);

    res &= compareData(srcTable, destTable);

    return res;
  }

  private static boolean compareHeader(final OITable srcTable, final OITable destTable) {
    boolean res = true;

    final String sExtName = srcTable.getExtName();
    final String dExtName = destTable.getExtName();

    if (sExtName != null && !sExtName.equals(dExtName)) {
      logger.info("ERROR:  different extension name " + sExtName + " <> " + dExtName);
      res = false;
    } else {
      logger.info("--------------------------------------------------------------------------------");
      logger.info("EXTNAME = " + sExtName);

      final int sCard = srcTable.getKeywordsValue().size();
      final int dCard = destTable.getKeywordsValue().size();

      if (sCard != dCard) {
        logger.info("ERROR:  different number of header card " + sCard + " <> " + dCard);
        res = false;
      }
      logger.info("KEYWORDS = " + sCard);

      Object srcVal, destVal;
      String key;
      for (KeywordMeta keyword : srcTable.getKeywordDescCollection()) {
        key = keyword.getName();

        srcVal = srcTable.getKeywordValue(key);
        destVal = destTable.getKeywordValue(key);

        logger.info("KEYWORD " + key + " = " + srcVal + "\t// " + keyword.getDescription());

        if (isChanged(srcVal, destVal)) {
          logger.info("ERROR:  different value   of header card[" + key + "] '" + srcVal + "' <> '" + destVal + "'");
          res = false;
        }
      }
    }

    return res;
  }

  private static boolean isChanged(final Object value1, final Object value2) {
    return (value1 == null && value2 != null) || (value1 != null && value2 == null) || (value1 != null && value2 != null && !value1.equals(value2));
  }

  private static boolean isChanged(final String value1, final String value2) {
    return (value1 == null && value2 != null) || (value1 != null && value2 == null) || (value1 != null && value2 != null && !value1.trim().equalsIgnoreCase(value2.trim()));
  }

  private static boolean compareData(final OITable srcTable, final OITable destTable) {
    boolean res = true;

    final int sCol = srcTable.getColumnsValue().size();
    final int dCol = destTable.getColumnsValue().size();

    if (sCol != dCol) {
      logger.info("ERROR:  different number of columns " + sCol + " <> " + dCol);
      res = false;
    } else {
      logger.info("--------------------------------------------------------------------------------");
      logger.info("NCOLS = " + sCol);

      final int sRow = srcTable.getNbRows();
      final int dRow = destTable.getNbRows();

      if (sCol != dCol) {
        logger.info("ERROR:  different number of rows " + sRow + " <> " + dRow);
        res = false;
      } else {
        logger.info("NROWS = " + sRow);

        Object sArray, dArray;
        String key;
        for (ColumnMeta column : srcTable.getColumnDescCollection()) {
          key = column.getName();

          sArray = srcTable.getColumnValue(key);
          dArray = destTable.getColumnValue(key);

          if (!ArrayFuncs.arrayEquals(sArray, dArray)) {
            logger.info("ERROR:  different values for column[" + key + "]\nSRC=" + arrayToString(sArray) + "\nDST=" + arrayToString(dArray));
            res = false;
          } else {
            if (PRINT_COL) {
              logger.info("COLUMN " + key + "\t" + ArrayFuncs.arrayDescription(sArray) + "\n" + arrayToString(sArray));
            } else {
              logger.info("COLUMN " + key + "\t" + ArrayFuncs.arrayDescription(sArray));
            }
          }
        }
      }
    }

    return res;
  }

  public static String arrayToString(final Object o) {

    if (o == null) {
      return "null";
    }

    final Class<?> oClass = o.getClass();

    if (!oClass.isArray()) {
      return o.toString();
    }

    if (oClass == double[].class) {
      return Arrays.toString((double[]) o);
    } else if (oClass == float[].class) {
      return Arrays.toString((float[]) o);
    } else if (oClass == int[].class) {
      return Arrays.toString((int[]) o);
    } else if (oClass == long[].class) {
      return Arrays.toString((long[]) o);
    } else if (oClass == boolean[].class) {
      return Arrays.toString((boolean[]) o);
    } else if (oClass == short[].class) {
      return Arrays.toString((short[]) o);
    } else if (oClass == char[].class) {
      return Arrays.toString((char[]) o);
    } else if (oClass == byte[].class) {
      return Arrays.toString((byte[]) o);
    } else {
      // Non-primitive and multidimensional arrays can be
      // cast to Object[]
      final Object[] objArray = (Object[]) o;
      return Arrays.deepToString(objArray);
    }

  }
}
