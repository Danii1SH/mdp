package com.example.mdp.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelService {

    public byte[] generateExcel(List<String> errors) throws IOException {

        Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            Sheet sheet = workbook.createSheet("Error Report");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);
            boldStyle.setAlignment(HorizontalAlignment.CENTER);

            Row headerRow = sheet.createRow(0);
            Cell headerCell0 = headerRow.createCell(0);
            headerCell0.setCellValue("#");
            headerCell0.setCellStyle(headerStyle);

            Cell headerCell1 = headerRow.createCell(1);
            headerCell1.setCellValue("Название дисциплины");
            headerCell1.setCellStyle(headerStyle);

            Cell headerCell2 = headerRow.createCell(2);
            headerCell2.setCellValue("Описание ошибки");
            headerCell2.setCellStyle(headerStyle);

            // Заполняем данные в таблице
            for (int i = 0; i < errors.size(); i++) {
                String error = errors.get(i).replace("[", "").replace("]", "");
                String[] parts = error.split(":", 2);

                String disciplineName = parts.length > 0 ? parts[0].trim() : "Неизвестно";
                String errorText = parts.length > 1 ? parts[1].trim() : "Без описания ошибки";

                Row row = sheet.createRow(i + 1);


                Cell numberCell = row.createCell(0);
                numberCell.setCellValue(i + 1);
                numberCell.setCellStyle(boldStyle);

                row.createCell(1).setCellValue(errorText); // Описание ошибки
                row.createCell(2).setCellValue(disciplineName); // Название дисциплины

            }
            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(outputStream);
        } finally {
            workbook.close();
        }
        return outputStream.toByteArray();
    }
}
