package sv.edu.ues.dam235.compucel;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import sv.edu.ues.dam235.compucel.datos.Proveedor;

/** Aplica la apariencia elegida en Perfil antes de abrir cualquier pantalla. */
public class CompuCelApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(Proveedor.sesion(this).tema());
    }
}
