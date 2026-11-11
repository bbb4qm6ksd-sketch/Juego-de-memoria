public class ControladorRecuperarContraseña {

    private final VistaRecuperarContraseña vista;
    private final ModeloRecuperarContraseña modelo;

    public ControladorRecuperarContraseña(VistaRecuperarContraseña vista, ModeloRecuperarContraseña modelo) {
        this.vista = vista;
        this.modelo = modelo;

        vista.getBtnContinuar().addActionListener(e -> verificarCuenta());
        vista.getBtnGuardar().addActionListener(e -> guardarClave());
        vista.getBtnAtras().addActionListener(e -> {
            vista.limpiarClaves();
            vista.mostrarPaso(VistaRecuperarContraseña.PASO_USUARIO);
        });
        vista.getBtnCancelar().addActionListener(e -> vista.dispose());
        vista.getBtnCancelar2().addActionListener(e -> vista.dispose());
        vista.getBtnFinalizar().addActionListener(e -> vista.dispose());
    }

    private void verificarCuenta() {
        String dato = vista.getUsuario();
        if (dato.isEmpty()) {
            error("Escribe tu usuario o correo.");
            return;
        }
        if (!modelo.identificar(dato)) {
            error("No encontramos ninguna cuenta con ese dato.");
            return;
        }
        vista.mostrarPaso(VistaRecuperarContraseña.PASO_CLAVE);
    }

    private void guardarClave() {
        String mensaje = modelo.cambiarClave(vista.getNuevaClave(), vista.getConfirmacion());
        if (mensaje != null) {
            error(mensaje);
            return;
        }
        vista.mostrarExito();
    }

    private void error(String mensaje) {
        Sonidos.reproducir(Sonidos.Efecto.ERROR);
        vista.mostrarError(mensaje);
    }
}