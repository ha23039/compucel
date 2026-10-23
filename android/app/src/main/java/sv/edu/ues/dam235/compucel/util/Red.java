package sv.edu.ues.dam235.compucel.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

/** Consulta si el telefono tiene internet en este momento. */
public final class Red {

    /** Permite cambiar la consulta de red en las pruebas. */
    public interface ConexionDisponible {
        boolean hay();
    }

    private Red() {
    }

    public static boolean hayInternet(Context context) {
        ConnectivityManager cm = context.getSystemService(ConnectivityManager.class);
        Network red = cm.getActiveNetwork();
        if (red == null) {
            return false;
        }
        NetworkCapabilities capacidades = cm.getNetworkCapabilities(red);
        return capacidades != null
                && capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }
}
