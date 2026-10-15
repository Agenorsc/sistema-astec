package br.com.astec.repository;

import br.com.astec.model.*;
import br.com.astec.model.enums.*;
import br.com.astec.repository.dto.TicketContagemPorEquipamentoDTO;
import br.com.astec.repository.dto.TicketContagemPorStatusDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class PesquisasRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private MensagemTicketRepository mensagemTicketRepository;

    @Autowired
    private LaudoTecnicoRepository laudoTecnicoRepository;

    @Autowired
    private ItemPecaSubstituidaRepository itemPecaRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    private Usuario cliente1;
    private Usuario tecnico1;
    private Equipamento tornoCnc;
    private Equipamento prensa;
    private Ticket ticket1;
    private Ticket ticket2;
    private Ticket ticket3;

    @BeforeEach
    public void setup() {
        // Criar Usuários
        cliente1 = new Usuario(null, "Empresa Metalúrgica ABC", "contato@metalabc.com.br", "123456", PerfilEnum.CLIENTE);
        usuarioRepository.save(cliente1);

        tecnico1 = new Usuario(null, "Felipe Técnico", "felipe@astec.com.br", "654321", PerfilEnum.TECNICO);
        usuarioRepository.save(tecnico1);

        // Criar Equipamentos
        tornoCnc = new Equipamento(null, "Torno CNC 4000", "TC-4000", "SERIE-1111");
        equipamentoRepository.save(tornoCnc);

        prensa = new Equipamento(null, "Prensa 50T", "PR-50", "SERIE-2222");
        equipamentoRepository.save(prensa);

        // Criar 2 Tickets para Torno CNC
        ticket1 = new Ticket(null, "AST-001", "Falha de acionamento no spindle", "Equipamento desliga sozinho",
                TipoAtendimentoEnum.CORRETIVA, UrgenciaEnum.CRITICA, StatusTicketEnum.EM_ATENDIMENTO, cliente1, tornoCnc);
        ticket1.setTecnicoResponsavel(tecnico1);
        ticketRepository.save(ticket1);

        ticket2 = new Ticket(null, "AST-002", "Revisão preventiva semestral", "Troca preventiva de guias",
                TipoAtendimentoEnum.PREVENTIVA, UrgenciaEnum.MEDIA, StatusTicketEnum.RESOLVIDO, cliente1, tornoCnc);
        ticket2.setTecnicoResponsavel(tecnico1);
        ticketRepository.save(ticket2);

        // Criar 1 Ticket para Prensa
        ticket3 = new Ticket(null, "AST-003", "Vazamento mangueira hidráulica", "Óleo vertendo na base",
                TipoAtendimentoEnum.PECA_DEFEITO, UrgenciaEnum.ALTA, StatusTicketEnum.ABERTO, cliente1, prensa);
        ticketRepository.save(ticket3);
    }

    @Test
    @DisplayName("Pesquisar usuário por email e perfil")
    public void testPesquisarUsuarioPorEmailEPerfil() {
        Optional<Usuario> usuario = usuarioRepository.findByEmail("contato@metalabc.com.br");
        assertTrue(usuario.isPresent());
        assertEquals("Empresa Metalúrgica ABC", usuario.get().getNome());

        List<Usuario> tecnicos = usuarioRepository.findByPerfil(PerfilEnum.TECNICO);
        assertEquals(1, tecnicos.size());
        assertEquals("Felipe Técnico", tecnicos.get(0).getNome());
    }

    @Test
    @DisplayName("Pesquisar equipamentos por número de série ou modelo")
    public void testPesquisarEquipamentoPorNumeroSerie() {
        Optional<Equipamento> equip = equipamentoRepository.findByNumeroSerie("SERIE-1111");
        assertTrue(equip.isPresent());
        assertEquals("Torno CNC 4000", equip.get().getNome());

        List<Equipamento> resultadoModelo = equipamentoRepository.findByModeloContainingIgnoreCase("PR-50");
        assertFalse(resultadoModelo.isEmpty());
        assertEquals(1, resultadoModelo.size());
    }

    @Test
    @DisplayName("Pesquisar tickets por cliente")
    public void testPesquisarTicketsPorCliente() {
        List<Ticket> ticketsCliente = ticketRepository.findByClienteId(cliente1.getId());
        assertEquals(3, ticketsCliente.size());
        assertTrue(ticketsCliente.stream().anyMatch(t -> t.getCodigo().equals("AST-001")));
    }

    @Test
    @DisplayName("Pesquisar tickets por equipamento")
    public void testPesquisarTicketsPorEquipamento() {
        List<Ticket> ticketsTorno = ticketRepository.findByEquipamentoId(tornoCnc.getId());
        assertEquals(2, ticketsTorno.size());

        List<Ticket> ticketsPrensa = ticketRepository.findByEquipamentoId(prensa.getId());
        assertEquals(1, ticketsPrensa.size());
        assertEquals("AST-003", ticketsPrensa.get(0).getCodigo());
    }

    @Test
    @DisplayName("Pesquisar tickets por status e urgência")
    public void testPesquisarTicketsPorStatusEUrgencia() {
        List<Ticket> ticketsAbertos = ticketRepository.findByStatus(StatusTicketEnum.ABERTO);
        assertEquals(1, ticketsAbertos.size());
        assertEquals("AST-003", ticketsAbertos.get(0).getCodigo());

        List<Ticket> ticketsCriticos = ticketRepository.findByUrgencia(UrgenciaEnum.CRITICA);
        assertEquals(1, ticketsCriticos.size());
        assertEquals("AST-001", ticketsCriticos.get(0).getCodigo());
    }

    @Test
    @DisplayName("Contar tickets por equipamento agrupados via DTO (@Query JPQL)")
    public void testContarTicketsPorEquipamentoEDTO() {
        List<TicketContagemPorEquipamentoDTO> contagens = ticketRepository.contarTicketsPorEquipamento();
        assertEquals(2, contagens.size());

        for (TicketContagemPorEquipamentoDTO dto : contagens) {
            if (dto.getEquipamento().getNome().equals("Torno CNC 4000")) {
                assertEquals(2L, dto.getQuantidade());
            } else if (dto.getEquipamento().getNome().equals("Prensa 50T")) {
                assertEquals(1L, dto.getQuantidade());
            }
        }

        // Teste de contagem individual por entidade (countByEquipamento)
        long totalTorno = ticketRepository.countByEquipamento(tornoCnc);
        assertEquals(2L, totalTorno);
    }

    @Test
    @DisplayName("Contar tickets por status agrupados via DTO")
    public void testContarTicketsPorStatus() {
        List<TicketContagemPorStatusDTO> contagensStatus = ticketRepository.contarTicketsPorStatus();
        assertEquals(3, contagensStatus.size()); // EM_ATENDIMENTO, RESOLVIDO, ABERTO

        long totalAbertos = ticketRepository.countByStatus(StatusTicketEnum.ABERTO);
        assertEquals(1L, totalAbertos);
    }

    @Test
    @DisplayName("Testar laudo técnico, peças substituídas e cálculo de subtotal")
    public void testLaudoTecnicoEPecasSubstituidas() {
        LaudoTecnico laudo = new LaudoTecnico(null, "Eixo alinhado e rolamento substituído.", 3.0, ticket2);
        laudoTecnicoRepository.save(laudo);

        ItemPecaSubstituida peca = new ItemPecaSubstituida(null, "Rolamento Blindado 6002", 2, new BigDecimal("45.50"), laudo);
        itemPecaRepository.save(peca);

        assertEquals(new BigDecimal("91.00"), peca.calcularSubtotal());

        List<ItemPecaSubstituida> pecasDoLaudo = itemPecaRepository.findByLaudoTecnicoId(laudo.getId());
        assertEquals(1, pecasDoLaudo.size());
        assertEquals("Rolamento Blindado 6002", pecasDoLaudo.get(0).getNomePeca());
    }

    @Test
    @DisplayName("Testar envio de mensagens no chamado (Chat/Histórico)")
    public void testMensagensTicket() {
        MensagemTicket msg = new MensagemTicket(null, "Técnico já a caminho da indústria.", ticket1, tecnico1);
        mensagemTicketRepository.save(msg);

        List<MensagemTicket> mensagens = mensagemTicketRepository.findByTicketIdOrderByDataEnvioAsc(ticket1.getId());
        assertEquals(1, mensagens.size());
        assertEquals("Técnico já a caminho da indústria.", mensagens.get(0).getTexto());
        assertEquals(tecnico1.getNome(), mensagens.get(0).getAutor().getNome());
    }
}
