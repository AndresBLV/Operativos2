/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Class;

import java.util.Date;

/**
 *
 * @author andre
 */
public class Archivo {
    private String name;
    private Date creationDate; 
    private int size; // Tamaño en bloques
    private Bloque firstBlock; // Primer bloque de la cadena

    // Constructor
    public Archivo(String name, int size) {
        this.name = name;
        this.size = size;
        this.firstBlock = null;
    }

    // Getters y Setters
    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }
    
    public Bloque getFirstBlock() {
        return firstBlock;
    }

    public void setFirstBlock(Bloque firstBlock) {
        this.firstBlock = firstBlock;
    }
    
    public void setName(String newName){
        this.name = newName;
    }
    
    public void setSize(int newSize){
        this.size = newSize;
    }
    
    public Date getDate(){
        return creationDate;
    }
    
    public void setDate(Date nDate){
        this.creationDate = nDate;
    }
    
}
