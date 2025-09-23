/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package console;

import db.ConnectionManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

public class ConsoleApp {

    private static final List<String> GENEROS = Arrays.asList(
            "ACCION","DRAMA","COMEDIA","TERROR","ANIMACION","CIENCIA_FICCION","AVENTURA","ROMANCE"
    );

    public static void main(String[] args) throws SQLException {

        
        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.println("\n===============================");
                System.out.println("   Cine Magenta - Cartelera   ");
                System.out.println("===============================");
                System.out.println("1) Agregar película");
                System.out.println("2) Ver lista de películas");
                System.out.println("0) Salir");
                System.out.print("Elige una opción: ");

                String op = in.readLine();
                if (op == null) break;

                switch (op.trim()) {
                    case "1":
                        agregarPelicula(in);
                        break;
                    case "2":
                        listarPeliculas();
                        break;
                    case "0":
                        System.out.println("Hasta luego.");
                        return;
                    default:
                        System.out.println("Opción inválida.");
                }
            }
        } catch (IOException e) {
            System.err.println("Error de entrada: " + e.getMessage());
        }
    }

    private static void agregarPelicula(BufferedReader in) throws SQLException {
        try {
            String titulo = prompt(in, "Título: ");
            String director = prompt(in, "Director: ");

            int anio = promptInt(in, "Año: ", 1888, 2100);
            int duracion = promptInt(in, "Duración en minutos: ", 1, 600);

            String genero = promptGenero(in);

            // Insert
            String sql = "INSERT INTO Cartelera (titulo, director, anio, duracion, genero) VALUES (?,?,?,?,?)";
            try (Connection c = ConnectionManager.get();
                 PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, titulo.trim());
                ps.setString(2, director.trim());
                ps.setInt(3, anio);
                ps.setInt(4, duracion);
                ps.setString(5, genero);

                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int newId = rs.getInt(1);
                        System.out.println("Película agregada con ID: " + newId);
                    } else {
                        System.out.println("Película agregada.");
                    }
                }
            } catch (SQLException e) {
                // Clave única (título+director+año) u otros errores
                System.err.println("Error BD al insertar: " + e.getMessage());
            }

        } catch (IOException e) {
            System.err.println("Error de entrada: " + e.getMessage());
        }
    }

    private static void listarPeliculas() {
        String sql = "SELECT id, titulo, director, anio, duracion, genero FROM Cartelera ORDER BY titulo";
        try (Connection c = ConnectionManager.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n=== Cartelera ===");
            int count = 0;
            while (rs.next()) {
                count++;
                int id = rs.getInt("id");
                String titulo = rs.getString("titulo");
                String director = rs.getString("director");
                int anio = rs.getInt("anio");
                int duracion = rs.getInt("duracion");
                String genero = rs.getString("genero");
                System.out.printf("[%d] %s (%d) | Dir: %s | %d min | %s%n",
                        id, titulo, anio, director, duracion, genero);
            }
            if (count == 0) System.out.println("(Sin registros)");
        } catch (SQLException e) {
            System.err.println("Error BD al listar: " + e.getMessage());
        }
    }

    private static String prompt(BufferedReader in, String label) throws IOException {
        while (true) {
            System.out.print(label + ": ");
            String s = in.readLine();
            if (s != null && !s.trim().isBlank()) return s;
            System.out.println("El campo " + label + " es obligatorio.");
        }
    }

    private static int promptInt(BufferedReader in, String label, int min, int max) throws IOException {
        while (true) {
            System.out.print(label + ": ");
            String s = in.readLine();
            try {
                int v = Integer.parseInt(s.trim());
                if (v < min || v > max) {
                    System.out.printf("Debe estar entre %d y %d.%n", min, max);
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.println("Ingresa un número válido.");
            }
        }
    }

    private static String promptGenero(BufferedReader in) throws IOException {
        System.out.println("Géneros disponibles: " + String.join(", ", GENEROS));
        while (true) {
            System.out.print("Género (exacto, ej. ACCION): ");
            String g = in.readLine();
            if (g != null) {
                g = g.trim().toUpperCase();
                if (GENEROS.contains(g)) return g;
            }
            System.out.println("Género inválido. Intenta nuevamente.");
        }
    }
}
