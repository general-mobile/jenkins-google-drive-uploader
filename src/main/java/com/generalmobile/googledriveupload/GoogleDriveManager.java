package com.generalmobile.googledriveupload;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.Permission;
import hudson.model.BuildListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Objects;

class GoogleDriveManager {

    private static final int MB = 0x100000;

    private static final String APPLICATION_NAME = "Jenkins drive uploader";

    private static HttpTransport HTTP_TRANSPORT;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private final Drive drive;

    GoogleDriveManager(Credential credentials) {
        drive = getDriveService(credentials);
    }

    private Drive getDriveService(Credential credential) {
        return new Drive.Builder(
                HTTP_TRANSPORT, new JacksonFactory(), credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    void uploadFolder(String file, String remoteDir, BuildListener listener, String userMail) {
        java.io.File tmpFile = new java.io.File(file);
        listener.getLogger().println("userMail " + userMail);
        File parent = controlParent(remoteDir, userMail);
        if (parent != null) {
            uploadFile(parent.getId(), tmpFile, listener);
        }
    }

    private void uploadFile(String copyTo, final java.io.File downloaded, final BuildListener listener) {
        try {
            if (downloaded != null && downloaded.isDirectory()) {
                File folder = new com.google.api.services.drive.model.File();
                folder.setTitle(downloaded.getName());
                if (!"-".equals(copyTo)) {
                    folder.setParents(Collections.singletonList(new ParentReference().setId(copyTo)));
                }
                folder.setMimeType("application/vnd.google-apps.folder");
                com.google.api.services.drive.model.File newFolder;
                try {
                    newFolder = drive.files().insert(folder)
                            .setFields("id")
                            .execute();
                    for (java.io.File file : Objects.requireNonNull(downloaded.listFiles())) {
                        uploadFile( newFolder.getId(), file, listener);
                    }
                } catch (IOException e) {
                    listener.getLogger().println(e.getMessage());
                }
            } else {
                com.google.api.services.drive.model.File body1 = new com.google.api.services.drive.model.File();


                if (downloaded != null) {
                    listener.getLogger().println("File upload starting. " + downloaded.getPath());
                    body1.setTitle(downloaded.getName());
                    String type1 = java.nio.file.Files.probeContentType(downloaded.toPath());
                    body1.setMimeType(type1);
                    if (!copyTo.isEmpty() && !"-".equals(copyTo)) {
                        body1.setParents(Collections.singletonList(new ParentReference().setId(copyTo)));
                    }
                    FileContent mediaContent1 = new FileContent(type1, downloaded);
                    Drive.Files.Insert create = drive.files().insert(body1, mediaContent1);
                    MediaHttpUploader uploader = create.getMediaHttpUploader();
                    uploader.setDirectUploadEnabled(false);
                    uploader.setChunkSize(2 * MB);
                    uploader.setProgressListener(new MediaHttpUploaderProgressListener() {
                        @Override
                        public void progressChanged(MediaHttpUploader uploader) throws IOException {
                            switch (uploader.getUploadState()) {
                                case MEDIA_IN_PROGRESS:
                                    NumberFormat formatter = new DecimalFormat("#0.00");
                                    String progress = formatter.format(uploader.getProgress() * 100);
                                    listener.getLogger().println("Uploading in progress %" + progress);
                                    break;
                                case MEDIA_COMPLETE:
                                    listener.getLogger().println("Uploading finish " + downloaded.getPath());
                                    break;
                            }
                        }
                    });
                    create.execute();
                }
            }
        } catch (IOException e) {
            listener.getLogger().println(e.getMessage());
        }
    }

    private File controlParent(String parentId, String userMail) {

        try {
            FileList list = drive.files().list().execute();

            for (File file : list.getItems()) {
                if (parentId.equals(file.getTitle())) {
                    return file;
                }
            }

            File file = new File();
            file.setTitle(parentId);
            file.setMimeType("application/vnd.google-apps.folder");

            File inserted = drive.files().insert(file).execute();

            Permission userPermission = new Permission()
                    .setValue(userMail)
                    .setType("user")
                    .setRole("writer")
                    .setEmailAddress(userMail);
            drive.permissions().insert(inserted.getId(), userPermission)
                    .setFields("id")
                    .execute();

            return inserted;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
