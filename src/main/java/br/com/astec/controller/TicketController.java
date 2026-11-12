package br.com.astec.controller;

import br.com.astec.dto.PageResponse;
import br.com.astec.dto.mensagem.MensagemRequest;
import br.com.astec.dto.mensagem.MensagemResponse;
import br.com.astec.dto.ticket.HistoricoStatusResponse;
import br.com.astec.dto.ticket.TicketAtribuicaoRequest;
import br.com.astec.dto.ticket.TicketRequest;
import br.com.astec.dto.ticket.TicketResponse;
import br.com.astec.dto.ticket.TicketStatusRequest;
import br.com.astec.exception.ApiException;
import br.com.astec.model.enums.StatusTicketEnum;
import br.com.astec.model.enums.UrgenciaEnum;
import br.com.astec.repository.dto.TicketContagemPorEquipamentoDTO;
import br.com.astec.repository.dto.TicketContagemPorStatusDTO;
import br.com.astec.service.MensagemTicketService;
import br.com.astec.service.TicketService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/tickets")
@SecurityRequirement(name = "bearerAuth")
public class TicketController {

    private final TicketService ticketService;
    private final MensagemTicketService mensagemService;

    public TicketController(TicketService ticketService, MensagemTicketService mensagemService) {
        this.ticketService = ticketService;
        this.mensagemService = mensagemService;
    }

    @GetMapping
    public PageResponse<TicketResponse> listarTickets(
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) Long tecnicoId,
            @RequestParam(required = false) Long equipamentoId,
            @RequestParam(required = false) StatusTicketEnum status,
            @RequestParam(required = false) UrgenciaEnum urgencia,
            @RequestParam(required = false) LocalDate dataInicio,
            @RequestParam(required = false) LocalDate dataFim,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        validarPaginacao(pagina, tamanho);
        return ticketService.listar(
                clienteId, tecnicoId, equipamentoId, status, urgencia, dataInicio, dataFim, pagina, tamanho);
    }

    @GetMapping("/{id}")
    public TicketResponse buscarPorId(@PathVariable Long id) {
        return ticketService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<TicketResponse> criarTicket(@Valid @RequestBody TicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.criar(request));
    }

    @PatchMapping("/{id}/status")
    public TicketResponse atualizarStatus(
            @PathVariable Long id,
            @Valid @RequestBody TicketStatusRequest request) {
        return ticketService.atualizarStatus(id, request.status(), request.observacao());
    }

    @PatchMapping("/{id}/atribuicao")
    public TicketResponse atribuir(
            @PathVariable Long id,
            @Valid @RequestBody TicketAtribuicaoRequest request) {
        return ticketService.atribuir(id, request.tecnicoId());
    }

    @GetMapping("/{id}/historico")
    public List<HistoricoStatusResponse> historico(@PathVariable Long id) {
        return ticketService.historico(id);
    }

    @GetMapping("/{id}/mensagens")
    public List<MensagemResponse> listarMensagens(@PathVariable Long id) {
        return mensagemService.listar(id);
    }

    @PostMapping("/{id}/mensagens")
    public ResponseEntity<MensagemResponse> criarMensagem(
            @PathVariable Long id,
            @Valid @RequestBody MensagemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mensagemService.criar(id, request));
    }

    @GetMapping("/estatisticas/sla")
    public TicketService.SlaResumo consultarSla() {
        return ticketService.consultarSla();
    }

    @GetMapping("/estatisticas/equipamentos")
    public List<TicketContagemPorEquipamentoDTO> contarPorEquipamento() {
        return ticketService.contarPorEquipamento();
    }

    @GetMapping("/estatisticas/status")
    public List<TicketContagemPorStatusDTO> contarPorStatus() {
        return ticketService.contarPorStatus();
    }

    private void validarPaginacao(int pagina, int tamanho) {
        if (pagina < 0 || tamanho < 1 || tamanho > 100) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST, "A página deve ser não negativa e o tamanho deve estar entre 1 e 100.");
        }
    }
}
