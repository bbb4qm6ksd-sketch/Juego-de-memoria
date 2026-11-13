import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ModeloClasico {

    public static final int JUGADORES = 2;
    public static final int FILAS = 6;
    public static final int COLUMNAS = 6;
    public static final int PAREJAS = FILAS * COLUMNAS / 2;      // 18
    public static final double TIEMPO_TURNO = 10.0;
    public static final double BONO_ACIERTO = 7.0;
    public enum Resultado { IGNORADA, PRIMERA, ACIERTO, FALLO }

    // Las 18 constelaciones (Carta.java sabe dibujar cada una por su nombre)
    public static final String[] CONSTELACIONES = {
            "Cisne", "Escorpio", "Orión", "Géminis", "Cruz del Sur", "Casiopea",
            "Osa Mayor", "Lira", "Leo", "Tauro", "Andrómeda", "Pegaso",
            "Acuario", "Aries", "Cáncer", "Virgo", "Sagitario", "Capricornio"
    };

    private final String[][][] nombres = new String[JUGADORES][FILAS][COLUMNAS];
    private final boolean[][][] encontradas = new boolean[JUGADORES][FILAS][COLUMNAS];
    private final int[] intentos = new int[JUGADORES];
    private final int[] parejas = new int[JUGADORES];
    private int tamanoMatriz = 6;
    private int parejasMaximas = PAREJAS;
    private int jugadorActivo = 0;
    private int ganador = -1;
    private int primeraFila = -1;
    private int primeraColumna = -1;
    private double tiempo = TIEMPO_TURNO;
    private double tiempoMaximo = TIEMPO_TURNO;

    public ModeloClasico() {
        nuevaPartida();
    }

    public void nuevaPartida() {
        nuevaPartida(tamanoMatriz);
    }

    public void nuevaPartida(int tamano) {
        tamanoMatriz = (tamano == 2 || tamano == 4 || tamano == 6) ? tamano : 6;
        parejasMaximas = tamanoMatriz * tamanoMatriz / 2;
        ganador = -1;
        for (int j = 0; j < JUGADORES; j++) {
            List<String> baraja = new ArrayList<>();
            for (int i = 0; i < parejasMaximas; i++) {
                String nombre = CONSTELACIONES[i];
                baraja.add(nombre);
                baraja.add(nombre);
            }
            Collections.shuffle(baraja);                 // cada jugador se baraja por separado
            for (int f = 0; f < FILAS; f++) {
                for (int c = 0; c < COLUMNAS; c++) {
                    int indice = f * tamanoMatriz + c;
                    nombres[j][f][c] = f < tamanoMatriz && c < tamanoMatriz && indice < baraja.size()
                            ? baraja.get(indice) : null;
                    encontradas[j][f][c] = false;
                }
            }
            intentos[j] = 0;
            parejas[j] = 0;
        }
        jugadorActivo = 0;
        reiniciarTurno();
    }

    /**
     * Destapa una carta. Devuelve:
     *  IGNORADA -> no es su turno, la carta ya esta encontrada, es la misma de antes o el juego termino
     *  PRIMERA  -> primera carta de la jugada
     *  ACIERTO  -> segunda carta, coincide (+7 s, el mismo jugador sigue)
     *  FALLO    -> segunda carta, no coincide (el controlador cede el turno)
     */
    public Resultado voltear(int jugador, int fila, int columna) {
        if (fila < 0 || columna < 0 || fila >= tamanoMatriz || columna >= tamanoMatriz
                || nombres[jugador][fila][columna] == null || jugador != jugadorActivo
                || encontradas[jugador][fila][columna] || partidaTerminada()) {
            return Resultado.IGNORADA;
        }
        if (primeraFila < 0) {
            primeraFila = fila;
            primeraColumna = columna;
            return Resultado.PRIMERA;
        }
        if (primeraFila == fila && primeraColumna == columna) {
            return Resultado.IGNORADA;
        }

        intentos[jugador]++;
        boolean acierto = nombres[jugador][fila][columna].equals(nombres[jugador][primeraFila][primeraColumna]);
        if (acierto) {
            encontradas[jugador][fila][columna] = true;
            encontradas[jugador][primeraFila][primeraColumna] = true;
            parejas[jugador]++;
            tiempo += BONO_ACIERTO;
            tiempoMaximo += BONO_ACIERTO;
            if (parejas[jugador] >= parejasMaximas && ganador < 0) ganador = jugador;
        }
        primeraFila = -1;
        primeraColumna = -1;
        return acierto ? Resultado.ACIERTO : Resultado.FALLO;
    }

    //Pasa el turno al otro jugador (si el otro ya termino su tablero, sigue el mismo).
    public void cambiarTurno() {
        int otro = 1 - jugadorActivo;
        if (!jugadorTermino(otro)) {
            jugadorActivo = otro;
        }
        reiniciarTurno();
    }

    private void reiniciarTurno() {
        tiempo = TIEMPO_TURNO;
        tiempoMaximo = TIEMPO_TURNO;
        primeraFila = -1;
        primeraColumna = -1;
    }

    public void restarTiempo(double segundos) {
        tiempo = Math.max(0, tiempo - segundos);
    }

    public boolean tiempoAgotado() {
        return tiempo < 0.001;
    }

    public boolean jugadorTermino(int jugador) {
        return parejas[jugador] >= parejasMaximas;
    }

    public boolean partidaTerminada() {
        return ganador >= 0;
    }

    // El primer jugador en completar todas sus parejas gana.
    public int getGanador() {
        return ganador;
    }






    public String getNombre(int jugador, int fila, int columna) { return nombres[jugador][fila][columna]; }
    public int getIntentos(int jugador)                         { return intentos[jugador]; }
    public int getParejas(int jugador)                          { return parejas[jugador]; }
    public int getJugadorActivo()                               { return jugadorActivo; }
    public double getTiempo()                                   { return tiempo; }
    public double getTiempoMaximo()                             { return tiempoMaximo; }
    public int getTamanoMatriz()                                 { return tamanoMatriz; }
    public int getParejasMaximas()                               { return parejasMaximas; }
}
