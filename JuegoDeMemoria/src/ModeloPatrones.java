import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ModeloPatrones {
    public static final int FILAS = 6;
    public static final int COLUMNAS = 6;
    public static final int NIVEL_INICIAL = 3;
    public static final double TIEMPO_TOTAL = 12.0;
    public static final double TIEMPO_ENTRE_CASILLAS = 2.0;

    private int[] patron = new int[0];
    private int nivel = NIVEL_INICIAL;
    private int ronda = 1;
    private int posicion = 0;
    private double tiempoTotal = TIEMPO_TOTAL;
    private double tiempoEntreCasillas = TIEMPO_ENTRE_CASILLAS;

    public void nuevoJuego(){
        nivel = NIVEL_INICIAL;
        ronda = 1;
        patron = new int[0];
    }

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
    public boolean pulsar (int fila, int columna){
        if(patron[posicion] != fila * COLUMNAS + columna){
            return false;
        }
        posicion++;
        tiempoEntreCasillas = TIEMPO_ENTRE_CASILLAS;
        return true;
    }
    public boolean patronCompleto(){
        return posicion == patron.length;
    }
    public void siguienteRonda(){
        ronda++;
        nivel = Math.min(nivel + 1, FILAS * COLUMNAS);
    }
    public void restarTiempo(double segundos) {
        tiempoTotal = Math.max(0, tiempoTotal - segundos);
        tiempoEntreCasillas = Math.max(0, tiempoEntreCasillas - segundos);
    }
    public boolean tiempoAgotado(){
        return tiempoTotal < 0.001 || tiempoEntreCasillas < 0.001;
    }

    public int getFilaPatron(int i){
        return patron[i] / COLUMNAS;
    }
    public int getColumnaPatron(int i){
        return patron[i] % COLUMNAS;
    }
    public int getTamanoPatron(){
        return patron.length;
    }
    public int getNivel(){
        return nivel;
    }
    public int getRonda(){
        return ronda;
    }
    public double getTiempoTotal(){
        return tiempoTotal;
    }
    public double getTiempoEntreCasillas(){
        return tiempoEntreCasillas;
    }
}

