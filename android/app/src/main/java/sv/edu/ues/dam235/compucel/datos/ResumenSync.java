package sv.edu.ues.dam235.compucel.datos;

import java.util.ArrayList;
import java.util.List;

/** Resultado de una sincronizacion: cuantos cambios se enviaron y cuales fallaron. */
public class ResumenSync {

    public int enviados;
    public int fallidos;
    public final List<String> errores = new ArrayList<>();
}
