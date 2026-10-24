package sv.edu.ues.dam235.compucel.ui.servicios;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import sv.edu.ues.dam235.compucel.R;
import sv.edu.ues.dam235.compucel.datos.Proveedor;
import sv.edu.ues.dam235.compucel.datos.Resultado;
import sv.edu.ues.dam235.compucel.datos.ServicioRepositorio;
import sv.edu.ues.dam235.compucel.datos.local.ServicioEntity;
import sv.edu.ues.dam235.compucel.util.Red;

public class ServiciosFragment extends Fragment {

    private ServicioRepositorio servicios;
    private ServicioAdapter adapter;
    private View estadoVacio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_servicios, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        servicios = Proveedor.servicios(requireContext());
        estadoVacio = view.findViewById(R.id.tvServiciosVacio);
        RecyclerView lista = view.findViewById(R.id.rvServicios);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ServicioAdapter(new ServicioAdapter.Acciones() {
            @Override
            public void editar(ServicioEntity servicio) {
                Intent intent = new Intent(requireContext(), ServicioFormActivity.class);
                intent.putExtra(ServicioFormActivity.EXTRA_ID, servicio.id);
                startActivity(intent);
            }

            @Override
            public void eliminar(ServicioEntity servicio) {
                confirmarEliminar(servicio);
            }
        });
        lista.setAdapter(adapter);
        servicios.lista().observe(getViewLifecycleOwner(), datos -> {
            adapter.setServicios(datos);
            estadoVacio.setVisibility(datos == null || datos.isEmpty() ? View.VISIBLE : View.GONE);
        });
        view.findViewById(R.id.fabServicio).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), ServicioFormActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (servicios != null && Red.hayInternet(requireContext())) {
            servicios.refrescarDesdeNube(new Resultado<Integer>() {
                @Override public void exito(Integer valor) { }
                @Override public void error(Exception e) { }
            });
        }
    }

    private void confirmarEliminar(ServicioEntity servicio) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.eliminar_servicio_titulo)
                .setMessage(getString(R.string.eliminar_servicio_mensaje, servicio.nombre))
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.eliminar, (dialog, which) -> servicios.eliminar(servicio.id, new Resultado<Boolean>() {
                    @Override public void exito(Boolean sincronizado) { }
                    @Override public void error(Exception e) {
                        if (isAdded()) requireActivity().runOnUiThread(() ->
                                android.widget.Toast.makeText(requireContext(), R.string.error_general, android.widget.Toast.LENGTH_LONG).show());
                    }
                }))
                .show();
    }
}