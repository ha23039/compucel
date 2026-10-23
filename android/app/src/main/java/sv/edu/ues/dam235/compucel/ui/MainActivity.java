package sv.edu.ues.dam235.compucel.ui;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import sv.edu.ues.dam235.compucel.R;
import sv.edu.ues.dam235.compucel.datos.Proveedor;
import sv.edu.ues.dam235.compucel.ui.clientes.ClientesFragment;
import sv.edu.ues.dam235.compucel.util.Red;

/** Home de la app: Toolbar con menu (Perfil, Ubicacion, Cerrar sesion) y barra de navegacion inferior. */
public class MainActivity extends AppCompatActivity {

    /** Lo usa Perfil para pedir el cierre de sesion con el mismo dialogo de esta pantalla. */
    public static final String EXTRA_CERRAR_SESION = "cerrar_sesion";

    private MaterialToolbar toolbar;
    private BottomNavigationView navegacion;
    private ImageView ivSync;
    private TextView tvSyncCantidad;
    private final MutableLiveData<Boolean> hayRed = new MutableLiveData<>(true);
    private final MutableLiveData<Integer> pendientes = new MutableLiveData<>(0);
    private int pendientesUsuario;
    private int pendientesClientes;
    private ConnectivityManager.NetworkCallback callbackRed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // El margen inferior lo aplica la propia barra de navegacion
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navegacion = findViewById(R.id.navegacion);
        navegacion.setOnItemSelectedListener(item -> {
            mostrar(item.getItemId());
            return true;
        });
        if (savedInstanceState == null) {
            mostrar(R.id.nav_inicio);
        }

        // Total de cambios pendientes (perfil + clientes) para la barra y la pantalla de inicio
        Proveedor.usuarios(this).pendientes().observe(this, n -> {
            pendientesUsuario = n == null ? 0 : n;
            pendientes.setValue(pendientesUsuario + pendientesClientes);
        });
        Proveedor.clientes(this).pendientes().observe(this, n -> {
            pendientesClientes = n == null ? 0 : n;
            pendientes.setValue(pendientesUsuario + pendientesClientes);
        });
        pendientes.observe(this, n -> actualizarIconoSync());
        hayRed.observe(this, conectado -> actualizarIconoSync());
        escucharRed();
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra(EXTRA_CERRAR_SESION, false)) {
            confirmarCerrarSesion();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (callbackRed != null) {
            getSystemService(ConnectivityManager.class).unregisterNetworkCallback(callbackRed);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        View accion = menu.findItem(R.id.menu_sync).getActionView();
        ivSync = accion.findViewById(R.id.ivSync);
        tvSyncCantidad = accion.findViewById(R.id.tvSyncCantidad);
        accion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirSincronizacion();
            }
        });
        actualizarIconoSync();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_perfil) {
            abrirPerfil();
            return true;
        } else if (item.getItemId() == R.id.menu_ubicacion) {
            abrirMapa();
            return true;
        } else if (item.getItemId() == R.id.menu_cerrar_sesion) {
            confirmarCerrarSesion();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public LiveData<Boolean> hayRed() {
        return hayRed;
    }

    public LiveData<Integer> pendientes() {
        return pendientes;
    }

    public void mostrarClientes() {
        navegacion.setSelectedItemId(R.id.nav_clientes);
    }

    public void abrirPerfil() {
        startActivity(new Intent(this, PerfilActivity.class));
    }

    public void abrirMapa() {
        startActivity(new Intent(this, MapaActivity.class));
    }

    public void abrirSincronizacion() {
        startActivity(new Intent(this, SincronizacionActivity.class));
    }

    /** AUT-04: si hay cambios sin enviar se avisa que se conservan en el telefono. */
    public void confirmarCerrarSesion() {
        int total = pendientes.getValue() == null ? 0 : pendientes.getValue();
        String mensaje = total > 0
                ? getString(R.string.cerrar_sesion_pendientes, total)
                : getString(R.string.cerrar_sesion_simple);
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.cerrar_sesion_titulo)
                .setMessage(mensaje)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.cerrar_sesion, (dialogo, boton) -> {
                    Proveedor.sesion(this).cerrar();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .show();
    }

    private void mostrar(int destino) {
        Fragment fragment;
        if (destino == R.id.nav_clientes) {
            fragment = new ClientesFragment();
            toolbar.setTitle(R.string.clientes);
        } else if (destino == R.id.nav_mas) {
            fragment = new MasFragment();
            toolbar.setTitle(R.string.nav_mas);
        } else {
            fragment = new InicioFragment();
            toolbar.setTitle(R.string.app_name);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.contenedor, fragment).commit();
    }

    private void actualizarIconoSync() {
        if (ivSync == null) {
            return;
        }
        int total = pendientes.getValue() == null ? 0 : pendientes.getValue();
        if (Boolean.FALSE.equals(hayRed.getValue())) {
            ivSync.setImageResource(R.drawable.ic_cloud_off);
        } else if (total > 0) {
            ivSync.setImageResource(R.drawable.ic_cloud_upload);
        } else {
            ivSync.setImageResource(R.drawable.ic_cloud_done);
        }
        tvSyncCantidad.setVisibility(total > 0 ? View.VISIBLE : View.GONE);
        tvSyncCantidad.setText(String.valueOf(total));
    }

    // Avisa cuando se pierde o se recupera internet (banner "Sin conexion" e icono de la barra)
    private void escucharRed() {
        hayRed.setValue(Red.hayInternet(this));
        callbackRed = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities capacidades) {
                hayRed.postValue(capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED));
            }

            @Override
            public void onLost(@NonNull Network network) {
                hayRed.postValue(false);
            }
        };
        getSystemService(ConnectivityManager.class).registerDefaultNetworkCallback(callbackRed);
    }
}
