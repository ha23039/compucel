package sv.edu.ues.dam235.compucel.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TextoTest {

    @Test
    public void inicialesDeDosPalabras() {
        assertEquals("MF", Texto.iniciales("María Elena Flores"));
        assertEquals("ME", Texto.iniciales("maría elena"));
        assertEquals("A", Texto.iniciales("  Ana  "));
        assertEquals("?", Texto.iniciales("   "));
        assertEquals("?", Texto.iniciales(null));
    }

    @Test
    public void inicialesUsanPrimeroYUltimoApellido() {
        assertEquals("JH", Texto.iniciales("José Roberto Portillo Hernández"));
    }

    @Test
    public void telefonoSeFormateaMientrasSeEscribe() {
        assertEquals("7654", Texto.formatoTelefono("7654"));
        assertEquals("7654-3", Texto.formatoTelefono("76543"));
        assertEquals("7654-3210", Texto.formatoTelefono("76543210"));
        assertEquals("7654-3210", Texto.formatoTelefono("7654-3210"));
        assertEquals("7654-3210", Texto.formatoTelefono("7654 32101"));
        assertEquals("", Texto.formatoTelefono(null));
    }
}
