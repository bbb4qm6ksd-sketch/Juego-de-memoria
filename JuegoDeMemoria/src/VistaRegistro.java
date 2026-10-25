import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

public class VistaRegistro extends JDialog {

    private final JTextField txtUsuario = new JTextField();
    private final JTextField txtCorreo = new JTextField();
    private final JPasswordField txtContrasena = new JPasswordField();
    private final JPasswordField txtConfirmar = new JPasswordField();
    private final JButton btnCrear = new VistaTablero.BotonPill("CREAR CUENTA", true);
    private final JButton btnVolver = Tema.botonTexto("VOLVER");
    private final JLabel lblError = new JLabel(" ");

    public VistaRegistro(Frame dueno) {
        super(dueno, "AstroMatch - Crear cuenta", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        Tema fondo = new Tema(110, new Color(0x141F47), Tema.FONDO);
        fondo.setAnimado(true);   // las estrellas del fondo titilan
        fondo.setLayout(new GridBagLayout());
        fondo.setBorder(new EmptyBorder(30, 40, 30, 40));
        setContentPane(fondo);
        fondo.add(crearFormulario());

        setSize(520, 700);
        setMinimumSize(new Dimension(480, 660));
        setLocationRelativeTo(dueno);
    }

    private JPanel crearFormulario() {
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(Box.createHorizontalStrut(360));

        txtContrasena.setEchoChar('\u2022');
        txtConfirmar.setEchoChar('\u2022');

        JTextField[] campos = {txtUsuario, txtCorreo, txtContrasena, txtConfirmar};
        for (JTextField campo : campos) {
            Tema.estilizarCampo(campo);
            campo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        }

        lblError.setFont(Tema.sans(Font.PLAIN, 13));
        lblError.setForeground(Tema.ERROR);
        btnCrear.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        form.add(alinear(etiqueta("NUEVA CUENTA", Tema.sans(Font.BOLD, 11), Tema.HIELO)));
        form.add(Box.createVerticalStrut(14));
        form.add(alinear(etiqueta("Crea tu cuenta", Tema.serif(Font.PLAIN, 40), Tema.TEXTO)));
        form.add(Box.createVerticalStrut(28));
        form.add(alinear(etiqueta("USUARIO", Tema.sans(Font.BOLD, 10), Tema.SECUNDARIO)));
        form.add(alinear(txtUsuario));
        form.add(Box.createVerticalStrut(16));
        form.add(alinear(etiqueta("CORREO", Tema.sans(Font.BOLD, 10), Tema.SECUNDARIO)));
        form.add(alinear(txtCorreo));
        form.add(Box.createVerticalStrut(16));
        form.add(alinear(etiqueta("CONTRASEÑA", Tema.sans(Font.BOLD, 10), Tema.SECUNDARIO)));
        form.add(alinear(campoClave(txtContrasena)));
        form.add(Box.createVerticalStrut(16));
        form.add(alinear(etiqueta("CONFIRMAR CONTRASEÑA", Tema.sans(Font.BOLD, 10), Tema.SECUNDARIO)));
        form.add(alinear(campoClave(txtConfirmar)));
        form.add(Box.createVerticalStrut(14));
        form.add(alinear(lblError));
        form.add(Box.createVerticalStrut(10));
        form.add(alinear(btnCrear));
        form.add(Box.createVerticalStrut(18));
        form.add(alinear(btnVolver));

        // Todos los hijos (incluidos los espacios Box.createXxxStrut, que por defecto se
        // centran) deben alinearse igual; si no, el contenido se corre hacia la derecha.
        for (Component hijo : form.getComponents()) {
            ((JComponent) hijo).setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        return form;
    }

    // ---------- Campo de contrasena con "ojito" para mostrar/ocultar ----------
    // Es comportamiento puramente visual (cambia el caracter de eco); no hay logica aqui.
    private JPanel campoClave(JPasswordField campo) {
        final char eco = campo.getEchoChar();

        JButton ojo = new JButton(iconoOjo(false));
        ojo.setContentAreaFilled(false);
        ojo.setBorderPainted(false);
        ojo.setFocusPainted(false);
        ojo.setMargin(new Insets(0, 8, 0, 0));
        ojo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ojo.setToolTipText("Mostrar contraseña");
        ojo.addActionListener(e -> {
            boolean estabaOculta = campo.getEchoChar() != 0;
            campo.setEchoChar(estabaOculta ? (char) 0 : eco);
            ojo.setIcon(iconoOjo(estabaOculta));               // tachado = ahora se ve; pulsa para ocultar
            ojo.setToolTipText(estabaOculta ? "Ocultar contraseña" : "Mostrar contraseña");
        });

        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        fila.add(campo, BorderLayout.CENTER);
        fila.add(ojo, BorderLayout.EAST);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        return fila;
    }

    /** Ojo dibujado con Java2D (sin imagenes). tachado=true: ojo con una raya. */
    private static Icon iconoOjo(boolean tachado) {
        return new Icon() {
            @Override public int getIconWidth()  { return 22; }
            @Override public int getIconHeight() { return 22; }

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.translate(x, y);
                g2.setColor(Tema.SECUNDARIO);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                Path2D contorno = new Path2D.Double();           // forma de almendra
                contorno.moveTo(2, 11);
                contorno.quadTo(11, 1, 20, 11);
                contorno.quadTo(11, 21, 2, 11);
                g2.draw(contorno);
                g2.draw(new Ellipse2D.Double(8, 8, 6, 6));       // pupila
                if (tachado) g2.draw(new Line2D.Double(4, 19, 18, 3));
                g2.dispose();
            }
        };
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

    public String getUsuario()       { return txtUsuario.getText().trim(); }
    public String getCorreo()        { return txtCorreo.getText().trim(); }
    public String getContrasena()    { return new String(txtContrasena.getPassword()); }
    public String getConfirmacion()  { return new String(txtConfirmar.getPassword()); }
    public JButton getBtnCrear()     { return btnCrear; }
    public JButton getBtnVolver()    { return btnVolver; }

    public void mostrarError(String mensaje) { lblError.setText(mensaje); }
    public void limpiarError()               { lblError.setText(" "); }
}