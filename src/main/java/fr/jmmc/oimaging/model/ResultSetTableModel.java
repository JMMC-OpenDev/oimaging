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

    private static final String[] HEADERS = {"Tick", "Target", "Timestamp reconstruction", "Wavelength", "Algorithm", "RGL_WGT", "Success", "Rating", "Comment"};
    private final List<ServiceResult> results;

    public ResultSetTableModel() {
        super();

        this.results = new ArrayList<>();
    }

    public void addResult(ServiceResult result) {
        this.results.add(result);
        fireTableRowsInserted(results.size() - 1, results.size() - 1);
    }

    public void removeResult(int rowIndex) {
        this.results.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }
    
   public void clear() {
       for (int i = 0; i < getRowCount(); i++) removeResult(i);
   }

    @Override
    public int getRowCount() {
        return this.results.size();
    }

    @Override
    public int getColumnCount() {
        return HEADERS.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return HEADERS[columnIndex];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return this.results.get(rowIndex).getInputFile().getName();

            case 1:
                try {
                return this.results.get(rowIndex).getOifitsFile().getImageOiData().getInputParam().getTarget();
            } catch (IOException | FitsException ex) {
                Logger.getLogger(ResultSetTableModel.class.getName()).log(Level.SEVERE, null, ex);
            }
            break;

            case 2:
                return this.results.get(rowIndex).getEndTime();

            case 3:
            try {
                return 
                        Double.toString(this.results.get(rowIndex).getOifitsFile().getImageOiData().getInputParam().getWaveMin()) + " " 
                        + Double.toString(this.results.get(rowIndex).getOifitsFile().getImageOiData().getInputParam().getWaveMax());
            } catch (IOException | FitsException ex) {
                Logger.getLogger(ResultSetTableModel.class.getName()).log(Level.SEVERE, null, ex);
            }
            break;

            case 4:
                return this.results.get(rowIndex).getService().getProgram();

            case 5:
                try {
                return this.results.get(rowIndex).getOifitsFile().getImageOiData().getInputParam().getRglWgt();
            } catch (IOException | FitsException ex) {
                Logger.getLogger(ResultSetTableModel.class.getName()).log(Level.SEVERE, null, ex);
            }
            break;

            case 6:
                return this.results.get(rowIndex).isValid();

            case 7:
                return "Rating WIP";

            case 8:
                return "Comment WIP";

            default:
                return null;
        }
        return null;
    }

}
