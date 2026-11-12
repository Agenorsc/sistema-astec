package br.com.astec.dto.laudo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ItemPecaRequest(
        @NotBlank @Size(max = 255) String nomePeca,
        @NotNull @Positive Integer quantidade,
        @NotNull @DecimalMin("0.00") BigDecimal valorUnitario) {
}
