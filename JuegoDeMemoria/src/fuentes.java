import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class fuentes {

    // Cada archivo .ttf se carga una sola vez
    private static final Map<String, Font> CACHE = new HashMap<>();

    private fuentes() {}

    // ---------- Fuentes del proyecto (una línea por fuente) ----------
    public static Font romantic(int estilo, float tamano) {
        return cargar("/fonts/Romantic.ttf", estilo, tamano, Font.SERIF);
    }

    public static Font romantic(float tamano) {
        return romantic(Font.PLAIN, tamano);
    }

    public static Font kiwiSoda(int estilo, float tamano){
        return cargar("/fonts/KiwiSoda.ttf", estilo, tamano, Font.SERIF);
    }

    public static Font kiwiSoda(float tamano) {
        return kiwiSoda(Font.PLAIN, tamano);
    }

    public static Font mono(int estilo, float tamano) {
        return cargar("/fonts/Mono.ttf", estilo, tamano, Font.SERIF);
    }

    public static Font mono(float tamano){
        return fuentes.mono(Font.PLAIN, tamano);
    }




    // ---------- Lógica común ----------
    private static Font cargar(String ruta, int estilo, float tamano, String respaldo) {
        Font base = CACHE.get(ruta);
        if (base == null && !CACHE.containsKey(ruta)) {
            base = leer(ruta);
            CACHE.put(ruta, base); // guarda también null para no reintentar
        }
        if (base == null) {
            return new Font(respaldo, estilo, Math.round(tamano));
        }
        return base.deriveFont(estilo, tamano);
    }

    private static Font leer(String ruta) {
        try (InputStream is = fuentes.class.getResourceAsStream(ruta)) {
            if (is == null) {
                System.err.println("No se encontró la fuente: " + ruta);
                return null;
            }
            Font f = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
            return f;
        } catch (Exception e) {
            System.err.println("Error cargando " + ruta + ": " + e.getMessage());
            return null;
        }
    }

}