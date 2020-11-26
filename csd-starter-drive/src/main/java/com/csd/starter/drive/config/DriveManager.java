package com.csd.starter.drive.config;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.csd.starter.drive.model.properties.DriveProperties.*;
import static java.lang.String.format;
import static java.util.Objects.isNull;

@Slf4j
public class DriveManager {

    private DriveConfig driveConfig;

    public DriveManager(DriveConfig driveConfig) {
        this.driveConfig = driveConfig;
    }

    private static Credential credential;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty
    private synchronized Credential getCredential() {
        try {
            if (isNull(credential)) {
                java.io.File credentialsFile = new java.io.File(format("/%s", driveConfig.getProperty(CREDENTIALS_FILE_NAME)));
                InputStream is = new FileInputStream(credentialsFile);
                credential = GoogleCredential.fromStream(is)
                        .createScoped(Collections.singleton(DriveScopes.DRIVE));
            }
        } catch (IOException e) {
            //throw new InputOutputException(e.getMessage());
        }
        return credential;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty
    private Drive service() {
        try {
            return new Drive.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(), getCredential())
                    .setApplicationName(driveConfig.getProperty(APP_NAME)).build();
        } catch (GeneralSecurityException e) {
            //throw new GeneralSecurityAppException(e.getMessage());
            throw new RuntimeException(e.getMessage());
        } catch (IOException e) {
            //throw new InputOutputException(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty
    public List<File> searchFiles(String query) {
        List<File> files = new ArrayList<>();
        String pageToken = null;
        try {
            do {
                FileList result = service().files().list()
                        .setQ(query)
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name)")
                        .setPageToken(pageToken)
                        .execute();

                files.addAll(result.getFiles());
                pageToken = result.getNextPageToken();
            } while (pageToken != null);
        } catch (IOException e) {
            //throw new InputOutputException(e.getMessage());
        }
        return files;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty
    protected File searchFileById(String fileId) {
        File file = null;
        try {
            file = service().files().get(fileId).execute();
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }
        } catch (IOException e) {
            //throw new InputOutputException(e.getMessage());
        }
        return file;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty
    protected File uploadFile(List<String> parentFoldersIds, java.io.File file, String fileType, String fileName) {
        File uploadedFile;
        try {
            File fileMetadata = new File();
            fileMetadata.setName(isNull(fileName) ? file.getName() : fileName);
            fileMetadata.setParents(parentFoldersIds);
            FileContent mediaContent = new FileContent(fileType, file);
            uploadedFile = service().files().create(fileMetadata, mediaContent)
                    .setFields("id, name")
                    .execute();
            log.info("Uploaded file with name {} and id {}", uploadedFile.getName(), uploadedFile.getId());
        } catch (IOException e) {
            String errorMessage = format("Fail uploading file %s because %s", file.getName(), e.getMessage());
            //throw new InputOutputException(errorMessage);
            throw new RuntimeException(e.getMessage());
        }
        return uploadedFile;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty
    protected File createFolder(List<String> parentFoldersIds, String folderName) {
        File createdFolder;
        try {
            File fileMetadata = new File();
            fileMetadata.setName(folderName);
            fileMetadata.setParents(parentFoldersIds);
            //fileMetadata.setMimeType(DriveFileType.FOLDER_MIME_TYPE);
            createdFolder = service().files().create(fileMetadata)
                    .setFields("id, name")
                    .execute();
            log.info("Create folder with name {} and id {}", createdFolder.getName(), createdFolder.getId());
        } catch (IOException e) {
            //throw new InputOutputException(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        return createdFolder;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty
    public byte[] downloadFile(String fileId) {
        ByteArrayOutputStream outputStream;
        try {
            outputStream = new ByteArrayOutputStream();
            service().files().get(fileId).executeMediaAndDownloadTo(outputStream);
            log.info("Downloaded file with id {}", fileId);
        } catch (IOException e) {
            //throw new InputOutputException(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        return outputStream.toByteArray();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty
    protected void deleteFile(String fileId) {
        try {
            service().files().delete(fileId).execute();
            log.info("Deleted file with id {}", fileId);
        } catch (IOException e) {
            //throw new InputOutputException(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty
    public void shareFile(String fileId, String userEmail) {
        try {
            JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
                @Override
                public void onFailure(GoogleJsonError e,
                                      HttpHeaders responseHeaders) {
                    log.error("Fail tot share main folder to user {} \n {}", userEmail, e.getMessage());
                }

                @Override
                public void onSuccess(Permission permission,
                                      HttpHeaders responseHeaders) {
                    log.info("Shared file {} with user {}", fileId, userEmail);
                }
            };
            BatchRequest batch = service().batch();
            Permission userPermission = new Permission()
                    .setType(driveConfig.getProperty(USER_TYPE))
                    .setRole(driveConfig.getProperty(USER_ROLE))
                    .setEmailAddress(userEmail);
            service().permissions().create(fileId, userPermission)
                    .setFields("id")
                    .queue(batch, callback);
            batch.execute();
        } catch (IOException e) {
            //throw new InputOutputException(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
}
