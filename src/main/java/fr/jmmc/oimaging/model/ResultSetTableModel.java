/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ***************************************************************************** */
package fr.jmmc.oimaging.model;

import fr.jmmc.jmcs.model.ColumnDesc;
import static fr.jmmc.jmcs.model.ColumnDesc.CMP_COLUMNS;
import fr.jmmc.jmcs.model.ColumnDescTableModel;
import fr.jmmc.jmcs.util.NumberUtils;
import fr.jmmc.oimaging.services.ServiceResult;
import fr.jmmc.oitools.fits.FitsHeaderCard;
import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.image.ImageOiInputParam;
import fr.jmmc.oitools.image.ImageOiOutputParam;
import fr.jmmc.oitools.meta.KeywordMeta;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.jmmc.oitools.fits.FitsUtils;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author martin
 */
public class ResultSetTableModel extends ColumnDescTableModel {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(ResultSetTableModel.class);

    // ColumnDesc source constants
    public static final int HARD_CODED = 0;
    public static final int OUTPUT_PARAM = 1;
    public static final int INPUT_PARAM = 2;

    /* members */
    /** results (another list copy) */
    private final List<ServiceResult> results;

    public ResultSetTableModel() {
        super();
        results = new ArrayList<>();
    }

    /** 
     * Update results, then also updates columns (they depend on results)
     * @param results list of results
     * @param allColumnNames all column names
     */
    public void setResults(List<ServiceResult> results, final List<String> allColumnNames) {
        // update results
        this.results.clear();
        this.results.addAll(results);

        // Fusion of columns:
        // 1. we create a set of columns, uniques by getName()
        final Map<String, ColumnDesc> columnDescMap = new HashMap<>(64);

        // 2. we add output columns to the set
        for (ServiceResult result : results) {
            if (result.getOifitsFile() != null) {
                final ImageOiOutputParam outputParam = result.getOifitsFile().getImageOiData().getOutputParam();
                processKeywordTable(columnDescMap, outputParam, OUTPUT_PARAM);
            }
        }

        // 3. we add input columns to the set
        for (ServiceResult result : results) {
            if (result.getOifitsFile() != null) {
                final ImageOiInputParam inputParam = result.getOifitsFile().getImageOiData().getInputParam();
                processKeywordTable(columnDescMap, inputParam, INPUT_PARAM);
            }
        }

        // 4. we add hardcoded columns to the set
        for (HardCodedColumn hcc : HardCodedColumn.values()) {
            if (!columnDescMap.containsKey(hcc.name())) {
                columnDescMap.put(hcc.name(), hcc.getColumnDesc());
            }
        }

        // 5. we add missing columns (from all known columns) to the set
        for (String columnName : allColumnNames) {
            if (!columnDescMap.containsKey(columnName)) {
                columnDescMap.put(columnName, new ColumnDesc(columnName, Object.class));
            }
        }

        // 6. we clear the list
        listColumnDesc.clear();

        // 7. we put set elements in the list
        listColumnDesc.addAll(columnDescMap.values());

        // 8. sort the list
        listColumnDesc.sort(CMP_COLUMNS);
        
        logger.debug("setResults: listColumnDesc: {}", listColumnDesc);

        // notify changes
        fireTableStructureChanged();
        fireTableDataChanged();
    }

    public ServiceResult getServiceResult(final int rowIndex) {
        return this.results.get(rowIndex);
    }

