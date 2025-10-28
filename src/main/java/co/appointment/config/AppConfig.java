package co.appointment.config;

import co.appointment.shared.config.SharedAppConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public SharedAppConfigProperties sharedAppConfigProperties(final AppConfigProperties appConfigProperties) {
        SharedAppConfigProperties sharedAppConfigProperties = new SharedAppConfigProperties();
        sharedAppConfigProperties.setEncryptionKey(appConfigProperties.getEncryptionKey());
        return sharedAppConfigProperties;
    }
}
