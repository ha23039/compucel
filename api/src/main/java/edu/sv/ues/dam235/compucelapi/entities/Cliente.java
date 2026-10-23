package edu.sv.ues.dam235.compucelapi.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

// Mismos campos que la tabla clientes de la app; el id es el UUID que genera el telefono
@Data
@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 9)
    private String telefono;

    @Column(length = 120)
    private String correo;

    @Column(length = 200)
    private String direccion;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_por", length = 120)
    private String creadoPor;

    @Column(name = "actualizado_en")
    private Long actualizadoEn;
}
