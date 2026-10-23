package sv.edu.ues.dam235.compucel.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

import sv.edu.ues.dam235.compucel.R;
import sv.edu.ues.dam235.compucel.datos.ClienteRepositorio;
import sv.edu.ues.dam235.compucel.datos.Proveedor;
import sv.edu.ues.dam235.compucel.ui.clientes.ClienteFormActivity;

/** INI-01 Inicio. */
public class InicioFragment extends Fragment {

    private int activos;
    private int total;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_inicio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MainActivity main = (MainActivity) requireActivity();
        TextView tvSaludo = view.findViewById(R.id.tvSaludo);
        TextView tvActivos = view.findViewById(R.id.tvActivos);
        TextView tvInactivos = view.findViewById(R.id.tvInactivos);
        TextView tvTotal = view.findViewById(R.id.tvTotal);
        TextView tvPendientes = view.findViewById(R.id.tvPendientes);
        View banner = view.findViewById(R.id.bannerSinConexion);
        ImageView ivAtencion = view.findViewById(R.id.ivAtencion);
        TextView tvAtencionTitulo = view.findViewById(R.id.tvAtencionTitulo);
        TextView tvAtencionDetalle = view.findViewById(R.id.tvAtencionDetalle);

        tvSaludo.setText(getString(saludo(), primerNombre(Proveedor.sesion(requireContext()).nombreActual())));

        ClienteRepositorio clientes = Proveedor.clientes(requireContext());
        clientes.totalActivos().observe(getViewLifecycleOwner(), n -> {
            activos = n == null ? 0 : n;
            tvActivos.setText(String.valueOf(activos));
            tvInactivos.setText(String.valueOf(Math.max(total - activos, 0)));
        });
        clientes.total().observe(getViewLifecycleOwner(), n -> {
            total = n == null ? 0 : n;
            tvTotal.setText(String.valueOf(total));
            tvInactivos.setText(String.valueOf(Math.max(total - activos, 0)));
        });
        main.pendientes().observe(getViewLifecycleOwner(), n -> {
            int cantidad = n == null ? 0 : n;
            tvPendientes.setText(String.valueOf(cantidad));
            tvPendientes.setTextColor(ContextCompat.getColor(requireContext(),
                    cantidad > 0 ? R.color.advertencia_texto : R.color.on_surface));
            if (cantidad > 0) {
                ivAtencion.setImageResource(R.drawable.ic_cloud_upload);
                tvAtencionTitulo.setText(getResources().getQuantityString(R.plurals.cambios_sin_sincronizar, cantidad, cantidad));
                tvAtencionDetalle.setText(R.string.atencion_pendientes_detalle);
            } else {
                ivAtencion.setImageResource(R.drawable.ic_cloud_done);
                tvAtencionTitulo.setText(R.string.atencion_al_dia);
                tvAtencionDetalle.setText(R.string.atencion_al_dia_detalle);
            }
        });
        main.hayRed().observe(getViewLifecycleOwner(), conectado ->
                banner.setVisibility(Boolean.FALSE.equals(conectado) ? View.VISIBLE : View.GONE));

        view.findViewById(R.id.accesoNuevoCliente).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireContext(), ClienteFormActivity.class));
            }
        });
        view.findViewById(R.id.accesoClientes).setOnClickListener(v -> main.mostrarClientes());
        view.findViewById(R.id.accesoUbicacion).setOnClickListener(v -> main.abrirMapa());
        view.findViewById(R.id.accesoSincronizar).setOnClickListener(v -> main.abrirSincronizacion());
        view.findViewById(R.id.accesoPerfil).setOnClickListener(v -> main.abrirPerfil());
        view.findViewById(R.id.cardAtencion).setOnClickListener(v -> main.abrirSincronizacion());
    }

    private static int saludo() {
        int hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hora < 12) {
            return R.string.buenos_dias;
        } else if (hora < 19) {
            return R.string.buenas_tardes;
        }
        return R.string.buenas_noches;
    }

    private static String primerNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return "";
        }
        return nombre.trim().split("\\s+")[0];
    }
}
