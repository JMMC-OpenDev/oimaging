/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: CheckFormatter.java,v 1.1 2010-04-28 14:46:28 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.3  2008/10/28 07:43:21  mella
 * jalopization
 *
 * Revision 1.2  2008/04/08 14:22:16  mella
 * Include Evelyne comments
 *
 * Revision 1.1  2008/03/31 14:14:33  mella
 * First revision
 *
 *
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
