/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.image.FitsImageFile;
import fr.jmmc.oitools.meta.OIFitsStandard;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents the data model of an OIFits standard file.
 *
 * @author bourgesl
 */
public class OIFits extends FitsImageFile {

    /* members */
    /**
    OIFITS standard version
     */
    private final OIFitsStandard version;

    /**
     * Missing references kept to avoid repeated warnings
     */
    private final Set<String> missingTableNames = new HashSet<String>();

    /* OIFits structure */
    /**
     * Storage of oi table references
     */
    private final List<OITable> oiTables = new LinkedList<OITable>();

    /* meta data */
    /**
     * List storing OI_TARGET table
     */
    private final List<OITarget> oiTargets = new LinkedList<OITarget>();
    /**
     * List storing OI_ARRAY table
     */
    private final List<OIArray> oiArrays = new LinkedList<OIArray>();
    /**
     * List storing OI_WAVELENGTH table
     */
    private final List<OIWavelength> oiWavelengths = new LinkedList<OIWavelength>();
    /**
     * List storing OI_CORR table references
     */
    private final List<OICorr> oiCorr = new LinkedList<OICorr>();
    /**
     * Storage of OI_INSPOL table references
     */
    private final List<OIInspol> oiInspol = new LinkedList<OIInspol>();

    /* data tables */
    /**
     * Storage of all OI data table references
     */
    private final List<OIData> oiDataTables = new LinkedList<OIData>();
    /**
     * Storage of OI_VIS table references
     */
    private final List<OIVis> oiVisTables = new LinkedList<OIVis>();
    /**
     * Storage of OI_VIS2 table references
     */
    private final List<OIVis2> oiVis2Tables = new LinkedList<OIVis2>();
    /**
     * Storage of OI_T3 table references
     */
    private final List<OIT3> oiT3Tables = new LinkedList<OIT3>();
    /**
     * Storage of OI_SPECTRUM table references
     */
    private final List<OISpectrum> oiSpectrumTables = new LinkedList<OISpectrum>();
    /* cached analyzed data */
    /**
     * List of OIData tables keyed by target (name)
     */
    private final Map<String, List<OIData>> oiDataPerTarget = new HashMap<String, List<OIData>>();
    /**
     * Set of OIData tables keyed by Granule
     */
    private final Map<Granule, Set<OIData>> oiDataPerGranule = new HashMap<Granule, Set<OIData>>();

    /**
     * Public constructor
     * @param version
     */
    public OIFits(OIFitsStandard version) {
        super();
        this.version = version;
    }

    /**
     * @return the version
     */
    public OIFitsStandard getVersion() {
        return version;
    }

    /**
     * Boolean test for OIFits Version 2
     *
     * @return true if version 2
     */
    public boolean isOIFits2() {
        return version == OIFitsStandard.VERSION_2;
    }

    /**
     * Return the Missing references kept to avoid repeated warnings
     *
     * @return Missing references kept to avoid repeated warnings
     */
    public Set<String> getMissingTableNames() {
        return missingTableNames;
    }

    /**
     * Return true if the given name corresponds to a missing reference
     *
     * @param name table name
     * @return true if the given name corresponds to a missing reference
     */
    public boolean hasMissingTableName(final String name) {
        return missingTableNames.contains(name);
    }

    /**
     * Add the given name to the missing references
     *
     * @param name table name
     */
    public void addMissingTableName(final String name) {
        missingTableNames.add(name);
    }

    /**
     * Get the number of OI_* tables
     *
     * @see #getOiTable(int)
     * @return the number of OI_* tables
     */
    public final int getNbOiTables() {
        return this.oiTables.size();
    }

    /**
     * Return the nth OI_* table
     *
     * @param index index
     * @return the nth OI_* table
     */
    public final OITable getOiTable(final int index) {
        return this.oiTables.get(index);
    }

    /**
     * Return an array containing all OI_* tables
     *
     * @return an array containing all OI_* tables
     */
    public final OITable[] getOiTables() {
        return this.oiTables.toArray(new OITable[this.oiTables.size()]);
    }

    /**
     * Return the (internal) list of OI_* tables 
     * (Used by OIFitsExplorer)
     *
     * @return the (internal) list of OI_* tables
     */
    public final List<OITable> getOITableList() {
        return this.oiTables;
    }

    /**
     * Tells if the file contains a OI_TARGET table.
     *
     * @return true if the file contains a OI_TARGET table
     */
    public final boolean hasOiTarget() {
        return !this.oiTargets.isEmpty();
    }

    /**
     * Return the OI_TARGET table or null if not present
     *
     * @return the OI_TARGET table
     */
    public final OITarget getOiTarget() {
        if (this.oiTargets.isEmpty()) {
            return null;
        } else {
            return this.oiTargets.get(0);
        }
    }

