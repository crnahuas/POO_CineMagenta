package ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.DocumentFilter.FilterBypass;
import javax.swing.text.InternationalFormatter;
import pelicula.Pelicula;

public class PeliculaFormDialog extends JDialog {

    private final JTextField txtTitulo = new JTextField(25);
    private final JTextField txtDirector = new JTextField(25);
    
    private final JFormattedTextField txtAnio = createLenientIntFieldNoGrouping(yearNow(), 4);
    private final JFormattedTextField txtDuracion = createLenientIntFieldNoGrouping(60, 3);

    private final JComboBox<String> cboGenero = new JComboBox<>(new String[]{
        "ACCION", "DRAMA", "COMEDIA", "TERROR", "ANIMACION", "CIENCIA_FICCION", "AVENTURA", "ROMANCE"
    });

    private Pelicula result = null;

    public PeliculaFormDialog() {
        setModal(true);
        setTitle("Película");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        txtTitulo.setToolTipText("Obligatorio");
        txtDirector.setToolTipText("Obligatorio");
        txtAnio.setToolTipText("Año entre 1888 y 2100");
        txtDuracion.setToolTipText("Duración en minutos (1 a 600)");

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;

        int r = 0;
        addRow(form, gc, r++, "Título*", txtTitulo);
        addRow(form, gc, r++, "Director*", txtDirector);
        addRow(form, gc, r++, "Año* (Ej: 2020)", txtAnio);
        addRow(form, gc, r++, "Duración* (Ej: 120)", txtDuracion);
        addRow(form, gc, r++, "Género*", cboGenero);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = new JButton("Guardar");
        JButton btnLimpiar = new JButton("Limpiar");
        JButton btnCancelar = new JButton("Cancelar");
        botones.add(btnLimpiar);
        botones.add(btnCancelar);
        botones.add(btnGuardar);

        btnGuardar.addActionListener(e -> onGuardar());
        btnLimpiar.addActionListener(e -> limpiarCampos());
        btnCancelar.addActionListener(e -> {
            result = null;
            dispose();
        });

        getRootPane().setDefaultButton(btnGuardar);
        getRootPane().registerKeyboardAction(
                e -> {
                    result = null;
                    dispose();
                },
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getContentPane().setLayout(new BorderLayout(8, 8));
        getContentPane().add(form, BorderLayout.CENTER);
        getContentPane().add(botones, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }

    public Pelicula mostrarParaCrear(Component parent) {
        setTitle("Agregar Película");
        limpiarCampos();
        setLocationRelativeTo(parent);
        setVisible(true);
        return result;
    }

    public Pelicula mostrarParaEditar(Component parent, Pelicula base) {
        setTitle("Editar Película");
        cargarDesde(base);
        setLocationRelativeTo(parent);
        setVisible(true);
        return result;
    }

    private void onGuardar() {
        try {
            String titulo = req(txtTitulo.getText(), "Título");
            String director = req(txtDirector.getText(), "Director");

            txtAnio.commitEdit();
            txtDuracion.commitEdit();

            String sAnio = txtAnio.getText().trim();
            String sDur = txtDuracion.getText().trim();
            if (sAnio.isEmpty()) {
                throw new IllegalArgumentException("Año es obligatorio");
            }
            if (sDur.isEmpty()) {
                throw new IllegalArgumentException("Duración es obligatoria");
            }

            int anio = Integer.parseInt(sAnio);
            int dur = Integer.parseInt(sDur);

            if (anio < 1888 || anio > 2100) {
                throw new IllegalArgumentException("Año debe estar entre 1888 y 2100");
            }
            if (dur < 1 || dur > 600) {
                throw new IllegalArgumentException("Duración debe estar entre 1 y 600");
            }

            String genero = (String) cboGenero.getSelectedItem();
            if (genero == null || genero.isBlank()) {
                throw new IllegalArgumentException("Género es obligatorio");
            }
            genero = genero.toUpperCase();

            result = new Pelicula(null, titulo.trim(), director.trim(), anio, dur, genero);
            dispose();
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Revisa Año y Duración: solo números.", "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error procesando los valores numéricos.", "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void limpiarCampos() {
        txtTitulo.setText("");
        txtDirector.setText("");
        txtAnio.setText(String.valueOf(yearNow()));
        txtDuracion.setText("60");
        cboGenero.setSelectedIndex(0);
    }

    private void cargarDesde(Pelicula p) {
        txtTitulo.setText(p.getTitulo());
        txtDirector.setText(p.getDirector());
        txtAnio.setText(String.valueOf(p.getAnio()));
        txtDuracion.setText(String.valueOf(p.getDuracion()));
        cboGenero.setSelectedItem(p.getGenero());
    }

    private static void addRow(JPanel panel, GridBagConstraints gc, int row, String label, JComponent field) {
        gc.gridx = 0;
        gc.gridy = row;
        gc.weightx = 0;
        panel.add(new JLabel(label), gc);
        gc.gridx = 1;
        gc.gridy = row;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gc);
        gc.fill = GridBagConstraints.NONE;
    }

    private static JFormattedTextField createLenientIntFieldNoGrouping(int initial, int columns) {
        DecimalFormatSymbols sym = DecimalFormatSymbols.getInstance(Locale.ROOT);
        DecimalFormat df = new DecimalFormat("#", sym);
        df.setGroupingUsed(false);
        InternationalFormatter fmt = new InternationalFormatter(df);
        fmt.setAllowsInvalid(true);
        fmt.setCommitsOnValidEdit(true); 
        fmt.setOverwriteMode(false);

        JFormattedTextField f = new JFormattedTextField(fmt);
        f.setColumns(columns);
        f.setText(String.valueOf(initial));

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

        f.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                SwingUtilities.invokeLater(f::selectAll);
            }
        });

        return f;
    }

    private static String req(String s, String campo) {
        if (s == null || s.trim().isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio");
        }
        return s;
    }

    private static int yearNow() {
        return java.time.Year.now().getValue();
    }
}
