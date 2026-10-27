import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.util.Map;

public class VistaTablero extends JFrame {

    public static final int JUGADORES = 2;
    public static final int FILAS = 6;
    public static final int COLUMNAS = 6;


    private final Carta[][][] cartas = new Carta[JUGADORES][FILAS][COLUMNAS];
    private final JPanel[] panelesJugador = new JPanel[JUGADORES];
    private final JLabel[] lblNombre = new JLabel[JUGADORES];
    private final JLabel[] lblIntentos = new JLabel[JUGADORES];
    private final JLabel[] lblParejas = new JLabel[JUGADORES];

    private final JLabel lblEyebrow = new JLabel();
    private final JLabel lblTitulo = new JLabel();
    private final JLabel lblTurno = new JLabel("TURNO");
    private final JLabel lblTiempo = new JLabel("10");
    private final BarraTiempo barra = new BarraTiempo();

    private final JPanel zona = new JPanel();

    private final JLabel lblMensaje = new JLabel(" ");
    private final JButton btnAccion = new BotonPill("REINICIAR", true);
    private final JButton btnSalir = Tema.botonTexto("SALIR");

    public VistaTablero() {
        super("AstroMatch");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Tema fondo = new Tema(140, new Color(0x141F47), Tema.FONDO);
        fondo.setLayout(new BorderLayout(0, 18));
        fondo.setBorder(new EmptyBorder(24, 32, 24, 32));
        setContentPane(fondo);

        for (int j = 0; j < JUGADORES; j++) {
            panelesJugador[j] = crearPanelJugador(j);
        }
        zona.setOpaque(false);


        JPanel centro = new JPanel(new BorderLayout(0, 16));
        centro.setOpaque(false);
        centro.add(zona, BorderLayout.CENTER);

        add(crearEncabezado(), BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(crearPie(), BorderLayout.SOUTH);

        zona.setLayout(new GridLayout(1, JUGADORES, 24, 0));
        for (JPanel panel : panelesJugador) zona.add(panel);
        lblEyebrow.setText("MODO CLÁSICO  ·  2 JUGADORES");
        lblTitulo.setText("<html>Encuentra las <i style='color:#A9BCFF'>constelaciones</i></html>");
        setTitle("AstroMatch - Modo clásico");
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        setMinimumSize(new Dimension(1000, 640));
        setSize(Math.min(1280, pantalla.width - 60), Math.min(820, pantalla.height - 80));
        setLocationRelativeTo(null);
        setJugadorActivo(0);
    }

    private JPanel crearEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);

        lblEyebrow.setFont(Tema.sans(Font.BOLD, 11));
        lblEyebrow.setForeground(Tema.HIELO);
        lblTitulo.setFont(Tema.serif(Font.PLAIN, 36));
        lblTitulo.setForeground(Tema.TEXTO);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        lblEyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        textos.add(lblEyebrow);
        textos.add(Box.createVerticalStrut(6));
        textos.add(lblTitulo);

