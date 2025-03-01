/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package EDD;

/**
 *
 * @author andre
 * @param <T>
 */
public class Lista<T> {
    private Nodo<T> cabeza; // Primer nodo de la lista
    private int tamaño; // Tamaño de la lista

    // Constructor
    public Lista() {
        this.cabeza = null;
        this.tamaño = 0;
    }

    // Método para agregar un elemento al final de la lista
    public void agregar(T dato) {
        Nodo<T> nuevoNodo = new Nodo<>(dato);
        if (cabeza == null) {
            cabeza = nuevoNodo; // Si la lista está vacía, el nuevo nodo es la cabeza
        } else {
            Nodo<T> actual = cabeza;
            while (actual.getSiguiente() != null) {
                actual = actual.getSiguiente(); // Recorrer hasta el último nodo
            }
            actual.setSiguiente(nuevoNodo); // Enlazar el nuevo nodo al final
        }
        tamaño++;
    }

    // Método para eliminar un elemento por su índice
    public void eliminar(int indice) {
        if (indice < 0 || indice >= tamaño) {
            throw new IndexOutOfBoundsException("Índice fuera de rango");
        }
        if (indice == 0) {
            cabeza = cabeza.getSiguiente(); // Eliminar el primer nodo
        } else {
            Nodo<T> actual = cabeza;
            for (int i = 0; i < indice - 1; i++) {
                actual = actual.getSiguiente(); // Recorrer hasta el nodo anterior al que se eliminará
            }
            actual.setSiguiente(actual.getSiguiente().getSiguiente()); // Saltar el nodo a eliminar
        }
        tamaño--;
    }

    // Método para obtener un elemento por su índice
    public T obtener(int indice) {
        if (indice < 0 || indice >= tamaño) {
            throw new IndexOutOfBoundsException("Índice fuera de rango");
        }
        Nodo<T> actual = cabeza;
        for (int i = 0; i < indice; i++) {
            actual = actual.getSiguiente(); // Recorrer hasta el nodo deseado
        }
        return actual.getDato();
    }

    // Método para obtener el tamaño de la lista
    public int getTamaño() {
        return tamaño;
    }

    // Método para imprimir la lista
    public void imprimir() {
        Nodo<T> actual = cabeza;
        while (actual != null) {
            System.out.print(actual.getDato() + " -> ");
            actual = actual.getSiguiente();
        }
        System.out.println("null");
    }
}
