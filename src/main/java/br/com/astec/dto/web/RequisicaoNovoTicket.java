package br.com.astec.dto.web;

import br.com.astec.model.Equipamento;
import br.com.astec.model.Ticket;
import br.com.astec.model.Usuario;
import br.com.astec.model.enums.StatusTicketEnum;
import br.com.astec.model.enums.TipoAtendimentoEnum;
import br.com.astec.model.enums.UrgenciaEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * DTO para formulário de abertura de novo chamado
 * Utiliza Bean Validation conforme ensinado em aula (Deck 4, Slides 24 a 33).
 */
public class RequisicaoNovoTicket {

    @NotBlank(message = "O título do chamado é obrigatório e não pode estar em branco.")
    @Size(min = 5, max = 150, message = "O título deve possuir entre 5 e 150 caracteres.")
    private String titulo;

    @NotBlank(message = "A descrição da falha técnica é obrigatória.")
    @Size(min = 10, max = 2000, message = "A descrição deve possuir no mínimo 10 caracteres.")
    private String descricao;

    @NotNull(message = "Informe o tipo de atendimento.")
    private TipoAtendimentoEnum tipoAtendimento;

    @NotNull(message = "Informe o nível de urgência.")
    private UrgenciaEnum urgencia;

    @NotNull(message = "Selecione o equipamento que necessita de assistência.")
    private Long equipamentoId;

    @NotNull(message = "Selecione o cliente solicitante.")
    private Long clienteId;

    public Ticket toTicket(Usuario cliente, Equipamento equipamento) {
        Ticket ticket = new Ticket();
        ticket.setTitulo(this.titulo);
        ticket.setDescricao(this.descricao);
        ticket.setTipoAtendimento(this.tipoAtendimento);
        ticket.setUrgencia(this.urgencia);
        ticket.setStatus(StatusTicketEnum.ABERTO);
        ticket.setDataCriacao(LocalDateTime.now());
        ticket.setCliente(cliente);
        ticket.setEquipamento(equipamento);
        ticket.setCodigo("AST-" + System.currentTimeMillis() % 1000000);
        return ticket;
    }

    // Getters e Setters

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public TipoAtendimentoEnum getTipoAtendimento() {
        return tipoAtendimento;
    }

    public void setTipoAtendimento(TipoAtendimentoEnum tipoAtendimento) {
        this.tipoAtendimento = tipoAtendimento;
    }

    public UrgenciaEnum getUrgencia() {
        return urgencia;
    }

    public void setUrgencia(UrgenciaEnum urgencia) {
        this.urgencia = urgencia;
    }

    public Long getEquipamentoId() {
        return equipamentoId;
    }

    public void setEquipamentoId(Long equipamentoId) {
        this.equipamentoId = equipamentoId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }
}
