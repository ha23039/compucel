package sv.edu.ues.dam235.compucel.util;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SesionTest {

    private Context ctx;

    @Before
    public void limpiar() {
        ctx = ApplicationProvider.getApplicationContext();
        ctx.getSharedPreferences("compucel_sesion", Context.MODE_PRIVATE).edit().clear().commit();
    }

    @Test
    public void recordarGuardaClaveCifrada() {
        Sesion s = new Sesion(ctx);
        s.recordar("erick@ues.edu.sv", "abcd1234");
        String crudo = ctx.getSharedPreferences("compucel_sesion", Context.MODE_PRIVATE).getAll().toString();
        assertFalse(crudo, crudo.contains("abcd1234"));
        assertTrue(s.hayRecordado());
        assertEquals("abcd1234", s.claveRecordada());
        assertEquals("erick@ues.edu.sv", s.correoRecordado());
    }

    @Test
    public void cerrarConservaRecordado() {
        Sesion s = new Sesion(ctx);
        s.recordar("a@b.co", "abcd1234");
        s.abrir("a@b.co", "Erick Hernandez");
        assertTrue(s.abierta());
        assertEquals("a@b.co", s.correoActual());
        assertEquals("Erick Hernandez", s.nombreActual());
        s.cerrar();
        assertFalse(s.abierta());
        assertTrue(s.hayRecordado());
    }

    @Test
    public void olvidarBorraTodo() {
        Sesion s = new Sesion(ctx);
        s.recordar("a@b.co", "abcd1234");
        s.olvidar();
        assertFalse(s.hayRecordado());
        assertNull(s.claveRecordada());
    }

    @Test
    public void actualizarNombreCambiaLaSesion() {
        Sesion s = new Sesion(ctx);
        s.abrir("a@b.co", "Erick Hernandez");
        s.actualizarNombre("Erick Enrique Hernandez");
        assertEquals("Erick Enrique Hernandez", s.nombreActual());
    }

    @Test
    public void ultimaSincronizacionSeGuarda() {
        Sesion s = new Sesion(ctx);
        assertEquals(0L, s.ultimaSincronizacion());
        s.guardarUltimaSincronizacion(1792771200000L);
        assertEquals(1792771200000L, s.ultimaSincronizacion());
    }

    @Test
    public void temaPorDefectoEsSistemaYSeGuarda() {
        Sesion s = new Sesion(ctx);
        assertEquals(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, s.tema());
        s.guardarTema(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        assertEquals(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES, s.tema());
        s.cerrar();
        assertEquals(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES, s.tema());
    }

    @Test
    public void cifradoDevuelveElTextoOriginal() {
        CifradoLocal cifrado = new CifradoLocal();
        String guardado = cifrado.cifrar("Taller2026");
        assertFalse(guardado.contains("Taller2026"));
        assertEquals("Taller2026", cifrado.descifrar(guardado));
        assertNull(cifrado.descifrar("no-es-un-cifrado"));
    }
}
