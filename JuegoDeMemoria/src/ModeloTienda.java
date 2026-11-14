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

    // Catálogo visual: cada diseño corresponde a una capa PNG original.
    private void crearCatalogo() {
        String[][] secciones = {
                {"antenas", "Antenas", "Antenas estelares"},
                {"casco", "Casco", "Casco de astronauta"},
                {"capa", "Capa", "Capa espacial"},
                {"halo", "Halo", "Halo orbital"},
                {"mochila", "Mochila", "Mochila propulsora"},
                {"visor", "Visor", "Visor cósmico"}
        };
        for (String[] seccion : secciones) {
            for (int i = 1; i <= 10; i++) {
                String numero = String.format("%02d", i);
                String id = "accesorio_" + seccion[0] + "_" + numero;
                catalogo.add(new Articulo(id, Articulo.Tipo.ACCESORIO,
                        seccion[1] + " " + numero,
                        seccion[2] + " - diseño " + numero,
                        45 + (i - 1) * 5, 0));
            }
        }
        catalogo.add(new Articulo("tiempo_5", Articulo.Tipo.TIEMPO, "+5 segundos", "Agrega 5 segundos a tu reserva de tiempo extra", 45, 5));
        catalogo.add(new Articulo("tiempo_10", Articulo.Tipo.TIEMPO, "+10 segundos", "Agrega 10 segundos a tu reserva de tiempo extra", 80, 10));
        catalogo.add(new Articulo("tiempo_15", Articulo.Tipo.TIEMPO, "+15 segundos", "Agrega 15 segundos a tu reserva de tiempo extra", 110, 15));
        catalogo.add(new Articulo("puntos_5", Articulo.Tipo.PUNTOS, "+5 puntos", "Agrega 5 puntos a tu reserva de puntos extra", 55, 5));
        catalogo.add(new Articulo("puntos_10", Articulo.Tipo.PUNTOS, "+10 puntos", "Agrega 10 puntos a tu reserva de puntos extra", 100, 10));
        catalogo.add(new Articulo("puntos_15", Articulo.Tipo.PUNTOS, "+15 puntos", "Agrega 15 puntos a tu reserva de puntos extra", 145, 15));
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
                equiparAccesorioSinGuardar(id);
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
        equiparAccesorioSinGuardar(id);
        guardar();
        return true;
    }

    private void equiparAccesorioSinGuardar(String id) {
        String ranura = ranuraAccesorio(id);
        accesoriosEquipados.removeIf(equipado -> ranuraAccesorio(equipado).equals(ranura));
        accesoriosEquipados.add(id);
    }

    private static String ranuraAccesorio(String id) {
        int ultimoGuion = id.lastIndexOf('_');
        return ultimoGuion > "accesorio_".length() ? id.substring("accesorio_".length(), ultimoGuion) : id;
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

    /** Recompensa creciente por cada ronda completada en el modo patrones. */
    public static int calcularEstrellasPatrones(int nivelCompletado) {
        return Math.min(100, Math.max(10, 10 + nivelCompletado * 5));
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
    private File archivo() { return archivoDe(usuario); }

    private static File archivoDe(String usuario) {
        String limpio = usuario.replaceAll("[^a-zA-Z0-9_-]", "_");
        return new File(CARPETA, "tienda_" + limpio + ".properties");
    }

    /** Solo lectura: accesorios equipados de un jugador. Vacío si no tiene tienda o no equipó nada. */
    public static Set<String> equipadosDe(String usuario) {
        Set<String> equipados = new LinkedHashSet<>();
        if (usuario == null || usuario.trim().isEmpty()) return equipados;
        File f = archivoDe(usuario.trim());
        if (!f.exists()) return equipados;

        Properties p = new Properties();
        try (InputStream in = new FileInputStream(f)) {
            p.load(in);
        } catch (IOException e) {
            return equipados;
        }
        Set<String> comprados = new HashSet<>();
        for (String guardado : p.getProperty("accesoriosComprados", "").split(",")) {
            comprados.add(migrarIdAccesorio(guardado));
        }
        for (String guardado : p.getProperty("accesoriosEquipados", "").split(",")) {
            String id = migrarIdAccesorio(guardado);
            if (id.startsWith("accesorio_") && comprados.contains(id)) equipados.add(id);
        }
        return equipados;
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
            for (String guardado : lista.split(",")) {
                String id = migrarIdAccesorio(guardado);
                Articulo a = buscar(id);
                if (a != null && a.getTipo() == Articulo.Tipo.ACCESORIO) accesoriosComprados.add(id);
            }
            String equipados = p.getProperty("accesoriosEquipados", "");
            for (String guardado : equipados.split(",")) {
                String id = migrarIdAccesorio(guardado);
                if (accesoriosComprados.contains(id)) equiparAccesorioSinGuardar(id);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("No se pudo leer la tienda de " + usuario + ": " + e.getMessage());
        }
    }

    private static String migrarIdAccesorio(String id) {
        if (!id.startsWith("accesorio_") || id.matches("accesorio_.+_\\d{2}")) return id;
        String ranura = id.substring("accesorio_".length());
        if (Arrays.asList("antenas", "casco", "capa", "halo", "mochila", "visor").contains(ranura)) return id + "_01";
        return id;
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