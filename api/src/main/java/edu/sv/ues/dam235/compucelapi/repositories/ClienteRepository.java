package edu.sv.ues.dam235.compucelapi.repositories;

import edu.sv.ues.dam235.compucelapi.entities.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, String> {
}
