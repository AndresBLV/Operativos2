/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.operativos2;

import Class.Archivo;
import Simulador.DiskSimulator;
import Vista.FileSystemFrame;

/**
 *
 * @author andre
 */
public class Operativos2 {

    public static void main(String[] args) {
//        DiskSimulator disk = new DiskSimulator(10);
//        
//        Archivo file = new Archivo("documento.txt",3);
//        
//        Archivo file2 = new Archivo("documento.txt",4);
//        
//        Archivo file3 = new Archivo("documento.txt",4);
//        
//        if (disk.allocateFile(file)){
//            System.out.println("Archivo asignado correctamente");
//        }else{
//            System.out.println("No hay suficientes bloques libres");
//        }
//        
//        disk.displayDiskStatus();
//        
//        if (disk.allocateFile(file2)){
//            System.out.println("Archivo asignado correctamente");
//        }else{
//            System.out.println("No hay suficientes bloques libres");
//        }
//        
//        disk.displayDiskStatus();
//        
//        disk.freeFile(file);
//        System.out.println("Bloques liberados");
//        
//        disk.displayDiskStatus();
//        
//        if (disk.allocateFile(file3)){
//            System.out.println("Archivo asignado correctamente");
//        }else{
//            System.out.println("No hay suficientes bloques libres");
//        }
//        
//        disk.displayDiskStatus();
        FileSystemFrame frame = new FileSystemFrame();
        frame.setVisible(true);
    }
}
