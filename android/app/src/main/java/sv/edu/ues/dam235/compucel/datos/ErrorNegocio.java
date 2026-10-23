package sv.edu.ues.dam235.compucel.datos;

/** Errores esperados que la interfaz muestra al usuario. */
public class ErrorNegocio extends Exception {

    public enum Tipo { SIN_CONEXION, CORREO_EXISTE, CREDENCIALES, SIN_CUENTA_LOCAL }

    private final Tipo tipo;

    public ErrorNegocio(Tipo tipo) {
        super(tipo.name());
        this.tipo = tipo;
    }

    public Tipo getTipo() {
        return tipo;
    }
}
