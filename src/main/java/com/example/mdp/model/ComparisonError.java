package com.example.mdp.model;

import lombok.Data;

@Data
public class ComparisonError {
    private String disciplineName; // Название дисциплины
    private String errorText;      // Описание ошибки

    // Конструктор с параметрами
    public ComparisonError(String disciplineName, String errorText) {
        this.disciplineName = disciplineName;
        this.errorText = errorText;
    }
}