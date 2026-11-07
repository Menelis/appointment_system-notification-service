package co.appointment.service;

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

    public void handlePasswordReset(final ConsumerRecord<String, byte[]> record, final String xCorrelationId) {
        AuthEvent authEvent = jsonMapper.readValue(record.value(), AuthEvent.class);

        if (log.isTraceEnabled()) {
            log.trace("Auth Event=[{}] with correlationId=[{}]", authEvent, xCorrelationId);
        }

        UserSummary userSummary = getUserSummary(authEvent);
        VerificationTokenSummary verificationTokenSummary = getVerificationTokenSummary(authEvent);
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
                grpcAuthService.getVerificationTokenByEmail(authEvent.getEmail());
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
    }
}
