import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Vista UNICA del juego de memoria. Sirve para los dos modos (ver setModo):
 *  - CLASICO:  dos jugadores, cada uno con su matriz 6x6 (18 parejas), turno con tiempo.
 *  - PATRONES: un jugador, una matriz 6x6 de casillas que se iluminan, con sus tiempos.
 * SOLO interfaz: no genera parejas ni patrones, no cuenta el tiempo ni decide turnos.
 */
public class VistaTablero extends JFrame {

    public enum Modo { CLASICO, PATRONES }

    public static final int JUGADORES = 2;
    public static final int FILAS = 6;
    public static final int COLUMNAS = 6;

    private Modo modo = Modo.CLASICO;

    private final Carta[][][] cartas = new Carta[JUGADORES][FILAS][COLUMNAS];
    private final JPanel[] panelesJugador = new JPanel[JUGADORES];
    private final JLabel[] lblNombre = new JLabel[JUGADORES];
    private final JLabel[] lblIntentos = new JLabel[JUGADORES];
    private final JLabel[] lblParejas = new JLabel[JUGADORES];

    // Titulos
    private final JLabel lblEyebrow = new JLabel();
    private final JLabel lblTitulo = new JLabel();

    // Modo clasico: turno y reloj
    private final JLabel lblTurno = new JLabel("TURNO");
    private final JLabel lblTiempo = new JLabel("10");
    private final BarraTiempo barra = new BarraTiempo();

    // Modo patrones: nivel, ronda y dos barras de tiempo
    private final JLabel lblNivel = new JLabel("3");
    private final JLabel lblRonda = new JLabel("1");
    private final JLabel lblTotal = new JLabel("12.0 s");
    private final JLabel lblEntre = new JLabel("2.0 s");
    private final BarraTiempo barraTotal = new BarraTiempo();
    private final BarraTiempo barraEntre = new BarraTiempo();

    private final JPanel derecha = new JPanel(new CardLayout());
    private final JPanel tiemposPatrones = new JPanel(new GridLayout(1, 2, 28, 0));
    private final JPanel zona = new JPanel();

    private final JLabel lblMensaje = new JLabel(" ");
    private final JButton btnAccion = new BotonPill("REINICIAR", true);   // Reiniciar / Comenzar
    private final JButton btnSalir = Tema.botonTexto("SALIR");