    /**
     * Tells if the file contains some OI_ARRAY tables
     *
     * @return true if the file contains some OI_ARRAY table
     */
    public final boolean hasOiArray() {
        return !this.oiArrays.isEmpty();
    }

    /**
     * Get the number of OI_ARRAY tables
     *
     * @return the number of OI_ARRAY tables
     */
    public final int getNbOiArrays() {
        return this.oiArrays.size();
    }

    /**
     * Return an array containing all OI_ARRAY tables
     *
     * @return an array containing all OI_ARRAY tables
     */
    public final OIArray[] getOiArrays() {
        return this.oiArrays.toArray(new OIArray[this.oiArrays.size()]);
    }

    /**
     * Get the number of OI_WAVELENGTH tables
     *
     * @return the number of OI_WAVELENGTH tables
     */
    public final int getNbOiWavelengths() {
        return this.oiWavelengths.size();
    }

    /**
     * Return an array containing all OI_WAVELENGTH tables
     *
     * @return an array containing all OI_WAVELENGTH tables
     */
    public final OIWavelength[] getOiWavelengths() {
        return this.oiWavelengths.toArray(new OIWavelength[this.oiWavelengths.size()]);
    }

    /**
     * Tell if the file contains some OI_CORR tables
     *
     * @return true if the file contains some OI_CORR table
     */
    public final boolean hasOiCorr() {
        return !this.oiCorr.isEmpty();
    }

    /**
     * Get the number of OI_CORR tables
     *
     * @return the number of OI_CORR tables
     */
    public final int getNbOiCorr() {
        return this.oiCorr.size();
    }

    /**
     * Return an array containing all OI_CORR tables
     *
     * @return an array containing all OI_CORR tables
     */
    public final OICorr[] getOiCorr() {
        return this.oiCorr.toArray(new OICorr[this.oiCorr.size()]);
    }

    /**
     * Tells if the file contains some OI data tables
     *
     * @return true if the file contains some OI data table
     */
    public final boolean hasOiData() {
        return !this.oiDataTables.isEmpty();
    }

    /**
     * Get the number of OI data tables
     *
     * @return the number of OI data tables
     */
    public final int getNbOiData() {
        return this.oiDataTables.size();
    }

    /**
     * Return an array containing all OI data tables
     *
     * @return an array containing all OI data tables
     */
    public final OIData[] getOiDatas() {
        return this.oiDataTables.toArray(new OIData[this.oiDataTables.size()]);
    }

    /**
     * Return the (internal) list of OI data tables
     *
     * @return the (internal) list of OI data tables
     */
    public final List<OIData> getOiDataList() {
        return this.oiDataTables;
    }

    /**
     * Tells if the file contains some OI_VIS tables
     *
     * @return true if the file contains some OI_VIS table
     */
    public final boolean hasOiVis() {
        return !this.oiVisTables.isEmpty();
    }

    /**
     * Get the number of OI_VIS tables
     *
     * @return the number of OI_VIS tables
     */
    public final int getNbOiVis() {
        return this.oiVisTables.size();
    }

    /**
     * Return an array containing all OI_VIS tables
     *
     * @return an array containing all OI_VIS tables
     */
    public final OIVis[] getOiVis() {
        return this.oiVisTables.toArray(new OIVis[this.oiVisTables.size()]);
    }

    /**
     * Tell if the file contains some OI_VIS2 tables
     *
     * @return true if the file contains some OI_VIS2 table
     */
    public final boolean hasOiVis2() {
        return !this.oiVis2Tables.isEmpty();
    }

    /**
     * Get the number of OI_VIS2 tables
     *
     * @return the number of OI_VIS2 tables
     */
    public final int getNbOiVis2() {
        return this.oiVis2Tables.size();
    }

    /**
     * Return an array containing all OI_VIS2 tables
     *
     * @return an array containing all OI_VIS2 tables
     */
    public final OIVis2[] getOiVis2() {
        return this.oiVis2Tables.toArray(new OIVis2[this.oiVis2Tables.size()]);
    }

    /**
     * Tells if the file contains some OI_T3 tables
     *
     * @return true if the file contains some OI_T3 table
     */
    public final boolean hasOiT3() {
        return !this.oiT3Tables.isEmpty();
    }

    /**
     * Get the number of OI_T3 tables
     *
     * @return the number of OI_T3 tables
     */
    public final int getNbOiT3() {
        return this.oiT3Tables.size();
    }

    /**
     * Return an array containing all OI_T3 tables
     *
     * @return an array containing all OI_T3 tables
     */
    public final OIT3[] getOiT3() {
        return this.oiT3Tables.toArray(new OIT3[this.oiT3Tables.size()]);
    }

    /**
     * Tell if the file contains some OI_SPECTRUM tables
     *
     * @return true if the file contains some OI_SPECTRUM table
     */
    public final boolean hasOiSpectrum() {
        return !this.oiSpectrumTables.isEmpty();
    }

