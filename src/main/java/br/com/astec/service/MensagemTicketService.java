package br.com.astec.service;

import br.com.astec.dto.mensagem.MensagemRequest;
import br.com.astec.dto.mensagem.MensagemResponse;
import br.com.astec.event.NotificacaoEvent;
import br.com.astec.exception.ApiException;
import br.com.astec.model.MensagemTicket;
import br.com.astec.model.Ticket;
import br.com.astec.model.Usuario;
import br.com.astec.model.enums.PerfilEnum;
import br.com.astec.model.enums.StatusTicketEnum;
import br.com.astec.repository.MensagemTicketRepository;
import br.com.astec.repository.TicketRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MensagemTicketService {

    private final MensagemTicketRepository mensagemRepository;
    private final TicketRepository ticketRepository;
    private final CurrentUserService currentUserService;
    private final ApplicationEventPublisher eventPublisher;

    public MensagemTicketService(
            MensagemTicketRepository mensagemRepository,
            TicketRepository ticketRepository,
            CurrentUserService currentUserService,
            ApplicationEventPublisher eventPublisher) {
        this.mensagemRepository = mensagemRepository;
        this.ticketRepository = ticketRepository;
        this.currentUserService = currentUserService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<MensagemResponse> listar(Long ticketId) {
        Ticket ticket = encontrarTicket(ticketId);
        verificarAcesso(ticket);
        return mensagemRepository.findByTicketIdOrderByDataEnvioAsc(ticketId).stream()
                .map(MensagemResponse::from)
                .toList();
    }

    @Transactional
    public MensagemResponse criar(Long ticketId, MensagemRequest request) {
        Ticket ticket = encontrarTicket(ticketId);
        Usuario autor = currentUserService.usuario();
        verificarAcesso(ticket);
        if (ticket.getStatus() == StatusTicketEnum.FECHADO) {
            throw new ApiException(HttpStatus.CONFLICT, "Não é possível enviar mensagens em um chamado fechado.");
        }
        MensagemTicket mensagem = new MensagemTicket(null, request.texto().trim(), ticket, autor);
        mensagem = mensagemRepository.save(mensagem);
        if (autor.getPerfil() != PerfilEnum.CLIENTE && ticket.getDataPrimeiraResposta() == null) {
            ticket.setDataPrimeiraResposta(LocalDateTime.now());
            ticketRepository.save(ticket);
        }
        Usuario destinatario = autor.getPerfil() == PerfilEnum.CLIENTE
                ? ticket.getTecnicoResponsavel()
                : ticket.getCliente();
        if (destinatario != null) {
            eventPublisher.publishEvent(new NotificacaoEvent(
                    destinatario.getEmail(),
                    "Nova mensagem no chamado " + ticket.getCodigo(),
                    "O usuário " + autor.getNome() + " enviou uma nova mensagem no chamado."));
        }
        return MensagemResponse.from(mensagem);
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
