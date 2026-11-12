package br.com.astec.controller;

import br.com.astec.dto.PageResponse;
import br.com.astec.dto.equipamento.EquipamentoRequest;
import br.com.astec.dto.equipamento.EquipamentoResponse;
import br.com.astec.exception.ApiException;
import br.com.astec.service.EquipamentoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/equipamentos")
@SecurityRequirement(name = "bearerAuth")
public class EquipamentoController {

    private final EquipamentoService equipamentoService;

    public EquipamentoController(EquipamentoService equipamentoService) {
        this.equipamentoService = equipamentoService;
    }

    @GetMapping
    public PageResponse<EquipamentoResponse> listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        validarPaginacao(pagina, tamanho);
        return equipamentoService.listar(pagina, tamanho);
    }

    @GetMapping("/{id}")
    public EquipamentoResponse buscar(@PathVariable Long id) {
        return equipamentoService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<EquipamentoResponse> cadastrar(@Valid @RequestBody EquipamentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipamentoService.criar(request));
    }

    @PutMapping("/{id}")
    public EquipamentoResponse atualizar(@PathVariable Long id, @Valid @RequestBody EquipamentoRequest request) {
        return equipamentoService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        equipamentoService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private void validarPaginacao(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 100) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A página deve ser não negativa e o tamanho deve estar entre 1 e 100.");
        }
    }
}
