import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Vista del tablero de memoria (matriz 6x6 = 36 cartas = 18 parejas).
 * SOLO interfaz: no sabe que carta es pareja de cual ni cuenta aciertos.
 */
public class VistaTablero extends JFrame {

    public static final int FILAS = 6;
    public static final int COLUMNAS = 6;

    private final Carta[][] cartas = new Carta[FILAS][COLUMNAS];

    private final JLabel lblJugador = new JLabel("OBSERVADOR");
    private final JLabel lblTiempo = new JLabel("00:00");
    private final JLabel lblIntentos = new JLabel("0");
    private final JLabel lblParejas = new JLabel("0/18");
    private final JLabel lblMensaje = new JLabel(" ");
    private final JButton btnReiniciar = new BotonPill("REINICIAR", true);
    private final JButton btnSalir = Tema.botonTexto("SALIR");

    public VistaTablero() {
        super("Constela - Tablero");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Tema fondo = new Tema(120, new Color(0x141F47), Tema.FONDO);
        fondo.setLayout(new BorderLayout(0, 20));
        fondo.setBorder(new EmptyBorder(26, 36, 26, 36));
        setContentPane(fondo);

        add(crearEncabezado(), BorderLayout.NORTH);
        add(crearTablero(), BorderLayout.CENTER);
        add(crearPie(), BorderLayout.SOUTH);

        int alto = Math.min(880, Toolkit.getDefaultToolkit().getScreenSize().height - 80);
        setSize(860, alto);
        setMinimumSize(new Dimension(700, 640));
        setLocationRelativeTo(null);
    }

    // ---------- Encabezado: titulo + estadisticas ----------
    private JPanel crearEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);

        lblJugador.setFont(Tema.sans(Font.BOLD, 11));
        lblJugador.setForeground(Tema.HIELO);
        JLabel titulo = new JLabel("<html>Encuentra las <i style='color:#A9BCFF'>constelaciones</i></html>");
        titulo.setFont(Tema.serif(Font.PLAIN, 38));
        titulo.setForeground(Tema.TEXTO);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        lblJugador.setAlignmentX(Component.LEFT_ALIGNMENT);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        textos.add(lblJugador);
        textos.add(Box.createVerticalStrut(6));
        textos.add(titulo);

        JPanel stats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        stats.setOpaque(false);
        stats.add(crearStat("TIEMPO", lblTiempo));
        stats.add(crearStat("INTENTOS", lblIntentos));
        stats.add(crearStat("PAREJAS", lblParejas));

        encabezado.add(textos, BorderLayout.WEST);
        encabezado.add(stats, BorderLayout.EAST);
        return encabezado;
    }

    private JPanel crearStat(String titulo, JLabel valor) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setBackground(Tema.SUPERFICIE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Tema.LINEA, 1, true), new EmptyBorder(8, 16, 8, 16)));

        JLabel t = new JLabel(titulo, SwingConstants.CENTER);
        t.setFont(Tema.sans(Font.BOLD, 9));
        t.setForeground(Tema.SECUNDARIO);

        valor.setHorizontalAlignment(SwingConstants.CENTER);
        valor.setFont(Tema.serif(Font.PLAIN, 26));
        valor.setForeground(Tema.TEXTO);

        p.add(t, BorderLayout.NORTH);
        p.add(valor, BorderLayout.CENTER);
        return p;
    }

    // ---------- Tablero 6x6 ----------
    private JPanel crearTablero() {
        JPanel tablero = new JPanel(new GridLayout(FILAS, COLUMNAS, 10, 10));
        tablero.setOpaque(false);
        for (int fila = 0; fila < FILAS; fila++) {
            for (int columna = 0; columna < COLUMNAS; columna++) {
                Carta carta = new Carta();
                cartas[fila][columna] = carta;
                tablero.add(carta);
            }
        }
        return tablero;
    }

    // ---------- Pie: mensajes + botones ----------
    private JPanel crearPie() {
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);

        lblMensaje.setFont(Tema.sans(Font.PLAIN, 14));
        lblMensaje.setForeground(Tema.SECUNDARIO);

        btnReiniciar.setPreferredSize(new Dimension(170, 42));
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
        botones.setOpaque(false);
        botones.add(btnSalir);
        botones.add(btnReiniciar);

        pie.add(lblMensaje, BorderLayout.WEST);
        pie.add(botones, BorderLayout.EAST);
        return pie;
    }

    // ---------- API para el controlador ----------
    public Carta getCarta(int fila, int columna) { return cartas[fila][columna]; }
    public Carta[][] getCartas()                 { return cartas; }
    public JButton getBtnReiniciar()                  { return btnReiniciar; }
    public JButton getBtnSalir()                      { return btnSalir; }

    public void setJugador(String nombre)             { lblJugador.setText("OBSERVADOR  ·  " + nombre.toUpperCase()); }
    public void setTiempo(String texto)               { lblTiempo.setText(texto); }
    public void setIntentos(int intentos)             { lblIntentos.setText(String.valueOf(intentos)); }
    public void setParejas(int encontradas, int total){ lblParejas.setText(encontradas + "/" + total); }
    public void setMensaje(String mensaje)            { lblMensaje.setText(mensaje); }

    // ---------- Boton redondeado (clase interna, solo visual) ----------
    /** Boton tipo pastilla. relleno=true: solido; false: solo contorno. */
    public static class BotonPill extends JButton {

        private final boolean relleno;

        public BotonPill(String texto, boolean relleno) {
            super(texto);
            this.relleno = relleno;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(relleno ? new Color(0x0A1030) : Tema.TEXTO);
            setFont(Tema.sans(Font.BOLD, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(220, 46));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            boolean hover = getModel().isRollover();
            int w = getWidth() - 1, h = getHeight() - 1;

            if (relleno) {
                g2.setColor(hover ? new Color(0xC4D1FF) : Tema.HIELO);
            } else {
                g2.setColor(new Color(169, 188, 255, hover ? 46 : 15));
            }
            g2.fillRoundRect(0, 0, w, h, h, h);
            g2.setColor(Tema.HIELO);
            g2.drawRoundRect(0, 0, w, h, h, h);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}