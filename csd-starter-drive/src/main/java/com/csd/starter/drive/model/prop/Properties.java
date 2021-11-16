package com.csd.starter.drive.model.prop;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Data;

import static com.csd.starter.drive.model.prop.Properties.PROP_PREFIX;


@Data
@ConfigurationProperties(prefix = PROP_PREFIX)
public class Properties {

    public static final String PROP_PREFIX = "drive";

    public static final String APP_NAME = "appName";
    public static final String CREDENTIALS_FILE_PATH = "credentialsFilePath";

    private String appName;
    private String credentialsFilePath;
}
