package com.example.mdp.service;

import com.google.api.services.docs.v1.Docs;
import com.google.api.services.docs.v1.DocsScopes;
import com.google.api.services.docs.v1.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
@Service
public class GoogleDocsService {

    private final Docs docsService;

    // ID вашего документа Google Docs
    private static final String DOCUMENT_ID = "1vASNSBK-bEW3WaVbjDbd_qBU2PKlTRw5PB198LummEQ"; // Замените на ваш ID документа
    private static final String APPLICATION_NAME = "mdp";

    public GoogleDocsService() throws IOException {
        GoogleCredentials credentials = getCredentials();
        this.docsService = getDocsService(credentials);
    }

    private GoogleCredentials getCredentials() throws IOException {
        FileInputStream serviceAccountStream = new FileInputStream("credentials.json");
        return GoogleCredentials.fromStream(serviceAccountStream)
                .createScoped(List.of(DocsScopes.DOCUMENTS));
    }

    private Docs getDocsService(GoogleCredentials credentials) {
        return new Docs.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                com.google.api.client.json.jackson2.JacksonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    // Метод для получения текста из конкретной ячейки (например, первой ячейки первой таблицы)
    public List<String> getTableCellsText(List<int[]> cellCoordinates) throws IOException {
        Document document = docsService.documents().get(DOCUMENT_ID).execute();
        List<StructuralElement> content = document.getBody().getContent();

        List<String> cellTexts = new ArrayList<>();

        // Проходим по каждой координате и получаем текст из ячеек
        for (int[] coords : cellCoordinates) {
            int tableIndex = coords[0];
            int rowIndex = coords[1];
            int cellIndex = coords[2];

            // Находим нужную таблицу
            Table table = findTableByIndex(content, tableIndex);
            if (table == null) {
                cellTexts.add("Table not found"); // Можно обработать как исключение или добавить пустое значение
                continue;
            }

            // Проверяем, что строка существует
            if (rowIndex >= table.getTableRows().size()) {
                cellTexts.add("Row index out of bounds");
                continue;
            }
            TableRow row = table.getTableRows().get(rowIndex);

            // Проверяем, что ячейка существует
            if (cellIndex >= row.getTableCells().size()) {
                cellTexts.add("Cell index out of bounds");
                continue;
            }
            TableCell cell = row.getTableCells().get(cellIndex);

            // Извлекаем текст из ячейки и добавляем в список
            cellTexts.add(getTextFromTableCell(cell));
        }
        return cellTexts; // Возвращаем список текстов для всех указанных ячеек
    }

    // Вспомогательный метод для поиска таблицы по индексу
    private Table findTableByIndex(List<StructuralElement> content, int tableIndex) {
        int currentTableIndex = -1;

        for (StructuralElement element : content) {
            if (element.getTable() != null) {
                currentTableIndex++;
                if (currentTableIndex == tableIndex) {
                    return element.getTable();
                }
            }
        }
        return null;
    }

    // Вспомогательный метод для извлечения текста из ячейки таблицы
    private String getTextFromTableCell(TableCell cell) {
        StringBuilder cellText = new StringBuilder();
        for (StructuralElement cellContent : cell.getContent()) {
            if (cellContent.getParagraph() != null) {
                cellContent.getParagraph().getElements().forEach(element ->
                        cellText.append(element.getTextRun().getContent()));
            }
        }
        return cellText.toString().trim();
    }
}
