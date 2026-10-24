package sv.edu.ues.dam235.compucel.datos;

import androidx.lifecycle.LiveData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;

import sv.edu.ues.dam235.compucel.datos.local.EstadoSync;
import sv.edu.ues.dam235.compucel.datos.local.ServicioDao;
import sv.edu.ues.dam235.compucel.datos.local.ServicioEntity;
import sv.edu.ues.dam235.compucel.datos.remoto.RemotoDatos;
import sv.edu.ues.dam235.compucel.util.Red;

/** Gestion local/remota de servicios. Primero guarda en Room y deja pendientes los fallos de nube. */
public class ServicioRepositorio {

    private final ServicioDao dao;
    private final RemotoDatos remoto;
    private final Red.ConexionDisponible conexion;
    private final Executor executor;

    public ServicioRepositorio(ServicioDao dao, RemotoDatos remoto, Red.ConexionDisponible conexion, Executor executor) {
        this.dao = dao;
        this.remoto = remoto;
        this.conexion = conexion;
        this.executor = executor;
    }

    public void guardar(ServicioEntity servicio, Resultado<Boolean> resultado) {
        trabajo(resultado, () -> {
            servicio.estadoSync = EstadoSync.PENDIENTE;
            servicio.eliminado = false;
            servicio.actualizadoEn = System.currentTimeMillis();
            dao.guardar(servicio);
            resultado.exito(enviarSinEsperarMas(servicio));
        });
    }

    public void eliminar(String id, Resultado<Boolean> resultado) {
        trabajo(resultado, () -> {
            ServicioEntity servicio = dao.buscar(id);
            if (servicio == null) {
                resultado.error(new IllegalStateException("El servicio ya no existe"));
                return;
            }
            servicio.eliminado = true;
            servicio.estadoSync = EstadoSync.PENDIENTE;
            servicio.actualizadoEn = System.currentTimeMillis();
            dao.guardar(servicio);
            resultado.exito(enviarSinEsperarMas(servicio));
        });
    }

    public LiveData<List<ServicioEntity>> lista() {
        return dao.observarLista();
    }

    public LiveData<ServicioEntity> observar(String id) {
        return dao.observar(id);
    }

    public LiveData<Integer> pendientes() {
        return dao.observarPendientes();
    }

    public void refrescarDesdeNube(Resultado<Integer> resultado) {
        trabajo(resultado, () -> {
            if (!conexion.hay()) {
                resultado.error(new ErrorNegocio(ErrorNegocio.Tipo.SIN_CONEXION));
                return;
            }
            try {
                List<ServicioEntity> nube = Espera.llamar((Resultado<List<ServicioEntity>> r) -> remoto.listarServicios(r));
                Set<String> idsNube = new HashSet<>();
                int actualizados = 0;
                for (ServicioEntity remotoServicio : nube) {
                    idsNube.add(remotoServicio.id);
                    ServicioEntity local = dao.buscar(remotoServicio.id);
                    if (local == null || EstadoSync.SINCRONIZADO.equals(local.estadoSync)) {
                        dao.guardar(remotoServicio);
                        actualizados++;
                    }
                }
                for (ServicioEntity local : dao.sincronizados()) {
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

    boolean enviar(ServicioEntity servicio) throws TimeoutException {
        if (!conexion.hay()) {
            return false;
        }
        try {
            if (servicio.eliminado) {
                Espera.llamar((Resultado<Void> r) -> remoto.eliminarServicio(servicio.id, r));
            } else {
                Espera.llamar((Resultado<Void> r) -> remoto.guardarServicio(servicio, r));
            }
        } catch (TimeoutException e) {
            throw e;
        } catch (Exception e) {
            return false;
        }
        ServicioEntity actual = dao.buscar(servicio.id);
        if (actual != null && actual.actualizadoEn == servicio.actualizadoEn) {
            if (actual.eliminado) {
                dao.borrar(actual.id);
            } else {
                actual.estadoSync = EstadoSync.SINCRONIZADO;
                dao.guardar(actual);
            }
        }
        return true;
    }

    private boolean enviarSinEsperarMas(ServicioEntity servicio) {
        try {
            return enviar(servicio);
        } catch (TimeoutException e) {
            return false;
        }
    }

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