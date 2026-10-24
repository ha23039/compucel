package sv.edu.ues.dam235.compucel.ui.servicios;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import sv.edu.ues.dam235.compucel.R;
import sv.edu.ues.dam235.compucel.datos.local.EstadoSync;
import sv.edu.ues.dam235.compucel.datos.local.ServicioEntity;

public class ServicioAdapter extends RecyclerView.Adapter<ServicioAdapter.Fila> {

    public interface Acciones {
        void editar(ServicioEntity servicio);
        void eliminar(ServicioEntity servicio);
    }

    private final List<ServicioEntity> servicios = new ArrayList<>();
    private final Acciones acciones;

    public ServicioAdapter(Acciones acciones) {
        this.acciones = acciones;
    }

    public void setServicios(List<ServicioEntity> nuevos) {
        servicios.clear();
        if (nuevos != null) servicios.addAll(nuevos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Fila(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_servicio, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int posicion) {
        ServicioEntity servicio = servicios.get(posicion);
        fila.nombre.setText(servicio.nombre);
        fila.descripcion.setText(servicio.descripcion);
        fila.costo.setText(String.format(Locale.getDefault(), "$%.2f", servicio.costo));
        fila.tiempo.setText(formatearTiempo(servicio.tiempoMinutos));
        fila.pendiente.setVisibility(EstadoSync.PENDIENTE.equals(servicio.estadoSync) ? View.VISIBLE : View.GONE);
        fila.editar.setOnClickListener(v -> acciones.editar(servicio));
        fila.eliminar.setOnClickListener(v -> acciones.eliminar(servicio));
    }

    private static String formatearTiempo(int minutos) {
        int horas = minutos / 60;
        int resto = minutos % 60;
        if (horas == 0) return resto + " min";
        if (resto == 0) return horas + " h";
        return horas + " h " + resto + " min";
    }

    @Override
    public int getItemCount() { return servicios.size(); }

    static class Fila extends RecyclerView.ViewHolder {
        final TextView nombre, descripcion, costo, tiempo;
        final ImageView pendiente;
        final MaterialButton editar, eliminar;

        Fila(View view) {
            super(view);
            nombre = view.findViewById(R.id.tvServicioNombre);
            descripcion = view.findViewById(R.id.tvServicioDescripcion);
            costo = view.findViewById(R.id.tvServicioCosto);
            tiempo = view.findViewById(R.id.tvServicioTiempo);
            pendiente = view.findViewById(R.id.ivServicioPendiente);
            editar = view.findViewById(R.id.btServicioEditar);
            eliminar = view.findViewById(R.id.btServicioEliminar);
        }
    }
}