package sv.edu.ues.dam235.compucel.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import sv.edu.ues.dam235.compucel.R;
import sv.edu.ues.dam235.compucel.datos.Proveedor;

/** Pantalla de inicio con el logo; decide si va al inicio de sesion o directo a la app. */
public class SplashActivity extends AppCompatActivity {

    private static final long DURACION = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                boolean sesionAbierta = Proveedor.sesion(SplashActivity.this).abierta();
                Class<?> destino = sesionAbierta ? MainActivity.class : LoginActivity.class;
                startActivity(new Intent(SplashActivity.this, destino));
                finish();
            }
        }, DURACION);
    }
}
