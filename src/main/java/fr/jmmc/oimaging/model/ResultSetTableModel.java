/** *****************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ***************************************************************************** */
package fr.jmmc.oimaging.model;

import fr.jmmc.oimaging.services.ServiceResult;
import fr.jmmc.oitools.fits.FitsHeaderCard;
import fr.jmmc.oitools.image.ImageOiInputParam;
import fr.jmmc.oitools.image.ImageOiOutputParam;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.table.AbstractTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author martin
 */
public class ResultSetTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(ResultSetTableModel.class);
   
    private final List<ServiceResult> results;
    
    // the ordered union of result columns (no duplicates).
    // it also contains some "hard-coded" columns
    // inputs columns from results are together. then the outputs columns.
    private final List<ColumnDesc> unionColumnDesc;

    public ResultSetTableModel() {
        super();
        results = new ArrayList<>();
        unionColumnDesc = new ArrayList<>();
        // add the hard-coded columns
        unionColumnDesc.add(HardCodedColumnDesc.INDEX);
        unionColumnDesc.add(HardCodedColumnDesc.FILE);
        unionColumnDesc.add(HardCodedColumnDesc.TARGET);
        unionColumnDesc.add(HardCodedColumnDesc.TIMESTAMP_RECONSTRUCTION);
        unionColumnDesc.add(HardCodedColumnDesc.WAVELENGTH);
        unionColumnDesc.add(HardCodedColumnDesc.ALGORITHM);
        unionColumnDesc.add(HardCodedColumnDesc.RGL_WGT);
        unionColumnDesc.add(HardCodedColumnDesc.SUCCESS);
        unionColumnDesc.add(HardCodedColumnDesc.RATING);
        unionColumnDesc.add(HardCodedColumnDesc.COMMENTS);
    }

    // updates results, then also updates columns (they depend on results)
    public void setResults(List<ServiceResult> results) {
        // update results
        this.results.clear();
        this.results.addAll(results);
        
        // update columns
        
        unionColumnDesc.clear();
        // add hardcoded columns 1
        unionColumnDesc.add(HardCodedColumnDesc.INDEX);
        unionColumnDesc.add(HardCodedColumnDesc.FILE);
        unionColumnDesc.add(HardCodedColumnDesc.TARGET);
        unionColumnDesc.add(HardCodedColumnDesc.TIMESTAMP_RECONSTRUCTION);
        unionColumnDesc.add(HardCodedColumnDesc.WAVELENGTH);
        unionColumnDesc.add(HardCodedColumnDesc.ALGORITHM);
        unionColumnDesc.add(HardCodedColumnDesc.RGL_WGT);
        // add all input columns
        for (ServiceResult result : results) {
            ImageOiInputParam input = result.getOifitsFile().getImageOiData().getInputParam();
            for (String name : input.getKeywordsValue().keySet()) {
                unionColumnDesc.add(new ParamColumnDesc(ColumnSource.INPUT_PARAM, name, Object.class)); // TODO: actual class
            }
            for (FitsHeaderCard card : input.getHeaderCards()) {
                unionColumnDesc.add(new ParamColumnDesc(ColumnSource.INPUT_PARAM, card.getKey(), card.getClass()));
            }
        }
        // add all output columns
        for (ServiceResult result : results) {
            ImageOiOutputParam output = result.getOifitsFile().getImageOiData().getOutputParam();
            for (String name : output.getKeywordsValue().keySet()) {
                unionColumnDesc.add(new ParamColumnDesc(ColumnSource.OUTPUT_PARAM, name, Object.class)); // TODO: actual class
            }
            for (FitsHeaderCard card : output.getHeaderCards()) {
                unionColumnDesc.add(new ParamColumnDesc(ColumnSource.OUTPUT_PARAM, card.getKey(), card.getClass()));
            }
        }
        // add hardcoded columns 2
        unionColumnDesc.add(HardCodedColumnDesc.SUCCESS);
        unionColumnDesc.add(HardCodedColumnDesc.RATING);
        unionColumnDesc.add(HardCodedColumnDesc.COMMENTS);
        
        // remove duplicates
        int nbElements = unionColumnDesc.size(); // number of elements (it will change because of remove)
        for (int i = 0 ; i < nbElements ; i ++) {
            // look if value is duplicate (compare with all elements to the left)
            for (int j = 0 ; j < i; j ++) {
                // if duplicate
                if (unionColumnDesc.get(j).equals(unionColumnDesc.get(i))) {
                    unionColumnDesc.remove(i);
                    // update loop variables because remove(i) shifts elements to the left
                    i -- ;
                    nbElements -- ;
                    break;
                }
            }
        }
        
        // notify changes
        fireTableDataChanged();
        fireTableStructureChanged();
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
        return unionColumnDesc.size();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return unionColumnDesc.get(columnIndex).getName();
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return unionColumnDesc.get(columnIndex).getDataClass();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        ColumnDesc columnDesc = unionColumnDesc.get(columnIndex);
        return columnDesc == HardCodedColumnDesc.COMMENTS 
                || columnDesc == HardCodedColumnDesc.RATING;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {

        final ServiceResult result = getServiceResult(rowIndex);
        ColumnDesc columnDesc = unionColumnDesc.get(columnIndex);

        if (columnDesc == HardCodedColumnDesc.COMMENTS) {
            result.setComments((String) value);
        }
        else if (columnDesc == HardCodedColumnDesc.RATING) {
            result.setRating((int) value);
        }
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        final ServiceResult result = getServiceResult(rowIndex);
        ColumnDesc columnDesc = unionColumnDesc.get(columnIndex);
        
        switch (columnDesc.getSource()) {
            case HARD_CODED:
                switch ((HardCodedColumnDesc) columnDesc) {
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
    
    public enum ColumnSource { HARD_CODED, INPUT_PARAM, OUTPUT_PARAM }

    public interface ColumnDesc {
        public String getName (); // name used for equality
        public String getLabel (); // name prettier to display in GUI
        public ColumnSource getSource ();
        public Class getDataClass (); // class of the data elements. ex int, String
    }

    public enum HardCodedColumnDesc implements ColumnDesc {
        INDEX("Index", int.class),
        FILE("Name", String.class), 
        TARGET("Target", String.class), 
        TIMESTAMP_RECONSTRUCTION("Timestamp reconstruction", String.class), 
        WAVELENGTH("Wavelength", double.class), 
        ALGORITHM("Algorithm", String.class), 
        RGL_WGT("RGL_WGT", String.class), 
        SUCCESS("Success", boolean.class), 
        RATING("Rating", int.class), 
        COMMENTS("Comments", String.class);

        private final String label;
        private final Class dataClass;

        private HardCodedColumnDesc (String label, Class dataClass) {
            this.label = label;
            this.dataClass = dataClass;
        }

        @Override public String getName () { return super.toString(); }
        @Override public String getLabel () { return label; }
        @Override public ColumnSource getSource () { return ColumnSource.HARD_CODED; }
        @Override public Class getDataClass () { return dataClass; }
    }

    public class ParamColumnDesc implements ColumnDesc {

        private final ColumnSource source;
        private final String name;
        private final String label;
        private final Class dataClass;

        public ParamColumnDesc (ColumnSource source, String name, String label, Class dataClass) {
            if (source == ColumnSource.INPUT_PARAM || source == ColumnSource.OUTPUT_PARAM) {
                this.source = source;
            }
            else {
                logger.warn("Incorrect source parameter for ParamColumnDesc : " + source);
                this.source = ColumnSource.INPUT_PARAM; // defaulting to this source
            }
            this.name = name;
            this.label = label;
            this.dataClass = dataClass;
        }
        public ParamColumnDesc (ColumnSource source, String name, Class dataClass) {
            this(source, name, name, dataClass);
        }

        @Override public String getName () { return name; }
        @Override public String getLabel () { return label; }
        @Override public ColumnSource getSource () { return source; }
        @Override public Class getDataClass () { return dataClass; }

        @Override public boolean equals (Object otherObject) {
            if (this == otherObject) return true;
            if (otherObject == null) return false;
            if (getClass() != otherObject.getClass()) return false;
            ParamColumnDesc other = (ParamColumnDesc) otherObject;
            return getName().equals(other.getName()) && getSource().equals(other.getSource());
        }
        @Override public int hashCode () { return Objects.hash(getSource(), getName()); }
    }

    
}

