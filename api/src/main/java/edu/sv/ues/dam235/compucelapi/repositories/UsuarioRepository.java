package edu.sv.ues.dam235.compucelapi.repositories;

import edu.sv.ues.dam235.compucelapi.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {
}
