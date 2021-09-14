/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ***************************************************************************** */
package fr.jmmc.oimaging.model;

import fr.jmmc.oimaging.services.ServiceResult;
import fr.jmmc.oitools.fits.FitsHeaderCard;
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
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author martin
 */
public class ResultSetTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(ResultSetTableModel.class);
   
    private final List<ServiceResult> results;
    
    /** the union of result columns.
    * when there is several columns with same name, only one is keeped based on priority.
    * the decreasing priority is : OutputParam > InputParam > HardCoded.
    */
    private final LinkedHashSet<ColumnDesc> unionColumnDesc;
    
    /** originaly the same as unionColumnDesc but the GUI user can filter some columns. 
     it is a list because then it is simpler to answer to columns index. */
    private final List<ColumnDesc> userUnionColumnDesc;
    
    public ResultSetTableModel() {
        super();
        results = new ArrayList<>();
        unionColumnDesc = new LinkedHashSet<>();
        userUnionColumnDesc = new ArrayList<>();
    }

    public Set<ColumnDesc> getUnionColumnDesc () {
        return Collections.unmodifiableSet(unionColumnDesc);
    }
    
    /** read only of userUnionColumnDesc */
    public List<ColumnDesc> getUserUnionColumnDesc () {
        return Collections.unmodifiableList(userUnionColumnDesc);
    }
    
    public void setUserUnionColumnDesc (List<ColumnDesc> userUnionColumnDesc) {
        this.userUnionColumnDesc.clear();
        userUnionColumnDesc.sort(ColumnDesc.orderByRevSourceThenName);
        this.userUnionColumnDesc.addAll(userUnionColumnDesc);
        fireTableStructureChanged();
    }
    
    /** updates results, then also updates columns (they depend on results) */
    public void setResults(List<ServiceResult> results) {
        // update results
        this.results.clear();
        this.results.addAll(results);
        
        // Fusion of columns.
        // 1. we add every hardcoded and input/output params columns.
        // 2. we filter FITS standard keywords.
        // 3. we order by reverse source first, name second
        // 4. we add it to an Ordered Set, with equality on name only, so
        //    that there is a priority Output > Input > HardCoded on columns.
        // 5. we reset userUnionColumnDesc to unionColumnDesc.
        
        List<ColumnDesc> listColumnDesc = new ArrayList<>();
        
        // add hardcoded columns 
        for (HardCodedColumn hcc : HardCodedColumn.values()) {
            listColumnDesc.add(hcc.getColumnDesc());
        }
        
         // add all param columns
        for (ServiceResult result : results) {
            // input params
            ImageOiInputParam input = result.getOifitsFile().getImageOiData().getInputParam();
            for (KeywordMeta keyMeta : input.getKeywordsDesc().values()) {
                listColumnDesc.add(new ColumnDesc(ColumnSource.INPUT_PARAM, keyMeta.getName(), keyMeta.getClass())); 
            }
            for (FitsHeaderCard card : input.getHeaderCards()) {
                listColumnDesc.add(new ColumnDesc(ColumnSource.INPUT_PARAM, card.getKey(), card.getClass()));
            }
            // output params
            ImageOiOutputParam output = result.getOifitsFile().getImageOiData().getOutputParam();
            for (KeywordMeta keyMeta : output.getKeywordsDesc().values()) {
                listColumnDesc.add(new ColumnDesc(ColumnSource.OUTPUT_PARAM,keyMeta.getName(), keyMeta.getClass())); 
            }
            for (FitsHeaderCard card : output.getHeaderCards()) {
                listColumnDesc.add(new ColumnDesc(ColumnSource.OUTPUT_PARAM, card.getKey(), card.getClass()));
            }
        }
        
        // remove any standard FITS keyword.
        listColumnDesc.removeIf(col -> FitsUtils.isStandardKeyword(col.getName()));
        
        // order by reverse source first, then name
        listColumnDesc.sort(ColumnDesc.orderByRevSourceThenName);
        
        // move the list to the set, removing duplicates based on Name unicity
        // making a priority for duplicates : Ouput > Input > HardCoded
        unionColumnDesc.clear();
        listColumnDesc.forEach(unionColumnDesc::add);
        
        // clear user union columns
        // with some work, we could try to keep its previous choices in table editor
        // instead of reseting it like here.
        userUnionColumnDesc.clear();
        userUnionColumnDesc.addAll(unionColumnDesc);

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
        return userUnionColumnDesc.size();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return userUnionColumnDesc.get(columnIndex).getName();
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return userUnionColumnDesc.get(columnIndex).getDataClass();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        ColumnDesc columnDesc = userUnionColumnDesc.get(columnIndex);
        return columnDesc.equals(HardCodedColumn.COMMENTS.getColumnDesc())
                || columnDesc.equals(HardCodedColumn.RATING.getColumnDesc());
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {

        final ServiceResult result = getServiceResult(rowIndex);
        ColumnDesc columnDesc = userUnionColumnDesc.get(columnIndex);

        if (columnDesc.equals(HardCodedColumn.COMMENTS.getColumnDesc())) {
            result.setComments((String) value);
        }
        else if (columnDesc.equals(HardCodedColumn.RATING.getColumnDesc())) {
            result.setRating((int) value);
        }
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        final ServiceResult result = getServiceResult(rowIndex);
        ColumnDesc columnDesc = userUnionColumnDesc.get(columnIndex);
        
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
                    default: 
                        logger.warn("Unknown HardCodedColumnDesc :" + columnDesc);
                        return null;
                }
                break;
            case INPUT_PARAM:
                if (result.getOifitsFile() != null) {
                    final ImageOiInputParam param = result.getOifitsFile().getImageOiData().getInputParam();
                    // Keywords
                    if (param.hasKeywordMeta(columnDesc.getName())) {
                        return param.getKeywordValue(columnDesc.getName());
                    // Header card
                    } else if (param.hasHeaderCards()) {
                        for (FitsHeaderCard card : param.getHeaderCards()) {
                            if (card.getKey().equals(columnDesc.getName())) {
                                return card.getValue();
                            }
                        }
                        return null;
                    }
                    else return null;
                }
                else return null;
                // break; (unreachable statement)
            case OUTPUT_PARAM:
                if (result.getOifitsFile() != null) {
                    final ImageOiOutputParam param = result.getOifitsFile().getImageOiData().getOutputParam();
                    // Keywords
                    if (param.hasKeywordMeta(columnDesc.getName())) {
                        return param.getKeywordValue(columnDesc.getName());
                    // Header card
                    }  else if (param.hasHeaderCards()) {
                        for (FitsHeaderCard card : param.getHeaderCards()) {
                            if (card.getKey().equals(columnDesc.getName())) {
                                return card.getValue();
                            }
                        }
                        return null;
                    }
                    return null;
                }
                return null;
                // break; (unreachable statement)
        }
        return null;
    }
    
    // please don't change the order as it is used as is, for sorting operations
    public enum ColumnSource { HARD_CODED, INPUT_PARAM, OUTPUT_PARAM } 

    public static class ColumnDesc {
        
        /** source must be knowned to extract value for columns data. also used for ordering */
        private ColumnSource source;
        /** name is used for equality */
        private String name;
        /** class of the elements in the columns */
        private Class dataClass;
        /** Prettier name for GUI display */
        private String label;
        
        /** alternative order that is based on reverse Source first, Name second */
        public static Comparator<ColumnDesc> orderByRevSourceThenName = new Comparator<ColumnDesc> () {
            @Override public int compare (ColumnDesc a, ColumnDesc b) {
                int compareSource = (-1) * (a.getSource().compareTo(b.getSource()));
                if (compareSource == 0) return a.getName().compareTo(b.getName());
                else return compareSource ;
            }
        };
        
        public ColumnDesc (ColumnSource source, String name, Class dataClass, String label) {
            this.source = source;
            this.name = name;
            this.dataClass = dataClass;
            this.label = label;
        }
        public ColumnDesc (ColumnSource source, String name, Class dataClass) {
            this(source, name, dataClass, name);
        }
        
        public ColumnSource getSource () { return source; }
        public String getName () { return name; } 
        public Class getDataClass () { return dataClass; }
        public String getLabel () { return label; }
        
        /** Equality is only on name, not on source */
        @Override public boolean equals (Object otherObject) {
            if (this == otherObject) return true;
            if (otherObject == null) return false;
            if (getClass() != otherObject.getClass()) return false;
            ColumnDesc other = (ColumnDesc) otherObject;
            return getName().equals(other.getName());
        }
        @Override public int hashCode () { return Objects.hash(getName()); }
        @Override public String toString() { return getName(); }
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
            this.columnDesc = new ColumnDesc(ColumnSource.HARD_CODED, super.toString(), dataClass, label);
        }

        public ColumnDesc getColumnDesc () { return columnDesc; }
    }
}

