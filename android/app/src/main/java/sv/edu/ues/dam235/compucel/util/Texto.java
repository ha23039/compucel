package sv.edu.ues.dam235.compucel.util;

import java.util.Locale;

/** Formatos de texto de la interfaz (avatares de iniciales y telefono 0000-0000). */
public final class Texto {

    private Texto() {
    }

    /** Iniciales del primer y ultimo nombre, como en los avatares del diseno. */
    public static String iniciales(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "?";
        }
        String[] partes = nombre.trim().split("\\s+");
        String primera = partes[0].substring(0, 1);
        String ultima = partes.length > 1 ? partes[partes.length - 1].substring(0, 1) : "";
        return (primera + ultima).toUpperCase(Locale.ROOT);
    }

    /** Deja solo 8 digitos y agrega el guion despues del cuarto. */
    public static String formatoTelefono(String texto) {
        if (texto == null) {
            return "";
        }
        String digitos = texto.replaceAll("\\D", "");
        if (digitos.length() > 8) {
            digitos = digitos.substring(0, 8);
        }
        return digitos.length() > 4 ? digitos.substring(0, 4) + "-" + digitos.substring(4) : digitos;
    }
}
