package br.com.astec.service;

import br.com.astec.event.NotificacaoEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class NotificacaoService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacaoService.class);

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String mailHost;
    private final String senderAddress;

    public NotificacaoService(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            @Value("${spring.mail.host:}") String mailHost,
            @Value("${app.notifications.from:no-reply@astec.local}") String senderAddress) {
        this.mailSenderProvider = mailSenderProvider;
        this.mailHost = mailHost;
        this.senderAddress = senderAddress;
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void enviar(NotificacaoEvent event) {
        if (mailHost.isBlank()) {
            logger.info("Notificação registrada para envio local: {}.", event.assunto());
            return;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            logger.error("Servidor SMTP configurado, mas o envio de email não está disponível.");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderAddress);
        message.setTo(event.destinatario());
        message.setSubject(event.assunto());
        message.setText(event.mensagem());
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            logger.error("Falha ao enviar notificação SMTP.", exception);
        }
    }
}
