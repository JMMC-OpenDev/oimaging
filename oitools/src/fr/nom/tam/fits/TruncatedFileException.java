package fr.nom.tam.fits;

/*
 * Copyright: Thomas McGlynn 1997-1998.
 * This code may be used for any purpose, non-commercial
 * or commercial so long as this copyright notice is retained
 * in the source code or included in or referred to in any
 * derived software.
 * Many thanks to David Glowacki (U. Wisconsin) for substantial
 * improvements, enhancements and bug fixes.
 */
/** This exception is thrown when an EOF is detected in the middle
 * of an HDU.
 */
public class TruncatedFileException
        extends FitsException {

  /** default serial UID for Serializable interface */
  private static final long serialVersionUID = 1;

  public TruncatedFileException() {
    super();
  }

  public TruncatedFileException(String msg) {
    super(msg);
  }
}
