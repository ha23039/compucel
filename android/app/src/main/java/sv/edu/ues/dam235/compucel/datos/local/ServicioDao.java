package sv.edu.ues.dam235.compucel.datos.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ServicioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void guardar(ServicioEntity servicio);

    @Query("SELECT * FROM servicios WHERE id = :id")
    ServicioEntity buscar(String id);

    @Query("SELECT * FROM servicios WHERE id = :id")
    LiveData<ServicioEntity> observar(String id);

    @Query("SELECT * FROM servicios WHERE eliminado = 0 ORDER BY nombre COLLATE NOCASE")
    LiveData<List<ServicioEntity>> observarLista();

    @Query("SELECT * FROM servicios WHERE estadoSync = 'PENDIENTE'")
    List<ServicioEntity> pendientes();

    @Query("SELECT * FROM servicios WHERE estadoSync = 'SINCRONIZADO'")
    List<ServicioEntity> sincronizados();

    @Query("SELECT COUNT(*) FROM servicios WHERE estadoSync = 'PENDIENTE'")
    LiveData<Integer> observarPendientes();

    @Query("DELETE FROM servicios WHERE id = :id")
    void borrar(String id);
}