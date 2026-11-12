package br.com.astec.dto.laudo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.util.List;

public record LaudoRequest(
        @NotBlank String diagnosticoSolucao,
        @NotNull @PositiveOrZero Double horasTrabalhadas,
        @NotNull @Valid List<@Valid ItemPecaRequest> pecasSubstituidas) {
}
