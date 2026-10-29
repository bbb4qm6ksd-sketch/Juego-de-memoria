import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;


public class VistaPatrones extends JFrame {

    public static final int FILAS = 6;
    public static final int COLUMNAS = 6;

    private final Casilla[][] casillas = new Casilla[FILAS][COLUMNAS];

    private final JLabel lblJugador = new JLabel("OBSERVADOR");
    private final JLabel lblNivel = new JLabel("3");
    private final JLabel lblRonda = new JLabel("1");
    private final JLabel lblTotal = new JLabel("12.0 s");
    private final JLabel lblEntre = new JLabel("2.0 s");
    private final VistaTablero.BarraTiempo barraTotal = new VistaTablero.BarraTiempo();
    private final VistaTablero.BarraTiempo barraEntre = new VistaTablero.BarraTiempo();
    private final JLabel lblFase = new JLabel("Pulsa COMENZAR para ver el patrón");
    private final JButton btnComenzar = new VistaTablero.BotonPill("COMENZAR", true);
    private final JButton btnSalir = Tema.botonTexto("SALIR");

    public VistaPatrones() {
        super("AstroMatch - Modo patrones");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Tema fondo = new Tema(150, new Color(0x141F47), Tema.FONDO);
        fondo.setAnimado(true);
        fondo.setLayout(new BorderLayout(0, 20));
        fondo.setBorder(new EmptyBorder(30, 52, 26, 52));
        setContentPane(fondo);

        JPanel centro = new JPanel(new BorderLayout(0, 20));
        centro.setOpaque(false);
        centro.add(crearTitulo(), BorderLayout.NORTH);
        JPanel resto = new JPanel(new BorderLayout(0, 18));
        resto.setOpaque(false);
        resto.add(crearTiempos(), BorderLayout.NORTH);
        resto.add(crearMatriz(), BorderLayout.CENTER);
        centro.add(resto, BorderLayout.CENTER);

        add(crearEncabezado(), BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(crearPie(), BorderLayout.SOUTH);

        int alto = Math.min(900, Toolkit.getDefaultToolkit().getScreenSize().height - 80);
        setSize(980, alto);
        setMinimumSize(new Dimension(760, 680));
        setLocationRelativeTo(null);
    }

    // ---------- Barra superior: marca + jugador + salir ----------
    private JPanel crearEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);

        JLabel marca = new JLabel("✦  AstroMatch");
        marca.setFont(Tema.sans(Font.BOLD, 14));
        marca.setForeground(Tema.TEXTO);

        lblJugador.setFont(Tema.sans(Font.BOLD, 10));
        lblJugador.setForeground(Tema.SECUNDARIO);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
        derecha.setOpaque(false);
        derecha.add(lblJugador);
        derecha.add(btnSalir);

