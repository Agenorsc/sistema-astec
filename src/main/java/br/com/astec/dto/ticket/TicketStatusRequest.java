package br.com.astec.dto.ticket;

import br.com.astec.model.enums.StatusTicketEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TicketStatusRequest(
        @NotNull StatusTicketEnum status,
        @Size(max = 1000) String observacao) {
}
