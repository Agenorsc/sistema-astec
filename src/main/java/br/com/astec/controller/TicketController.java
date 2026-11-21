package br.com.astec.controller;

import br.com.astec.model.Ticket;
import br.com.astec.model.enums.StatusTicketEnum;
import br.com.astec.repository.TicketRepository;
import br.com.astec.repository.dto.TicketContagemPorEquipamentoDTO;
import br.com.astec.repository.dto.TicketContagemPorStatusDTO;
import br.com.astec.repository.projection.TicketResumoProjecao;
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
@RequestMapping("/api/tickets")
@Tag(name = "Tickets", description = "Endpoints RESTful para gestão do ciclo de vida dos chamados técnicos")
public class TicketController {

    @Autowired
    private TicketRepository ticketRepository;

    @Operation(summary = "Listar chamados com filtros opcionais")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de chamados recuperada com sucesso")
    })
    @GetMapping
    public List<Ticket> listarTickets(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long tecnicoId,
            @RequestParam(required = false) StatusTicketEnum status) {

        if (clienteId != null) {
            return ticketRepository.findByClienteId(clienteId);
        }
        if (tecnicoId != null) {
            return ticketRepository.findByTecnicoResponsavelId(tecnicoId);
        }
        if (status != null) {
            return ticketRepository.findByStatus(status);
        }
        return ticketRepository.findAll();
    }

    @Operation(summary = "Buscar chamado por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chamado localizado"),
            @ApiResponse(responseCode = "404", description = "Chamado não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Ticket> buscarPorId(@PathVariable Long id) {
        return ticketRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Criar novo chamado técnico")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Chamado criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos")
    })
    @PostMapping
    public ResponseEntity<Ticket> criarTicket(@RequestBody Ticket ticket) {
        Ticket criado = ticketRepository.save(ticket);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @Operation(summary = "Atualizar status do chamado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Chamado não encontrado")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<Ticket> atualizarStatus(@PathVariable Long id, @RequestParam StatusTicketEnum status) {
        return ticketRepository.findById(id).map(ticket -> {
            ticket.alterarStatus(status);
            ticketRepository.save(ticket);
            return ResponseEntity.ok(ticket);
        }).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Remover chamado")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Chamado removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Chamado não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirTicket(@PathVariable Long id) {
        if (!ticketRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ticketRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Projeção resumida de chamados por status")
    @GetMapping("/projecao")
    public List<TicketResumoProjecao> buscarProjecao(@RequestParam StatusTicketEnum status) {
        return ticketRepository.findProjecaoByStatus(status);
    }

    @Operation(summary = "Estatísticas agregadas de chamados por equipamento (JPQL DTO)")
    @GetMapping("/estatisticas/equipamentos")
    public List<TicketContagemPorEquipamentoDTO> contarPorEquipamento() {
        return ticketRepository.contarTicketsPorEquipamento();
    }

    @Operation(summary = "Estatísticas agregadas de chamados por status (JPQL DTO)")
    @GetMapping("/estatisticas/status")
    public List<TicketContagemPorStatusDTO> contarPorStatus() {
        return ticketRepository.contarTicketsPorStatus();
    }
}
