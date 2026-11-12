package br.com.astec.dto.anexo;

import br.com.astec.model.Anexo;

public record AnexoResponse(
        Long id,
        Long ticketId,
        String nomeArquivo,
        String tipoArquivo,
        long tamanhoBytes,
        String urlDownload) {

    public static AnexoResponse from(Anexo anexo) {
        return new AnexoResponse(
                anexo.getId(),
                anexo.getTicket().getId(),
                anexo.getNomeArquivo(),
                anexo.getTipoArquivo(),
                Math.round(anexo.getTamanhoMb() * 1024 * 1024),
                "/api/tickets/" + anexo.getTicket().getId() + "/anexos/" + anexo.getId());
    }
}
