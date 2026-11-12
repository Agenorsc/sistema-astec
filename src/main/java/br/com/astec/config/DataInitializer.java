package br.com.astec.config;

import br.com.astec.model.*;
import br.com.astec.model.enums.*;
import br.com.astec.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
@ConditionalOnProperty(name = "app.seed.demo-data", havingValue = "true")
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(
            UsuarioRepository usuarioRepository,
            EquipamentoRepository equipamentoRepository,
            TicketRepository ticketRepository,
            MensagemTicketRepository mensagemTicketRepository,
            LaudoTecnicoRepository laudoTecnicoRepository,
            ItemPecaSubstituidaRepository itemPecaRepository,
            AvaliacaoRepository avaliacaoRepository,
            PasswordEncoder passwordEncoder) {
        
        return args -> {
            Usuario admin = new Usuario(null, "Agenor Alvise", "agenor@agemquinas.com.br", passwordEncoder.encode("admin123"), PerfilEnum.ADMIN);
            Usuario tecnico1 = new Usuario(null, "Felipe Santos", "felipe.tecnico@agemquinas.com.br", passwordEncoder.encode("tec123"), PerfilEnum.TECNICO);
            Usuario tecnico2 = new Usuario(null, "Henrique Cezar", "henrique.tecnico@agemquinas.com.br", passwordEncoder.encode("tec456"), PerfilEnum.TECNICO);
            Usuario cliente1 = new Usuario(null, "Metalúrgica Joinville LTDA", "contato@metaljoin.com.br", passwordEncoder.encode("cli123"), PerfilEnum.CLIENTE);
            Usuario cliente2 = new Usuario(null, "Indústria Têxtil Catarinense", "manutencao@textilcat.com.br", passwordEncoder.encode("cli456"), PerfilEnum.CLIENTE);

            usuarioRepository.save(admin);
            usuarioRepository.save(tecnico1);
            usuarioRepository.save(tecnico2);
            usuarioRepository.save(cliente1);
            usuarioRepository.save(cliente2);

            Equipamento eq1 = new Equipamento(null, "Torno CNC Industrial 5000", "TC-5000X", "SN-CNC-2023-001");
            eq1.setCliente(cliente1);
            Equipamento eq2 = new Equipamento(null, "Centro de Usinagem Vertical", "CUV-800", "SN-CUV-2022-042");
            eq2.setCliente(cliente2);
            Equipamento eq3 = new Equipamento(null, "Prensa Hidráulica 100T", "PH-100", "SN-PRENSA-2021-099");
            eq3.setCliente(cliente1);

            equipamentoRepository.save(eq1);
            equipamentoRepository.save(eq2);
            equipamentoRepository.save(eq3);

            Ticket ticket1 = new Ticket(null, "AST-20261001", "Vibração excessiva no fuso principal",
                    "Durante a usinagem em alta rotação, o torno emite vibração atípica e superaquecimento.",
                    TipoAtendimentoEnum.CORRETIVA, UrgenciaEnum.ALTA, StatusTicketEnum.EM_ATENDIMENTO, cliente1, eq1);
            ticket1.setTecnicoResponsavel(tecnico1);
            ticketRepository.save(ticket1);

            Ticket ticket2 = new Ticket(null, "AST-20261002", "Manutenção preventiva semestral",
                    "Troca de filtros, verificação de sensores pneumáticos e alinhamento de guias lineares.",
                    TipoAtendimentoEnum.PREVENTIVA, UrgenciaEnum.MEDIA, StatusTicketEnum.RESOLVIDO, cliente2, eq2);
            ticket2.setTecnicoResponsavel(tecnico2);
            ticket2.setDataFechamento(LocalDateTime.now().minusHours(2));
            ticketRepository.save(ticket2);

            Ticket ticket3 = new Ticket(null, "AST-20261003", "Vazamento no retentor do cilindro hidráulico",
                    "Prensa perdendo pressão máxima de compactação de 100 toneladas devido a vazamento de óleo.",
                    TipoAtendimentoEnum.PECA_DEFEITO, UrgenciaEnum.CRITICA, StatusTicketEnum.ABERTO, cliente1, eq3);
            ticketRepository.save(ticket3);

            MensagemTicket msg1 = new MensagemTicket(null, "Olá, o problema começou ontem durante o turno da tarde.", ticket1, cliente1);
            MensagemTicket msg2 = new MensagemTicket(null, "Entendido! Já estou a caminho com o kit de aferição de folgas.", ticket1, tecnico1);
            mensagemTicketRepository.save(msg1);
            mensagemTicketRepository.save(msg2);

            LaudoTecnico laudo2 = new LaudoTecnico(null, "Realizada a limpeza completa dos bicos de lubrificação e troca do jogo de retentores.", 4.5, ticket2);
            laudoTecnicoRepository.save(laudo2);

            ItemPecaSubstituida peca1 = new ItemPecaSubstituida(null, "Kit Retentores Viton", 2, new BigDecimal("185.50"), laudo2);
            ItemPecaSubstituida peca2 = new ItemPecaSubstituida(null, "Filtro Hidráulico de Sucção", 1, new BigDecimal("320.00"), laudo2);
            itemPecaRepository.save(peca1);
            itemPecaRepository.save(peca2);

            Avaliacao avaliacao2 = new Avaliacao(null, 5, "Atendimento exemplar do técnico Henrique, resolveu com agilidade e perícia!", ticket2);
            avaliacaoRepository.save(avaliacao2);

            System.out.println(">>> [Sistema Astec] Carga inicial de dados finalizada com sucesso!");
        };
    }
}
