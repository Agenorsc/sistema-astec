package br.com.astec.dto.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RequisicaoAvaliacao {

    @NotNull(message = "A nota é obrigatória (1 a 5)")
    @Min(value = 1, message = "A nota mínima é 1 estrela")
    @Max(value = 5, message = "A nota máxima é 5 estrelas")
    private Integer nota;

    @Size(max = 1000, message = "O comentário não pode ter mais de 1000 caracteres")
    private String comentario;

    public Integer getNota() {
        return nota;
    }

    public void setNota(Integer nota) {
        this.nota = nota;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
