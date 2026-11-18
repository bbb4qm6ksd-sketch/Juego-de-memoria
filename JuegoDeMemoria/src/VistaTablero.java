import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.util.Map;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Window;



/**
 * Ventana visual del modo clásico. Presenta el selector de dificultad,
 * los tableros de los jugadores, el tiempo y los marcadores; no decide las reglas.
 */
public class VistaTablero extends JFrame {

    public static final int JUGADORES = 2;
    public static final int FILAS = 6;
    public static final int COLUMNAS = 6;


    private final Carta[][][] cartas = new Carta[JUGADORES][FILAS][COLUMNAS];
    private final JPanel[] panelesJugador = new JPanel[JUGADORES];
    private final JPanel[] matricesJugador = new JPanel[JUGADORES];
    private final JLabel[] lblNombre = new JLabel[JUGADORES];
    private final JLabel[] lblIntentos = new JLabel[JUGADORES];
    private final JLabel[] lblParejas = new JLabel[JUGADORES];

    private final JLabel lblEyebrow = new JLabel();
    private final JLabel lblTitulo = new JLabel();
    private final JLabel lblTurno = new JLabel("TURNO");
    private final JLabel lblTiempo = new JLabel("10");
    private final BarraTiempo barra = new BarraTiempo();
    private JPanel panelReloj;

    private final JPanel zona = new JPanel(new CardLayout());
    private final JPanel pantallas = new JPanel(new CardLayout());
    private final JButton btnFacil = new BotonPill("FÁCIL", true);
    private final JButton btnMedio = new BotonPill("MEDIO", true);
    private final JButton btnDificil = new BotonPill("DIFÍCIL", true);

    private final JLabel lblMensaje = new JLabel(" ");
    private final JButton btnAccion = new BotonPill("REINICIAR", true);
    private final JButton btnSalir = Tema.botonTexto("SALIR");

    // ---------- Confeti ----------
    private JComponent superficie;
    private Confeti confeti;
    private long ultimoConfetiNs;
    private final Timer timerConfeti = new Timer(16, e -> {
        long ahora = System.nanoTime();
        double dt = (ahora - ultimoConfetiNs) / 1e9;
        ultimoConfetiNs = ahora;
        if (confeti != null) {
            confeti.actualizar(dt);
            if (!confeti.estaActivo()) {
                ((Timer) e.getSource()).stop();
            }
        }
        if (superficie != null) superficie.repaint();
    });

    /** Inicia la animación de confeti sobre la ventana al terminar una partida. */
    public void lanzarConfeti() {
        int ancho = superficie == null ? getWidth() : superficie.getWidth();
        int alto = superficie == null ? getHeight() : superficie.getHeight();
        confeti = new Confeti(Math.max(1, ancho), Math.max(1, alto));
        confeti.iniciar();
        ultimoConfetiNs = System.nanoTime();
        timerConfeti.start();
        if (superficie != null) superficie.repaint();
    }

    /** Abre un diálogo temático asociado a esta ventana. */
    public void mostrarMensajeFinal(String titulo, String detalle) {
        Window ventana = SwingUtilities.getWindowAncestor(getContentPane());
        DialogoAstro.informar(ventana == null ? this : ventana, titulo, detalle);
    }

    /** Detiene y limpia la animación de confeti antes de salir o reiniciar. */
    public void detenerConfeti() {
        timerConfeti.stop();
        confeti = null;
        if (superficie != null) superficie.repaint();
    }

