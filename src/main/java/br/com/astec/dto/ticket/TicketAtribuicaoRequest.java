package br.com.astec.dto.ticket;

import jakarta.validation.constraints.NotNull;

public record TicketAtribuicaoRequest(@NotNull Long tecnicoId) {
}
