import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

/**
 * Carta del tablero. Solo se encarga de DIBUJARSE segun su estado visual.
 * Que estado tiene en cada momento lo decide el controlador.
 *
 * Cada constelacion tiene su propio color: las dos cartas de una pareja comparten color,
 * asi es mas facil reconocerlas. Las cartas OCULTAS no muestran color (no delatan nada).
 */
public class Carta extends JButton {

    public enum Estado { OCULTA, VOLTEADA, ENCONTRADA }

    // ---------- Un color por constelacion (clave = nombre sin tildes y en minusculas) ----------
    private static final Map<String, Color> COLORES = new HashMap<>();
    private static final Color COLOR_POR_DEFECTO = Tema.HIELO;

    static {
        color(0x5CD8FF, "cisne", "cygnus");                    // celeste
        color(0xFF5D5D, "escorpio", "scorpius");               // rojo
        color(0xFFD43B, "orion");                              // dorado
        color(0xA178FF, "geminis", "gemini");                  // violeta
        color(0x3DDC84, "cruz del sur", "crux");               // verde
        color(0xD77CFF, "casiopea", "cassiopeia");             // magenta
        color(0x4A90FF, "osa mayor", "ursa major");            // azul
        color(0xB5E84A, "lira", "lyra");                       // lima
        color(0xFF9A3C, "leo");                                // naranja
        color(0xC98B5A, "tauro", "taurus");                    // cobre
        color(0xC8B6FF, "andromeda");                          // lavanda
        color(0xFFC9A8, "pegaso", "pegasus");                  // durazno
        color(0x6C7BFF, "acuario", "aquarius");                // indigo
        color(0xE6EAF5, "aries");                              // plata
        color(0xA8F0D0, "cancer");                             // menta
        color(0xFF7AC8, "virgo");                              // rosa
        color(0xF3F0A0, "sagitario", "sagittarius");           // limon palido
        color(0x1FD1B2, "capricornio", "capricornus");         // turquesa
    }

    private static void color(int rgb, String... claves) {
        for (String k : claves) COLORES.put(k, new Color(rgb));
    }

    /** Color de una constelacion por su nombre (util tambien para leyendas u otras vistas). */
    public static Color colorDe(String nombre) {
        return COLORES.getOrDefault(clave(nombre), COLOR_POR_DEFECTO);
    }

