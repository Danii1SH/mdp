package com.example.mdp.controller;

import com.example.mdp.service.GoogleSheetsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.List;

@Controller
public class GoogleSheetsController {

    @Autowired
    private GoogleSheetsService googleSheetsService;

    private static final String SHEET_NAME = "План";  // Название листа

    @GetMapping("/home")
    public String getTableData(Model model) throws IOException {
        String range = "A1:E10";  // Диапазон данных
        List<List<Object>> data = googleSheetsService.getSheetData(SHEET_NAME, range);

        model.addAttribute("data", data);
        return "home";
    }
}
