import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

public class Cuentas {

    // cada linea del archivo: usuario;correo;sal;hash
    private static final Path ARCHIVO = Paths.get("cuentas.txt");
    private static final Pattern USUARIO = Pattern.compile("[A-Za-z0-9_.-]{3,20}");
    private static final Pattern CORREO = Pattern.compile("[^\\s@;]+@[^\\s@;]+\\.[^\\s@;]+");
    private static final int MIN_CONTRASENA = 6;
    private static final int ITERACIONES = 65536;

    private static class Cuenta {
        String usuario;
        String correo;
        String sal;
        String hash;
    }

    // devuelve null si se creo bien, o el mensaje de error
    public static String registrar(String usuario, String correo, String contrasena) {
        if (!USUARIO.matcher(usuario).matches()) {
            return "Usuario: de 3 a 20 caracteres (letras, números, . _ -).";
        }
        if (!CORREO.matcher(correo).matches()) {
            return "Escribe un correo válido.";
        }
        if (contrasena.length() < MIN_CONTRASENA) {
            return "La contraseña debe tener al menos " + MIN_CONTRASENA + " caracteres.";
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
            byte[] sal = new byte[16];
            new SecureRandom().nextBytes(sal);
            String linea = usuario + ";" + correo + ";"
                    + Base64.getEncoder().encodeToString(sal) + ";"
                    + Base64.getEncoder().encodeToString(calcularHash(contrasena, sal))
                    + System.lineSeparator();
            Files.write(ARCHIVO, linea.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return null;
        } catch (IOException | GeneralSecurityException e) {
            return "No se pudo guardar la cuenta.";
        }
    }

    // devuelve el nombre de usuario si los datos son correctos, o null
    public static String validar(String usuarioOCorreo, String contrasena) {
        try {
            for (Cuenta c : cargar()) {
                boolean coincide = c.usuario.equalsIgnoreCase(usuarioOCorreo)
                        || c.correo.equalsIgnoreCase(usuarioOCorreo);
                if (!coincide) {
                    continue;
                }
                byte[] sal = Base64.getDecoder().decode(c.sal);
                byte[] esperado = Base64.getDecoder().decode(c.hash);
                if (MessageDigest.isEqual(esperado, calcularHash(contrasena, sal))) {
                    return c.usuario;
                }
                return null;
            }
        } catch (IOException | GeneralSecurityException | IllegalArgumentException e) {
            return null;
        }
        return null;
    }

    private static List<Cuenta> cargar() throws IOException {
        List<Cuenta> lista = new ArrayList<>();
        if (!Files.exists(ARCHIVO)) {
            return lista;
        }
        for (String linea : Files.readAllLines(ARCHIVO, StandardCharsets.UTF_8)) {
            String[] partes = linea.split(";", -1);
            if (partes.length != 4) {
                continue;
            }
            Cuenta c = new Cuenta();
            c.usuario = partes[0];
            c.correo = partes[1];
            c.sal = partes[2];
            c.hash = partes[3];
            lista.add(c);
        }
        return lista;
    }

    private static byte[] calcularHash(String contrasena, byte[] sal) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(contrasena.toCharArray(), sal, ITERACIONES, 256);
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    }
}
