/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Class;

import EDD.Lista;
import java.util.Date;

/**
 *
 * @author Indatech
 */
public class FileSystem {
    private int totalBlocks;
    private Bloque[] bloques;
    private Directorio rootDirectory;
    private Lista<ArchivoInfo> allFilesInfo;
    
    public FileSystem(int totalBlocks) {
        this.totalBlocks = totalBlocks;
        this.bloques = new Bloque[totalBlocks];
        
        // Inicializar todos los bloques
        for (int i = 0; i < totalBlocks; i++) {
            bloques[i] = new Bloque(i);
        }
        
        // Crear el directorio raíz
        Lista<Archivo> emptyArchivos = new Lista<>();
        Lista<Directorio> emptySubdirs = new Lista<>();
        this.rootDirectory = new Directorio("root", emptyArchivos, emptySubdirs);
        
        this.allFilesInfo = new Lista<>();
    }
    
    public Directorio getRootDirectory() {
        return rootDirectory;
    }
    
    public int getAvailableBlocks() {
        int count = 0;
        for (int i = 0; i < totalBlocks; i++) {
            if (bloques[i].isFree()) {
                count++;
            }
        }
        return count;
    }
    
    public Archivo createFile(String name, int size, Directorio parentDir) {
        if (size > getAvailableBlocks()) {
            return null;
        }
        
        // Crear el archivo
        Archivo archivo = new Archivo(name, size);
        archivo.setDate(new Date()); // Establecer la fecha de creación
        
        // Asignar bloques usando asignación encadenada
        Bloque firstBlock = null;
        Bloque prevBlock = null;
        
        for (int i = 0; i < size; i++) {
            Bloque freeBlock = findFreeBlock();
            if (freeBlock == null) {
                // Esto no debería ocurrir si verificamos correctamente el espacio
                return null;
            }
            
            freeBlock.setFree(false);
            
            if (firstBlock == null) {
                firstBlock = freeBlock;
                archivo.setFirstBlock(firstBlock);
            }
            
            if (prevBlock != null) {
                prevBlock.setNextBlock(freeBlock);
            }
            
            prevBlock = freeBlock;
            
        }
        
        // Añadir el archivo al directorio padre
        parentDir.getArchivos().agregar(archivo);
        
        // Añadir a la lista de todos los archivos
        allFilesInfo.agregar(new ArchivoInfo(archivo, parentDir));
        
        return archivo;
    }
    
    private Bloque findFreeBlock() {
        for (int i = 0; i < totalBlocks; i++) {
            if (bloques[i].isFree()) {
                return bloques[i];
            }
        }
        return null;
    }
    
    public Directorio createDirectory(String name, Directorio parentDir) {
        Lista<Archivo> emptyArchivos = new Lista<>();
        Lista<Directorio> emptySubdirs = new Lista<>();
        
        Directorio newDir = new Directorio(name, emptyArchivos, emptySubdirs);
        parentDir.getSubdirectorios().agregar(newDir);
        
        return newDir;
    }
    
    public void deleteFile(Archivo archivo, Directorio parentDir) {
        // Liberar los bloques del archivo
        Bloque bloque = archivo.getFirstBlock();
        while (bloque != null) {
            Bloque nextBloque = bloque.getNextBlock();
            bloque.setFree(true);
            bloque.setNextBlock(null);
            bloque = nextBloque;
        }
        
        // Eliminar el archivo del directorio padre
        Lista<Archivo> archivos = parentDir.getArchivos();
        for (int i = 0; i < archivos.getTamaño(); i++) {
            if (archivos.obtener(i) == archivo) {
                archivos.eliminar(i);
                break;
            }
        }
        
        // Eliminar de la lista de todos los archivos
        for (int i = 0; i < allFilesInfo.getTamaño(); i++) {
            ArchivoInfo info = allFilesInfo.obtener(i);
            if (info.getArchivo() == archivo && info.getParentDir() == parentDir) {
                allFilesInfo.eliminar(i);
                break;
            }
        }
    }
    
    public void deleteDirectory(Directorio directorio, Directorio parentDir) {
        // Eliminar recursivamente todos los archivos y subdirectorios
        
        // Primero eliminamos todos los archivos del directorio
        Lista<Archivo> archivos = directorio.getArchivos();
        while (archivos.getTamaño() > 0) {
            deleteFile(archivos.obtener(0), directorio);
        }
        
        // Luego eliminamos todos los subdirectorios recursivamente
        Lista<Directorio> subdirectorios = directorio.getSubdirectorios();
        while (subdirectorios.getTamaño() > 0) {
            deleteDirectory(subdirectorios.obtener(0), directorio);
        }
        
        // Finalmente, eliminamos este directorio del padre
        subdirectorios = parentDir.getSubdirectorios();
        for (int i = 0; i < subdirectorios.getTamaño(); i++) {
            if (subdirectorios.obtener(i) == directorio) {
                subdirectorios.eliminar(i);
                break;
            }
        }
    }
    
