package com.example.mdp.controller;

import com.example.mdp.enums.ControlForm;
import com.example.mdp.model.ComparisonError;
import com.example.mdp.service.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static com.example.mdp.service.GoogleDriveService.ANNOTATION_FOLDER_ID;

@Data
@Controller
public class GoogleController {

    private final PDFService pdfService;
    private final GoogleSheetsService googleSheetsService;
    private final GoogleDocsService googleDocsService;
    private final GoogleDriveService googleDriveService;

    private static final String SHEET_NAME = "План";

    @GetMapping("/compare")
    public String compareData(Model model) throws IOException {
        List<ComparisonError> errors = new ArrayList<>();
        List<String> documentIds;

        try {
            documentIds = googleDriveService.getDocumentsInFolder(ANNOTATION_FOLDER_ID);
        } catch (IOException e) {
            errors.add(new ComparisonError("Ошибка", "Ошибка при получении документов: " + e.getMessage()));
            model.addAttribute("errors", errors);
            return "compare";
        }

        if (documentIds.isEmpty()) {
            errors.add(new ComparisonError("Ошибка", "Нет документов в папке."));
            model.addAttribute("errors", errors);
            return "compare";
        }

        String range = "A1:CE72";
        List<List<Object>> sheetData;

        try {
            sheetData = googleSheetsService.getSheetData(SHEET_NAME, range);
        } catch (IOException e) {
            errors.add(new ComparisonError("Ошибка", "Ошибка при получении данных из Google Sheets: " + e.getMessage()));
            model.addAttribute("errors", errors);
            return "compare";
        }

        Map<String, List<Object>> sheetDataMap = new HashMap<>();

        for (List<Object> row : sheetData) {
            if (row.size() > 2) {
                sheetDataMap.put(row.get(2).toString(), row);
            }
        }

        List<int[]> docCoordinates = List.of(
                new int[]{0, 1, 1},  // Наименование дисциплины
                new int[]{0, 5, 1},  // Зачетные единицы
                new int[]{0, 5, 2},  // Часы
                new int[]{0, 12, 0}, // Компетенции
                new int[]{0, 6, 1}   // Формы контроля
        );

        for (String documentId : documentIds) {
            List<String> docValues;
            try {
                docValues = googleDocsService.getTableCellsText(documentId, docCoordinates);
            } catch (IOException e) {
                errors.add(new ComparisonError("Документ " + documentId, "Ошибка при получении данных из документа: " + e.getMessage()));
                continue;
            }

            if (docValues.size() < 5) {
                errors.add(new ComparisonError("Документ " + documentId, "Недостаточно данных в документе."));
                continue;
            }

            String docDiscipline = docValues.get(0);
            String docCreditUnits = docValues.get(1);
            String docHours = docValues.get(2);
            String docCompetencyText = docValues.get(3);
            String docControlText = docValues.get(4);

            List<String> docCompetencies = CompetencyChecker.findCompetenciesInText(docCompetencyText);
            List<Object> sheetRow = sheetDataMap.get(docDiscipline);

            if (sheetRow != null) {
                String sheetCreditUnits = sheetRow.size() > 9 ? sheetRow.get(9).toString() : "";
                String sheetHours = sheetRow.size() > 11 ? sheetRow.get(11).toString() : "";
                String sheetCompetencyText = sheetRow.size() > 82 ? sheetRow.get(82).toString() : "";

                List<String> sheetCompetencies = CompetencyChecker.findCompetenciesInText(sheetCompetencyText);

                if (!docCreditUnits.equals(sheetCreditUnits)) {
                    errors.add(new ComparisonError(docDiscipline, "Несоответствие в зачетных единицах."));
                }

                if (!docHours.equals(sheetHours)) {
                    errors.add(new ComparisonError(docDiscipline, "Несоответствие в часах."));
                }

                if (!docCompetencies.containsAll(sheetCompetencies) || !sheetCompetencies.containsAll(docCompetencies)) {
                    errors.add(new ComparisonError(docDiscipline, "Несоответствие в компетенциях."));
                }

                Map<ControlForm, Boolean> sheetControlForms = getControlFormsFromSheet(sheetRow);
                Map<ControlForm, Boolean> docControlForms = getControlFormsFromDoc(docControlText);
                String controlFormsComparison = compareControlForms(sheetControlForms, docControlForms);

                if (!controlFormsComparison.isEmpty()) {
                    errors.add(new ComparisonError(docDiscipline, "Несоответствие форм контроля: " + controlFormsComparison));
                }
            } else {
                errors.add(new ComparisonError(docDiscipline, "Дисциплина не найдена в Google Sheets."));
            }
        }

        model.addAttribute("errors", errors);
        return "compare";
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

    @PostMapping("/pdf/download")
    public void downloadPdf(@RequestParam List<ComparisonError> errors, HttpServletResponse response) throws IOException {
        // Генерация PDF
        byte[] pdfContent = pdfService.generatePdf(errors);

        // Устанавливаем заголовки для скачивания PDF
        response.setContentType("application/pdf");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=error_report.pdf");
        response.getOutputStream().write(pdfContent);
    }
}
