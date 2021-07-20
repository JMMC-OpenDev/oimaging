/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.jmmc.oimaging.model;

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
    
    private final static int INDEX = 0, FILE = 1, TARGET = 2, TIMESTAMP_RECONSTRUCTION = 3, WAVELENGTH = 4, ALGORITHM = 5, RGL_WGT = 6, SUCCESS = 7, RATING = 8, COMMENTS = 9;
    private static final String[] COLUMNS_NAMES = {"Index", "Name", "Target", "Timestamp reconstruction", "Wavelength", "Algorithm", "RGL_WGT", "Success", "Rating", "Comment"};
    private final List<ServiceResult> results;
    
    public ResultSetTableModel() {
        super();

        this.results = new ArrayList<>();
    }

    public void addResult(List<ServiceResult> result) {
        this.results.addAll(result);
        fireTableDataChanged();
    }

    public void removeResult(int rowIndex) {
        this.results.remove(rowIndex);
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
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case INDEX:
                return rowIndex;
            case FILE:
                return this.results.get(rowIndex).getInputFile().getName();

            case TARGET:
                try {
                return this.results.get(rowIndex).getOifitsFile().getImageOiData().getInputParam().getTarget();
            } catch (IOException | FitsException ex) {
                Logger.getLogger(ResultSetTableModel.class.getName()).log(Level.SEVERE, null, ex);
            }
            break;

            case TIMESTAMP_RECONSTRUCTION:
                return this.results.get(rowIndex).getEndTime();

            case WAVELENGTH:
            try {
                return 
                        Double.toString(this.results.get(rowIndex).getOifitsFile().getImageOiData().getInputParam().getWaveMin()) + " " 
                        + Double.toString(this.results.get(rowIndex).getOifitsFile().getImageOiData().getInputParam().getWaveMax());
            } catch (IOException | FitsException ex) {
                Logger.getLogger(ResultSetTableModel.class.getName()).log(Level.SEVERE, null, ex);
            }
            break;

            case ALGORITHM:
                return this.results.get(rowIndex).getService().getProgram();

            case RGL_WGT:
                try {
                return this.results.get(rowIndex).getOifitsFile().getImageOiData().getInputParam().getRglWgt();
            } catch (IOException | FitsException ex) {
                Logger.getLogger(ResultSetTableModel.class.getName()).log(Level.SEVERE, null, ex);
            }
            break;

            case SUCCESS:
                return this.results.get(rowIndex).isValid();

            case RATING:
                return "Rating WIP";

            case COMMENTS:
                return "Comment WIP";

            default:
                return null;
        }
        return null;
    }

}