    public VistaTablero() {
        super("Constela");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Tema fondo = new Tema(140, new Color(0x141F47), Tema.FONDO);
        fondo.setLayout(new BorderLayout(0, 18));
        fondo.setBorder(new EmptyBorder(24, 32, 24, 32));
        setContentPane(fondo);

        for (int j = 0; j < JUGADORES; j++) {
            panelesJugador[j] = crearPanelJugador(j);
        }
        zona.setOpaque(false);

        tiemposPatrones.setOpaque(false);
        tiemposPatrones.add(crearBloqueTiempo("TIEMPO TOTAL", lblTotal, barraTotal));
        tiemposPatrones.add(crearBloqueTiempo("ENTRE CASILLAS", lblEntre, barraEntre));

        JPanel centro = new JPanel(new BorderLayout(0, 16));
        centro.setOpaque(false);
        centro.add(tiemposPatrones, BorderLayout.NORTH);
        centro.add(zona, BorderLayout.CENTER);

        add(crearEncabezado(), BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(crearPie(), BorderLayout.SOUTH);

        setModo(Modo.CLASICO);
    }

    // ---------- Encabezado ----------
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

        // Derecha: una "tarjeta" por modo
        derecha.setOpaque(false);
        derecha.add(crearRelojClasico(), Modo.CLASICO.name());
        derecha.add(crearStatsPatrones(), Modo.PATRONES.name());

        encabezado.add(textos, BorderLayout.WEST);
        encabezado.add(derecha, BorderLayout.EAST);
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

    private JPanel crearStatsPatrones() {
        JPanel stats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        stats.setOpaque(false);
        stats.add(crearStat("CASILLAS", lblNivel));
        stats.add(crearStat("RONDA", lblRonda));
        return stats;
    }

    private JPanel crearStat(String titulo, JLabel valor) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setBackground(Tema.SUPERFICIE);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Tema.LINEA, 1, true), new EmptyBorder(8, 18, 8, 18)));

        JLabel t = new JLabel(titulo, SwingConstants.CENTER);
        t.setFont(Tema.sans(Font.BOLD, 9));
        t.setForeground(Tema.SECUNDARIO);

        valor.setHorizontalAlignment(SwingConstants.CENTER);
        valor.setFont(Tema.serif(Font.PLAIN, 28));
        valor.setForeground(Tema.TEXTO);

        p.add(t, BorderLayout.NORTH);
        p.add(valor, BorderLayout.CENTER);
        return p;
    }

    private JPanel crearBloqueTiempo(String titulo, JLabel valor, BarraTiempo barraTiempo) {
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
        bloque.add(barraTiempo, BorderLayout.CENTER);
        return bloque;
    }

    // ---------- Panel de un jugador (nombre + matriz 6x6) ----------
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

    // ---------- Pie ----------
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

    // ---------- Cambio de modo (solo muestra/oculta piezas) ----------
    public void setModo(Modo nuevoModo) {
        this.modo = nuevoModo;
        boolean clasico = nuevoModo == Modo.CLASICO;
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();

        zona.removeAll();
        if (clasico) {
            zona.setLayout(new GridLayout(1, JUGADORES, 24, 0));
            for (JPanel p : panelesJugador) {
                p.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
                zona.add(p);
            }
        } else {
            zona.setLayout(new BoxLayout(zona, BoxLayout.X_AXIS));
            panelesJugador[0].setMaximumSize(new Dimension(680, Short.MAX_VALUE));
            zona.add(Box.createHorizontalGlue());
            zona.add(panelesJugador[0]);
            zona.add(Box.createHorizontalGlue());
        }
        for (int j = 0; j < JUGADORES; j++) {
            lblIntentos[j].setVisible(clasico);
            lblParejas[j].setVisible(clasico);
            marcarPanel(panelesJugador[j], Tema.LINEA);
        }
        tiemposPatrones.setVisible(!clasico);
        ((CardLayout) derecha.getLayout()).show(derecha, nuevoModo.name());

        lblEyebrow.setText(clasico ? "MODO CLÁSICO  ·  2 JUGADORES" : "MODO PATRONES  ·  1 JUGADOR");
        lblTitulo.setText(clasico
                ? "<html>Encuentra las <i style='color:#A9BCFF'>constelaciones</i></html>"
                : "<html>Sigue el <i style='color:#A9BCFF'>patrón</i></html>");
        btnAccion.setText(clasico ? "REINICIAR" : "COMENZAR");
        setTitle(clasico ? "Constela - Modo clásico" : "Constela - Modo patrones");

        if (clasico) {
            setMinimumSize(new Dimension(1000, 640));
            setSize(Math.min(1280, pantalla.width - 60), Math.min(820, pantalla.height - 80));
        } else {
            setMinimumSize(new Dimension(700, 640));
            setSize(Math.min(860, pantalla.width - 60), Math.min(900, pantalla.height - 80));
        }
        setLocationRelativeTo(null);
        zona.revalidate();
        zona.repaint();
    }

    public Modo getModo() { return modo; }

    // ---------- API para el controlador (ambos modos) ----------
    /** En modo patrones se usa solo jugador 0. */
    public Carta getCarta(int jugador, int fila, int columna) { return cartas[jugador][fila][columna]; }
    public Carta[][] getCartas(int jugador)                   { return cartas[jugador]; }
    public JButton getBtnAccion()                             { return btnAccion; }  // Reiniciar / Comenzar
    public JButton getBtnSalir()                              { return btnSalir; }

    public void setNombreJugador(int jugador, String nombre)  { lblNombre[jugador].setText(nombre); }
    public void setMensaje(String mensaje)                    { lblMensaje.setText(mensaje); }

    // ---------- API modo CLASICO ----------
    public void setIntentos(int jugador, int intentos)        { lblIntentos[jugador].setText("INTENTOS  " + intentos); }
    public void setParejas(int jugador, int encontradas, int total) {
        lblParejas[jugador].setText("PAREJAS  " + encontradas + "/" + total);
    }

    /** Resalta el panel del jugador al que le toca (solo cambia el aspecto). */
    public void setJugadorActivo(int jugador) {
        for (int j = 0; j < JUGADORES; j++) {
            marcarPanel(panelesJugador[j], j == jugador ? Tema.ESTRELLA : Tema.LINEA);
        }
        lblTurno.setText("TURNO DE  " + lblNombre[jugador].getText().toUpperCase());
    }

    /** Segundos restantes del turno; 'maximo' sirve para dibujar la barra. */
    public void setTiempoTurno(int restante, int maximo) {
        lblTiempo.setText(String.format("%02d", Math.max(0, restante)));
        barra.setFraccion(maximo <= 0 ? 0 : (double) restante / maximo);
    }

    // ---------- API modo PATRONES ----------
    public void setNivel(int casillasDelPatron) { lblNivel.setText(String.valueOf(casillasDelPatron)); }
    public void setRonda(int ronda)             { lblRonda.setText(String.valueOf(ronda)); }

    /** Tiempo total para replicar el patron (segundos). */
    public void setTiempoTotal(double restante, double maximo) {
        lblTotal.setText(String.format("%.1f s", Math.max(0, restante)));
        barraTotal.setFraccion(maximo <= 0 ? 0 : restante / maximo);
    }

    /** Tiempo para pulsar la siguiente casilla (segundos). */
    public void setTiempoCasilla(double restante, double maximo) {
        lblEntre.setText(String.format("%.1f s", Math.max(0, restante)));
        barraEntre.setFraccion(maximo <= 0 ? 0 : restante / maximo);
    }

    // ---------- Barra de tiempo (clase interna, solo visual; tambien la usa VistaPatrones) ----------
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