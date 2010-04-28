/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: Types.java,v 1.1 2010-04-28 14:45:44 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
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
}
