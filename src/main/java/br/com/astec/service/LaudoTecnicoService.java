package br.com.astec.service;

import br.com.astec.dto.laudo.ItemPecaRequest;
import br.com.astec.dto.laudo.LaudoRequest;
import br.com.astec.dto.laudo.LaudoResponse;
import br.com.astec.event.NotificacaoEvent;
import br.com.astec.exception.ApiException;
import br.com.astec.model.HistoricoStatusTicket;
import br.com.astec.model.ItemPecaSubstituida;
import br.com.astec.model.LaudoTecnico;
import br.com.astec.model.Ticket;
import br.com.astec.model.Usuario;
import br.com.astec.model.enums.PerfilEnum;
import br.com.astec.model.enums.StatusTicketEnum;
import br.com.astec.repository.HistoricoStatusTicketRepository;
import br.com.astec.repository.LaudoTecnicoRepository;
import br.com.astec.repository.TicketRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LaudoTecnicoService {

    private final LaudoTecnicoRepository laudoRepository;
    private final TicketRepository ticketRepository;
    private final HistoricoStatusTicketRepository historicoRepository;
    private final CurrentUserService currentUserService;
    private final ApplicationEventPublisher eventPublisher;

    public LaudoTecnicoService(
            LaudoTecnicoRepository laudoRepository,
            TicketRepository ticketRepository,
            HistoricoStatusTicketRepository historicoRepository,
            CurrentUserService currentUserService,
            ApplicationEventPublisher eventPublisher) {
        this.laudoRepository = laudoRepository;
        this.ticketRepository = ticketRepository;
        this.historicoRepository = historicoRepository;
        this.currentUserService = currentUserService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public LaudoResponse buscar(Long ticketId) {
        Ticket ticket = encontrarTicket(ticketId);
        verificarAcesso(ticket);
        return LaudoResponse.from(laudoRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Laudo técnico não encontrado.")));
    }

    @Transactional
    public LaudoResponse criar(Long ticketId, LaudoRequest request) {
        Ticket ticket = encontrarTicket(ticketId);
        Usuario usuario = currentUserService.usuario();
        verificarAcesso(ticket);
        verificarPermissaoTecnica(ticket, usuario);
        if (ticket.getStatus() != StatusTicketEnum.EM_ATENDIMENTO) {
            throw new ApiException(HttpStatus.CONFLICT, "O laudo só pode ser registrado durante o atendimento.");
        }
        if (laudoRepository.findByTicketId(ticketId).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "O chamado já possui um laudo técnico.");
        }
        LaudoTecnico laudo = new LaudoTecnico(null, request.diagnosticoSolucao().trim(),
                request.horasTrabalhadas(), ticket);
        for (ItemPecaRequest pecaRequest : request.pecasSubstituidas()) {
            ItemPecaSubstituida item = new ItemPecaSubstituida(
                    null, pecaRequest.nomePeca().trim(), pecaRequest.quantidade(),
                    pecaRequest.valorUnitario(), laudo);
            laudo.adicionarPeca(item);
        }
        laudo = laudoRepository.save(laudo);
        StatusTicketEnum anterior = ticket.getStatus();
        ticket.alterarStatus(StatusTicketEnum.RESOLVIDO);
        ticketRepository.save(ticket);
        historicoRepository.save(new HistoricoStatusTicket(
                ticket, usuario, anterior, StatusTicketEnum.RESOLVIDO, "Laudo técnico registrado."));
        eventPublisher.publishEvent(new NotificacaoEvent(
                ticket.getCliente().getEmail(),
                "Chamado " + ticket.getCodigo() + " resolvido",
                "O atendimento foi concluído e o laudo técnico está disponível."));
        return LaudoResponse.from(laudo);
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

    private void verificarPermissaoTecnica(Ticket ticket, Usuario usuario) {
        if (usuario.getPerfil() == PerfilEnum.CLIENTE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Somente técnicos podem registrar laudos.");
        }
        if (usuario.getPerfil() == PerfilEnum.TECNICO
                && (ticket.getTecnicoResponsavel() == null
                || !ticket.getTecnicoResponsavel().getId().equals(usuario.getId()))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "O técnico não está atribuído a este chamado.");
        }
    }
}
