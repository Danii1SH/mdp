package com.example.mdp.controller;

import com.example.mdp.service.ExcelService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class ExcelController {

    private final ExcelService excelService;

    public ExcelController(ExcelService excelService) {
        this.excelService = excelService;
    }

    @PostMapping("/excel/download")
    public void downloadExcel(@RequestParam List<String> errors, HttpServletResponse response) throws IOException {

        byte[] excelContent = excelService.generateExcel(errors);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=error_report.xlsx");
        response.getOutputStream().write(excelContent);
    }
}
