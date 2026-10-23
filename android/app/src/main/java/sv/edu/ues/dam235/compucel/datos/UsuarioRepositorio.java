package sv.edu.ues.dam235.compucel.datos;

import androidx.lifecycle.LiveData;

import java.util.concurrent.Executor;

import sv.edu.ues.dam235.compucel.datos.local.EstadoSync;
import sv.edu.ues.dam235.compucel.datos.local.UsuarioDao;
import sv.edu.ues.dam235.compucel.datos.local.UsuarioEntity;
import sv.edu.ues.dam235.compucel.datos.remoto.RemotoDatos;
import sv.edu.ues.dam235.compucel.util.ClaveSegura;
import sv.edu.ues.dam235.compucel.util.Red;
import sv.edu.ues.dam235.compucel.util.Validaciones;

/**
 * Registro, inicio de sesion y perfil. Siempre se guarda primero en Room;
 * si la nube no responde, el cambio queda PENDIENTE para la sincronizacion.
 * Las respuestas llegan en el hilo de trabajo: la interfaz debe usar runOnUiThread.
 */
public class UsuarioRepositorio {

    private final UsuarioDao dao;
    private final RemotoDatos remoto;
    private final Red.ConexionDisponible conexion;
    private final Executor executor;

    public UsuarioRepositorio(UsuarioDao dao, RemotoDatos remoto, Red.ConexionDisponible conexion, Executor executor) {
        this.dao = dao;
        this.remoto = remoto;
        this.conexion = conexion;
        this.executor = executor;
    }

    public void registrar(String correo, String nombre, String clave, Resultado<UsuarioEntity> resultado) {
        executor.execute(() -> {
            String id = Validaciones.normalizarCorreo(correo);
            // El registro necesita internet para comprobar que el correo no exista en la nube
            if (!conexion.hay()) {
                resultado.error(new ErrorNegocio(ErrorNegocio.Tipo.SIN_CONEXION));
                return;
            }
            try {
                if (dao.buscar(id) != null || Espera.llamar((Resultado<UsuarioEntity> r) -> remoto.buscarUsuario(id, r)) != null) {
                    resultado.error(new ErrorNegocio(ErrorNegocio.Tipo.CORREO_EXISTE));
                    return;
                }
            } catch (Exception e) {
                resultado.error(e);
                return;
            }
            UsuarioEntity usuario = new UsuarioEntity();
            usuario.correo = id;
            usuario.nombre = nombre.trim();
            usuario.claveHash = ClaveSegura.hash(clave);
            guardarYEnviar(usuario);
            resultado.exito(dao.buscar(id));
        });
    }

    public void iniciarSesion(String correo, String clave, Resultado<UsuarioEntity> resultado) {
        executor.execute(() -> {
            String id = Validaciones.normalizarCorreo(correo);
            UsuarioEntity local = dao.buscar(id);
            if (local != null && ClaveSegura.verificar(clave, local.claveHash)) {
                resultado.exito(local);
                if (EstadoSync.SINCRONIZADO.equals(local.estadoSync) && conexion.hay()) {
                    refrescarDesdeNube(local);
                }
                return;
            }
            // Un cambio local pendiente manda: no se compara contra la nube
            if (local != null && EstadoSync.PENDIENTE.equals(local.estadoSync)) {
                resultado.error(new ErrorNegocio(ErrorNegocio.Tipo.CREDENCIALES));
                return;
            }
            if (!conexion.hay()) {
                resultado.error(new ErrorNegocio(local == null
                        ? ErrorNegocio.Tipo.SIN_CUENTA_LOCAL : ErrorNegocio.Tipo.CREDENCIALES));
                return;
            }
            try {
                UsuarioEntity nube = Espera.llamar((Resultado<UsuarioEntity> r) -> remoto.buscarUsuario(id, r));
                if (nube != null && ClaveSegura.verificar(clave, nube.claveHash)) {
                    dao.guardar(nube);
                    resultado.exito(nube);
                } else {
                    resultado.error(new ErrorNegocio(ErrorNegocio.Tipo.CREDENCIALES));
                }
            } catch (Exception e) {
                resultado.error(e);
            }
        });
    }

    /** Devuelve true si el cambio ya llego a la nube; false si quedo pendiente. */
    public void actualizarNombre(String correo, String nombre, Resultado<Boolean> resultado) {
        executor.execute(() -> {
            UsuarioEntity usuario = dao.buscar(Validaciones.normalizarCorreo(correo));
            usuario.nombre = nombre.trim();
            resultado.exito(guardarYEnviar(usuario));
        });
    }

    public void cambiarClave(String correo, String actual, String nueva, Resultado<Boolean> resultado) {
        executor.execute(() -> {
            UsuarioEntity usuario = dao.buscar(Validaciones.normalizarCorreo(correo));
            if (usuario == null || !ClaveSegura.verificar(actual, usuario.claveHash)) {
                resultado.error(new ErrorNegocio(ErrorNegocio.Tipo.CREDENCIALES));
                return;
            }
            usuario.claveHash = ClaveSegura.hash(nueva);
            resultado.exito(guardarYEnviar(usuario));
        });
    }

    public LiveData<Integer> pendientes() {
        return dao.observarPendientes();
    }

    private boolean guardarYEnviar(UsuarioEntity usuario) {
        usuario.estadoSync = EstadoSync.PENDIENTE;
        usuario.actualizadoEn = System.currentTimeMillis();
        dao.guardar(usuario);
        return enviar(usuario);
    }

    /** Envia a la nube y marca SINCRONIZADO solo si no hubo otro cambio mientras tanto. */
    boolean enviar(UsuarioEntity usuario) {
        if (!conexion.hay()) {
            return false;
        }
        try {
            Espera.llamar((Resultado<Void> r) -> remoto.guardarUsuario(usuario, r));
        } catch (Exception e) {
            return false;
        }
        UsuarioEntity actual = dao.buscar(usuario.correo);
        if (actual != null && actual.actualizadoEn == usuario.actualizadoEn) {
            actual.estadoSync = EstadoSync.SINCRONIZADO;
            dao.guardar(actual);
        }
        return true;
    }

    // Trae el nombre y la clave por si se cambiaron desde otro telefono
    private void refrescarDesdeNube(UsuarioEntity local) {
        try {
            UsuarioEntity nube = Espera.llamar((Resultado<UsuarioEntity> r) -> remoto.buscarUsuario(local.correo, r));
            UsuarioEntity actual = dao.buscar(local.correo);
            if (nube != null && actual != null && EstadoSync.SINCRONIZADO.equals(actual.estadoSync)
                    && nube.actualizadoEn > actual.actualizadoEn) {
                dao.guardar(nube);
            }
        } catch (Exception ignorado) {
            // Sin respuesta de la nube se conserva la copia local
        }
    }
}
