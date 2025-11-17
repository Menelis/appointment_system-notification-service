package co.appointment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

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
        private TopicSetting topic;
    }
    @Data
    public static class TopicSetting {
        private String name;
        private TopicHeaderFilter topicHeaderFilter;

    }
    @Data
    public static class TopicHeaderFilter {
        private String headerKey;
        private List<String> headerValues;
    }
    @Data
    public static class MailSettings {
        private String fromAddress;
    }
}
