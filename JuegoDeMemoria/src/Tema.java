import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Map;
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

    // Fuentes logicas de Java. Para usar otras (ej. Instrument Serif, Manrope)
    // se pueden cargar luego con Font.createFont(...).
    public static Font serif(int estilo, int tam) { return new Font(Font.SERIF, estilo, tam); }
    public static Font sans(int estilo, int tam)  { return new Font(Font.SANS_SERIF, estilo, tam); }

    /** Boton de texto sin fondo (para "Salir", "Crear cuenta", etc.). */
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

    /** Campo de texto minimalista con linea inferior. */
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

    // ---------- Fondo nocturno con estrellas ----------

    // ---------- Constelacion de Orion (coordenadas en una cuadricula de 0 a 100) ----------
    private static final double[][] ORION = {
            {30, 15}, {70, 18}, {42, 48}, {50, 50}, {58, 52}, {28, 85}, {72, 82}
    };
    private static final int[][] ORION_LINEAS = {
            {0, 1}, {0, 2}, {1, 4}, {2, 3}, {3, 4}, {2, 5}, {4, 6}
    };
    private static final double[] ORION_RADIO = {0.014, 0.012, 0.012, 0.012, 0.012, 0.018, 0.010};

    private boolean mostrarOrion = false;
    private final int cantidad;
    private final Color centro;
    private final Color borde;

    public Tema(int cantidad) {
        this(cantidad, new Color(0x16224A), FONDO);
    }

    public Tema(int cantidad, Color centro, Color borde) {
        this.cantidad = cantidad;
        this.centro = centro;
        this.borde = borde;
        setOpaque(true);
        new Timer(50, e -> repaint()).start();
    }

    /** Activa o desactiva el dibujo de la constelacion de Orion sobre el fondo. */
    public void setMostrarOrion(boolean mostrar) {
        this.mostrarOrion = mostrar;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();

        g2.setPaint(new RadialGradientPaint(
                new Point2D.Float(w * 0.3f, h * 0.2f), Math.max(w, h) * 0.85f,
                new float[]{0f, 1f}, new Color[]{centro, borde}));
        g2.fillRect(0, 0, w, h);

        Random r = new Random(7); // semilla fija: las estrellas no "bailan" al redibujar
        double tiempo = System.nanoTime() / 1_000_000_000.0;
        for (int i = 0; i < cantidad; i++) {
            float x = r.nextFloat() * w, y = r.nextFloat() * h;
            float t = 1f + r.nextFloat() * 2f;
            r.nextInt(180); // conserva las mismas posiciones aleatorias
            int opacidad = 40 + (int) (170 * (0.5 + 0.5 * Math.sin(tiempo * 2.4 + i * 1.7)));
            g2.setColor(new Color(255, 255, 255, opacidad));
            g2.fill(new Ellipse2D.Float(x, y, t, t));
        }
        if (mostrarOrion) dibujarOrion(g2, w, h, tiempo);
        g2.dispose();
    }

    private static void dibujarOrion(Graphics2D g0, int w, int h, double tiempo) {
        if (w < 760) return;                                   // ventana chica: no se dibuja
        Graphics2D g2 = (Graphics2D) g0.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int lado = (int) Math.min(h * 0.55, w * 0.38);         // tamano del dibujo
        int x0 = (int) (w * 0.94 - lado * 0.74);
        int y0 = (int) (h * 0.5 - lado * 0.58);

        g2.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(169, 188, 255, 170));
        for (int[] l : ORION_LINEAS) {
            g2.drawLine((int) (x0 + ORION[l[0]][0] * lado / 100), (int) (y0 + ORION[l[0]][1] * lado / 100),
                    (int) (x0 + ORION[l[1]][0] * lado / 100), (int) (y0 + ORION[l[1]][1] * lado / 100));
        }

        for (int i = 0; i < ORION.length; i++) {
            float cx = (float) (x0 + ORION[i][0] * lado / 100);
            float cy = (float) (y0 + ORION[i][1] * lado / 100);
            float r = (float) (lado * ORION_RADIO[i]);
            Color color = (i >= 2 && i <= 4) ? ESTRELLA : Color.WHITE;
            double brillo = 0.5 + 0.5 * Math.sin(tiempo * 2.1 + i * 1.3);
            int opacidadHalo = 35 + (int) (75 * brillo);
            int opacidadCentro = 165 + (int) (90 * brillo);

            g2.setPaint(new RadialGradientPaint(new Point2D.Float(cx, cy), r * 4,
                    new float[]{0f, 1f},
                    new Color[]{new Color(color.getRed(), color.getGreen(), color.getBlue(), opacidadHalo),
                            new Color(color.getRed(), color.getGreen(), color.getBlue(), 0)}));
            g2.fill(new Ellipse2D.Float(cx - r * 4, cy - r * 4, r * 8, r * 8));
            g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), opacidadCentro));
            g2.fill(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
        }

        g2.setFont(sans(Font.BOLD, 11).deriveFont(Map.of(TextAttribute.TRACKING, 0.25f)));
        g2.setColor(SECUNDARIO);
        g2.drawString("ORIÓN", (int) (x0 + lado * 0.33), (int) (y0 + lado * 1.0));
        g2.dispose();
    }
}