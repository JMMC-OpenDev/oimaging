/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: OIFitsFile.java,v 1.1 2010-04-28 14:47:38 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.24  2009/09/20 15:55:09  mella
 * Add new getter
 *
 * Revision 1.23  2009/08/25 13:41:01  mella
 * fix bug
 *
 * Revision 1.22  2009/08/25 13:00:18  mella
 * add minimal implementation to output xml onto data tables
 *
 * Revision 1.21  2009/04/10 07:10:03  mella
 * Add check one table that get one arrname without corresponding oi_array table
 *
 * Revision 1.20  2009/03/21 10:20:36  mella
 * Add todo
 *
 * Revision 1.19  2009/03/20 09:16:42  mella
 * Fix exception throwing
 *
 * Revision 1.18  2009/03/17 11:49:04  mella
 * Be more precise on throwed exceptions
 *
 * Revision 1.17  2009/03/17 08:09:34  mella
 * Improve logged message
 *
 * Revision 1.16  2009/03/16 15:41:37  mella
 * Find the difference ;)
 *
 * Revision 1.15  2009/03/09 10:27:24  mella
 * Add spacialFreq and spacialCoord getter
 *
 * Revision 1.14  2009/01/06 13:30:51  mella
 * GuiValidator clear report before analysis display
 *
 * Revision 1.13  2008/10/28 08:37:17  mella
 * Add javadoc
 *
 * Revision 1.12  2008/09/01 12:20:43  mella
 * Improve code when no Oitarget is present
 *
 * Revision 1.11  2008/04/08 14:22:16  mella
 * Include Evelyne comments
 *
 * Revision 1.10  2008/04/07 07:29:03  mella
 * Add nb of errors  in log file
 *
 * Revision 1.9  2008/04/01 11:24:44  mella
 * Add fine log to be able to collect data offline
 *
 * Revision 1.8  2008/04/01 07:40:17  mella
 * fix simpler logging usage
 *
 * Revision 1.7  2008/03/31 10:39:24  mella
 * remove debug code which saves fits file at end of check
 *
 * Revision 1.6  2008/03/31 08:05:58  mella
 * Add quick and dirty support of url and gzipped files
 *
 * Revision 1.5  2008/03/28 09:00:29  mella
 * - Add common log handler
 * - Add accessor method for acceptedStaIndexes
 *
 * Revision 1.4  2008/03/20 14:25:06  mella
 * First semantic step
 *
 * Revision 1.3  2008/03/14 12:52:11  mella
 * Add cardinality check
 *
 * Revision 1.2  2008/03/11 14:48:52  mella
 * commit when evening is comming
 *
 * Revision 1.1  2008/02/28 08:10:40  mella
 * First revision
 *
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.validator.CheckHandler;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents the data model of an OIFits standard file.
 */
public final class OIFitsFile extends OIFits {
  /* constants */

  /* members */
  /** absolute file path */
  private String absoluteFilePath = null;
  /** Storage of oi tables extension names : TODO : is it really useful ? */
  protected final LinkedList<String> extNames = new LinkedList<String>();
  /** Hashtable connecting each ARRNAME keyword value with associated OI_ARRAY table */
  protected final Map<String, List<OIArray>> arrNameToOiArray = new HashMap<String, List<OIArray>>();
  /** Hashtable connecting each target identifier with associated target name */
  protected final Map<Integer, String> targetIdToTargetName = new HashMap<Integer, String>();
  /** Hashtable connecting each INSNAME keyword value with associated OI_WAVELENGTH table */
  protected final Map<String, List<OIWavelength>> insNameToOiWavelength = new HashMap<String, List<OIWavelength>>();

  /**
   * Public constructor
   */
  public OIFitsFile() {
    super();
  }

  /**
   * Public constructor
   *
   * @param absoluteFilePath absolute file path
   */
  public OIFitsFile(final String absoluteFilePath) {
    super();
    setAbsoluteFilePath(absoluteFilePath);
  }

