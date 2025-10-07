package ui;

import doa.PeliculaDAO;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import pelicula.Pelicula;

public class PeliculaListPanel extends JPanel {

    private final PeliculaDAO dao = new PeliculaDAO();
    private final PeliculaTableModel model = new PeliculaTableModel();
    private final JTable table = new JTable(model);

    // Buscador LIKE (título/director/género)
    private final JTextField txtBuscar = new JTextField(18);
    private final JButton btnBuscar = new JButton("Buscar");

    // Filtros S8
    private final JComboBox<String> cboGenero = new JComboBox<>(new String[]{
        "TODOS", "ACCION", "DRAMA", "COMEDIA", "TERROR", "ANIMACION", "CIENCIA_FICCION", "AVENTURA", "ROMANCE"
    });
    private final JFormattedTextField txtDesde = createLenientIntField(1888, 4);
    private final JFormattedTextField txtHasta = createLenientIntField(2100, 4);
    private final JButton btnFiltrar = new JButton("Filtrar");
    private final JButton btnLimpiar = new JButton("Limpiar");

    public PeliculaListPanel() {
        setLayout(new BorderLayout(8, 8));

        // Fila superior (búsqueda texto)
        JPanel top1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top1.add(new JLabel("Buscar (título/director/género): "));
        top1.add(txtBuscar);
        top1.add(btnBuscar);

        // Fila filtros (género + rango años)
        JPanel top2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top2.add(new JLabel("Género:"));
        top2.add(cboGenero);
        top2.add(new JLabel("Año desde:"));
        top2.add(txtDesde);
        top2.add(new JLabel("Hasta:"));
        top2.add(txtHasta);
        top2.add(btnFiltrar);
        top2.add(btnLimpiar);

        JPanel north = new JPanel(new GridLayout(2, 1));
        north.add(top1);
        north.add(top2);
        add(north, BorderLayout.NORTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(false); // orden lo define SQL (id ASC)
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Tooltips
        txtDesde.setToolTipText("Predeterminado 1888 si vacío");
        txtHasta.setToolTipText("Predeterminado 2100 si vacío");

        // Listeners
        btnBuscar.addActionListener(e -> buscarTexto());
        btnFiltrar.addActionListener(e -> aplicarFiltros());
        btnLimpiar.addActionListener(e -> limpiarFiltros());

        cargarTabla(); // carga inicial
    }

    /**
     * Carga completa (sin filtros)
     */
    public void cargarTabla() {
        try {
            List<Pelicula> lista = dao.listarTodos(); // ORDER BY id ASC
            model.setData(lista);
        } catch (SQLException ex) {
            mostrarError("Error BD al listar: " + ex.getMessage());
        }
    }

    /**
     * Buscar por texto (LIKE) manteniendo filtros de año y género actuales
     */
    private void buscarTexto() {
        aplicarFiltros(); // reutilizamos la misma lógica combinada
    }

    /**
     * Filtra por género + rango + texto
     */
    private void aplicarFiltros() {
        String genero = (String) cboGenero.getSelectedItem();
        Integer desde = parseEntero(txtDesde.getText().trim());
        Integer hasta = parseEntero(txtHasta.getText().trim());
        String texto = txtBuscar.getText();

        // Defaults y validaciones
        int d = (desde == null ? 1888 : desde);
        int h = (hasta == null ? 2100 : hasta);
        if (d < 1888) {
            d = 1888;
        }
        if (h > 2100) {
            h = 2100;
        }
        if (d > h) {
            mostrarError("Rango de años inválido: 'Desde' no puede ser mayor que 'Hasta'.");
            return;
        }

        try {
            List<Pelicula> lista = dao.filtrar(genero, d, h, texto);
            model.setData(lista);
        } catch (SQLException ex) {
            mostrarError("Error BD al filtrar: " + ex.getMessage());
        }
    }

    /**
     * Limpia filtros y recarga todo
     */
    private void limpiarFiltros() {
        txtBuscar.setText("");
        cboGenero.setSelectedIndex(0); // TODOS
        txtDesde.setText(""); // mostrar vacío → toma 1888 en aplicarFiltros()
        txtHasta.setText(""); // vacío → 2100
        cargarTabla();
    }

    // ===== Helpers =====
    /**
     * Campo entero "suave": permite escribir libremente; validamos al usar. Sin
     * separadores.
     */
    private static JFormattedTextField createLenientIntField(int initial, int columns) {
        // Formato entero sin agrupación (independiente del locale), pero 'lenient' al escribir
        java.text.DecimalFormatSymbols sym = java.text.DecimalFormatSymbols.getInstance(java.util.Locale.ROOT);
        java.text.DecimalFormat df = new java.text.DecimalFormat("#", sym);
        df.setGroupingUsed(false);
        javax.swing.text.InternationalFormatter fmt = new javax.swing.text.InternationalFormatter(df);
        fmt.setAllowsInvalid(true);
        fmt.setCommitsOnValidEdit(true);
        fmt.setOverwriteMode(false);

        JFormattedTextField f = new JFormattedTextField(fmt);
        f.setColumns(columns);
        f.setText(String.valueOf(initial));

        // Solo dígitos (evita letras/espacios)
        ((AbstractDocument) f.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offs, String str, AttributeSet a) throws BadLocationException {
                if (str != null && str.chars().allMatch(Character::isDigit)) {
                    super.insertString(fb, offs, str, a);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offs, int len, String str, AttributeSet a) throws BadLocationException {
                if (str == null || str.chars().allMatch(Character::isDigit)) {
                    super.replace(fb, offs, len, str, a);
                }
            }
        });

        // Select-all al enfocar
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                SwingUtilities.invokeLater(f::selectAll);
            }
        });

        return f;
    }

    private static Integer parseEntero(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void mostrarError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // Acciones existentes de CRUD desde toolbar del MainFrame (se mantienen):
    public void accionAgregar() {
        PeliculaFormDialog dlg = new PeliculaFormDialog();
        Pelicula p = dlg.mostrarParaCrear(this);
        if (p == null) {
            return;
        }
        try {
            dao.crear(p);
            aplicarFiltros(); // refresca con filtros actuales
            JOptionPane.showMessageDialog(this, "Película agregada con ID " + p.getId());
        } catch (SQLException ex) {
            if (esDuplicado(ex)) {
                mostrarError("Ya existe una película con mismo Título + Director + Año.");
            } else {
                mostrarError("Error BD al insertar: " + ex.getMessage());
            }
        } catch (IllegalArgumentException iae) {
            mostrarError("Validación: " + iae.getMessage());
        }
    }

    public void accionEditarSeleccion() {
        int row = table.getSelectedRow();
        if (row < 0) {
            mostrarInfo("Selecciona una fila primero.");
            return;
        }
        Pelicula actual = model.getAt(row);

        PeliculaFormDialog dlg = new PeliculaFormDialog();
        Pelicula editada = dlg.mostrarParaEditar(this, actual);
        if (editada == null) {
            return;
        }

        try {
            editada.setId(actual.getId());
            dao.actualizar(editada); // transacción en DAO
            aplicarFiltros();
            mostrarInfo("Película actualizada.");
        } catch (SQLException ex) {
            if (esDuplicado(ex)) {
                mostrarError("Conflicto: ya existe otra película con ese Título + Director + Año.");
            } else {
                mostrarError("Error BD al actualizar: " + ex.getMessage());
            }
        } catch (IllegalArgumentException iae) {
            mostrarError("Validación: " + iae.getMessage());
        }
    }

    public void accionEliminarSeleccion() {
        int row = table.getSelectedRow();
        if (row < 0) {
            mostrarInfo("Selecciona una fila primero.");
            return;
        }
        Pelicula p = model.getAt(row);
        int opt = JOptionPane.showConfirmDialog(
                this,
                "¿Eliminar \"" + p.getTitulo() + "\" (" + p.getAnio() + ")?",
                "Confirmar eliminación",
                JOptionPane.YES_NO_OPTION
        );
        if (opt != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            dao.eliminar(p.getId()); // transacción en DAO
            aplicarFiltros();
            mostrarInfo("Película eliminada.");
        } catch (SQLException ex) {
            mostrarError("No se pudo eliminar: " + ex.getMessage());
        }
    }

    private boolean esDuplicado(SQLException ex) {
        String m = ex.getMessage().toLowerCase();
        return m.contains("duplicate") || m.contains("uk_cartelera_titulo_director_anio");
    }

    private void mostrarInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Información", JOptionPane.INFORMATION_MESSAGE);
    }
}
