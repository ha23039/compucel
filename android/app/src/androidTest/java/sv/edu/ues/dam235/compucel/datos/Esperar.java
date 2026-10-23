package sv.edu.ues.dam235.compucel.datos;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.Assert.assertTrue;

/** Ayuda de las pruebas: convierte una llamada con Resultado en un valor o una excepcion. */
public final class Esperar {

    public static final class Respuesta<T> {
        public T valor;
        public Exception error;
    }

    private Esperar() {
    }

    public static <T> Respuesta<T> respuesta(Consumer<Resultado<T>> llamada, long segundos) throws InterruptedException {
        Respuesta<T> r = new Respuesta<>();
        CountDownLatch listo = new CountDownLatch(1);
        llamada.accept(new Resultado<T>() {
            @Override
            public void exito(T valor) {
                r.valor = valor;
                listo.countDown();
            }

            @Override
            public void error(Exception e) {
                r.error = e;
                listo.countDown();
            }
        });
        assertTrue("sin respuesta", listo.await(segundos, TimeUnit.SECONDS));
        return r;
    }

    public static <T> Respuesta<T> respuesta(Consumer<Resultado<T>> llamada) throws InterruptedException {
        return respuesta(llamada, 5);
    }

    public static ErrorNegocio.Tipo tipo(Respuesta<?> r) {
        return r.error instanceof ErrorNegocio ? ((ErrorNegocio) r.error).getTipo() : null;
    }

    public static AtomicReference<Boolean> bandera(boolean valor) {
        return new AtomicReference<>(valor);
    }
}
