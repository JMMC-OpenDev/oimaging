/*******************************************************************************
 * JMMC project
 *
 * "@(#) $Id: OIData.java,v 1.4 2010-05-27 16:13:29 bourgesl Exp $"
 *
 * History
 * -------
 * $Log: not supported by cvs2svn $
 * Revision 1.3  2010/05/03 14:25:36  bourgesl
 * removed comment
 *
 * Revision 1.2  2010/04/29 15:47:01  bourgesl
 * use OIFitsChecker instead of CheckLogger / Handler to make OIFits validation
 *
 * Revision 1.1  2010/04/28 14:47:38  bourgesl
 * refactored OIValidator classes to represent the OIFits data model
 *
 * Revision 1.11  2009/09/08 16:10:42  mella
 * add same block for all oitable after optionnal specific data
 *
 * Revision 1.10  2009/08/25 13:00:18  mella
 * add minimal implementation to output xml onto data tables
 *
 * Revision 1.9  2009/04/10 07:10:03  mella
 * Add check one table that get one arrname without corresponding oi_array table
 *
 * Revision 1.8  2009/04/08 08:42:43  mella
 * improve optional ARRNAME keyword check
 *
 * Revision 1.7  2009/04/08 08:40:56  mella
 * Fix optional ARRNAME keyword check
 *
 * Revision 1.6  2009/03/09 10:27:24  mella
 * Add spacialFreq and spacialCoord getter
 *
 * Revision 1.5  2009/01/06 13:29:14  mella
 * Add getFlags()
 *
 * Revision 1.4  2008/10/28 08:21:11  mella
 * Add javadoc
 *
 * Revision 1.3  2008/04/08 14:22:16  mella
 * Include Evelyne comments
 *
 * Revision 1.2  2008/04/01 07:37:37  mella
 * fix bad log level
 *
 * Revision 1.1  2008/03/31 14:14:33  mella
 * First revision
 *
 * Revision 1.3  2008/03/13 07:25:48  mella
 * General commit after first keywords and columns definitions
 *
 * Revision 1.2  2008/03/11 14:48:52  mella
 * commit when evening is comming
 *
 * Revision 1.1  2008/02/28 08:10:40  mella
 * First revision
 *
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.OIFitsConstants;
import fr.jmmc.oitools.meta.ColumnMeta;
import fr.jmmc.oitools.meta.KeywordMeta;
import fr.jmmc.oitools.meta.Types;
import fr.jmmc.oitools.meta.Units;
import java.util.logging.Level;

/**
 * OIData table is the base class for OI_VIS, OI_VIS2 and OI_T3 tables.
 */
public class OIData extends OITable {

  /* constants */
  /* static descriptors */
  /** DATE-OBS keyword descriptor */
  private final static KeywordMeta KEYWORD_DATE_OBS = new KeywordMeta(OIFitsConstants.KEYWORD_DATE_OBS,
          "UTC start date of observations", Types.TYPE_CHAR);
  /** TIME column descriptor */
  private final static ColumnMeta COLUMN_TIME = new ColumnMeta(OIFitsConstants.COLUMN_TIME,
          "UTC time of observation", Types.TYPE_DBL, Units.UNIT_SECOND);
  /** MJD column descriptor */
  private final static ColumnMeta COLUMN_MJD = new ColumnMeta(OIFitsConstants.COLUMN_MJD,
          "modified Julian Day", Types.TYPE_DBL, Units.UNIT_MJD);
  /** INT_TIME column descriptor */
  private final static ColumnMeta COLUMN_INT_TIME = new ColumnMeta(OIFitsConstants.COLUMN_INT_TIME,
          "integration time", Types.TYPE_DBL, Units.UNIT_SECOND);
  /** UCOORD column descriptor */
  protected final static ColumnMeta COLUMN_UCOORD = new ColumnMeta(OIFitsConstants.COLUMN_UCOORD,
          "U coordinate of the data", Types.TYPE_DBL, Units.UNIT_METER);
  /** VCOORD column descriptor */
  protected final static ColumnMeta COLUMN_VCOORD = new ColumnMeta(OIFitsConstants.COLUMN_VCOORD,
          "V coordinate of the data", Types.TYPE_DBL, Units.UNIT_METER);
  /** members */
  /** cached reference on OI_ARRAY table associated to this OIData table */
  private OIArray oiArrayRef = null;
  /** cached reference on OI_WAVELENGTH table associated to this OIData table */
  private OIWavelength oiWavelengthRef = null;

