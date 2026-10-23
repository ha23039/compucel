package sv.edu.ues.dam235.compucel.datos;

/** Respuesta de una operacion que termina despues (base local o nube). */
public interface Resultado<T> {

    void exito(T valor);

    void error(Exception e);
}
