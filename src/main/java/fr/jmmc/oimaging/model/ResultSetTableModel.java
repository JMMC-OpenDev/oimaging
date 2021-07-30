/*******************************************************************************
 * JMMC project ( http://www.jmmc.fr ) - Copyright (C) CNRS.
 ******************************************************************************/
package fr.jmmc.oimaging.model;

import fr.jmmc.oimaging.services.Service;
import fr.jmmc.oimaging.services.ServiceResult;
import fr.nom.tam.fits.FitsException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;


/**
 *
 * @author martin
 */
public class ResultSetTableModel extends AbstractTableModel { 
    
    public final static int INDEX = 0, FILE = 1, TARGET = 2, TIMESTAMP_RECONSTRUCTION = 3, WAVELENGTH = 4, ALGORITHM = 5, RGL_WGT = 6, SUCCESS = 7, RATING = 8, COMMENTS = 9;
    private static final String[] COLUMNS_NAMES = {"Index", "Name", "Target", "Timestamp reconstruction", "Wavelength", "Algorithm", "RGL_WGT", "Success", "Rating", "Comment"};
    List<ServiceResult> results;
    
    private class Row {
        public ServiceResult result;
        public int rating = 5;
        public String comments = null;
        
        public Row(ServiceResult result) {
            this.result = result;
        }
    }
    List<Row> rows;
    
    public ResultSetTableModel() {
        super();
        
        results = new ArrayList<>();
        rows = new ArrayList<>();
    }

    public void addResult(List<ServiceResult> results) {
        this.results.addAll(results);
        results.forEach(result -> {
            rows.add(new Row(result));
        });
        fireTableDataChanged();
    }
    
    public void clear() {
       results.clear();
    }

    @Override
    public int getRowCount() {
        return this.results.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS_NAMES.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return COLUMNS_NAMES[columnIndex];
    }
    
    @Override
    public Class getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case SUCCESS:
                return boolean.class;
            default:
                return Object.class;                
        }
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (columnIndex == COMMENTS);
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case COMMENTS:
                rows.get(rowIndex).comments = (String) value;
        }
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        ServiceResult result = (ServiceResult) rows.get(rowIndex).result;

        switch (columnIndex) {
            case INDEX:
                return getRowCount() - rowIndex;
            case FILE:
                return result.getInputFile().getName();

            case TARGET:
                try {
                return result.getOifitsFile().getImageOiData().getInputParam().getTarget();
            } catch (IOException | FitsException ex) {
                Logger.getLogger(ResultSetTableModel.class.getName()).log(Level.SEVERE, null, ex);
            }
            break;

            case TIMESTAMP_RECONSTRUCTION:
                return result.getEndTime();

            case WAVELENGTH:
            try {
                    return result.getOifitsFile().getWavelengthRange();
            } catch (IOException | FitsException ex) {
                Logger.getLogger(ResultSetTableModel.class.getName()).log(Level.SEVERE, null, ex);
            }
            break;

            case ALGORITHM:
                return result.getService().getProgram();

            case RGL_WGT:
                try {
                return result.getOifitsFile().getImageOiData().getInputParam().getRglWgt();
            } catch (IOException | FitsException ex) {
                Logger.getLogger(ResultSetTableModel.class.getName()).log(Level.SEVERE, null, ex);
            }
            break;

            case SUCCESS:
                return result.isValid();

            case RATING:
                return rows.get(rowIndex).rating;

            case COMMENTS:
                return rows.get(rowIndex).comments;

            default:
                return null;
        }
        return null;
    }
    
}
