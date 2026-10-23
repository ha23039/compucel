package sv.edu.ues.dam235.compucel.datos.remoto;

import android.app.UiAutomation;
import android.content.Context;
import android.os.ParcelFileDescriptor;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileInputStream;
import java.util.List;

import sv.edu.ues.dam235.compucel.datos.Esperar;
import sv.edu.ues.dam235.compucel.datos.local.ClienteEntity;
import sv.edu.ues.dam235.compucel.util.Red;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** Firestore real: sin red, listar clientes debe fallar en lugar de devolver la cache vacia. */
@RunWith(AndroidJUnit4.class)
public class FirestoreRemotoTest {

    private final Context ctx = ApplicationProvider.getApplicationContext();

    private static void shell(String comando) throws Exception {
        UiAutomation ui = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        ParcelFileDescriptor salida = ui.executeShellCommand(comando);
        try (FileInputStream in = new FileInputStream(salida.getFileDescriptor())) {
            while (in.read() != -1) {
                // esperar a que termine el comando
            }
        }
    }

    private void esperarRed(boolean conectado) throws Exception {
        for (int i = 0; i < 40 && Red.hayInternet(ctx) != conectado; i++) {
            Thread.sleep(500);
        }
        assertTrue("estado de red no cambio", Red.hayInternet(ctx) == conectado);
    }

    @After
    public void restaurarRed() throws Exception {
        shell("svc wifi enable");
        shell("svc data enable");
        esperarRed(true);
    }

    @Test
    public void listarSinRedFallaEnVezDeDevolverCacheVacia() throws Exception {
        FirestoreRemoto remoto = new FirestoreRemoto();
        shell("svc wifi disable");
        shell("svc data disable");
        esperarRed(false);
        Esperar.Respuesta<List<ClienteEntity>> r = Esperar.respuesta(remoto::listarClientes, 30);
        assertNull("no debe responder con una lista desde la cache: " + r.valor, r.valor);
        assertNotNull(r.error);
        assertFalse(r.error instanceof RuntimeException && r.error.getMessage() == null);
    }
}
