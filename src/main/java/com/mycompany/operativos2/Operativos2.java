/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.operativos2;
import Class.Archivo;
import Simulador.DiskSimulator;
/**
 *
 * @author andre
 */
public class Operativos2 {

    public static void main(String[] args) {
        DiskSimulator disk = new DiskSimulator(10);
        
        Archivo file = new Archivo("documento.txt",3);
        
        if (disk.allocateFile(file)){
            System.out.println("Archivo asignado correctamente");
        }else{
            System.out.println("No hay suficientes bloques libres");
        }
        
        disk.displayDiskStatus();
        
        disk.freeFile(file);
        System.out.println("Bloques liberados");
        
        disk.displayDiskStatus();
    }
}
