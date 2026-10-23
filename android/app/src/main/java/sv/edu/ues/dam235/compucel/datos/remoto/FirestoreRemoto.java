package sv.edu.ues.dam235.compucel.datos.remoto;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.MemoryCacheSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.List;

import sv.edu.ues.dam235.compucel.datos.Resultado;
import sv.edu.ues.dam235.compucel.datos.local.ClienteEntity;
import sv.edu.ues.dam235.compucel.datos.local.UsuarioEntity;

/** Base de datos remota en Cloud Firestore (colecciones "usuarios" y "clientes"). */
public class FirestoreRemoto implements RemotoDatos {

    private static final String USUARIOS = "usuarios";
    private static final String CLIENTES = "clientes";

    private final FirebaseFirestore db;

    public FirestoreRemoto() {
        db = FirebaseFirestore.getInstance();
        // Sin cache propia de Firestore: la unica base local es Room y los pendientes se ven en la app
        db.setFirestoreSettings(new FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
                .build());
    }

    @Override
    public void guardarUsuario(UsuarioEntity usuario, Resultado<Void> resultado) {
        db.collection(USUARIOS).document(usuario.correo).set(usuario.toMap())
                .addOnCompleteListener(alTerminar(resultado));
    }

    @Override
    public void buscarUsuario(String correo, Resultado<UsuarioEntity> resultado) {
        db.collection(USUARIOS).document(correo).get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documento = task.getResult();
                            if (documento.exists() && documento.getData() != null) {
                                resultado.exito(UsuarioEntity.fromMap(documento.getData()));
                            } else {
                                resultado.exito(null);
                            }
                        } else {
                            resultado.error(task.getException());
                        }
                    }
                });
    }

    @Override
    public void guardarCliente(ClienteEntity cliente, Resultado<Void> resultado) {
        db.collection(CLIENTES).document(cliente.id).set(cliente.toMap())
                .addOnCompleteListener(alTerminar(resultado));
    }

    @Override
    public void eliminarCliente(String id, Resultado<Void> resultado) {
        db.collection(CLIENTES).document(id).delete()
                .addOnCompleteListener(alTerminar(resultado));
    }

    @Override
    public void listarClientes(Resultado<List<ClienteEntity>> resultado) {
        // Solo del servidor: sin red, la cache vacia haria creer que la nube no tiene clientes
        db.collection(CLIENTES).get(Source.SERVER)
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<ClienteEntity> lista = new ArrayList<>();
                            for (QueryDocumentSnapshot documento : task.getResult()) {
                                lista.add(ClienteEntity.fromMap(documento.getId(), documento.getData()));
                            }
                            resultado.exito(lista);
                        } else {
                            resultado.error(task.getException());
                        }
                    }
                });
    }

    private static OnCompleteListener<Void> alTerminar(Resultado<Void> resultado) {
        return new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    resultado.exito(null);
                } else {
                    resultado.error(task.getException());
                }
            }
        };
    }
}
