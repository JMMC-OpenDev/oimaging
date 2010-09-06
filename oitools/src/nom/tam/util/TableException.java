package nom.tam.util;
/*
 * Copyright: Thomas McGlynn 1997-1998.
 * This code may be used for any purpose, non-commercial
 * or commercial so long as this copyright notice is retained
 * in the source code or included in or referred to in any
 * derived software.
 */

public class TableException extends Exception {

  /** default serial UID for Serializable interface */
  private static final long serialVersionUID = 1;

  public TableException() {
    super();
  }

  public TableException(String msg) {
    super(msg);
  }
}
