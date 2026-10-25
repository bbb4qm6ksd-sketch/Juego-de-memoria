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
                        "AstroMatch", JOptionPane.INFORMATION_MESSAGE));
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
        abrirMenu(usuario);
    }

    private void abrirRegistro() {
        VistaRegistro registro = new VistaRegistro(vista);
        new ControladorRegistro(registro);
        registro.setVisible(true);
    }

    private void abrirMenu(String usuario) {
        VistaPantallaPrincipal menu = new VistaPantallaPrincipal(usuario);
        VistaTablero tablero = new VistaTablero();
        VistaPremios premios = new VistaPremios();

        tablero.setNombreJugador(0, usuario);
        tablero.setJugadorActivo(0);
        tablero.setMensaje("Tu turno, " + usuario + ".");

        menu.getBtnClasico().addActionListener(e -> {
            menu.setVisible(false);
            tablero.setVisible(true);
        });
        menu.getBtnPremios().addActionListener(e -> {
            menu.setVisible(false);
            premios.setVisible(true);
        });
        menu.getBtnCerrarSesion().addActionListener(e -> {
            menu.dispose();
            tablero.dispose();
            premios.dispose();
            vista.limpiarCampos();
            vista.setVisible(true);
        });
        tablero.getBtnSalir().addActionListener(e -> {
            tablero.setVisible(false);
            menu.setVisible(true);
        });
        premios.getBtnVolver().addActionListener(e -> {
            premios.setVisible(false);
            menu.setVisible(true);
        });

        vista.setVisible(false);
        menu.setVisible(true);
    }
}