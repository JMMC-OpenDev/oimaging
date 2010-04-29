/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: ColumnMeta.java,v 1.2 2010-04-29 15:46:02 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2010/04/28 14:45:44  bourgesl
 * meta data package with Column and Keyword descriptors, Types and Units enumeration
 *
 */
package fr.jmmc.oitools.meta;

import java.util.logging.Logger;
import org.eso.fits.FitsColumn;

/**
 * This class describes a FITS column
 * @author bourgesl
 */
public class ColumnMeta extends CellMeta {

  /**
   * ColumnMeta class constructor with cardinality of 1 and without unit
   *
   * @param name column name
   * @param desc column descriptive comment
   * @param dataType column data type
   */
  public ColumnMeta(final String name, final String desc, final Types dataType) {
    super(MetaType.COLUMN, name, desc, dataType, 1, NO_INT_VALUES, NO_STR_VALUES, Units.NO_UNIT);
  }

  /**
   * ColumnMeta class constructor with the given cardinality
   *
   * @param name column name
   * @param desc column descriptive comment
   * @param dataType column data type
   * @param repeat column cardinality
   */
  public ColumnMeta(final String name, final String desc, final Types dataType, final int repeat) {
    super(MetaType.COLUMN, name, desc, dataType, repeat, NO_INT_VALUES, NO_STR_VALUES, Units.NO_UNIT);
  }

  /**
   * ColumnMeta class constructor with the given cardinality and unit
   *
   * @param name column name
   * @param desc column descriptive comment
   * @param dataType column data type
   * @param unit column unit
   */
  public ColumnMeta(final String name, final String desc, final Types dataType, final Units unit) {
    super(MetaType.COLUMN, name, desc, dataType, 1, NO_INT_VALUES, NO_STR_VALUES, unit);
  }

  /**
   * ColumnMeta class constructor with the given cardinality and unit
   *
   * @param name column name
   * @param desc column descriptive comment
   * @param dataType column data type
   * @param repeat column cardinality
   * @param unit column unit
   */
  public ColumnMeta(final String name, final String desc, final Types dataType, final int repeat,
                     final Units unit) {
    super(MetaType.COLUMN, name, desc, dataType, repeat, NO_INT_VALUES, NO_STR_VALUES, unit);
  }

  /**
   * ColumnMeta class constructor with the given cardinality and string possible values
   *
   * @param name column name
   * @param desc column descriptive comment
   * @param dataType column data type
   * @param repeat column cardinality
   * @param acceptedValues string possible values
   */
  public ColumnMeta(final String name, final String desc, final Types dataType, final int repeat,
                     final String[] acceptedValues) {
    super(MetaType.COLUMN, name, desc, dataType, repeat, NO_INT_VALUES, acceptedValues, Units.NO_UNIT);
  }

  /**
   * Return true if the value is multiple (array)
   * @return true if the value is multiple
   */
  public final boolean isArray() {
    return getRepeat() > 1;
  }

  /**
   * Check if the input column is valid.
   *
   * TODO : remove Fits API dependency
   *
   * @param fitsColumn column to check
   * @param noRows number of rows in the column
   * @param logger logger associated to input column
   */
  public void check(FitsColumn fitsColumn, int noRows, Logger logger) {
    logger.entering("" + this.getClass(), "checkColumn", fitsColumn);

    // Check type and cardinality
    char cDataType = fitsColumn.getDataType();
    int cRepeat = fitsColumn.getRepeat();

    if (this.getRepeat() != -1) {
      boolean severe = false;

      if (cDataType != this.getType()) {
        severe = true;
      } else {
        if (cDataType == 'A') {
          if (cRepeat < this.getRepeat()) {
            logger.warning("Invalid format for column '" + this.getName() + "', found '" + cRepeat + cDataType + "' should be '" + this.getRepeat() + this.getType() + "'");
          } else if (cRepeat > this.getRepeat()) {
            severe = true;
          }
        } else {
          if (cRepeat != this.getRepeat()) {
            severe = true;
          }
        }
      }

      if (severe) {
        logger.severe("Invalid format for column '" + this.getName() + "', found '" + cRepeat + cDataType + "' should be '" + this.getRepeat() + this.getType() + "'");
      }
    } else {
      logger.warning("Can't check repeat for column '" + this.getName() + "'");

      if (cDataType != this.getType()) {
        logger.severe("Invalid format for column '" + this.getName() + "', found '" + cDataType + "' should be '" + this.getType() + "'");
      }
    }

    // Check unit
    String cUnit = fitsColumn.getUnit();

    if (!checkUnit(cUnit)) {
      if ((cUnit == null) || (cUnit.length() == 0)) {
        logger.warning("Missing unit for column '" + this.getName() + "', should be '" + this.getUnit() + "'");
      } else {
        logger.warning("Invalid unit for column '" + this.getName() + "', found '" + cUnit + "' should be '" + this.getUnit() + "'");
      }
    }

    // Check accepted value
    checkAcceptedValues(fitsColumn, noRows, logger);
  }

  /**
   * If any are mentionned, check column values are fair.
   *
   * TODO : remove Fits API dependency
   *
   * @param fitsColumn column to check
   * @param noRows number of rows
   * @param l logger associated to input column.
   */
  private void checkAcceptedValues(FitsColumn fitsColumn, int noRows, Logger l) {
    boolean error = true;

    final short[] intAcceptedValues = getIntAcceptedValues();
    final String[] stringAcceptedValues = getStringAcceptedValues();

    if (intAcceptedValues.length != 0) {

      for (int rowNb = 0; rowNb < noRows; rowNb++) {
        int[] values = fitsColumn.getInts(rowNb);

        for (int r = 0; r < values.length; r++) {
          error = true;

          for (int i = 0; i < intAcceptedValues.length; i++) {
            if (values[r] == intAcceptedValues[i]) {
              error = false;
            }
          }

          if (error) {
            if (values.length > 1) {
              l.severe("Invalid value at index " + r + " for column '" + this.getName() + "' line " + rowNb + ", found '" + values[r] + "' should be '" + getIntAcceptedValuesAsString() + "'");
            } else {
              l.severe("Invalid value for column '" + this.getName() + "' line " + rowNb + ", found '" + values[r] + "' should be '" + getIntAcceptedValuesAsString() + "'");
            }
          }
        }
      }
    } else if (stringAcceptedValues.length != 0) {

      for (int rowNb = 0; rowNb < noRows; rowNb++) {
        String value;

        if (fitsColumn.getString(rowNb) == null) {
          error = true;
          value = "";
        } else {
          value = fitsColumn.getString(rowNb).trim();

          for (int i = 0; i < stringAcceptedValues.length; i++) {
            if (value.equals(stringAcceptedValues[i].trim())) {
              error = false;
            }
          }
        }

        if (error) {
          l.severe("Invalid value for column '" + this.getName() + "' line " + rowNb + ", found '" + value + "' should be '" + getStringAcceptedValuesAsString() + "'");
        }
      }
    }
  }

}
