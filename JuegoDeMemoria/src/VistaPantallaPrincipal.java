import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


public class VistaPantallaPrincipal extends JFrame {
    private final JButton btnClasico = new VistaTablero.BotonPill("INICIAR JUEGO  →", true, true);
    private final JButton btnPremios = new VistaTablero.BotonPill("VER PREMIOS  →", false);
    private final JButton btnCerrarSesion = Tema.botonTexto("CERRAR SESIÓN");
    private final JButton btnSonido = Tema.botonTexto("SONIDO: ON");

    public VistaPantallaPrincipal(String usuario) {
        super("AstroMatch - Inicio");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Tema fondo = new Tema(150, new Color(0x141F47), Tema.FONDO);
        fondo.setAnimado(true);
        fondo.setLayout(new BorderLayout());
        fondo.setBorder(new EmptyBorder(30, 52, 28, 52));
        setContentPane(fondo);
        fondo.add(crearEncabezado(usuario), BorderLayout.NORTH);
        fondo.add(crearContenido(), BorderLayout.CENTER);
        fondo.add(crearPie(), BorderLayout.SOUTH);
        setSize(1280, 780);
        setMinimumSize(new Dimension(980, 650));
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

        JPanel opciones = new JPanel(new GridLayout(1, 2, 24, 0));
        opciones.setOpaque(false);
        opciones.add(crearTarjeta("01  ·  JUEGO DE MEMORIA", "Memoriza las constelaciones",
                "Compite con otro observador. Encuentra las parejas de constelaciones y consigue más puntos.",
                btnClasico, true));
        opciones.add(crearTarjeta("02  ·  CLASIFICACIÓN", "Top 5 premios",
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
        JLabel encabezado = new JLabel(titulo);
        encabezado.setFont(Tema.serif(Font.PLAIN, 34));
        encabezado.setForeground(Tema.TEXTO);
        JLabel detalle = new JLabel("<html><div style='width:360px'>" + descripcion + "</div></html>");
        detalle.setFont(Tema.sans(Font.PLAIN, 15));
        detalle.setForeground(Tema.SECUNDARIO);
        for (JComponent c : new JComponent[]{etiqueta, encabezado, detalle}) c.setAlignmentX(Component.LEFT_ALIGNMENT);
        textos.add(etiqueta);
        textos.add(Box.createVerticalStrut(16));
        textos.add(encabezado);
        textos.add(Box.createVerticalStrut(12));
        textos.add(detalle);
        boton.setPreferredSize(new Dimension(360, principal ? 54 : 48));
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, principal ? 54 : 48));
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

    public JButton getBtnClasico() { return btnClasico; }
    public JButton getBtnPremios() { return btnPremios; }
    public JButton getBtnCerrarSesion() { return btnCerrarSesion; }
    public JButton getBtnSonido() { return btnSonido; }

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
}