    /** Construye la ventana con selector de nivel y componentes para los dos tableros. */
    public VistaTablero() {
        super("AstroMatch");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Tema fondo = new Tema(140, new Color(0x141F47), Tema.FONDO) {
            @Override
            protected void paintChildren(Graphics g) {
                super.paintChildren(g);
                if (confeti != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    confeti.dibujar(g2);
                    g2.dispose();
                }
            }
        };
        superficie = fondo;
        fondo.setLayout(new BorderLayout(0, 18));
        fondo.setBorder(new EmptyBorder(24, 32, 24, 32));
        setContentPane(fondo);

        for (int j = 0; j < JUGADORES; j++) {
            panelesJugador[j] = crearPanelJugador(j);
        }
        zona.setOpaque(false);


        JPanel centro = new JPanel(new BorderLayout(0, 16));
        centro.setOpaque(false);
        pantallas.setOpaque(false);
        pantallas.add(crearSelectorNiveles(), "niveles");
        pantallas.add(zona, "tablero");
        centro.add(pantallas, BorderLayout.CENTER);

        add(crearEncabezado(), BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(crearPie(), BorderLayout.SOUTH);

        for (int j = 0; j < JUGADORES; j++) zona.add(panelesJugador[j], "jugador" + j);
        lblEyebrow.setText("MODO CLÁSICO  *  2 JUGADORES");
        lblTitulo.setText("<html>Encuentra las <i style='color:#A9BCFF'>constelaciones</i></html>");
        setTitle("AstroMatch - Modo clásico");
        Dimension pantalla = Toolkit.getDefaultToolkit().getScreenSize();
        setMinimumSize(new Dimension(1000, 640));
        setSize(Math.min(1280, pantalla.width - 60), Math.min(820, pantalla.height - 80));
        setLocationRelativeTo(null);
        setJugadorActivo(0);
        mostrarSelectorNiveles();

    }

    private JPanel crearEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);

        lblEyebrow.setFont(fuentes.mono(Font.BOLD, 11));
        lblEyebrow.setForeground(Tema.HIELO);
        lblTitulo.setFont(fuentes.kiwiSoda(Font.PLAIN, 36));
        lblTitulo.setForeground(Tema.TEXTO);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        lblEyebrow.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        textos.add(lblEyebrow);
        textos.add(Box.createVerticalStrut(6));
        textos.add(lblTitulo);

