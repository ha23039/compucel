package edu.sv.ues.dam235.compucelapi.implementations;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import edu.sv.ues.dam235.compucelapi.dtos.ClienteDTO;
import edu.sv.ues.dam235.compucelapi.entities.Cliente;
import edu.sv.ues.dam235.compucelapi.repositories.ClienteRepository;
import edu.sv.ues.dam235.compucelapi.services.ClienteServices;

@Service
public class ClienteServicesImpl implements ClienteServices {

    private final ClienteRepository repository;

    public ClienteServicesImpl(ClienteRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<ClienteDTO> listar() {
        return repository.findAll().stream().map(ClienteServicesImpl::aDto).toList();
    }

    @Override
    public Optional<ClienteDTO> buscar(String id) {
        return repository.findById(id).map(ClienteServicesImpl::aDto);
    }

    @Override
    public ClienteDTO guardar(String id, ClienteDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNombre(dto.getNombre().trim());
        cliente.setTelefono(dto.getTelefono());
        cliente.setCorreo(dto.getCorreo());
        cliente.setDireccion(dto.getDireccion());
        cliente.setActivo(dto.getActivo() == null || dto.getActivo());
        cliente.setCreadoPor(dto.getCreadoPor());
        cliente.setActualizadoEn(dto.getActualizadoEn() == null ? System.currentTimeMillis() : dto.getActualizadoEn());
        return aDto(repository.save(cliente));
    }

    @Override
    public void eliminar(String id) {
        repository.deleteById(id);
    }

    private static ClienteDTO aDto(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getId());
        dto.setNombre(cliente.getNombre());
        dto.setTelefono(cliente.getTelefono());
        dto.setCorreo(cliente.getCorreo());
        dto.setDireccion(cliente.getDireccion());
        dto.setActivo(cliente.getActivo());
        dto.setCreadoPor(cliente.getCreadoPor());
        dto.setActualizadoEn(cliente.getActualizadoEn());
        return dto;
    }
}
