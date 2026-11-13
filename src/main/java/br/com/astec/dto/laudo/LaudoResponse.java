package br.com.astec.dto.laudo;

import br.com.astec.model.LaudoTecnico;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record LaudoResponse(
        Long id,
        Long ticketId,
        String diagnosticoSolucao,
        Double horasTrabalhadas,
        LocalDateTime dataRegistro,
        List<ItemPecaResponse> pecasSubstituidas,
        BigDecimal totalPecas) {

    public static LaudoResponse from(LaudoTecnico laudo) {
        List<ItemPecaResponse> items = laudo.getPecasSubstituidas().stream()
                .map(ItemPecaResponse::from)
                .toList();
        BigDecimal total = items.stream()
                .map(ItemPecaResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new LaudoResponse(
                laudo.getId(),
                laudo.getTicket().getId(),
                laudo.getDiagnosticoSolucao(),
                laudo.getHorasTrabalhadas(),
                laudo.getDataRegistro(),
                items,
                total);
    }
}
