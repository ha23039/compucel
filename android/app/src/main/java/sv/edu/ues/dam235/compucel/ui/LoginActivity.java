package sv.edu.ues.dam235.compucel.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputLayout;

import sv.edu.ues.dam235.compucel.R;
import sv.edu.ues.dam235.compucel.datos.ErrorNegocio;
import sv.edu.ues.dam235.compucel.datos.Proveedor;
import sv.edu.ues.dam235.compucel.datos.Resultado;
import sv.edu.ues.dam235.compucel.datos.UsuarioRepositorio;
import sv.edu.ues.dam235.compucel.datos.local.UsuarioEntity;
import sv.edu.ues.dam235.compucel.util.Campos;
import sv.edu.ues.dam235.compucel.util.Sesion;
import sv.edu.ues.dam235.compucel.util.Validaciones;

public class LoginActivity extends AppCompatActivity {

    public static final String EXTRA_CORREO = "correo";

    private TextInputLayout tilCorreo;
    private TextInputLayout tilClave;
    private Chip chipRecordarme;
    private MaterialButton btIniciar;
    private Sesion sesion;
    private UsuarioRepositorio usuarios;

    private final ActivityResultLauncher<Intent> registro = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (resultado.getResultCode() == Activity.RESULT_OK && resultado.getData() != null) {
                    tilCorreo.getEditText().setText(resultado.getData().getStringExtra(EXTRA_CORREO));
                    tilClave.getEditText().setText("");
                    tilClave.requestFocus();
                    Toast.makeText(this, R.string.registro_listo, Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tilCorreo = findViewById(R.id.tilCorreo);
        tilClave = findViewById(R.id.tilClave);
        chipRecordarme = findViewById(R.id.chipRecordarme);
        btIniciar = findViewById(R.id.btIniciar);
        TextView tvCrearCuenta = findViewById(R.id.tvCrearCuenta);
        sesion = Proveedor.sesion(this);
        usuarios = Proveedor.usuarios(this);

        // Si el usuario eligio "Recordarme", se rellenan el correo y la contrasena guardados
        if (sesion.hayRecordado()) {
            tilCorreo.getEditText().setText(sesion.correoRecordado());
            String clave = sesion.claveRecordada();
            if (clave != null) {
                tilClave.getEditText().setText(clave);
            }
            chipRecordarme.setChecked(true);
        }
        Campos.limpiarAlEscribir(tilCorreo, tilClave);

        btIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iniciarSesion();
            }
        });
        tvCrearCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registro.launch(new Intent(LoginActivity.this, RegistroActivity.class));
            }
        });
    }

    private void iniciarSesion() {
        String correo = Validaciones.normalizarCorreo(Campos.texto(tilCorreo));
        String clave = Campos.texto(tilClave);
        boolean valido = true;
        if (!Validaciones.correoValido(correo)) {
            Campos.mostrarError(tilCorreo, R.string.error_correo);
            valido = false;
        }
        if (!Validaciones.claveValida(clave)) {
            Campos.mostrarError(tilClave, R.string.error_clave);
            valido = false;
        }
        if (!valido) {
            return;
        }
        cargando(true);
        usuarios.iniciarSesion(correo, clave, new Resultado<UsuarioEntity>() {
            @Override
            public void exito(UsuarioEntity usuario) {
                runOnUiThread(() -> {
                    if (chipRecordarme.isChecked()) {
                        sesion.recordar(usuario.correo, clave);
                    } else {
                        sesion.olvidar();
                    }
                    sesion.abrir(usuario.correo, usuario.nombre);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                });
            }

            @Override
            public void error(Exception e) {
                runOnUiThread(() -> {
                    cargando(false);
                    ErrorNegocio.Tipo tipo = e instanceof ErrorNegocio ? ((ErrorNegocio) e).getTipo() : null;
                    if (tipo == ErrorNegocio.Tipo.CREDENCIALES) {
                        Campos.mostrarError(tilClave, R.string.error_credenciales);
                    } else if (tipo == ErrorNegocio.Tipo.SIN_CUENTA_LOCAL) {
                        Toast.makeText(LoginActivity.this, R.string.error_sin_cuenta_local, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this, R.string.error_general, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void cargando(boolean activo) {
        btIniciar.setEnabled(!activo);
        btIniciar.setText(activo ? R.string.login_entrando : R.string.login_boton);
    }
}