        panelReloj = crearRelojClasico();
        encabezado.add(panelReloj, BorderLayout.EAST);
        encabezado.add(crearRelojClasico(), BorderLayout.EAST);
        return encabezado;
    }

    private JPanel crearRelojClasico() {
        lblTurno.setFont(fuentes.mono(Font.BOLD, 11));
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

        lblTiempo.setFont(fuentes.kiwiSoda(Font.PLAIN, 44));
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
        lblNombre[j].setFont(fuentes.kiwiSoda(Font.PLAIN, 24));
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
        matricesJugador[j] = matriz;
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

    private JPanel crearSelectorNiveles() {
        JPanel panel = new JPanel(new BorderLayout(0, 30));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(35, 24, 35, 24));

        JLabel titulo = new JLabel("Elige la dificultad", SwingConstants.CENTER);
        titulo.setFont(fuentes.kiwiSoda(Font.PLAIN, 38));
        titulo.setForeground(Tema.TEXTO);
        JLabel detalle = new JLabel("Selecciona el tamaño del tablero para comenzar", SwingConstants.CENTER);
        detalle.setFont(fuentes.mono(Font.PLAIN, 13));
        detalle.setForeground(Tema.SECUNDARIO);
        JPanel encabezado = new JPanel(new GridLayout(2, 1, 0, 8));
        encabezado.setOpaque(false);
        encabezado.add(titulo);
        encabezado.add(detalle);

        JPanel opciones = new JPanel(new GridLayout(1, 3, 22, 0));
        opciones.setOpaque(false);
        opciones.add(crearOpcionNivel("NIVEL 01", "Ideal para comenzar", 2, btnFacil));
        opciones.add(crearOpcionNivel("NIVEL 02", "Un reto intermedio", 4, btnMedio));
        opciones.add(crearOpcionNivel("NIVEL 03", "Pon a prueba tu memoria", 6, btnDificil));
        panel.add(encabezado, BorderLayout.NORTH);
        panel.add(opciones, BorderLayout.CENTER);
        return panel;
    }

    private static javax.swing.border.Border bordeNivel(Color color) {
        return BorderFactory.createCompoundBorder(
                new LineBorder(color, 1, true), new EmptyBorder(24, 18, 24, 18));
    }

    private JPanel crearOpcionNivel(String numero, String descripcion, int tamano, JButton boton) {
        JPanel opcion = new JPanel(new BorderLayout(0, 18));
        opcion.setOpaque(false);
        opcion.setBorder(bordeNivel(Tema.LINEA));

        JLabel nivel = new JLabel(numero, SwingConstants.CENTER);
        nivel.setFont(fuentes.mono(Font.BOLD, 12));
        nivel.setForeground(Tema.HIELO);

        JLabel parejas = new JLabel((tamano * tamano / 2) + " PAREJAS", SwingConstants.CENTER);
        parejas.setFont(fuentes.mono(Font.BOLD, 11));
        parejas.setForeground(Tema.ESTRELLA);

        JLabel texto = new JLabel("<html><div style='text-align:center'>" + descripcion + "</div></html>", SwingConstants.CENTER);
        texto.setFont(Tema.sans(Font.PLAIN, 13));
        texto.setForeground(Tema.SECUNDARIO);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 6));
        info.setOpaque(false);
        info.add(parejas);
        info.add(texto);

        JPanel centro = new JPanel(new BorderLayout(0, 16));
        centro.setOpaque(false);
        centro.add(new MiniTablero(tamano), BorderLayout.CENTER);
        centro.add(info, BorderLayout.SOUTH);

        opcion.add(nivel, BorderLayout.NORTH);
        opcion.add(centro, BorderLayout.CENTER);
        opcion.add(boton, BorderLayout.SOUTH);

        // Hover: el borde se ilumina cuando el mouse está sobre la tarjeta
        java.awt.event.MouseAdapter hover = new java.awt.event.MouseAdapter() {
            private void actualizar() {
                boolean dentro = opcion.getMousePosition(true) != null;
                opcion.setBorder(bordeNivel(dentro ? Tema.ESTRELLA : Tema.LINEA));
            }
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { actualizar(); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { SwingUtilities.invokeLater(this::actualizar); }
        };
        opcion.addMouseListener(hover);
        boton.addMouseListener(hover);
        return opcion;
    }

    /** Solicita el nombre del segundo jugador antes de iniciar el modo clásico. */
    public String solicitarNombreJugador2(String nombreInicial) {
        Window ventana = SwingUtilities.getWindowAncestor(getContentPane());
        return DialogoAstro.pedirTexto(ventana == null ? this : ventana,
                "Identifica al jugador 2", "Su nombre aparecerá durante la partida",
                nombreInicial, "CONTINUAR", "CANCELAR");
    }

    /** Muestra la pantalla para elegir la dificultad de la siguiente partida. */
    public void mostrarSelectorNiveles() {
        ((CardLayout) pantallas.getLayout()).show(pantallas, "niveles");
        btnAccion.setVisible(false);
        lblEyebrow.setText("MODO CLÁSICO  *  2 JUGADORES");
        panelReloj.setVisible(false);
        lblTurno.setVisible(false);
        lblTiempo.setVisible(false);
        barra.setVisible(false);
        getContentPane().revalidate();
        getContentPane().repaint();
    }

    /** Prepara y muestra las cuadrículas con el tamaño seleccionado. */
    public void mostrarTablero(int tamano) {
        for (int j = 0; j < JUGADORES; j++) {
            JPanel matriz = matricesJugador[j];
            matriz.removeAll();
            matriz.setLayout(new GridLayout(tamano, tamano, 6, 6));
            for (int f = 0; f < tamano; f++) {
                for (int c = 0; c < tamano; c++) {
                    matriz.add(cartas[j][f][c]);
                }
            }
            matriz.revalidate();
            matriz.repaint();
        }
        String nivel = tamano == 2 ? "FÁCIL" : tamano == 4 ? "MEDIO" : "DIFÍCIL";
        lblEyebrow.setText("MODO CLÁSICO  *  " + nivel + "  *  " + tamano + " × " + tamano);
        ((CardLayout) pantallas.getLayout()).show(pantallas, "tablero");
        btnAccion.setVisible(true);
        panelReloj.setVisible(true);
    }

    public JButton getBtnFacil() { return btnFacil; }
    public JButton getBtnMedio() { return btnMedio; }
    public JButton getBtnDificil() { return btnDificil; }

    private static JLabel etiquetaDato(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(fuentes.mono(Font.BOLD, 11));
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

        lblMensaje.setFont(fuentes.mono(Font.PLAIN, 14));
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
    /** Devuelve una carta concreta para que el controlador conecte eventos y estados. */
    public Carta getCarta(int jugador, int fila, int columna) { return cartas[jugador][fila][columna]; }
    /** Devuelve todas las cartas del tablero indicado. */
    public Carta[][] getCartas(int jugador)                   { return cartas[jugador]; }
    public JButton getBtnAccion()                             { return btnAccion; }  // Reiniciar / Comenzar
    public JButton getBtnSalir()                              { return btnSalir; }

    /** Cambia el nombre que se muestra sobre el tablero de un jugador. */
    public void setNombreJugador(int jugador, String nombre)  { lblNombre[jugador].setText(nombre); }
    /** Actualiza el mensaje de estado que aparece bajo los tableros. */
    public void setMensaje(String mensaje) {
        lblMensaje.setText(mensaje);
        lblMensaje.setFont(fuentes.mono(Font.PLAIN, 14));
        lblMensaje.setForeground(Tema.SECUNDARIO);
    }

    /** Presenta el resultado final de la partida. */
    public void mostrarFelicitacion(String mensaje) {
        lblMensaje.setText(mensaje);
        lblMensaje.setFont(fuentes.mono(Font.ITALIC, 22));
        lblMensaje.setForeground(Tema.ESTRELLA);
    }

    /** Actualiza el contador de intentos de un jugador. */
    public void setIntentos(int jugador, int intentos)        { lblIntentos[jugador].setText("INTENTOS  " + intentos); }
    /** Actualiza el avance de parejas encontradas de un jugador. */
    public void setParejas(int jugador, int encontradas, int total) {
        lblParejas[jugador].setText("PAREJAS  " + encontradas + "/" + total);
    }

    /** Resalta el panel del jugador cuyo turno está activo. */
    public void setJugadorActivo(int jugador) {
        for (int j = 0; j < JUGADORES; j++) {
            marcarPanel(panelesJugador[j], j == jugador ? Tema.ESTRELLA : Tema.LINEA);
        }
        ((CardLayout) zona.getLayout()).show(zona, "jugador" + jugador);
        lblTurno.setText("TURNO DE  " + lblNombre[jugador].getText().toUpperCase());
    }

    /** Actualiza el número y la barra del tiempo restante del turno. */
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

    public static class MiniTablero extends JComponent {

        private final int n;

        public MiniTablero(int n) {
            this.n = n;
            setOpaque(false);
            setPreferredSize(new Dimension(140, 140));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int lado = Math.min(getWidth(), getHeight()) - 4;
            int gap = n >= 6 ? 4 : 6;
            int celda = (lado - gap * (n - 1)) / n;
            int total = celda * n + gap * (n - 1);
            int x0 = (getWidth() - total) / 2;
            int y0 = (getHeight() - total) / 2;

            for (int f = 0; f < n; f++) {
                for (int c = 0; c < n; c++) {
                    int x = x0 + c * (celda + gap);
                    int y = y0 + f * (celda + gap);
                    boolean destacada = (f + c) % 2 == 0;   // patrón de tablero de ajedrez
                    g2.setColor(new Color(169, 188, 255, destacada ? 120 : 35));
                    g2.fillRoundRect(x, y, celda, celda, 6, 6);
                    g2.setColor(new Color(169, 188, 255, 110));
                    g2.drawRoundRect(x, y, celda, celda, 6, 6);
                }
            }
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
            setFont(fuentes.mono(Font.BOLD, 12));
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
