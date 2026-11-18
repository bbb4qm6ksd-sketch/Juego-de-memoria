import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Cuentas {

    // Maneja el registro, acceso y cambios de las cuentas guardadas.
    private static final Path ARCHIVO = Paths.get("cuentas.txt");
    private static final Pattern USUARIO = Pattern.compile("[A-Za-z0-9_.-]{1,20}");
    private static final Pattern CORREO = Pattern.compile("[^\\s@;]+@[^\\s@;]+\\.[^\\s@;]+");
    private static final int MIN_CONTRASENA = 8;

    private static class Cuenta {
        String usuario;
        String correo;
        String clave;
    }

    // Revisa los datos y guarda la cuenta si todo está en orden.
    public static String registrar(String usuario, String correo, String contrasena) {
        String error = validarRegistro(usuario, correo, contrasena);
        if (error != null) {
            return error;
        }
        try {
            String linea = usuario + ";" + correo + ";" + contrasena + System.lineSeparator();
            Files.write(ARCHIVO, linea.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return null;
        } catch (IOException e) {
            return "No se pudo guardar la cuenta.";
        }
    }

    // Comprueba los datos del registro antes de crear la cuenta.
    public static String validarRegistro(String usuario, String correo, String contrasena) {
        if (!USUARIO.matcher(usuario).matches()) {
            return "Usuario: usa de 1 a 20 caracteres (letras, números, . _ -).";
        }
        if (!CORREO.matcher(correo).matches()) {
            return "Escribe un correo válido.";
        }
        String errorClave = revisarClave(contrasena);
        if (errorClave != null) {
            return errorClave;
        }
        try {
            for (Cuenta c : cargar()) {
                if (c.usuario.equalsIgnoreCase(usuario)) {
                    return "Ese usuario ya existe.";
                }
                if (c.correo.equalsIgnoreCase(correo)) {
                    return "Ese correo ya tiene una cuenta.";
                }
            }
            return null;
        } catch (IOException e) {
            return "No se pudo validar la cuenta.";
        }
    }
    // Busca la cuenta y confirma que la contraseña coincida.
    public static String validar(String usuarioOCorreo, String contrasena) {
        try {
            for (Cuenta c : cargar()) {
                if (!coincide(c, usuarioOCorreo)) {
                    continue;
                }
                return c.clave.equals(contrasena) ? c.usuario : null;
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }
    public static boolean existe(String usuarioOCorreo) {
        try {
            for (Cuenta c : cargar()) {
                if (coincide(c, usuarioOCorreo)) {
                    return true;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return false;
    }

    public static String correoDe(String usuario) {
        if (usuario == null) return null;
        try {
            for (Cuenta c : cargar()) {
                if (c.usuario.equalsIgnoreCase(usuario.trim())) return c.correo;
            }
        } catch (IOException e) {
            return null;
        }
        return null;
    }

    public static boolean eliminar(String usuario) {
        try {
            List<Cuenta> lista = cargar();
            boolean eliminada = lista.removeIf(c -> c.usuario.equalsIgnoreCase(usuario));
            if (eliminada) {
                guardar(lista);
            }
            return eliminada;
        } catch (IOException e) {
            return false;
        }
    }
    // Actualiza la contraseña de una cuenta existente.
    public static String restablecerClave(String usuarioOCorreo, String nuevaContrasena) {
        String errorClave = revisarClave(nuevaContrasena);
        if (errorClave != null) {
            return errorClave;
        }
        try {
            List<Cuenta> lista = cargar();
            for (Cuenta c : lista) {
                if (!coincide(c, usuarioOCorreo)) {
                    continue;
                }
                c.clave = nuevaContrasena;
                guardar(lista);
                return null;
            }
            return "No existe esa cuenta.";
        } catch (IOException e) {
            return "No se pudo guardar la nueva contraseña.";
        }
    }

    // Comprueba que la contraseña cumpla con los requisitos.
    private static String revisarClave(String contrasena) {
        if (contrasena.length() < MIN_CONTRASENA) {
            return "La contraseña debe tener al menos " + MIN_CONTRASENA + " caracteres.";
        }
        boolean tieneMinuscula = false;
        boolean tieneMayuscula = false;
        boolean tieneCaracterEspecial = false;
        for (char caracter : contrasena.toCharArray()) {
            if (Character.isLowerCase(caracter)) tieneMinuscula = true;
            else if (Character.isUpperCase(caracter)) tieneMayuscula = true;
            else if (!Character.isLetterOrDigit(caracter) && !Character.isWhitespace(caracter)) {
                tieneCaracterEspecial = true;
            }
        }
        if (!tieneMinuscula || !tieneMayuscula || !tieneCaracterEspecial) {
            return "La contraseña debe incluir una minúscula, una mayúscula y un carácter especial.";
        }
        if (contrasena.contains(";")) {
            return "La contraseña no puede llevar el símbolo ;";
        }
        return null;
    }

    private static boolean coincide(Cuenta c, String usuarioOCorreo) {
        return c.usuario.equalsIgnoreCase(usuarioOCorreo) || c.correo.equalsIgnoreCase(usuarioOCorreo);
    }

    // Lee las cuentas del archivo y las prepara para usarlas.
    private static List<Cuenta> cargar() throws IOException {
        List<Cuenta> lista = new ArrayList<>();
        if (!Files.exists(ARCHIVO)) {
            return lista;
        }
        for (String linea : Files.readAllLines(ARCHIVO, StandardCharsets.UTF_8)) {
            String[] partes = linea.split(";", -1);
            if (partes.length != 3) {
                continue;
            }
            Cuenta c = new Cuenta();
            c.usuario = partes[0];
            c.correo = partes[1];
            c.clave = partes[2];
            lista.add(c);
        }
        return lista;
    }
    // Guarda en el archivo la lista actualizada de cuentas.
    private static void guardar(List<Cuenta> lista) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Cuenta c : lista) {
            sb.append(c.usuario).append(';')
                    .append(c.correo).append(';')
                    .append(c.clave)
                    .append(System.lineSeparator());
        }
        Files.write(ARCHIVO, sb.toString().getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}

