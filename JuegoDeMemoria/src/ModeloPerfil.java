import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Modelo del perfil de un jugador. Guarda el HISTORIAL de partidas y de ahi calcula todas las
 * estadisticas (partidas, victorias, mejor ronda, estrellas ganadas, rango...).
 * No conoce nada de Swing.
 *
 * Los datos se guardan en  datos_perfil/historial_<usuario>.txt  (una partida por linea).
 */
public class ModeloPerfil {

    /** Identifica el modo de juego asociado a cada registro del historial. */
    public enum Modo { CLASICO, PATRONES }

    /** Una partida del historial. */
    public static class Entrada {
        public final long momento;       // milisegundos (fecha y hora de la partida)
        public final Modo modo;
        public final boolean victoria;   // solo tiene sentido en el modo clasico
        public final int valor;          // CLASICO: intentos | PATRONES: rondas completadas
        public final String detalle;     // CLASICO: nombre del rival
        public final int tamano;         // CLASICO: 2, 4 o 6 (dificultad) | PATRONES: 0
        public final int estrellas;      // estrellas que dio la partida

        /** Crea una entrada inmutable con los datos de una partida ya terminada. */
        Entrada(long momento, Modo modo, boolean victoria, int valor, String detalle, int tamano, int estrellas) {
            this.momento = momento;
            this.modo = modo;
            this.victoria = victoria;
            this.valor = valor;
            this.detalle = detalle;
            this.tamano = tamano;
            this.estrellas = estrellas;
        }
    }

    private static final String CARPETA = "datos_perfil";
    private static final int MAX_ENTRADAS = 500;     // si se pasa, se descartan las mas antiguas

    // Rangos segun el total de partidas jugadas
    private static final String[] RANGOS = {"CADETE", "EXPLORADOR", "NAVEGANTE", "COMANDANTE"};
    private static final int[] UMBRALES = {0, 5, 15, 35};

    private final String usuario;
    private final Path archivo;
    private final List<Entrada> historial = new ArrayList<>();   // la mas antigua primero

    public ModeloPerfil(String usuario) {
        this.usuario = usuario == null ? "" : usuario;
        String limpio = this.usuario.replaceAll("[^a-zA-Z0-9_-]", "_");
        this.archivo = Paths.get(CARPETA, "historial_" + limpio + ".txt");
        cargar();
    }

    // ---------- Registro de partidas (lo llaman los controladores de los modos de juego) ----------

    /** Guarda el resultado clásico, donde valor representa intentos y detalle el nombre del rival. */
    public void registrarClasico(boolean victoria, int intentos, String rival, int tamano, int estrellas) {
        agregar(new Entrada(System.currentTimeMillis(), Modo.CLASICO, victoria, intentos,
                limpiar(rival), tamano, Math.max(0, estrellas)));
    }

    /** Guarda una partida de patrones; valor representa cuántas rondas se completaron. */
    public void registrarPatrones(int rondasCompletadas, int estrellas) {
        agregar(new Entrada(System.currentTimeMillis(), Modo.PATRONES, false,
                Math.max(0, rondasCompletadas), "", 0, Math.max(0, estrellas)));
    }

    // ---------- Estadisticas ----------

    /** Devuelve el nombre de la cuenta asociada a este perfil. */
    public String getUsuario() { return usuario; }

    /** Cantidad de partidas clásicas registradas. */
    public int getPartidasClasico() { return contar(Modo.CLASICO, false); }
    /** Cantidad de victorias del usuario en el modo clásico. */
    public int getVictoriasClasico() { return contar(Modo.CLASICO, true); }
    /** Cantidad de partidas terminadas en el modo patrones. */
    public int getPartidasPatrones() { return contar(Modo.PATRONES, false); }
    /** Total de partidas de ambos modos que conserva el historial. */
    public int getPartidasTotales() { return historial.size(); }

