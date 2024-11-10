package com.example.mdp.service;

import com.example.mdp.model.ComparisonError;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PDFService {

    public byte[] generatePdf(List<ComparisonError> errors) throws IOException {
        // Создание нового документа PDF
        Document document = new Document();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            // Попытка создать PdfWriter
            PdfWriter.getInstance(document, outputStream);

            // Открываем документ для записи
            document.open();

            // Добавляем заголовок
            document.add(new Paragraph("Результаты сравнения"));

            PdfPTable table = new PdfPTable(3);

            // Добавляем заголовки таблицы
            table.addCell("№");
            table.addCell("Название дисциплины");
            table.addCell("Текст ошибок");

            // Заполняем таблицу данными из списка ошибок
            for (int i = 0; i < errors.size(); i++) {
                ComparisonError error = errors.get(i);
                table.addCell(String.valueOf(i + 1));  // Номер ошибки
                table.addCell(error.getDisciplineName());  // Название дисциплины
                table.addCell(error.getErrorText());  // Описание ошибки
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
