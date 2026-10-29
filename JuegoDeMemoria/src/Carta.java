import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.text.Normalizer;

public class Carta extends JButton {

    public enum Estado { OCULTA, VOLTEADA, ENCONTRADA }

    private Estado estado = Estado.OCULTA;
    private String nombre = "";
    private Icon imagen;

    public Carta() {
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public void setEstado(Estado estado) { this.estado = estado; repaint(); }
    public Estado getEstado()            { return estado; }
    public void setNombre(String nombre) { this.nombre = nombre; repaint(); }
    public void setImagen(Icon imagen)   { this.imagen = imagen; repaint(); }

    private static Color alfa(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight();
        boolean oculta = estado == Estado.OCULTA;

        RoundRectangle2D forma = new RoundRectangle2D.Float(1, 1, w - 3, h - 3, 18, 18);
        g2.setPaint(new GradientPaint(0, 0, oculta ? new Color(0x111A33) : new Color(0x141D3A),
                w, h, oculta ? new Color(0x0A0F20) : new Color(0x0C1226)));
        g2.fill(forma);

        if (estado == Estado.ENCONTRADA) {
            for (int i = 3; i >= 1; i--) {
                g2.setColor(alfa(Tema.ESTRELLA, 28 / i));
                g2.setStroke(new BasicStroke(1 + i * 2.5f));
                g2.draw(forma);
            }
        }
        Color borde = estado == Estado.ENCONTRADA ? Tema.ESTRELLA
                : (oculta && getModel().isRollover() ? Tema.HIELO : Tema.LINEA);
        g2.setColor(borde);
        g2.setStroke(new BasicStroke(1.2f));
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
        g2.setFont(Tema.serif(Font.ITALIC, Math.max(12, h / 7)));
        FontMetrics fm = g2.getFontMetrics();
        int altoTexto = fm.getHeight() + 10;
        if (imagen != null) {
            int x = (w - imagen.getIconWidth()) / 2;
            int y = (h - altoTexto - imagen.getIconHeight()) / 2;
            imagen.paintIcon(this, g2, x, y);
        } else {
            dibujarConstelacion(g2, w, h - altoTexto - 20);
        }
        g2.setColor(estado == Estado.ENCONTRADA ? Tema.ESTRELLA : Tema.TEXTO);
        String etiqueta = nombre.isBlank() ? "Constelación" : nombre;
        g2.drawString(etiqueta, (w - fm.stringWidth(etiqueta)) / 2, h - 14);
    }

    private void dibujarConstelacion(Graphics2D original, int w, int alto) {
        Patron p = Patron.para(nombre);
        Graphics2D g2 = (Graphics2D) original.create();
        int margen = Math.max(12, Math.min(w, alto) / 7);
        double escalaX = Math.max(1, w - margen * 2);
        double escalaY = Math.max(1, alto - margen * 2);
        g2.setStroke(new BasicStroke(Math.max(1.1f, Math.min(w, alto) / 85f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(0x8296C9));
        for (int[] linea : p.lineas) {
            Point2D a = punto(p, linea[0], margen, escalaX, escalaY);
            Point2D b = punto(p, linea[1], margen, escalaX, escalaY);
            g2.draw(new java.awt.geom.Line2D.Double(a, b));
        }
        for (int i = 0; i < p.puntos.length; i++) {
            Point2D estrella = punto(p, i, margen, escalaX, escalaY);
            double radio = (i == p.brillante ? 3.6 : 2.5) + Math.min(w, alto) / 100.0;
            g2.setColor(Tema.TEXTO);
            g2.fill(new java.awt.geom.Ellipse2D.Double(estrella.getX() - radio, estrella.getY() - radio, radio * 2, radio * 2));
        }
        g2.dispose();
    }

    private static Point2D punto(Patron p, int indice, int margen, double ancho, double alto) {
        return new Point2D.Double(margen + p.puntos[indice][0] * ancho, margen + p.puntos[indice][1] * alto);
    }

    //Dibujo de cada una de las constelaciones
    private static class Patron {
        final double[][] puntos;
        final int[][] lineas;
        final int brillante;
        Patron(double[][] puntos, int[][] lineas, int brillante) { this.puntos = puntos; this.lineas = lineas; this.brillante = brillante; }

        static Patron para(String nombre) {
            String n = Normalizer.normalize(nombre == null ? "" : nombre, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "").toLowerCase().trim();
            switch (n) {
                case "cisne": case "cygnus": return new Patron(new double[][]{{.5,.08},{.5,.92},{.16,.43},{.84,.43},{.5,.25},{.5,.68}}, new int[][]{{0,1},{2,3},{4,5}}, 0);
                case "escorpio": case "scorpius": return new Patron(new double[][]{{.72,.08},{.55,.24},{.42,.42},{.28,.60},{.35,.78},{.57,.88},{.77,.78},{.86,.62}}, new int[][]{{0,1},{1,2},{2,3},{3,4},{4,5},{5,6},{6,7}}, 0);
                case "orion": return new Patron(new double[][]{{.22,.12},{.78,.16},{.38,.48},{.50,.52},{.62,.48},{.22,.90},{.78,.88}}, new int[][]{{0,2},{1,4},{2,3},{3,4},{2,5},{4,6}}, 3);
                case "geminis": case "gemini": return new Patron(new double[][]{{.28,.12},{.72,.16},{.24,.48},{.76,.50},{.20,.90},{.80,.88}}, new int[][]{{0,2},{2,4},{1,3},{3,5},{2,3}}, 3);
                case "cruz del sur": case "crux": return new Patron(new double[][]{{.5,.08},{.5,.92},{.14,.48},{.86,.48},{.63,.73}}, new int[][]{{0,1},{2,3}}, 3);
                case "casiopea": case "cassiopeia": return new Patron(new double[][]{{.10,.72},{.30,.30},{.50,.62},{.72,.24},{.92,.60}}, new int[][]{{0,1},{1,2},{2,3},{3,4}}, 3);
                case "osa mayor": case "ursa major": return new Patron(new double[][]{{.10,.30},{.31,.24},{.50,.34},{.66,.49},{.72,.78},{.92,.75},{.93,.45}}, new int[][]{{0,1},{1,2},{2,3},{3,4},{4,5},{5,6},{3,6}}, 3);
                case "lira": case "lyra": return new Patron(new double[][]{{.50,.10},{.30,.45},{.70,.47},{.33,.84},{.72,.88}}, new int[][]{{0,1},{0,2},{1,2},{1,3},{2,4},{3,4}}, 0);
                case "leo": return new Patron(new double[][]{{.12,.28},{.36,.18},{.56,.32},{.76,.25},{.88,.50},{.62,.66},{.47,.89},{.27,.72}}, new int[][]{{0,1},{1,2},{2,3},{3,4},{4,5},{5,6},{6,7},{7,0},{2,5}}, 2);
                case "tauro": case "taurus": return new Patron(new double[][]{{.18,.24},{.44,.48},{.70,.18},{.82,.68},{.48,.88},{.16,.72}}, new int[][]{{0,1},{1,2},{1,3},{1,4},{1,5}}, 1);
                case "andromeda": return new Patron(new double[][]{{.10,.72},{.32,.48},{.55,.58},{.76,.32},{.92,.18}}, new int[][]{{0,1},{1,2},{2,3},{3,4}}, 3);
                case "pegaso": case "pegasus": return new Patron(new double[][]{{.18,.25},{.78,.25},{.75,.76},{.20,.76},{.18,.25},{.48,.50},{.06,.92},{.92,.92}}, new int[][]{{0,1},{1,2},{2,3},{3,0},{0,5},{5,6},{2,7}}, 1);
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
