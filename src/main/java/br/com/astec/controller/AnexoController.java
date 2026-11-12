package br.com.astec.controller;

import br.com.astec.dto.anexo.AnexoResponse;
import br.com.astec.service.AnexoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/anexos")
@SecurityRequirement(name = "bearerAuth")
public class AnexoController {

    private final AnexoService anexoService;

    public AnexoController(AnexoService anexoService) {
        this.anexoService = anexoService;
    }

    @GetMapping
    public List<AnexoResponse> listar(@PathVariable Long ticketId) {
        return anexoService.listar(ticketId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnexoResponse> enviar(
            @PathVariable Long ticketId,
            @RequestPart("arquivo") MultipartFile arquivo) {
        return ResponseEntity.status(201).body(anexoService.salvar(ticketId, arquivo));
    }

    @GetMapping("/{anexoId}")
    public ResponseEntity<Resource> baixar(@PathVariable Long ticketId, @PathVariable Long anexoId) {
        AnexoService.DownloadAnexo download = anexoService.baixar(ticketId, anexoId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.tipoConteudo()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(download.nome(), StandardCharsets.UTF_8).build().toString())
                .body(download.recurso());
    }

    @DeleteMapping("/{anexoId}")
    public ResponseEntity<Void> remover(@PathVariable Long ticketId, @PathVariable Long anexoId) {
        anexoService.remover(ticketId, anexoId);
        return ResponseEntity.noContent().build();
    }
}
