package ui;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import pelicula.Pelicula;

public class PeliculaTableModel extends AbstractTableModel {

    private final String[] cols = {"ID", "Título", "Director", "Año", "Duración", "Género"};
    private final List<Pelicula> data = new ArrayList<>();

    public void setData(List<Pelicula> lista) {
        data.clear();
        if (lista != null) {
            data.addAll(lista);
        }
        fireTableDataChanged();
    }

    public Pelicula getAt(int viewRow) {
        return data.get(viewRow);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return cols.length;
    }

    @Override
    public String getColumnName(int column) {
        return cols[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0, 3, 4 ->
                Integer.class;
            default ->
                String.class;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Pelicula p = data.get(rowIndex);
        return switch (columnIndex) {
            case 0 ->
                p.getId();
            case 1 ->
                p.getTitulo();
            case 2 ->
                p.getDirector();
            case 3 ->
                p.getAnio();
            case 4 ->
                p.getDuracion();
            case 5 ->
                p.getGenero();
            default ->
                null;
        };
    }
}
