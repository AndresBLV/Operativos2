/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Class;

import EDD.Lista;

/**
 *
 * @author Indatech
 */
public class JsonDirectory {
    private String name;
    private Lista<JsonFile> archivos;
    private Lista<JsonDirectory> directorio;

    // Getters y Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Lista<JsonFile> getArchivos() {
        return archivos;
    }

    public void setArchivos(Lista<JsonFile> archivos) {
        this.archivos = archivos;
    }

    public Lista<JsonDirectory> getDirectorio() {
        return directorio;
    }

    public void setDirectorio(Lista<JsonDirectory> directorio) {
        this.directorio = directorio;
    }
}
