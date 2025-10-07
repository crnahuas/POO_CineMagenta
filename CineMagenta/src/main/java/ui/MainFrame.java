package ui;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;


public class MainFrame extends JFrame {

    public MainFrame() {
        super("Cine Magenta - Cartelera");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        JButton btnAgregar = new JButton("Agregar");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnRefrescar = new JButton("Refrescar");
        tb.add(btnAgregar);
        tb.add(btnEditar);
        tb.add(btnEliminar);
        tb.addSeparator();
        tb.add(btnRefrescar);
        add(tb, BorderLayout.NORTH);

        PeliculaListPanel listPanel = new PeliculaListPanel();
        add(listPanel, BorderLayout.CENTER);

        btnAgregar.addActionListener(e -> listPanel.accionAgregar());
        btnEditar.addActionListener(e -> listPanel.accionEditarSeleccion());
        btnEliminar.addActionListener(e -> listPanel.accionEliminarSeleccion());
        btnRefrescar.addActionListener(e -> listPanel.cargarTabla());

        JMenuBar mb = new JMenuBar();
        JMenu archivo = new JMenu("Archivo");
        JMenuItem salir = new JMenuItem("Salir");
        salir.addActionListener(e -> dispose());
        archivo.add(salir);
        mb.add(archivo);
        setJMenuBar(mb);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
