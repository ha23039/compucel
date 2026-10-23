package edu.sv.ues.dam235.compucelapi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import edu.sv.ues.dam235.compucelapi.repositories.ClienteRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ClienteControllerTests {

    private static final String ID = "3f1c2a4e-8b7d-4c1a-9e2f-0a1b2c3d4e5f";
    private static final String CLIENTE = """
            {"nombre":"Maria Elena Flores","telefono":"7654-3210","correo":"mflores@correo.test",
             "direccion":"Col. Santa Lucia, Santa Ana","activo":true,"creadoPor":"demo.taller@compucel.test",
             "actualizadoEn":1792771200000}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepository clientes;

    @BeforeEach
    void limpiar() {
        clientes.deleteAll();
    }

    @Test
    void putCreaYGetDevuelve() throws Exception {
        mockMvc.perform(put("/clientes/" + ID).contentType(MediaType.APPLICATION_JSON).content(CLIENTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID));
        mockMvc.perform(get("/clientes/" + ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Maria Elena Flores"))
                .andExpect(jsonPath("$.telefono").value("7654-3210"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void putDosVecesNoDuplica() throws Exception {
        mockMvc.perform(put("/clientes/" + ID).contentType(MediaType.APPLICATION_JSON).content(CLIENTE));
        mockMvc.perform(put("/clientes/" + ID).contentType(MediaType.APPLICATION_JSON)
                .content(CLIENTE.replace("Col. Santa Lucia", "Barrio El Calvario")));
        mockMvc.perform(get("/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].direccion").value("Barrio El Calvario, Santa Ana"));
    }

    @Test
    void telefonoInvalidoDa400() throws Exception {
        mockMvc.perform(put("/clientes/" + ID).contentType(MediaType.APPLICATION_JSON)
                        .content(CLIENTE.replace("7654-3210", "76543210")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void nombreVacioDa400() throws Exception {
        mockMvc.perform(put("/clientes/" + ID).contentType(MediaType.APPLICATION_JSON)
                        .content(CLIENTE.replace("Maria Elena Flores", "")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void clienteInexistenteDa404() throws Exception {
        mockMvc.perform(get("/clientes/no-existe")).andExpect(status().isNotFound());
    }

    @Test
    void deleteBorra() throws Exception {
        mockMvc.perform(put("/clientes/" + ID).contentType(MediaType.APPLICATION_JSON).content(CLIENTE));
        mockMvc.perform(delete("/clientes/" + ID)).andExpect(status().isNoContent());
        mockMvc.perform(get("/clientes/" + ID)).andExpect(status().isNotFound());
    }
}
