package br.com.astec.dto.ticket;

import br.com.astec.model.Ticket;
import br.com.astec.model.enums.StatusTicketEnum;
import br.com.astec.model.enums.TipoAtendimentoEnum;
import br.com.astec.model.enums.UrgenciaEnum;

import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        String codigo,
        String titulo,
        String descricao,
        TipoAtendimentoEnum tipoAtendimento,
        UrgenciaEnum urgencia,
        StatusTicketEnum status,
        LocalDateTime dataCriacao,
        LocalDateTime prazoPrimeiraResposta,
        LocalDateTime prazoResolucao,
        LocalDateTime dataPrimeiraResposta,
        LocalDateTime dataResolucao,
        LocalDateTime dataFechamento,
        Long clienteId,
        String clienteNome,
        Long tecnicoId,
        String tecnicoNome,
        Long equipamentoId,
        String equipamentoNome) {

    public static TicketResponse from(Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getCodigo(),
                ticket.getTitulo(),
                ticket.getDescricao(),
                ticket.getTipoAtendimento(),
                ticket.getUrgencia(),
                ticket.getStatus(),
                ticket.getDataCriacao(),
                ticket.getPrazoPrimeiraResposta(),
                ticket.getPrazoResolucao(),
                ticket.getDataPrimeiraResposta(),
                ticket.getDataResolucao(),
                ticket.getDataFechamento(),
                ticket.getCliente().getId(),
                ticket.getCliente().getNome(),
                ticket.getTecnicoResponsavel() == null ? null : ticket.getTecnicoResponsavel().getId(),
                ticket.getTecnicoResponsavel() == null ? null : ticket.getTecnicoResponsavel().getNome(),
                ticket.getEquipamento().getId(),
                ticket.getEquipamento().getNome());
    }
}
