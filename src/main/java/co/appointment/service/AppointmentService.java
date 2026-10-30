package co.appointment.service;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    public void handleAppointment(
            final ConsumerRecord<String, byte[]> record, final String event, final String xCorrelationId) {}
}
