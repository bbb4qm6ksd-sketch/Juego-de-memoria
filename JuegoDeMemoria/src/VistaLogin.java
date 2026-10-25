import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


public class VistaLogin extends JFrame {

    private static final char ECO = '\u2022';

    private final JTextField txtUsuario = new JTextField();
    private final JPasswordField txtContrasena = new JPasswordField();
    private final JButton btnVer = Tema.botonTexto("VER");
    private final JButton btnIngresar = new VistaTablero.BotonPill("INICIAR SESIÓN", true);
    private final JButton btnOlvide = Tema.botonTexto("¿OLVIDASTE TU CONTRASEÑA?");
    private final JButton btnCrearCuenta = new VistaTablero.BotonPill("CREAR CUENTA", false);
    private final JLabel lblError = new JLabel(" ");

    public VistaLogin() {
        super("AstroMatch - Iniciar sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;

        c.gridx = 0;
        c.weightx = 0.58; // fondo estrellado: 58 % del ancho
        add(crearPanelHero(), c);

        c.gridx = 1;
        c.weightx = 0.42; // formulario: 42 % del ancho
        add(crearPanelFormulario(), c);

        setSize(1380, 820);
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
    }

    private JPanel crearPanelHero() {
        Tema hero = new Tema(140);
        hero.setLayout(new BorderLayout());
        hero.setBorder(new EmptyBorder(48, 56, 48, 56));

        JLabel marca = new JLabel("AstroMatch");
        marca.setFont(new Font("Cooper Black", Font.PLAIN, 30));
        marca.setForeground(new Color(0,4,31));

        JLabel lema = new JLabel("<html>Une los puntos,<br><i style='color:#A9BCFF'>lee el cielo.</i></html>");
        lema.setFont(Tema.serif(Font.PLAIN, 54));
        lema.setForeground(Tema.TEXTO);

        JLabel pie = new JLabel("OBSERVATORIO  ·  EDICIÓN NOCTURNA");
        pie.setFont(Tema.sans(Font.PLAIN, 11));
        pie.setForeground(Tema.SECUNDARIO);

        JLabel descripcion = new JLabel("<html><div style='width:400px'>"
                + "Un juego de memoria sobre constelaciones. Compite con otro observador "
                + "o desafía tu mente con patrones cada vez más largos.</div></html>");
        descripcion.setFont(Tema.sans(Font.PLAIN, 16));
        descripcion.setForeground(Tema.SECUNDARIO);

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

    // ---------- Lado derecho: formulario ----------
    private JPanel crearPanelFormulario() {
        JPanel lado = new JPanel(new GridBagLayout());
        lado.setBackground(Tema.FONDO);

        PanelTarjeta tarjeta = new PanelTarjeta();
        tarjeta.setLayout(new BorderLayout());
        tarjeta.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(Box.createHorizontalStrut(380));

        Tema.estilizarCampo(txtUsuario);
        Tema.estilizarCampo(txtContrasena);
        txtContrasena.setEchoChar(ECO);
        prepararCampo(txtUsuario);
        prepararCampo(txtContrasena);

        PanelCampo campoUsuario = new PanelCampo();
        campoUsuario.setLayout(new BorderLayout(12, 0));
        campoUsuario.add(new IconoCampo(false), BorderLayout.WEST);
        campoUsuario.add(txtUsuario, BorderLayout.CENTER);

        PanelCampo campoContrasena = new PanelCampo();
        campoContrasena.setLayout(new BorderLayout(12, 0));
        campoContrasena.add(new IconoCampo(true), BorderLayout.WEST);
        campoContrasena.add(txtContrasena, BorderLayout.CENTER);
        btnVer.setForeground(Tema.SECUNDARIO);
        btnVer.setFont(Tema.sans(Font.BOLD, 11));
        campoContrasena.add(btnVer, BorderLayout.EAST);

        lblError.setFont(Tema.sans(Font.PLAIN, 13));
        lblError.setForeground(Tema.ERROR);

        JCheckBox recordar = new JCheckBox("Recordarme");
        recordar.setSelected(true);
        recordar.setOpaque(false);
        recordar.setForeground(Tema.SECUNDARIO);
        recordar.setFont(Tema.sans(Font.PLAIN, 12));
        recordar.setFocusPainted(false);
        btnOlvide.setForeground(Tema.HIELO);
        btnOlvide.setFont(Tema.sans(Font.PLAIN, 12));
        JPanel filaOpciones = new JPanel(new BorderLayout());
        filaOpciones.setOpaque(false);
        filaOpciones.add(recordar, BorderLayout.WEST);
        filaOpciones.add(btnOlvide, BorderLayout.EAST);
        filaOpciones.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        btnIngresar.setText("INICIAR SESIÓN   →");
        btnIngresar.setFont(Tema.sans(Font.BOLD, 12));
        btnIngresar.setPreferredSize(new Dimension(380, 54));
        btnIngresar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
        btnCrearCuenta.setText("CREAR CUENTA");
        btnCrearCuenta.setPreferredSize(new Dimension(380, 48));
        btnCrearCuenta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        form.add(alinear(etiqueta("—  A C C E S O", Tema.sans(Font.BOLD, 11), Tema.HIELO)));
        form.add(Box.createVerticalStrut(18));
        form.add(alinear(etiqueta(
                "Bienvenido",
                Tema.serif(Font.PLAIN, 42), Tema.TEXTO)));
        form.add(Box.createVerticalStrut(8));
        form.add(alinear(etiqueta("Inicia sesión para continuar tu observación.",
                Tema.sans(Font.PLAIN, 14), Tema.SECUNDARIO)));
        form.add(Box.createVerticalStrut(28));
        form.add(alinear(etiqueta("U S U A R I O  O  C O R R E O",
                Tema.sans(Font.BOLD, 10), Tema.SECUNDARIO)));
        form.add(Box.createVerticalStrut(8));
        form.add(alinear(campoUsuario));
        form.add(Box.createVerticalStrut(18));
        form.add(alinear(etiqueta("C O N T R A S E Ñ A",
                Tema.sans(Font.BOLD, 10), Tema.SECUNDARIO)));
        form.add(Box.createVerticalStrut(8));
        form.add(alinear(campoContrasena));
        form.add(Box.createVerticalStrut(8));
        form.add(alinear(lblError));
        form.add(Box.createVerticalStrut(6));
        form.add(alinear(filaOpciones));
        form.add(Box.createVerticalStrut(28));
        form.add(alinear(btnIngresar));
        form.add(Box.createVerticalStrut(24));
        form.add(alinear(crearSeparador()));
        form.add(Box.createVerticalStrut(18));
        form.add(alinear(btnCrearCuenta));

        for (Component hijo : form.getComponents()) {
            ((JComponent) hijo).setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        btnVer.addActionListener(e -> {
            boolean oculta = txtContrasena.getEchoChar() != 0;
            txtContrasena.setEchoChar(oculta ? (char) 0 : ECO);
            btnVer.setText(oculta ? "OCULTAR" : "VER");
        });

        tarjeta.add(form, BorderLayout.CENTER);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        lado.add(tarjeta, c);
        return lado;
    }

    private static void prepararCampo(JTextField campo) {
        campo.setOpaque(false);
        campo.setForeground(Tema.TEXTO);
        campo.setCaretColor(Tema.HIELO);
        campo.setBorder(new EmptyBorder(10, 0, 10, 0));
        campo.setPreferredSize(new Dimension(250, 48));
        campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
    }

    private static JPanel crearSeparador() {
        JPanel separador = new JPanel(new BorderLayout(12, 0));
        separador.setOpaque(false);
        JLabel texto = etiqueta("¿ P R I M E R A  V E Z ?", Tema.sans(Font.PLAIN, 10), Tema.SECUNDARIO);
        texto.setHorizontalAlignment(SwingConstants.CENTER);
        separador.add(lineaSeparadora(), BorderLayout.WEST);
        separador.add(texto, BorderLayout.CENTER);
        separador.add(lineaSeparadora(), BorderLayout.EAST);
        separador.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        return separador;
    }

    private static JComponent lineaSeparadora() {
        JPanel linea = new JPanel();
        linea.setOpaque(true);
        linea.setBackground(Tema.LINEA);
        linea.setPreferredSize(new Dimension(40, 1));
        return linea;
    }

    private static class PanelTarjeta extends JPanel {
        PanelTarjeta() { setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 1, h = getHeight() - 1;
            g2.setColor(Tema.SUPERFICIE);
            g2.fillRoundRect(0, 0, w, h, 28, 28);
            g2.setColor(Tema.LINEA);
            g2.drawRoundRect(0, 0, w, h, 28, 28);
            g2.dispose();
        }
    }

    private static class PanelCampo extends JPanel {
        PanelCampo() {
            setOpaque(false);
            setBackground(Tema.FONDO);
            setBorder(new EmptyBorder(0, 14, 0, 14));
            setPreferredSize(new Dimension(380, 52));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth() - 1, h = getHeight() - 1;
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, w, h, 18, 18);
            g2.setColor(Tema.LINEA);
            g2.drawRoundRect(0, 0, w, h, 18, 18);
            g2.dispose();
        }
    }

    private static class IconoCampo extends JComponent {
        private final boolean candado;
        IconoCampo(boolean candado) {
            this.candado = candado;
            setPreferredSize(new Dimension(18, 20));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Tema.SECUNDARIO);
            g2.setStroke(new BasicStroke(1.5f));
            if (candado) {
                g2.drawRoundRect(4, 9, 10, 8, 2, 2);
                g2.drawArc(6, 3, 6, 11, 0, 180);
            } else {
                g2.drawOval(6, 2, 6, 6);
                g2.drawArc(3, 9, 12, 10, 0, 180);
            }
            g2.dispose();
        }
    }

    private static JLabel etiqueta(String texto, Font fuente, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(fuente);
        l.setForeground(color);
        return l;
    }

    private static <T extends JComponent> T alinear(T c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    // ---------- API para el controlador ----------
    public String getUsuario()           { return txtUsuario.getText().trim(); }
    public String getContrasena()        { return new String(txtContrasena.getPassword()); }
    public JButton getBtnIngresar()      { return btnIngresar; }
    public JButton getBtnOlvide()        { return btnOlvide; }
    public JButton getBtnCrearCuenta()   { return btnCrearCuenta; }

    public void mostrarError(String mensaje) { lblError.setText(mensaje); }
    public void limpiarError()               { lblError.setText(" "); }
    public void limpiarCampos()              { txtUsuario.setText(""); txtContrasena.setText(""); limpiarError(); }
}