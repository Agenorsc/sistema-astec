package br.com.astec.service;

import br.com.astec.dto.PageResponse;
import br.com.astec.dto.ticket.HistoricoStatusResponse;
import br.com.astec.dto.ticket.TicketResponse;
import br.com.astec.event.NotificacaoEvent;
import br.com.astec.exception.ApiException;
import br.com.astec.model.Equipamento;
import br.com.astec.model.HistoricoStatusTicket;
import br.com.astec.model.Ticket;
import br.com.astec.model.Usuario;
import br.com.astec.model.enums.PerfilEnum;
import br.com.astec.model.enums.StatusTicketEnum;
import br.com.astec.model.enums.UrgenciaEnum;
import br.com.astec.dto.ticket.TicketRequest;
import br.com.astec.repository.EquipamentoRepository;
import br.com.astec.repository.HistoricoStatusTicketRepository;
import br.com.astec.repository.TicketRepository;
import br.com.astec.repository.UsuarioRepository;
import br.com.astec.repository.dto.TicketContagemPorEquipamentoDTO;
import br.com.astec.repository.dto.TicketContagemPorStatusDTO;
import jakarta.persistence.criteria.Predicate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final EquipamentoRepository equipamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistoricoStatusTicketRepository historicoRepository;
    private final CurrentUserService currentUserService;
    private final SlaPolicy slaPolicy;
    private final ApplicationEventPublisher eventPublisher;

    public TicketService(
            TicketRepository ticketRepository,
            EquipamentoRepository equipamentoRepository,
            UsuarioRepository usuarioRepository,
            HistoricoStatusTicketRepository historicoRepository,
            CurrentUserService currentUserService,
            SlaPolicy slaPolicy,
            ApplicationEventPublisher eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.equipamentoRepository = equipamentoRepository;
        this.usuarioRepository = usuarioRepository;
        this.historicoRepository = historicoRepository;
        this.currentUserService = currentUserService;
        this.slaPolicy = slaPolicy;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> listar(
            Long clienteId,
            Long tecnicoId,
            Long equipamentoId,
            StatusTicketEnum status,
            UrgenciaEnum urgencia,
            LocalDate dataInicio,
            LocalDate dataFim,
            int pagina,
            int tamanho) {
        Usuario usuario = currentUserService.usuario();
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "A data inicial não pode ser posterior à data final.");
        }
        if (usuario.getPerfil() == PerfilEnum.CLIENTE && clienteId != null
                && !clienteId.equals(usuario.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Não é permitido consultar chamados de outro cliente.");
        }
        Long filtroCliente = usuario.getPerfil() == PerfilEnum.CLIENTE ? usuario.getId() : clienteId;
        var resultado = ticketRepository.findAll((root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filtroCliente != null) {
                predicates.add(builder.equal(root.get("cliente").get("id"), filtroCliente));
            }
            if (tecnicoId != null) {
                predicates.add(builder.equal(root.get("tecnicoResponsavel").get("id"), tecnicoId));
            }
            if (equipamentoId != null) {
                predicates.add(builder.equal(root.get("equipamento").get("id"), equipamentoId));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            if (urgencia != null) {
                predicates.add(builder.equal(root.get("urgencia"), urgencia));
            }
            if (dataInicio != null) {
                predicates.add(builder.greaterThanOrEqualTo(
                        root.get("dataCriacao"), dataInicio.atStartOfDay()));
            }
            if (dataFim != null) {
                predicates.add(builder.lessThan(
                        root.get("dataCriacao"), dataFim.plusDays(1).atStartOfDay()));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        }, PageRequest.of(pagina, tamanho, Sort.by(Sort.Direction.DESC, "dataCriacao")));
        return PageResponse.from(resultado, TicketResponse::from);
    }

    @Transactional(readOnly = true)
    public TicketResponse buscar(Long id) {
        Ticket ticket = encontrar(id);
        verificarAcesso(ticket);
        return TicketResponse.from(ticket);
    }

    @Transactional
    public TicketResponse criar(TicketRequest request) {
        Usuario cliente = currentUserService.usuario();
        if (cliente.getPerfil() != PerfilEnum.CLIENTE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Somente clientes podem abrir chamados.");
        }
        Equipamento equipamento = equipamentoRepository.findById(request.equipamentoId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Equipamento não encontrado."));
        if (equipamento.getCliente() == null || !equipamento.getCliente().getId().equals(cliente.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "O equipamento não pertence ao cliente autenticado.");
        }
        LocalDateTime agora = LocalDateTime.now();
        Ticket ticket = new Ticket(null, gerarCodigo(), request.titulo().trim(), request.descricao().trim(),
                request.tipoAtendimento(), request.urgencia(), StatusTicketEnum.ABERTO, cliente, equipamento);
        ticket.setPrazoPrimeiraResposta(slaPolicy.prazoPrimeiraResposta(request.urgencia(), agora));
        ticket.setPrazoResolucao(slaPolicy.prazoResolucao(request.urgencia(), agora));
        ticket = ticketRepository.save(ticket);
        historicoRepository.save(new HistoricoStatusTicket(
                ticket, cliente, null, StatusTicketEnum.ABERTO, "Chamado criado."));
        return TicketResponse.from(ticket);
    }

    @Transactional
    public TicketResponse atualizarStatus(Long id, StatusTicketEnum novoStatus, String observacao) {
        Ticket ticket = encontrar(id);
        verificarAcesso(ticket);
        Usuario usuario = currentUserService.usuario();
        if (usuario.getPerfil() == PerfilEnum.CLIENTE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Clientes não podem alterar o status do chamado.");
        }
        StatusTicketEnum anterior = ticket.getStatus();
        validarTransicao(anterior, novoStatus);
        ticket.alterarStatus(novoStatus);
        ticketRepository.save(ticket);
        historicoRepository.save(new HistoricoStatusTicket(ticket, usuario, anterior, novoStatus, observacao));
        eventPublisher.publishEvent(new NotificacaoEvent(
                ticket.getCliente().getEmail(),
                "Atualização do chamado " + ticket.getCodigo(),
                "O status do chamado foi alterado de " + anterior + " para " + novoStatus + "."));
        return TicketResponse.from(ticket);
    }

    @Transactional
    public TicketResponse atribuir(Long id, Long tecnicoId) {
        Ticket ticket = encontrar(id);
        verificarAcesso(ticket);
        if (ticket.getStatus() == StatusTicketEnum.FECHADO) {
            throw new ApiException(HttpStatus.CONFLICT, "Não é possível atribuir um chamado fechado.");
        }
        Usuario usuario = currentUserService.usuario();
        if (usuario.getPerfil() == PerfilEnum.CLIENTE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Clientes não podem atribuir chamados.");
        }
        Usuario tecnico = usuarioRepository.findById(tecnicoId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Técnico não encontrado."));
        if (tecnico.getPerfil() != PerfilEnum.TECNICO) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "O responsável deve ter perfil de técnico.");
        }
        if (usuario.getPerfil() == PerfilEnum.TECNICO && !usuario.getId().equals(tecnico.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Técnicos só podem assumir chamados para si mesmos.");
        }
        Usuario tecnicoAnterior = ticket.getTecnicoResponsavel();
        ticket.atribuirTecnico(tecnico);
        ticketRepository.save(ticket);
        if (ticket.getStatus() != StatusTicketEnum.ABERTO && ticket.getStatus() != StatusTicketEnum.EM_ANALISE) {
            historicoRepository.save(new HistoricoStatusTicket(
                    ticket, usuario, ticket.getStatus(), ticket.getStatus(), "Chamado atribuído ao técnico " + tecnico.getNome() + "."));
        } else if (tecnicoAnterior == null && ticket.getStatus() == StatusTicketEnum.EM_ANALISE) {
            historicoRepository.save(new HistoricoStatusTicket(
                    ticket, usuario, StatusTicketEnum.ABERTO, StatusTicketEnum.EM_ANALISE,
                    "Chamado atribuído ao técnico " + tecnico.getNome() + "."));
        }
        eventPublisher.publishEvent(new NotificacaoEvent(
                ticket.getCliente().getEmail(),
                "Técnico atribuído ao chamado " + ticket.getCodigo(),
                "O chamado foi atribuído ao técnico " + tecnico.getNome() + "."));
        return TicketResponse.from(ticket);
    }

    @Transactional(readOnly = true)
    public List<HistoricoStatusResponse> historico(Long id) {
        Ticket ticket = encontrar(id);
        verificarAcesso(ticket);
        return historicoRepository.findByTicketIdOrderByDataAlteracaoAsc(ticket.getId()).stream()
                .map(HistoricoStatusResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SlaResumo consultarSla() {
        LocalDateTime agora = LocalDateTime.now();
        List<StatusTicketEnum> estadosEncerrados = List.of(StatusTicketEnum.RESOLVIDO, StatusTicketEnum.FECHADO);
        return new SlaResumo(
                ticketRepository.findByPrazoPrimeiraRespostaBeforeAndDataPrimeiraRespostaIsNullAndStatusNotIn(
                        agora, estadosEncerrados).size(),
                ticketRepository.findByPrazoResolucaoBeforeAndDataResolucaoIsNullAndStatusNotIn(
                        agora, List.of(StatusTicketEnum.FECHADO)).size());
    }

    @Transactional(readOnly = true)
    public List<TicketContagemPorEquipamentoDTO> contarPorEquipamento() {
        return ticketRepository.contarTicketsPorEquipamento();
    }

    @Transactional(readOnly = true)
    public List<TicketContagemPorStatusDTO> contarPorStatus() {
        return ticketRepository.contarTicketsPorStatus();
    }

    private Ticket encontrar(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Chamado não encontrado."));
    }

    private void verificarAcesso(Ticket ticket) {
        Usuario usuario = currentUserService.usuario();
        if (usuario.getPerfil() == PerfilEnum.CLIENTE && !ticket.getCliente().getId().equals(usuario.getId())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Chamado não encontrado.");
        }
    }

    private void validarTransicao(StatusTicketEnum atual, StatusTicketEnum proximo) {
        boolean permitida = switch (atual) {
            case ABERTO -> proximo == StatusTicketEnum.EM_ANALISE || proximo == StatusTicketEnum.EM_ATENDIMENTO;
            case EM_ANALISE -> proximo == StatusTicketEnum.AGUARDANDO_PECAS
                    || proximo == StatusTicketEnum.EM_ATENDIMENTO || proximo == StatusTicketEnum.RESOLVIDO;
            case AGUARDANDO_PECAS -> proximo == StatusTicketEnum.EM_ATENDIMENTO || proximo == StatusTicketEnum.RESOLVIDO;
            case EM_ATENDIMENTO -> proximo == StatusTicketEnum.AGUARDANDO_PECAS || proximo == StatusTicketEnum.RESOLVIDO;
            case RESOLVIDO -> proximo == StatusTicketEnum.FECHADO || proximo == StatusTicketEnum.EM_ATENDIMENTO;
            case FECHADO -> false;
        };
        if (!permitida) {
            throw new ApiException(HttpStatus.CONFLICT, "Transição de status não permitida: " + atual + " para " + proximo + ".");
        }
    }

    private String gerarCodigo() {
        String codigo;
        do {
            codigo = "AST-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase(java.util.Locale.ROOT);
        } while (ticketRepository.findByCodigo(codigo).isPresent());
        return codigo;
    }

    public record SlaResumo(long respostasAtrasadas, long resolucoesAtrasadas) {
    }
}
