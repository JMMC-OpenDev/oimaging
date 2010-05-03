/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: Types.java,v 1.3 2010-05-03 14:26:51 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.2  2010/04/29 15:46:18  bourgesl
 * added getDataType(Object)
 *
 * Revision 1.1  2010/04/28 14:45:44  bourgesl
 * meta data package with Column and Keyword descriptors, Types and Units enumeration
 *
 */
package fr.jmmc.oitools.meta;

/**
 * This enumeration describes allowed data types in the OIFits standard :
 *   - 'A' : character
 *   - 'I' : integer (16 bits)
 *   - 'E' : real    (32 bits)
 *   - 'D' : double  (64 bits)
 *   - 'L' : logical (true/false)
 * @author bourgesl
 */
public enum Types {

  /** character/date data type */
  TYPE_CHAR('A'),
  /** integer data type */
  TYPE_INT('I'),
  /** real data type */
  TYPE_REAL('E'),
  /** double data type */
  TYPE_DBL('D'),
  /** logical data type */
  TYPE_LOGICAL('L');

  /**
   * Custom constructor
   * @param representation fits char representation
   */
  private Types(final char representation) {
    this.representation = representation;
  }
  /** fits char representation */
  private final char representation;

  public char getRepresentation() {
    return representation;
  }

  /**
   * Get the data type corresponding to the given value class
   * Does not support an array value
   *
   * @param clazz value class
   *
   * @return data type if it is known, null otherwise.
   */
  public static Types getDataType(final Class<?> clazz) {

    if (clazz == String.class) {
      return Types.TYPE_CHAR;
    }
    if (clazz  ==  Double.class) {
      return Types.TYPE_DBL;
    }
    if (clazz  ==  double.class) {
      return Types.TYPE_DBL;
    }
    if (clazz  ==  Integer.class) {
      return Types.TYPE_INT;
    }
    if (clazz  ==  int.class) {
      return Types.TYPE_INT;
    }
    if (clazz  ==  short.class) {
      return Types.TYPE_INT;
    }
    if (clazz  ==  Float.class) {
      return Types.TYPE_REAL;
    }
    if (clazz  ==  float.class) {
      return Types.TYPE_REAL;
    }
    if (clazz  ==  Boolean.class) {
      return Types.TYPE_LOGICAL;
    }
    if (clazz  ==  boolean.class) {
      return Types.TYPE_LOGICAL;
    }

    return null;
  }
}
