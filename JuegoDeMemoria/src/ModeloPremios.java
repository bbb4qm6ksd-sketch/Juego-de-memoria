import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


// Administra los puntajes de los jugadores y calcula la tabla de premios.
public class ModeloPremios {

    // Cantidad máxima de jugadores que aparecen en la tabla.
    public static final int TOP = 5;
    // Archivo local que conserva los nombres de jugadores y sus intentos.
    private static final Path ARCHIVO = Paths.get("premios.txt");

    //Rgeistra mejor puntaje de un jugador
    public static class Registro {
        private final String jugador;
        private int intentos;
        Registro(String jugador, int intentos) { this.jugador = jugador; this.intentos = intentos; }
        public String getJugador() { return jugador; }
        public int getIntentos()   { return intentos; }
    }

    //Lista para mostrar la informacion
    public static class Fila {
        public final int posicion;
        public final String jugador;
        public final int intentos;
        public final double premio;
        Fila(int posicion, String jugador, int intentos, double premio) {
            this.posicion = posicion; this.jugador = jugador;
            this.intentos = intentos; this.premio = premio;
        }
    }


    private final List<Registro> registros = new ArrayList<>();

    // Al crear el modelo, carga los puntajes que ya están guardados.
    public ModeloPremios() {
        cargar();
    }

    // Registra una victoria o actualiza el récord existente si se logró con menos intentos.
    public void registrar(String jugador, int intentos) {
        // Ignora datos incompletos o puntajes que no pueden ser válidos.
        if (jugador == null || jugador.trim().isEmpty() || intentos <= 0) return;
        // Busca al jugador sin distinguir mayúsculas y minúsculas.
        for (Registro r : registros) {
            if (r.jugador.equalsIgnoreCase(jugador)) {
                if (intentos < r.intentos) {
                    r.intentos = intentos;
                    guardar();
                }
                return;
            }
        }
        registros.add(new Registro(jugador, intentos));
        guardar();
    }

    // Vuelve a leer desde el archivo para reflejar los datos guardados más recientes.
    public void recargar() {
        registros.clear();
        cargar();
    }


    // Devuelve el mejor puntaje del jugador, o -1 cuando no hay registro.
    public int getMejorIntento(String jugador) {
        if (jugador == null) return -1;
        for (Registro registro : registros) {
            if (registro.jugador.equalsIgnoreCase(jugador.trim())) return registro.intentos;
        }
        return -1;
    }


    // Calcula el monto del premio usando los intentos y el tipo de cambio.
    public static double calcularPremio(int intentos, double cambioDolar) {
        if (intentos <= 0) return 0;
        return 1.0 / intentos * 100 * cambioDolar;
    }


    private static final Path ARCHIVO_CAMBIO = Paths.get("tipo_cambio.txt");

    // Guarda el tipo de cambio actual para poder recuperarlo si falla otra consulta.
    public void guardarCambio(double valor) {
        try {
            Files.write(ARCHIVO_CAMBIO, String.valueOf(valor).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("No se pudo guardar el tipo de cambio: " + e.getMessage());
        }
    }

    // Lee el tipo de cambio guardado; devuelve -1 si no existe o no se puede interpretar.
    public double ultimoCambio() {
        try {
            String t = new String(Files.readAllBytes(ARCHIVO_CAMBIO), StandardCharsets.UTF_8);
            return Double.parseDouble(t.trim());
        } catch (IOException | NumberFormatException e) {
            return -1;
        }
    }

    // Ordena los registros por intentos y prepara las filas de los mejores jugadores.
    public List<Fila> getTabla(double cambioDolar) {
        // Ordena una copia para mantener intacto el orden de los registros originales.
        List<Registro> orden = new ArrayList<>(registros);
        orden.sort(Comparator.comparingInt(Registro::getIntentos));   

        List<Fila> tabla = new ArrayList<>();
        for (int i = 0; i < Math.min(TOP, orden.size()); i++) {
            Registro r = orden.get(i);
            tabla.add(new Fila(i + 1, r.jugador, r.intentos, calcularPremio(r.intentos, cambioDolar)));
        }
        return tabla;
    }

    // Guarda todos los registros en formato: nombre del jugador, tabulador e intentos.
    private void guardar() {
        List<String> lineas = new ArrayList<>();
        for (Registro r : registros) lineas.add(r.jugador + "\t" + r.intentos);
        try {
            Files.write(ARCHIVO, lineas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("No se pudo guardar premios: " + e.getMessage());
        }
    }

    // Lee el archivo de puntajes y reconstruye los registros en memoria.
    private void cargar() {
        if (!Files.exists(ARCHIVO)) return;
        try {
            for (String linea : Files.readAllLines(ARCHIVO, StandardCharsets.UTF_8)) {
                // Separa cada línea en nombre e intentos y solo procesa filas con ambos datos.
                String[] p = linea.split("\t");
                if (p.length == 2) registros.add(new Registro(p[0], Integer.parseInt(p[1].trim())));
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("No se pudo leer premios: " + e.getMessage());
        }
    }
}
