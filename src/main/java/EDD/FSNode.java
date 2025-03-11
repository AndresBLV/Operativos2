/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EDD;

import Class.Directorio;

/**
 *
 * @author Indatech
 */
public class FSNode {
    private String name;
    private boolean isDirectory;
    private Object node; 
    
    public FSNode(String name, boolean isDirectory, Object node) {
        this.name = name;
        this.isDirectory = isDirectory;
        this.node = node;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public boolean isDirectory() {
        return isDirectory;
    }
    
    public Object getNode() {
        return node;
    }
    
    public Directorio getDirectory() {
        if (isDirectory) {
            return (Directorio) node;
        }
        return null;
    }
    
    @Override
    public String toString() {
        return name;
    }
}