package br.com.astec.service;

import br.com.astec.dto.avaliacao.AvaliacaoRequest;
import br.com.astec.dto.avaliacao.AvaliacaoResponse;
import br.com.astec.exception.ApiException;
import br.com.astec.model.Avaliacao;
import br.com.astec.model.Ticket;
import br.com.astec.model.Usuario;
import br.com.astec.model.enums.PerfilEnum;
import br.com.astec.model.enums.StatusTicketEnum;
import br.com.astec.repository.AvaliacaoRepository;
import br.com.astec.repository.TicketRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final TicketRepository ticketRepository;
    private final CurrentUserService currentUserService;

    public AvaliacaoService(
            AvaliacaoRepository avaliacaoRepository,
            TicketRepository ticketRepository,
            CurrentUserService currentUserService) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.ticketRepository = ticketRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public AvaliacaoResponse buscar(Long ticketId) {
        Ticket ticket = encontrarTicket(ticketId);
        verificarAcesso(ticket);
        return AvaliacaoResponse.from(avaliacaoRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Avaliação não encontrada.")));
    }

    @Transactional
    public AvaliacaoResponse criar(Long ticketId, AvaliacaoRequest request) {
        Ticket ticket = encontrarTicket(ticketId);
        Usuario usuario = currentUserService.usuario();
        verificarAcesso(ticket);
        if (usuario.getPerfil() != PerfilEnum.CLIENTE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Somente clientes podem avaliar o atendimento.");
        }
        if (ticket.getStatus() != StatusTicketEnum.RESOLVIDO && ticket.getStatus() != StatusTicketEnum.FECHADO) {
            throw new ApiException(HttpStatus.CONFLICT, "O chamado deve estar resolvido antes de ser avaliado.");
        }
        if (avaliacaoRepository.findByTicketId(ticketId).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "O chamado já possui uma avaliação.");
        }
        Avaliacao avaliacao = new Avaliacao(null, request.nota(), request.comentario(), ticket);
        return AvaliacaoResponse.from(avaliacaoRepository.save(avaliacao));
    }

    private Ticket encontrarTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Chamado não encontrado."));
    }

    private void verificarAcesso(Ticket ticket) {
        Usuario usuario = currentUserService.usuario();
        if (usuario.getPerfil() == PerfilEnum.CLIENTE && !ticket.getCliente().getId().equals(usuario.getId())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Chamado não encontrado.");
        }
    }
}
