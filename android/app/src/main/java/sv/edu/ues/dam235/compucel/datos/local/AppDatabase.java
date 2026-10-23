package sv.edu.ues.dam235.compucel.datos.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/** Base de datos local (SQLite con Room). */
@Database(entities = {UsuarioEntity.class, ClienteEntity.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String NOMBRE = "compucel.db";
    private static volatile AppDatabase instancia;

    public abstract UsuarioDao usuarioDao();

    public abstract ClienteDao clienteDao();

    public static AppDatabase obtener(Context context) {
        if (instancia == null) {
            synchronized (AppDatabase.class) {
                if (instancia == null) {
                    instancia = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, NOMBRE).build();
                }
            }
        }
        return instancia;
    }
}
