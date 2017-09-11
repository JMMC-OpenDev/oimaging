/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.image.ImageOiData;
import fr.jmmc.oitools.meta.OIFitsStandard;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * This class represents the data model of an OIFits standard file.
 */
public final class OIFitsFile extends OIFits {

    /* members */
    /** source URI */
    private URI sourceURI = null;
    /** file size in bytes */
    private long fileSize = 0;
    /** (optional) MD5 sum of the file */
    private String md5sum = null;
    /** Hash table connecting each ARRNAME keyword value with associated OI_ARRAY table */
    private final Map<String, List<OIArray>> arrNameToOiArray = new HashMap<String, List<OIArray>>();
    /** Hash table connecting each INSNAME keyword value with associated OI_WAVELENGTH table */
    private final Map<String, List<OIWavelength>> insNameToOiWavelength = new HashMap<String, List<OIWavelength>>();
    /** Hash table connecting each CORRNAME keyword value with associated OI_CORR table */
    private final Map<String, List<OICorr>> corrNameToOiCorr = new HashMap<String, List<OICorr>>();
    /** (optional) ImageOi data */
    private ImageOiData imageOiData = null;
    // TODO make next wlen min max getters more generic : stats accross every compatible tables ... (e.g. min/max vis2 e_vis2 ...)
    /** Store min wavelength of oifits file */
    private double minWavelengthBound;
    /** Store max wavelength of oifits file */
    private double maxWavelengthBound;

    /**
     * Public constructor
     * @param version
     */
    public OIFitsFile(final OIFitsStandard version) {
        super(version);
    }

    /**
     * Public constructor
     * @param version
     * @param absoluteFilePath absolute file path
     */
    public OIFitsFile(final OIFitsStandard version, final String absoluteFilePath) {
        super(version);
        setAbsoluteFilePath(absoluteFilePath);
    }

    /**
     * Add the given OI_* tables to this OIFitsFile structure
     * @param oiTable new OI_* table
     */
    public void addOiTable(final OITable oiTable) {
        // Prepare table keywords (ExtNb and ExtVer):
        // note: avoid reentrance as OIFitsCollection can reuse OITable

        // ext number (0..n):
        if (oiTable.getExtNb() == -1) {
            // keep existing ExtNb (OIFitsFile structure per Target):
            oiTable.setExtNb(getNbOiTables());
        }

        // ext version (1..n):
        if (oiTable.getExtVer() == 0) {
            // keep existing ExtNb (OIFitsFile structure per Target):
            int extVer = 0;
            if (oiTable instanceof OITarget) {
                // only 1 OI_TARGET table allowed.
                if (hasOiTarget()) {
                    throw new IllegalArgumentException("OI_TARGET is already defined !");
                }
            } else if (oiTable instanceof OIWavelength) {
                extVer = getNbOiWavelengths();
            } else if (oiTable instanceof OIArray) {
                extVer = getNbOiArrays();
            } else if (oiTable instanceof OIVis) {
                extVer = getNbOiVis();
            } else if (oiTable instanceof OIVis2) {
                extVer = getNbOiVis2();
            } else if (oiTable instanceof OIT3) {
                extVer = getNbOiT3();
            } else if (oiTable instanceof OISpectrum) {
                extVer = getNbOiSpectrum();
            } else if (oiTable instanceof OICorr) {
                extVer = getNbOiCorr();
            } else if (oiTable instanceof OIInspol) {
                extVer = getNbOiInspol();
            }

            extVer++;
            oiTable.setExtVer(extVer);
        }

        this.registerOiTable(oiTable);
    }