    static String clave(String nombre) {
        return Normalizer.normalize(nombre == null ? "" : nombre, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase().trim();
    }

    // ---------- Estado de la carta ----------
    private Estado estado = Estado.OCULTA;
    private String nombre = "";
    private Icon imagen;
    private Color acento = COLOR_POR_DEFECTO;
    private Patron patron = Patron.para("");

    public Carta() {
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setEstado(Estado estado) { this.estado = estado; repaint(); }
    public Estado getEstado()            { return estado; }
    public void setImagen(Icon imagen)   { this.imagen = imagen; repaint(); }

    public void setNombre(String nombre) {
        this.nombre = nombre == null ? "" : nombre;
        this.acento = colorDe(this.nombre);
        this.patron = Patron.para(this.nombre);
        repaint();
    }

    // ---------- Utilidades de color ----------
    private static Color alfa(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
    }

    /** Mezcla a con b: t=0 -> a, t=1 -> b. */
    private static Color mezclar(Color a, Color b, float t) {
        return new Color(
                Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    // ---------- Dibujo de la carta ----------
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        boolean oculta = estado == Estado.OCULTA;
        boolean encontrada = estado == Estado.ENCONTRADA;

        RoundRectangle2D forma = new RoundRectangle2D.Float(1, 1, w - 3, h - 3, 18, 18);

        // fondo: oculta = azul noche; volteada/encontrada = azul noche con un toque del color de su constelacion
        Color arriba = oculta ? new Color(0x111A33)
                : mezclar(new Color(0x141D3A), acento, encontrada ? 0.26f : 0.09f);
        Color abajo = oculta ? new Color(0x0A0F20) : new Color(0x0C1226);
        g2.setPaint(new GradientPaint(0, 0, arriba, w, h, abajo));
        g2.fill(forma);

        // resplandor del color de la constelacion (mas fuerte cuando ya esta encontrada)
        if (!oculta) {
            int capas = encontrada ? 4 : 1;
            for (int i = capas; i >= 1; i--) {
                g2.setColor(alfa(acento, (encontrada ? 52 : 20) / i));
                g2.setStroke(new BasicStroke(1 + i * 2.5f));
                g2.draw(forma);
            }
        }
        Color borde;
        if (encontrada)        borde = acento;
        else if (!oculta)      borde = alfa(acento, 210);
        else                   borde = getModel().isRollover() ? Tema.HIELO : Tema.LINEA;
        g2.setColor(borde);
        g2.setStroke(new BasicStroke(encontrada ? 2f : 1.2f));
        g2.draw(forma);

        if (oculta) dibujarEstrella(g2, w / 2f, h / 2f);
        else        dibujarContenido(g2, w, h);
        g2.dispose();
    }

    private void dibujarEstrella(Graphics2D g2, float cx, float cy) {
        g2.setPaint(new RadialGradientPaint(new Point2D.Float(cx, cy), 14f,
                new float[]{0f, 1f}, new Color[]{alfa(Tema.HIELO, 110), alfa(Tema.HIELO, 0)}));
        g2.fillOval((int) (cx - 14), (int) (cy - 14), 28, 28);
        g2.setColor(Tema.HIELO);
        g2.fillOval((int) (cx - 2.5f), (int) (cy - 2.5f), 5, 5);
    }

    private void dibujarContenido(Graphics2D g2, int w, int h) {
        String etiqueta = nombre.isBlank() ? "Constelación" : nombre;
        int tam = Math.max(12, h / 7);
        g2.setFont(Tema.serif(Font.ITALIC, tam));
        FontMetrics fm = g2.getFontMetrics();
        while (tam > 9 && fm.stringWidth(etiqueta) > w - 10) {      // nombres largos: letra mas chica
            tam--;
            g2.setFont(Tema.serif(Font.ITALIC, tam));
            fm = g2.getFontMetrics();
        }
        int altoTexto = fm.getHeight() + 10;
        if (imagen != null) {
            int x = (w - imagen.getIconWidth()) / 2;
            int y = (h - altoTexto - imagen.getIconHeight()) / 2;
            imagen.paintIcon(this, g2, x, y);
        } else {
            dibujarConstelacion(g2, w, h - altoTexto - 20);
        }
        g2.setColor(mezclar(acento, Color.WHITE, 0.35f));
        g2.drawString(etiqueta, (w - fm.stringWidth(etiqueta)) / 2, h - 14);
    }

    /**
     * Constelacion con: halo suave de su color, lineas con brillo, estrellas con resplandor
     * y un destello en forma de cruz en la estrella mas brillante.
     */
    private void dibujarConstelacion(Graphics2D original, int w, int alto) {
        Patron p = patron;
        Graphics2D g2 = (Graphics2D) original.create();
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int lado = Math.min(w, alto);
        int margen = Math.max(12, lado / 7);
        double escalaX = Math.max(1, w - margen * 2);
        double escalaY = Math.max(1, alto - margen * 2);
        boolean encontrada = estado == Estado.ENCONTRADA;

        // halo detras de la figura
        float cx = w / 2f, cy = alto / 2f;
        float radioHalo = Math.max(10f, lado * 0.62f);
        g2.setPaint(new RadialGradientPaint(new Point2D.Float(cx, cy), radioHalo,
                new float[]{0f, 1f}, new Color[]{alfa(acento, encontrada ? 80 : 55), alfa(acento, 0)}));
        g2.fill(new Ellipse2D.Float(cx - radioHalo, cy - radioHalo, radioHalo * 2, radioHalo * 2));

        // lineas: una capa ancha y suave (brillo) y otra fina y nitida encima
        float grosor = Math.max(1.4f, lado / 70f);
        g2.setStroke(new BasicStroke(grosor * 3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(alfa(acento, encontrada ? 65 : 45));
        for (int[] linea : p.lineas) {
            g2.draw(new Line2D.Double(punto(p, linea[0], margen, escalaX, escalaY),
                    punto(p, linea[1], margen, escalaX, escalaY)));
        }
        g2.setStroke(new BasicStroke(grosor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(mezclar(acento, Color.WHITE, 0.15f));
        for (int[] linea : p.lineas) {
            g2.draw(new Line2D.Double(punto(p, linea[0], margen, escalaX, escalaY),
                    punto(p, linea[1], margen, escalaX, escalaY)));
        }

        // estrellas
        double base = 2.4 + lado / 90.0;
        for (int i = 0; i < p.puntos.length; i++) {
            Point2D s = punto(p, i, margen, escalaX, escalaY);
            boolean grande = i == p.brillante;
            double r = (grande ? 1.7 : 1.0) * base;

            float rg = (float) (r * 3.4);
            g2.setPaint(new RadialGradientPaint(new Point2D.Float((float) s.getX(), (float) s.getY()), rg,
                    new float[]{0f, 1f}, new Color[]{alfa(acento, 150), alfa(acento, 0)}));
            g2.fill(new Ellipse2D.Double(s.getX() - rg, s.getY() - rg, rg * 2, rg * 2));

            g2.setColor(mezclar(acento, Color.WHITE, 0.8f));
            g2.fill(new Ellipse2D.Double(s.getX() - r, s.getY() - r, r * 2, r * 2));

            if (grande) {                                       // destello en cruz
                double l = r * 3.2;
                g2.setStroke(new BasicStroke(1.1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(alfa(mezclar(acento, Color.WHITE, 0.7f), 200));
                g2.draw(new Line2D.Double(s.getX() - l, s.getY(), s.getX() + l, s.getY()));
                g2.draw(new Line2D.Double(s.getX(), s.getY() - l, s.getX(), s.getY() + l));
            }
        }
        g2.dispose();
    }

    private static Point2D punto(Patron p, int indice, int margen, double ancho, double alto) {
        return new Point2D.Double(margen + p.puntos[indice][0] * ancho, margen + p.puntos[indice][1] * alto);
    }

    // Dibujo de cada una de las constelaciones (coordenadas de 0 a 1)
    private static class Patron {
        final double[][] puntos;
        final int[][] lineas;
        final int brillante;
        Patron(double[][] puntos, int[][] lineas, int brillante) { this.puntos = puntos; this.lineas = lineas; this.brillante = brillante; }

        static Patron para(String nombre) {
            switch (clave(nombre)) {
                case "cisne": case "cygnus": return new Patron(new double[][]{{.5,.08},{.5,.92},{.16,.43},{.84,.43},{.5,.25},{.5,.68}}, new int[][]{{0,1},{2,3},{4,5}}, 0);
                case "escorpio": case "scorpius": return new Patron(new double[][]{{.72,.08},{.55,.24},{.42,.42},{.28,.60},{.35,.78},{.57,.88},{.77,.78},{.86,.62}}, new int[][]{{0,1},{1,2},{2,3},{3,4},{4,5},{5,6},{6,7}}, 0);
                case "orion": return new Patron(new double[][]{{.22,.12},{.78,.16},{.38,.48},{.50,.52},{.62,.48},{.22,.90},{.78,.88}}, new int[][]{{0,2},{1,4},{2,3},{3,4},{2,5},{4,6}}, 3);
                case "geminis": case "gemini": return new Patron(new double[][]{{.28,.12},{.72,.16},{.24,.48},{.76,.50},{.20,.90},{.80,.88}}, new int[][]{{0,2},{2,4},{1,3},{3,5},{2,3}}, 3);
                case "cruz del sur": case "crux": return new Patron(new double[][]{{.5,.08},{.5,.92},{.14,.48},{.86,.48},{.63,.73}}, new int[][]{{0,1},{2,3}}, 3);
                case "casiopea": case "cassiopeia": return new Patron(new double[][]{{.10,.72},{.30,.30},{.50,.62},{.72,.24},{.92,.60}}, new int[][]{{0,1},{1,2},{2,3},{3,4}}, 3);
                case "osa mayor": case "ursa major": return new Patron(new double[][]{{.10,.30},{.31,.24},{.50,.34},{.66,.49},{.72,.78},{.92,.75},{.93,.45}}, new int[][]{{0,1},{1,2},{2,3},{3,4},{4,5},{5,6},{3,6}}, 3);
                case "lira": case "lyra": return new Patron(new double[][]{{.50,.10},{.30,.45},{.70,.47},{.33,.84},{.72,.88}}, new int[][]{{0,1},{0,2},{1,2},{1,3},{2,4},{3,4}}, 0);
                case "leo": return new Patron(new double[][]{{.12,.28},{.36,.18},{.56,.32},{.76,.25},{.88,.50},{.62,.66},{.47,.89},{.27,.72}}, new int[][]{{0,1},{1,2},{2,3},{3,4},{4,5},{5,6},{6,7},{7,0},{2,5}}, 2);
                // Tauro: la "V" de la cara (Hiades) con Aldebaran en el vertice y los dos cuernos largos
                case "tauro": case "taurus": return new Patron(new double[][]{{.32,.58},{.18,.40},{.20,.76},{.62,.42},{.88,.16},{.64,.72},{.90,.62}}, new int[][]{{0,1},{0,2},{0,3},{3,4},{0,5},{5,6}}, 0);
                case "andromeda": return new Patron(new double[][]{{.10,.72},{.32,.48},{.55,.58},{.76,.32},{.92,.18}}, new int[][]{{0,1},{1,2},{2,3},{3,4}}, 3);
                // Pegaso: el Gran Cuadrado con el cuello y la cabeza saliendo de una esquina
                case "pegaso": case "pegasus": return new Patron(new double[][]{{.30,.28},{.70,.24},{.72,.62},{.34,.66},{.18,.80},{.08,.62},{.90,.10}}, new int[][]{{0,1},{1,2},{2,3},{3,0},{3,4},{4,5},{1,6}}, 1);
                case "acuario": case "aquarius": return new Patron(new double[][]{{.15,.25},{.35,.42},{.52,.24},{.70,.48},{.88,.36},{.40,.70},{.68,.86}}, new int[][]{{0,1},{1,2},{2,3},{3,4},{1,5},{5,6}}, 3);
                case "aries": return new Patron(new double[][]{{.15,.75},{.40,.35},{.62,.58},{.86,.20}}, new int[][]{{0,1},{1,2},{2,3}}, 1);
                case "cancer": return new Patron(new double[][]{{.18,.30},{.50,.50},{.82,.28},{.22,.75},{.78,.78}}, new int[][]{{0,1},{1,2},{1,3},{1,4}}, 1);
                case "virgo": return new Patron(new double[][]{{.12,.20},{.36,.38},{.22,.70},{.52,.56},{.72,.25},{.90,.68}}, new int[][]{{0,1},{1,2},{1,3},{3,4},{3,5}}, 3);
                case "sagitario": case "sagittarius": return new Patron(new double[][]{{.15,.75},{.35,.55},{.58,.40},{.82,.18},{.62,.16},{.84,.38},{.42,.82},{.58,.67}}, new int[][]{{0,1},{1,2},{2,3},{3,4},{3,5},{1,6},{1,7}}, 3);
                case "capricornio": case "capricornus": return new Patron(new double[][]{{.12,.30},{.36,.22},{.56,.50},{.80,.25},{.84,.72},{.52,.88},{.28,.62}}, new int[][]{{0,1},{1,2},{2,3},{3,4},{4,5},{5,6},{6,0}}, 2);
                default: return new Patron(new double[][]{{.18,.72},{.32,.32},{.52,.56},{.72,.18},{.86,.68}}, new int[][]{{0,1},{1,2},{2,3},{3,4}}, 3);
            }
        }
    }
}