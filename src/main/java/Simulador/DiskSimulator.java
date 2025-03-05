/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Simulador;
import Class.Archivo;
import Class.Bloque;
import EDD.Lista;

/**
 *
 * @author andre
 */
public class DiskSimulator {
    private Bloque[] blocks; // Arreglo de bloques
    private int totalBlocks; // Número total de bloques
    private int blockSize; // Tamaño de cada bloque en bytes

    // Constructor
    public DiskSimulator(int totalBlocks) {
        this.totalBlocks = totalBlocks;
        this.blocks = new Bloque[totalBlocks];
        // Inicializar bloques
        for (int i = 0; i < totalBlocks; i++) {
            blocks[i] = new Bloque(i);
        }
    }

    // Método para asignar bloques a un archivo
    public boolean allocateFile(Archivo file) {
        int blocksNeeded = file.getSize();
        Lista<Bloque> freeBlocks = new Lista<>();

        // Buscar bloques libres
        for (Bloque block : blocks) {
            if (block.isFree()) {
                freeBlocks.agregar(block);
                if (freeBlocks.getTamaño() == blocksNeeded) {
                    break;
                }
            }
        }

        // Si no hay suficientes bloques libres
        if (freeBlocks.getTamaño() < blocksNeeded) {
            return false; // No se puede asignar el archivo
        }

        // Asignar bloques al archivo
        for (int i = 0; i < freeBlocks.getTamaño(); i++) {
            Bloque currentBlock = freeBlocks.obtener(i);
            currentBlock.setFree(false); // Marcar como ocupado
            if (i < freeBlocks.getTamaño() - 1) {
                currentBlock.setNextBlock(freeBlocks.obtener(i + 1)); // Enlazar al siguiente bloque
            }
        }

        file.setFirstBlock(freeBlocks.obtener(0)); // Asignar el primer bloque al archivo
        return true;
    }

    // Método para liberar bloques de un archivo
    public void freeFile(Archivo file) {
        Bloque currentBlock = file.getFirstBlock();
        while (currentBlock != null) {
            currentBlock.setFree(true); // Marcar como libre
            currentBlock = currentBlock.getNextBlock();
        }
        file.setFirstBlock(null); // Eliminar referencia al primer bloque
    }

    // Método para mostrar el estado del disco
    public void displayDiskStatus() {
        for (Bloque block : blocks) {
            System.out.println("Block " + block.getId() + ": " + (block.isFree() ? "Free" : "Occupied"));
        }
    }    
}
