package sv.edu.ues.dam235.compucel.datos.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.annotation.NonNull;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/** Base de datos local (SQLite con Room). */
@Database(entities = {UsuarioEntity.class, ClienteEntity.class, ServicioEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final String NOMBRE = "compucel.db";
    private static final Migration MIGRACION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `servicios` (`id` TEXT NOT NULL, `nombre` TEXT, `descripcion` TEXT, `costo` REAL NOT NULL, `tiempoMinutos` INTEGER NOT NULL, `estadoSync` TEXT, `eliminado` INTEGER NOT NULL, `actualizadoEn` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        }
    };
    private static volatile AppDatabase instancia;

    public abstract UsuarioDao usuarioDao();

    public abstract ClienteDao clienteDao();

    public abstract ServicioDao servicioDao();

    public static AppDatabase obtener(Context context) {
        if (instancia == null) {
            synchronized (AppDatabase.class) {
                if (instancia == null) {
                    instancia = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, NOMBRE).addMigrations(MIGRACION_1_2).build();
                }
            }
        }
        return instancia;
    }
}
