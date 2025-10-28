package co.appointment.kafka.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.listener.adapter.RecordFilterStrategy;
import org.springframework.util.StringUtils;

@Slf4j
public  class HeaderRecordFilterStrategy implements RecordFilterStrategy<String, String> {
    private final String eventType;
    private final String eventValue;

    public HeaderRecordFilterStrategy(final String eventType, final String eventValue) {
        this.eventType = eventType;
        this.eventValue = eventValue;
    }
    @Override
    public boolean filter(final ConsumerRecord<String, String> record) {
        Header header = record.headers().lastHeader(eventType);
        if(header == null) {
            return true;
        }
        String headerValue = new String(header.value());
        if(!StringUtils.hasText(headerValue)) {
            return true;
        }
        return !eventValue.equalsIgnoreCase(headerValue);
    }
}
