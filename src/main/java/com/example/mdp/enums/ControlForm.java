package com.example.mdp.enums;

public enum ControlForm {
    EXAM("экзамен"),
    ZACHET("зачёт"),
    KURS_PROJECT("курсовой проект"),
    KURS_WORK("курсовая работа"),
    KP("КП"),
    KR("КР");

    private final String docValue;

    ControlForm(String docValue) {
        this.docValue = docValue;
    }

    public String getDocValue() {
        return docValue;
    }

    // Метод для поиска соответствующего enum по строке
    public static ControlForm fromString(String text) {
        for (ControlForm form : ControlForm.values()) {
            if (form.getDocValue().equalsIgnoreCase(text.trim())) {
                return form;
            }
        }
        return null;
    }
}