  /**
   * Register OI_* tables.
   *
   * @param oiTable reference on one OI_* table
   */
  protected void registerOiTable(final OITable oiTable) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("Registering object for " + oiTable.getExtName());
    }
    this.extNames.add(oiTable.getExtName());
    this.oiTables.add(oiTable);

    if (oiTable instanceof OITarget) {
      oiTargets.add((OITarget) oiTable);
    } else if (oiTable instanceof OIWavelength) {
      OIWavelength o = (OIWavelength) oiTable;
      String insName = o.getInsName();

      if (insName != null) {
        List<OIWavelength> v = insNameToOiWavelength.get(insName);

        if (v == null) {
          v = new LinkedList<OIWavelength>();
          insNameToOiWavelength.put(insName, v);
        }

        v.add((OIWavelength) oiTable);
      } else {
        logger.warning("INSNAME of OI_WAVELENGTH table is null during building step");
      }
      oiWavelengths.add(o);
    } else if (oiTable instanceof OIArray) {
      OIArray o = (OIArray) oiTable;
      String arrName = o.getArrName();

      if (arrName != null) {
        List<OIArray> v = arrNameToOiArray.get(arrName);

        if (v == null) {
          v = new LinkedList<OIArray>();
          arrNameToOiArray.put(arrName, v);
        }

        v.add((OIArray) oiTable);
      } else {
        logger.warning("ARRNAME of OI_ARRAY table is null during building step");
      }
      oiArrays.add(o);
    } else if (oiTable instanceof OIVis) {
      oiVisTables.add((OIVis) oiTable);
    } else if (oiTable instanceof OIVis2) {
      oiVis2Tables.add((OIVis2) oiTable);
    } else if (oiTable instanceof OIT3) {
      oiT3Tables.add((OIT3) oiTable);
    }
  }

  /** 
   * Mediator method to resolve cross references. Returns OiArray associated
   * to input parameter
   *
   * @param arrName string containing ARRNAME value
   * @return the OI_ARRAY table reference associated. If none is associated,
   *  returns NULL
   */
  public OIArray getOiArray(final String arrName) {
    final List<OIArray> v = arrNameToOiArray.get(arrName);

    if (v == null) {
      return null;
    }

    return v.get(0);
  }

  /**
   * Mediator method to resolve cross references. Returns OiWavelength
   * associated to input parameter.
   *
   * @param insName string containing INSNAME value
   * @return the OI_WAVELENGTH table reference associated. If none is
   *  associated, returns NULL
   */
  public OIWavelength getOiWavelength(final String insName) {
    List<OIWavelength> v = insNameToOiWavelength.get(insName);

    if (v == null) {
      return null;
    }

    return v.get(0);
  }

  /**
   * Mediator method to resolve cross references. Returns the accepted (ie
   * valid) station indexes.
   *
   * @param oiArray OiArray where station indexes are defined
   * @return the array containing the indexes.
   */
  public short[] getAcceptedStaIndexes(final OIArray oiArray) {
    if (oiArray == null) {
      return EMPTY_SHORT_ARRAY;
    }

    return oiArray.getAcceptedStaIndexes();
  }

  /**
   * Get all INSNAME values already defined.
   *
   * @return an string array containing all accepted values.
   */
  public String[] getAcceptedInsNames() {
    final int len = insNameToOiWavelength.size();
    if (len == 0) {
      return EMPTY_STRING;
    }
    return insNameToOiWavelength.keySet().toArray(new String[len]);
  }

  /**
   * Get all ARRNAME values already defined.
   *
   * @return an string array containing all accepted values.
   */
  public String[] getAcceptedArrNames() {
    final int len = arrNameToOiArray.size();
    if (len == 0) {
      return EMPTY_STRING;
    }
    return arrNameToOiArray.keySet().toArray(new String[len]);
  }

  /** Get all target identifiers defined, and then valid.
   *
   * @return an integer array containing all accepted values.
   */
  public short[] getAcceptedTargetIds() {
    final OITarget oiTarget = getOiTarget();
    if (oiTarget == null) {
      return EMPTY_SHORT_ARRAY;
    }

    return oiTarget.getAcceptedTargetIds();
  }

  /** Return a short description of OIFITS content. */
  @Override
  public String toString() {
    return "\nextNames:" + extNames + "\narrNameToOiArray:" + arrNameToOiArray + "\ntargetIdToTargetName:" + targetIdToTargetName + "\ninsNameToOiWavelength:" + insNameToOiWavelength + "\n";
  }

  /**
   * Check the global structure of oifits file, including table presence and
   * syntax correction.
   *
   * @param checkLogger validation logger
   * @param checkHandler validation handler
   */
  public void check(final Logger checkLogger, final CheckHandler checkHandler) {
    logger.finest("Checking mandatory tables");

    /* Checking presence of one and only one OI_TARGET table */
    if (oiTargets.isEmpty()) {
      checkLogger.severe(
              "No OI_TARGET table found: one and only one must be present");
    }

    /* Checking presence of at least one OI_WAVELENGTH table */
    if (insNameToOiWavelength.isEmpty()) {
      checkLogger.severe(
              "No OI_WAVELENGTH table found: one or more must be present");
    }

    /* Starting syntactical analysis */
    logger.finest("Building list of table for keywords analysis");

    for (int i = 0; i < getNbOiTables(); i++) {
      OITable table = getOiTable(i);
      table.checkSyntax(checkLogger);
    }

    // Collect some data for post analysis (at fine level)
    // post analysis is done by one external tool that read the xml log
    if (checkLogger.isLoggable(Level.FINE)) {
      if (getOiTarget() != null) {
        checkLogger.fine("NBTARGETS");
        checkLogger.fine("" + getOiTarget().getNbTargets());
      }

      for (int i = 0; i < getNbOiTables(); i++) {
        OITable table = getOiTable(i);
        checkLogger.fine("TABLENAME");
        checkLogger.fine(table.getExtName());
      }

      Iterator<String> e = arrNameToOiArray.keySet().iterator();

      while (e.hasNext()) {
        checkLogger.fine("ARRNAME");
        checkLogger.fine(e.next());
      }

      e = insNameToOiWavelength.keySet().iterator();

      while (e.hasNext()) {
        checkLogger.fine("INSNAME");
        checkLogger.fine(e.next());
      }

      checkLogger.fine("WARNINGS");
      checkLogger.fine("" + checkHandler.getNbWarnings());

      checkLogger.fine("SEVERES");
      checkLogger.fine("" + checkHandler.getNbSeveres());
    }
  }

  /** Check validity of cross references of non-data tables, ie check both
   * tables have different identifiers, or no mandatory identifier is not
   * defined.
   *
   * @param oiTable reference on table to check
   * @param logger logger associated to this table.
   */
  public void checkCrossRefering(OITable oiTable, Logger logger) {
    if (logger.isLoggable(Level.FINE)) {
      logger.fine("Checking cross references for " + oiTable.getExtName());
    }

    if (oiTable instanceof OITarget) {
      OITarget o = (OITarget) oiTable;

      if (o.getNbTargets() < 1) {
        logger.severe("No target defined");
      }
    }

    /* Check for OiWavelength table */
    if (oiTable instanceof OIWavelength) {
      OIWavelength o = (OIWavelength) oiTable;
      String insName = o.getInsName();

      if (insName != null) {
        /* Get OiWavelength asseociated to INSNAME value */
        List<OIWavelength> v = insNameToOiWavelength.get(insName);

        if (v == null) {
          /* Problem: INSNAME value has not been encoutered during
           * building step, that should be impossible */
          logger.severe("invalid INSNAME identifier");
        } else {
          if (v.size() > 1) {
            /* Problem: more that one OiWavelength table associated
             * to INSNAME value, that is strictly forbiden */
            final StringBuilder sb = new StringBuilder();

            for (Iterator<OIWavelength> it = v.iterator(); it.hasNext();) {
              o = it.next();
              sb.append("|").append(o.getExtNb());
            }

            if (logger.isLoggable(Level.SEVERE)) {
              logger.severe("OI_WAVELENGTH tables [" + sb.toString().substring(1) + "] are identified by same INSNAME='" + o.getInsName() + "'");
            }
          }
        }
      } else {
        /* Problem: INSNAME value is "", that should not be possible */
        logger.severe(
                "INSNAME identifier is missing during reference checking step");
      }
    }

    if (oiTable instanceof OIArray) {
      OIArray o = (OIArray) oiTable;
      String arrName = o.getArrName();

      if (arrName != null) {
        /* Get OiArray asseociated to ARRNAME value */
        List<OIArray> v = arrNameToOiArray.get(arrName);

        if (v == null) {
          /* Problem: ARRNAME value has not been encoutered during
           * building step, that should be impossible */
          logger.severe("invalid ARRNAME identifier");
        } else {
          if (v.size() > 1) {
            /* Problem: more that one OiArray table associated
             * to ARRNAME value, that is strictly forbiden */
            final StringBuilder sb = new StringBuilder();

            for (Iterator<OIArray> it = v.iterator(); it.hasNext();) {
              o = it.next();
              sb.append("|").append(o.getExtNb());
            }

            if (logger.isLoggable(Level.SEVERE)) {
              logger.severe("OI_ARRAY tables [" + sb.toString().substring(1) + "] are identified by same ARRNAME='" + o.getArrName() + "'");
            }
          }
        }
      } else {
        /* Problem: ARRNAME value is "", that should not be possible */
        logger.severe(
                "ARRNAME identifier is missing during reference checking step");
      }
    }
  }

  /**
   * Return one xml string with file information.
   *
   * @return the xml description
   */
  public final String getXmlDesc() {
    return this.getXmlDesc(false);
  }

  /**
   * Return one xml string with file information.
   *
   * @param detailled if true the result will contain the table content
   * @return the xml description
   */
  public final String getXmlDesc(final boolean detailled) {
    final StringBuilder sb = new StringBuilder(16384);

    // fill the buffer :
    this.getXmlDesc(sb, detailled);

    if (logger.isLoggable(Level.FINEST)) {
      logger.finest("xmlDesc buffer = " + sb.length());
    }

    return sb.toString();
  }

  /**
   * Fill the given buffer with file information.
   *
   * @param sb string buffer
   * @param detailled if true the result will contain the table content
   */
  public final void getXmlDesc(final StringBuilder sb, final boolean detailled) {
    sb.append("<oifits>\n");
    sb.append("<filename>").append(getName()).append("</filename>\n");

    String[] strings;
    OITable t;

    // arrnames
    sb.append("<arrnames>");
    strings = getAcceptedArrNames();
    for (int i = 0, len = strings.length; i < len; i++) {
      t = getOiArray(strings[i]);
      if (t != null) {
        t.getXmlDesc(sb, true);
      }
    }
    sb.append("</arrnames>\n");

    // insnames
    sb.append("<insnames>");
    strings = getAcceptedInsNames();
    for (int i = 0, len = strings.length; i < len; i++) {
      t = getOiWavelength(strings[i]);
      if (t != null) {
        t.getXmlDesc(sb, detailled);
      }
    }
    sb.append("</insnames>\n");

    // targets
    final OITarget oiTarget = getOiTarget();
    if (oiTarget != null) {
      oiTarget.getXmlDesc(sb, detailled);
    }

    // data tables
    for (OITable oiTable : getOiTables()) {
      if (oiTable instanceof OIData) {
        oiTable.getXmlDesc(sb, detailled);
      }
    }

    sb.append("</oifits>\n");
  }

  /*
   * Getter - Setter -----------------------------------------------------------
   */
  /**
   * Get the name of this OIFits file.
   *
   *  @return a string containing the name of the OIFits file.
   */
  public String getName() {
    return getAbsoluteFilePath();
  }

  /**
   * Return the absolute file path
   * @return absolute file path or null if the file does not exist
   */
  public String getAbsoluteFilePath() {
    return absoluteFilePath;
  }

  /**
   * Define the absolute file path
   * @param absoluteFilePath absolute file path
   */
  public void setAbsoluteFilePath(final String absoluteFilePath) {
    this.absoluteFilePath = absoluteFilePath;
  }
}
/*___oOo___*/
