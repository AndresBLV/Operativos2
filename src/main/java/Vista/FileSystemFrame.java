package Vista;

import Class.ArchivoInfo;
import Class.FileAllocationTableModel;
import Class.Archivo;
import Class.AuditLogger;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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
    private JButton saveButton;

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
        saveButton = new JButton("Guardar");

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
        controlPanel.add(saveButton);
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

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isAdminMode) {
                    JOptionPane.showMessageDialog(FileSystemFrame.this,
                            "Operación no permitida en modo usuario",
                            "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                saveSystemStateToJson();  // Guardar el estado del sistema en JSON
                AuditLogger.log("SAVEDATA", "Sistema guardado de forma exitosa", "Administrador");
            }
        });

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
                    AuditLogger.log("ERROR", "Por favor seleccione un directorio", "Administrador");
                    return;
                }

                FSNode fsNode = (FSNode) selectedNode.getUserObject();
                if (!fsNode.isDirectory()) {
                    JOptionPane.showMessageDialog(FileSystemFrame.this,
                            "No se puede crear un archivo dentro de un archivo",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    AuditLogger.log("ERROR", "No se puede crear un archivo dentro de un archivo", "Administrador");
                    return;
                }

                String fileName = JOptionPane.showInputDialog(FileSystemFrame.this,
                        "Ingrese el nombre del archivo:");
                if (fileName == null || fileName.trim().isEmpty()) {
                    return;
                }

                // Verificar si ya existe un archivo con ese nombre
                if (fileSystem.getRootDirectory().getNmae() == fsNode.toString()) {
                    Directorio directorio = fileSystem.getRootDirectory();
                    Lista<Archivo> archivos = directorio.getArchivos();
                    for (int i = 0; i < archivos.getTamaño(); i++) {
                        if (archivos.obtener(i).getName().equals(fileName)) {
                            JOptionPane.showMessageDialog(FileSystemFrame.this,
                                    "Ya existe un archivo con ese nombre",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            AuditLogger.log("ERROR", "Ya existe un archivo con ese nombre", "Administrador");
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
                            AuditLogger.log("ERROR", "Ya existe un directorio con ese nombre", "Administrador");
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
                        AuditLogger.log("ERROR", "Por favor ingrese un número entero positivo", "Administrador");
                        return;
                    }

                    // Verificar si hay suficiente espacio
                    if (fileSize > fileSystem.getAvailableBlocks()) {
                        JOptionPane.showMessageDialog(FileSystemFrame.this,
                                "No hay suficiente espacio en disco. Disponible: "
                                + fileSystem.getAvailableBlocks() + " bloques",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        AuditLogger.log("ERROR", "No hay suficiente espacio en disco. Disponible: "
                                + fileSystem.getAvailableBlocks() + " bloques", "Administrador");
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

                        // Guardar Registro de auditoria
                        AuditLogger.log("CREATE_FILE", "Archivo creado: " + fileName, "Administrador");
                    }
                } else {
                    Directorio directorio = (Directorio) fsNode.getNode();
                    Lista<Archivo> archivos = directorio.getArchivos();
                    for (int i = 0; i < archivos.getTamaño(); i++) {
                        if (archivos.obtener(i).getName().equals(fileName)) {
                            JOptionPane.showMessageDialog(FileSystemFrame.this,
                                    "Ya existe un archivo con ese nombre",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            AuditLogger.log("ERROR", "Ya existe un archivo con ese nombre", "Administrador");
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
                            AuditLogger.log("ERROR", "Ya existe un directorio con ese nombre", "Administrador");
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
                        AuditLogger.log("ERROR", "Por favor ingrese un número entero positivo", "Administrador");
                        return;
                    }

                    // Verificar si hay suficiente espacio
                    if (fileSize > fileSystem.getAvailableBlocks()) {
                        JOptionPane.showMessageDialog(FileSystemFrame.this,
                                "No hay suficiente espacio en disco. Disponible: "
                                + fileSystem.getAvailableBlocks() + " bloques",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        AuditLogger.log("ERROR", "No hay suficiente espacio en disco. Disponible: "
                                + fileSystem.getAvailableBlocks() + " bloques", "Administrador");
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

                        // Guardar Registro de Auditoria
                        AuditLogger.log("CREATE_FILE", "Archivo creado: " + fileName, "Administrador");
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
                    AuditLogger.log("ERROR", "Por favor seleccione un directorio", "Administrador");
                    return;
                }

                FSNode fsNode = (FSNode) selectedNode.getUserObject();
                if (!fsNode.isDirectory()) {
                    JOptionPane.showMessageDialog(FileSystemFrame.this,
                            "No se puede crear un directorio dentro de un archivo",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    AuditLogger.log("ERROR", "No se puede crear un directorio dentro de un archivo", "Administrador");
                    return;
                }

                String dirName = JOptionPane.showInputDialog(FileSystemFrame.this,
                        "Ingrese el nombre del directorio:");
                if (dirName == null || dirName.trim().isEmpty()) {
                    return;
                }

                if (fileSystem.getRootDirectory().getNmae() == fsNode.toString()) {
                    // Verificar si ya existe un directorio con ese nombre
                    Directorio parentDir = fileSystem.getRootDirectory();
                    Lista<Directorio> subdirectorios = parentDir.getSubdirectorios();
                    for (int i = 0; i < subdirectorios.getTamaño(); i++) {
                        if (subdirectorios.obtener(i).getNmae().equals(dirName)) {
                            JOptionPane.showMessageDialog(FileSystemFrame.this,
                                    "Ya existe un directorio con ese nombre",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            AuditLogger.log("ERROR", "Ya existe un directorio con ese nombre", "Administrador");
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
                            AuditLogger.log("ERROR", "Ya existe un archivo con ese nombre", "Administrador");
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

                    // Guardar el Registro de Auditoria
                    AuditLogger.log("CREATE_DIRECTORY", "Directorio creado: " + dirName, "Administrador");

                } else {
                    // Verificar si ya existe un directorio con ese nombre
                    Directorio parentDir = (Directorio) fsNode.getNode();
                    Lista<Directorio> subdirectorios = parentDir.getSubdirectorios();
                    for (int i = 0; i < subdirectorios.getTamaño(); i++) {
                        if (subdirectorios.obtener(i).getNmae().equals(dirName)) {
                            JOptionPane.showMessageDialog(FileSystemFrame.this,
                                    "Ya existe un directorio con ese nombre",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            AuditLogger.log("ERROR", "Ya existe un directorio con ese nombre", "Administrador");
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
                            AuditLogger.log("ERROR", "Ya existe un archivo con ese nombre", "Administrador");
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

                    // Guardar el Registro de auditoria
                    AuditLogger.log("CREATE_DIRECTORY", "Directorio creado: " + dirName, "Administrador");

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
                    AuditLogger.log("ERROR", "Seleccione un archivo o directorio para renombrar", "Administrador");
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

                if (fileSystem.getRootDirectory().getNmae() == parentFSNode.toString()) {
                    Directorio parentDir = fileSystem.getRootDirectory();
                    // Verificar duplicados en archivos
                    Lista<Archivo> archivos = parentDir.getArchivos();
                    for (int i = 0; i < archivos.getTamaño(); i++) {
                        Archivo archivo = archivos.obtener(i);
                        if (!archivo.getName().equals(fsNode.getName()) && archivo.getName().equals(newName)) {
                            JOptionPane.showMessageDialog(FileSystemFrame.this,
                                    "Ya existe un archivo con ese nombre",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            AuditLogger.log("ERROR", "Ya existe un archivo con ese nombre", "Administrador");
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
                            AuditLogger.log("ERROR", "Ya existe un directorio con ese nombre", "Administrador");
                            return;
                        }
                    }

                    // Renombrar el nodo
                    if (fsNode.isDirectory()) {
                        Directorio dir = (Directorio) fsNode.getNode();
                        String dirName = dir.getNmae();
                        dir.setName(newName);
                        AuditLogger.log("RENAME_DIRECTORY", "Nombre de directorio" + dirName + " cambiado a: " + newName, "Administrador");
                    } else {
                        Archivo archivo = (Archivo) fsNode.getNode();
                        String fileName = archivo.getName();

                        // Guardar el color antes de renombrar
                        String oldPath = getFilePath(archivo, parentDir);
                        Color fileColor = fileColorMap.get(oldPath);
                        fileColorMap.remove(oldPath);

                        archivo.setName(newName);

                        // Asignar el color al nuevo path
                        fileColorMap.put(getFilePath(archivo, parentDir), fileColor);
                        AuditLogger.log("RENAME_FILE", "Nombre de archivo " + fileName + " cambiado a: " + newName, "Administrador");
                    }

                    fsNode.setName(newName);
                    treeModel.nodeChanged(selectedNode);
                    updateUI();

                    // Guardar el Registro de Auditoria
                } else {
                    Directorio parentDir = (Directorio) parentFSNode.getNode();
                    // Verificar duplicados en archivos
                    Lista<Archivo> archivos = parentDir.getArchivos();
                    for (int i = 0; i < archivos.getTamaño(); i++) {
                        Archivo archivo = archivos.obtener(i);
                        if (!archivo.getName().equals(fsNode.getName()) && archivo.getName().equals(newName)) {
                            JOptionPane.showMessageDialog(FileSystemFrame.this,
                                    "Ya existe un archivo con ese nombre",
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            AuditLogger.log("ERROR", "Ya existe un archivo con ese nombre", "Administrador");
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
                            AuditLogger.log("ERROR", "Ya existe un directorio con ese nombre", "Administrador");
                            return;
                        }
                    }

                    // Renombrar el nodo
                    if (fsNode.isDirectory()) {
                        Directorio dir = (Directorio) fsNode.getNode();
                        String dirName = dir.getNmae();
                        dir.setName(newName);
                        AuditLogger.log("RENAME_DIRECTORY", "Nombre de directorio" + dirName + " cambiado a: " + newName, "Administrador");
                    } else {
                        Archivo archivo = (Archivo) fsNode.getNode();
                        String fileName = archivo.getName();

                        // Guardar el color antes de renombrar
                        String oldPath = getFilePath(archivo, parentDir);
                        Color fileColor = fileColorMap.get(oldPath);
                        fileColorMap.remove(oldPath);

                        archivo.setName(newName);

                        // Asignar el color al nuevo path
                        fileColorMap.put(getFilePath(archivo, parentDir), fileColor);
                        AuditLogger.log("RENAME_FILE", "Nombre de archivo " + fileName + " cambiado a: " + newName, "Administrador");
                    }

                    fsNode.setName(newName);
                    treeModel.nodeChanged(selectedNode);
                    updateUI();
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
                    AuditLogger.log("ERROR", "Seleccione un archivo o directorio para eliminar", "Administrador");
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

                    if (fileSystem.getRootDirectory().getNmae() == parentFSNode.toString()) {
                        Directorio parentDir = fileSystem.getRootDirectory();
                        // Eliminar del sistema de archivos
                        if (fsNode.isDirectory()) {
                            // Si es un directorio, eliminamos recursivamente
                            fileSystem.deleteDirectory((Directorio) fsNode.getNode(), parentDir);
                            AuditLogger.log("DELETE_DIRECTORY", "Directorio eliminado: " + fsNode.getName(), "Administrador");
                        } else {
                            // Si es un archivo, lo eliminamos directamente
                            Archivo archivo = (Archivo) fsNode.getNode();
                            fileSystem.deleteFile(archivo, parentDir);
                            fileColorMap.remove(getFilePath(archivo, parentDir));
                            AuditLogger.log("DELETE_FILE", "Archivo eliminado: " + fsNode.getName(), "Administrador");
                        }

                        // Eliminar del árbol
                        treeModel.removeNodeFromParent(selectedNode);
                        updateUI();
                    } else {
                        Directorio parentDir = (Directorio) parentFSNode.getNode();

                        // Eliminar del sistema de archivos
                        if (fsNode.isDirectory()) {
                            // Si es un directorio, eliminamos recursivamente
                            fileSystem.deleteDirectory((Directorio) fsNode.getNode(), parentDir);
                            AuditLogger.log("DELETE_DIRECTORY", "Direcctorio eliminado: " + fsNode.getName(), "Administrador");
                        } else {
                            // Si es un archivo, lo eliminamos directamente
                            Archivo archivo = (Archivo) fsNode.getNode();
                            fileSystem.deleteFile(archivo, parentDir);
                            fileColorMap.remove(getFilePath(archivo, parentDir));
                            AuditLogger.log("DELETE_FILE", "Archivo eliminado: " + fsNode.getName(), "Administrador");
                        }

                        // Eliminar del árbol
                        treeModel.removeNodeFromParent(selectedNode);
                        updateUI();
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
                if (isAdminMode) {
                    AuditLogger.log("CHANGE_MODE", "Modo cambiado: administrador", "Usuario");
                } else {
                    AuditLogger.log("CHANGE_MODE", "Modo cambiado: usuario", "Usuario");
                }
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
        saveButton.setEnabled(isAdminMode);
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

    private void saveSystemStateToJson() {
        String userDir = System.getProperty("user.dir"); // Obtiene el directorio de trabajo
        String filePath = userDir + "\\src\\main\\java\\Json\\Guardar.json"; // Ruta completa del archivo

        try (FileWriter writer = new FileWriter(filePath)) { // Usa la ruta corregida
            // Crear un objeto JSON que represente el estado del sistema
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\n");
            jsonBuilder.append("  \"root\": {\n");
            jsonBuilder.append("    \"name\": \"").append(fileSystem.getRootDirectory().getNmae()).append("\",\n");
            jsonBuilder.append("    \"list\": [\n");

            // Recorrer los archivos y directorios en el sistema de archivos
            Directorio rootDir = fileSystem.getRootDirectory();
            Lista<Archivo> archivos = rootDir.getArchivos();
            Lista<Directorio> subdirectorios = rootDir.getSubdirectorios();

            // Guardar archivos en el directorio raíz
            for (int i = 0; i < archivos.getTamaño(); i++) {
                Archivo archivo = archivos.obtener(i);
                jsonBuilder.append("      {\n");
                jsonBuilder.append("        \"archivo\": {\n");
                jsonBuilder.append("          \"name\": \"").append(archivo.getName()).append("\",\n");
                jsonBuilder.append("          \"length\": ").append(archivo.getSize()).append("\n");
                jsonBuilder.append("        }\n");
                jsonBuilder.append("      }");

                if (i < archivos.getTamaño() - 1 || subdirectorios.getTamaño() > 0) {
                    jsonBuilder.append(",");
                }
                jsonBuilder.append("\n");
            }

            // Guardar subdirectorios en el directorio raíz
            for (int i = 0; i < subdirectorios.getTamaño(); i++) {
                Directorio subDir = subdirectorios.obtener(i);
                jsonBuilder.append("      {\n");
                jsonBuilder.append("        \"directorio\": {\n");
                jsonBuilder.append("          \"name\": \"").append(subDir.getNmae()).append("\",\n");
                jsonBuilder.append("          \"list\": [\n");

                // Guardar archivos en el subdirectorio
                Lista<Archivo> subArchivos = subDir.getArchivos();
                for (int j = 0; j < subArchivos.getTamaño(); j++) {
                    Archivo subArchivo = subArchivos.obtener(j);
                    jsonBuilder.append("            {\n");
                    jsonBuilder.append("              \"archivo\": {\n");
                    jsonBuilder.append("                \"name\": \"").append(subArchivo.getName()).append("\",\n");
                    jsonBuilder.append("                \"length\": ").append(subArchivo.getSize()).append("\n");
                    jsonBuilder.append("              }\n");
                    jsonBuilder.append("            }");

                    if (j < subArchivos.getTamaño() - 1 || subDir.getSubdirectorios().getTamaño() > 0) {
                        jsonBuilder.append(",");
                    }
                    jsonBuilder.append("\n");
                }

                // Guardar subdirectorios anidados (recursivamente)
                processNestedDirectories(jsonBuilder, subDir, 3);

                jsonBuilder.append("          ]\n");
                jsonBuilder.append("        }\n");
                jsonBuilder.append("      }");

                if (i < subdirectorios.getTamaño() - 1) {
                    jsonBuilder.append(",");
                }
                jsonBuilder.append("\n");
            }

            jsonBuilder.append("    ]\n");
            jsonBuilder.append("  }\n");
            jsonBuilder.append("}");

            // Escribir el JSON en el archivo
            writer.write(jsonBuilder.toString());
            writer.flush();

            JOptionPane.showMessageDialog(this, "Sistema guardado en Guardar.json", "Guardado exitoso", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error al guardar el sistema: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método auxiliar para procesar directorios anidados recursivamente
    private void processNestedDirectories(StringBuilder jsonBuilder, Directorio dir, int indentLevel) {
        Lista<Directorio> nestedDirs = dir.getSubdirectorios();
        String indent = "  ".repeat(indentLevel);

        for (int i = 0; i < nestedDirs.getTamaño(); i++) {
            Directorio nestedDir = nestedDirs.obtener(i);
            jsonBuilder.append(indent).append("{\n");
            jsonBuilder.append(indent).append("  \"directorio\": {\n");
            jsonBuilder.append(indent).append("    \"name\": \"").append(nestedDir.getNmae()).append("\",\n");
            jsonBuilder.append(indent).append("    \"list\": [\n");

            // Guardar archivos en el directorio anidado
            Lista<Archivo> nestedFiles = nestedDir.getArchivos();
            for (int j = 0; j < nestedFiles.getTamaño(); j++) {
                Archivo nestedFile = nestedFiles.obtener(j);
                jsonBuilder.append(indent).append("      {\n");
                jsonBuilder.append(indent).append("        \"archivo\": {\n");
                jsonBuilder.append(indent).append("          \"name\": \"").append(nestedFile.getName()).append("\",\n");
                jsonBuilder.append(indent).append("          \"length\": ").append(nestedFile.getSize()).append("\n");
                jsonBuilder.append(indent).append("        }\n");
                jsonBuilder.append(indent).append("      }");

                if (j < nestedFiles.getTamaño() - 1 || nestedDir.getSubdirectorios().getTamaño() > 0) {
                    jsonBuilder.append(",");
                }
                jsonBuilder.append("\n");
            }

            // Procesar directorios anidados más profundos (recursivamente)
            if (nestedDir.getSubdirectorios().getTamaño() > 0) {
                processNestedDirectories(jsonBuilder, nestedDir, indentLevel + 1);
            }

            jsonBuilder.append(indent).append("    ]\n");
            jsonBuilder.append(indent).append("  }\n");
            jsonBuilder.append(indent).append("}");

            if (i < nestedDirs.getTamaño() - 1) {
                jsonBuilder.append(",");
            }
            jsonBuilder.append("\n");
        }
    }

    public void loadSystemState() {
        String userDir = System.getProperty("user.dir");
        File jsonFile = new File(userDir, "src\\main\\java\\Json\\Leer.json");
        if (!jsonFile.exists()) {
            System.err.println("Error: No se encontró el archivo JSON en: " + jsonFile.getAbsolutePath());
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(jsonFile))) {
            StringBuilder json = new StringBuilder();
            String line;

            // Leer el archivo JSON línea por línea
            while ((line = reader.readLine()) != null) {
                json.append(line.trim());
            }

            System.out.println("JSON leído: " + json.toString()); // Depuración

            // Procesar el JSON manualmente
            processJson(json.toString());

            // Actualizar la UI después de cargar todo
            updateUI();

        } catch (IOException e) {
            System.err.println("Error al cargar el archivo JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processJson(String json) {
        // Eliminar espacios en blanco y saltos de línea
        json = json.replaceAll("\\s", "");

        System.out.println("JSON procesado: " + json); // Depuración

        // Extraer el contenido dentro de "root"
        int rootStart = json.indexOf("\"root\":{") + "\"root\":{".length();
        int rootEnd = json.lastIndexOf("}");
        String rootContent = json.substring(rootStart, rootEnd);

        System.out.println("Contenido de root: " + rootContent); // Depuración

        // Verificar si el JSON tiene un directorio dentro de "root"
        if (rootContent.contains("\"directorio\":")) {
            System.out.println("Procesando como directorio dentro de root..."); // Depuración
            processRootDirectory(rootContent);
        } else {
            System.out.println("No se encontró un directorio dentro de root."); // Depuración
        }
    }

 

    private void processFileEntry(String entry, Directorio parentDir, DefaultMutableTreeNode parentNode) {
        // Extraer el nombre y el tamaño del archivo
        String name = extractValue(entry, "name");
        String lengthStr = extractValue(entry, "length");

        System.out.println("Nombre del archivo: " + name); // Depuración
        System.out.println("Tamaño del archivo: " + lengthStr); // Depuración

        if (name != null && lengthStr != null) {
            try {
                int length = Integer.parseInt(lengthStr);

                // Crear el archivo en el sistema de archivos
                Archivo archivo = fileSystem.createFile(name, length, parentDir);
                if (archivo != null) {
                   // Asignar un color al archivo
                   Color fileColor = FILE_COLORS[random.nextInt(FILE_COLORS.length)];
                   fileColorMap.put(getFilePath(archivo, parentDir), fileColor);

                   // Añadir el archivo al árbol dentro del directorio
                   FSNode newFileNode = new FSNode(name, false, archivo);
                   DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newFileNode);
                   parentNode.add(newNode); // Agregar al nodo del directorio
                   treeModel.nodesWereInserted(parentNode, new int[]{parentNode.getChildCount() - 1});

                   System.out.println("Archivo creado: " + name); // Depuración
                }
            }catch (NumberFormatException e) {
               System.err.println("Error: El tamaño del archivo no es un número válido: " + lengthStr);
            }
        }
    }

    private String extractValue(String entry, String key) {
        // Construir la clave con formato JSON
        String keyTag = "\"" + key + "\":";
        int keyStart = entry.indexOf(keyTag);

        if (keyStart == -1) {
            return null; // Si no se encuentra la clave, retornar null
        }

        int valueStart = keyStart + keyTag.length(); // Posición inicial del valor
        int valueEnd;

        // Si el valor es una cadena (empieza con comillas)
        if (entry.charAt(valueStart) == '"') {
            valueStart++; // Saltar la comilla inicial
            valueEnd = entry.indexOf("\"", valueStart); // Buscar la comilla final
        } else {
            // Si es un número o booleano, buscar el final (coma o fin de cadena)
            valueEnd = entry.indexOf(",", valueStart);
            if (valueEnd == -1) {
                valueEnd = entry.length(); // Si no hay coma, tomar hasta el final
            }
        }

        return entry.substring(valueStart, valueEnd).trim(); // Extraer y retornar el valor
    }
    
    private void processRootDirectory(String json) {
    // Extraer el nombre del directorio
    int nameStart = json.indexOf("\"name\":\"") + "\"name\":\"".length();
    int nameEnd = json.indexOf("\"", nameStart);
    String directoryName = json.substring(nameStart, nameEnd);

    System.out.println("Nombre del directorio: " + directoryName); // Depuración

    // Crear el directorio en el sistema de archivos
    Directorio parentDir = fileSystem.getRootDirectory();

    // Verificar si ya existe un directorio con ese nombre
    Lista<Directorio> subdirectorios = parentDir.getSubdirectorios();
    for (int i = 0; i < subdirectorios.getTamaño(); i++) {
        if (subdirectorios.obtener(i).getNmae().equals(directoryName)) {
            System.out.println("Ya existe un directorio con ese nombre: " + directoryName);
            return;
        }
    }

    // Verificar archivos también
    Lista<Archivo> archivos = parentDir.getArchivos();
    for (int i = 0; i < archivos.getTamaño(); i++) {
        if (archivos.obtener(i).getName().equals(directoryName)) {
            System.out.println("Ya existe un archivo con ese nombre: " + directoryName);
            return;
        }
    }

    // Crear el directorio
    Directorio newDir = fileSystem.createDirectory(directoryName, parentDir);

    // Añadir el directorio al árbol
    FSNode newDirNode = new FSNode(directoryName, true, newDir);
    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newDirNode);
    rootNode.add(newNode);
    treeModel.nodesWereInserted(rootNode, new int[]{rootNode.getChildCount() - 1});

    // Extraer la lista de archivos dentro del directorio
    int listStart = json.indexOf("\"list\":[") + "\"list\":[".length();
    int listEnd = json.lastIndexOf("]");
    String listContent = json.substring(listStart, listEnd);

    System.out.println("Contenido de la lista: " + listContent); // Depuración

    // Procesar cada archivo dentro de la lista
    String[] entries = listContent.split("\\},\\{"); // Separar por entradas
    for (String entry : entries) {
        entry = entry.replace("{", "").replace("}", ""); // Limpiar la entrada
        System.out.println("Procesando entrada: " + entry); // Depuración

        if (entry.contains("\"archivo\":")) {
            // Es un archivo
            processFileEntry(entry, newDir, newNode); // Pasar el nodo del directorio
        }
    }

    // Actualizar la UI
    updateUI();

    // Guardar el estado del sistema
   }
}

