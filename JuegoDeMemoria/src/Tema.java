import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Random;

/**
 * Tema visual del juego. Una sola clase que:
 *  - guarda la paleta, las fuentes y los estilos compartidos (miembros static), y
 *  - ES el panel de fondo nocturno con estrellas (usalo con new Tema(...)).
 * No contiene logica de juego.
 */
public class Tema extends JPanel {

    // Paleta "medianoche"
    public static final Color FONDO       = new Color(0x060914);
    public static final Color SUPERFICIE  = new Color(0x0D1324);
    public static final Color LINEA       = new Color(0x222C48);
    public static final Color TEXTO       = new Color(0xE9EDF8);
    public static final Color SECUNDARIO  = new Color(0x8A94B2);
    public static final Color HIELO       = new Color(0xA9BCFF);
    public static final Color ESTRELLA    = new Color(0xF6DCA2);
    public static final Color ERROR       = new Color(0xFF8D7E);

    public static Font serif(int estilo, int tam) { return new Font(Font.SERIF, estilo, tam); }
    public static Font sans(int estilo, int tam)  { return new Font(Font.SANS_SERIF, estilo, tam); }

    public static JButton botonTexto(String texto) {
        JButton b = new JButton(texto);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setForeground(SECUNDARIO);
        b.setFont(sans(Font.BOLD, 11));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setForeground(TEXTO); }
            @Override public void mouseExited(MouseEvent e)  { b.setForeground(SECUNDARIO); }
        });
        return b;
    }

    public static void estilizarCampo(JTextField campo) {
        campo.setOpaque(false);
        campo.setForeground(TEXTO);
        campo.setCaretColor(HIELO);
        campo.setFont(sans(Font.PLAIN, 16));
        Border normal = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, LINEA),
                BorderFactory.createEmptyBorder(8, 0, 10, 0));
        Border enfoque = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, HIELO),
                BorderFactory.createEmptyBorder(8, 0, 10, 0));
        campo.setBorder(normal);
        campo.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { campo.setBorder(enfoque); }
            @Override public void focusLost(FocusEvent e)   { campo.setBorder(normal); }
        });
    }

    // ---------- Fondo nocturno con estrellas estáticas y parpadeo suave ----------
    private final int cantidad;
    private final Color centro;
    private final Color borde;
    private final Timer animacionTimer;

    // Estructura para guardar cada estrella en una posición fija con su propio ciclo de parpadeo
    private static class EstrellaData {
        float xNorm, yNorm, tam;
        double fase;
        double velocidad;
    }
    private final EstrellaData[] estrellasData;

    public Tema(int cantidad) {
        this(cantidad, new Color(0x16224A), FONDO);
    }

    public Tema(int cantidad, Color centro, Color borde) {
        this.cantidad = cantidad;
        this.centro = centro;
        this.borde = borde;
        setOpaque(true);

        // Usamos la semilla fija (7) para que las posiciones nunca cambien de lugar
        Random r = new Random(7);
        estrellasData = new EstrellaData[cantidad];
        for (int i = 0; i < cantidad; i++) {
            EstrellaData e = new EstrellaData();
            e.xNorm = r.nextFloat(); // Posición X en proporción (0 a 1)
            e.yNorm = r.nextFloat(); // Posición Y en proporción (0 a 1)
            e.tam = 1f + r.nextFloat() * 2f;
            e.fase = r.nextFloat() * Math.PI * 2; // Fase inicial del parpadeo
            e.velocidad = 0.04 + r.nextFloat() * 0.08; // Qué tan rápido parpadea cada una
            estrellasData[i] = e;
        }

        // Timer que actualiza únicamente la intensidad del brillo cada 50 milisegundos
        animacionTimer = new Timer(50, e -> {
            for (EstrellaData est : estrellasData) {
                est.fase += est.velocidad; // Avanza el ciclo de la estrella
            }
            repaint(); // Redibuja con los nuevos niveles de brillo
        });
        animacionTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();

        g2.setPaint(new RadialGradientPaint(
                new Point2D.Float(w * 0.3f, h * 0.2f), Math.max(w, h) * 0.85f,
                new float[]{0f, 1f}, new Color[]{centro, borde}));
        g2.fillRect(0, 0, w, h);

        // Dibujamos las estrellas en sus sitios fijos, variando solo su opacidad con una función senoidal
        for (EstrellaData est : estrellasData) {
            float x = est.xNorm * w;
            float y = est.yNorm * h;

            // El seno genera un cambio suave de subida y bajada (efecto de encender y apagar)
            int alpha = (int) (40 + 160 * (0.5 + 0.5 * Math.sin(est.fase)));
            alpha = Math.max(0, Math.min(255, alpha));

            g2.setColor(new Color(255, 255, 255, alpha));
            g2.fill(new Ellipse2D.Float(x, y, est.tam, est.tam));
        }
        g2.dispose();
    }
}