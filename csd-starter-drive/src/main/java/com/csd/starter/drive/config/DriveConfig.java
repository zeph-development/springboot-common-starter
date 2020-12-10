package com.csd.starter.drive.config;

import com.csd.starter.drive.component.DriveOperationsManager;
import com.csd.starter.drive.model.prop.Properties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.csd.starter.drive.model.prop.Properties.APP_NAME;
import static com.csd.starter.drive.model.prop.Properties.CREDENTIALS_FILE_PATH;
import static com.csd.starter.drive.model.prop.Properties.PROP_PREFIX;

@Configuration
@EnableConfigurationProperties(Properties.class)
public class DriveConfig {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = PROP_PREFIX, name = {APP_NAME, CREDENTIALS_FILE_PATH})
    public DriveOperationsManager driveOperationsManager(Properties prop) {
        return new DriveOperationsManager(prop);
    }
}
