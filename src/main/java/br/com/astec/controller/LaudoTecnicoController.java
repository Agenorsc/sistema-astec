package br.com.astec.controller;

import br.com.astec.dto.laudo.LaudoRequest;
import br.com.astec.dto.laudo.LaudoResponse;
import br.com.astec.service.LaudoTecnicoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets/{ticketId}/laudo")
@SecurityRequirement(name = "bearerAuth")
public class LaudoTecnicoController {

    private final LaudoTecnicoService laudoService;

    public LaudoTecnicoController(LaudoTecnicoService laudoService) {
        this.laudoService = laudoService;
    }

    @GetMapping
    public LaudoResponse buscar(@PathVariable Long ticketId) {
        return laudoService.buscar(ticketId);
    }

    @PostMapping
    public ResponseEntity<LaudoResponse> criar(
            @PathVariable Long ticketId,
            @Valid @RequestBody LaudoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(laudoService.criar(ticketId, request));
    }
}
