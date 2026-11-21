package br.com.astec.config;

import br.com.astec.model.*;
import br.com.astec.model.enums.*;
import br.com.astec.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(
            UsuarioRepository usuarioRepository,
            EquipamentoRepository equipamentoRepository,
            CategoriaEquipamentoRepository categoriaRepository,
            TicketRepository ticketRepository,
            MensagemTicketRepository mensagemTicketRepository,
            LaudoTecnicoRepository laudoTecnicoRepository,
            AvaliacaoRepository avaliacaoRepository) {

        return args -> {
            // Evita duplicidade se o banco já estiver populado
            if (categoriaRepository.count() > 0 || usuarioRepository.count() > 0) {
                return;
            }

            // 1. Cadastrar Categorias de Equipamento (@ManyToMany - Deck 3)
            CategoriaEquipamento catUsinagem = new CategoriaEquipamento("Usinagem CNC", "Máquinas operatrizes de precisão computadorizada");
            CategoriaEquipamento catHidraulica = new CategoriaEquipamento("Sistemas Hidráulicos", "Prensas e atuadores de alta pressão");
            CategoriaEquipamento catInformatica = new CategoriaEquipamento("TI & Hardware", "Computadores, servidores e estações de trabalho");
            CategoriaEquipamento catAutomacao = new CategoriaEquipamento("Automação Industrial", "CLPs, inversores de frequência e sensores");

            categoriaRepository.saveAll(Arrays.asList(catUsinagem, catHidraulica, catInformatica, catAutomacao));

            // 2. Cadastrar Usuários com @Embedded Endereço (Deck 3)
            Usuario admin = new Usuario(null, "Agenor Alvise", "agenor@agemquinas.com.br", "admin123", PerfilEnum.ADMIN);
            admin.setTelefone("(47) 98888-0001");
            admin.setEndereco(new Endereco("Rua das Indústrias", "1000", "Distrito Industrial", "Joinville", "SC", "89219-500"));

            Usuario tecnico1 = new Usuario(null, "Felipe Santos", "felipe.tecnico@agemquinas.com.br", "tec123", PerfilEnum.TECNICO);
            tecnico1.setTelefone("(47) 98888-0002");
            tecnico1.setEndereco(new Endereco("Rua Anita Garibaldi", "450", "Anita Garibaldi", "Joinville", "SC", "89203-300"));

            Usuario tecnico2 = new Usuario(null, "Henrique Cezar", "henrique.tecnico@agemquinas.com.br", "tec456", PerfilEnum.TECNICO);
            tecnico2.setTelefone("(47) 98888-0003");
            tecnico2.setEndereco(new Endereco("Av. Getúlio Vargas", "120", "Bucarein", "Joinville", "SC", "89202-000"));

            Usuario cliente1 = new Usuario(null, "Metalúrgica Joinville LTDA", "contato@metaljoin.com.br", "cli123", PerfilEnum.CLIENTE);
            cliente1.setTelefone("(47) 3451-9000");
            cliente1.setEndereco(new Endereco("Rua Dona Francisca", "8300", "Zona Industrial Norte", "Joinville", "SC", "89219-600"));

            Usuario cliente2 = new Usuario(null, "Indústria Têxtil Catarinense", "manutencao@textilcat.com.br", "cli456", PerfilEnum.CLIENTE);
            cliente2.setTelefone("(47) 3433-2200");
            cliente2.setEndereco(new Endereco("Rua Santa Catarina", "1540", "Floresta", "Joinville", "SC", "89211-300"));

            usuarioRepository.saveAll(Arrays.asList(admin, tecnico1, tecnico2, cliente1, cliente2));

            // 3. Cadastrar Equipamentos com @ElementCollection (Tags) e @ManyToMany (Categorias)
            Equipamento eq1 = new Equipamento(null, "Torno CNC Industrial 5000", "TC-5000X", "SN-CNC-2023-001");
            eq1.setTags(Arrays.asList("CNC", "Siemens 840D", "Alta Rotação", "Fuso Esferas"));
            eq1.setCategorias(Arrays.asList(catUsinagem, catAutomacao));

            Equipamento eq2 = new Equipamento(null, "Centro de Usinagem Vertical", "CUV-800", "SN-CUV-2022-042");
            eq2.setTags(Arrays.asList("3 Eixos", "Fanuc", "Refrigeração Interna"));
            eq2.setCategorias(Arrays.asList(catUsinagem));

            Equipamento eq3 = new Equipamento(null, "Prensa Hidráulica 100T", "PH-100", "SN-PRENSA-2021-099");
            eq3.setTags(Arrays.asList("100 Toneladas", "Rexroth", "Cilindro Duplo"));
            eq3.setCategorias(Arrays.asList(catHidraulica, catAutomacao));

            Equipamento eq4 = new Equipamento(null, "Servidor de Produção Dell PowerEdge", "R740", "SN-DELL-SRV-901");
            eq4.setTags(Arrays.asList("Rack 2U", "Xeon Gold", "RAID 10", "Rede 10GbE"));
            eq4.setCategorias(Arrays.asList(catInformatica));

            equipamentoRepository.saveAll(Arrays.asList(eq1, eq2, eq3, eq4));

            // 4. Cadastrar Tickets
            Ticket ticket1 = new Ticket(null, "AST-20261001", "Vibração excessiva no fuso principal",
                    "Durante a usinagem em alta rotação, o torno emite vibração atípica e superaquecimento no cabeçote móvel.",
                    TipoAtendimentoEnum.CORRETIVA, UrgenciaEnum.ALTA, StatusTicketEnum.EM_ATENDIMENTO, cliente1, eq1);
            ticket1.setTecnicoResponsavel(tecnico1);
            ticketRepository.save(ticket1);

            Ticket ticket2 = new Ticket(null, "AST-20261002", "Manutenção preventiva semestral",
                    "Troca de filtros hidráulicos, inspeção de guias lineares e calibração de sensores ópticos.",
                    TipoAtendimentoEnum.PREVENTIVA, UrgenciaEnum.MEDIA, StatusTicketEnum.RESOLVIDO, cliente2, eq2);
            ticket2.setTecnicoResponsavel(tecnico2);
            ticket2.setDataFechamento(LocalDateTime.now().minusHours(3));
            ticket2 = ticketRepository.save(ticket2);

            Ticket ticket3 = new Ticket(null, "AST-20261003", "Vazamento no retentor do cilindro hidráulico",
                    "Prensa perdendo pressão máxima de compactação de 100 toneladas devido a vazamento contínuo de óleo na vedação primária.",
                    TipoAtendimentoEnum.PECA_DEFEITO, UrgenciaEnum.CRITICA, StatusTicketEnum.ABERTO, cliente1, eq3);
            ticketRepository.save(ticket3);

            // 5. Cadastrar Mensagens (Chat do Chamado - @OneToMany)
            MensagemTicket msg1 = new MensagemTicket(ticket1, cliente1, "Olá suporte! Notamos ruído estridente e vibração acima de 4000 RPM.");
            MensagemTicket msg2 = new MensagemTicket(ticket1, tecnico1, "Recebido! Já separamos o kit de micrômetros e rolamentos de precisão para visita in-loco.");
            mensagemTicketRepository.saveAll(Arrays.asList(msg1, msg2));

            // 6. Cadastrar Laudo Técnico e Peças com Subtotal (@Transient)
            LaudoTecnico laudo2 = new LaudoTecnico();
            laudo2.setTicket(ticket2);
            laudo2.setDiagnosticoSolucao("Realizada limpeza ultra-sônica das guias lineares, substituição preventiva de filtros e reposição de fluído lubrificante.");
            laudo2.setHorasTrabalhadas(4.5);
            laudo2.setDataFinalizacao(LocalDateTime.now().minusHours(2));

            ItemPecaSubstituida peca1 = new ItemPecaSubstituida("Kit Retentores Viton", 2, new BigDecimal("185.50"));
            ItemPecaSubstituida peca2 = new ItemPecaSubstituida("Filtro Hidráulico de Sucção", 1, new BigDecimal("320.00"));

            laudo2.adicionarItem(peca1);
            laudo2.adicionarItem(peca2);
            laudo2.setValorTotal(peca1.getSubtotal().add(peca2.getSubtotal()));

            laudoTecnicoRepository.save(laudo2);

            // 7. Cadastrar Avaliação de Satisfação (@OneToOne)
            Avaliacao avaliacao2 = new Avaliacao(ticket2, 5, "Atendimento exemplar da equipe Astec! Máquina operando com precisão milesimal.");
            avaliacaoRepository.save(avaliacao2);

            System.out.println(">>> [Sistema Astec] Carga inicial de dados finalizada com sucesso!");
        };
    }
}
