package com.example.mdp.controller;

import com.example.mdp.service.GoogleSheetsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.List;

@Controller
@AllArgsConstructor
public class SheetController {

    private final GoogleSheetsService googleSheetsService;

    @GetMapping("/")
    public String displayPlanData(Model model) {
        try {
            // Получаем данные из Google Sheets
            List<List<Object>> sheetData = googleSheetsService.getSheetData("План", "A1:CE72");

            // Передаем данные в модель для отображения на странице
            model.addAttribute("data", sheetData);
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Ошибка при загрузке данных из Google Sheets: " + e.getMessage());
        }
        return "plan";
    }

    @GetMapping("/competency")
    public String displayCompetencyData(Model model) {
        try {
            // Получаем данные из Google Sheets
            List<List<Object>> sheetData = googleSheetsService.getSheetData("Компетенции(2)", "A1:F70");

            // Передаем данные в модель для отображения на странице
            model.addAttribute("data", sheetData);
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Ошибка при загрузке данных из Google Sheets: " + e.getMessage());
        }
        return "competency";
    }
}
