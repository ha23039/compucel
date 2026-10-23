package sv.edu.ues.dam235.compucel.util;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Cifra textos con una clave AES guardada en el Android Keystore (la clave nunca sale del telefono).
 * Se usa para la contrasena recordada en SharedPreferences: a diferencia del hash BCrypt,
 * este cifrado se puede revertir para rellenar el campo del inicio de sesion.
 */
public class CifradoLocal {

    private static final String ALMACEN = "AndroidKeyStore";
    private static final String ALIAS = "compucel_recordarme";
    private static final String TRANSFORMACION = "AES/GCM/NoPadding";
    private static final int BITS_ETIQUETA = 128;

    /** Devuelve "iv:cifrado", ambos en Base64. */
    public String cifrar(String texto) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMACION);
            cipher.init(Cipher.ENCRYPT_MODE, obtenerClave());
            byte[] cifrado = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));
            return base64(cipher.getIV()) + ":" + base64(cifrado);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo cifrar", e);
        }
    }

    /** Devuelve el texto original, o null si el valor guardado no es valido. */
    public String descifrar(String guardado) {
        if (guardado == null) {
            return null;
        }
        try {
            String[] partes = guardado.split(":");
            if (partes.length != 2) {
                return null;
            }
            Cipher cipher = Cipher.getInstance(TRANSFORMACION);
            cipher.init(Cipher.DECRYPT_MODE, obtenerClave(),
                    new GCMParameterSpec(BITS_ETIQUETA, Base64.decode(partes[0], Base64.NO_WRAP)));
            byte[] texto = cipher.doFinal(Base64.decode(partes[1], Base64.NO_WRAP));
            return new String(texto, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private SecretKey obtenerClave() throws Exception {
        KeyStore almacen = KeyStore.getInstance(ALMACEN);
        almacen.load(null);
        if (almacen.containsAlias(ALIAS)) {
            return ((KeyStore.SecretKeyEntry) almacen.getEntry(ALIAS, null)).getSecretKey();
        }
        KeyGenerator generador = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ALMACEN);
        generador.init(new KeyGenParameterSpec.Builder(ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build());
        return generador.generateKey();
    }

    private static String base64(byte[] datos) {
        return Base64.encodeToString(datos, Base64.NO_WRAP);
    }
}
