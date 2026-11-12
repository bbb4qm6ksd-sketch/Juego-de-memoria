import java.io.*;
import java.util.*;

/**
 * Modelo de la tienda. Guarda, por jugador: saldo de estrellas, accesorios comprados y equipados para un avatar fijo y consumibles (segundos y puntos extra) pendientes de usar.
 * No conoce nada de Swing: solo datos y reglas de negocio.
 *
 * Los datos se guardan en  datos_tienda/tienda_<usuario>.properties
 */
public class ModeloTienda {

    public enum Resultado { EXITO, SALDO_INSUFICIENTE, YA_COMPRADO, NO_EXISTE }

    public static final int SALDO_INICIAL = 200;    // estrellas con las que empieza un jugador nuevo
    private static final String CARPETA = "datos_tienda";

    private final String usuario;
    private final List<Articulo> catalogo = new ArrayList<>();
    private final Set<String> accesoriosComprados = new LinkedHashSet<>();
    private final Set<String> accesoriosEquipados = new LinkedHashSet<>();
    private int saldo;
    private int segundosExtra;   // acumulados por compras de TIEMPO
    private int puntosExtra;     // acumulados por compras de PUNTOS

    public ModeloTienda(String usuario) {
        this.usuario = usuario;
        crearCatalogo();
        this.saldo = SALDO_INICIAL;
        cargar();
    }

    //Catalogo
    private void crearCatalogo() {
        catalogo.add(new Articulo("accesorio_antenas", Articulo.Tipo.ACCESORIO, "Antenas estelares", "Dos antenas con puntas luminosas", 50, 0));
        catalogo.add(new Articulo("accesorio_visor", Articulo.Tipo.ACCESORIO, "Visor cósmico", "Un visor para explorar galaxias lejanas", 70, 0));
        catalogo.add(new Articulo("accesorio_casco", Articulo.Tipo.ACCESORIO, "Casco lunar", "Casco resistente para tu próxima misión", 110, 0));
        catalogo.add(new Articulo("accesorio_capa", Articulo.Tipo.ACCESORIO, "Capa nebular", "Una capa con polvo de estrellas", 130, 0));
        catalogo.add(new Articulo("accesorio_halo", Articulo.Tipo.ACCESORIO, "Halo orbital", "Un halo brillante que flota sobre el avatar", 150, 0));
        catalogo.add(new Articulo("accesorio_mochila", Articulo.Tipo.ACCESORIO, "Mochila cohete", "Propulsor para recorrer el espacio", 180, 0));

        catalogo.add(new Articulo("tiempo_5",  Articulo.Tipo.TIEMPO, "+5 segundos",  "Suma 5 segundos a tu tiempo por jugada",  40, 5));
        catalogo.add(new Articulo("tiempo_10", Articulo.Tipo.TIEMPO, "+10 segundos", "Suma 10 segundos a tu tiempo por jugada", 70, 10));

        catalogo.add(new Articulo("puntos_1", Articulo.Tipo.PUNTOS, "+1 punto",  "Resta 1 intento a tu puntaje final",  60, 1));
        catalogo.add(new Articulo("puntos_3", Articulo.Tipo.PUNTOS, "+3 puntos", "Resta 3 intentos a tu puntaje final", 150, 3));
    }

    public List<Articulo> getCatalogo() { return Collections.unmodifiableList(catalogo); }

    public Articulo buscar(String id) {
        for (Articulo a : catalogo) if (a.getId().equals(id)) return a;
        return null;
    }

    //Consultas
    public String getUsuario()          { return usuario; }
    public int getSaldo()               { return saldo; }
    public int getSegundosExtra()       { return segundosExtra; }
    public int getPuntosExtra()         { return puntosExtra; }
    public Set<String> getAccesoriosComprados() { return Collections.unmodifiableSet(accesoriosComprados); }
    public Set<String> getAccesoriosEquipados() { return Collections.unmodifiableSet(accesoriosEquipados); }
    public boolean yaComprado(String id) { return accesoriosComprados.contains(id); }

