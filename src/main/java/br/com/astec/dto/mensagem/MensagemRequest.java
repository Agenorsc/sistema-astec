package br.com.astec.dto.mensagem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MensagemRequest(@NotBlank @Size(max = 10000) String texto) {
}
