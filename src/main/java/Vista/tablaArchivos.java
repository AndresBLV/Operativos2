/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Vista;

import Class.Archivo;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 *
 * @author andre
 */
public class tablaArchivos extends JFrame{
    
    private String [] nombresColumna = {"Nombre","Bloques Asignados","Direccion"};
    
    private Archivo [][] datosFila;
    
    @SuppressWarnings("empty-statement")
    public tablaArchivos(Archivo archivo){
        
        setTitle("Tabla de Archivos");
        
        setBounds(350,300,600,200);
        
        JTable tablaArchivo = new JTable(datosFila, nombresColumna); //Definir Datos Fila
        
        add(new JScrollPane(tablaArchivo),BorderLayout.CENTER);
        
    }
    
}
