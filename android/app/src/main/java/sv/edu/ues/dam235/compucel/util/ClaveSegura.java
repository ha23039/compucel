package sv.edu.ues.dam235.compucel.util;

import java.nio.charset.StandardCharsets;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Hash de contrasenas con BCrypt, el mismo formato ($2a$10$...) que usa la API de la clase.
 * La contrasena nunca se guarda en texto plano: solo se guarda y se compara su hash.
 */
public final class ClaveSegura {

    private static final int COSTO = 10;

    private ClaveSegura() {
    }

    public static String hash(String clave) {
        return BCrypt.with(BCrypt.Version.VERSION_2A).hashToString(COSTO, clave.toCharArray());
    }

    public static boolean verificar(String clave, String hash) {
        if (clave == null || hash == null || hash.isEmpty()) {
            return false;
        }
        return BCrypt.verifyer().verify(clave.toCharArray(), hash.getBytes(StandardCharsets.UTF_8)).verified;
    }
}
