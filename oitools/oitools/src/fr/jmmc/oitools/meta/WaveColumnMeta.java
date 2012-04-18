/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.meta;

import fr.jmmc.oitools.model.OIData;

/**
 * This specific ColumnMeta overrides the getRepeat() method to use the OIWaveLength.getNWave() method
 * in a dynamic way
 * @author bourgesl
 */
public final class WaveColumnMeta extends ColumnMeta {

  /* members */
  /** reference to OIData object to resolve OIWaveLength reference */
  private final OIData oiData;
  /** flag to indicate if the column is optional */
  private final boolean optional;

  /**
   * ColumnMeta class constructor
   *
   * @param name keyword/column name
   * @param desc keyword/column descriptive comment
   * @param dataType keyword/column data type
   * @param oiData OIData object to resolve OIWaveLength reference
   */
  public WaveColumnMeta(final String name, final String desc, final Types dataType, final OIData oiData) {
    super(name, desc, dataType, 0);
    this.oiData = oiData;
    this.optional = false;
  }

  /**
   * ColumnMeta class constructor for an optional column
   *
   * @param name keyword/column name
   * @param desc keyword/column descriptive comment
   * @param dataType keyword/column data type
   * @param optional flag to indicate if the column is optional
   * @param oiData OIData object to resolve OIWaveLength reference
   */
  public WaveColumnMeta(final String name, final String desc, final Types dataType, final boolean optional, final OIData oiData) {
    super(name, desc, dataType, 0);
    this.oiData = oiData;
    this.optional = optional;
  }

  /**
   * ColumnMeta class constructor
   *
   * @param name keyword/column name
   * @param desc keyword/column descriptive comment
   * @param dataType keyword/column data type
   * @param oiData OIData object to resolve OIWaveLength reference
   * @param unit keyword/column unit
   */
  public WaveColumnMeta(final String name, final String desc, final Types dataType, final Units unit, final OIData oiData) {
    super(name, desc, dataType, 0, unit);
    this.oiData = oiData;
    this.optional = false;
  }

  /**
   * Return the repeat value i.e. cardinality = number of distinct spectral channels
   * It uses the OIData.getNWave() method to get the number of distinct spectral
   * channels of the associated OI_WAVELENGTH
   * @return repeat value i.e. cardinality = number of distinct spectral channels
   */
  @Override
  public int getRepeat() {
    return this.oiData.getNWave();
  }

  /**
   * Return true if the column is optional
   * @return true if the column is optional
   */
  @Override
  public boolean isOptional() {
    return optional;
  }
}
