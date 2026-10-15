package br.com.astec.repository.dto;

import br.com.astec.model.Equipamento;

public class TicketContagemPorEquipamentoDTO {

    private Equipamento equipamento;
    private Long quantidade;

    public TicketContagemPorEquipamentoDTO(Equipamento equipamento, Long quantidade) {
        this.equipamento = equipamento;
        this.quantidade = quantidade;
    }

    public Equipamento getEquipamento() {
        return equipamento;
    }

    public void setEquipamento(Equipamento equipamento) {
        this.equipamento = equipamento;
    }

    public Long getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Long quantidade) {
        this.quantidade = quantidade;
    }
}
