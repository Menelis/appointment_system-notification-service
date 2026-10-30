package co.appointment.service;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthNotificationService {

    public void handlePasswordReset(final ConsumerRecord<String, byte[]> record, final String xCorrelationId) {}

    public void handleVerifyEmail(final ConsumerRecord<String, byte[]> record, final String xCorrelationId) {}
}
