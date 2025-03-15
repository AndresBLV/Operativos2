package Vista;

import Class.ArchivoInfo;
import Class.FileAllocationTableModel;
import Class.Archivo;
import Class.Bloque;
import Class.Directorio;
import Class.FileSystem;
import EDD.FSNode;
import EDD.Lista;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class FileSystemFrame extends JFrame {
      
    private JTree fileTree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    
    private JTable fileAllocationTable;
    private FileAllocationTableModel tableModel;
    
    private JPanel diskPanel;
    private JLabel[] diskBlocks;
    
    private JButton createFileButton;
    private JButton createDirButton;
    private JButton renameButton;
    private JButton deleteButton;
    
    private JRadioButton adminMode;
    private JRadioButton userMode;
    
    private JLabel selectedItemLabel;
    private JLabel diskSpaceLabel;
    
    private FileSystem fileSystem;
    private boolean isAdminMode = true;
    
    private static final int TOTAL_BLOCKS = 100;
    private static final Color[] FILE_COLORS = {
        Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE, 
        Color.MAGENTA, Color.CYAN, Color.PINK, Color.YELLOW
    };
    
    private Map<String, Color> fileColorMap = new HashMap<>();
    private Random random = new Random();
    
    public FileSystemFrame() {
        setTitle("Simulador de Sistema de Archivos");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        fileSystem = new FileSystem(TOTAL_BLOCKS);
        
        // Inicializa componentes
        initComponents();
        
        // Carga datos guardados si existen
        loadSystemState();
        
        // Actualiza la visualización
        updateUI();
    }
    
    private void initComponents() {
        // Panel superior - Controles
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());
        
        createFileButton = new JButton("Crear Archivo");
        createDirButton = new JButton("Crear Directorio");
        renameButton = new JButton("Renombrar");
        deleteButton = new JButton("Eliminar");
        
        // Radio buttons para modo de usuario
        adminMode = new JRadioButton("Modo Administrador", true);
        userMode = new JRadioButton("Modo Usuario", false);
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(adminMode);
        modeGroup.add(userMode);
        
        // Agregar componentes al panel de control
        controlPanel.add(createFileButton);
        controlPanel.add(createDirButton);
        controlPanel.add(renameButton);
        controlPanel.add(deleteButton);
        controlPanel.add(new JSeparator(JSeparator.VERTICAL));
        controlPanel.add(adminMode);
        controlPanel.add(userMode);
        
        // Panel izquierdo - JTree
        
        rootNode = new DefaultMutableTreeNode(new FSNode("root", true, null));
        treeModel = new DefaultTreeModel(rootNode);
        fileTree = new JTree(treeModel);
        JScrollPane treeScrollPane = new JScrollPane(fileTree);
        treeScrollPane.setPreferredSize(new Dimension(300, 500));
        
        // Panel central - Información y disco
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        
        // Panel de información
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        selectedItemLabel = new JLabel("Selección: Ninguna");
        diskSpaceLabel = new JLabel("Espacio Disponible: " + fileSystem.getAvailableBlocks() + "/" + TOTAL_BLOCKS + " bloques");
        infoPanel.add(selectedItemLabel);
        infoPanel.add(diskSpaceLabel);
        
        // Panel del disco
        diskPanel = new JPanel();
        diskPanel.setLayout(new GridLayout(10, 10, 2, 2));
        diskBlocks = new JLabel[TOTAL_BLOCKS];
        for (int i = 0; i < TOTAL_BLOCKS; i++) {
            diskBlocks[i] = new JLabel(String.valueOf(i), JLabel.CENTER);
            diskBlocks[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            diskBlocks[i].setOpaque(true);
            diskBlocks[i].setBackground(Color.WHITE);
            diskPanel.add(diskBlocks[i]);
        }
        
        centerPanel.add(infoPanel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(diskPanel), BorderLayout.CENTER);
        
        // Panel inferior - Tabla de asignación de archivos
        tableModel = new FileAllocationTableModel();
        fileAllocationTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(fileAllocationTable);
        tableScrollPane.setPreferredSize(new Dimension(400, 150));
        
        // Agregar todos los paneles al frame
        add(controlPanel, BorderLayout.NORTH);
        add(treeScrollPane, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(tableScrollPane, BorderLayout.SOUTH);
        
        // Agregar listeners
        createFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isAdminMode) {
                    JOptionPane.showMessageDialog(FileSystemFrame.this, 
                            "Operación no permitida en modo usuario",
                            "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
                if (selectedNode == null) {
                    JOptionPane.showMessageDialog(FileSystemFrame.this, 
                            "Por favor seleccione un directorio",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                FSNode fsNode = (FSNode) selectedNode.getUserObject();
                if (!fsNode.isDirectory()) {
                    JOptionPane.showMessageDialog(FileSystemFrame.this, 
                            "No se puede crear un archivo dentro de un archivo",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String fileName = JOptionPane.showInputDialog(FileSystemFrame.this, 
                        "Ingrese el nombre del archivo:");
                if (fileName == null || fileName.trim().isEmpty()) {
                    return;
                }
                
                // Verificar si ya existe un archivo con ese nombre
                if (fileSystem.getRootDirectory().getNmae() == fsNode.toString()){
                    Directorio directorio = fileSystem.getRootDirectory();
                    Lista<Archivo> archivos = directorio.getArchivos();
                    for (int i = 0; i < archivos.getTamaño(); i++) {
                        if (archivos.obtener(i).getName().equals(fileName)) {
                            JOptionPane.showMessageDialog(FileSystemFrame.this, 
                                    "Ya existe un archivo con ese nombre",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    // Verificar subdirectorios también
                    Lista<Directorio> subdirectorios = directorio.getSubdirectorios();
                    for (int i = 0; i < subdirectorios.getTamaño(); i++) {
                        if (subdirectorios.obtener(i).getNmae().equals(fileName)) {
                            JOptionPane.showMessageDialog(FileSystemFrame.this, 
                                    "Ya existe un directorio con ese nombre",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    int fileSize = 0;
                    try {
                        String sizeStr = JOptionPane.showInputDialog(FileSystemFrame.this, 
                                "Ingrese el tamaño del archivo en bloques:");
                        if (sizeStr == null) {
                            return;
                        }
                        fileSize = Integer.parseInt(sizeStr);
                        if (fileSize <= 0) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(FileSystemFrame.this, 
                                "Por favor ingrese un número entero positivo",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Verificar si hay suficiente espacio
                    if (fileSize > fileSystem.getAvailableBlocks()) {
                        JOptionPane.showMessageDialog(FileSystemFrame.this, 
                                "No hay suficiente espacio en disco. Disponible: " 
                                        + fileSystem.getAvailableBlocks() + " bloques",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Crear el archivo
                    Archivo archivo = fileSystem.createFile(fileName, fileSize, directorio);
                    if (archivo != null) {
                        // Asignar un color aleatorio al archivo
                        Color fileColor = FILE_COLORS[random.nextInt(FILE_COLORS.length)];
                        fileColorMap.put(getFilePath(archivo, directorio), fileColor);

                        FSNode newFileNode = new FSNode(fileName, false, archivo);
                        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newFileNode);
                        selectedNode.add(newNode);
                        treeModel.nodesWereInserted(selectedNode, new int[]{selectedNode.getChildCount() - 1});
                        updateUI();

                        // Guardar el estado del sistema
                        saveSystemState();
                    } 
                }else{
                    Directorio directorio = (Directorio) fsNode.getNode();
                    Lista<Archivo> archivos = directorio.getArchivos();
                    for (int i = 0; i < archivos.getTamaño(); i++) {
                        if (archivos.obtener(i).getName().equals(fileName)) {
                            JOptionPane.showMessageDialog(FileSystemFrame.this, 
                                    "Ya existe un archivo con ese nombre",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    // Verificar subdirectorios también
                    Lista<Directorio> subdirectorios = directorio.getSubdirectorios();
                    for (int i = 0; i < subdirectorios.getTamaño(); i++) {
                        if (subdirectorios.obtener(i).getNmae().equals(fileName)) {
                            JOptionPane.showMessageDialog(FileSystemFrame.this, 
                                    "Ya existe un directorio con ese nombre",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    int fileSize = 0;
                    try {
                        String sizeStr = JOptionPane.showInputDialog(FileSystemFrame.this, 
                                "Ingrese el tamaño del archivo en bloques:");
                        if (sizeStr == null) {
                            return;
                        }
                        fileSize = Integer.parseInt(sizeStr);
                        if (fileSize <= 0) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException ex) {    
                        JOptionPane.showMessageDialog(FileSystemFrame.this, 
                                "Por favor ingrese un número entero positivo",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Verificar si hay suficiente espacio
                    if (fileSize > fileSystem.getAvailableBlocks()) {
                        JOptionPane.showMessageDialog(FileSystemFrame.this, 
                                "No hay suficiente espacio en disco. Disponible: " 
                                        + fileSystem.getAvailableBlocks() + " bloques",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Crear el archivo
                    Archivo archivo = fileSystem.createFile(fileName, fileSize, directorio);
                    if (archivo != null) {
                        // Asignar un color aleatorio al archivo
                        Color fileColor = FILE_COLORS[random.nextInt(FILE_COLORS.length)];
                        fileColorMap.put(getFilePath(archivo, directorio), fileColor);

                        FSNode newFileNode = new FSNode(fileName, false, archivo);
                        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newFileNode);
                        selectedNode.add(newNode);
                        treeModel.nodesWereInserted(selectedNode, new int[]{selectedNode.getChildCount() - 1});
                        updateUI();

                        // Guardar el estado del sistema
                        saveSystemState();
                    }
                }
            }
        });
        
        createDirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isAdminMode) {
                    JOptionPane.showMessageDialog(FileSystemFrame.this, 
                            "Operación no permitida en modo usuario",
                            "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
                if (selectedNode == null) {
                    JOptionPane.showMessageDialog(FileSystemFrame.this, 
                            "Por favor seleccione un directorio",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                FSNode fsNode = (FSNode) selectedNode.getUserObject();
                if (!fsNode.isDirectory()) {
                    JOptionPane.showMessageDialog(FileSystemFrame.this, 
                            "No se puede crear un directorio dentro de un archivo",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String dirName = JOptionPane.showInputDialog(FileSystemFrame.this, 
                        "Ingrese el nombre del directorio:");
                if (dirName == null || dirName.trim().isEmpty()) {
                    return;
                }
                
                if (fileSystem.getRootDirectory().getNmae() == fsNode.toString()){
                    // Verificar si ya existe un directorio con ese nombre
                    Directorio parentDir = fileSystem.getRootDirectory();
                    Lista<Directorio> subdirectorios = parentDir.getSubdirectorios();
                    for (int i = 0; i < subdirectorios.getTamaño(); i++) {
                        if (subdirectorios.obtener(i).getNmae().equals(dirName)) {
                            JOptionPane.showMessageDialog(FileSystemFrame.this, 
                                    "Ya existe un directorio con ese nombre",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    // Verificar archivos también
                    Lista<Archivo> archivos = parentDir.getArchivos();
                    for (int i = 0; i < archivos.getTamaño(); i++) {
                        if (archivos.obtener(i).getName().equals(dirName)) {
                            JOptionPane.showMessageDialog(FileSystemFrame.this, 
                                    "Ya existe un archivo con ese nombre",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    // Crear el directorio
                    Directorio newDir = fileSystem.createDirectory(dirName, parentDir);

                    FSNode newDirNode = new FSNode(dirName, true, newDir);
                    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newDirNode);
                    selectedNode.add(newNode);
                    treeModel.nodesWereInserted(selectedNode, new int[]{selectedNode.getChildCount() - 1});
                    updateUI();

                    // Guardar el estado del sistema
                    saveSystemState();
                }else{
                    // Verificar si ya existe un directorio con ese nombre
                    Directorio parentDir = (Directorio) fsNode.getNode();
                    Lista<Directorio> subdirectorios = parentDir.getSubdirectorios();
                    for (int i = 0; i < subdirectorios.getTamaño(); i++) {
                        if (subdirectorios.obtener(i).getNmae().equals(dirName)) {
                            JOptionPane.showMessageDialog(FileSystemFrame.this, 
                                    "Ya existe un directorio con ese nombre",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    // Verificar archivos también
                    Lista<Archivo> archivos = parentDir.getArchivos();
                    for (int i = 0; i < archivos.getTamaño(); i++) {
                        if (archivos.obtener(i).getName().equals(dirName)) {
                            JOptionPane.showMessageDialog(FileSystemFrame.this, 
                                    "Ya existe un archivo con ese nombre",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    // Crear el directorio
                    Directorio newDir = fileSystem.createDirectory(dirName, parentDir);

                    FSNode newDirNode = new FSNode(dirName, true, newDir);
                    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newDirNode);
                    selectedNode.add(newNode);
                    treeModel.nodesWereInserted(selectedNode, new int[]{selectedNode.getChildCount() - 1});
                    updateUI();

                    // Guardar el estado del sistema
                    saveSystemState();
                }
            }
        });
        
        renameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isAdminMode) {
                    JOptionPane.showMessageDialog(FileSystemFrame.this, 
                            "Operación no permitida en modo usuario",
                            "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
                if (selectedNode == null || selectedNode == rootNode) {
                    JOptionPane.showMessageDialog(FileSystemFrame.this, 
                            "Seleccione un archivo o directorio para renombrar",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                FSNode fsNode = (FSNode) selectedNode.getUserObject();
                String newName = JOptionPane.showInputDialog(FileSystemFrame.this, 
                        "Ingrese el nuevo nombre:", fsNode.getName());
                if (newName == null || newName.trim().isEmpty()) {
                    return;
                }
                
                // Obtener el directorio padre para verificar duplicados
                DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent();                
                FSNode parentFSNode = (FSNode) parentNode.getUserObject();              
                
                if (fileSystem.getRootDirectory().getNmae() == parentFSNode.toString()){
                    Directorio parentDir = fileSystem.getRootDirectory();
                    // Verificar duplicados en archivos
                    Lista<Archivo> archivos = parentDir.getArchivos();
                    for (int i = 0; i < archivos.getTamaño(); i++) {
                        Archivo archivo = archivos.obtener(i);
                        if (!archivo.getName().equals(fsNode.getName()) && archivo.getName().equals(newName)) {
                            JOptionPane.showMessageDialog(FileSystemFrame.this, 
                                    "Ya existe un archivo con ese nombre",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    // Verificar duplicados en directorios
                    Lista<Directorio> subdirectorios = parentDir.getSubdirectorios();
                    for (int i = 0; i < subdirectorios.getTamaño(); i++) {
                        Directorio dir = subdirectorios.obtener(i);
                        if (!dir.getNmae().equals(fsNode.getName()) && dir.getNmae().equals(newName)) {
                            JOptionPane.showMessageDialog(FileSystemFrame.this, 
                                    "Ya existe un directorio con ese nombre",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    // Renombrar el nodo
                    if (fsNode.isDirectory()) {
                        Directorio dir = (Directorio) fsNode.getNode();
                        dir.setName(newName);
                    } else {
                        Archivo archivo = (Archivo) fsNode.getNode();

                        // Guardar el color antes de renombrar
                        String oldPath = getFilePath(archivo, parentDir);
                        Color fileColor = fileColorMap.get(oldPath);
                        fileColorMap.remove(oldPath);

                        archivo.setName(newName);

                        // Asignar el color al nuevo path
                        fileColorMap.put(getFilePath(archivo, parentDir), fileColor);
                    }

                    fsNode.setName(newName);
                    treeModel.nodeChanged(selectedNode);
                    updateUI();

                    // Guardar el estado del sistema
                    saveSystemState();
                }else{
                    Directorio parentDir = (Directorio) parentFSNode.getNode();
                    // Verificar duplicados en archivos
                    Lista<Archivo> archivos = parentDir.getArchivos();
                    for (int i = 0; i < archivos.getTamaño(); i++) {
                        Archivo archivo = archivos.obtener(i);
                        if (!archivo.getName().equals(fsNode.getName()) && archivo.getName().equals(newName)) {
                            JOptionPane.showMessageDialog(FileSystemFrame.this, 
                                    "Ya existe un archivo con ese nombre",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    // Verificar duplicados en directorios
                    Lista<Directorio> subdirectorios = parentDir.getSubdirectorios();
                    for (int i = 0; i < subdirectorios.getTamaño(); i++) {
                        Directorio dir = subdirectorios.obtener(i);
                        if (!dir.getNmae().equals(fsNode.getName()) && dir.getNmae().equals(newName)) {
                            JOptionPane.showMessageDialog(FileSystemFrame.this, 
                                    "Ya existe un directorio con ese nombre",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }

                    // Renombrar el nodo
                    if (fsNode.isDirectory()) {
                        Directorio dir = (Directorio) fsNode.getNode();
                        dir.setName(newName);
                    } else {
                        Archivo archivo = (Archivo) fsNode.getNode();

                        // Guardar el color antes de renombrar
                        String oldPath = getFilePath(archivo, parentDir);
                        Color fileColor = fileColorMap.get(oldPath);
                        fileColorMap.remove(oldPath);

                        archivo.setName(newName);

                        // Asignar el color al nuevo path
                        fileColorMap.put(getFilePath(archivo, parentDir), fileColor);
                    }

                    fsNode.setName(newName);
                    treeModel.nodeChanged(selectedNode);
                    updateUI();

                    // Guardar el estado del sistema
                    saveSystemState();
                }
            }
        });
        
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isAdminMode) {
                    JOptionPane.showMessageDialog(FileSystemFrame.this, 
                            "Operación no permitida en modo usuario",
                            "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
                if (selectedNode == null || selectedNode == rootNode) {
                    JOptionPane.showMessageDialog(FileSystemFrame.this, 
                            "Seleccione un archivo o directorio para eliminar",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                FSNode fsNode = (FSNode) selectedNode.getUserObject();
                int confirm = JOptionPane.showConfirmDialog(FileSystemFrame.this, 
                        "¿Está seguro de que desea eliminar " + fsNode.getName() + "?",
                        "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    // Obtener el padre para eliminar la referencia
                    DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent();
                    FSNode parentFSNode = (FSNode) parentNode.getUserObject();
                    
                    if (fileSystem.getRootDirectory().getNmae() == parentFSNode.toString()){
                        Directorio parentDir = fileSystem.getRootDirectory();
                        // Eliminar del sistema de archivos
                        if (fsNode.isDirectory()) {
                            // Si es un directorio, eliminamos recursivamente
                            fileSystem.deleteDirectory((Directorio)fsNode.getNode(), parentDir);
                        } else {
                            // Si es un archivo, lo eliminamos directamente
                            Archivo archivo = (Archivo) fsNode.getNode();
                            fileSystem.deleteFile(archivo, parentDir);
                            fileColorMap.remove(getFilePath(archivo, parentDir));
                        }

                        // Eliminar del árbol
                        treeModel.removeNodeFromParent(selectedNode);
                        updateUI();

                        // Guardar el estado del sistema
                        saveSystemState();    
                    }else{
                        Directorio parentDir = (Directorio) parentFSNode.getNode();
                        
                        // Eliminar del sistema de archivos
                        if (fsNode.isDirectory()) {
                            // Si es un directorio, eliminamos recursivamente
                            fileSystem.deleteDirectory((Directorio)fsNode.getNode(), parentDir);
                        } else {
                            // Si es un archivo, lo eliminamos directamente
                            Archivo archivo = (Archivo) fsNode.getNode();
                            fileSystem.deleteFile(archivo, parentDir);
                            fileColorMap.remove(getFilePath(archivo, parentDir));
                        }

                        // Eliminar del árbol
                        treeModel.removeNodeFromParent(selectedNode);
                        updateUI();

                        // Guardar el estado del sistema
                        saveSystemState();
                    }
                }
            }
        });
        
        // Listener para modos de usuario
        ActionListener modeListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isAdminMode = adminMode.isSelected();
                updateButtonStates();
            }
        };
        adminMode.addActionListener(modeListener);
        userMode.addActionListener(modeListener);
        
        // Listener para el árbol
        fileTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) fileTree.getLastSelectedPathComponent();
                if (selectedNode != null) {
                    FSNode fsNode = (FSNode) selectedNode.getUserObject();
                    StringBuilder info = new StringBuilder("Selección: " + fsNode.getName());
                    if (fsNode.isDirectory()) {
                        info.append(" (Directorio)");
                        Directorio dir = (Directorio) fsNode.getNode();
                        if (dir != null) {
                            info.append(" - Archivos: ").append(dir.getArchivos().getTamaño());
                            info.append(" - Subdirectorios: ").append(dir.getSubdirectorios().getTamaño());
                        }
                    } else {
                        Archivo archivo = (Archivo) fsNode.getNode();
                        info.append(" (Archivo) - Tamaño: ").append(archivo.getSize()).append(" bloques");
                        if (archivo.getFirstBlock() != null) {
                            info.append(" - Primer bloque: ").append(archivo.getFirstBlock().getId());
                        }
                    }
                    selectedItemLabel.setText(info.toString());
                } else {
                    selectedItemLabel.setText("Selección: Ninguna");
                }
            }
        });
        
        // Configuración inicial de los botones
        updateButtonStates();
    }
    
    private void updateButtonStates() {
        createFileButton.setEnabled(isAdminMode);
        createDirButton.setEnabled(isAdminMode);
        renameButton.setEnabled(isAdminMode);
        deleteButton.setEnabled(isAdminMode);
    }
    
    private String getFilePath(Archivo archivo, Directorio parent) {
        // Método simple para obtener un identificador único del archivo
        return parent.getNmae() + "/" + archivo.getName();
    }
    
    private void updateUI() {
        // Actualizar etiqueta de espacio en disco
        diskSpaceLabel.setText("Espacio Disponible: " + fileSystem.getAvailableBlocks() + "/" + TOTAL_BLOCKS + " bloques");
        
        // Actualizar visualización del disco
        for (int i = 0; i < TOTAL_BLOCKS; i++) {
            diskBlocks[i].setBackground(Color.WHITE);
            diskBlocks[i].setForeground(Color.BLACK);
        }
        
        // Recorrer todos los archivos y colorear los bloques
        Lista<ArchivoInfo> archivosInfo = fileSystem.getAllFilesInfo();
        for (int i = 0; i < archivosInfo.getTamaño(); i++) {
            ArchivoInfo info = archivosInfo.obtener(i);
            Archivo archivo = info.getArchivo();
            Directorio parent = info.getParentDir();
            
            String path = getFilePath(archivo, parent);
            Color fileColor = fileColorMap.get(path);
            if (fileColor == null) {
                fileColor = FILE_COLORS[random.nextInt(FILE_COLORS.length)];
                fileColorMap.put(path, fileColor);
            }
            
            // Colorear los bloques del archivo
            Bloque bloque = archivo.getFirstBlock();
            while (bloque != null) {
                int blockId = bloque.getId();
                diskBlocks[blockId].setBackground(fileColor);
                diskBlocks[blockId].setForeground(Color.WHITE);
                bloque = bloque.getNextBlock();
            }
        }
        
        // Actualizar la tabla de asignación de archivos
        tableModel.updateData(archivosInfo, fileColorMap);
    }
    
    private void saveSystemState() {
        try (FileWriter writer = new FileWriter("filesystem_state.json")) {
            // Aquí se implementaría el guardado real utilizando JSON u otro formato
            // Por simplicidad, este código no implementa la funcionalidad completa
            writer.write("{ \"message\": \"Sistema guardado\" }");
        } catch (IOException e) {
            System.err.println("Error al guardar el estado del sistema: " + e.getMessage());
        }
    }
    
    private void loadSystemState() {
        try (FileReader reader = new FileReader("filesystem_state.json")) {
            // Aquí se implementaría la carga real desde JSON u otro formato
            // Por simplicidad, este código no implementa la funcionalidad completa
        } catch (IOException e) {
            System.err.println("No se encontró estado guardado del sistema o error al cargar: " + e.getMessage());
        }
    }
}