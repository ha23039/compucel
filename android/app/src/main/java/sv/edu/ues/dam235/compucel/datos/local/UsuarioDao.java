package sv.edu.ues.dam235.compucel.datos.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void guardar(UsuarioEntity usuario);

    @Query("SELECT * FROM usuarios WHERE correo = :correo")
    UsuarioEntity buscar(String correo);

    @Query("SELECT * FROM usuarios WHERE estadoSync = 'PENDIENTE'")
    List<UsuarioEntity> pendientes();

    @Query("SELECT COUNT(*) FROM usuarios WHERE estadoSync = 'PENDIENTE'")
    LiveData<Integer> observarPendientes();
}
