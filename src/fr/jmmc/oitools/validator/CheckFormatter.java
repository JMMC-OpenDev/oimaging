/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.validator;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * This class formats a validation message
 */
public final class CheckFormatter extends Formatter {

  /** Get level and message as a string from a record.
   *
   * @param record LogRecord from which level and message are extracted.
   *
   * @return a string containing level and message.
   */
  public final String format(final LogRecord record) {
    return record.getLevel().getName() + "\t" + record.getMessage();
  }
}
/*___oOo___*/
