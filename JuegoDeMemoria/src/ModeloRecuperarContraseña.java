
public class ModeloRecuperarContraseña {

    private String cuenta;

    public boolean identificar(String usuarioOCorreo) {
        if (!Cuentas.existe(usuarioOCorreo)) {
            cuenta = null;
            return false;
        }
        cuenta = usuarioOCorreo;
        return true;
    }

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