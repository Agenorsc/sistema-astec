package br.com.astec.repository;

import br.com.astec.model.*;
import br.com.astec.model.enums.*;
import br.com.astec.repository.dto.TicketContagemPorEquipamentoDTO;
import br.com.astec.repository.dto.TicketContagemPorStatusDTO;
import br.com.astec.repository.projection.TicketResumoProjecao;
import br.com.astec.repository.specification.TicketSpecification;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.Arrays;
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
    private CategoriaEquipamentoRepository categoriaRepository;

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

    @Autowired
    private EntityManager entityManager;

    private Usuario cliente1;
    private Usuario tecnico1;
    private Equipamento tornoCnc;
    private Equipamento prensa;
    private Ticket ticket1;
    private Ticket ticket2;
    private Ticket ticket3;

    @BeforeEach
    public void setup() {
        // Categorias de Equipamento (@ManyToMany - Deck 3)
        CategoriaEquipamento catUsinagem = new CategoriaEquipamento("Usinagem", "Máquinas operatrizes de precisão");
        CategoriaEquipamento catHidraulica = new CategoriaEquipamento("Hidráulica", "Prensas e pistões");
        categoriaRepository.saveAll(Arrays.asList(catUsinagem, catHidraulica));

        // Usuário com Endereço Embutido (@Embedded - Deck 3)
        cliente1 = new Usuario(null, "Empresa Metalúrgica ABC", "contato@metalabc.com.br", "123456", PerfilEnum.CLIENTE);
        cliente1.setEndereco(new Endereco("Rua Industrial", "500", "Distrito", "Joinville", "SC", "89200-000"));
        usuarioRepository.save(cliente1);

        tecnico1 = new Usuario(null, "Felipe Técnico", "felipe@astec.com.br", "654321", PerfilEnum.TECNICO);
        usuarioRepository.save(tecnico1);

        // Equipamento com Tags (@ElementCollection) e Categorias (@ManyToMany)
        tornoCnc = new Equipamento(null, "Torno CNC 4000", "TC-4000", "SERIE-1111");
        tornoCnc.setTags(Arrays.asList("CNC", "Fuso Esferas", "Siemens"));
        tornoCnc.setCategorias(Arrays.asList(catUsinagem));
        equipamentoRepository.save(tornoCnc);

        prensa = new Equipamento(null, "Prensa 50T", "PR-50", "SERIE-2222");
        prensa.setTags(Arrays.asList("Hidráulica", "50T"));
        prensa.setCategorias(Arrays.asList(catHidraulica));
        equipamentoRepository.save(prensa);

        // Tickets
        ticket1 = new Ticket(null, "AST-001", "Falha de acionamento no spindle", "Equipamento desliga sozinho",
                TipoAtendimentoEnum.CORRETIVA, UrgenciaEnum.CRITICA, StatusTicketEnum.EM_ATENDIMENTO, cliente1, tornoCnc);
        ticket1.setTecnicoResponsavel(tecnico1);
        ticketRepository.save(ticket1);

        ticket2 = new Ticket(null, "AST-002", "Revisão preventiva semestral", "Troca preventiva de guias",
                TipoAtendimentoEnum.PREVENTIVA, UrgenciaEnum.MEDIA, StatusTicketEnum.RESOLVIDO, cliente1, tornoCnc);
        ticket2.setTecnicoResponsavel(tecnico1);
        ticketRepository.save(ticket2);

        ticket3 = new Ticket(null, "AST-003", "Vazamento mangueira hidráulica", "Óleo vertendo na base",
                TipoAtendimentoEnum.PECA_DEFEITO, UrgenciaEnum.ALTA, StatusTicketEnum.ABERTO, cliente1, prensa);
        ticketRepository.save(ticket3);
    }

    @Test
    @DisplayName("Testar endereço embutido (@Embedded) e persistência de usuário")
    public void testEnderecoEmbutidoUsuario() {
        Optional<Usuario> opt = usuarioRepository.findByEmail("contato@metalabc.com.br");
        assertTrue(opt.isPresent());
        Usuario u = opt.get();
        assertNotNull(u.getEndereco());
        assertEquals("Rua Industrial", u.getEndereco().getLogradouro());
        assertEquals("Joinville", u.getEndereco().getCidade());
        assertEquals("SC", u.getEndereco().getUf());
    }

    @Test
    @DisplayName("Testar @ElementCollection (Tags) e @ManyToMany (Categorias) no Equipamento")
    public void testElementCollectionEManyToManyEquipamento() {
        Optional<Equipamento> opt = equipamentoRepository.findByNumeroSerie("SERIE-1111");
        assertTrue(opt.isPresent());
        Equipamento eq = opt.get();

        assertEquals(3, eq.getTags().size());
        assertTrue(eq.getTags().contains("CNC"));

        assertEquals(1, eq.getCategorias().size());
        assertEquals("Usinagem", eq.getCategorias().get(0).getNome());
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
    @DisplayName("Pesquisar tickets por cliente")
    public void testPesquisarTicketsPorCliente() {
        List<Ticket> ticketsCliente = ticketRepository.findByClienteId(cliente1.getId());
        assertEquals(3, ticketsCliente.size());
        assertTrue(ticketsCliente.stream().anyMatch(t -> t.getCodigo().equals("AST-001")));
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
        assertEquals(2L, ticketRepository.countByEquipamento(tornoCnc));
    }

    @Test
    @DisplayName("Contar tickets por status agrupados via DTO")
    public void testContarTicketsPorStatus() {
        List<TicketContagemPorStatusDTO> contagensStatus = ticketRepository.contarTicketsPorStatus();
        assertEquals(3, contagensStatus.size());
        assertEquals(1L, ticketRepository.countByStatus(StatusTicketEnum.ABERTO));
    }

    @Test
    @DisplayName("Paginação de chamados (Spring Data Pageable - Deck 2, Slides 13-14)")
    public void testPaginacaoChamados() {
        Page<Ticket> pagina = ticketRepository.findByStatus(StatusTicketEnum.ABERTO, PageRequest.of(0, 5));
        assertNotNull(pagina);
        assertEquals(1, pagina.getTotalElements());
        assertEquals(1, pagina.getContent().size());
        assertEquals("AST-003", pagina.getContent().get(0).getCodigo());
    }

    @Test
    @DisplayName("Projeção por Interface (Deck 2, Slides 5 e 15)")
    public void testProjecaoPorInterface() {
        List<TicketResumoProjecao> projecoes = ticketRepository.findProjecaoByStatus(StatusTicketEnum.RESOLVIDO);
        assertFalse(projecoes.isEmpty());
        TicketResumoProjecao proj = projecoes.get(0);
        assertEquals(ticket2.getId(), proj.getId());
        assertEquals("AST-002", proj.getCodigo());
        assertEquals("Revisão preventiva semestral", proj.getTitulo());
        assertEquals(StatusTicketEnum.RESOLVIDO, proj.getStatus());
        assertEquals(UrgenciaEnum.MEDIA, proj.getUrgencia());
    }

    @Test
    @DisplayName("Consulta Nativa SQL (Deck 2, Slide 11)")
    public void testNativeQueryUrgencia() {
        List<Ticket> criticos = ticketRepository.buscarPorUrgenciaNativa("CRITICA");
        assertFalse(criticos.isEmpty());
        assertEquals("AST-001", criticos.get(0).getCodigo());
    }

    @Test
    @DisplayName("Consultas Dinâmicas com Criteria / Specification (Deck 2, Slides 18-21)")
    public void testSpecificationCriteria() {
        Specification<Ticket> spec = TicketSpecification.comFiltros(StatusTicketEnum.EM_ATENDIMENTO, UrgenciaEnum.CRITICA, "spindle");
        List<Ticket> resultado = ticketRepository.findAll(spec);

        assertEquals(1, resultado.size());
        assertEquals("AST-001", resultado.get(0).getCodigo());

        Specification<Ticket> specVazia = TicketSpecification.comFiltros(null, null, "Inexistente");
        List<Ticket> vazia = ticketRepository.findAll(specVazia);
        assertTrue(vazia.isEmpty());
    }

    @Test
    @DisplayName("Named Queries declaradas na Entidade (Deck 2, Slides 45-54)")
    public void testNamedQueries() {
        List<Ticket> ticketsNamed = entityManager.createNamedQuery("Ticket.listarTodos", Ticket.class).getResultList();
        assertEquals(3, ticketsNamed.size());

        List<Equipamento> equipamentosNamed = entityManager.createNamedQuery("Equipamento.listarTodos", Equipamento.class).getResultList();
        assertEquals(2, equipamentosNamed.size());
    }

    @Test
    @DisplayName("Testar laudo técnico, peças substituídas e cálculo de subtotal (@Transient)")
    public void testLaudoTecnicoEPecasSubstituidas() {
        LaudoTecnico laudo = new LaudoTecnico(null, "Eixo alinhado e rolamento substituído.", 3.0, ticket2);
        laudoTecnicoRepository.save(laudo);

        ItemPecaSubstituida peca = new ItemPecaSubstituida(null, "Rolamento Blindado 6002", 2, new BigDecimal("45.50"), laudo);
        itemPecaRepository.save(peca);

        assertEquals(new BigDecimal("91.00"), peca.getSubtotal());

        List<ItemPecaSubstituida> pecasDoLaudo = itemPecaRepository.findByLaudoTecnicoId(laudo.getId());
        assertEquals(1, pecasDoLaudo.size());
        assertEquals("Rolamento Blindado 6002", pecasDoLaudo.get(0).getNomePeca());
    }

    @Test
    @DisplayName("Testar controle de concorrência otimista (@Version - Deck 3, Slides 97-100)")
    public void testControleConcorrenciaOtimistaVersion() {
        Ticket t = ticketRepository.findById(ticket1.getId()).orElseThrow();
        Long versaoInicial = t.getVersao();
        assertNotNull(versaoInicial);

        t.setTitulo("Título Modificado Para Teste de Versão");
        ticketRepository.saveAndFlush(t);

        Ticket tAtualizado = ticketRepository.findById(ticket1.getId()).orElseThrow();
        assertTrue(tAtualizado.getVersao() > versaoInicial);
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