    /**
     * Remove the given OI_* tables from this OIFitsFile structure.
     * Only valid for data tables (OI_VIS, OI_VIS2, OI_T3) tables
     * @param oiTable OI_* table to remove
     */
    public void removeOiTable(final OIData oiTable) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Unregistering object for {0}", oiTable.idToString());
        }
        super.unregisterOiTable(oiTable);

        // TODO: remove oiTable in insNameToOiWavelength and arrNameToOiArray if needed
        computeWavelengthBounds();
    }

    /**
     * Register valid OI_* tables (keyword and column values must be defined).
     * @param oiTable reference on one OI_* table
     */
    @Override
    protected void registerOiTable(final OITable oiTable) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Registering object for {0}", oiTable.idToString());
        }
        super.registerOiTable(oiTable);

        if (oiTable instanceof OIWavelength) {
            final OIWavelength o = (OIWavelength) oiTable;
            final String insName = o.getInsName();

            if (insName != null) {
                List<OIWavelength> v = this.insNameToOiWavelength.get(insName);

                if (v == null) {
                    v = new LinkedList<OIWavelength>();
                    this.insNameToOiWavelength.put(insName, v);
                }
                v.add(o);
            } else {
                logger.warning("INSNAME of OI_WAVELENGTH table is null during building step");
            }

            computeWavelengthBounds();
        } else if (oiTable instanceof OIArray) {
            final OIArray o = (OIArray) oiTable;
            final String arrName = o.getArrName();

            if (arrName != null) {
                List<OIArray> v = this.arrNameToOiArray.get(arrName);

                if (v == null) {
                    v = new LinkedList<OIArray>();
                    this.arrNameToOiArray.put(arrName, v);
                }
                v.add(o);
            } else {
                logger.warning("ARRNAME of OI_ARRAY table is null during building step");
            }
        } else if (oiTable instanceof OICorr) {
            final OICorr o = (OICorr) oiTable;
            final String corrName = o.getCorrName();

            if (corrName != null) {
                List<OICorr> v = this.corrNameToOiCorr.get(corrName);

                if (v == null) {
                    v = new LinkedList<OICorr>();
                    this.corrNameToOiCorr.put(corrName, v);
                }
                v.add(o);
            } else {
                logger.warning("CORRNAME of OI_CORR table is null during building step");
            }
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
        final List<OIArray> v = this.arrNameToOiArray.get(arrName);
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
        List<OIWavelength> v = this.insNameToOiWavelength.get(insName);
        if (v == null) {
            return null;
        }
        return v.get(0);
    }

    /**
     * Mediator method to resolve cross references. Returns OiCorr associated
     * to input parameter
     *
     * @param corrName string containing CORRNAME value
     * @return the OI_CORR table reference associated. If none is associated,
     *  returns NULL
     */
    public OICorr getOiCorr(final String corrName) {
        final List<OICorr> v = this.corrNameToOiCorr.get(corrName);
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
    public final short[] getAcceptedStaIndexes(final OIArray oiArray) {
        if (oiArray == null) {
            return EMPTY_SHORT_ARRAY;
        }
        return oiArray.getStaIndex();
    }

    /**
     * Get all ARRNAME values already defined.
     * @return an string array containing all accepted values.
     */
    public final String[] getAcceptedArrNames() {
        final int len = this.arrNameToOiArray.size();
        if (len == 0) {
            return EMPTY_STRING;
        }
        return this.arrNameToOiArray.keySet().toArray(new String[len]);
    }

    /**
     * Get all INSNAME values already defined.
     * @return an string array containing all accepted values.
     */
    public final String[] getAcceptedInsNames() {
        final int len = this.insNameToOiWavelength.size();
        if (len == 0) {
            return EMPTY_STRING;
        }
        return this.insNameToOiWavelength.keySet().toArray(new String[len]);
    }

    /**
     * Get all CORRNAME values already defined.
     * @return an string array containing all accepted values.
     */
    public final String[] getAcceptedCorrNames() {
        final int len = this.corrNameToOiCorr.size();
        if (len == 0) {
            return EMPTY_STRING;
        }
        return this.corrNameToOiCorr.keySet().toArray(new String[len]);
    }

    /**
     * Get all target identifiers defined.
     * @return an integer array containing all accepted values.
     */
    public final short[] getAcceptedTargetIds() {
        final OITarget oiTarget = getOiTarget();
        if (oiTarget == null) {
            return EMPTY_SHORT_ARRAY;
        }

        return oiTarget.getTargetId();
    }

    /**
     * Get the min wavelength value found on any of the OI_WAVELENGTH tables.
     * @return the min wavelength value found on any of the OI_WAVELENGTH tables.
     */
    public double getMinWavelengthBound() {
        return minWavelengthBound;
    }

    /**
     * Get the max wavelength value found on any of the OI_WAVELENGTH tables.
     * @return the max wavelength value found on any of the OI_WAVELENGTH tables.
     */
    public double getMaxWavelengthBound() {
        return maxWavelengthBound;
    }

    /**
     * Compute min and max wavelength that could be present.
     * This reflects the min and max values of every OI_WAVELENGTH tables.
     * TODO : make it more generic
     */
    private void computeWavelengthBounds() {
        // Set wavelength bounds
        minWavelengthBound = Double.MAX_VALUE;
        maxWavelengthBound = Double.MIN_VALUE;
        for (OIWavelength oiWavelength : getOiWavelengths()) {
            float omin = oiWavelength.getEffWaveMin();
            float omax = oiWavelength.getEffWaveMax();
            minWavelengthBound = (omin < minWavelengthBound) ? omin : minWavelengthBound;
            maxWavelengthBound = (omax > maxWavelengthBound) ? omax : maxWavelengthBound;
        }
    }

    /**
     * Return a short description of OIFITS content.
     * @return short description of OIFITS content
     */
    @Override
    public String toString() {
        return "\nFilePath:" + getAbsoluteFilePath() + "\n arrNameToOiArray:" + this.arrNameToOiArray
                + "\n insNameToOiWavelength:" + this.insNameToOiWavelength
                + "\n corrNameToOiCorr:" + this.corrNameToOiCorr
                + "\n " + this.getOITableList();
    }

    /**
     * Check the global structure of oifits file, including table presence and
     * syntax correction.
     *
     * @param checker checker component
     */
    public final void check(final OIFitsChecker checker) {
        final long start = System.nanoTime();

        logger.info("Analysing values and references");

        /* Checking primary HDU */
        if (isOIFits2()) {
            if (getPrimaryImageHDU() == null) {
                checker.severe("No primary HDU found: one must be present");
            } else {
                getPrimaryImageHDU().checkSyntax(checker);
            }
        }

        logger.finest("Checking mandatory tables");

        /* Checking presence of one and only one OI_TARGET table */
        if (!hasOiTarget()) {
            checker.severe("No OI_TARGET table found: one and only one must be present");
        }

        /* Checking presence of at least one OI_WAVELENGTH table */
        if (this.insNameToOiWavelength.isEmpty()) {
            checker.severe("No OI_WAVELENGTH table found: one or more must be present");
        }

        /* Checking presence of at least one OI_ARRAY table V2*/
        if (isOIFits2()) {
            if (this.arrNameToOiArray.isEmpty()) {
                checker.severe("No OI_ARRAY table found: one or more must be present");
            }
        }

        /* Starting syntactical analysis */
        logger.finest("Building list of table for keywords analysis");

        for (OITable oiTable : getOITableList()) {
            oiTable.checkSyntax(checker);
        }

        // cleanup temporary variables
        checker.cleanup();

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "OIFitsFile.check: duration = {0} ms.", 1e-6d * (System.nanoTime() - start));
        }
    }

    /**
     * Check validity of cross references of non-data tables, ie check both
     * tables have different identifiers, or no mandatory identifier is not
     * defined.
     *
     * @param oiTable reference on table to check
     * @param checker checker component
     */
    public void checkCrossReference(final OITable oiTable, final OIFitsChecker checker) {
        if (oiTable instanceof OITarget) {
            OITarget o = (OITarget) oiTable;

            if (o.getNbTargets() < 1) {
                checker.severe("No target defined");
            }
        } else if (oiTable instanceof OIWavelength) {
            OIWavelength o = (OIWavelength) oiTable;
            final String insName = o.getInsName();

            if (insName != null) {
                /* Get OiWavelength associated to INSNAME value */
                final List<OIWavelength> v = this.insNameToOiWavelength.get(insName);

                if (v == null) {
                    /* Problem: INSNAME value has not been encoutered during
                     * building step, that should be impossible */
                    checker.severe("invalid INSNAME identifier");
                } else if (v.size() > 1) {
                    /* Problem: more that one OiWavelength table associated
                         * to INSNAME value, that is strictly forbidden */
                    final StringBuilder sb = new StringBuilder();

                    for (Iterator<OIWavelength> it = v.iterator(); it.hasNext();) {
                        o = it.next();
                        sb.append('|').append(o.getExtNb());
                    }

                    checker.severe("OI_WAVELENGTH tables [" + sb.toString().substring(1) + "] are identified by same INSNAME='" + o.getInsName() + "'");
                }
            } else {
                /* Problem: INSNAME value is "", that should not be possible */
                checker.severe("INSNAME identifier is missing during reference checking step");
            }
        } else if (oiTable instanceof OIArray) {
            OIArray o = (OIArray) oiTable;
            final String arrName = o.getArrName();

            if (arrName != null) {
                /* Get OiArray associated to ARRNAME value */
                final List<OIArray> v = arrNameToOiArray.get(arrName);

                if (v == null) {
                    /* Problem: ARRNAME value has not been encoutered during
                     * building step, that should be impossible */
                    checker.severe("invalid ARRNAME identifier");
                } else if (v.size() > 1) {
                    /* Problem: more that one OiArray table associated
                         * to ARRNAME value, that is strictly forbiden */
                    final StringBuilder sb = new StringBuilder();

                    for (Iterator<OIArray> it = v.iterator(); it.hasNext();) {
                        o = it.next();
                        sb.append('|').append(o.getExtNb());
                    }

                    checker.severe("OI_ARRAY tables [" + sb.toString().substring(1) + "] are identified by same ARRNAME='" + o.getArrName() + "'");
                }
            } else {
                /* Problem: ARRNAME value is "", that should not be possible */
                checker.severe("ARRNAME identifier is missing during reference checking step");
            }
        } else if (oiTable instanceof OICorr) {
            OICorr o = (OICorr) oiTable;
            final String corrName = o.getCorrName();

            if (corrName != null) {
                /* Get OICorr associated to CORRNAME value */
                final List<OICorr> v = corrNameToOiCorr.get(corrName);

                if (v == null) {
                    /* Problem: CORRNAME value has not been encoutered during
                     * building step, that should be impossible */
                    checker.severe("invalid CORRNAME identifier");
                } else if (v.size() > 1) {
                    /* Problem: more that one OICorr table associated
                         * to CORRNAME value, that is strictly forbiden */
                    final StringBuilder sb = new StringBuilder();

                    for (Iterator<OICorr> it = v.iterator(); it.hasNext();) {
                        o = it.next();
                        sb.append('|').append(o.getExtNb());
                    }

                    checker.severe("OI_CORR tables [" + sb.toString().substring(1) + "] are identified by same CORRNAME='" + o.getCorrName() + "'");
                }
            } else {
                /* Problem: CORRNAME value is "", that should not be possible */
                checker.severe("CORRNAME identifier is missing during reference checking step");
            }
        }
    }

    /**
     * Implements the Visitor pattern
     * @param visitor visitor implementation
     */
    @Override
    public final void accept(final ModelVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * This equals method uses the absoluteFilePath equality
     * @param obj other object (OIFitsFile)
     * @return true if the absoluteFilePath are equals
     */
    @Override
    public final boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        // identity comparison:
        if (this == obj) {
            return true;
        }
        // class check:
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OIFitsFile other = (OIFitsFile) obj;

        return areEquals(this.getAbsoluteFilePath(), other.getAbsoluteFilePath());
    }

    /**
     * This hashcode implementation uses only the absoluteFilePath field
     * @return hashcode
     */
    @Override
    public final int hashCode() {
        int hash = 3;
        hash = 71 * hash + (this.getAbsoluteFilePath() != null ? this.getAbsoluteFilePath().hashCode() : 0);
        return hash;
    }

    /*
     * --- data analysis  -----------------------------------------------------
     */
    /**
     * Do analyze this OIFits structure
     */
    public final void analyze() {
        Analyzer.getInstance().visit(this);
    }

    /*
     * Getter - Setter --------------------------------------------------------
     */
    /**
     * Get the name of this OIFits file
     *  @return a string containing the name of the OIFits file
     */
    @Deprecated
    public final String getName() {
        //TODO: 
        return getFileName();
    }

    /**
     * Define the source location associated to the local file
     * @param sourceURI source URI
     */
    public final void setSourceURI(final URI sourceURI) {
        this.sourceURI = sourceURI;
    }

    public final URI getSourceURI() {
        return this.sourceURI;
    }

    /**
     * Define the file size
     * @param size file size in bytes
     */
    public final void setSize(final long size) {
        this.fileSize = size;
    }

    /**
     * Return the size of the file
     * @return file size in bytes
     */
    public final long getSize() {
        return fileSize;
    }

    /**
     * Return the (optional) MD5 sum of the file
     * @return (optional) MD5 sum of the file
     */
    public final String getMd5sum() {
        return md5sum;
    }

    /**
     * Define the (optional) MD5 sum of the file
     * @param md5sum (optional) MD5 sum of the file
     */
    public final void setMd5sum(final String md5sum) {
        this.md5sum = md5sum;
    }

    /**
     * Define the (optional) ImageOi data.
     * If set, imageOidata are serialized in the oifits generated by OiFitsWriter.
     *
     * @param imageOiData
     */
    public void setImageOiData(final ImageOiData imageOiData) {
        this.imageOiData = imageOiData;
    }

    /**
     * Get the IMAGE-OI data if defined.
     * @return the IMAGE-OI data if defined else null.
     */
    public ImageOiData getExistingImageOiData() {
        return imageOiData;
    }

    /**
     * Get the IMAGE-OI data or create a new one.
     * @return the IMAGE-OI data or create a new one.
     */
    public ImageOiData getImageOiData() {
        if (imageOiData == null) {
            imageOiData = new ImageOiData(this);
        }
        return imageOiData;
    }
}
/*___oOo___*/
