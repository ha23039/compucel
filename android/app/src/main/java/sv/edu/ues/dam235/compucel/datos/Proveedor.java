package sv.edu.ues.dam235.compucel.datos;

import android.content.Context;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import sv.edu.ues.dam235.compucel.datos.local.AppDatabase;
import sv.edu.ues.dam235.compucel.datos.remoto.FirestoreRemoto;
import sv.edu.ues.dam235.compucel.datos.remoto.RemotoDatos;
import sv.edu.ues.dam235.compucel.util.Red;
import sv.edu.ues.dam235.compucel.util.Sesion;

/** Arma las piezas reales de datos (Room, Firestore y un hilo de trabajo compartido). */
public final class Proveedor {

    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();
    private static RemotoDatos remoto;

    private Proveedor() {
    }

    private static synchronized RemotoDatos remoto() {
        if (remoto == null) {
            remoto = new FirestoreRemoto();
        }
        return remoto;
    }

    public static UsuarioRepositorio usuarios(Context context) {
        Context app = context.getApplicationContext();
        return new UsuarioRepositorio(AppDatabase.obtener(app).usuarioDao(), remoto(), () -> Red.hayInternet(app), EXECUTOR);
    }

    public static ClienteRepositorio clientes(Context context) {
        Context app = context.getApplicationContext();
        return new ClienteRepositorio(AppDatabase.obtener(app).clienteDao(), remoto(), () -> Red.hayInternet(app), EXECUTOR);
    }

    public static ServicioRepositorio servicios(Context context) {
        Context app = context.getApplicationContext();
        return new ServicioRepositorio(AppDatabase.obtener(app).servicioDao(), remoto(), () -> Red.hayInternet(app), EXECUTOR);
    }

    public static Sincronizador sincronizador(Context context) {
        Context app = context.getApplicationContext();
        AppDatabase db = AppDatabase.obtener(app);
        return new Sincronizador(db.usuarioDao(), db.clienteDao(), db.servicioDao(), remoto(), () -> Red.hayInternet(app), EXECUTOR);
    }

    public static Sesion sesion(Context context) {
        return new Sesion(context);
    }
}
