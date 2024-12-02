package com.example.mdp.controller;

import com.example.mdp.enums.ControlForm;
import com.example.mdp.service.*;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.example.mdp.service.GoogleDriveService.ANNOTATION_FOLDER_ID;

@Data
@Controller
public class GoogleController {

    private final GoogleSheetsService googleSheetsService;
    private final GoogleDocsService googleDocsService;
    private final GoogleDriveService googleDriveService;

    private static final String SHEET_NAME = "План";
    private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    @GetMapping("/compare")
    public String compareData(Model model) {
        List<String> errors = Collections.synchronizedList(new ArrayList<>());
        List<String> documentIds;

        CompletableFuture<List<String>> documentIdsFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return googleDriveService.getDocumentsInFolder(ANNOTATION_FOLDER_ID);
            } catch (IOException e) {
                errors.add("Ошибка при получении документов: " + e.getMessage());
                return Collections.emptyList();
            }
        }, executor);

        CompletableFuture<Map<String, List<Object>>> sheetDataFuture = CompletableFuture.supplyAsync(() -> {
            try {
                String range = "A1:CE72";
                List<List<Object>> sheetData = googleSheetsService.getSheetData(SHEET_NAME, range);

                return sheetData.stream()
                        .filter(row -> row.size() > 2)
                        .collect(Collectors.toMap(
                                row -> normalizeString(row.get(2).toString()),
                                row -> row,
                                (existing, duplicate) -> existing
                        ));
            } catch (IOException e) {
                errors.add("Ошибка при получении данных из Google Sheets: " + e.getMessage());
                return new HashMap<>();
            }
        }, executor);

        // Ожидаем завершения всех асинхронных операций
        CompletableFuture.allOf(documentIdsFuture, sheetDataFuture).join();

        documentIds = documentIdsFuture.join();
        Map<String, List<Object>> sheetDataMap = sheetDataFuture.join();

        if (documentIds.isEmpty()) {
            errors.add("Нет документов в папке");
            model.addAttribute("errors", errors);
            return "compare";
        }

        List<CompletableFuture<Void>> documentTasks = documentIds.stream()
                .map(documentId -> CompletableFuture.runAsync(() -> {
                    try {
                        List<String> docValues = googleDocsService.getTableCellsText(documentId, getCoordinates());

                        if (docValues.size() < 5) {
                            errors.add("Недостаточно данных в документе: " + documentId);
                            return;
                        }

                        String docDiscipline = normalizeString(docValues.get(0));
                        String docCreditUnits = docValues.get(1);
                        String docHours = docValues.get(2);
                        String docCompetencyText = docValues.get(3);
                        String docControlText = docValues.get(4);

                        List<String> docCompetencies = CompetencyChecker.findCompetenciesInText(docCompetencyText);
                        List<Object> sheetRow = sheetDataMap.get(docDiscipline);

                        if (sheetRow != null) {
                            List<CompletableFuture<Void>> cellChecks = new ArrayList<>();
                            cellChecks.add(CompletableFuture.runAsync(() -> compareCreditUnits(docCreditUnits, sheetRow, docDiscipline, errors)));
                            cellChecks.add(CompletableFuture.runAsync(() -> compareHours(docHours, sheetRow, docDiscipline, errors)));
                            cellChecks.add(CompletableFuture.runAsync(() -> compareCompetencies(docCompetencies, sheetRow, docDiscipline, errors)));
                            cellChecks.add(CompletableFuture.runAsync(() -> compareControlForms(docControlText, sheetRow, docDiscipline, errors)));

                            CompletableFuture.allOf(cellChecks.toArray(new CompletableFuture[0])).join();
                        } else {
                            errors.add("Не найдена в Google Sheet или различие в написании: " + docDiscipline);
                        }

                    } catch (IOException e) {
                        errors.add("Ошибка при обработке документа " + documentId + ": " + e.getMessage());
                    }
                }, executor))
                .collect(Collectors.toList());

        CompletableFuture.allOf(documentTasks.toArray(new CompletableFuture[0])).join();

        model.addAttribute("errors", errors);
        return "compare";
    }

    private static String normalizeString(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        input = input.trim();
        return input.length() > 1
                ? input.substring(0, 1) + input.substring(1).toLowerCase()
                : input.toUpperCase();
    }

    private void compareCreditUnits(String docCreditUnits, List<Object> sheetRow, String docDiscipline, List<String> errors) {
        String sheetCreditUnits = sheetRow.size() > 9 ? sheetRow.get(9).toString() : "";
        if (!docCreditUnits.equals(sheetCreditUnits)) {
            errors.add("Несоответствие в зачетных единицах: " + docDiscipline);
        }
    }

    private void compareHours(String docHours, List<Object> sheetRow, String docDiscipline, List<String> errors) {
        String sheetHours = sheetRow.size() > 11 ? sheetRow.get(11).toString() : "";
        if (!docHours.equals(sheetHours)) {
            errors.add("Несоответствие в часах: " + docDiscipline);
        }
    }

    private void compareCompetencies(List<String> docCompetencies, List<Object> sheetRow, String docDiscipline, List<String> errors) {
        String sheetCompetencyText = sheetRow.size() > 82 ? sheetRow.get(82).toString() : "";
        List<String> sheetCompetencies = CompetencyChecker.findCompetenciesInText(sheetCompetencyText);

        if (!docCompetencies.containsAll(sheetCompetencies) || !sheetCompetencies.containsAll(docCompetencies)) {
            errors.add("Несоответствие в компетенциях: " + docDiscipline);
        }
    }

    private void compareControlForms(String docControlText, List<Object> sheetRow, String docDiscipline, List<String> errors) {
        Map<ControlForm, Boolean> sheetControlForms = getControlFormsFromSheet(sheetRow);
        Map<ControlForm, Boolean> docControlForms = getControlFormsFromDoc(docControlText);
        String controlFormsComparison = compareControlForms(sheetControlForms, docControlForms);

        if (!controlFormsComparison.isEmpty()) {
            errors.add("Несоответствие форм контроля: " + docDiscipline);
        }
    }

    private List<int[]> getCoordinates() {
        return List.of(
                new int[]{0, 1, 1},  // Наименование дисциплины
                new int[]{0, 5, 1},  // Зачетные единицы
                new int[]{0, 5, 2},  // Часы
                new int[]{0, 14, 0}, // Компетенции
                new int[]{0, 6, 1}   // Формы контроля
        );
    }

    private Map<ControlForm, Boolean> getControlFormsFromSheet(List<Object> row) {
        Map<ControlForm, Boolean> controlFormsMap = new HashMap<>();
        controlFormsMap.put(ControlForm.EXAM, row.size() > 3 && row.get(3) != null && !row.get(3).toString().isEmpty());
        controlFormsMap.put(ControlForm.ZACHET, row.size() > 4 && row.get(4) != null && !row.get(4).toString().isEmpty());
        controlFormsMap.put(ControlForm.KURS_PROJECT, row.size() > 6 && row.get(6) != null && !row.get(6).toString().isEmpty());
        controlFormsMap.put(ControlForm.KURS_WORK, row.size() > 7 && row.get(7) != null && !row.get(7).toString().isEmpty());
        controlFormsMap.put(ControlForm.KP, row.size() > 5 && row.get(5) != null && !row.get(5).toString().isEmpty());
        controlFormsMap.put(ControlForm.KR, row.size() > 7 && row.get(7) != null && !row.get(7).toString().isEmpty());
        return controlFormsMap;
    }

    private Map<ControlForm, Boolean> getControlFormsFromDoc(String docControlText) {
        Map<ControlForm, Boolean> controlFormsMap = new HashMap<>();
        for (ControlForm form : ControlForm.values()) {
            controlFormsMap.put(form, false);
        }
        String[] docForms = docControlText.split(",\\s*");
        for (String formText : docForms) {
            ControlForm form = ControlForm.fromString(formText);
            if (form != null) {
                controlFormsMap.put(form, true);
            }
        }
        return controlFormsMap;
    }

    private String compareControlForms(Map<ControlForm, Boolean> sheetMap, Map<ControlForm, Boolean> docMap) {
        StringBuilder result = new StringBuilder();
        for (ControlForm form : ControlForm.values()) {
            if (!sheetMap.getOrDefault(form, false).equals(docMap.getOrDefault(form, false))) {
                result.append(form.name()).append("; ");
            }
        }
        return result.toString().trim();
    }
}
