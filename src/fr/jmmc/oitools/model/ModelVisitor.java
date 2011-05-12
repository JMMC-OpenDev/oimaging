/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: ModelVisitor.java,v 1.1 2010-08-18 14:29:33 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.model.OIFitsFile;
import fr.jmmc.oitools.model.OITable;

/**
 * This interface defines methods used by the visitor pattern
 * @author bourgesl
 */
public interface ModelVisitor {

  /**
   * Process the given OIFitsFile element with this visitor implementation
   * @param oiFitsFile OIFitsFile element to visit
   */
  public void visit(final OIFitsFile oiFitsFile);

  /**
   * Process the given OITable element with this visitor implementation
   * @param oiTable OITable element to visit
   */
  public void visit(final OITable oiTable);
}
