package br.com.astec.model.enums;

public enum StatusTicketEnum {
    ABERTO("Aberto"),
    EM_ANALISE("Em Análise"),
    AGUARDANDO_PECAS("Aguardando Peças"),
    EM_ATENDIMENTO("Em Atendimento"),
    RESOLVIDO("Resolvido"),
    FECHADO("Fechado"),
    CANCELADO("Cancelado");

    private final String descricao;

    StatusTicketEnum(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
