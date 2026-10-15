package br.com.astec.controller;

import br.com.astec.model.Ticket;
import br.com.astec.model.enums.StatusTicketEnum;
import br.com.astec.repository.TicketRepository;
import br.com.astec.repository.dto.TicketContagemPorEquipamentoDTO;
import br.com.astec.repository.dto.TicketContagemPorStatusDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired
    private TicketRepository ticketRepository;

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

    @GetMapping("/{id}")
    public ResponseEntity<Ticket> buscarPorId(@PathVariable Long id) {
        return ticketRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Ticket> criarTicket(@RequestBody Ticket ticket) {
        Ticket criado = ticketRepository.save(ticket);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Ticket> atualizarStatus(@PathVariable Long id, @RequestParam StatusTicketEnum status) {
        return ticketRepository.findById(id).map(ticket -> {
            ticket.alterarStatus(status);
            ticketRepository.save(ticket);
            return ResponseEntity.ok(ticket);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/estatisticas/equipamentos")
    public List<TicketContagemPorEquipamentoDTO> contarPorEquipamento() {
        return ticketRepository.contarTicketsPorEquipamento();
    }

    @GetMapping("/estatisticas/status")
    public List<TicketContagemPorStatusDTO> contarPorStatus() {
        return ticketRepository.contarTicketsPorStatus();
    }
}
