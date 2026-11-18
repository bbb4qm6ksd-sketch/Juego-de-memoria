
// Contiene las comprobaciones y operaciones necesarias para recuperar una contraseña.
public class ModeloRecuperarContraseña {

    private String cuenta;

    // Comprueba si el usuario o correo corresponde a una cuenta y lo recuerda.
    public boolean identificar(String usuarioOCorreo) {
        if (!Cuentas.existe(usuarioOCorreo)) {
            cuenta = null;
            return false;
        }
        cuenta = usuarioOCorreo;
        return true;
    }

    // Valida los campos y guarda la nueva contrasena si coinciden.
    public String cambiarClave(String nueva, String confirmar) {
        if (cuenta == null) {
            return "Primero identifica tu cuenta.";
        }
        if (nueva.isEmpty() || confirmar.isEmpty()) {
            return "Completa ambos campos.";
        }
        if (!nueva.equals(confirmar)) {
            return "Las contraseñas no coinciden.";
        }
        return Cuentas.restablecerClave(cuenta, nueva);
    }
}