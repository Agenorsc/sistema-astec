package br.com.astec.controller;

import br.com.astec.dto.avaliacao.AvaliacaoRequest;
import br.com.astec.dto.avaliacao.AvaliacaoResponse;
import br.com.astec.service.AvaliacaoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets/{ticketId}/avaliacao")
@SecurityRequirement(name = "bearerAuth")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @GetMapping
    public AvaliacaoResponse buscar(@PathVariable Long ticketId) {
        return avaliacaoService.buscar(ticketId);
    }

    @PostMapping
    public ResponseEntity<AvaliacaoResponse> criar(
            @PathVariable Long ticketId,
            @Valid @RequestBody AvaliacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(avaliacaoService.criar(ticketId, request));
    }
}
