package sv.edu.ues.dam235.compucel.datos.local;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DaosTest {

    @Rule
    public InstantTaskExecutorRule reglaInstantanea = new InstantTaskExecutorRule();

    private AppDatabase db;
    private ClienteDao clientes;
    private UsuarioDao usuarios;

    @Before
    public void crear() {
        Context ctx = ApplicationProvider.getApplicationContext();
        db = Room.inMemoryDatabaseBuilder(ctx, AppDatabase.class).allowMainThreadQueries().build();
        clientes = db.clienteDao();
        usuarios = db.usuarioDao();
    }

    @After
    public void cerrar() {
        db.close();
    }

    private static <T> T valor(LiveData<T> datos) throws InterruptedException {
        AtomicReference<T> resultado = new AtomicReference<>();
        CountDownLatch listo = new CountDownLatch(1);
        datos.observeForever(v -> {
            resultado.set(v);
            listo.countDown();
        });
        assertTrue(listo.await(2, TimeUnit.SECONDS));
        return resultado.get();
    }

    private static ClienteEntity cliente(String nombre, String telefono) {
        ClienteEntity c = ClienteEntity.nuevo("a@b.co");
        c.nombre = nombre;
        c.telefono = telefono;
        return c;
    }

    @Test
    public void clienteNuevoEsPendienteYActivo() {
        ClienteEntity c = ClienteEntity.nuevo("a@b.co");
        assertEquals(EstadoSync.PENDIENTE, c.estadoSync);
        assertTrue(c.activo);
        assertEquals(36, c.id.length());
    }

    @Test
    public void clientePendienteApareceEnPendientes() {
        clientes.guardar(cliente("Maria Elena Flores", "7654-3210"));
        assertEquals(1, clientes.pendientes().size());
    }

    @Test
    public void guardarDosVecesElMismoIdNoDuplica() throws Exception {
        ClienteEntity c = cliente("Maria Elena Flores", "7654-3210");
        clientes.guardar(c);
        clientes.guardar(c);
        assertEquals(1, clientes.pendientes().size());
        assertEquals(Integer.valueOf(1), valor(clientes.observarTotal()));
    }

    @Test
    public void eliminadoNoSaleEnListaPeroSiEnPendientes() throws Exception {
        ClienteEntity c = cliente("Maria Elena Flores", "7654-3210");
        c.eliminado = true;
        clientes.guardar(c);
        assertTrue(valor(clientes.observarLista(true, "")).isEmpty());
        assertEquals(1, clientes.pendientes().size());
        assertEquals(Integer.valueOf(0), valor(clientes.observarTotal()));
    }

    @Test
    public void filtroPorNombreYTelefono() throws Exception {
        clientes.guardar(cliente("María Elena Flores", "7654-3210"));
        clientes.guardar(cliente("Carlos Menjívar", "7812-4455"));
        assertEquals(1, valor(clientes.observarLista(true, "7812")).size());
        assertEquals(1, valor(clientes.observarLista(true, "maría")).size());
        assertEquals(2, valor(clientes.observarLista(true, "")).size());
        assertEquals(0, valor(clientes.observarLista(false, "")).size());
    }

    @Test
    public void listaOrdenadaPorNombre() throws Exception {
        clientes.guardar(cliente("ana Beatriz Cruz", "7003-9981"));
        clientes.guardar(cliente("Carlos Menjívar", "7812-4455"));
        clientes.guardar(cliente("Ana Abarca Ruiz", "7000-0001"));
        List<ClienteEntity> lista = valor(clientes.observarLista(true, ""));
        assertEquals("Ana Abarca Ruiz", lista.get(0).nombre);
        assertEquals("Carlos Menjívar", lista.get(2).nombre);
    }

    @Test
    public void contadoresDeInicio() throws Exception {
        ClienteEntity activo = cliente("Maria Elena Flores", "7654-3210");
        activo.estadoSync = EstadoSync.SINCRONIZADO;
        ClienteEntity inactivo = cliente("Carlos Menjivar", "7812-4455");
        inactivo.activo = false;
        clientes.guardar(activo);
        clientes.guardar(inactivo);
        assertEquals(Integer.valueOf(1), valor(clientes.observarTotalActivos()));
        assertEquals(Integer.valueOf(2), valor(clientes.observarTotal()));
        assertEquals(Integer.valueOf(1), valor(clientes.observarPendientes()));
    }

    @Test
    public void borrarQuitaLaFila() {
        ClienteEntity c = cliente("Maria Elena Flores", "7654-3210");
        clientes.guardar(c);
        clientes.borrar(c.id);
        assertEquals(null, clientes.buscar(c.id));
    }

    @Test
    public void usuarioSeGuardaYBusca() throws Exception {
        UsuarioEntity u = new UsuarioEntity();
        u.correo = "erick@ues.edu.sv";
        u.nombre = "Erick Hernandez";
        u.claveHash = "$2a$10$hash";
        u.estadoSync = EstadoSync.PENDIENTE;
        usuarios.guardar(u);
        assertNotNull(usuarios.buscar("erick@ues.edu.sv"));
        assertEquals(1, usuarios.pendientes().size());
        assertEquals(Integer.valueOf(1), valor(usuarios.observarPendientes()));
    }

    @Test
    public void usuarioToMapNoLlevaEstado() {
        UsuarioEntity u = new UsuarioEntity();
        u.correo = "erick@ues.edu.sv";
        u.nombre = "Erick Hernandez";
        u.claveHash = "$2a$10$hash";
        u.estadoSync = EstadoSync.PENDIENTE;
        assertFalse(u.toMap().containsKey("estadoSync"));
        assertEquals("Erick Hernandez", UsuarioEntity.fromMap(u.toMap()).nombre);
        assertEquals(EstadoSync.SINCRONIZADO, UsuarioEntity.fromMap(u.toMap()).estadoSync);
    }

    @Test
    public void clienteToMapYFromMap() {
        ClienteEntity c = cliente("Maria Elena Flores", "7654-3210");
        c.correo = "mflores@correo.com";
        assertFalse(c.toMap().containsKey("estadoSync"));
        assertFalse(c.toMap().containsKey("eliminado"));
        ClienteEntity copia = ClienteEntity.fromMap(c.id, c.toMap());
        assertEquals(c.id, copia.id);
        assertEquals("mflores@correo.com", copia.correo);
        assertTrue(copia.activo);
        assertEquals(EstadoSync.SINCRONIZADO, copia.estadoSync);
    }
}
