package br.com.astec.service;

import br.com.astec.model.enums.UrgenciaEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.Map;

@Component
public class SlaPolicy {

    private final Map<UrgenciaEnum, Duration> firstResponse = new EnumMap<>(UrgenciaEnum.class);
    private final Map<UrgenciaEnum, Duration> resolution = new EnumMap<>(UrgenciaEnum.class);

    public SlaPolicy(
            @Value("${app.sla.critica.primeira-resposta:PT1H}") String criticalResponse,
            @Value("${app.sla.critica.resolucao:PT4H}") String criticalResolution,
            @Value("${app.sla.alta.primeira-resposta:PT4H}") String highResponse,
            @Value("${app.sla.alta.resolucao:PT24H}") String highResolution,
            @Value("${app.sla.media.primeira-resposta:PT8H}") String mediumResponse,
            @Value("${app.sla.media.resolucao:PT72H}") String mediumResolution,
            @Value("${app.sla.baixa.primeira-resposta:PT24H}") String lowResponse,
            @Value("${app.sla.baixa.resolucao:PT120H}") String lowResolution) {
        firstResponse.put(UrgenciaEnum.CRITICA, Duration.parse(criticalResponse));
        resolution.put(UrgenciaEnum.CRITICA, Duration.parse(criticalResolution));
        firstResponse.put(UrgenciaEnum.ALTA, Duration.parse(highResponse));
        resolution.put(UrgenciaEnum.ALTA, Duration.parse(highResolution));
        firstResponse.put(UrgenciaEnum.MEDIA, Duration.parse(mediumResponse));
        resolution.put(UrgenciaEnum.MEDIA, Duration.parse(mediumResolution));
        firstResponse.put(UrgenciaEnum.BAIXA, Duration.parse(lowResponse));
        resolution.put(UrgenciaEnum.BAIXA, Duration.parse(lowResolution));
        if (firstResponse.values().stream().anyMatch(value -> value.isNegative() || value.isZero())
                || resolution.values().stream().anyMatch(value -> value.isNegative() || value.isZero())) {
            throw new IllegalArgumentException("Os prazos de SLA devem ser positivos.");
        }
    }

    public LocalDateTime prazoPrimeiraResposta(UrgenciaEnum urgencia, LocalDateTime inicio) {
        return inicio.plus(firstResponse.get(urgencia));
    }

    public LocalDateTime prazoResolucao(UrgenciaEnum urgencia, LocalDateTime inicio) {
        return inicio.plus(resolution.get(urgencia));
    }
}
