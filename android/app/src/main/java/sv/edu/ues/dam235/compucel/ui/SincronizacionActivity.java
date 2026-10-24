package sv.edu.ues.dam235.compucel.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import sv.edu.ues.dam235.compucel.R;
import sv.edu.ues.dam235.compucel.datos.Proveedor;
import sv.edu.ues.dam235.compucel.datos.ResumenSync;

/** CTA-02 Estado de sincronizacion: pendientes por modulo, errores y reintento. */
public class SincronizacionActivity extends AppCompatActivity {

    private int pendientesUsuario;
    private int pendientesClientes;
    private int pendientesServicios;
    private MaterialButton btSincronizar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sincronizacion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        btSincronizar = findViewById(R.id.btSincronizar);

        Proveedor.usuarios(this).pendientes().observe(this, n -> {
            pendientesUsuario = n == null ? 0 : n;
            mostrar();
        });
        Proveedor.clientes(this).pendientes().observe(this, n -> {
            pendientesClientes = n == null ? 0 : n;
            mostrar();
        });
        Proveedor.servicios(this).pendientes().observe(this, n -> {
            pendientesServicios = n == null ? 0 : n;
            mostrar();
        });
        btSincronizar.setOnClickListener(v -> sincronizar());
        mostrarErrores(SincronizarAccion.ultimoResumen());
    }

    private void sincronizar() {
        btSincronizar.setEnabled(false);
        btSincronizar.setText(R.string.sincronizando);
        SincronizarAccion.ejecutar(this, resumen -> {
            btSincronizar.setEnabled(true);
            btSincronizar.setText(R.string.sincronizar_ahora);
            mostrarErrores(resumen);
        });
    }

    private void mostrar() {
        int total = pendientesUsuario + pendientesClientes + pendientesServicios;
        View banner = findViewById(R.id.banner);
        ImageView ivBanner = findViewById(R.id.ivBanner);
        TextView tvBanner = findViewById(R.id.tvBanner);
        if (total > 0) {
            banner.setBackgroundResource(R.drawable.fondo_banner_advertencia);
            ivBanner.setImageResource(R.drawable.ic_cloud_upload);
            tvBanner.setText(getResources().getQuantityString(R.plurals.banner_pendientes, total, total));
            tvBanner.setTextColor(ContextCompat.getColor(this, R.color.advertencia_texto));
        } else {
            banner.setBackgroundResource(R.drawable.fondo_banner_exito);
            ivBanner.setImageResource(R.drawable.ic_cloud_done);
            tvBanner.setText(R.string.banner_al_dia);
            tvBanner.setTextColor(ContextCompat.getColor(this, R.color.exito_texto));
        }
        dato(findViewById(R.id.filaPerfil), R.string.modulo_perfil, pendientesUsuario);
        dato(findViewById(R.id.filaClientes), R.string.modulo_clientes, pendientesClientes);
        dato(findViewById(R.id.filaServicios), R.string.modulo_servicios, pendientesServicios);
    }

    private void mostrarErrores(ResumenSync resumen) {
        View seccion = findViewById(R.id.seccionErrores);
        ViewGroup lista = findViewById(R.id.listaErrores);
        lista.removeAllViews();
        if (resumen == null || resumen.errores.isEmpty()) {
            seccion.setVisibility(View.GONE);
            return;
        }
        seccion.setVisibility(View.VISIBLE);
        for (String error : resumen.errores) {
            View fila = LayoutInflater.from(this).inflate(R.layout.item_error_sync, lista, false);
            // Cada error llega como "Que fallo: motivo"; se muestra solo lo que fallo
            ((TextView) fila.findViewById(R.id.tvErrorTitulo)).setText(error.split(":")[0]);
            fila.findViewById(R.id.btReintentar).setOnClickListener(v -> sincronizar());
            lista.addView(fila);
        }
    }

    private void dato(View fila, @StringRes int etiqueta, int valor) {
        ((TextView) fila.findViewById(R.id.tvEtiqueta)).setText(etiqueta);
        ((TextView) fila.findViewById(R.id.tvValor)).setText(String.valueOf(valor));
    }
}
