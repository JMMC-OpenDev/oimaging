/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: OIFits.java,v 1.1 2010-04-28 14:47:37 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 */
package fr.jmmc.oitools.model;

import java.util.LinkedList;
import java.util.List;

/**
 * This class represents the data model of an OIFits standard file.
 * @author bourgesl
 */
public class OIFits extends ModelBase {
  /* constants */

  /* members */

  /* OIFits structure */
  /** Storage of oi table references */
  protected final List<OITable> oiTables = new LinkedList<OITable>();

  /* meta data */
  /** List storing OI_TARGET table */
  protected final List<OITarget> oiTargets = new LinkedList<OITarget>();
  /** List storing OI_ARRAY table */
  protected final List<OIArray> oiArrays = new LinkedList<OIArray>();
  /** List storing OI_WAVELENGTH table */
  protected final List<OIWavelength> oiWavelengths = new LinkedList<OIWavelength>();

  /* data tables */
  /** Storage of OI_VIS table references */
  protected final List<OIVis> oiVisTables = new LinkedList<OIVis>();
  /** Storage of OI_VIS2 table references */
  protected final List<OIVis2> oiVis2Tables = new LinkedList<OIVis2>();
  /** Storage of OI_T3 table references */
  protected final List<OIT3> oiT3Tables = new LinkedList<OIT3>();

  /**
   * Public constructor
   */
  public OIFits() {
    super();
  }

  /**
   * Get number of OI_* tables in oifits file
   *
   * @return the number of OI_* tables
   * @see #getOiTable(int)
   */
  public int getNbOiTables() {
    return oiTables.size();
  }

  public OITable getOiTable(final int index) {
    return oiTables.get(index);
  }

  public OITable[] getOiTables() {
    return oiTables.toArray(new OITable[oiTables.size()]);
  }

  public OITarget getOiTarget() {
    if (oiTargets.isEmpty()) {
      return null;
    } else {
      return oiTargets.get(0);
    }
  }

  public OIArray[] getOiArrays() {
    return oiArrays.toArray(new OIArray[oiArrays.size()]);
  }

  public OIWavelength[] OiWavelengths() {
    return oiWavelengths.toArray(new OIWavelength[oiWavelengths.size()]);
  }

  public OIVis[] getOiVis() {
    return oiVisTables.toArray(new OIVis[oiVisTables.size()]);
  }

  public OIVis2[] getOiVis2() {
    return oiVis2Tables.toArray(new OIVis2[oiVis2Tables.size()]);
  }

  public OIT3[] getOiT3() {
    return oiT3Tables.toArray(new OIT3[oiT3Tables.size()]);
  }

  /**
   * Tells if the file contains some OI_ARRAY tables.
   * @return true if the file contains some OI_ARRAY table
   */
  public boolean hasOiArray() {
    return !oiArrays.isEmpty();
  }

  /**
   * Tells if the file contains some OI_VIS tables.
   * @return true if the file contains some OI_VIS table
   */
  public boolean hasOiVis() {
    return !oiVisTables.isEmpty();
  }

  /**
   * Tell if the file contains some OI_VIS2 tables.
   * @return true if the file contains some OI_VIS2 table
   */
  public boolean hasOiVis2() {
    return !oiVis2Tables.isEmpty();
  }

  /**
   * Tells if the file contains some OI_T3 tables.
   * @return true if the file contains some OI_T3 table
   */
  public boolean hasOiT3() {
    return !oiT3Tables.isEmpty();
  }
}
