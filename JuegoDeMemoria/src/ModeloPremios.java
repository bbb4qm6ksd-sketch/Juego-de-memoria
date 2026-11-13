import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//Gana quien logra hacer lo 18 pares en menos intentos
public class ModeloPremios {

    public static final int TOP = 5;
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

    public ModeloPremios() {
        cargar();
    }

    //Se registran victorias, si ya existe el jugador se actualiza cual ha sido su mejor puntaje
    public void registrar(String jugador, int intentos) {
        if (jugador == null || jugador.trim().isEmpty() || intentos <= 0) return;
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

    public void recargar() {
        registros.clear();
        cargar();
    }

    /** Devuelve el mejor registro persistido del jugador o -1 si todavía no existe. */
    public int getMejorIntento(String jugador) {
        if (jugador == null) return -1;
        for (Registro registro : registros) {
            if (registro.jugador.equalsIgnoreCase(jugador.trim())) return registro.intentos;
        }
        return -1;
    }


    // se hace el calculo del premio segun la formula
    public static double calcularPremio(int intentos, double cambioDolar) {
        if (intentos <= 0) return 0;
        return 1.0 / intentos * 100 * cambioDolar;
    }

    private static final Path ARCHIVO_CAMBIO = Paths.get("tipo_cambio.txt");

    //Guarda el tipo de cambio
    public void guardarCambio(double valor) {
        try {
            Files.write(ARCHIVO_CAMBIO, String.valueOf(valor).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("No se pudo guardar el tipo de cambio: " + e.getMessage());
        }
    }

    //Cual fue el utlimo tipo de cambio?
    public double ultimoCambio() {
        try {
            String t = new String(Files.readAllBytes(ARCHIVO_CAMBIO), StandardCharsets.UTF_8);
            return Double.parseDouble(t.trim());
        } catch (IOException | NumberFormatException e) {
            return -1;
        }
    }

    //Calcula el top 5 mejores
    public List<Fila> getTabla(double cambioDolar) {
        List<Registro> orden = new ArrayList<>(registros);
        orden.sort(Comparator.comparingInt(Registro::getIntentos));   

        List<Fila> tabla = new ArrayList<>();
        for (int i = 0; i < Math.min(TOP, orden.size()); i++) {
            Registro r = orden.get(i);
            tabla.add(new Fila(i + 1, r.jugador, r.intentos, calcularPremio(r.intentos, cambioDolar)));
        }
        return tabla;
    }

    // ---------- Persistencia (jugador<TAB>intentos) ----------

    private void guardar() {
        List<String> lineas = new ArrayList<>();
        for (Registro r : registros) lineas.add(r.jugador + "\t" + r.intentos);
        try {
            Files.write(ARCHIVO, lineas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("No se pudo guardar premios: " + e.getMessage());
        }
    }

    private void cargar() {
        if (!Files.exists(ARCHIVO)) return;
        try {
            for (String linea : Files.readAllLines(ARCHIVO, StandardCharsets.UTF_8)) {
                String[] p = linea.split("\t");
                if (p.length == 2) registros.add(new Registro(p[0], Integer.parseInt(p[1].trim())));
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("No se pudo leer premios: " + e.getMessage());
        }
    }
}
