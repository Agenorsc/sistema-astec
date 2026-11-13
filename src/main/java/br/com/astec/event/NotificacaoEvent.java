package br.com.astec.event;

public record NotificacaoEvent(String destinatario, String assunto, String mensagem) {
}
