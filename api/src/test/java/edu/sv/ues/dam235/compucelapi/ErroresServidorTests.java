package edu.sv.ues.dam235.compucelapi;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Con servidor real: los errores de validacion pasan por /error y no deben volverse 403
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ErroresServidorTests {

    @LocalServerPort
    private int puerto;

    @Test
    void clienteInvalidoDa400ConServidorReal() throws Exception {
        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + puerto + "/api/v1/compucel/clientes/prueba-2"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString("{\"nombre\":\"X\",\"telefono\":\"123\"}"))
                .build();
        HttpResponse<String> respuesta = HttpClient.newHttpClient().send(peticion, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, respuesta.statusCode());
    }
}
