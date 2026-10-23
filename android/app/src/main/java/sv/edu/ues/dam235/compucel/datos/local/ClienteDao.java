package sv.edu.ues.dam235.compucel.datos.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ClienteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void guardar(ClienteEntity cliente);

    @Query("SELECT * FROM clientes WHERE id = :id")
    ClienteEntity buscar(String id);

    @Query("SELECT * FROM clientes WHERE id = :id")
    LiveData<ClienteEntity> observar(String id);

    // Busqueda por nombre o telefono, sin los borrados pendientes de confirmar
    @Query("SELECT * FROM clientes WHERE eliminado = 0 AND activo = :activo"
            + " AND (nombre LIKE '%' || :filtro || '%' OR telefono LIKE '%' || :filtro || '%')"
            + " ORDER BY nombre COLLATE NOCASE")
    LiveData<List<ClienteEntity>> observarLista(boolean activo, String filtro);

    /** Incluye los borrados que faltan por enviar a la nube. */
    @Query("SELECT * FROM clientes WHERE estadoSync = 'PENDIENTE'")
    List<ClienteEntity> pendientes();

    @Query("SELECT COUNT(*) FROM clientes WHERE estadoSync = 'PENDIENTE'")
    LiveData<Integer> observarPendientes();

    @Query("SELECT COUNT(*) FROM clientes WHERE eliminado = 0 AND activo = 1")
    LiveData<Integer> observarTotalActivos();

    @Query("SELECT COUNT(*) FROM clientes WHERE eliminado = 0")
    LiveData<Integer> observarTotal();

    @Query("DELETE FROM clientes WHERE id = :id")
    void borrar(String id);
}
