package sv.edu.ues.dam235.compucel.ui.clientes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import sv.edu.ues.dam235.compucel.R;
import sv.edu.ues.dam235.compucel.datos.ClienteRepositorio;
import sv.edu.ues.dam235.compucel.datos.Proveedor;
import sv.edu.ues.dam235.compucel.datos.Resultado;
import sv.edu.ues.dam235.compucel.datos.local.ClienteEntity;
import sv.edu.ues.dam235.compucel.util.Campos;
import sv.edu.ues.dam235.compucel.util.Texto;
import sv.edu.ues.dam235.compucel.util.Validaciones;

/** CLI-03 Agregar o editar cliente. */
public class ClienteFormActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "cliente_id";

    private TextInputLayout tilNombre;
    private TextInputLayout tilTelefono;
    private TextInputLayout tilCorreo;
    private TextInputLayout tilDireccion;
    private MaterialButton btGuardar;
    private ClienteRepositorio clientes;
    // Se crea una sola vez: repetir "Guardar" no duplica el cliente
    private ClienteEntity cliente;
    private boolean huboCambios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cliente_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> salir());
        tilNombre = findViewById(R.id.tilNombre);
        tilTelefono = findViewById(R.id.tilTelefono);
        tilCorreo = findViewById(R.id.tilCorreo);
        tilDireccion = findViewById(R.id.tilDireccion);
        btGuardar = findViewById(R.id.btGuardar);
        clientes = Proveedor.clientes(this);
        Campos.limpiarAlEscribir(tilNombre, tilTelefono, tilCorreo);
        darFormatoAlTelefono(tilTelefono.getEditText());

        String id = getIntent().getStringExtra(EXTRA_ID);
        if (id == null) {
            cliente = ClienteEntity.nuevo(Proveedor.sesion(this).correoActual());
            vigilarCambios();
        } else {
            toolbar.setTitle(R.string.editar_cliente);
            btGuardar.setEnabled(false);
            clientes.observar(id).observe(this, existente -> {
                if (existente != null && cliente == null) {
                    cliente = existente;
                    tilNombre.getEditText().setText(existente.nombre);
                    tilTelefono.getEditText().setText(existente.telefono);
                    tilCorreo.getEditText().setText(existente.correo);
                    tilDireccion.getEditText().setText(existente.direccion);
                    btGuardar.setEnabled(true);
                    vigilarCambios();
                }
            });
        }

        btGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardar();
            }
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                salir();
            }
        });
    }

    private void guardar() {
        String nombre = Campos.texto(tilNombre).trim();
        String telefono = Campos.texto(tilTelefono).trim();
        String correo = Validaciones.normalizarCorreo(Campos.texto(tilCorreo));
        String direccion = Campos.texto(tilDireccion).trim();
        boolean valido = true;
        if (nombre.isEmpty()) {
            Campos.mostrarError(tilNombre, R.string.error_nombre_obligatorio);
            valido = false;
        }
        if (!Validaciones.telefonoValido(telefono)) {
            Campos.mostrarError(tilTelefono, R.string.error_telefono);
            valido = false;
        }
        if (!correo.isEmpty() && !Validaciones.correoValido(correo)) {
            Campos.mostrarError(tilCorreo, R.string.error_correo);
            valido = false;
        }
        if (!valido) {
            return;
        }
        cliente.nombre = nombre;
        cliente.telefono = telefono;
        cliente.correo = correo.isEmpty() ? null : correo;
        cliente.direccion = direccion.isEmpty() ? null : direccion;
        btGuardar.setEnabled(false);
        btGuardar.setText(R.string.guardando);
        clientes.guardar(cliente, new Resultado<Boolean>() {
            @Override
            public void exito(Boolean sincronizado) {
                runOnUiThread(() -> {
                    Toast.makeText(ClienteFormActivity.this,
                            sincronizado ? R.string.cliente_guardado : R.string.guardado_local, Toast.LENGTH_LONG).show();
                    finish();
                });
            }

            @Override
            public void error(Exception e) {
                runOnUiThread(() -> {
                    btGuardar.setEnabled(true);
                    btGuardar.setText(R.string.guardar);
                    Toast.makeText(ClienteFormActivity.this, R.string.error_general, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    // Pregunta antes de salir si hay datos escritos sin guardar (patron Formulario del PRD)
    private void salir() {
        if (!huboCambios) {
            finish();
            return;
        }
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.cambios_sin_guardar_titulo)
                .setMessage(R.string.cambios_sin_guardar_mensaje)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.descartar, (dialogo, boton) -> finish())
                .show();
    }

    private void vigilarCambios() {
        TextWatcher vigia = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                huboCambios = true;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };
        for (TextInputLayout campo : new TextInputLayout[]{tilNombre, tilTelefono, tilCorreo, tilDireccion}) {
            campo.getEditText().addTextChangedListener(vigia);
        }
    }

    private static void darFormatoAlTelefono(EditText campo) {
        campo.addTextChangedListener(new TextWatcher() {
            private boolean editando;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (editando) {
                    return;
                }
                String formato = Texto.formatoTelefono(s.toString());
                if (!formato.equals(s.toString())) {
                    editando = true;
                    s.replace(0, s.length(), formato);
                    editando = false;
                }
            }
        });
    }
}
