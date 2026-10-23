package edu.sv.ues.dam235.compucelapi.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

// Mismos campos que la tabla usuarios de la app (Room y Firestore)
@Data
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @Column(length = 120)
    private String correo;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(name = "clave_hash", nullable = false, length = 100)
    private String claveHash;

    @Column(name = "actualizado_en")
    private Long actualizadoEn;
}
