package br.com.astec.dto.avaliacao;

import br.com.astec.model.Avaliacao;

import java.time.LocalDateTime;

public record AvaliacaoResponse(
        Long id,
        Long ticketId,
        Integer nota,
        String comentario,
        LocalDateTime dataAvaliacao) {

    public static AvaliacaoResponse from(Avaliacao avaliacao) {
        return new AvaliacaoResponse(
                avaliacao.getId(),
                avaliacao.getTicket().getId(),
                avaliacao.getNota(),
                avaliacao.getComentario(),
                avaliacao.getDataAvaliacao());
    }
}
