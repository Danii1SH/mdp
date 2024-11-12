package com.example.mdp.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Data
@Service
public class GoogleDriveService {

    private final Drive driveService;
    private static final String APPLICATION_NAME = "mdp";
    public static final String ANNOTATION_FOLDER_ID = "1WC-bGmLWhEYM74ev9UvihcVf08ffgcn4";


    public GoogleDriveService() throws IOException, GeneralSecurityException {
        GoogleCredentials credentials = getCredentials();
        this.driveService = getDriveService(credentials);
    }

    private GoogleCredentials getCredentials() throws IOException {
        FileInputStream serviceAccountStream = new FileInputStream("credentials.json");
        return GoogleCredentials.fromStream(serviceAccountStream)
                .createScoped(List.of("https://www.googleapis.com/auth/drive.readonly"));
    }

    private Drive getDriveService(GoogleCredentials credentials) {
        return new Drive.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public List<String> getDocumentsInFolder(String folderId) throws IOException {
        List<String> documentIds = new ArrayList<>();
        FileList result = driveService.files().list()
                .setQ("'" + folderId + "' in parents and mimeType = 'application/vnd.google-apps.document'")
                .setFields("files(id, name)")
                .execute();

        for (File file : result.getFiles()) {
            documentIds.add(file.getId());
        }

        return documentIds;
    }
}
