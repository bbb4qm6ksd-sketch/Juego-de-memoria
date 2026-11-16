import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Window;
import javax.swing.JRootPane;
import javax.swing.SwingUtilities;

public class ControladorRegistro {

    private final VistaRegistro vista;
    private final Window ventanaActiva;
    private final Runnable alTerminar;
    private String rostroRegistradoPara;
    private boolean cuentaCreada;

    public ControladorRegistro(VistaRegistro vista) {
        this(vista, vista, vista::dispose);
    }

    public ControladorRegistro(VistaRegistro vista, Window ventanaActiva, Runnable alTerminar) {
        this.vista = vista;
        this.ventanaActiva = ventanaActiva;
        this.alTerminar = alTerminar;
        JRootPane raiz = SwingUtilities.getRootPane(vista.getContentPane());
        if (raiz != null) raiz.setDefaultButton(vista.getBtnCrear());
        vista.getBtnCrear().addActionListener(e -> crear());
        vista.getBtnRegistrarRostro().addActionListener(e -> registrarRostro());
        vista.getBtnVolver().addActionListener(e -> terminar());
        vista.addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) { descartarRostroPendiente(); }
        });
    }

    private void registrarRostro() {
        String usuario = vista.getUsuario();
        String error = validarFormulario();
        if (error != null) {
            mostrarError(error);
            return;
        }

        descartarSiCambioUsuario(usuario);
        vista.limpiarError();
        vista.getBtnRegistrarRostro().setEnabled(false);
        vista.getBtnCrear().setEnabled(false);
        boolean listo;
        try {
            listo = FaceID.registrar(ventanaActiva, usuario);
        } finally {
            vista.getBtnRegistrarRostro().setEnabled(true);
            vista.getBtnCrear().setEnabled(true);
        }

        if (listo) {
            rostroRegistradoPara = usuario;
            vista.fijarRostroRegistrado(true);
            vista.limpiarError();
        } else if (rostroRegistradoPara == null) {
            vista.fijarRostroRegistrado(false);
            vista.mostrarError("No se registró el rostro. Revisa la cámara e inténtalo de nuevo.");
        }
    }

    private void crear() {
        String usuario = vista.getUsuario();
        String error = validarFormulario();
        if (error != null) {
            mostrarError(error);
            return;
        }

        descartarSiCambioUsuario(usuario);
        if (rostroRegistradoPara == null || !rostroRegistradoPara.equalsIgnoreCase(usuario)
                || !FaceID.tieneFaceID(usuario)) {
            Sonidos.reproducir(Sonidos.Efecto.ERROR);
            vista.mostrarError("Primero pulsa REGISTRAR ROSTRO y completa la captura.");
            vista.getBtnRegistrarRostro().requestFocusInWindow();
            return;
        }

        error = Cuentas.registrar(usuario, vista.getCorreo(), vista.getContrasena());
        if (error != null) {
            FaceID.eliminar(usuario);
            rostroRegistradoPara = null;
            vista.fijarRostroRegistrado(false);
            mostrarError(error);
            return;
        }

        cuentaCreada = true;
        Sonidos.reproducir(Sonidos.Efecto.ACIERTO);
        DialogoAstro.informar(ventanaActiva,
                "Cuenta lista",
                "Cuenta y Face ID registrados. Ya puedes iniciar sesión con tu rostro.");
        terminar();
    }

    private String validarFormulario() {
        String usuario = vista.getUsuario();
        String correo = vista.getCorreo();
        String clave = vista.getContrasena();
        String confirmacion = vista.getConfirmacion();
        if (usuario.isEmpty() || correo.isEmpty() || clave.isEmpty() || confirmacion.isEmpty()) {
            return "Completa todos los campos antes de registrar el rostro.";
        }
        if (!clave.equals(confirmacion)) {
            return "Las contraseñas no coinciden.";
        }
        return Cuentas.validarRegistro(usuario, correo, clave);
    }

    private void descartarSiCambioUsuario(String usuarioActual) {
        if (rostroRegistradoPara != null && !rostroRegistradoPara.equalsIgnoreCase(usuarioActual)) {
            FaceID.eliminar(rostroRegistradoPara);
            rostroRegistradoPara = null;
            vista.fijarRostroRegistrado(false);
        }
    }

    private void terminar() {
        if (!cuentaCreada) descartarRostroPendiente();
        vista.dispose();
        alTerminar.run();
    }

    private void descartarRostroPendiente() {
        if (!cuentaCreada && rostroRegistradoPara != null) {
            FaceID.eliminar(rostroRegistradoPara);
            rostroRegistradoPara = null;
        }
    }

    private void mostrarError(String mensaje) {
        Sonidos.reproducir(Sonidos.Efecto.ERROR);
        vista.mostrarError(mensaje);
    }
}