    //Compra
    public Resultado comprar(String id) {
        Articulo a = buscar(id);
        if (a == null) return Resultado.NO_EXISTE;
        if (a.isUnico() && accesoriosComprados.contains(id)) return Resultado.YA_COMPRADO;
        if (saldo < a.getPrecio()) return Resultado.SALDO_INSUFICIENTE;

        saldo -= a.getPrecio();
        switch (a.getTipo()) {
            case ACCESORIO:
                accesoriosComprados.add(id);
                accesoriosEquipados.add(id); // El accesorio se aplica al avatar único.
                break;
            case TIEMPO:
                segundosExtra += a.getValor();
                break;
            case PUNTOS:
                puntosExtra += a.getValor();
                break;
        }
        guardar();
        return Resultado.EXITO;
    }

    public boolean equiparAccesorio(String id) {
        if (!accesoriosComprados.contains(id)) return false;
        accesoriosEquipados.add(id); guardar(); return true;
    }
    public boolean quitarAccesorio(String id) {
        if (!accesoriosEquipados.remove(id)) return false;
        guardar(); return true;
    }

    //Ganar estrellas (lo llama el juego al terminar una partida)
    public void agregarEstrellas(int cantidad) {
        if (cantidad <= 0) return;
        saldo += cantidad;
        guardar();
    }

    /** Menos intentos = más estrellas. Ajusta la fórmula a tu gusto. */
    public static int calcularEstrellas(int intentos) {
        return Math.max(10, 120 - intentos * 3);
    }

    //Uso de consumibles (lo llaman los controladores de los modos de juego)
    /** Devuelve los segundos extra acumulados y los gasta (quedan en 0). */
    public int usarTiempoExtra() {
        int s = segundosExtra;
        segundosExtra = 0;
        if (s > 0) guardar();
        return s;
    }

    /** Devuelve los puntos extra acumulados y los gasta (quedan en 0). */
    public int usarPuntosExtra() {
        int p = puntosExtra;
        puntosExtra = 0;
        if (p > 0) guardar();
        return p;
    }

    //Persistencia
    private File archivo() {
        String limpio = usuario.replaceAll("[^a-zA-Z0-9_-]", "_");
        return new File(CARPETA, "tienda_" + limpio + ".properties");
    }

    private void cargar() {
        File f = archivo();
        if (!f.exists()) { guardar(); return; }
        Properties p = new Properties();
        try (InputStream in = new FileInputStream(f)) {
            p.load(in);
            saldo = Integer.parseInt(p.getProperty("saldo", String.valueOf(SALDO_INICIAL)));
            segundosExtra = Integer.parseInt(p.getProperty("segundosExtra", "0"));
            puntosExtra = Integer.parseInt(p.getProperty("puntosExtra", "0"));
            accesoriosComprados.clear(); accesoriosEquipados.clear();
            String lista = p.getProperty("accesoriosComprados", "");
            for (String id : lista.split(",")) {
                Articulo a = buscar(id);
                if (a != null && a.getTipo() == Articulo.Tipo.ACCESORIO) accesoriosComprados.add(id);
            }
            String equipados = p.getProperty("accesoriosEquipados", "");
            for (String id : equipados.split(",")) if (accesoriosComprados.contains(id)) accesoriosEquipados.add(id);
        } catch (IOException | NumberFormatException e) {
            System.err.println("No se pudo leer la tienda de " + usuario + ": " + e.getMessage());
        }
    }

    private void guardar() {
        Properties p = new Properties();
        p.setProperty("saldo", String.valueOf(saldo));
        p.setProperty("segundosExtra", String.valueOf(segundosExtra));
        p.setProperty("puntosExtra", String.valueOf(puntosExtra));
        p.setProperty("accesoriosComprados", String.join(",", accesoriosComprados));
        p.setProperty("accesoriosEquipados", String.join(",", accesoriosEquipados));
        File f = archivo();
        f.getParentFile().mkdirs();
        try (OutputStream out = new FileOutputStream(f)) {
            p.store(out, "Tienda AstroMatch - " + usuario);
        } catch (IOException e) {
            System.err.println("No se pudo guardar la tienda de " + usuario + ": " + e.getMessage());
        }
    }
}