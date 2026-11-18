
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;


// Ventana de inicio de sesión con una sección visual y otra para ingresar credenciales.
public class VistaLogin extends JFrame {

    // Carácter que oculta la contraseña mientras se escribe.
    private static final char ECO = '\u2022';

    // Campos, botones y etiqueta que componen el formulario de acceso.
    private final JTextField txtUsuario = new JTextField();
    private final JPasswordField txtContrasena = new JPasswordField();
    private final JButton btnVer = Tema.botonTexto("VER");
    private final VistaTablero.BotonPill btnIngresar = new VistaTablero.BotonPill("INICIAR SESIÓN  →", true, true);
    private final JButton btnOlvide = Tema.botonTexto("¿OLVIDASTE TU CONTRASEÑA?");
    private final JButton btnCrearCuenta = Tema.botonTexto("CREAR CUENTA");
    private final JLabel lblError = new JLabel(" ");

    // Configura la ventana y distribuye el panel decorativo junto al formulario.
    public VistaLogin() {
        super("AstroMatch - Iniciar sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;

        c.gridx = 0;
        c.weightx = 0.65;
        add(crearPanelHero(), c);

        c.gridx = 1;
        c.weightx = 0.35;
        add(crearPanelFormulario(), c);

        setSize(2000, 1000);
        setMinimumSize(new Dimension(820, 560));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    // Construye el panel izquierdo con el fondo animado y la presentación del juego.
    private JPanel crearPanelHero() {
        Tema hero = new Tema(140);
        hero.setMostrarOrion(true);
        hero.setAnimado(true);
        hero.setViaje(true);
        hero.setLayout(new BorderLayout());
        hero.setBorder(new EmptyBorder(48, 56, 48, 56));

        JLabel marca = new JLabel("<html><span style='color:#F6DCA2'>&#9679;</span>&nbsp;&nbsp;AstroMatch</html>");
        marca.setFont(fuentes.mono(Font.BOLD, 13));
        marca.setForeground(Tema.TEXTO);

        Lema lema = new Lema();

        JLabel pie = new JLabel("OBSERVATORIO  *  EDICIÓN NOCTURNA");
        pie.setFont(fuentes.mono(Font.PLAIN, 11));
        pie.setForeground(Tema.SECUNDARIO);

        JLabel descripcion = new JLabel("<html><div style='width:400px'>"
                + "Un juego de memoria sobre constelaciones. Compite con otro observador "
                + "y demuestra quién tiene mejor memoria.</div></html>");
        descripcion.setFont(fuentes.mono(Font.PLAIN, 16));
        descripcion.setForeground(Tema.SECUNDARIO);

        // Agrupa el lema y la descripción en una columna alineada a la izquierda.
        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));
        lema.setAlignmentX(Component.LEFT_ALIGNMENT);
        descripcion.setAlignmentX(Component.LEFT_ALIGNMENT);
        textos.add(lema);
        textos.add(Box.createVerticalStrut(22));
        textos.add(descripcion);

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        centro.add(textos, c);

        hero.add(marca, BorderLayout.NORTH);
        hero.add(centro, BorderLayout.CENTER);
        hero.add(pie, BorderLayout.SOUTH);
        return hero;
    }

