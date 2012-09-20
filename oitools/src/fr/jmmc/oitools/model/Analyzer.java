/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oitools.model;

import fr.jmmc.oitools.util.CombUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * This class visit the table for a given oifits file to process some computation on them. 
 */
public final class Analyzer implements ModelVisitor {

    /** Logger */
    private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Analyzer.class.getName());
    /* members */
    /** OIFits: List of OIData tables keyed by target (name) */
    private Map<String, List<OIData>> _oiDataPerTarget;
    /** OITarget: mapping of target (name) values to targetId */
    private Map<String, Short> _targetToTargetId;

    /**
     * Protected constructor
     */
    Analyzer() {
        super();
    }

    /**
     * Process the given OIFitsFile element with this visitor implementation :
     * fill the internal buffer with file information
     * @param oiFitsFile OIFitsFile element to visit
     */
    @Override
    public void visit(final OIFitsFile oiFitsFile) {

        // process OITarget table:
        final OITarget oiTarget = oiFitsFile.getOiTarget();
        oiTarget.accept(this);

        _targetToTargetId = oiTarget.getTargetToTargetId();

        // define oiDataPerTarget in OIData processing :
        _oiDataPerTarget = oiFitsFile.getOiDataPerTarget();

        // process OIWavelength tables:
        for (final OIWavelength oiWavelength : oiFitsFile.getOiWavelengths()) {
            oiWavelength.accept(this);
        }

        // process OIArray tables:
        for (final OIArray oiArray : oiFitsFile.getOiArrays()) {
            oiArray.accept(this);
        }

        // process OIData tables:
        for (final OIData oiData : oiFitsFile.getOiDataList()) {
            oiData.accept(this);
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("process: OIFitsFile[" + oiFitsFile.getAbsoluteFilePath() + "] oiDataPerTarget " + _oiDataPerTarget);
        }
    }

    /**
     * Process the given OITable element with this visitor implementation :
     * fill the internal buffer with table information
     * @param oiTable OITable element to visit
     */
    @Override
    public void visit(final OITable oiTable) {
        if (oiTable instanceof OIWavelength) {
            process((OIWavelength) oiTable);
        } else if (oiTable instanceof OIData) {
            process((OIData) oiTable);
        } else if (oiTable instanceof OIArray) {
            process((OIArray) oiTable);
        } else if (oiTable instanceof OITarget) {
            process((OITarget) oiTable);
        }
    }

    /* --- process OITable --- */
    /**
     * Process the given OIData table
     * @param oiData OIData table to process
     */
    private void process(final OIData oiData) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("process: OIData[" + oiData + "] OIWavelength range: " + Arrays.toString(oiData.getEffWaveRange()));
        }

        // reset cached analyzed data:
        oiData.setChanged();

        final int nRows = oiData.getNbRows();
        final int nWaves = oiData.getNWave();

        // Distinct Target Id:
        final short[] targetIds = oiData.getTargetId();

        final Set<Short> distinctTargetId = oiData.getDistinctTargetId();

        for (int i = 0; i < nRows; i++) {
            distinctTargetId.add(Short.valueOf(targetIds[i]));
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("process: OIData[" + oiData + "] distinctTargetId " + distinctTargetId);
        }

        // Process station indexes:
        processStaIndex(oiData);

        // Count Flags:
        final boolean[][] flags = oiData.getFlag();

        int nFlagged = 0;
        boolean[] row;
        for (int i = 0, j; i < nRows; i++) {
            row = flags[i];
            for (j = 0; j < nWaves; j++) {
                if (row[j]) {
                    nFlagged++;
                }
            }
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("process: OIData[" + oiData + "] nFlagged: " + nFlagged);
        }
        oiData.setNFlagged(nFlagged);

        // FINALLY: Update list of tables per target:
        String target;
        List<OIData> oiDataTables;
        for (Map.Entry<String, Short> entry : _targetToTargetId.entrySet()) {
            if (distinctTargetId.contains(entry.getValue())) {
                target = entry.getKey();

                oiDataTables = _oiDataPerTarget.get(target);
                if (oiDataTables == null) {
                    oiDataTables = new ArrayList<OIData>();
                    _oiDataPerTarget.put(target, oiDataTables);
                }

                oiDataTables.add(oiData);
            }
        }
    }

    /**
     * Process the given OIArray table
     * @param oiArray OIArray table to process
     */
    private void process(final OIArray oiArray) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("process: OIArray[" + oiArray + "]");
        }

        // reset cached analyzed data:
        oiArray.setChanged();

        final Map<Short, Integer> staIndexToRowIndex = oiArray.getStaIndexToRowIndex();

        final short[] staIndexes = oiArray.getStaIndex();

        Short staIndex;
        for (int i = 0, len = oiArray.getNbRows(); i < len; i++) {
            staIndex = Short.valueOf(staIndexes[i]);
            staIndexToRowIndex.put(staIndex, Integer.valueOf(i));
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("process: OIArray[" + oiArray + "] staIndexToRowIndex: " + staIndexToRowIndex);
        }
    }

    /**
     * Process the given OIWavelength table
     * @param oiWavelength OIWavelength table to process
     */
    private void process(final OIWavelength oiWavelength) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("process: OIWavelength[" + oiWavelength + "]");
        }

        // reset cached analyzed data:
        oiWavelength.setChanged();

        final float[] range = oiWavelength.getEffWaveRange();

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("process: OIWavelength[" + oiWavelength + "] range: " + Arrays.toString(range));
        }
    }

    /**
     * Process the given OITarget table
     * @param oiTarget OITarget table to process
     */
    private void process(final OITarget oiTarget) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("process: OITarget[" + oiTarget + "]");
        }

        // reset cached analyzed data:
        oiTarget.setChanged();

        final Map<Short, Integer> targetIdToRowIndex = oiTarget.getTargetIdToRowIndex();
        final Map<String, Short> targetToTargetId = oiTarget.getTargetToTargetId();

        final short[] targetIds = oiTarget.getTargetId();
        final String[] targets = oiTarget.getTarget();

        Short targetId;
        for (int i = 0, len = oiTarget.getNbRows(); i < len; i++) {
            targetId = Short.valueOf(targetIds[i]);
            targetIdToRowIndex.put(targetId, Integer.valueOf(i));

            targetToTargetId.put(targets[i], targetId);
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("process: OITarget[" + oiTarget + "] targetIdToRowIndex: " + targetIdToRowIndex);
            logger.fine("process: OITarget[" + oiTarget + "] targetToTargetId: " + targetToTargetId);
        }
    }

    // --- baseline / configuration processing
    /**
     * Process station indexes on the given OIData table
     * @param oiData OIData table to process
     */
    private void processStaIndex(final OIData oiData) {

        final int nRows = oiData.getNbRows();

        // StaIndex column:
        final short[][] staIndexes = oiData.getStaIndex();

        // distinct staIndex arrays:
        final Set<short[]> distinctStaIndex = oiData.getDistinctStaIndex();

        short[] staIndex;
        short[] uniqueStaIndex;
        for (int i = 0; i < nRows; i++) {
            staIndex = staIndexes[i];

            // Find existing array:
            uniqueStaIndex = null;

            for (short[] item : distinctStaIndex) {
                if (staIndex == item || Arrays.equals(staIndex, item)) {
                    uniqueStaIndex = item;
                    break;
                }
            }

            if (uniqueStaIndex == null) {
                // not found:
                // TODO: warning: not sorted !!
                distinctStaIndex.add(staIndex);
            } else {
                // store distinct instance (minimize array instances):
                staIndexes[i] = uniqueStaIndex;
            }
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("processStaIndex: OIData[" + oiData + "] distinctStaIndex:");

            for (short[] item : distinctStaIndex) {
                logger.fine("Baseline: " + Arrays.toString(item) + " = " + oiData.getStaNames(item));
            }
        }

        processStaConf(oiData);
    }

    /**
     * Process station configurations on the given OIData table
     * @param oiData OIData table to process
     */
    private void processStaConf(final OIData oiData) {

        final int nRows = oiData.getNbRows();

        // Derived StaConf column:
        final short[][] staConfs = oiData.getStaConf();

        // distinct staIndex arrays:
        final Set<short[]> distinctStaIndex = oiData.getDistinctStaIndex();

        // distinct staConf arrays:
        final Set<short[]> distinctStaConf = oiData.getDistinctStaConf();

        // Sort distinct staIndex arrays:
        // @TODO: keep such arrays in map[sorted, real array] ???
        final Set<List<Short>> sortedStaIndex = new LinkedHashSet<List<Short>>(distinctStaIndex.size());

        // sorted sta index mapping (unique instances) to distinct station indexes (maybe more than 1 due to permutations):
        final Map<List<Short>, List<short[]>> mappingSortedStaIndex = new HashMap<List<Short>, List<short[]>>(distinctStaIndex.size());

        // sta index mapping (unique instances) to station configuration:
        final Map<short[], short[]> mappingStaConf = new HashMap<short[], short[]>(distinctStaIndex.size());

        int staLen = 0;

        // Use List<Short> for easier manipulation ie List.equals() use also Short.equals on all items (missing baselines) !
        List<short[]> equalStaIndexes;

        for (short[] staIndex : distinctStaIndex) {
            staLen = staIndex.length;
            final List<Short> staList = new ArrayList<Short>(staLen);

            for (int i = 0; i < staLen; i++) {
                staList.add(Short.valueOf(staIndex[i]));
            }

            // sort station list:
            Collections.sort(staList);

            if (sortedStaIndex.add(staList)) {
                equalStaIndexes = new ArrayList<short[]>(2);
                mappingSortedStaIndex.put(staList, equalStaIndexes);
            } else {
                equalStaIndexes = mappingSortedStaIndex.get(staList);

            }
            // add staIndex corresponding to sorted station list:
            equalStaIndexes.add(staIndex);
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("processStaIndex: OIData[" + oiData + "] sortedStaIndex:");

            for (List<Short> item : sortedStaIndex) {
                logger.fine("StaIndex: " + item);
            }
        }

        if (sortedStaIndex.size() == 1) {
            final short[] staConf = toArray(sortedStaIndex.iterator().next());

            // single staIndex array = single configuration
            distinctStaConf.add(staConf);

            // Fill StaConf derived column:
            for (int i = 0; i < nRows; i++) {
                // store single station configuration:
                staConfs[i] = staConf;
            }

        } else {

            // Guess configurations:
            // simple algorithm works only on distinct values (Aspro2 for now)
            // but advanced one should in fact use baselines having same MJD (same time) !

            // mapping between staId (1 station) and its station node:
            final Map<Short, StationNode> staIndexNodes = new HashMap<Short, StationNode>(32);

            // loop on baselines:
            for (List<Short> staList : sortedStaIndex) {
                for (Short staId : staList) {
                    StationNode node = staIndexNodes.get(staId);

                    if (node == null) {
                        node = new StationNode(staId);
                        staIndexNodes.put(staId, node);
                    }
                    node.addStaList(staList);
                }
            }

            // convert map into list of nodes:
            final List<StationNode> nodes = new ArrayList<StationNode>(staIndexNodes.values());

            // sort station node on its related staIndex counts (smaller count first):
            Collections.sort(nodes);

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("StationNodes --------------------------------------");

                for (StationNode n : nodes) {
                    logger.fine("Station: " + n.staId + " = " + n.count + " {");
                    for (List<Short> item : n.staLists) {
                        logger.fine("StaIndex: " + item);
                    }
                    logger.fine("}");
                }
            }

            // current guessed station configuration:
            final Set<Short> guessConf = new HashSet<Short>(8);

            final List<List<Short>> combStaLists = new ArrayList<List<Short>>();
            final List<Short> sortedConf = new ArrayList<Short>();
            List<Short> cStaList;

            Integer combKey;
            List<int[]> iCombs;
            final Map<Integer, List<int[]>> combPerLen = new HashMap<Integer, List<int[]>>(8);

            boolean valid;
            int confLen;

            StationNode other;
            for (StationNode node : nodes) {

                // skip empty nodes:
                if (node.count > 0) {

                    guessConf.add(node.staId);

                    // TODO: try using all stations then use less to match one valid conf ...
                    for (List<Short> item : node.staLists) {
                        for (Short staId : item) {
                            guessConf.add(staId);
                        }
                    }

                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("node: " + node.staId + " - conf = " + guessConf);
                    }

                    // compute missing staIndexes:
                    sortedConf.clear();
                    sortedConf.addAll(guessConf);
                    guessConf.clear();
                    Collections.sort(sortedConf);

                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("node: " + node.staId + " - sortedConf = " + sortedConf);
                    }

                    combStaLists.clear();

                    // see CombUtils for generics
                    confLen = sortedConf.size();

                    // get permutations:
                    combKey = Integer.valueOf(confLen);

                    iCombs = combPerLen.get(combKey);

                    if (iCombs == null) {
                        iCombs = CombUtils.generateCombinations(combKey.intValue(), staLen);
                        combPerLen.put(combKey, iCombs);
                    }

                    // iCombs is sorted so should keep array sorted as otherStaIds is also sorted !
                    for (int[] combination : iCombs) {
                        cStaList = new ArrayList<Short>(staLen);

                        for (int i : combination) {
                            cStaList.add(sortedConf.get(i));
                        }

                        combStaLists.add(cStaList);
                    }

                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("node: " + node.staId + " - combStaLists = " + combStaLists);
                    }

                    valid = true;

                    // Test missing staIndex:
                    for (List<Short> item : combStaLists) {

                        // test if present:
                        if (!sortedStaIndex.contains(item)) {
                            valid = false;

                            if (staLen == 3) {
                                if (logger.isLoggable(Level.FINE)) {
                                    logger.fine("node: " + node.staId + " - StaIndex not found = " + item);
                                }
                            } else {
                                logger.warning("node: " + node.staId + " - StaIndex not found = " + item);
                            }
                            break;
                        }
                    }

                    // TODO: T3 seems more complex as some triplets are often not present:
                    if (!valid && staLen == 3) {
                        valid = true;
                    }

                    if (valid) {
                        final short[] staConf = toArray(sortedConf);

                        // add this configuration:
                        distinctStaConf.add(staConf);

                        // add mappings:
                        for (List<Short> item : combStaLists) {
                            equalStaIndexes = mappingSortedStaIndex.get(item);

                            // some baseline or triplet may be missing:
                            if (equalStaIndexes != null) {
                                for (short[] staIndex : equalStaIndexes) {
                                    mappingStaConf.put(staIndex, staConf);
                                }
                            }
                        }

                        // process all possible staList:
                        node.clear();

                        for (List<Short> item : combStaLists) {
                            // note: may contain also existing BL:
                            node.addStaList(item);
                        }

                        // remove node baselines in other nodes:
                        for (List<Short> item : node.staLists) {
                            for (Short staId : item) {

                                if (staId != node.staId) {
                                    // remove baseline:
                                    other = staIndexNodes.get(staId);

                                    if (other != null) {
                                        other.removeStaList(item);
                                    }
                                }
                            }
                        }

                        // remove all staList in node:
                        node.clear();
                    } else {
                        // BAD CASE
                        throw new IllegalStateException("bad case");
                    }

                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("StationNodes --------------------------------------");

                        for (StationNode n : nodes) {
                            logger.fine("Station: " + n.staId + " = " + n.count + " {");
                            for (List<Short> item : n.staLists) {
                                logger.fine("StaIndex: " + item);
                            }
                            logger.fine("}");
                        }
                    }
                }
            } // nodes

            // StaIndex column:
            final short[][] staIndexes = oiData.getStaIndex();

            // Fill StaConf derived column:

            short[] staIndex;
            for (int i = 0; i < nRows; i++) {
                staIndex = staIndexes[i];

                // store station configuration according to mapping (should be not null):
                staConfs[i] = mappingStaConf.get(staIndex);

                if (staConfs[i] == null) {
                    logger.warning("station index :" + oiData.getStaNames(staIndex) + " - MISSING station configuration !");

                } else if (logger.isLoggable(Level.FINE)) {
                    logger.fine("station index :" + oiData.getStaNames(staIndex) + " - sta conf " + oiData.getStaNames(staConfs[i]));
                }
            }
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("processStaIndex: OIData[" + oiData + "] distinctStaConf:");

            for (short[] item : distinctStaConf) {
                logger.fine("StaConf: " + Arrays.toString(item) + " = " + oiData.getStaNames(item));
            }
        }
    }

    private static class StationNode implements Comparable<StationNode> {

        final Short staId;
        int count = 0;
        final List<List<Short>> staLists = new ArrayList<List<Short>>();

        StationNode(final Short staId) {
            this.staId = staId;
        }

        void clear() {
            staLists.clear();
            count = 0;
        }

        void addStaList(final List<Short> bl) {
            staLists.add(bl);
            count++;
        }

        void removeStaList(final List<Short> bl) {
            staLists.remove(bl);
            count--;
        }

        @Override
        public int compareTo(final StationNode other) {
            int res = count - other.count;
            if (res == 0) {
                res = staId.compareTo(other.staId);
            }
            return res;
        }
    }

    private static short[] toArray(final List<Short> staList) {
        final short[] staIndex = new short[staList.size()];

        int i = 0;
        for (Short staId : staList) {
            staIndex[i++] = staId.shortValue();
        }

        return staIndex;
    }
}
