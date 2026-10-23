package edu.sv.ues.dam235.compucelapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class InfoControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void infoResponde200SinToken() throws Exception {
        mockMvc.perform(get("/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("CompuCel API"))
                .andExpect(jsonPath("$.version").value("0.1.0"));
    }

    @Test
    void usuariosRequiereAutenticacion() throws Exception {
        int codigo = mockMvc.perform(get("/usuarios")).andReturn().getResponse().getStatus();
        assertTrue(codigo == 401 || codigo == 403, "codigo recibido: " + codigo);
    }

    @Test
    void passwordEncoderEsBcrypt() {
        String hash = passwordEncoder.encode("abcd1234");
        assertTrue(hash.startsWith("$2a$10$"), hash);
        assertTrue(passwordEncoder.matches("abcd1234", hash));
    }
}
