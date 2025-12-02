package co.appointment.service;

import co.appointment.config.AppConfigProperties;
import co.appointment.shared.event.EmailEvent;
import co.appointment.shared.service.EncryptionService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final AppConfigProperties.MailSettings mailSettings;
    private final EncryptionService encryptionService;

    public EmailService(
            final JavaMailSender javaMailSender,
            final AppConfigProperties appConfigProperties,
            final EncryptionService encryptionService) {
        this.javaMailSender = javaMailSender;
        this.mailSettings = appConfigProperties.getMail();
        this.encryptionService = encryptionService;
    }
    public void sendEmail(final EmailEvent emailEvent) {
        try {
            if(emailEvent == null) {
                return;
            }
            final String emailBody =  emailEvent.bodyEncrypted() ? encryptionService.decryptCipherText(emailEvent.body()) : emailEvent.body();
            final String recipientEmail = emailEvent.recipientEmailEncrypted() ? encryptionService.decryptCipherText(emailEvent.recipientEmail())  : emailEvent.recipientEmail();
            javaMailSender.send(
                    getMimeMessage(emailEvent.subject(),emailBody, recipientEmail));
        } catch (Exception exception) {
            log.error("There was an issue sending email to: {}", emailEvent.recipientEmail());
            log.error(exception.getMessage(), exception);
        }
    }
    private MimeMessage getMimeMessage(final String subject,
                                       final String body,
                                       final String toEmailAddress) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(mailSettings.getFromAddress());
        helper.setTo(toEmailAddress);
        helper.setSubject(subject);
        helper.setText(body, true);
        return message;
    }
}
