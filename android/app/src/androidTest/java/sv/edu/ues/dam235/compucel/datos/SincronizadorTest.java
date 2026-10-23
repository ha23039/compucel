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
import sv.edu.ues.dam235.compucel.datos.local.UsuarioEntity;
import sv.edu.ues.dam235.compucel.util.ClaveSegura;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SincronizadorTest {

    private AppDatabase db;
    private RemotoFalso remoto;
    private final AtomicBoolean red = new AtomicBoolean(true);
    private Sincronizador sincronizador;

    @Before
    public void crear() {
        Context ctx = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase.class).allowMainThreadQueries().build();
        remoto = new RemotoFalso();
        sincronizador = new Sincronizador(db.usuarioDao(), db.clienteDao(), remoto, red::get, Runnable::run);
    }

    @After
    public void cerrar() {
        db.close();
    }

    private ClienteEntity clientePendiente(String nombre) {
        ClienteEntity c = ClienteEntity.nuevo("erick@ues.edu.sv");
        c.nombre = nombre;
        c.telefono = "7654-3210";
        db.clienteDao().guardar(c);
        return c;
    }

    @Test
    public void sincronizarEnviaPendientes() throws Exception {
        clientePendiente("Maria Elena Flores");
        UsuarioEntity u = new UsuarioEntity();
        u.correo = "erick@ues.edu.sv";
        u.nombre = "Erick Hernandez";
        u.claveHash = ClaveSegura.hash("abcd1234");
        u.estadoSync = EstadoSync.PENDIENTE;
        db.usuarioDao().guardar(u);
        Esperar.Respuesta<ResumenSync> r = Esperar.respuesta(sincronizador::sincronizarTodo);
        assertNull(r.error);
        assertEquals(2, r.valor.enviados);
        assertEquals(0, r.valor.fallidos);
        assertTrue(db.clienteDao().pendientes().isEmpty());
        assertTrue(db.usuarioDao().pendientes().isEmpty());
        assertEquals("Erick Hernandez", remoto.usuarios.get("erick@ues.edu.sv").get("nombre"));
    }

    @Test
    public void sincronizarEnviaBorradosPendientes() throws Exception {
        ClienteEntity c = clientePendiente("Maria Elena Flores");
        remoto.clientes.put(c.id, c.toMap());
        c.eliminado = true;
        db.clienteDao().guardar(c);
        Esperar.Respuesta<ResumenSync> r = Esperar.respuesta(sincronizador::sincronizarTodo);
        assertEquals(1, r.valor.enviados);
        assertNull(db.clienteDao().buscar(c.id));
        assertFalse(remoto.clientes.containsKey(c.id));
    }

    @Test
    public void sincronizarConRemotoQueFallaDejaPendiente() throws Exception {
        clientePendiente("Maria Elena Flores");
        remoto.fallar = true;
        Esperar.Respuesta<ResumenSync> r = Esperar.respuesta(sincronizador::sincronizarTodo);
        assertEquals(1, r.valor.fallidos);
        assertEquals(1, r.valor.errores.size());
        assertEquals(1, db.clienteDao().pendientes().size());
    }

    @Test
    public void sincronizarSinRedFallaConSinConexion() throws Exception {
        clientePendiente("Maria Elena Flores");
        red.set(false);
        Esperar.Respuesta<ResumenSync> r = Esperar.respuesta(sincronizador::sincronizarTodo);
        assertEquals(ErrorNegocio.Tipo.SIN_CONEXION, Esperar.tipo(r));
    }

    @Test
    public void conVariosPendientesYRedColgadaSeDetieneEnElPrimerTimeout() throws Exception {
        clientePendiente("Maria Elena Flores");
        clientePendiente("Carlos Alberto Menjivar");
        clientePendiente("Ana Beatriz Cruz");
        remoto.noResponder = true;
        long inicio = System.currentTimeMillis();
        Esperar.Respuesta<ResumenSync> r = Esperar.respuesta(sincronizador::sincronizarTodo, 40);
        long segundos = (System.currentTimeMillis() - inicio) / 1000;
        assertTrue("tardo " + segundos + " s", segundos <= 20);
        assertEquals(3, r.valor.fallidos);
        assertEquals(0, r.valor.enviados);
        assertEquals(3, db.clienteDao().pendientes().size());
    }

    @Test
    public void sincronizarConRemotoQueNoRespondeTerminaPorTimeout() throws Exception {
        clientePendiente("Maria Elena Flores");
        remoto.noResponder = true;
        Esperar.Respuesta<ResumenSync> r = Esperar.respuesta(sincronizador::sincronizarTodo, 20);
        assertEquals(1, r.valor.fallidos);
        assertEquals(EstadoSync.PENDIENTE, db.clienteDao().pendientes().get(0).estadoSync);
    }
}
