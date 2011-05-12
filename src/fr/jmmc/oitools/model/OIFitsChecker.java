/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: OIFitsChecker.java,v 1.2 2010-05-27 16:13:29 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.1  2010/04/29 15:47:02  bourgesl
 * use OIFitsChecker instead of CheckLogger / Handler to make OIFits validation
 *
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.validator.CheckHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains several static methods to validate the OI Fits structure (keywords, columns)
 * @author bourgesl
 */
public class OIFitsChecker {

  /* members */
  /** Specific logger dedicated to validation */
  private final Logger checkLogger;
  /** Handler associated to check logger */
  private final CheckHandler checkHandler;

  /**
   * Public constructor
   */
  public OIFitsChecker() {
    super();

    /* Build logger that will collect checking treatment results */

    // This logger is anonymous to avoid any concurrency issue ...
    checkLogger = Logger.getAnonymousLogger();
    checkLogger.setUseParentHandlers(false);

    /* Build and associate new specific handler to handle output */
    // print flag to FALSE: 
    checkHandler = new CheckHandler(false);
    checkLogger.addHandler(checkHandler);

  }

  /**
   * Return true if the FINE level is enabled
   * @return true if the FINE level is enabled
   */
  protected boolean isFineEnabled() {
    return this.checkLogger.isLoggable(Level.FINE);
  }

  /**
   * Add an information message
   * @param message information message
   */
  protected void fine(final String message) {
    this.checkLogger.fine(message);
  }

  /**
   * Add an information message
   * @param message information message
   */
  public void info(final String message) {
    this.checkLogger.info(message);
  }

  /**
   * Add a warning message
   * @param message warning message
   */
  public void warning(final String message) {
    this.checkLogger.warning(message);
  }

  /**
   * Add a severe message
   * @param message severe message
   */
  public void severe(final String message) {
    this.checkLogger.severe(message);
  }

  /*
   * Getter - Setter -----------------------------------------------------------
   */
  /**
   * Return the checkHandler that get informations about checking process.
   *
   * @return the checkHandler
   */
  public CheckHandler getCheckHandler() {
    return checkHandler;
  }

  /**
   * Get check handler status
   *
   * @return a string containing the status
   */
  public String getCheckStatus() {
    return checkHandler.getStatus();
  }

  /**
   * Get ckeck report message
   *
   * @return a string containing the analysis report
   */
  public String getCheckReport() {
    return checkHandler.getReport();
  }

  /**
   * Clear the report message informations
   */
  public void clearCheckReport() {
    checkHandler.clearReport();
  }
}
