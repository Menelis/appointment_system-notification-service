package co.appointment.service;

import co.appointment.config.AppConfigProperties;
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

    public EmailService(final JavaMailSender mailSender, final AppConfigProperties appConfigProperties) {
        this.javaMailSender = mailSender;
        this.mailSettings = appConfigProperties.getMail();
    }

    public void sendMail(final String to, final String subject, final String body) {
        try {
            MimeMessage mimeMessage = getMimeMessage(to, subject, body);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException exception) {
            log.error(exception.getMessage(), exception);
        }
    }

    private MimeMessage getMimeMessage(final String subject, final String body, final String toEmailAddress)
            throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(mailSettings.getFromAddress());
        helper.setTo(toEmailAddress);
        helper.setSubject(subject);
        helper.setText(body, true);
        return message;
    }
}
