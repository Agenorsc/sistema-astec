package br.com.astec.dto.equipamento;

import br.com.astec.model.Equipamento;

public record EquipamentoResponse(
        Long id,
        String nome,
        String modelo,
        String numeroSerie,
        Long clienteId,
        String clienteNome) {

    public static EquipamentoResponse from(Equipamento equipamento) {
        return new EquipamentoResponse(
                equipamento.getId(),
                equipamento.getNome(),
                equipamento.getModelo(),
                equipamento.getNumeroSerie(),
                equipamento.getCliente() == null ? null : equipamento.getCliente().getId(),
                equipamento.getCliente() == null ? null : equipamento.getCliente().getNome());
    }
}
