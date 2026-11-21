package br.com.astec.service;

import br.com.astec.dto.web.RequisicaoAvaliacao;
import br.com.astec.dto.web.RequisicaoNovaMensagem;
import br.com.astec.dto.web.RequisicaoNovoLaudo;
import br.com.astec.dto.web.RequisicaoNovoTicket;
import br.com.astec.model.*;
import br.com.astec.model.enums.StatusTicketEnum;
import br.com.astec.model.enums.UrgenciaEnum;
import br.com.astec.repository.*;
import br.com.astec.repository.specification.TicketSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @Autowired
    private MensagemTicketRepository mensagemRepository;

    @Autowired
    private LaudoTecnicoRepository laudoRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    public List<Ticket> listarTodos() {
        return ticketRepository.findAll();
    }

    public Optional<Ticket> buscarPorId(Long id) {
        return ticketRepository.findById(id);
    }

    public Page<Ticket> pesquisarComFiltro(StatusTicketEnum status, UrgenciaEnum urgencia, String termo, Pageable pageable) {
        Specification<Ticket> spec = TicketSpecification.comFiltros(status, urgencia, termo);
        return ticketRepository.findAll(spec, pageable);
    }

    @Transactional
    public Ticket criarTicket(RequisicaoNovoTicket requisicao) {
        Usuario cliente = usuarioRepository.findById(requisicao.getClienteId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado com ID: " + requisicao.getClienteId()));

        Equipamento equipamento = equipamentoRepository.findById(requisicao.getEquipamentoId())
                .orElseThrow(() -> new IllegalArgumentException("Equipamento não encontrado com ID: " + requisicao.getEquipamentoId()));

        Ticket ticket = requisicao.toTicket(cliente, equipamento);
        return ticketRepository.save(ticket);
    }

    @Transactional
    public Ticket alterarStatus(Long id, StatusTicketEnum novoStatus) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket não encontrado com ID: " + id));

        ticket.alterarStatus(novoStatus);
        return ticketRepository.save(ticket);
    }

    @Transactional
    public MensagemTicket adicionarMensagem(Long ticketId, RequisicaoNovaMensagem req) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket não encontrado com ID: " + ticketId));

        Usuario autor = usuarioRepository.findById(req.getAutorId())
                .orElseThrow(() -> new IllegalArgumentException("Autor não encontrado com ID: " + req.getAutorId()));

        MensagemTicket msg = new MensagemTicket(ticket, autor, req.getTexto());
        return mensagemRepository.save(msg);
    }

    @Transactional
    public LaudoTecnico emitirLaudo(Long ticketId, RequisicaoNovoLaudo req) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket não encontrado com ID: " + ticketId));

        LaudoTecnico laudo = new LaudoTecnico();
        laudo.setTicket(ticket);
        laudo.setDiagnosticoSolucao(req.getDiagnosticoSolucao());
        laudo.setHorasTrabalhadas(req.getHorasTrabalhadas());
        laudo.setDataFinalizacao(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;
        if (req.getNomePeca() != null && !req.getNomePeca().isBlank() && req.getQuantidadePeca() != null && req.getValorUnitarioPeca() != null) {
            ItemPecaSubstituida item = new ItemPecaSubstituida(req.getNomePeca(), req.getQuantidadePeca(), req.getValorUnitarioPeca());
            laudo.adicionarItem(item);
            total = total.add(item.getSubtotal());
        }
        laudo.setValorTotal(total);

        LaudoTecnico salvo = laudoRepository.save(laudo);
        ticket.setLaudoTecnico(salvo);
        ticket.alterarStatus(StatusTicketEnum.RESOLVIDO);
        ticketRepository.save(ticket);

        return salvo;
    }

    @Transactional
    public Avaliacao avaliarTicket(Long ticketId, RequisicaoAvaliacao req) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket não encontrado com ID: " + ticketId));

        if (ticket.getStatus() != StatusTicketEnum.RESOLVIDO && ticket.getStatus() != StatusTicketEnum.FECHADO) {
            throw new IllegalStateException("O ticket só pode ser avaliado após ser resolvido ou fechado.");
        }

        Avaliacao avaliacao = new Avaliacao(ticket, req.getNota(), req.getComentario());
        Avaliacao salva = avaliacaoRepository.save(avaliacao);
        ticket.setAvaliacao(salva);
        ticket.alterarStatus(StatusTicketEnum.FECHADO);
        ticketRepository.save(ticket);

        return salva;
    }
}
