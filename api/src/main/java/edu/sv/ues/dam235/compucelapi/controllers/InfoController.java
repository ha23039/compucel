package edu.sv.ues.dam235.compucelapi.controllers;

import edu.sv.ues.dam235.compucelapi.dtos.InfoDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfoController {

    @Value("${app.version}")
    private String version;

    // Permite comprobar que la API esta arriba sin necesidad de token
    @GetMapping("/info")
    public ResponseEntity<InfoDTO> info() {
        return ResponseEntity.ok(new InfoDTO("CompuCel API", version));
    }
}