        encabezado.add(marca, BorderLayout.WEST);
        encabezado.add(derecha, BorderLayout.EAST);
        return encabezado;
    }

    // ---------- Titulo + nivel y ronda ----------
    private JPanel crearTitulo() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel eyebrow = new JLabel("MODO PATRONES  ·  UN JUGADOR");
        eyebrow.setFont(Tema.sans(Font.BOLD, 11));
        eyebrow.setForeground(Tema.HIELO);
        JLabel titulo = new JLabel("<html>Sigue el <i style='color:#A9BCFF'>patrón</i></html>");
        titulo.setFont(Tema.serif(Font.PLAIN, 46));
        titulo.setForeground(Tema.TEXTO);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        eyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        textos.add(eyebrow);
        textos.add(Box.createVerticalStrut(8));
        textos.add(titulo);

        JPanel stats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        stats.setOpaque(false);
        stats.add(crearStat("CASILLAS", lblNivel));
        stats.add(crearStat("RONDA", lblRonda));

        p.add(textos, BorderLayout.WEST);
        p.add(stats, BorderLayout.EAST);
        return p;
    }

    private JPanel crearStat(String titulo, JLabel valor) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setBackground(Tema.SUPERFICIE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Tema.LINEA, 1, true), new EmptyBorder(8, 20, 8, 20)));

        JLabel t = new JLabel(titulo, SwingConstants.CENTER);
        t.setFont(Tema.sans(Font.BOLD, 9));
        t.setForeground(Tema.SECUNDARIO);

        valor.setHorizontalAlignment(SwingConstants.CENTER);
        valor.setFont(Tema.serif(Font.PLAIN, 30));
        valor.setForeground(Tema.TEXTO);

        p.add(t, BorderLayout.NORTH);
        p.add(valor, BorderLayout.CENTER);
        return p;
    }

    // ---------- Dos barras: tiempo total (12 s) y tiempo entre casillas (2 s) ----------
    private JPanel crearTiempos() {
        JPanel tiempos = new JPanel(new GridLayout(1, 2, 28, 0));
        tiempos.setOpaque(false);
        tiempos.add(crearBloqueTiempo("TIEMPO TOTAL", lblTotal, barraTotal));
        tiempos.add(crearBloqueTiempo("ENTRE CASILLAS", lblEntre, barraEntre));
        return tiempos;
    }

    private JPanel crearBloqueTiempo(String titulo, JLabel valor, VistaTablero.BarraTiempo barra) {
        JLabel t = new JLabel(titulo);
        t.setFont(Tema.sans(Font.BOLD, 10));
        t.setForeground(Tema.SECUNDARIO);
        valor.setFont(Tema.sans(Font.BOLD, 12));
        valor.setForeground(Tema.TEXTO);

        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.add(t, BorderLayout.WEST);
        fila.add(valor, BorderLayout.EAST);

        JPanel bloque = new JPanel(new BorderLayout(0, 8));
        bloque.setOpaque(false);
        bloque.add(fila, BorderLayout.NORTH);
        bloque.add(barra, BorderLayout.CENTER);
        return bloque;
    }

    // ---------- Matriz 6x6, siempre cuadrada y centrada ----------
    private JPanel crearMatriz() {
        JPanel tablero = new JPanel(new GridLayout(FILAS, COLUMNAS, 10, 10));
        tablero.setOpaque(false);
        for (int f = 0; f < FILAS; f++) {
            for (int c = 0; c < COLUMNAS; c++) {
                Casilla casilla = new Casilla();
                casillas[f][c] = casilla;
                tablero.add(casilla);
            }
        }
        JPanel contenedor = new JPanel(null) {
            @Override public void doLayout() {
                int lado = Math.min(getWidth(), getHeight());
                tablero.setBounds((getWidth() - lado) / 2, (getHeight() - lado) / 2, lado, lado);
            }
        };
        contenedor.setOpaque(false);
        contenedor.add(tablero);
        return contenedor;
    }

    // ---------- Pie: fase actual + comenzar ----------
    private JPanel crearPie() {
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);

        lblFase.setFont(Tema.serif(Font.ITALIC, 22));
        lblFase.setForeground(Tema.ESTRELLA);

        btnComenzar.setPreferredSize(new Dimension(180, 44));
        pie.add(lblFase, BorderLayout.WEST);
        pie.add(btnComenzar, BorderLayout.EAST);
        return pie;
    }

    // ---------- API para el controlador (solo muestra datos, no decide nada) ----------
    public Casilla getCasilla(int fila, int columna) { return casillas[fila][columna]; }
    public Casilla[][] getCasillas()                 { return casillas; }
    public JButton getBtnComenzar()                  { return btnComenzar; }
    public JButton getBtnSalir()                     { return btnSalir; }

    public void setJugador(String nombre)            { lblJugador.setText("OBSERVADOR  ·  " + nombre.toUpperCase()); }
    public void setNivel(int casillasDelPatron)      { lblNivel.setText(String.valueOf(casillasDelPatron)); }
    public void setRonda(int ronda)                  { lblRonda.setText(String.valueOf(ronda)); }
    public void setFase(String texto)                { lblFase.setText(texto); }

    /** Tiempo total para repetir el patron (segundos). */
    public void setTiempoTotal(double restante, double maximo) {
        lblTotal.setText(String.format("%.1f s", Math.max(0, restante)));
        barraTotal.setFraccion(maximo <= 0 ? 0 : restante / maximo);
    }

    /** Tiempo para pulsar la siguiente casilla (segundos). */
    public void setTiempoCasilla(double restante, double maximo) {
        lblEntre.setText(String.format("%.1f s", Math.max(0, restante)));
        barraEntre.setFraccion(maximo <= 0 ? 0 : restante / maximo);
    }

    /** Deja todas las casillas en reposo y sin numero. */
    public void limpiarCasillas() {
        for (Casilla[] fila : casillas) {
            for (Casilla c : fila) {
                c.setEstado(Casilla.Estado.REPOSO);
                c.setNumero(0);
            }
        }
    }

    // ---------- Casilla del modo patrones (clase interna, solo se dibuja) ----------
    public static class Casilla extends JButton {

        /** REPOSO: normal | ILUMINADA: parte del patron | ACIERTO: pulsada bien | ERROR: pulsada mal */
        public enum Estado { REPOSO, ILUMINADA, ACIERTO, ERROR }

        private Estado estado = Estado.REPOSO;
        private int numero = 0;   // orden dentro del patron (0 = no mostrar numero)

        public Casilla() {
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        public void setEstado(Estado estado) { this.estado = estado; repaint(); }
        public Estado getEstado()            { return estado; }
        public void setNumero(int numero)    { this.numero = numero; repaint(); }

        private static Color alfa(Color c, int a) {
            return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            Color c1, c2, luz;
            switch (estado) {
                case ILUMINADA: c1 = new Color(0xF6DCA2); c2 = new Color(0xC9A572); luz = Tema.ESTRELLA; break;
                case ACIERTO:   c1 = new Color(0xC9D6FF); c2 = Tema.HIELO;          luz = Tema.HIELO;    break;
                case ERROR:     c1 = new Color(0x6B2A2A); c2 = new Color(0x2E1112); luz = Tema.ERROR;    break;
                default:        c1 = new Color(0x111A33); c2 = new Color(0x0A0F20); luz = Tema.LINEA;    break;
            }
            RoundRectangle2D forma = new RoundRectangle2D.Float(1, 1, w - 3, h - 3, 18, 18);
            g2.setPaint(new GradientPaint(0, 0, c1, w, h, c2));
            g2.fill(forma);

            boolean brilla = estado != Estado.REPOSO;
            if (brilla) {                                          // resplandor
                for (int i = 3; i >= 1; i--) {
                    g2.setColor(alfa(luz, 30 / i));
                    g2.setStroke(new BasicStroke(1 + i * 2.5f));
                    g2.draw(forma);
                }
            }
            g2.setColor(brilla ? luz : (getModel().isRollover() ? Tema.HIELO : Tema.LINEA));
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(forma);

            if (estado == Estado.REPOSO) {                         // puntito de estrella
                float cx = w / 2f, cy = h / 2f;
                g2.setPaint(new RadialGradientPaint(new Point2D.Float(cx, cy), 10f,
                        new float[]{0f, 1f}, new Color[]{alfa(Tema.HIELO, 90), alfa(Tema.HIELO, 0)}));
                g2.fillOval((int) (cx - 10), (int) (cy - 10), 20, 20);
                g2.setColor(alfa(Tema.HIELO, 200));
                g2.fillOval((int) (cx - 2), (int) (cy - 2), 4, 4);
            }
            if (numero > 0) {                                      // orden en el patron
                g2.setFont(Tema.serif(Font.BOLD, Math.max(14, h / 2)));
                FontMetrics fm = g2.getFontMetrics();
                String t = String.valueOf(numero);
                g2.setColor(estado == Estado.ERROR ? Tema.TEXTO : new Color(0x0A1030));
                g2.drawString(t, (w - fm.stringWidth(t)) / 2, (h + fm.getAscent() - fm.getDescent()) / 2);
            }
            g2.dispose();
        }
    }
}