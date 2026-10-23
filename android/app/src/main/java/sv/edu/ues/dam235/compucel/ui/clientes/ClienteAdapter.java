package sv.edu.ues.dam235.compucel.ui.clientes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import sv.edu.ues.dam235.compucel.R;
import sv.edu.ues.dam235.compucel.datos.local.ClienteEntity;
import sv.edu.ues.dam235.compucel.datos.local.EstadoSync;
import sv.edu.ues.dam235.compucel.util.Texto;

/** Tarjetas de la lista de clientes (CLI-01). */
public class ClienteAdapter extends RecyclerView.Adapter<ClienteAdapter.Fila> {

    public interface AlTocar {
        void cliente(ClienteEntity cliente);
    }

    private final List<ClienteEntity> clientes = new ArrayList<>();
    private final AlTocar alTocar;

    public ClienteAdapter(AlTocar alTocar) {
        this.alTocar = alTocar;
    }

    public void setClientes(List<ClienteEntity> nuevos) {
        clientes.clear();
        if (nuevos != null) {
            clientes.addAll(nuevos);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cliente, parent, false);
        return new Fila(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int posicion) {
        ClienteEntity cliente = clientes.get(posicion);
        fila.tvAvatar.setText(Texto.iniciales(cliente.nombre));
        fila.tvNombre.setText(cliente.nombre);
        String detalle = cliente.telefono;
        if (cliente.correo != null && !cliente.correo.isEmpty()) {
            detalle += " · " + cliente.correo;
        }
        fila.tvDetalle.setText(detalle);
        fila.ivPendiente.setVisibility(EstadoSync.PENDIENTE.equals(cliente.estadoSync) ? View.VISIBLE : View.GONE);
        fila.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alTocar.cliente(cliente);
            }
        });
    }

    @Override
    public int getItemCount() {
        return clientes.size();
    }

    static class Fila extends RecyclerView.ViewHolder {
        final TextView tvAvatar;
        final TextView tvNombre;
        final TextView tvDetalle;
        final ImageView ivPendiente;

        Fila(View vista) {
            super(vista);
            tvAvatar = vista.findViewById(R.id.tvAvatar);
            tvNombre = vista.findViewById(R.id.tvNombre);
            tvDetalle = vista.findViewById(R.id.tvDetalle);
            ivPendiente = vista.findViewById(R.id.ivPendiente);
        }
    }
}
