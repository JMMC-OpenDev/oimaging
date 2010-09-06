/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: CheckHandler.java,v 1.1 2010-04-28 14:46:28 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.6  2009/01/06 13:30:51  mella
 * GuiValidator clear report before analysis display
 *
 * Revision 1.5  2008/10/28 07:45:56  mella
 * Add javadoc
 *
 * Revision 1.4  2008/04/08 14:22:16  mella
 * Include Evelyne comments
 *
 * Revision 1.3  2008/04/07 07:28:07  mella
 * Add getter method to get number of errors
 *
 * Revision 1.2  2008/04/01 07:37:18  mella
 * Set default logLevel to INFO
 *
 * Revision 1.1  2008/03/31 14:14:33  mella
 * First revision
 *
 *
 ******************************************************************************/
package fr.jmmc.oitools.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * This class stores the checking results.
 */
public class CheckHandler extends Handler {

  /* constants */
  /** Formatter associated to checkHandler */
  private final static Formatter formatter = new CheckFormatter();

  /* members */
  /** Vector storing all records */
  private final List<LogRecord> records;
  /** Boolean set to true if messages are to be printed */
  private final boolean printFlag;

  /** 
   * CheckHandler class constructor.
   */
  public CheckHandler() {
    this(false);
  }

  /**
   * CheckHandler class constructor.
   *
   * @param printFlag boolean indicating if messages are to be printed.
   */
  public CheckHandler(final boolean printFlag) {
    this.printFlag = printFlag;
    this.records = new ArrayList<LogRecord>();
    setFormatter(formatter);
    setLevel(Level.INFO);
  }

  /** 
   * Close the CheckHandler and free all associated ressources
   */
  public void close() {
  }

  /** 
   * Flush any buffered output
   */
  public void flush() {
  }

  /**
   * Publish a LogRecord.
   *
   * @param record LogRecord to be published.
   */
  public void publish(final LogRecord record) {
    if (isLoggable(record)) {
      records.add(record);

      if (printFlag) {
        if (record.getLevel() == Level.INFO) {
          System.out.println();
        }

        System.out.println(getFormatter().format(record));
      }
    }
  }

  /** 
   * Get number of warning errors
   * @return number of warning errors
   */
  public int getNbWarnings() {
    int warnings = 0;

    LogRecord record;
    for (int i = 0, len = records.size(); i < len; i++) {
      record = records.get(i);

      if (record.getLevel() == Level.WARNING) {
        warnings++;
      }
    }

    return warnings;
  }

  /**
   * Get number of severe errors
   * @return number of severe errors 
   */
  public int getNbSeveres() {
    int severes = 0;

    LogRecord record;
    for (int i = 0, len = records.size(); i < len; i++) {
      record = records.get(i);

      if (record.getLevel() == Level.SEVERE) {
        severes++;
      }
    }

    return severes;
  }

  /**
   * Return a simple text report that log past check.
   *
   * @return the string with report content.
   */
  public String getReport() {
    final StringBuilder sb = new StringBuilder(256);

    LogRecord record;
    for (int i = 0, len = records.size(); i < len; i++) {
      record = records.get(i);

      sb.append(getFormatter().format(record)).append("\n");
    }
    sb.append("\n").append(getStatus());

    return sb.toString();
  }

  /**
   * Return a simple string that show numbers of warnings and severe errors.
   *
   * @return a string with number of warnings and severe errors.
   */
  public String getStatus() {
    int warnings = 0;
    int severes = 0;

    LogRecord record;
    for (int i = 0, len = records.size(); i < len; i++) {
      record = records.get(i);

      if (record.getLevel() == Level.WARNING) {
        warnings++;
      } else if (record.getLevel() == Level.SEVERE) {
        severes++;
      }
    }

    return warnings + " warnings, " + severes + " severe errors";
  }

  /**
   * Clear previously collected report informations
   */
  public void clearReport() {
    records.clear();
  }
}
/*___oOo___*/
