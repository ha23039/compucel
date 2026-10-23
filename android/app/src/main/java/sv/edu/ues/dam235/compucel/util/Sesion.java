package sv.edu.ues.dam235.compucel.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Datos de sesion guardados con SharedPreferences.
 * - "Recordarme": correo y contrasena (cifrada con CifradoLocal) para rellenar el inicio de sesion.
 * - Sesion activa: quien esta dentro de la app. Cerrar sesion no borra lo recordado.
 */
public class Sesion {

    private static final String ARCHIVO = "compucel_sesion";
    private static final String RECORDAR_CORREO = "recordar_correo";
    private static final String RECORDAR_CLAVE = "recordar_clave";
    private static final String ACTIVA_CORREO = "activa_correo";
    private static final String ACTIVA_NOMBRE = "activa_nombre";

    private final SharedPreferences preferencias;
    private final CifradoLocal cifrado = new CifradoLocal();

    public Sesion(Context context) {
        preferencias = context.getApplicationContext().getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE);
    }

    public void recordar(String correo, String clave) {
        preferencias.edit()
                .putString(RECORDAR_CORREO, correo)
                .putString(RECORDAR_CLAVE, cifrado.cifrar(clave))
                .apply();
    }

    public boolean hayRecordado() {
        return preferencias.contains(RECORDAR_CORREO) && preferencias.contains(RECORDAR_CLAVE);
    }

    public String correoRecordado() {
        return preferencias.getString(RECORDAR_CORREO, null);
    }

    public String claveRecordada() {
        return cifrado.descifrar(preferencias.getString(RECORDAR_CLAVE, null));
    }

    public void olvidar() {
        preferencias.edit().remove(RECORDAR_CORREO).remove(RECORDAR_CLAVE).apply();
    }

    public void abrir(String correo, String nombre) {
        preferencias.edit().putString(ACTIVA_CORREO, correo).putString(ACTIVA_NOMBRE, nombre).apply();
    }

    public boolean abierta() {
        return preferencias.contains(ACTIVA_CORREO);
    }

    public String correoActual() {
        return preferencias.getString(ACTIVA_CORREO, null);
    }

    public String nombreActual() {
        return preferencias.getString(ACTIVA_NOMBRE, null);
    }

    public void actualizarNombre(String nombre) {
        preferencias.edit().putString(ACTIVA_NOMBRE, nombre).apply();
    }

    public void cerrar() {
        preferencias.edit().remove(ACTIVA_CORREO).remove(ACTIVA_NOMBRE).apply();
    }
}
