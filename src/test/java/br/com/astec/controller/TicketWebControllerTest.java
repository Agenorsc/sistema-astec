package br.com.astec.controller;

import br.com.astec.model.Equipamento;
import br.com.astec.model.Ticket;
import br.com.astec.model.Usuario;
import br.com.astec.model.enums.PerfilEnum;
import br.com.astec.model.enums.StatusTicketEnum;
import br.com.astec.model.enums.TipoAtendimentoEnum;
import br.com.astec.model.enums.UrgenciaEnum;
import br.com.astec.repository.EquipamentoRepository;
import br.com.astec.repository.TicketRepository;
import br.com.astec.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TicketWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    @DisplayName("GET /home deve carregar dashboard com status 200 e view home")
    public void testHomeView() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attributeExists("tickets", "totalTickets", "todosStatus"));
    }

    @Test
    @DisplayName("GET /tickets/novo deve exibir formulário com status 200")
    public void testFormularioNovoTicket() throws Exception {
        mockMvc.perform(get("/tickets/novo"))
                .andExpect(status().isOk())
                .andExpect(view().name("tickets/formulario"))
                .andExpect(model().attributeExists("requisicaoNovoTicket", "clientes", "equipamentos", "urgencias"));
    }

    @Test
    @DisplayName("POST /tickets/novo com campos inválidos deve reexibir formulário com erros de validação (Deck 4)")
    public void testValidacaoFormularioComErros() throws Exception {
        mockMvc.perform(post("/tickets/novo")
                        .param("titulo", "") // Invalido: @NotBlank
                        .param("descricao", "") // Invalido: @NotBlank
                )
                .andExpect(status().isOk())
                .andExpect(view().name("tickets/formulario"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("POST /tickets/novo com dados válidos deve persistir e redirecionar para detalhes (Deck 4)")
    public void testSalvarTicketValido() throws Exception {
        Usuario cliente = usuarioRepository.findByPerfil(PerfilEnum.CLIENTE).get(0);
        Equipamento eq = equipamentoRepository.findAll().get(0);

        mockMvc.perform(post("/tickets/novo")
                        .param("titulo", "Problema com motor elétrico trifásico")
                        .param("descricao", "Motor aquece rapidamente e desarma o disjuntor do quadro")
                        .param("clienteId", cliente.getId().toString())
                        .param("equipamentoId", eq.getId().toString())
                        .param("urgencia", UrgenciaEnum.ALTA.name())
                        .param("tipoAtendimento", TipoAtendimentoEnum.CORRETIVA.name())
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/tickets/*"));
    }

    @Test
    @DisplayName("GET /tickets/{id} deve exibir tela de detalhes do chamado")
    public void testDetalhesTicket() throws Exception {
        Ticket t = ticketRepository.findAll().get(0);

        mockMvc.perform(get("/tickets/" + t.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("tickets/detalhes"))
                .andExpect(model().attributeExists("ticket", "todosStatus"));
    }

    @Test
    @DisplayName("GET /equipamentos deve listar inventário com status 200")
    public void testEquipamentosLista() throws Exception {
        mockMvc.perform(get("/equipamentos"))
                .andExpect(status().isOk())
                .andExpect(view().name("equipamentos/lista"))
                .andExpect(model().attributeExists("equipamentos"));
    }

    @Test
    @DisplayName("REST API: GET /api/tickets deve retornar JSON 200 OK")
    public void testRestListarTickets() throws Exception {
        mockMvc.perform(get("/api/tickets")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}
