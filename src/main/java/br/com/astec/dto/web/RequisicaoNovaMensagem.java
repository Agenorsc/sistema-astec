package br.com.astec.dto.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RequisicaoNovaMensagem {

    @NotNull(message = "Autor deve ser informado")
    private Long autorId;

    @NotBlank(message = "A mensagem não pode estar vazia")
    @Size(max = 2000, message = "Mensagem muito longa (máximo 2000 caracteres)")
    private String texto;

    public Long getAutorId() {
        return autorId;
    }

    public void setAutorId(Long autorId) {
        this.autorId = autorId;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
}
