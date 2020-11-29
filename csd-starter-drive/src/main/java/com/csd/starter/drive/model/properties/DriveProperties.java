package com.csd.starter.drive.model.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "drive")
public class DriveProperties {

    private String userType;
    private String userRole;
    private String appName;
    private String credentialsFileName;
    private String driveCredentialsPath;
    private String userEmails;
    private boolean driveClean;
    private boolean driveShareRootAgain;

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getCredentialsFileName() {
        return credentialsFileName;
    }

    public void setCredentialsFileName(String credentialsFileName) {
        this.credentialsFileName = credentialsFileName;
    }

    public String getDriveCredentialsPath() {
        return driveCredentialsPath;
    }

    public void setDriveCredentialsPath(String driveCredentialsPath) {
        this.driveCredentialsPath = driveCredentialsPath;
    }

    public String getUserEmails() {
        return userEmails;
    }

    public void setUserEmails(String userEmails) {
        this.userEmails = userEmails;
    }

    public boolean isDriveClean() {
        return driveClean;
    }

    public void setDriveClean(boolean driveClean) {
        this.driveClean = driveClean;
    }

    public boolean isDriveShareRootAgain() {
        return driveShareRootAgain;
    }

    public void setDriveShareRootAgain(boolean driveShareRootAgain) {
        this.driveShareRootAgain = driveShareRootAgain;
    }
}