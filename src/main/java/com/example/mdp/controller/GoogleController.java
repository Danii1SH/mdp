package com.example.mdp.controller;

import com.example.mdp.service.GoogleDocsService;
import com.example.mdp.service.GoogleSheetsService;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.List;

@Data
@Controller
public class GoogleController {

    private final GoogleSheetsService googleSheetsService;
    private final GoogleDocsService googleDocsService;

    private static final String SHEET_NAME = "План";  // Название листа

    @GetMapping("/sheets")
    public String getTableData(Model model) throws IOException {
        String range = "A1:CE72";  // Диапазон данных
        List<List<Object>> data = googleSheetsService.getSheetData(SHEET_NAME, range);

        model.addAttribute("data", data);
        return "sheets";
    }

    @GetMapping("/docs")
    public void getDocData() throws IOException {
        List<int[]> cellCoordinates = List.of(
                new int[]{0, 1, 1}, // Наименование дисциплины
                new int[]{0, 5, 1}, // Зачетные единицы
                new int[]{0, 5, 2}, // Часы
                new int[]{0, 6, 1},  // Формы контроля
                new int[]{0, 12, 0}  // Формируемые компетенции
        );
        List<String> tableCellsText = googleDocsService.getTableCellsText(cellCoordinates);
        tableCellsText.set(4, tableCellsText.get(4).substring(0, 4));
        System.out.println(tableCellsText);
    }
}
