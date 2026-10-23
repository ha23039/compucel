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
import sv.edu.ues.dam235.compucel.datos.local.ClienteEntity;
import sv.edu.ues.dam235.compucel.datos.local.EstadoSync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ClienteRepositorioTest {

    private AppDatabase db;
    private RemotoFalso remoto;
    private final AtomicBoolean red = new AtomicBoolean(true);
    private ClienteRepositorio repo;

    @Before
    public void crear() {
        Context ctx = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase.class).allowMainThreadQueries().build();
        remoto = new RemotoFalso();
        repo = new ClienteRepositorio(db.clienteDao(), remoto, red::get, Runnable::run);
    }

    @After
    public void cerrar() {
        db.close();
    }

    private ClienteEntity cliente() {
        ClienteEntity c = ClienteEntity.nuevo("erick@ues.edu.sv");
        c.nombre = "Maria Elena Flores";
        c.telefono = "7654-3210";
        return c;
    }

    @Test
    public void clienteSinRedQuedaPendiente() throws Exception {
        red.set(false);
        ClienteEntity c = cliente();
        assertEquals(Boolean.FALSE, Esperar.respuesta((Resultado<Boolean> r) -> repo.guardar(c, r)).valor);
        assertEquals(EstadoSync.PENDIENTE, db.clienteDao().buscar(c.id).estadoSync);
        assertTrue(remoto.clientes.isEmpty());
    }

    @Test
    public void clienteConRedQuedaSincronizado() throws Exception {
        ClienteEntity c = cliente();
        assertEquals(Boolean.TRUE, Esperar.respuesta((Resultado<Boolean> r) -> repo.guardar(c, r)).valor);
        assertEquals(EstadoSync.SINCRONIZADO, db.clienteDao().buscar(c.id).estadoSync);
        assertEquals("Maria Elena Flores", remoto.clientes.get(c.id).get("nombre"));
    }

    @Test
    public void guardarDosVecesUnSoloDocumentoRemoto() throws Exception {
        ClienteEntity c = cliente();
        Esperar.respuesta((Resultado<Boolean> r) -> repo.guardar(c, r));
        Esperar.respuesta((Resultado<Boolean> r) -> repo.guardar(c, r));
        assertEquals(1, remoto.clientes.size());
        assertTrue(db.clienteDao().pendientes().isEmpty());
        assertEquals(EstadoSync.SINCRONIZADO, db.clienteDao().buscar(c.id).estadoSync);
    }

    @Test
    public void nubeQueFallaDejaPendiente() throws Exception {
        remoto.fallar = true;
        ClienteEntity c = cliente();
        assertEquals(Boolean.FALSE, Esperar.respuesta((Resultado<Boolean> r) -> repo.guardar(c, r)).valor);
        assertEquals(EstadoSync.PENDIENTE, db.clienteDao().buscar(c.id).estadoSync);
    }

    @Test
    public void cambiarActivoSeGuardaEnAmbos() throws Exception {
        ClienteEntity c = cliente();
        Esperar.respuesta((Resultado<Boolean> r) -> repo.guardar(c, r));
        assertEquals(Boolean.TRUE, Esperar.respuesta((Resultado<Boolean> r) -> repo.cambiarActivo(c.id, false, r)).valor);
        assertFalse(db.clienteDao().buscar(c.id).activo);
        assertEquals(Boolean.FALSE, remoto.clientes.get(c.id).get("activo"));
    }

    @Test
    public void eliminarConRedBorraEnAmbos() throws Exception {
        ClienteEntity c = cliente();
        Esperar.respuesta((Resultado<Boolean> r) -> repo.guardar(c, r));
        assertEquals(Boolean.TRUE, Esperar.respuesta((Resultado<Boolean> r) -> repo.eliminar(c.id, r)).valor);
        assertNull(db.clienteDao().buscar(c.id));
        assertTrue(remoto.clientes.isEmpty());
    }

    @Test
    public void eliminarSinRedQuedaMarcadoYPendiente() throws Exception {
        ClienteEntity c = cliente();
        Esperar.respuesta((Resultado<Boolean> r) -> repo.guardar(c, r));
        red.set(false);
        assertEquals(Boolean.FALSE, Esperar.respuesta((Resultado<Boolean> r) -> repo.eliminar(c.id, r)).valor);
        ClienteEntity local = db.clienteDao().buscar(c.id);
        assertTrue(local.eliminado);
        assertEquals(EstadoSync.PENDIENTE, local.estadoSync);
    }

    @Test
    public void refrescarNoResucitaEliminadoPendiente() throws Exception {
        ClienteEntity c = cliente();
        Esperar.respuesta((Resultado<Boolean> r) -> repo.guardar(c, r));
        red.set(false);
        Esperar.respuesta((Resultado<Boolean> r) -> repo.eliminar(c.id, r));
        red.set(true);
        assertNull(Esperar.respuesta((Resultado<Integer> r) -> repo.refrescarDesdeNube(r)).error);
        ClienteEntity local = db.clienteDao().buscar(c.id);
        assertTrue(local.eliminado);
        assertEquals(EstadoSync.PENDIENTE, local.estadoSync);
    }

    @Test
    public void refrescarNoPisaCambioPendiente() throws Exception {
        ClienteEntity c = cliente();
        Esperar.respuesta((Resultado<Boolean> r) -> repo.guardar(c, r));
        red.set(false);
        c.nombre = "Maria Elena Flores de Lopez";
        Esperar.respuesta((Resultado<Boolean> r) -> repo.guardar(c, r));
        red.set(true);
        Esperar.respuesta((Resultado<Integer> r) -> repo.refrescarDesdeNube(r));
        assertEquals("Maria Elena Flores de Lopez", db.clienteDao().buscar(c.id).nombre);
    }

    @Test
    public void refrescarTraeClientesNuevosYQuitaLosBorradosEnLaNube() throws Exception {
        ClienteEntity deOtroTelefono = cliente();
        remoto.clientes.put(deOtroTelefono.id, deOtroTelefono.toMap());
        ClienteEntity borradoEnNube = cliente();
        Esperar.respuesta((Resultado<Boolean> r) -> repo.guardar(borradoEnNube, r));
        remoto.clientes.remove(borradoEnNube.id);
        Esperar.Respuesta<Integer> r = Esperar.respuesta((Resultado<Integer> c) -> repo.refrescarDesdeNube(c));
        assertNull(r.error);
        assertEquals(EstadoSync.SINCRONIZADO, db.clienteDao().buscar(deOtroTelefono.id).estadoSync);
        assertNull(db.clienteDao().buscar(borradoEnNube.id));
    }

    @Test
    public void refrescarSinRedFalla() throws Exception {
        red.set(false);
        assertEquals(ErrorNegocio.Tipo.SIN_CONEXION, Esperar.tipo(Esperar.respuesta((Resultado<Integer> r) -> repo.refrescarDesdeNube(r))));
    }
}
