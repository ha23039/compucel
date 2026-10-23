package sv.edu.ues.dam235.compucel.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import sv.edu.ues.dam235.compucel.R;

/** INI-02 Mas: accesos a gestion y sistema, y cierre de sesion. */
public class MasFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MainActivity main = (MainActivity) requireActivity();
        fila(view.findViewById(R.id.filaClientes), R.drawable.ic_people, R.string.clientes, R.string.mas_clientes_detalle,
                v -> main.mostrarClientes());
        fila(view.findViewById(R.id.filaUbicacion), R.drawable.ic_location_on, R.string.ubicacion_taller,
                R.string.mas_ubicacion_detalle, v -> main.abrirMapa());
        View filaSync = view.findViewById(R.id.filaSincronizacion);
        fila(filaSync, R.drawable.ic_sync, R.string.sincronizacion, R.string.sincronizacion_al_dia,
                v -> main.abrirSincronizacion());
        fila(view.findViewById(R.id.filaPerfil), R.drawable.ic_person, R.string.perfil, R.string.mas_perfil_detalle,
                v -> main.abrirPerfil());
        view.findViewById(R.id.btCerrarSesion).setOnClickListener(v -> main.confirmarCerrarSesion());

        TextView detalleSync = filaSync.findViewById(R.id.tvDetalle);
        main.pendientes().observe(getViewLifecycleOwner(), n -> {
            int cantidad = n == null ? 0 : n;
            detalleSync.setText(cantidad > 0
                    ? getResources().getQuantityString(R.plurals.cambios_pendientes, cantidad, cantidad)
                    : getString(R.string.sincronizacion_al_dia));
        });
    }

    private void fila(View fila, @DrawableRes int icono, @StringRes int titulo, @StringRes int detalle, View.OnClickListener accion) {
        ImageView ivIcono = fila.findViewById(R.id.ivIcono);
        ivIcono.setImageResource(icono);
        ivIcono.setColorFilter(ContextCompat.getColor(requireContext(), R.color.primary));
        ((TextView) fila.findViewById(R.id.tvTitulo)).setText(titulo);
        ((TextView) fila.findViewById(R.id.tvDetalle)).setText(detalle);
        fila.setOnClickListener(accion);
    }
}
