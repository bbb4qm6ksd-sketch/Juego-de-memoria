
import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

// Centraliza la carga y reutilización de las fuentes tipográficas del proyecto.
public final class fuentes {


    // Guarda las fuentes ya cargadas para evitar leer el mismo archivo varias veces.
    private static final Map<String, Font> CACHE = new HashMap<>();

    // Evita crear objetos; las fuentes se obtienen mediante métodos estáticos.
    private fuentes() {}

    // ---------- Fuentes del proyecto (una línea por fuente) ----------
    // Métodos públicos para obtener cada fuente con el estilo y tamaño solicitados.
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




    // Busca la fuente en caché, la carga si hace falta y aplica estilo y tamaño.
    private static Font cargar(String ruta, int estilo, float tamano, String respaldo) {
        Font base = CACHE.get(ruta);
        if (base == null && !CACHE.containsKey(ruta)) {
            base = leer(ruta);
            CACHE.put(ruta, base); // guarda también null para no reintentar
        }
        // Si el archivo no se pudo cargar, crea una fuente estándar de respaldo.
        if (base == null) {
            return new Font(respaldo, estilo, Math.round(tamano));
        }
        return base.deriveFont(estilo, tamano);
    }

    // Lee el archivo de recursos, crea la fuente y la registra en el sistema gráfico.
    private static Font leer(String ruta) {
        try (InputStream is = fuentes.class.getResourceAsStream(ruta)) {
            // Si el recurso no existe, informa el problema para facilitar su diagnóstico.
            if (is == null) {
                System.err.println("No se encontró la fuente: " + ruta);
                return null;
            }
            // Interpreta el archivo como TrueType y lo registra para poder utilizarlo.
            Font f = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
            return f;
        } catch (Exception e) {
            // Informa errores de lectura o de formato y permite usar la fuente de respaldo.
            System.err.println("Error cargando " + ruta + ": " + e.getMessage());
            return null;
        }
    }

}