package co.appointment.service;

import static co.appointment.shared.constant.EmailConstants.PASSWORD_RESET_SUBJECT;
import static co.appointment.shared.constant.EmailConstants.VERIFY_EMAIL_SUBJECT;
import static co.appointment.shared.constant.SharedConstants.APPOINTMENT_SYSTEM_EMAIL_FOOTER;

import co.appointment.config.AppConfigProperties;
import co.appointment.service.grpc.client.GrpcAuthService;
import co.appointment.shared.event.AuthEvent;
import co.appointment.shared.record.UserSummary;
import co.appointment.shared.record.VerificationTokenSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthNotificationService {

    private final JsonMapper jsonMapper;
    private final GrpcAuthService grpcAuthService;
    private final EmailService emailService;
    private final AppConfigProperties configProperties;

    public void handlePasswordReset(final ConsumerRecord<String, byte[]> record, final String xCorrelationId) {
        AuthEvent authEvent = jsonMapper.readValue(record.value(), AuthEvent.class);

        if (log.isTraceEnabled()) {
            log.trace("Auth Event=[{}] with correlationId=[{}]", authEvent, xCorrelationId);
        }
        UserSummary userSummary = getUserSummary(authEvent);
        VerificationTokenSummary verificationTokenSummary = getVerificationTokenSummary(authEvent);

        String emailBody = getPasswordResetEmailBody(userSummary, verificationTokenSummary);
        if (log.isTraceEnabled()) {
            log.trace("Password Reset Email=[{}]", emailBody);
        }
        emailService.sendMail(userSummary.email(), PASSWORD_RESET_SUBJECT, emailBody);
    }

    String getPasswordResetEmailBody(
            final UserSummary userSummary, final VerificationTokenSummary verificationTokenSummary) {
        String passwordResetEmailTemplate =
                configProperties.getMessageTemplate().getResetPasswordTemplate();
        String forgotPasswordLink = "%s/account/reset-password?email=%s&token=%s"
                .formatted(configProperties.getAppUrl(), userSummary.email(), verificationTokenSummary.token());
        return passwordResetEmailTemplate.formatted(
                userSummary.fullName(), forgotPasswordLink, APPOINTMENT_SYSTEM_EMAIL_FOOTER);
    }

    UserSummary getUserSummary(final AuthEvent authEvent) {
        UserSummary userSummary = grpcAuthService.getUserByEmail(authEvent.getEmail());
        if (log.isTraceEnabled()) {
            log.trace("Auth Server User Summary=[{}]", userSummary);
        }
        return userSummary;
    }

    VerificationTokenSummary getVerificationTokenSummary(final AuthEvent authEvent) {
        VerificationTokenSummary verificationTokenSummary =
                grpcAuthService.getVerificationTokenByEmail(authEvent.getEmail(), authEvent.getTokenType());
        if (log.isTraceEnabled()) {
            log.trace("Auth Server Verification Token Summary=[{}]", verificationTokenSummary);
        }
        return verificationTokenSummary;
    }

    public void handleVerifyEmail(final ConsumerRecord<String, byte[]> record, final String xCorrelationId) {
        AuthEvent authEvent = jsonMapper.readValue(record.value(), AuthEvent.class);

        if (log.isTraceEnabled()) {
            log.trace("Auth Event=[{}] with correlationId=[{}]", authEvent, xCorrelationId);
        }
        UserSummary userSummary = getUserSummary(authEvent);
        VerificationTokenSummary verificationTokenSummary = getVerificationTokenSummary(authEvent);

        String emailBody = getEmailVerificationEmailBody(userSummary, verificationTokenSummary);
        if (log.isTraceEnabled()) {
            log.trace("Email Body=[{}]", emailBody);
        }
        emailService.sendMail(userSummary.email(), VERIFY_EMAIL_SUBJECT, emailBody);
    }

    String getEmailVerificationEmailBody(
            final UserSummary userSummary, final VerificationTokenSummary verificationTokenSummary) {

        String passwordResetEmailTemplate =
                configProperties.getMessageTemplate().getVerifyTemplate();
        String registrationEmailVerificationLink = "%s/account/confirm-email?email=%s&token=%s"
                .formatted(configProperties.getAppUrl(), userSummary.email(), verificationTokenSummary.token());

        return passwordResetEmailTemplate.formatted(
                userSummary.fullName(), registrationEmailVerificationLink, APPOINTMENT_SYSTEM_EMAIL_FOOTER);
    }
}
