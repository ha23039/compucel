package sv.edu.ues.dam235.compucel.datos.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Cliente del taller. El id se genera en el telefono y se usa igual en Firestore. */
@Entity(tableName = "clientes")
public class ClienteEntity {

    @PrimaryKey
    @NonNull
    public String id = "";
    public String nombre;
    public String telefono;
    public String correo;
    public String direccion;
    public boolean activo;
    public String creadoPor;
    public String estadoSync;
    /** Borrado local que todavia no se confirma en la nube. */
    public boolean eliminado;
    public long actualizadoEn;

    public static ClienteEntity nuevo(String creadoPor) {
        ClienteEntity c = new ClienteEntity();
        c.id = UUID.randomUUID().toString();
        c.activo = true;
        c.creadoPor = creadoPor;
        c.estadoSync = EstadoSync.PENDIENTE;
        c.actualizadoEn = System.currentTimeMillis();
        return c;
    }

    /** Campos que se guardan en Firestore. */
    public Map<String, Object> toMap() {
        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", nombre);
        datos.put("telefono", telefono);
        datos.put("correo", correo);
        datos.put("direccion", direccion);
        datos.put("activo", activo);
        datos.put("creadoPor", creadoPor);
        datos.put("actualizadoEn", actualizadoEn);
        return datos;
    }

    public static ClienteEntity fromMap(String id, Map<String, Object> datos) {
        ClienteEntity c = new ClienteEntity();
        c.id = id;
        c.nombre = (String) datos.get("nombre");
        c.telefono = (String) datos.get("telefono");
        c.correo = (String) datos.get("correo");
        c.direccion = (String) datos.get("direccion");
        c.activo = !Boolean.FALSE.equals(datos.get("activo"));
        c.creadoPor = (String) datos.get("creadoPor");
        Object fecha = datos.get("actualizadoEn");
        c.actualizadoEn = fecha instanceof Number ? ((Number) fecha).longValue() : 0L;
        c.estadoSync = EstadoSync.SINCRONIZADO;
        return c;
    }
}
