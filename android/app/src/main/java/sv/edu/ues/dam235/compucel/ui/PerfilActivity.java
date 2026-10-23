package sv.edu.ues.dam235.compucel.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import sv.edu.ues.dam235.compucel.R;
import sv.edu.ues.dam235.compucel.datos.ErrorNegocio;
import sv.edu.ues.dam235.compucel.datos.Proveedor;
import sv.edu.ues.dam235.compucel.datos.Resultado;
import sv.edu.ues.dam235.compucel.datos.UsuarioRepositorio;
import sv.edu.ues.dam235.compucel.util.Campos;
import sv.edu.ues.dam235.compucel.util.Sesion;
import sv.edu.ues.dam235.compucel.util.Texto;
import sv.edu.ues.dam235.compucel.util.Validaciones;

/**
 * Perfil del usuario: actualiza nombre y contrasena (con confirmacion) en Room y en Firestore.
 * Si no hay red, el cambio queda pendiente y se reintenta con "Sincronizar ahora".
 */
public class PerfilActivity extends AppCompatActivity {

    private Sesion sesion;
    private UsuarioRepositorio usuarios;
    private TextView tvEstadoPerfil;
    private MaterialButton btSincronizar;
    private int pendientesUsuario;
    private int pendientesClientes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_perfil);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        sesion = Proveedor.sesion(this);
        usuarios = Proveedor.usuarios(this);
        tvEstadoPerfil = findViewById(R.id.tvEstadoPerfil);
        btSincronizar = findViewById(R.id.btSincronizar);

        fila(findViewById(R.id.filaCambiarNombre), R.drawable.ic_edit, R.string.cambiar_nombre, R.string.cambiar_nombre_detalle,
                v -> pedirNombre());
        fila(findViewById(R.id.filaCambiarClave), R.drawable.ic_lock, R.string.cambiar_clave, R.string.cambiar_clave_detalle,
                v -> pedirClave());
        dato(findViewById(R.id.filaCorreo), R.string.campo_correo, sesion.correoActual());
        dato(findViewById(R.id.filaVersion), R.string.version_app, version());
        mostrarDatos();
        configurarTema();

        usuarios.pendientes().observe(this, n -> {
            pendientesUsuario = n == null ? 0 : n;
            mostrarPendientes();
        });
        Proveedor.clientes(this).pendientes().observe(this, n -> {
            pendientesClientes = n == null ? 0 : n;
            mostrarPendientes();
        });

        btSincronizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btSincronizar.setEnabled(false);
                btSincronizar.setText(R.string.sincronizando);
                SincronizarAccion.ejecutar(PerfilActivity.this, resumen -> {
                    btSincronizar.setEnabled(true);
                    btSincronizar.setText(R.string.sincronizar_ahora);
                    mostrarDatos();
                });
            }
        });
        findViewById(R.id.btCerrarSesion).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra(MainActivity.EXTRA_CERRAR_SESION, true);
            startActivity(intent);
        });
    }

    private void mostrarDatos() {
        String nombre = sesion.nombreActual();
        ((TextView) findViewById(R.id.tvAvatar)).setText(Texto.iniciales(nombre));
        ((TextView) findViewById(R.id.tvNombre)).setText(nombre);
        long ultima = sesion.ultimaSincronizacion();
        String texto;
        if (ultima == 0) {
            texto = getString(R.string.nunca);
        } else if (DateUtils.isToday(ultima)) {
            texto = getString(R.string.hoy_hora, new SimpleDateFormat("HH:mm", Locale.ROOT).format(new Date(ultima)));
        } else {
            texto = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ROOT).format(new Date(ultima));
        }
        dato(findViewById(R.id.filaUltimaSync), R.string.ultima_sincronizacion, texto);
    }

    private void mostrarPendientes() {
        int total = pendientesUsuario + pendientesClientes;
        dato(findViewById(R.id.filaPendientes), R.string.cambios_por_enviar, String.valueOf(total));
        boolean pendiente = pendientesUsuario > 0;
        tvEstadoPerfil.setText(pendiente ? R.string.perfil_pendiente : R.string.perfil_sincronizado);
        tvEstadoPerfil.setBackgroundResource(pendiente ? R.drawable.fondo_badge_advertencia : R.drawable.fondo_badge_neutro);
        tvEstadoPerfil.setTextColor(ContextCompat.getColor(this, pendiente ? R.color.advertencia_texto : R.color.neutro_texto));
    }

    // Paso 1: escribir el nombre; paso 2: confirmar el cambio
    private void pedirNombre() {
        View vista = LayoutInflater.from(this).inflate(R.layout.dialogo_nombre, null);
        TextInputLayout tilNombre = vista.findViewById(R.id.tilNombre);
        tilNombre.getEditText().setText(sesion.nombreActual());
        Campos.limpiarAlEscribir(tilNombre);
        AlertDialog dialogo = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.cambiar_nombre)
                .setView(vista)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.guardar, null)
                .create();
        dialogo.setOnShowListener(d -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nombre = Campos.texto(tilNombre).trim();
            if (!Validaciones.nombreValido(nombre)) {
                Campos.mostrarError(tilNombre, R.string.error_nombre);
                return;
            }
            dialogo.dismiss();
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.confirmar_nombre_titulo)
                    .setMessage(getString(R.string.confirmar_nombre_mensaje, nombre))
                    .setNegativeButton(R.string.cancelar, null)
                    .setPositiveButton(R.string.confirmar, (c, b) -> guardarNombre(nombre))
                    .show();
        }));
        dialogo.show();
    }

    private void guardarNombre(String nombre) {
        usuarios.actualizarNombre(sesion.correoActual(), nombre, new Resultado<Boolean>() {
            @Override
            public void exito(Boolean sincronizado) {
                runOnUiThread(() -> {
                    sesion.actualizarNombre(nombre);
                    mostrarDatos();
                    Toast.makeText(PerfilActivity.this,
                            sincronizado ? R.string.nombre_actualizado : R.string.guardado_local, Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void error(Exception e) {
                runOnUiThread(() -> Toast.makeText(PerfilActivity.this, R.string.error_general, Toast.LENGTH_LONG).show());
            }
        });
    }

    // La contrasena nueva se escribe dos veces (confirmacion) y se pide la actual
    private void pedirClave() {
        View vista = LayoutInflater.from(this).inflate(R.layout.dialogo_clave, null);
        TextInputLayout tilActual = vista.findViewById(R.id.tilActual);
        TextInputLayout tilNueva = vista.findViewById(R.id.tilNueva);
        TextInputLayout tilConfirmar = vista.findViewById(R.id.tilConfirmar);
        Campos.limpiarAlEscribir(tilActual, tilNueva, tilConfirmar);
        AlertDialog dialogo = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.cambiar_clave)
                .setView(vista)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.guardar, null)
                .create();
        dialogo.setOnShowListener(d -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String actual = Campos.texto(tilActual);
            String nueva = Campos.texto(tilNueva);
            String confirmar = Campos.texto(tilConfirmar);
            boolean valido = true;
            if (actual.isEmpty()) {
                Campos.mostrarError(tilActual, R.string.error_clave_actual);
                valido = false;
            }
            if (!Validaciones.claveValida(nueva)) {
                Campos.mostrarError(tilNueva, R.string.error_clave);
                valido = false;
            } else if (nueva.equals(actual)) {
                Campos.mostrarError(tilNueva, R.string.error_clave_igual);
                valido = false;
            }
            if (!nueva.equals(confirmar)) {
                Campos.mostrarError(tilConfirmar, R.string.error_claves_distintas);
                valido = false;
            }
            if (!valido) {
                return;
            }
            usuarios.cambiarClave(sesion.correoActual(), actual, nueva, new Resultado<Boolean>() {
                @Override
                public void exito(Boolean sincronizado) {
                    runOnUiThread(() -> {
                        dialogo.dismiss();
                        // Si el usuario tenia "Recordarme", se guarda la nueva (cifrada)
                        if (sesion.hayRecordado()) {
                            sesion.recordar(sesion.correoActual(), nueva);
                        }
                        Toast.makeText(PerfilActivity.this,
                                sincronizado ? R.string.clave_actualizada : R.string.guardado_local, Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void error(Exception e) {
                    runOnUiThread(() -> {
                        if (e instanceof ErrorNegocio && ((ErrorNegocio) e).getTipo() == ErrorNegocio.Tipo.CREDENCIALES) {
                            Campos.mostrarError(tilActual, R.string.error_clave_actual);
                        } else {
                            Toast.makeText(PerfilActivity.this, R.string.error_general, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }));
        dialogo.show();
    }

    private void configurarTema() {
        MaterialButtonToggleGroup grupo = findViewById(R.id.grupoTema);
        int tema = sesion.tema();
        grupo.check(tema == AppCompatDelegate.MODE_NIGHT_NO ? R.id.btTemaClaro
                : tema == AppCompatDelegate.MODE_NIGHT_YES ? R.id.btTemaOscuro : R.id.btTemaSistema);
        grupo.addOnButtonCheckedListener((g, id, marcado) -> {
            if (!marcado) {
                return;
            }
            int modo = id == R.id.btTemaClaro ? AppCompatDelegate.MODE_NIGHT_NO
                    : id == R.id.btTemaOscuro ? AppCompatDelegate.MODE_NIGHT_YES
                    : AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            if (modo != sesion.tema()) {
                sesion.guardarTema(modo);
                AppCompatDelegate.setDefaultNightMode(modo);
            }
        });
    }

    private void fila(View fila, @DrawableRes int icono, @StringRes int titulo, @StringRes int detalle, View.OnClickListener accion) {
        ImageView ivIcono = fila.findViewById(R.id.ivIcono);
        ivIcono.setImageResource(icono);
        ivIcono.setColorFilter(ContextCompat.getColor(this, R.color.primary));
        ((TextView) fila.findViewById(R.id.tvTitulo)).setText(titulo);
        ((TextView) fila.findViewById(R.id.tvDetalle)).setText(detalle);
        fila.setOnClickListener(accion);
    }

    private void dato(View fila, @StringRes int etiqueta, String valor) {
        ((TextView) fila.findViewById(R.id.tvEtiqueta)).setText(etiqueta);
        ((TextView) fila.findViewById(R.id.tvValor)).setText(valor);
    }

    private String version() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "";
        }
    }
}
