/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: CellMeta.java,v 1.1 2010-04-28 14:45:44 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.12  2009/09/09 06:40:19  mella
 * add getter for type and unit
 *
 * Revision 1.11  2009/08/25 12:38:12  mella
 * add description getter
 *
 * Revision 1.10  2008/10/27 16:11:57  mella
 * Add some javadoc
 *
 * Revision 1.9  2008/04/08 14:22:16  mella
 * Include Evelyne comments
 *
 * Revision 1.8  2008/03/31 07:19:48  mella
 * add error for null value
 *
 * Revision 1.7  2008/03/28 08:57:44  mella
 * check ints array of given columns
 *
 * Revision 1.6  2008/03/20 14:25:06  mella
 * First semantic step
 *
 * Revision 1.5  2008/03/18 13:32:16  mella
 * adapt DATE keyword type checking
 *
 * Revision 1.4  2008/03/14 13:54:46  mella
 * Perform accepted values of keywords
 *
 * Revision 1.3  2008/03/13 07:25:48  mella
 * General commit after first keywords and columns definitions
 *
 * Revision 1.2  2008/03/11 14:48:52  mella
 * commit when evening is comming
 *
 * Revision 1.1  2008/02/28 08:10:40  mella
 * First revision
 *
 ******************************************************************************/
package fr.jmmc.oitools.meta;

/**
 * Base class to describe table keyword and column.
 */
public class CellMeta {
  /* constants */

  /** Logger associated to meta model classes */
  protected final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
          "fr.jmmc.oitools.model");

  /** no int accepted values */
  protected final static short[] NO_INT_VALUES = new short[0];
  /** no string accepted values */
  protected final static String[] NO_STR_VALUES = new String[0];

  /** keyword or column meta type */
  public enum MetaType {

    KEYWORD,
    COLUMN;
  }

  /* members */
  /** Keyword or column type */
  private final MetaType type;
  /** Keyword/column name */
  private final String name;
  /** Keyword/column descriptive comment */
  private final String desc;
  /** Keyword/column data type */
  private final Types dataType;
  /** Cardinality of Keyword/column :
   * For a Keyword, it defines if it is optional (0/1).
   * For a Column, there are two cases :
   * - String (A) : maximum number of characters
   * - Other : dimension of the value (1 = single value, more it is an array)
   */
  private final int repeat;
  /** Keyword/column unit */
  private final Units unit;
  /** Stored integer possible values for column/keyword */
  private final short[] acceptedValuesInteger;
  /** Stored string possible values for column/keyword */
  private final String[] acceptedValuesString;

  /** 
   * CellMeta class protected constructor
   *
   * @param type keyword or column type
   * @param name keyword/column name
   * @param desc keyword/column descriptive comment
   * @param dataType keyword/column data type
   * @param repeat keyword/column cardinality
   * @param intAcceptedValues integer possible values for column/keyword
   * @param stringAcceptedValues string possible values for column/keyword
   * @param unit keyword/column unit
   */
  protected CellMeta(final MetaType type, final String name, final String desc, final Types dataType, final int repeat,
                     final short[] intAcceptedValues, final String[] stringAcceptedValues, final Units unit) {
    this.type = type;
    this.name = name;
    this.desc = desc;
    this.dataType = dataType;
    this.repeat = repeat;
    this.acceptedValuesInteger = intAcceptedValues;
    this.acceptedValuesString = stringAcceptedValues;
    this.unit = unit;
  }

  @Override
  public final boolean equals(final Object anObject) {
    if (anObject == null) {
      return false;
    }
    if (getClass() != anObject.getClass()) {
      return false;
    }
    final CellMeta other = (CellMeta) anObject;
    if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public final int hashCode() {
    if (this.name != null) {
      return this.name.hashCode();
    }
    return 3;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(64);
    switch (getMetaType()) {
      case KEYWORD:
        sb.append("KEYWORD ");
        break;
      case COLUMN:
        sb.append("COLUMN ");
        break;

      default:
    }
    sb.append("'").append(getName()).append("' ");
    sb.append("[").append(getRepeat()).append(" ").append(getDataType()).append("] ");
    if (getUnits() != Units.NO_UNIT) {
      sb.append("(").append(getUnit()).append(")");
    }
    return sb.toString();
  }

  /**
   * Get meta type.
   *
   * @return the meta type.
   */
  public final MetaType getMetaType() {
    return this.type;
  }

  /**
   * Get name.
   *
   * @return the name.
   */
  public final String getName() {
    return this.name;
  }

  /**
   * Return cell description.
   *
   * @return the cell description.
   */
  public final String getDescription() {
    return this.desc;
  }

  /**
   * Get type.
   *
   * @return the type.
   */
  public final Types getDataType() {
    return this.dataType;
  }

  /**
   * Get type.
   *
   * @return the type.
   */
  public final char getType() {
    return this.dataType.getRepresentation();
  }

  /**
   * Return units.
   *
   * @return the units.
   */
  public final Units getUnits() {
    return this.unit;
  }

  /**
   * Return cell unit.
   *
   * @return the cell unit.
   */
  public final String getUnit() {
    return this.unit.getRepresentation();
  }

  /**
   * Return true if the value is multiple (array)
   * @return true if the value is multiple
   */
  public final boolean isArray() {
    return getRepeat() > 1;
  }

  /**
   * Return the repeat value i.e. cardinality
   * Can be overriden to represent cross - references
   * @return repeat value i.e. cardinality
   */
  public int getRepeat() {
    return this.repeat;
  }

  /**
   * Return integer possible values for column/keyword
   * Can be overriden to represent cross - references
   * @return integer possible values
   */
  public short[] getIntAcceptedValues() {
    return acceptedValuesInteger;
  }

  /**
   * Return string possible values for column/keyword
   * Can be overriden to represent cross - references
   * @return string possible values for column/keyword
   */
  public String[] getStringAcceptedValues() {
    return acceptedValuesString;
  }

  /**
   * Unit analysis.
   *
   * @param unit unit to check
   * @return true if input unit is the same as what was defined, false
   * otherwise.
   */
  public final boolean checkUnit(final String unit) {
    return this.unit == Units.parseUnit(unit);
  }
}
/*___oOo___*/
