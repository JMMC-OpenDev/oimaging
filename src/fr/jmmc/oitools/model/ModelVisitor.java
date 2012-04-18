/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
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
