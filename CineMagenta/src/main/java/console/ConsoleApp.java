package console;

import doa.PeliculaDAO;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import pelicula.Pelicula;

public class ConsoleApp {

    private static final List<String> GENEROS = Arrays.asList(
            "ACCION", "DRAMA", "COMEDIA", "TERROR", "ANIMACION", "CIENCIA_FICCION", "AVENTURA", "ROMANCE"
    );

    public static void main(String[] args) {
        PeliculaDAO dao = new PeliculaDAO();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.println("\n==============================");
                System.out.println(" Magenta - Cartelera (CLI) S7 ");
                System.out.println("==============================");
                System.out.println("1) Agregar película");
                System.out.println("2) Ver lista (id asc)");
                System.out.println("3) Buscar (LIKE, id asc)");
                System.out.println("4) Modificar por ID");
                System.out.println("5) Eliminar por ID");
                System.out.println("0) Salir");
                System.out.print("Opción: ");
                String op = in.readLine();
                if (op == null) {
                    break;
                }

                switch (op.trim()) {
                    case "1" ->
                        agregar(in, dao);
                    case "2" ->
                        listar(dao);
                    case "3" ->
                        buscar(in, dao);
                    case "4" ->
                        modificar(in, dao);
                    case "5" ->
                        eliminar(in, dao);
                    case "0" -> {
                        System.out.println("Hasta luego.");
                        return;
                    }
                    default ->
                        System.out.println("Opción inválida.");
                }
            }
        } catch (IOException e) {
            System.err.println("I/O: " + e.getMessage());
        }
    }

    private static void agregar(BufferedReader in, PeliculaDAO dao) {
        try {
            Pelicula p = leerFormulario(in, null);
            try {
                dao.crear(p);
                System.out.println("Agregada con ID: " + p.getId());
            } catch (SQLException e) {
                System.out.println("Error BD al insertar: " + e.getMessage());
            } catch (IllegalArgumentException iae) {
                System.out.println("Validación: " + iae.getMessage());
            }
        } catch (IOException e) {
            System.out.println("I/O: " + e.getMessage());
        }
    }

    private static void listar(PeliculaDAO dao) {
        try {
            var lista = dao.listarTodos();
            imprimir(lista, "=== Cartelera (id asc) ===");
        } catch (SQLException e) {
            System.out.println("Error BD al listar: " + e.getMessage());
        }
    }

    private static void buscar(BufferedReader in, PeliculaDAO dao) {
        try {
            System.out.print("Texto (título/director/género): ");
            String txt = in.readLine();
            try {
                var lista = dao.buscarPorTexto(txt);
                imprimir(lista, "=== Resultados (id asc) ===");
            } catch (SQLException e) {
                System.out.println("Error BD en búsqueda: " + e.getMessage());
            }
        } catch (IOException e) {
            System.out.println("I/O: " + e.getMessage());
        }
    }

    private static void modificar(BufferedReader in, PeliculaDAO dao) {
        try {
            int id = promptInt(in, "ID a modificar", 1, Integer.MAX_VALUE);
            Optional<Pelicula> opt;
            try {
                opt = dao.buscarPorId(id);
            } catch (SQLException e) {
                System.out.println("Error BD al buscar: " + e.getMessage());
                return;
            }

            if (opt.isEmpty()) {
                System.out.println("No existe película con ID " + id);
                return;
            }

            Pelicula actual = opt.get();
            System.out.printf("Actual: %s (%d) | Dir: %s | %d min | %s%n",
                    actual.getTitulo(), actual.getAnio(), actual.getDirector(), actual.getDuracion(), actual.getGenero());

            Pelicula editada = leerFormulario(in, actual);
            editada.setId(id);
            try {
                dao.actualizar(editada);
                System.out.println("Actualizada correctamente.");
            } catch (SQLException e) {
                System.out.println("Error BD al actualizar: " + e.getMessage());
            } catch (IllegalArgumentException iae) {
                System.out.println("Validación: " + iae.getMessage());
            }
        } catch (IOException e) {
            System.out.println("I/O: " + e.getMessage());
        }
    }

    private static void eliminar(BufferedReader in, PeliculaDAO dao) {
        try {
            int id = promptInt(in, "ID a eliminar", 1, Integer.MAX_VALUE);
            Optional<Pelicula> opt;
            try {
                opt = dao.buscarPorId(id);
            } catch (SQLException e) {
                System.out.println("Error BD al buscar: " + e.getMessage());
                return;
            }

            if (opt.isEmpty()) {
                System.out.println("No existe película con ID " + id);
                return;
            }

            Pelicula p = opt.get();
            System.out.printf("¿Eliminar \"%s\" (%d) | Dir: %s | %d min | %s ? (S/N): ",
                    p.getTitulo(), p.getAnio(), p.getDirector(), p.getDuracion(), p.getGenero());
            String conf = in.readLine();
            if (conf != null && conf.trim().equalsIgnoreCase("S")) {
                try {
                    dao.eliminar(id);
                    System.out.println("Eliminada correctamente.");
                } catch (SQLException e) {
                    System.out.println("No se pudo eliminar: " + e.getMessage());
                }
            } else {
                System.out.println("Operación cancelada.");
            }
        } catch (IOException e) {
            System.out.println("I/O: " + e.getMessage());
        }
    }

    private static Pelicula leerFormulario(BufferedReader in, Pelicula base) throws IOException {
        String titulo = promptDefault(in, "Título", base == null ? null : base.getTitulo());
        String director = promptDefault(in, "Director", base == null ? null : base.getDirector());
        int anio = promptIntDefault(in, "Año (1888..2100)", 1888, 2100, base == null ? 2024 : base.getAnio());
        int duracion = promptIntDefault(in, "Duración (1..600)", 1, 600, base == null ? 120 : base.getDuracion());
        String genero = promptGeneroDefault(in, base == null ? null : base.getGenero());
        return new Pelicula(null, titulo, director, anio, duracion, genero);
    }

    private static void imprimir(java.util.List<Pelicula> lista, String titulo) {
        System.out.println("\n" + titulo);
        if (lista.isEmpty()) {
            System.out.println("(Sin registros)");
            return;
        }
        for (var p : lista) {
            System.out.printf("[%d] %s (%d) | Dir: %s | %d min | %s%n",
                    p.getId(), p.getTitulo(), p.getAnio(), p.getDirector(), p.getDuracion(), p.getGenero());
        }
    }

    private static String promptDefault(BufferedReader in, String label, String def) throws IOException {
        while (true) {
            System.out.print(label + (def != null ? " [" + def + "]" : "") + ": ");
            String s = in.readLine();
            if (s != null && !s.trim().isBlank()) {
                return s.trim();
            }
            if (def != null && !def.isBlank()) {
                return def;
            }
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
            } catch (Exception e) {
                System.out.println("Ingresa un número válido.");
            }
        }
    }

    private static int promptIntDefault(BufferedReader in, String label, int min, int max, int def) throws IOException {
        while (true) {
            System.out.print(label + " [" + def + "]: ");
            String s = in.readLine();
            if (s == null || s.isBlank()) {
                return def;
            }
            try {
                int v = Integer.parseInt(s.trim());
                if (v < min || v > max) {
                    System.out.printf("Debe estar entre %d y %d.%n", min, max);
                    continue;
                }
                return v;
            } catch (Exception e) {
                System.out.println("Ingresa un número válido.");
            }
        }
    }

    private static String promptGeneroDefault(BufferedReader in, String def) throws IOException {
        var generos = Arrays.asList("ACCION", "DRAMA", "COMEDIA", "TERROR", "ANIMACION", "CIENCIA_FICCION", "AVENTURA", "ROMANCE");
        System.out.println("Géneros: " + String.join(", ", generos));
        while (true) {
            System.out.print("Género" + (def != null ? " [" + def + "]" : "") + ": ");
            String g = in.readLine();
            if (g == null || g.isBlank()) {
                if (def != null) {
                    return def.toUpperCase();
                }
            } else {
                g = g.trim().toUpperCase();
                if (generos.contains(g)) {
                    return g;
                }
            }
            System.out.println("Género inválido. Intenta nuevamente.");
        }
    }
}
