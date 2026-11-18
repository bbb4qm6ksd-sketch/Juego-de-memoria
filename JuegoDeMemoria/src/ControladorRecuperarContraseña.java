// Atiende las acciones de recuperación y comunica la pantalla con la lógica del modelo.
public class ControladorRecuperarContraseña {

    private final VistaRecuperarContraseña vista;
    private final ModeloRecuperarContraseña modelo;
    private final Runnable alSalir;

    // Conecta las acciones del formulario con la logica de recuperacion.
    public ControladorRecuperarContraseña(VistaRecuperarContraseña vista, ModeloRecuperarContraseña modelo) {
        this(vista, modelo, () -> { });
    }

    public ControladorRecuperarContraseña(VistaRecuperarContraseña vista,
                                          ModeloRecuperarContraseña modelo, Runnable alSalir) {
        this.vista = vista;
        this.modelo = modelo;
        this.alSalir = alSalir;

        vista.mostrarPaso(VistaRecuperarContraseña.PASO_USUARIO);
        vista.getBtnContinuar().addActionListener(e -> verificarCuenta());
        vista.getBtnGuardar().addActionListener(e -> guardarClave());
        vista.getBtnAtras().addActionListener(e -> {
            vista.limpiarClaves();
            vista.mostrarPaso(VistaRecuperarContraseña.PASO_USUARIO);
        });
        vista.getBtnCancelar().addActionListener(e -> salir());
        vista.getBtnCancelar2().addActionListener(e -> salir());
        vista.getBtnFinalizar().addActionListener(e -> salir());
    }

    // Comprueba la cuenta y avanza al cambio de clave si existe.
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

    // Valida y envia las contrasenas ingresadas para guardarlas.
    private void guardarClave() {
        String mensaje = modelo.cambiarClave(vista.getNuevaClave(), vista.getConfirmacion());
        if (mensaje != null) {
            error(mensaje);
            return;
        }
        vista.mostrarExito();
    }

    // Muestra el mensaje de error en el formulario.
    private void error(String mensaje) {
        Sonidos.reproducir(Sonidos.Efecto.ERROR);
        vista.mostrarError(mensaje);
    }

    // Cierra el flujo de recuperacion y vuelve a la pantalla anterior.
    private void salir() {
        alSalir.run();
    }
}
