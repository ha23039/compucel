package sv.edu.ues.dam235.compucel.ui.clientes;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;

import java.util.List;

import sv.edu.ues.dam235.compucel.R;
import sv.edu.ues.dam235.compucel.datos.ClienteRepositorio;
import sv.edu.ues.dam235.compucel.datos.Proveedor;
import sv.edu.ues.dam235.compucel.datos.Resultado;
import sv.edu.ues.dam235.compucel.datos.local.ClienteEntity;
import sv.edu.ues.dam235.compucel.util.Red;

/** CLI-01 Lista de clientes: busqueda por nombre o telefono y filtro Activos / Inactivos. */
public class ClientesFragment extends Fragment {

    private ClienteRepositorio clientes;
    private ClienteAdapter adapter;
    private LiveData<List<ClienteEntity>> listaActual;
    private EditText edtBuscar;
    private ChipGroup grupoFiltro;
    private View estadoVacio;
    private ImageView ivVacio;
    private TextView tvVacioTitulo;
    private TextView tvVacioDetalle;
    private View btLimpiarBusqueda;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_clientes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        clientes = Proveedor.clientes(requireContext());
        edtBuscar = view.findViewById(R.id.edtBuscar);
        grupoFiltro = view.findViewById(R.id.grupoFiltro);
        estadoVacio = view.findViewById(R.id.estadoVacio);
        ivVacio = view.findViewById(R.id.ivVacio);
        tvVacioTitulo = view.findViewById(R.id.tvVacioTitulo);
        tvVacioDetalle = view.findViewById(R.id.tvVacioDetalle);
        btLimpiarBusqueda = view.findViewById(R.id.btLimpiarBusqueda);

        RecyclerView rvClientes = view.findViewById(R.id.rvClientes);
        rvClientes.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ClienteAdapter(new ClienteAdapter.AlTocar() {
            @Override
            public void cliente(ClienteEntity cliente) {
                Intent intent = new Intent(requireContext(), ClienteDetalleActivity.class);
                intent.putExtra(ClienteDetalleActivity.EXTRA_ID, cliente.id);
                startActivity(intent);
            }
        });
        rvClientes.setAdapter(adapter);

        edtBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cargarLista();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        grupoFiltro.setOnCheckedStateChangeListener((grupo, ids) -> cargarLista());
        btLimpiarBusqueda.setOnClickListener(v -> edtBuscar.setText(""));
        view.findViewById(R.id.fabAgregar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(requireContext(), ClienteFormActivity.class));
            }
        });
        cargarLista();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Con internet se traen los clientes registrados desde otros telefonos
        if (Red.hayInternet(requireContext())) {
            clientes.refrescarDesdeNube(new Resultado<Integer>() {
                @Override
                public void exito(Integer valor) {
                }

                @Override
                public void error(Exception e) {
                }
            });
        }
    }

    private void cargarLista() {
        if (listaActual != null) {
            listaActual.removeObservers(getViewLifecycleOwner());
        }
        boolean activos = grupoFiltro.getCheckedChipId() != R.id.chipInactivos;
        String filtro = edtBuscar.getText().toString().trim();
        listaActual = clientes.lista(activos, filtro);
        listaActual.observe(getViewLifecycleOwner(), lista -> {
            adapter.setClientes(lista);
            mostrarVacio(lista == null || lista.isEmpty(), filtro, activos);
        });
    }

    private void mostrarVacio(boolean vacio, String filtro, boolean activos) {
        estadoVacio.setVisibility(vacio ? View.VISIBLE : View.GONE);
        if (!vacio) {
            return;
        }
        if (!filtro.isEmpty()) {
            ivVacio.setImageResource(R.drawable.ic_search_grande);
            tvVacioTitulo.setText(getString(R.string.sin_resultados, filtro));
            tvVacioDetalle.setText(R.string.sin_resultados_detalle);
            btLimpiarBusqueda.setVisibility(View.VISIBLE);
        } else {
            ivVacio.setImageResource(R.drawable.ic_people_grande);
            tvVacioTitulo.setText(activos ? R.string.sin_clientes : R.string.sin_inactivos);
            tvVacioDetalle.setText(activos ? R.string.sin_clientes_detalle : R.string.sin_inactivos_detalle);
            btLimpiarBusqueda.setVisibility(View.GONE);
        }
    }
}
