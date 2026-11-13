package br.com.astec;

import br.com.astec.model.Usuario;
import br.com.astec.model.enums.PerfilEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.security.jwt.secret=0123456789abcdef0123456789abcdef",
        "app.seed.demo-data=true",
        "app.attachments.directory=target/test-anexos"
})
@AutoConfigureMockMvc
class SistemaAstecApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void usuarioJsonDoesNotExposePassword() throws Exception {
        Usuario usuario = new Usuario(1L, "Cliente", "cliente@example.com", "senha-secreta", PerfilEnum.CLIENTE);

        String json = objectMapper.writeValueAsString(usuario);

        assertFalse(json.contains("senha"));
        assertFalse(json.contains("senha-secreta"));
    }

    @Test
    void protectedEndpointRejectsAnonymousRequest() throws Exception {
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void openApiPublishesBearerAuthenticationScheme() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
    }

    @Test
    void loginReturnsBearerTokenAndAllowsAuthenticatedRequest() throws Exception {
        String token = login("agenor@agemquinas.com.br", "admin123");

        mockMvc.perform(get("/api/tickets")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void clientCannotReadUserAdministrationOrGlobalStatistics() throws Exception {
        String token = login("contato@metaljoin.com.br", "cli123");

        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/tickets/estatisticas/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void technicianCanReadGlobalStatistics() throws Exception {
        String token = login("felipe.tecnico@agemquinas.com.br", "tec123");

        mockMvc.perform(get("/api/tickets/estatisticas/status")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void clientCanCreateTicketOnlyForOwnedEquipment() throws Exception {
        String clientToken = login("contato@metaljoin.com.br", "cli123");
        String request = """
                {
                  "titulo": "Falha no equipamento",
                  "descricao": "O equipamento parou durante a operação.",
                  "tipoAtendimento": "CORRETIVA",
                  "urgencia": "ALTA",
                  "equipamentoId": 1
                }
                """;

        mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").isNotEmpty())
                .andExpect(jsonPath("$.status").value("ABERTO"))
                .andExpect(jsonPath("$.prazoPrimeiraResposta").isNotEmpty());

        String otherCustomerEquipmentRequest = request.replace("\"equipamentoId\": 1", "\"equipamentoId\": 2");
        mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(otherCustomerEquipmentRequest))
                .andExpect(status().isForbidden());
    }

    @Test
    void clientCanFilterOwnTicketsByEquipmentAndDateRange() throws Exception {
        String clientToken = login("contato@metaljoin.com.br", "cli123");

        mockMvc.perform(get("/api/tickets")
                        .param("equipamentoId", "1")
                        .param("dataInicio", "2020-01-01")
                        .param("dataFim", "2035-12-31")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content.length()").value(
                                org.hamcrest.Matchers.greaterThan(0)))
                        .andExpect(jsonPath("$.content[0].equipamentoId").value(1));

        mockMvc.perform(get("/api/tickets")
                                .param("equipamentoId", "2")
                                .header("Authorization", "Bearer " + clientToken))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.content.length()").value(0));

        mockMvc.perform(get("/api/tickets")
                        .param("dataInicio", "2026-12-31")
                        .param("dataFim", "2026-01-01")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void technicianCanAssignResolveAndReceiveCustomerEvaluation() throws Exception {
        String clientToken = login("contato@metaljoin.com.br", "cli123");
        String technicianToken = login("felipe.tecnico@agemquinas.com.br", "tec123");
        String createRequest = """
                {
                  "titulo": "Manutenção para validação",
                  "descricao": "Chamado criado para validar o fluxo do atendimento.",
                  "tipoAtendimento": "PREVENTIVA",
                  "urgencia": "MEDIA",
                  "equipamentoId": 1
                }
                """;

        String ticketResponse = mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long ticketId = objectMapper.readTree(ticketResponse).get("id").asLong();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                        "/api/tickets/" + ticketId + "/atribuicao")
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tecnicoId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANALISE"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                        "/api/tickets/" + ticketId + "/status")
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"EM_ATENDIMENTO\",\"observacao\":\"Atendimento iniciado\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/tickets/" + ticketId + "/laudo")
                        .header("Authorization", "Bearer " + technicianToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "diagnosticoSolucao": "Equipamento inspecionado e liberado.",
                                  "horasTrabalhadas": 1.5,
                                  "pecasSubstituidas": [
                                    {"nomePeca":"Filtro","quantidade":2,"valorUnitario":15.50}
                                  ]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalPecas").value(31.00));

        mockMvc.perform(post("/api/tickets/" + ticketId + "/avaliacao")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nota\":5,\"comentario\":\"Atendimento excelente\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nota").value(5));

        mockMvc.perform(get("/api/tickets/" + ticketId + "/historico")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void clientCanUploadAndDownloadOnlyAllowedAttachmentTypes() throws Exception {
        String clientToken = login("contato@metaljoin.com.br", "cli123");
        String technicianToken = login("felipe.tecnico@agemquinas.com.br", "tec123");
        String ticketResponse = mockMvc.perform(post("/api/tickets")
                        .header("Authorization", "Bearer " + clientToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "titulo": "Anexo de teste",
                                  "descricao": "Chamado para validar anexos.",
                                  "tipoAtendimento": "DUVIDA_TECNICA",
                                  "urgencia": "BAIXA",
                                  "equipamentoId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long ticketId = objectMapper.readTree(ticketResponse).get("id").asLong();

        MockMultipartFile pdf = new MockMultipartFile(
                "arquivo", "manual.pdf", "application/pdf", "%PDF-1.4\nconteudo".getBytes());
        String attachmentResponse = mockMvc.perform(multipart("/api/tickets/" + ticketId + "/anexos")
                        .file(pdf)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeArquivo").value("manual.pdf"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long attachmentId = objectMapper.readTree(attachmentResponse).get("id").asLong();

        mockMvc.perform(get("/api/tickets/" + ticketId + "/anexos/" + attachmentId)
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .content().contentType(MediaType.APPLICATION_PDF));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(
                        "/api/tickets/" + ticketId + "/anexos/" + attachmentId)
                        .header("Authorization", "Bearer " + technicianToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void adminCanCreateUserWithoutReturningOrStoringPlaintextPassword() throws Exception {
        String adminToken = login("agenor@agemquinas.com.br", "admin123");
        String request = """
                {
                  "nome": "Novo Cliente",
                  "email": "novo-cliente@example.com",
                  "senha": "senha-segura-123",
                  "perfil": "CLIENTE"
                }
                """;

        String response = mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertFalse(response.contains("senha-segura-123"));
        assertFalse(response.contains("\"senha\""));
        assertFalse(response.contains("$2a$"));
        login("novo-cliente@example.com", "senha-segura-123");
    }

    @Test
    void loginRejectsInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"agenor@agemquinas.com.br","senha":"incorreta"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginNormalizesEmailCaseAndWhitespace() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"  AGENOR@AGEMQUINAS.COM.BR  ","senha":"admin123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    private String login(String email, String senha) throws Exception {
        String body = objectMapper.writeValueAsString(java.util.Map.of("email", email, "senha", senha));

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("accessToken").asText();
    }

}
