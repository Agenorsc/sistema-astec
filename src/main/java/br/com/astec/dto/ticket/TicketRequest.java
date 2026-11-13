package br.com.astec.dto.ticket;

import br.com.astec.model.enums.TipoAtendimentoEnum;
import br.com.astec.model.enums.UrgenciaEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TicketRequest(
        @NotBlank @Size(max = 255) String titulo,
        @NotBlank @jakarta.validation.constraints.Size(max = 10000) String descricao,
        @NotNull TipoAtendimentoEnum tipoAtendimento,
        @NotNull UrgenciaEnum urgencia,
        @NotNull Long equipamentoId) {
}