    public Lista<ArchivoInfo> getAllFilesInfo() {
        return allFilesInfo;
    }
    
    // Método para obtener todos los archivos recursivamente
    public void collectAllFiles(Directorio directory, Lista<ArchivoInfo> filesList) {
        // Añadir archivos del directorio actual
        Lista<Archivo> archivos = directory.getArchivos();
        for (int i = 0; i < archivos.getTamaño(); i++) {
            filesList.agregar(new ArchivoInfo(archivos.obtener(i), directory));
        }
        
        // Recorrer subdirectorios recursivamente
        Lista<Directorio> subdirectorios = directory.getSubdirectorios();
        for (int i = 0; i < subdirectorios.getTamaño(); i++) {
            collectAllFiles(subdirectorios.obtener(i), filesList);
        }
    }
    
    // Método para reconstruir la lista de todos los archivos
    public void rebuildAllFilesInfo() {
        allFilesInfo = new Lista<>();
        collectAllFiles(rootDirectory, allFilesInfo);
    }
    
    // Método para buscar un archivo por nombre (busqueda simple)
    public ArchivoInfo findFile(String fileName) {
        for (int i = 0; i < allFilesInfo.getTamaño(); i++) {
            ArchivoInfo info = allFilesInfo.obtener(i);
            if (info.getArchivo().getName().equals(fileName)) {
                return info;
            }
        }
        return null;
    }
    
    // Método para obtener estadísticas de fragmentación
    public double getFragmentationRatio() {
        int fragmentationCount = 0;
        int totalFiles = 0;
        
        for (int i = 0; i < allFilesInfo.getTamaño(); i++) {
            Archivo archivo = allFilesInfo.obtener(i).getArchivo();
            totalFiles++;
            
            // Contar cuántos bloques no son contiguos
            Bloque bloque = archivo.getFirstBlock();
            while (bloque != null && bloque.getNextBlock() != null) {
                if (bloque.getId() + 1 != bloque.getNextBlock().getId()) {
                    fragmentationCount++;
                }
                bloque = bloque.getNextBlock();
            }
        }
        
        if (totalFiles == 0) {
            return 0.0;
        }
        
        return (double) fragmentationCount / totalFiles;
    }
    
    // Método para desfragmentar el sistema de archivos
    public void defragment() {
        // Crear un mapa temporal para recordar cuáles archivos ya fueron procesados
        boolean[] processed = new boolean[allFilesInfo.getTamaño()];
        
        // Marcamos todos los bloques como libres temporalmente
        for (int i = 0; i < totalBlocks; i++) {
            bloques[i].setNextBlock(null);
            bloques[i].setFree(true);
        }
        
        // Índice del próximo bloque disponible
        int nextFreeBlock = 0;
        
        // Reasignar bloques continuos a cada archivo
        for (int i = 0; i < allFilesInfo.getTamaño(); i++) {
            if (!processed[i]) {
                Archivo archivo = allFilesInfo.obtener(i).getArchivo();
                int size = archivo.getSize();
                
                // Asignar bloques contiguos
                Bloque firstBlock = null;
                Bloque prevBlock = null;
                
                for (int j = 0; j < size; j++) {
                    // Verificar si hay espacio disponible
                    if (nextFreeBlock + j >= totalBlocks) {
                        // No hay suficiente espacio contiguo, esto no debería ocurrir
                        // en una desfragmentación normal
                        break;
                    }
                    
                    Bloque block = bloques[nextFreeBlock + j];
                    block.setFree(false);
                    
                    if (firstBlock == null) {
                        firstBlock = block;
                        archivo.setFirstBlock(firstBlock);
                    }
                    
                    if (prevBlock != null) {
                        prevBlock.setNextBlock(block);
                    }
                    
                    prevBlock = block;
                }
                
                // Actualizar el próximo bloque libre
                nextFreeBlock += size;
                
                // Marcar como procesado
                processed[i] = true;
            }
        }
    }
    
    // Método para obtener la información detallada de bloques
    public String[] getBlocksInfo() {
        String[] info = new String[totalBlocks];
        
        for (int i = 0; i < totalBlocks; i++) {
            Bloque bloque = bloques[i];
            if (bloque.isFree()) {
                info[i] = "Libre";
            } else {
                // Buscar a qué archivo pertenece este bloque
                String fileInfo = "Ocupado";
                for (int j = 0; j < allFilesInfo.getTamaño(); j++) {
                    Archivo archivo = allFilesInfo.obtener(j).getArchivo();
                    Bloque current = archivo.getFirstBlock();
                    while (current != null) {
                        if (current.getId() == i) {
                            fileInfo = "Archivo: " + archivo.getName();
                            break;
                        }
                        current = current.getNextBlock();
                    }
                }
                info[i] = fileInfo;
            }
        }
        
        return info;
    }
}