package sv.edu.ues.dam235.compucel.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import sv.edu.ues.dam235.compucel.datos.ErrorNegocio;
import sv.edu.ues.dam235.compucel.datos.Proveedor;
import sv.edu.ues.dam235.compucel.datos.Resultado;
import sv.edu.ues.dam235.compucel.datos.UsuarioRepositorio;
import sv.edu.ues.dam235.compucel.datos.local.UsuarioEntity;
import sv.edu.ues.dam235.compucel.util.Campos;
import sv.edu.ues.dam235.compucel.util.Validaciones;

public class RegistroActivity extends AppCompatActivity {

    private TextInputLayout tilCorreo;
    private TextInputLayout tilNombre;
    private TextInputLayout tilClave;
    private TextInputLayout tilConfirmar;
    private MaterialButton btCrear;
    private UsuarioRepositorio usuarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        tilCorreo = findViewById(R.id.tilCorreo);
        tilNombre = findViewById(R.id.tilNombre);
        tilClave = findViewById(R.id.tilClave);
        tilConfirmar = findViewById(R.id.tilConfirmar);
        btCrear = findViewById(R.id.btCrear);
        usuarios = Proveedor.usuarios(this);
        Campos.limpiarAlEscribir(tilCorreo, tilNombre, tilClave, tilConfirmar);

        btCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                crearCuenta();
            }
        });
    }

    private void crearCuenta() {
        String correo = Validaciones.normalizarCorreo(Campos.texto(tilCorreo));
        String nombre = Campos.texto(tilNombre).trim();
        String clave = Campos.texto(tilClave);
        String confirmar = Campos.texto(tilConfirmar);
        boolean valido = true;
        if (!Validaciones.correoValido(correo)) {
            Campos.mostrarError(tilCorreo, R.string.error_correo);
            valido = false;
        }
        if (!Validaciones.nombreValido(nombre)) {
            Campos.mostrarError(tilNombre, R.string.error_nombre);
            valido = false;
        }
        if (!Validaciones.claveValida(clave)) {
            Campos.mostrarError(tilClave, R.string.error_clave);
            valido = false;
        }
        if (!clave.equals(confirmar)) {
            Campos.mostrarError(tilConfirmar, R.string.error_claves_distintas);
            valido = false;
        }
        if (!valido) {
            return;
        }
        cargando(true);
        usuarios.registrar(correo, nombre, clave, new Resultado<UsuarioEntity>() {
            @Override
            public void exito(UsuarioEntity usuario) {
                runOnUiThread(() -> {
                    Intent datos = new Intent().putExtra(LoginActivity.EXTRA_CORREO, usuario.correo);
                    setResult(Activity.RESULT_OK, datos);
                    finish();
                });
            }

            @Override
            public void error(Exception e) {
                runOnUiThread(() -> {
                    cargando(false);
                    ErrorNegocio.Tipo tipo = e instanceof ErrorNegocio ? ((ErrorNegocio) e).getTipo() : null;
                    if (tipo == ErrorNegocio.Tipo.CORREO_EXISTE) {
                        Campos.mostrarError(tilCorreo, R.string.error_correo_existe);
                    } else if (tipo == ErrorNegocio.Tipo.SIN_CONEXION) {
                        Toast.makeText(RegistroActivity.this, R.string.error_sin_conexion_registro, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(RegistroActivity.this, R.string.error_general, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void cargando(boolean activo) {
        btCrear.setEnabled(!activo);
        btCrear.setText(activo ? R.string.registro_creando : R.string.crear_cuenta);
    }
}
