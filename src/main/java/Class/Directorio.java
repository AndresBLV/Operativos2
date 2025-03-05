/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Class;

import EDD.Lista;

/**
 *
 * @author andre
 */
public class Directorio {
    private String name;
    private Lista<Archivo> archivos;
    private Lista<Directorio> subdirectorios;
    
    public Directorio(String name, Lista archivos, Lista subdirectorios){
        this.name = name;
        this.archivos = archivos;
        this.subdirectorios = subdirectorios;
    }
    
    public Lista<Archivo> getArchivos(){
        return archivos;
    }
    
    public Lista<Directorio> getSubdirectorios(){
        return subdirectorios;
    }
    
    public String getNmae(){
        return name;
    }
    
    public void setName(String nName){
        this.name = nName;
    }
    
    public void setArchivos(Lista<Archivo> nArchivos){
        this.archivos = nArchivos;
    }
    
    public void setSubdirectorio(Lista<Directorio> nSub){
        this.subdirectorios = nSub;
    }
    
}
