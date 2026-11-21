package br.com.astec.model.enums;

public enum TipoAtendimentoEnum {
    CORRETIVA("Manutenção Corretiva"),
    PREVENTIVA("Manutenção Preventiva"),
    DUVIDA_TECNICA("Dúvida Técnica / Suporte"),
    PECA_DEFEITO("Substituição de Peça com Defeito");

    private final String descricao;

    TipoAtendimentoEnum(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