  /**
   * OIData class contructor
   *
   * @param oifitsFile main OifitsFile
   */
  public OIData(final OIFitsFile oifitsFile) {
    super(oifitsFile);

    // since every class constructor of OI_VIS, OI_VIS2, OI_T3 calls super
    // constructor, next keywords will be common to every subclass :

    // DATE-OBS  keyword definition
    addKeywordMeta(KEYWORD_DATE_OBS);

    // ARRNAME  Optional keyword definition
    addKeywordMeta(new KeywordMeta(OIFitsConstants.KEYWORD_ARRNAME, "name of corresponding array", Types.TYPE_CHAR, 0) {

      @Override
      public String[] getStringAcceptedValues() {
        return getOIFitsFile().getAcceptedArrNames();
      }
    });

    // INSNAME  keyword definition
    addKeywordMeta(new KeywordMeta(OIFitsConstants.KEYWORD_INSNAME, "name of corresponding detector", Types.TYPE_CHAR) {

      @Override
      public String[] getStringAcceptedValues() {
        return getOIFitsFile().getAcceptedInsNames();
      }
    });

    // TARGET_ID  column definition
    addColumnMeta(new ColumnMeta(OIFitsConstants.COLUMN_TARGET_ID,
            "target number as index into OI_TARGET table", Types.TYPE_INT) {

      @Override
      public short[] getIntAcceptedValues() {
        return getOIFitsFile().getAcceptedTargetIds();
      }
    });

    // TIME  column definition
    addColumnMeta(COLUMN_TIME);

    // MJD  column definition
    addColumnMeta(COLUMN_MJD);

    // INT_TIME  column definition
    addColumnMeta(COLUMN_INT_TIME);
  }

  /**
   * Return the number of measurements in this table.
   * @return the number of measurements.
   */
  public final int getNbMeasurements() {
    return getNbRows();
  }

  /* --- Keywords --- */
  /**
   * Get the DATE-OBS keyword value.
   * @return the value of DATE-OBS keyword
   */
  public final String getDateObs() {
    return getKeyword(OIFitsConstants.KEYWORD_DATE_OBS);
  }

  /**
   * Define the DATE-OBS keyword value
   * @param dateObs value of DATE-OBS keyword
   */
  public final void setDateObs(final String dateObs) {
    setKeyword(OIFitsConstants.KEYWORD_DATE_OBS, dateObs);
  }

  /**
   * Return the Optional ARRNAME keyword value.
   * @return the value of ARRNAME keyword if present, NULL otherwise.
   */
  public final String getArrName() {
    return getKeyword(OIFitsConstants.KEYWORD_ARRNAME);
  }

  /**
   * Define the Optional ARRNAME keyword value
   * @param arrName value of ARRNAME keyword
   */
  public final void setArrName(final String arrName) {
    setKeyword(OIFitsConstants.KEYWORD_ARRNAME, arrName);
    // reset cached reference :
    this.oiArrayRef = null;
  }

  /**
   * Get the INSNAME keyword value.
   * @return the value of INSNAME keyword
   */
  public final String getInsName() {
    return getKeyword(OIFitsConstants.KEYWORD_INSNAME);
  }

  /**
   * Define the INSNAME keyword value
   * @param insName value of INSNAME keyword
   */
  public final void setInsName(final String insName) {
    setKeyword(OIFitsConstants.KEYWORD_INSNAME, insName);
    // reset cached reference :
    this.oiWavelengthRef = null;
  }

  /* --- Columns --- */
  /**
   * Return the TARGET_ID column.
   * @return the TARGET_ID column.
   */
  public short[] getTargetId() {
    return this.getColumnShort(OIFitsConstants.COLUMN_TARGET_ID);
  }

