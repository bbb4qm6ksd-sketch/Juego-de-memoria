import javax.swing.JOptionPane;

public class ControladorRegistro {

    private final VistaRegistro vista;

    public ControladorRegistro(VistaRegistro vista) {
        this.vista = vista;
        vista.getRootPane().setDefaultButton(vista.getBtnCrear());
        vista.getBtnCrear().addActionListener(e -> crear());
        vista.getBtnVolver().addActionListener(e -> vista.dispose());
    }

    private void crear() {
        String usuario = vista.getUsuario();
        String correo = vista.getCorreo();
        String clave = vista.getContrasena();
        String confirmacion = vista.getConfirmacion();

        if (usuario.isEmpty() || correo.isEmpty() || clave.isEmpty() || confirmacion.isEmpty()) {
            vista.mostrarError("Completa todos los campos.");
            return;
        }
        if (!clave.equals(confirmacion)) {
            vista.mostrarError("Las contraseñas no coinciden.");
            return;
        }
        String error = Cuentas.registrar(usuario, correo, clave);
        if (error != null) {
            vista.mostrarError(error);
            return;
        }
        vista.limpiarError();
        JOptionPane.showMessageDialog(vista, "Cuenta creada. Ya puedes iniciar sesión.",
                "Constela", JOptionPane.INFORMATION_MESSAGE);
        vista.dispose();
    }
}
