package co.appointment.kafka;

import static co.appointment.shared.constant.AppointmentStatus.BOOKING_CONFIRMED;
import static co.appointment.shared.constant.EventTypeConstants.BOOKING_CANCELLED_EVENT;
import static co.appointment.shared.constant.EventTypeConstants.BOOKING_CONFIRMED_EVENT;
import static co.appointment.shared.constant.EventTypeConstants.EVENT_TYPE;
import static co.appointment.shared.constant.EventTypeConstants.PASSWORD_RESET_EVENT;
import static co.appointment.shared.constant.EventTypeConstants.VERIFY_EMAIL_EVENT;
import static co.appointment.shared.constant.SharedConstants.X_CORRELATION_ID;

import co.appointment.service.AppointmentService;
import co.appointment.service.AuthNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Service
@Slf4j
public class KafkaConsumer {

    private final AppointmentService appointmentService;
    private final AuthNotificationService authNotificationService;

    @KafkaListener(
            topics = "${app.kafka.consumer.topic.name}",
            containerFactory = "containerFactory",
            filter = "filterStrategy")
    public void listenNotifications(
            @Header(name = EVENT_TYPE, required = false) final String eventType,
            @Header(name = X_CORRELATION_ID, required = false) final String xCorrelationId,
            final ConsumerRecord<String, byte[]> record,
            final Acknowledgment acknowledgment) {
        log.info(
                "Received event from topic=[{}], partition=[{}], offset=[{}]",
                record.topic(),
                record.partition(),
                record.offset());

        processEvent(eventType, record, xCorrelationId);

        log.info(
                "Finished processing event from topic=[{}], partition=[{}], offset=[{}]",
                record.topic(),
                record.partition(),
                record.offset());
        acknowledgment.acknowledge();
    }

    private void processEvent(
            final String eventType, final ConsumerRecord<String, byte[]> record, final String xCorrelationId) {
        if (!StringUtils.hasText(eventType)) {
            log.error(
                    "Event from topic:[{}], partition:[{}], offset:[{}] will not be processed because it does not have eventType.",
                    record.topic(),
                    record.partition(),
                    record.offset());
            return;
        }
        switch (eventType) {
            case BOOKING_CONFIRMED, BOOKING_CONFIRMED_EVENT, BOOKING_CANCELLED_EVENT ->
                appointmentService.handleAppointment(record, eventType, xCorrelationId);
            case PASSWORD_RESET_EVENT -> authNotificationService.handlePasswordReset(record, xCorrelationId);
            case VERIFY_EMAIL_EVENT -> authNotificationService.handleVerifyEmail(record, xCorrelationId);
            default -> throw new IllegalStateException("Unsupported event Type=[%s]".formatted(eventType));
        }
    }
}
