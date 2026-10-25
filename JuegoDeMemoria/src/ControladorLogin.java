import javax.swing.JOptionPane;

public class ControladorLogin {

    private final VistaLogin vista;

    public ControladorLogin(VistaLogin vista) {
        this.vista = vista;
        vista.getRootPane().setDefaultButton(vista.getBtnIngresar());
        vista.getBtnIngresar().addActionListener(e -> ingresar());
        vista.getBtnCrearCuenta().addActionListener(e -> abrirRegistro());
        vista.getBtnOlvide().addActionListener(e ->
                JOptionPane.showMessageDialog(vista,
                        "La recuperación de contraseña estará disponible próximamente.",
                        "Constela", JOptionPane.INFORMATION_MESSAGE));
    }

    private void ingresar() {
        String usuarioOCorreo = vista.getUsuario();
        String clave = vista.getContrasena();

        if (usuarioOCorreo.isEmpty() || clave.isEmpty()) {
            vista.mostrarError("Escribe tu usuario y contraseña.");
            return;
        }
        String usuario = Cuentas.validar(usuarioOCorreo, clave);
        if (usuario == null) {
            vista.mostrarError("Usuario o contraseña incorrectos.");
            return;
        }
        vista.limpiarError();
        abrirTablero(usuario);
    }

    private void abrirRegistro() {
        VistaRegistro registro = new VistaRegistro(vista);
        new ControladorRegistro(registro);
        registro.setVisible(true);
    }

    private void abrirTablero(String usuario) {
        VistaTablero tablero = new VistaTablero();
        tablero.setNombreJugador(0, usuario);
        tablero.setJugadorActivo(0);
        tablero.setMensaje("Bienvenido, " + usuario + ".");
        tablero.setVisible(true);
        vista.dispose();
    }
}
