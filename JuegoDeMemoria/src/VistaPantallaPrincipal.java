import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.Map;

public class VistaPantallaPrincipal extends JFrame {
    private final JButton btnClasico = new BotonEstrella("JUGAR", true);
    private final JButton btnPatrones = new BotonEstrella("JUGAR", true);
    private final JButton btnPremios = new BotonEstrella("PREMIOS", false);
    private final JButton btnCerrarSesion = Tema.botonTexto("CERRAR SESIÓN");
    private final JButton btnSonido = Tema.botonTexto("SONIDO: ON");
    private CardLayout tarjetas;
    private JPanel pantallas;

    public VistaPantallaPrincipal(String usuario) {
        super("AstroMatch - Inicio");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Tema fondo = new Tema(150, new Color(0x141F47), Tema.FONDO);
        fondo.setAnimado(true);
        fondo.setViaje(true);
        fondo.setLayout(new BorderLayout());
        fondo.setBorder(new EmptyBorder(30, 52, 28, 52));
        setContentPane(fondo);
        tarjetas = new CardLayout();
        pantallas = new JPanel(tarjetas);
        pantallas.setOpaque(false);


        pantallas.add(crearBienvenida(usuario), "bienvenida");
        pantallas.add(crearContenido(), "opciones");

        fondo.add(crearEncabezado(usuario), BorderLayout.NORTH);
        fondo.add(pantallas, BorderLayout.CENTER);
        fondo.add(crearPie(), BorderLayout.SOUTH);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);
    }

    private JPanel crearEncabezado(String usuario) {
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        JLabel marca = new JLabel("✦  AstroMatch");
        marca.setFont(Tema.sans(Font.BOLD, 14));
        marca.setForeground(Tema.TEXTO);
        JPanel usuarioPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
        usuarioPanel.setOpaque(false);
        JLabel nombre = new JLabel("OBSERVADOR  ·  " + usuario.toUpperCase());
        nombre.setFont(Tema.sans(Font.BOLD, 10));
        nombre.setForeground(Tema.SECUNDARIO);
        btnCerrarSesion.setForeground(Tema.SECUNDARIO);
        usuarioPanel.add(nombre);
        usuarioPanel.add(btnSonido);
        usuarioPanel.add(btnCerrarSesion);
        encabezado.add(marca, BorderLayout.WEST);
        encabezado.add(usuarioPanel, BorderLayout.EAST);
        return encabezado;
    }

    private JPanel crearBienvenida(String usuario) {
        JPanel seccion = new JPanel(new BorderLayout());
        seccion.setOpaque(false);

        JLabel eyebrow = new JLabel("BIENVENIDO, " + usuario.toUpperCase());
        eyebrow.setFont(Tema.sans(Font.BOLD, 12));
        eyebrow.setForeground(Tema.HIELO);

        JLabel titulo = new JLabel("AstroMatch");
        titulo.setFont(Tema.serif(Font.PLAIN, 96));
        titulo.setForeground(Tema.TEXTO);

        DivisorEstelar divisor = new DivisorEstelar();

        JLabel lema = new JLabel("<html>Une los puntos, <i style='color:#A9BCFF'>lee el cielo.</i></html>");
        lema.setFont(Tema.serif(Font.PLAIN, 28));
        lema.setForeground(Tema.SECUNDARIO);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        for (JComponent c : new JComponent[]{eyebrow, titulo, divisor, lema}) c.setAlignmentX(Component.CENTER_ALIGNMENT);
        textos.add(eyebrow);
        textos.add(Box.createVerticalStrut(14));
        textos.add(titulo);
        textos.add(Box.createVerticalStrut(10));
        textos.add(divisor);
        textos.add(Box.createVerticalStrut(14));
        textos.add(lema);

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        centro.add(textos);

        JPanel sur = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        sur.setOpaque(false);
        sur.setBorder(new EmptyBorder(0, 0, 6, 0));

        JButton btnVerOpciones =
                new VistaTablero.BotonPill("VER OPCIONES  →", true, true);
        btnVerOpciones.addActionListener(e -> tarjetas.show(pantallas, "opciones"));
        sur.add(btnVerOpciones);

        seccion.add(centro, BorderLayout.CENTER);
        seccion.add(sur, BorderLayout.SOUTH);
        return seccion;
    }



    private JPanel crearContenido() {
        JPanel contenido = new JPanel(new BorderLayout(0, 32));
        contenido.setOpaque(false);
        JPanel titulos = new JPanel();
        titulos.setOpaque(false);
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));
        JLabel eyebrow = new JLabel("OBSERVATORIO  ·  EDICIÓN NOCTURNA");
        eyebrow.setFont(Tema.sans(Font.BOLD, 11));
        eyebrow.setForeground(Tema.HIELO);
        JLabel titulo = new JLabel("¿A dónde quieres ir?");
        titulo.setFont(Tema.serif(Font.PLAIN, 52));
        titulo.setForeground(Tema.TEXTO);
        JLabel subtitulo = new JLabel("Elige una opción para continuar tu observación.");
        subtitulo.setFont(Tema.sans(Font.PLAIN, 16));
        subtitulo.setForeground(Tema.SECUNDARIO);
        for (JComponent c : new JComponent[]{eyebrow, titulo, subtitulo}) c.setAlignmentX(Component.LEFT_ALIGNMENT);
        titulos.add(eyebrow);
        titulos.add(Box.createVerticalStrut(12));
        titulos.add(titulo);
        titulos.add(Box.createVerticalStrut(8));
        titulos.add(subtitulo);

        JPanel opciones = new JPanel(new GridLayout(1, 3, 24, 0));
        opciones.setOpaque(false);
        opciones.add(crearTarjeta("01  ·  MODO CLÁSICO", "Memoriza las constelaciones",
                "Dos jugadores, cada uno con su tablero de 6×6. Encuentra las 18 parejas con el menor número de intentos.",
                btnClasico, true));
        opciones.add(crearTarjeta("02  ·  MODO PATRONES", "Repite el patrón",
                "Un jugador. Memoriza la secuencia de casillas que se ilumina y repítela en orden, con una más en cada ronda.",
                btnPatrones, true));
        opciones.add(crearTarjeta("03  ·  CLASIFICACIÓN", "Top 5 premios",
                "Consulta los mejores resultados y descubre quién lidera el observatorio.",
                btnPremios, false));
        contenido.add(titulos, BorderLayout.NORTH);
        contenido.add(opciones, BorderLayout.CENTER);
        return contenido;
    }

    private JPanel crearTarjeta(String categoria, String titulo, String descripcion, JButton boton, boolean principal) {
        PanelOpcion tarjeta = new PanelOpcion(principal);
        tarjeta.setLayout(new BorderLayout(0, 18));
        tarjeta.setBorder(new EmptyBorder(28, 30, 28, 30));
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        JLabel etiqueta = new JLabel(categoria);
        etiqueta.setFont(Tema.sans(Font.BOLD, 10));
        etiqueta.setForeground(principal ? Tema.ESTRELLA : Tema.HIELO);
        JLabel encabezado = new JLabel("<html><body style='width:210px'>" + titulo + "</body></html>");
        encabezado.setFont(Tema.serif(Font.PLAIN, 30));
        encabezado.setForeground(Tema.TEXTO);
        JLabel detalle = new JLabel("<html><body style='width:200px'>" + descripcion + "</body></html>");
        detalle.setFont(Tema.sans(Font.PLAIN, 15));
        detalle.setForeground(Tema.SECUNDARIO);
        for (JComponent c : new JComponent[]{etiqueta, encabezado, detalle}) c.setAlignmentX(Component.LEFT_ALIGNMENT);
        textos.add(etiqueta);
        textos.add(Box.createVerticalStrut(16));
        textos.add(encabezado);
        textos.add(Box.createVerticalStrut(12));
        textos.add(detalle);
        boton.setPreferredSize(new Dimension(225, principal ? 66 : 60));
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, principal ? 66 : 60));
        tarjeta.add(textos, BorderLayout.CENTER);
        tarjeta.add(boton, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel crearPie() {
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        JLabel nota = new JLabel("AstroMatch  ·  UNE LOS PUNTOS, LEE EL CIELO");
        nota.setFont(Tema.sans(Font.PLAIN, 10));
        nota.setForeground(Tema.SECUNDARIO);
        pie.add(nota, BorderLayout.WEST);
        return pie;
    }

    private void mostrarBienvenida() {
        tarjetas.first(pantallas);
    }

    private void mostrarOpciones() {
        tarjetas.next(pantallas);
    }

    public JButton getBtnClasico() { return btnClasico; }
    public JButton getBtnPatrones() { return btnPatrones; }
    public JButton getBtnPremios() { return btnPremios; }
    public JButton getBtnCerrarSesion() { return btnCerrarSesion; }
    public JButton getBtnSonido() { return btnSonido; }
    private static Path2D estrella(double cx, double cy, double re, double ri) {
        Path2D.Double p = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double ang = -Math.PI / 2 + i * Math.PI / 5;
            double r = (i % 2 == 0) ? re : ri;
            double px = cx + r * Math.cos(ang);
            double py = cy + r * Math.sin(ang);
            if (i == 0) p.moveTo(px, py); else p.lineTo(px, py);
        }
        p.closePath();
        return p;
    }

    private static Color mezclar(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        return new Color(
                Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    private static class PanelOpcion extends JPanel {
        private final boolean destacado;
        PanelOpcion(boolean destacado) { this.destacado = destacado; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 1, h = getHeight() - 1;
            g2.setColor(destacado ? new Color(0x101A34) : new Color(0x0B1020));
            g2.fillRoundRect(0, 0, w, h, 28, 28);
            g2.setColor(destacado ? new Color(0x52699B) : Tema.LINEA);
            g2.drawRoundRect(0, 0, w, h, 28, 28);
            g2.dispose();
        }
    }


    private static class BotonEstrella extends JButton {
        private final boolean principal;
        private boolean encima = false;
        private float nivel = 0f;
        private long ultimoNs = System.nanoTime();

        BotonEstrella(String texto, boolean principal) {
            super(texto);
            this.principal = principal;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { encima = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { encima = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            long ahora = System.nanoTime();
            float dt = Math.min((ahora - ultimoNs) / 1e9f, 0.1f);
            ultimoNs = ahora;
            float meta = encima ? 1f : 0f;
            nivel += (meta - nivel) * Math.min(1f, dt * 9f);
            if (Math.abs(meta - nivel) < 0.01f) nivel = meta; else repaint();

            int margen = 7;
            int x = margen, y = margen;
            int w = getWidth() - 2 * margen - 1, h = getHeight() - 2 * margen - 1;
            if (getModel().isPressed()) g2.translate(0, 1);

            for (int i = 1; i <= 6; i++) {
                float a = nivel * 0.16f * (1f - i / 7f);
                g2.setStroke(new BasicStroke(i * 2f));
                g2.setColor(new Color(169, 188, 255, (int) (255 * a)));
                g2.drawRoundRect(x, y, w, h, h, h);
            }

            g2.setColor(new Color(169, 188, 255, (int) ((principal ? 34 : 10) + nivel * 40)));
            g2.fillRoundRect(x, y, w, h, h, h);
            g2.setStroke(new BasicStroke(1.3f));
            g2.setColor(principal ? mezclar(Tema.HIELO, Color.WHITE, nivel * 0.45f)
                    : mezclar(Tema.LINEA, Tema.HIELO, nivel));
            g2.drawRoundRect(x, y, w, h, h, h);

            Font fuente = Tema.sans(Font.BOLD, 14).deriveFont(Map.of(TextAttribute.TRACKING, 0.22f));
            g2.setFont(fuente);
            String texto = getText();
            double anchoTexto = fuente.getStringBounds(texto, g2.getFontRenderContext()).getWidth();
            double radio = 8.5, hueco = 14;
            double inicioX = (getWidth() - (anchoTexto + hueco + radio * 2)) / 2.0;
            FontMetrics fm = g2.getFontMetrics();
            double baseY = y + (h + fm.getAscent() - fm.getDescent()) / 2.0;
            g2.setColor(Tema.TEXTO);
            g2.drawString(texto, (float) inicioX, (float) baseY);

            double cx = inicioX + anchoTexto + hueco + radio;
            double cy = y + h / 2.0;

            float rh = (float) (radio * 2.6 * (1 + 0.25 * nivel));
            g2.setPaint(new RadialGradientPaint((float) cx, (float) cy, rh, new float[]{0f, 1f},
                    new Color[]{new Color(246, 220, 162, (int) (70 + 110 * nivel)), new Color(246, 220, 162, 0)}));
            g2.fill(new Ellipse2D.Double(cx - rh, cy - rh, rh * 2, rh * 2));

            double pulso = 1.0 + 0.07 * Math.sin(ahora / 1e9 * 2.2);
            double escala = (1.0 + 0.22 * nivel) * pulso;
            Graphics2D gs = (Graphics2D) g2.create();
            gs.translate(cx, cy);
            gs.rotate(Math.toRadians(nivel * 72.0));
            gs.scale(escala, escala);
            Path2D forma = estrella(0, 0, radio, radio * 0.42);
            gs.setColor(Tema.ESTRELLA);
            gs.fill(forma);
            gs.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            gs.draw(forma);
            gs.dispose();

            g2.dispose();
        }
    }

    private static class DivisorEstelar extends JComponent {
        @Override public Dimension getPreferredSize() { return new Dimension(280, 28); }
        @Override public Dimension getMaximumSize()   { return getPreferredSize(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), cx = w / 2, cy = getHeight() / 2;

            g2.setPaint(new GradientPaint(0, cy, new Color(169, 188, 255, 0), cx - 24, cy, new Color(169, 188, 255, 150)));
            g2.drawLine(0, cy, cx - 24, cy);
            g2.setPaint(new GradientPaint(cx + 24, cy, new Color(169, 188, 255, 150), w, cy, new Color(169, 188, 255, 0)));
            g2.drawLine(cx + 24, cy, w, cy);

            double brillo = 0.75 + 0.25 * Math.sin(System.nanoTime() / 1e9 * 2.0);
            float rh = 18f;
            g2.setPaint(new RadialGradientPaint(cx, cy, rh, new float[]{0f, 1f},
                    new Color[]{new Color(246, 220, 162, (int) (110 * brillo)), new Color(246, 220, 162, 0)}));
            g2.fill(new Ellipse2D.Float(cx - rh, cy - rh, rh * 2, rh * 2));
            g2.setColor(Tema.ESTRELLA);
            g2.fill(estrella(cx, cy, 8, 3.4));
            g2.dispose();
        }
    }


}