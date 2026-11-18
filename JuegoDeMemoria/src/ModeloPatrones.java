import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


// Guarda el estado y las reglas del reto de memoria, como el patrón, el nivel y los tiempos.
public class ModeloPatrones {
    public static final int FILAS = 6;
    public static final int COLUMNAS = 6;
    public static final int NIVEL_INICIAL = 3;
    public static final double TIEMPO_TOTAL = 12.0;
    public static final double TIEMPO_ENTRE_CASILLAS = 2.0;
    public static final double TIEMPO_SIGUIENTE_RONDA = 3.0;

    private int[] patron = new int[0];
    private int nivel = NIVEL_INICIAL;
    private int ronda = 1;
    private int posicion = 0;
    private double tiempoTotal = TIEMPO_TOTAL;
    private double tiempoEntreCasillas = TIEMPO_ENTRE_CASILLAS;
    private double tiempoSiguienteRonda = TIEMPO_SIGUIENTE_RONDA;

    // Reinicia el nivel, la ronda y el patron para empezar desde el principio.
    public void nuevoJuego(){
        nivel = NIVEL_INICIAL;
        ronda = 1;
        patron = new int[0];
    }

    // Conserva las casillas anteriores y anade nuevas para formar el patron.
    public void nuevaRonda(){
        ArrayList<Integer> nuevo = new ArrayList<>();
        for(int casilla : patron){
            nuevo.add(casilla);
        }
        ArrayList<Integer> libres = new ArrayList<>();
        for(int i = 0; i < FILAS * COLUMNAS; i++){
            if(!nuevo.contains(i)){
                libres.add(i);
            }
        }
        Collections.shuffle(libres);

        while (nuevo.size() < nivel){
            nuevo.add(libres.remove(0));
        }
        patron = new int[nuevo.size()];

        for(int i = 0; i < patron.length; i++){
            patron[i] = nuevo.get(i);
        }
        posicion = 0;
        tiempoTotal = TIEMPO_TOTAL;
        tiempoEntreCasillas = TIEMPO_ENTRE_CASILLAS;
    }
    // Comprueba si la casilla elegida coincide con el siguiente paso del patron.
    public boolean pulsar (int fila, int columna){
        if(patron[posicion] != fila * COLUMNAS + columna){
            return false;
        }
        posicion++;
        tiempoEntreCasillas = TIEMPO_ENTRE_CASILLAS;
        return true;
    }
    // Indica si ya se repitieron correctamente todas las casillas.
    public boolean patronCompleto(){
        return posicion == patron.length;
    }
    // Avanza la ronda y aumenta el patron hasta el limite del tablero.
    public void siguienteRonda(){
        ronda++;
        nivel = Math.min(nivel + 1, FILAS * COLUMNAS);
    }
    // Descuenta el tiempo transcurrido de los contadores de la ronda.
    public void restarTiempo(double segundos) {
        tiempoTotal = Math.max(0, tiempoTotal - segundos);
        tiempoEntreCasillas = Math.max(0, tiempoEntreCasillas - segundos);
    }
    // Indica si se termino el tiempo total o el disponible entre casillas.
    public boolean tiempoAgotado(){
        return tiempoTotal < 0.001 || tiempoEntreCasillas < 0.001;
    }

    // ---------- Cuenta atrás entre rondas ----------
    public void iniciarCuentaAtras() {
        tiempoSiguienteRonda = TIEMPO_SIGUIENTE_RONDA;
    }
    // Reduce el tiempo que falta para iniciar la ronda siguiente.
    public void restarCuentaAtras(double segundos) {
        tiempoSiguienteRonda = Math.max(0, tiempoSiguienteRonda - segundos);
    }
    // Indica si ya termino la espera entre rondas.
    public boolean cuentaAtrasTerminada() {
        return tiempoSiguienteRonda < 0.001;
    }
    // Devuelve los segundos que faltan para la ronda siguiente.
    public double getTiempoSiguienteRonda() {
        return tiempoSiguienteRonda;
    }

    // Obtiene la fila de la casilla indicada del patron.
    public int getFilaPatron(int i){
        return patron[i] / COLUMNAS;
    }
    // Obtiene la columna de la casilla indicada del patron.
    public int getColumnaPatron(int i){
        return patron[i] % COLUMNAS;
    }
    // Devuelve cuantas casillas forman el patron actual.
    public int getTamanoPatron(){
        return patron.length;
    }
    // Devuelve el nivel actual.
    public int getNivel(){
        return nivel;
    }
    // Devuelve el numero de ronda en curso.
    public int getRonda(){
        return ronda;
    }
    // Devuelve el tiempo total restante de la ronda.
    public double getTiempoTotal(){
        return tiempoTotal;
    }
    // Devuelve el tiempo restante para pulsar la proxima casilla.
    public double getTiempoEntreCasillas(){
        return tiempoEntreCasillas;
    }
}
