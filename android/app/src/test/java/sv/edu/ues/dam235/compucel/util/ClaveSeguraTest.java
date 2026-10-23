package sv.edu.ues.dam235.compucel.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class ClaveSeguraTest {

    @Test
    public void hashEsBcryptYVerifica() {
        String hash = ClaveSegura.hash("abcd1234");
        assertTrue(hash, hash.startsWith("$2a$10$"));
        assertNotEquals("abcd1234", hash);
        assertTrue(ClaveSegura.verificar("abcd1234", hash));
        assertFalse(ClaveSegura.verificar("abcd1235", hash));
        assertFalse(ClaveSegura.verificar("abcd1234", null));
        assertFalse(ClaveSegura.verificar("abcd1234", ""));
    }

    @Test
    public void dosHashesDeLaMismaClaveSonDistintos() {
        assertNotEquals(ClaveSegura.hash("abcd1234"), ClaveSegura.hash("abcd1234"));
    }
}
