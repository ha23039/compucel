package sv.edu.ues.dam235.compucel.ui;

import android.app.Activity;
import android.widget.Toast;

import sv.edu.ues.dam235.compucel.R;
import sv.edu.ues.dam235.compucel.datos.ErrorNegocio;
import sv.edu.ues.dam235.compucel.datos.Proveedor;
import sv.edu.ues.dam235.compucel.datos.Resultado;
import sv.edu.ues.dam235.compucel.datos.ResumenSync;

/** "Sincronizar ahora" de Perfil y de Sincronizacion: reintenta los pendientes y avisa el resultado. */
public final class SincronizarAccion {

    public interface AlTerminar {
        void listo(ResumenSync resumen);
    }

    /** Resultado del ultimo intento, para mostrar los errores en la pantalla de Sincronizacion. */
    private static ResumenSync ultimoResumen;

    private SincronizarAccion() {
    }

    public static ResumenSync ultimoResumen() {
        return ultimoResumen;
    }

    public static void ejecutar(Activity activity, AlTerminar alTerminar) {
        Proveedor.sincronizador(activity).sincronizarTodo(new Resultado<ResumenSync>() {
            @Override
            public void exito(ResumenSync resumen) {
                ultimoResumen = resumen;
                if (resumen.fallidos == 0) {
                    Proveedor.sesion(activity).guardarUltimaSincronizacion(System.currentTimeMillis());
                }
                activity.runOnUiThread(() -> {
                    String mensaje;
                    if (resumen.fallidos > 0) {
                        mensaje = activity.getResources().getQuantityString(R.plurals.sync_fallidos, resumen.fallidos, resumen.fallidos);
                    } else if (resumen.enviados > 0) {
                        mensaje = activity.getResources().getQuantityString(R.plurals.sync_enviados, resumen.enviados, resumen.enviados);
                    } else {
                        mensaje = activity.getString(R.string.sync_nada);
                    }
                    Toast.makeText(activity, mensaje, Toast.LENGTH_LONG).show();
                    alTerminar.listo(resumen);
                });
            }

            @Override
            public void error(Exception e) {
                activity.runOnUiThread(() -> {
                    boolean sinRed = e instanceof ErrorNegocio && ((ErrorNegocio) e).getTipo() == ErrorNegocio.Tipo.SIN_CONEXION;
                    Toast.makeText(activity, sinRed ? R.string.sync_sin_conexion : R.string.error_general, Toast.LENGTH_LONG).show();
                    alTerminar.listo(null);
                });
            }
        });
    }
}