    /** Calcula el porcentaje de victorias clásicas; devuelve cero si aún no hay partidas. */
    public int getPorcentajeVictorias() {
        int jugadas = getPartidasClasico();
        return jugadas == 0 ? 0 : Math.round(100f * getVictoriasClasico() / jugadas);
    }

    /** Mayor numero de rondas completadas en una partida de patrones (0 si no ha jugado). */
    public int getMejorRonda() {
        int mejor = 0;
        for (Entrada e : historial) {
            if (e.modo == Modo.PATRONES) mejor = Math.max(mejor, e.valor);
        }
        return mejor;
    }

    /** Estrellas ganadas jugando (suma de todas las partidas). */
    public int getEstrellasGanadas() {
        int total = 0;
        for (Entrada e : historial) total += e.estrellas;
        return total;
    }

    /** Las ultimas partidas, la mas reciente primero. */
    public List<Entrada> getHistorial(int maximo) {
        List<Entrada> copia = new ArrayList<>(historial);
        Collections.reverse(copia);
        return copia.size() > maximo ? new ArrayList<>(copia.subList(0, maximo)) : copia;
    }

    // ---------- Rango ----------

    public String getRango() {
        return RANGOS[indiceRango()];
    }

    /** Nombre del siguiente rango, o null si ya tiene el maximo. */
    public String getSiguienteRango() {
        int i = indiceRango();
        return i + 1 < RANGOS.length ? RANGOS[i + 1] : null;
    }

    /** Partidas que faltan para el siguiente rango, o -1 si ya tiene el maximo. */
    public int getPartidasParaSiguienteRango() {
        int i = indiceRango();
        return i + 1 < RANGOS.length ? UMBRALES[i + 1] - getPartidasTotales() : -1;
    }

    private int indiceRango() {
        int partidas = getPartidasTotales();
        int i = 0;
        while (i + 1 < UMBRALES.length && partidas >= UMBRALES[i + 1]) i++;
        return i;
    }

    private int contar(Modo modo, boolean soloVictorias) {
        int n = 0;
        for (Entrada e : historial) {
            if (e.modo == modo && (!soloVictorias || e.victoria)) n++;
        }
        return n;
    }

    // ---------- Persistencia (una linea por partida, campos separados por TAB) ----------

    private void agregar(Entrada e) {
        historial.add(e);
        try {
            Files.createDirectories(archivo.getParent());
            if (historial.size() > MAX_ENTRADAS) {
                historial.remove(0);
                List<String> lineas = new ArrayList<>();
                for (Entrada x : historial) lineas.add(aLinea(x));
                Files.write(archivo, lineas, StandardCharsets.UTF_8);       // reescribe sin la mas antigua
            } else {
                Files.write(archivo, (aLinea(e) + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
        } catch (IOException ex) {
            System.err.println("No se pudo guardar el historial de " + usuario + ": " + ex.getMessage());
        }
    }

    private void cargar() {
        if (!Files.exists(archivo)) return;
        try {
            for (String linea : Files.readAllLines(archivo, StandardCharsets.UTF_8)) {
                String[] p = linea.split("\t", -1);
                if (p.length != 7) continue;
                try {
                    historial.add(new Entrada(Long.parseLong(p[0]), Modo.valueOf(p[1]), "1".equals(p[2]),
                            Integer.parseInt(p[3]), p[4], Integer.parseInt(p[5]), Integer.parseInt(p[6])));
                } catch (IllegalArgumentException ignorada) {
                    // linea danada: se salta
                }
            }
        } catch (IOException ex) {
            System.err.println("No se pudo leer el historial de " + usuario + ": " + ex.getMessage());
        }
    }

    private static String aLinea(Entrada e) {
        return e.momento + "\t" + e.modo + "\t" + (e.victoria ? "1" : "0") + "\t" + e.valor + "\t"
                + e.detalle + "\t" + e.tamano + "\t" + e.estrellas;
    }

    private static String limpiar(String texto) {
        return texto == null ? "" : texto.replace('\t', ' ').replace('\n', ' ').replace('\r', ' ').trim();
    }
}
