package com.csd.starter.drive.autoconfigure;

import com.csd.starter.drive.config.DriveManager;
import com.csd.starter.drive.config.DriveManagerConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.csd.starter.drive.config.DriveManagerConfigParams.*;

@Configuration
@ConditionalOnClass(DriveManager.class)
@EnableConfigurationProperties(DriveManagerProperties.class)
public class DriveManagerAutoConfiguration {

    private final DriveManagerProperties driveManagerProperties;

    public DriveManagerAutoConfiguration (DriveManagerProperties driveManagerProperties) {
        this.driveManagerProperties = driveManagerProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public DriveManagerConfig driveManagerConfig() {
        String userType = driveManagerProperties.getUserType() == null ? System.getProperty("user.name") : driveManagerProperties.getUserType();
        String userRole = driveManagerProperties.getUserRole() == null ? System.getProperty("user.role") : driveManagerProperties.getUserRole();
        String appName = driveManagerProperties.getAppName() == null ? System.getProperty("app.name") : driveManagerProperties.getAppName();
        String credentialFileName = driveManagerProperties.getCredentialsFileName() == null ? System.getProperty("credential.name") : driveManagerProperties.getCredentialsFileName();

        DriveManagerConfig driveManagerConfig = new DriveManagerConfig();
        driveManagerConfig.put(USER_TYPE, userType);
        driveManagerConfig.put(USER_ROLE, userRole);
        driveManagerConfig.put(APP_NAME, appName);
        driveManagerConfig.put(CREDENTIALS_FILE_NAME, credentialFileName);

        return driveManagerConfig;
    }

    @Bean
    @ConditionalOnMissingBean
    public DriveManager driveManager(DriveManagerConfig driveManagerConfig) {
        return new DriveManager(driveManagerConfig);
    }
}
