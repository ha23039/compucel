package sv.edu.ues.dam235.compucel.util;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Reglas de validacion de los formularios (enunciado del parcial y PRD de CompuCel).
 */
public final class Validaciones {

    // Equivalente a Patterns.EMAIL_ADDRESS, pero usable tambien en pruebas sin Android
    private static final Pattern CORREO = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    // De 8 a 72 caracteres (72 es el maximo de BCrypt), solo letras y numeros, con al menos una de cada uno
    private static final Pattern CLAVE = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,72}$");
    private static final Pattern TELEFONO = Pattern.compile("^\\d{4}-\\d{4}$");
    private static final int LARGO_MINIMO_NOMBRE = 7;

    private Validaciones() {
    }

    public static String normalizarCorreo(String correo) {
        if (correo == null) {
            return "";
        }
        return correo.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean correoValido(String correo) {
        return CORREO.matcher(normalizarCorreo(correo)).matches();
    }

    public static boolean claveValida(String clave) {
        return clave != null && CLAVE.matcher(clave).matches();
    }

    public static boolean nombreValido(String nombre) {
        return nombre != null && nombre.trim().length() >= LARGO_MINIMO_NOMBRE;
    }

    public static boolean telefonoValido(String telefono) {
        return telefono != null && TELEFONO.matcher(telefono.trim()).matches();
    }
}
