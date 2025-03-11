package Class;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Indatech
 */


import Class.Archivo;
import EDD.Lista;
import javax.swing.table.AbstractTableModel;
import java.awt.Color;
import java.util.Map;

public class FileAllocationTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Nombre", "Tamaño (bloques)", "Primer Bloque", "Color"};
    private Lista<Object[]> data = new Lista<>();
    
    public void updateData(Lista<ArchivoInfo> archivosInfo, Map<String, Color> fileColorMap) {
        data = new Lista<>();
        for (int i = 0; i < archivosInfo.getTamaño(); i++) {
            ArchivoInfo info = archivosInfo.obtener(i);
            Archivo archivo = info.getArchivo();
            String path = info.getParentDir().getNmae() + "/" + archivo.getName();
            Color fileColor = fileColorMap.get(path);
            
            Object[] row = new Object[4];
            row[0] = archivo.getName();
            row[1] = archivo.getSize();
            row[2] = archivo.getFirstBlock() != null ? archivo.getFirstBlock().getId() : -1;
            row[3] = fileColor;
            
            data.agregar(row);
        }
        fireTableDataChanged();
    }
    
    @Override
    public int getRowCount() {
        return data.getTamaño();
    }
    
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
    
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= 0 && rowIndex < data.getTamaño()) {
            Object[] row = data.obtener(rowIndex);
            return row[columnIndex];
        }
        return null;
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 3) {
            return Color.class;
        }
        return String.class;
    }
}