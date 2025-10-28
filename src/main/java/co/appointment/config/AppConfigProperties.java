package co.appointment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app")
public class AppConfigProperties {
    private String encryptionKey;
    private KafkaSettings kafka;
    private MailSettings mail;

    @Data
    public static class KafkaSettings {
        private ConsumerSettings consumer;
    }

    @Data
    public static class ConsumerSettings {
        private String notificationTopic;
    }
    @Data
    public static class MailSettings {
        private String fromAddress;
    }
}
