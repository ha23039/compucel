package sv.edu.ues.dam235.compucel.datos;

import androidx.lifecycle.LiveData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;

import sv.edu.ues.dam235.compucel.datos.local.ClienteDao;
import sv.edu.ues.dam235.compucel.datos.local.ClienteEntity;
import sv.edu.ues.dam235.compucel.datos.local.EstadoSync;
import sv.edu.ues.dam235.compucel.datos.remoto.RemotoDatos;
import sv.edu.ues.dam235.compucel.util.Red;

/**
 * CRUD de clientes. Primero se escribe en Room y despues se envia a la nube.
 * Las respuestas (true = ya esta en la nube) llegan en el hilo de trabajo.
 */
public class ClienteRepositorio {

    private final ClienteDao dao;
    private final RemotoDatos remoto;
    private final Red.ConexionDisponible conexion;
    private final Executor executor;

    public ClienteRepositorio(ClienteDao dao, RemotoDatos remoto, Red.ConexionDisponible conexion, Executor executor) {
        this.dao = dao;
        this.remoto = remoto;
        this.conexion = conexion;
        this.executor = executor;
    }

    public void guardar(ClienteEntity cliente, Resultado<Boolean> resultado) {
        trabajo(resultado, () -> {
            cliente.estadoSync = EstadoSync.PENDIENTE;
            cliente.actualizadoEn = System.currentTimeMillis();
            dao.guardar(cliente);
            resultado.exito(enviarSinEsperarMas(cliente));
        });
    }

    public void cambiarActivo(String id, boolean activo, Resultado<Boolean> resultado) {
        trabajo(resultado, () -> {
            ClienteEntity cliente = dao.buscar(id);
            if (cliente == null) {
                resultado.error(new IllegalStateException("El cliente ya no existe"));
                return;
            }
            cliente.activo = activo;
            cliente.estadoSync = EstadoSync.PENDIENTE;
            cliente.actualizadoEn = System.currentTimeMillis();
            dao.guardar(cliente);
            resultado.exito(enviarSinEsperarMas(cliente));
        });
    }

    public void eliminar(String id, Resultado<Boolean> resultado) {
        trabajo(resultado, () -> {
            ClienteEntity cliente = dao.buscar(id);
            if (cliente == null) {
                resultado.error(new IllegalStateException("El cliente ya no existe"));
                return;
            }
            // Se marca como borrado hasta que la nube confirme
            cliente.eliminado = true;
            cliente.estadoSync = EstadoSync.PENDIENTE;
            cliente.actualizadoEn = System.currentTimeMillis();
            dao.guardar(cliente);
            resultado.exito(enviarSinEsperarMas(cliente));
        });
    }

    /** Trae los clientes de la nube sin tocar los que tienen cambios pendientes en el telefono. */
    public void refrescarDesdeNube(Resultado<Integer> resultado) {
        trabajo(resultado, () -> {
            if (!conexion.hay()) {
                resultado.error(new ErrorNegocio(ErrorNegocio.Tipo.SIN_CONEXION));
                return;
            }
            try {
                List<ClienteEntity> nube = Espera.llamar((Resultado<List<ClienteEntity>> r) -> remoto.listarClientes(r));
                Set<String> idsNube = new HashSet<>();
                int actualizados = 0;
                for (ClienteEntity remotoCliente : nube) {
                    idsNube.add(remotoCliente.id);
                    ClienteEntity local = dao.buscar(remotoCliente.id);
                    if (local == null || EstadoSync.SINCRONIZADO.equals(local.estadoSync)) {
                        dao.guardar(remotoCliente);
                        actualizados++;
                    }
                }
                // Los que ya estaban sincronizados y no existen en la nube se borraron desde otro telefono
                for (ClienteEntity local : dao.sincronizados()) {
                    if (!idsNube.contains(local.id)) {
                        dao.borrar(local.id);
                    }
                }
                resultado.exito(actualizados);
            } catch (Exception e) {
                resultado.error(e);
            }
        });
    }

    public LiveData<List<ClienteEntity>> lista(boolean activos, String filtro) {
        return dao.observarLista(activos, filtro == null ? "" : filtro.trim());
    }

    public LiveData<ClienteEntity> observar(String id) {
        return dao.observar(id);
    }

    public LiveData<Integer> pendientes() {
        return dao.observarPendientes();
    }

    public LiveData<Integer> totalActivos() {
        return dao.observarTotalActivos();
    }

    public LiveData<Integer> total() {
        return dao.observarTotal();
    }

    private boolean enviarSinEsperarMas(ClienteEntity cliente) {
        try {
            return enviar(cliente);
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * Envia un cambio (o un borrado) y solo lo marca sincronizado si no cambio entretanto.
     * Lanza TimeoutException si la nube no respondio, para que el Sincronizador no siga esperando.
     */
    boolean enviar(ClienteEntity cliente) throws TimeoutException {
        if (!conexion.hay()) {
            return false;
        }
        try {
            if (cliente.eliminado) {
                Espera.llamar((Resultado<Void> r) -> remoto.eliminarCliente(cliente.id, r));
            } else {
                Espera.llamar((Resultado<Void> r) -> remoto.guardarCliente(cliente, r));
            }
        } catch (TimeoutException e) {
            throw e;
        } catch (Exception e) {
            return false;
        }
        ClienteEntity actual = dao.buscar(cliente.id);
        if (actual != null && actual.actualizadoEn == cliente.actualizadoEn) {
            if (actual.eliminado) {
                dao.borrar(actual.id);
            } else {
                actual.estadoSync = EstadoSync.SINCRONIZADO;
                dao.guardar(actual);
            }
        }
        return true;
    }

    // Un error inesperado se informa a la pantalla en lugar de cerrar la app
    private void trabajo(Resultado<?> resultado, Runnable tarea) {
        executor.execute(() -> {
            try {
                tarea.run();
            } catch (RuntimeException e) {
                resultado.error(e);
            }
        });
    }
}
