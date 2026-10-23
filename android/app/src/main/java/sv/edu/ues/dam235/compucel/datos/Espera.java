package sv.edu.ues.dam235.compucel.datos;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Espera la respuesta de la nube en el hilo de trabajo (nunca en el hilo principal).
 * Si la nube no responde a tiempo se toma como fallo y el registro queda pendiente.
 */
final class Espera {

    static final long SEGUNDOS = 15;

    private Espera() {
    }

    static <T> T llamar(Consumer<Resultado<T>> llamada) throws Exception {
        AtomicReference<T> valor = new AtomicReference<>();
        AtomicReference<Exception> error = new AtomicReference<>();
        CountDownLatch listo = new CountDownLatch(1);
        llamada.accept(new Resultado<T>() {
            @Override
            public void exito(T v) {
                valor.set(v);
                listo.countDown();
            }

            @Override
            public void error(Exception e) {
                error.set(e);
                listo.countDown();
            }
        });
        if (!listo.await(SEGUNDOS, TimeUnit.SECONDS)) {
            throw new TimeoutException("La nube no respondio");
        }
        if (error.get() != null) {
            throw error.get();
        }
        return valor.get();
    }
}
