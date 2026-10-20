package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.List;

/** Розбирає CSV-текст за правилами екранування лапками. */
public final class CsvParser {

    private CsvParser() {
    }

    /**
     * Розбирає CSV-документ, зокрема поля з комами, лапками та переносами рядків у лапках.
     * Повертає список записів, а не фізичних рядків файлу.
     *
     * @param text текст документа
     * @return записи, кожен — список полів
     * @throws DataStorageException якщо лапки не закриті або стоять у неправильному місці
     */
    public static List<List<String>> parseDocument(String text) throws DataStorageException {
        if (text == null) {
            throw new DataStorageException("CSV-текст не може бути null");
        }
        List<List<String>> records = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean afterQuote = false;
        boolean fieldStarted = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (quoted) {
                if (c == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        quoted = false;
                        afterQuote = true;
                    }
                } else {
                    field.append(c);
                }
                continue;
            }
            if (c == '"') {
                if (fieldStarted || afterQuote) {
                    throw new DataStorageException("Лапка всередині незакритого поля, символ " + i);
                }
                quoted = true;
                fieldStarted = true;
            } else if (c == ',') {
                row.add(field.toString());
                field.setLength(0);
                fieldStarted = false;
                afterQuote = false;
            } else if (c == '\n' || c == '\r') {
                if (c == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                }
                row.add(field.toString());
                records.add(row);
                row = new ArrayList<>();
                field.setLength(0);
                fieldStarted = false;
                afterQuote = false;
            } else {
                if (afterQuote) {
                    throw new DataStorageException("Символ після закритої лапки, символ " + i);
                }
                field.append(c);
                fieldStarted = true;
            }
        }
        if (quoted) {
            throw new DataStorageException("Не закрито лапку наприкінці CSV");
        }
        if (fieldStarted || afterQuote || !row.isEmpty()) {
            row.add(field.toString());
            records.add(row);
        }
        return records;
    }
}
