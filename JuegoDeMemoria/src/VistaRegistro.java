import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class VistaRegistro extends JDialog {

    private final JTextField txtUsuario = new JTextField();
    private final JTextField txtCorreo = new JTextField();
    private final JPasswordField txtContrasena = new JPasswordField();
    private final JPasswordField txtConfirmar = new JPasswordField();
    private final JButton btnCrear = new VistaTablero.BotonPill("CREAR CUENTA", true);
    private final JButton btnVolver = Tema.botonTexto("VOLVER");
    private final JLabel lblError = new JLabel(" ");

    public VistaRegistro(Frame dueno) {
        super(dueno, "Constela - Crear cuenta", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        Tema fondo = new Tema(70, new Color(0x141F47), Tema.FONDO);
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
        form.add(alinear(txtContrasena));
        form.add(Box.createVerticalStrut(16));
        form.add(alinear(etiqueta("CONFIRMAR CONTRASEÑA", Tema.sans(Font.BOLD, 10), Tema.SECUNDARIO)));
        form.add(alinear(txtConfirmar));
        form.add(Box.createVerticalStrut(14));
        form.add(alinear(lblError));
        form.add(Box.createVerticalStrut(10));
        form.add(alinear(btnCrear));
        form.add(Box.createVerticalStrut(18));
        form.add(alinear(btnVolver));
        return form;
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