    /**
     * Get the number of OI_SPECTRUM tables
     *
     * @return the number of OI_SPECTRUM tables
     */
    public final int getNbOiSpectrum() {
        return this.oiSpectrumTables.size();
    }

    /**
     * Return an array containing all OI_SPECTRUM tables
     *
     * @return an array containing all OI_SPECTRUM tables
     */
    public final OISpectrum[] getOiSpectrum() {
        return this.oiSpectrumTables.toArray(new OISpectrum[this.oiSpectrumTables.size()]);
    }

    /**
     * Tell if the file contains some OI_INSPOL tables
     *
     * @return true if the file contains some OI_INSPOL table
     */
    public final boolean hasOiInspol() {
        return !this.oiInspol.isEmpty();
    }

    /**
     * Get the number of OI_INSPOL tables
     *
     * @return the number of OI_INSPOL tables
     */
    public final int getNbOiInspol() {
        return this.oiInspol.size();
    }

    /**
     * Return an array containing all OI_INSPOL tables
     *
     * @return an array containing all OI_INSPOL tables
     */
    public final OIInspol[] getOiInspol() {
        return this.oiInspol.toArray(new OIInspol[this.oiInspol.size()]);
    }

    /**
     * Register an OI_* table.
     *
     * @param oiTable reference on one OI_* table
     */
    protected void registerOiTable(final OITable oiTable) {
        this.oiTables.add(oiTable);

        if (oiTable instanceof OITarget) {
            this.oiTargets.add((OITarget) oiTable);
        } else if (oiTable instanceof OIWavelength) {
            this.oiWavelengths.add((OIWavelength) oiTable);
        } else if (oiTable instanceof OIArray) {
            this.oiArrays.add((OIArray) oiTable);
        } else if (oiTable instanceof OICorr) {
            this.oiCorr.add((OICorr) oiTable);
        } else if (oiTable instanceof OIInspol) {
            this.oiInspol.add((OIInspol) oiTable);
        } else if (oiTable instanceof OIVis) {
            this.oiDataTables.add((OIVis) oiTable);
            this.oiVisTables.add((OIVis) oiTable);
        } else if (oiTable instanceof OIVis2) {
            this.oiDataTables.add((OIVis2) oiTable);
            this.oiVis2Tables.add((OIVis2) oiTable);
        } else if (oiTable instanceof OIT3) {
            this.oiDataTables.add((OIT3) oiTable);
            this.oiT3Tables.add((OIT3) oiTable);
        } else if (oiTable instanceof OISpectrum) {
            this.oiDataTables.add((OISpectrum) oiTable);
            this.oiSpectrumTables.add((OISpectrum) oiTable);
        }
    }

    /**
     * Unregister an OI_* table.
     *
     * @param oiTable reference on one OI_* table
     */
    protected void unregisterOiTable(final OITable oiTable) {
        this.oiTables.remove(oiTable);

        if (oiTable instanceof OITarget) {
            this.oiTargets.remove((OITarget) oiTable);
        } else if (oiTable instanceof OIWavelength) {
            this.oiWavelengths.remove((OIWavelength) oiTable);
        } else if (oiTable instanceof OIArray) {
            this.oiArrays.remove((OIArray) oiTable);
        } else if (oiTable instanceof OICorr) {
            this.oiCorr.remove((OICorr) oiTable);
        } else if (oiTable instanceof OIInspol) {
            this.oiInspol.remove((OIInspol) oiTable);
        } else if (oiTable instanceof OIVis) {
            this.oiDataTables.remove((OIVis) oiTable);
            this.oiVisTables.remove((OIVis) oiTable);
        } else if (oiTable instanceof OIVis2) {
            this.oiDataTables.remove((OIVis2) oiTable);
            this.oiVis2Tables.remove((OIVis2) oiTable);
        } else if (oiTable instanceof OIT3) {
            this.oiDataTables.remove((OIT3) oiTable);
            this.oiT3Tables.remove((OIT3) oiTable);
        } else if (oiTable instanceof OISpectrum) {
            this.oiDataTables.remove((OISpectrum) oiTable);
            this.oiSpectrumTables.remove((OISpectrum) oiTable);
        }
    }


    /* --- data analysis --- */
    /**
     * Indicate to clear any cached value (derived column ...)
     */
    public void setChanged() {
        oiDataPerTarget.clear();
        oiDataPerGranule.clear();
    }

    public Map<String, List<OIData>> getOiDataPerTarget() {
        return oiDataPerTarget;
    }

    public Map<Granule, Set<OIData>> getOiDataPerGranule() {
        return oiDataPerGranule;
    }

    /**
     * Return the list of OIData tables corresponding to the given target (name)
     * or null if missing
     *
     * @param target target (name)
     * @return list of OIData tables corresponding to the given target (name) or
     * null if missing
     */
    public List<OIData> getOiDataList(final String target) {
        return getOiDataPerTarget().get(target);
    }
}
