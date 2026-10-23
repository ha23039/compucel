package edu.sv.ues.dam235.compucelapi.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

// Mismos campos y reglas que el formulario de la app (CLI-03)
@Data
public class ClienteDTO {
    private String id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotNull(message = "El telefono es obligatorio")
    @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "Formato valido: 0000-0000")
    private String telefono;

    private String correo;
    private String direccion;
    private Boolean activo = true;
    private String creadoPor;
    private Long actualizadoEn;
}
