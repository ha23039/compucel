package sv.edu.ues.dam235.compucel.datos.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.HashMap;
import java.util.Map;

/** Usuario de la app. La contrasena solo se guarda como hash BCrypt. */
@Entity(tableName = "usuarios")
public class UsuarioEntity {

    @PrimaryKey
    @NonNull
    public String correo = "";
    public String nombre;
    public String claveHash;
    public String estadoSync;
    public long actualizadoEn;

    /** Campos que se guardan en Firestore (el estado de sincronizacion es solo local). */
    public Map<String, Object> toMap() {
        Map<String, Object> datos = new HashMap<>();
        datos.put("correo", correo);
        datos.put("nombre", nombre);
        datos.put("claveHash", claveHash);
        datos.put("actualizadoEn", actualizadoEn);
        return datos;
    }

    public static UsuarioEntity fromMap(Map<String, Object> datos) {
        UsuarioEntity u = new UsuarioEntity();
        u.correo = String.valueOf(datos.get("correo"));
        u.nombre = (String) datos.get("nombre");
        u.claveHash = (String) datos.get("claveHash");
        Object fecha = datos.get("actualizadoEn");
        u.actualizadoEn = fecha instanceof Number ? ((Number) fecha).longValue() : 0L;
        u.estadoSync = EstadoSync.SINCRONIZADO;
        return u;
    }
}
