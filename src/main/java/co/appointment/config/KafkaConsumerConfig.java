package co.appointment.config;

import co.appointment.kafka.filter.HeaderRecordFilterStrategy;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> containerFactory(final KafkaProperties kafkaProperties) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties(null)));
        if(kafkaProperties.getListener() != null) {
            factory.setConcurrency(kafkaProperties.getListener().getConcurrency());
            factory.getContainerProperties().setAckMode(kafkaProperties.getListener().getAckMode());
            factory.setAutoStartup(kafkaProperties.getListener().isAutoStartup());
        }
        return factory;
    }
    @Bean
    public RecordFilterStrategy<String, String> filterStrategy(final AppConfigProperties appConfigProperties) {
        final AppConfigProperties.TopicHeaderFilter topicHeaderFilter = appConfigProperties.getKafka()
                .getConsumer()
                .getTopic()
                .getTopicHeaderFilter();
        return new HeaderRecordFilterStrategy(topicHeaderFilter.getHeaderKey(), topicHeaderFilter.getHeaderValues());
    }
}
