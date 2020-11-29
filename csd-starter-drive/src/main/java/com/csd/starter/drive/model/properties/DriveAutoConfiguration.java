package com.csd.starter.drive.model.properties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass
@EnableConfigurationProperties(DriveProperties.class)
public class DriveAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DriveProperties driveProperties() {

        DriveProperties driveProperties = new DriveProperties();




        return driveProperties;
    }
}