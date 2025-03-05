/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Class;

/**
 *
 * @author andre
 */
public class Bloque {
    private int id; // Identificador del bloque
    private boolean isFree; // Estado del bloque (libre u ocupado)
    private Bloque nextBlock; // Puntero al siguiente bloque en la cadena
    
    // Constructor
    public Bloque(int id) {
        this.id = id;
        this.isFree = true; // Por defecto, el bloque está libre
        this.nextBlock = null; // Inicialmente no tiene siguiente bloque
    }

    // Getters y Setters
    public int getId() {
        return id;
    }
    
    public void setId(int newId){
        this.id = newId;
    }

    public boolean isFree() {
        return isFree;
    }

    public void setFree(boolean free) {
        isFree = free;
    }

    public Bloque getNextBlock() {
        return nextBlock;
    }

    public void setNextBlock(Bloque nextBlock) {
        this.nextBlock = nextBlock;
    }

}
