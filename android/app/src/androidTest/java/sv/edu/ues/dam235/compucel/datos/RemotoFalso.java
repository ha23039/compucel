package sv.edu.ues.dam235.compucel.datos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import sv.edu.ues.dam235.compucel.datos.local.ClienteEntity;
import sv.edu.ues.dam235.compucel.datos.local.UsuarioEntity;
import sv.edu.ues.dam235.compucel.datos.remoto.RemotoDatos;

/** Nube en memoria para las pruebas. Puede fallar o no responder nunca. */
public class RemotoFalso implements RemotoDatos {

    public final Map<String, Map<String, Object>> usuarios = new ConcurrentHashMap<>();
    public final Map<String, Map<String, Object>> clientes = new ConcurrentHashMap<>();
    public volatile boolean fallar;
    public volatile boolean noResponder;
    public volatile int escriturasCliente;

    private boolean responder(Resultado<?> resultado) {
        if (noResponder) {
            return false;
        }
        if (fallar) {
            resultado.error(new Exception("Servidor no disponible"));
            return false;
        }
        return true;
    }

    @Override
    public void guardarUsuario(UsuarioEntity usuario, Resultado<Void> resultado) {
        if (responder(resultado)) {
            usuarios.put(usuario.correo, usuario.toMap());
            resultado.exito(null);
        }
    }

    @Override
    public void buscarUsuario(String correo, Resultado<UsuarioEntity> resultado) {
        if (responder(resultado)) {
            Map<String, Object> datos = usuarios.get(correo);
            resultado.exito(datos == null ? null : UsuarioEntity.fromMap(datos));
        }
    }

    @Override
    public void guardarCliente(ClienteEntity cliente, Resultado<Void> resultado) {
        if (responder(resultado)) {
            clientes.put(cliente.id, cliente.toMap());
            escriturasCliente++;
            resultado.exito(null);
        }
    }

    @Override
    public void eliminarCliente(String id, Resultado<Void> resultado) {
        if (responder(resultado)) {
            clientes.remove(id);
            resultado.exito(null);
        }
    }

    @Override
    public void listarClientes(Resultado<List<ClienteEntity>> resultado) {
        if (responder(resultado)) {
            List<ClienteEntity> lista = new ArrayList<>();
            for (Map.Entry<String, Map<String, Object>> e : clientes.entrySet()) {
                lista.add(ClienteEntity.fromMap(e.getKey(), e.getValue()));
            }
            resultado.exito(lista);
        }
    }
}
