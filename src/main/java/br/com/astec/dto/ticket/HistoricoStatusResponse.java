package br.com.astec.dto.ticket;

import br.com.astec.model.HistoricoStatusTicket;
import br.com.astec.model.enums.StatusTicketEnum;

import java.time.LocalDateTime;

public record HistoricoStatusResponse(
        Long id,
        StatusTicketEnum statusAnterior,
        StatusTicketEnum statusNovo,
        String observacao,
        LocalDateTime dataAlteracao,
        Long usuarioId,
        String usuarioNome) {

    public static HistoricoStatusResponse from(HistoricoStatusTicket historico) {
        return new HistoricoStatusResponse(
                historico.getId(),
                historico.getStatusAnterior(),
                historico.getStatusNovo(),
                historico.getObservacao(),
                historico.getDataAlteracao(),
                historico.getUsuario().getId(),
                historico.getUsuario().getNome());
    }
}
