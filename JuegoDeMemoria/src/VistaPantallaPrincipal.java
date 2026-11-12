import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;


//Botones
public class VistaPantallaPrincipal extends JFrame {
    private final JButton btnClasico = new BotonEstrella("JUGAR", true);
    private final JButton btnPatrones = new BotonEstrella("JUGAR", true);
    private final JButton btnPremios = new BotonEstrella("PREMIOS", false);
    private final JButton btnCerrarSesion = Tema.botonTexto("CERRAR SESIÓN");
    private final JButton btnSonido = Tema.botonTexto("SONIDO: ON");
    private CardLayout tarjetas;
    private JPanel pantallas;

    //Constructor
    public VistaPantallaPrincipal(String usuario) {
        super("AstroMatch - Inicio");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Fondo de estrellas
        Tema fondo = new Tema(150, new Color(0x141F47), Tema.FONDO);
        fondo.setAnimado(true);    //estrellas titilan
        fondo.setViaje(true);   //estrellas se mueven
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
        setSize(2000, 1000);
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);
    }

    //Encabezado
    private JPanel crearEncabezado(String usuario) {
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);

        JLabel marca = etiqueta("AstroMatch", fuentes.kiwiSoda(Font.BOLD, 14), Tema.TEXTO);

        JPanel usuarioPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
        usuarioPanel.setOpaque(false);

        JLabel nombre = etiqueta("OBSERVADOR  *  " + usuario.toUpperCase(), fuentes.mono(Font.BOLD, 10), Tema.SECUNDARIO);

        btnCerrarSesion.setForeground(Tema.SECUNDARIO);
        usuarioPanel.add(nombre);
        usuarioPanel.add(btnSonido);
        usuarioPanel.add(btnCerrarSesion);
        encabezado.add(marca, BorderLayout.WEST);
        encabezado.add(usuarioPanel, BorderLayout.EAST);
        return encabezado;
    }

    //Bienvenida
    private JPanel crearBienvenida(String usuario) {
        JPanel seccion = new JPanel(new BorderLayout());
        seccion.setOpaque(false);

        JLabel eyebrow = etiqueta("BIENVENIDO, " + usuario.toUpperCase(), fuentes.mono(Font.BOLD, 12), Tema.HIELO);
        JLabel titulo  = etiqueta("AstroMatch", fuentes.kiwiSoda(170f), Tema.TEXTO);

        DivisorEstelar divisor = new DivisorEstelar(); //linea con estrella en el centro

        LemaBienvenida lema = new LemaBienvenida();

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        //Acomoda todo en el centro
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

        //Estrella fugaz que cruza por delante y por detras del titulo
        EstrellaFugaz estrellaFugaz = new EstrellaFugaz(seccion, titulo);
        getRootPane().setGlassPane(estrellaFugaz);
        estrellaFugaz.setVisible(true);

        return seccion;
    }


    //Opciones
    private JPanel crearContenido() {
        JPanel contenido = new JPanel(new BorderLayout(0, 32));
        contenido.setOpaque(false);

        JPanel titulos = new JPanel();
        titulos.setOpaque(false);
        titulos.setLayout(new BoxLayout(titulos, BoxLayout.Y_AXIS));

        JLabel titulo    = etiqueta("¿A dónde quieres ir?", fuentes.kiwiSoda(Font.PLAIN, 52), Tema.TEXTO);
        JLabel subtitulo = etiqueta("Elige una opción para continuar tu observación.", fuentes.mono(Font.PLAIN, 13), Tema.SECUNDARIO);
        for (JComponent c : new JComponent[]{titulo, subtitulo}) c.setAlignmentX(Component.LEFT_ALIGNMENT);
        titulos.add(Box.createVerticalStrut(12));
        titulos.add(titulo);
        titulos.add(Box.createVerticalStrut(8));
        titulos.add(subtitulo);

        //Las tres tarjetas
        JPanel opciones = new JPanel(new GridLayout(1, 3, 24, 0));
        opciones.setOpaque(false);
        opciones.add(crearTarjeta(" MODO CLÁSICO", "Memoriza las constelaciones",
                "Dos jugadores, cada uno con su tablero de 6×6. Encuentra las 18 parejas con el menor número de intentos.",
                btnClasico, true, true, false, false));
        opciones.add(crearTarjeta(" MODO PATRONES", "Repite el patrón",
                "Un jugador. Memoriza la secuencia de casillas que se ilumina y repítela en orden, con una más en cada ronda.",
                btnPatrones, true, false, true, false));
        opciones.add(crearTarjeta("  CLASIFICACIÓN", "Top 5 premios",
                "Consulta los mejores resultados y descubre quién lidera el observatorio.",
                btnPremios, false, false, false, true));

        contenido.add(titulos, BorderLayout.NORTH);
        contenido.add(opciones, BorderLayout.CENTER);
        return contenido;
    }

    private JPanel crearTarjeta(String categoria, String titulo, String descripcion, JButton boton,
                                boolean principal, boolean conConstelaciones, boolean conPatron, boolean conPodio) {
        PanelOpcion tarjeta = new PanelOpcion(principal);
        tarjeta.setConstelaciones(conConstelaciones);
        tarjeta.setPatron(conPatron);
        tarjeta.setPodio(conPodio);
        tarjeta.setLayout(new BorderLayout(0, 18));
        tarjeta.setBorder(new EmptyBorder(28, 30, 28, 30));

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        JLabel lblCategoria = etiqueta(categoria, fuentes.mono(Font.BOLD, 10), principal ? Tema.ESTRELLA : Tema.HIELO);
        JLabel encabezado = etiqueta("<html><body style='width:210px'>" + titulo + "</body></html>", fuentes.mono(Font.PLAIN, 30), Tema.TEXTO);
        JLabel detalle    = etiqueta("<html><body style='width:200px'>" + descripcion + "</body></html>", fuentes.mono(Font.PLAIN, 15), Tema.SECUNDARIO);

        for (JComponent c : new JComponent[]{lblCategoria, encabezado, detalle}) c.setAlignmentX(Component.LEFT_ALIGNMENT);
        textos.add(lblCategoria);
        textos.add(Box.createVerticalStrut(16));
        textos.add(encabezado);
        textos.add(Box.createVerticalStrut(12));
        textos.add(detalle);

        boton.setPreferredSize(new Dimension(225, principal ? 66 : 60));
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, principal ? 66 : 60));

        tarjeta.add(textos, BorderLayout.CENTER);
        tarjeta.add(boton, BorderLayout.SOUTH);

        tarjeta.agregarZonaLibre(lblCategoria, encabezado, detalle);  // el bloque de texto
        tarjeta.agregarZonaLibre(boton);                              // el botón
        return tarjeta;
    }


    private static JLabel etiqueta(String texto, Font fuente, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente);
        l.setForeground(color);
        return l;
    }

    private JPanel crearPie() {
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);
        JLabel nota = new JLabel("AstroMatch  *  UNE LOS PUNTOS, LEE EL CIELO");
        nota.setFont(fuentes.mono(Font.PLAIN, 10));
        nota.setForeground(Tema.SECUNDARIO);
        pie.add(nota, BorderLayout.WEST);
        return pie;
    }

    //Cambio de pagina
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

    //Mezclar colores
    private static Color mezclar(Color a, Color b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        return new Color(
                Math.round(a.getRed() + (b.getRed() - a.getRed()) * t),
                Math.round(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                Math.round(a.getBlue() + (b.getBlue() - a.getBlue()) * t));
    }

    private static class PanelOpcion extends JPanel {
        private static final String[] CONSTELACIONES = {
                "Orión", "Escorpio", "Cisne", "Géminis", "Cruz del Sur", "Casiopea",
                "Osa Mayor", "Lira", "Leo", "Tauro", "Andrómeda", "Pegaso",
                "Acuario", "Aries", "Cáncer", "Virgo", "Sagitario", "Capricornio"};

        private final boolean destacado;
        private boolean conConstelaciones = false;
        private BufferedImage cache;// el fondo se dibuja una vez y se reutiliza

        private static final int MARGEN_LIBRE = 10;                  // aire alrededor del texto y del botón
        private final List<JComponent[]> grupos = new ArrayList<>(); // grupos de componentes que deben quedar sin dibujo
        private List<Rectangle> zonasUsadas = new ArrayList<>();     // zonas con las que se hizo el cache
        private BufferedImage[] miniaturas;                     // una imagen por constelación
        private int ladoMini, anchoUsado, altoUsado;
        private final long inicioNs = System.nanoTime();
        private long ultimoNs = System.nanoTime();
        private double tiempo = 0;    // segundos de animación: solo avanza con el mouse encima
        private float nivel = 0f;     // 0 = sin hover, 1 = con hover (transición suave)

        // por constelación: posición (0 a 1), velocidad, periodo y fase del parpadeo (igual que en Tema)
        private final float[] pos = new float[18], vel = new float[18], periodo = new float[18], fase = new float[18];
        private boolean conPatron = false;
        private boolean conPodio = false;
        private static final double SEG_VUELTA_PODIO = 6.0;   // lo que tarda la estrella en dar una vuelta
        private static final int[] SECUENCIA = {0, 4, 2, 5, 8, 7, 3, 1, 6};   // orden en que se iluminan (casillas 0 a 8)
        private static final double SEG_POR_ESTRELLA = 0.6, SEG_PAUSA = 1.0;

        private final Timer animacion = new Timer(16, e -> tic());

        private void tic() {
            long ahora = System.nanoTime();
            float dt = Math.min((ahora - ultimoNs) / 1e9f, 0.1f);
            ultimoNs = ahora;
            if (!isShowing()) return;

            float meta = mouseEncima() ? 1f : 0f;
            float previo = nivel;
            nivel += (meta - nivel) * Math.min(1f, dt * 8f);
            if (Math.abs(meta - nivel) < 0.01f) nivel = meta;

            if (nivel > 0f) {                 // animar mientras esté encima o se esté apagando
                tiempo += dt;
                if (conConstelaciones) avanzar(dt);
            }
            if (nivel > 0f || previo > 0f) repaint();
        }

        private boolean mouseEncima() {
            PointerInfo pi = MouseInfo.getPointerInfo();
            if (pi == null) return false;
            Point p = pi.getLocation();
            SwingUtilities.convertPointFromScreen(p, this);
            return contains(p);
        }

        // Mismo esquema que Tema.actualizar(): cada constelación a su velocidad
        private void avanzar(float dt) {
            for (int i = 0; i < pos.length; i++) pos[i] = Tema.derivar(pos[i], vel[i], dt);
        }

        private float factorBrillo() { return 0.55f + 0.45f * nivel; }   // opacidad del dibujo: tenue sin hover

        PanelOpcion(boolean destacado) {
            this.destacado = destacado;
            setOpaque(false);
            animacion.start();
        }

        void setConstelaciones(boolean activo) { this.conConstelaciones = activo; }
        void setPatron(boolean activo)         { this.conPatron = activo; }
        void setPodio(boolean activo)          { this.conPodio = activo; }


        // Cada llamada define una zona libre: el rectángulo que envuelve a esos componentes
        void agregarZonaLibre(JComponent... componentes) { grupos.add(componentes); }

        private List<Rectangle> calcularZonas() {
            List<Rectangle> zonas = new ArrayList<>();
            for (JComponent[] grupo : grupos) {
                Rectangle union = null;
                for (JComponent c : grupo) {
                    if (c.getWidth() == 0) continue;   // aún no se ha acomodado
                    Rectangle r = SwingUtilities.convertRectangle(c.getParent(), c.getBounds(), this);
                    union = (union == null) ? r : union.union(r);
                }
                if (union != null) {
                    union.grow(MARGEN_LIBRE, MARGEN_LIBRE);
                    zonas.add(union);
                }
            }
            return zonas;
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 1, h = getHeight() - 1;

            // fondo: se aclara con el hover
            Color fondo = destacado ? new Color(0x101A34) : new Color(0x0B1020);
            g2.setColor(mezclar(fondo, new Color(0x22346B), nivel * 0.7f));
            g2.fillRoundRect(0, 0, w, h, 28, 28);

            if (conConstelaciones) dibujarConstelaciones(g2, w, h);

            Graphics2D gd = (Graphics2D) g2.create();
            gd.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, factorBrillo()));
            if (conPatron) dibujarPatron(gd, w, h);
            if (conPodio) dibujarPodio(gd, w, h);
            gd.dispose();

            // borde con resplandor
            for (int i = 1; i <= 4; i++) {
                g2.setStroke(new BasicStroke(i * 2.5f));
                g2.setColor(new Color(169, 188, 255, (int) (nivel * 40 / i)));
                g2.drawRoundRect(0, 0, w, h, 28, 28);
            }
            g2.setStroke(new BasicStroke(1f + nivel));
            g2.setColor(mezclar(destacado ? new Color(0x52699B) : Tema.LINEA, Tema.HIELO, nivel));
            g2.drawRoundRect(0, 0, w, h, 28, 28);
            g2.dispose();
        }

        // Se dibuja DESPUÉS del texto y el botón: así las tarjetas sin hover se ven más apagadas
        @Override protected void paintChildren(Graphics g) {
            super.paintChildren(g);
            float apagado = 1f - nivel;
            if (apagado <= 0f) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(6, 9, 20, (int) (90 * apagado)));
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 28, 28);
            g2.dispose();
        }


        private void crearMiniaturas(int w, int h) {
            int columnas = 3, filas = 6;
            ladoMini = (int) Math.min(w / (float) columnas, h / (float) filas);
            miniaturas = new BufferedImage[CONSTELACIONES.length];
            for (int i = 0; i < CONSTELACIONES.length; i++) {
                miniaturas[i] = new BufferedImage(ladoMini, ladoMini, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gi = miniaturas[i].createGraphics();
                gi.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Carta.dibujarMiniatura(gi, CONSTELACIONES[i], 0, 0, ladoMini, ladoMini, 1f);
                gi.dispose();
            }
            anchoUsado = w;
            altoUsado = h;

            // valores iniciales (solo la primera vez), con la misma semilla que usa Tema
            if (vel[0] == 0f) {
                java.util.Random r = new java.util.Random(7);
                for (int i = 0; i < pos.length; i++) {
                    int fila = i / columnas;
                    pos[i] = ((i % columnas) + 0.5f) / columnas;                 // repartidas en su fila
                    vel[i] = 0.012f + (fila / (float) (filas - 1)) * 0.05f;      // filas de abajo = más cerca = más rápidas
                    periodo[i] = 2f + r.nextFloat() * 4f;
                    fase[i] = r.nextFloat();
                }
            }
        }

        private void dibujarConstelaciones(Graphics2D g2, int w, int h) {
            if (w <= 0 || h <= 0) return;
            if (miniaturas == null || w != anchoUsado || h != altoUsado) crearMiniaturas(w, h);

            // zona visible = tarjeta menos los rectángulos del texto y del botón
            Area visible = new Area(new RoundRectangle2D.Float(0, 0, w, h, 28, 28));
            for (Rectangle r : calcularZonas()) {
                visible.subtract(new Area(new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, 24, 24)));
            }

            Graphics2D g = (Graphics2D) g2.create();
            g.clip(visible);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int filas = 6;
            float altoCelda = h / (float) filas;
            float recorrido = w + ladoMini;                       // entran por la derecha y salen por la izquierda
            double seg = tiempo;

            for (int i = 0; i < miniaturas.length; i++) {
                int fila = i / 3;
                int x = Math.round(pos[i] * recorrido - ladoMini);
                int y = Math.round((fila + 0.5f) * altoCelda - ladoMini / 2f);

                float base = 0.10f + 0.25f * fila / (filas - 1);
                float brillo = (float) Tema.parpadeo(seg, periodo[i], fase[i]);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                        base * (0.5f + 0.5f * brillo) * factorBrillo()));
                g.drawImage(miniaturas[i], x, y, null);
            }
            g.dispose();
        }

        // Cuadrícula 3x3 de estrellas; una secuencia se ilumina y crece una estrella por ronda.
        private void dibujarPatron(Graphics2D g2, int w, int h) {
            List<Rectangle> zonas = calcularZonas();
            if (zonas.size() < 2) return;                          // aún no se acomodó el layout

            // espacio libre = entre el final del texto y el inicio del botón
            Rectangle texto = zonas.get(0), boton = zonas.get(zonas.size() - 1);
            int arriba = texto.y + texto.height, abajo = boton.y;
            int lado = Math.min(w - 80, abajo - arriba - 30);
            if (lado < 90) return;
            double cx = w / 2.0, cy = (arriba + abajo) / 2.0, paso = lado / 3.0;
            float r = (float) (lado * 0.035);

            // en qué ronda y en qué paso de la secuencia estamos
            double t = tiempo;
            int n = 1;
            double dentro = 0;
            for (; n <= SECUENCIA.length; n++) {
                double dur = n * SEG_POR_ESTRELLA + SEG_PAUSA;
                if (t % ciclo() < dur + inicioRonda(n)) { dentro = (t % ciclo()) - inicioRonda(n); break; }
            }
            if (n > SECUENCIA.length) n = SECUENCIA.length;
            int actual = dentro < n * SEG_POR_ESTRELLA ? (int) (dentro / SEG_POR_ESTRELLA) : -1;   // -1 = pausa
            double fraccion = actual >= 0 ? (dentro / SEG_POR_ESTRELLA) - actual : 0;
            int hasta = actual >= 0 ? actual : n - 1;              // último paso ya mostrado

            // posición de cada casilla y su lugar en la secuencia
            double[] px = new double[9], py = new double[9];
            int[] orden = new int[9];
            java.util.Arrays.fill(orden, -1);
            for (int i = 0; i < 9; i++) {
                px[i] = cx + (i % 3 - 1) * paso;
                py[i] = cy + (i / 3 - 1) * paso;
            }
            for (int k = 0; k < SECUENCIA.length; k++) orden[SECUENCIA[k]] = k;

            Graphics2D g = (Graphics2D) g2.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // líneas del tramo ya recorrido
            g.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(169, 188, 255, 90));
            for (int k = 1; k <= hasta; k++) {
                int a = SECUENCIA[k - 1], b = SECUENCIA[k];
                g.draw(new java.awt.geom.Line2D.Double(px[a], py[a], px[b], py[b]));
            }

            // estrellas
            for (int i = 0; i < 9; i++) {
                boolean iluminada = orden[i] >= 0 && orden[i] <= hasta;
                boolean esActual = actual >= 0 && orden[i] == actual;

                if (esActual) {                                    // la que sigue en el patrón: brilla fuerte
                    double pulso = 0.4 + 0.6 * Math.sin(Math.PI * fraccion);
                    float rg = (float) (r * 6 * (0.6 + 0.4 * pulso));
                    g.setPaint(new RadialGradientPaint((float) px[i], (float) py[i], rg, new float[]{0f, 1f},
                            new Color[]{new Color(246, 220, 162, (int) (200 * pulso)), new Color(246, 220, 162, 0)}));
                    g.fill(new java.awt.geom.Ellipse2D.Double(px[i] - rg, py[i] - rg, rg * 2, rg * 2));
                    AffineTransform viejo = g.getTransform();
                    g.translate(px[i], py[i]);
                    g.scale(1.0 + 0.5 * pulso, 1.0 + 0.5 * pulso);
                    g.setColor(Tema.ESTRELLA);
                    g.fill(estrella(0, 0, r * 1.6, r * 0.7));
                    g.setTransform(viejo);
                } else if (iluminada) {                            // ya mostradas en esta ronda
                    g.setColor(new Color(169, 188, 255, 200));
                    g.fill(estrella(px[i], py[i], r * 1.3, r * 0.55));
                } else {                                           // apagadas
                    g.setColor(new Color(255, 255, 255, 70));
                    g.fill(new java.awt.geom.Ellipse2D.Double(px[i] - r * 0.6, py[i] - r * 0.6, r * 1.2, r * 1.2));
                }
            }
            g.dispose();
        }

        // Podio de 5 puestos (orden: 4º, 2º, 1º, 3º, 5º); una estrella fugaz recorre su contorno.
        private void dibujarPodio(Graphics2D g2, int w, int h) {
            List<Rectangle> zonas = calcularZonas();
            if (zonas.size() < 2) return;
            Rectangle texto = zonas.get(0), boton = zonas.get(zonas.size() - 1);
            int arriba = texto.y + texto.height, abajo = boton.y;
            int disponible = abajo - arriba - 30;
            if (disponible < 90) return;

            // medidas
            final int N = 5;
            int ancho = Math.min(w - 80, (int) (disponible * 1.9));
            double bw = ancho / (double) N;
            double x0 = (w - ancho) / 2.0, base = abajo - 15;

            // alturas por columna (izq. a der.) y puesto que representa cada una
            double[] fraccion = {0.34, 0.62, 0.78, 0.48, 0.22};
            String[] num = {"4", "2", "1", "3", "5"};
            double[] alt = new double[N];
            for (int i = 0; i < N; i++) alt[i] = disponible * fraccion[i];

            // contorno: sube por la izquierda, recorre las cimas y baja por la derecha
            List<double[]> lista = new ArrayList<>();
            lista.add(new double[]{x0, base});
            for (int i = 0; i < N; i++) {
                lista.add(new double[]{x0 + i * bw, base - alt[i]});
                lista.add(new double[]{x0 + (i + 1) * bw, base - alt[i]});
            }
            lista.add(new double[]{x0 + N * bw, base});
            lista.add(new double[]{x0, base});                       // cierra el bucle
            double[][] pts = lista.toArray(new double[0][]);

            double[] acum = new double[pts.length];                  // longitud acumulada de la ruta
            for (int i = 1; i < pts.length; i++)
                acum[i] = acum[i - 1] + Math.hypot(pts[i][0] - pts[i - 1][0], pts[i][1] - pts[i - 1][1]);
            double perimetro = acum[acum.length - 1];

            Graphics2D g = (Graphics2D) g2.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // silueta tenue + divisiones internas
            Path2D.Double silueta = new Path2D.Double();
            silueta.moveTo(pts[0][0], pts[0][1]);
            for (int i = 1; i < pts.length; i++) silueta.lineTo(pts[i][0], pts[i][1]);
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(new Color(169, 188, 255, 90));
            g.draw(silueta);
            g.setColor(new Color(169, 188, 255, 45));
            for (int i = 1; i < N; i++) {
                double xd = x0 + i * bw;
                g.draw(new Line2D.Double(xd, base - Math.min(alt[i - 1], alt[i]), xd, base));
            }

            // números de los puestos
            g.setFont(fuentes.mono(Font.BOLD, Math.max(12, (int) (bw * 0.38))));
            FontMetrics fm = g.getFontMetrics();
            g.setColor(new Color(169, 188, 255, 120));
            for (int i = 0; i < N; i++) {
                double cx = x0 + (i + 0.5) * bw;
                g.drawString(num[i], (float) (cx - fm.stringWidth(num[i]) / 2.0),
                        (float) (base - alt[i] + fm.getAscent() + 10));
            }

            // estrella sobre el primer puesto (columna central)
            double seg = (System.nanoTime() - inicioNs) / 1e9;
            double brillo = 0.75 + 0.25 * Math.sin(seg * 2.0);
            double sx = x0 + 2.5 * bw, sy = base - alt[2] - 14;
            g.setPaint(new RadialGradientPaint((float) sx, (float) sy, 18f, new float[]{0f, 1f},
                    new Color[]{new Color(246, 220, 162, (int) (110 * brillo)), new Color(246, 220, 162, 0)}));
            g.fill(new Ellipse2D.Double(sx - 18, sy - 18, 36, 36));
            g.setColor(Tema.ESTRELLA);
            g.fill(estrella(sx, sy, 8, 3.4));

            // estrella fugaz: solo con el mouse encima
            if (nivel > 0.02f) {
                double d = (seg % SEG_VUELTA_PODIO) / SEG_VUELTA_PODIO * perimetro;
                int pasos = 45;
                double largoCola = perimetro * 0.18, salto = largoCola / pasos;
                double r0 = Math.max(4, ancho * 0.015);
                for (int k = pasos; k >= 1; k--) {
                    double[] a = puntoEnRuta(pts, acum, d - k * salto);
                    double[] b = puntoEnRuta(pts, acum, d - (k - 1) * salto);
                    double q = (pasos - k + 1) / (double) pasos;
                    Line2D.Double l = new Line2D.Double(a[0], a[1], b[0], b[1]);
                    g.setColor(new Color(225, 235, 255, (int) (Math.pow(q, 1.5) * 70)));
                    g.setStroke(new BasicStroke((float) (r0 * 2.0 * q), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.draw(l);
                    g.setColor(new Color(255, 255, 255, (int) (Math.pow(q, 1.3) * 230)));
                    g.setStroke(new BasicStroke((float) (r0 * 0.6 * q), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g.draw(l);
                }
                double[] cab = puntoEnRuta(pts, acum, d);
                EstrellaFugaz.cabeza(g, cab[0], cab[1], r0, System.nanoTime() / 9e8);
            }
            g.dispose();
        }


        // Punto de la ruta a una distancia d del inicio (la ruta es un bucle, así que d se "da la vuelta")
        private static double[] puntoEnRuta(double[][] pts, double[] acum, double d) {
            double total = acum[acum.length - 1];
            d = ((d % total) + total) % total;
            int i = 1;
            while (i < acum.length - 1 && acum[i] < d) i++;
            double f = (d - acum[i - 1]) / (acum[i] - acum[i - 1]);
            return new double[]{pts[i - 1][0] + (pts[i][0] - pts[i - 1][0]) * f,
                    pts[i - 1][1] + (pts[i][1] - pts[i - 1][1]) * f};
        }

        // duración total de un ciclo completo (todas las rondas) y dónde empieza cada ronda
        private static double ciclo() {
            double total = 0;
            for (int n = 1; n <= SECUENCIA.length; n++) total += n * SEG_POR_ESTRELLA + SEG_PAUSA;
            return total;
        }

        private static double inicioRonda(int n) {
            double total = 0;
            for (int k = 1; k < n; k++) total += k * SEG_POR_ESTRELLA + SEG_PAUSA;
            return total;
        }
    }

    //Boton de estrella que gira
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
            //Calidad de dibujo
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

            Font fuente = fuentes.mono(Font.BOLD, 14).deriveFont(Map.of(TextAttribute.TRACKING, 0.22f));
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

    //Dibuja la frase de bienvenida
    private static class LemaBienvenida extends JComponent {
        private static final String PARTE1 = "Une los puntos,";
        private static final String PARTE2 = "lee el cielo.";
        private final Font fuente = fuentes.mono(44f);
        private final FontRenderContext frc = new FontRenderContext(null, true, true);
        private final int alto;
        private final int ancho;

        LemaBienvenida() {
            LineMetrics lm = fuente.getLineMetrics(PARTE1, frc);
            alto = (int) Math.ceil(lm.getAscent() + lm.getDescent()) + 6;
            ancho = (int) Math.ceil(fuente.getStringBounds(PARTE1 + " " + PARTE2, frc).getWidth()) + 12;
            setOpaque(false);
        }

        @Override public Dimension getPreferredSize() { return new Dimension(ancho, alto); }
        @Override public Dimension getMaximumSize()   { return getPreferredSize(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g2.setFont(fuente);

            FontRenderContext f = g2.getFontRenderContext();
            double total = fuente.getStringBounds(PARTE1 + " " + PARTE2, f).getWidth();
            double anchoParte2 = fuente.getStringBounds(PARTE2, f).getWidth();
            float x1 = (float) ((getWidth() - total) / 2.0);
            float x2 = (float) (x1 + total - anchoParte2);
            float base = fuente.getLineMetrics(PARTE1, f).getAscent() + 3;

            g2.setColor(Tema.TEXTO);
            g2.drawString(PARTE1, x1, base);
            g2.setColor(Tema.HIELO);
            g2.drawString(PARTE2, x2, base);
            g2.dispose();
        }
    }

    //Linea con estrella
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

    //Estrella fugaz grande: pasa por delante y por detras de las letras del titulo (se dibuja en el glass pane)
    private static class EstrellaFugaz extends JComponent {
        //x, y (fraccion del area de bienvenida), z (-1 detras del texto, 1 delante)
        private static final double[][] RUTA = {
                {-.18, .92, -1}, {.15, .66, -1}, {.45, .50, -1}, {.78, .36, 0}, {1.00, .14, 1},
                {.90, -.12, 1}, {.68, 0, 1}, {.58, .30, 1}, {.40, .62, 1}, {.20, .55, 0},
                {.05, .28, -1}, {.30, .06, -1}, {.60, .46, -1}, {.95, .80, -1}, {1.20, .98, -1}};
        private static final double DURACION = 13.0, CICLO = 14.5;

        private final JComponent area;
        private final JLabel titulo;
        private final List<double[]> cola = new ArrayList<>();     //x, y, z, escala
        private final List<double[]> chispas = new ArrayList<>();  //x, y, vx, vy, vida, z, radio
        private BufferedImage capa;
        private long inicio;
        private double previo;
        private boolean activo, enVuelo;

        EstrellaFugaz(JComponent area, JLabel titulo) {
            this.area = area;
            this.titulo = titulo;
            setOpaque(false);
            new Timer(16, e -> avanzar()).start();
        }

        //Deja pasar los clics a lo que hay debajo
        @Override public boolean contains(int x, int y) { return false; }

        private void avanzar() {
            if (!titulo.isShowing() || area.getWidth() == 0) { activo = false; return; }
            long ahora = System.nanoTime();
            if (!activo) { activo = true; inicio = ahora; previo = 0; cola.clear(); chispas.clear(); }
            double t = ((ahora - inicio) / 1e9) % CICLO;
            if (t < previo) { cola.clear(); chispas.clear(); }
            previo = t;
            double w = area.getWidth(), h = area.getHeight();
            enVuelo = t < DURACION;
            if (enVuelo) {
                double[] p = punto(t / DURACION * (RUTA.length - 1));
                cola.add(new double[]{p[0], p[1], p[2], 1 + 0.3 * p[2]});
                for (int k = 0; k < 2; k++)
                    chispas.add(new double[]{p[0], p[1], (Math.random() - .5) * 1.6 / w,
                            (Math.random() - .2) * 1.6 / h, 1, p[2], Math.random() * 2.2 + .6});
            } else if (!cola.isEmpty()) {
                cola.remove(0);
            }
            while (cola.size() > 130) cola.remove(0);
            for (double[] c : chispas) { c[0] += c[2]; c[1] += c[3]; c[3] += .02 / h; c[4] -= .014; }
            chispas.removeIf(c -> c[4] <= 0);
            repaint();
        }

        //Curva suave (Catmull-Rom) por los puntos de RUTA
        private static double[] punto(double u) {
            int n = RUTA.length, i = Math.min((int) Math.floor(u), n - 2);
            double f = u - i;
            double[] p0 = RUTA[Math.max(i - 1, 0)], p1 = RUTA[i], p2 = RUTA[i + 1], p3 = RUTA[Math.min(i + 2, n - 1)];
            double[] r = new double[3];
            for (int k = 0; k < 3; k++)
                r[k] = 0.5 * ((2 * p1[k]) + (-p0[k] + p2[k]) * f
                        + (2 * p0[k] - 5 * p1[k] + 4 * p2[k] - p3[k]) * f * f
                        + (-p0[k] + 3 * p1[k] - 3 * p2[k] + p3[k]) * f * f * f);
            return r;
        }

        //Silueta exacta de las letras del titulo, en coordenadas de esta capa
        private Shape formaTitulo() {
            Font f = titulo.getFont();
            GlyphVector gv = f.createGlyphVector(new FontRenderContext(null, true, true), titulo.getText());
            FontMetrics fm = titulo.getFontMetrics(f);
            int base = titulo.getBaseline(titulo.getWidth(), titulo.getHeight());
            if (base < 0) base = fm.getAscent() + (titulo.getHeight() - fm.getHeight()) / 2;
            Point p = SwingUtilities.convertPoint(titulo, titulo.getInsets().left, base, this);
            return gv.getOutline(p.x, p.y);
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (!activo || (cola.isEmpty() && chispas.isEmpty())) return;
            int ancho = getWidth(), alto = getHeight();
            if (ancho <= 0 || alto <= 0) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Point o = SwingUtilities.convertPoint(area, 0, 0, this);
            double w = area.getWidth(), h = area.getHeight();
            double r0 = Math.min(w * 0.020, h * 0.05);

            //Capa de atras: se dibuja y luego se "borra" donde hay letras, asi la estrella queda detras del texto
            if (capa == null || capa.getWidth() != ancho || capa.getHeight() != alto)
                capa = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gb = capa.createGraphics();
            gb.setComposite(AlphaComposite.Clear);
            gb.fillRect(0, 0, ancho, alto);
            gb.setComposite(AlphaComposite.SrcOver);
            gb.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            dibujar(gb, false, o, w, h, r0);
            gb.setComposite(AlphaComposite.DstOut);
            Shape texto = formaTitulo();
            gb.setColor(Color.WHITE);
            gb.fill(texto);
            gb.setStroke(new BasicStroke(1.2f));
            gb.draw(texto);
            gb.dispose();
            g2.drawImage(capa, 0, 0, null);

            //Capa de adelante: se dibuja encima del texto
            dibujar(g2, true, o, w, h, r0);
            g2.dispose();
        }

        //Dibuja la parte de la cola, chispas y estrella que esta en la capa pedida
        private void dibujar(Graphics2D c, boolean frente, Point o, double w, double h, double r0) {
            int n = cola.size();
            for (int i = 1; i < n; i++) {
                double[] a = cola.get(i - 1), b = cola.get(i);
                if ((b[2] > 0) != frente) continue;
                double q = (double) i / n;
                Line2D.Double l = new Line2D.Double(o.x + a[0] * w, o.y + a[1] * h, o.x + b[0] * w, o.y + b[1] * h);
                c.setColor(new Color(225, 235, 255, (int) (Math.pow(q, 1.5) * 60)));
                c.setStroke(new BasicStroke((float) (r0 * 2.4 * Math.pow(q, 0.9) * b[3]), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
                c.draw(l);
                c.setColor(new Color(255, 255, 255, (int) (Math.pow(q, 1.3) * 230)));
                c.setStroke(new BasicStroke((float) (r0 * 0.75 * Math.pow(q, 1.1) * b[3]), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                c.draw(l);
            }
            for (double[] s : chispas) {
                if ((s[5] > 0) != frente) continue;
                c.setColor(new Color(255, 255, 255, (int) (Math.max(0, s[4]) * 230)));
                double r = s[6] * s[4];
                c.fill(new Ellipse2D.Double(o.x + s[0] * w - r, o.y + s[1] * h - r, r * 2, r * 2));
            }
            if (enVuelo && n > 0) {
                double[] u = cola.get(n - 1);
                if ((u[2] > 0) == frente)
                    cabeza(c, o.x + u[0] * w, o.y + u[1] * h, r0 * u[3], System.nanoTime() / 9e8);
            }
        }

        //La estrella: brillo, destello de 4 puntas y cuerpo dorado
        private static void cabeza(Graphics2D c, double x, double y, double r, double giro) {
            float rg = (float) (r * 3.4);
            c.setPaint(new RadialGradientPaint((float) x, (float) y, rg, new float[]{0f, .4f, 1f},
                    new Color[]{new Color(255, 255, 255, 130), new Color(215, 228, 255, 40), new Color(215, 228, 255, 0)}));
            c.fill(new Ellipse2D.Double(x - rg, y - rg, rg * 2, rg * 2));

            AffineTransform viejo = c.getTransform();
            c.translate(x, y);
            c.rotate(giro);
            Path2D.Double destello = new Path2D.Double();
            for (int i = 0; i < 8; i++) {
                double ang = i * Math.PI / 4, rr = (i % 2 == 0) ? r * 2.1 : r * 0.22;
                double px = rr * Math.cos(ang), py = rr * Math.sin(ang);
                if (i == 0) destello.moveTo(px, py); else destello.lineTo(px, py);
            }
            destello.closePath();
            c.setColor(new Color(255, 255, 255, 215));
            c.fill(destello);

            Path2D forma = estrella(0, 0, r, r * 0.5);
            c.setPaint(new GradientPaint((float) -r, (float) -r, new Color(0xFFFFFF), (float) r, (float) r, new Color(0xC9D6F2)));
            c.fill(forma);
            c.setStroke(new BasicStroke((float) (r * 0.28), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            c.draw(forma);
            c.setTransform(viejo);
        }
    }

}