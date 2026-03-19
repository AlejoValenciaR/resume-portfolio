package com.alejandro.cv.contact;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ContactMailService {

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
    private static final Logger log = LoggerFactory.getLogger(ContactMailService.class);

    private final JavaMailSender mailSender;
    private final String recipientEmail;
    private final String senderEmail;
    private final String smtpHost;
    private final MessageSource messageSource;

    public ContactMailService(
            JavaMailSender mailSender,
            MessageSource messageSource,
            @Value("${app.contact.mail.to:alejo.valenciarivera@gmail.com}") String recipientEmail,
            @Value("${app.contact.mail.from:}") String senderEmail,
            @Value("${spring.mail.host:}") String smtpHost) {
        this.mailSender = mailSender;
        this.messageSource = messageSource;
        this.recipientEmail = recipientEmail;
        this.senderEmail = senderEmail;
        this.smtpHost = smtpHost;
    }

    public void send(ContactMessageRequest request, Locale locale) throws MessagingException {
        if (!StringUtils.hasText(smtpHost)) {
            log.error("Mail delivery is not configured: spring.mail.host is empty.");
            throw new IllegalStateException(
                    messageSource.getMessage("contact.api.mailHostMissing", null, locale));
        }

        if (!StringUtils.hasText(senderEmail)) {
            log.error("Mail delivery is not configured: sender email is empty.");
            throw new IllegalStateException(
                    messageSource.getMessage("contact.api.senderMissing", null, locale));
        }

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper =
                new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());

        helper.setTo(recipientEmail);
        helper.setFrom(senderEmail);
        helper.setReplyTo(request.fromEmail().trim());
        helper.setSubject(messageSource.getMessage("contact.mail.subject", new Object[] { request.subject().trim() }, locale));
        helper.setText(buildMessageBody(request, locale), false);

        try {
            mailSender.send(mimeMessage);
        } catch (MailException ex) {
            log.error(
                    "Failed to send contact email via SMTP host '{}' from '{}' to '{}'.",
                    smtpHost,
                    senderEmail,
                    recipientEmail,
                    ex);
            throw new IllegalStateException(
                    messageSource.getMessage("contact.api.sendFailure", null, locale),
                    ex);
        }
    }

    private String buildMessageBody(ContactMessageRequest request, Locale locale) {
        return messageSource.getMessage(
                "contact.mail.body",
                new Object[] {
                    request.fromEmail().trim(),
                    request.subject().trim(),
                    request.message().trim(),
                    ZonedDateTime.now().format(TIMESTAMP_FORMAT)
                },
                locale);
    }
}
