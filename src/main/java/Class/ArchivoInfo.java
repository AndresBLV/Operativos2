package Class;


import Class.Archivo;
import Class.Directorio;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author Indatech
 */
public class ArchivoInfo {
    private Archivo archivo;
    private Directorio parentDir;
    
    public ArchivoInfo(Archivo archivo, Directorio parentDir) {
        this.archivo = archivo;
        this.parentDir = parentDir;
    }
    
    public Archivo getArchivo() {
        return archivo;
    }
    
    public Directorio getParentDir() {
        return parentDir;
    }
}