        encabezado.add(textos, BorderLayout.WEST);
        encabezado.add(crearRelojClasico(), BorderLayout.EAST);
        return encabezado;
    }

    private JPanel crearRelojClasico() {
        lblTurno.setFont(Tema.sans(Font.BOLD, 11));
        lblTurno.setForeground(Tema.ESTRELLA);
        barra.setPreferredSize(new Dimension(260, 8));
        barra.setMaximumSize(new Dimension(260, 8));
        lblTurno.setAlignmentX(Component.LEFT_ALIGNMENT);
        barra.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel turno = new JPanel();
        turno.setOpaque(false);
        turno.setLayout(new BoxLayout(turno, BoxLayout.Y_AXIS));
        turno.add(lblTurno);
        turno.add(Box.createVerticalStrut(10));
        turno.add(barra);

        lblTiempo.setFont(Tema.serif(Font.PLAIN, 44));
        lblTiempo.setForeground(Tema.TEXTO);

        JPanel reloj = new JPanel(new BorderLayout(18, 0));
        reloj.setOpaque(false);
        reloj.add(turno, BorderLayout.WEST);
        reloj.add(lblTiempo, BorderLayout.EAST);
        return reloj;
    }

    private JPanel crearPanelJugador(int j) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setOpaque(false);
        marcarPanel(panel, Tema.LINEA);

        lblNombre[j] = new JLabel("Jugador " + (j + 1));
        lblNombre[j].setFont(Tema.serif(Font.PLAIN, 24));
        lblNombre[j].setForeground(Tema.TEXTO);

        lblIntentos[j] = etiquetaDato("INTENTOS  0");
        lblParejas[j] = etiquetaDato("PAREJAS  0/18");
        JPanel datos = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        datos.setOpaque(false);
        datos.add(lblIntentos[j]);
        datos.add(lblParejas[j]);

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        cabecera.add(lblNombre[j], BorderLayout.WEST);
        cabecera.add(datos, BorderLayout.EAST);

        JPanel matriz = new JPanel(new GridLayout(FILAS, COLUMNAS, 6, 6));
        matriz.setOpaque(false);
        for (int f = 0; f < FILAS; f++) {
            for (int c = 0; c < COLUMNAS; c++) {
                Carta carta = new Carta();
                cartas[j][f][c] = carta;
                matriz.add(carta);
            }
        }

        panel.add(cabecera, BorderLayout.NORTH);
        panel.add(matriz, BorderLayout.CENTER);
        return panel;
    }

    private static JLabel etiquetaDato(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(Tema.sans(Font.BOLD, 11));
        l.setForeground(Tema.SECUNDARIO);
        return l;
    }

    private static void marcarPanel(JPanel panel, Color color) {
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(color, 1, true), new EmptyBorder(14, 14, 14, 14)));
    }

    private JPanel crearPie() {
        JPanel pie = new JPanel(new BorderLayout());
        pie.setOpaque(false);

        lblMensaje.setFont(Tema.sans(Font.PLAIN, 14));
        lblMensaje.setForeground(Tema.SECUNDARIO);

        btnAccion.setPreferredSize(new Dimension(170, 42));
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 18, 0));
        botones.setOpaque(false);
        botones.add(btnSalir);
        botones.add(btnAccion);

        pie.add(lblMensaje, BorderLayout.WEST);
        pie.add(botones, BorderLayout.EAST);
        return pie;
    }

    // ---------- API para el controlador del modo clásico ----------
    public Carta getCarta(int jugador, int fila, int columna) { return cartas[jugador][fila][columna]; }
    public Carta[][] getCartas(int jugador)                   { return cartas[jugador]; }
    public JButton getBtnAccion()                             { return btnAccion; }  // Reiniciar / Comenzar
    public JButton getBtnSalir()                              { return btnSalir; }

    public void setNombreJugador(int jugador, String nombre)  { lblNombre[jugador].setText(nombre); }
    public void setMensaje(String mensaje)                    { lblMensaje.setText(mensaje); }

    public void setIntentos(int jugador, int intentos)        { lblIntentos[jugador].setText("INTENTOS  " + intentos); }
    public void setParejas(int jugador, int encontradas, int total) {
        lblParejas[jugador].setText("PAREJAS  " + encontradas + "/" + total);
    }

    public void setJugadorActivo(int jugador) {
        for (int j = 0; j < JUGADORES; j++) {
            marcarPanel(panelesJugador[j], j == jugador ? Tema.ESTRELLA : Tema.LINEA);
        }
        lblTurno.setText("TURNO DE  " + lblNombre[jugador].getText().toUpperCase());
    }

    public void setTiempoTurno(int restante, int maximo) {
        lblTiempo.setText(String.format("%02d", Math.max(0, restante)));
        barra.setFraccion(maximo <= 0 ? 0 : (double) restante / maximo);
    }

    public static class BarraTiempo extends JComponent {

        private double fraccion = 1.0;

        public BarraTiempo() {
            setOpaque(false);
            setPreferredSize(new Dimension(200, 8));
        }

        public void setFraccion(double fraccion) {
            this.fraccion = Math.max(0, Math.min(1, fraccion));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            g2.setColor(Tema.LINEA);
            g2.fillRoundRect(0, 0, w, h, h, h);
            g2.setColor(fraccion < 0.25 ? Tema.ERROR : Tema.HIELO);
            g2.fillRoundRect(0, 0, (int) (w * fraccion), h, h, h);
            g2.dispose();
        }
    }


    public static class BotonPill extends JButton {

        private final boolean relleno;
        private final int margen;

        public BotonPill(String texto, boolean relleno) {
            this(texto, relleno, false);
        }

        public BotonPill(String texto, boolean relleno, boolean brillo) {
            super(texto);
            this.relleno = relleno;
            this.margen = brillo ? 16 : 0;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setForeground(relleno ? new Color(0x0A1030) : Tema.TEXTO);
            setFont(Tema.sans(Font.BOLD, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(220, (brillo ? 52 : 46) + 2 * margen));
        }

        public int getAltoTotal() { return getPreferredSize().height; }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            boolean hover = getModel().isRollover();

            int x = 0;
            int y = margen - (hover && margen > 0 ? 1 : 0);
            int w = getWidth() - 1;
            int h = getHeight() - 2 * margen - 1;

            if (relleno) {
                if (margen > 0) {
                    for (int i = 10; i >= 1; i--) {
                        int recorte = (int) (i * 1.4);
                        g2.setColor(new Color(169, 188, 255, hover ? 6 : 4));
                        g2.fillRoundRect(x + recorte, y - i + 5, w - 2 * recorte, h + 2 * i, h + 2 * i, h + 2 * i);
                    }
                }
                g2.setPaint(new GradientPaint(0, y,
                        hover ? new Color(0xD6E0FF) : new Color(0xC9D6FF), 0, y + h, Tema.HIELO));
                g2.fillRoundRect(x, y, w, h, h, h);
            } else {
                g2.setColor(new Color(169, 188, 255, hover ? 46 : 15));
                g2.fillRoundRect(x, y, w, h, h, h);
                g2.setColor(Tema.HIELO);
                g2.drawRoundRect(x, y, w, h, h, h);
            }

            String texto = getText();
            if (texto != null && !texto.isEmpty()) {
                Font fuente = getFont().deriveFont(Map.of(TextAttribute.TRACKING, 0.2f));
                TextLayout tl = new TextLayout(texto, fuente, g2.getFontRenderContext());
                float tx = x + (w - tl.getAdvance()) / 2f;
                float ty = y + (h + tl.getAscent() - tl.getDescent()) / 2f;
                g2.setColor(getForeground());
                tl.draw(g2, tx, ty);
            }
            g2.dispose();
        }
    }
}