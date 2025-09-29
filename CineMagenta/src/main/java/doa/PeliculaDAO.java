package doa;

import db.ConnectionManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import pelicula.Pelicula;

public class PeliculaDAO {


    public void crear(Pelicula p) throws SQLException {
        validar(p);
        String sql = "INSERT INTO Cartelera (titulo, director, anio, duracion, genero) VALUES (?,?,?,?,?)";
        try (Connection c = ConnectionManager.get(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getTitulo().trim());
            ps.setString(2, p.getDirector().trim());
            ps.setInt(3, p.getAnio());
            ps.setInt(4, p.getDuracion());
            ps.setString(5, p.getGenero().trim().toUpperCase());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getInt(1));
                }
            }
        }
    }


    public List<Pelicula> listarTodos() throws SQLException {
        String sql = "SELECT id, titulo, director, anio, duracion, genero "
                + "FROM Cartelera "
                + "ORDER BY id ASC";
        try (Connection c = ConnectionManager.get(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<Pelicula> out = new ArrayList<>();
            while (rs.next()) {
                out.add(map(rs));
            }
            return out;
        }
    }

 
    public List<Pelicula> buscarPorTexto(String texto) throws SQLException {
        String like = "%" + (texto == null ? "" : texto.trim()) + "%";
        String sql = "SELECT id, titulo, director, anio, duracion, genero "
                + "FROM Cartelera "
                + "WHERE titulo LIKE ? OR director LIKE ? OR genero LIKE ? "
                + "ORDER BY id ASC";
        try (Connection c = ConnectionManager.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                List<Pelicula> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(map(rs));
                }
                return out;
            }
        }
    }

 
    public Optional<Pelicula> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, titulo, director, anio, duracion, genero FROM Cartelera WHERE id=?";
        try (Connection c = ConnectionManager.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            }
        }
    }


    public void actualizar(Pelicula p) throws SQLException {
        if (p.getId() == null) {
            throw new IllegalArgumentException("ID requerido para actualizar");
        }
        validar(p);

        String sql = "UPDATE Cartelera SET titulo=?, director=?, anio=?, duracion=?, genero=? WHERE id=?";
        try (Connection c = ConnectionManager.getTx(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getTitulo().trim());
            ps.setString(2, p.getDirector().trim());
            ps.setInt(3, p.getAnio());
            ps.setInt(4, p.getDuracion());
            ps.setString(5, p.getGenero().trim().toUpperCase());
            ps.setInt(6, p.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                c.rollback();
                throw new SQLException("No existe película con ID " + p.getId());
            }
            c.commit();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM Cartelera WHERE id=?";
        try (Connection c = ConnectionManager.getTx(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                c.rollback();
                throw new SQLException("No existe película con ID " + id);
            }
            c.commit();
        }
    }


    private Pelicula map(ResultSet rs) throws SQLException {
        return new Pelicula(
                rs.getInt("id"),
                rs.getString("titulo"),
                rs.getString("director"),
                rs.getInt("anio"),
                rs.getInt("duracion"),
                rs.getString("genero")
        );
    }

    private void validar(Pelicula p) {
        if (p.getTitulo() == null || p.getTitulo().isBlank()) {
            throw new IllegalArgumentException("Título es obligatorio");
        }
        if (p.getDirector() == null || p.getDirector().isBlank()) {
            throw new IllegalArgumentException("Director es obligatorio");
        }
        int y = p.getAnio();
        if (y < 1888 || y > java.time.Year.now().getValue()) {
            throw new IllegalArgumentException("Año fuera de rango válido");
        }
        int d = p.getDuracion();
        if (d <= 0 || d > 600) {
            throw new IllegalArgumentException("Duración inválida");
        }
        if (p.getGenero() == null || p.getGenero().isBlank()) {
            throw new IllegalArgumentException("Género es obligatorio");
        }
    }
}