    // Crea el formulario, sus controles y la acción para mostrar u ocultar la contraseña.
    private JPanel crearPanelFormulario() {
        JPanel lado = new JPanel(new GridBagLayout());
        lado.setBackground(Tema.FONDO);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(Box.createHorizontalStrut(280));

        // Aplica el estilo visual compartido a los campos de usuario y contraseña.
        Tema.estilizarCampo(txtUsuario);
        Tema.estilizarCampo(txtContrasena);
        txtContrasena.setEchoChar(ECO);
        txtUsuario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        // Coloca el campo de contraseña junto al botón para alternar su visibilidad.
        JPanel filaClave = new JPanel(new BorderLayout());
        filaClave.setOpaque(false);
        filaClave.add(txtContrasena, BorderLayout.CENTER);
        filaClave.add(btnVer, BorderLayout.EAST);
        filaClave.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        lblError.setFont(Tema.sans(Font.PLAIN, 13));
        lblError.setForeground(Tema.ERROR);

        btnIngresar.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnIngresar.getPreferredSize().height));

        JPanel filaLinks = new JPanel(new BorderLayout());
        filaLinks.setOpaque(false);
        filaLinks.add(btnOlvide, BorderLayout.WEST);
        filaLinks.add(btnCrearCuenta, BorderLayout.EAST);
        filaLinks.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        // Agrega títulos, campos, mensajes y botones en el orden de lectura del formulario.
        form.add(alinear(etiqueta("ACCESO", fuentes.mono(Font.BOLD, 11), Tema.HIELO)));
        form.add(Box.createVerticalStrut(14));
        form.add(alinear(etiqueta("Bienvenido", fuentes.romantic(Font.PLAIN, 46), Tema.TEXTO)));
        form.add(Box.createVerticalStrut(8));
        form.add(alinear(etiqueta("Inicia sesión para continuar.", fuentes.mono(Font.PLAIN, 14), Tema.SECUNDARIO)));
        form.add(Box.createVerticalStrut(34));
        form.add(alinear(etiqueta("USUARIO O CORREO", fuentes.mono(Font.BOLD, 10), Tema.SECUNDARIO)));
        form.add(alinear(txtUsuario));
        form.add(Box.createVerticalStrut(22));
        form.add(alinear(etiqueta("CONTRASEÑA", fuentes.mono(Font.BOLD, 10), Tema.SECUNDARIO)));
        form.add(alinear(filaClave));
        form.add(Box.createVerticalStrut(14));
        form.add(alinear(lblError));
        form.add(alinear(btnIngresar));
        form.add(Box.createVerticalStrut(4));
        form.add(alinear(filaLinks));
        form.add(Box.createVerticalStrut(30));

        for (Component hijo : form.getComponents()) {
            ((JComponent) hijo).setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        // Cambia entre mostrar la contraseña como puntos y mostrar el texto ingresado.
        btnVer.addActionListener(e -> {
            boolean oculta = txtContrasena.getEchoChar() != 0;
            txtContrasena.setEchoChar(oculta ? (char) 0 : ECO);
            btnVer.setText(oculta ? "OCULTAR" : "VER");
        });

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        lado.add(form, c);
        return lado;
    }

    // Crea una etiqueta de texto con fuente y color personalizados.
    private static JLabel etiqueta(String texto, Font fuente, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente);
        l.setForeground(color);
        return l;
    }

    // Alinea un componente al inicio de la columna del formulario y lo devuelve.
    private static <T extends JComponent> T alinear(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    // Componente personalizado que dibuja el lema en dos líneas con fuentes decorativas.
    private static class Lema extends JComponent {
        private static final String LINEA1 = "Une los puntos,";
        private static final String LINEA2 = "lee el cielo.";
        private final Font fuente = fuentes.romantic(Font.PLAIN, 72f);
        private final FontRenderContext frc = new FontRenderContext(null, true, true);
        private final int altoLinea;
        private final int ancho;

        // Calcula el espacio necesario para que ambas líneas del lema quepan completas.
        Lema() {
            LineMetrics lm = fuente.getLineMetrics(LINEA1, frc);
            altoLinea = (int) Math.ceil((lm.getAscent() + lm.getDescent()) * 0.95);
            ancho = (int) Math.ceil(Math.max(
                    fuente.getStringBounds(LINEA1, frc).getWidth(),
                    fuente.getStringBounds(LINEA2, frc).getWidth())) + 12;
            setOpaque(false);
        }

        @Override public Dimension getPreferredSize() { return new Dimension(ancho, altoLinea * 2 + 10); }
        @Override public Dimension getMaximumSize()   { return getPreferredSize(); }

        @Override
        protected void paintComponent(Graphics g) {
            // Dibuja cada línea del lema con su color y suavizado de texto.
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            g2.setFont(fuente);
            float base = fuente.getLineMetrics(LINEA1, g2.getFontRenderContext()).getAscent();
            g2.setColor(Tema.TEXTO);
            g2.drawString(LINEA1, 0f, base);
            g2.setColor(Tema.HIELO);
            g2.drawString(LINEA2, 0f, base + altoLinea);
            g2.dispose();
        }
    }

    // Métodos para que el controlador lea los datos y acceda a los botones del formulario.
    public String getUsuario()           { return txtUsuario.getText().trim(); }
    public String getContrasena()        { return new String(txtContrasena.getPassword()); }
    public JButton getBtnIngresar()      { return btnIngresar; }
    public JButton getBtnOlvide()        { return btnOlvide; }
    public JButton getBtnCrearCuenta()   { return btnCrearCuenta; }

    // Métodos para mostrar errores o limpiar los campos del formulario.
    public void mostrarError(String mensaje) { lblError.setText(mensaje); }
    public void limpiarError()               { lblError.setText(" "); }
    public void limpiarCampos()              { txtUsuario.setText(""); txtContrasena.setText(""); limpiarError(); }
}
