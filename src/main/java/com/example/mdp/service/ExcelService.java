package com.example.mdp.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PDFService {

    public byte[] generatePdf(List<String> errors) throws IOException {
        // Создание нового документа PDF
        Document document = new Document();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            // Попытка создать PdfWriter
            PdfWriter.getInstance(document, outputStream);

            // Открываем документ для записи
            document.open();

            // Используем шрифт Times New Roman
            BaseFont baseFont = BaseFont.createFont("resources/Fonts/times.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font font = new Font(baseFont, 12);

            // Добавляем заголовок с правильным шрифтом
            document.add(new Paragraph("Результаты сравнения", font));

            PdfPTable table = new PdfPTable(3);

            // Добавляем заголовки таблицы с правильным шрифтом
            table.addCell(new Phrase("#", font));
            table.addCell(new Phrase("Название дисциплины", font));
            table.addCell(new Phrase("Описание ошибки", font));

            // Заполняем таблицу данными из списка ошибок
            for (int i = 0; i < errors.size(); i++) {
                String error = errors.get(i);
                String[] parts = error.split(":", 2);

                String disciplineName = parts.length > 0 ? parts[0].trim() : "Неизвестно";
                String errorText = parts.length > 1 ? parts[1].trim() : "Без описания ошибки";

                table.addCell(new Phrase(String.valueOf(i + 1), font));  // Номер ошибки
                table.addCell(new Phrase(disciplineName, font));  // Название дисциплины
                table.addCell(new Phrase(errorText, font));  // Описание ошибки
            }

            // Добавляем таблицу в документ
            document.add(table);
        } catch (DocumentException e) {
            e.printStackTrace();  // Обрабатываем исключение DocumentException
            throw new IOException("Ошибка при генерации PDF: " + e.getMessage(), e);
        } finally {
            // Закрываем документ в любом случае
            document.close();
        }

        // Возвращаем сгенерированный PDF как массив байтов
        return outputStream.toByteArray();
    }

}
