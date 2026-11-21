package br.com.astec.controller;

import br.com.astec.model.Equipamento;
import br.com.astec.repository.EquipamentoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipamentos")
@Tag(name = "Equipamentos", description = "Endpoints RESTful para gestão de equipamentos e maquinários cadastrados")
public class EquipamentoController {

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @Operation(summary = "Listar todos os equipamentos")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GetMapping
    public List<Equipamento> listarTodos() {
        return equipamentoRepository.findAll();
    }

    @Operation(summary = "Buscar equipamento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Equipamento encontrado"),
            @ApiResponse(responseCode = "404", description = "Equipamento não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Equipamento> buscarPorId(@PathVariable Long id) {
        return equipamentoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Cadastrar novo equipamento")
    @ApiResponse(responseCode = "201", description = "Equipamento criado com sucesso")
    @PostMapping
    public ResponseEntity<Equipamento> cadastrar(@RequestBody Equipamento equipamento) {
        Equipamento salvo = equipamentoRepository.save(equipamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @Operation(summary = "Remover equipamento")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Equipamento removido"),
            @ApiResponse(responseCode = "404", description = "Equipamento não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        if (!equipamentoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        equipamentoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
