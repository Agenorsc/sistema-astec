package br.com.astec.dto.equipamento;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EquipamentoRequest(
        @NotBlank @Size(max = 255) String nome,
        @Size(max = 255) String modelo,
        @Size(max = 100) String numeroSerie,
        Long clienteId) {
}
