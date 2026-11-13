package br.com.astec.dto.mensagem;

import br.com.astec.model.MensagemTicket;

import java.time.LocalDateTime;

public record MensagemResponse(
        Long id,
        String texto,
        LocalDateTime dataEnvio,
        Long ticketId,
        Long autorId,
        String autorNome) {

    public static MensagemResponse from(MensagemTicket mensagem) {
        return new MensagemResponse(
                mensagem.getId(),
                mensagem.getTexto(),
                mensagem.getDataEnvio(),
                mensagem.getTicket().getId(),
                mensagem.getAutor().getId(),
                mensagem.getAutor().getNome());
    }
}
