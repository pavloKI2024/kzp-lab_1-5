package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void validRecordsAreCalculated() {
        String report = Main.buildReport(List.of(
                "101;Іваненко;3;1200.50;Standard",
                "102;Коваль;7;2000.00;Lux"));
        assertTrue(report.contains("Коректних записів: 2"));
        assertTrue(report.contains("Сумарна кількість ночей: 10"));
        assertTrue(report.contains("Загальний виторг: 17601.50"));
        assertTrue(report.contains("Найдовше проживання: 7 ночей (Коваль)"));
    }

    @Test
    void wrongFieldCountIsSkippedWithLineNumber() {
        String report = Main.buildReport(List.of("101;Іваненко;3;1200.50;Standard", "bad"));
        assertTrue(report.contains("Рядок 2 пропущено: неправильна кількість полів"));
        assertTrue(report.contains("Коректних записів: 1"));
    }

    @Test
    void blankLineIsSkipped() {
        String report = Main.buildReport(List.of(""));
        assertTrue(report.contains("Рядок 1 пропущено: порожній рядок"));
    }

    @Test
    void emptyTextFieldIsSkipped() {
        String report = Main.buildReport(List.of("101;;3;1200.50;Standard"));
        assertTrue(report.contains("порожнє текстове поле"));
    }

    @Test
    void nonNumericValueIsSkipped() {
        String report = Main.buildReport(List.of("101;Іваненко;abc;1200.50;Standard"));
        assertTrue(report.contains("нечислове значення"));
    }

    @Test
    void negativeValueIsSkipped() {
        String report = Main.buildReport(List.of("101;Іваненко;-2;1200.50;Standard"));
        assertTrue(report.contains("недопустиме числове значення"));
    }

    @Test
    void noValidRecordsIsReported() {
        String report = Main.buildReport(List.of("bad"));
        assertTrue(report.contains("Коректних записів: 0"));
        assertTrue(report.contains("Коректних записів немає"));
    }
}
