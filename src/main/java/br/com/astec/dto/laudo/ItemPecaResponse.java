package br.com.astec.dto.laudo;

import br.com.astec.model.ItemPecaSubstituida;

import java.math.BigDecimal;

public record ItemPecaResponse(
        Long id,
        String nomePeca,
        Integer quantidade,
        BigDecimal valorUnitario,
        BigDecimal subtotal) {

    public static ItemPecaResponse from(ItemPecaSubstituida item) {
        return new ItemPecaResponse(
                item.getId(),
                item.getNomePeca(),
                item.getQuantidade(),
                item.getValorUnitario(),
                item.calcularSubtotal());
    }
}
