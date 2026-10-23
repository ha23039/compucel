package edu.sv.ues.dam235.compucelapi.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.sv.ues.dam235.compucelapi.dtos.ClienteDTO;
import edu.sv.ues.dam235.compucelapi.services.ClienteServices;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteServices clientes;

    public ClienteController(ClienteServices clientes) {
        this.clientes = clientes;
    }

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> listar() {
        return ResponseEntity.ok(clientes.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> buscar(@PathVariable String id) {
        return clientes.buscar(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // El id lo genera la app (UUID), por eso se usa PUT: repetir el envio no duplica el cliente
    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> guardar(@PathVariable String id, @Valid @RequestBody ClienteDTO cliente) {
        return ResponseEntity.ok(clientes.guardar(id, cliente));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        clientes.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
