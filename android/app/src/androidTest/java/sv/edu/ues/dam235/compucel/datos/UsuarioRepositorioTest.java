package sv.edu.ues.dam235.compucel.datos;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import sv.edu.ues.dam235.compucel.datos.local.AppDatabase;
import sv.edu.ues.dam235.compucel.datos.local.EstadoSync;
import sv.edu.ues.dam235.compucel.datos.local.UsuarioEntity;
import sv.edu.ues.dam235.compucel.util.ClaveSegura;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class UsuarioRepositorioTest {

    private AppDatabase db;
    private RemotoFalso remoto;
    private final AtomicBoolean red = new AtomicBoolean(true);
    private UsuarioRepositorio repo;

    @Before
    public void crear() {
        Context ctx = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase.class).allowMainThreadQueries().build();
        remoto = new RemotoFalso();
        repo = new UsuarioRepositorio(db.usuarioDao(), remoto, red::get, Runnable::run);
    }

    @After
    public void cerrar() {
        db.close();
    }

    private Esperar.Respuesta<UsuarioEntity> registrar(String correo) throws Exception {
        return Esperar.respuesta(r -> repo.registrar(correo, "Erick Hernandez", "abcd1234", r));
    }

    @Test
    public void registroGuardaHashEnLocalYNube() throws Exception {
        assertNull(registrar("erick@ues.edu.sv").error);
        UsuarioEntity local = db.usuarioDao().buscar("erick@ues.edu.sv");
        assertTrue(local.claveHash.startsWith("$2a$"));
        assertNotEquals("abcd1234", local.claveHash);
        assertEquals(EstadoSync.SINCRONIZADO, local.estadoSync);
        String hashNube = (String) remoto.usuarios.get("erick@ues.edu.sv").get("claveHash");
        assertTrue(hashNube.startsWith("$2a$"));
        assertTrue(ClaveSegura.verificar("abcd1234", hashNube));
    }

    @Test
    public void registroSinRedFallaConSinConexion() throws Exception {
        red.set(false);
        assertEquals(ErrorNegocio.Tipo.SIN_CONEXION, Esperar.tipo(registrar("erick@ues.edu.sv")));
        assertNull(db.usuarioDao().buscar("erick@ues.edu.sv"));
    }

    @Test
    public void registroCorreoRepetidoFallaConCorreoExiste() throws Exception {
        registrar("erick@ues.edu.sv");
        assertEquals(ErrorNegocio.Tipo.CORREO_EXISTE, Esperar.tipo(registrar("erick@ues.edu.sv")));
    }

    @Test
    public void registroCorreoQueYaEstaEnLaNubeFalla() throws Exception {
        UsuarioEntity otro = new UsuarioEntity();
        otro.correo = "erick@ues.edu.sv";
        otro.nombre = "Otra Persona";
        otro.claveHash = ClaveSegura.hash("zzzz9999");
        remoto.usuarios.put(otro.correo, otro.toMap());
        assertEquals(ErrorNegocio.Tipo.CORREO_EXISTE, Esperar.tipo(registrar("erick@ues.edu.sv")));
    }

    @Test
    public void registroNormalizaCorreo() throws Exception {
        assertNull(registrar(" Erick@UES.edu.sv ").error);
        assertNotNull(db.usuarioDao().buscar("erick@ues.edu.sv"));
        assertNotNull(remoto.usuarios.get("erick@ues.edu.sv"));
    }

    @Test
    public void registroConNubeQueFallaQuedaPendiente() throws Exception {
        remoto.usuarios.clear();
        RemotoFalso soloFallaAlGuardar = new RemotoFalso() {
            @Override
            public void guardarUsuario(UsuarioEntity usuario, Resultado<Void> resultado) {
                resultado.error(new Exception("Servidor no disponible"));
            }
        };
        repo = new UsuarioRepositorio(db.usuarioDao(), soloFallaAlGuardar, red::get, Runnable::run);
        assertNull(registrar("erick@ues.edu.sv").error);
        assertEquals(EstadoSync.PENDIENTE, db.usuarioDao().buscar("erick@ues.edu.sv").estadoSync);
    }

    @Test
    public void loginSinRedConCuentaLocalFunciona() throws Exception {
        registrar("erick@ues.edu.sv");
        red.set(false);
        Esperar.Respuesta<UsuarioEntity> r = Esperar.respuesta(c -> repo.iniciarSesion(" ERICK@ues.edu.sv", "abcd1234", c));
        assertNull(r.error);
        assertEquals("Erick Hernandez", r.valor.nombre);
    }

    @Test
    public void loginSinRedSinCuentaLocalFallaSinCuentaLocal() throws Exception {
        red.set(false);
        Esperar.Respuesta<UsuarioEntity> r = Esperar.respuesta(c -> repo.iniciarSesion("erick@ues.edu.sv", "abcd1234", c));
        assertEquals(ErrorNegocio.Tipo.SIN_CUENTA_LOCAL, Esperar.tipo(r));
    }

    @Test
    public void loginConClaveMalaFallaCredenciales() throws Exception {
        registrar("erick@ues.edu.sv");
        Esperar.Respuesta<UsuarioEntity> r = Esperar.respuesta(c -> repo.iniciarSesion("erick@ues.edu.sv", "abcd9999", c));
        assertEquals(ErrorNegocio.Tipo.CREDENCIALES, Esperar.tipo(r));
    }

    @Test
    public void loginConRedEnTelefonoNuevoTraeLaCuentaDeLaNube() throws Exception {
        registrar("erick@ues.edu.sv");
        db.clearAllTables();
        Esperar.Respuesta<UsuarioEntity> r = Esperar.respuesta(c -> repo.iniciarSesion("erick@ues.edu.sv", "abcd1234", c));
        assertNull(r.error);
        assertNotNull(db.usuarioDao().buscar("erick@ues.edu.sv"));
    }

    @Test
    public void cambioDeClaveSinRedQuedaPendienteYLoginOnlineNoLoPisa() throws Exception {
        registrar("erick@ues.edu.sv");
        red.set(false);
        Esperar.Respuesta<Boolean> cambio = Esperar.respuesta(c -> repo.cambiarClave("erick@ues.edu.sv", "abcd1234", "nueva2026", c));
        assertEquals(Boolean.FALSE, cambio.valor);
        assertEquals(EstadoSync.PENDIENTE, db.usuarioDao().buscar("erick@ues.edu.sv").estadoSync);
        red.set(true);
        Esperar.Respuesta<UsuarioEntity> r = Esperar.respuesta(c -> repo.iniciarSesion("erick@ues.edu.sv", "nueva2026", c));
        assertNull(r.error);
        UsuarioEntity local = db.usuarioDao().buscar("erick@ues.edu.sv");
        assertTrue(ClaveSegura.verificar("nueva2026", local.claveHash));
        assertEquals(EstadoSync.PENDIENTE, local.estadoSync);
    }

    @Test
    public void cambiarClaveConClaveActualMalaFalla() throws Exception {
        registrar("erick@ues.edu.sv");
        Esperar.Respuesta<Boolean> r = Esperar.respuesta(c -> repo.cambiarClave("erick@ues.edu.sv", "mala1234", "nueva2026", c));
        assertEquals(ErrorNegocio.Tipo.CREDENCIALES, Esperar.tipo(r));
    }

    @Test
    public void actualizarNombreSinUsuarioLocalDaErrorSinFallar() throws Exception {
        Esperar.Respuesta<Boolean> r = Esperar.respuesta(c -> repo.actualizarNombre("nadie@ues.edu.sv", "Nombre Largo", c));
        assertNotNull(r.error);
    }

    @Test
    public void actualizarNombreConRedSeSincroniza() throws Exception {
        registrar("erick@ues.edu.sv");
        Esperar.Respuesta<Boolean> r = Esperar.respuesta(c -> repo.actualizarNombre("erick@ues.edu.sv", "Erick Enrique Hernandez", c));
        assertEquals(Boolean.TRUE, r.valor);
        assertEquals("Erick Enrique Hernandez", remoto.usuarios.get("erick@ues.edu.sv").get("nombre"));
        assertEquals(EstadoSync.SINCRONIZADO, db.usuarioDao().buscar("erick@ues.edu.sv").estadoSync);
    }
}
