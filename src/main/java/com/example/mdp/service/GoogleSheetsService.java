package com.example.mdp.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Service
public class GoogleSheetsService {

    private Sheets sheetsService;
    private Drive driveService;

    private static final String SPREADSHEET_ID = "15Vd7Lb6JJOkFyLlsR7nun2c4CYRRRG06";
    private static final String APPLICATION_NAME = "mdp";

    public GoogleSheetsService() throws IOException, GeneralSecurityException {
        GoogleCredentials credentials = getCredentials();
        this.sheetsService = getSheetsService(credentials);
        this.driveService = getDriveService(credentials);
    }

    private GoogleCredentials getCredentials() throws IOException {
        FileInputStream serviceAccountStream = new FileInputStream("credentials.json");
        return GoogleCredentials.fromStream(serviceAccountStream)
                .createScoped(List.of(SheetsScopes.SPREADSHEETS, DriveScopes.DRIVE));
    }

    private Sheets getSheetsService(GoogleCredentials credentials) {
        return new Sheets.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Drive getDriveService(GoogleCredentials credentials) {
        return new Drive.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    // Метод для получения данных из конкретного листа и диапазона
    public List<List<Object>> getSheetData(String sheetName, String range) throws IOException {
        String fullRange = sheetName + "!" + range;
        ValueRange response = sheetsService.spreadsheets().values()
                .get(SPREADSHEET_ID, fullRange)
                .execute();
        return response.getValues();
    }
}