    @Override
    public int getRowCount() {
        return results.size();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        final ServiceResult result = getServiceResult(rowIndex);
        if (!result.isValid()) {
            logger.debug("ServiceResult invalid, cannot edit");
            return false;
        }
        final ColumnDesc columnDesc = getColumnDesc(columnIndex);
        return columnDesc.equals(HardCodedColumn.COMMENTS.getColumnDesc())
                || columnDesc.equals(HardCodedColumn.RATING.getColumnDesc());
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        final ServiceResult result = getServiceResult(rowIndex);
        final ColumnDesc columnDesc = getColumnDesc(columnIndex);

        if (columnDesc.equals(HardCodedColumn.COMMENTS.getColumnDesc())) {
            result.setComments((String) value);
        } else if (columnDesc.equals(HardCodedColumn.RATING.getColumnDesc())) {
            result.setRating((int) value);
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        final ServiceResult result = getServiceResult(rowIndex);
        final ColumnDesc columnDesc = getColumnDesc(columnIndex);

        switch (columnDesc.getSource()) {
            case HARD_CODED:
                switch (HardCodedColumn.valueOf(columnDesc.getName())) {
                    case INDEX:
                        return getRowCount() - rowIndex;
                    case FILE:
                        return result.getInputFile().getName();
                    case TIMESTAMP:
                        return (result.getEndTime() != null) ? result.getEndTime() : result.getStartTime();
                    case DURATION:
                        if (result.getEndTime() != null) {
                            final long duration = (result.getEndTime().getTime() - result.getStartTime().getTime());
                            return NumberUtils.trimTo3Digits(duration / 1000.0);
                        }
                        break;
                    case ALGORITHM:
                        return result.getService().getProgram();
                    case SUCCESS:
                        return result.isValid();
                    case RATING:
                        return result.getRating();
                    case COMMENTS:
                        return result.getComments();
                }
                break;
            case INPUT_PARAM:
                if (result.getOifitsFile() != null) {
                    final FitsTable inputParam = result.getOifitsFile().getImageOiData().getInputParam();
                    return getKeywordValue(inputParam, columnDesc.getName());
                }
                break;
            case OUTPUT_PARAM:
                if (result.getOifitsFile() != null) {
                    final FitsTable outputParam = result.getOifitsFile().getImageOiData().getOutputParam();
                    return getKeywordValue(outputParam, columnDesc.getName());
                }
                break;
            default:
        }
        // if nothing was found
        return null;
    }

    private static void processKeywordTable(final Map<String, ColumnDesc> columnDescMap, final FitsTable fitsTable, final int source) {
        // keep column even if empty:
        for (KeywordMeta keyMeta : fitsTable.getKeywordsDesc().values()) {
            final String key = keyMeta.getName();

            // ignore standard FITS keywords:
            if (!columnDescMap.containsKey(key) && !FitsUtils.isStandardKeyword(key)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Column[{}] data class: {}", key, keyMeta.getBaseClass());
                }
                columnDescMap.put(key, new ColumnDesc(key, keyMeta.getBaseClass(), source, null, keyMeta.getDescription()));
            }
        }
        if (fitsTable.hasHeaderCards()) {
            // note: the header cards list may contain multiple cards with the same key ...
            for (FitsHeaderCard card : fitsTable.getHeaderCards()) {
                final String key = card.getKey();

                // ignore standard FITS keywords:
                if (!FitsUtils.isStandardKeyword(key)) {
                    ColumnDesc columnDesc = columnDescMap.get(key);
                    if (columnDesc != null) {
                        if (columnDesc.getDataClass() == Object.class) {
                            // fix ColumnDesc.dataClass (= Object.class) if this value is not null:
                            final Object value = card.parseValue();
                            if (value != null) {
                                logger.debug("Column[{}] set data class: {}", key, value.getClass());
                                columnDesc.setDataClass(value.getClass());
                            }
                        }
                    } else {
                        final Object value = card.parseValue();
                        final Class<?> dataClass = (value != null) ? value.getClass() : Object.class;
                        logger.debug("Column[{}] data class: {}", key, dataClass);
                        columnDescMap.put(key, new ColumnDesc(key, dataClass, source, null, card.getComment()));
                    }
                }
            }
        }
    }

    /**
     * Method used to get param value from both either input or output params.
     * @param fitsTable is obtained for example by calling serviceResult.getOifitsFile().getImageOiData().getInputParam(), must not be null
     * @param paramKey the String key of the targeted param, can be empty or null but then will return null
     * @return param value if key found, null otherwise
     */
    private static Object getKeywordValue(final FitsTable fitsTable, final String paramKey) {
        if (paramKey != null) {
            // Keywords
            if (fitsTable.hasKeywordMeta(paramKey)) {
                return fitsTable.getKeywordValue(paramKey);
                // Header card
            } else if (fitsTable.hasHeaderCards()) {
                FitsHeaderCard card = fitsTable.findFirstHeaderCard(paramKey);
                if (card != null) {
                    return card.parseValue();
                }
            }
        }

        // if nothing was found
        return null;
    }

    /** 
     * Enum for HardCoded Columns wrapping ColumnDesc
     */
    public enum HardCodedColumn {
        ALGORITHM(String.class, "Algorithm"),
        COMMENTS(String.class, "Comments"),
        FILE(String.class, "File"),
        INDEX(Integer.class, "Index"),
        DURATION(Double.class, "Job duration"),
        TIMESTAMP(Date.class, "Job timestamp"),
        RATING(Integer.class, "Rating"),
        SUCCESS(Boolean.class, "Success");

        private final ColumnDesc columnDesc;

        private HardCodedColumn(Class<?> dataClass, String label) {
            this.columnDesc = new ColumnDesc(name(), dataClass, HARD_CODED, label);
        }

        public ColumnDesc getColumnDesc() {
            return columnDesc;
        }
    }
}
