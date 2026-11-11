import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class VistaRecuperarContraseña extends JDialog {

    private static final char ECO = '\u2022';
    public static final String PASO_USUARIO = "usuario";
    public static final String PASO_CLAVE = "clave";
    public static final String PASO_EXITO = "exito";

    private final CardLayout cartas = new CardLayout();
    private final JPanel contenido = new JPanel(cartas);

    private final JTextField txtUsuario = new JTextField();
    private final JPasswordField txtNueva = new JPasswordField();
    private final JPasswordField txtConfirmar = new JPasswordField();

    private final VistaTablero.BotonPill btnContinuar = new VistaTablero.BotonPill("CONTINUAR  →", true, true);
    private final VistaTablero.BotonPill btnGuardar = new VistaTablero.BotonPill("GUARDAR CONTRASEÑA  →", true, true);
    private final VistaTablero.BotonPill btnFinalizar = new VistaTablero.BotonPill("INICIAR SESIÓN  →", true, true);
    private final JButton btnCancelar = Tema.botonTexto("CANCELAR");
    private final JButton btnCancelar2 = Tema.botonTexto("CANCELAR");
    private final JButton btnAtras = Tema.botonTexto("← ATRÁS");
    private final JButton btnVer = Tema.botonTexto("VER");

    private final JLabel lblSubtitulo = new JLabel(" ");
    private final JLabel lblError = new JLabel(" ");

    public VistaRecuperarContraseña(Frame padre) {
        super(padre, "AstroMatch - Restablecer contraseña", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Tema.FONDO);
        raiz.setBorder(new EmptyBorder(36, 44, 30, 44));
        setContentPane(raiz);

        raiz.add(crearEncabezado(), BorderLayout.NORTH);

        contenido.setOpaque(false);
        contenido.add(crearPasoUsuario(), PASO_USUARIO);
        contenido.add(crearPasoClave(), PASO_CLAVE);
        contenido.add(crearPasoExito(), PASO_EXITO);
        raiz.add(contenido, BorderLayout.CENTER);

        lblError.setFont(Tema.sans(Font.PLAIN, 13));
        lblError.setForeground(Tema.ERROR);
        lblError.setBorder(new EmptyBorder(10, 0, 0, 0));
        raiz.add(lblError, BorderLayout.SOUTH);

        btnVer.addActionListener(e -> {
            boolean oculta = txtNueva.getEchoChar() != 0;
            char eco = oculta ? (char) 0 : ECO;
            txtNueva.setEchoChar(eco);
            txtConfirmar.setEchoChar(eco);
            btnVer.setText(oculta ? "OCULTAR" : "VER");
        });

        mostrarPaso(PASO_USUARIO);
        setMinimumSize(new Dimension(440, 0));
        pack();
        setResizable(false);
        setLocationRelativeTo(padre);
    }

    private JPanel crearEncabezado() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.add(Box.createHorizontalStrut(340));

        p.add(alinear(etiqueta("RECUPERAR ACCESO", fuentes.mono(Font.BOLD, 11), Tema.HIELO)));
        p.add(Box.createVerticalStrut(12));
        p.add(alinear(etiqueta("Nueva clave", fuentes.romantic(Font.PLAIN, 40), Tema.TEXTO)));
        p.add(Box.createVerticalStrut(8));
        lblSubtitulo.setFont(fuentes.mono(Font.PLAIN, 13));
        lblSubtitulo.setForeground(Tema.SECUNDARIO);
        p.add(alinear(lblSubtitulo));
        p.add(Box.createVerticalStrut(26));
        return p;
    }

    private JPanel crearPasoUsuario() {
        JPanel p = nuevoPaso();
        Tema.estilizarCampo(txtUsuario);
        txtUsuario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        p.add(alinear(etiqueta("USUARIO O CORREO", fuentes.mono(Font.BOLD, 10), Tema.SECUNDARIO)));
        p.add(alinear(txtUsuario));
        p.add(Box.createVerticalStrut(24));
        btnContinuar.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnContinuar.getPreferredSize().height));
        p.add(alinear(btnContinuar));
        p.add(Box.createVerticalStrut(6));
        p.add(alinear(filaLinks(btnCancelar, null)));
        return p;
    }

    private JPanel crearPasoClave() {
        JPanel p = nuevoPaso();
        Tema.estilizarCampo(txtNueva);
        Tema.estilizarCampo(txtConfirmar);
        txtNueva.setEchoChar(ECO);
        txtConfirmar.setEchoChar(ECO);

        JPanel filaNueva = new JPanel(new BorderLayout());
        filaNueva.setOpaque(false);
        filaNueva.add(txtNueva, BorderLayout.CENTER);
        filaNueva.add(btnVer, BorderLayout.EAST);
        filaNueva.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        txtConfirmar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        p.add(alinear(etiqueta("NUEVA CONTRASEÑA", fuentes.mono(Font.BOLD, 10), Tema.SECUNDARIO)));
        p.add(alinear(filaNueva));
        p.add(Box.createVerticalStrut(18));
        p.add(alinear(etiqueta("CONFIRMAR CONTRASEÑA", fuentes.mono(Font.BOLD, 10), Tema.SECUNDARIO)));
        p.add(alinear(txtConfirmar));
        p.add(Box.createVerticalStrut(24));
        btnGuardar.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnGuardar.getPreferredSize().height));
        p.add(alinear(btnGuardar));
        p.add(Box.createVerticalStrut(6));
        p.add(alinear(filaLinks(btnAtras, btnCancelar2)));
        return p;
    }

    private JPanel crearPasoExito() {
        JPanel p = nuevoPaso();

        p.add(alinear(etiqueta("CONTRASEÑA ACTUALIZADA", fuentes.mono(Font.BOLD, 10), Tema.HIELO)));
        p.add(Box.createVerticalStrut(10));
        p.add(alinear(etiqueta("Tu contraseña se actualizó.", fuentes.mono(Font.PLAIN, 14), Tema.TEXTO)));
        p.add(Box.createVerticalStrut(6));
        p.add(alinear(etiqueta("Ya puedes iniciar sesión.", fuentes.mono(Font.PLAIN, 13), Tema.SECUNDARIO)));
        p.add(Box.createVerticalStrut(28));
        btnFinalizar.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnFinalizar.getPreferredSize().height));
        p.add(alinear(btnFinalizar));
        return p;
    }

    private static JPanel nuevoPaso() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    private static JPanel filaLinks(JButton izquierda, JButton derecha) {
        JPanel f = new JPanel(new BorderLayout());
        f.setOpaque(false);
        f.add(izquierda, BorderLayout.WEST);
        if (derecha != null) f.add(derecha, BorderLayout.EAST);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return f;
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

    // ---- API para el controlador ----

    public void mostrarPaso(String paso) {
        cartas.show(contenido, paso);
        limpiarError();
        if (PASO_USUARIO.equals(paso)) {
            lblSubtitulo.setText("Escribe tu usuario o correo.");
            getRootPane().setDefaultButton(btnContinuar);
            txtUsuario.requestFocusInWindow();
        } else if (PASO_CLAVE.equals(paso)) {
            lblSubtitulo.setText("Elige una contraseña nueva.");
            getRootPane().setDefaultButton(btnGuardar);
            txtNueva.requestFocusInWindow();
        } else {
            lblSubtitulo.setText("Todo listo.");
            getRootPane().setDefaultButton(btnFinalizar);
            btnFinalizar.requestFocusInWindow();
        }
    }

    public void mostrarExito() { mostrarPaso(PASO_EXITO); }

    public String getUsuario()          { return txtUsuario.getText().trim(); }
    public String getNuevaClave()       { return new String(txtNueva.getPassword()); }
    public String getConfirmacion()     { return new String(txtConfirmar.getPassword()); }

    public JButton getBtnContinuar()    { return btnContinuar; }
    public JButton getBtnGuardar()      { return btnGuardar; }
    public JButton getBtnFinalizar()    { return btnFinalizar; }
    public JButton getBtnAtras()        { return btnAtras; }
    public JButton getBtnCancelar()     { return btnCancelar; }
    public JButton getBtnCancelar2()    { return btnCancelar2; }

    public void mostrarError(String mensaje) { lblError.setText(mensaje); }
    public void limpiarError()               { lblError.setText(" "); }
    public void limpiarClaves()              { txtNueva.setText(""); txtConfirmar.setText(""); }
}