/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: KeywordMeta.java,v 1.1 2010-04-28 14:45:44 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 */
package fr.jmmc.oitools.meta;

import java.util.logging.Logger;
import org.eso.fits.FitsKeyword;

/**
 * This class describes a FITS keyword
 * @author bourgesl
 */
public class KeywordMeta extends CellMeta {

  /**
   * KeywordMeta class constructor
   *
   * @param name keyword name
   * @param desc keyword descriptive comment
   * @param dataType keyword data type
   */
  public KeywordMeta(final String name, final String desc, final Types dataType) {
    super(MetaType.KEYWORD, name, desc, dataType, 1, NO_INT_VALUES, NO_STR_VALUES, Units.NO_UNIT);
  }

  /**
   * KeywordMeta class constructor
   *
   * @param name keyword name
   * @param desc keyword descriptive comment
   * @param dataType keyword data type
   * @param unit keyword unit
   */
  public KeywordMeta(final String name, final String desc, final Types dataType, final Units unit) {
    super(MetaType.KEYWORD, name, desc, dataType, 1, NO_INT_VALUES, NO_STR_VALUES, unit);
  }

  /**
   * KeywordMeta class constructor
   *
   * @param name keyword name
   * @param desc keyword descriptive comment
   * @param dataType keyword data type
   * @param repeat keyword cardinality
   */
  public KeywordMeta(final String name, final String desc, final Types dataType, final int repeat) {
    super(MetaType.KEYWORD, name, desc, dataType, repeat, NO_INT_VALUES, NO_STR_VALUES, Units.NO_UNIT);
  }

  /**
   * KeywordMeta class constructor with integer possible values
   *
   * @param name keyword name
   * @param desc keyword descriptive comment
   * @param dataType keyword data type
   * @param repeat keyword cardinality
   * @param acceptedValues integer possible values
   */
  public KeywordMeta(final String name, final String desc, final Types dataType,
                     final short[] acceptedValues) {
    super(MetaType.KEYWORD, name, desc, dataType, 1, acceptedValues, NO_STR_VALUES, Units.NO_UNIT);
  }

  /**
   * KeywordMeta class constructor with string possible values
   *
   * @param name keyword name
   * @param desc keyword descriptive comment
   * @param dataType keyword data type
   * @param acceptedValues string possible values
   */
  public KeywordMeta(final String name, final String desc, final Types dataType, 
                     final String[] acceptedValues) {
    super(MetaType.KEYWORD, name, desc, dataType, 1, NO_INT_VALUES, acceptedValues, Units.NO_UNIT);
  }

  /**
   * Get data type.
   * WARNING - a DATE format is converted in 'A'.
   *
   * TODO : remove Fits API dependency
   *
   * @param fType fits data type
   *
   * @return data type if it is known, '?' otherwise.
   */
  protected char getDataType(final int fType) {
    if (fType == FitsKeyword.BOOLEAN) {
      return 'L';
    }

    if (fType == FitsKeyword.INTEGER) {
      return 'I';
    }

    if (fType == FitsKeyword.REAL) {
      return 'D';
    }

    if (fType == FitsKeyword.STRING) {
      return 'A';
    }

    // Developper note: take care that DATE is down graded to STRING
    if (getName().startsWith("DATE") && (fType == FitsKeyword.DATE)) {
      return 'A';
    }

    logger.severe(getName() + " keyword type : '" + fType + "' is not supported by software");

    return '?';

    /* Not yet applicable
    FitsKeyword.COMMENT
    FitsKeyword.NONE
     */
  }

  /**
   * Check if the input keyword is valid.
   *
   * TODO : remove Fits API dependency
   *
   * @param fitsKeyword keyword to check.
   * @param logger logger associated to input keyword
   */
  public void check(FitsKeyword fitsKeyword, Logger logger) {
    logger.entering("" + this.getClass(), "checkKeyword", fitsKeyword);

    // Check type
    char kDataType = getDataType(fitsKeyword.getType());

    if (kDataType != this.getType()) {
      logger.severe("Invalid format for keyword '" + this.getName() + "', found '" + kDataType + "' should be '" + this.getType() + "'");
    }

    // Check accepted value
    checkAcceptedValues(fitsKeyword, logger);
  }

  /**
   * If any are mentionned, check keyword values are fair.
   *
   * TODO : remove Fits API dependency
   *
   * @param fitsKeyword keyword to check
   * @param l logger associated to input keyword.
   */
  private void checkAcceptedValues(FitsKeyword fitsKeyword, Logger l) {
    final short[] intAcceptedValues = getIntAcceptedValues();
    final String[] stringAcceptedValues = getStringAcceptedValues();

    if (intAcceptedValues.length != 0) {
      StringBuffer sb = new StringBuffer();

      for (int i = 0; i < intAcceptedValues.length; i++) {
        int value = fitsKeyword.getInt();

        if (value == intAcceptedValues[i]) {
          return;
        }

        sb.append("|" + intAcceptedValues[i]);
      }

      l.severe("Invalid value for keyword '" + this.getName() + "', found '" + fitsKeyword.getInt() + "' should be '" + sb.toString().substring(1) + "'");
    } else if (stringAcceptedValues.length != 0) {
      StringBuffer sb = new StringBuffer();

      for (int i = 0; i < stringAcceptedValues.length; i++) {
        String value = fitsKeyword.getString().trim();

        if (value.equals(stringAcceptedValues[i].trim())) {
          return;
        }

        sb.append("|" + stringAcceptedValues[i]);
      }

      l.severe("Invalid value for keyword '" + this.getName() + "', found '" + fitsKeyword.getString() + "' should be '" + sb.toString().substring(1) + "'");
    }
  }
}
