package sv.edu.ues.dam235.compucel.ui.clientes;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import sv.edu.ues.dam235.compucel.R;
import sv.edu.ues.dam235.compucel.datos.ClienteRepositorio;
import sv.edu.ues.dam235.compucel.datos.Proveedor;
import sv.edu.ues.dam235.compucel.datos.Resultado;
import sv.edu.ues.dam235.compucel.datos.local.ClienteEntity;
import sv.edu.ues.dam235.compucel.datos.local.EstadoSync;
import sv.edu.ues.dam235.compucel.util.Texto;

/** CLI-02 Detalle del cliente con editar, inactivar y eliminar (CLI-04). */
public class ClienteDetalleActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "cliente_id";

    private ClienteRepositorio clientes;
    private ClienteEntity cliente;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cliente_detalle);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        clientes = Proveedor.clientes(this);
        String id = getIntent().getStringExtra(EXTRA_ID);
        clientes.observar(id).observe(this, actual -> {
            // Si se elimino (o se confirmo el borrado en la nube) ya no hay nada que mostrar
            if (actual == null || actual.eliminado) {
                finish();
                return;
            }
            cliente = actual;
            mostrar(actual);
        });

        findViewById(R.id.btLlamar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cliente != null) {
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + cliente.telefono)));
                }
            }
        });
        findViewById(R.id.btCorreo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cliente != null && cliente.correo != null) {
                    startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + cliente.correo)));
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cliente, menu);
        this.menu = menu;
        if (cliente != null) {
            actualizarMenu(cliente);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (cliente == null) {
            return false;
        } else if (item.getItemId() == R.id.menu_editar) {
            Intent intent = new Intent(this, ClienteFormActivity.class);
            intent.putExtra(ClienteFormActivity.EXTRA_ID, cliente.id);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.menu_eliminar) {
            confirmarEliminar();
            return true;
        } else if (item.getItemId() == R.id.menu_activo) {
            confirmarCambioDeEstado();
            return true;
        } else if (item.getItemId() == R.id.menu_estado) {
            Toast.makeText(this, EstadoSync.PENDIENTE.equals(cliente.estadoSync)
                    ? R.string.cliente_pendiente : R.string.sincronizacion_al_dia, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void mostrar(ClienteEntity c) {
        ((TextView) findViewById(R.id.tvAvatar)).setText(Texto.iniciales(c.nombre));
        ((TextView) findViewById(R.id.tvNombre)).setText(c.nombre);
        View badge = findViewById(R.id.badgeEstado);
        TextView tvEstado = findViewById(R.id.tvEstado);
        ImageView punto = findViewById(R.id.ivPuntoEstado);
        badge.setBackgroundResource(c.activo ? R.drawable.fondo_badge_exito : R.drawable.fondo_badge_neutro);
        tvEstado.setText(c.activo ? R.string.estado_activo : R.string.estado_inactivo);
        tvEstado.setTextColor(ContextCompat.getColor(this, c.activo ? R.color.exito_texto : R.color.neutro_texto));
        punto.setColorFilter(ContextCompat.getColor(this, c.activo ? R.color.exito_punto : R.color.neutro_punto));
        findViewById(R.id.badgePendiente).setVisibility(
                EstadoSync.PENDIENTE.equals(c.estadoSync) ? View.VISIBLE : View.GONE);
        dato(findViewById(R.id.filaTelefono), R.string.telefono, c.telefono);
        dato(findViewById(R.id.filaCorreo), R.string.correo, c.correo);
        dato(findViewById(R.id.filaDireccion), R.string.direccion, c.direccion);
        findViewById(R.id.btCorreo).setEnabled(c.correo != null && !c.correo.isEmpty());
        if (menu != null) {
            actualizarMenu(c);
        }
    }

    private void actualizarMenu(ClienteEntity c) {
        menu.findItem(R.id.menu_estado).setIcon(EstadoSync.PENDIENTE.equals(c.estadoSync)
                ? R.drawable.ic_cloud_upload : R.drawable.ic_cloud_done);
        menu.findItem(R.id.menu_activo).setTitle(c.activo ? R.string.inactivar : R.string.activar);
    }

    private void dato(View fila, @StringRes int etiqueta, String valor) {
        ((TextView) fila.findViewById(R.id.tvEtiqueta)).setText(etiqueta);
        ((TextView) fila.findViewById(R.id.tvValor)).setText(
                valor == null || valor.isEmpty() ? getString(R.string.sin_dato) : valor);
    }

    // CLI-04: inactivar conserva el cliente; activar lo devuelve a la lista
    private void confirmarCambioDeEstado() {
        boolean activo = cliente.activo;
        new MaterialAlertDialogBuilder(this)
                .setTitle(activo ? R.string.inactivar_titulo : R.string.activar_titulo)
                .setMessage(getString(activo ? R.string.inactivar_mensaje : R.string.activar_mensaje, cliente.nombre))
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(activo ? R.string.inactivar : R.string.activar, (dialogo, boton) ->
                        clientes.cambiarActivo(cliente.id, !activo, avisar(
                                activo ? R.string.cliente_inactivado : R.string.cliente_activado, R.string.guardado_local)))
                .show();
    }

    private void confirmarEliminar() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.eliminar_titulo)
                .setMessage(getString(R.string.eliminar_mensaje, cliente.nombre))
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.eliminar, (dialogo, boton) ->
                        clientes.eliminar(cliente.id, avisar(R.string.cliente_eliminado, R.string.eliminado_local)))
                .show();
    }

    private Resultado<Boolean> avisar(@StringRes int enNube, @StringRes int enLocal) {
        return new Resultado<Boolean>() {
            @Override
            public void exito(Boolean sincronizado) {
                runOnUiThread(() -> Toast.makeText(ClienteDetalleActivity.this,
                        sincronizado ? enNube : enLocal, Toast.LENGTH_LONG).show());
            }

            @Override
            public void error(Exception e) {
                runOnUiThread(() -> Toast.makeText(ClienteDetalleActivity.this,
                        R.string.error_general, Toast.LENGTH_LONG).show());
            }
        };
    }
}
