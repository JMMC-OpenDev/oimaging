/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ***************************************************************************** */
package fr.jmmc.oimaging.model;

import fr.jmmc.oimaging.services.ServiceResult;
import fr.jmmc.oitools.fits.FitsHeaderCard;
import fr.jmmc.oitools.fits.FitsTable;
import fr.jmmc.oitools.image.ImageOiInputParam;
import fr.jmmc.oitools.image.ImageOiOutputParam;
import fr.jmmc.oitools.meta.KeywordMeta;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import javax.swing.table.AbstractTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.jmmc.oitools.fits.FitsUtils;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author martin
 */
public class ResultSetTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(ResultSetTableModel.class);
   
    private final List<ServiceResult> results;
    
    /** the list of ColumnDesc. Uniques by getName().
     * When there is two equals getName(), we choose by following priority : OutputParam > InputParam > HardCoded column
     */
    private final List<ColumnDesc> listColumnDesc;
    
    // ColumnDesc source constants
    public static final int OUTPUT_PARAM = 0;
    public static final int INPUT_PARAM = 1;
    public static final int HARD_CODED = 2;
    
    public ResultSetTableModel() {
        super();
        results = new ArrayList<>();
        listColumnDesc = new ArrayList<>();
    }
    
    /** read only of userUnionColumnDesc */
    public List<ColumnDesc> getListColumnDesc () {
        return Collections.unmodifiableList(listColumnDesc);
    }
    
    /** updates results, then also updates columns (they depend on results) */
    public void setResults(List<ServiceResult> results) {
        // update results
        this.results.clear();
        this.results.addAll(results);
        
        // Fusion of columns.
        // 1. we create a set of columns, uniques by getName()
        // 2. we add output columns to the set
        // 3. we add input columns to the set
        // 4. we add hardcoded columns to the set
        // 5. we remove standard FITS keywords from the set
        // 6. we clear the list
        // 7. we put set elements in the list
        
        // 1. we create a set of columns, uniques by getName()
        Set<ColumnDesc> setColumnDesc = new HashSet<> ();
        
        // 2. we add output columns to the set
        for (ServiceResult result : results) {
            if (result.getOifitsFile() == null) continue;
            ImageOiOutputParam output = result.getOifitsFile().getImageOiData().getOutputParam();
            for (KeywordMeta keyMeta : output.getKeywordsDesc().values()) {
                setColumnDesc.add(new ColumnDesc(keyMeta.getName(), keyMeta.getClass(), OUTPUT_PARAM)); 
            }
            for (FitsHeaderCard card : output.getHeaderCards()) {
                setColumnDesc.add(new ColumnDesc(card.getKey(), card.getClass(), OUTPUT_PARAM));
            }
        }
        
        // 3. we add input columns to the set
        for (ServiceResult result : results) {
            // input params
            if (result.getOifitsFile() == null) continue;
            ImageOiInputParam input = result.getOifitsFile().getImageOiData().getInputParam();
            for (KeywordMeta keyMeta : input.getKeywordsDesc().values()) {
                setColumnDesc.add(new ColumnDesc(keyMeta.getName(), keyMeta.getClass(), INPUT_PARAM)); 
            }
            for (FitsHeaderCard card : input.getHeaderCards()) {
                setColumnDesc.add(new ColumnDesc(card.getKey(), card.getClass(), INPUT_PARAM));
            }
        }
        
        // 4. we add hardcoded columns to the set
        for (HardCodedColumn hcc : HardCodedColumn.values()) {
            setColumnDesc.add(hcc.getColumnDesc());
        }
        
        // 5. we remove standard FITS keywords from the set
        setColumnDesc.removeIf(col -> FitsUtils.isStandardKeyword(col.getName()));
        
        // 6. we clear the list
        listColumnDesc.clear();
        
        // 7. we put set elements in the list
        listColumnDesc.addAll(setColumnDesc);

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
    public int getColumnCount() {
        return listColumnDesc.size();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return listColumnDesc.get(columnIndex).getName();
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return listColumnDesc.get(columnIndex).getDataClass();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        ColumnDesc columnDesc = listColumnDesc.get(columnIndex);
        return columnDesc.equals(HardCodedColumn.COMMENTS.getColumnDesc())
                || columnDesc.equals(HardCodedColumn.RATING.getColumnDesc());
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        
        if (! isCellEditable(rowIndex, columnIndex)) {
            logger.debug("Cell row " + rowIndex + " col " + columnIndex + " is not editable.");
            return;
        }
        
        final ServiceResult result = getServiceResult(rowIndex);
        
        if (! result.isValid()) {
            logger.debug("ServiceResult invalid, cannot edit");
            return;
        }
        
        ColumnDesc columnDesc = listColumnDesc.get(columnIndex);

        if (columnDesc.equals(HardCodedColumn.COMMENTS.getColumnDesc())) {
            result.setComments((String) value);
        }
        else if (columnDesc.equals(HardCodedColumn.RATING.getColumnDesc())) {
            result.setRating((int) value);
        }
    }
    
    /**
     * Method used to get param value from both either input or output params.
     * @param fitsTable is obtained for example by calling serviceResult.getOifitsFile().getImageOiData().getInputParam(), must not be null
     * @param paramKey the String key of the targeted param, can be empty or null but then will return null
     * @return param value if key found, null otherwise
     */
    private static Object getKeywordValue (FitsTable fitsTable, String paramKey) {
        
        if (paramKey != null) {
            // Keywords
            if (fitsTable.hasKeywordMeta(paramKey)) {
                return fitsTable.getKeywordValue(paramKey);
            // Header card
            } else if (fitsTable.hasHeaderCards()) {
                for (FitsHeaderCard card : fitsTable.getHeaderCards()) {
                    if (card.getKey().equals(paramKey)) {
                        return card.getValue();
                    }
                }
            }
        }
        
        // if nothing was found
        return null;
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        final ServiceResult result = getServiceResult(rowIndex);
        ColumnDesc columnDesc = listColumnDesc.get(columnIndex);
        
        switch (columnDesc.getSource()) {
            case HARD_CODED:
                switch (HardCodedColumn.valueOf(columnDesc.getName())) {
                    case INDEX: return getRowCount() - rowIndex;
                    case FILE: return result.getInputFile().getName();
                    case TARGET:
                        if (result.getOifitsFile() != null) {
                            return result.getOifitsFile().getImageOiData().getInputParam().getTarget();
                        }
                        break;
                    case TIMESTAMP_RECONSTRUCTION: return result.getEndTime();
                    case WAVELENGTH:
                        if (result.getOifitsFile() != null) {
                            return result.getOifitsFile().getWavelengthRange();
                        }
                        break;
                    case ALGORITHM: return result.getService().getProgram();
                    case RGL_WGT:
                        if (result.getOifitsFile() != null) {
                            return result.getOifitsFile().getImageOiData().getInputParam().getRglWgt();
                        }
                        break;
                    case SUCCESS: return result.isValid();
                    case RATING: return result.getRating();
                    case COMMENTS: return result.getComments();
                }
                break;
            case INPUT_PARAM:
                if (result.getOifitsFile() != null) {
                    final FitsTable inputFitsTable = result.getOifitsFile().getImageOiData().getInputParam();
                    return getKeywordValue(inputFitsTable, columnDesc.getName());
                }
                break;
            case OUTPUT_PARAM:
                if (result.getOifitsFile() != null) {
                    final FitsTable outputFitsTable = result.getOifitsFile().getImageOiData().getOutputParam();
                    return getKeywordValue(outputFitsTable, columnDesc.getName());
                }
                break;
        }
        
        // if nothing was found
        return null;
    }

    /** Some HardCoded Columns. 
     * It does not extends ColumnDesc because it already extends Enum */
    public enum HardCodedColumn  {
        INDEX(int.class, "Index"),
        FILE(String.class, "Name"), 
        TARGET(String.class, "Target"), 
        TIMESTAMP_RECONSTRUCTION(String.class, "Timestamp reconstruction"), 
        WAVELENGTH(double.class, "Wavelength"), 
        ALGORITHM(String.class, "Algorithm"), 
        RGL_WGT(String.class, "RGL_WGT"), 
        SUCCESS(boolean.class, "Success"), 
        RATING(Integer.class, "Rating"), 
        COMMENTS(String.class, "Comments");

        private final ColumnDesc columnDesc;

        private HardCodedColumn (Class dataClass, String label) {
            this.columnDesc = new ColumnDesc(super.toString(), dataClass, HARD_CODED, label);
        }

        public ColumnDesc getColumnDesc () { return columnDesc; }
    }
}

