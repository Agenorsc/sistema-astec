package br.com.astec.repository.dto;

import br.com.astec.model.enums.StatusTicketEnum;

public class TicketContagemPorStatusDTO {

    private StatusTicketEnum status;
    private Long quantidade;

    public TicketContagemPorStatusDTO(StatusTicketEnum status, Long quantidade) {
        this.status = status;
        this.quantidade = quantidade;
    }

    public StatusTicketEnum getStatus() {
        return status;
    }

    public void setStatus(StatusTicketEnum status) {
        this.status = status;
    }

    public Long getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Long quantidade) {
        this.quantidade = quantidade;
    }
}
