/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Vista;

import Class.Archivo;
import Class.Directorio;
import java.awt.Container;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author andre
 */
public class arbolArchivos extends JFrame{
    
    //ilizar Clase Directorio con Lista de Archivos para realizar un for y colocar todos sus archivos
    public arbolArchivos(Directorio directorio, Archivo archivo){
        
        setTitle("Arbol de Directorios y Archivos");
        
        setBounds(350,300,600,200);
        
        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("C:");
        
        DefaultMutableTreeNode hijo = new DefaultMutableTreeNode(directorio);
        
        raiz.add(hijo);
        
        DefaultMutableTreeNode  subhijo = new DefaultMutableTreeNode(archivo);
        
        hijo.add(subhijo);
        
        JTree arbol = new JTree(raiz);
        
        Container laminaContenido = getContentPane();
        
        laminaContenido.add(new JScrollPane(arbol));
        
    }
    
}


