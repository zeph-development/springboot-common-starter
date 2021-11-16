package com.csd.starter.drive.component;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.csd.starter.drive.model.prop.Properties;
import com.csd.starter.drive.error.GeneralSecurityAppException;
import com.csd.starter.drive.error.InputOutputException;
import com.csd.starter.drive.util.DriveFileType;
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
import org.springframework.http.HttpStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static java.lang.String.format;
import static java.util.Objects.isNull;

@Slf4j
@RequiredArgsConstructor
public class DriveOperationsManager {

    private final Properties prop;
    private static Credential credential;

    private synchronized Credential getCredential() {
        try {
            if (isNull(credential)) {
                java.io.File credentialsFile = new java.io.File(format("/%s", prop.getCredentialsFilePath()));
                InputStream is = new FileInputStream(credentialsFile);
                credential = GoogleCredential.fromStream(is)
                    .createScoped(Collections.singleton(DriveScopes.DRIVE));
            }
        } catch (IOException e) {
            throw new InputOutputException(e.getMessage(), e.getCause());
        }
        return credential;
    }

    private Drive service() {
        try {
            return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(), getCredential())
                .setApplicationName(prop.getAppName()).build();
        } catch (GeneralSecurityException e) {

            throw new GeneralSecurityAppException(e.getMessage(), e.getCause());
        } catch (IOException e) {
            throw new InputOutputException(e.getMessage(), e.getCause());
        }
    }

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

            throw new InputOutputException(e.getMessage(), e.getCause());
        }
        return files;
    }

    public File searchFileById(String fileId) {
        File file = null;
        try {
            file = service().files().get(fileId).execute();
        } catch (HttpResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
                return null;
            }
        } catch (IOException e) {
            throw new InputOutputException(e.getMessage(), e.getCause());
        }
        return file;
    }

    public File uploadFile(List<String> parentFoldersIds, java.io.File file, String fileType, String fileName) {
        File uploadedFile;
        try {
            File fileMetadata = new File();
            fileMetadata.setName(isNull(fileName) ? file.getName() : fileName);
            fileMetadata.setParents(parentFoldersIds);
            FileContent mediaContent = new FileContent(fileType, file);
            uploadedFile = service().files().create(fileMetadata, mediaContent)
                .setFields("id, name")
                .execute();
            log.trace("Uploaded file with name {} and id {}", uploadedFile.getName(), uploadedFile.getId());
        } catch (IOException e) {
            String errorMessage = format("Fail uploading file %s because %s", file.getName(), e.getMessage());
            throw new InputOutputException(errorMessage, e.getCause());
        }
        return uploadedFile;
    }

    public File createFolder(List<String> parentFoldersIds, String folderName) {
        File createdFolder;
        try {
            File fileMetadata = new File();
            fileMetadata.setName(folderName);
            fileMetadata.setParents(parentFoldersIds);
            fileMetadata.setMimeType(DriveFileType.FOLDER_MIME_TYPE);
            createdFolder = service().files().create(fileMetadata)
                .setFields("id, name")
                .execute();
            log.trace("Create folder with name {} and id {}", createdFolder.getName(), createdFolder.getId());
        } catch (IOException e) {

            throw new InputOutputException(e.getMessage(), e.getCause());
        }
        return createdFolder;
    }

    public byte[] downloadFile(String fileId) {
        ByteArrayOutputStream outputStream;
        try {
            outputStream = new ByteArrayOutputStream();
            service().files().get(fileId).executeMediaAndDownloadTo(outputStream);
            log.trace("Downloaded file with id {}", fileId);
        } catch (IOException e) {
            throw new InputOutputException(e.getMessage(), e.getCause());
        }
        return outputStream.toByteArray();
    }

    public void deleteFile(String fileId) {
        try {
            service().files().delete(fileId).execute();
            log.trace("Deleted file with id {}", fileId);
        } catch (IOException e) {
            throw new InputOutputException(e.getMessage(), e.getCause());
        }
    }

    public void shareFile(String fileId, String userEmail, String userRole, String userType) {
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
                    log.trace("Shared file {} with user {}", fileId, userEmail);
                }
            };
            BatchRequest batch = service().batch();
            Permission userPermission = new Permission()
                .setType(userRole)
                .setRole(userType)
                .setEmailAddress(userEmail);
            service().permissions().create(fileId, userPermission)
                .setFields("id")
                .queue(batch, callback);
            batch.execute();
        } catch (IOException e) {

            throw new InputOutputException(e.getMessage(), e.getCause());
        }
    }
}
