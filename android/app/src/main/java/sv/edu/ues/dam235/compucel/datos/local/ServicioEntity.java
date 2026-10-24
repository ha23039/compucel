package sv.edu.ues.dam235.compucel.datos.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Servicio que ofrece el taller. El mismo UUID se conserva en Room y Firestore. */
@Entity(tableName = "servicios")
public class ServicioEntity {

    @PrimaryKey
    @NonNull
    public String id = "";
    public String nombre;
    public String descripcion;
    public double costo;
    public int tiempoMinutos;
    public String estadoSync;
    public boolean eliminado;
    public long actualizadoEn;

    public static ServicioEntity nuevo() {
        ServicioEntity servicio = new ServicioEntity();
        servicio.id = UUID.randomUUID().toString();
        servicio.estadoSync = EstadoSync.PENDIENTE;
        servicio.actualizadoEn = System.currentTimeMillis();
        return servicio;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> datos = new HashMap<>();
        datos.put("nombre", nombre);
        datos.put("descripcion", descripcion);
        datos.put("costo", costo);
        datos.put("tiempoMinutos", tiempoMinutos);
        datos.put("actualizadoEn", actualizadoEn);
        return datos;
    }

    public static ServicioEntity fromMap(String id, Map<String, Object> datos) {
        ServicioEntity servicio = new ServicioEntity();
        servicio.id = id;
        servicio.nombre = (String) datos.get("nombre");
        servicio.descripcion = (String) datos.get("descripcion");
        Object costo = datos.get("costo");
        servicio.costo = costo instanceof Number ? ((Number) costo).doubleValue() : 0d;
        Object tiempo = datos.get("tiempoMinutos");
        servicio.tiempoMinutos = tiempo instanceof Number ? ((Number) tiempo).intValue() : 0;
        Object fecha = datos.get("actualizadoEn");
        servicio.actualizadoEn = fecha instanceof Number ? ((Number) fecha).longValue() : 0L;
        servicio.estadoSync = EstadoSync.SINCRONIZADO;
        return servicio;
    }
}