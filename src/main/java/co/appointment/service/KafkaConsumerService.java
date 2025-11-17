package co.appointment.service;

import co.appointment.shared.kafka.event.EmailEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final ObjectMapper objectMapper;
    private final EmailService emailService;

    @KafkaListener(
            topics = "${app.kafka.consumer.topic.name}",
            containerFactory = "containerFactory",
            filter = "filterStrategy")
    public void notificationListener(final ConsumerRecord<String, String> record, final Acknowledgment acknowledgment) {
        log.info("Processing record from topic: {}, partition: {}, offset: {}", record.topic(), record.partition(), record.offset());
        emailService.sendEmail(getEmailEvent(record));
        acknowledgment.acknowledge();
        log.info("Finished processing record from topic: {}, partition: {}, offset: {}", record.topic(), record.partition(), record.offset());
    }
    private EmailEvent getEmailEvent(final ConsumerRecord<String, String> record) {
        try {
            return objectMapper.readValue(record.value(), EmailEvent.class);
        } catch (JsonProcessingException exception) {
            log.error(exception.getMessage(), exception);
        }
        return null;
    }
}
