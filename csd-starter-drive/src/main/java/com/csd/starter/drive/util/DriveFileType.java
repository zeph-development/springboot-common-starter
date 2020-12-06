package com.csd.starter.drive.util;

import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class DriveFileType {

    public static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
}
