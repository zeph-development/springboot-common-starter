package com.csd.starter.drive.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "drive")
public class DriveManagerProperties {

    private String userType;
    private String userRole;
    private String appName;
    private String credentialsFileName;


}
