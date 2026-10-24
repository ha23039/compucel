package sv.edu.ues.dam235.compucel.datos;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;

import sv.edu.ues.dam235.compucel.datos.local.ClienteDao;
import sv.edu.ues.dam235.compucel.datos.local.ClienteEntity;
import sv.edu.ues.dam235.compucel.datos.local.ServicioDao;
import sv.edu.ues.dam235.compucel.datos.local.ServicioEntity;
import sv.edu.ues.dam235.compucel.datos.local.UsuarioDao;
import sv.edu.ues.dam235.compucel.datos.local.UsuarioEntity;
import sv.edu.ues.dam235.compucel.datos.remoto.RemotoDatos;
import sv.edu.ues.dam235.compucel.util.Red;

/** Reenvia a la nube todo lo que quedo PENDIENTE. */
public class Sincronizador {

    private final UsuarioDao usuarioDao;
    private final ClienteDao clienteDao;
    private final ServicioDao servicioDao;
    private final UsuarioRepositorio usuarios;
    private final ClienteRepositorio clientes;
    private final ServicioRepositorio servicios;
    private final Red.ConexionDisponible conexion;
    private final Executor executor;

    public Sincronizador(UsuarioDao usuarioDao, ClienteDao clienteDao, ServicioDao servicioDao, RemotoDatos remoto,
                         Red.ConexionDisponible conexion, Executor executor) {
        this.usuarioDao = usuarioDao;
        this.clienteDao = clienteDao;
        this.servicioDao = servicioDao;
        this.conexion = conexion;
        this.executor = executor;
        this.usuarios = new UsuarioRepositorio(usuarioDao, remoto, conexion, Runnable::run);
        this.clientes = new ClienteRepositorio(clienteDao, remoto, conexion, Runnable::run);
        this.servicios = new ServicioRepositorio(servicioDao, remoto, conexion, Runnable::run);
    }

    public void sincronizarTodo(Resultado<ResumenSync> resultado) {
        executor.execute(() -> {
            if (!conexion.hay()) {
                resultado.error(new ErrorNegocio(ErrorNegocio.Tipo.SIN_CONEXION));
                return;
            }
            ResumenSync resumen = new ResumenSync();
            List<UsuarioEntity> usuariosPendientes = usuarioDao.pendientes();
            List<ClienteEntity> clientesPendientes = clienteDao.pendientes();
            List<ServicioEntity> serviciosPendientes = servicioDao.pendientes();
            int total = usuariosPendientes.size() + clientesPendientes.size() + serviciosPendientes.size();
            try {
                for (UsuarioEntity usuario : usuariosPendientes) {
                    if (usuarios.enviar(usuario)) resumen.enviados++; else fallo(resumen, "Perfil de " + usuario.correo);
                }
                for (ClienteEntity cliente : clientesPendientes) {
                    if (clientes.enviar(cliente)) resumen.enviados++; else fallo(resumen, "Cliente " + cliente.nombre);
                }
                for (ServicioEntity servicio : serviciosPendientes) {
                    if (servicios.enviar(servicio)) resumen.enviados++; else fallo(resumen, "Servicio " + servicio.nombre);
                }
            } catch (TimeoutException e) {
                resumen.fallidos = total - resumen.enviados;
                resumen.errores.add("Sin respuesta del servidor: no se pudo enviar");
            }
            resultado.exito(resumen);
        });
    }

    private static void fallo(ResumenSync resumen, String que) {
        resumen.fallidos++;
        resumen.errores.add(que + ": no se pudo enviar");
    }
}