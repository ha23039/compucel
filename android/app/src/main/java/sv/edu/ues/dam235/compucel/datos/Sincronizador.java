package sv.edu.ues.dam235.compucel.datos;

import java.util.concurrent.Executor;

import sv.edu.ues.dam235.compucel.datos.local.ClienteDao;
import sv.edu.ues.dam235.compucel.datos.local.ClienteEntity;
import sv.edu.ues.dam235.compucel.datos.local.UsuarioDao;
import sv.edu.ues.dam235.compucel.datos.local.UsuarioEntity;
import sv.edu.ues.dam235.compucel.datos.remoto.RemotoDatos;
import sv.edu.ues.dam235.compucel.util.Red;

/** Reenvia a la nube todo lo que quedo PENDIENTE (se usa desde Perfil y Sincronizacion). */
public class Sincronizador {

    private final UsuarioDao usuarioDao;
    private final ClienteDao clienteDao;
    private final UsuarioRepositorio usuarios;
    private final ClienteRepositorio clientes;
    private final Red.ConexionDisponible conexion;
    private final Executor executor;

    public Sincronizador(UsuarioDao usuarioDao, ClienteDao clienteDao, RemotoDatos remoto,
                         Red.ConexionDisponible conexion, Executor executor) {
        this.usuarioDao = usuarioDao;
        this.clienteDao = clienteDao;
        this.conexion = conexion;
        this.executor = executor;
        // Se usan solo para enviar; el trabajo ya corre en el executor de este objeto
        this.usuarios = new UsuarioRepositorio(usuarioDao, remoto, conexion, Runnable::run);
        this.clientes = new ClienteRepositorio(clienteDao, remoto, conexion, Runnable::run);
    }

    public void sincronizarTodo(Resultado<ResumenSync> resultado) {
        executor.execute(() -> {
            if (!conexion.hay()) {
                resultado.error(new ErrorNegocio(ErrorNegocio.Tipo.SIN_CONEXION));
                return;
            }
            ResumenSync resumen = new ResumenSync();
            for (UsuarioEntity usuario : usuarioDao.pendientes()) {
                if (usuarios.enviar(usuario)) {
                    resumen.enviados++;
                } else {
                    resumen.fallidos++;
                    resumen.errores.add("Perfil de " + usuario.correo + ": no se pudo enviar");
                }
            }
            for (ClienteEntity cliente : clienteDao.pendientes()) {
                if (clientes.enviar(cliente)) {
                    resumen.enviados++;
                } else {
                    resumen.fallidos++;
                    resumen.errores.add("Cliente " + cliente.nombre + ": no se pudo enviar");
                }
            }
            resultado.exito(resumen);
        });
    }
}
