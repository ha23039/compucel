package sv.edu.ues.dam235.compucel.ui.servicios;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;

import sv.edu.ues.dam235.compucel.R;
import sv.edu.ues.dam235.compucel.datos.Proveedor;
import sv.edu.ues.dam235.compucel.datos.Resultado;
import sv.edu.ues.dam235.compucel.datos.ServicioRepositorio;
import sv.edu.ues.dam235.compucel.datos.local.ServicioEntity;

public class ServicioFormActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "servicio_id";
    private TextInputLayout tilNombre, tilDescripcion, tilCosto, tilTiempo;
    private MaterialButton btGuardar;
    private ServicioRepositorio servicios;
    private ServicioEntity servicioActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_servicio_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        tilNombre = findViewById(R.id.tilServicioNombre);
        tilDescripcion = findViewById(R.id.tilServicioDescripcion);
        tilCosto = findViewById(R.id.tilServicioCosto);
        tilTiempo = findViewById(R.id.tilServicioTiempo);
        btGuardar = findViewById(R.id.btServicioGuardar);
        servicios = Proveedor.servicios(this);

        String id = getIntent().getStringExtra(EXTRA_ID);
        if (id != null) {
            toolbar.setTitle(R.string.editar_servicio);
            servicios.observar(id).observe(this, servicio -> {
                if (servicio == null) { finish(); return; }
                servicioActual = servicio;
                if (!btGuardar.isEnabled()) return;
                tilNombre.getEditText().setText(servicio.nombre);
                tilDescripcion.getEditText().setText(servicio.descripcion);
                tilCosto.getEditText().setText(String.valueOf(servicio.costo));
                tilTiempo.getEditText().setText(String.valueOf(servicio.tiempoMinutos));
            });
        } else {
            toolbar.setTitle(R.string.agregar_servicio);
        }
        btGuardar.setOnClickListener(v -> guardar());
    }

    private void guardar() {
        tilNombre.setError(null);
        tilDescripcion.setError(null);
        tilCosto.setError(null);
        tilTiempo.setError(null);
        String nombre = texto(tilNombre).trim();
        String descripcion = texto(tilDescripcion).trim();
        String costoTexto = texto(tilCosto).trim().replace(',', '.');
        String tiempoTexto = texto(tilTiempo).trim();
        boolean valido = true;
        if (nombre.isEmpty()) { tilNombre.setError(getString(R.string.error_servicio_nombre)); valido = false; }
        if (descripcion.isEmpty()) { tilDescripcion.setError(getString(R.string.error_servicio_descripcion)); valido = false; }
        double costo = 0d;
        try {
            costo = Double.parseDouble(costoTexto);
            if (!Double.isFinite(costo) || costo < 0d) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            tilCosto.setError(getString(R.string.error_servicio_costo)); valido = false;
        }
        int tiempo = 0;
        try {
            tiempo = Integer.parseInt(tiempoTexto);
            if (tiempo <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            tilTiempo.setError(getString(R.string.error_servicio_tiempo)); valido = false;
        }
        if (!valido) return;

        ServicioEntity servicio = servicioActual == null ? ServicioEntity.nuevo() : servicioActual;
        servicio.nombre = nombre;
        servicio.descripcion = descripcion;
        servicio.costo = costo;
        servicio.tiempoMinutos = tiempo;
        btGuardar.setEnabled(false);
        btGuardar.setText(R.string.guardando);
        servicios.guardar(servicio, new Resultado<Boolean>() {
            @Override public void exito(Boolean sincronizado) {
                runOnUiThread(() -> {
                    Toast.makeText(ServicioFormActivity.this,
                            Boolean.TRUE.equals(sincronizado) ? R.string.servicio_guardado : R.string.servicio_guardado_pendiente,
                            Toast.LENGTH_LONG).show();
                    finish();
                });
            }
            @Override public void error(Exception e) {
                runOnUiThread(() -> {
                    btGuardar.setEnabled(true);
                    btGuardar.setText(R.string.guardar);
                    Toast.makeText(ServicioFormActivity.this, R.string.error_general, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private static String texto(TextInputLayout campo) {
        return campo.getEditText() == null || campo.getEditText().getText() == null
                ? "" : campo.getEditText().getText().toString();
    }
}