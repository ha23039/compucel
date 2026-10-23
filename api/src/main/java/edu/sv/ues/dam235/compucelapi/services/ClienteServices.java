package edu.sv.ues.dam235.compucelapi.services;

import java.util.List;
import java.util.Optional;

import edu.sv.ues.dam235.compucelapi.dtos.ClienteDTO;

public interface ClienteServices {

    List<ClienteDTO> listar();

    Optional<ClienteDTO> buscar(String id);

    /** Crea o reemplaza el cliente con ese id (como set() en Firestore). */
    ClienteDTO guardar(String id, ClienteDTO cliente);

    void eliminar(String id);
}
