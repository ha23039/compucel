package sv.edu.ues.dam235.compucel.datos.remoto;

import java.util.List;

import sv.edu.ues.dam235.compucel.datos.Resultado;
import sv.edu.ues.dam235.compucel.datos.local.ClienteEntity;
import sv.edu.ues.dam235.compucel.datos.local.UsuarioEntity;

/**
 * Operaciones con la base de datos remota. Hoy la implementa Firestore;
 * en el proyecto final la implementara el cliente de la API REST de CompuCel.
 */
public interface RemotoDatos {

    void guardarUsuario(UsuarioEntity usuario, Resultado<Void> resultado);

    /** Devuelve null en exito si el usuario no existe. */
    void buscarUsuario(String correo, Resultado<UsuarioEntity> resultado);

    void guardarCliente(ClienteEntity cliente, Resultado<Void> resultado);

    void eliminarCliente(String id, Resultado<Void> resultado);

    void listarClientes(Resultado<List<ClienteEntity>> resultado);
}
