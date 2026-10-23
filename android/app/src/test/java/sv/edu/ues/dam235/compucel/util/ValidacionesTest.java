package sv.edu.ues.dam235.compucel.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ValidacionesTest {

    @Test
    public void correoSeNormaliza() {
        assertEquals("erick@ues.edu.sv", Validaciones.normalizarCorreo(" Erick@UES.edu.sv "));
        assertEquals("", Validaciones.normalizarCorreo(null));
    }

    @Test
    public void correos() {
        assertTrue(Validaciones.correoValido("erick@ues.edu.sv"));
        assertTrue(Validaciones.correoValido(" Erick@UES.edu.sv "));
        assertFalse(Validaciones.correoValido("erick@ues"));
        assertFalse(Validaciones.correoValido("erick.ues.edu.sv"));
        assertFalse(Validaciones.correoValido(""));
        assertFalse(Validaciones.correoValido(null));
    }

    @Test
    public void claves() {
        assertTrue(Validaciones.claveValida("abcd1234"));
        assertTrue(Validaciones.claveValida("Taller2026"));
        for (String c : new String[]{"abcdefgh", "12345678", "abc123!@", "abc1234", "", "abcd 1234"}) {
            assertFalse(c, Validaciones.claveValida(c));
        }
        assertFalse(Validaciones.claveValida(null));
    }

    @Test
    public void nombres() {
        assertTrue(Validaciones.nombreValido("Erick H"));
        assertFalse(Validaciones.nombreValido("  Erick  "));
        assertFalse(Validaciones.nombreValido(null));
    }

    @Test
    public void telefonos() {
        assertTrue(Validaciones.telefonoValido("7654-3210"));
        assertFalse(Validaciones.telefonoValido("76543210"));
        assertFalse(Validaciones.telefonoValido("7654-321"));
        assertFalse(Validaciones.telefonoValido(null));
    }
}