  /**
   * Return the TIME column.
   * @return the TIME column.
   */
  public double[] getTime() {
    return this.getColumnDouble(OIFitsConstants.COLUMN_TIME);
  }

  /**
   * Return the MJD column.
   * @return the MJD column.
   */
  public double[] getMjd() {
    return this.getColumnDouble(OIFitsConstants.COLUMN_MJD);
  }

  /**
   * Return the INT_TIME column.
   * @return the INT_TIME column.
   */
  public double[] getIntTime() {
    return this.getColumnDouble(OIFitsConstants.COLUMN_INT_TIME);
  }

  /**
   * Return the STA_INDEX column.
   * @return the STA_INDEX column.
   */
  public final short[][] getStaIndex() {
    return this.getColumnShorts(OIFitsConstants.COLUMN_STA_INDEX);
  }

  /**
   * Return the FLAG column.
   * @return the FLAG column.
   */
  public final boolean[][] getFlag() {
    return this.getColumnBooleans(OIFitsConstants.COLUMN_FLAG);
  }

  /* --- Utility methods for cross-referencing --- */
  /**
   * Return the associated optional OIArray table.
   * @return the associated OIArray or null if the keyword ARRNAME is undefined
   */
  public final OIArray getOiArray() {
    /** cached resolved reference */
    if (this.oiArrayRef != null) {
      return this.oiArrayRef;
    }

    final String arrName = getArrName();
    if (arrName != null) {
      final OIArray oiArray = getOIFitsFile().getOiArray(getArrName());

      if (oiArray != null) {
        if (logger.isLoggable(Level.FINE)) {
          logger.fine("Resolved OI_Array reference [" + oiArray.getExtNb() + "] to " + super.toString());
        }
        this.oiArrayRef = oiArray;
      }
      return oiArray;
    }

    return null;
  }

  /**
   * Mediator method to resolve cross references. Returns the accepted (ie
   * valid) station indexes for the associated OIArray table.
   *
   * @return the array containing the indexes.
   */
  public final short[] getAcceptedStaIndexes() {
    return getOIFitsFile().getAcceptedStaIndexes(getOiArray());
  }

  /**
   * Return the associated OIWavelength table.
   * @return the associated OIWavelength
   */
  public final OIWavelength getOiWavelength() {
    /** cached resolved reference */
    if (this.oiWavelengthRef != null) {
      return this.oiWavelengthRef;
    }

    final String insName = getInsName();
    if (insName != null) {
      final OIWavelength oiWavelength = getOIFitsFile().getOiWavelength(insName);

      if (oiWavelength != null) {
        if (logger.isLoggable(Level.FINE)) {
          logger.fine("Resolved OI_WAVELENGTH reference [" + oiWavelength.getExtNb() + " | NWAVE=" + oiWavelength.getNWave() + " ] to " + super.toString());
        }
        this.oiWavelengthRef = oiWavelength;
      } else {
        if (logger.isLoggable(Level.WARNING)) {
          logger.warning("Missing OI_WAVELENGTH identified by '" + insName + "'");
        }
      }
      return oiWavelength;
    }

    return null;
  }

  /**
   * Return the number of distinct spectral channels of the associated OI_WAVELENGTH.
   * @return the number of distinct spectral channels of the associated OI_WAVELENGTH.
   */
  public final int getNWave() {
    final OIWavelength oiWavelength = getOiWavelength();
    if (oiWavelength != null) {
      return oiWavelength.getNWave();
    }
    return 0;
  }

  /* --- Other methods --- */
  /**
   * Returns a string representation of this table
   * @return a string representation of this table
   */
  @Override
  public String toString() {
    return super.toString() + " [ INSNAME=" + getInsName() + " NB_MEASUREMENTS=" + getNbMeasurements() + " ]";
  }

  /**
   * Add arrname and oiarray test in addition to OITable.checkKeywords()
   * @param checker checker component
   */
  @Override
  public final void checkKeywords(final OIFitsChecker checker) {
    super.checkKeywords(checker);

    if (getArrName() != null && getOiArray() == null) {
      /* No keyword with keywordName name */
      checker.severe("Missing OI_ARRAY table that describes the '" + getArrName() + "' array");
    }
  }
}
/*___oOo___*/
