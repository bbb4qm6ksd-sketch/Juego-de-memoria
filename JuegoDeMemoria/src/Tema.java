import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Random;


public class Tema extends JPanel {

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
    private static Font fuenteLemaBase;

    public static Font lema(float tam) {
        if (fuenteLemaBase == null) {
            try (java.io.InputStream in = Tema.class.getResourceAsStream("/fonts/ROMANTIC.TTF")) {
                fuenteLemaBase = Font.createFont(Font.TRUETYPE_FONT, in);
            } catch (Exception e) {
                System.err.println("No se pudo cargar ROMANTIC.TTF, se usa la fuente serif: " + e);
                fuenteLemaBase = new Font(Font.SERIF, Font.PLAIN, 12);
            }
        }
        return fuenteLemaBase.deriveFont(Map.of(TextAttribute.SIZE, tam, TextAttribute.TRACKING, 0.08f));
    }

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
    private static final double[][] ORION = {
            {30, 15}, {70, 18}, {42, 48}, {50, 50}, {58, 52}, {28, 85}, {72, 82}
    };
    private static final int[][] ORION_LINEAS = {
            {0, 1}, {0, 2}, {1, 4}, {2, 3}, {3, 4}, {2, 5}, {4, 6}
    };
    private static final double[] ORION_RADIO = {0.014, 0.012, 0.012, 0.012, 0.012, 0.018, 0.010};
    private static final double[] ORION_RETRASO = {0.2, 0.5, 0.8, 1.1, 1.3, 1.5, 1.7};
    private static final double ORION_TRAZO = 1.6;
    private static final double[] ORION_FASE = {0.25, 0.5, 0.0, 0.1, 0.2, 0.75, 0.125};

    private final Color centro;
    private final Color borde;
    private final int cantidad;
    private final float[] sx, sy, tam, brilloBase, periodo, fase;
    private final float[] vel;
    private final Random azar = new Random();
    private boolean viaje = false;
    private long ultimoNs = System.nanoTime();


    private BufferedImage fondoCache;
    private boolean mostrarOrion = false;
    private boolean animado = true;
    private long inicioNs = System.nanoTime();

    private final Timer reloj = new Timer(33, e -> { actualizar(); repaint(); });

    public Tema(int cantidad) {
        this(cantidad, new Color(0x16224A), FONDO);
    }

