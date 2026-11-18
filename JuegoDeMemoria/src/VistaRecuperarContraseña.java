import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

// Muestra el formulario donde la persona solicita recuperar el acceso a su cuenta.
public class VistaRecuperarContraseña extends JPanel {

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

    // Prepara los pasos y controles del formulario de recuperacion.
    public VistaRecuperarContraseña() {
        setOpaque(true);
        setBackground(Tema.FONDO);
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(36, 44, 30, 44));

        contenido.setOpaque(false);
        contenido.add(crearPasoUsuario(), PASO_USUARIO);
        contenido.add(crearPasoClave(), PASO_CLAVE);
        contenido.add(crearPasoExito(), PASO_EXITO);

        lblError.setFont(Tema.sans(Font.PLAIN, 13));
        lblError.setForeground(Tema.ERROR);
        lblError.setBorder(new EmptyBorder(10, 0, 0, 0));

        JPanel bloque = new JPanel();
        bloque.setOpaque(false);
        bloque.setLayout(new BoxLayout(bloque, BoxLayout.Y_AXIS));
        bloque.add(crearEncabezado());
        bloque.add(contenido);
        bloque.add(lblError);
        bloque.setPreferredSize(new Dimension(520, bloque.getPreferredSize().height));
        bloque.setMaximumSize(new Dimension(520, Integer.MAX_VALUE));

        GridBagConstraints centrado = new GridBagConstraints();
        centrado.gridx = 0;
        centrado.gridy = 0;
        centrado.anchor = GridBagConstraints.CENTER;
        add(bloque, centrado);

        btnVer.addActionListener(e -> {
            boolean oculta = txtNueva.getEchoChar() != 0;
            char eco = oculta ? (char) 0 : ECO;
            txtNueva.setEchoChar(eco);
            txtConfirmar.setEchoChar(eco);
            btnVer.setText(oculta ? "OCULTAR" : "VER");
        });

        mostrarPaso(PASO_USUARIO);
    }

    // Prepara los pasos y controles del formulario de recuperacion.
    public VistaRecuperarContraseña(Frame padre) { this(); }

    // Prepara la franja superior con la informacion del jugador.
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

    // Arma el formulario donde se identifica la cuenta.
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

    // Arma los campos para escribir y confirmar la nueva clave.
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

    // Prepara el mensaje que confirma que el cambio termino.
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

    // Crea un panel con el formato comun de cada paso.
    private static JPanel nuevoPaso() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    // Organiza dos botones de navegacion en una fila.
    private static JPanel filaLinks(JButton izquierda, JButton derecha) {
        JPanel f = new JPanel(new BorderLayout());
        f.setOpaque(false);
        f.add(izquierda, BorderLayout.WEST);
        if (derecha != null) f.add(derecha, BorderLayout.EAST);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return f;
    }

    // Crea una etiqueta con el texto, fuente y color indicados.
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

    // Ensenia el paso solicitado y oculta los demas.
    public void mostrarPaso(String paso) {
        cartas.show(contenido, paso);
        limpiarError();
        JRootPane raizVentana = SwingUtilities.getRootPane(contenido);
        if (PASO_USUARIO.equals(paso)) {
            lblSubtitulo.setText("Escribe tu usuario o correo.");
            if (raizVentana != null) raizVentana.setDefaultButton(btnContinuar);
            txtUsuario.requestFocusInWindow();
        } else if (PASO_CLAVE.equals(paso)) {
            lblSubtitulo.setText("Elige una contraseña nueva.");
            if (raizVentana != null) raizVentana.setDefaultButton(btnGuardar);
            txtNueva.requestFocusInWindow();
        } else {
            lblSubtitulo.setText("Todo listo.");
            if (raizVentana != null) raizVentana.setDefaultButton(btnFinalizar);
            btnFinalizar.requestFocusInWindow();
        }
    }

    // Muestra la confirmacion de que la recuperacion termino.
    public void mostrarExito() { mostrarPaso(PASO_EXITO); }

    // Devuelve el nombre o correo ingresado sin espacios sobrantes.
    public String getUsuario()          { return txtUsuario.getText().trim(); }
    // Devuelve la nueva clave escrita en el formulario.
    public String getNuevaClave()       { return new String(txtNueva.getPassword()); }
    // Devuelve la confirmacion de la nueva clave.
    public String getConfirmacion()     { return new String(txtConfirmar.getPassword()); }

    // Devuelve el boton para continuar el proceso.
    public JButton getBtnContinuar()    { return btnContinuar; }
    // Devuelve el boton para guardar la nueva clave.
    public JButton getBtnGuardar()      { return btnGuardar; }
    // Devuelve el boton para cerrar el proceso exitoso.
    public JButton getBtnFinalizar()    { return btnFinalizar; }
    // Devuelve el boton para volver al paso anterior.
    public JButton getBtnAtras()        { return btnAtras; }
    // Devuelve el boton para cancelar la recuperacion.
    public JButton getBtnCancelar()     { return btnCancelar; }
    // Devuelve el boton alternativo para cancelar.
    public JButton getBtnCancelar2()    { return btnCancelar2; }

    // Presenta el mensaje indicado junto a los campos.
    public void mostrarError(String mensaje) { lblError.setText(mensaje); }
    // Quita el mensaje de error visible.
    public void limpiarError()               { lblError.setText(" "); }
    // Borra las claves escritas en ambos campos.
    public void limpiarClaves()              { txtNueva.setText(""); txtConfirmar.setText(""); }
}
