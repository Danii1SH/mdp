package com.example.mdp.service;

import java.util.ArrayList;
import java.util.List;

public class CompetencyChecker {

    // Список компетенций
    private static final List<String> COMPETENCIES = List.of(
            "КС-1", "УК-1", "УК-2", "УК-3", "УК-4", "УК-5", "УК-6", "УК-7", "УК-8", "УК-9", "УК-10",
            "ОПК-1", "ОПК-2", "ОПК-3", "ОПК-4", "ОПК-5", "ОПК-6", "ОПК-7", "ОПК-8",
            "ПК-1", "ПК-2", "ПК-3", "ПК-4", "ПКс-3"
    );

    // Метод для поиска компетенций в тексте
    public static List<String> findCompetenciesInText(String text) {
        List<String> foundCompetencies = new ArrayList<>();
        for (String competency : COMPETENCIES) {
            if (text.contains(competency)) {
                foundCompetencies.add(competency);
            }
        }
        return foundCompetencies;
    }
}
