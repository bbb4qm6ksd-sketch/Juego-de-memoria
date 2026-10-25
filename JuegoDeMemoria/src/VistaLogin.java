import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Vista de inicio de sesion. SOLO interfaz: no valida ni consulta nada.
 * El controlador usa los getters para leer datos y asignar listeners.
 */
public class VistaLogin extends JFrame {

    private static final char ECO = '\u2022';

    private final JTextField txtUsuario = new JTextField();
    private final JPasswordField txtContrasena = new JPasswordField();
    private final JButton btnVer = Tema.botonTexto("VER");
    private final JButton btnIngresar = new VistaTablero.BotonPill("INICIAR SESIÓN", true);
    private final JButton btnOlvide = Tema.botonTexto("¿OLVIDASTE TU CONTRASEÑA?");
    private final JButton btnCrearCuenta = Tema.botonTexto("CREAR CUENTA");
    private final JLabel lblError = new JLabel(" ");

    public VistaLogin() {
        super("Constela - Iniciar sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 2));

        add(crearPanelHero());
        add(crearPanelFormulario());

        setSize(1000, 620);
        setMinimumSize(new Dimension(820, 560));
        setLocationRelativeTo(null);
    }

    // ---------- Lado izquierdo: cielo + lema ----------
    private JPanel crearPanelHero() {
        Tema hero = new Tema(90);
        hero.setLayout(new BorderLayout());
        hero.setBorder(new EmptyBorder(48, 56, 48, 56));

        JLabel marca = new JLabel("<html><span style='color:#F6DCA2'>&#9679;</span>&nbsp;&nbsp;CONSTELA</html>");
        marca.setFont(Tema.sans(Font.BOLD, 13));
        marca.setForeground(Tema.TEXTO);

        JLabel lema = new JLabel("<html>Une los puntos,<br><i style='color:#A9BCFF'>lee el cielo.</i></html>");
        lema.setFont(Tema.serif(Font.PLAIN, 54));
        lema.setForeground(Tema.TEXTO);

        JLabel pie = new JLabel("OBSERVATORIO  ·  EDICIÓN NOCTURNA");
        pie.setFont(Tema.sans(Font.PLAIN, 11));
        pie.setForeground(Tema.SECUNDARIO);

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.weightx = 1;
        centro.add(lema, c);

        hero.add(marca, BorderLayout.NORTH);
        hero.add(centro, BorderLayout.CENTER);
        hero.add(pie, BorderLayout.SOUTH);
        return hero;
    }

    // ---------- Lado derecho: formulario ----------
    private JPanel crearPanelFormulario() {
        JPanel lado = new JPanel(new GridBagLayout());
        lado.setBackground(Tema.FONDO);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(Box.createHorizontalStrut(360)); // ancho fijo del formulario

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

        btnIngresar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

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
        form.add(Box.createVerticalStrut(10));
        form.add(alinear(btnIngresar));
        form.add(Box.createVerticalStrut(20));
        form.add(alinear(filaLinks));
        form.add(Box.createVerticalStrut(30));
        form.add(alinear(etiqueta("MOCKUP · SIN AUTENTICACIÓN REAL", Tema.sans(Font.PLAIN, 10), new Color(0x586184))));

        // Mostrar/ocultar contrasena: comportamiento puramente visual
        btnVer.addActionListener(e -> {
            boolean oculta = txtContrasena.getEchoChar() != 0;
            txtContrasena.setEchoChar(oculta ? (char) 0 : ECO);
            btnVer.setText(oculta ? "OCULTAR" : "VER");
        });

        lado.add(form);
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