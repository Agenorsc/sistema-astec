package br.com.astec.controller;

import br.com.astec.dto.PageResponse;
import br.com.astec.dto.usuario.UsuarioRequest;
import br.com.astec.dto.usuario.UsuarioResponse;
import br.com.astec.model.enums.PerfilEnum;
import br.com.astec.service.UsuarioService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public PageResponse<UsuarioResponse> listar(
            @RequestParam(required = false) PerfilEnum perfil,
            @RequestParam(required = false) String nome,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        validarPaginacao(pagina, tamanho);
        return usuarioService.listar(perfil, nome, pagina, tamanho);
    }

    @GetMapping("/{id}")
    public UsuarioResponse buscar(@PathVariable Long id) {
        return usuarioService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> cadastrar(@Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.criar(request));
    }

    @PatchMapping("/{id}/perfil")
    public UsuarioResponse atualizarPerfil(@PathVariable Long id, @Valid @RequestBody PerfilRequest request) {
        return usuarioService.atualizarPerfil(id, request.perfil());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        usuarioService.remover(id);
        return ResponseEntity.noContent().build();
    }

    private void validarPaginacao(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 100) {
            throw new br.com.astec.exception.ApiException(
                    HttpStatus.BAD_REQUEST, "A página deve ser não negativa e o tamanho deve estar entre 1 e 100.");
        }
    }

    public record PerfilRequest(@jakarta.validation.constraints.NotNull PerfilEnum perfil) {
    }
}