    public Tema(int cantidad, Color centro, Color borde) {
        this.cantidad = cantidad;
        this.centro = centro;
        this.borde = borde;
        setOpaque(true);
        vel = new float[cantidad];

        Random r = new Random(7);
        sx = new float[cantidad]; sy = new float[cantidad]; tam = new float[cantidad];
        brilloBase = new float[cantidad]; periodo = new float[cantidad]; fase = new float[cantidad];
        for (int i = 0; i < cantidad; i++) {
            sx[i] = r.nextFloat();
            sy[i] = r.nextFloat();
            tam[i] = 1f + r.nextFloat() * 2f;
            brilloBase[i] = (40 + r.nextInt(180)) / 255f;
            periodo[i] = 2f + r.nextFloat() * 4f;
            fase[i] = r.nextFloat();
            float profundidad = (tam[i] - 1f) / 2f;
            vel[i] = 0.012f + profundidad * 0.05f;
        }

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (isShowing()) iniciarAnimacion(); else reloj.stop();

            }
        });
    }

    public void setMostrarOrion(boolean mostrar) {
        this.mostrarOrion = mostrar;
        repaint();
    }

    public void setAnimado(boolean animado) {
        this.animado = animado;
        if (animado && isShowing()) iniciarAnimacion(); else reloj.stop();
        repaint();
    }

    public void setViaje(boolean viaje) {
        this.viaje = viaje;
        reloj.setDelay(viaje ? 16 : 33);
        ultimoNs = System.nanoTime();
    }

    private void iniciarAnimacion() {
        if (animado) {
            inicioNs = System.nanoTime();
            ultimoNs = System.nanoTime();
            reloj.start();
        }
    }

    private void actualizar() {
        long ahora = System.nanoTime();
        float dt = Math.min((ahora - ultimoNs) / 1e9f, 0.1f);
        ultimoNs = ahora;
        if (!viaje) return;

        for (int i = 0; i < cantidad; i++) {
            sx[i] -= vel[i] * dt;
            if (sx[i] < 0f) {
                sx[i] += 1f;
                sy[i] = azar.nextFloat();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (fondoCache == null || fondoCache.getWidth() != w || fondoCache.getHeight() != h) {
            fondoCache = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D gc = fondoCache.createGraphics();
            gc.setPaint(new RadialGradientPaint(
                    new Point2D.Float(w * 0.3f, h * 0.2f), Math.max(w, h) * 0.85f,
                    new float[]{0f, 1f}, new Color[]{centro, borde}));
            gc.fillRect(0, 0, w, h);
            gc.dispose();
        }
        g2.drawImage(fondoCache, 0, 0, null);

        double seg = animado ? (System.nanoTime() - inicioNs) / 1e9 : 1e6;   // 1e6 = ya terminado

        for (int i = 0; i < cantidad; i++) {
            double f = animado ? 0.2 + 0.8 * (0.5 + 0.5 * Math.sin(2 * Math.PI * (seg / periodo[i] + fase[i]))) : 1.0;
            int alfa = (int) Math.min(255, 255 * brilloBase[i] * f);
            g2.setColor(new Color(255, 255, 255, alfa));
            g2.fill(new Ellipse2D.Float(sx[i] * w, sy[i] * h, tam[i], tam[i]));
        }

        if (mostrarOrion) dibujarOrion(g2, w, h, seg);
        g2.dispose();
    }

    private static int[] geometriaOrion(int w, int h) {
        int lado = (int) Math.min(h * 0.55, w * 0.38);
        int x0 = (int) (w * 0.94 - lado * 0.74);
        int y0 = (int) (h * 0.5 - lado * 0.58);
        return new int[]{lado, x0, y0};
    }

    private void dibujarOrion(Graphics2D g0, int w, int h, double seg) {
        int[] d = geometriaOrion(w, h);
        if (d == null) return;
        int lado = d[0], x0 = d[1], y0 = d[2];

        Graphics2D g2 = (Graphics2D) g0.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setStroke(new BasicStroke(1.3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(169, 188, 255, 170));
        for (int i = 0; i < ORION_LINEAS.length; i++) {
            double p = Math.max(0, Math.min(1, (seg - ORION_RETRASO[i]) / ORION_TRAZO));
            if (p <= 0) continue;
            p = p * p * (3 - 2 * p);                           // arranque y final suaves
            double ax = x0 + ORION[ORION_LINEAS[i][0]][0] * lado / 100;
            double ay = y0 + ORION[ORION_LINEAS[i][0]][1] * lado / 100;
            double bx = x0 + ORION[ORION_LINEAS[i][1]][0] * lado / 100;
            double by = y0 + ORION[ORION_LINEAS[i][1]][1] * lado / 100;
            g2.draw(new Line2D.Double(ax, ay, ax + (bx - ax) * p, ay + (by - ay) * p));
        }

        for (int i = 0; i < ORION.length; i++) {
            float cx = (float) (x0 + ORION[i][0] * lado / 100);
            float cy = (float) (y0 + ORION[i][1] * lado / 100);
            float r = (float) (lado * ORION_RADIO[i]);
            Color c = (i >= 2 && i <= 4) ? ESTRELLA : Color.WHITE;
            double brillo = animado
                    ? 0.3 + 0.7 * (0.5 + 0.5 * Math.sin(2 * Math.PI * (seg / 4.0 + ORION_FASE[i])))
                    : 1.0;

            g2.setPaint(new RadialGradientPaint(new Point2D.Float(cx, cy), r * 4,
                    new float[]{0f, 1f},
                    new Color[]{new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (90 * brillo)),
                            new Color(c.getRed(), c.getGreen(), c.getBlue(), 0)}));
            g2.fill(new Ellipse2D.Float(cx - r * 4, cy - r * 4, r * 8, r * 8));
            g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (255 * brillo)));
            g2.fill(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
        }

        g2.setFont(sans(Font.BOLD, 11).deriveFont(Map.of(TextAttribute.TRACKING, 0.25f)));
        g2.setColor(SECUNDARIO);
        g2.drawString("ORIÓN", (int) (x0 + lado * 0.33), (int) (y0 + lado * 1.0));
        g2.dispose();
    }
}