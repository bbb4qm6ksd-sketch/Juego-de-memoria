import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;


public class VistaLogin extends JFrame {

    private static final char ECO = '\u2022';

    private final JTextField txtUsuario = new JTextField();
    private final JPasswordField txtContrasena = new JPasswordField();
    private final JButton btnVer = Tema.botonTexto("VER");
    private final VistaTablero.BotonPill btnIngresar = new VistaTablero.BotonPill("INICIAR SESIÓN  →", true, true);
    private final JButton btnOlvide = Tema.botonTexto("¿OLVIDASTE TU CONTRASEÑA?");
    private final JButton btnCrearCuenta = Tema.botonTexto("CREAR CUENTA");
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
        c.weightx = 0.65;
        add(crearPanelHero(), c);

        c.gridx = 1;
        c.weightx = 0.35;
        add(crearPanelFormulario(), c);

        setSize(1000, 620);
        setMinimumSize(new Dimension(820, 560));
        setLocationRelativeTo(null);
    }

    private JPanel crearPanelHero() {
        Tema hero = new Tema(140);
        hero.setMostrarOrion(true);
        hero.setAnimado(true);
        hero.setLayout(new BorderLayout());
        hero.setBorder(new EmptyBorder(48, 56, 48, 56));

        JLabel marca = new JLabel("<html><span style='color:#F6DCA2'>&#9679;</span>&nbsp;&nbsp;AstroMatch</html>");
        marca.setFont(Tema.sans(Font.BOLD, 13));
        marca.setForeground(Tema.TEXTO);

        JLabel lema = new JLabel("<html>Une los puntos,<br><i style='color:#A9BCFF'>lee el cielo.</i></html>");
        lema.setFont(Tema.serif(Font.PLAIN, 54));
        lema.setForeground(Tema.TEXTO);

        JLabel pie = new JLabel("OBSERVATORIO  ·  EDICIÓN NOCTURNA");
        pie.setFont(Tema.sans(Font.PLAIN, 11));
        pie.setForeground(Tema.SECUNDARIO);

        JLabel descripcion = new JLabel("<html><div style='width:400px'>"
                + "Un juego de memoria sobre constelaciones. Compite con otro observador "
                + "y demuestra quién tiene mejor memoria.</div></html>");
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

    private JPanel crearPanelFormulario() {
        JPanel lado = new JPanel(new GridBagLayout());
        lado.setBackground(Tema.FONDO);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(Box.createHorizontalStrut(280));

        Tema.estilizarCampo(txtUsuario);
        Tema.estilizarCampo(txtContrasena);
        txtContrasena.setEchoChar(ECO);
        txtUsuario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

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

        form.add(alinear(etiqueta("ACCESO", Tema.sans(Font.BOLD, 11), Tema.HIELO)));
        form.add(Box.createVerticalStrut(14));
        form.add(alinear(etiqueta("Bienvenido", Tema.serif(Font.PLAIN, 46), Tema.TEXTO)));
        form.add(Box.createVerticalStrut(8));
        form.add(alinear(etiqueta("Inicia sesión para continuar.", Tema.sans(Font.PLAIN, 14), Tema.SECUNDARIO)));
        form.add(Box.createVerticalStrut(34));
        form.add(alinear(etiqueta("USUARIO O CORREO", Tema.sans(Font.BOLD, 10), Tema.SECUNDARIO)));
        form.add(alinear(txtUsuario));
        form.add(Box.createVerticalStrut(22));
        form.add(alinear(etiqueta("CONTRASEÑA", Tema.sans(Font.BOLD, 10), Tema.SECUNDARIO)));
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

    public String getUsuario()           { return txtUsuario.getText().trim(); }
    public String getContrasena()        { return new String(txtContrasena.getPassword()); }
    public JButton getBtnIngresar()      { return btnIngresar; }
    public JButton getBtnOlvide()        { return btnOlvide; }
    public JButton getBtnCrearCuenta()   { return btnCrearCuenta; }

    public void mostrarError(String mensaje) { lblError.setText(mensaje); }
    public void limpiarError()               { lblError.setText(" "); }
    public void limpiarCampos()              { txtUsuario.setText(""); txtContrasena.setText(""); limpiarError(); }
}